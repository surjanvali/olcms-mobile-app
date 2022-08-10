package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

/**
 * @author : Bhanu Krishna Kota
 * @title :
 * 
 *       
 *	
 **/

@Path("/reports")
public class OfficersRegistered {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/officersRegistered")
	public static Response submitInstructions(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";PreparedStatement ps = null;
		int a=0;String uploadedFilePath=null;
		JSONObject userDetails = new JSONObject();
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);
					
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
					else if(!jObject.has("OFFICER_TYPE") || jObject.get("OFFICER_TYPE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- OFFICER_TYPE is missing in the request.\" }}";
					}
					
					else {

					
					String dept_code=jObject.get("DEPT_CODE").toString();
					String dist_id = jObject.get("DIST_ID").toString();
					String user_id = jObject.get("USER_ID").toString();
					String roleId = jObject.get("ROLE_ID").toString();
					String officerType = jObject.get("OFFICER_TYPE").toString();
					
					con = DatabasePlugin.connect();

					//System.out.println("USERID:"+jObject.get("USERID"));
					
					if (officerType.equals("DNO")) {
						String tableName = "";
						
						if (!jObject.has("SELECTED_DIST_ID") || jObject.get("SELECTED_DIST_ID").toString().equals("")) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- SELECTED_DIST_ID is missing in the request.\" }}";
							return Response.status(200).entity(jsonStr).build();
						}
						else {
							String selectedDistId = "";
							selectedDistId = jObject.get("SELECTED_DIST_ID").toString();
							tableName = getTableName(selectedDistId, con);

							sql = "select m.dept_id,upper(d.description) as description,trim(nd.fullname_en) as fullname_en, trim(nd.designation_name_en) as designation_name_en,m.mobileno,m.emailid from nodal_officer_details m "
									+ "inner join (select distinct employee_id,fullname_en,designation_name_en, designation_id from "
									+ tableName
									+ ") nd on (m.employeeid=nd.employee_id and m.designation=nd.designation_id)"
									+ "inner join users u on (m.emailid=u.userid)"
									+ "inner join dept_new d on (m.dept_id=d.dept_code)" + "where m.dist_id='"
									+ selectedDistId + "' order by 1";
						}
					} else if (officerType.equals("NO")) {

						sql = "select m.dept_id,upper(d.description) as description,trim(nd.fullname_en) as fullname_en, trim(nd.designation_name_en) as designation_name_en,m.mobileno,m.emailid from nodal_officer_details m "
								+ "    inner join (select distinct employee_id,fullname_en,designation_name_en from nic_data) nd on (m.employeeid=nd.employee_id)"
								+ "    inner join users u on (m.emailid=u.userid)"
								+ "    inner join dept_new d on (m.dept_id=d.dept_code)"
								+ "    where m.inserted_by ilike '%01'" + "    order by 1";

					} else if (officerType.equals("MLOSUBJECT")) {

						sql = "select d.dept_code as dept_id,upper(d.description) as description,b.fullname_en, b.designation_name_en,m.mobileno,m.emailid from mlo_subject_details m "
								+ " inner join (select distinct employee_id,fullname_en,designation_id, designation_name_en from nic_data) b"
								+ " on (m.employeeid=b.employee_id and m.designation=b.designation_id)"
								+ " inner join users u on (m.emailid=u.userid)"
								+ " inner join dept_new d on (m.user_id=d.dept_code) order by 1";

					} else {
						sql = "select d.dept_code as dept_id,upper(d.description) as description,b.fullname_en, b.designation_name_en,m.mobileno,m.emailid from mlo_details m "
								+ "inner join (select distinct employee_id,fullname_en,designation_id, designation_name_en from nic_data) b on (m.employeeid=b.employee_id and m.designation=b.designation_id)"
								+ "inner join users u on (m.emailid=u.userid)"
								+ "inner join dept_new d on (m.user_id=d.dept_code)" + "order by 1";

					}
					
					System.out.println("FINAL SQL:" + sql);

					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
					
					JSONArray finalList = new JSONArray();
					JSONObject casesData = new JSONObject();
					boolean isDataAvailable = false;

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DEPT_CODE", entry.get("dept_id"));
							cases.put("DEPT_NAME", entry.get("description"));
							cases.put("EMP_NAME", entry.get("fullname_en"));
							cases.put("DESIGNATION", entry.get("designation_name_en"));
							cases.put("MOBILE", entry.get("mobileno"));
							cases.put("EMAIL", entry.get("emailid"));				
							
							
							finalList.put(cases);
						}

						casesData.put("OFFICERS_LIST", finalList);
						isDataAvailable = true;

					} else {

						casesData.put("OFFICERS_LIST", finalList);

					}

					String finalString = casesData.toString();

					if (isDataAvailable)
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Details retrived successfully\"  , "
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
	
	
	
	
	public static String getTableName(String distId, Connection con) {
		String tableName = "nic_data";
		if(distId!=null && !distId.equals("") && Integer.parseInt(distId) > 0)
			tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst where district_id="+distId, con);
			// tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst_new where district_id="+distId, con);
		System.out.println("dist::Id"+distId+"-tableName::"+tableName);
		return tableName;
	}
}
