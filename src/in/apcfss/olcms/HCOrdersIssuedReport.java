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


@Path("/hcOrders")
public class HCOrdersIssuedReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getOrdersIssuedReport")
	public static Response getOrdersIssuedReport(String incomingData) throws Exception {
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

						System.out.println("roleId--" + roleId);

						if (roleId.equals("5") || roleId.equals("9")) {
							return getHODWiseOrdersIssuedReport(incomingData);
						}

						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and order_date >= to_date('" + jObject.get("FROM_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and order_date <= to_date('" + jObject.get("TO_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}

						sql = "select x.reporting_dept_code as deptcode, upper(d1.description) as description,sum(total_cases) as total_cases, sum(interim_order_cases) as interim_order_cases, sum(final_order_cases) as final_order_cases,  "
								+ " sum(interim_orders)  as interim_orders, sum(final_orders) as final_orders from "
								+ "(select a.dept_code , case when reporting_dept_code='CAB01' then d.dept_code else reporting_dept_code end as reporting_dept_code,"
								+ " count(*) as total_cases, count(distinct io.cino) as interim_order_cases, count(distinct fo.cino) as final_order_cases,sum(coalesce(interim_orders,'0')::int4)  as interim_orders, sum(coalesce(final_orders,'0')::int4) as final_orders "
								+ " from ecourts_case_data a inner join dept_new d on (a.dept_code=d.dept_code) "
								+ " left join (select cino, count(*) as interim_orders from ecourts_case_interimorder where 1=1 "
								+ sqlCondition + " group by cino) io on (a.cino=io.cino) "
								+ " left join (select cino, count(*) as final_orders from ecourts_case_finalorder  where 1=1 "
								+ sqlCondition + " group by cino) fo on (a.cino=fo.cino) " + " where d.display = true ";

						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "')";
						else if (roleId.equals("2")) {
							sql += " and a.dist_id='" + dist_id + "'";
						}

						sql += " group by a.dept_code,d.dept_code ,reporting_dept_code ) x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code)"
								+ " group by x.reporting_dept_code, d1.description order by 1";

						System.out.println("HC Orders SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("DEPT_CODE", entry.get("deptcode").toString());
								cases.put("DEPT_NAME", entry.get("description"));
								cases.put("TOTAL_CASES", entry.get("total_cases").toString());
								cases.put("INTERIM_ORDERS_CASES", entry.get("interim_order_cases").toString());
								cases.put("INTERIM_ORDERS_ISSUED", entry.get("interim_orders").toString());
								cases.put("FINAL_ORDERS_CASES", entry.get("final_order_cases").toString());
								cases.put("FINAL_ORDERS_ISSUED", entry.get("final_orders").toString());
								
								finalList.put(cases);
							}

							casesData.put("SEC_DEPT_WISE_ORDERS_ISSUED_REPORT", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("SEC_DEPT_WISE_ORDERS_ISSUED_REPORT", finalList);

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
	@Path("/getHODWiseOrdersIssuedReport")
	public static Response getHODWiseOrdersIssuedReport(String incomingData) throws Exception {
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

						System.out.println("roleId--" + roleId);

						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and order_date >= to_date('" + jObject.get("FROM_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and order_date <= to_date('" + jObject.get("TO_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}

						sql="select a.dept_code as deptcode , d.description,count(*) as total_cases, "
								+ " count(distinct io.cino) as interim_order_cases, count(distinct fo.cino) as final_order_cases,sum(coalesce(interim_orders,'0')::int4)  as interim_orders, sum(coalesce(final_orders,'0')::int4) as final_orders "
								+ " from ecourts_case_data a inner join dept_new d on (a.dept_code=d.dept_code) "
								+ " left join (select cino,count(*) as interim_orders from ecourts_case_interimorder where 1=1 "+sqlCondition+" group by cino) io on (a.cino=io.cino) "
								+ " left join (select cino,count(*) as final_orders from ecourts_case_finalorder  where 1=1 "+sqlCondition+" group by cino) fo on (a.cino=fo.cino) "
								+ " where d.display = true ";
						
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
							if(roleId.equals("2")){
								sql+=" and a.dist_id='"+dist_id+"' ";
							}
							
						sql += " group by a.dept_code,d.description order by 1";

						System.out.println("HC Orders SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("DEPT_CODE", entry.get("deptcode").toString());
								cases.put("DEPT_NAME", entry.get("description"));
								cases.put("TOTAL_CASES", entry.get("total_cases").toString());
								cases.put("INTERIM_ORDERS_CASES", entry.get("interim_order_cases").toString());
								cases.put("INTERIM_ORDERS_ISSUED", entry.get("interim_orders").toString());
								cases.put("FINAL_ORDERS_CASES", entry.get("final_order_cases").toString());
								cases.put("FINAL_ORDERS_ISSUED", entry.get("final_orders").toString());
								
								finalList.put(cases);
							}

							casesData.put("HOD_DEPT_WISE_ORDERS_ISSUED_REPORT", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("HOD_DEPT_WISE_ORDERS_ISSUED_REPORT", finalList);

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
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {
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
					else if(!jObject.has("DEPT_TYPE") || jObject.get("DEPT_TYPE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_TYPE is missing in the request.\" }}";
					}
					else if(!jObject.has("ORDER_TYPE") || jObject.get("ORDER_TYPE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ORDER_TYPE is missing in the request.\" }}";
					}
					else {

						String roleId = jObject.get("ROLE_ID").toString();
						String dept_code = jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String deptType = jObject.get("DEPT_TYPE").toString();
						String orderType = jObject.get("ORDER_TYPE").toString();

						System.out.println("roleId--" + roleId);

						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							sqlCondition += " and order_date >= to_date('" + jObject.get("FROM_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and order_date <= to_date('" + jObject.get("TO_DATE").toString()
									+ "','dd-mm-yyyy') ";
						}

						
						String condition="";
						//if (roleId.equals("6") )
							//condition= " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) ";
						
						
						
						sql = "select a.*, b.orderpaths, coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, prayer, ra.address from ecourts_case_data a "
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "								
								+ ""+condition+" inner join" + " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from (select * from";

						if (orderType.equals("IO") && roleId.equals("6"))
							sql += "  (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder "
									+ " where 1=1  " + sqlCondition + ") x1";
						
						if (orderType.equals("IO") && !roleId.equals("6"))
							sql += "  (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
									+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 " + sqlCondition + ") x1";

						if (orderType.equals("FO") && !roleId.equals("6"))
							sql += " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
									+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
									+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 " + sqlCondition + ") x2";
						
						if (orderType.equals("FO") && roleId.equals("6"))
							sql += " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder "
									+ " where length(order_document_path) > 10  " + sqlCondition + ") x2";

						sql += " order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code)  where d.display = true ";

						// sql += " and (reporting_dept_code='" + deptCode + "' or a.dept_code='" + deptCode + "') ";
						if (deptType.equals("HOD"))
							sql += " and (a.dept_code='" + dept_code + "') ";
						else  if (deptType.equals("SD"))
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
						
						
						
						if(roleId.equals("3") || roleId.equals("4")) {
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
						}
						if(roleId.equals("5") || roleId.equals("9")) {
							sql += " and (a.dept_code='" + dept_code + "') ";
						}
						
						
						if(roleId.equals("2")){
							sql+=" and a.dist_id='"+dist_id+"'";
						}
						
						System.out.println("CASES DATA SQL:" + sql);
						
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("CINO", entry.get("cino").toString());
								cases.put("SCANNED_AFFIDAVIT", entry.get("scanned_document_path"));
								cases.put("TOTAL_CASES", entry.get("date_of_filing"));
								cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
						    	cases.put("PRAYER", entry.get("prayer").toString());
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_OF_NEXT_LISTING", entry.get("date_of_filing").toString());
						    	cases.put("BENCH", entry.get("bench_name").toString());
						    	cases.put("JUDGE_NAME", "Hon'ble Judge " +entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DISTRICT_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE", entry.get("purpose_name"));
						    	cases.put("RESPONDENTS", entry.get("res_name")+","+entry.get("address"));
						    	cases.put("PETITIONER_ADVOCATE", entry.get("pet_adv"));
						    	cases.put("RESPONDENT_ADVOCATE", entry.get("res_adv"));
						    	cases.put("ORDER_PATHS", entry.get("orderpaths"));
								
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
	
	
	
	
}