package in.apcfss.olcms;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
 *        PRD URL : https://aprcrp.apcfss.in/apolcms-services/services/instructions/submitInstructions
 *        TEST URL :http://localhost:8080/apolcms-services/services/instructions/submitInstructions
 * 
 *        {"REQUEST" : {"CINO":"APHC010191782022","USER_ID":"RAMESH.DAMMU@APCT.GOV.IN", "INSTRUCTIONS":"Instructions will be submitted", "ROLE_ID":"5", "DEPT_CODE":"REV03", "DIST_ID":"0"}}
 *		  {"RESPONSE": {"RSPCODE": "01","RSPDESC": "INSTRUCTIONS SAVED SUCCESSFULLY"  }
 *	
 **/

@Path("/instructions")
public class InstructionsSubmissionService {
	@POST
	@Produces({ "application/json" })
	@Consumes({ "application/json" })
	@Path("/submitInstructions")
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
					
					if(!jObject.has("CINO") || jObject.get("CINO").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- CINO is missing in the request.\" }}";
					}
					else if(!jObject.has("INSTRUCTIONS") || jObject.get("INSTRUCTIONS").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- INSTRUCTIONS is missing in the request.\" }}";
					}
					else if(!jObject.has("DEPT_CODE") || jObject.get("DEPT_CODE").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DEPT_CODE is missing in the request.\" }}";
					}
					else if(!jObject.has("DIST_ID") || jObject.get("DIST_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- DIST_ID is missing in the request.\" }}";
					}
					else if(!jObject.has("USER_ID") || jObject.get("USER_ID").toString().equals("")) {
						jsonStr = "{\"RESPONSE\" : {\"RSPCODE\" :\"00\"  ,  \"RSPDESC\" :\"Error:Mandatory parameter- USER_ID is missing in the request.\" }}";
					}
					else {

					String cino=jObject.get("CINO").toString();
					String instructions=jObject.get("INSTRUCTIONS").toString();
					String dept_code=jObject.get("DEPT_CODE").toString();
					int dist_id = Integer.parseInt(jObject.get("DIST_ID").toString());
					String user_id = jObject.get("USER_ID").toString();
					

					//System.out.println("USERID:"+jObject.get("USERID"));
					System.out.println("CINO:"+cino);
					
					if(cino!=null && !cino.equals("")){
						con = DatabasePlugin.connect();
						
						sql = "insert into ecourts_dept_instructions (cino, instructions , upload_fileno,dept_code ,dist_code,insert_by ) "
								+ " values (?,?, ?, ?, ?, ?)";

						ps = con.prepareStatement(sql);
						int i = 1;
						ps.setString(i, cino);
						ps.setString(++i, instructions != null ? instructions : "");
						ps.setObject(++i, uploadedFilePath);
						ps.setString(++i, dept_code);
						ps.setInt(++i, dist_id);
						ps.setString(++i, user_id);


						System.out.println("sql--"+sql);

						a = ps.executeUpdate();

						System.out.println("a--->"+a);
						if(a>0) {
							sql="insert into ecourts_case_activities (cino , action_type , inserted_by , inserted_ip, remarks) "
									+ " values ('" + cino + "','SUBMITTED INSTRUCTIONS TO GP', '"+user_id+"', '"+""+"', '"+instructions+"')";
							DatabasePlugin.executeUpdate(sql, con);
							
							userDetails.put("RSPCODE", "01");
							userDetails.put("RSPDESC", "INSTRUCTIONS SAVED SUCCESSFULLY");
							
						}else {
							userDetails.put("RSPCODE", "00");
							userDetails.put("RSPDESC", "ERROR IN SUBMISSION.KINDLY TRY AGAIN");
							
						}
						jsonStr = "{\"RESPONSE\" : "+userDetails.toString()+"}";
					}
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
