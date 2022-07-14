package in.apcfss.olcms;

import java.util.List;
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

import in.apcfss.struts.commons.CommonModels;
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
		Connection conn = null;
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
					conn = DatabasePlugin.connect(); //DriverManager.getConnection(CommonVariables.dataBase, CommonVariables.userName, CommonVariables.password);
					//Statement stmt = conn.createStatement();
					//java.util.List data= new ArrayList();

					String sql="";

					String sqlCondition = "";
					String sqlCondition1 = "",sqlCondition4="";
					String sqlCondition2 = "",sqlConditionDist="",sqlConditionStatus="",sqlConditionUser="",sqlConditionUser1="",sqlConditionUser2="",sql1="",sql2="",sql3="",sqlConditionUsergp="";

					if(roleId!=null && !roleId.equals("")){

						if(roleId.equals("1") || roleId.equals("7")) { 

							//sqlCondition +=" and dept_code='"+deptCode+"' ";

							//sqlCondition1 +=" and (reporting_dept_code='"+deptCode+"' or ad.dept_code='"+deptCode+"') and respondent_slno=1 ";

						}else if(roleId.equals("3") || roleId.equals("4")  || roleId.equals("5") || roleId.equals("9")) {

							sqlCondition2 +=" and (reporting_dept_code='"+deptCode+"' or a.dept_code='"+deptCode+"') ";

						}if(roleId.equals("2")) {

							sqlConditionDist +=" and dist_id='"+distId+"' ";

						}else if(roleId.equals("3")) { 
							sqlCondition4 +=" and (reporting_dept_code='"+deptCode+"' or ad.dept_code='"+deptCode+"') ";
							sqlCondition +=" and dept_code='"+deptCode+"' ";

						}else if(roleId.equals("9")) { 
							sqlCondition4 +=" and (reporting_dept_code='"+deptCode+"' or ad.dept_code='"+deptCode+"') ";
							sqlCondition +=" and dept_code='"+deptCode+"' ";

						}else if(roleId.equals("4")) {
							sqlCondition4 +=" and (reporting_dept_code='"+deptCode+"' or ad.dept_code='"+deptCode+"') ";
							sqlCondition +=" and dept_code='"+deptCode+"' ";

						}else  if(roleId.equals("5")) {
							sqlCondition4 +=" and (reporting_dept_code='"+deptCode+"' or ad.dept_code='"+deptCode+"') ";
							sqlCondition +=" and dept_code='"+deptCode+"' ";

						}else  if(roleId.equals("10")) {

							sqlCondition +=" and dept_code='"+deptCode+"' ";

							sqlConditionDist +=" and dist_id='"+distId+"' ";

						}else if(roleId.equals("8")) {

							sqlConditionStatus +=" and  assigned=true and assigned_to='"+userid+"' and case_status=5 and coalesce(ecourts_case_status,'')!='Closed' ";

						}else if(roleId.equals("11")) {

							sqlConditionStatus +=" and  assigned=true and assigned_to='"+userid+"' and case_status=9 and coalesce(ecourts_case_status,'')!='Closed' ";

						}else if(roleId.equals("12")) {

							sqlConditionStatus +=" and  assigned=true and assigned_to='"+userid+"' and case_status=10 and coalesce(ecourts_case_status,'')!='Closed' ";

						}else if(roleId.equals("6") || roleId.equals("61")) { // GP NEW CODE

							sqlConditionUser +="  inner join ecourts_mst_gp_dept_map b on (a.dept_code=b.dept_code)   ";
							sql1+=" and b.gp_id='"+userid+"'";

							sqlConditionUser1 +=" inner join ecourts_case_data b  on (a.cino=b.cino) inner join ecourts_mst_gp_dept_map c on (b.dept_code=c.dept_code)   ";
							sql2+=" and  c.gp_id='"+userid+"'";

							sqlConditionUser2 +=" inner join ecourts_mst_gp_dept_map egm on (egm.dept_code=d.dept_code)   ";
							sql3=" and  egm.gp_id='"+userid+"'";

						}
						else if(roleId.equals("61")) { // GPO OLD CODE

							sqlConditionUsergp +=" and  emgd.gp_id='"+userid+"' ";

						}
						else if(roleId.equals("13") || roleId.equals("14")) { // HC-DEOs

						}
					}

					/*
					 * sql=" select count(*) as total, " +
					 * " sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as assignment_pending,"
					 * +
					 * " sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases  from ecourts_case_data "
					 * ;
					 */

					String total_value="0",assignment_value="0",closedcases_value="0",new_cases_value="0",order_value="0";
					String  disposed_value="0",allowed_value="0",dismissed_value="0",withdrawn_value="0",closed_value="0",returned_value="0",assigned_value="0",counterfilecount_value="0",parawisecount_value="0";


					if(! (roleId.equals("8") || roleId.equals("11")  || roleId.equals("12"))) {
						sql="select count(*) as total, "
								+ " coalesce(sum(case when (case_status is null or case_status=2) and coalesce(assigned,'f')='f' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end),'0') as assignment_pending,"
								+ " coalesce(sum(case when (case_status=2) and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end),'0') as approval_pending,"
								+ " coalesce(sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end),'0') as closedcases"
								+ " from ecourts_case_data a "+sqlConditionUser+"  where 1=1 "+sqlCondition+"  "+sqlConditionDist+" "+sql1+" ";

						System.out.println("sql--"+sql);
						// ResultSet rs = stmt.executeQuery(sql);
						List rs = DatabasePlugin.executeQuery(sql, conn);

						Long total=(Long) ((Map) rs.get(0)).get("total");
						total_value=Long.toString(total);
						Long assignment_pending=(Long) ((Map) rs.get(0)).get("assignment_pending");
						assignment_value=Long.toString(assignment_pending);
						Long closedcases=(Long) ((Map) rs.get(0)).get("closedcases");
						closedcases_value=Long.toString(closedcases);
					
					}

					//sql="select count(*) as new_cases  from ecourts_gpo_ack_dtls where ack_type='NEW'"; 

					if(! (roleId.equals("8") || roleId.equals("11")  || roleId.equals("12"))) {

						sql="select count(*) as new_cases from ecourts_gpo_ack_depts ad inner join ecourts_gpo_ack_dtls ad1 on (ad.ack_no=ad1.ack_no) "
								+ "inner join dept_new d on (ad.dept_code=d.dept_code)  "+sqlConditionUser2+" "
								+ "where ack_type='NEW' "+sqlCondition1+" "+sqlConditionDist+"  "+sqlCondition4+" "+sql3+" ";
						
						System.out.println("sql--"+sql); 
						List rs1 = DatabasePlugin.executeQuery(sql, conn);

						Long new_cases=(Long) ((Map) rs1.get(0)).get("new_cases");
						new_cases_value=Long.toString(new_cases);
					}

					//sql="select sum(case when length(order_document_path) > 10 then 1 else 0 end) as orders from ecourts_case_finalorder"; 
					if(! (roleId.equals("8") || roleId.equals("11")  || roleId.equals("12"))) {
						sql="select coalesce(sum(case when length(order_document_path) > 10 then 1 else 0 end),'0') as orders from ecourts_case_finalorder b inner join ecourts_case_data a on (a.cino=b.cino) "
								+ "inner join dept_new d on (a.dept_code=d.dept_code)  "+sqlConditionUser2+" "
								+ "where d.display = true "+sqlCondition2+"  "+sqlConditionDist+" "+sql3+" ";
					
						System.out.println("sql--"+sql); 
						List rs2 = DatabasePlugin.executeQuery(sql, conn);
						//System.out.println("rs2--"+rs2);

						Long orders=(Long) ((Map) rs2.get(0)).get("orders");

						order_value=Long.toString(orders);
					
					}
					

					String str1="0";
					String str2="0";

					if(! (roleId.equals("8") || roleId.equals("11")  || roleId.equals("12"))) {
						sql="select coalesce(count(distinct a.cino) ||','||sum(case when length(order_document_path) > 10 then 1 else 0 end),'0') as orders_data from ecourts_case_interimorder b "
								+ "inner join ecourts_case_data a on (a.cino=b.cino) "
								+ "inner join dept_new d on (a.dept_code=d.dept_code)  "+sqlConditionUser2+" "
								+ "where d.display = true "+sqlCondition2+" "+sqlConditionDist+" "+sql3+"   ";

						String order_data[]=DatabasePlugin.getStringfromQuery(sql, conn).split(",");
						str1=order_data[0];
						str2=order_data[1];

					}
					System.out.println("sql--"+sql); 

					
					if( (roleId.equals("1") || roleId.equals("7") || roleId.equals("2") || roleId.equals("6") || roleId.equals("3") || roleId.equals("4")  || roleId.equals("5") || roleId.equals("9") )) {
						sql="select "
								+ " coalesce(sum(case when disposal_type='DISPOSED OF NO COSTS' or disposal_type='DISPOSED OF AS INFRUCTUOUS' then 1 else 0 end),'0') as disposed,"
								+ " coalesce(sum(case when disposal_type='ALLOWED NO COSTS' or disposal_type='PARTLY ALLOWED NO COSTS' then 1 else 0 end),'0') as allowed,"
								+ " coalesce(sum(case when disposal_type='DISMISSED' or disposal_type='DISMISSED AS INFRUCTUOUS' or disposal_type='DISMISSED NO COSTS' or disposal_type='DISMISSED FOR DEFAULT' or disposal_type='DISMISSED AS NON PROSECUTION' or disposal_type='DISMISSED AS ABATED' or disposal_type='DISMISSED AS NOT PRESSED'  then 1 else 0 end),'0') as dismissed ,"
								+ " coalesce(sum(case when disposal_type='WITHDRAWN' then 1 else 0 end),'0') as withdrawn,"
								+ " coalesce(sum(case when disposal_type='CLOSED NO COSTS' or disposal_type='CLOSED AS NOT PRESSED' then 1 else 0 end),'0') as closed,"
								+ " coalesce(sum(case when disposal_type='REJECTED' or disposal_type='ORDERED' or disposal_type='RETURN TO COUNSEL' or disposal_type='TRANSFERRED' then 1 else 0 end),'0') as returned"
								+ " from ecourts_case_data a "
								+ " inner join dept_new d on (a.dept_code=d.dept_code) "+sqlConditionUser+""
								+ " where d.display = true "+sqlCondition2+" "+sqlConditionDist+"  "+sql1+"     ";
						
						System.out.println("sql--"+sql);

						List rs4 = DatabasePlugin.executeQuery(sql, conn);


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
					

					if((roleId.equals("8")  || roleId.equals("11")|| roleId.equals("12") || roleId.equals("6") || roleId.equals("1") || roleId.equals("7")  )) {
						sql="select count(*) as assigned from ecourts_case_data where 1=1  "+sqlConditionStatus+"  ";
						System.out.println("sql--"+sql);
						List rs5 = DatabasePlugin.executeQuery(sql, conn);

						Long assigned=(Long) ((Map) rs5.get(0)).get("assigned");
						assigned_value=Long.toString(assigned);
					}
					

					if((roleId.equals("6") || roleId.equals("1") || roleId.equals("7") )) {
						sql="select count(*) as counterfilecount From ecourts_olcms_case_details a "
								+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
								+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code) "
								+ "inner join ecourts_mst_gp_dept_map ad on (ad.dept_code =ecd.dept_code ) "
								+ "where counter_filed='Yes' and coalesce(counter_approved_gp,'F')='F' and ecd.case_status='6' "
								+ "  "+sqlConditionUsergp+"  ";
						List rs6 = DatabasePlugin.executeQuery(sql, conn);
						Long counterfilecount=(Long) ((Map) rs6.get(0)).get("counterfilecount");
						counterfilecount_value=Long.toString(counterfilecount);
					}
					

					if((roleId.equals("6") || roleId.equals("1") || roleId.equals("7") )) {
						sql="select count(*) as parawisecount From ecourts_olcms_case_details a "
								+ "inner join ecourts_case_data ecd on (a.cino=ecd.cino)  "
								+ "inner join ecourts_mst_gp_dept_map emgd on (ecd.dept_code=emgd.dept_code) "
								+ "inner join ecourts_mst_gp_dept_map ad on (ad.dept_code =ecd.dept_code ) "
								+ "where pwr_uploaded='Yes' and coalesce(pwr_approved_gp,'No')='No' and ecd.case_status='6' "
								+ " "+sqlConditionUsergp+"  ";
						
						List rs7 = DatabasePlugin.executeQuery(sql, conn);
						Long parawisecount=(Long) ((Map) rs7.get(0)).get("parawisecount");
						parawisecount_value=Long.toString(parawisecount);

					}

						jsonStr = "{\"RESPONSE\" : "; 

						jsonStr +="{\"TOTAL\":\"" +total_value + "\",\"ASSIGNMENT_PENDING\":\"" +assignment_value+ "\", \"CLOSED\":\"" +closedcases_value+ "\"  , "
								+ " \"NEWCASES\":\"" +new_cases_value+"\", \"INTERIM_CASES\":\"" +str1+ "\" ,\"INTERIM_ORDERS\":\""  +str2+ "\",\"FINAL_ORDERS\":\"" +order_value+ "\" ,"
								+ " \"DISPOSED\":\"" +disposed_value+"\", \"ALLOWED\":\"" +allowed_value+ "\" ,\"DISMISSED\":\""  +dismissed_value+ "\",\"WITHDRAWN\":\"" +withdrawn_value+ "\" ,\"HCCLOSED\":\"" +closed_value+ "\" ,"
								+ " \"RETURNED\":\"" +returned_value+ "\" ,\"ASSIGNED\":\"" +assigned_value+ "\"  ,\"COUNTERFILECOUNT\":\"" +counterfilecount_value+ "\"  ,\"PARAWISECOUNT\":\"" +parawisecount_value
								+ "\", \"RSPCODE\": \"01\",\"RSPDESC\": \"SUCCESS\"}";
								//+"\" },"; 
					
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
			if (conn != null)
				conn.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
}
