package in.apcfss.struts.commons;


import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.struts.upload.FormFile;

public class CommonModels {
	
	public static String randomTransactionNo(){
		String randomNo=null;
		
		Calendar calendar = Calendar.getInstance();
		//System.out.println("Calender - Time in milliseconds : " + calendar.getTimeInMillis());
		randomNo = calendar.getTimeInMillis()+"";
		
		return randomNo;
	}

	public static String checkStringObject(Object objVal){
		
		return objVal!=null ? objVal.toString().trim() : "";
	}
	
	
	public static int checkIntObject(Object objVal){
		
		return objVal!=null && !(objVal.toString().trim()).equals("") ? Integer.parseInt(objVal.toString().trim()) : 0;
	}
	public static Double checkDoubleObject(Object objVal){
		
		return objVal!=null && !(objVal.toString().trim()).equals("") ? Double.parseDouble(objVal.toString().trim()) : 0.0;
	}
	
	
	
	
	
	public static boolean NullValidation(String str) {
		boolean flag = true; 

		if(str == null || str.equals("") || str.trim() == null || str.trim().equals("") || str.trim().equals("null"))
			flag = false;

		return flag;
	}

	public static boolean NonZeroValidation(String str) {
		boolean flag = true; 

		if(str == null || str.equals("") || str.trim() == null || str.trim().equals("")  || str.trim().equals("null")|| str.trim().equals("0"))
			flag = false;

		return flag;
	}
	
	
}
