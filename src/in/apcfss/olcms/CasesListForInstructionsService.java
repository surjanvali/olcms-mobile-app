package in.apcfss.olcms;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displayCaseFilters")
	public static Response getCasesList(String incomingData) throws Exception {
		System.out.println("CasesListForInstructionsService..............................getCasesList()");
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

					if (!jObject.has("SELECTED_YEAR") || jObject.get("SELECTED_YEAR").toString().equals("")) {
						ArrayList selectData = new ArrayList();
						for (int i = 2022; i > 1980; i--) {
							selectData.add(i);
						}

						JSONObject response = new JSONObject();
						response.put("yearsList", new JSONArray(selectData));
						jsonStr = "{\"RESPONSE\" : " + response.toString() + "}";
					} else if (jObject.has("SELECTED_YEAR") && !jObject.get("SELECTED_YEAR").toString().equals("")) {
						sqlCondition += " and reg_year='" + jObject.get("SELECTED_YEAR").toString() + "' ";

						if (!roleId.equals("2")) { // District Nodal Officer
							sqlCondition += " and dept_code='" + deptCode + "' ";
						}
						if (roleId.equals("2")) { // District Collector
							sqlCondition += "  and dist_id='" + distId + "'";// and case_status=7
						} else if (roleId.equals("10")) { // District Nodal Officer
							sqlCondition += " and dist_id='" + distId + "'";// and case_status=8
						}

						if (jObject.has("REG_TYPE") && !jObject.get("REG_TYPE").toString().equals("")) {
							sqlCondition += " and type_name_reg ='" + jObject.get("REG_TYPE").toString() + "' ";
							sql = "select cino,concat(type_name_reg,'/',reg_no,'/',reg_year) as case_id from ecourts_case_data where coalesce(ecourts_case_status,'')!='Closed' "
									+ sqlCondition + "";
							JSONObject response2 = new JSONObject();
							response2.put("selected_year", jObject.get("SELECTED_YEAR").toString());
							response2.put("selected_case_type", jObject.get("REG_TYPE").toString());
							List<String> data = DatabasePlugin.executeQuery(sql, con);

							response2.put("case_details", new JSONArray(data));
							jsonStr = "{\"RESPONSE\" : " + response2.toString() + "}";

						}

						else {
							sql = " select distinct type_name_reg from ecourts_case_data where coalesce(ecourts_case_status,'')!='Closed' "
									+ sqlCondition + "";
							System.out.println("FINAL SQL:" + sql);
							List<String> data = DatabasePlugin.executeQuery(sql, con);

							JSONObject response1 = new JSONObject();
							response1.put("selected_year", jObject.get("SELECTED_YEAR").toString());

							response1.put("case_types", new JSONArray(data));
							jsonStr = "{\"RESPONSE\" : " + response1.toString() + "}";

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
	
}
	