package in.apcfss.olcms;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import in.apcfss.util.ECourtsCryptoHelper;
import in.apcfss.util.EHighCourtAPI;
import in.apcfss.util.HASHHMACJava;
import plugins.DatabasePlugin;

/**
 * @author : Bhanu Krishna Kota
 * @title :
 * 
 *        PRD URL :
 *        https://apolcms.ap.gov.in/apolcms-services/services/getCasesList/displayCaseFilters
 *        TEST URL :
 *        http://localhost:9090/apolcms-services/services/getCasesList/displayCaseFilters
 * 
 *        {"REQUEST" : {"USER_ID":"RAMESH.DAMMU@APCT.GOV.IN", "ROLE_ID":"5","DEPT_CODE":"REV03", "DIST_ID":"0","SELECTED_YEAR":"2022", "REG_TYPE":"WP"}} 
 *        
 **/

@Path("/getCasesList")
public class CasesListForInstructionsService {
	/*
	 * @POST
	 * 
	 * @Produces({ "application/json" })
	 * 
	 * @Consumes({ "application/json" })
	 * 
	 * @Path("/displayCaseFilters") public static Response getCasesList(String
	 * incomingData) throws Exception { System.out.println(
	 * "CasesListForInstructionsService..............................getCasesList()"
	 * ); Connection con = null; String jsonStr = ""; try { if (incomingData != null
	 * && !incomingData.toString().trim().equals("")) { JSONObject jObject1 = new
	 * JSONObject(incomingData);
	 * 
	 * System.out.println("jObject1:" + jObject1);
	 * 
	 * JSONObject jObject = new
	 * JSONObject(jObject1.get("REQUEST").toString().trim());
	 * System.out.println("jObject:" + jObject);
	 * 
	 * if (!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals(""))
	 * { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}"
	 * ; } else if (!jObject.has("ROLE_ID") ||
	 * jObject.get("ROLE_ID").toString().equals("")) { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- ROLE_ID is missing in the request.\" }}"
	 * ; } else if (!jObject.has("DEPT_CODE") ||
	 * jObject.get("DEPT_CODE").toString().equals("")) { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- DEPT_CODE is missing in the request.\" }}"
	 * ; } else if (!jObject.has("DIST_ID") ||
	 * jObject.get("DIST_ID").toString().equals("")) { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Mandatory parameter- DIST_ID is missing in the request.\" }}"
	 * ; } else {
	 * 
	 * String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode =
	 * "";
	 * 
	 * roleId = jObject.get("ROLE_ID").toString(); deptCode =
	 * jObject.get("DEPT_CODE").toString(); distId =
	 * jObject.get("DIST_ID").toString(); con = DatabasePlugin.connect();
	 * 
	 * if (!jObject.has("SELECTED_YEAR") ||
	 * jObject.get("SELECTED_YEAR").toString().equals("")) { ArrayList selectData =
	 * new ArrayList(); for (int i = 2022; i > 1980; i--) { selectData.add(i); }
	 * 
	 * JSONObject response = new JSONObject(); response.put("yearsList", new
	 * JSONArray(selectData)); jsonStr = "{\"RESPONSE\" : " + response.toString() +
	 * "}"; } else if (jObject.has("SELECTED_YEAR") &&
	 * !jObject.get("SELECTED_YEAR").toString().equals("")) { sqlCondition +=
	 * " and reg_year='" + jObject.get("SELECTED_YEAR").toString() + "' ";
	 * 
	 * if (!roleId.equals("2")) { // District Nodal Officer sqlCondition +=
	 * " and dept_code='" + deptCode + "' "; } if (roleId.equals("2")) { // District
	 * Collector sqlCondition += "  and dist_id='" + distId + "'";// and
	 * case_status=7 } else if (roleId.equals("10")) { // District Nodal Officer
	 * sqlCondition += " and dist_id='" + distId + "'";// and case_status=8 }
	 * 
	 * if (jObject.has("REG_TYPE") &&
	 * !jObject.get("REG_TYPE").toString().equals("")) { sqlCondition +=
	 * " and type_name_reg ='" + jObject.get("REG_TYPE").toString() + "' "; sql =
	 * "select cino,concat(type_name_reg,'/',reg_no,'/',reg_year) as case_id from ecourts_case_data where coalesce(ecourts_case_status,'')!='Closed' "
	 * + sqlCondition + ""; JSONObject response2 = new JSONObject();
	 * response2.put("selected_year", jObject.get("SELECTED_YEAR").toString());
	 * response2.put("selected_case_type", jObject.get("REG_TYPE").toString());
	 * List<String> data = DatabasePlugin.executeQuery(sql, con);
	 * 
	 * response2.put("case_details", new JSONArray(data)); jsonStr =
	 * "{\"RESPONSE\" : " + response2.toString() + "}";
	 * 
	 * }
	 * 
	 * else { sql =
	 * " select distinct type_name_reg from ecourts_case_data where coalesce(ecourts_case_status,'')!='Closed' "
	 * + sqlCondition + ""; System.out.println("FINAL SQL:" + sql); List<String>
	 * data = DatabasePlugin.executeQuery(sql, con);
	 * 
	 * JSONObject response1 = new JSONObject(); response1.put("selected_year",
	 * jObject.get("SELECTED_YEAR").toString());
	 * 
	 * response1.put("case_types", new JSONArray(data)); jsonStr =
	 * "{\"RESPONSE\" : " + response1.toString() + "}";
	 * 
	 * } } } } else { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Input Data.\" }}"
	 * ; }
	 * 
	 * } catch (Exception e) { jsonStr =
	 * "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}"
	 * ; // conn.rollback(); e.printStackTrace();
	 * 
	 * } finally { if (con != null) con.close(); } return
	 * Response.status(200).entity(jsonStr).build(); }
	 */
	
	

	/**
	 * @author : Bhanu Krishna Kota
	 * @title :
	 * @Description : This method retrives the case details from the Ecourts API call for the corresponding cino.
	 *        PRD URL :
	 *        https://apolcms.ap.gov.in/apolcms-services/services/getCasesList/viewSelectedCaseDetails
	 *        TEST URL :
	 *        http://localhost:9090/apolcms-services/services/getCasesList/viewSelectedCaseDetails
	 * 
	 *        {"REQUEST" : {"CINO":"APHC010149352022"}} 
	 *        
	 **/
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/viewSelectedCaseDetails")
	public static Response getCaseDetails(String incomingData) throws Exception {

		String request_token = "", requeststring = "";
		String inputStr = "", targetURL = "";
		String authToken = "";
		Connection con = null;
		String jsonStr = "";

		int totalCount = 0;

		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);

				JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
				System.out.println("jObject:" + jObject);

				if (!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- Cino is missing in the request.\" }}";
				} else {

					inputStr = "cino=" + jObject.get("CINO").toString();// ECourtAPIs.getInputStringValue(opVal);

					// 1. Encoding Request Token
					byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"),
							inputStr.getBytes("UTF-8"));
					request_token = String.format("%032x", new BigInteger(1, hmacSha256));
					// 2. Encoding Request String
					requeststring = URLEncoder.encode(ECourtsCryptoHelper.encrypt(inputStr.getBytes()), "UTF-8");

					targetURL = "https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-CNRSearch/v1.0/cnrFullCaseDetails"
							+ "?dept_id=SE00031&request_str=" + requeststring + "&request_token=" + request_token
							+ "&version=v1.0";

					System.out.println("Target URL : " + targetURL);
					System.out.println("Input String : " + inputStr);

					authToken = EHighCourtAPI.getAuthToken();
					String resp = "";

					try {
						resp = EHighCourtAPI.sendGetRequest(targetURL, authToken);
					} catch (Exception e) {
						e.printStackTrace();
					}

					
					if (resp != null && !resp.equals("") && (!resp.contains("INVALID_TOKEN"))) {
						try {
							String response_str = "", decryptedRespStr = "";
							JSONObject jObj = new JSONObject(resp);
							if ((jObj.has("response_str")) && (jObj.getString("response_str") != null)) {
								response_str = jObj.getString("response_str").toString();
							}

							if ((response_str != null) && (!response_str.equals(""))) {
								decryptedRespStr = ECourtsCryptoHelper.decrypt(response_str.getBytes());
							}
							System.out.println("decryptedRespStr:" + decryptedRespStr);

							JSONObject jObjCaseData = new JSONObject(decryptedRespStr);

							jsonStr = "{\"RESPONSE\" : " + jObjCaseData.toString() + "}";
						} catch (Exception e) {
							e.printStackTrace();
						}
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
	@Path("/displayCases")
	public static Response displayCasesList(String incomingData) throws Exception {

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
						sqlCondition +=" and dept_code='" + deptCode + "' ";
					}

					if(roleId.equals("2") || roleId.equals("12")) { //District Collector

						sqlCondition +="  and dist_id='"+distId+"'";//and case_status=7
					}
					else if(roleId.equals("10")) { //District Nodal Officer
						sqlCondition +=" and dist_id='"+distId+"'";// and case_status=8
					}
					else if(roleId.equals("5") || roleId.equals("9")) {//NO & HOD
						//sqlCondition +=" and case_status in (3,4)";
					}
					else if(roleId.equals("3") || roleId.equals("4")) {//MLO & Sect. Dept.
						//sqlCondition +=" and (case_status is null or case_status in (1, 2))";
					}
					else if(roleId.equals("8") || roleId.equals("11") || roleId.equals("12")) {
						sqlCondition +="  and assigned_to='"+userid+"'";
					}

					
					sql= " select a.* from ecourts_case_data a where coalesce(ecourts_case_status,'')!='Closed' "+sqlCondition+" order by 1";

					System.out.println("Final SQL:" + sql);
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray finalList = new JSONArray();
					// System.out.println("data=" + data);
					if (data != null && !data.isEmpty() && data.size() > 0) {
					
						for (Map<String, Object> entry : data) {		
						    
						    	JSONObject cases = new JSONObject();
						    	cases.put("cino", entry.get("cino").toString());
						    	cases.put("date_of_filing", entry.get("date_of_filing").toString());						    	
						    	cases.put("type_name_fil", entry.get("type_name_fil").toString());
						    	cases.put("reg_no", entry.get("reg_no").toString());
						    	cases.put("reg_year", entry.get("reg_year").toString());
						    	cases.put("pet_name", entry.get("pet_name").toString());
						    	cases.put("dist_name", entry.get("dist_name").toString());
						    	cases.put("purpose_name", entry.get("purpose_name").toString());
						    	cases.put("res_name", entry.get("res_name").toString());
						    	cases.put("pet_adv", entry.get("pet_adv").toString());
						    	cases.put("res_adv", entry.get("res_adv").toString());
						    	finalList.put(cases);
						}
						JSONObject casesData = new JSONObject();
						casesData.put("CASES_DATA", finalList);
						String finalString = casesData.toString();
						    
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"OK\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
						
					} else {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\" }}";
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
	@Path("/displayCaseFilters")
	public static Response getCasesListFilters(String incomingData) throws Exception {
		System.out.println("CasesListForInstructionsService...........START...................getCasesListFilters()");
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

					String sql = null, sqlCondition = "", roleId = "", distId = "", deptCode = "";

					roleId = jObject.get("ROLE_ID").toString();
					deptCode = jObject.get("DEPT_CODE").toString();
					distId = jObject.get("DIST_ID").toString();
					con = DatabasePlugin.connect();

					/* START - Code to populate the registered years select box */
						List selectData = new ArrayList();
						
						
						for (int i = 2022; i > 1980; i--) {
							LinkedHashMap<String,Integer> hm=new LinkedHashMap<String,Integer>();
							hm.put("year", i);
							selectData.add(hm);
						}

						JSONObject response = new JSONObject();
						response.put("years_list", new JSONArray(selectData));
					/* END - Code to populate the registered years select box */
						
						
					/* START - Code to populate the purpose name select box */
						sql= "select purpose_name from apolcms.ecourts_case_data where dept_code='"
								+ deptCode	 + "' group by purpose_name order by 1";

						System.out.println("Purpose query SQL:" + sql);
						List<String> purposeData = DatabasePlugin.executeQuery(sql, con);
						response.put("purpose_list", new JSONArray(purposeData));
					/* END - Code to populate the purpose name select box */	
						
						
					/* START - Code to populate the district name select box */
						sql= "select trim(dist_name) as district_name from apolcms.ecourts_case_data where trim(dist_name)!='null' group by trim(dist_name) order by 1";
						System.out.println("District query SQL:" + sql);
						List<String> districtData = DatabasePlugin.executeQuery(sql, con);
						response.put("district_list", new JSONArray(districtData));
					/* END - Code to populate the district name select box */
						
						String finalString = response.toString();
						
						if(finalString!=null & !finalString.equals("")) {
							finalString = finalString.substring(1,finalString.length()-1);
						}
						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"OK\"  , \"RSPDESC\" :\"Case Filters retrieved successfully\"," + finalString + "}}";
						System.out.println("CasesListForInstructionsService...........END...................getCasesListFilters()");	
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
	