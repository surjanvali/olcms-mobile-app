package in.apcfss.olcms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

import in.apcfss.struts.commons.CommonModels;
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

@Path("/pendingNewCasesApproval")
public class AssignNewCasesToSection {
			
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {

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
					String sql = null, sqlCondition = "",roleId="", distId="", deptCode="", userId="";
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					con = DatabasePlugin.connect();
					
					
					String condition1 = "";
					
					if(roleId!=null && roleId.equals("4")) { // MLO
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=2 and coalesce(assigned,'f')='t'";
					}
					else if(roleId!=null && roleId.equals("5")) { // NO
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=4 and coalesce(assigned,'f')='t'  ";
					}
					else if(roleId!=null && roleId.equals("8")) { // SECTION OFFICER - SECT. DEPT
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=5 and ad.assigned_to='"+userId+"'";
					}
					else if(roleId!=null && roleId.equals("11")) { // SECTION OFFICER - HOD
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=9 and ad.assigned_to='"+userId+"'";
					}
					else if(roleId!=null && roleId.equals("12")) { // SECTION OFFICER - DISTRICT
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.dist_id='"+distId+"' and ad.case_status=10 and coalesce(assigned,'f')='t'   and ad.assigned_to='"+userId+"'";
					}
					
					
					else if(roleId!=null && roleId.equals("3")) { // SECT DEPT
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=1  and coalesce(assigned,'f')='t'   ";
					}
					else if(roleId!=null && roleId.equals("9")) { // HOD
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=3  and coalesce(assigned,'f')='t' ";
					}
					else if(roleId!=null && roleId.equals("2")) { // DC
						sqlCondition=" and ad.case_status=7 and coalesce(assigned,'f')='t'  and ad.dist_id='"+distId+"'";
					}
					else if(roleId!=null && roleId.equals("10")) { // DC-NO
						sqlCondition=" and ad.dept_code='"+deptCode+"' and ad.case_status=8 and coalesce(assigned,'f')='t'   and ad.dist_id='"+distId+"'";
					}
					

					if(roleId!=null && roleId.equals("6")) { // GPO
						
						condition1 = " inner join ecourts_mst_gp_dept_map emgd on (ad.dept_code=emgd.dept_code) "
								+ " inner join ecourts_olcms_case_details eocd on (eocd.cino=ad.ack_no)";
						
						sqlCondition += " and counter_filed='Yes' and ad.case_status='6' and coalesce(assigned,'f')='t' and ad.assigned_to='"+userId+"' ";
						//cform.setDynaForm("districtId", distCode);
						
						String counter_pw_flag = "";
						
						if (jObject.has("PW_COUNTER_FLAG") && !jObject.get("PW_COUNTER_FLAG").toString().equals("")
								&& !jObject.get("PW_COUNTER_FLAG").toString().equals("0")) {
							counter_pw_flag = CommonModels.checkStringObject(jObject.get("PW_COUNTER_FLAG").toString());
						}
						
						
						if(counter_pw_flag.equals("PR")) {
							// pwr_uploaded='No' and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' ) and ecd.case_status='6'
							condition1+=" and pwr_uploaded='No' and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' )";
						}
						if(counter_pw_flag.equals("COUNTER")) {
							//pwr_uploaded='Yes' and counter_filed='No' and coalesce(counter_approved_gp,'F')='F' and ecd.case_status='6'
							condition1+=" and pwr_uploaded='Yes' and counter_filed='No' and coalesce(counter_approved_gp,'F')='F'";
						}
					}
					
					
					sql = "select a.slno ,ad.respondent_slno, a.ack_no , distid , advocatename ,advocateccno , casetype , maincaseno , a.remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
							+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
							+ "to_char(a.inserted_time,'dd-mm-yyyy') as generated_date, "
							+ "getack_dept_desc(a.ack_no::text) as dept_descs , coalesce(a.hc_ack_no,'-') as hc_ack_no "
							+ " from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
							+ " left join district_mst dm on (ad.dist_id=dm.district_id) "
							+ " left join dept_new dmt on (ad.dept_code=dmt.dept_code)"
							+ " inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name) "+condition1+"   "
							+ " where a.delete_status is false and ack_type='NEW'  and coalesce(ad.ecourts_case_status,'')!='Closed'  " + sqlCondition
							+ " order by a.inserted_time desc";
					
					System.out.println("CASES SQL:" + sql);
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
						
					if (data != null && !data.isEmpty() && data.size() > 0) {
						for (Map<String, Object> entry : data) {		
						    
					    	JSONObject cases = new JSONObject();
					    	cases.put("ACK_NO", entry.get("ack_no"));
					    	if (entry.get("ack_no") != null)
							{
								if (entry.get("hc_ack_no")!=null && !entry.get("hc_ack_no").equals("-")) {
									cases.put("HC_ACK_NO", entry.get("hc_ack_no").toString());
								}
								else if (entry.get("hc_ack_no")!=null && entry.get("hc_ack_no").equals("-")) {
									cases.put("HC_ACK_NO", "");
								}
							}
							
							cases.put("DATE", entry.get("generated_date"));
							cases.put("DIST_NAME", entry.get("district_name"));
							cases.put("CASE_TYPE", entry.get("case_full_name"));
							cases.put("MAIN_CASE_NO", entry.get("maincaseno"));
							cases.put("DEPARTMENTS", entry.get("dept_descs"));
							cases.put("ADVOCATE_CCNO", entry.get("advocateccno"));
							cases.put("ADVOCATE_NAME", entry.get("advocatename"));
							cases.put("RESPONDENT_SLNO", entry.get("respondent_slno"));
							cases.put("ACK_FILE_PATH", "https://apolcms.ap.gov.in/"+entry.get("ack_file_path"));
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
	@Path("/updateStatus")
	public static Response updateStatus(String incomingData) throws Exception {

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
				} else if (!jObject.has("ACK_NO") || jObject.get("ACK_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- ACK_NO is missing in the request.\" }}";
				} else if (!jObject.has("RESPONDENT_NO") || jObject.get("RESPONDENT_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- RESPONDENT_NO is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", condition="",roleId="", distId="", deptCode="", userId="";
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					String cIno = jObject.get("ACK_NO").toString();
					String respNo = jObject.get("RESPONDENT_NO").toString();
					
					con = DatabasePlugin.connect();
					
					JSONArray buttonsList = new JSONArray();
					JSONObject jsonObject = new JSONObject();
					JSONObject casesData = new JSONObject();
					
					
					if (roleId.equals("2")) {
						sqlCondition += " and ad.dist_id='" + distId + "' ";
						//cform.setDynaForm("districtId", distCode);
					}
					if (roleId.equals("3")) {// Secretariat Department
						sqlCondition += " and (dmt.dept_code='" + deptCode + "' or dmt.reporting_dept_code='" + deptCode + "') ";
					}
					if (roleId.equals("9")) {// Secretariat Department
						sqlCondition += " and (dmt.dept_code='" + deptCode + "') ";
					}
					if (roleId.equals("8")) {
						sqlCondition += " and ad.case_status='5' ";
						//cform.setDynaForm("districtId", distCode);
					}
					if (roleId.equals("11")) {
						sqlCondition += " and ad.case_status='9' ";
						//cform.setDynaForm("districtId", distCode);
					}
					if (roleId.equals("12")) {
						sqlCondition += " and ad.case_status='10' ";
						//cform.setDynaForm("districtId", distCode);
					}
					
					
					sql = "select slno , a.ack_no , distid , advocatename ,advocateccno , casetype , maincaseno , remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
							+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
							+ "to_char(inserted_time,'dd-mm-yyyy') as generated_date, getack_dept_desc(a.ack_no) as dept_descs,ad.assigned,ad.assigned_to,ad.case_status,ad.ecourts_case_status,ad.section_officer_updated,ad.mlo_no_updated  "
							+ "from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no ) "
							+ "left join district_mst dm on (a.distid=dm.district_id) "
							+ "left join dept_new dmt on (ad.dept_code=dmt.dept_code)"
							+ "inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name) "
							+ " where a.delete_status is false and ack_type='NEW' and respondent_slno='"+respNo+"' " + sqlCondition 
							+ " and a.ack_no='"+cIno+"'  order by inserted_time desc";
					
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray caseDetailsArray = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {
						
						for (Map<String, Object> entry : data) {

							JSONObject obj = new JSONObject();
							obj.put("REPORTED_BY", entry.get("inserted_by"));
							obj.put("REGISTRATION_NO", entry.get("ack_no"));
							obj.put("DIST_NAME", entry.get("district_name"));
							obj.put("ADVOCATE_NAME", entry.get("advocatename"));
							obj.put("ADVOCATE_NO", entry.get("advocateccno"));
							obj.put("CASE_FULL_NAME", entry.get("case_full_name"));
							obj.put("GENERATED_DATE", entry.get("generated_date"));
							obj.put("MAIN_CASE_NO", entry.get("maincaseno"));
							obj.put("DEPARTMENT_DESC", entry.get("dept_descs"));
							
							caseDetailsArray.put(obj);

						}

						casesData.put("CASE_DETAILS", caseDetailsArray);
						
						
						Map caseData1 = (Map)data.get(0);
						
						if(roleId!=null && (roleId.equals("4") || roleId.equals("5") || roleId.equals("10"))) {
							jsonObject.put("SHOWBACKBTN", "TRUE");
						}
						else {
							jsonObject.put("SHOWBACKBTN", "FALSE");
						}
						
						if(roleId!=null && roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
							if(CommonModels.checkStringObject(caseData1.get("section_officer_updated")).equals("T")) {
								System.out.println("dept code-3,5:"+deptCode.substring(3, 5));
								
								if(deptCode.substring(3, 5)=="01" || deptCode.substring(3, 5).equals("01")) {
									
									jsonObject.put("SHOWMLOBTN", "TRUE");
								}
								else {
									jsonObject.put("SHOWNOBTN", "TRUE");
								}
							}
						}
						else if(roleId!=null && roleId.equals("4") && CommonModels.checkStringObject(caseData1.get("mlo_no_updated")).equals("T")) { 
							// MLO TO SECT DEPT
							jsonObject.put("SHOWSECDEPTBTN", "TRUE");
						}
						else if(roleId!=null && (roleId.equals("5") || roleId.equals("10")) && CommonModels.checkStringObject(caseData1.get("mlo_no_updated")).equals("T")) { 
							// NO TO HOD/DEPT
							jsonObject.put("SHOWHODDEPTBTN", "TRUE");
						}
						else if((roleId.equals("3") || roleId.equals("9")) && CommonModels.checkStringObject(caseData1.get("mlo_no_updated")).equals("T")) {
		
							sql = "select emailid, full_name||' ('|| replace(emailid,'@ap.gov.in','') ||')' as display_name from ecourts_mst_gps a inner join ecourts_mst_gp_dept_map b on (a.emailid=b.gp_id) where b.dept_code='"+deptCode+"' order by emailid";
							
							data = DatabasePlugin.executeQuery(sql, con);
							
							JSONArray gpArray = new JSONArray();
							if (data != null && !data.isEmpty() && data.size() > 0) {
								for (Map<String, Object> entry : data) {
									JSONObject obj = new JSONObject();
									obj.put("EMAIL_ID",entry.get("emailid") !=null ? entry.get("emailid").toString() :"");
									obj.put("DISPLAY_NAME",entry.get("display_name") !=null ? entry.get("display_name").toString() :"");
									
									gpArray.put(obj);
								}
							}
							
							casesData.put("GP_LIST", gpArray);							
							jsonObject.put("SHOWGPBTN", "TRUE");
						}
						else if(roleId.equals("6") ) { // GP LOGIN
							jsonObject.put("SHOWGPAPPROVEBTN", "TRUE");
						}
						
						buttonsList.put(jsonObject);
						casesData.put("BUTTONS_LIST", buttonsList);
					}
				
					
					sql="select cino,action_type,inserted_by,inserted_on,assigned_to,remarks as remarks, "
							+ "    CASE  WHEN length(trim(uploaded_doc_path)) > 10 THEN uploaded_doc_path else '---'  end as uploaded_doc_path from ecourts_case_activities where cino = '"+cIno+"' order by inserted_on";
					System.out.println("ecourts activities SQL:" + sql);
					data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray acitivitiesArray = new JSONArray();

					if (data != null && !data.isEmpty() && data.size() > 0) {
						for (Map<String, Object> entry : data) {

							JSONObject obj = new JSONObject();
							obj.put("DATE", entry.get("inserted_on"));							
							obj.put("ACTIVITY", entry.get("action_type"));
							obj.put("UDPATED_BY", entry.get("inserted_by"));
							obj.put("ASSIGNED_TO", entry.get("assigned_to"));
							obj.put("REMARKS", entry.get("remarks"));
							obj.put("UPLOADED_DOC_PATH", entry.get("uploaded_doc_path"));
							
							acitivitiesArray.put(obj);

						}						
						casesData.put("ACTIVITIES_DATA", acitivitiesArray);
					}
					
					
					String	sql_set = "SELECT cino, case when length(petition_document) > 0 then petition_document else null end as petition_document, "
							+ " case when length(counter_filed_document) > 0 then counter_filed_document else null end as counter_filed_document,"
							+ " case when length(judgement_order) > 0 then judgement_order else null end as judgement_order,"
							+ " case when length(action_taken_order) > 0 then action_taken_order else null end as action_taken_order,"
							+ " last_updated_by, last_updated_on, counter_filed, remarks, ecourts_case_status, corresponding_gp, "
							+ " pwr_uploaded, to_char(pwr_submitted_date,'mm/dd/yyyy') as pwr_submitted_date, to_char(pwr_received_date,'mm/dd/yyyy') as pwr_received_date, "
							+ " pwr_approved_gp, to_char(pwr_gp_approved_date,'mm/dd/yyyy') as pwr_gp_approved_date, appeal_filed, "
							+ " appeal_filed_copy, to_char(appeal_filed_date,'mm/dd/yyyy') as appeal_filed_date, pwr_uploaded_copy "
							+ " FROM apolcms.ecourts_olcms_case_details where cino='" + cIno + "' and respondent_slno='"+respNo+"' ";

						data = DatabasePlugin.executeQuery(sql_set, con);

						JSONArray jsonArray = new JSONArray();

						if (data != null && !data.isEmpty() && data.size() > 0) {
							for (Map<String, Object> entry : data) {

								JSONObject obj = new JSONObject();
								obj.put("COUNTER_FILED_DOC", CommonModels.checkStringObject(entry.get("counter_filed_document")) != "" 
										? "https://apolcms.ap.gov.in/" + entry.get("counter_filed_document")
										: "");
								obj.put("JUDGEMENT_ORDER", CommonModels.checkStringObject(entry.get("judgement_order")) != ""
										? "https://apolcms.ap.gov.in/" + entry.get("judgement_order")
										: "");
								obj.put("ACTION_TAKEN_ORDER", CommonModels.checkStringObject(entry.get("action_taken_order")) != ""
										? "https://apolcms.ap.gov.in/" + entry.get("action_taken_order")
										: "");
								obj.put("COUNTER_FILED", entry.get("counter_filed"));
								obj.put("REMARKS", entry.get("remarks"));
								obj.put("CASE_STATUS", entry.get("ecourts_case_status"));
								obj.put("PWR_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("PWR_UPLOADED_COPY", CommonModels.checkStringObject(entry.get("pwr_uploaded_copy")) != ""
										? "https://apolcms.ap.gov.in/" + entry.get("pwr_uploaded_copy")
										: "");
								obj.put("DATE_OF_PWR_SUBMISSION", entry.get("pwr_submitted_date"));
								obj.put("PWR_RECEIVED_DATE", entry.get("pwr_received_date"));
								obj.put("PWR_APPROVED_BY_GP", entry.get("pwr_approved_gp"));
								obj.put("PWR_GP_APPROVED_DATE", entry.get("pwr_gp_approved_date"));
								obj.put("APPEAL_FILED", entry.get("appeal_filed"));
								obj.put("APPEAL_FILED_COPY", entry.get("appeal_filed_copy"));
								obj.put("APPEAL_FILED_DATE", entry.get("appeal_filed_date"));
								
								jsonArray.put(obj);

							}

							
							casesData.put("CASES_LIST", jsonArray);
							String finalString = casesData.toString();

							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";

						} else {
							
							casesData.put("CASES_LIST", jsonArray);
							String finalString = casesData.toString();
							
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1,finalString.length()-1)+" }}";
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
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Path("/updateCaseDetails")
	public Response updateCaseDetails(@FormDataParam("petitionDocument") InputStream petitionDoc,
			@FormDataParam("petitionDocument") FormDataBodyPart petitionDocBody, @FormDataParam("actionTakenOrder") InputStream actionTakenOrderDoc,
			@FormDataParam("actionTakenOrder") FormDataBodyPart actionTakenOrderDocBody, @FormDataParam("judgementOrder") InputStream judgementOrderDoc,
			@FormDataParam("judgementOrder") FormDataBodyPart judgementOrderDocBody, @FormDataParam("appealFiledDocument") InputStream appealFiledDoc,
			@FormDataParam("appealFiledDocument") FormDataBodyPart appealFiledDocBody, @FormDataParam("ackno") String cino,
			@FormDataParam("userId") String userId, @FormDataParam("roleId") String roleId,@FormDataParam("deptCode") String deptCode,
			@FormDataParam("remarks") String remarks,@FormDataParam("appealFiledFlag") String appealFiledFlag,
			@FormDataParam("ecourtsCaseStatus") String ecourtsCaseStatus, @FormDataParam("actionToPerform") String actionToPerform,
			@FormDataParam("appealFiledDate") String appealFiledDate,
			@FormDataParam("counterFiledFlag") String counterFiledFlag,
			@FormDataParam("counterFiledDoc") InputStream counterFiledDoc,
			@FormDataParam("counterFiledDoc") FormDataBodyPart counterFiledDocBody,
			@FormDataParam("paraWiseRemarksFlag") String paraWiseRemarksFlag,
			@FormDataParam("paraWiseRemarksDoc") InputStream paraWiseRemarksDoc,
			@FormDataParam("paraWiseRemarksDoc") FormDataBodyPart paraWiseRemarksDocBody,
			@FormDataParam("pwrSubmittedDate") String pwrSubmittedDate,
			@FormDataParam("pwrReceivedDate") String pwrReceivedDate,
			@FormDataParam("pwrApprovedGP") String pwrApprovedGP,
			@FormDataParam("pwrGPApprovedDate") String pwrGPApprovedDate, 
			@FormDataParam("respondentId") String respondentId) throws Exception {
		
		Connection con = null;
		String jsonStr = "";
		String sql = "";

		try {
			
			if (cino == null || cino.equals("")) {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ackno is missing in the request.\" }}";
			} 
			else if (ecourtsCaseStatus == null || ecourtsCaseStatus.equals("")) {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ecourtsCaseStatus is missing in the request.\" }}";
			}
			else {	
			if (cino != null && !cino.equals("")) {
				
				con = DatabasePlugin.connect();
				con.setAutoCommit(false);
				
				String newFileName="";
				
				String actionPerformed="";
				actionPerformed = !CommonModels.checkStringObject(actionToPerform).equals("") && !CommonModels.checkStringObject(actionToPerform).equals("0") ?  actionToPerform   : "CASE DETAILS UPDATED";
				
				
				String sqlCondition2="";
				String petitionFileUploadPath="", actionFileUploadPath="", judgementFileUploadPath="", appealFileUploadPath="", counter_filed_document="", pwr_uploaded_copy="";
				
				// This section uploads the petition document in the server
				
				if(ecourtsCaseStatus != null && ecourtsCaseStatus.equals("Closed")) {
					
					if(actionTakenOrderDoc!=null  && !actionTakenOrderDoc.equals("") && actionTakenOrderDocBody != null && !actionTakenOrderDocBody.getFormDataContentDisposition().getFileName().equals("")) {
						
						
						newFileName="actionorder_"+CommonModels.randomTransactionNo()+"."+actionTakenOrderDocBody.getMediaType().getSubtype();
						
						String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/actionorder/"+newFileName;
						
						actionFileUploadPath = "uploads/actionorder/"+newFileName;
						
						writeToFile(actionTakenOrderDoc, uploadedFileLocation);
						
						
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
								+ "values ('" + cino + "','Uploaded Action Taken Order','"+userId+"', '"+remarks+"', '"+actionFileUploadPath+"')";
						DatabasePlugin.executeUpdate(sql, con);
						
						sqlCondition2 = ", action_taken_order='"+actionFileUploadPath+"'";
					}
					
					if(judgementOrderDoc!=null  && !judgementOrderDoc.equals("") && judgementOrderDocBody != null && !judgementOrderDocBody.getFormDataContentDisposition().getFileName().equals("")) {
						
						
						newFileName="judgementorder_"+CommonModels.randomTransactionNo()+"."+judgementOrderDocBody.getMediaType().getSubtype();
						
						String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/judgementorder/"+newFileName;
						
						judgementFileUploadPath = "uploads/judgementorder/"+newFileName;
						
						writeToFile(judgementOrderDoc, uploadedFileLocation);
						
						
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
								+ "values ('" + cino + "','Uploaded Judgement Order','"+userId+"', '"+remarks+"', '"+judgementFileUploadPath+"')";
						DatabasePlugin.executeUpdate(sql, con);
						
						sqlCondition2 = ", judgement_order='"+judgementFileUploadPath+"'";
					}
					
					
					if(appealFiledFlag != null && appealFiledFlag.equals("Yes") && appealFiledDoc !=null  && !appealFiledDoc.equals("") && appealFiledDocBody != null && !appealFiledDocBody.getFormDataContentDisposition().getFileName().equals("")) {
						
						
						newFileName="appealcopy_"+CommonModels.randomTransactionNo()+"."+appealFiledDocBody.getMediaType().getSubtype();
						
						String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/appealcopies/"+newFileName;
						
						appealFileUploadPath = "uploads/appealcopies/"+newFileName;
						
						writeToFile(appealFiledDoc, uploadedFileLocation);
						
						
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
								+ "values ('" + cino + "','Uploaded Appeal Copy','"+userId+"', '"+remarks+"', '"+appealFileUploadPath+"')";
						DatabasePlugin.executeUpdate(sql, con);
						
						sqlCondition2 = ", appeal_filed_copy='"+appealFileUploadPath+"'";
					}
					
					
					if(Integer.parseInt(DatabasePlugin.getSingleValue(con, "select count(*) from ecourts_olcms_case_details where cino='"+cino+"'")) > 0) {
						
						sql="insert into ecourts_olcms_case_details_log (cino ,  petition_document   ,    counter_filed_document  ,    judgement_order  , action_taken_order  ,last_updated_by ,  last_updated_on ,  counter_filed , remarks  , "
								+ "ecourts_case_status ,  corresponding_gp, pwr_uploaded , pwr_submitted_date, pwr_received_date , pwr_approved_gp ,  pwr_gp_approved_date , appeal_filed ,  appeal_filed_copy, appeal_filed_date, pwr_uploaded_copy, "
								+ " counter_approved_gp ,action_to_perfom, counter_approved_date ,counter_approved_by , respondent_slno, cordered_impl_date , dismissed_copy , final_order_status  ,no_district_updated)"
								+ " select cino ,  petition_document   ,    counter_filed_document  ,    judgement_order  , action_taken_order  ,last_updated_by ,  last_updated_on ,  counter_filed , remarks  ,"
								+ "ecourts_case_status ,  corresponding_gp, pwr_uploaded , pwr_submitted_date, pwr_received_date , pwr_approved_gp ,  pwr_gp_approved_date , appeal_filed ,  appeal_filed_copy, appeal_filed_date, pwr_uploaded_copy, "
								+ " counter_approved_gp ,action_to_perfom, counter_approved_date ,counter_approved_by , respondent_slno, cordered_impl_date , dismissed_copy , final_order_status  ,no_district_updated from ecourts_olcms_case_details where cino='"+cino+"'";
						
						DatabasePlugin.executeUpdate(sql, con);
						
						sql = "update ecourts_olcms_case_details set ecourts_case_status='"
								+ ecourtsCaseStatus + "', appeal_filed='"
								+ appealFiledFlag + "',appeal_filed_date=to_date('"
								+ CommonModels.checkStringObject(appealFiledDate) + "','mm/dd/yyyy'), remarks='"
								+ remarks + "', last_updated_by='" + userId
								+ "', last_updated_on=now(), action_to_perfom='"+actionToPerform
								+"' " + sqlCondition2 + " where cino='" + cino + "' and respondent_slno='"+respondentId+"'";
					}
					else {
						
						
						sql = "insert into ecourts_olcms_case_details (cino, ecourts_case_status, petition_document,counter_filed_document,pwr_uploaded_copy, appeal_filed, appeal_filed_copy, judgement_order, action_taken_order"
								+ ", last_updated_by, last_updated_on, remarks, appeal_filed_date, action_to_perfom,respondent_slno) "
								+ " values ('" + cino + "', '" 
								+ ecourtsCaseStatus + "', '"
								+ petitionFileUploadPath + "','" 
								+ counter_filed_document + "','" 
								+ pwr_uploaded_copy + "','" 
								+ appealFiledFlag + "', '"
								+ appealFileUploadPath + "', '" 
								+ judgementFileUploadPath + "', '" 
								+ actionFileUploadPath + "', '" 
								+ userId + "', now(),'" 
								+ remarks + "',to_date('"
								+ CommonModels.checkStringObject(appealFiledDate)+"','mm/dd/yyyy'),"
										+ " '"+actionToPerform+"','"+respondentId+"')";
					}
					
					
					int a = DatabasePlugin.executeUpdate(sql, con);
					
					sql="update ecourts_gpo_ack_depts set ecourts_case_status='"+ecourtsCaseStatus+"',section_officer_updated='T' "
							+ " where ack_no='"+cino+"' and dept_code='"+deptCode+"' and respondent_slno='"+respondentId+"'  ";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks ) "
							+ "values ('" + cino + "','"+actionPerformed+"','"+userId+"', '"+remarks+"')";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					if (a > 0) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case details updated successfully\" }}";
						con.commit();
					} else {
						con.rollback();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Error while updating the case details\" }}";
					}
					
				
				}
				else if(ecourtsCaseStatus != null && ecourtsCaseStatus.equals("Pending")){
					String counterFileUploadPath = "";
					String pwrFileUploadPath = "";
					
					
					if(counterFiledFlag!=null && counterFiledFlag.toString().equals("Yes") 
							&& counterFiledDoc!=null  && !counterFiledDoc.equals("") && counterFiledDocBody != null && counterFiledDocBody.getFormDataContentDisposition().getFileName().equals("")) {
						
						newFileName="counter_"+CommonModels.randomTransactionNo()+"."+counterFiledDocBody.getMediaType().getSubtype();
						
						String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/counters/"+newFileName;
						
						counterFileUploadPath = "uploads/counters/"+newFileName;
						
						writeToFile(counterFiledDoc, uploadedFileLocation);
						
						
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
								+ "values ('" + cino + "','Uploaded Counter','"+userId+"', '"+remarks+"', '"+counterFileUploadPath+"')";
						DatabasePlugin.executeUpdate(sql, con);
						
						sqlCondition2 = ", counter_filed_document='"+counterFileUploadPath+"'";
					}
					
					
					if(paraWiseRemarksFlag!=null && paraWiseRemarksFlag.toString().equals("Yes") 
							&& paraWiseRemarksDoc!=null  && !paraWiseRemarksDoc.equals("") && paraWiseRemarksDocBody != null && paraWiseRemarksDocBody.getFormDataContentDisposition().getFileName().equals("")) {
						
						newFileName="parawiseremarks_"+CommonModels.randomTransactionNo()+"."+paraWiseRemarksDocBody.getMediaType().getSubtype();
						
						String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/parawiseremarks/"+newFileName;
						
						pwrFileUploadPath = "uploads/parawiseremarks/"+newFileName;
						
						writeToFile(paraWiseRemarksDoc, uploadedFileLocation);
						
						
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
								+ "values ('" + cino + "','Uploaded Parawise Remarks','"+userId+"', '"+remarks+"', '"+pwrFileUploadPath+"')";
						DatabasePlugin.executeUpdate(sql, con);
						
						sqlCondition2 = ", pwr_uploaded_copy='"+pwrFileUploadPath+"'";
					}
					
					
					if(Integer.parseInt(DatabasePlugin.getSingleValue(con, "select count(*) from ecourts_olcms_case_details where cino='"+cino+"'")) > 0) {
						
						sql="insert into ecourts_olcms_case_details_log (cino ,  petition_document   ,    counter_filed_document  ,    judgement_order  , action_taken_order  ,last_updated_by ,  last_updated_on ,  counter_filed , remarks  , "
								+ "ecourts_case_status ,  corresponding_gp, pwr_uploaded , pwr_submitted_date, pwr_received_date , pwr_approved_gp ,  pwr_gp_approved_date , appeal_filed ,  appeal_filed_copy, appeal_filed_date, pwr_uploaded_copy, "
								+ " counter_approved_gp ,action_to_perfom, counter_approved_date ,counter_approved_by , respondent_slno, cordered_impl_date , dismissed_copy , final_order_status  ,no_district_updated)"
								+ " select cino ,  petition_document   ,    counter_filed_document  ,    judgement_order  , action_taken_order  ,last_updated_by ,  last_updated_on ,  counter_filed , remarks  ,"
								+ "ecourts_case_status ,  corresponding_gp, pwr_uploaded , pwr_submitted_date, pwr_received_date , pwr_approved_gp ,  pwr_gp_approved_date , appeal_filed ,  appeal_filed_copy, appeal_filed_date, pwr_uploaded_copy, "
								+ " counter_approved_gp ,action_to_perfom, counter_approved_date ,counter_approved_by , respondent_slno, cordered_impl_date , dismissed_copy , final_order_status  ,no_district_updated from ecourts_olcms_case_details where cino='"+cino+"'";
						
						DatabasePlugin.executeUpdate(sql, con);
						
						sql = "update ecourts_olcms_case_details set ecourts_case_status='"
								+ ecourtsCaseStatus + "', counter_filed='"
								+ counterFiledFlag + "', remarks='" + remarks
								+ "', last_updated_by='" + userId + "', last_updated_on=now() " + sqlCondition2
								+ ", corresponding_gp='" + "" + "', pwr_uploaded='"
								+ paraWiseRemarksFlag + "', pwr_submitted_date=to_date('"
								+ CommonModels.checkStringObject(pwrSubmittedDate)
								+ "','mm/dd/yyyy'), pwr_received_date=to_date('"
								+ CommonModels.checkStringObject(pwrReceivedDate) + "','mm/dd/yyyy'),pwr_approved_gp='"
								+ pwrApprovedGP + "',"
								+ " pwr_gp_approved_date=to_date('" + CommonModels.checkStringObject(pwrGPApprovedDate)
								+ "','mm/dd/yyyy'), action_to_perfom='"+actionToPerform  + "' where cino='" + cino + "' and respondent_slno='"+respondentId+"'";
						
					}
					else {
						
						sql = "insert into ecourts_olcms_case_details (cino, ecourts_case_status, petition_document, counter_filed_document, last_updated_by, last_updated_on, "
								+ "counter_filed, remarks,  corresponding_gp, pwr_uploaded, pwr_submitted_date, pwr_received_date, pwr_approved_gp, "
								+ "pwr_gp_approved_date, pwr_uploaded_copy, action_to_perfom, respondent_slno) "
								+ " values ('" + cino + "', '" + ecourtsCaseStatus + "', '"
								+ petitionFileUploadPath + "','" + counterFileUploadPath + "','" + userId + "', now(),'"
								+ counterFiledFlag+ "', '" + remarks + "', '"
								+ "" + "', '"
								+ paraWiseRemarksFlag + "'," 
								+ " to_date('" + CommonModels.checkStringObject(pwrSubmittedDate) + "','mm/dd/yyyy'), " 
								+ " to_date('" + CommonModels.checkStringObject(pwrReceivedDate) + "','mm/dd/yyyy'), '"
								+ pwrApprovedGP + "'," 
								+ " to_date('" + CommonModels.checkStringObject(pwrGPApprovedDate) + "','mm/dd/yyyy'), '" 
								+ pwrFileUploadPath + "','"+actionToPerform+"','"+respondentId+"')";
					
					}
					System.out.println("SQL:"+sql);
					
					int a = DatabasePlugin.executeUpdate(sql, con);
					
					if(roleId!=null && (roleId.equals("4") || roleId.equals("5") || roleId.equals("10"))) {//MLO / NO / Dist-NO
						sql="update ecourts_gpo_ack_depts set ecourts_case_status='"+ecourtsCaseStatus+"', mlo_no_updated='T' "
								+ " where ack_no='"+cino+"' and dept_code='"+deptCode+"' and respondent_slno='"+respondentId+"'  ";
						a += DatabasePlugin.executeUpdate(sql, con);
					}
					else {
						sql="update ecourts_gpo_ack_depts set ecourts_case_status='"+ecourtsCaseStatus+"', section_officer_updated='T' "
								+ " where ack_no='"+cino+"' and dept_code='"+deptCode+"' and respondent_slno='"+respondentId+"'";
						a += DatabasePlugin.executeUpdate(sql, con);
					}
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks ) "
							+ "values ('" + cino + "','"+actionPerformed+"','"+userId+"', '"+remarks+"')";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					if (a > 0) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case details updated successfully\" }}";
						con.commit();
					} else {
						con.rollback();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Error while updating the case details\" }}";
					}
					
				} else if(ecourtsCaseStatus!=null && ecourtsCaseStatus.equals("Private")){
					
						int a=0;
						
						sql="update ecourts_case_data set ecourts_case_status='"+ecourtsCaseStatus+"', case_status='98' "
								+ " where cino='"+cino+"' and dept_code='"+deptCode+"'  "; 
						 a = DatabasePlugin.executeUpdate(sql, con);
					
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks ) "
							+ "values ('" + cino + "','"+actionPerformed+"','"+userId+"', '"+remarks+"')";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					if (a > 0) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case details updated successfully\" }}";
						con.commit();
					} else {
						con.rollback();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Error while updating the case details\" }}";
					}
					
				}				
				
			}
			
			}
			
		}	catch (Exception e) {
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
	@Path("/forwardCaseDetailsToGP")
	public static Response forwardCaseDetailsToGP(String incomingData) throws Exception {
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
				}  else if (!jObject.has("ACK_NO") || jObject.get("ACK_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- ACK_NO is missing in the request.\" }}";
				} else if (!jObject.has("RESPONDENT_ID") || jObject.get("RESPONDENT_ID").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- RESPONDENT_ID is missing in the request.\" }}";
				}else if (!jObject.has("GP_CODE") || jObject.get("GP_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- GP_CODE is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", condition="",roleId="", distId="", deptCode="", userId="",cino="",respondentId="";
					String newStatus = "";
					int a=0;
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					cino = jObject.get("ACK_NO").toString();
					respondentId =  jObject.get("RESPONDENT_ID").toString();
					String fwdOfficer = CommonModels.checkStringObject(jObject.get("GP_CODE").toString());
					String remarks = "";
					
					con = DatabasePlugin.connect();
					
					con.setAutoCommit(false);
					
					if (jObject.has("REMARKS") && !jObject.get("REMARKS").toString().equals("")
							&& !jObject.get("REMARKS").toString().equals("0")) {
						remarks = CommonModels.checkStringObject(jObject.get("REMARKS").toString());
					}
					
					if(roleId!=null && roleId.equals("3")) {//FROM SECT DEPT TO GP.
						newStatus = "6";
						sql="update ecourts_gpo_ack_depts set case_status="+newStatus+", assigned_to='"+fwdOfficer+"' where ack_no='"+cino+"' and section_officer_updated='T' and mlo_no_updated='T' and case_status='1' and dept_code='"+deptCode+"'  and respondent_slno='"+respondentId+"'  ";
						
					} else if(roleId!=null && roleId.equals("9")) {//FROM HOD TO GP
						newStatus = "6";						
						sql="update ecourts_gpo_ack_depts set case_status="+newStatus+", assigned_to='"+fwdOfficer+"' where ack_no='"+cino+"' and section_officer_updated='T' and mlo_no_updated='T' and case_status='3' and dept_code='"+deptCode+"' and respondent_slno='"+respondentId+"'   ";
						
					}
					
					System.out.println("SQL:"+sql);
					a = DatabasePlugin.executeUpdate(sql, con);
					
					if (a > 0) {
						sql="insert into ecourts_case_activities (cino , action_type , inserted_by , assigned_to , remarks) "
								+ " values ('" + cino + "','CASE FORWARDED TO GP', '"+userId+"', '"+fwdOfficer+"', '"+remarks+"')";
						DatabasePlugin.executeUpdate(sql, con);
						con.commit();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case successfully forwarded to the selected GP.\" }}";
					} else {
						con.rollback();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error while forwarding the case to the selected GP.\" }}";
					}			

				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}";
			}
		} catch (Exception e) {
			con.rollback();
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();

	}
	
	
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			
			FileOutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			byte[] bytes = IOUtils.toByteArray(uploadedInputStream);	
			out.write(bytes);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
	