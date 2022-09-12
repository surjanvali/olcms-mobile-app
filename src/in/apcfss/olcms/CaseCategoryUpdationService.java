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

import in.apcfss.struts.commons.CommonModels;
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

@Path("/caseCategoryUpdationService")
public class CaseCategoryUpdationService {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/secDeptWise")
	public static Response secDeptWise(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="",sqlCondition = "";
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
					
					
					if (jObject.has("DOF_FROM_DATE") &&  jObject.get("DOF_FROM_DATE")!= null && !jObject.get("DOF_FROM_DATE").toString().equals("")) {
						sqlCondition += " and date_of_filing >= to_date('" + jObject.get("DOF_FROM_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("DOF_TO_DATE") &&  jObject.get("DOF_TO_DATE")!= null && !jObject.get("DOF_TO_DATE").toString().equals("")) {
						sqlCondition += " and date_of_filing <= to_date('" + jObject.get("DOF_TO_DATE").toString() + "','dd-mm-yyyy') ";
					}
					if (jObject.has("PURPOSE") &&  jObject.get("PURPOSE")!= null && !jObject.get("PURPOSE").toString().equals("")) {
						sqlCondition += " and trim(purpose_name)='" + jObject.get("PURPOSE").toString() + "' ";
					}
					if (jObject.has("DISTRICT_NAME") && jObject.get("DISTRICT_NAME") != null
							&& !jObject.get("DISTRICT_NAME").toString().equals("")
							&& !jObject.get("DISTRICT_NAME").toString().equals("0")) {
						sqlCondition += " and trim(dist_name)='" + jObject.get("DISTRICT_NAME").toString().trim()
								+ "' ";
					}


					if(jObject.has("REG_YEAR") && jObject.get("REG_YEAR") != null
							&& !jObject.get("REG_YEAR").toString().equals("")
							&& jObject.get("REG_YEAR").toString().equals("default")) {
						sqlCondition += " and reg_year in ('2021','2022') ";
						
					}
					else if (jObject.has("REG_YEAR") && jObject.get("REG_YEAR") != null
							&& !jObject.get("REG_YEAR").toString().equals("")
							&& !jObject.get("REG_YEAR").toString().equals("ALL")
							&& !jObject.get("REG_YEAR").toString().equals("0")) {
						sqlCondition += " and reg_year='" + jObject.get("REG_YEAR").toString() + "' ";
					}
					
					
					if(!roleId.equals("2")) { //District Nodal Officer
						sqlCondition +=" and dept_code='" + dept_code + "' ";
					}

					if(roleId.equals("2")) { //District Collector

						sqlCondition +="  and dist_id='"+dist_id+"'";//and case_status=7
					}
					else if(roleId.equals("10")) { //District Nodal Officer
						sqlCondition +=" and dist_id='"+dist_id+"'";// and case_status=8
					}
					else if(roleId.equals("5") || roleId.equals("9")) {//NO & HOD
						//sqlCondition +=" and case_status in (3,4)";
					}
					else if(roleId.equals("3") || roleId.equals("4")) {//MLO & Sect. Dept.
						//sqlCondition +=" and (case_status is null or case_status in (1, 2))";
					}

					
					sql= " select a.*, b.finance_category from ecourts_case_data a left join ecourts_case_category_wise_data b on (a.cino=b.cino) "
							+ " where coalesce(assigned,'f')='f'  "+sqlCondition+" and coalesce(ecourts_case_status,'')!='Closed' order by finance_category";

					System.out.println("ecourts SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					// System.out.println("data=" + data);
					
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;
					
					if (data != null && !data.isEmpty() && data.size() > 0) {
						
						for (Map<String, Object> entry : data) {								   
							JSONObject cases = new JSONObject();
					    	cases.put("CINO", entry.get("cino").toString());						    	
					    	cases.put("DATE_OF_FILING", entry.get("date_of_filing").toString());
					    	cases.put("CASE_TYPE", entry.get("type_name_fil"));
					    	cases.put("REG_NO", entry.get("reg_no"));
					    	cases.put("REG_YEAR", entry.get("reg_year"));
					    	cases.put("PET_NAME", entry.get("pet_name").toString());
					    	cases.put("DIST_NAME", entry.get("dist_name").toString());
					    	cases.put("PURPOSE_NAME", entry.get("purpose_name").toString());
					    	cases.put("RES_NAME", entry.get("res_name").toString());
					    	cases.put("PET_ADV", entry.get("pet_adv").toString());
					    	cases.put("RES-ADV", entry.get("res_adv").toString());
					    	cases.put("FIN_CATEGORY", entry.get("finance_category").toString());
					    	
					    	finalList.put(cases);
						}
						
						casesData.put("CASES_LIST", finalList);
						isDataAvailable = true;						
													
						} else {
							
						casesData.put("CASES_LIST", finalList);	
							
						}
					
						String finalString = casesData.toString();
						
						if (isDataAvailable)					    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases List retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/hodDeptWise")
	public static Response hodDeptWise(String incomingData) throws Exception {
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
					else if(!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
					}
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();		
					String selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();	

						
					con = DatabasePlugin.connect();
					
					
						
						
					sql = "select a.dept_code as deptcode,dn.description,count(*) as total_cases,sum(case when scanned_document_path is not null and length(scanned_document_path)>10 then 1 else 0 end) as olcms_uploads, "
							+ "sum(case when petition_document is not null and length(petition_document)>10  then 1 else 0 end) as petition_uploaded  "
							+ ", sum(case when a.ecourts_case_status='Closed' then 1 else 0 end) as closed_cases "
							+ ",sum(case when a.ecourts_case_status='Pending' and counter_filed_document is not null  and length(counter_filed_document)>10 then 1 else 0 end) as counter_uploaded ,"
							+ " sum(case when a.ecourts_case_status='Pending' and pwr_uploaded_copy is not null  and length(pwr_uploaded_copy)>10 then 1 else 0 end) as pwrcounter_uploaded  ,"
							+ " sum(case when counter_approved_gp='Yes' then 1 else 0 end) as counter_approved_gp from ecourts_case_data a "
							+ " left join apolcms.ecourts_olcms_case_details b using (cino) "
							+ " inner join dept_new dn on (a.dept_code=dn.dept_code) "
							+ " where dn.display = true and (dn.reporting_dept_code='" + selectedDeptCode + "' or a.dept_code='" + selectedDeptCode
							+ "') ";
					
							if(roleId.equals("2")){
								sql+=" and a.dist_id='"+dist_id+"'";
							}
							
							
							// + "where dn.reporting_dept_code='AGC01' or a.dept_code='AGC01' "
							sql+= "group by a.dept_code,dn.description order by 1";
						
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;
							
							if (data != null && !data.isEmpty() && data.size() > 0) {
								
								for (Map<String, Object> entry : data) {								   
									JSONObject cases = new JSONObject();
							    	cases.put("HOD_DEPT_CODE", entry.get("deptcode").toString());						    	
							    	cases.put("HOD_DEPT_NAME", entry.get("description").toString());
							    	cases.put("TOTAL_CASES", entry.get("total_cases"));
							    	cases.put("OLCMS_UPLOADS", entry.get("olcms_uploads"));
							    	cases.put("PETITION_UPLOADED", entry.get("petition_uploaded"));
							    	cases.put("CLOSED", entry.get("closed_cases").toString());
							    	cases.put("COUNTER_FILED", entry.get("counter_uploaded").toString());
							    	cases.put("PWR_UPLOADED", entry.get("pwrcounter_uploaded").toString());
							    	cases.put("PWR_APPROVED", entry.get("counter_approved_gp").toString());
							    	
							    	finalList.put(cases);
								}
								
								casesData.put("HOD_DEPT_WISE_LIST", finalList);
								isDataAvailable = true;						
															
								} else {
									
									casesData.put("HOD_DEPT_WISE_LIST", finalList);	
									
								}
							
								String finalString = casesData.toString();
								
								if (isDataAvailable)					    
									jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"HOD Dept wise cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
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
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {
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
					else if(!jObject.has("SELECTED_DEPT_CODE") || jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}					
					else {

					String roleId=jObject.get("ROLE_ID").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();	
					String selectedDeptCode = jObject.get("SELECTED_DEPT_CODE").toString();
					String caseStatus = jObject.get("CASE_STATUS").toString();
					String sqlCondition="",condition="";

						
					con = DatabasePlugin.connect();
					
					
					
					if(!caseStatus.equals("")) {
						if(caseStatus.equals("CLOSED")){
								sqlCondition= " and coalesce(a.ecourts_case_status,'')='Closed' ";
							}
						if(caseStatus.equals("PET")) {
							sqlCondition=" and petition_document is not null";
						}
						if(caseStatus.equals("COUNTERUPLOADED")) {
							sqlCondition=" and counter_filed_document is not null  ";
						}
						if(caseStatus.equals("PWRUPLOADED")) {
							sqlCondition= " and pwr_uploaded_copy is not null ";
						}
						if(caseStatus.equals("GPCOUNTER")) {
							sqlCondition=" and counter_approved_gp='Yes' ";
						}
						if(caseStatus.equals("SCANNEDDOC")) {
							sqlCondition=" and scanned_document_path is not null and length(scanned_document_path)>10 ";
						}
					}
					
					
					
					sql = "select a.*, "
							+ ""
							// + "n.global_org_name as globalorgname, n.fullname_en as fullname, n.designation_name_en as designation, n.mobile1 as mobile, n.email as email, "
							+ ""
							+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a "
							+ " left join nic_prayer_data np on (a.cino=np.cino)"
							+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
							//+ "inner join nic_data n on (a.assigned_to=n.email) "
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
					
					sql += " and (reporting_dept_code='" + selectedDeptCode + "' or a.dept_code='" + selectedDeptCode + "') " + sqlCondition;
					
					
							System.out.println("SQL:" + sql);	
						
							List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);						
							
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							boolean isDataAvailable = false;

							if (data != null && !data.isEmpty() && data.size() > 0) {

								for (Map<String, Object> entry : data) {
									JSONObject cases = new JSONObject();
									cases.put("CINO", entry.get("cino").toString());
									cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
									cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
							    	cases.put("FILING_NO", entry.get("fil_no").toString());
							    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
							    	cases.put("DATE_OF_NEXT_LISTING", entry.get("date_next_list").toString());
							    	cases.put("BENCH", entry.get("bench_name").toString());
							    	cases.put("JUDGE_NAME", "Hon'ble Judge " +entry.get("coram"));
							    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
							    	cases.put("DISTRICT_NAME", entry.get("dist_name"));
							    	cases.put("PURPOSE", entry.get("purpose_name"));
							    	cases.put("RESPONDENTS", entry.get("res_name")+", "+ entry.get("address"));
							    	cases.put("PETITIONER_ADVOCATE", entry.get("pet_adv"));
							    	cases.put("RESPONDENT_ADVOCATE", entry.get("res_adv"));
							    	cases.put("PRAYER", entry.get("prayer"));
							    	
							    	String scannedAffidavitPath="";

									if (entry.get("scanned_document_path1") != null)
									{
										if (entry.get("scanned_document_path1")!=null && !entry.get("scanned_document_path1").equals("-")) {
											scannedAffidavitPath = "https://apolcms.ap.gov.in/" + entry.get("scanned_document_path");
										}
										
									}
									
									cases.put("SCANNED_AFFIDAVIT_PATH", scannedAffidavitPath);
							    	
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
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
										+ finalString.substring(1, finalString.length() - 1) + "}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
										+ finalString.substring(1, finalString.length() - 1) + " }}";

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
