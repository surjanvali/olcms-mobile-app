package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import plugins.DatabasePlugin;

/**
 * @author : Surjan Vali - APCFSS
 * @title :
 * 
 *        URL :
 *        https://aprcrp.apcfss.in/aprcrp-services/services/APRCRPAbstracts/showAbstracts
 * 
 *        URL :
 *        http://localhost:8080/aprcrp-services/services/APRCRPAbstracts/showAbstracts
 * 
 *        {"REQUEST" : {"DIST":"","CIRCLE":"","FROMDATE" : "","TODATE" : ""}}
 *        {"RESPONSE" :
 *        [{"APLTYPE":"","TOTAL":"","APPROVED":"","PENDING":"","REJECTED":""}]}
 **/

@Path("/servicesMenu")
public class ServicesMenu {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/showMenuItems")
	public static Response showMenuItems(String incomingData) throws Exception {
		Connection conn = null;
		String jsonStr = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());

					//Class.forName("org.postgresql.Driver");
					conn = DatabasePlugin.connect(); //DriverManager.getConnection(CommonVariables.dataBase, CommonVariables.userName,CommonVariables.password);

					Statement stmt = conn.createStatement();

					String sql = "SELECT permission_type,count(*) as total_applied,"
							+ "sum(case when application_status=13 then 1 else 0 end) as total_approved,"
							+ "sum(case when (application_status=7 or application_status=8 or application_status=9) then 1 else 0 end) as total_rejected,"
							+ "sum(case when (application_status not in (7,8,9,13)) then 1 else 0 end) as under_process"
							+ " FROM public.aprcrp_permission_applications a inner join aprcrp_circles_master cm on (a.circle_id=cm.circle_id) where 1=1";

					sql = "SELECT permission_type,count(*) as total_applied, sum(case when application_status in (11,12,13,15) then 1 else 0 end) as total_approved, "
							+ "sum(case when application_status in (7,8,9) then 1 else 0 end) as total_rejected, sum(case when application_status not in (7,8,9,11,12,13,15) then 1 else 0 end) as under_process "
							+ "FROM public.aprcrp_permission_applications a inner join aprcrp_circles_master cm on (a.circle_id=cm.circle_id) "
							+ "inner join aprcrp_divisions_master dm on (a.circle_id=dm.circle_id and a.division_code=dm.division_id) "
							+ "inner join district_master dsm on (cm.district_id=dsm.dist_code) inner join aprcrp_application_status_master sm on (a.application_status=sm.status_code) "
							+ "inner join aprcrp_consumer_accounts ca on (a.submitted_by=ca.login_id)  "
							+ "where permission_type ='OFC CABLES' ";

					if (jObject.has("DIST") && jObject.get("DIST") != null && !jObject.get("DIST").toString().equals("")
							&& !jObject.get("DIST").toString().equals("0")
							&& !jObject.get("DIST").toString().equals("ALL")) {
						sql += " and cm.district_id='" + jObject.get("DIST") + "' ";
					}
					if (jObject.has("CIRCLE") && jObject.get("CIRCLE") != null
							&& !jObject.get("CIRCLE").toString().equals("")
							&& !jObject.get("CIRCLE").toString().equals("0")
							&& !jObject.get("CIRCLE").toString().equals("ALL")) {
						sql += " and circle_id='" + jObject.get("CIRCLE") + "' ";
					}

					if (jObject.has("FROMDATE") && jObject.get("FROMDATE") != null
							&& !jObject.get("FROMDATE").toString().equals("")) {
						sql += " and submitted_on::date >= to_date('" + jObject.get("FROMDATE") + "','dd-mm-yyyy')";
					}

					if (jObject.has("TODATE") && jObject.get("TODATE") != null
							&& !jObject.get("TODATE").toString().equals("")) {
						sql += " and submitted_on::date <= to_date('" + jObject.get("TODATE") + "','dd-mm-yyyy')";
					}

					sql += " group by permission_type order by permission_type";
					System.out.println("SQL:" + sql);
					ResultSet rs = stmt.executeQuery(sql);
					jsonStr = "{\"RESPONSE\" : [";
					while (rs.next()) {
						jsonStr += "{\"APLTYPE\":\"" + rs.getString("permission_type") + "\",\"TOTAL\":\""
								+ rs.getString("total_applied") + "\",\"APPROVED\":\"" + rs.getString("total_approved")
								+ "\",\"PENDING\":\"" + rs.getString("under_process") + "\",\"REJECTED\":\""
								+ rs.getString("total_rejected") + "\"},";
					}
					jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
					jsonStr += "]}";
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			// conn.rollback();
			e.printStackTrace();

		} finally {
			if (conn != null)
				conn.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
}
