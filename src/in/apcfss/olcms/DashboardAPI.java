package in.apcfss.olcms;

import java.util.List;
import java.sql.Connection;
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
 * @author : Surjan Vali - APCFSS
 * @title :
 * 
 *        PRD URL :
 *        https://aprcrp.apcfss.in/apolcms-services/services/userdashboard/viewDashboard
 *        TEST URL :
 *        http://localhost:9090/apolcms-services/services/userdashboard/viewDashboard
 * 
 *        {"REQUEST" : {"USERID":"DC-ATP", "ROLE_ID":"", "DEPT_CODE":"", "DIST_ID":""}}
		  {"RESPONSE" : {"TOTAL":"","ASSIGNMENT_PENDING":"", "APPROVAL_PENDING":"","CLOSED":"","NEWCASES":"", "FINAL_ORDERS":"", "INTERIM_CASES":"", "INTERIM_ORDERS":""}}		
 **/

@Path("/userdashboard")
public class DashboardAPI {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/viewDashboard")
	public static Response viewDashboard(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);

					String deptCode=jObject.get("DEPT_CODE").toString();
					String roleId=jObject.get("ROLE_ID").toString();
					String userid=jObject.get("USERID").toString();
					String distId=jObject.get("DIST_ID").toString();

					//System.out.println("USERID:"+jObject.get("USERID"));
					System.out.println("ROLE_ID:"+jObject.get("ROLE_ID"));


					//Class.forName("org.postgresql.Driver");
					con = DatabasePlugin.connect(); //DriverManager.getConnection(CommonVariables.dataBase, CommonVariables.userName, CommonVariables.password);
					//Statement stmt = conn.createStatement();
					//java.util.List data= new ArrayList();

					String sql="";

					
					String total_value="0",assignment_value="0",approval_pending_value="0",closedcases_value="0",new_cases_value="0",final_order_value="0",assignment_value_new_cases="0",approval_pending_value_new_cases="0";
					String  disposed_value="0",allowed_value="0",dismissed_value="0",withdrawn_value="0",closed_value="0",returned_value="0",assigned_value="0",counterfilecount_value="0",parawisecount_value="0";
					String str1="0",str2="0";
					String dailyStatusbyGP = "0", assigned_new_cases_value = "0",instruction_count ="0",totaldeptcases = "0";
					String yearWiseData = "\"GP_YEAR_WISECOUNT\" : 0";
					if(roleId!=null && !roleId.equals("")){
						
						if(roleId.equals("1") || roleId.equals("7")) { // ADMIN LOGIN
							
							
							sql="select count(*) as total, "
									+ " sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									//+ " sum(case when (case_status=7) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ " sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data ";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}
							
							
							sql="select count(*) from ecourts_gpo_ack_dtls where ack_type='NEW'";
							
							
							new_cases_value= DatabasePlugin.getStringfromQuery(sql, con);
							
							
							sql="select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder";
							final_order_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							
							sql="select count(distinct cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_interimorder";
							String interimOrderData = DatabasePlugin.getStringfromQuery(sql, con);
							
							if(interimOrderData != null && !interimOrderData.isEmpty())
							{
								String interimData[] = DatabasePlugin.getStringfromQuery(sql, con).split(",");
								str1=interimData[0];
								str2=interimData[1];
							}
							
							
							
							sql="select "
									+ " sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end) as disposed,"
									+ " sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end) as allowed,"
									+ " sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end) as dismissed ,"
									+ " sum(case when disposal_type='WITHDRAWN' then 1 else 0 end) as withdrawn,"
									+ " sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end) as closed,"
									+ " sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end) as returned"
									+ " from ecourts_case_data";
							
							
							
							List rs4 = DatabasePlugin.executeQuery(sql, con);


							Long disposed=(Long) ((Map) rs4.get(0)).get("disposed");
							disposed_value=Long.toString(disposed);
							Long allowed=(Long) ((Map) rs4.get(0)).get("allowed");
							allowed_value=Long.toString(allowed);
							Long dismissed=(Long) ((Map) rs4.get(0)).get("dismissed");
							dismissed_value=Long.toString(dismissed);
							Long withdrawn=(Long) ((Map) rs4.get(0)).get("withdrawn");
							withdrawn_value=Long.toString(withdrawn);
							Long closed=(Long) ((Map) rs4.get(0)).get("closed");
							closed_value=Long.toString(closed);
							Long returned=(Long) ((Map) rs4.get(0)).get("returned");
							returned_value=Long.toString(returned);
							
							
						} else if(roleId.equals("3") || roleId.equals("4")  || roleId.equals("5") || roleId.equals("9")) {
							
							if(roleId.equals("3") || roleId.equals("4")) {
								sql="select count(*) "
									+ "from ecourts_case_data a "
									+ "inner join dept_new d on (a.dept_code=d.dept_code) "
									+ "where d.display = true and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";
								
								
								totaldeptcases = DatabasePlugin.getStringfromQuery(sql, con);
								
								
							}							
							
							sql="select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder b inner join ecourts_case_data a on (a.cino=b.cino) "
									+ "inner join dept_new d on (a.dept_code=d.dept_code) "
									+ "where d.display = true and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";
							
							final_order_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select count(distinct a.cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_interimorder b "
									+ "inner join ecourts_case_data a on (a.cino=b.cino) "
									+ "inner join dept_new d on (a.dept_code=d.dept_code) "
									+ "where d.display = true and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";
							
							String interimOrderData = DatabasePlugin.getStringfromQuery(sql, con);
							
							if(interimOrderData != null && !interimOrderData.isEmpty())
							{
								String interimData[] = DatabasePlugin.getStringfromQuery(sql, con).split(",");
								str1=interimData[0];
								str2=interimData[1];
							}
							sql="select "
									+ " sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end) as disposed,"
									+ " sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end) as allowed,"
									+ " sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end) as dismissed ,"
									+ " sum(case when disposal_type='WITHDRAWN' then 1 else 0 end) as withdrawn,"
									+ " sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end) as closed,"
									+ " sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end) as returned"
									+ " from ecourts_case_data a "
									+ " inner join dept_new d on (a.dept_code=d.dept_code) "
									+ " where d.display = true and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";
							
							
							List rs4 = DatabasePlugin.executeQuery(sql, con);


							Long disposed=(Long) ((Map) rs4.get(0)).get("disposed");
							disposed_value=Long.toString(disposed);
							Long allowed=(Long) ((Map) rs4.get(0)).get("allowed");
							allowed_value=Long.toString(allowed);
							Long dismissed=(Long) ((Map) rs4.get(0)).get("dismissed");
							dismissed_value=Long.toString(dismissed);
							Long withdrawn=(Long) ((Map) rs4.get(0)).get("withdrawn");
							withdrawn_value=Long.toString(withdrawn);
							Long closed=(Long) ((Map) rs4.get(0)).get("closed");
							closed_value=Long.toString(closed);
							Long returned=(Long) ((Map) rs4.get(0)).get("returned");
							returned_value=Long.toString(returned);
							
							// Daily Status
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " inner join dept_new d on (a.dept_code=d.dept_code) "
									+ " where d.display = true and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
							
						}
						
						//District Collector
						if(roleId.equals("2")) {
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status=7) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=7) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dist_id='"+distId+"'";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}					
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=7) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=7) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ "  from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW'  and respondent_slno=1 and ad2.dist_id='"+distId+"'";
							
							dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							
							
							
							sql="select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder b inner join ecourts_case_data a on (a.cino=b.cino) where a.dist_id='"+distId+"' ";
							final_order_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select count(distinct b.cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_interimorder b inner join ecourts_case_data a on (a.cino=b.cino) where a.dist_id='"+distId+"' ";
							String interimOrderData = DatabasePlugin.getStringfromQuery(sql, con);
							
							if(interimOrderData != null && !interimOrderData.isEmpty())
							{
								String interimData[] = DatabasePlugin.getStringfromQuery(sql, con).split(",");
								str1=interimData[0];
								str2=interimData[1];
							}
							
							sql="select "
									+ " sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end) as disposed,"
									+ " sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end) as allowed,"
									+ " sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end) as dismissed ,"
									+ " sum(case when disposal_type='WITHDRAWN' then 1 else 0 end) as withdrawn,"
									+ " sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end) as closed,"
									+ " sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end) as returned"
									+ " from ecourts_case_data a "
									+ " inner join dept_new d on (a.dept_code=d.dept_code) "
									+ " where d.display = true and a.dist_id='"+distId+"' ";
							
							List rs4 = DatabasePlugin.executeQuery(sql, con);


							Long disposed=(Long) ((Map) rs4.get(0)).get("disposed");
							disposed_value=Long.toString(disposed);
							Long allowed=(Long) ((Map) rs4.get(0)).get("allowed");
							allowed_value=Long.toString(allowed);
							Long dismissed=(Long) ((Map) rs4.get(0)).get("dismissed");
							dismissed_value=Long.toString(dismissed);
							Long withdrawn=(Long) ((Map) rs4.get(0)).get("withdrawn");
							withdrawn_value=Long.toString(withdrawn);
							Long closed=(Long) ((Map) rs4.get(0)).get("closed");
							closed_value=Long.toString(closed);
							Long returned=(Long) ((Map) rs4.get(0)).get("returned");
							returned_value=Long.toString(returned);
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " inner join dept_new d on (a.dept_code=d.dept_code) "
									+ " where d.display = true and a.dist_id='"+distId+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
							
						} else if(roleId.equals("3")) { // Sect. Dept.
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status=1) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=1) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dept_code='"+deptCode+"'";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(totaldeptcases !="0")
							{
								total_value = totaldeptcases;
							}
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}	
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=1) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=1) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ " from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and ad2.dept_code='"+deptCode+"'";
							
							dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where dept_code='"+deptCode+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
							
						}else if(roleId.equals("9")) { // HOD
							sql="select count(*) as total, "
									+ "sum(case when (case_status=3) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=3) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dept_code='"+deptCode+"'";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}	
							
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=3) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=3) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ "  from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and ad2.dept_code='"+deptCode+"'";
							dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where dept_code='"+deptCode+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
							
						}else if(roleId.equals("4")) { // MLO
							// sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=2 and coalesce(ecourts_case_status,'')!='Closed'";
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=2) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=2) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dept_code='"+deptCode+"'";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql, con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(totaldeptcases !="0")
							{
								total_value = totaldeptcases;
							}
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}	
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=2) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=2) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ " from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and ad2.dept_code='"+deptCode+"'";
							
							dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							
							
							sql="select count(*) from ecourts_dept_instructions where dept_code='"+deptCode+"'";
							instruction_count =  DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where dept_code='"+deptCode+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
							
							
						}
						else  if(roleId.equals("5")) { // NODAL OFFICER
							// sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=4 and coalesce(ecourts_case_status,'')!='Closed'";
							sql="select count(*) as total, "
									+ "sum(case when (case_status=4) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=4) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dept_code='"+deptCode+"'";
							
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql, con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}
							
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=4) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=4) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ " from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and ad2.dept_code='"+deptCode+"'";
							
							dashboardCounts = DatabasePlugin.executeQuery(sql, con);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
							Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
							assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where dept_code='"+deptCode+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
						}
						else  if(roleId.equals("10")) { // District NODAL OFFICER
							// sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=4 and coalesce(ecourts_case_status,'')!='Closed'";
							sql="select count(*) as total, "
									+ "sum(case when (case_status=8) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=8) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data where dept_code='"+deptCode+"' and dist_id='"+distId+"'";
							System.out.println("SQL:"+sql);
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value=Long.toString(approval_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}
							
							sql="select count(*) as total, "
									+ "sum(case when (case_status is null or case_status=8) and coalesce(assigned,'f')='f' then 1 else 0 end) as assignment_pending,"
									+ "sum(case when (case_status=8) and coalesce(assigned,'f')='t' then 1 else 0 end) as approval_pending,"
									+ "sum(case when case_status=99 then 1 else 0 end) as closedcases"
									+ " from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and ad2.dept_code='"+deptCode+"'  and ad2.dist_id='"+distId+"'";
							
							dashboardCounts = DatabasePlugin.executeQuery(sql, con);
							total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							new_cases_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value_new_cases=Long.toString(assignment_pending_new);
							}
							if(((Map) dashboardCounts.get(0)).get("approval_pending") !=null && !((Map) dashboardCounts.get(0)).get("approval_pending").equals(" "))
							{
								Long approval_pending_new=(Long) ((Map) dashboardCounts.get(0)).get("approval_pending");
								approval_pending_value_new_cases=Long.toString(approval_pending_new);
							}
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where dept_code='"+deptCode+"' ";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);
						}
						
						
						else if(roleId.equals("8")) { // Section Officer
							// sql="select emp_id,count(*) as assigned from ecourts_case_emp_assigned_dtls where emp_id='"+empId+"' group by emp_id";
							sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=5 and coalesce(ecourts_case_status,'')!='Closed'";
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_value = Long.toString(assigned_cases);
							
							
							sql="select count(*) as assigned from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and assigned=true and assigned_to='"+userid+"' and case_status=5 ";
							dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_new_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_new_cases_value = Long.toString(assigned_new_cases);
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where assigned=true and assigned_to='"+userid+"' and case_status=5 and coalesce(ecourts_case_status,'')!='Closed'";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);							
						}
						else if(roleId.equals("11")) { // Section Officer (HOD)
							// sql="select emp_id,count(*) as assigned from ecourts_case_emp_assigned_dtls where emp_id='"+empId+"' group by emp_id";
							sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=9 and coalesce(ecourts_case_status,'')!='Closed'";
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_value = Long.toString(assigned_cases);
							
							
							sql="select count(*) as assigned from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and assigned=true and assigned_to='"+userid+"' and case_status=9 ";
							dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_new_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_new_cases_value = Long.toString(assigned_new_cases);
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where assigned=true and assigned_to='"+userid+"' and case_status=9 and coalesce(ecourts_case_status,'')!='Closed'";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);	
							
						}else if(roleId.equals("12")) { // Section Officer (DIST - HOD)
							// sql="select emp_id,count(*) as assigned from ecourts_case_emp_assigned_dtls where emp_id='"+empId+"' group by emp_id";
							sql="select count(*) as assigned from ecourts_case_data where assigned=true and assigned_to='"+userid+"' and case_status=10 and coalesce(ecourts_case_status,'')!='Closed'";
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_value = Long.toString(assigned_cases);
							
							sql="select count(*) as assigned from ecourts_gpo_ack_dtls ad1 inner join ecourts_gpo_ack_depts ad2 on (ad1.ack_no=ad2.ack_no)  where ack_type='NEW' and assigned=true and assigned_to='"+userid+"' and case_status=10 ";
							dashboardCounts = DatabasePlugin.executeQuery(con, sql);
							Long assigned_new_cases=(Long) ((Map) dashboardCounts.get(0)).get("assigned");
							assigned_new_cases_value = Long.toString(assigned_new_cases);
							
							sql="select count(*) from ecourts_case_data a inner join (select distinct cino from ecourts_gpo_daily_status) b on (a.cino=b.cino) "
									+ " where assigned=true and assigned_to='"+userid+"' and case_status=10 and coalesce(ecourts_case_status,'')!='Closed'";
							dailyStatusbyGP = DatabasePlugin.getStringfromQuery(sql, con);							
							
						}
						else if(roleId.equals("1") || roleId.equals("7")) {
							
							// request.setAttribute("SHOWABSTRACTS", "SHOWABSTRACTS");
						}
						
						else if(roleId.equals("6")) { // GP NEW CODE
							
							sql="select reg_year,count(*) as casescount from ecourts_case_data a "
									+ "inner join dept_new d on (a.dept_code=d.dept_code)   inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) "
									+ "where reg_year > 0 and d.display = true  and e.gp_id='"+userid+"' "
									+ "group by reg_year order by reg_year desc";
							System.out.println("YEARLY COUNT SQL:"+sql);
							//TO BE DONE request.setAttribute("YEARWISECASES", DatabasePlugin.executeQuery(con, sql));
							List<Map<Object, Integer>> yearWiseCounts = DatabasePlugin.executeQuery(con, sql);
							JSONArray finalList = new JSONArray();
							JSONObject casesData = new JSONObject();
							
							if (yearWiseCounts != null && !yearWiseCounts.isEmpty() && yearWiseCounts.size() > 0) {								
								for (Map<Object, Integer> map : yearWiseCounts) {									
								    	JSONObject cases = new JSONObject();
								    	cases.put("reg_year",map.get("reg_year").toString());
								    	cases.put("cases_count", map.get("casescount"));								    	
								    	finalList.put(cases);
									
								}
								casesData.put("GP_YEAR_WISE_COUNTS", finalList);
								yearWiseData = casesData.toString().substring(1,casesData.toString().length()-1);
								
								}
							
							  sql="select count(distinct a.cino) from ecourts_dept_instructions a where a.dept_code in (select dept_code from ecourts_mst_gp_dept_map where gp_id='"+userid+"')";
							  System.out.println("instruction SQL:"+sql);
							  instruction_count = DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select count(*) From ecourts_olcms_case_details a "
									+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
									+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code and ecd.assigned_to=emgd.gp_id) "
									+ "where pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='Yes' and (counter_filed='No' or counter_filed='Yes') and coalesce(counter_approved_gp,'F')='F' and ecd.case_status='6' "
									+ "and emgd.gp_id='"+userid+"'";
							System.out.println("COUNTERS SQL:"+sql);
							counterfilecount_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							//System.out.println("counterFile--"+sql);
							sql="select count(*) From ecourts_olcms_case_details a "
									+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
									+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code and ecd.assigned_to=emgd.gp_id) "
									+ "where (pwr_uploaded='No' or pwr_uploaded='Yes') and (coalesce(pwr_approved_gp,'0')='0' or coalesce(pwr_approved_gp,'No')='No' ) and ecd.case_status='6' "
									+ "and emgd.gp_id='"+userid+"'  ";
							System.out.println("PARAWISE COUNT SQL:"+sql);
							parawisecount_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							
						}						
						
						else if(roleId.equals("61")) { // GPO OLD CODE
							
							sql="select count(*) as total, "
									+ " sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
									+ " sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases"
									+ "  from ecourts_case_data a inner join ecourts_mst_gp_dept_map b on (a.dept_code=b.dept_code) where b.gp_id='"+userid+"'";
							
							System.out.println("total--"+sql);
							
														
							List<Map<Object, String>> dashboardCounts = DatabasePlugin.executeQuery(sql,con);
							Long total_cases=(Long) ((Map) dashboardCounts.get(0)).get("total");
							total_value=Long.toString(total_cases);
							if(((Map) dashboardCounts.get(0)).get("assignment_pending") !=null && !((Map) dashboardCounts.get(0)).get("assignment_pending").equals(" "))
							{
								Long assignment_pending=(Long) ((Map) dashboardCounts.get(0)).get("assignment_pending");
								assignment_value=Long.toString(assignment_pending);
							}
							
							if(((Map) dashboardCounts.get(0)).get("closedcases") !=null && !((Map) dashboardCounts.get(0)).get("closedcases").equals(" "))
							{
								Long closedcases=(Long) ((Map) dashboardCounts.get(0)).get("closedcases");
								closedcases_value=Long.toString(closedcases);
							}
							
							sql="select count(*) from ecourts_gpo_ack_dtls ad inner join ecourts_gpo_ack_depts d on (ad.ack_no=d.ack_no) inner join dept_new dm on (d.dept_code=dm.dept_code) "
									+ " inner join ecourts_mst_gp_dept_map egm on (egm.dept_code=d.dept_code)  where ack_type='NEW' and respondent_slno=1 and egm.gp_id='"+userid+"'";
							
							new_cases_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							
							// OLD CASES
							sql="select count(*) From ecourts_olcms_case_details a "
									+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
									+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code) "
									+ "inner join ecourts_mst_gp_dept_map ad on (ad.dept_code =ecd.dept_code ) "
									+ "where counter_filed='Yes' and coalesce(counter_approved_gp,'F')='F' and ecd.case_status='6' "
									+ "and emgd.gp_id='"+userid+"'";
							System.out.println("COUNTERS SQL:"+sql);
							counterfilecount_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select count(*) From ecourts_olcms_case_details a "
									+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
									+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code) "
									+ "inner join ecourts_mst_gp_dept_map ad on (ad.dept_code =ecd.dept_code ) "
									+ "where pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='No' and ecd.case_status='6' "
									+ "and emgd.gp_id='"+userid+"'  ";
							System.out.println("PARAWISE COUNT SQL:"+sql);
							parawisecount_value = DatabasePlugin.getStringfromQuery(sql, con);
							
							sql="select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder a "
									+ "inner join ecourts_case_data b  on (a.cino=b.cino) inner join ecourts_mst_gp_dept_map c on (b.dept_code=c.dept_code) where c.gp_id='"+userid+"'";
							
							final_order_value = DatabasePlugin.getStringfromQuery(sql, con);
							//System.out.println("FINALORDERS--"+sql);
							
							  sql="select count(distinct a.cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_interimorder a "
							  		+ "inner join ecourts_case_data b  on (a.cino=b.cino) inner join ecourts_mst_gp_dept_map c on (b.dept_code=c.dept_code) where c.gp_id='"+userid+"'"; 
							  String interimOrderData = DatabasePlugin.getStringfromQuery(sql, con);
								
								if(interimOrderData != null && !interimOrderData.isEmpty())
								{
									String interimData[] = DatabasePlugin.getStringfromQuery(sql, con).split(",");
									str1=interimData[0];
									str2=interimData[1];
								}
							
							sql="select "
									+ " sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end) as disposed,"
									+ " sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end) as allowed,"
									+ " sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end) as dismissed ,"
									+ " sum(case when disposal_type='WITHDRAWN' then 1 else 0 end) as withdrawn,"
									+ " sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end) as closed,"
									+ " sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end) as returned"
									+ " from ecourts_case_data a inner join ecourts_mst_gp_dept_map b on (a.dept_code=b.dept_code) where b.gp_id='"+userid+"'";
							
							List rs4 = DatabasePlugin.executeQuery(sql, con);

							Long disposed=(Long) ((Map) rs4.get(0)).get("disposed");
							disposed_value=Long.toString(disposed);
							Long allowed=(Long) ((Map) rs4.get(0)).get("allowed");
							allowed_value=Long.toString(allowed);
							Long dismissed=(Long) ((Map) rs4.get(0)).get("dismissed");
							dismissed_value=Long.toString(dismissed);
							Long withdrawn=(Long) ((Map) rs4.get(0)).get("withdrawn");
							withdrawn_value=Long.toString(withdrawn);
							Long closed=(Long) ((Map) rs4.get(0)).get("closed");
							closed_value=Long.toString(closed);
							Long returned=(Long) ((Map) rs4.get(0)).get("returned");
							returned_value=Long.toString(returned);
							
							
						}
						else if(roleId.equals("13") || roleId.equals("14")) { // HC-DEOs
							
						}					
						
					}

						jsonStr = "{\"RESPONSE\" : "; 

						jsonStr += "{\"TOTAL\":\"" + total_value + "\",\"ASSIGNMENT_PENDING\":\"" + assignment_value
								+ "\", \"CLOSED\":\"" + closedcases_value + "\"  , " + " \"NEWCASES\":\""
								+ new_cases_value + "\",\"NEWCASES_PENDING_ASSIGNMENT\":\"" + assignment_value_new_cases
								+ "\", \"NEWCASES_PENDING_APPROVAL\":\"" + approval_pending_value_new_cases
								+ "\", \"NEWCASES_ASSIGNED\":\"" + assigned_new_cases_value + "\" ,\"INTERIM_CASES\":\"" + str1 + "\" ,\"INTERIM_ORDERS\":\"" + str2
								+ "\",\"FINAL_ORDERS\":\"" + final_order_value + "\" ," + " \"DISPOSED\":\""
								+ disposed_value + "\", \"ALLOWED\":\"" + allowed_value + "\" ,\"DISMISSED\":\""
								+ dismissed_value + "\",\"WITHDRAWN\":\"" + withdrawn_value + "\" ,\"HCCLOSED\":\""
								+ closed_value + "\" ," + " \"RETURNED\":\"" + returned_value + "\" ,\"ASSIGNED\":\""
								+ assigned_value + "\"  ,\"APPROVAL_PENDING\":\"" + approval_pending_value + "\" , \"INSTRUCTION_COUNT\":\""
								+ instruction_count + "\" ,\"DAILY_STATUS_BY_GP\":\"" + dailyStatusbyGP + "\" , \"COUNTERFILECOUNT\":\""
								+ counterfilecount_value + "\"  ,\"PARAWISECOUNT\":\"" + parawisecount_value+"\","+yearWiseData
								+ ", \"RSPCODE\": \"01\",\"RSPDESC\": \"SUCCESS\"}";
						// +"\" },";
					
						//jsonStr = jsonStr.substring(0,jsonStr.length() - 1); 
					jsonStr += "}";

					System.out.println("jsonStr--"+jsonStr);

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
