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


@Path("/nextHearing")
public class UpcomingHearingCasesReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getUpcomingCasesReport")
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
						
												
						if (roleId.equals("5") || roleId.equals("9")) {

							return getHODDeptWiseNewCases(incomingData);
						}
						
						sql="select a1.reporting_dept_code as deptcode,dn1.description,sum(total) as  total,sum(today) as today, sum(tomorrow) as tomorrow,sum(week1) as week1, "
								+ " sum(week2) as week2, sum(week3) as week3,sum(week4) as week4 "
								+ " from ( "
								+ " select case when reporting_dept_code='CAB01' then a.dept_code else reporting_dept_code end as reporting_dept_code,a.dept_code,count(*) as total"
								+ ",sum(case when date_next_list = current_date then 1 else 0 end) as today,"
								+ "sum(case when date_next_list = current_date+1 then 1 else 0 end) as tomorrow,"
								+ "sum(case when date_next_list > current_date and date_next_list <= current_date+7  then 1 else 0 end) as week1,  "
								+ "sum(case when date_next_list > current_date+7 and date_next_list <= current_date+14  then 1 else 0 end) as week2,  "
								+ "sum(case when date_next_list > current_date+14 and date_next_list <= current_date+21  then 1 else 0 end) as week3,  "
								+ "sum(case when date_next_list > current_date+21 and date_next_list <= current_date+28  then 1 else 0 end) as week4"
								+ " from ecourts_case_data a "
								+ " inner join dept_new dn on (a.dept_code=dn.dept_code) ";
								

								if(roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
									sql+=" and (dn.reporting_dept_code='"+dept_code+"' or dn.dept_code='"+dept_code+"')";
								else if(roleId.equals("2")){
									sql+=" and a.dist_id='"+dist_id+"'";
								}
								
								
								sql+= " group by reporting_dept_code,a.dept_code) a1"
								
								+ " inner join dept_new dn1 on (a1.reporting_dept_code=dn1.dept_code) "
								+ " group by a1.reporting_dept_code,dn1.description"
								+ " order by 1";

						
						System.out.println("SECT DEPT WISE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("SECT DEPT WISE DATA=" + data);
						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode"));						    	
						    	cases.put("DEPT_NAME", entry.get("description"));
						    	cases.put("TOTAL_CASES", entry.get("total"));
						    	cases.put("TODAY", entry.get("today"));
						    	cases.put("TOMORROW", entry.get("tomorrow"));
						    	cases.put("THIS_WEEK", entry.get("week1"));
						    	cases.put("WITHIN_7_TO_14_DAYS", entry.get("week2"));
						    	cases.put("WITHIN_14_TO_21_DAYS", entry.get("week3"));
						    	cases.put("WITHIN_21_TO_28_DAYS", entry.get("week4"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("SECT_DEPT_WISE_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("SECT_DEPT_WISE_DATA", finalList);	
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
	@Path("/getHODDeptWiseReport")
	public static Response getHODDeptWiseNewCases(String incomingData) throws Exception {
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
						
						sql = "select a.dept_code as deptcode,dn.description,"
								+ " count(*) as total"
								+ ",sum(case when date_next_list = current_date then 1 else 0 end) as today,"
								+ "sum(case when date_next_list = current_date+1 then 1 else 0 end) as tomorrow,"
								+ "sum(case when date_next_list > current_date and date_next_list <= current_date+7  then 1 else 0 end) as week1,  "
								+ "sum(case when date_next_list > current_date+7 and date_next_list <= current_date+14  then 1 else 0 end) as week2,  "
								+ "sum(case when date_next_list > current_date+14 and date_next_list <= current_date+21  then 1 else 0 end) as week3,  "
								+ "sum(case when date_next_list > current_date+21 and date_next_list <= current_date+28  then 1 else 0 end) as week4"
								+ " from ecourts_case_data a "


					+ " inner join dept_new dn on (a.dept_code=dn.dept_code) "
					+ " where dn.display = true and (dn.reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code
					+ "') ";
			
					if(roleId.equals("2")){
						sql+=" and a.dist_id='"+dist_id+"'";
					}
					
					
					// + "where dn.reporting_dept_code='AGC01' or a.dept_code='AGC01' "
					sql+= "group by a.dept_code,dn.description order by 1";
						
						
						System.out.println("HOD DEPT WISE  DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("HOD DEPT WISE  DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
								cases.put("DEPT_CODE", entry.get("deptcode"));						    	
						    	cases.put("DEPT_NAME", entry.get("description"));
						    	cases.put("TOTAL_CASES", entry.get("total"));
						    	cases.put("TODAY", entry.get("today"));
						    	cases.put("TOMORROW", entry.get("tomorrow"));
						    	cases.put("THIS_WEEK", entry.get("week1"));
						    	cases.put("WITHIN_7_TO_14_DAYS", entry.get("week2"));
						    	cases.put("WITHIN_14_TO_21_DAYS", entry.get("week3"));
						    	cases.put("WITHIN_21_TO_28_DAYS", entry.get("week4"));
						    	
						    	finalList.put(cases);
							}
							
								casesData.put("HOD_DEPT_WISE_DATA", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("HOD_DEPT_WISE_DATA", finalList);									
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
						
						if(!caseStatus.equals("")) {
							if(caseStatus.equals("today")){
									sqlCondition= " and date_next_list = current_date ";
								}
							if(caseStatus.equals("tomorrow")) {
								sqlCondition=" and date_next_list = current_date+1 ";
							}
							if(caseStatus.equals("week1")) {
								sqlCondition=" and date_next_list > current_date and date_next_list <= current_date+7  ";
							}
							if(caseStatus.equals("week2")) {
								sqlCondition= " and date_next_list > current_date+7 and date_next_list <= current_date+14 ";
							}
							if(caseStatus.equals("week3")) {
								sqlCondition=" and date_next_list > current_date+14 and date_next_list <= current_date+21 ";
							}
							if(caseStatus.equals("week4")) {
								sqlCondition=" and date_next_list > current_date+21 and date_next_list <= current_date+28 ";
							}
						}
						
						sql = "select a.*, "
								+ "";
								//+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a "
							sql += " nda.fullname_en as fullname, nda.designation_name_en as designation, nda.post_name_en as post_name, nda.email, nda.mobile1 as mobile,dim.district_name , ";
							sql += " 'Pending at '||ecs.status_description||'' as current_status, coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths,"
								+ " case when (prayer is not null and coalesce(trim(prayer),'')!='' and length(prayer) > 2) then substr(prayer,1,250) else '-' end as prayer, prayer as prayer_full, ra.address from ecourts_case_data a "
								
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
								+ " left join district_mst dim on (a.dist_id=dim.district_id) "
								+ " inner join ecourts_mst_case_status ecs on (a.case_status=ecs.status_id) "
								+ " left join nic_data_all nda on (a.dept_code=substr(nda.global_org_name,1,5) and a.assigned_to=nda.email and nda.is_primary='t' and coalesce(a.dist_id,'0')=coalesce(nda.dist_id,'0')) "
								
								+ " left join"
								+ " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) where d.display = true ";
						
						
						if(roleId.equals("2")){
							sql+=" and a.dist_id='"+dist_id+"'";
						}
						
						sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') " + sqlCondition;
						
						sql +=" order by date_next_list asc";
						
						 
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
						    	
						    	String scannedAffidavitPath="";

								if (entry.get("scanned_document_path1") != null)
								{
									if (entry.get("scanned_document_path1")!=null && !entry.get("scanned_document_path1").equals("-")) {
										scannedAffidavitPath = "https://apolcms.ap.gov.in/" + entry.get("scanned_document_path");
									}
									
								}
								cases.put("SCANNED_AFFIDAVIT_PATH", scannedAffidavitPath);
								String name = "";
								
								if(entry.get("fullname") != null && !entry.get("fullname").toString().equals(" ")) {
									name = name +" "+ entry.get("fullname").toString();
								}
								if(entry.get("designation") != null && !entry.get("designation").toString().equals(" ")) {
									name = name +" "+entry.get("designation").toString();
								}
								if(entry.get("mobile") != null && !entry.get("mobile").toString().equals(" ")) {
									name = name +" "+entry.get("mobile").toString();
								}
								if(entry.get("email") != null && !entry.get("email").toString().equals(" ")) {
									name = name +" "+entry.get("email").toString();
								}
								if(entry.get("district_name") != null && !entry.get("district_name").toString().equals(" ")) {
									name = name +" "+entry.get("district_name").toString();
								}
						    	cases.put("CURRENT_STATUS", entry.get("current_status")+name);				    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
						    	cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));						    	
						    	cases.put("PRAYER", entry.get("prayer_full"));						    	
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_NEXT_LIST", entry.get("date_next_list"));
						    	cases.put("BENCH_NAME", entry.get("bench_name"));
						    	cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DIST_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE_NAME", entry.get("purpose_name"));
						    	cases.put("RESPONDENT_NAME", entry.get("res_name")+","+entry.get("address"));
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