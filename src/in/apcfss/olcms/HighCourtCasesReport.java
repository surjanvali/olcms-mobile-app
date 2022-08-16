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


@Path("/casesReport")
public class HighCourtCasesReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getDeptWise")
	public static Response getDeptWise(String incomingData) throws Exception {
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
						
						sql="select d.dept_code as deptshortname ,upper(trim(d.description)) as description,count(*) total, sum(case when assigned is true then 1 else 0 end) as assigned "
								+ " from ecourts_case_data a inner join dept_new d on (a.dept_code=d.dept_code) where (d.dept_code='"+dept_code+"' or d.reporting_dept_code='"+dept_code+"') "
								+ " group by d.dept_code,upper(trim(d.description)) order by d.dept_code";
						
						System.out.println("SHOW DEPT WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("DEPT_CODE", entry.get("deptshortname").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total"));
						    	cases.put("ASSIGNED_CASES", entry.get("assigned"));
								
								
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
					else if(!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
					}
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
						
						sql = "select a.*, b.orderpaths from ecourts_case_data a left join" + " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1"
								+ " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code = d.dept_code) where d.dept_code='"
								+ selectedDeptCode + "' ";
						 
						System.out.println("ecourts SQL:" + sql);
						
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
								cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing").toString());
						    	cases.put("CASE_TYPE", entry.get("type_name_fil"));
						    	cases.put("REG_NO", entry.get("reg_no").toString());
						    	cases.put("REG_YEAR", entry.get("reg_year").toString());
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DIST_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE_NAME", entry.get("purpose_name"));
						    	cases.put("RESPONDENT_NAME", entry.get("res_name"));
						    	
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