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


@Path("/contemptCases")
public class ContemptCasesAbstractReport {
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getSectDeptWiseAbstract")
	public static Response getDeptWiseAbstract(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
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

						String roleId = jObject.get("ROLE_ID").toString();
						String dept_code = jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						
						if (roleId.equals("5") || roleId.equals("9")) {

							return getHODDeptWiseAbstract(incomingData);
						}

						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and a.dist_id='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and a.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString().trim() + "' ";
						}
						
						if (jObject.has("SELECTED_REG_YEAR") && jObject.get("SELECTED_REG_YEAR") != null
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("")
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("0")) {
							sqlCondition += " and a.reg_year='" + CommonModels.checkIntObject(jObject.get("SELECTED_REG_YEAR").toString()) + "' ";
						}
						
						if (jObject.has("SELECTED_RESPONDENT_NAME") && jObject.get("SELECTED_RESPONDENT_NAME") != null
								&& !jObject.get("SELECTED_RESPONDENT_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(res_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_RESPONDENT_NAME").toString()+"%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(pet_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_PETITIONER_NAME").toString()+"%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							
							sqlCondition += " and a.dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and a.dt_regis <= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}						
						
						
						sql = "select x.reporting_dept_code as deptcode, upper(d1.description) as description,sum(total_cases) as total_cases,sum(withsectdept) as withsectdept,sum(withmlo) as withmlo,sum(withhod) as withhod,sum(withnodal) as withnodal,sum(withsection) as withsection, sum(withdc) as withdc, sum(withdistno) as withdistno,sum(withsectionhod) as withsectionhod, sum(withsectiondist) as withsectiondist, sum(withgpo) as withgpo, sum(closedcases) as closedcases, sum(goi) as goi, sum(psu) as psu, sum(privatetot) as privatetot  from ("
								+ "select a.dept_code , case when reporting_dept_code='CAB01' then d.dept_code else reporting_dept_code end as reporting_dept_code,count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_case_data a " + "inner join dept_new d on (a.dept_code=d.dept_code) "
								+ "where d.display = true  and a.case_type_id='6' " + sqlCondition;
						

						if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9"))
							sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='"
									+ dept_code + "')";

						if (roleId.equals("2")) {
							sql += " and a.dist_id='" + dist_id + "' ";
						}

						sql += " group by a.dept_code,d.dept_code ,reporting_dept_code ) x inner join dept_new d1 on (x.reporting_dept_code=d1.dept_code)"
								+ " group by x.reporting_dept_code, d1.description order by 1";
						

						System.out.println("SHOW SEC DEPT WISE SQL: " + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isDataAvailable = false;

						if (data != null && !data.isEmpty() && data.size() > 0) {

							for (Map<String, Object> entry : data) {
								JSONObject cases = new JSONObject();
								cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
								
								
								finalList.put(cases);
							}

							casesData.put("DEPT_WISE_LIST", finalList);
							isDataAvailable = true;

						} else {

							casesData.put("DEPT_WISE_LIST", finalList);

						}

						String finalString = casesData.toString();

						if (isDataAvailable)
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "
									+ finalString.substring(1, finalString.length() - 1) + "}}";
						else
							jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "
									+ finalString.substring(1, finalString.length() - 1) + " }}";

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
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getHODDeptWiseAbstract")
	public static Response getHODDeptWiseAbstract(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
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
						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and a.dist_id='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and a.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString().trim() + "' ";
						}
						
						if (jObject.has("SELECTED_REG_YEAR") && jObject.get("SELECTED_REG_YEAR") != null
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("")
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("0")) {
							sqlCondition += " and a.reg_year='" + CommonModels.checkIntObject(jObject.get("SELECTED_REG_YEAR").toString()) + "' ";
						}
						
						if (jObject.has("SELECTED_RESPONDENT_NAME") && jObject.get("SELECTED_RESPONDENT_NAME") != null
								&& !jObject.get("SELECTED_RESPONDENT_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(res_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_RESPONDENT_NAME").toString()+"%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(pet_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_PETITIONER_NAME").toString()+"%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							
							sqlCondition += " and a.dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and a.dt_regis <= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}	
						
						if (roleId.equals("2")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						
						
						if (jObject.has("SELECTED_DEPT_CODE") && !jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
								dept_code = jObject.get("SELECTED_DEPT_CODE").toString();	
						}
						else {
								dept_code = jObject.get("DEPT_CODE").toString();	
						}
						
						sql = "select a.dept_code as deptcode , upper(d.description) as description,count(*) as total_cases, "
								+ "sum(case when case_status=1 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectdept, "
								+ "sum(case when (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withmlo, "
								+ "sum(case when case_status=3  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withhod, "
								+ "sum(case when case_status=4  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withnodal, "
								+ "sum(case when case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsection, "
								+ "sum(case when case_status=7  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdc, "
								+ "sum(case when case_status=8  and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withdistno, "
								+ "sum(case when case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectionhod, "
								+ "sum(case when case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withsectiondist, "
								+ "sum(case when case_status=6 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as withgpo, "
								+ "sum(case when case_status=99 or coalesce(ecourts_case_status,'')='Closed' then 1 else 0 end) as closedcases, "
								+ "sum(case when case_status=96 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as goi, "
								+ "sum(case when case_status=97 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as psu, "
								+ "sum(case when case_status=98 and coalesce(ecourts_case_status,'')!='Closed' then 1 else 0 end) as privatetot "
								+ "from ecourts_case_data a " + "inner join dept_new d on (a.dept_code=d.dept_code) "
								+ "where d.display = true  and a.case_type_id='6' and (d.reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code
								+ "') " + sqlCondition + "group by a.dept_code , d.description order by 1";
						
						
						System.out.println("HOD DEPT WISE NEW CASE DATA SQL:" + sql);
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("HOD DEPT WISE NEW CASE DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
						    	cases.put("DEPT_CODE", entry.get("deptcode").toString());						    	
						    	cases.put("DEPT_NAME", entry.get("description").toString());
						    	cases.put("TOTAL_CASES", entry.get("total_cases"));
						    	cases.put("PENDING_WITH_SECT_DEPT", entry.get("withsectdept"));
						    	cases.put("PENDING_WITH_MLO", entry.get("withmlo").toString());
						    	cases.put("PENDING_WITH_HOD", entry.get("withhod").toString());
						    	cases.put("PENDING_WITH_NODAL", entry.get("withnodal").toString());
						    	cases.put("PENDING_WITH_SECTION_SECT_DEPT", entry.get("withsection").toString());
						    	cases.put("PENDING_WITH_SECTION_HOD", entry.get("withsectionhod").toString());
						    	cases.put("PENDING_WITH_DIST_COLLECTOR", entry.get("withdc").toString());
						    	cases.put("PENDING_WITH_DIST_NODAL", entry.get("withdistno").toString());
						    	cases.put("PENDING_WITH_SECTION_DIST", entry.get("withsectiondist").toString());
						    	cases.put("PENDING_WITH_GPO", entry.get("withgpo").toString());
						    	cases.put("CLOSED", entry.get("closedcases").toString());
						    	cases.put("GOI", entry.get("goi").toString());
						    	cases.put("PSU", entry.get("psu").toString());
						    	cases.put("PRIVATE", entry.get("privatetot").toString());
						    	finalList.put(cases);
							}
							
								casesData.put("DEPT_WISE_LIST", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("DEPT_WISE_LIST", finalList);									
							}
						
						
							String finalString = casesData.toString();
							
							if (isNewDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
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
	
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/getCasesList")
	public static Response getCasesList(String incomingData) throws Exception {
		Connection con = null;
		String jsonStr = "",sql="";String sqlCondition = "";
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
					else if(!jObject.has("CASE_STATUS") || jObject.get("CASE_STATUS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CASE_STATUS is missing in the request.\" }}";
					}
					else if(!jObject.has("REPORT_LEVEL") || jObject.get("REPORT_LEVEL").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- REPORT_LEVEL is missing in the request.\" }}";
					}
					else {
						
						String roleId=jObject.get("ROLE_ID").toString();
						String dept_code=jObject.get("DEPT_CODE").toString();
						int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
						String user_id = jObject.get("USER_ID").toString();
						String caseStatus = jObject.get("CASE_STATUS").toString();
						String report_level = jObject.get("REPORT_LEVEL").toString();
						
						if (jObject.has("SELECTED_DIST_ID") && jObject.get("SELECTED_DIST_ID") != null
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("")
								&& !jObject.get("SELECTED_DIST_ID").toString().equals("0")) {
							sqlCondition += " and a.dist_id='" + jObject.get("SELECTED_DIST_ID").toString().trim() + "' ";
						}
						if (jObject.has("SELECTED_DEPT_ID") && jObject.get("SELECTED_DEPT_ID") != null
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("")
								&& !jObject.get("SELECTED_DEPT_ID").toString().equals("0")) {
							sqlCondition += " and a.dept_code='" + jObject.get("SELECTED_DEPT_ID").toString().trim() + "' ";
						}
						
						if (jObject.has("SELECTED_REG_YEAR") && jObject.get("SELECTED_REG_YEAR") != null
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("")
								&& !jObject.get("SELECTED_REG_YEAR").toString().equals("0")) {
							sqlCondition += " and a.reg_year='" + CommonModels.checkIntObject(jObject.get("SELECTED_REG_YEAR").toString()) + "' ";
						}
						
						if (jObject.has("SELECTED_RESPONDENT_NAME") && jObject.get("SELECTED_RESPONDENT_NAME") != null
								&& !jObject.get("SELECTED_RESPONDENT_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(res_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_RESPONDENT_NAME").toString()+"%'";
						}
						if (jObject.has("SELECTED_PETITIONER_NAME") && jObject.get("SELECTED_PETITIONER_NAME") != null
								&& !jObject.get("SELECTED_PETITIONER_NAME").toString().equals("")) {
							sqlCondition += " and replace(replace(pet_name,' ',''),'.','') ilike  '%"+jObject.get("SELECTED_PETITIONER_NAME").toString()+"%'";
						}
						
						if (jObject.has("FROM_DATE") && jObject.get("FROM_DATE") != null && !jObject.get("FROM_DATE").toString().equals("")) {
							
							sqlCondition += " and a.dt_regis >= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}
						if (jObject.has("TO_DATE") && jObject.get("TO_DATE") != null && !jObject.get("TO_DATE").toString().equals("")) {
							sqlCondition += " and a.dt_regis <= to_date('" + jObject.get("FROM_DATE").toString() + "','dd-mm-yyyy') ";
						}	
						
						
						
						if (!caseStatus.equals("")) {
							if (caseStatus.equals("withSD")) {
								sqlCondition = " and case_status=1 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withMLO")) {
								sqlCondition = " and (case_status is null or case_status=2)  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHOD")) {
								sqlCondition = " and case_status=3  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withNO")) {
								sqlCondition = " and case_status=4  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withSDSec")) {
								sqlCondition = " and case_status=5 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDC")) {
								sqlCondition = " and case_status=7  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistNO")) {
								sqlCondition = " and case_status=8  and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withHODSec")) {
								sqlCondition = " and case_status=9 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withDistSec")) {
								sqlCondition = " and case_status=10 and coalesce(assigned,'f')='t' and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("withGP")) {
								sqlCondition = " and case_status=6 and coalesce(ecourts_case_status,'')!='Closed' ";
							}
							if (caseStatus.equals("closed")) {
								sqlCondition = " and (case_status=99 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("goi")) {
								sqlCondition = " and (case_status=96 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("psu")) {
								sqlCondition = " and (case_status=97 or coalesce(ecourts_case_status,'')='Closed') ";
							}
							if (caseStatus.equals("Private")) {
								sqlCondition = " and (case_status=98 or coalesce(ecourts_case_status,'')='Closed') ";
							}
						}

						if (roleId.equals("2") || roleId.equals("10")) {
							sqlCondition += " and a.dist_id='" + dist_id + "' ";
						}
						System.out.println("roleId--"+roleId);
						
						String condition="";
						if (roleId.equals("6"))
							condition= " inner join ecourts_mst_gp_dept_map e on (a.dept_code=e.dept_code) ";

						
						
						sql = "select a.*, "
								+ ""
								// + "n.global_org_name as globalorgname, n.fullname_en as fullname, n.designation_name_en as designation, n.mobile1 as mobile, n.email as email, "
								+ ""
								+ "coalesce(trim(a.scanned_document_path),'-') as scanned_document_path1, b.orderpaths, prayer, ra.address from ecourts_case_data a "
								+ " left join nic_prayer_data np on (a.cino=np.cino)"
								+ " left join nic_resp_addr_data ra on (a.cino=ra.cino and party_no=1) "
								//+ "inner join nic_data n on (a.assigned_to=n.email) "
								+ " left join"
								+ " ("
								+ " select cino, string_agg('<a href=\"./'||order_document_path||'\" target=\"_new\" class=\"btn btn-sm btn-info\"><i class=\"glyphicon glyphicon-save\"></i><span>'||order_details||'</span></a><br/>','- ') as orderpaths"
								+ " from "
								+ " (select * from (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_interimorder where order_document_path is not null and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) x1" + " union"
								+ " (select cino, order_document_path,order_date,order_details||' Dt.'||to_char(order_date,'dd-mm-yyyy') as order_details from ecourts_case_finalorder where order_document_path is not null"
								+ " and  POSITION('RECORD_NOT_FOUND' in order_document_path) = 0"
								+ " and POSITION('INVALID_TOKEN' in order_document_path) = 0 ) order by cino, order_date desc) c group by cino ) b"
								+ " on (a.cino=b.cino) inner join dept_new d on (a.dept_code=d.dept_code) "+condition+" where d.display = true  and a.case_type_id='6' ";
						

						if(jObject.has("SELECTED_DEPT_CODE") && jObject.get("SELECTED_DEPT_CODE") != null
								&& !jObject.get("SELECTED_DEPT_CODE").toString().equals("")) {
							dept_code = jObject.get("SELECTED_DEPT_CODE").toString();
						}
					
						 if(report_level.equals("SD")) {
							 sql += " and (reporting_dept_code='" + dept_code + "' or a.dept_code='" + dept_code + "') ";
						 }
						 else {//if(reportLevel.equals("HOD")) {
							 sql += " and a.dept_code='" + dept_code + "' ";
						 }
						 
						 sql += sqlCondition;
						 
						System.out.println("ecourts SQL:" + sql);
						
						
						con = DatabasePlugin.connect();
						List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);
						System.out.println("HOD DEPT WISE NEW CASE DATA =" + data);
						
												
						JSONArray finalList = new JSONArray();
						JSONObject casesData = new JSONObject();
						boolean isNewDataAvailable = false;
						
						if (data != null && !data.isEmpty() && data.size() > 0) {
							
							for (Map<String, Object> entry : data) {								   
								JSONObject cases = new JSONObject();
								cases.put("CINO", entry.get("cino").toString());						    	
						    	cases.put("DATE_OF_FILING", entry.get("date_of_filing").toString());
						    	cases.put("CASE_REG_NO", entry.get("type_name_fil")+"/"+entry.get("reg_no")+"/"+entry.get("reg_year"));
						    	cases.put("PRAYER", entry.get("prayer"));
						    	cases.put("FILING_NO", entry.get("fil_no").toString());
						    	cases.put("FILING_YEAR", entry.get("fil_year").toString());
						    	cases.put("DATE_NEXT_LIST", entry.get("date_of_filing"));
						    	cases.put("BENCH_NAME", entry.get("bench_name"));
						    	cases.put("JUDGE_NAME", "Hon'ble Judge: "+entry.get("coram"));
						    	cases.put("PETITIONER_NAME", entry.get("pet_name"));
						    	cases.put("DIST_NAME", entry.get("dist_name"));
						    	cases.put("PURPOSE_NAME", entry.get("purpose_name"));
						    	cases.put("RESPONDENT_NAME", entry.get("res_name")+","+entry.get("address"));
						    	cases.put("PET_ADV", entry.get("pet_adv"));
						    	cases.put("RES_ADV", entry.get("res_adv"));
						    	
						    	String scannedAffidavitPath="";

								if (entry.get("scanned_document_path1") != null)
								{
									if (entry.get("scanned_document_path1")!=null && !entry.get("scanned_document_path1").equals("-")) {
										scannedAffidavitPath = "https://apolcms.ap.gov.in/" + entry.get("scanned_document_path");
									}
									
								}
								
								cases.put("SCANNED_AFFIDAVIT_PATH", scannedAffidavitPath);
								
								
								JSONArray orderdocumentList = new JSONArray();
								
						    	
						    	if (entry.get("orderpaths") != null)
						    	{
						    		String mydata = entry.get("orderpaths").toString();
						    		Pattern pattern = Pattern.compile("(?<=a href=\".)(.*?)(?=\" target)");
						    		Matcher matcher = pattern.matcher(mydata);
						    		Pattern pattern2 = Pattern.compile("(?<=<span>)(.*?)(?=</span)");
						    		Matcher matcher2 = pattern2.matcher(mydata);
						    		
						            while (matcher.find() && matcher2.find()){
						            	JSONObject orderData = new JSONObject();
						                String s = matcher.group();
						                String s1 = matcher2.group();
						                orderData.put("ORDER_NAME", s1);
						                orderData.put("ORDER_DOC_PATH", "https://apolcms.ap.gov.in/"+s);
						                
						                orderdocumentList.put(orderData);
						            }
						           
						           
						    	}
						    	cases.put("ORDER_PATHS", orderdocumentList);
								
						    	finalList.put(cases);
							}
							
								casesData.put("CASES_LIST", finalList);							
								isNewDataAvailable=true;
														
							} else {								
								casesData.put("CASES_LIST", finalList);									
							}
						
						
							String finalString = casesData.toString();
							
							if (isNewDataAvailable)					    
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases retrived successfully\"  , "+finalString.substring(1,finalString.length()-1)+"}}";
							else
								jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"No Records Found.\", "+finalString.substring(1,finalString.length()-1)+" }}";
						
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
	
	
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/displayCaseFilters")
	public static Response displayCaseFilters(String incomingData) throws Exception {
		
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
					
					//Populate the District list in the District dropdown
					
					if (roleId.equals("2")) {
						sql = "select district_id,upper(district_name) as dist_name from district_mst where district_id='" + distId + "' order by district_name";
					}
						
					else {
						sql = "select district_id,upper(district_name) as dist_name from district_mst order by district_name";
					}
					
					List<Map<String, Object>> data = DatabasePlugin.executeQuery(sql, con);

					JSONArray distList = new JSONArray();
					JSONObject casesData = new JSONObject();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DIST_CODE", entry.get("district_id").toString());								
							cases.put("DIST_NAME", entry.get("dist_name").toString());
							
							distList.put(cases);
						}
					} 
					casesData.put("DIST_LIST", distList);					
					
					
					//Populate the Dept list in the Department dropdown
					
					if (roleId.equals("3") || roleId.equals("4") || roleId.equals("5") || roleId.equals("9")
							|| roleId.equals("10")) {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true";
						sql += " and (reporting_dept_code='" + deptCode + "' or dept_code='"
								+ deptCode + "')";
						sql += "  order by dept_code ";
					}
					else {
						sql = "select dept_code,dept_code||'-'||upper(description) as dept_desc from dept_new where display=true order by dept_code";
					}					
					
					data = DatabasePlugin.executeQuery(sql, con);

					JSONArray deptList = new JSONArray();

					if (data != null && !data.isEmpty() && data.size() > 0) {

						for (Map<String, Object> entry : data) {
							JSONObject cases = new JSONObject();
							cases.put("DEPT_ID", entry.get("dept_code").toString());								
							cases.put("DEPT_DESC", entry.get("dept_desc").toString());
							
							deptList.put(cases);
						}
					} 
					casesData.put("DEPT_LIST", deptList);
					
					
					/* START - Code to populate the registered years select box */
					List selectData = new ArrayList();
					
					
					for (int i = 2022; i > 1980; i--) {
						LinkedHashMap<String,Integer> hm=new LinkedHashMap<String,Integer>();
						hm.put("year", i);
						selectData.add(hm);
					}

					
					casesData.put("YEARS_LIST", new JSONArray(selectData));
					/* END - Code to populate the registered years select box */
					
					
					
					String finalString = casesData.toString();
					
					if (casesData.length()>0)						
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"01\"  , \"RSPDESC\" :\"Cases Filters retrived successfully\"  , "
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