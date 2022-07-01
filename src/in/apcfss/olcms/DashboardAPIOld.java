package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
 *        PRD URL :
 *        https://aprcrp.apcfss.in/apolcms-services/services/userdashboard/viewDashboard
 *        TEST URL :
 *        http://localhost:9090/apolcms-services/services/userdashboard/viewDashboard
 * 
 *        {"REQUEST" : {"USERID":"DC-ATP", "ROLE_ID":"", "DEPT_CODE":"", "DIST_ID":""}} 
 *        {"RESPONSE" : {"TOTAL":"","ASSIGNMENT_PENDING":"",  "APPROVAL_PENDING":"","CLOSED":"","NEWCASES":"", "FINAL_ORDERS":"", "INTERIM_CASES":"", "INTERIM_ORDERS":""}}
 **/

@Path("/userdashboardOld")
public class DashboardAPIOld {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/viewDashboard")
	public static Response viewDashboard(String incomingData) throws Exception {
		Connection conn = null;
		String jsonStr = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);

					String deptCode = jObject.get("DEPT_CODE").toString();

					// System.out.println("USERID:"+jObject.get("USERID"));
					// System.out.println("DEPT_CODE:"+jObject.get("DEPT_CODE"));

					conn = DatabasePlugin.connect();

					String sql = "";

					sql = " select count(*) as total, "
							+ " sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
							+ " sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases  from ecourts_case_data ";
					System.out.println("sql--" + sql);
					// ResultSet rs = stmt.executeQuery(sql);
					List rs = DatabasePlugin.executeQuery(sql, conn);

					Long total = (Long) ((Map) rs.get(0)).get("total");
					String total_value = Long.toString(total);
					Long assignment_pending = (Long) ((Map) rs.get(0)).get("assignment_pending");
					String assignment_value = Long.toString(assignment_pending);
					Long closedcases = (Long) ((Map) rs.get(0)).get("closedcases");
					String closedcases_value = Long.toString(closedcases);

					sql = "select count(*) as new_cases  from ecourts_gpo_ack_dtls where ack_type='NEW'";
					// System.out.println("sql--"+sql);
					List rs1 = DatabasePlugin.executeQuery(sql, conn);

					Long new_cases = (Long) ((Map) rs1.get(0)).get("new_cases");
					String new_cases_value = Long.toString(new_cases);

					sql = "select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder";
					// System.out.println("sql--"+sql);
					List rs2 = DatabasePlugin.executeQuery(sql, conn);
					// System.out.println("rs2--"+rs2);
					Long orders = (Long) ((Map) rs2.get(0)).get("orders");

					String order_value = Long.toString(orders);

					sql = "select count(distinct cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders_data from ecourts_case_interimorder";
					System.out.println("sql--" + sql);
					// ResultSet rs3 = stmt.executeQuery(sql);
					List rs3 = DatabasePlugin.executeQuery(sql, conn);

					String order_data[] = ((Map<String, String>) rs3.get(0)).get("orders_data").split(",");
					// System.out.println("order_data--"+order_data);

					sql = "select "
							+ " sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end) as disposed,"
							+ " sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end) as allowed,"
							+ " sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end) as dismissed ,"
							+ " sum(case when disposal_type='WITHDRAWN' then 1 else 0 end) as withdrawn,"
							+ " sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end) as closed,"
							+ " sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end) as returned"
							+ " from ecourts_case_data a " + " inner join dept_new d on (a.dept_code=d.dept_code) "
							+ " where d.display = true and (reporting_dept_code='" + deptCode + "' or a.dept_code='"
							+ deptCode + "') ";

					System.out.println("sql--" + sql);

					List rs4 = DatabasePlugin.executeQuery(sql, conn);

					Long disposed = (Long) ((Map) rs4.get(0)).get("disposed");
					String disposed_value = Long.toString(disposed);
					Long allowed = (Long) ((Map) rs4.get(0)).get("allowed");
					String allowed_value = Long.toString(allowed);
					Long dismissed = (Long) ((Map) rs4.get(0)).get("dismissed");
					String dismissed_value = Long.toString(dismissed);
					Long withdrawn = (Long) ((Map) rs4.get(0)).get("withdrawn");
					String withdrawn_value = Long.toString(withdrawn);
					Long closed = (Long) ((Map) rs4.get(0)).get("closed");
					String closed_value = Long.toString(closed);
					Long returned = (Long) ((Map) rs4.get(0)).get("returned");
					String returned_value = Long.toString(returned);

					if ((rs != null && !rs.isEmpty() && rs.size() > 0)
							&& (rs1 != null && !rs1.isEmpty() && rs1.size() > 0)
							&& (rs2 != null && !rs2.isEmpty() && rs2.size() > 0)
							&& (rs3 != null && !rs3.isEmpty() && rs3.size() > 0)
							&& (rs4 != null && !rs4.isEmpty() && rs4.size() > 0)) {

						jsonStr = "{\"RESPONSE\" : ";

						jsonStr += "{\"TOTAL\":\"" + total_value + "\", \"ASSIGNMENT_PENDING\":\"" + assignment_value
								+ "\", \"CLOSED\":\"" + closedcases_value + "\"  , " + " \"NEWCASES\":\""
								+ new_cases_value + "\", \"INTERIM_CASES\":\"" + order_data[0]
								+ "\" ,\"INTERIM_ORDERS\":\"" + order_data[1] + "\",\"FINAL_ORDERS\":\"" + order_value
								+ "\" ,\"DISPOSED\":\"" + disposed_value + "\", \"ALLOWED\":\"" + allowed_value
								+ "\" ,\"DISMISSED\":\"" + dismissed_value + "\",\"WITHDRAWN\":\"" + withdrawn_value
								+ "\" ,\"CLOSED\":\"" + closed_value + "\" ,\"RETURNED\":\"" + returned_value
								+ "\", \"RSPCODE\": \"01\",\"RSPDESC\": \"SUCCESS\"  }";
					}
					// jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
					jsonStr += "}";

					System.out.println("jsonStr--" + jsonStr);

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
