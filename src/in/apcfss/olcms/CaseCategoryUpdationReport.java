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


@Path("/caseCategoryUpdation")
public class CaseCategoryUpdationReport {
	
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
						
						if (roleId.equals("5") || roleId.equals("9")) {
							return getHODDeptWiseAbstract(incomingData);
						} else // if(roleId.equals("3") || roleId.equals("4"))
						{
							String str1 = "";

							if ((roleId.equals("4") || roleId.equals("5") || roleId.equals("3"))
									&& dept_code.equals("FIN01")) {

								str1 = " ";
							} else {

								str1 = " where b.dept_code='" + dept_code + "' ";
							}

							if (roleId.equals("1") || roleId.equals("7"))
								str1 = " ";

							sql = "select x.reporting_dept_code as deptcode, upper(d1.description) as description,   sum(a1) as a1 ,sum(a2) as a2 , sum(b1) as b1 ,  sum(b2) as b2, sum(c1) as c1 ,sum(c2)as c2  from "
									+ "( select c.dept_code , case when reporting_dept_code='CAB01' then c.dept_code else reporting_dept_code end as reporting_dept_code,"
									+ " c.dept_code as deptcode,upper(c.description) as description, "
									+ "  coalesce(sum (case  when a.finance_category='A1' then 1 end),'0') as A1 ,"
									+ " coalesce(sum (case  when a.finance_category='A2' then 1 end),'0') as A2 ,"
									+ " coalesce(sum (case  when a.finance_category='B1' then 1 end),'0') as B1 ,"
									+ " coalesce(sum (case  when a.finance_category='B2' then 1 end),'0') as B2 ,"
									+ " coalesce(sum (case  when a.finance_category='C1' then 1 end),'0') as C1 ,"
									+ " coalesce(sum (case  when a.finance_category='C2' then 1 end),'0') as C2  "
									+ " from ecourts_case_category_wise_data a "
									+ " inner join ecourts_case_data b on (a.cino=b.cino)      "
									+ "  inner join dept_new c on (b.dept_code=c.dept_code)     " + str1 + "  "
									+ " group by description,c.dept_code,c.reporting_dept_code order by c.description )  "
									+ " x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code) "
									+ " group by x.reporting_dept_code, d1.description,a1 ,a2 , b1 ,  b2 , c1 ,c2 order by 1";

							System.out.println("SHOW SEC DEPT WISE SQL: " + sql);
							con = DatabasePlugin.connect();
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("DEPT_NAME", entry.get("description").toString());
									cases.put("A1", entry.get("a1"));
									cases.put("A2", entry.get("a2"));
									cases.put("B1", entry.get("b1"));
									cases.put("B2", entry.get("b2"));
									cases.put("C1", entry.get("c1"));
									cases.put("C2", entry.get("c2"));

									finalList.put(cases);
								}

								casesData.put("DEPT_WISE_LIST", finalList);
								isDataAvailable = true;

							} else {

								casesData.put("DEPT_WISE_LIST", finalList);

							}

							String finalString = casesData.toString();

							if (isDataAvailable)
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
						String deptId = "";
						
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							deptId = jObject.get("SELECTED_DEPT_ID").toString();
						}
						else {
							deptId = dept_code;
						}
						
						sql = " select c.dept_code as deptcode,upper(c.description) as description, "
								+ " coalesce(sum (case  when a.finance_category='A1' then 1 end),'0') as A1 ,"
								+ "coalesce(sum (case  when a.finance_category='A2' then 1 end),'0') as A2 ,"
								+ "coalesce(sum (case  when a.finance_category='B1' then 1 end),'0') as B1 ,"
								+ "coalesce(sum (case  when a.finance_category='B2' then 1 end),'0') as B2 ,"
								+ "coalesce(sum (case  when a.finance_category='C1' then 1 end),'0') as C1 ,"
								+ "coalesce(sum (case  when a.finance_category='C2' then 1 end),'0') as C2 "
								+ " from ecourts_case_category_wise_data a  inner join ecourts_case_data b on (a.cino=b.cino)       "
								+ " inner join dept_new c on (b.dept_code=c.dept_code) " + " where ( c.reporting_dept_code='"
								+ deptId + "' or b.dept_code='" + deptId + "' ) " // request.getParameter("deptId").toString()
								+ " group by description,c.dept_code order by c.description";
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("HOD DEPT WISE NEW CASE DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
								cases.put("DEPT_NAME", entry.get("description").toString());
								cases.put("A1", entry.get("a1"));
								cases.put("A2", entry.get("a2"));
								cases.put("B1", entry.get("b1"));
								cases.put("B2", entry.get("b2"));
								cases.put("C1", entry.get("c1"));
								cases.put("C2", entry.get("c2"));
						    	finalList.put(cases);
							}
							
								casesData.put("HOD_DEPT_WISE_NEW_CASE_DATA", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("HOD_DEPT_WISE_NEW_CASE_DATA", finalList);									
							}
						
						
							String finalString = casesData.toString();
							
							if (isNewDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Finance category report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
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
					else if(!jObject.has("SELECTED_DEPT_ID") || jObject.get("SELECTED_DEPT_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("SELECTED_DEPT_TYPE") || jObject.get("SELECTED_DEPT_TYPE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_TYPE is missing in the request.\" }}";
					}
					else if(!jObject.has("FIN_CATEGORY") || jObject.get("FIN_CATEGORY").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- FIN_CATEGORY is missing in the request.\" }}";
					}
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String deptType = jObject.get("SELECTED_DEPT_TYPE").toString();
						String finCategoryType = jObject.get("FIN_CATEGORY").toString();
						String selectedDeptCode = jObject.get("SELECTED_DEPT_ID").toString();
						
						if (deptType.equals("SD")) {
							sqlCondition = " and (c.reporting_dept_code='" + selectedDeptCode + "' or c.dept_code='" + selectedDeptCode + "') ";
						} else if (deptType.equals("HOD")) {
							sqlCondition = " and c.dept_code='" + selectedDeptCode + "'";
						}

						if (!finCategoryType.equals("All")) {
							sql = "SELECT d.cino,date_of_filing,type_name_fil,fil_no,fil_year,reg_no,reg_year,bench_name,coram,dist_name,"
									+ " purpose_name,res_name,pet_name,pet_adv,res_adv, d.finance_category,d.work_name,d.est_cost,d.admin_sanction,d.grant_val,e.cfms_bill,e.bill_amount  "
									+ " FROM  ecourts_case_data a right join ecourts_case_category_wise_data d    on (a.cino=d.cino) inner join dept_new c on (a.dept_code=c.dept_code)"
									+ "  inner join (select cino, string_agg(cfms_bill_id,',') as cfms_bill, sum(COALESCE(NULLIF(trim(cfms_bill_amount),''), '0')::int4) as bill_amount"
									+ "  from cfms_bill_data_mst group by cino) e on (a.cino=e.cino)  where  d.finance_category='"
									+ finCategoryType + "' " + "  " + sqlCondition
									+ "   ORDER BY d.finance_category ";
						} else {
							sql = "SELECT d.cino,date_of_filing,type_name_fil,fil_no,fil_year,reg_no,reg_year,bench_name,coram,dist_name,"
									+ " purpose_name,res_name,pet_name,pet_adv,res_adv, d.finance_category,d.work_name,d.est_cost,d.admin_sanction,d.grant_val,e.cfms_bill,e.bill_amount  "
									+ " FROM  ecourts_case_data a right join ecourts_case_category_wise_data d    on (a.cino=d.cino) inner join dept_new c on (a.dept_code=c.dept_code)"
									+ "  inner join (select cino, string_agg(cfms_bill_id,',') as cfms_bill, sum(COALESCE(NULLIF(trim(cfms_bill_amount),''), '0')::int4) as bill_amount"
									+ "  from cfms_bill_data_mst group by cino) e on (a.cino=e.cino)  where  1=1 " + sqlCondition
									+ "    ORDER BY d.finance_category ";
						}
						System.out.println("ecourts SQL:" + sql);
						
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("CASE DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
								cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("FINANCE_CATEGORY", entry.get("finance_category"));
						    	cases.put("WORK_NAME", entry.get("work_name"));
						    	cases.put("EST_COST", entry.get("est_cost"));
						    	cases.put("ADMIN_SANCTION", entry.get("admin_sanction"));
						    	cases.put("GRANT_VALUE", entry.get("grant_val"));
						    	cases.put("CFMS_BILL_ID", entry.get("cfms_bill"));
						    	cases.put("CFMS_BILL_AMT", entry.get("bill_amount"));
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
						    	cases.put("CASE_TYPE", entry.get("type_name_fil"));
						    	cases.put("REG_NO", entry.get("reg_no"));
						    	cases.put("REG_YEAR", entry.get("reg_year"));
						    	cases.put("FILING_NO", entry.get("fil_no"));
						    	cases.put("FILING_YEAR", entry.get("fil_year"));
						    	cases.put("DATE_NEXT_LIST", entry.get("date_of_filing"));
						    	cases.put("BENCH", entry.get("bench_name"));
						    	cases.put("JUDGE_NAME", "Hon'ble Judge : "+entry.get("coram"));
						    	cases.put("PETITIONENR", entry.get("pet_name"));
						    	cases.put("DISTRICT", entry.get("dist_name"));
						    	cases.put("PURPOSE", entry.get("purpose_name"));
						    	cases.put("RESPONDENTS", entry.get("res_name"));
						    	cases.put("PETITIONER_ADVOCATE", entry.get("pet_adv"));
						    	cases.put("RESPONDENT_ADVOCATE", entry.get("res_adv"));
						    	
						    	finalList.put(cases);
							}
							
								casesData.put("CASES_LIST", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("CASES_LIST", finalList);									
							}
						
						
							String finalString = casesData.toString();
							
							if (isNewDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
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