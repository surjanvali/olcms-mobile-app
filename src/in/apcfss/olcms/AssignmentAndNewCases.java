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

@Path("/assignment")
public class AssignmentAndNewCases {
			
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/newCases")
	public static Response newCasesPendingForAssignment(String incomingData) throws Exception {

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
					
					
					
					if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals("")
							&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
						sqlCondition += " and a.distid='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
					}

					if (jObject.has("SELECTED_DEPT_ID") && !jObject.get("SELECTED_DEPT_ID").toString().equals("")
							&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
						sqlCondition += " and ad.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString().trim() + "' ";
					}

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and a.inserted_time::date >= to_date('" + jObject.get("FROM_DATE").toString()
								+ "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and a.inserted_time::date <= to_date('" + jObject.get("TO_DATE").toString()
								+ "','dd-mm-yyyy') ";
					}

					

					if (!(roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("3"))) {
						// sqlCondition += " and (dmt.dept_code='" + deptCode + "' or
						// dmt.reporting_dept_code='"+deptCode+"') ";
						sqlCondition += " and dmt.dept_code='" + deptCode + "' ";
					}

					if (roleId.equals("2")) {// District Collector
						sqlCondition += " and (case_status is null or case_status=7) and ad.distid='" + distId + "' ";
					}

					if (roleId.equals("3")) {// Secretariat Department
						sqlCondition += " and (dmt.dept_code='" + deptCode + "' or dmt.reporting_dept_code='" + deptCode + "') ";
					}

					if (jObject.has("SELECTED_CASE_TYPE") && !jObject.get("SELECTED_CASE_TYPE").toString().equals("")
							&& !jObject.get("SELECTED_CASE_TYPE").toString().equals("0")) {
						sqlCondition += " and a.casetype='" + jObject.get("SELECTED_CASE_TYPE").toString().trim() + "' ";
					}
					
					else if (roleId.equals("10")) { // District Nodal Officer
						sqlCondition += " and (case_status is null or case_status=8) and dist_id='" + distId + "'";
					} else if (roleId.equals("5") || roleId.equals("9")) { // NO & HOD
						sqlCondition += " and (case_status is null or case_status in (3,4))";
					} else if (roleId.equals("3") || roleId.equals("4")) {// MLO & Sect. Dept.
						sqlCondition += " and (case_status is null or case_status in (1, 2))";
					}

					if (jObject.has("ADVOCATE_NAME") && !jObject.get("ADVOCATE_NAME").toString().equals("")) {
						sqlCondition += " and replace(replace(advocatename,' ',''),'.','') ilike  '%"+jObject.get("ADVOCATE_NAME").toString()+"%'";
					}
					
					
					sql = "select slno , a.ack_no,respondent_slno , distid , advocatename ,advocateccno , casetype , maincaseno , remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
							+ " upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
							+ " to_char(inserted_time,'dd-mm-yyyy') as generated_date, getack_dept_desc(a.ack_no::text) as dept_descs, coalesce(a.hc_ack_no,'-') as hc_ack_no "
							+ " from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
							+ " left join district_mst dm on (ad.dist_id=dm.district_id) "
							+ " left join dept_new dmt on (ad.dept_code=dmt.dept_code)"
							+ " inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name) "
							+ " where a.delete_status is false and coalesce(assigned,'f')='f' and ack_type='NEW' " //and respondent_slno=1 
							+ sqlCondition + " order by inserted_time desc";
					
					System.out.println("CASES SQL:" + sql);
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
						
					if (data != null && !data.isEmpty() && data.size() > 0) {
						for (Map<String, Object> entry : data) {		
						    
					    	JSONObject cases = new JSONObject();
					    	cases.put("ACK_NO", entry.get("ack_no").toString());
					    	
					    	if(entry.get("hc_ack_no") !=null && !entry.get("hc_ack_no").toString().equals("-")) {
					    		cases.put("HC_ACK_NO", entry.get("hc_ack_no").toString());
					    		cases.put("SCANNED_AFFIDAVIT_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("hc_ack_no").toString()+"/"+entry.get("hc_ack_no").toString()+".pdf");
					    	} else {
					    		cases.put("HC_ACK_NO", "");
					    		cases.put("SCANNED_AFFIDAVIT_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("ack_no").toString()+"/"+entry.get("ack_no").toString()+".pdf");
					    	}					    	
					    							    	
					    	cases.put("DATE", entry.get("generated_date").toString());
					    	cases.put("DIST_NAME", entry.get("district_name").toString());
					    	cases.put("CASE_TYPE", entry.get("case_full_name").toString());
					    	cases.put("MAIN_CASE_NO", entry.get("maincaseno").toString());
					    	cases.put("DEPARTMENTS", entry.get("dept_descs").toString());
					    	cases.put("ADVOCATE_CC_NO", entry.get("advocateccno").toString());
					    	cases.put("ADVOCATE_NAME", entry.get("advocatename").toString());
					    	cases.put("ACK_FILE_PATH", entry.get("ack_file_path"));
					    	cases.put("BARCODE_FILE_PATH", entry.get("barcode_file_path"));
					    	cases.put("RESPONDENT_SLNO", entry.get("respondent_slno"));
					    	
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
	
	
}
	