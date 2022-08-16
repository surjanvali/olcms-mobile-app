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

import in.apcfss.struts.commons.AjaxModels;
import in.apcfss.struts.commons.CommonModels;
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

@Path("/eOfficeEmployees")
public class EOfficeEmployeeReport {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/deptWiseReport")
	public static Response deptWiseReport(String incomingData) throws Exception {
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
					String dist_id = jObject.get("DIST_ID").toString();
					String user_id = jObject.get("USER_ID").toString();	
					String distId="";
					
					
					if (roleId.equals("2")) {
						distId = CommonModels.checkStringObject(dist_id);
					} else {
						if (roleId != null && (roleId.equals("1") || roleId.equals("7"))) {// 1. OLCMS - ADMIN
							if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals(""))
								distId = CommonModels.checkStringObject(jObject.get("SELECTED_DIST_ID"));
						}	
					}
					con = DatabasePlugin.connect();
					

					String dist = CommonModels.checkStringObject(distId);
					String tableName = "";

					tableName = AjaxModels.getTableName(CommonModels.checkStringObject(dist), con);

					sql = "select substr(global_org_name,1,5) as code,global_org_name,count(*) from " + tableName;

					if (roleId.equals("3") || roleId.equals("4")) { // 2. Sect Dept./ MLO Login
						sql += " where substr(trim(global_org_name),1,5) in (select dept_code from dept_new where reporting_dept_code='"
								+ dept_code + "' or dept_code='" + dept_code + "')";
					}
					if (roleId.equals("5") || roleId.equals("9")) { // 2. HOD / NO Login
						sql += " where substr(trim(global_org_name),1,5)='" + dept_code + "'";
					}
					
					sql += " group by global_org_name order by global_org_name";

					System.out.println("SQL:" + sql);

					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;
					
					if (data != null && !data.isEmpty() && data.size() > 0) {
						
						for (Map<String, Object> entry : data) {								   
							JSONObject cases = new JSONObject();
					    	cases.put("DEPT_CODE", entry.get("code").toString());						    	
					    	cases.put("DEPT_NAME", entry.get("global_org_name").toString());
					    	cases.put("EMP_COUNT", entry.get("count"));
					    	
					    	finalList.put(cases);
						}
						
						casesData.put("DEPT_WISE_LIST", finalList);
						isDataAvailable = true;						
													
						} else {
							
							casesData.put("DEPT_WISE_LIST", finalList);	
							
						}
					
						String finalString = casesData.toString();
						
						if (isDataAvailable)					    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Dept wise list retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/officerWiseReport")
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
					String distId = "";

						
					con = DatabasePlugin.connect();	
					
					if (roleId.equals("2")) {
						distId = CommonModels.checkStringObject(dist_id);
					} else {
						if (roleId != null && (roleId.equals("1") || roleId.equals("7"))) {// 1. OLCMS - ADMIN
							if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals(""))
								distId = CommonModels.checkStringObject(jObject.get("SELECTED_DIST_ID"));
						}	
					}
						
					String tableName = "";

					tableName = AjaxModels.getTableName(CommonModels.checkStringObject(distId), con);
					// String sql = "select * from " + tableName + " where global_org_name='" +
					// cform.getDynaForm("deptId") + "'";
					sql = "select * from " + tableName
							+ " where trim(regexp_replace(global_org_name, '\\W', '', 'g'))=trim(regexp_replace('"
							+ selectedDeptCode + "', '\\W', '', 'g')) order by designation_id";
					System.out.println("SQL::" + sql);
						
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("EMP_CODE", entry.get("employee_code").toString());						    	
							    	cases.put("EMP_ID", entry.get("employee_id").toString());
							    	cases.put("EMP_IDENTITY", entry.get("employee_identity"));
							    	cases.put("GLOBAL_ORG_ID", entry.get("global_org_id"));
							    	cases.put("GLOBAL_ORG_NAME", entry.get("global_org_name").toString());
							    	cases.put("FULLNAME", entry.get("fullname_en").toString());
							    	cases.put("DESIGNATION_ID", entry.get("designation_id").toString());
							    	cases.put("DESIGNATION_NAME", entry.get("designation_name_en").toString());
							    	cases.put("POST_NAME", entry.get("post_name_en").toString());
							    	cases.put("ORG_UNIT_NAME", entry.get("org_unit_name_en").toString());
							    	cases.put("MARKING", entry.get("marking_abbr").toString());
							    	cases.put("MOBILE", entry.get("mobile1").toString());
							    	cases.put("EMAIL", entry.get("email").toString());
							    	cases.put("ADDRESS_TYPE", entry.get("address_type").toString());
							    	cases.put("PRIMARY", entry.get("is_primary").toString());
							    	cases.put("OU_HEAD", entry.get("is_ou_head").toString());
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("OFFICER_WISE_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("OFFICER_WISE_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Officer wise cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
