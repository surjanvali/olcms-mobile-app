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

@Path("/districtNodalOfficers")
public class DistrictNodalOfficersAbstractReport {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/distWiseReport")
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
					
					
					sql = "select dist_id as  distid,upper(b.district_name) as district_name,count(*) as acks From nodal_officer_details a "
							+ "inner join district_mst b on (a.dist_id=b.district_id) where 1=1 ";

					if (!dept_code.equals("") && !dept_code.equals("0"))
						sql += " and a.dept_id='" + dept_code + "'";

					if (!dist_id.equals("") && !dist_id.equals("0"))
						sql += " and a.dist_id='" + dist_id + "'";

					sql += " group by a.dist_id,b.district_name order by district_name ";

					

					System.out.println("SQL:" + sql);
					con = DatabasePlugin.connect();	

					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;
					
					if (data != null && !data.isEmpty() && data.size() > 0) {
						
						for (Map<String, Object> entry : data) {								   
							JSONObject cases = new JSONObject();
					    	cases.put("DIST_NAME", entry.get("district_name").toString());						    	
					    	cases.put("NO_REGISTERED", entry.get("acks").toString());
					    	
					    	finalList.put(cases);
						}
						
						casesData.put("DIST_WISE_LIST", finalList);
						isDataAvailable = true;						
													
						} else {
							
							casesData.put("DIST_WISE_LIST", finalList);	
							
						}
					
						String finalString = casesData.toString();
						
						if (isDataAvailable)					    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Dist. wise list retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/caseWiseReport")
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
					else if(!jObject.has("SELECTED_DIST_ID") || jObject.get("SELECTED_DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DIST_ID is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					String dist_id = jObject.get("DIST_ID").toString();
					String user_id = jObject.get("USER_ID").toString();	
					String selectedDistCode = jObject.get("SELECTED_DIST_ID").toString();
					String distId = "";
					
					if (roleId.equals("2")) {
						distId = CommonModels.checkStringObject(dist_id);
					} else {
						if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals(""))
								distId = CommonModels.checkStringObject(jObject.get("SELECTED_DIST_ID"));
						
					}
						
					con = DatabasePlugin.connect();	
					
					
					String tableName = "";
					System.out.println("dist--" + distId);
					tableName = AjaxModels.getTableName(CommonModels.checkStringObject(distId), con);


					sql = "select m.dept_id,upper(d.description) as description,trim(nd.fullname_en) as fullname_en, trim(nd.designation_name_en) as designation_name_en,m.mobileno,m.emailid from nodal_officer_details m "
							+ "inner join (select distinct employee_id,fullname_en,designation_name_en, designation_id from "
							+ tableName + ") nd on (m.employeeid=nd.employee_id and m.designation=nd.designation_id)"
							+ "inner join users u on (m.emailid=u.userid)"
							+ "inner join dept_new d on (m.dept_id=d.dept_code)" + "where m.dist_id='" + distId + "'";

					if (!dept_code.equals("") && !dept_code.equals("0"))
						sql += " and  m.dept_id='" + dept_code + "'";

					sql += " order by 1";
						
							
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DEPT_CODE", entry.get("dept_id"));						    	
							    	cases.put("DEPT_NAME", entry.get("description"));
							    	cases.put("EMP_NAME", entry.get("fullname_en"));
							    	cases.put("DESIGNATION", entry.get("designation_name_en"));
							    	cases.put("MOBILE", entry.get("mobileno"));
							    	cases.put("EMAIL", entry.get("emailid"));
							    	
							    	
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
