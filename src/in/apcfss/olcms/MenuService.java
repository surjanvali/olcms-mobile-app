package in.apcfss.olcms;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import plugins.DatabasePlugin;

/**
 * @author : Bhanu Krishna Kota
 * @title :
 * 
 *        PRD URL :
 *        https://aprcrp.apcfss.in/apolcms-services/services/menu/displayMenu
 *        TEST URL :
 *        http://localhost:9090/apolcms-services/services/menu/displayMenu
 * 
 *        {"REQUEST" : {"USERID":"DC-ATP", "ROLE_ID":"", "DEPT_CODE":"", "DIST_ID":""}}
		  {"RESPONSE" : {"TOTAL":"","ASSIGNMENT_PENDING":"", "APPROVAL_PENDING":"","CLOSED":"","NEWCASES":"", "FINAL_ORDERS":"", "INTERIM_CASES":"", "INTERIM_ORDERS":""}}		
 **/

@Path("/menu")
public class MenuService {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displayMenu")
	public static Response viewMenu(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";PreparedStatement ps = null;ResultSet rs = null;
		try {
			if (incomingData != null && !incomingData.toString().trim().equals("")) {
				JSONObject jObject1 = new JSONObject(incomingData);

				System.out.println("jObject1:" + jObject1);
				if (jObject1.has("REQUEST") && jObject1.get("REQUEST") != null && !jObject1.get("REQUEST").toString().trim().equals("")) {

					JSONObject jObject = new JSONObject(jObject1.get("REQUEST").toString().trim());
					System.out.println("jObject:" + jObject);
					
					if(!jObject.has("USERID") || jObject.get("USERID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid userId.\" }}";
					}
					else if(!jObject.has("ROLE_ID") || jObject.get("ROLE_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Invalid RoleId.\" }}";
					}

					String roleId=jObject.get("ROLE_ID").toString();
					String userid=jObject.get("USERID").toString();

					//System.out.println("USERID:"+jObject.get("USERID"));
					System.out.println("ROLE_ID:"+roleId);
					
					if(roleId!=null && !roleId.equals("")){
					
					/* START - Code for displaying custom services menu based on role */
					sql = "SELECT service_name,target,show_icon as icon,has_childs as has_child,parent_id,a.service_id,display_id FROM services_mobile a inner join role_services_mobile b on (a.service_id=b.service_id) where b.role_id=?  "
							+ " " + " union "
							+ " SELECT service_name,target,show_icon as icon,has_childs as has_child,parent_id,a1.service_id,display_id FROM services_mobile a1 inner join user_services_mobile b1 on (a1.service_id=b1.service_id) where b1.user_id=? "
							+ " " + " order by 7,5,6";

					System.out.println("Display Left hand menu sql ............ " + sql);
					System.out.println("roleId:" + roleId);
					System.out.println("userId:" + userid);
					//Class.forName("org.postgresql.Driver");
					 con = DatabasePlugin.connect(); //DriverManager.getConnection(CommonVariables.dataBase, CommonVariables.userName, CommonVariables.password);
					//Statement stmt = conn.createStatement();
					//java.util.List data= new ArrayList();
					ps = con.prepareStatement(sql);
					DatabasePlugin.setDefaultParameters(ps, 1, roleId, "Int");
					DatabasePlugin.setDefaultParameters(ps, 2, userid, "String");
					rs = ps.executeQuery();
					List<Map<String, Object>> services = DatabasePlugin.processResultSet(rs);
					
					System.out.println("***********LIST OF MENU ITEMS FOR THE ROLE ID :" + roleId + "*********:" +services);
					
					
					if (services != null && !services.isEmpty()) {

						List<JSONObject> menuList = new ArrayList<JSONObject>();

						for (Map<String, Object> map : services) {

							JSONObject parentMenu = new JSONObject();
							if (map.get("parent_id").equals(0) && map.get("has_child").equals(true)) {
								// System.out.println(map.get("service_name"));
								parentMenu.put("parentMenu", map.get("service_name"));

								List<JSONObject> childMenuList = new ArrayList<JSONObject>();
								for (Map<String, Object> innerMap : services) {
									JSONObject childMenu = new JSONObject();
									if (map.get("service_id").equals(innerMap.get("parent_id"))) {
										// System.out.println("--" + innerMap.get("service_name"));
										childMenu.put("childMenu", innerMap.get("service_name"));
										childMenuList.add(childMenu);
									}
								}
								parentMenu.put("ChildMenus", childMenuList);

							} else if (map.get("parent_id").equals(0) && map.get("has_child").equals(false)) {
								// System.out.println(map.get("service_name"));
								parentMenu.put("parentMenu", map.get("service_name"));

							}
							if (parentMenu.length() > 0)
								menuList.add(parentMenu);

						}

						System.out.println(menuList.toString());
						//responseString.put("response", menuList.toString());
						jsonStr = "{\"RESPONSE\" : " + menuList.toString() + "}";
					}
					
					/* END - Code for displaying custom services menu based on role */

					

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
}
