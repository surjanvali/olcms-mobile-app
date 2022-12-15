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
						sqlCondition += " and order_date >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and order_date <= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9") || roleId.equals("10"))
						sqlCondition += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
								+ dept_code + "')";

					if (roleId.equals("2") || roleId.equals("10")) {
						sqlCondition += " and a.dist_id='" + dist_id + "' ";
					}

					sql=" select dm.district_id, dm.district_name, "
							+ " coalesce(d.casescount,'0') casescount, "
							+ " coalesce(d.order_implemented,'0') order_implemented, "
							+ " coalesce(d.appeal_filed,'0') appeal_filed, "
							+ " coalesce(d.dismissed_copy,'0') dismissed_copy,  "
							//+ " coalesce(d.closed,'0') closed,    "
							+ " coalesce(casescount-(order_implemented + appeal_filed+dismissed_copy),'0') as pending,   "
							+ " case when coalesce(d.casescount,'0') > 0 then round((((coalesce(order_implemented,'0')::int4 + coalesce(appeal_filed,'0')::int4 + coalesce(dismissed_copy,'0')::int4 ) * 100) / coalesce(d.casescount,'0')) , 2) else 0 end as actoin_taken_percent "
							//+ " round(coalesce( (order_implemented::numeric + appeal_filed::numeric+dismissed_copy::numeric+closed::numeric)/(4*100::numeric),'0'),2) as actoin_taken_percent   "
							+ " from district_mst dm "
							+ " left join ( select dist_id,count( a.cino) as casescount,   "
							+ " sum(case when length(action_taken_order)> 10   or final_order_status='final' then 1 else 0 end) as order_implemented ,  "
							+ " sum(case when length(appeal_filed_copy)> 10 or final_order_status='appeal'  then 1 else 0 end) as appeal_filed ,"
							+ " sum(case when length(dismissed_copy)> 10 or final_order_status='dismissed'  then 1 else 0 end) as dismissed_copy "  //or ocd.ecourts_case_status='dismissed'
						//	+ " sum(case when ocd.ecourts_case_status='Closed' then 1 else 0 end) as closed   "
							+ " from  ecourts_case_data a  "
							+ " inner join ecourts_case_finalorder b on (a.cino=b.cino)  LEFT join dept_new dn on (a.dept_code=dn.dept_code) "
							+ " LEFT join ecourts_olcms_case_details ocd on (a.cino=ocd.cino)   "
							+ " where 1=1  " +sqlCondition 
							+ " group by dist_id ) d on (dist_id=dm.district_id) order by casescount desc ";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("DIST_NAME", entry.get("district_name").toString());
							    	cases.put("DIST_ID", entry.get("district_id").toString());
							    	cases.put("ORDERS_COUNT", entry.get("casescount").toString());
							    	cases.put("ORDERS_IMPL", entry.get("order_implemented"));
							    	cases.put("APPEAL_FILED", entry.get("appeal_filed"));
							    	cases.put("DISMISSED", entry.get("dismissed_copy"));
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
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Final Order Impl report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/getCasesReport")
	public static Response getCasesReport(String incomingData) throws Exception {
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
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();	
					String caseStatus = jObject.get("CASE_STATUS").toString();	
					String sqlCondition = "";

						
					con = DatabasePlugin.connect();
					
					if (jObject.has("SELECTED_DIST_ID") && !jObject.get("SELECTED_DIST_ID").toString().equals("")) {
						sqlCondition += " and a.dist_id='" + jObject.get("SELECTED_DIST_ID").toString() + "'";
					}

					if (jObject.has("FROM_DATE") && !jObject.get("FROM_DATE").toString().equals("")) {
						sqlCondition += " and order_date >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("TO_DATE") && !jObject.get("TO_DATE").toString().equals("")) {
						sqlCondition += " and order_date <= to_date('" + jObject.get("TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					
					if(!caseStatus.equals("")) {
						if(caseStatus.equals("CLOSED")){
								sqlCondition+= " and coalesce(a.ecourts_case_status,'')='Closed' ";
							}
						
						if(caseStatus.equals("FINALORDER")) {
							sqlCondition+=" and (length(action_taken_order)> 10   or final_order_status='final') ";
						}
						if(caseStatus.equals("APPEALFILED")) {
							sqlCondition+=" and  (length(appeal_filed_copy)> 10 or final_order_status='appeal') ";
						}
						if(caseStatus.equals("DISMISSED")) {
							sqlCondition+=" and (length(dismissed_copy)> 10 or final_order_status='dismissed' or eocd.ecourts_case_status='dismissed') ";
						}
						if(caseStatus.equals("PENDING")) {
							sqlCondition+=" and   ( action_taken_order is null or appeal_filed_copy is null or dismissed_copy is null ) ";
						}
						
					}
				
					if (roleId.equals("2")) {
						sqlCondition += " and a.dist_id='" + dist_id + "'";
					} else if (roleId.equals("3") || roleId.equals("4")) {
						sqlCondition += " and (a.dept_code='" + dept_code + "' or d.reporting_dept_code='" + dept_code
								+ "') ";
					} else if (roleId.equals("5") || roleId.equals("9") || roleId.equals("10")) {
						sqlCondition += " and (a.dept_code='" + dept_code + "') ";
					}

					if (dist_id != 0) {
						sqlCondition += " and a.dist_id='" + dist_id + "'";
					}
					 
						
						
					sql = "select a.*, coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, case when (prayer is not null and coalesce(trim(prayer),'')!='' and length(prayer) > 2) then substr(prayer,1,250) else '-' end as prayer, prayer as prayer_full, ra.address from ecourts_case_data a  "
							+ " left join nic_prayer_data np on (a.cino=np.cino)"
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1)"
							+ "  left join ecourts_olcms_case_details eocd on (a.cino=eocd.cino)  "
							+ " inner join ( select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths "
							+ " from  (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null "
							+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0 and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) c group by cino ) b on (a.cino=b.cino)"
							+ " inner join dept_new d on (a.dept_code=d.dept_code) where d.display = true   "+sqlCondition+"    ";


					System.out.println("SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
												
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("CINO", entry.get("cino").toString());
							    	cases.put("SCANNED_AFFIDAVIT", entry.get("scanned_document_path"));
							    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
							    	cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
							    	cases.put("PRAYER", entry.get("prayer").toString());
							    	cases.put("FILING_NO", entry.get("fil_no").toString());
							    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
							    	cases.put("DATE_OF_NEXT_LISTING", entry.get("date_next_list").toString());
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
								
								casesData.put("FINAL_ORDERS_IMPL_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("FINAL_ORDERS_IMPL_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Final Order Impl report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"New cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy cases report retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
