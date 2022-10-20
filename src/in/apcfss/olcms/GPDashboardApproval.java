package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.FormDataBodyPart;

import in.apcfss.struts.commons.CommonModels;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import plugins.DatabasePlugin;


@Path("/gpApproval")
public class GPDashboardApproval {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/legacyCasesInstructions")
	public static Response displayCaseDetailsForLegacy(String incomingData) throws Exception {
		
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
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", condition = "", caseType = "", caseRegYear = "", caseNo = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					
					con = DatabasePlugin.connect();
					
					if(roleId!=null && roleId.equals("6")) { // GPO
						
						condition=" and a.case_status=6 and e.gp_id='"+userId+"' ";						
						
					}
					/*
					 * sql =
					 * "select type_name_reg, reg_no, reg_year, to_char(dt_regis,'dd-mm-yyyy') as dt_regis, a.cino, case when length(scanned_document_path) > 10 then scanned_document_path else '-' end as scanned_document_path "
					 * + " from ecourts_case_data a " +
					 * " left join ecourts_olcms_case_details od on (a.cino=od.cino)" +
					 * " left join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) " +
					 * " inner join dept_new d on (a.dept_code=d.dept_code) " +
					 * " where assigned=true "+condition +
					 * " and coalesce(a.ecourts_case_status,'')!='Closed' "+condition;
					 * 
					 * 
					 * sql += "order by reg_year,type_name_reg,reg_no";
					 * request.setAttribute("HEADING", heading);
					 */
					
					 sql="select type_name_reg, reg_no,reg_year, to_char(dt_regis,'dd-mm-yyyy') as dt_regis, a.cino, "
					 		+ " case when length(scanned_document_path) > 10 then scanned_document_path else '-' end as scanned_document_path,legacy_ack_flag "
					 		+ " from (select distinct cino,legacy_ack_flag from ecourts_dept_instructions where legacy_ack_flag='Legacy') a inner join ecourts_case_data d on (a.cino=d.cino)"
					 		+ " where d.dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='"+userId+"')";
					 
					 
					System.out.println("SQL:" + sql);						

						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CASE_TYPE", entry.get("type_name_reg").toString());
					    	String caseno = entry.get("type_name_reg").toString()+" "+entry.get("reg_no").toString()+"/"+entry.get("reg_year").toString();
					    	cases.put("CASE_NO", caseno);						    	
					    	cases.put("CASE_REG_DATE", entry.get("dt_regis"));
					    	cases.put("STATUS", "Pending");
					    	cases.put("CINO", entry.get("cino"));
							
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case details retrived successfully\"  , "
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
	@Path("/displayCaseDetailsForLegacy")
	public static Response displayCaseDetails(String incomingData) throws Exception {
		
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
				} else if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CINO is missing in the request.\" }}";
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", Condition = "", ackNo = "", cino = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					cino = jObject.get("CINO").toString();
					
											
					sql = "select a.*, "
							+ " nda.fullname_en as fullname,'Legacy' as legacy_ack_flag , nda.designation_name_en as designation, nda.post_name_en as post_name, nda.email, nda.mobile1 as mobile,dim.district_name , "
							+ " 'Pending at '||ecs.status_description||'' as current_status, coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths,"
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
							+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) where d.display = true and a.cino='"+cino+"' ";
					
					
					
						
					System.out.println("ecourts SQL:" + sql);
						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
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
					    	
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case details retrived successfully\"  , "
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
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Path("/submitDailyStatusForLegacyCases")
	public Response submitInstructions(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataBodyPart body, @FormDataParam("cino") String cino,
			@FormDataParam("dailyStatus") String dailyStatus, @FormDataParam("deptCode") String deptCode,
			@FormDataParam("distCode") String distCode, @FormDataParam("roleId") String roleId,
			@FormDataParam("userId") String userId, @FormDataParam("oldNewType") String oldNewType) throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		String jsonStr = "";

		try {
				String newFileName="DailyStatus_"+CommonModels.randomTransactionNo()+"."+body.getMediaType().getSubtype();
				
				String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/DailyStatus/"+newFileName;
				String fileUploadPath = "uploads/DailyStatus/"+newFileName;
				
				writeToFile(uploadedInputStream, uploadedFileLocation);
				
				String status_flag = "D";
				
				
				con = DatabasePlugin.connect();
				
				String sql = "insert into ecourts_dept_instructions (cino, instructions , upload_fileno,dept_code ,dist_code,insert_by,legacy_ack_flag,status_instruction_flag ) "
						+ " values (?,?, ?, ?, ?, ?,?,?)";

				ps = con.prepareStatement(sql);
				int i = 1;
				ps.setString(i, cino);
				ps.setString(++i, dailyStatus != null ? dailyStatus.toString() : "");
				ps.setString(++i, fileUploadPath);
				ps.setString(++i, CommonModels.checkStringObject(deptCode));
				ps.setInt(++i, CommonModels.checkIntObject(distCode));
				ps.setString(++i, userId);
				ps.setString(++i, "Legacy");
				ps.setString(++i, status_flag);
			 

				System.out.println("sql--"+sql);
		
				int a = ps.executeUpdate();
				if(a>0) {
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, remarks,uploaded_doc_path) "
							+ " values ('" + cino + "','SUBMITTED DAILY CASE STATUS', '"+userId+"', 'MOBILE APP', '"+dailyStatus+"','"+fileUploadPath+"')";
					DatabasePlugin.executeUpdate(sql, con);
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Instructions saved successfully\" }}";
				}		
			
		}	catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			// conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();

	}
	
	
	
	/*
	 * private void writeToFile(InputStream uploadedInputStream, String
	 * uploadedFileLocation) {
	 * 
	 * try { FileOutputStream out = new FileOutputStream(new
	 * File(uploadedFileLocation)); byte[] bytes =
	 * IOUtils.toByteArray(uploadedInputStream); out.write(bytes); out.flush();
	 * out.close(); } catch (IOException e) { e.printStackTrace(); } }
	 */
	
	

	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			
			FileOutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			byte[] bytes = IOUtils.toByteArray(uploadedInputStream);	
			out.write(bytes);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/newCasesInstructions")
	public static Response newCasesInstructions(String incomingData) throws Exception {
		
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
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", condition = "", caseType = "", caseRegYear = "", caseNo = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					
					con = DatabasePlugin.connect();
					
					if(roleId!=null && roleId.equals("6")) { // GPO
						
						condition=" and a.case_status=6 and e.gp_id='"+userId+"' ";
						
						
					}
					
					
					 sql="select (select case_full_name from case_type_master ctm where ctm.sno::text=e.casetype::text) as type_name_reg, "
						 		+ " e.reg_no, e.reg_year, to_char(e.inserted_time,'dd-mm-yyyy') as dt_regis, a.cino, "
						 		+ "  case when length(ack_file_path) > 10 then ack_file_path else '-' end as scanned_document_path,legacy_ack_flag  "
						 		+ " from (select distinct cino,dept_code,legacy_ack_flag from ecourts_dept_instructions where legacy_ack_flag='New') a "
						 		+ " inner join ecourts_gpo_ack_depts d on (d.ack_no=a.cino) and (d.dept_code=a.dept_code)   inner join ecourts_gpo_ack_dtls e on (d.ack_no=e.ack_no)  "
						 		+ " where d.dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='"+userId+"')";
					 
					 
					System.out.println("SQL:" + sql);						

						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CASE_TYPE", entry.get("type_name_reg").toString());
					    							    	
					    	cases.put("CASE_REG_DATE", entry.get("dt_regis"));
					    	cases.put("STATUS", "Pending");
					    	cases.put("ACK_NO", entry.get("cino"));
							
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case details retrived successfully\"  , "
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
	@Path("/displayCaseDetailsForNew")
	public static Response displayCaseDetailsForNew(String incomingData) throws Exception {
		
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
				} else if (!jObject.has("ACK_NO") || jObject.get("ACK_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- ACK_NO is missing in the request.\" }}";
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", Condition = "", ackNo = "", cino = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					cino = jObject.get("ACK_NO").toString();
					
											
					sql = "select a.slno ,ad.respondent_slno, a.ack_no,'New' as legacy_ack_flag , distid , advocatename ,advocateccno , casetype , maincaseno , a.remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
							+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
							+ "to_char(a.inserted_time,'dd-mm-yyyy') as generated_date, "
							+ "getack_dept_desc(a.ack_no::text) as dept_descs , coalesce(a.hc_ack_no,'-') as hc_ack_no "
							+ " from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
							+ " left join district_mst dm on (ad.dist_id=dm.district_id) "
							+ " left join dept_new dmt on (ad.dept_code=dmt.dept_code)"
							+ " inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name)  "
							+ " where a.delete_status is false and ack_type='NEW'    and (a.ack_no='"+cino+"' or a.hc_ack_no='"+cino+"' )  and respondent_slno='1'   "
							+ " order by a.inserted_time desc";
					
					
						
					System.out.println("ecourts SQL:" + sql);
						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("ACK_NO", entry.get("ack_no").toString());						    	
					    	
							if(entry.get("hc_ack_no") !=null && !entry.get("hc_ack_no").toString().equals("-")) {
					    		cases.put("HC_ACK_NO", entry.get("hc_ack_no").toString());
					    		cases.put("SCANNED_AFFIDAVIT_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("hc_ack_no").toString()+"/"+entry.get("hc_ack_no").toString()+".pdf");
					    	} else {
					    		cases.put("HC_ACK_NO", "");
					    		cases.put("SCANNED_AFFIDAVIT_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("ack_no").toString()+"/"+entry.get("ack_no").toString()+".pdf");
					    	}					    	
					    							    	
					    	cases.put("DATE", entry.get("generated_date").toString());
					    	cases.put("DIST_NAME", entry.get("district_name").toString());
					    	cases.put("CASE_TYPE", entry.get("case_full_name").toString());
					    	cases.put("MAIN_CASE_NO", entry.get("maincaseno").toString());
					    	cases.put("DEPARTMENTS", entry.get("dept_descs").toString());
					    	cases.put("ADVOCATE_CC_NO", entry.get("advocateccno").toString());
					    	cases.put("ADVOCATE_NAME", entry.get("advocatename").toString());
					    	cases.put("ACK_FILE_PATH", entry.get("ack_file_path"));
					    	cases.put("BARCODE_FILE_PATH", entry.get("barcode_file_path"));
					    	
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case details retrived successfully\"  , "
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
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Path("/submitDailyStatus")
	public Response submitDailyStatusForNewCases(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataBodyPart body, @FormDataParam("cino") String cino,
			@FormDataParam("dailyStatus") String dailyStatus, @FormDataParam("deptCode") String deptCode,
			@FormDataParam("distCode") String distCode, @FormDataParam("roleId") String roleId,
			@FormDataParam("userId") String userId, @FormDataParam("oldNewType") String oldNewType) throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		String jsonStr = "";

		try {
				String newFileName="DailyStatus_"+CommonModels.randomTransactionNo()+"."+body.getMediaType().getSubtype();
				
				String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/DailyStatus/"+newFileName;
				String fileUploadPath = "uploads/DailyStatus/"+newFileName;
				
				writeToFile(uploadedInputStream, uploadedFileLocation);
				
				String status_flag = "D";
				
				if(oldNewType != null && oldNewType.equals("Legacy")) {
					oldNewType = "Legacy";
				}
				else if (oldNewType != null && oldNewType.equals("New")){
					oldNewType = "New";
				}
				
				
				con = DatabasePlugin.connect();
				
				String sql = "insert into ecourts_dept_instructions (cino, instructions , upload_fileno,dept_code ,dist_code,insert_by,legacy_ack_flag,status_instruction_flag ) "
						+ " values (?,?, ?, ?, ?, ?,?,?)";

				ps = con.prepareStatement(sql);
				int i = 1;
				ps.setString(i, cino);
				ps.setString(++i, dailyStatus != null ? dailyStatus.toString() : "");
				ps.setString(++i, fileUploadPath);
				ps.setString(++i, CommonModels.checkStringObject(deptCode));
				ps.setInt(++i, CommonModels.checkIntObject(distCode));
				ps.setString(++i, userId);
				ps.setString(++i, oldNewType);
				ps.setString(++i, status_flag);
			 

				System.out.println("sql--"+sql);
		
				int a = ps.executeUpdate();
				if(a>0) {
					sql="insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, remarks,uploaded_doc_path) "
							+ " values ('" + cino + "','SUBMITTED DAILY CASE STATUS', '"+userId+"', 'MOBILE APP', '"+dailyStatus+"','"+fileUploadPath+"')";
					DatabasePlugin.executeUpdate(sql, con);
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  ,  \"RSPDESC\" :\"Instructions saved successfully\" }}";
				}		
			
		}	catch (Exception e) {
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
	@Path("/legacyCasesPWRCounterForApproval")
	public static Response legacyCasesPWRCounterForApproval(String incomingData) throws Exception {
		
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
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", condition = "", caseType = "", caseRegYear = "", caseNo = "" ;
					String counter_pw_flag = "";
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					
					con = DatabasePlugin.connect();
					
					if(roleId!=null && roleId.equals("6")) { // GPO
						
						if(jObject.has("PW_COUNTER_FLAG") && !jObject.get("PW_COUNTER_FLAG").toString().equals("")) {
							counter_pw_flag = CommonModels.checkStringObject(jObject.get("PW_COUNTER_FLAG").toString());
						}
						
						condition=" and a.case_status=6 and e.gp_id='"+userId+"' ";		
						
						if(counter_pw_flag.equals("PR")) {
							condition+=" and (pwr_uploaded='No' or pwr_uploaded='Yes') and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' )";
						}
						if(counter_pw_flag.equals("COUNTER")) {
							condition+=" and pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='Yes' and (counter_filed='No' or counter_filed='Yes') and coalesce(counter_approved_gp,'F')='F'";
						}
						
					}
					
					
					sql = "select type_name_reg,'Legacy' as legacy_ack_flag, reg_no, reg_year, to_char(dt_regis,'dd-mm-yyyy') as dt_regis, a.cino, "
							+ "case when length(scanned_document_path) > 10 then scanned_document_path else '-' end as scanned_document_path from ecourts_case_data a "
							+ " left join ecourts_olcms_case_details od on (a.cino=od.cino)"
							+ " left join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code and a.assigned_to=e.gp_id) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where assigned=true "+condition
							+ " and coalesce(a.ecourts_case_status,'')!='Closed' ";
					
					sql	+= "order by reg_year,type_name_reg,reg_no";
					 
					 
					System.out.println("SQL:" + sql);						

						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CASE_TYPE", entry.get("type_name_reg").toString());
					    	String caseno = entry.get("type_name_reg").toString()+" "+entry.get("reg_no").toString()+"/"+entry.get("reg_year").toString();
					    	cases.put("CASE_NO", caseno);						    	
					    	cases.put("CASE_REG_DATE", entry.get("dt_regis"));
					    	cases.put("STATUS", "Pending");
					    	cases.put("CINO", entry.get("cino"));
							
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case para wise remarks retrived successfully\"  , "
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
	@Path("/newCasesPWRCounterForApproval")
	public static Response newCasesPWRCounterForApproval(String incomingData) throws Exception {
		
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
				} 
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", condition = "", caseType = "", caseRegYear = "", caseNo = "" ;
					String counter_pw_flag = "";
					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					
					con = DatabasePlugin.connect();
					
					if(roleId!=null && roleId.equals("6")) { // GPO
						
						if(jObject.has("PW_COUNTER_FLAG") && !jObject.get("PW_COUNTER_FLAG").toString().equals("")) {
							counter_pw_flag = CommonModels.checkStringObject(jObject.get("PW_COUNTER_FLAG").toString());
						}
						
						condition=" and a.case_status=6 and e.gp_id='"+userId+"' ";		
						
						if(counter_pw_flag.equals("PR")) {
							condition+=" and (pwr_uploaded='No' or pwr_uploaded='Yes') and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' )";
						}
						if(counter_pw_flag.equals("COUNTER")) {
							condition+=" and pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='Yes' and (counter_filed='No' or counter_filed='Yes') and coalesce(counter_approved_gp,'F')='F'";
						}
						
					}
					
					
					sql = "select (select case_full_name from case_type_master ctm where ctm.sno::text=b.casetype::text) as type_name_reg,'New' as legacy_ack_flag, reg_no, reg_year, inserted_time::date as dt_regis, a.ack_no as cino, "
							+ "case when length(ack_file_path) > 10 then ack_file_path else '-' end as scanned_document_path "
							+ " from ecourts_gpo_ack_depts a inner join ecourts_gpo_ack_dtls b on (a.ack_no=b.ack_no)"
							+ " left join ecourts_olcms_case_details od on (a.ack_no=od.cino)"
							//+" inner join  ecourts_gpo_ack_dtls ad on (a.cino=ad.ack_no) inner join ecourts_gpo_ack_depts d on (ad.ack_no=d.ack_no)"
							+ " left join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code and a.assigned_to=e.gp_id) "
							+ " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where assigned=true "+condition
							+ " and coalesce(a.ecourts_case_status,'')!='Closed' ";
					
					sql	+= "order by reg_year,type_name_reg,reg_no";
					 
					 
					System.out.println("SQL:" + sql);						

						
					
					JSONObject casesData = new JSONObject();

					con = DatabasePlugin.connect();
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("CASE_TYPE", entry.get("type_name_reg").toString());
					    	String caseno = entry.get("type_name_reg").toString()+" "+entry.get("reg_no").toString()+"/"+entry.get("reg_year").toString();
					    	cases.put("CASE_NO", caseno);						    	
					    	cases.put("CASE_REG_DATE", entry.get("dt_regis"));
					    	cases.put("STATUS", "Pending");
					    	cases.put("CINO", entry.get("cino"));
							
							
							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy case para wise remarks retrived successfully\"  , "
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
	@Path("/getCaseDetailsForLegacy")
	public static Response getCaseDetailsForLegacy(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CINO is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					
					cino=jObject.get("CINO").toString();
					con = DatabasePlugin.connect();
					
					sql = "select a.*, prayer from ecourts_case_data a left join nic_prayer_data np on (a.cino=np.cino) where a.cino='" + cino + "'";
					List<Map<String, Object>> caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONObject casesData = new JSONObject();
					JSONArray caseDetailsArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						
						for (Map<String, Object> entry : caseDetails) {		
						    
						    	JSONObject casehistory = new JSONObject();
						    	casehistory.put("DOWNLOAD_AFFIDAVIT", entry.get("scanned_document_path") !=null ? entry.get("scanned_document_path").toString() :"");
						    	casehistory.put("DATE_OF_FILING", entry.get("date_of_filing") !=null ? entry.get("date_of_filing").toString() :"");
						    	casehistory.put("CASE_TYPE", entry.get("type_name_reg") !=null ? entry.get("type_name_reg").toString() :"");
						    	casehistory.put("FILING_NO", entry.get("fil_no") !=null ? entry.get("fil_no").toString() :"");
						    	casehistory.put("FILING YEAR", entry.get("fil_year") !=null ? entry.get("fil_year").toString() :"");
						    	casehistory.put("REG_NO", entry.get("reg_no") !=null ? entry.get("reg_no").toString() :"");
						    	casehistory.put("EST_CODE", entry.get("est_code") !=null ? entry.get("est_code").toString() :"");
						    	casehistory.put("CASE_ID", entry.get("case_type_id") !=null ? entry.get("case_type_id").toString() :"");
						    	casehistory.put("CAUSE_TYPE", entry.get("causelist_type") !=null ? entry.get("causelist_type").toString() :"");
						    	casehistory.put("BENCH_NAME", entry.get("bench_name") !=null ? entry.get("bench_name").toString() :"");
						    	casehistory.put("JUDICIAL_BRANCH", entry.get("judicial_branch") !=null ? entry.get("judicial_branch").toString() :"");
						    	casehistory.put("CORAM", entry.get("coram") !=null ? entry.get("coram").toString() :"");
						    	casehistory.put("COURT_EST_NAME", entry.get("court_est_name") !=null ? entry.get("court_est_name").toString() :"");
						    	casehistory.put("STATE_NAME", entry.get("state_name") !=null ? entry.get("state_name").toString() :"");
						    	casehistory.put("DIST_NAME", entry.get("dist_name") !=null ? entry.get("dist_name").toString() :"");
						    	casehistory.put("DATE_FIRST_LIST", entry.get("date_first_list") !=null ? entry.get("date_first_list").toString() :"");
						    	casehistory.put("DATE_NEXT_LIST", entry.get("date_next_list") !=null ? entry.get("date_next_list").toString() :"");
						    	casehistory.put("DATE_OF_DECISION", entry.get("date_of_decision") !=null ? entry.get("date_of_decision").toString() :"");
						    	casehistory.put("PURPOSE", entry.get("purpose_name") !=null ? entry.get("purpose_name").toString() :"");
						    	casehistory.put("PETITIONER_NAME", entry.get("pet_name") !=null ? entry.get("pet_name").toString() :"");
						    	casehistory.put("PETITIONER_ADV", entry.get("pet_adv") !=null ? entry.get("pet_adv").toString() :"");
						    	casehistory.put("PETITIONER_LEGAL_HEIR", entry.get("pet_legal_heir") !=null ? entry.get("pet_legal_heir").toString() :"");
						    	casehistory.put("RESP_NAME", entry.get("res_name") !=null ? entry.get("res_name").toString() :"" + "," +entry.get("address") !=null ? entry.get("address").toString() :"" );
						    	casehistory.put("RESPONDENT_ADV", entry.get("res_adv") !=null ? entry.get("res_adv").toString() :"");
						    	casehistory.put("PRAYER", entry.get("prayer") !=null ? entry.get("prayer").toString() :"");
						    	
						    	caseDetailsArray.put(casehistory);
						}
						
						
					} 
					
					casesData.put("CASE_DETAILS", caseDetailsArray);
								
								
					sql = "select * from ecourts_case_acts where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray actsListArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
						JSONObject actsList = new JSONObject();
						actsList.put("ACT",entry.get("act") !=null ? entry.get("act").toString() :"");
						actsList.put("ACT_NAME",entry.get("actname") !=null ? entry.get("actname").toString() :"");
						actsList.put("SECTION",entry.get("section") !=null ? entry.get("section").toString() :"");

						actsListArray.put(actsList);
						}
					}
					    
					casesData.put("ACTS_LIST", actsListArray);
					
					
					
					sql = "select  * from apolcms.ecourts_pet_extra_party where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray petListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");

							petListArray.put(respList);
						}
					}
					
					casesData.put("PETITIONERS_LIST", petListArray);
					
					
					/* Respondent's List */
					
					sql = "select b.party_no,b.res_name as party_name, b.address from nic_resp_addr_data b left join ecourts_res_extra_party a on (b.cino=a.cino and b.party_no-1=coalesce(trim(a.party_no),'0')::int4) where b.cino='" + cino + "' order by b.party_no";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray respListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");
							respList.put("ADDRESS",entry.get("address") !=null ? entry.get("address").toString() :"");
							respListArray.put(respList);
						}
					}
					
					casesData.put("RESPONDENTS_LIST", respListArray);
					
					
					// Dept. Instructions
					sql = "select instructions, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_dept_instructions where cino='" + cino + "'  order by 1 ";
					System.out.println("Dept INstructions sql--" + sql);
					List<Map<String, Object>> existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray instructionsArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("instructions") !=null ? entry.get("instructions").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							instructionsArray.put(obj);
						}
					}
					
					casesData.put("INSTRUCTIONS", instructionsArray);
					
					
					// Daily Case Status Updates by GP
					sql = "select status_remarks, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_gpo_daily_status where cino='" + cino + "'  order by 1 ";
					System.out.println("DAILY STATUS SQL--" + sql);
					existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray dailyStatusArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("status_remarks") !=null ? entry.get("status_remarks").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							dailyStatusArray.put(obj);
						}
					}
					
					casesData.put("DAILY_CASE_STATUS", dailyStatusArray);
					
					
					
					//IA filings list
					sql = "select  * from apolcms.ecourts_case_iafiling where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray iaFilingsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("IA_NO",entry.get("ia_no") !=null ? entry.get("ia_no").toString() :"");
							obj.put("IA_PET_NAME",entry.get("ia_pet_name") !=null ? entry.get("ia_pet_name").toString() :"");
							obj.put("IA_PET_DISPOSAL",entry.get("ia_pend_disp") !=null ? entry.get("ia_pend_disp").toString() :"");
							obj.put("DATE_OF_FILING",entry.get("date_of_filing") !=null ? entry.get("date_of_filing").toString() :"");
							

							iaFilingsArray.put(obj);
						}
					}
					
					casesData.put("IA_FILINGS_LIST", iaFilingsArray);
					
					
					
					//Parawise remarks history and Counter Affidavit History
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("PARA WISE REMARKS SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray pwrArray = new JSONArray();
					JSONArray counterArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							if(entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Parawise Remarks"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");

								pwrArray.put(obj);
							}
							else if (entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Counter"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
								counterArray.put(obj);
							}
						}
					}
					
					casesData.put("PARA_WISE_REMARKS_HISTORY", pwrArray);
					casesData.put("COUNTER_AFFIDAVIT_HISTORY", counterArray);
					
					
					// START - Para-wise remarks & Counter Filed submission section...
					sql = "SELECT cino, case when length(petition_document) > 0 then petition_document else null end as petition_document, "
							+ " case when length(counter_filed_document) > 0 then counter_filed_document else null end as counter_filed_document,"
							+ " case when length(judgement_order) > 0 then judgement_order else null end as judgement_order,"
							+ " case when length(action_taken_order) > 0 then action_taken_order else null end as action_taken_order,"
							+ " last_updated_by, last_updated_on, counter_filed, remarks, ecourts_case_status, corresponding_gp, "
							+ " pwr_uploaded, to_char(pwr_submitted_date,'dd/mm/yyyy') as pwr_submitted_date, to_char(pwr_received_date,'dd/mm/yyyy') as pwr_received_date, "
							+ " pwr_approved_gp, to_char(pwr_gp_approved_date,'dd/mm/yyyy') as pwr_gp_approved_date, appeal_filed, "
							+ " appeal_filed_copy, to_char(appeal_filed_date,'dd/mm/yyyy') as appeal_filed_date, pwr_uploaded_copy, action_to_perfom, counter_approved_gp "
							+ " FROM apolcms.ecourts_olcms_case_details where cino='" + cino + "'";
					
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray parawiseArray = new JSONArray();
					JSONArray counterFiledArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Parawise Remarks")
									&& entry.get("pwr_approved_gp") != null
									&& entry.get("pwr_approved_gp").equals("No")) {
								JSONObject obj = new JSONObject();
								obj.put("PETITION_DOC_PATH", CommonModels.checkStringObject(entry.get("petition_document"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("petition_document") : "");
								obj.put("CASE_STATUS", entry.get("ecourts_case_status"));
								obj.put("JUDGEMENT_ORDER", CommonModels.checkStringObject(entry.get("judgement_order"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("judgement_order") : "");
								obj.put("ACTION_TAKEN_ORDER", CommonModels.checkStringObject(entry.get("action_taken_order"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("action_taken_order") : "");
								obj.put("PWR_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("DATE_OF_PWR_SUBMISSION", entry.get("pwr_submitted_date"));
								obj.put("PWR_UPLOADED_COPY", CommonModels.checkStringObject(entry.get("pwr_uploaded_copy"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("pwr_uploaded_copy") : "");
								obj.put("PWR_APPROVED_BY_GP", entry.get("pwr_approved_gp"));
								obj.put("PWR_GP_APPROVED_DATE", entry.get("pwr_gp_approved_date"));
								obj.put("PWR_RECEIVED_DATE", entry.get("pwr_received_date"));
								obj.put("PARA_WISE_REMARKS_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("ACTION_TO_PERFORM", entry.get("action_to_perfom"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Counter Affidavit"))
									obj.put("REMARKS", entry.get("remarks"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Parawise Remarks"))
									obj.put("REMARKS", entry.get("remarks"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Parawise Remarks")) {
									// a. APPROVED
									if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("Yes")) {
										// disable Submission
										obj.put("PWR_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. NOT APPROVED
									else if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("PWR_SUBMISSION_BUTTON", "ENABLE");
									}
								}
								// 2. View Counter uploaded by Dept. and Disable Parawise Remarks Updation and
								// enable Counter Upload by GP.
								else if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Counter Affidavit")) {
									// a. PWR NOT APPROVED
									if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. PWR APPROVED COUNTER NOT APPROVED
									else if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& !CommonModels.checkStringObject(entry.get("counter_approved_gp"))
													.equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "ENABLE");
									}
									// c. COUNTER APPROVED
									if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& CommonModels.checkStringObject(entry.get("counter_approved_gp"))
													.equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
								}

								parawiseArray.put(obj);
							}
							else if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Counter Affidavit")
									&& entry.get("pwr_approved_gp") != null
									&& entry.get("pwr_approved_gp").equals("Yes") && !CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
								JSONObject obj = new JSONObject();
								obj.put("COUNTER_FILED", entry.get("counter_filed"));
								obj.put("COUNTER_FILED_DOC_PATH", entry.get("counter_filed_document"));
								obj.put("ACTION_TO_PERFORM", entry.get("action_to_perfom"));
								obj.put("REMARKS", entry.get("remarks"));

								

								if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Counter Affidavit")) {
									// a. PWR NOT APPROVED
									if (entry.get("pwr_approved_gp") != null && entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. PWR APPROVED COUNTER NOT APPROVED
									else if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& !CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "ENABLE");
									}
									// c. COUNTER APPROVED
									if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
								}

								counterFiledArray.put(obj);
							}
						}
						
						casesData.put("PARA_WISE_REMARKS_DETAILS", parawiseArray);
						casesData.put("COUNTER_FILED_DETAILS", counterFiledArray);
					}
					
					// END - Para-wise remarks & Counter Filed submission section...
					
					
					
					
					
					//INTERIM ORDERS LIST
					sql = "select  * from apolcms.ecourts_case_interimorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray interimOrderArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH","https://apolcms.ap.gov.in/HighCourtsCaseOrders/"+entry.get("cino").toString()+"-interimorder-"+entry.get("order_no").toString()+".pdf");

							interimOrderArray.put(obj);
						}
					}
					
					casesData.put("INTERIM_ORDERS_LIST", interimOrderArray);
					
					
					//Tagged along cases List or Linked cases list
					sql = "select  * from apolcms.ecourts_case_link_cases where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray linkedCasesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("FILING_NO",entry.get("filing_number") !=null ? entry.get("filing_number").toString() :"");
							obj.put("CASE_NO",entry.get("case_number") !=null ? entry.get("case_number").toString() :"");
							

							linkedCasesArray.put(obj);
						}
					}
					
					casesData.put("TAGGED_ALONG_CASES_LIST", linkedCasesArray);
					
					
					
					//Objections History
					sql = "select  * from apolcms.ecourts_case_objections where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray objectionsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("OBJ_NO",entry.get("objection_no") !=null ? entry.get("objection_no").toString() :"");
							obj.put("OBJ_DESC",entry.get("objection_desc") !=null ? entry.get("objection_desc").toString() :"");
							obj.put("SCRUTINY_DATE",entry.get("scrutiny_date") !=null ? entry.get("scrutiny_date").toString() :"");
							obj.put("COMPLIANCE_DATE",entry.get("objections_compliance_by_date") !=null ? entry.get("objections_compliance_by_date").toString() :"");
							obj.put("RECEIPT_DATE",entry.get("obj_reciept_date") !=null ? entry.get("obj_reciept_date").toString() :"");
							
							objectionsArray.put(obj);
						}
					}
					
					casesData.put("OBJECTIONS_HISTORY", objectionsArray);
		
					
					//Case History Data
					sql = "select  * from apolcms.ecourts_historyofcasehearing where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseHistoryArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("JUDGE_NAME",entry.get("judge_name") !=null ? entry.get("judge_name").toString() :"");
							obj.put("BUSINESS_DATE",entry.get("business_date") !=null ? entry.get("business_date").toString() :"");
							obj.put("HEARING_DATE",entry.get("hearing_date") !=null ? entry.get("hearing_date").toString() :"");
							obj.put("PURPOSE",entry.get("purpose_of_listing") !=null ? entry.get("purpose_of_listing").toString() :"");
							obj.put("CAUSE_TYPE",entry.get("causelist_type") !=null ? entry.get("causelist_type").toString() :"");
							
							caseHistoryArray.put(obj);
						}
					}
					
					casesData.put("CASE_HISTORY", caseHistoryArray);
					
					
					//Case Activities Data
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("ecourts activities SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseActivitiesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
							obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
							obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
							obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
							obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
							obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null && !entry.get("uploaded_doc_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
							
							caseActivitiesArray.put(obj);
						}
					}
					
					casesData.put("CASE_ACTIVITIES_LIST", caseActivitiesArray);
					
					
					//Final Orders List
					sql = "select  * from apolcms.ecourts_case_finalorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray finalOrdersArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH", entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("order_document_path").toString():"");
							
							finalOrdersArray.put(obj);
						}
					}
					
					casesData.put("FINAL_ORDERS_LIST", finalOrdersArray);
					
					
					String finalString = casesData.toString();

					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases details retrieved successfully\"  , "
								+ finalString.substring(1, finalString.length() - 1) + "}}";
					
					
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
	@Path("/getCaseDetailsForNew")
	public static Response getCaseDetailsForNew(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("ACK_NO") || jObject.get("ACK_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ACK_NO is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					
					cino=jObject.get("ACK_NO").toString();
					con = DatabasePlugin.connect();
					
					sql = " select  a.ack_no ,  dept_code,respondent_slno  , (select district_name from district_mst dm where dm.district_id::text=b.distid::text) as district_name,"
							+ " servicetpye  ,    advocatename , advocateccno,   (select case_full_name from case_type_master ctm where ctm.sno::text=b.casetype::text) as casetype,maincaseno , "
							+ "remarks , inserted_time::date ,  inserted_by ,  ack_file_path   ,  "
							+ "petitioner_name    ,  reg_year  , reg_no , mode_filing  , case_category  ,  hc_ack_no  "
							+ " from ecourts_gpo_ack_depts a inner join ecourts_gpo_ack_dtls b on (a.ack_no=b.ack_no) where b.ack_no='" + cino + "'  and respondent_slno='1'";
					
					List<Map<String, Object>> caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONObject casesData = new JSONObject();
					JSONArray caseDetailsArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						
						for (Map<String, Object> entry : caseDetails) {		
						    
						    	JSONObject casehistory = new JSONObject();
						    	casehistory.put("DOWNLOAD_AFFIDAVIT", entry.get("scanned_document_path") !=null ? entry.get("ack_file_path") :"");
						    	casehistory.put("DATE_OF_FILING", entry.get("inserted_time") !=null ? entry.get("inserted_time").toString() :"");
						    	casehistory.put("CASE_TYPE", entry.get("casetype") !=null ? entry.get("casetype").toString() :"");
						    	casehistory.put("FILING_NO", entry.get("mode_filing") !=null ? entry.get("mode_filing").toString() :"");
						    	
						    	casehistory.put("CASE_ID", entry.get("casetype") !=null ? entry.get("casetype").toString() :"");
						    	casehistory.put("DIST_NAME", entry.get("district_name") !=null ? entry.get("district_name").toString() :"");
						    	casehistory.put("PETITIONER_NAME", entry.get("petitioner_name") !=null ? entry.get("petitioner_name").toString() :"");
						    	casehistory.put("RESP_ADVOCATE", entry.get("advocatename") !=null ? entry.get("advocatename").toString() :"");
						    	casehistory.put("RESP_ADVOCATE_NO", entry.get("advocateccno") !=null ? entry.get("advocateccno").toString() :"");
						    	casehistory.put("ACK_NO", entry.get("ack_no") !=null ? entry.get("ack_no").toString() :"");
						    	casehistory.put("HC_ACK_NO", entry.get("hc_ack_no") !=null ? entry.get("hc_ack_no").toString() :"");
						    	casehistory.put("FILING_YEAR", entry.get("reg_year") !=null ? entry.get("reg_year").toString() :"");
						    	casehistory.put("REG_NO", entry.get("reg_no") !=null ? entry.get("reg_no").toString() :"");
						    	
						    	caseDetailsArray.put(casehistory);
						}
						
						
					} 
					
					casesData.put("CASE_DETAILS", caseDetailsArray);
								
								
					sql = "select * from ecourts_case_acts where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray actsListArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
						JSONObject actsList = new JSONObject();
						actsList.put("ACT",entry.get("act") !=null ? entry.get("act").toString() :"");
						actsList.put("ACT_NAME",entry.get("actname") !=null ? entry.get("actname").toString() :"");
						actsList.put("SECTION",entry.get("section") !=null ? entry.get("section").toString() :"");

						actsListArray.put(actsList);
						}
					}
					    
					casesData.put("ACTS_LIST", actsListArray);
					
					
					
					sql = "select  * from apolcms.ecourts_pet_extra_party where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray petListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");

							petListArray.put(respList);
						}
					}
					
					casesData.put("PETITIONERS_LIST", petListArray);
					
					
					/* Respondent's List */
					
					sql = "select b.party_no,b.res_name as party_name, b.address from nic_resp_addr_data b left join ecourts_res_extra_party a on (b.cino=a.cino and b.party_no-1=coalesce(trim(a.party_no),'0')::int4) where b.cino='" + cino + "' order by b.party_no";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray respListArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject respList = new JSONObject();
							respList.put("PARTY_NO",entry.get("party_no") !=null ? entry.get("party_no").toString() :"");
							respList.put("PARTY_NAME",entry.get("party_name") !=null ? entry.get("party_name").toString() :"");
							respList.put("ADDRESS",entry.get("address") !=null ? entry.get("address").toString() :"");
							respListArray.put(respList);
						}
					}
					
					casesData.put("RESPONDENTS_LIST", respListArray);
					
					
					// Dept. Instructions
					sql = "select instructions, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_dept_instructions where cino='" + cino + "'  order by 1 ";
					System.out.println("Dept INstructions sql--" + sql);
					List<Map<String, Object>> existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray instructionsArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("instructions") !=null ? entry.get("instructions").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							instructionsArray.put(obj);
						}
					}
					
					casesData.put("INSTRUCTIONS", instructionsArray);
					
					
					// Daily Case Status Updates by GP
					sql = "select status_remarks, to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time from ecourts_gpo_daily_status where cino='" + cino + "'  order by 1 ";
					System.out.println("DAILY STATUS SQL--" + sql);
					existData = DatabasePlugin.executeQuery(sql, con);
					JSONArray dailyStatusArray = new JSONArray();
					if (existData != null && !existData.isEmpty() && existData.size() > 0) {
						for (Map<String, Object> entry : existData) {
							JSONObject obj = new JSONObject();
							obj.put("DESCRIPTION",entry.get("status_remarks") !=null ? entry.get("status_remarks").toString() :"");
							obj.put("SUBMITTED_DATE",entry.get("insert_time") !=null ? entry.get("insert_time").toString() :"");

							dailyStatusArray.put(obj);
						}
					}
					
					casesData.put("DAILY_CASE_STATUS", dailyStatusArray);
					
					
					
					//IA filings list
					sql = "select  * from apolcms.ecourts_case_iafiling where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray iaFilingsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("IA_NO",entry.get("ia_no") !=null ? entry.get("ia_no").toString() :"");
							obj.put("IA_PET_NAME",entry.get("ia_pet_name") !=null ? entry.get("ia_pet_name").toString() :"");
							obj.put("IA_PET_DISPOSAL",entry.get("ia_pend_disp") !=null ? entry.get("ia_pend_disp").toString() :"");
							obj.put("DATE_OF_FILING",entry.get("date_of_filing") !=null ? entry.get("date_of_filing").toString() :"");
							

							iaFilingsArray.put(obj);
						}
					}
					
					casesData.put("IA_FILINGS_LIST", iaFilingsArray);
					
					
					
					//Parawise remarks history and Counter Affidavit History
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("PARA WISE REMARKS SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					JSONArray pwrArray = new JSONArray();
					JSONArray counterArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							if(entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Parawise Remarks"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");

								pwrArray.put(obj);
							}
							else if (entry.get("action_type") !=null && entry.get("action_type").toString().equals("Uploaded Counter"))
							{
								JSONObject obj = new JSONObject();
								obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
								obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
								obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
								obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
								obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
								obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null ? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
								counterArray.put(obj);
							}
						}
					}
					
					casesData.put("PARA_WISE_REMARKS_HISTORY", pwrArray);
					casesData.put("COUNTER_AFFIDAVIT_HISTORY", counterArray);
					
					
					// START - Para-wise remarks & Counter Filed submission section...
					sql = "SELECT cino, case when length(petition_document) > 0 then petition_document else null end as petition_document, "
							+ " case when length(counter_filed_document) > 0 then counter_filed_document else null end as counter_filed_document,"
							+ " case when length(judgement_order) > 0 then judgement_order else null end as judgement_order,"
							+ " case when length(action_taken_order) > 0 then action_taken_order else null end as action_taken_order,"
							+ " last_updated_by, last_updated_on, counter_filed, remarks, ecourts_case_status, corresponding_gp, "
							+ " pwr_uploaded, to_char(pwr_submitted_date,'dd/mm/yyyy') as pwr_submitted_date, to_char(pwr_received_date,'dd/mm/yyyy') as pwr_received_date, "
							+ " pwr_approved_gp, to_char(pwr_gp_approved_date,'dd/mm/yyyy') as pwr_gp_approved_date, appeal_filed, "
							+ " appeal_filed_copy, to_char(appeal_filed_date,'dd/mm/yyyy') as appeal_filed_date, pwr_uploaded_copy, action_to_perfom, counter_approved_gp "
							+ " FROM apolcms.ecourts_olcms_case_details where cino='" + cino + "'";
					
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray parawiseArray = new JSONArray();
					JSONArray counterFiledArray = new JSONArray();
					
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Parawise Remarks")
									&& entry.get("pwr_approved_gp") != null
									&& entry.get("pwr_approved_gp").equals("No")) {
								JSONObject obj = new JSONObject();
								obj.put("PETITION_DOC_PATH", CommonModels.checkStringObject(entry.get("petition_document"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("petition_document") : "");
								obj.put("CASE_STATUS", entry.get("ecourts_case_status"));
								obj.put("JUDGEMENT_ORDER", CommonModels.checkStringObject(entry.get("judgement_order"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("judgement_order") : "");
								obj.put("ACTION_TAKEN_ORDER", CommonModels.checkStringObject(entry.get("action_taken_order"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("action_taken_order") : "");
								obj.put("PWR_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("DATE_OF_PWR_SUBMISSION", entry.get("pwr_submitted_date"));
								obj.put("PWR_UPLOADED_COPY", CommonModels.checkStringObject(entry.get("pwr_uploaded_copy"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("pwr_uploaded_copy") : "");
								obj.put("PWR_APPROVED_BY_GP", entry.get("pwr_approved_gp"));
								obj.put("PWR_GP_APPROVED_DATE", entry.get("pwr_gp_approved_date"));
								obj.put("PWR_RECEIVED_DATE", entry.get("pwr_received_date"));
								obj.put("PARA_WISE_REMARKS_SUBMITTED", entry.get("pwr_uploaded"));
								obj.put("ACTION_TO_PERFORM", entry.get("action_to_perfom"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Counter Affidavit"))
									obj.put("REMARKS", entry.get("remarks"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Parawise Remarks"))
									obj.put("REMARKS", entry.get("remarks"));

								if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Parawise Remarks")) {
									// a. APPROVED
									if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("Yes")) {
										// disable Submission
										obj.put("PWR_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. NOT APPROVED
									else if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("PWR_SUBMISSION_BUTTON", "ENABLE");
									}
								}
								// 2. View Counter uploaded by Dept. and Disable Parawise Remarks Updation and
								// enable Counter Upload by GP.
								else if (CommonModels.checkStringObject(entry.get("action_to_perfom"))
										.equals("Counter Affidavit")) {
									// a. PWR NOT APPROVED
									if (entry.get("pwr_approved_gp") != null
											&& entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. PWR APPROVED COUNTER NOT APPROVED
									else if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& !CommonModels.checkStringObject(entry.get("counter_approved_gp"))
													.equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "ENABLE");
									}
									// c. COUNTER APPROVED
									if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& CommonModels.checkStringObject(entry.get("counter_approved_gp"))
													.equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
								}

								parawiseArray.put(obj);
							}
							else if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Counter Affidavit")
									&& entry.get("pwr_approved_gp") != null
									&& entry.get("pwr_approved_gp").equals("Yes") && !CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
								JSONObject obj = new JSONObject();
								obj.put("COUNTER_FILED", entry.get("counter_filed"));
								obj.put("COUNTER_FILED_DOC_PATH", CommonModels.checkStringObject(entry.get("counter_filed_document"))!="" ? "https://apolcms.ap.gov.in/"+entry.get("counter_filed_document") : "");
								obj.put("ACTION_TO_PERFORM", entry.get("action_to_perfom"));
								obj.put("REMARKS", entry.get("remarks"));

								

								if (CommonModels.checkStringObject(entry.get("action_to_perfom")).equals("Counter Affidavit")) {
									// a. PWR NOT APPROVED
									if (entry.get("pwr_approved_gp") != null && entry.get("pwr_approved_gp").equals("No")) {
										// enable upload & entry.
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
									// b. PWR APPROVED COUNTER NOT APPROVED
									else if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& !CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "ENABLE");
									}
									// c. COUNTER APPROVED
									if (CommonModels.checkStringObject(entry.get("pwr_approved_gp")).equals("Yes")
											&& CommonModels.checkStringObject(entry.get("counter_approved_gp")).equals("T")) {
										obj.put("COUNTER_SUBMISSION_BUTTON", "DISABLE");
									}
								}

								counterFiledArray.put(obj);
							}
						}
						
						casesData.put("PARA_WISE_REMARKS_DETAILS", parawiseArray);
						casesData.put("COUNTER_FILED_DETAILS", counterFiledArray);
					}
					
					// END - Para-wise remarks & Counter Filed submission section...
					
					
					
					//INTERIM ORDERS LIST
					sql = "select  * from apolcms.ecourts_case_interimorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray interimOrderArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH","https://apolcms.ap.gov.in/HighCourtsCaseOrders/"+entry.get("cino").toString()+"-interimorder-"+entry.get("order_no").toString()+".pdf");

							interimOrderArray.put(obj);
						}
					}
					
					casesData.put("INTERIM_ORDERS_LIST", interimOrderArray);
					
					
					//Tagged along cases List or Linked cases list
					sql = "select  * from apolcms.ecourts_case_link_cases where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray linkedCasesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("FILING_NO",entry.get("filing_number") !=null ? entry.get("filing_number").toString() :"");
							obj.put("CASE_NO",entry.get("case_number") !=null ? entry.get("case_number").toString() :"");
							

							linkedCasesArray.put(obj);
						}
					}
					
					casesData.put("TAGGED_ALONG_CASES_LIST", linkedCasesArray);
					
					
					
					//Objections History
					sql = "select  * from apolcms.ecourts_case_objections where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray objectionsArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("OBJ_NO",entry.get("objection_no") !=null ? entry.get("objection_no").toString() :"");
							obj.put("OBJ_DESC",entry.get("objection_desc") !=null ? entry.get("objection_desc").toString() :"");
							obj.put("SCRUTINY_DATE",entry.get("scrutiny_date") !=null ? entry.get("scrutiny_date").toString() :"");
							obj.put("COMPLIANCE_DATE",entry.get("objections_compliance_by_date") !=null ? entry.get("objections_compliance_by_date").toString() :"");
							obj.put("RECEIPT_DATE",entry.get("obj_reciept_date") !=null ? entry.get("obj_reciept_date").toString() :"");
							
							objectionsArray.put(obj);
						}
					}
					
					casesData.put("OBJECTIONS_HISTORY", objectionsArray);
		
					
					//Case History Data
					sql = "select  * from apolcms.ecourts_historyofcasehearing where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseHistoryArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("JUDGE_NAME",entry.get("judge_name") !=null ? entry.get("judge_name").toString() :"");
							obj.put("BUSINESS_DATE",entry.get("business_date") !=null ? entry.get("business_date").toString() :"");
							obj.put("HEARING_DATE",entry.get("hearing_date") !=null ? entry.get("hearing_date").toString() :"");
							obj.put("PURPOSE",entry.get("purpose_of_listing") !=null ? entry.get("purpose_of_listing").toString() :"");
							obj.put("CAUSE_TYPE",entry.get("causelist_type") !=null ? entry.get("causelist_type").toString() :"");
							
							caseHistoryArray.put(obj);
						}
					}
					
					casesData.put("CASE_HISTORY", caseHistoryArray);
					
					
					//Case Activities Data
					sql="select cino,action_type,inserted_by,to_char(inserted_on,'dd-Mon-yyyy hh24:mi:ss PM') as inserted_on,assigned_to,remarks as remarks, coalesce(uploaded_doc_path,'-') as uploaded_doc_path from ecourts_case_activities where cino = '"+cino+"' order by inserted_on desc";
					System.out.println("ecourts activities SQL:" + sql);
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray caseActivitiesArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("DATE",entry.get("inserted_on") !=null ? entry.get("inserted_on").toString() :"");
							obj.put("ACTIVITY",entry.get("action_type") !=null ? entry.get("action_type").toString() :"");
							obj.put("UPDATED_BY",entry.get("inserted_by") !=null ? entry.get("inserted_by").toString() :"");
							obj.put("ASSIGNED_TO",entry.get("assigned_to") !=null ? entry.get("assigned_to").toString() :"");
							obj.put("REMARKS",entry.get("remarks") !=null ? entry.get("remarks").toString() :"");
							obj.put("DOCUMENT_PATH",entry.get("uploaded_doc_path") !=null && !entry.get("uploaded_doc_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("uploaded_doc_path").toString() :"");
							
							caseActivitiesArray.put(obj);
						}
					}
					
					casesData.put("CASE_ACTIVITIES_LIST", caseActivitiesArray);
					
					
					//Final Orders List
					sql = "select  * from apolcms.ecourts_case_finalorder where cino='" + cino + "'";
					caseDetails = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray finalOrdersArray = new JSONArray();
					if (caseDetails != null && !caseDetails.isEmpty() && caseDetails.size() > 0) {
						for (Map<String, Object> entry : caseDetails) {
							JSONObject obj = new JSONObject();
							obj.put("SR_NO",entry.get("sr_no") !=null ? entry.get("sr_no").toString() :"");
							obj.put("ORDER_NO",entry.get("order_no") !=null ? entry.get("order_no").toString() :"");
							obj.put("ORDER_DATE",entry.get("order_date") !=null ? entry.get("order_date").toString() :"");
							obj.put("ORDER_DETAILS",entry.get("order_details") !=null ? entry.get("order_details").toString() :"");
							obj.put("ORDER_DOCUMENT_NAME",entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? entry.get("order_details").toString() +"-"+ entry.get("order_no").toString():"");
							obj.put("ORDER_DOCUMENT_PATH", entry.get("order_document_path") !=null && !entry.get("order_document_path").toString().equals("-")? "https://apolcms.ap.gov.in/"+entry.get("order_document_path").toString():"");
							
							finalOrdersArray.put(obj);
						}
					}
					
					casesData.put("FINAL_ORDERS_LIST", finalOrdersArray);
					
					
					String finalString = casesData.toString();

					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases details retrieved successfully\"  , "
								+ finalString.substring(1, finalString.length() - 1) + "}}";
					
					
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