package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

/**
 * @author : Bhanu Krishna Kota
 * @title :
 * 
 *        PRD URL : https://aprcrp.apcfss.in/apolcms-services/services/instructions/submitInstructions
 *        TEST URL :http://localhost:8080/apolcms-services/services/instructions/submitInstructions
 * 
 *        {"REQUEST" : {"CINO":"APHC010191782022","USER_ID":"RAMESH.DAMMU@APCT.GOV.IN", "INSTRUCTIONS":"Instructions will be submitted", "ROLE_ID":"5", "DEPT_CODE":"REV03", "DIST_ID":"0"}}
 *		  {"RESPONSE": {"RSPCODE": "01","RSPDESC": "INSTRUCTIONS SAVED SUCCESSFULLY"  }
 *	
 **/

@Path("/newCaseProcessingStatus")
public class CaseProcessingStatusReport {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/secDeptWise")
	public static Response secDeptWise(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);
					
					if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();					

						
						if(roleId.equals("5") || roleId.equals("9")) {
							
							 return hodDeptWise(incomingData); 
						}
						
						else {
							con = DatabasePlugin.connect();
							sql="select a1.reporting_dept_code as dept_code,upper(trim(dn1.description)) as description,sum(total_resp1) as cases_respondent_one,sum(total_resp_other) as cases_respondent_other,sum(totalcases) as total,sum(closed_cases) as closed_cases,sum(counter_uploaded) as counter_uploaded,"
									+ "	sum(pwrcounter_uploaded) as pwrcounter_uploaded,sum(counter_approved_gp) as counter_approved_gp  from ("
									
									+" select case when reporting_dept_code='CAB01' then a.dept_code else reporting_dept_code end as reporting_dept_code,a.dept_code,upper(trim(dn.description)) as description,sum(case when a.respondent_slno=1 then 1 else 0 end) as total_resp1,"
									+ " sum(case when a.respondent_slno > 1 then 1 else 0 end) as total_resp_other,"
									+ " count(*) as totalcases,sum(case when a.ecourts_case_status='Closed' then 1 else 0 end) as closed_cases,"
									+ "	sum(case when a.ecourts_case_status='Pending' and counter_filed_document is not null and length(counter_filed_document)>10 then 1 else 0 end) as counter_uploaded,"
									+ "	sum(case when a.ecourts_case_status='Pending' and pwr_uploaded_copy is not null and length(pwr_uploaded_copy)>10 then 1 else 0 end) as pwrcounter_uploaded,"
									+ "	sum(case when counter_approved_gp='Yes' then 1 else 0 end) as counter_approved_gp"
									+ "	from ecourts_gpo_ack_depts  a "
									+ " left join ecourts_olcms_case_details ecod on(a.ack_no=ecod.cino and a.respondent_slno=ecod.respondent_slno)"
									+ "	left join ecourts_gpo_ack_dtls  b using(ack_no) inner join dept_new dn on (a.dept_code=dn.dept_code)"
									+ " where  b.ack_type='NEW' "
									+ "	group by dn.reporting_dept_code,a.dept_code,dn.description "
									
									+ ") a1 inner join dept_new dn1 on (a1.reporting_dept_code=dn1.dept_code)  group by a1.reporting_dept_code,dn1.description order by 1";
							
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("SEC_DEPT_CODE", entry.get("dept_code").toString());						    	
							    	cases.put("SEC_DEPT_NAME", entry.get("description").toString());
							    	cases.put("CASE_COUNT_RESP_ONE", entry.get("cases_respondent_one"));
							    	cases.put("CASE_COUNT_RESP_OTHER", entry.get("cases_respondent_other"));
							    	cases.put("CLOSED", entry.get("closed_cases").toString());
							    	cases.put("COUNTER_FILED", entry.get("counter_uploaded").toString());
							    	cases.put("PWR_UPLOADED", entry.get("pwrcounter_uploaded").toString());
							    	cases.put("PWR_APPROVED", entry.get("counter_approved_gp").toString());
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("SEC_DEPT_WISE_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("SEC_DEPT_WISE_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Sect. Dept wise cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
								else
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							
						}					
					} 
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\", \"RSPDESC\" :\"Invalid Data Format.\"}}";
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
	@Path("/hodDeptWise")
	public static Response hodDeptWise(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);
					
					if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();		
					String selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();	

						
					con = DatabasePlugin.connect();
					
					
						
						
								sql="select a.dept_code,upper(trim(dn.description)) as description,sum(case when a.respondent_slno=1 then 1 else 0 end) as total_resp1,"
								+ " sum(case when a.respondent_slno > 1 then 1 else 0 end) as total_resp_other,"
								+ " count(*) as total,sum(case when a.ecourts_case_status='Closed' then 1 else 0 end) as closed_cases,"
								+ "	sum(case when a.ecourts_case_status='Pending' and counter_filed_document is not null and length(counter_filed_document)>10 then 1 else 0 end) as counter_uploaded,"
								+ "	sum(case when a.ecourts_case_status='Pending' and pwr_uploaded_copy is not null and length(pwr_uploaded_copy)>10 then 1 else 0 end) as pwrcounter_uploaded,"
								+ "	sum(case when counter_approved_gp='Yes' then 1 else 0 end) as counter_approved_gp"
								+ "	 from ecourts_gpo_ack_depts  a "
								+ "left join ecourts_olcms_case_details ecod on(a.ack_no=ecod.cino and a.respondent_slno=ecod.respondent_slno)"
								+ "	left join ecourts_gpo_ack_dtls  b using(ack_no) inner join dept_new dn on (a.dept_code=dn.dept_code)"
								+ " where  b.ack_type='NEW' and (a.dept_code='" + selectedDeptCode +"' or reporting_dept_code='"+selectedDeptCode+"')"
								+ "	group by dn.reporting_dept_code,a.dept_code,dn.description ";
						

					
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("HOD_DEPT_CODE", entry.get("dept_code").toString());						    	
							    	cases.put("HOD_DEPT_NAME", entry.get("description").toString());
							    	cases.put("CASE_COUNT_RESP_ONE", entry.get("total_resp1"));
							    	cases.put("CASE_COUNT_RESP_OTHER", entry.get("total_resp_other"));
							    	cases.put("CLOSED", entry.get("closed_cases").toString());
							    	cases.put("COUNTER_FILED", entry.get("counter_uploaded").toString());
							    	cases.put("PWR_UPLOADED", entry.get("pwrcounter_uploaded").toString());
							    	cases.put("PWR_APPROVED", entry.get("counter_approved_gp").toString());
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("HOD_DEPT_WISE_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("HOD_DEPT_WISE_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"HOD Dept wise cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
								else
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							
						}					
					 
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\", \"RSPDESC\" :\"Invalid Data Format.\"}}";
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
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);
					
					if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
					else if(!jObject.has("RESPONDENT_TYPE") || jObject.get("RESPONDENT_TYPE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- RESPONDENT_TYPE is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();	
					String selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					String respondenttype = jObject.get("RESPONDENT_TYPE").toString();
					String caseStatus = jObject.get("CASE_STATUS").toString();
					String sqlCondition="",condition="";

						
					con = DatabasePlugin.connect();
					
					
					if(!caseStatus.equals("")) {
						if(caseStatus.equals("CLOSED")){
								sqlCondition= " and coalesce(ad.ecourts_case_status,'')='Closed'  ";
							}
						
						if(caseStatus.equals("COUNTERUPLOADED")) {
							sqlCondition=" and counter_filed_document is not null and length(counter_filed_document)>10 ";
						}
						if(caseStatus.equals("PWRUPLOADED")) {
							sqlCondition= " and pwr_uploaded_copy is not null and length(pwr_uploaded_copy)>10  ";
						}
						if(caseStatus.equals("GPCOUNTER")) {
							sqlCondition=" and counter_approved_gp='Yes' ";
						}
						
						if(caseStatus.equals("SD")) {
							sqlCondition += " and (dmt.dept_code='" + selectedDeptCode + "' or dmt.reporting_dept_code='" + selectedDeptCode+ "') "; 
						}
						
						if(caseStatus.equals("HOD")) {
							sqlCondition += " and dmt.dept_code='" + selectedDeptCode + "' "; 
						}
					}
					
					if(respondenttype.equals("1"))
					{
						sqlCondition += " and ad.respondent_slno=1 ";
					}
					else if(respondenttype.equals("2"))
					{
						sqlCondition += " and ad.respondent_slno>1 ";
					}
					else if(respondenttype.equals("SD")) {
						sqlCondition += " and (dmt.dept_code='" + selectedDeptCode + "' or dmt.reporting_dept_code='" + selectedDeptCode+ "') "; 
					}
					
					if(respondenttype.equals("HOD")) {
						sqlCondition += " and dmt.dept_code='" + selectedDeptCode + "' "; 
					}
					
					if ((roleId.equals("6"))) {
						condition = " inner join ecourts_mst_gp_dept_map egm on (egm.dept_code=ad.dept_code) ";
					}

					if (roleId.equals("2")) {
						sql+=" and a.dist_id='"+dist_id+"'";
					}

					

					sql = "select  a.slno , a.ack_no , distid , advocatename ,advocateccno , casetype , maincaseno , ecod.remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
							+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
							+ "to_char(inserted_time,'dd-mm-yyyy') as generated_date, getack_dept_desc(a.ack_no) as dept_descs,inserted_time, coalesce(a.hc_ack_no,'-') as hc_ack_no "
							+ "from ecourts_gpo_ack_depts ad  inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
							+ "left join ecourts_olcms_case_details ecod on(ad.ack_no=ecod.cino and ad.respondent_slno=ecod.respondent_slno)"
							+ "inner join district_mst dm on (a.distid=dm.district_id) "
							+ "inner join dept_new dmt on (ad.dept_code=dmt.dept_code)  " + condition + " "
							+ "inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name) "
							+ "where a.delete_status is false and ack_type='NEW' " + sqlCondition
							+ "order by inserted_time desc";

							System.out.println("SQL:" + sql);	
						
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("ACK_NO", entry.get("ack_no").toString());						    	
							    	cases.put("DATE", entry.get("generated_date").toString());
							    	cases.put("DIST_NAME", entry.get("district_name"));
							    	cases.put("CASE_TYPE", entry.get("case_full_name"));
							    	cases.put("MAIN_CASE_NO", entry.get("maincaseno").toString());
							    	cases.put("RESPONDENTS", entry.get("dept_descs").toString());
							    	cases.put("ADV_CCNO", entry.get("advocateccno").toString());
							    	cases.put("ADV_NAME", entry.get("advocatename").toString());
							    	cases.put("ACK_FILE_PATH", entry.get("ack_file_path").toString());
							    	
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
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases List retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
								else
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							
						}					
					 
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\", \"RSPDESC\" :\"Invalid Data Format.\"}}";
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
	
	
}
