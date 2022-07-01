package in.apcfss.struts.commons;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import plugins.DatabasePlugin;

public class AjaxModels extends DispatchAction {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {

		System.out.println(
				"AjaxModels..............................................................................execute()");
		response.setContentType("text/html");
		PrintWriter out = null;
		Connection con = null;
		PreparedStatement ps = null;
		String userId = null;
		String tableName = "nic_data";
		HttpSession session = request.getSession();
		if (session == null || session.getAttribute("userid") == null || session.getAttribute("role_id") == null) {
			return null;
		}
		ResultSet rs = null;
		try {
			out = response.getWriter();
			String getType = request.getParameter("getType") != null
					|| !request.getParameter("getType").toString().trim().equals("")
							? request.getParameter("getType").toString()
							: null;
			userId = CommonModels.checkStringObject(session.getAttribute("userid"));
			System.out.println(
					"AjaxModels..............................................................................getType="
							+ getType);

			con = DatabasePlugin.connect();
			String mandal = request.getParameter("mandal") != null
					&& Pattern.matches("[0-9]+", request.getParameter("mandal").toString())
							? request.getParameter("mandal")
							: "";
			if (getType != null && !mandal.equals("")) {

				if (getType.equals("parliament")) {

					String sql = "select a.parliament_id,upper(a.parliament_name) as parliament_name from apdrp_parliament_mst a inner join apdrp_parliament_mandal_map b on (a.parliament_id=b.parliament_id) where b.district_id=? and b.mandal_id=?";
					ps = con.prepareStatement(sql);
					DatabasePlugin.setDefaultParameters(ps, 1, session.getAttribute("district_id"), "Int");
					DatabasePlugin.setDefaultParameters(ps, 2, mandal, "Int");
					rs = ps.executeQuery();
					// out.println("<option value='0'>---SELECT---</option>");
					if (rs != null) {
						while (rs.next()) {
							out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
						}
					}
				} else if (getType.equals("assembly")) {
					String sql = "select a.assembly_id,upper(a.assembly_name) as assembly_name from apdrp_assembly_mst a inner join apdrp_assembly_mandal_map b on (a.assembly_id=b.assembly_id) where b.district_id=? and b.mandal_id=?";
					ps = con.prepareStatement(sql);
					DatabasePlugin.setDefaultParameters(ps, 1, session.getAttribute("district_id"), "Int");
					DatabasePlugin.setDefaultParameters(ps, 2, mandal, "Int");
					rs = ps.executeQuery();
					// out.println("<option value='0'>---SELECT---</option>");
					if (rs != null) {
						while (rs.next()) {
							out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
						}
					}
				} else if (getType.equals("village")) {
					String sql = "select village_id::int4,upper(village_name) as village_name from apdrp_village_master where state_id='01' and district_id::int4=? and mandal_id::int4=?";
					System.out.println("sql" + sql);
					ps = con.prepareStatement(sql);
					DatabasePlugin.setDefaultParameters(ps, 1, session.getAttribute("district_id"), "Int");
					DatabasePlugin.setDefaultParameters(ps, 2, mandal, "Int");
					rs = ps.executeQuery();
					out.println("<option value='0'>---SELECT---</option>");
					if (rs != null) {
						while (rs.next()) {
							out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
						}
					}
				}
			} else if (getType.equals("getEmployeesList")) {
				String deptId = request.getParameter("deptId") != null ? request.getParameter("deptId").toString()
						: "-";
				String designationId = request.getParameter("designationId") != null
						? request.getParameter("designationId").toString()
						: "-";
				
				String distId = CommonModels.checkStringObject(session.getAttribute("dist_id"));
				tableName = AjaxModels.getTableName(distId, con);
				
				/*
				 * if (userId != null && userId.equals("DC-ATP")) tableName = "nic_data_atp";
				 * else if (userId != null && userId.equals("DC-CHT")) tableName =
				 * "nic_data_ctr"; else if (userId != null && userId.equals("DC-EG")) tableName
				 * = "nic_data_eg"; else if (userId != null && userId.equals("DC-GNT"))
				 * tableName = "nic_data_gnt"; else if (userId != null &&
				 * userId.equals("DC-KDP")) tableName = "nic_data_kdp"; else if (userId != null
				 * && userId.equals("DC-KNL")) tableName = "nic_data_knl"; else if (userId !=
				 * null && userId.equals("DC-KRS")) tableName = "nic_data_krishna"; else if
				 * (userId != null && userId.equals("DC-NLR")) tableName = "nic_data_nlr"; else
				 * if (userId != null && userId.equals("DC-PRK")) tableName = "nic_data_pksm";
				 * else if (userId != null && userId.equals("DC-SKL")) tableName =
				 * "nic_data_sklm"; else if (userId != null && userId.equals("DC-VSP"))
				 * tableName = "nic_data_vspm"; else if (userId != null &&
				 * userId.equals("DC-VZM")) tableName = "nic_data_vznm"; else if (userId != null
				 * && userId.equals("DC-WG")) tableName = "nic_data_wg"; else tableName =
				 * "nic_data";
				 */
				// String serviceType = request.getParameter("serviceType") != null ?
				// request.getParameter("serviceType").toString() : "-";
				String userType = request.getParameter("userType") != null ? request.getParameter("userType").toString()
						: null;

				String sql = "select distinct employee_id, fullname_en from " + tableName
						+ " where substring(global_org_name,1,5)='" + deptId.substring(0,5)
						+ "' and designation_id=? order by fullname_en";
				// System.out.println("sql="+sql);
				if (userType != null && !userType.equals("0")) {
					if (userType.equals("MLO")) {

						sql = "select distinct employee_id, fullname_en from " + tableName
								+ " where substring(global_org_name,1,5)='" + deptId
								+ "' and designation_id=? and employee_id not in (select employeeid from mlo_details) order by fullname_en";
					} else if (userType.equals("NO")) {
						sql = "select distinct employee_id, fullname_en from " + tableName
								+ " where substring(global_org_name,1,5)='" + deptId
								+ "' and designation_id=? and employee_id not in (select employeeid from nodal_officer_details where dept_id='"
								+ deptId + "') order by fullname_en";
					}
				}
				System.out.println("getEmployeesList : SQL:" + sql);
				ps = con.prepareStatement(sql);
				DatabasePlugin.setDefaultParameters(ps, 1, designationId, "String");
				rs = ps.executeQuery();
				out.println("<option value='0'>---SELECT---</option>");
				if (rs != null) {
					while (rs.next()) {
						out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
					}
				}
			} else if (getType.equals("getDesignationsList")) {
				
				String distId = CommonModels.checkStringObject(session.getAttribute("dist_id"));
				tableName = AjaxModels.getTableName(distId, con);
				
				/*
				 * if (userId != null && userId.equals("DC-ATP")) tableName = "nic_data_atp";
				 * else if (userId != null && userId.equals("DC-CHT")) tableName =
				 * "nic_data_ctr"; else if (userId != null && userId.equals("DC-EG")) tableName
				 * = "nic_data_eg"; else if (userId != null && userId.equals("DC-GNT"))
				 * tableName = "nic_data_gnt"; else if (userId != null &&
				 * userId.equals("DC-KDP")) tableName = "nic_data_kdp"; else if (userId != null
				 * && userId.equals("DC-KNL")) tableName = "nic_data_knl"; else if (userId !=
				 * null && userId.equals("DC-KRS")) tableName = "nic_data_krishna"; else if
				 * (userId != null && userId.equals("DC-NLR")) tableName = "nic_data_nlr"; else
				 * if (userId != null && userId.equals("DC-PRK")) tableName = "nic_data_pksm";
				 * else if (userId != null && userId.equals("DC-SKL")) tableName =
				 * "nic_data_sklm"; else if (userId != null && userId.equals("DC-VSP"))
				 * tableName = "nic_data_vspm"; else if (userId != null &&
				 * userId.equals("DC-VZM")) tableName = "nic_data_vznm"; else if (userId != null
				 * && userId.equals("DC-WG")) tableName = "nic_data_wg";
				 */

				String deptId = request.getParameter("deptId") != null ? request.getParameter("deptId").toString()
						: "-";

				String sql = "select distinct designation_id::int4, designation_name_en from " + tableName
						+ " where substring(global_org_name,1,5)='" + deptId
						+ "'  and trim(upper(designation_name_en))<>'MINISTER' order by designation_id::int4 desc";
				System.out.println(" getDesignationsList sql=" + sql);
				ps = con.prepareStatement(sql);
				rs = ps.executeQuery();
				out.println("<option value='0'>---SELECT---</option>");
				if (rs != null) {
					while (rs.next()) {
						out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
					}
				}
			} else if (getType.equals("getEmpDetails")) {
				System.out.println("getType : getEmpDetails");
				
				String distId = CommonModels.checkStringObject(session.getAttribute("dist_id"));
				tableName = AjaxModels.getTableName(distId, con);
				
				/*
				 * if (userId != null && userId.equals("DC-ATP")) tableName = "nic_data_atp";
				 * else if (userId != null && userId.equals("DC-CHT")) tableName =
				 * "nic_data_ctr"; else if (userId != null && userId.equals("DC-EG")) tableName
				 * = "nic_data_eg"; else if (userId != null && userId.equals("DC-GNT"))
				 * tableName = "nic_data_gnt"; else if (userId != null &&
				 * userId.equals("DC-KDP")) tableName = "nic_data_kdp"; else if (userId != null
				 * && userId.equals("DC-KNL")) tableName = "nic_data_knl"; else if (userId !=
				 * null && userId.equals("DC-KRS")) tableName = "nic_data_krishna"; else if
				 * (userId != null && userId.equals("DC-NLR")) tableName = "nic_data_nlr"; else
				 * if (userId != null && userId.equals("DC-PRK")) tableName = "nic_data_pksm";
				 * else if (userId != null && userId.equals("DC-SKL")) tableName =
				 * "nic_data_sklm"; else if (userId != null && userId.equals("DC-VSP"))
				 * tableName = "nic_data_vspm"; else if (userId != null &&
				 * userId.equals("DC-VZM")) tableName = "nic_data_vznm"; else if (userId != null
				 * && userId.equals("DC-WG")) tableName = "nic_data_wg";
				 */
				String empId = request.getParameter("empId") != null ? request.getParameter("empId").toString() : null;
				if (empId != null) {
					String sql = "select distinct employee_id||'#'||replace(mobile1, 'NULL', '')||'#'||replace(email, 'NULL', '')||'#'||replace(uid, 'NULL', '')  from "
							+ tableName + " where employee_id ='" + empId + "' ";
					System.out.println("SQL:" + sql);
					out.println(DatabasePlugin.getSingleValue(con, sql));
				}
			}

			else if (getType.equals("getEmpDeptSectionsList")) {
				// String sql="select sdeptcode from dept where
				// dept_id='"+request.getParameter("empDept")+"'";
				String sql = "";
				String deptCode = (String) request.getParameter("empDept");// DatabasePlugin.getStringfromQuery(sql,
																			// con);
				String distCode = (String) request.getParameter("distCode");

				tableName = getTableName(distCode, con);

				if (deptCode != null && deptCode != "")
					sql = "select trim(employee_identity),trim(employee_identity) from " + tableName
							+ " where substr(trim(global_org_name),1,5)=? and trim(employee_identity)!='NULL' "
							+ " and trim(email) not in (select userid from user_roles where role_id in (4,5)) group by trim(employee_identity) order by 1";
				System.out.println(deptCode + ":getEmpDeptSectionsList sql:" + sql);
				ps = con.prepareStatement(sql);
				DatabasePlugin.setDefaultParameters(ps, 1, deptCode, "String");
				rs = ps.executeQuery();
				if (rs != null) {
					out.println("<option value='0'>---SELECT---</option>");
					while (rs.next()) {
						out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
					}
				} else {
					// out.println("Error occurred. Pls Login and try again.");
				}
			} else if (getType.equals("getEmpPostsList")) {
				// String sql="select sdeptcode from dept where
				// dept_id='"+request.getParameter("empDept")+"'";
				String sql = "";
				String deptCode = (String) request.getParameter("empDept");// DatabasePlugin.getStringfromQuery(sql,
																			// con);
				String distCode = (String) request.getParameter("distCode");

				tableName = getTableName(distCode, con);

				if (deptCode != null && deptCode != "")
					sql = "select trim(post_name_en), trim(post_name_en) from " + tableName
							+ " where substr(trim(global_org_name),1,5)=? and trim(employee_identity)!='NULL' and trim(employee_identity)=trim(?) "
							+ "  and trim(email) not in (select userid from user_roles where role_id in (4,5)) group by post_name_en";
				System.out.println(deptCode + ":getEmpPostsList sql:" + sql);
				ps = con.prepareStatement(sql);
				DatabasePlugin.setDefaultParameters(ps, 1, deptCode, "String");
				DatabasePlugin.setDefaultParameters(ps, 2, request.getParameter("empSec"), "String");
				rs = ps.executeQuery();
				if (rs != null) {
					out.println("<option value='0'>---SELECT---</option>");
					while (rs.next()) {
						out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
					}
				} else {
					// out.println("Error occurred. Pls Login and try again.");
				}
			} else if (getType.equals("getEmpsList")) {

				// String sql="select sdeptcode from dept where
				// dept_id='"+request.getParameter("empDept")+"'";
				String sql = "";
				String deptCode = (String) request.getParameter("empDept");// DatabasePlugin.getStringfromQuery(sql,
																			// con);

				String distCode = (String) request.getParameter("distCode");

				tableName = getTableName(distCode, con);

				if (deptCode != null && deptCode != "")
					sql = "select distinct trim(employee_id), trim(fullname_en)||' - '||trim(designation_name_en) from "
							+ tableName
							+ " where employee_identity=? and post_name_en=? and substr(trim(global_org_name),1,5)=? "
							+ " and trim(email) not in (select userid from user_roles where role_id in (4,5))  ";
				System.out.println(deptCode + ":getEmpsList :sql" + sql);
				ps = con.prepareStatement(sql);
				DatabasePlugin.setDefaultParameters(ps, 1, request.getParameter("empSec"), "String");
				DatabasePlugin.setDefaultParameters(ps, 2, request.getParameter("empPost"), "String");
				DatabasePlugin.setDefaultParameters(ps, 3, deptCode, "String");
				rs = ps.executeQuery();

				if (rs != null) {
					out.println("<option value='0'>---SELECT---</option>");
					while (rs.next()) {
						out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
					}
				} else {
					// out.println("Error occurred. Pls Login and try again.");
				}
			} else if (getType.equals("getDeptList")) {

				String sql = "";
				String deptCode = (String) request.getSession().getAttribute("dept_code");
				String typeCode = (String) request.getParameter("typeCode");

				if (typeCode != null && !typeCode.equals("") && !typeCode.equals("0")) {
					// chkdVal=="S-HOD" || chkdVal=="D-HOD"
					// chkdVal=="SD-SO" || chkdVal=="OD-SO"
					if (deptCode != null && deptCode != "" && deptCode != "0") {

						if (typeCode.equals("S-HOD") || typeCode.equals("SD-SO")) {

							if (deptCode.substring(3, 5).equals("01")) {
								sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where (dept_code='"
										+ deptCode + "' or reporting_dept_code='" + deptCode
										+ "') and display=true order by dept_code";
							} else {
								sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where reporting_dept_code in (select reporting_dept_code from dept_new where dept_code='"
										+ deptCode + "') and display=true order by dept_code";
							}
							// sql = "select sdeptcode||deptcode, sdeptcode||deptcode||'-'||description from
							// dept where sdeptcode='"+deptCode.substring(0,3)+"' order by
							// sdeptcode,deptcode"; // and deptcode!='01'
						} else if (typeCode.equals("D-HOD") || typeCode.equals("OD-SO")) {
							// sql = "select sdeptcode||deptcode, sdeptcode||deptcode||'-'||description from
							// dept where sdeptcode!='"+deptCode.substring(0,3)+"' order by
							// sdeptcode,deptcode";
							sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where (dept_code!='"
									+ deptCode + "' and reporting_dept_code!='" + deptCode
									+ "') and display=true order by dept_code";
						} else if (typeCode.equals("DC-SO")) {
							// sql = "select sdeptcode||deptcode, sdeptcode||deptcode||'-'||description from
							// dept where sdeptcode!='"+deptCode.substring(0,3)+"' order by
							// sdeptcode,deptcode";
							sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where deptcode!='01' and display=true order by dept_code";
						} else {
							// sql = "select sdeptcode||deptcode, sdeptcode||deptcode||'-'||description from
							// dept where dept_id!=null order by sdeptcode,deptcode";
							sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where display=true order by dept_code";
						}
						System.out.println(deptCode + ":getEmpsList :sql" + sql);
						ps = con.prepareStatement(sql);
						rs = ps.executeQuery();
						if (rs != null) {
							out.println("<option value='0'>---SELECT---</option>");
							while (rs.next()) {
								out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
							}
						} else {
							// out.println("Error occurred. Pls Login and try again.");
						}
					}else if(CommonModels.checkStringObject(request.getSession().getAttribute("role_id")).equals("2")) {
						sql = "select dept_code,dept_code||'-'||upper(description) from dept_new where display=true order by dept_code";
						System.out.println(deptCode + ":getEmpsList :sql" + sql);
						ps = con.prepareStatement(sql);
						rs = ps.executeQuery();
						if (rs != null) {
							out.println("<option value='0'>---SELECT---</option>");
							while (rs.next()) {
								out.println("<option value='" + rs.getString(1) + "'>" + rs.getString(2) + "</option>");
							}
						} else {
							// out.println("Error occurred. Pls Login and try again.");
						}
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			DatabasePlugin.closeConnection(con);
		}
		return null;
	}
	
	public static String getTableName(String distId, Connection con) {
		String tableName = "nic_data";
		if(distId!=null && !distId.equals("") && Integer.parseInt(distId) > 0)
			tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst where district_id="+distId, con);
			// tableName = DatabasePlugin.getStringfromQuery("select tablename from district_mst_new where district_id="+distId, con);
		System.out.println("dist::Id"+distId+"-tableName::"+tableName);
		return tableName;
	}

	public static String getTableNameOld(String distId) {
		String tableName = "nic_data";
		
		switch (CommonModels.checkIntObject(distId)) {
		case 14:
			tableName = "nic_data_atp";
			break;
		case 15:
			tableName = "nic_data_ctr";
			break;
		case 16:
			tableName = "nic_data_eg";
			break;
		case 17:
			tableName = "nic_data_gnt";
			break;
		case 18:
			tableName = "nic_data_kdp";
			break;
		case 19:
			tableName = "nic_data_krishna";
			break;
		case 20:
			tableName = "nic_data_knl";
			break;
		case 21:
			tableName = "nic_data_nlr";
			break;
		case 22:
			tableName = "nic_data_pksm";
			break;
		case 23:
			tableName = "nic_data_sklm";
			break;
		case 24:
			tableName = "nic_data_vspm";
			break;
		case 25:
			tableName = "nic_data_vznm";
			break;
		case 26:
			tableName = "nic_data_wg";
			break;
		}

		return tableName;
	}

}