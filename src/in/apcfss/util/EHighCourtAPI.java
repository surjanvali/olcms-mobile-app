package in.apcfss.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

public class EHighCourtAPI {

	public static final String deptId = "SE00031";
	// CNR SEARCH API
	public static final String apiURL = "https://egw.bharatapi.gov.in/t/ecourts.gov.in/";// cnrFullCaseDetails?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version

	public static final String version = "v1.0";

	/*
	 6 CASE SEARCH APIS..........................................................................................................................................................12
		6.1 SEARCH BY CNR API....................................................................................................................................................12
		6.2 SHOW BUSINESS API ...................................................................................................................................................19
		6.3 SHOW ORDERS API .....................................................................................................................................................21
		6.4 SEARCH BY CNR (CURRENT CASE STATUS) API .................................................................................................................23
		6.5 SEARCH BY CASE NUMBER API.......................................................................................................................................28
		6.6 SEARCH BY FILING NUMBER API.....................................................................................................................................30
		6.7 SEARCH BY PARTY NAME API ........................................................................................................................................31
		6.8 SEARCH BY ADVOCATE NAME API ...................................................................................................................................33
		6.9 SEARCH BY ADVOCATE BAR REGISTRATION NUMBER API.....................................................................................................35
		6.10 SEARCH BY ACT API .....................................................................................................................................................37 
	 * */
	
	public static void main(String[] args) throws Exception {
		String inputStr = "";
		try {
			// 6.1 SEARCH BY CNR API
			//inputStr = "cino=APHC010419392018";
			inputStr = "cino=APHC010665392018";
			//System.out.println("in main:" + inputStr);
			searchByCNR(inputStr);
			// Response Str After Decryption : {"date_of_filing":"2018-09-05","cino":"APHC010665392018","dt_regis":"2018-09-06","type_name_fil":"WP","type_name_reg":"WP","case_type_id":63,"fil_no":"51379","fil_year":"2018","reg_no":"32118","reg_year":"2018","date_first_list":"2018-09-07","date_next_list":"","pend_disp":"P","date_of_decision":"","disposal_type":null,"bench_type":2,"causelist_type":"CAUSE LIST MOTION HEARING","bench_name":"Division Bench","judicial_branch":"WRIT Section","coram":"AHSANUDDIN AMANULLAH , B KRISHNA MOHAN","short_order":"ADJOURNED","desgname":"The Honourable Sri Justice","bench_id":"3295","court_est_name":"High Court of aphc","est_code":"APHC01","state_name":"ANDHRAPRADESH","dist_name":"KURNOOL","purpose_name":"ADMISSION","pet_name":"N.Ramudu,","pet_adv":"M R TAGORE","pet_legal_heir":"N","res_name":"The Superintending Engineer,","res_adv":"GP FOR SERVICES III","res_legal_heir":"N","main_matter":"","fir_no":"","police_station":null,"uniform_code":null,"police_st_code":"","fir_year":"","lower_court_name":"","lower_court_caseno":"","lower_court_dec_dt":"","trial_lower_court_name":"","trial_lower_court_caseno":"","trial_lower_court_dec_dt":"","date_last_list":"2021-11-16","main_matter_cino":"","date_filing_disp":"","reason_for_rej":"","acts":{"act1":{"actname":"01- CONSTITUTION OF INDIA","section":"226"}},"pet_extra_party":[],"res_extra_party":{"party_no1":"The Engineer-in-Chief","party_no2":"The State of Andhra Pradesh","party_no3":"The State of Andhra Pradesh"},"historyofcasehearing":{"sr_no1":{"judge_name":"","business_date":"","hearing_date":"2018-09-07","purpose_of_listing":"FOR ADMISSION"},"sr_no2":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-09-07","hearing_date":"2018-10-04","purpose_of_listing":"FOR ADMISSION","causelist_type":"DAILY LIST"},"sr_no3":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-10-04","hearing_date":"2018-10-25","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no4":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-10-25","hearing_date":"2018-11-15","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no5":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-11-15","hearing_date":"2018-11-29","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no6":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-11-29","hearing_date":"2018-11-30","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no7":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-11-30","hearing_date":"2018-12-19","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no8":{"judge_name":"SANJAY KUMAR , M.GANGA RAO","business_date":"2018-12-19","hearing_date":"2019-02-11","purpose_of_listing":"ADMISSION","causelist_type":"DAILY LIST"},"sr_no9":{"judge_name":"A V SESHA SAI","business_date":"2019-12-04","hearing_date":"2019-12-14","purpose_of_listing":"FOR REFERENCE TO LOK ADALAT","causelist_type":"NATIONAL LOK ADALAT PRE-SITTING LIST"},"sr_no10":{"judge_name":"A V SESHA SAI","business_date":"2019-12-14","hearing_date":"2019-12-17","purpose_of_listing":"REFERRED TO LOK ADALAT","causelist_type":"NATIONAL LOK ADALAT"},"sr_no11":{"judge_name":"JOYMALYA BAGCHI , KONGARA VIJAYA LAKSHMI","business_date":"2021-09-07","hearing_date":"2021-09-14","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no12":{"judge_name":"JOYMALYA BAGCHI , KONGARA VIJAYA LAKSHMI","business_date":"2021-09-14","hearing_date":"2021-09-28","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no13":{"judge_name":"JOYMALYA BAGCHI , KONGARA VIJAYA LAKSHMI","business_date":"2021-09-28","hearing_date":"2021-10-05","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no14":{"judge_name":"JOYMALYA BAGCHI , KONGARA VIJAYA LAKSHMI","business_date":"2021-10-05","hearing_date":"2021-11-08","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no15":{"judge_name":"AHSANUDDIN AMANULLAH , B KRISHNA MOHAN","business_date":"2021-11-08","hearing_date":"2021-11-15","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no16":{"judge_name":"AHSANUDDIN AMANULLAH , B KRISHNA MOHAN","business_date":"2021-11-15","hearing_date":"2021-11-16","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"},"sr_no17":{"judge_name":"AHSANUDDIN AMANULLAH , B KRISHNA MOHAN","business_date":"2021-11-16","hearing_date":"5000-01-01","purpose_of_listing":"ADMISSION","causelist_type":"CAUSE LIST MOTION HEARING"}},"interimorder":{"sr_no1":{"order_no":"1","order_date":"2021-09-07","order_details":"Court Proceedings"},"sr_no2":{"order_no":"2","order_date":"2021-09-14","order_details":"Court Proceedings"},"sr_no3":{"order_no":"3","order_date":"2021-09-28","order_details":"Court Proceedings"},"sr_no4":{"order_no":"4","order_date":"2021-10-05","order_details":"Court Proceedings"},"sr_no5":{"order_no":"5","order_date":"2021-11-08","order_details":"Court Proceedings"}},"finalorder":null,"iafiling":{"sr_no1":{"ia_number":"IA\/1\/2018  <\/br> <b>Classification :<\/b> Direction Petition","ia_pet_name":"N.Ramudu, The Superintending Engineer, ","ia_pend_disp":"P","date_of_filing":"2018-09-06"}},"link_cases":null,"objections":[],"category_details":{"category":"WP ( 28 )","sub_category":"A.P ADMINISTRATIVE TRIBUNAL ( 92 )"}}
	
			// 6.2 SHOW BUSINESS API
			//inputStr = "cino=APHC010419392018|business_date=2018-09-07";
			//decryptedRespStr:{"desgname":"","case_number":"WP\/0019419\/2018","business_date":"2018-09-07","business":"disposed of","short_order_det":null}
			//showBusiness(inputStr);
			
			// 6.5 SEARCH BY CASE NUMBER API
			//inputStr = "est_code=APHC01|case_type=63|reg_no=19419|reg_year=2018";
			//searchByCaseNo(inputStr);
			// Response after decryption:{"establishment_name":"Principlal Bench at Andhra Pradesh" ,"casenos":{"case1":{"cino":"APHC010419392018","type_name":"WP","reg_no": "19419","reg_year":"2018","pet_name":"Seelanki Satyanarayana" ,"res_name":"The State of Andhra Pradesh"}}}
			
			// 6.6 SEARCH BY FILING NUMBER API
			//inputStr = "est_code=APHC01|case_type=63|fil_no=32044|fil_year=2018";
			//searchByFilingNo(inputStr);
			// Response :{"establishment_name":"Principlal Bench at Andhra Pradesh" ,"casenos":{"case1":{"cino":"APHC010419392018","type_name":"WP","fil_no":"32044","fil_year":"2018","pet_name":"Seelanki Satyanarayana" ,"res_name":"The State of Andhra Pradesh"}}}
			
			// 6.7 SEARCH BY PARTY NAME API
			//inputStr = "est_code=APHC01|pend_disp=P|litigant_name=The State of Andhra Pradesh|reg_year=2018";
			//searchByParty(inputStr);
			
			// 6.8 SEARCH BY ADVOCATE NAME API
			//inputStr = "est_code=APHC01|pend_disp=P|advocate_name=V KISHORE|reg_year=2018";
			// decryptedRespStr:{"establishment_name":"Principlal Bench at Andhra Pradesh","casenos":{"case1":{"cino":"APHC010155782018","type_name":"CRP","reg_no":"1407","reg_year":"2018","pet_name":"","res_name":""},"case2":{"cino":"APHC010995142018","type_name":"WP","reg_no":"48184","reg_year":"2018","pet_name":"","res_name":""},"case3":{"cino":"APHC010994862018","type_name":"WP","reg_no":"48085","reg_year":"2018","pet_name":"","res_name":""},"case4":{"cino":"APHC010957632018","type_name":"WP","reg_no":"46261","reg_year":"2018","pet_name":"","res_name":""},"case5":{"cino":"APHC010653402018","type_name":"WP","reg_no":"31390","reg_year":"2018","pet_name":"","res_name":""},"case6":{"cino":"APHC010649592018","type_name":"WP","reg_no":"31172","reg_year":"2018","pet_name":"","res_name":""},"case7":{"cino":"APHC010641502018","type_name":"WP","reg_no":"30755","reg_year":"2018","pet_name":"","res_name":""},"case8":{"cino":"APHC010547522018","type_name":"WP","reg_no":"25920","reg_year":"2018","pet_name":"","res_name":""},"case9":{"cino":"APHC010529672018","type_name":"WP","reg_no":"25049","reg_year":"2018","pet_name":"","res_name":""},"case10":{"cino":"APHC010523372018","type_name":"WP","reg_no":"24804","reg_year":"2018","pet_name":"","res_name":""},"case11":{"cino":"APHC010524932018","type_name":"WP","reg_no":"24722","reg_year":"2018","pet_name":"","res_name":""},"case12":{"cino":"APHC010310462018","type_name":"WP","reg_no":"14126","reg_year":"2018","pet_name":"","res_name":""},"case13":{"cino":"APHC010298482018","type_name":"WP","reg_no":"13571","reg_year":"2018","pet_name":"","res_name":""},"case14":{"cino":"APHC010281812018","type_name":"WP","reg_no":"12561","reg_year":"2018","pet_name":"","res_name":""},"case15":{"cino":"APHC010279552018","type_name":"WP","reg_no":"12538","reg_year":"2018","pet_name":"","res_name":""},"case16":{"cino":"APHC010266632018","type_name":"WP","reg_no":"11794","reg_year":"2018","pet_name":"","res_name":""},"case17":{"cino":"APHC010257272018","type_name":"WP","reg_no":"11523","reg_year":"2018","pet_name":"","res_name":""},"case18":{"cino":"APHC010776242018","type_name":"WP","reg_no":"37645","reg_year":"2018","pet_name":"M\/s The Pedapadu Large Sized Co-operative Credit Society Limited,","res_name":"The State of Andhra Pradesh,"},"case19":{"cino":"APHC010183492018","type_name":"WP","reg_no":"8001","reg_year":"2018","pet_name":"Yadlapalli Venkata Krishna","res_name":"The State of Andhra Pradesh,"},"case20":{"cino":"APHC010005902018","type_name":"WP","reg_no":"473","reg_year":"2018","pet_name":"M VENKATA  RAMUDU","res_name":"THE ANDHRA PRADESH STATE COOPERATIVE BANK LTD."},"case21":{"cino":"APHC010155782018","type_name":"CRP","reg_no":"1407","reg_year":"2018","pet_name":"Veeravarapu Ratnagiri Rao","res_name":"Veeravarapu Durga Rao"}}}
			//searchByAdvocateName(inputStr);

			
			// 6.3 SHOW ORDERS API
			// inputStr = "cino=APHC010665392018|order_no=3|order_date=2021-09-28";
			// showOrders(inputStr);
			
			// 6.4 SEARCH BY CNR (CURRENT CASE STATUS) API
			//inputStr = "cino=APHC010419392018";
			//in processResponse:{"response_str":"11GpxJMryiT0+0e3GJDL1paGw3SVKW6UcUcUVn3bNNMrO2PQN\/kyCkp62fNrfdedzvB1UJgzH6v9nUJWwJPxsokmUksm7kNnhJDBgL0fkwcE8DkSeSWPFVeQDGE1KOMqO29eiYcSVQ3kuouXeaRv2Y85q+aH4VDEXlZoHWyGQNHxlIWp0qYMYZHox\/t6uWmNqytSKbwd5MLa77I8JmgZXWs7TcKc+q+sqo263+UJRPago96HHt7BF4jpGhAgsFEolEy\/AG3x5V131cIUmount6+f\/ralFwuEfXssjutdrNUKCRJp7IWUjubjARgLoHaZ159MlzcLnnsJrlZo0J5FcAr\/Zy8hn4ND6d07HkkpnPVJt0id+bIEPsBi8MBaHLlL8gNujp8txk79rDSeHTOrvyjBzgQES8CnpjtAjIS2VytsbHujQgy\/beH2fuczmPhhZiX0MSC+JgilMRj2m8C+onkFCDxkQMLlfmE4f13MpHaDqKh3QA72Vxl3Vb9NVkvLdnvtgJ2enIjsNralkwtRfh3eHsFH7CFc56e+iJ052rK6x8V7TuxdtxHe1AKdW+2MUhXW7raOp57xXO4+Ii42B6rzwecv2yzp27fldvPk6t79kMChKytmD2KNl7Zl\/rPfKNuFpjHg+pjmm6zzhBmw5VoJepYPZ\/7A8Y+FFgVA9atiyDO7AwOxtdUE2W53lUoa9Qf5XYOSAT9\/JRjI3bIxjhFSsmgKDuHUW7SmfjIXq7XVNUv1u84It9Vv7yaYaL20Di8wnrXL9e7opyYUWENnb6cjx8tRYfgy1Zucbk1rsp1Rlo\/PK7OxnoLgBz1ruhVnh6t+g6gNKubdS5K1nEHnMpRGRjovj2EFC1LCBbaAXLHHOWU7Fsg0TWVCw\/WcRwcAxLjy8eluX+1rqU2B8uAT7Q==","response_token":"59f382d599efd3730448ba98d8c393f86bfce817916304a8be91332f35cfd74d","version":"v1.0"}
			//searchByCNRCurrentCaseState(inputStr);
			
			/*
			// 6.9 SEARCH BY ADVOCATE BAR REGISTRATION NUMBER API
			inputStr = "est_code=APHC01|pend_disp=P|advocate_bar_regn_no=’value’|reg_year=2018";
			searchByAdvocateBarRegNo(inputStr);
			 */
			// 6.10 SEARCH BY ACT API
			//inputStr = "est_code=APHC01|pend_disp=D|national_act_code=20170430027001|reg_year=2017";
			//searchByAct(inputStr);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void searchByCNR(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			// System.out.println("in getCNRdetails");
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				String url = apiURL + "HighCourt-CNRSearch/v1.0/cnrFullCaseDetails?dept_id=" + deptId + "&request_str="
						+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="
						+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return outPutVal;
	}
	
	
	public static void showBusiness(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in showBusinessAPI");
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-ShowBusiness/v1.0/business?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				
				String url = apiURL + "HighCourt-ShowBusiness/v1.0/business?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return outPutVal;
	}
	
	
	public static void showOrders(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in showOrdersAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-Order/v1.0/order?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				
				String url = apiURL + "HighCourt-Order/v1.0/order?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return outPutVal;
	}
	
	// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-CurrentStatus/v1.0/currentStatus/
	public static void searchByCaseNo(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in searchByCaseNoAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				//https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-CaseNumber/v1.0/caseNumber?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				
				String url = apiURL + "HighCourt-CaseNumber/v1.0/caseNumber?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return outPutVal;
	} 
	
	public static void searchByFilingNo(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in searchByFilingNoAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				//https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-FilingNumber/v1.0/filingNumber?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				
				String url = apiURL + "HighCourt-FilingNumber/v1.0/filingNumber?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return outPutVal;
	} 
	
	public static void searchByCNRCurrentCaseState(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in searchByCNRCurrentCaseStateAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				String url = apiURL + "HighCourt-CurrentStatus/v1.0/currentStatus?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				System.out.println("URL:" + url);
				String resp = sendPostRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	public static void searchByParty(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in showOrdersAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-PartyName/v1.0/partyName?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				String url = apiURL + "HighCourt-PartyName/v1.0/partyName?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	public static void searchByAdvocateName(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in showOrdersAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-AdvocateName/v1.0/advocateName?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				String url = apiURL + "HighCourt-AdvocateName/v1.0/advocateName?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	
	public static void searchByAdvocateBarRegNo(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in searchByAdvocateBarRegNoAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-AdvocateBarRegistration/v1.0/advocateBarRegn?dept_id=dept_id&request_str=re quest_str&request_token=request_token&version=version
				String url = apiURL + "HighCourt-AdvocateBarRegistration/v1.0/advocateBarRegn?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	public static void searchByAct(String inputStr) throws Exception {
		String outPutVal = "";
		String request_token = "", requeststring = "";String authToken ="";
		try {
			System.out.println("in searchByActAPI:"+inputStr);
			authToken = getAuthToken();
			if(authToken!=null && !authToken.equals("")) {
				// 1. Encoding Request Token
				byte[] hmacSha256 = HASHHMACJava.calcHmacSha256("15081947".getBytes("UTF-8"), inputStr.getBytes("UTF-8"));
				request_token = String.format("%032x", new BigInteger(1, hmacSha256));
				// 2. Encoding Request String
				requeststring = ECourtsCryptoHelper.encrypt(inputStr.getBytes());
				//System.out.println("request_str::" + requeststring);
				
				// https://egw.bharatapi.gov.in/t/ecourts.gov.in/HighCourt-Act/v1.0/act?dept_id=dept_id&request_str=request_str&request_token=request_token&version=version
				String url = apiURL + "HighCourt-Act/v1.0/act?dept_id=" + deptId + "&request_str="+ URLEncoder.encode(requeststring, "UTF-8") + "&request_token=" + request_token + "&version="+ version;
				//System.out.println("URL:" + url);
				String resp = sendGetRequest(url, authToken);
				//System.out.println("RESP:" + resp);
				if(resp!=null && !resp.equals("")) {
					processResponse(resp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	

	public static String getAuthToken() throws JSONException {
		System.out.println("in getAuthToken");
		String authToken = "";
		
		String urlToken = "https://egw.bharatapi.gov.in/token?grant_type=password&username=GOVT-AP@ecourts.gov.in&password=Apcfss@123&scope=";
		System.out.println("Token URL:" + urlToken);
		String respAuth = sendPostRequest(urlToken);
		System.out.println("" + respAuth);
		if (respAuth != null && !respAuth.equals("")) {

			JSONObject jobj1 = new JSONObject(respAuth);
			authToken = jobj1.has("access_token") ? jobj1.get("access_token").toString() : "";
			System.out.println("authToken:" + authToken);
		}
		
		return authToken;
	}
	
	public static String sendGetRequest(String requestUrl, String authToken) throws Exception {
		// Sending get request
		System.out.println("sendGetRequest requestUrl:"+requestUrl);
		URL url = new URL(requestUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Bearer " + authToken);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestMethod("GET");

		System.out.println(conn.getResponseCode() + "-" + conn.getResponseMessage());

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String output="";
		StringBuffer response = new StringBuffer();
		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		// printing result from response
		// System.out.println("Response:-" + response.toString());
		if(conn.getResponseCode()==200 && (response==null || response.toString().equals(""))) {
			System.out.println("Record Not Found.");
		}
		return response.toString();
	}

	public static void processResponse(String resp) throws Exception {
		// System.out.println("in processResponse:"+resp);
		String response_str = "", response_token = "", version = "", decryptedRespStr="";
		FileWriter file;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date dt1 = new Date();
			file = new FileWriter("D:\\HighCourts\\fileResp"+sdf.format(dt1)+".txt");
			
			file.write(resp);
		if (resp != null && !resp.equals("")) {

			JSONObject jObj = new JSONObject(resp);

			if(jObj.has("response_str") && jObj.getString("response_str")!=null) {
				response_str = jObj.getString("response_str").toString();
				//System.out.println("response_str:"+response_str);
			}
			if(jObj.has("response_token") && jObj.getString("response_token")!=null) {
				response_token = jObj.getString("response_token").toString();
			}
			if(jObj.has("version") && jObj.getString("version")!=null) {
				version = jObj.getString("version").toString();
			}
			// DecryptResponse String
			if(response_str != null && !response_str.equals(""))
				decryptedRespStr = ECourtsCryptoHelper.decrypt(response_str.getBytes());
			//System.out.println("decryptedRespStr:"+decryptedRespStr);
			file.write("\n");
			file.write("\n");
			file.write("decryptedRespStr");
			file.write("\n");
			file.write("\n");
			
			file.write(decryptedRespStr);
			file.flush();
            file.close();
			System.out.println("Successfully Copied JSON Object to File...");
			
			//System.out.println("in response_token:"+response_token);
			
			//Decrypt Response Token
			
			/*
			ECourtResponse ecresp = new ECourtResponse();
			ecresp = new Gson().fromJson(decryptedRespStr, ECourtResponse.class);
			System.out.println("getCourt_no"+ecresp.getBench_name());
			 */
			System.out.println("END");
		} else {
			System.out.println("Invalid/Empty Response");
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static String sendPostRequest(String requestUrl) {
		StringBuffer jsonString = new StringBuffer();
		try {
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic b3F0dV9MaHMxSktDcHhHOGhhWTM3R1VyUHU0YTpYQUJlVkNoNjZEdHNJakMxN0IzOXVyelEwWG9h");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while ((line = br.readLine()) != null) {
				jsonString.append(line);
			}
			br.close();
			connection.disconnect();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return jsonString.toString();
	}

	public static String sendPostRequest(String requestUrl, String authToken) throws Exception {
		// Sending get request
		URL url = new URL(requestUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Bearer " + authToken);
		// conn.setRequestProperty("Authorization", "Basic b3F0dV9MaHMxSktDcHhHOGhhWTM3R1VyUHU0YTpYQUJlVkNoNjZEdHNJakMxN0IzOXVyelEwWG9h");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		conn.setRequestMethod("POST");

		System.out.println(conn.getResponseCode() + " " + conn.getResponseMessage());

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String output="";
		StringBuffer response = new StringBuffer();
		while ((output = in.readLine()) != null) {
			response.append(output);
		}
		in.close();
		// printing result from response
		// System.out.println("Response:-" + response.toString());
		if(conn.getResponseCode()==200 && (response==null || response.toString().equals(""))) {
			System.out.println("Record Not Found.");
		}
		return response.toString();
	}
	
	public static void processResponseToFile(String resp, String fileName) throws Exception {
		// System.out.println("in processResponse:"+resp);
		String response_str = "", response_token = "", version = "", decryptedRespStr="";
		FileWriter file;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
			Date dt1 = new Date();
			file = new FileWriter("D:\\HighCourts\\"+fileName+sdf.format(dt1)+".txt");
			
			file.write(resp);
			resp = resp.trim();
			System.out.println("RESP STRING:"+resp);
		if (resp != null && !resp.equals("")) {

			JSONObject jObj = new JSONObject(resp);

			if(jObj.has("response_str") && jObj.getString("response_str")!=null) {
				response_str = jObj.getString("response_str").toString();
				//System.out.println("response_str:"+response_str);
			}
			if(jObj.has("response_token") && jObj.getString("response_token")!=null) {
				response_token = jObj.getString("response_token").toString();
			}
			if(jObj.has("version") && jObj.getString("version")!=null) {
				version = jObj.getString("version").toString();
			}
			// DecryptResponse String
			if(response_str != null && !response_str.equals(""))
				decryptedRespStr = ECourtsCryptoHelper.decrypt(response_str.getBytes());
			System.out.println("decryptedRespStr:"+decryptedRespStr);
			file.write("\n");
			file.write("\n");
			file.write("decryptedRespStr");
			file.write("\n");
			file.write("\n");
			
			file.write(decryptedRespStr);
			file.flush();
            file.close();
			System.out.println("Successfully Copied JSON Object to File...");
			
			//System.out.println("in response_token:"+response_token);
			
			//Decrypt Response Token
			
			/*
			ECourtResponse ecresp = new ECourtResponse();
			ecresp = new Gson().fromJson(decryptedRespStr, ECourtResponse.class);
			System.out.println("getCourt_no"+ecresp.getBench_name());
			 */
			System.out.println("END");
		} else {
			System.out.println("Invalid/Empty Response");
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}