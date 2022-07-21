package in.apcfss.olcms;

import java.sql.Connection;
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
	
	
	
}