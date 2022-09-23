package in.apcfss.olcms;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import plugins.DatabasePlugin;


@Path("/finalOrdersImplementation")
public class DistwiseFinalOrderImplementationReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displayFilters")
	public static Response displayCaseFilters(String incomingData) throws Exception {
		
		Connection con = null;
		String jsonStr = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
				} else if (!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
				} else if (!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
				} else if (!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- DIST_ID is missing in the request.\" }}";
				} else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "";

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					con = DatabasePlugin.connect();
					
					//Populate the District list in the District dropdown
					
					if (roleId.equals("2")) {
						sql = "select district_id,upper(district_name) as dist_name from district_mst where district_id='" + distId + "' order by district_name";
					}
						
					else {
						sql = "select district_id,upper(district_name) as dist_name from district_mst order by 1";
					}
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

					JSONArray distList = new JSONArray();
					JSONObject casesData = new JSONObject();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DIST_CODE", entry.get("district_id").toString());								
							cases.put("DIST_NAME", entry.get("dist_name").toString());
							
							distList.put(cases);
						}
					} 
					casesData.put("DIST_LIST", distList);					
					
					
					//Populate the Dept list in the Department dropdown
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9")
							|| roleId.equals("10")) {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true";
						sql += " and (reporting_dept_code='" + deptCode + "' or dept_code='"
								+ deptCode + "')";
						sql += "  order by dept_code ";
					}
					else {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true order by dept_code";
					}					
					
					data = DatabasePlugin.executeQuery(sql, con);

					JSONArray deptList = new JSONArray();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DEPT_ID", entry.get("dept_code").toString());								
							cases.put("DEPT_DESC", entry.get("dept_desc").toString());
							
							deptList.put(cases);
						}
					} 
					casesData.put("DEPT_LIST", deptList);
					
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases Filters retrived successfully\"  , "
								+ finalString.substring(1, finalString.length() - 1) + "}}";
					else
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
								+ finalString.substring(1, finalString.length() - 1) + " }}";
					
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}";
			}

		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			// conn.rollback();
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
	@Path("/distwise")
	public static Response distwise(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";
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
						String dept_id ="",district_id = "",condition = "";
						
						if (jObject.has("SELECTED_DEPT_CODE") && !jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
							dept_id = jObject.get("SELECTED_DEPT_CODE").toString();	
							condition += " and a.dept_code='" + dept_id.trim() + "' ";
						}	
						
						if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals("")) {
							dept_id = jObject.get("SELECTED_DIST_ID").toString();	
							condition += " and a.dist_id='" + district_id.trim() + "' ";
						}
						
						
						if(roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9")) {
								condition+=" and ( a.dept_code='"+dept_code+"')";
						}	
						else if(roleId.equals("2")){
								condition+=" and a.dist_id='"+dist_id+"'";
						}
						
						sql = "select dm.district_id,dm.district_name,count(*) as total_cases,"

								+ "sum(case when case_status=7 then 1 else 0 end) as pending_dc, "
								+ "sum(case when case_status=8 then 1 else 0 end) as pending_dno, "
								+ "sum(case when case_status=10 then 1 else 0 end) as pending_dsec, "

								+ "sum(case when scanned_document_path is not null and length(scanned_document_path)>10 then 1 else 0 end) as olcms_uploads, "
								+ "sum(case when petition_document is not null and length(petition_document)>10 then 1 else 0 end) as petition_uploaded , "
								+ "sum(case when a.ecourts_case_status='Closed' then 1 else 0 end) as closed_cases , "
								+ "sum(case when a.ecourts_case_status='Pending' and counter_filed_document is not null and length(counter_filed_document)>10  then 1 else 0 end) as counter_uploaded,"
								+ " sum(case when a.ecourts_case_status='Pending' and pwr_uploaded_copy is not null and length(pwr_uploaded_copy)>10  then 1 else 0 end) as pwrcounter_uploaded ,"
								+ " sum(case when counter_approved_gp='Yes' then 1 else 0 end) as counter_approved_gp,"
								+ " sum(case when length(action_taken_order)> 10   or final_order_status='final' then 1 else 0 end) as final_order ,"
								+ " sum(case when length(appeal_filed_copy)> 10 or final_order_status='appeal' then 1 else 0 end) as appeal_order ,"
								+ " sum(case when length(dismissed_copy)> 10 or final_order_status='dismissed'  or b.ecourts_case_status='dismissed' then 1 else 0 end) as dismissed_order  "
								+ "from ecourts_case_data a "
								+ "left join apolcms.ecourts_olcms_case_details b on (b.cino=a.cino)"
								+ "inner join ecourts_case_finalorder ecf on (a.cino=ecf.cino)"
								+ "inner join district_mst dm on (dm.district_id=a.dist_id)"
								+ "inner join dept_new dn on (dn.dept_code=a.dept_code) where 1=1 " + condition + " "
								+ "group by dm.district_id,dm.district_name ";

						
						System.out.println("DIST WISE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("DIST WISE DATA SQL:" + data);						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DIST_NAME", entry.get("district_name"));						    	
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_AT_COLLECTOR", entry.get("pending_dc"));
						    	cases.put("PENDING_AT_DIST_NO", entry.get("pending_dno"));
						    	cases.put("PENDING_AT_DIST_SO", entry.get("pending_dsec"));
						    	cases.put("SCANNED_BY_OLCMS", entry.get("olcms_uploads"));
						    	cases.put("PETITION_UPLOADED", entry.get("petition_uploaded"));
						    	cases.put("PARAWISE_UPLOADED", entry.get("counter_uploaded"));
						    	cases.put("COUNTER_FILED", entry.get("pwrcounter_uploaded"));
						    	cases.put("FINAL_ORDER_IMPLEMENTED", entry.get("final_order"));
						    	cases.put("APPEAL_FILED", entry.get("appeal_order"));
						    	cases.put("DISMISSED", entry.get("dismissed_order"));
						    	cases.put("CLOSED", entry.get("closed_cases"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("DIST_WISE_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("DIST_WISE_DATA", finalList);	
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
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String caseStatus = jObject.get("CASE_STATUS").toString();
						
						
						if(!caseStatus.equals("")) {
							if(caseStatus.equals("CLOSED")){
									sqlCondition+= " and coalesce(a.ecourts_case_status,'')='Closed' ";
								}
							if(caseStatus.equals("PET")) {
								sqlCondition+=" and eocd.petition_document is not null and length(eocd.petition_document)>10 ";
							}
							if(caseStatus.equals("COUNTERUPLOADED")) {
								sqlCondition+=" and eocd.counter_filed_document is not null  and length(eocd.counter_filed_document)>10  ";
							}
							if(caseStatus.equals("PWRUPLOADED")) {
								sqlCondition+= " and eocd.pwr_uploaded_copy is not null  and length(eocd.pwr_uploaded_copy)>10 ";
							}
							if(caseStatus.equals("GPCOUNTER")) {
								sqlCondition+=" and eocd.counter_approved_gp='Yes' ";
							}
							if(caseStatus.equals("SCANNEDDOC")) {
								sqlCondition+=" and scanned_document_path is not null and length(scanned_document_path)>10 ";
							}
							if(caseStatus.equals("FINALORDER")) {
								sqlCondition+=" and final_order_status='final' ";
							}
							if(caseStatus.equals("APPEALORDER")) {
								sqlCondition+=" and final_order_status='appeal' ";
							}
							if(caseStatus.equals("DISMISSEDORDER")) {
								sqlCondition+=" and final_order_status='dismissed' ";
							}
							if(caseStatus.equals("DC")) {
								sqlCondition+=" and  case_status=7 ";
							}
							if(caseStatus.equals("DNO")) {
								sqlCondition+=" and case_status=8 ";
							}
							if(caseStatus.equals("DSEC")) {
								sqlCondition+=" and case_status=10 ";
							}
						}
					
						if(roleId.equals("2")) {
							sqlCondition+=" and a.dist_id='"+dist_id+"'";
						}
						else if(roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9")) {
							sqlCondition += " and (a.dept_code='" + dept_code + "') " ;
						}
						if (dept_code != null && !dept_code.toString().contentEquals("")
								&& !dept_code.toString().contentEquals("0")) {
							sqlCondition += " and a.dept_code='" + dept_code + "' ";
						}
						
						sql = "select a.*, coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a  "
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1)"
								+ "  left join ecourts_olcms_case_details eocd on (a.cino=eocd.cino)  "
								+ " inner join ( select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths "
								+ " from  (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null "
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0 and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) c group by cino ) b on (a.cino=b.cino)"
								+ " inner join dept_new d on (a.dept_code=d.dept_code) where d.display = true   "+sqlCondition+"    ";
						

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
						    	
						    	String scannedAffidavitPath="";

								if (entry.get("scanned_document_path1") != null)
								{
									if (entry.get("scanned_document_path1")!=null && !entry.get("scanned_document_path1").equals("-")) {
										scannedAffidavitPath = "https://apolcms.ap.gov.in/" + entry.get("scanned_document_path");
									}
									
								}
								cases.put("SCANNED_AFFIDAVIT_PATH", scannedAffidavitPath);
											    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
						    	cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));						    	
						    	cases.put("PRAYER", entry.get("prayer"));						    	
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_NEXT_LIST", entry.get("date_next_list"));
						    	cases.put("BENCH_NAME", entry.get("bench_name"));
						    	cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DIST_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE_NAME", entry.get("purpose_name"));
						    	cases.put("RESPONDENT_NAME", entry.get("res_name")+","+entry.get("address"));
						    	cases.put("PET_ADV", entry.get("pet_adv"));
						    	cases.put("RES_ADV", entry.get("res_adv"));
						    	JSONArray orderdocumentList = new JSONArray();								
						    	
						    	if (entry.get("orderpaths") != null)
						    	{
						    		String mydata = entry.get("orderpaths").toString();
						    		Pattern pattern = Pattern.compile("(?<=a href=\".)(.*?)(?=\" target)");
						    		Matcher matcher = pattern.matcher(mydata);
						    		Pattern pattern2 = Pattern.compile("(?<=<span>)(.*?)(?=</span)");
						    		Matcher matcher2 = pattern2.matcher(mydata);
						    		
						            while (matcher.find() && matcher2.find()){
						            	JSONObject orderData = new JSONObject();
						                String s = matcher.group();
						                String s1 = matcher2.group();
						                orderData.put("ORDER_NAME", s1);
						                orderData.put("ORDER_DOC_PATH", "https://apolcms.ap.gov.in/"+s);
						                
						                orderdocumentList.put(orderData);
						            }						           
						           
						    	}
						    	cases.put("ORDER_PATHS", orderdocumentList);						    	
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