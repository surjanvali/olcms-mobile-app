package in.apcfss.olcms;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
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

import in.apcfss.util.ECourtsCryptoHelper;
import in.apcfss.util.EHighCourtAPI;
import in.apcfss.util.HASHHMACJava;
import plugins.DatabasePlugin;

/**
 * @author : Bhanu Krishna Kota
 * @title :
 * 
 *        PRD URL :
 *        https://apolcms.ap.gov.in/apolcms-services/services/getInstructions/displayCasesList
 *        TEST URL :
 *        http://localhost:9090/apolcms-services/services/getInstructions/displayCasesList
 * 
 *        {"REQUEST" : {"USER_ID":"gp-ser2@ap.gov.in", "ROLE_ID":"6","DEPT_CODE":"0", "DIST_ID":"0"}} 
 *        
 **/

@Path("/getInstructions")
public class InstructionsStatusForGP {
			
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displayCasesList")
	public static Response displayCasesListForInstructions(String incomingData) throws Exception {

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
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					con = DatabasePlugin.connect();
					
					
					 sql="select type_name_reg, reg_no, reg_year, to_char(dt_regis,'dd-mm-yyyy') as dt_regis, a.cino, case when length(scanned_document_path) > 10 then scanned_document_path else '-' end as scanned_document_path "
						 		+ " from (select distinct cino from ecourts_dept_instructions) a inner join ecourts_case_data d on (a.cino=d.cino)"
						 		+ " where d.dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='"+userid+"')";
						 
						 
						System.out.println("SQL:" + sql);
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						JSONArray finalList = new JSONArray();
						
					if (data != null && !data.isEmpty() && data.size() > 0) {
						for (Map<String, Object> entry : data) {		
						    
					    	JSONObject cases = new JSONObject();
					    	cases.put("case_type", entry.get("type_name_reg").toString());
					    	String caseno = entry.get("type_name_reg").toString()+" "+entry.get("reg_no").toString()+"/"+entry.get("reg_year").toString();
					    	cases.put("case_no", caseno);						    	
					    	cases.put("case_reg_date", entry.get("dt_regis").toString());
					    	cases.put("status", "Pending");
					    	cases.put("cino", entry.get("cino").toString());
					    	finalList.put(cases);
					}
					JSONObject casesData = new JSONObject();
					casesData.put("CASES_LIST", finalList);
					String finalString = casesData.toString();
					    
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
					
						
					} else {
						JSONObject casesData = new JSONObject();
						casesData.put("CASES_LIST", finalList);
						String finalString = casesData.toString();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
					}				

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
	@Path("/submitDailyStatus")
	public static Response submitInstructions(String incomingData) throws Exception {

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
				}  else if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CINO is missing in the request.\" }}";
				}  else if (!jObject.has("DAILY_STATUS") || jObject.get("DAILY_STATUS").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- DAILY_STATUS is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					cino=jObject.get("CINO").toString();
					daily_status=jObject.get("DAILY_STATUS").toString();
					con = DatabasePlugin.connect();
					PreparedStatement ps = null;
					
					
					sql = "insert into ecourts_gpo_daily_status (cino, status_remarks , dept_code ,dist_code,insert_by ) "
							+ " values (?,?, ?, ?, ?)";

					ps = con.prepareStatement(sql);
					int i = 1;
					ps.setString(i, cino);
					ps.setString(++i, daily_status != null ? daily_status : "");
					ps.setString(++i, deptCode);
					ps.setInt(++i, Integer.parseInt(distId));
					ps.setString(++i, userid);

					System.out.println("sql--"+sql);

					int a = ps.executeUpdate();
					if(a>0) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Daily status details saved sucessfully\" }}";
					}
					else {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error in submission. Kindly try again.\" }}";
					}

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
	@Path("/getInstructionsHistory")
	public static Response getInstructionsHistory(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CINO is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					
					cino=jObject.get("CINO").toString();
					con = DatabasePlugin.connect();
					sql = "select status_remarks, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_gpo_daily_status where cino='" + cino + "'  order by 1 ";
					System.out.println("sql--" + sql);
					List<Map<String, Object>> instructionsHistory = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
					
					if (instructionsHistory != null && !instructionsHistory.isEmpty() && instructionsHistory.size() > 0) {
						
						for (Map<String, Object> entry : instructionsHistory) {		
						    
						    	JSONObject history = new JSONObject();
						    	history.put("status_remarks", entry.get("status_remarks").toString());
						    	history.put("submitted_time", entry.get("insert_time").toString());
						    	
						    	finalList.put(history);
						}
						JSONObject casesData = new JSONObject();
						casesData.put("DAILY_STATUS_HISTORY", finalList);
						String finalString = casesData.toString();
						    
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Daily Status history retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
						
					} else {
						JSONObject casesData = new JSONObject();
						casesData.put("DAILY_STATUS_HISTORY", finalList);
						String finalString = casesData.toString();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+ " }}";
					}
					
					
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
	@Path("/getCaseDetails")
	public static Response getCaseDetails(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CINO is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					
					cino=jObject.get("CINO").toString();
					con = DatabasePlugin.connect();
					
					sql = "select a.*, prayer from ecourts_case_data a left join nic_prayer_data np on (a.cino=np.cino) where a.cino='" + cino + "'";
					List<Map<String, Object>> caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONObject casesData = new JSONObject();
					JSONArray caseDetailsArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						
						for (Map<String, Object> entry : caseDetails) {		
						    
						    	JSONObject casehistory = new JSONObject();
						    	casehistory.put("DOWNLOAD_AFFIDAVIT", entry.get("scanned_document_path") !=null ? entry.get("scanned_document_path").toString() :"");
						    	casehistory.put("DATE_OF_FILING", entry.get("date_of_filing") !=null ? entry.get("date_of_filing").toString() :"");
						    	casehistory.put("CASE_TYPE", entry.get("type_name_reg") !=null ? entry.get("type_name_reg").toString() :"");
						    	casehistory.put("FILING_NO", entry.get("fil_no") !=null ? entry.get("fil_no").toString() :"");
						    	casehistory.put("FILING YEAR", entry.get("fil_year") !=null ? entry.get("fil_year").toString() :"");
						    	casehistory.put("REG_NO", entry.get("reg_no") !=null ? entry.get("reg_no").toString() :"");
						    	casehistory.put("EST_CODE", entry.get("est_code") !=null ? entry.get("est_code").toString() :"");
						    	casehistory.put("CASE_ID", entry.get("case_type_id") !=null ? entry.get("case_type_id").toString() :"");
						    	casehistory.put("CAUSE_TYPE", entry.get("causelist_type") !=null ? entry.get("causelist_type").toString() :"");
						    	casehistory.put("BENCH_NAME", entry.get("bench_name") !=null ? entry.get("bench_name").toString() :"");
						    	casehistory.put("JUDICIAL_BRANCH", entry.get("judicial_branch") !=null ? entry.get("judicial_branch").toString() :"");
						    	casehistory.put("CORAM", entry.get("coram") !=null ? entry.get("coram").toString() :"");
						    	casehistory.put("COURT_EST_NAME", entry.get("court_est_name") !=null ? entry.get("court_est_name").toString() :"");
						    	casehistory.put("STATE_NAME", entry.get("state_name") !=null ? entry.get("state_name").toString() :"");
						    	casehistory.put("DIST_NAME", entry.get("dist_name") !=null ? entry.get("dist_name").toString() :"");
						    	casehistory.put("DATE_FIRST_LIST", entry.get("date_first_list") !=null ? entry.get("date_first_list").toString() :"");
						    	casehistory.put("DATE_NEXT_LIST", entry.get("date_next_list") !=null ? entry.get("date_next_list").toString() :"");
						    	casehistory.put("DATE_OF_DECISION", entry.get("date_of_decision") !=null ? entry.get("date_of_decision").toString() :"");
						    	casehistory.put("PURPOSE", entry.get("purpose_name") !=null ? entry.get("purpose_name").toString() :"");
						    	casehistory.put("PETITIONER_NAME", entry.get("pet_name") !=null ? entry.get("pet_name").toString() :"");
						    	casehistory.put("PETITIONER_ADV", entry.get("pet_adv") !=null ? entry.get("pet_adv").toString() :"");
						    	casehistory.put("PETITIONER_LEGAL_HEIR", entry.get("pet_legal_heir") !=null ? entry.get("pet_legal_heir").toString() :"");
						    	casehistory.put("RESP_NAME", entry.get("res_name") !=null ? entry.get("res_name").toString() :"" + "," +entry.get("address") !=null ? entry.get("address").toString() :"" );
						    	casehistory.put("RESPONDENT_ADV", entry.get("res_adv") !=null ? entry.get("res_adv").toString() :"");
						    	casehistory.put("PRAYER", entry.get("prayer") !=null ? entry.get("prayer").toString() :"");
						    	
						    	caseDetailsArray.put(casehistory);
						}
						
						
					} 
					
					casesData.put("CASE_DETAILS", caseDetailsArray);
								
								
					sql = "select * from ecourts_case_acts where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray actsListArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
						JSONObject actsList = new JSONObject();
						actsList.put("ACT",entry.get("act") !=null ? entry.get("act").toString() :"");
						actsList.put("ACT_NAME",entry.get("actname") !=null ? entry.get("actname").toString() :"");
						actsList.put("SECTION",entry.get("section") !=null ? entry.get("section").toString() :"");

						actsListArray.put(actsList);
						}
					}
					    
					casesData.put("ACTS_LIST", actsListArray);
					
					
					
					sql = "select  * from apolcms.ecourts_pet_extra_party where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray petListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");

							petListArray.put(respList);
						}
					}
					
					casesData.put("PETITIONERS_LIST", petListArray);
					
					
					/* Respondent's List */
					
					sql = "select b.party_no,b.res_name as party_name, b.address from nic_resp_addr_data b left join ecourts_res_extra_party a on (b.cino=a.cino and b.party_no-1=coalesce(trim(a.party_no),'0')::int4) where b.cino='" + cino + "' order by b.party_no";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray respListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");
							respList.put("ADDRESS",entry.get("address") !=null ? entry.get("address").toString() :"");
							respListArray.put(respList);
						}
					}
					
					casesData.put("RESPONDENTS_LIST", respListArray);
					
					
					// Dept. Instructions
					sql = "select instructions, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_dept_instructions where cino='" + cino + "'  order by 1 ";
					System.out.println("Dept INstructions sql--" + sql);
					List<Map<String, Object>> existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray instructionsArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("instructions") !=null ? entry.get("instructions").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							instructionsArray.put(obj);
						}
					}
					
					casesData.put("INSTRUCTIONS", instructionsArray);
					
					
					// Daily Case Status Updates by GP
					sql = "select status_remarks, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_gpo_daily_status where cino='" + cino + "'  order by 1 ";
					System.out.println("DAILY STATUS SQL--" + sql);
					existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray dailyStatusArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("status_remarks") !=null ? entry.get("status_remarks").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							dailyStatusArray.put(obj);
						}
					}
					
					casesData.put("DAILY_CASE_STATUS", dailyStatusArray);
					
					
					
					//IA filings list
					sql = "select  * from apolcms.ecourts_case_iafiling where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray iaFilingsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("IA_NO",entry.get("ia_no") !=null ? entry.get("ia_no").toString() :"");
							obj.put("IA_PET_NAME",entry.get("ia_pet_name") !=null ? entry.get("ia_pet_name").toString() :"");
							obj.put("IA_PET_DISPOSAL",entry.get("ia_pend_disp") !=null ? entry.get("ia_pend_disp").toString() :"");
							obj.put("DATE_OF_FILING",entry.get("date_of_filing") !=null ? entry.get("date_of_filing").toString() :"");
							

							iaFilingsArray.put(obj);
						}
					}
					
					casesData.put("IA_FILINGS_LIST", iaFilingsArray);
					
					
					
					//Parawise remarks history and Counter Affidavit History
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("PARA WISE REMARKS SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray pwrArray = new JSONArray();
					JSONArray counterArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							if(entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Parawise Remarks"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");

								pwrArray.put(obj);
							}
							else if (entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Counter"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
								counterArray.put(obj);
							}
						}
					}
					
					casesData.put("PARA_WISE_REMARKS_HISTORY", pwrArray);
					casesData.put("COUNTER_AFFIDAVIT_HISTORY", counterArray);
					
					
					//INTERIM ORDERS LIST
					sql = "select  * from apolcms.ecourts_case_interimorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray interimOrderArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH","https://apolcms.ap.gov.in/HighCourtsCaseOrders/"+entry.get("cino").toString()+"-interimorder-"+entry.get("order_no").toString()+".pdf");

							interimOrderArray.put(obj);
						}
					}
					
					casesData.put("INTERIM_ORDERS_LIST", interimOrderArray);
					
					
					//Tagged along cases List or Linked cases list
					sql = "select  * from apolcms.ecourts_case_link_cases where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray linkedCasesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("FILING_NO",entry.get("filing_number") !=null ? entry.get("filing_number").toString() :"");
							obj.put("CASE_NO",entry.get("case_number") !=null ? entry.get("case_number").toString() :"");
							

							linkedCasesArray.put(obj);
						}
					}
					
					casesData.put("TAGGED_ALONG_CASES_LIST", linkedCasesArray);
					
					
					
					//Objections History
					sql = "select  * from apolcms.ecourts_case_objections where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray objectionsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("OBJ_NO",entry.get("objection_no") !=null ? entry.get("objection_no").toString() :"");
							obj.put("OBJ_DESC",entry.get("objection_desc") !=null ? entry.get("objection_desc").toString() :"");
							obj.put("SCRUTINY_DATE",entry.get("scrutiny_date") !=null ? entry.get("scrutiny_date").toString() :"");
							obj.put("COMPLIANCE_DATE",entry.get("objections_compliance_by_date") !=null ? entry.get("objections_compliance_by_date").toString() :"");
							obj.put("RECEIPT_DATE",entry.get("obj_reciept_date") !=null ? entry.get("obj_reciept_date").toString() :"");
							
							objectionsArray.put(obj);
						}
					}
					
					casesData.put("OBJECTIONS_HISTORY", objectionsArray);
		
					
					//Case History Data
					sql = "select  * from apolcms.ecourts_historyofcasehearing where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseHistoryArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("JUDGE_NAME",entry.get("judge_name") !=null ? entry.get("judge_name").toString() :"");
							obj.put("BUSINESS_DATE",entry.get("business_date") !=null ? entry.get("business_date").toString() :"");
							obj.put("HEARING_DATE",entry.get("hearing_date") !=null ? entry.get("hearing_date").toString() :"");
							obj.put("PURPOSE",entry.get("purpose_of_listing") !=null ? entry.get("purpose_of_listing").toString() :"");
							obj.put("CAUSE_TYPE",entry.get("causelist_type") !=null ? entry.get("causelist_type").toString() :"");
							
							caseHistoryArray.put(obj);
						}
					}
					
					casesData.put("CASE_HISTORY", caseHistoryArray);
					
					
					//Case Activities Data
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("ecourts activities SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseActivitiesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
							obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
							obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
							obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
							obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
							obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null && !entry.get("uploaded_doc_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
							
							caseActivitiesArray.put(obj);
						}
					}
					
					casesData.put("CASE_ACTIVITIES_LIST", caseActivitiesArray);
					
					
					//Final Orders List
					sql = "select  * from apolcms.ecourts_case_finalorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray finalOrdersArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH", entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("order_document_path").toString():"");
							
							finalOrdersArray.put(obj);
						}
					}
					
					casesData.put("FINAL_ORDERS_LIST", finalOrdersArray);
					
					
					String finalString = casesData.toString();

					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases details retrieved successfully\"  , "
								+ finalString.substring(1, finalString.length() - 1) + "}}";
					
					
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
	@Path("/viewGPCasesList")
	public static Response viewGPCasesList(String incomingData) throws Exception {

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
				} else if (!jObject.has("PWR_COUNTER_FLAG") || jObject.get("PWR_COUNTER_FLAG").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- PWR_COUNTER_FLAG is missing in the request.\" }}";
				} else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="", pwrCounterFlag="",condition="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					pwrCounterFlag = jObject.get("PWR_COUNTER_FLAG").toString();
					con = DatabasePlugin.connect();
					
					
					if(roleId!=null && roleId.equals("6")) { // GPO
						
						condition=" and a.case_status=6 and e.gp_id='"+userid+"' ";
						
						if(pwrCounterFlag.equals("PR")) {
							condition+=" and (pwr_uploaded='No' or pwr_uploaded='Yes') and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' )";
						}
						if(pwrCounterFlag.equals("COUNTER")) {
							condition+=" and pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='Yes' and (counter_filed='No' or counter_filed='Yes') and coalesce(counter_approved_gp,'F')='F'";
						}
					}
					
					sql = "select type_name_reg, reg_no, reg_year, to_char(dt_regis,'dd-mm-yyyy') as dt_regis, a.cino, case when length(scanned_document_path) > 10 then scanned_document_path else '-' end as scanned_document_path from ecourts_case_data a "
							+ " left join ecourts_olcms_case_details od on (a.cino=od.cino)"
							+ " left join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where assigned=true "+condition
							+ " and coalesce(a.ecourts_case_status,'')!='Closed' "+condition;
							
					
					sql	+= "order by reg_year,type_name_reg,reg_no";
					
						System.out.println("GP CASES SQL:" + sql);
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						JSONArray finalList = new JSONArray();
						
					if (data != null && !data.isEmpty() && data.size() > 0) {
						for (Map<String, Object> entry : data) {		
						    
					    	JSONObject cases = new JSONObject();
					    	cases.put("case_type", entry.get("type_name_reg").toString());
					    	String caseno = entry.get("type_name_reg").toString()+" "+entry.get("reg_no").toString()+"/"+entry.get("reg_year").toString();
					    	cases.put("case_no", caseno);						    	
					    	cases.put("case_reg_date", entry.get("dt_regis").toString());
					    	cases.put("status", "Pending");
					    	cases.put("cino", entry.get("cino").toString());
					    	finalList.put(cases);
					}
					JSONObject casesData = new JSONObject();
					casesData.put("CASES_LIST", finalList);
					String finalString = casesData.toString();
					    
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
					
						
					} else {
						JSONObject casesData = new JSONObject();
						casesData.put("CASES_LIST", finalList);
						String finalString = casesData.toString();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
					}				

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
	