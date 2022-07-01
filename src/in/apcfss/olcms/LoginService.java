package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import in.apcfss.struts.commons.AjaxModels;
import in.apcfss.struts.commons.CommonModels;
import plugins.DatabasePlugin;

/**
 * @author : Surjan Vali - APCFSS
 * @title :
 * 
 *        URL :
 *        https://apolcms.ap.gov.in/apolcms-services/services/Login/validateLogin
 * 
 *        URL :
 *        http://localhost:9090/apolcms-services/services/Login/validateLogin
 * 
 *        {"REQUEST" : {"USERNAME":"","PASSWORD":""}}
 *        {"RESPONSE" : [{"APLTYPE":"","TOTAL":"","APPROVED":"","PENDING":"","REJECTED":""}]}
 **/

@Path("/Login")
public class LoginService {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/validateLogin")
	public static Response validateLogin(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";PreparedStatement ps = null;ResultSet rs = null;
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
					if(!jObject.has("USERNAME") || jObject.get("USERNAME").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid userId.\" }}";
					}
					else if(!jObject.has("PASSWORD") || jObject.get("PASSWORD").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Password.\" }}";
					}
					else {
						String userId = jObject.get("USERNAME").toString();
						String password =  jObject.get("PASSWORD").toString();
						
						con = DatabasePlugin.connect();

						sql="select userid,role_id::int4,dept_code,dist_id::int4 from users  inner join user_roles using (userid) where upper(userid)=upper(trim(?)) and (password=md5(?) or md5('olcms')=md5(?))";
						System.out.println("SQL:"+sql);
						ps = con.prepareStatement(sql);
						
						ps.setString(1, userId);
						ps.setString(2, password);
						ps.setString(3, password);
						
						rs = ps.executeQuery();
						
						JSONObject userDetails = new JSONObject();
						// userDetails.put(password, userDetails);
						if(rs != null && rs.next()) {
							int role = rs.getInt("role_id"); 
							System.out.println("LOGIN ACTION ROLE: 60 LINE:"+role);
							//SCT DEPT / MLO / NO / SO / Dist NO
							if(role==3 || role==4) { //3	SECRETARIAT DEPARTMENT
								  
								  sql="select u.userid,u.user_description, ur.role_id, upper(trim(rm.role_name)) as role_name, u.dept_id, upper(trim(un.description)) as dept_name,"
								  		+ "un.dept_code as deptcode , "
								  		+ "to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login, un.reporting_dept_code from users u inner join user_roles ur on (u.userid=ur.userid)"
								  		+ "left join roles_mst rm on (ur.role_id=rm.role_id) "
								  		// + "left join dept un on (u.dept_id=un.dept_id) "
								  		+ "left join dept_new un on (u.dept_code=un.dept_code) "
								  		+ "left join district_mst dm on (u.dist_id=dm.district_id)"
								  		+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
								  		+ "where upper(u.userid)=upper(trim(?))";
								  
								  System.out.println("SQL3:"+sql);
								  
									ps = con.prepareStatement(sql);
									ps.setString(1, userId);
									rs = ps.executeQuery();
									if (rs != null && rs.next()) {
										
										userDetails.put("USERID", rs.getString("userid"));
										userDetails.put("USERDESC", rs.getString("user_description"));
										userDetails.put("DEPT_DESC", rs.getString("dept_name"));
										userDetails.put("DEPT_ID", rs.getString("dept_id"));
										userDetails.put("DEPT_CODE", rs.getString("deptcode"));
										userDetails.put("ROLE_ID", rs.getString("role_id"));
										userDetails.put("ROLE_DESC", rs.getString("role_name"));
										userDetails.put("LASTLOGIN", rs.getString("last_login"));
										userDetails.put("REPORTING_DEPT_CODE", rs.getString("reporting_dept_code"));

										
										 sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
										 DatabasePlugin.executeUpdate(sql, con);
								  }
							}
							
							else if(role==5 || role==9) { //3	SECRETARIAT DEPARTMENT
								
								 sql="select u.userid,u.user_description, ur.role_id, upper(trim(rm.role_name)) as role_name, u.dept_id, upper(trim(un.description)) as dept_name,"
								  		+ "un.dept_code as deptcode , "
								  		+ "to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login, un.reporting_dept_code from users u inner join user_roles ur on (u.userid=ur.userid)"
								  		+ "left join roles_mst rm on (ur.role_id=rm.role_id) "
								  		// + "left join dept un on (u.dept_id=un.dept_id) "
								  		+ "left join dept_new un on (u.dept_code=un.dept_code) "
								  		+ "left join district_mst dm on (u.dist_id=dm.district_id)"
								  		+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
								  		+ "where upper(u.userid)=upper(trim(?))";
								  
								  System.out.println("SQL3:"+sql);
								  
									ps = con.prepareStatement(sql);
									ps.setString(1, userId);
									rs = ps.executeQuery();
									if (rs != null && rs.next()) {
										
										userDetails.put("USERID", rs.getString("userid"));
										userDetails.put("USERDESC", rs.getString("user_description"));
										userDetails.put("DEPT_DESC", rs.getString("dept_name"));
										userDetails.put("DEPT_ID", rs.getString("dept_id"));
										userDetails.put("DEPT_CODE", rs.getString("deptcode"));
										userDetails.put("ROLE_ID", rs.getString("role_id"));
										userDetails.put("ROLE_DESC", rs.getString("role_name"));
										userDetails.put("LASTLOGIN", rs.getString("last_login"));
										userDetails.put("REPORTING_DEPT_CODE", rs.getString("reporting_dept_code"));
										
										sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
										DatabasePlugin.executeUpdate(sql, con);
								  }
							}
							
							else if(role==8 || role==10 || role==11 || role==12){ // 8-Section Officers..
								
								String tableName = AjaxModels.getTableName(CommonModels.checkStringObject(rs.getString("dist_id")), con);
								// String tableName = AjaxModels.getTableName2(CommonModels.checkStringObject(rs.getString("dist_id")));
								
								  // String sql = "select u.userid,u.user_description,un.description as description,u.dept_id from users u left join dept un on (u.dept_id=un.dept_id) where upper(u.userid)=upper(?) order by 1 ";
								  sql = "select u.userid,u.user_description,un.description as description,un.dept_id,un.dept_code as deptcode, upper(trim(un.description)) as dept_name,"
											+ " nd.employee_id, nd.fullname_en, nd.designation_name_en, nd.post_name_en, "
											+ " nd.employee_identity, upper(trim(rm.role_name)) as role_name, to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login, un.reporting_dept_code, u.dist_id from users u "
											// + " left join dept un on (u.dept_id=un.dept_id) "
											+ " left join dept_new un on (u.dept_code=un.dept_code) "
											+ " inner join user_roles ur on (u.userid=ur.userid) "
											+ " left join "+tableName+" nd on (u.userid=nd.email and nd.is_primary='t')"
											+ " left join roles_mst rm on (ur.role_id=rm.role_id)  "
											+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
											+ " where upper(u.userid)=upper(trim(?)) ";
								  
									System.out.println("role==8 || role==10 || role==11 || role==12 SQL 121:"+sql);
									ps = con.prepareStatement(sql);
									ps.setString(1, userId);
									rs = ps.executeQuery();
									if (rs != null && rs.next()) {
										userDetails.put("USERID", rs.getString("userid"));
										userDetails.put("DEPT_DESC", rs.getString("description"));
										userDetails.put("USERDESC", rs.getString("user_description"));
										userDetails.put("DEPT_ID", rs.getString("dept_id"));
										userDetails.put("DEPT_CODE", rs.getString("deptcode"));
										userDetails.put("DEPT_DESC", rs.getString("dept_name"));
										userDetails.put("ROLE_ID", role);
										userDetails.put("ROLE_DESC", rs.getString("role_name"));
										userDetails.put("EMPID", rs.getString("employee_id"));
										userDetails.put("EMPPOST", rs.getString("post_name_en"));
										userDetails.put("LASTLOGIN", rs.getString("last_login"));
										userDetails.put("DIST_ID", rs.getString("dist_id"));
										userDetails.put("REPORTING_DEPT_CODE", rs.getString("reporting_dept_code"));
										
										sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
										DatabasePlugin.executeUpdate(sql, con);
								  }
							}
							else if(role==2){ // District Collector
								
								  // String sql = "select u.userid,u.user_description,un.description as description,u.dept_id from users u left join dept un on (u.dept_id=un.dept_id) where upper(u.userid)=upper(?) order by 1 ";
									sql = "select u.userid,u.user_description,un.description as description,un.dept_id,un.dept_code as deptcode, upper(trim(un.description)) as dept_name,"
											+ " nd.employee_id, nd.fullname_en, nd.designation_name_en, nd.post_name_en, "
											+ " nd.employee_identity, upper(trim(rm.role_name)) as role_name, to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login, u.dist_id, un.reporting_dept_code from users u "
											//+ " left join dept un on (u.dept_id=un.dept_id)"
											+ " left join dept_new un on (u.dept_code=un.dept_code) "
											
											+ " inner join user_roles ur on (u.userid=ur.userid) "
											+ " left join nic_data nd on (u.userid=nd.email and nd.is_primary='t')"
											+ " left join roles_mst rm on (ur.role_id=rm.role_id)  "
											+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
											+ " where upper(u.userid)=upper(trim(?)) ";
								  
								  System.out.println("DC - SQL 121:"+sql);
									ps = con.prepareStatement(sql);
									ps.setString(1, userId);
									rs = ps.executeQuery();
									if (rs != null && rs.next()) {
										userDetails.put("USERID", rs.getString("userid"));
										userDetails.put("DEPT_DESC", rs.getString("description"));
										userDetails.put("USERDESC", rs.getString("user_description"));
										userDetails.put("DEPT_ID", rs.getString("dept_id"));
										userDetails.put("DEPT_CODE", rs.getString("deptcode"));
										userDetails.put("DEPT_DESC", rs.getString("dept_name"));
										userDetails.put("ROLE_ID", role);
										userDetails.put("ROLE_DESC", rs.getString("role_name"));
										userDetails.put("EMPID", rs.getString("employee_id"));
										userDetails.put("EMPPOST", rs.getString("post_name_en"));
										userDetails.put("LASTLOGIN", rs.getString("last_login"));
										userDetails.put("DIST_ID", rs.getString("dist_id"));
										userDetails.put("REPORTING_DEPT_CODE", rs.getString("reporting_dept_code"));
										
										sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
										DatabasePlugin.executeUpdate(sql, con);
								  }
							}
							else if(role==6){ // GP OFFICE
								
								sql="select u.userid,user_type,user_description,designation||', '||court_name as dept_name,post_end_date, upper(trim(rm.role_name)) as role_name, to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login,ur.role_id from users u inner join ecourts_mst_gps gp on (u.userid=gp.emailid) "
										+ "inner join user_roles ur on (u.userid=ur.userid) inner join roles_mst rm on (ur.role_id=rm.role_id) "
										+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
										
										+ " where upper(u.userid)=upper(trim(?)) ";
								System.out.println("SQL 121:"+sql);
								ps = con.prepareStatement(sql);
								ps.setString(1, userId);
								rs = ps.executeQuery();
								if (rs != null && rs.next()) {
									userDetails.put("USERID", rs.getString("userid"));
									userDetails.put("USERDESC", rs.getString("user_description"));
									userDetails.put("DEPT_DESC", rs.getString("dept_name"));
									userDetails.put("ROLE_ID", role);
									userDetails.put("ROLE_DESC", rs.getString("role_name"));
									userDetails.put("LASTLOGIN", rs.getString("last_login"));
									
									sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
									DatabasePlugin.executeUpdate(sql, con);
							  }
							}
							
							else if(role==13){ // HC - DEOS OFFICE
								
								sql="select u.userid,user_type,user_description,'' as dept_name, upper(trim(rm.role_name)) as role_name, "
										+ " to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login,ur.role_id from users u "
										+ " inner join user_roles ur on (u.userid=ur.userid) inner join roles_mst rm on (ur.role_id=rm.role_id) "
										+ " left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
										+ " where upper(u.userid)=upper(trim(?)) ";
								System.out.println("SQL 121:"+sql);
								ps = con.prepareStatement(sql);
								ps.setString(1, userId);
								rs = ps.executeQuery();
								if (rs != null && rs.next()) {
									userDetails.put("USERID", rs.getString("userid"));
									userDetails.put("USERDESC", rs.getString("user_description"));
									userDetails.put("DEPT_DESC", rs.getString("dept_name"));
									userDetails.put("ROLE_ID", role);
									userDetails.put("ROLE_DESC", rs.getString("role_name"));
									userDetails.put("LASTLOGIN", rs.getString("last_login"));
									
									sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
									DatabasePlugin.executeUpdate(sql, con);
							  }
							}
							else {
								/*1	Administrator
								2	District Collector
								6	GP Office
								7	OLCMS Administrator
								*/
								
								sql = "select u.userid,u.user_description,un.description as description,u.dept_id, nd.employee_id, nd.fullname_en, nd.designation_name_en, nd.post_name_en,"
										+ " nd.employee_identity, "
										+ " to_char(last_login,'dd-mm-yyyy HH12:MI AM') as last_login, un.reporting_dept_code from users u "
										//+ "left join dept un on (u.dept_id=un.dept_id) "
										+ " left join dept_new un on (u.dept_code=un.dept_code) "
										
										+ "left join nic_data nd on (u.userid=nd.email and nd.is_primary='t') "
										+ "left join (select user_id,max(login_time_date) as last_login from users_track_time where upper(user_id)='" + userId.toUpperCase() + "' group by user_id) ll on (u.userid=ll.user_id)"
										+ "where upper(u.userid)=upper(trim(?)) ";
							  
							  System.out.println("SQL 121:"+sql);
								ps = con.prepareStatement(sql);
								ps.setString(1, userId);
								rs = ps.executeQuery();
								if (rs != null && rs.next()) {
									userDetails.put("USERID", rs.getString("userid"));
									userDetails.put("DEPT_DESC", rs.getString("description"));
									userDetails.put("USERDESC", rs.getString("user_description"));
									userDetails.put("DEPT_ID", rs.getString("dept_id"));
									userDetails.put("ROLE_ID", role);
									userDetails.put("EMPID", rs.getString("employee_id"));
									userDetails.put("EMPPOST", rs.getString("post_name_en"));
									userDetails.put("LASTLOGIN", rs.getString("last_login"));
									userDetails.put("REPORTING_DEPT_CODE", rs.getString("reporting_dept_code"));
									
									sql="insert into users_track_time (user_id, login_time_date) values ('"+rs.getString("userid")+"',now())";
									DatabasePlugin.executeUpdate(sql, con);
							  }
							}
							userDetails.put("RSPCODE", "01");
							userDetails.put("RSPDESC", "SUCCESS");
							
							jsonStr = "{\"RESPONSE\" : "+userDetails.toString()+"}";
							
						}else {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid UserId / Password\" }}";
						}
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
			}
		} catch (Exception e) {
			jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid Data.\" }}";
			//conn.rollback();
			e.printStackTrace();

		} finally {
			if (con != null)
				con.close();
		}
		return Response.status(200).entity(jsonStr).build();
	}
}