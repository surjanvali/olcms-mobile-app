package in.apcfss.olcms;

import java.sql.Connection;
import java.util.ArrayList;
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

import plugins.DatabasePlugin;


@Path("/newCases")
public class AcksAbstractReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getDeptWiseAbstract")
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

						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and ad.distid='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and d.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString() + "' ";
						}
						
						if (jObject.has("SELECTED_ADVOCATE_NAME") && jObject.get("SELECTED_ADVOCATE_NAME") != null
								&& !jObject.get("SELECTED_ADVOCATE_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(advocatename,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_ADVOCATE_NAME").toString() + "%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(petitioner_name,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_PETITIONER_NAME").toString() + "%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and ad.inserted_time::date >= to_date('" + jObject.get("FROM_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and ad.inserted_time::date <= to_date('" + jObject.get("TO_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}
						
						
						if ((roleId.equals("1") || roleId.equals("7") || roleId.equals("14"))) {
							sqlCondition += " and respondent_slno=1 ";
						}

						if (!(roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("14")
								|| roleId.equals("6"))) {
							sqlCondition += " and (dm.dept_code='" + dept_code + "' or dm.reporting_dept_code='" + dept_code + "') ";
						}
						System.out.println("roleId---" + roleId);
						

						String condition = "";
						if ((roleId.equals("6"))) {
							condition = " left join ecourts_mst_gp_dept_map egm on (egm.dept_code=d.dept_code) ";
							sqlCondition += " and egm.gp_id='" + user_id + "'";
						}

						if (roleId.equals("2")) {
							sqlCondition += " and ad.distid='" + dist_id + "' ";
						}

						if (jObject.has("SELECTED_CASE_TYPE") && jObject.get("SELECTED_CASE_TYPE") != null
								&& !jObject.get("SELECTED_CASE_TYPE").toString().equals("")) {
							sqlCondition += " and ad.casetype='" + jObject.get("SELECTED_CASE_TYPE").toString().trim() + "' ";
						}

						sql = "select d.dept_code,upper(description) as description,count(distinct ad.ack_no) as acks from ecourts_gpo_ack_dtls ad  inner join ecourts_gpo_ack_depts d on (ad.ack_no=d.ack_no) "
								+ "inner join dept_new dm on (d.dept_code=dm.dept_code) " + condition + " "
								+ " where ack_type='NEW' and respondent_slno=1 " + sqlCondition
								+ " group by d.dept_code,description " + " order by d.dept_code,description";

						System.out.println("SHOW DEPT WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("PRIMARY_RESPONDENT", entry.get("dept_code").toString()+" - "+entry.get("description").toString());
								cases.put("CASES_REGISTERED", entry.get("acks").toString());
								
								finalList.put(cases);
							}

							casesData.put("DEPT_WISE_LIST", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("DEPT_WISE_LIST", finalList);

						}

						String finalString = casesData.toString();

						if (isDataAvailable)
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";
						else
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1, finalString.length() - 1) + " }}";

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
	@Path("/getCasesListForDept")
	public static Response getCasesListForDept(String incomingData) throws Exception {
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
						
												
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and a.distid='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and ad.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString() + "' ";
						}
						
						if (jObject.has("SELECTED_ADVOCATE_NAME") && jObject.get("SELECTED_ADVOCATE_NAME") != null
								&& !jObject.get("SELECTED_ADVOCATE_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(advocatename,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_ADVOCATE_NAME").toString() + "%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(petitioner_name,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_PETITIONER_NAME").toString() + "%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and a.inserted_time::date >= to_date('" + jObject.get("FROM_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and a.inserted_time::date <= to_date('" + jObject.get("TO_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						
						
						if ((roleId.equals("1") || roleId.equals("7") || roleId.equals("14"))) {
							sqlCondition += " and respondent_slno=1 ";
						}

						if (!(roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("14")
								|| roleId.equals("6"))) {
							sqlCondition += " and (dmt.dept_code='" + dept_code + "' or dmt.reporting_dept_code='" + dept_code + "') ";
						}
						System.out.println("roleId---" + roleId);
						

						String condition = "";
						if ((roleId.equals("6"))) {
							condition = " left join ecourts_mst_gp_dept_map egm on (egm.dept_code=d.dept_code) ";
						}

						if (roleId.equals("2")) {
							sqlCondition += " and a.distid='" + dist_id + "' ";
						}

						if (jObject.has("SELECTED_CASE_TYPE") && jObject.get("SELECTED_CASE_TYPE") != null
								&& !jObject.get("SELECTED_CASE_TYPE").toString().equals("")) {
							sqlCondition += " and a.casetype='" + jObject.get("SELECTED_CASE_TYPE").toString().trim() + "' ";
						}

						sql = "select distinct a.slno , a.ack_no , distid , advocatename ,advocateccno , casetype , maincaseno , remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
								+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
								+ "to_char(inserted_time,'dd-mm-yyyy') as generated_date, getack_dept_desc(a.ack_no::text) as dept_descs ,inserted_time, coalesce(a.hc_ack_no,'-') as hc_ack_no "//getack_dept_desc(a.ack_no) as dept_descs,
								+ "from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
								+ "inner join district_mst dm on (a.distid=dm.district_id) "
								+ "inner join dept_new dmt on (ad.dept_code=dmt.dept_code)  " + condition + " "
								+ "inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name) "
								+ "where a.delete_status is false and ack_type='NEW' and respondent_slno=1 " + sqlCondition
								+ "order by inserted_time desc";

						System.out.println("SHOW CASE WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("ACK_NO", entry.get("ack_no").toString());
								cases.put("HC_ACK_NO", entry.get("hc_ack_no").toString());
								cases.put("DATE", entry.get("generated_date").toString());
								cases.put("DIST_NAME", entry.get("district_name").toString());
								cases.put("CASE_TYPE", entry.get("case_full_name").toString());
								cases.put("MAIN_CASE_NO", entry.get("maincaseno").toString());
								cases.put("DEPARTMENTS", entry.get("dept_descs").toString());
								cases.put("ADVOCATE_CCNO", entry.get("advocateccno").toString());
								cases.put("ADVOCATE_NAME", entry.get("advocatename").toString());
								cases.put("ACK_FILE_PATH", entry.get("ack_file_path"));
								cases.put("BARCODE_FILE_PATH", entry.get("barcode_file_path"));
								finalList.put(cases);
							}

							casesData.put("CASES_LIST", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("CASES_LIST", finalList);

						}

						String finalString = casesData.toString();

						if (isDataAvailable)
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";
						else
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1, finalString.length() - 1) + " }}";

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
	@Path("/getDistWiseAbstract")
	public static Response getDistWiseAbstract(String incomingData) throws Exception {
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

						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and ad.distid='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and d.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString() + "' ";
						}
						
						if (jObject.has("SELECTED_ADVOCATE_NAME") && jObject.get("SELECTED_ADVOCATE_NAME") != null
								&& !jObject.get("SELECTED_ADVOCATE_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(advocatename,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_ADVOCATE_NAME").toString() + "%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(petitioner_name,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_PETITIONER_NAME").toString() + "%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and inserted_time::date >= to_date('" + jObject.get("FROM_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and inserted_time::date <= to_date('" + jObject.get("TO_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						
						
						
						if (!(roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("14"))) {
							sqlCondition += " and (dmt.dept_code='" + dept_code + "' or dmt.reporting_dept_code='" + dept_code + "') ";
						}
						System.out.println("roleId---" + roleId);
						

						
						if (roleId.equals("2")) {
							sqlCondition += " and ad.distid='" + dist_id + "' ";
						}

						if (jObject.has("SELECTED_CASE_TYPE") && jObject.get("SELECTED_CASE_TYPE") != null
								&& !jObject.get("SELECTED_CASE_TYPE").toString().equals("")) {
							sqlCondition += " and ad.casetype='" + jObject.get("SELECTED_CASE_TYPE").toString().trim() + "' ";
						}

						sql = "select distid,district_name,count(distinct ad.ack_no) as acks from ecourts_gpo_ack_dtls ad "
								+ " inner join district_mst dm on (ad.distid=dm.district_id) "
								+ " inner join ecourts_gpo_ack_depts d on (ad.ack_no=d.ack_no) "
								+ "inner join dept_new dmt on (d.dept_code=dmt.dept_code)" + " where ack_type='NEW' and respondent_slno=1 " + sqlCondition
								+ " group by distid,dm.district_name order by district_name";

						System.out.println("SHOW DIST WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("DIST_ID", entry.get("distid").toString());
								cases.put("DIST_NAME", entry.get("district_name").toString());
								cases.put("CASES_REGISTERED", entry.get("acks").toString());
								
								finalList.put(cases);
							}

							casesData.put("DIST_WISE_LIST", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("DIST_WISE_LIST", finalList);

						}

						String finalString = casesData.toString();

						if (isDataAvailable)
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";
						else
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1, finalString.length() - 1) + " }}";

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
	@Path("/getUserWiseAbstract")
	public static Response getUserWiseAbstract(String incomingData) throws Exception {
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

						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and ad.distid='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and d.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString() + "' ";
						}
						
						if (jObject.has("SELECTED_ADVOCATE_NAME") && jObject.get("SELECTED_ADVOCATE_NAME") != null
								&& !jObject.get("SELECTED_ADVOCATE_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(advocatename,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_ADVOCATE_NAME").toString() + "%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(petitioner_name,' ',''),'.','') ilike  '%"
									+ jObject.get("SELECTED_PETITIONER_NAME").toString() + "%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and ad.inserted_time::date >= to_date('" + jObject.get("FROM_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and ad.inserted_time::date <= to_date('" + jObject.get("TO_DATE").toString()
							+ "','dd-mm-yyyy') ";
						}
						
						
						if (!(roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("14"))) {
							sqlCondition += " and (dm.dept_code='" + dept_code + "' or dm.reporting_dept_code='" + dept_code + "') ";
						}
						System.out.println("roleId---" + roleId);
						

						
						if (roleId.equals("2")) {
							sqlCondition += " and ad.distid='" + dist_id + "' ";
						}

						if (jObject.has("SELECTED_CASE_TYPE") && jObject.get("SELECTED_CASE_TYPE") != null
								&& !jObject.get("SELECTED_CASE_TYPE").toString().equals("")) {
							sqlCondition += " and ad.casetype='" + jObject.get("SELECTED_CASE_TYPE").toString().trim() + "' ";
						}

						sql = "select inserted_by,count(distinct ad.ack_no) as acks from ecourts_gpo_ack_dtls ad "
								+ " inner join district_mst dm on (ad.distid=dm.district_id) "
								+ " inner join ecourts_gpo_ack_depts d on (ad.ack_no=d.ack_no) "
								+ "inner join dept_new dmt on (d.dept_code=dmt.dept_code)" + " where ack_type='NEW' and respondent_slno=1 " + sqlCondition
								+ " group by inserted_by";

						

						System.out.println("SHOW USER WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("USER", entry.get("inserted_by").toString());								
								cases.put("CASES_REGISTERED", entry.get("acks").toString());
								
								finalList.put(cases);
							}

							casesData.put("USER_WISE_LIST", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("USER_WISE_LIST", finalList);

						}

						String finalString = casesData.toString();

						if (isDataAvailable)
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";
						else
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1, finalString.length() - 1) + " }}";

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
	@Path("/displayCaseFilters")
	public static Response getCasesListFilters(String incomingData) throws Exception {
		
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
				} else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "";

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					con = DatabasePlugin.connect();
					
					
					
					if (roleId.equals("2")) {
						sql = "select district_id,upper(district_name) as dist_name from district_mst where district_id='" + distId + "' order by district_name";
					}
						
					else {
						sql = "select district_id,upper(district_name) as dist_name from district_mst order by district_name";
					}
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

					JSONArray distList = new JSONArray();
					JSONObject casesData = new JSONObject();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DIST_CODE", entry.get("district_id").toString());								
							cases.put("DIST_NAME", entry.get("dist_name").toString());
							
							distList.put(cases);
						}
					} 
					casesData.put("DIST_LIST", distList);					
					
					
					if (roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("14")) {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true order by dept_code";
					}
						
					else if (roleId.equals("6")) { // GPO
						sql = " select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where "
								+ " dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='" + userId
								+ "') or "
								+ " reporting_dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='"
								+ userId + "')" + " order by dept_code";						
						
					}
					else {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true and reporting_dept_code='"
										+ deptCode + "' or dept_code='" + deptCode + "' order by dept_code";
					}						
					
					data = DatabasePlugin.executeQuery(sql, con);

					JSONArray deptList = new JSONArray();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DEPT_ID", entry.get("dept_code").toString());								
							cases.put("DEPT_DESC", entry.get("dept_desc").toString());
							
							deptList.put(cases);
						}
					} 
					casesData.put("DEPT_LIST", deptList);
					
					
					sql = "select sno,case_full_name from case_type_master order by sno";
					
					data = DatabasePlugin.executeQuery(sql, con);

					JSONArray caseTypeList = new JSONArray();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("SNO", entry.get("sno").toString());								
							cases.put("CASE_TYPE", entry.get("case_full_name").toString());
							
							caseTypeList.put(cases);
						}
					} 
					casesData.put("CASE_TYPE_LIST", caseTypeList);
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases Filters retrived successfully\"  , "
								+ finalString.substring(1, finalString.length() - 1) + "}}";
					else
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
								+ finalString.substring(1, finalString.length() - 1) + " }}";
					
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