package in.apcfss.olcms;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import in.apcfss.struts.commons.CommonModels;
import plugins.DatabasePlugin;


@Path("/userLogin")
public class UserLoginStatusReport {	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/loginReport")
	public static Response getCasesDetails(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null
						&& !jObject1.get("REQUEST").toString().trim().equals("")) {
					
					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					
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
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String officerType ="";
						
						
						if (jObject.has("OFFICER_TYPE") && !jObject.get("OFFICER_TYPE").toString().equals("")) {
							officerType = jObject.get("OFFICER_TYPE").toString();	
						}	
						
						if (!(roleId.trim().equals("1") || roleId.trim().equals("7"))) {
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Unauthorized to access this service.\" }}";
							return Response.status(200).entity(jsonStr).build();
						}
						
						else if (roleId.trim().equals("1") || roleId.trim().equals("7")) {

							if(CommonModels.checkStringObject(officerType).equals("NO"))
							{

								sql = "select u.userid, to_char(firstlogin,'DD/MM/YYYY') as firstlogin, loggedindays , to_char(lastlogin,'DD/MM/YYYY') as lastlogin ,  "
										+ " (case when (lastlogin::date-firstlogin::date) > 1 then (lastlogin::date-firstlogin::date) - (loggedindays -1) else 0 end) + (current_date - lastlogin::date) as notlogedindays,"
										+ " u.user_description,r.role_name,  d.dept_code as dept_code, d.description from nodal_officer_details md "
										+ " inner join dept_new d on (md.dept_id=d.dept_code) "
										+ " inner join users u on (md.emailid=u.userid) "
										+ " inner join user_roles ur on (u.userid=ur.userid) "
										+ " inner join roles_mst r on (ur.role_id=r.role_id) "
										+ " left join (select user_id, min(login_time_date) as firstlogin,count(distinct login_time_date::date) as loggedindays, max(login_time_date) as lastlogin "
										+ " from users_track_time group by user_id) a on (md.emailid=a.user_id)"
										+ " where coalesce(md.dist_id,0)=0 order by d.dept_code";			
								
							}
							else if(CommonModels.checkStringObject(officerType).equals("GP"))
							{
								
								sql="select u.userid, to_char(firstlogin,'DD/MM/YYYY') as firstlogin, loggedindays , to_char(lastlogin,'DD/MM/YYYY') as lastlogin ,  "
										+ "(case when (lastlogin::date-firstlogin::date) > 1 then (lastlogin::date-firstlogin::date) - (loggedindays -1) else 0 end) + (current_date - lastlogin::date) as notlogedindays, "
										+ "u.user_description as user_description,r.role_name,  md.department as description, md.mobile_no, md.emailid from ecourts_mst_gps  md "
										+ " inner join users u on (md.emailid=u.userid) inner join user_roles ur on (u.userid=ur.userid) "
										+ "inner join roles_mst r on (ur.role_id=r.role_id) "
										+ "left join (select user_id, min(login_time_date) as firstlogin, count(distinct login_time_date::date) as loggedindays, max(login_time_date) as lastlogin  "
										+ "from users_track_time group by user_id) a on (md.emailid=a.user_id) order by md.slno ";								
								
							}
							else if(CommonModels.checkStringObject(officerType).equals("MLOS"))
								{
								sql = "select u.userid, to_char(firstlogin,'DD/MM/YYYY') as firstlogin, loggedindays , to_char(lastlogin,'DD/MM/YYYY') as lastlogin , "
										+ " (case when (lastlogin::date-firstlogin::date) > 1 then (lastlogin::date-firstlogin::date) - (loggedindays -1) else 0 end) + (current_date - lastlogin::date) as notlogedindays, u.user_description,r.role_name, "
										+ " d.dept_code as dept_code, d.description from mlo_subject_details  md inner join dept_new d on (md.user_id=d.dept_code)"
										+ " inner join users u on (md.emailid=u.userid) inner join user_roles ur on (u.userid=ur.userid) inner join roles_mst r on (ur.role_id=r.role_id)"
										+ " left join (select user_id, min(login_time_date) as firstlogin,"
										+ " count(distinct login_time_date::date) as loggedindays, max(login_time_date) as lastlogin "
										+ " from users_track_time group by user_id) a on (md.emailid=a.user_id) order by d.dept_code";
								
							}
							else {
								sql = "select u.userid, to_char(firstlogin,'DD/MM/YYYY') as firstlogin, loggedindays , to_char(lastlogin,'DD/MM/YYYY') as lastlogin , "
										+ "(case when (lastlogin::date-firstlogin::date) > 1 then (lastlogin::date-firstlogin::date) - (loggedindays -1) else 0 end) + (current_date - lastlogin::date) as notlogedindays, u.user_description,r.role_name, "
										+ " d.dept_code as dept_code, d.description from mlo_details md inner join dept_new d on (md.user_id=d.dept_code)"
										+ " inner join users u on (md.emailid=u.userid) inner join user_roles ur on (u.userid=ur.userid) inner join roles_mst r on (ur.role_id=r.role_id)"
										+ " left join (select user_id, min(login_time_date) as firstlogin,count(distinct login_time_date::date) as loggedindays, max(login_time_date) as lastlogin from users_track_time group by user_id) a on (md.emailid=a.user_id) order by d.dept_code";
			
							}
						}
						
						System.out.println("Login Status SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("LOGIN STATUS DATA:" + data);						
												
						JSONArray finalList = new JSONArray();
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	
								cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("EMP_NAME", entry.get("user_description"));				    	
						    	cases.put("FIRST_LOGIN_DATE", entry.get("firstlogin"));						    	
						    	cases.put("LOGGED_IN_DAYS", entry.get("loggedindays"));						    	
						    	cases.put("NOT_LOGGED_IN_DAYS", entry.get("notlogedindays"));
						    	cases.put("LAST_LOGIN_DATE", entry.get("lastlogin"));
						    	
						    	finalList.put(cases);
							}
							JSONObject casesData = new JSONObject();
							casesData.put("CASE_DATA", finalList);
							String finalString = casesData.toString();
							    
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
														
							} else {
								JSONObject casesData = new JSONObject();
								casesData.put("CASE_DATA", finalList);	
								String finalString = casesData.toString();
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
							}					
						
					}
				} else {
					jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Invalid Format\" }}";
				}
			} else {
				jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No input data\" }}";
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