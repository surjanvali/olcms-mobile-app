package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

@Path("/countersFiled")
public class HCFinalOrdersImplementedReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/contemptCasesReport")
	public static Response contemptCasesReport(String incomingData) throws Exception {
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
					String sqlCondition = "";

						
					con = DatabasePlugin.connect();

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9") || roleId.equals("10"))
						sqlCondition += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
								+ dept_code + "')";

					if (roleId.equals("2") || roleId.equals("10")) {
						sqlCondition += " and a.dist_id='" + dist_id + "' ";
					}

					sql = "select dist_id, district_name, casescount, counterscount" + " from ( "
							+ " select dist_id,dm.district_name,count(distinct a.cino) as casescount,  "
							+ " sum(case when length(counter_filed_document)> 10 then 1 else 0 end) as counterscount   "
							+ "  "
							+ " from district_mst dm left join ecourts_case_data a on (a.dist_id=dm.district_id)"
							+ " inner join dept_new dn on (a.dept_code=dn.dept_code) " + " "
							+ " left join ecourts_olcms_case_details ocd on (a.cino=ocd.cino) "
							+ " where type_name_reg='CC' " +sqlCondition
							+ " group by dist_id, dm.district_name) a1 order by casescount desc";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DIST_NAME", entry.get("district_name").toString());						    	
							    	cases.put("CASE_COUNT", entry.get("casescount").toString());
							    	cases.put("COUNTER_FILED", entry.get("counterscount"));
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("CC_CASES_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("CC_CASES_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Contempt cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/finalOrdersImplReport")
	public static Response finalOrdersImplReport(String incomingData) throws Exception {
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
					String sqlCondition = "";

						
					con = DatabasePlugin.connect();

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9") || roleId.equals("10"))
						sqlCondition += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
								+ dept_code + "')";

					if (roleId.equals("2") || roleId.equals("10")) {
						sqlCondition += " and a.dist_id='" + dist_id + "' ";
					}

					sql = "select dist_id, district_name, casescount,order_implemented,appeal_filed,  "
							+ " casescount-(order_implemented + appeal_filed) as pending,   "
							+ " (order_implemented + appeal_filed)/(4*100) as actoin_taken_percent " + " from ( "
							+ " select dist_id,dm.district_name,count(distinct a.cino) as casescount,  "
							+ " sum(case when length(action_taken_order)> 10 then 1 else 0 end) as order_implemented , "
							+ " sum(case when length(appeal_filed_copy)> 10 then 1 else 0 end) as appeal_filed " + "  "
							+ " from district_mst dm left join ecourts_case_data a on (a.dist_id=dm.district_id)"
							+ " inner join ecourts_case_finalorder b on (a.cino=b.cino)  "
							+ " inner join dept_new dn on (a.dept_code=dn.dept_code) " + " "
							+ " left join ecourts_olcms_case_details ocd on (a.cino=ocd.cino) "
							+ " where 1=1 " +sqlCondition
							+ " group by dist_id, dm.district_name) a1 order by casescount desc";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DIST_NAME", entry.get("district_name").toString());						    	
							    	cases.put("ORDERS_COUNT", entry.get("casescount").toString());
							    	cases.put("ORDERS_IMPL", entry.get("order_implemented"));
							    	cases.put("APPEAL_FILED", entry.get("appeal_filed"));
							    	cases.put("PENDING", entry.get("pending"));
							    	cases.put("ACTION_TAKEN_PERCENT", entry.get("actoin_taken_percent"));
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("FINAL_ORDERS_IMPL_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("FINAL_ORDERS_IMPL_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Contempt cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/getNewCasesReport")
	public static Response getNewCasesReport(String incomingData) throws Exception {
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
					String sqlCondition = "";

						
					con = DatabasePlugin.connect();

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9") || roleId.equals("10"))
						sqlCondition += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
								+ dept_code + "')";

					if (roleId.equals("2") || roleId.equals("10")) {
						sqlCondition += " and a.dist_id='" + dist_id + "' ";
					}

					sql = "select dist_id, district_name, casescount, counterscount from ( "
							+ " select dist_id,dm.district_name,count(distinct a.cino) as casescount,    "
							+ " sum(case when length(counter_filed_document)> 10 then 1 else 0 end) as counterscount   " + "  "
							+ " from district_mst dm left join ecourts_case_data a on (a.dist_id=dm.district_id)"
							+ " inner join dept_new dn on (a.dept_code=dn.dept_code) " + " "
							+ " left join ecourts_olcms_case_details ocd on (a.cino=ocd.cino) "
							+ " where dt_regis >= current_date - 30  "+sqlCondition 
							+ " group by dist_id, dm.district_name) a1 order by casescount desc";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DIST_NAME", entry.get("district_name").toString());						    	
							    	cases.put("CASE_COUNT", entry.get("casescount").toString());
							    	cases.put("COUNTER_FILED", entry.get("counterscount"));
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("NEW_CASES_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("NEW_CASES_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Contempt cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/getLegacyCasesReport")
	public static Response getLegacyCasesReport(String incomingData) throws Exception {
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
					String sqlCondition = "";

						
					con = DatabasePlugin.connect();

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and dt_regis >= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9") || roleId.equals("10"))
						sqlCondition += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
								+ dept_code + "')";

					if (roleId.equals("2") || roleId.equals("10")) {
						sqlCondition += " and a.dist_id='" + dist_id + "' ";
					}

					sql = "select dist_id, district_name, casescount, counterscount from ( "
							+ " select dist_id,dm.district_name,count(distinct a.cino) as casescount,  "
							+ " sum(case when length(counter_filed_document)> 10 then 1 else 0 end) as counterscount   " + "  "
							+ " from district_mst dm left join ecourts_case_data a on (a.dist_id=dm.district_id)"
							+ " inner join dept_new dn on (a.dept_code=dn.dept_code) " + " "
							+ " left join ecourts_olcms_case_details ocd on (a.cino=ocd.cino) "
							+ " where 1=1 "+sqlCondition
							+ " group by dist_id, dm.district_name) a1 order by casescount desc";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DIST_NAME", entry.get("district_name").toString());						    	
							    	cases.put("CASE_COUNT", entry.get("casescount").toString());
							    	cases.put("COUNTER_FILED", entry.get("counterscount"));
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("LEGACY_CASES_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("LEGACY_CASES_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Contempt cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
