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
	
}