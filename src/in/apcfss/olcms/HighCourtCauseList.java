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

@Path("/causeList")
public class HighCourtCauseList {
			
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/showPopUp")
	public static Response usersCauseList(String incomingData) throws Exception {

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
					
					
					sql = "select ecc.causelist_date::date as causelist_date ,a.*, "
							+ ""
							+ "  ra.address "
							+ " from ecourts_causelist_cases ecc "
		                    + " inner join ecourts_case_data a on (ecc.case_no=a.type_name_reg||'/'||a.reg_no||'/'||a.reg_year) "
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where d.display = true and ecc.causelist_date::date=current_date  ";
		            
		            if(roleId.equals("2") || roleId.equals("10")) {
		            	sql+=" and a.dist_id='"+distId+"'";
		            }
		            
		            if(!roleId.equals("2")) {
		            	sql+=" and a.dept_code='"+deptCode+"'";
		            }
		            
		            sql+=" order by ecc.causelist_date::date desc ";
		            System.out.println("SQL:" + sql);
		            List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

		           
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CAUSELIST_DATE", entry.get("causelist_date") != null ? entry.get("causelist_date").toString():"");
							cases.put("CINO", entry.get("cino")!=null ? entry.get("cino").toString():"");
							cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
							cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
					    	cases.put("PETITIONER_NAME", entry.get("pet_name"));					    	
					    	cases.put("RESPONDENTS", entry.get("res_name")+","+entry.get("address"));
					    	cases.put("PETITIONER_ADVOCATE", entry.get("pet_adv"));
					    	cases.put("RESPONDENT_ADVOCATE", entry.get("res_adv"));
					    	
					    	finalList.put(cases);
						}

						casesData.put("CAUSE_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("CAUSE_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cause List retrived successfully\"  , "
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
	

	
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/dashboardCauseList")
	public static Response dashboardCauseList(String incomingData) throws Exception {

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
					
					
					sql = "select ecc.causelist_date::date as causelist_date ,a.*, "
							+ ""
							+ " coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address "
							+ " from ecourts_causelist_cases ecc "
		                    + " inner join ecourts_case_data a on (ecc.case_no=a.type_name_reg||'/'||a.reg_no||'/'||a.reg_year) "
							+ " left join nic_prayer_data np on (a.cino=np.cino)"
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
							+ " left join"
							
							+ " ("
							+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
							+ " from "
							+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
							+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
							+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
							
							+ " on (a.cino=b.cino) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where d.display = true and ecc.causelist_date::date=current_date  ";
		            
		            if(roleId.equals("2") || roleId.equals("10")) {
		            	sql+=" and a.dist_id='"+distId+"'";
		            }
		            
		            if(!roleId.equals("2") && !roleId.equals("1") && !roleId.equals("7")) {
		            	sql+=" and a.dept_code='"+deptCode+"'";
		            }
		            
		            sql+=" order by ecc.causelist_date::date desc ";
		            System.out.println("SQL:" + sql);
		            List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

		           
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CAUSELIST_DATE", entry.get("causelist_date") != null ? entry.get("causelist_date").toString():"");
							cases.put("SCANNED_AFFIDAVIT", entry.get("scanned_document_path")!=null && !entry.get("scanned_document_path").toString().trim().equals("") ? "https://apolcms.ap.gov.in/"+entry.get("scanned_document_path").toString():"");
							cases.put("CINO", entry.get("cino")!=null ? entry.get("cino").toString():"");
							cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
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
					    	
					    	JSONArray orderdocumentList = new JSONArray();
							
					    	
					    	if (entry.get("orderpaths") != null)
					    	{
					    		String mydata = entry.get("orderpaths").toString();
					    		Pattern pattern = Pattern.compile("(?<=a href=\".)(.*?)(?=\" target)");
					    		Matcher matcher = pattern.matcher(mydata);
					    		Pattern pattern2 = Pattern.compile("(?<=<span>)(.*?)(?=</span)");
					    		Matcher matcher2 = pattern2.matcher(mydata);
					    		
					            while (matcher.find() && matcher2.find()){
					            	JSONObject orderData = new JSONObject();
					                String s = matcher.group();
					                String s1 = matcher2.group();
					                orderData.put("ORDER_NAME", s1);
					                orderData.put("ORDER_DOC_PATH", "https://apolcms.ap.gov.in/"+s);
					                
					                orderdocumentList.put(orderData);
					            }
					           
					           
					    	}
					    	cases.put("ORDER_PATHS", orderdocumentList);
					    	finalList.put(cases);
						}

						casesData.put("CAUSE_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("CAUSE_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cause List retrived successfully\"  , "
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
	
	
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/showCauseListDateWise")
	public static Response showCauseListByDate(String incomingData) throws Exception {

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
				}  else if (!jObject.has("CAUSE_LIST_DATE") || jObject.get("CAUSE_LIST_DATE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CAUSE_LIST_DATE is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="", causeListDate="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					causeListDate = jObject.get("CAUSE_LIST_DATE").toString();	
					
					
					
					con = DatabasePlugin.connect();
					
					
					sql="select distinct a.est_code , a. causelist_date , a.bench_id , a. causelist_id , cause_list_type ,coalesce(causelist_document,'') as document, b.judge_name "
							+ "from ecourts_causelist_bench_data a  left join  ecourts_causelist_data b on (a.bench_id=b.bench_id) where a.causelist_date=to_date('"
							+ causeListDate + "','mm/dd/yyyy') and coalesce(causelist_document,'') not like '%status_code%'";
					

					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					
		           
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CAUSELIST_DATE", entry.get("causelist_date") != null ? entry.get("causelist_date").toString():"");
							cases.put("BENCH_ID", entry.get("bench_id"));
							cases.put("JUDGE_NAME", entry.get("judge_name").toString());
					    	cases.put("CAUSE_LIST_ID", entry.get("causelist_id").toString());
					    	cases.put("CAUSE_LIST_TYPE", entry.get("causelist_type").toString());
					    	cases.put("ORDER_DOC_PATH", "https://apolcms.ap.gov.in/"+entry.get("document").toString());
					    	
					    	finalList.put(cases);
						}

						casesData.put("CAUSE_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("CAUSE_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cause List retrived successfully\"  , "
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
	
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/showCauseListDeptWise")
	public static Response showCauseListDeptWise(String incomingData) throws Exception {

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
				}  else if (!jObject.has("CAUSE_LIST_DATE") || jObject.get("CAUSE_LIST_DATE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CAUSE_LIST_DATE is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="", causeListDate="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					causeListDate = jObject.get("CAUSE_LIST_DATE").toString();	
					
					
					
					con = DatabasePlugin.connect();
					
					
					sql=" select to_char(ecc.causelist_date::date,'dd-mm-yyyy') as causelist_date, ecd.dept_code,upper(d.description) as description, count(*) as casescount from ecourts_causelist_cases ecc "
		            		+ "inner join ecourts_case_data ecd on (ecc.case_no=ecd.type_name_reg||'/'||ecd.reg_no||'/'||ecd.reg_year) "
		            		//+ "left join nic_resp_addr_data ra on (ecd.cino=ra.cino and party_no=1) "
		            		+ "left join dept_new d on (ecd.dept_code=d.dept_code) "
		            		+ "where ecc.causelist_date::date = to_date('"+causeListDate+"','mm-dd-yyyy') group by ecd.dept_code,d.description, ecc.causelist_date order by casescount desc ";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					
		           
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CAUSELIST_DATE", entry.get("causelist_date") != null ? entry.get("causelist_date").toString():"");
							cases.put("DEPT_CODE", entry.get("dept_code"));
							cases.put("DEPT_NAME", entry.get("description").toString());
					    	cases.put("CASES_COUNT", entry.get("casescount").toString());
					    	
					    	
					    	finalList.put(cases);
						}

						casesData.put("CAUSE_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("CAUSE_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cause List retrived successfully\"  , "
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
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/showCauseListReportForDept")
	public static Response showCauseListReportForDept(String incomingData) throws Exception {

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
				}  else if (!jObject.has("CAUSE_LIST_DATE") || jObject.get("CAUSE_LIST_DATE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CAUSE_LIST_DATE is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="", causeListDate="";
					userid = jObject.get("USER_ID").toString();
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					causeListDate = jObject.get("CAUSE_LIST_DATE").toString();	
					
					
					
					con = DatabasePlugin.connect();
					
					sql = "select ecc.causelist_date::date as causelist_date ,a.*, "
							+ ""
							+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address "
							+ " from ecourts_causelist_cases ecc "
		                    + " inner join ecourts_case_data a on (ecc.case_no=a.type_name_reg||'/'||a.reg_no||'/'||a.reg_year) "
							+ " left join nic_prayer_data np on (a.cino=np.cino)"
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
							+ " left join"
							+ " ("
							+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
							+ " from "
							+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
							+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
							+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
							+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
							+ " on (a.cino=b.cino) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code)  "
							+ " where d.display = true and ecc.causelist_date::date = to_date('" + causeListDate + "','mm-dd-yyyy') ";
		            
		            if(roleId.equals("2") || roleId.equals("10")) {
		            	sql+=" and a.dist_id='"+distId+"'";
		            }
		            
		            if(!roleId.equals("2")) {
		            	sql+=" and a.dept_code='"+deptCode+"'";
		            }
					
					
		            System.out.println("SQL:" + sql);
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					
		           
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CAUSELIST_DATE", entry.get("causelist_date") != null ? entry.get("causelist_date").toString():"");
							cases.put("SCANNED_AFFIDAVIT", entry.get("scanned_document_path")!=null && !entry.get("scanned_document_path").toString().trim().equals("") ? "https://apolcms.ap.gov.in/"+entry.get("scanned_document_path").toString():"");
							cases.put("CINO", entry.get("cino")!=null ? entry.get("cino").toString():"");
							cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
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
					    	
					    	JSONArray orderdocumentList = new JSONArray();
							
					    	
					    	if (entry.get("orderpaths") != null)
					    	{
					    		String mydata = entry.get("orderpaths").toString();
					    		Pattern pattern = Pattern.compile("(?<=a href=\".)(.*?)(?=\" target)");
					    		Matcher matcher = pattern.matcher(mydata);
					    		Pattern pattern2 = Pattern.compile("(?<=<span>)(.*?)(?=</span)");
					    		Matcher matcher2 = pattern2.matcher(mydata);
					    		
					            while (matcher.find() && matcher2.find()){
					            	JSONObject orderData = new JSONObject();
					                String s = matcher.group();
					                String s1 = matcher2.group();
					                orderData.put("ORDER_NAME", s1);
					                orderData.put("ORDER_DOC_PATH", "https://apolcms.ap.gov.in/"+s);
					                
					                orderdocumentList.put(orderData);
					            }
					           
					           
					    	}
					    	cases.put("ORDER_PATHS", orderdocumentList);
					    	finalList.put(cases);
						}

						casesData.put("CAUSE_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("CAUSE_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cause List retrived successfully\"  , "
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
	