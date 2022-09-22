package in.apcfss.olcms;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import plugins.DatabasePlugin;


@Path("/hcCaseStatus")
public class HCCaseStatusAbstractReport {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getCategoryWiseList")
	public static Response getCaseCategoryReport(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_CATEGORY") || jObject.get("CASE_CATEGORY").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_CATEGORY is missing in the request.\" }}";
					}
					else {
						String caseCategory=jObject.get("CASE_CATEGORY").toString();
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						
						if (caseCategory != null && !caseCategory.equals("")) {
							if (caseCategory.equals("DISPOSED")) {
								sqlCondition += " and (disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS')";
							} else if (caseCategory.equals("ALLOWED")) {
								sqlCondition += " and (disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS')";
							} else if (caseCategory.equals("DISMISSED")) {
								sqlCondition += " and (disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED' )";
							} else if (caseCategory.equals("WITHDRAWN")) {
								sqlCondition += " and (disposal_type='WITHDRAWN')";
							} else if (caseCategory.equals("CLOSED")) {
								sqlCondition += " and (disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED')";
							} else if (caseCategory.equals("RETURNED")) {
								sqlCondition += " and (disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED')";
							}
						}

						String condition="";
						if (roleId.equals("6") )
							condition= " inner join ecourts_mst_gp_dept_map b on a.dept_code=b.dept_code ";

						sql = "select disposal_type, count(*) as casescount from ecourts_case_data a "+condition+" where 1=1 " + sqlCondition;

						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and a.dept_code='" + dept_code + "' ";


						sql += " group by disposal_type";

						System.out.println("roleId--"+roleId);

						System.out.println("DISPWISE SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						JSONArray finalList = new JSONArray();
						// System.out.println("data=" + data);
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {		
							    
								JSONObject cases = new JSONObject();
						    	cases.put("DISPOSAL_TYPE", entry.get("disposal_type").toString());
						    	cases.put("CASES_COUNT", entry.get("casescount").toString());
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SELECTED_CATEGORY", caseCategory);
							casesData.put("DISPOSAL_TYPES_LIST", finalList);						
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SELECTED_CATEGORY", caseCategory);
								casesData.put("DISPOSAL_TYPES_LIST", finalList);							
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getDeptWiseCasesList")
	public static Response getDeptWiseCasesList(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_CATEGORY") || jObject.get("CASE_CATEGORY").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_CATEGORY is missing in the request.\" }}";
					}
					else {
						String caseCategory=jObject.get("CASE_CATEGORY").toString();
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						
						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}

						
						if (caseCategory != null && !caseCategory.equals("")) {

							if (caseCategory.equals("DISPOSED OF NO COSTS")) {
								sqlCondition += " and (disposal_type='DISPOSED OF NO COSTS')";
							} else if (caseCategory.equals("DISPOSED OF AS INFRUCTUOUS")) {
								sqlCondition += " and (disposal_type='DISPOSED OF AS INFRUCTUOUS')";
							} 
							else if (caseCategory.equals("ALLOWED NO COSTS")) {
								sqlCondition += " and (disposal_type='ALLOWED NO COSTS')";
							} 
							else if (caseCategory.equals("PARTLY ALLOWED NO COSTS")) {
								sqlCondition += " and (disposal_type='PARTLY ALLOWED NO COSTS')";
							} 
							else if (caseCategory.equals("DISMISSED")) {
								sqlCondition += " and (disposal_type='DISMISSED')";
							} 
							else if (caseCategory.equals("DISMISSED AS INFRUCTUOUS")) {
								sqlCondition += " and (disposal_type='DISMISSED AS INFRUCTUOUS')";
							} 
							else if (caseCategory.equals("DISMISSED NO COSTS")) {
								sqlCondition += " and (disposal_type='DISMISSED NO COSTS')";
							} 
							else if (caseCategory.equals("DISMISSED FOR DEFAULT")) {
								sqlCondition += " and (disposal_type='DISMISSED FOR DEFAULT')";
							} 
							else if (caseCategory.equals("DISMISSED AS NON PROSECUTION")) {
								sqlCondition += " and (disposal_type='DISMISSED AS NON PROSECUTION')";
							} 
							else if (caseCategory.equals("DISMISSED AS ABATED")) {
								sqlCondition += " and (disposal_type='DISMISSED AS ABATED')";
							} 
							else if (caseCategory.equals("DISMISSED AS NOT PRESSED")) {
								sqlCondition += " and (disposal_type='DISMISSED AS NOT PRESSED')";
							} 
							else if (caseCategory.equals("WITHDRAWN")) {
								sqlCondition += " and (disposal_type='WITHDRAWN')";
							} 
							else if (caseCategory.equals("CLOSED NO COSTS")) {
								sqlCondition += " and (disposal_type='CLOSED NO COSTS')";
							}
							else if (caseCategory.equals("CLOSED AS NOT PRESSED")) {
								sqlCondition += " and (disposal_type='CLOSED AS NOT PRESSED')";
							}

							else if (caseCategory.equals("REJECTED")) {
								sqlCondition += " and (disposal_type='REJECTED' )";
							}

							else if (caseCategory.equals("ORDERED")) {
								sqlCondition += " and ( disposal_type='ORDERED' )";
							}
							else if (caseCategory.equals("RETURN TO COUNSEL")) {
								sqlCondition += " and (disposal_type='RETURN TO COUNSEL')";
							}

							else if (caseCategory.equals("TRANSFERRED")) {
								sqlCondition += " and (disposal_type='TRANSFERRED')";
							}
						}

						String condition="";
						
						if (roleId.equals("6") )
							condition= " inner join ecourts_mst_gp_dept_map c on a.dept_code=c.dept_code ";

						//sql = "select disposal_type, count(*) as casescount from ecourts_case_data a where 1=1 " + sqlCondition;
						sql="select disposal_type,b.dept_code,b.description,count(*) as casescount from ecourts_case_data a "+condition+" inner join dept_new b on a.dept_code=b.dept_code   where 1=1 " + sqlCondition;


						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and a.dept_code='" + dept_code + "' ";

						sql += " group by disposal_type,b.description,b.dept_code";

						System.out.println("DISPWISE SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						
						
						JSONArray finalList = new JSONArray();
						int total=0;
						// System.out.println("data=" + data);
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {		
							    total = total + Integer.parseInt(entry.get("casescount").toString());
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("CASES_COUNT", entry.get("casescount").toString());
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SELECTED_CATEGORY", caseCategory);
							casesData.put("DEPT_LIST", finalList);
							casesData.put("TOTAL_CASES_COUNT", String.valueOf(total));
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SELECTED_CATEGORY", caseCategory);
								casesData.put("DEPT_LIST", finalList);	
								casesData.put("TOTAL_CASES-COUNT", String.valueOf(total));
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_CATEGORY") || jObject.get("CASE_CATEGORY").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_CATEGORY is missing in the request.\" }}";
					}
					else if(!jObject.has("DEPT_NAME") || jObject.get("DEPT_NAME").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_NAME is missing in the request.\" }}";
					}
					else {
						String caseCategory=jObject.get("CASE_CATEGORY").toString();
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String dept_name = jObject.get("DEPT_NAME").toString();
						
						
						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						

						System.out.println("roleId--"+roleId);

						String condition="";
						if (roleId.equals("6"))
							condition= " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) ";

						
						if (caseCategory != null && !caseCategory.equals("")) {							
							sqlCondition += " and trim(disposal_type)='" + caseCategory.trim() + "'  and trim(d.description)='"+dept_name.trim()+"'  ";
						}


						 sql = "select a.*, "
								+ ""
								+ ""
								+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a "
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
								+ " left join"
								+ " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) "+condition+" where d.display = true ";


						sql += sqlCondition;

						System.out.println("ecourts SQL:" + sql);						
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
						
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing").toString());
						    	cases.put("CASE_REG_NO", entry.get("type_name_fil") + "/" + entry.get("reg_no") + "/" + entry.get("reg_year"));
						    	cases.put("PRAYER", entry.get("prayer"));
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_OF_NEXT_LISTING", entry.get("date_next_list").toString());
						    	cases.put("BENCH_NAME", entry.get("bench_name").toString());
						    	cases.put("JUDGE_NAME", "Hon'ble Judge : "+entry.get("coram").toString());
						    	cases.put("PETITIONER_NAME", entry.get("pet_name").toString());
						    	cases.put("DISTRICT_NAME", entry.get("dist_name").toString());
						    	cases.put("PURPOSE", entry.get("purpose_name").toString());
						    	cases.put("RESPONDENTS", entry.get("res_name").toString() + "," + entry.get("address").toString() );
						    	cases.put("PETITIONER_ADVOCATE_NAME", entry.get("pet_adv").toString());
						    	cases.put("RESPONDENT_ADVOCATE_NAME", entry.get("res_adv").toString());
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SELECTED_CATEGORY", caseCategory);
							casesData.put("SELECTED_DEPT", dept_name);
							casesData.put("DEPT_LIST", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SELECTED_CATEGORY", caseCategory);
								casesData.put("SELECTED_DEPT", dept_name);
								casesData.put("DEPT_LIST", finalList);	
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
		
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getDashboardNewCasesReport")
	public static Response getDashboardNewCasesReport(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
												
						if(roleId.equals("1") || roleId.equals("7")) {
							return getSectDeptWiseNewCases(incomingData);
						}
						else if(roleId.equals("3") || roleId.equals("4")  || roleId.equals("5") || roleId.equals("9")) {
							return getHODDeptWiseNewCases(incomingData);
						}
						else if(roleId.equals("2")) {
							sql="select a.dept_code as deptcode , upper(d.description) as description,count(*) as total_cases, "
									+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
									+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
									+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
									+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
									+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
									+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
									+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
									+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
									+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
									+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
									+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
									+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
									+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
									+ "from ecourts_gpo_ack_depts  a "
									+ " inner join ecourts_gpo_ack_dtls b on (a.ack_no=b.ack_no) inner join dept_new d on (a.dept_code=d.dept_code)"
									+ " where b.ack_type='NEW'  and respondent_slno=1 and a.dist_id='"+dist_id+"'  " 
									+ "group by a.dept_code , d.description order by 1";
							
							con = DatabasePlugin.connect();
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
							JSONArray finalList = new JSONArray();
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
							    	cases.put("DEPT_NAME", entry.get("description").toString());
							    	cases.put("TOTAL_CASES", entry.get("total_cases"));
							    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
							    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
							    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
							    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
							    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
							    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
							    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
							    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
							    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
							    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
							    	cases.put("CLOSED", entry.get("closedcases").toString());
							    	cases.put("GOI", entry.get("goi").toString());
							    	cases.put("PSU", entry.get("psu").toString());
							    	cases.put("PRIVATE", entry.get("privatetot").toString());
							    	finalList.put(cases);
								}
								JSONObject casesData = new JSONObject();
								casesData.put("HOD_DEPT_WISE_NEW_DATA", finalList);
								String finalString = casesData.toString();
								    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
															
								} else {
									JSONObject casesData = new JSONObject();
									casesData.put("HOD_DEPT_WISE_NEW_DATA", finalList);	
									String finalString = casesData.toString();
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
								}		
							
						}
						else {
								JSONArray finalList = new JSONArray();
								JSONObject casesData = new JSONObject();
								casesData.put("SECT_DEPT_WISE_NEW_DATA", finalList);
								casesData.put("HOD_DEPT_WISE_NEW_DATA", finalList);
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";						
						}
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getDashboardLegacyCasesReport")
	public static Response getDashboardLegacyCasesReport(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
												
						if(roleId.equals("1") || roleId.equals("7")) {
							return getSectDeptWiseLegacyCases(incomingData);
						}
						else if(roleId.equals("3") || roleId.equals("4")  || roleId.equals("5") || roleId.equals("9") ) {
							return getHODDeptWiseLegacyCases(incomingData);
						}
						else if(roleId.equals("2")) {
							sql="select a.dept_code as deptcode , upper(d.description) as description,count(*) as total_cases, "
									+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
									+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
									+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
									+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
									+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
									+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
									+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
									+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
									+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
									+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
									+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
									+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
									+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
									+ "from ecourts_case_data a "
									+ "inner join dept_new d on (a.dept_code=d.dept_code) "
									+ "where d.display = true and a.dist_id='"+dist_id+"' "
									+ "group by a.dept_code , d.description order by 1";
							
							con = DatabasePlugin.connect();
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
							JSONArray finalList = new JSONArray();
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
							    	cases.put("DEPT_NAME", entry.get("description").toString());
							    	cases.put("TOTAL_CASES", entry.get("total_cases"));
							    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
							    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
							    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
							    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
							    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
							    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
							    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
							    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
							    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
							    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
							    	cases.put("CLOSED", entry.get("closedcases").toString());
							    	cases.put("GOI", entry.get("goi").toString());
							    	cases.put("PSU", entry.get("psu").toString());
							    	cases.put("PRIVATE", entry.get("privatetot").toString());
							    	finalList.put(cases);
								}
								JSONObject casesData = new JSONObject();
								casesData.put("HOD_DEPT_WISE_LEGACY_DATA", finalList);
								String finalString = casesData.toString();
								    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
															
								} else {
									JSONObject casesData = new JSONObject();
									casesData.put("HOD_DEPT_WISE_LEGACY_DATA", finalList);	
									String finalString = casesData.toString();
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
								}		
							
						}
						else if (roleId.equals("6")) {
							// LEGACY CASES DATA
							sql="select x.reporting_dept_code as deptcode, upper(d1.description) as description,sum(total_cases) as total_cases,sum(withsectdept) as withsectdept,sum(withmlo) as withmlo,sum(withhod) as withhod,sum(withnodal) as withnodal,sum(withsection) as withsection, sum(withdc) as withdc, sum(withdistno) as withdistno,sum(withsectionhod) as withsectionhod, sum(withsectiondist) as withsectiondist, sum(withgpo) as withgpo, sum(closedcases) as closedcases, sum(goi) as goi, sum(psu) as psu, sum(privatetot) as privatetot   from ("
									+ "select a.dept_code , case when reporting_dept_code='CAB01' then d.dept_code else reporting_dept_code end as reporting_dept_code,count(*) as total_cases, "
									+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
									+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
									+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
									+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
									+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
									+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
									+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
									+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
									+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
									+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
									+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
									+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
									+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
									+ "from ecourts_case_data a "
									+ "inner join dept_new d on (a.dept_code=d.dept_code) "
									+ " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) "
									+ "where d.display = true  and e.gp_id='"+user_id+"' ";
							
								sql+= "group by a.dept_code,d.dept_code ,reporting_dept_code ) x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code)"
									+ "group by x.reporting_dept_code, d1.description order by 1";
								con = DatabasePlugin.connect();
								List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
								JSONArray finalList = new JSONArray();
								
								if (data != null && !data.isEmpty() && data.size() > 0) {
									
									for (Map<String, Object> entry : data) {								   
										JSONObject cases = new JSONObject();
								    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
								    	cases.put("DEPT_NAME", entry.get("description").toString());
								    	cases.put("TOTAL_CASES", entry.get("total_cases"));
								    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
								    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
								    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
								    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
								    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
								    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
								    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
								    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
								    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
								    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
								    	cases.put("CLOSED", entry.get("closedcases").toString());
								    	cases.put("GOI", entry.get("goi").toString());
								    	cases.put("PSU", entry.get("psu").toString());
								    	cases.put("PRIVATE", entry.get("privatetot").toString());
								    	finalList.put(cases);
									}
									JSONObject casesData = new JSONObject();
									casesData.put("SECT_DEPT_WISE_LEGACY_DATA", finalList);
									String finalString = casesData.toString();
									    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
																
									} else {
										JSONObject casesData = new JSONObject();
										casesData.put("SECT_DEPT_WISE_LEGACY_DATA", finalList);	
										String finalString = casesData.toString();
										jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
									}		
								
								
						}
						else {
								JSONArray finalList = new JSONArray();
								JSONObject casesData = new JSONObject();
								casesData.put("SECT_DEPT_WISE_LEGACY_DATA", finalList);
								casesData.put("HOD_DEPT_WISE_LEGACY_DATA", finalList);
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";						
						}
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getSectDeptWiseNewCases")
	public static Response getSectDeptWiseNewCases(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
												
						if (roleId.equals("5") || roleId.equals("9")) {

							return getHODDeptWiseNewCases(incomingData);
						}
						
						sql = "select x.reporting_dept_code as deptcode, upper(d1.description) as description,sum(total_cases) as total_cases,sum(withsectdept) as withsectdept,sum(withmlo) as withmlo,sum(withhod) as withhod,sum(withnodal) as withnodal,sum(withsection) as withsection, sum(withdc) as withdc, sum(withdistno) as withdistno,sum(withsectionhod) as withsectionhod, sum(withsectiondist) as withsectiondist, sum(withgpo) as withgpo, sum(closedcases) as closedcases, sum(goi) as goi, sum(psu) as psu, sum(privatetot) as privatetot  from ("
								+ "select a.dept_code , case when reporting_dept_code='CAB01' then d.dept_code else reporting_dept_code end as reporting_dept_code,count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_gpo_ack_depts  a "
								+ " inner join ecourts_gpo_ack_dtls b on (a.ack_no=b.ack_no) inner join dept_new d on (a.dept_code=d.dept_code)"
								+ " where b.ack_type='NEW'  and respondent_slno=1  " + sqlCondition;

						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
									+ dept_code + "')";

						if (roleId.equals("2")) {
							sql += " and a.dist_id='" + dist_id + "' ";
						}

						sql += " group by a.dept_code,d.dept_code ,reporting_dept_code ) x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code)"
								+ " group by x.reporting_dept_code, d1.description order by 1";

						
						System.out.println("SECT DEPT WISE NEW DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("SECT DEPT WISE NEW DATA=" + data);
						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SECT_DEPT_WISE_NEW_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SECT_DEPT_WISE_NEW_DATA", finalList);	
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getSectDeptWiseLegacyCases")
	public static Response getSectDeptWiseLegacyCases(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
												
						if (roleId.equals("5") || roleId.equals("9")) {

							return getHODDeptWiseLegacyCases(incomingData);
						}
						
						sql = "select x.reporting_dept_code as deptcode, upper(d1.description) as description,sum(total_cases) as total_cases,sum(withsectdept) as withsectdept,sum(withmlo) as withmlo,sum(withhod) as withhod,sum(withnodal) as withnodal,sum(withsection) as withsection, sum(withdc) as withdc, sum(withdistno) as withdistno,sum(withsectionhod) as withsectionhod, sum(withsectiondist) as withsectiondist, sum(withgpo) as withgpo, sum(closedcases) as closedcases, sum(goi) as goi, sum(psu) as psu, sum(privatetot) as privatetot  from ("
								+ "select a.dept_code , case when reporting_dept_code='CAB01' then d.dept_code else reporting_dept_code end as reporting_dept_code,count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_case_data a " + "inner join dept_new d on (a.dept_code=d.dept_code) "
								+ "where d.display = true " + sqlCondition;

						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
									+ dept_code + "')";

						if (roleId.equals("2")) {
							sql += " and a.dist_id='" + dist_id + "' ";
						}

						sql += " group by a.dept_code,d.dept_code ,reporting_dept_code ) x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code)"
								+ " group by x.reporting_dept_code, d1.description order by 1";

						
						System.out.println("SECT DEPT WISE LEGACY DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("SECT DEPT WISE LEGACY DATA=" + data);
						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SECT_DEPT_WISE_LEGACY_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SECT_DEPT_WISE_LEGACY_DATA", finalList);	
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getHODDeptWiseNewCases")
	public static Response getHODDeptWiseNewCases(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						if (roleId.equals("2")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						
						
						sql = "select a.dept_code as deptcode ,upper(d.description) as description, count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_gpo_ack_depts  a "
								+ " inner join ecourts_gpo_ack_dtls b on (a.ack_no=b.ack_no) inner join dept_new d on (a.dept_code=d.dept_code)"
								+ " where b.ack_type='NEW'  and respondent_slno=1 and (reporting_dept_code='"+dept_code+"' or a.dept_code='"+dept_code+"')  " + sqlCondition;

						sql += " group by a.dept_code,d.dept_code ,description order by 1";
						
						
						System.out.println("HOD DEPT WISE NEW CASE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("HOD DEPT WISE NEW CASE DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
						    	finalList.put(cases);
							}
							
								casesData.put("HOD_DEPT_WISE_NEW_DATA", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("HOD_DEPT_WISE_NEW_DATA", finalList);									
							}
						
						
							String finalString = casesData.toString();
							
							if (isNewDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getHODDeptWiseLegacyCases")
	public static Response getHODDeptWiseLegacyCases(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						if (roleId.equals("2")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						
						sql="select a.dept_code as deptcode , upper(d.description) as description,count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_case_data a "
								+ "inner join dept_new d on (a.dept_code=d.dept_code) "
								+ "where d.display = true and (reporting_dept_code='"+dept_code+"' or a.dept_code='"+dept_code+"') " + sqlCondition
								+ "group by a.dept_code , d.description order by 1";

						System.out.println("HOD DEPT WISE LEGACY CASE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						
						System.out.println("HOD DEPT WISE LEGACY CASE DATA =" + data);						
						
						JSONArray legacyCaseList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isLegacyDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
						    	legacyCaseList.put(cases);
							}
							
							casesData.put("HOD_DEPT_WISE_LEGACY_DATA", legacyCaseList);
							isLegacyDataAvailable = true;						
														
							} else {
								
								casesData.put("HOD_DEPT_WISE_LEGACY_DATA", legacyCaseList);	
								
							}
						
							String finalString = casesData.toString();
							
							if (isLegacyDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getNewCasesListForDept")
	public static Response getCasesListForDept(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
					else if(!jObject.has("REPORT_LEVEL") || jObject.get("REPORT_LEVEL").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- REPORT_LEVEL is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String caseStatus = jObject.get("CASE_STATUS").toString();
						String report_level = jObject.get("REPORT_LEVEL").toString();
						
						if (!caseStatus.equals("")) {
							if (caseStatus.equals("withSD")) {
								sqlCondition = " and case_status=1 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withMLO")) {
								sqlCondition = " and (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHOD")) {
								sqlCondition = " and case_status=3  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withNO")) {
								sqlCondition = " and case_status=4  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withSDSec")) {
								sqlCondition = " and case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDC")) {
								sqlCondition = " and case_status=7  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistNO")) {
								sqlCondition = " and case_status=8  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHODSec")) {
								sqlCondition = " and case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistSec")) {
								sqlCondition = " and case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withGP")) {
								sqlCondition = " and case_status=6 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("closed")) {
								sqlCondition = " and (case_status=99 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("goi")) {
								sqlCondition = " and (case_status=96 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("psu")) {
								sqlCondition = " and (case_status=97 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("Private")) {
								sqlCondition = " and (case_status=98 or coalesce(ecourts_case_status,'')='Closed') ";
							}
						}
						
						
						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}

						
						System.out.println("roleId--"+roleId);
						
						String condition="";
						if (roleId.equals("6") )
							condition= " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) ";					
						

						sql = "select a.ack_no,advocatename,advocateccno,cm.case_short_name,maincaseno,inserted_time,petitioner_name,getack_dept_desc(a.ack_no::text) as dept_descs,"
								+ "	 services_flag,reg_year,reg_no,mode_filing,case_category,dm.district_name,coalesce(e.hc_ack_no,'-') as hc_ack_no,barcode_file_path, "
								+ " coalesce(trim(e.ack_file_path),'-') as scanned_document_path1,'' as orderpaths "
								+ " from  ecourts_gpo_ack_depts  a "
								+ " inner join ecourts_gpo_ack_dtls e on (a.ack_no=e.ack_no) "
								+ " inner join dept_new d on (a.dept_code=d.dept_code)"
								+ " inner join district_mst dm on (e.distid=dm.district_id) "
								+ " inner join case_type_master cm on (e.casetype=cm.sno::text or e.casetype=cm.case_short_name) "
								+ " "+condition+" where e.ack_type='NEW' and respondent_slno=1 ";
			 
						 
						 if(report_level.equals("SD")) {
							 sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
						 }
						 else {//if(reportLevel.equals("HOD")) {
							 sql += " and a.dept_code='" + dept_code + "' ";
						 }

						 sql += sqlCondition;
						 
						System.out.println("ecourts SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
						
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("ACK_NO", entry.get("ack_no").toString());						    	
						    	cases.put("CASE_TYPE", entry.get("case_short_name").toString());
						    	cases.put("MAIN_CASE_NO", entry.get("maincaseno"));
						    	cases.put("CASE_CATEGORY", entry.get("case_category"));
						    	cases.put("DEPARTMENTS", entry.get("dept_descs").toString());
						    	cases.put("PETITIONER_ADVOCATE", entry.get("petitioner_name").toString());
						    	cases.put("RESPONDENT_ADVOCATE", entry.get("advocatename").toString());
						    	cases.put("RESPONDENT_ADVOCATE_NO", entry.get("advocateccno").toString());
						    	cases.put("DISTRICT_NAME", entry.get("district_name").toString());
						    	cases.put("ACKNOWLEDGEMENT_FILE_PATH", entry.get("ack_file_path"));
						    	cases.put("BARCODE_FILE_PATH", entry.get("barcode_file_path")!=null?"https://apolcms.ap.gov.in/"+entry.get("barcode_file_path"):"");
						    	
						    	String scannedAffidavitPath="";

								if (entry.get("ack_no") != null)
								{
									if (entry.get("hc_ack_no")!=null && !entry.get("hc_ack_no").equals("-")) {
										scannedAffidavitPath = "https://apolcms.ap.gov.in/uploads/scandocs/" + entry.get("hc_ack_no")
										+ "/" + entry.get("hc_ack_no") + ".pdf";
									}
									else if (entry.get("hc_ack_no")!=null && entry.get("hc_ack_no").equals("-")) {
										scannedAffidavitPath = "https://apolcms.ap.gov.in/uploads/scandocs/" + entry.get("ack_no")
										+ "/" + entry.get("ack_no") + ".pdf";
									}
								}
								
								cases.put("SCANNED_AFFIDAVIT_PATH", scannedAffidavitPath);
						    	
						    	finalList.put(cases);
							}
							
							casesData.put("CASES_LIST", finalList);
							isDataAvailable = true;						
														
							} else {
								
								casesData.put("CASES_LIST", finalList);	
								
							}
						
							String finalString = casesData.toString();
							
							if (isDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
												
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getLegacyCasesListForDept")
	public static Response getLegacyCasesListForDept(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
					else if(!jObject.has("REPORT_LEVEL") || jObject.get("REPORT_LEVEL").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- REPORT_LEVEL is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String caseStatus = jObject.get("CASE_STATUS").toString();
						String report_level = jObject.get("REPORT_LEVEL").toString();
						
						if (!caseStatus.equals("")) {
							if (caseStatus.equals("withSD")) {
								sqlCondition = " and case_status=1 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withMLO")) {
								sqlCondition = " and (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHOD")) {
								sqlCondition = " and case_status=3  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withNO")) {
								sqlCondition = " and case_status=4  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withSDSec")) {
								sqlCondition = " and case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDC")) {
								sqlCondition = " and case_status=7  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistNO")) {
								sqlCondition = " and case_status=8  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHODSec")) {
								sqlCondition = " and case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistSec")) {
								sqlCondition = " and case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withGP")) {
								sqlCondition = " and case_status=6 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("closed")) {
								sqlCondition = " and (case_status=99 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("goi")) {
								sqlCondition = " and (case_status=96 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("psu")) {
								sqlCondition = " and (case_status=97 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("Private")) {
								sqlCondition = " and (case_status=98 or coalesce(ecourts_case_status,'')='Closed') ";
							}
						}
						
						
						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}

						
						System.out.println("roleId--"+roleId);
						
						String condition="";
						if (roleId.equals("6") )
							condition= " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) ";					
						

						sql = "select a.*, "
								+ ""
								// + "n.global_org_name as globalorgname, n.fullname_en as fullname, n.designation_name_en as designation, n.mobile1 as mobile, n.email as email, "
								+ ""
								+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a "
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
								//+ "inner join nic_data n on (a.assigned_to=n.email) "
								+ " left join"
								+ " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) "+condition+" where d.display = true ";
			 
						 
						 if(report_level.equals("SD")) {
							 sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
						 }
						 else {//if(reportLevel.equals("HOD")) {
							 sql += " and a.dept_code='" + dept_code + "' ";
						 }

						 sql += sqlCondition;
						 
						System.out.println("ecourts SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
						
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("SCANNED_AFFIDAVIT", entry.get("scanned_document_path"));
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
						    	cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
						    	cases.put("PRAYER", entry.get("prayer").toString());
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_OF_NEXT_LISTING", entry.get("date_of_filing").toString());
						    	cases.put("BENCH", entry.get("bench_name").toString());
						    	cases.put("JUDGE_NAME", "Hon'ble Judge " +entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DISTRICT_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE", entry.get("purpose_name"));
						    	cases.put("RESPONDENTS", entry.get("res_name")+","+entry.get("address"));
						    	cases.put("PETITIONER_ADVOCATE", entry.get("pet_adv"));
						    	cases.put("RESPONDENT_ADVOCATE", entry.get("res_adv"));
						    	cases.put("ORDER_PATHS", entry.get("orderpaths"));
						    	finalList.put(cases);
							}
							
							casesData.put("CASES_LIST", finalList);
							isDataAvailable = true;						
														
							} else {
								
								casesData.put("CASES_LIST", finalList);	
								
							}
						
							String finalString = casesData.toString();
							
							if (isDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
												
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
	
}