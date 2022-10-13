package in.apcfss.olcms;

import java.sql.Connection;
import java.util.ArrayList;
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
						sql = "select sno,case_full_name from case_type_master order by sno";
	
						List<Map<String, Object>> data  = DatabasePlugin.executeQuery(sql, con);
	
						JSONArray caseTypeList = new JSONArray();
	
						if (data != null && !data.isEmpty() && data.size() > 0) {
	
							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("SNO", entry.get("sno").toString());
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
	
	
}