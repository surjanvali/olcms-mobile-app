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

import plugins.DatabasePlugin;


@Path("/caseAssignmentStatus")
public class CaseAssignmentStatusReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/deptwise")
	public static Response getUpcomingCasesReport(String incomingData) throws Exception {
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
						
												
						if(roleId.equals("4")) {
							sql = "select d.sdeptcode||'01' as deptshortname,upper(d.description) as description,count(a.*) as total"
									+ ", sum(case when (substr(a.dept_code,4,2)!='01') then 1 else 0 end) as assigned_to_hod "
									+ ", sum(case when substr(a.dept_code,4,2)='01' and assigned=true then 1 else 0 end) as assigned_to_sect_sec "
									+ ", sum(case when substr(a.dept_code,4,2)!='01' and assigned=true then 1 else 0 end) as assigned_to_hod_sec  from ecourts_case_data a "
									+ " right join (select * from dept where sdeptcode||deptcode='"+dept_code+"') d on (substr(a.dept_code,1,3)=d.sdeptcode) "
									+ " group by substr(a.dept_code,1,3),d.sdeptcode,description order by 1";
						}
						else if(roleId.equals("7")) {
						
							sql = "select d.sdeptcode||'01' as deptshortname,upper(d.description) as description,count(a.*) as total"
								+ ", sum(case when (substr(a.dept_code,4,2)!='01') then 1 else 0 end) as assigned_to_hod "
								+ ", sum(case when substr(a.dept_code,4,2)='01' and assigned=true then 1 else 0 end) as assigned_to_sect_sec "
								+ ", sum(case when substr(a.dept_code,4,2)!='01' and assigned=true then 1 else 0 end) as assigned_to_hod_sec  from ecourts_case_data a "
								+ " right join (select * from dept where deptcode='01') d on (substr(a.dept_code,1,3)=d.sdeptcode) "
								+ " group by substr(a.dept_code,1,3),d.sdeptcode,description order by 1";
						}

						
						System.out.println("DEPT WISE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("DEPT WISE DATA=" + data);
						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptshortname"));						    	
						    	cases.put("DEPT_NAME", entry.get("description"));
						    	cases.put("TOTAL_CASES", entry.get("total"));
						    	cases.put("ASSIGNED_TO_HOD", entry.get("assigned_to_hod"));
						    	cases.put("ASSIGNED_TO_SECT_SECTION", entry.get("assigned_to_sect_sec"));
						    	cases.put("ASSIGNED_TO_HOD_SECTION", entry.get("assigned_to_hod_sec"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("DEPT_WISE_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("DEPT_WISE_DATA", finalList);	
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
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
										
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String caseStatus = jObject.get("CASE_STATUS").toString();
						
						if (caseStatus.equals("assigned2HOD")) {
							sqlCondition = " and substr(a.dept_code,4,2)!='01' ";
						} else if (caseStatus.equals("assigned2SectSec")) {
							sqlCondition = " and substr(a.dept_code,4,2)='01' and assigned=true ";
						} else if (caseStatus.equals("assigned2HodSec")) {
							sqlCondition = " and substr(a.dept_code,4,2)!='01' and assigned=true ";
						} 

						sql = "select a.*, b.orderpaths from ecourts_case_data a left join" + " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1"
								+ " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) where substr(a.dept_code,1,3)='" + dept_code.substring(0, 3) + "' "
								+ sqlCondition + " ";

						System.out.println("ecourts SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
						
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
						    	cases.put("CASE_TYPE", entry.get("type_name_fil"));	
						    	cases.put("REG_NO", entry.get("reg_no"));
						    	cases.put("REG_YEAR", entry.get("reg_year"));						    	
						    	cases.put("FILING_NO", entry.get("fil_no"));
						    	cases.put("FILING_YEAR", entry.get("fil_year"));
						    	cases.put("DATE_NEXT_LIST", entry.get("date_of_filing"));
						    	cases.put("BENCH_NAME", entry.get("bench_name"));
						    	cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DIST_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE_NAME", entry.get("purpose_name"));
						    	cases.put("RESPONDENT_NAME", entry.get("res_name"));
						    	cases.put("PET_ADV", entry.get("pet_adv"));
						    	cases.put("RES_ADV", entry.get("res_adv"));
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
							isDataAvailable = true;						
														
							} else {
								
								casesData.put("CASES_LIST", finalList);	
								
							}
						
							String finalString = casesData.toString();
							
							if (isDataAvailable)					    
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