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

import in.apcfss.struts.commons.CommonModels;
import plugins.DatabasePlugin;


@Path("/nodalOfficersRegistered")
public class RegisteredNodalOfficersAbstractReport {	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/deptwise")
	public static Response getCasesDetails(String incomingData) throws Exception {
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
						
						
						if (!(roleId.trim().equals("1") || roleId.trim().equals("7") || roleId.trim().equals("3") || roleId.trim().equals("4"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";
							return Response.status(200).entity(jsonStr).build();
						}
						else if(user_id.equals("rajat.bhargava@nic.in") ||  user_id.equals("saiprasad@ap.gov.in") ||  user_id.equals("anilkumar.singhal@ap.gov.in")) {
							return getOfficerWise(incomingData);
						}
						
						else if (roleId.trim().equals("1") || roleId.trim().equals("7") ||roleId.trim().equals("3")||roleId.trim().equals("4")) {

							sql="select a1.dept_code as deptcode,upper(description) as deptname, coalesce(hods,0) as hods, coalesce(nodalofficers,0) as nodalofficers"
									+ " from (select dept_code,description from dept_new where deptcode='01' and display=true and dept_id is not null) a1"
									+ " left join ("
									+ " select reporting_dept_code,count(dn.*) as hods,count(nd.*) as nodalofficers from dept_new dn left join nodal_officer_details nd on (dn.dept_code=nd.dept_id)"
									+ "	where dn.display=true and dn.deptcode!='01' and coalesce(nd.dist_id,0)=0 group by reporting_dept_code"
									+ "	) a2 on (a1.dept_code=a2.reporting_dept_code) where 1=1 ";
							
							if(roleId.trim().equals("3")||roleId.trim().equals("4")) {
								sql+=" and a1.dept_code='"+dept_code+"' ";
								
							}
							
							sql+=" order by 1";
						}
						
						System.out.println("Nodal Officers Data SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("Nodal Officers DATA:" + data);						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	
								cases.put("DEPT_CODE", entry.get("deptcode").toString());
						    	cases.put("DEPT_NAME", entry.get("deptname"));				    	
						    	cases.put("NO_HODS", entry.get("hods"));						    	
						    	cases.put("NO_REGISTERED", entry.get("nodalofficers"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("OFFICERS_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Data retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("OFFICERS_DATA", finalList);	
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
	@Path("/getOfficerWise")
	public static Response getOfficerWise(String incomingData) throws Exception {
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
						String dept_id="";
						
						if (jObject.has("SELECTED_DEPT_ID") && !jObject.get("SELECTED_DEPT_ID").toString().equals("")) {
							dept_id = jObject.get("SELECTED_DEPT_ID").toString();	
						}
						else {
							dept_id = dept_code;
						}
						
						
						if (!(roleId.trim().equals("1") || roleId.trim().equals("7") || roleId.trim().equals("3") || roleId.trim().equals("4"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";
							return Response.status(200).entity(jsonStr).build();
						}
						
						
						else if (roleId.trim().equals("1") || roleId.trim().equals("7") ||roleId.trim().equals("3")||roleId.trim().equals("4")) {

							sql = "select d.dept_code, upper(trim(d.description)) as description,slno, user_id, designation, employeeid, mobileno, emailid, aadharno, b.fullname_en, designation_name_en "
									+ "from dept_new d "
									+ "left join (select slno, user_id, designation, employeeid, mobileno, emailid, aadharno, b.fullname_en, designation_name_en, a.dept_id, a.dist_id from nodal_officer_details a  "
									+ "inner join (select distinct employee_id,fullname_en,designation_id, designation_name_en from nic_data) b on (a.employeeid=b.employee_id and a.designation=b.designation_id)"
											+ "  "
									+ "  ) b on (d.dept_code = b.dept_id) where (reporting_dept_code='" + dept_id
									+ "' or dept_code='"+dept_id+"') and substr(dept_code,4,2)!='01' and coalesce(b.dist_id,0)=0  and d.display= true order by 1";
						}
						
						System.out.println("Officer wise SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("OFFICER WISE DATA:" + data);						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	
								cases.put("DEPT_CODE", entry.get("dept_code").toString());
						    	cases.put("DESCRIPTION", entry.get("description"));				    	
						    	cases.put("EMP_NAME", entry.get("fullname_en"));						    	
						    	cases.put("DESIGNATION", entry.get("designation_name_en"));						    	
						    	cases.put("MOBILE", entry.get("mobileno"));
						    	cases.put("EMAIL", entry.get("emailid"));
						    	cases.put("AADHAAR_NO", entry.get("aadharno"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("OFFICERS_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Data retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("OFFICERS_DATA", finalList);	
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