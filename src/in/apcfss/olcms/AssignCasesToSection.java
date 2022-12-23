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

@Path("/pendingApproval")
public class AssignCasesToSection {
			
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
					String sql = null, sqlCondition = "", condition="",roleId="", distId="", deptCode="", userId="";
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					con = DatabasePlugin.connect();
					
					
					
					

					if(roleId!=null && roleId.equals("4")) { // MLO
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=2";
					}
					else if(roleId!=null && roleId.equals("5")) { // NO
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=4";
					}
					else if(roleId!=null && roleId.equals("8")) { // SECTION OFFICER - SECT. DEPT
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=5 and a.assigned_to='"+userId+"'";
					}
					else if(roleId!=null && roleId.equals("11")) { // SECTION OFFICER - HOD
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=9 and a.assigned_to='"+userId+"'";
					}
					else if(roleId!=null && roleId.equals("12")) { // SECTION OFFICER - DISTRICT
						condition=" and a.dept_code='"+deptCode+"' and dist_id='"+distId+"' and a.case_status=10 and a.assigned_to='"+userId+"'";
					}
					
					
					else if(roleId!=null && roleId.equals("3")) { // SECT DEPT
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=1";
					}
					else if(roleId!=null && roleId.equals("9")) { // HOD
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=3";
					}
					else if(roleId!=null && roleId.equals("2")) { // DC
						condition=" and a.case_status=7 and dist_id='"+distId+"'";
					}
					else if(roleId!=null && roleId.equals("10")) { // DC-NO
						condition=" and a.dept_code='"+deptCode+"' and a.case_status=8 and a.dist_id='"+distId+"'";
					}
					else if(roleId!=null && roleId.equals("6")) { // GPO
						
						String counter_pw_flag = "";
						
						if (jObject.has("PW_COUNTER_FLAG") && !jObject.get("PW_COUNTER_FLAG").toString().equals("")
								&& !jObject.get("PW_COUNTER_FLAG").toString().equals("0")) {
							counter_pw_flag = CommonModels.checkStringObject(jObject.get("PW_COUNTER_FLAG").toString());
						}
						condition=" and a.case_status=6 and e.gp_id='"+userId+"' ";
						
						if(counter_pw_flag.equals("PR")) {
							// pwr_uploaded='No' and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' ) and ecd.case_status='6'
							condition+=" and pwr_uploaded='No' and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' )";
						}
						if(counter_pw_flag.equals("COUNTER")) {
							//pwr_uploaded='Yes' and counter_filed='No' and coalesce(counter_approved_gp,'F')='F' and ecd.case_status='6'
							condition+=" and pwr_uploaded='Yes' and counter_filed='No' and coalesce(counter_approved_gp,'F')='F'";
						}
					}
					
					sql = "select a.*, b.orderpaths , od.pwr_uploaded, od.counter_filed, od.pwr_approved_gp, coalesce(od.counter_approved_gp,'-') as counter_approved_gp "
							+ " ,case when pwr_uploaded='Yes' then 'Parawise Remarks Uploaded' else 'Parawise Remarks not Submitted' end as casestatus1,"
							+ " case when pwr_approved_gp='Yes' then 'Parawise Remarks Approved by GP' else 'Parawise Remarks Not Approved by GP' end as casestatus2,"
							+ " case when counter_filed='Yes' then 'Counter Filed' else 'Counter Not Filed' end as casestatus3,"
							+ " case when counter_approved_gp='T' then 'Counter Approved by GP' else 'Counter Not Approved by GP' end as casestatus4 "
							+ " " //sql = "select a.*, prayer from ecourts_case_data a left join nic_prayer_data np on (a.cino=np.cino) where a.cino='" + cIno + "'";
							+ " ,coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, prayer, ra.address "
							+ " from ecourts_case_data a "
							+ " left join nic_prayer_data np on (a.cino=np.cino) "
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
							+ "left join" + " ("
							+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
							+ " from "
							+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1"
							+ " union"
							+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
							+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
							+ " on (a.cino=b.cino) "
							+ " "
							+ " left join ecourts_olcms_case_details od on (a.cino=od.cino)"
							// + " left join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) "
							
							+ " where assigned=true "+condition
							+ " and coalesce(a.ecourts_case_status,'')!='Closed' "
							+ " order by a.cino";
					
					System.out.println("CASES SQL:" + sql);
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
						
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
					    	cases.put("DATE_NEXT_LIST", entry.get("date_of_filing"));
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
					    	
					    	JSONArray counterApprovedList = new JSONArray();

							if (entry.get("counter_approved_gp") != null && !entry.get("counter_approved_gp").equals("-"))
							{
								JSONObject counterData = new JSONObject();
								counterData.put("CASE_STATUS_1", entry.get("casestatus1"));
								counterData.put("CASE_STATUS_2", entry.get("casestatus2"));
								counterData.put("CASE_STATUS_3", entry.get("casestatus3"));
								counterData.put("CASE_STATUS_4", entry.get("casestatus4"));
								counterApprovedList.put(counterData);
								
							}
							cases.put("COUNTER_APPROVED_GP", counterApprovedList);
							
							
							if (entry.get("counter_approved_gp") != null && !entry.get("counter_approved_gp").equals("T"))
							{
								cases.put("ENABLE_UPDATE_STATUS_BUTTON", true);
								
							}
							else {
								cases.put("ENABLE_UPDATE_STATUS_BUTTON", false);
							}
					    	
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
				} else if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CINO is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", condition="",roleId="", distId="", deptCode="", userId="";
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					String cIno = jObject.get("CINO").toString();
					
					con = DatabasePlugin.connect();
					
					JSONArray buttonsList = new JSONArray();
					JSONObject jsonObject = new JSONObject();
					JSONObject casesData = new JSONObject();
					
					sql = "select a.*, prayer from ecourts_case_data a left join nic_prayer_data np on (a.cino=np.cino) where a.cino='" + cIno + "'";
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					if (data != null && !data.isEmpty() && data.size() > 0) {
						
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
					
					sql=" select case_status,ecourts_case_status from ecourts_case_data where cino='" + cIno + "' ";
					List<Map<String, Object>> data_status = DatabasePlugin.executeQuery(sql, con);
					
					
					String caseStatus=(String)data_status.get(0).get("ecourts_case_status").toString();
					
					if ((caseStatus.equals(null) || !caseStatus.equals("Private"))) {

						sql = "SELECT cino, case when length(petition_document) > 0 then petition_document else null end as petition_document, "
								+ " case when length(counter_filed_document) > 0 then counter_filed_document else null end as counter_filed_document,"
								+ " case when length(judgement_order) > 0 then judgement_order else null end as judgement_order,"
								+ " case when length(action_taken_order) > 0 then action_taken_order else null end as action_taken_order,"
								+ " last_updated_by, last_updated_on, counter_filed, remarks, ecourts_case_status, corresponding_gp, "
								+ " pwr_uploaded, to_char(pwr_submitted_date,'mm/dd/yyyy') as pwr_submitted_date, to_char(pwr_received_date,'mm/dd/yyyy') as pwr_received_date, "
								+ " pwr_approved_gp, to_char(pwr_gp_approved_date,'mm/dd/yyyy') as pwr_gp_approved_date, appeal_filed, "
								+ " appeal_filed_copy, to_char(appeal_filed_date,'mm/dd/yyyy') as appeal_filed_date, pwr_uploaded_copy "
								+ " FROM apolcms.ecourts_olcms_case_details where cino='" + cIno + "'";

						data = DatabasePlugin.executeQuery(sql, con);

						JSONArray jsonArray = new JSONArray();

						if (data != null && !data.isEmpty() && data.size() > 0) {
							for (Map<String, Object> entry : data) {

								JSONObject obj = new JSONObject();
								obj.put("PETITION_DOC_PATH",CommonModels.checkStringObject(entry.get("petition_document")) != ""
												? "https://apolcms.ap.gov.in/" + entry.get("petition_document")
												: "");
								obj.put("CASE_STATUS", entry.get("ecourts_case_status"));
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
								obj.put("PWR_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("DATE_OF_PWR_SUBMISSION", entry.get("pwr_submitted_date"));
								obj.put("PWR_UPLOADED_COPY", CommonModels.checkStringObject(entry.get("pwr_uploaded_copy")) != ""
												? "https://apolcms.ap.gov.in/" + entry.get("pwr_uploaded_copy")
												: "");
								obj.put("PWR_APPROVED_BY_GP", entry.get("pwr_approved_gp"));
								obj.put("PWR_GP_APPROVED_DATE", entry.get("pwr_gp_approved_date"));
								obj.put("PWR_RECEIVED_DATE", entry.get("pwr_received_date"));
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
	
	
	
	//This method updates the case details and uploads the petition document, action taken order, judgement order, appealFiled in the server.
	
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Path("/updateCaseDetails")
	public Response updateCaseDetails(@FormDataParam("petitionDocument") InputStream petitionDoc,
			@FormDataParam("petitionDocument") FormDataBodyPart petitionDocBody, @FormDataParam("actionTakenOrder") InputStream actionTakenOrderDoc,
			@FormDataParam("actionTakenOrder") FormDataBodyPart actionTakenOrderDocBody, @FormDataParam("judgementOrder") InputStream judgementOrderDoc,
			@FormDataParam("judgementOrder") FormDataBodyPart judgementOrderDocBody, @FormDataParam("appealFiledDocument") InputStream appealFiledDoc,
			@FormDataParam("appealFiledDocument") FormDataBodyPart appealFiledDocBody, @FormDataParam("cino") String cino,
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
			@FormDataParam("pwrGPApprovedDate") String pwrGPApprovedDate) throws Exception {
		
		Connection con = null;
		String jsonStr = "";
		String sql = "";

		try {
			
			if (cino == null || cino.equals("")) {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- cino is missing in the request.\" }}";
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
				String petitionFileUploadPath="", actionFileUploadPath="", judgementFileUploadPath="", appealFileUploadPath="";
				
				// This section uploads the petition document in the server
				
				
				if(petitionDoc!=null  && !petitionDoc.equals("") && petitionDocBody != null && !petitionDocBody.getFormDataContentDisposition().getFileName().equals("")) {
					
					
					newFileName="petition_"+CommonModels.randomTransactionNo()+"."+petitionDocBody.getMediaType().getSubtype();
					
					String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/petitions/"+newFileName;
					
					petitionFileUploadPath = "uploads/petitions/"+newFileName;
					
					writeToFile(petitionDoc, uploadedFileLocation);
					
					
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks, uploaded_doc_path ) "
							+ "values ('" + cino + "','Uploaded Petition','"+userId+"', '"+remarks+"', '"+petitionFileUploadPath+"')";
					DatabasePlugin.executeUpdate(sql, con);
					
					sqlCondition2 = ", petition_document='"+petitionFileUploadPath+"'";
				}
				
				
				
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
						
						// sql="insert into ecourts_olcms_case_details_log select * from ecourts_olcms_case_details where cino='"+cIno+"'";
						
						sql="insert into ecourts_olcms_case_details_log (cino,petition_document, counter_filed_document,   judgement_order,action_taken_order,last_updated_by,last_updated_on,  counter_filed,remarks,ecourts_case_status,corresponding_gp,pwr_uploaded,pwr_submitted_date,pwr_received_date,pwr_approved_gp,pwr_gp_approved_date,appeal_filed,appeal_filed_copy,appeal_filed_date,pwr_uploaded_copy,counter_approved_gp,action_to_perfom,counter_approved_date,counter_approved_by,respondent_slno,cordered_impl_date,dismissed_copy,final_order_status,no_district_updated) "
								+ "select cino,petition_document, counter_filed_document,   judgement_order,action_taken_order,last_updated_by,last_updated_on,  counter_filed,remarks,ecourts_case_status,corresponding_gp,pwr_uploaded,pwr_submitted_date,pwr_received_date,pwr_approved_gp,pwr_gp_approved_date,appeal_filed,appeal_filed_copy,appeal_filed_date,pwr_uploaded_copy,counter_approved_gp,action_to_perfom,counter_approved_date,counter_approved_by,respondent_slno,cordered_impl_date,dismissed_copy,final_order_status,no_district_updated  from ecourts_olcms_case_details where cino='"+cino+"'";
						
						DatabasePlugin.executeUpdate(sql, con);
						
						sql = "update ecourts_olcms_case_details set ecourts_case_status='"
								+ ecourtsCaseStatus + "', appeal_filed='"
								+ appealFiledFlag + "',appeal_filed_date=to_date('"
								+ CommonModels.checkStringObject(appealFiledDate) + "','mm/dd/yyyy'), remarks='"
								+ remarks + "', last_updated_by='" + userId
								+ "', last_updated_on=now(), action_to_perfom='"+actionToPerform
								+"' " + sqlCondition2 + " where cino='" + cino + "'";
					}
					else {
						
						sql = "insert into ecourts_olcms_case_details (cino, ecourts_case_status, petition_document, appeal_filed, appeal_filed_copy, judgement_order, action_taken_order"
								+ ", last_updated_by, last_updated_on, remarks, appeal_filed_date, action_to_perfom) "
								+ " values ('" + cino + "', '" 
								+ ecourtsCaseStatus + "', '"
								+ petitionFileUploadPath + "','" 
								+ appealFiledFlag + "', '"
								+ appealFileUploadPath + "', '" 
								+ judgementFileUploadPath + "', '" 
								+ actionFileUploadPath + "', '" 
								+ userId + "', now(),'" 
								+ remarks + "',to_date('"
								+ CommonModels.checkStringObject(appealFiledDate)+"','mm/dd/yyyy'),'"+actionToPerform+"')";
					}
					
					
					
					int a = DatabasePlugin.executeUpdate(sql, con);
					
					sql="update ecourts_case_data set ecourts_case_status='"+ecourtsCaseStatus+"',section_officer_updated='T' where cino='"+cino+"'";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks ) "
							+ "values ('" + cino + "','"+actionPerformed+"','"+userId+"', '"+remarks+"')";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					if (a == 3) {
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
						
						sql="insert into ecourts_olcms_case_details_log (cino,petition_document, counter_filed_document,   judgement_order,action_taken_order,last_updated_by,last_updated_on,  counter_filed,remarks,ecourts_case_status,corresponding_gp,pwr_uploaded,pwr_submitted_date,pwr_received_date,pwr_approved_gp,pwr_gp_approved_date,appeal_filed,appeal_filed_copy,appeal_filed_date,pwr_uploaded_copy,counter_approved_gp,action_to_perfom,counter_approved_date,counter_approved_by,respondent_slno,cordered_impl_date,dismissed_copy,final_order_status,no_district_updated) "
								+ "select cino,petition_document, counter_filed_document, judgement_order,action_taken_order,last_updated_by,last_updated_on,  counter_filed,remarks,ecourts_case_status,corresponding_gp,pwr_uploaded,pwr_submitted_date,pwr_received_date,pwr_approved_gp,pwr_gp_approved_date,appeal_filed,appeal_filed_copy,appeal_filed_date,pwr_uploaded_copy,counter_approved_gp,action_to_perfom,counter_approved_date,counter_approved_by,respondent_slno,cordered_impl_date,dismissed_copy,final_order_status,no_district_updated  from ecourts_olcms_case_details where cino='"+cino+"'";
						
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
								+ "','mm/dd/yyyy'), action_to_perfom='"+actionToPerform  + "' where cino='" + cino + "'";
						
					}
					else {
						
						sql = "insert into ecourts_olcms_case_details (cino, ecourts_case_status, petition_document, counter_filed_document, last_updated_by, last_updated_on, "
								+ "counter_filed, remarks,  corresponding_gp, pwr_uploaded, pwr_submitted_date, pwr_received_date, pwr_approved_gp, "
								+ "pwr_gp_approved_date, pwr_uploaded_copy, action_to_perfom) "
								+ " values ('" + cino + "', '" + ecourtsCaseStatus + "', '"
								+ petitionFileUploadPath + "','" + counterFileUploadPath + "','" + userId + "', now(),'"
								+ counterFiledFlag+ "', '" + remarks + "', '"
								+ "" + "', '"
								+ paraWiseRemarksFlag + "'," 
								+ " to_date('" + CommonModels.checkStringObject(pwrSubmittedDate) + "','mm/dd/yyyy'), " 
								+ " to_date('" + CommonModels.checkStringObject(pwrReceivedDate) + "','mm/dd/yyyy'), '"
								+ pwrApprovedGP + "'," 
								+ " to_date('" + CommonModels.checkStringObject(pwrGPApprovedDate) + "','mm/dd/yyyy'), '" 
								+ pwrFileUploadPath + "','"+actionToPerform+"')";
					
					}
					System.out.println("SQL:"+sql);
					
					int a = DatabasePlugin.executeUpdate(sql, con);
					
					if(roleId!=null && (roleId.equals("4") || roleId.equals("5") || roleId.equals("10"))) {//MLO / NO / Dist-NO
						sql="update ecourts_case_data set ecourts_case_status='"+ecourtsCaseStatus+"', mlo_no_updated='T' where cino='"+cino+"'";
						a += DatabasePlugin.executeUpdate(sql, con);
					}
					else {
						sql="update ecourts_case_data set ecourts_case_status='"+ecourtsCaseStatus+"', section_officer_updated='T' where cino='"+cino+"'";
						a += DatabasePlugin.executeUpdate(sql, con);
					}
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , remarks ) "
							+ "values ('" + cino + "','"+actionPerformed+"','"+userId+"', '"+remarks+"')";
					a += DatabasePlugin.executeUpdate(sql, con);
					
					if (a == 3) {
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
				}  else if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CINO is missing in the request.\" }}";
				} else if (!jObject.has("GP_CODE") || jObject.get("GP_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- GP_CODE is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", condition="",roleId="", distId="", deptCode="", userId="",cino="";
					String newStatus = "";
					int a=0;
					userId = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					cino = jObject.get("CINO").toString();
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
						sql="update ecourts_case_data set case_status="+newStatus+", assigned_to='"+fwdOfficer+"' where cino='"+cino+"'  and mlo_no_updated='T' and case_status='1'";//and section_officer_updated='T' 
						
					}
					else if(roleId!=null && roleId.equals("9")) {//FROM HOD TO GP
						newStatus = "6";						
						sql="update ecourts_case_data set case_status="+newStatus+", assigned_to='"+fwdOfficer+"' where cino='"+cino+"' and mlo_no_updated='T' and case_status='3'"; //and section_officer_updated='T' 
						
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
	@Path("/showDeptSelection")
	public static Response showDeptSelection(String incomingData) throws Exception {

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
				} else if (!jObject.has("SELECTED_OFFICER_TYPE") || jObject.get("SELECTED_OFFICER_TYPE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_OFFICER_TYPE is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",typeCode="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					typeCode = jObject.get("SELECTED_OFFICER_TYPE").toString();
					con = DatabasePlugin.connect();
					
					
						if (deptCode != null && deptCode != "" && deptCode != "0") {

							if (typeCode.equals("S-HOD") || typeCode.equals("SD-SO")) {

								if (deptCode.substring(3, 5).equals("01")) {
									sql = "select dept_code,dept_code||'-'||upper(description) as desc from dept_new where (dept_code='"
											+ deptCode + "' or reporting_dept_code='" + deptCode
											+ "') and display=true order by dept_code";
								} else {
									sql = "select dept_code,dept_code||'-'||upper(description) as desc from dept_new where reporting_dept_code in (select reporting_dept_code from dept_new where dept_code='"
											+ deptCode + "') and display=true order by dept_code";
								}
								
							} else if (typeCode.equals("D-HOD") || typeCode.equals("OD-SO")) {
								
								sql = "select dept_code,dept_code||'-'||upper(description) as desc from dept_new where (dept_code!='"
										+ deptCode + "' and reporting_dept_code!='" + deptCode
										+ "') and display=true order by dept_code";
							} else if (typeCode.equals("DC-SO")) {
								
								sql = "select dept_code,dept_code||'-'||upper(description) as desc from dept_new where deptcode!='01' and display=true order by dept_code";
							} else {
								
								sql = "select dept_code,dept_code||'-'||upper(description) as desc from dept_new where display=true order by dept_code";
							}
							System.out.println(deptCode + ":getDeptList :sql" + sql);
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray deptList = new JSONArray();
							JSONObject casesData = new JSONObject();

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("DEPT_ID", entry.get("dept_code").toString());								
									cases.put("DEPT_DESC", entry.get("desc").toString());
									
									deptList.put(cases);
								}
							} 
							casesData.put("DEPT_LIST", deptList);
							String finalString = casesData.toString();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							
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
	@Path("/showSectionList")
	public static Response showSectionList(String incomingData) throws Exception {

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
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				} 
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",typeCode="",selectedDeptCode="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					con = DatabasePlugin.connect();
					String tableName = "nic_data";
					
					
						if (deptCode != null && deptCode != "" && deptCode != "0") {

							tableName = getTableName(distId, con);

							if (deptCode != null && deptCode != "")
								sql = "select trim(employee_identity)as identity from " + tableName
										+ " where substr(trim(global_org_name),1,5)='"+ selectedDeptCode +"' and trim(employee_identity)!='NULL' "
										+ " and trim(email) not in (select userid from user_roles where role_id in (4,5)) group by trim(employee_identity) order by 1";
							
							System.out.println(selectedDeptCode + ":getEmpDeptSectionsList sql:" + sql);
							
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray deptList = new JSONArray();
							JSONObject casesData = new JSONObject();

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("IDENTITY", entry.get("identity").toString());	
									
									deptList.put(cases);
								}
							} 
							casesData.put("SECTION_LIST", deptList);
							String finalString = casesData.toString();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							
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
	@Path("/showPostsList")
	public static Response showPostsList(String incomingData) throws Exception {

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
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_SEC_CODE") || jObject.get("SELECTED_SEC_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_SEC_CODE is missing in the request.\" }}";
				}
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",typeCode="",selectedDeptCode="",selectedSecCode="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					selectedSecCode = jObject.get("SELECTED_SEC_CODE").toString();
					
					con = DatabasePlugin.connect();
					String tableName = "nic_data";
					
					
							tableName = getTableName(distId, con);

							if (selectedDeptCode != null && selectedDeptCode != "")
								sql = "select trim(post_name_en) as post_name from " + tableName
								+ " where substr(trim(global_org_name),1,5)='"+selectedDeptCode+"' and trim(employee_identity)!='NULL' and trim(employee_identity)=trim('"+selectedSecCode+"') "
								+ "  and trim(email) not in (select userid from user_roles where role_id in (4,5)) group by post_name_en";
					
							System.out.println(deptCode + ":showPostsList sql:" + sql);
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray deptList = new JSONArray();
							JSONObject casesData = new JSONObject();

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("POST", entry.get("post_name").toString());	
									
									deptList.put(cases);
								}
							} 
							casesData.put("POSTS_LIST", deptList);
							String finalString = casesData.toString();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
					
						
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
	@Path("/showEmployeeList")
	public static Response showEmployeeList(String incomingData) throws Exception {

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
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_SEC_CODE") || jObject.get("SELECTED_SEC_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_SEC_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_POST_CODE") || jObject.get("SELECTED_POST_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_POST_CODE is missing in the request.\" }}";
				}
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",typeCode="",selectedDeptCode="",selectedSecCode="",selectedPostCode="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					selectedSecCode = jObject.get("SELECTED_SEC_CODE").toString();
					selectedPostCode = jObject.get("SELECTED_POST_CODE").toString();
					
					con = DatabasePlugin.connect();
					String tableName = "nic_data";
					
					
							tableName = getTableName(distId, con);

							if (selectedDeptCode != null && selectedDeptCode != "")
								sql = "select distinct trim(employee_id) as empid, trim(fullname_en)||' - '||trim(designation_name_en)||' - '||trim(org_unit_name_en) as empname from "
										+ tableName
										+ " where employee_identity='"+selectedSecCode+"' and post_name_en='"+selectedPostCode+"' and substr(trim(global_org_name),1,5)='"+selectedDeptCode+"' "
										+ " and trim(email) not in (select userid from user_roles where role_id in (4,5))  ";
								
							
							System.out.println(deptCode + ":showEmployeeList sql:" + sql);
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray deptList = new JSONArray();
							JSONObject casesData = new JSONObject();

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("EMP_ID", entry.get("empid").toString());	
									cases.put("EMP_NAME", entry.get("empname").toString());	
									
									deptList.put(cases);
								}
							} 
							casesData.put("EMP_NAMES_LIST", deptList);
							String finalString = casesData.toString();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
					
						
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
	
	
	public static String getTableName(String distId, Connection con) {
		String tableName = "nic_data";
		if(distId!=null && !distId.equals("") && Integer.parseInt(distId) > 0)
			tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst where district_id="+distId, con);
			// tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst_new where district_id="+distId, con);
		System.out.println("dist::Id"+distId+"-tableName::"+tableName);
		return tableName;
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/assignToDeptHOD")
	public static Response assignToDeptHOD(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";
		int a = 0;


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
				} else if (!jObject.has("SELECTED_CASE_IDS") || jObject.get("SELECTED_CASE_IDS").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_CASE_IDS is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				}
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",assignedToDept="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					assignedToDept = jObject.get("SELECTED_DEPT_CODE").toString();
					con = DatabasePlugin.connect();
					con.setAutoCommit(false);
					String[] ids_split=null;
					
					JSONArray arrObj = jObject.getJSONArray("SELECTED_CASE_IDS");
					for (int i = 0; i < arrObj.length(); i++) {

						// get field value from JSON Array
						System.out.println(arrObj.get(i));

						String ids = (String) arrObj.get(i);
						ids_split = ids.split("@");
						System.out.println("ids--" + ids_split[0]);
						System.out.println("ids--" + ids_split[1]);

						System.out.println("newCaseId::::::" + ids_split[0]);

						InetAddress localhost = InetAddress.getLocalHost();
						System.out.println("System IP Address : " + (localhost.getHostAddress()).trim());

						sql = "insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, assigned_to , remarks ) "
								+ "values ('" + ids_split[0] + "','CASE ASSSIGNED','" + userid + "', '"
								+ localhost.getHostAddress().trim() + "', '" + assignedToDept + "', null)";

						a += DatabasePlugin.executeUpdate(sql, con);
						System.out.println(a + ":ACTIVITIES SQL:" + sql);

						sql = " INSERT INTO apolcms.ecourts_gpo_ack_depts_log(ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id) "
								+ " SELECT ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id "
								+ "    FROM apolcms.ecourts_gpo_ack_depts  where ack_no in ('" + ids_split[0]
								+ "') and respondent_slno='" + ids_split[1] + "' ";
						System.out.println("INSERT SQL:" + sql);
						a += DatabasePlugin.executeUpdate(sql, con);

						String newStatusCode = "4";
						if (assignedToDept.contains("01")) {
							newStatusCode = "2";
						} else {
							newStatusCode = "4";
						}

						sql = "update ecourts_gpo_ack_depts set  dept_code='" + assignedToDept + "',case_status="
								+ newStatusCode + " where ack_no in ('" + ids_split[0] + "') and respondent_slno='" + ids_split[1] + "'  ";
						System.out.println("UPDATE SQL:" + sql);
						a += DatabasePlugin.executeUpdate(sql, con);

					} 
					
					con.commit();
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case/Cases successfully moved to selected Department / HOD.\" }}";								

				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}";
			}
		} catch (Exception e) {
			con.rollback();
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error in assigning Case to Department/HOD. Kindly try again.\" }}";
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
	@Path("/assignToSection")
	public static Response assignToSection(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";
		int a = 0;
		PreparedStatement ps = null;


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
				} else if (!jObject.has("SELECTED_CASE_IDS") || jObject.get("SELECTED_CASE_IDS").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_CASE_IDS is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_EMP_ID") || jObject.get("SELECTED_EMP_ID").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_EMP_ID is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_POST_CODE") || jObject.get("SELECTED_POST_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_POST_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_SEC_CODE") || jObject.get("SELECTED_SEC_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_SEC_CODE is missing in the request.\" }}";
				} else if(!jObject.has("SELECTED_OFFICER_TYPE") || jObject.get("SELECTED_OFFICER_TYPE").toString().trim().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_OFFICER_TYPE is missing in the request.\" }}";
				}
				else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",selectedDeptCode="",selectedEmpId="",selectedPostCode="",selectedSecCode="",selectedDistCode="0",selectedOfficerType="",remarks="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					selectedEmpId = jObject.get("SELECTED_EMP_ID").toString();
					selectedPostCode = jObject.get("SELECTED_POST_CODE").toString();
					selectedSecCode = jObject.get("SELECTED_SEC_CODE").toString();
					selectedOfficerType = jObject.get("SELECTED_OFFICER_TYPE").toString();
					con = DatabasePlugin.connect();
					con.setAutoCommit(false);
					String[] ids_split=null;
					
					InetAddress localhost = InetAddress.getLocalHost();
					String ipAddress = (localhost.getHostAddress()).trim();
					
					System.out.println("System IP Address : " + (localhost.getHostAddress()).trim());
					
					if(jObject.has("SELECTED_DIST_CODE") && !jObject.get("SELECTED_DIST_CODE").toString().trim().equals(""))
						selectedDistCode = jObject.get("SELECTED_DIST_CODE").toString();
					
					
					
					if(jObject.has("REMARKS") && !jObject.get("REMARKS").toString().trim().equals("")) {
						remarks = jObject.get("REMARKS").toString();
					}
					
					String tableName = getTableName(selectedDistCode, con);
					
					String emailId = DatabasePlugin.getStringfromQuery("select distinct trim(email) from " + tableName
							+ " where substring(global_org_name,1,5)='" + selectedDeptCode
							+ "' and trim(employee_identity)='" + selectedSecCode
							+ "' and trim(post_name_en)='" + selectedPostCode + "' and trim(employee_id)='"
							+ selectedEmpId + "' and email is not null ", con);
				 
					System.out.println("EMAIL ID:"+emailId);
					
					JSONArray arrObj = jObject.getJSONArray("SELECTED_CASE_IDS");
					
					for (int i = 0; i < arrObj.length(); i++) {
						// get field value from JSON Array
						System.out.println(arrObj.get(i));

						String ids = (String) arrObj.get(i);
						ids_split = ids.split("@");
						System.out.println("ids--" + ids_split[0]);
						System.out.println("ids--" + ids_split[1]);

						System.out.println("newCaseId::::::" + ids_split[0]);

						

						if (ids_split[0] != null && !ids_split[0].equals("")) {
							sql = "insert into ecourts_ack_assignment_dtls (ackno, dept_code, emp_section, emp_post, emp_id, inserted_time, inserted_ip, inserted_by, emp_user_id) values (?, ?, ?, ?, ?, now(), ?, ?, ?)";
						
							System.out.println("INSERT SQL:"+sql);
							ps = con.prepareStatement(sql);
							int var = 0;

							ps.setString(++var, ids_split[0]);
							ps.setString(++var, (String) selectedDeptCode);
							ps.setString(++var, (String) selectedSecCode);
							ps.setString(++var, (String) selectedPostCode);
							ps.setString(++var, (String) selectedEmpId);
							ps.setString(++var, ipAddress);
							ps.setString(++var, userid);
							ps.setString(++var, emailId);

							a = ps.executeUpdate();

							String newStatusCode = "0", activityDesc = "";
							
							if (CommonModels.checkIntObject(selectedDistCode) > 0) { // Dist. - Section Officer
								newStatusCode = "10";
								activityDesc = "CASE ASSSIGNED TO Section Officer (District)";
							} else if (selectedDeptCode.contains("01")) { // Sect. Dept. - Section Officer
								newStatusCode = "5";
								activityDesc = "CASE ASSSIGNED TO Section Officer (Sect. Dept.)";
							} else { // HOD - Section Officer.
								newStatusCode = "9";
								activityDesc = "CASE ASSSIGNED TO Section Officer (HOD)";
							}
							
							System.out.println("newStatusCode--"+newStatusCode);

							sql = " INSERT INTO apolcms.ecourts_gpo_ack_depts_log(ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id) "
									+ " SELECT ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id "
									+ "    FROM apolcms.ecourts_gpo_ack_depts where ack_no='" + ids_split[0] + "'  and respondent_slno='"+ids_split[1]+"'";

							System.out.println("INSERT SQL:" + sql);
							a += DatabasePlugin.executeUpdate(sql, con);

							
							if (selectedOfficerType.equals("DC") || selectedOfficerType.equals("DC-NO") || selectedOfficerType.equals("DC-SO")) {
							sql = "update ecourts_gpo_ack_depts set dept_code='" + selectedDeptCode
									+ "', assigned=true, assigned_to='" + emailId + "',case_status=" + 
									newStatusCode + ", dist_id=" + selectedDistCode + " where ack_no='" + ids_split[0] + "'  and respondent_slno='"+ids_split[1]+"' "; //and dept_code='"+ login_deptId + "'   and dist_id=" + user_dist + "
							}else {
							
							sql = "update ecourts_gpo_ack_depts set dept_code='" + selectedDeptCode
									+ "', assigned=true, assigned_to='" + emailId + "',case_status=" + 
									newStatusCode + ", dist_id=" + selectedDistCode + " where ack_no='" + ids_split[0] + "'  and respondent_slno='"+ids_split[1]+"' ";   //and respondent_slno='1'  and dept_code='"+ login_deptId +"'
							}
							
							System.out.println("UPDATE SQL:" + sql);
							a += DatabasePlugin.executeUpdate(sql, con);

							sql = "insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, assigned_to , remarks ,dist_id) "
									+ "values ('" + ids_split[0] + "','" + activityDesc + "','" + userid + "', '"
									+ ipAddress + "', '" + (String) selectedEmpId + "', '"
									+ (String) remarks + "','" + selectedDistCode + "')";

							a += DatabasePlugin.executeUpdate(sql, con);
							System.out.println("a:----" + a);

							if (a > 0) {
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case successfully Assigned to Selected Employee.\" }}";
							} else {
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Error in assigning Cases. Kindly try again--.\" }}";
							}
						}
						
					} 
					
					int b = 0;
					if (Integer.parseInt(DatabasePlugin.getStringfromQuery(
							"select count(*) from users where trim(userid)='" + emailId.trim() + "'", con)) > 0) {
						con.commit();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Case successfully Assigned to Selected Employee.\" }}";
					} else {

						String newRoleId = "8";
						if (Integer.parseInt(selectedDistCode) > 0) { // Dist. - Section Officer
							newRoleId = "12";
						} else if (CommonModels.checkStringObject(selectedDeptCode).contains("01")) { // Sect. Dept.- Section Officer
							newRoleId = "8";
						} else { // HOD - Section Officer.
							newRoleId = "11";
						}

						// NEW SECTION OFFICER CREATION
						sql = "insert into section_officer_details (emailid, dept_id,designation,employeeid,mobileno,aadharno,inserted_by,inserted_ip, dist_id) "
								+ "select distinct b.email,d.sdeptcode||d.deptcode,b.designation_id,b.employee_id,b.mobile1,uid, '"
								+ (String) userid + "', '" + ipAddress + "'::inet,"
								+ selectedDistCode + " from " + tableName + " b inner join dept_new d on (d.dept_code='"
								+ selectedDeptCode + "')"
								+ " where b.employee_id='"+ selectedEmpId + "' and trim(b.employee_identity)='"+selectedSecCode+"' and trim(b.post_name_en)='"+selectedPostCode+"'";
						//+ " where b.employee_id='" + cform.getDynaForm("employeeId") + "'";
						System.out.println("NEW SECTION OFFICER CREATION SQL:" + sql);
						b += DatabasePlugin.executeUpdate(sql, con);

						sql = "insert into users (userid, password, user_description, created_by, created_on, created_ip, dept_id, dept_code, user_type, dist_id) "
								+ "select distinct b.email, md5('olcms@2021'), b.fullname_en, '"
								+ (String) userid + "', now(),'" + ipAddress
								+ "'::inet, d.dept_id,d.dept_code," + newRoleId + "," + selectedDistCode + " from " + tableName
								+ " b inner join dept_new d on (d.dept_code='" + selectedDeptCode
								+ "')"
								+" where b.employee_id='"+ selectedEmpId + "' and trim(b.employee_identity)='"+selectedSecCode+"' and trim(b.post_name_en)='"+selectedPostCode+"'";
								//+ " where b.employee_id='" + cform.getDynaForm("employeeId") + "' ";

						System.out.println("USER CREATION SQL:" + sql);

						b += DatabasePlugin.executeUpdate(sql, con);

						// sql = "select distinct mobile1 from " + tableName + " where employee_id='"+ cform.getDynaForm("employeeId") + "' and mobile1 is not null";
						sql="select distinct mobile1 from "+tableName+" b "
								+" where b.employee_id='"+ selectedEmpId + "' and trim(b.employee_identity)='"+selectedSecCode+"' and trim(b.post_name_en)='"+selectedPostCode+"'"
								+ " and mobile1 is not null";
						
						System.out.println("MOBILE SQL:" + sql);
						String mobileNo = DatabasePlugin.getStringfromQuery(sql, con);

						sql = "insert into user_roles (userid, role_id) values ('" + emailId + "','" + newRoleId + "')";
						System.out.println("INSERT ROLE SQL:" + sql);
						b += DatabasePlugin.executeUpdate(sql, con);

						if (b == 3) {
							String smsText = "Your User Id is " + emailId
									+ " and Password is olcms@2021 to Login to https://apolcms.ap.gov.in/ Portal. Please do not share with anyone. \r\n-APOLCMS";
							String templateId = "1007784197678878760";
							 //mobileNo = "8500909816";
							System.out.println(mobileNo + "" + smsText + "" + templateId);
							if (mobileNo != null && !mobileNo.equals("")) {
							// mobileNo = "8500909816";
								System.out.println("mobileNo::" + mobileNo);
								//SendSMSAction.sendSMS(mobileNo, smsText, templateId, con);
							}
							con.commit();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Cases successfully Assigned to Selected Employee & User Login created successfully. Login details sent to Registered Mobile No.\" }}";
							
						} else {
							con.rollback();
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Error in assigning Cases. Kindly try again.\" }}";
						}
					}
					
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}";
			}
		} catch (Exception e) {
			con.rollback();
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error in assigning Cases. Kindly try again.\" }}";
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
	@Path("/assignToDistCollector")
	public static Response assignToDistCollector(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";
		int a = 0;
		PreparedStatement ps = null;


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
				} else if (!jObject.has("SELECTED_CASE_IDS") || jObject.get("SELECTED_CASE_IDS").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_CASE_IDS is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_OFFICER_TYPE") || jObject.get("SELECTED_OFFICER_TYPE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_OFFICER_TYPE is missing in the request.\" }}";
				} else if (!jObject.has("SELECTED_DIST_CODE") || jObject.get("SELECTED_DIST_CODE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- SELECTED_DIST_CODE is missing in the request.\" }}";
				} else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",selectedDeptCode="",selectedEmpId="",selectedPostCode="",selectedSecCode="",selectedDistCode="0",selectedOfficerType="",remarks="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					con = DatabasePlugin.connect();
					con.setAutoCommit(false);
					String[] ids_split=null;
					
					InetAddress localhost = InetAddress.getLocalHost();
					String ipAddress = (localhost.getHostAddress()).trim();
					
					System.out.println("System IP Address : " + (localhost.getHostAddress()).trim());
					
					if(jObject.has("SELECTED_DIST_CODE") && !jObject.get("SELECTED_DIST_CODE").toString().trim().equals(""))
						selectedDistCode = jObject.get("SELECTED_DIST_CODE").toString();
					
					if(jObject.has("SELECTED_OFFICER_TYPE") && !jObject.get("SELECTED_OFFICER_TYPE").toString().trim().equals(""))
						selectedOfficerType = jObject.get("SELECTED_OFFICER_TYPE").toString();
					
					if(jObject.has("REMARKS") && !jObject.get("REMARKS").toString().trim().equals("")) {
						remarks = jObject.get("REMARKS").toString();
					}
					
					
					
					JSONArray arrObj = jObject.getJSONArray("SELECTED_CASE_IDS");
					
					for (int i = 0; i < arrObj.length(); i++) {
						// get field value from JSON Array
						System.out.println(arrObj.get(i));

						String ids = (String) arrObj.get(i);
						ids_split = ids.split("@");
						System.out.println("ids--" + ids_split[0]);
						System.out.println("ids--" + ids_split[1]);

						System.out.println("newCaseId::::::" + ids_split[0]);

						if (selectedOfficerType.equals("DC")) {

							sql = "insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, assigned_to , remarks ,dist_id) "
									+ "values ('" + ids_split[0] + "','CASE ASSSIGNED','" + userid + "', '"
									+ ipAddress + "', '"
									+ CommonModels.checkStringObject(selectedDistCode) + "', null,'"
									+ CommonModels.checkIntObject(selectedDistCode) + "')";
							DatabasePlugin.executeUpdate(sql, con);
						} else if (selectedOfficerType.equals("DC-NO")) {

							sql = "insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, assigned_to , remarks ,dist_id) "
									+ "values ('" + ids_split[0] + "','CASE ASSSIGNED','" + userid + "', '"
									+ ipAddress + "', '"
									+ CommonModels.checkStringObject(selectedDeptCode) + "', null, '"
									+ CommonModels.checkIntObject(selectedDistCode) + "')";
							DatabasePlugin.executeUpdate(sql, con);
						}
						
						String successMsg = "";
						if (selectedOfficerType.equals("DC")) {

							sql = " INSERT INTO apolcms.ecourts_gpo_ack_depts_log(ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id) "
									+ " SELECT ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id "
									+ "    FROM apolcms.ecourts_gpo_ack_depts  where ack_no in ('" + ids_split[0] + "') "
											+ " and dist_id='"+CommonModels.checkIntObject(selectedDistCode)+"' and respondent_slno='"+ids_split[1]+"' ";

							System.out.println("INSERT SQL:" + sql);
							DatabasePlugin.executeUpdate(sql, con);

							sql = "update ecourts_gpo_ack_depts set case_status=7, dist_id='"
									+ CommonModels.checkStringObject(selectedDistCode) + "',dept_code='"+selectedDeptCode+"' " // assigned=true,
									+ " where ack_no in ('" + ids_split[0] + "')   and respondent_slno='"+ids_split[1]+"'  ";   //and and respondent_slno=1 and dist_id='"+user_dist+"'
							System.out.println("UPDATE SQL:" + sql);
							DatabasePlugin.executeUpdate(sql, con);

							successMsg = "Case/Cases successfully moved to selected District Collector Login";

						} else if (selectedOfficerType.equals("DC-NO")) {

							sql = " INSERT INTO apolcms.ecourts_gpo_ack_depts_log(ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id) "
									+ " (SELECT ack_no, dept_code, respondent_slno, assigned, assigned_to, case_status, dist_id "
									+ "    FROM apolcms.ecourts_gpo_ack_depts where ack_no in ('" + ids_split[0] + "') and respondent_slno='"+ids_split[1]+"') ";
							System.out.println("INSERT SQL:" + sql);

							DatabasePlugin.executeUpdate(sql, con);

							sql = "update ecourts_gpo_ack_depts set dept_code='"
									+ CommonModels.checkStringObject(selectedDeptCode) + "',dist_id='"
									+ CommonModels.checkIntObject(selectedDistCode) + "',case_status=8    "
									+ " where ack_no in ('" + ids_split[0] + "')  and respondent_slno='"+ids_split[1]+"' ";  //and dept_code='" + login_deptId + "' and respondent_slno='1'    and dist_id='"+user_dist+"'
							DatabasePlugin.executeUpdate(sql, con);
							successMsg = "Case/Cases successfully moved to selected District Nodal Officer Login";
						}
						
						
						con.commit();
						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :"+successMsg+"}}";
						
						
				
					} 
						
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}";
			}
		} catch (Exception e) {
			con.rollback();
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error in assigning Cases to District Collector/ Nodal Officer. Kindly try again.\" }}";
			// conn.rollback();
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
	