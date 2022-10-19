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


@Path("/submitInstructionsService")
public class NewInstructionService {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displaySelectionFiltersForLegacy")
	public static Response displaySelectionFiltersForLegacy(String incomingData) throws Exception {
		
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
				} else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "";

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					con = DatabasePlugin.connect();
					
					
					if (roleId.equals("2") || roleId.equals("12")) { // District Collector

						sqlCondition += "  and distid='" + distId + "'";// and case_status=7
					} else if (roleId.equals("10")) { // District Nodal Officer
						sqlCondition += " and distid='" + distId + "' and dept_code='" + deptCode + "'    ";																											
					}
					if (roleId.equals("5") || roleId.equals("9")) {// NO & HOD
						sqlCondition += " and dept_code='" + deptCode + "' ";
					} else if (roleId.equals("3") || roleId.equals("4")) {// MLO & Sect. Dept.
						sqlCondition += " and dept_code='" + deptCode + "' ";
					} else if (roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
						sqlCondition += "  and assigned_to='" + userId + "'";
					}
					
					
										
					JSONObject casesData = new JSONObject();

					//START:Populate the Case Types selection for the Legacy cases//
						sql = "select upper(trim(case_short_name)) as sno,upper(trim(case_short_name)) as case_full_name from case_type_master order by sno";
	
						List<Map<String, Object>> data  = DatabasePlugin.executeQuery(sql, con);
	
						JSONArray caseTypeList = new JSONArray();
	
						if (data != null && !data.isEmpty() && data.size() > 0) {
	
							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("CASE_TYPE", entry.get("case_full_name").toString());
	
								caseTypeList.put(cases);
							}
						}
						casesData.put("CASE_TYPES_LIST", caseTypeList);
					//END:Populate the Case Types selection for the Legacy cases//
						
						
					/* START - Code to populate the registered years select box */
						List selectData = new ArrayList();
						
						
						for (int i = 2022; i > 1980; i--) {
							LinkedHashMap<String,Integer> hm=new LinkedHashMap<String,Integer>();
							hm.put("YEAR", i);
							selectData.add(hm);
						}

						casesData.put("YEARS_LIST", new JSONArray(selectData));
					/* END - Code to populate the registered years select box */
					
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Legacy Cases retrived successfully\"  , "
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
	@Path("/displaySelectionFiltersForNew")
	public static Response displaySelectionFiltersForNew(String incomingData) throws Exception {
		
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
				} else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "";

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					con = DatabasePlugin.connect();
					
					
					if (roleId.equals("2") || roleId.equals("12")) { // District Collector
						sqlCondition += "  and distid='" + distId + "'";// and case_status=7
					} else if (roleId.equals("10")) { // District Nodal Officer
						sqlCondition += " and distid='" + distId + "' and dept_code='" + deptCode + "'    ";																											
					}
					if (roleId.equals("5") || roleId.equals("9")) {// NO & HOD
						sqlCondition += " and dept_code='" + deptCode + "' ";
					} else if (roleId.equals("3") || roleId.equals("4")) {// MLO & Sect. Dept.
						sqlCondition += " and dept_code='" + deptCode + "' ";
					} else if (roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
						sqlCondition += "  and assigned_to='" + userId + "'";
					}
					
					
					JSONObject casesData = new JSONObject();

					sql="select b.ack_no,b.ack_no from ecourts_gpo_ack_dtls a "
							+ " inner join ecourts_gpo_ack_depts b on (a.ack_no=b.ack_no) where ack_type='NEW' and respondent_slno='1'  "+sqlCondition+"  order by b.ack_no";
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray casesList = new JSONArray();
					
					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("ACK_NO", entry.get("ack_no").toString());

							casesList.put(cases);
						}
					}
					casesData.put("CASES_LIST", casesList);
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"New case details retrived successfully\"  , "
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
				} else if (!jObject.has("CASE_TYPE") || jObject.get("CASE_TYPE").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CASE_TYPE is missing in the request.\" }}";
				}else if (!jObject.has("CASE_REG_YEAR") || jObject.get("CASE_REG_YEAR").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CASE_REG_YEAR is missing in the request.\" }}";
				}else if (!jObject.has("CASE_NO") || jObject.get("CASE_NO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- CASE_NO is missing in the request.\" }}";
				}
				else {

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", Condition = "", caseType = "", caseRegYear = "", caseNo = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					caseType = jObject.get("CASE_TYPE").toString();
					caseRegYear = jObject.get("CASE_REG_YEAR").toString();
					caseNo = jObject.get("CASE_NO").toString();
					
											

						if (caseType != null && !caseType.contentEquals("")
								&& !caseType.contentEquals("0")) {
							sqlCondition += " and a.type_name_reg='" + caseType.trim() + "' ";
						}

						if (caseNo != null && !caseNo.contentEquals("")
								&& !caseNo.contentEquals("0")) {
							sqlCondition += " and a.reg_no='" + caseNo.trim() + "' ";
						}

						if (caseRegYear != null && !caseRegYear.contentEquals("")
								&& !caseRegYear.contentEquals("0")) {
							sqlCondition += " and a.reg_year='" + caseRegYear.trim() + "' ";
						}


						if(roleId.equals("2") || roleId.equals("12")) { //District Collector

							sqlCondition +="  and a.dist_id='"+distId+"'";//and case_status=7
						}
						else if(roleId.equals("10")) { //District Nodal Officer
							sqlCondition +=" and a.dist_id='"+distId+"'";// and case_status=8
						}

						if(roleId.equals("5") || roleId.equals("9")) {//NO & HOD
							sqlCondition +=" and a.dept_code='" + deptCode + "' ";
						}
						else if(roleId.equals("3") || roleId.equals("4")) {//MLO & Sect. Dept.
							sqlCondition +=" and a.dept_code='" + deptCode + "' ";
						}
						else if(roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
							sqlCondition +="  and assigned_to='"+userId+"'";
						}
						
						sql = "select a.*, "
								+ " nda.fullname_en as fullname, nda.designation_name_en as designation, nda.post_name_en as post_name, nda.email, nda.mobile1 as mobile,dim.district_name , "
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
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) where d.display = true " + sqlCondition;
						
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
							cases.put("CURRENT_STATUS", entry.get("current_status")+" "+entry.get("fullname")+","+entry.get("designation")+","+entry.get("mobile")+","+entry.get("email")+","+entry.get("district_name"));
							
							cases.put("DATE_OF_FILING", entry.get("date_of_filing"));
							cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
							cases.put("PRAYER", entry.get("prayer_full"));
							cases.put("FIL_NO", entry.get("fil_no"));
							cases.put("FIL_YEAR", entry.get("fil_year"));
							cases.put("DATE_NEXT_LIST", entry.get("date_next_list"));
							cases.put("BENCH", entry.get("bench_name"));
							cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
							cases.put("PETITIONER_NAME", entry.get("pet_name"));
							cases.put("DIST_NAME", entry.get("dist_name"));
							cases.put("PURPOSE", entry.get("purpose_name"));
							cases.put("RESPONDENTS", entry.get("res_name")+","+entry.get("address"));
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

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "", userId = "", Condition = "", ackNo = "" ;

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					userId = jObject.get("USER_ID").toString();
					ackNo = jObject.get("ACK_NO").toString();
					
											
						if(roleId.equals("2") || roleId.equals("12")) { //District Collector

							Condition +="  and distid='"+distId+"'";//and case_status=7
						}
						else if(roleId.equals("10")) { //District Nodal Officer
							Condition +=" and distid='"+distId+"' and dept_code='" + deptCode + "'  ";// and case_status=8
						}

						if(roleId.equals("5") || roleId.equals("9")) {//NO & HOD
							Condition +=" and dept_code='" + deptCode + "' ";
						}
						else if(roleId.equals("3") || roleId.equals("4")) {//MLO & Sect. Dept.
							Condition +=" and dept_code='" + deptCode + "' ";
						}
						else if(roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
							Condition +="  and assigned_to='"+userId+"'";
						}
						
						sql = "select a.slno ,ad.respondent_slno, a.ack_no , distid , advocatename ,advocateccno , casetype , maincaseno , a.remarks ,  inserted_by , inserted_ip, upper(trim(district_name)) as district_name, "
								+ "upper(trim(case_full_name)) as  case_full_name, a.ack_file_path, case when services_id='0' then null else services_id end as services_id,services_flag, "
								+ "to_char(a.inserted_time,'dd-mm-yyyy') as generated_date, "
								+ "getack_dept_desc(a.ack_no::text) as dept_descs , coalesce(a.hc_ack_no,'-') as hc_ack_no "
								+ " from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls a on (ad.ack_no=a.ack_no) "
								+ " left join district_mst dm on (ad.dist_id=dm.district_id) "
								+ " left join dept_new dmt on (ad.dept_code=dmt.dept_code)"
								+ " inner join case_type_master cm on (a.casetype=cm.sno::text or a.casetype=cm.case_short_name)  "
								+ " where a.delete_status is false and ack_type='NEW'    and (a.ack_no='"+ackNo+"' or a.hc_ack_no='"+ackNo+"' )  and respondent_slno='1'   "
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
					    	cases.put("ACK_FILE_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("ack_file_path"));
					    	cases.put("BARCODE_FILE_PATH", "https://apolcms.ap.gov.in/uploads/scandocs/"+entry.get("barcode_file_path"));
					    	
							
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
	@Path("/getInstructionsHistoryForNew")
	public static Response getInstructionsHistoryForNew(String incomingData) throws Exception {

		Connection con = null;
		String jsonStr = "";

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("ACKNO") || jObject.get("ACKNO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ACKNO is missing in the request.\" }}";
				}  else {
					String sql = null, sqlCondition = "", roleId="", distId="", deptCode="", userid="",cino="",daily_status="";
					
					cino=jObject.get("ACKNO").toString();
					con = DatabasePlugin.connect();
					sql = "select instructions,to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time,coalesce(upload_fileno,'-') as upload_fileno from ecourts_dept_instructions where cino='" + cino + "'  order by 1 ";
					
					System.out.println("sql--" + sql);
					List<Map<String, Object>> instructionsHistory = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
					
					if (instructionsHistory != null && !instructionsHistory.isEmpty() && instructionsHistory.size() > 0) {
						
						for (Map<String, Object> entry : instructionsHistory) {		
						    
						    	JSONObject history = new JSONObject();
						    	history.put("INSTRUCTIONS", entry.get("instructions").toString());						    	
						    	
						    	history.put("SUBMITTED_TIME", entry.get("insert_time").toString());
						    	
						    	
						    	if(entry.get("upload_fileno") !=null && !entry.get("upload_fileno").toString().equals("-")) {
						    		history.put("UPLOADED_FILE_PATH", "https://apolcms.ap.gov.in/"+entry.get("upload_fileno").toString());
						    		
						    	} else {
						    		history.put("UPLOADED_FILE_PATH", "");
						    		
						    	}
						    	
						    	finalList.put(history);
						}
						JSONObject casesData = new JSONObject();
						casesData.put("INSTRUCTIONS_HISTORY", finalList);
						String finalString = casesData.toString();
						    
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Instructions history retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
						
					} else {
						JSONObject casesData = new JSONObject();
						casesData.put("INSTRUCTIONS_HISTORY", finalList);
						String finalString = casesData.toString();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+ " }}";
					}
					
					
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
	@Path("/getInstructionsHistoryForLegacy")
	public static Response getInstructionsHistoryForLegacy(String incomingData) throws Exception {

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
					sql = "select instructions,to_char(insert_time,'dd-mm-yyyy HH:mi:ss') as insert_time,coalesce(upload_fileno,'-') as upload_fileno from ecourts_dept_instructions where cino='" + cino + "'  order by 1 ";
					
					System.out.println("sql--" + sql);
					List<Map<String, Object>> instructionsHistory = DatabasePlugin.executeQuery(sql, con);
					JSONArray finalList = new JSONArray();
					
					if (instructionsHistory != null && !instructionsHistory.isEmpty() && instructionsHistory.size() > 0) {
						
						for (Map<String, Object> entry : instructionsHistory) {		
						    
						    	JSONObject history = new JSONObject();
						    	history.put("INSTRUCTIONS", entry.get("instructions").toString());						    	
						    	
						    	history.put("SUBMITTED_TIME", entry.get("insert_time").toString());
						    	
						    	
						    	if(entry.get("upload_fileno") !=null && !entry.get("upload_fileno").toString().equals("-")) {
						    		history.put("UPLOADED_FILE_PATH", "https://apolcms.ap.gov.in/"+entry.get("upload_fileno").toString());
						    		
						    	} else {
						    		history.put("UPLOADED_FILE_PATH", "");
						    		
						    	}
						    	
						    	finalList.put(history);
						}
						JSONObject casesData = new JSONObject();
						casesData.put("INSTRUCTIONS_HISTORY", finalList);
						String finalString = casesData.toString();
						    
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Instructions history retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
						
					} else {
						JSONObject casesData = new JSONObject();
						casesData.put("INSTRUCTIONS_HISTORY", finalList);
						String finalString = casesData.toString();
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+ " }}";
					}
					
					
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
	@Path("/submitInstructions")
	public Response submitInstructions(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataBodyPart body, @FormDataParam("cino") String cino,
			@FormDataParam("instructions") String instructions, @FormDataParam("deptCode") String deptCode,
			@FormDataParam("distCode") String distCode, @FormDataParam("roleId") String roleId,
			@FormDataParam("userId") String userId, @FormDataParam("oldNewType") String oldNewType) throws Exception {

		Connection con = null;
		PreparedStatement ps = null;
		String jsonStr = "";

		try {
				String newFileName="Instruction_"+CommonModels.randomTransactionNo()+"."+body.getMediaType().getSubtype();
				
				String uploadedFileLocation = "/app/tomcat9/webapps/apolcms/uploads/Instruction/"+newFileName;
				String fileUploadPath = "uploads/Instruction/"+newFileName;
				
				writeToFile(uploadedInputStream, uploadedFileLocation);
				
				String status_flag = "";
				
				if(roleId.equals("6")) {
					 status_flag="D";
				}else {
					 status_flag="I";
				}
				con = DatabasePlugin.connect();
				
				String sql = "insert into ecourts_dept_instructions (cino, instructions , upload_fileno,dept_code ,dist_code,insert_by,legacy_ack_flag,status_instruction_flag ) "
						+ " values (?,?, ?, ?, ?, ?,?,?)";

				ps = con.prepareStatement(sql);
				int i = 1;
				ps.setString(i, cino);
				ps.setString(++i, instructions != null ? instructions.toString() : "");
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
							+ " values ('" + cino + "','SUBMITTED INSTRUCTIONS TO GP', '"+userId+"', 'MOBILE APP', '"+instructions+"','"+fileUploadPath+"')";
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
	
}