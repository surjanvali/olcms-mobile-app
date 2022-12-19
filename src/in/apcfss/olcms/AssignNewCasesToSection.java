package in.apcfss.olcms;

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
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

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
					    	cases.put("ACK_NO", entry.get("ack_no").toString());
					    	if (entry.get("ack_no") != null)
							{
								if (entry.get("hc_ack_no")!=null && !entry.get("hc_ack_no").equals("-")) {
									cases.put("HC_ACK_NO", entry.get("hc_ack_no").toString());
								}
								else if (entry.get("hc_ack_no")!=null && entry.get("hc_ack_no").equals("-")) {
									cases.put("HC_ACK_NO", "");
								}
							}
							
							cases.put("DATE", entry.get("generated_date").toString());
							cases.put("DIST_NAME", entry.get("district_name").toString());
							cases.put("CASE_TYPE", entry.get("case_full_name").toString());
							cases.put("MAIN_CASE_NO", entry.get("maincaseno").toString());
							cases.put("DEPARTMENTS", entry.get("dept_descs").toString());
							cases.put("ADVOCATE_CCNO", entry.get("advocateccno").toString());
							cases.put("ADVOCATE_NAME", entry.get("advocatename").toString());
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
	
	
	
	
	
	
	
	
}
	