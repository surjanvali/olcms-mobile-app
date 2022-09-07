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


@Path("/nodalOfficers")
public class NodalOfficerAbstractReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getSectDeptWiseAbstract")
	public static Response getDeptWiseAbstract(String incomingData) throws Exception {
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
					
					else {

						String roleId = jObject.get("ROLE_ID").toString();
						String dept_code = jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						if (!(roleId.trim().equals("1") || roleId.trim().equals("7")||roleId.trim().equals("3")||roleId.trim().equals("4"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";
						} 
						else if(user_id.equals("rajat.bhargava@nic.in") ||  user_id.equals("saiprasad@ap.gov.in") ||  user_id.equals("anilkumar.singhal@ap.gov.in")) {
							return getOfficerWise(incomingData);
						}
						
						else if (roleId.trim().equals("1") || roleId.trim().equals("7")||roleId.trim().equals("3")||roleId.trim().equals("4")) {
								
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
							
							System.out.println("SQL:"+sql);
							con = DatabasePlugin.connect();
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
				
							System.out.println("data=" + data);
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isNewDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
									cases.put("DEPT_CODE", entry.get("deptcode"));
									cases.put("DEPT_NAME", entry.get("deptname"));
									cases.put("HODS", entry.get("hods"));
									cases.put("NODAL_REGISTERED", entry.get("nodalofficers"));
							    	finalList.put(cases);
								}
								
									casesData.put("DEPT_WISE_LIST", finalList);							
									isNewDataAvailable=true;
															
								} else {								
									casesData.put("DEPT_WISE_LIST", finalList);									
								}
							
							
								String finalString = casesData.toString();
								
								if (isNewDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Nodal Officers report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
								else
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
	@Path("/getHODDeptWiseAbstract")
	public static Response getHODDeptWiseAbstract(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());

					if (!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					} else if (!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					} else if (!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					} else if (!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					} else if(!jObject.has("SELECTED_DEPT_ID") || jObject.get("SELECTED_DEPT_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_ID is missing in the request.\" }}";
					}
					else {

						String roleId = jObject.get("ROLE_ID").toString();
						String dept_code = jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String selectedSectDept = jObject.get("SELECTED_DEPT_ID").toString();
						String deptId = "";

						if (!(roleId.trim().equals("3") || roleId.trim().equals("2") || roleId.trim().equals("1")
								|| roleId.trim().equals("7"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";

						} else if (roleId.trim().equals("3") || roleId.trim().equals("2") || roleId.trim().equals("1")
								|| roleId.trim().equals("7")) {

							con = DatabasePlugin.connect();

							if (roleId.trim().equals("3")) {
								sql = "select a.inserted_by,d.dept_code,upper(d.description) as dept_desc,count(a.*) as registered_nodal_officers from dept_new d"
										+ " left join nodal_officer_details a on (a.inserted_by=d.reporting_dept_code)"
										+ " where a.inserted_by='" + user_id + "' and d.display=true "
										+ " group by inserted_by, d.description, d.dept_code order by 4 desc, d.description";

							} else if (roleId.trim().equals("2")) {
								sql = "select a.inserted_by,d.sdeptcode||d.deptcode,upper(d.description) as dept_desc,count(a.*) as registered_nodal_officers from dept_new d"
										+ " left join nodal_officer_details a on (a.inserted_by=d.reporting_dept_code)"
										+ " where a.inserted_by='" + user_id + "'  and d.display=true"
										+ " group by inserted_by, d.description, d.sdeptcode||d.deptcode order by 4 desc, d.description";

							} else if (roleId.trim().equals("1") || roleId.trim().equals("7")) {

								sql = "select a.inserted_by,d.sdeptcode||d.deptcode,upper(d.description) as dept_desc,count(a.*) as registered_nodal_officers "
										+ "from (select * from dept_new where reporting_dept_code='" + selectedSectDept
										+ "' and and d.display=true) d "
										+ "left join nodal_officer_details a on (a.inserted_by=d.reporting_dept_code) "
										+ "group by inserted_by, d.description, d.sdeptcode||d.deptcode "
										+ "order by 4 desc, d.description";

							}
							System.out.println("SQL:" + sql);
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							System.out.println("data=" + data);

							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isNewDataAvailable = false;

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("DEPT_NAME", entry.get("dept_desc"));
									cases.put("NODAL_OFFICERS_REGISTERED", entry.get("registered_nodal_officers"));
									finalList.put(cases);
								}

								casesData.put("DEPT_WISE_LIST", finalList);
								isNewDataAvailable = true;

							} else {
								casesData.put("DEPT_WISE_LIST", finalList);
							}

							String finalString = casesData.toString();

							if (isNewDataAvailable)
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Finance category report retrived successfully\"  , "
										+ finalString.substring(1, finalString.length() - 1) + "}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
										+ finalString.substring(1, finalString.length() - 1) + " }}";

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
		String jsonStr = "",sql="";String sqlCondition = "";
		String tableName="nic_data";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());

					if (!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					} else if (!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					} else if (!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					} else if (!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}";
					} else if (!jObject.has("SELECTED_DEPT_ID")
							|| jObject.get("SELECTED_DEPT_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_ID is missing in the request.\" }}";
					}

					else {

						String roleId = jObject.get("ROLE_ID").toString();
						String dept_code = jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String selectedDeptCode = jObject.get("SELECTED_DEPT_ID").toString();

						if (!(roleId.trim().equals("1") || roleId.trim().equals("7") || roleId.trim().equals("3")
								|| roleId.trim().equals("4"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";
						} else if (roleId.trim().equals("1") || roleId.trim().equals("7") || roleId.trim().equals("3")
								|| roleId.trim().equals("4")) {

							String deptId = CommonModels.checkStringObject(selectedDeptCode);
							con = DatabasePlugin.connect();

							sql = "select d.dept_code, upper(trim(d.description)) as description,slno, user_id, designation, employeeid, mobileno, emailid, aadharno, b.fullname_en, designation_name_en "
									// + ", slno, user_id, designation, employeeid, mobileno, emailid, aadharno,
									// b.fullname_en, designation_name_en,d.description "
									+ "from dept_new d "
									+ "left join (select slno, user_id, designation, employeeid, mobileno, emailid, aadharno, b.fullname_en, designation_name_en, a.dept_id, a.dist_id from nodal_officer_details a  "
									+ "inner join (select distinct employee_id,fullname_en,designation_id, designation_name_en from "
									+ tableName
									+ ") b on (a.employeeid=b.employee_id and a.designation=b.designation_id)" + "  "
									+ "  ) b on (d.dept_code = b.dept_id) where (reporting_dept_code='" + deptId
									+ "' or dept_code='" + deptId
									+ "') and substr(dept_code,4,2)!='01' and coalesce(b.dist_id,0)=0  and d.display= true order by 1";

							System.out.println("getOfficerWise SQL:" + sql);

							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isNewDataAvailable = false;

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("DEPT_CODE", entry.get("dept_code").toString());
									cases.put("DEPT_NAME", entry.get("description"));
									cases.put("EMP_NAME", entry.get("fullname_en"));
									cases.put("DESIGNATION", entry.get("designation_name_en"));
									cases.put("MOBILE", entry.get("mobileno"));
									cases.put("EMAIL_ID", entry.get("emailid"));
									cases.put("AADHAR_NO", entry.get("aadharno"));

									finalList.put(cases);
								}

								casesData.put("OFFICERS_LIST", finalList);
								isNewDataAvailable = true;

							} else {
								casesData.put("OFFICERS_LIST", finalList);
							}

							String finalString = casesData.toString();

							if (isNewDataAvailable)
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Officers list retrived successfully\"  , "
										+ finalString.substring(1, finalString.length() - 1) + "}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
										+ finalString.substring(1, finalString.length() - 1) + " }}";

						}
					}
				}else {
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