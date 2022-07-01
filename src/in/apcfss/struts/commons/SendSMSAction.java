package in.apcfss.struts.commons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import plugins.DatabasePlugin;

import com.google.gson.Gson;

public class SendSMSAction {

	public static final String requestUrl = "http://cdacsms.apcfss.in/services/APCfssSmsGateWayReq/sendTextSms";
	public static final String userpass = "PRPNUSER:PRPNU$#R"; // Production
																// Credentials

	public static boolean sendSMS(String mobileNo, String smsText,
			String templateId, Connection con) throws JSONException {
		boolean retVal=false;
		try {
			
			if (mobileNo != null && !mobileNo.equals("")
					&& mobileNo.length() == 10 && smsText != null
					&& !smsText.equals("") && templateId != null
					&& !templateId.equals("")
			) {
				SMSForm smsForm = new SMSForm();

				smsForm.setSMSID(generateRandomNo());
				smsForm.setMOBNO(mobileNo);
				smsForm.setSMSTEXT(smsText);
				smsForm.setPROJCODE("CFMS-IMS");
				smsForm.setTEMPLATEID(templateId);// "1007713986799127731"
	
				Gson gsonObj = new Gson();
				String jsonStr = gsonObj.toJson(smsForm);
	
				jsonStr = "{\"REQUEST\":" + jsonStr + "}";
				System.out.println("Request=" + jsonStr.toString());
				String resp = sendPostRequest(requestUrl, userpass, jsonStr);
				System.out.println("RESP:" + resp);
	
				JSONObject respObj = new JSONObject(resp);
				JSONObject respObj2 = new JSONObject(respObj.get("RESPONSE").toString());
				System.out.println(respObj2.get("RSPCODE"));
				
				if (respObj2.get("RSPCODE").equals("01")) {
					//retVal = respObj2.get("RSPCODE").toString();
					retVal = true;
				}
			
				if(con!=null){
					String sql="insert into olcms_sms_sent (sms_id, sms_text, sms_sent_time, mobile_no, sms_resp_code) values ('"+smsForm.getSMSID()+"','"+smsForm.getSMSTEXT()+"', now(), '"+smsForm.getMOBNO()+"','"+respObj2.get("RSPCODE").toString()+"')";
					DatabasePlugin.executeUpdate(sql, con);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	public static String generateRandomNo() {
		String randomNo = null;
		Date d1 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		randomNo = "OLCMS" + sdf.format(d1);
		return randomNo;
	}

	public static String sendPostRequest(String requestUrl,
			String userCredentials, String payload) {
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();

			String userpass = userCredentials;
			String basicAuth = "Basic "
					+ javax.xml.bind.DatatypeConverter
							.printBase64Binary(userpass.getBytes());
			connection.setRequestProperty("Authorization", basicAuth);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type",
					"application/json; charset=UTF-8");

			System.out.println("connection:" + connection);

			OutputStreamWriter writer = new OutputStreamWriter(
					connection.getOutputStream(), "UTF-8");
			writer.write(payload);
			writer.close();

			int respCode = connection.getResponseCode();
			// System.out.println(""+respCode);
			if (respCode == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					jsonString.append(line);
				}
				br.close();
			} else {
				jsonString.append("" + respCode);
			}
			connection.disconnect();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return jsonString.toString();
	}

}
