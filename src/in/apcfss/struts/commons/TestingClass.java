package in.apcfss.struts.commons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TestingClass {

	//final static String dbUrl = "jdbc:postgresql://172.16.98.2:9432/apolcms", dbUserName = "apolcms", dbPassword = "apolcms";

	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		try {
			
			String name1="John";
			String name2= new String("John");
			System.out.println(name1==name2);
			
			
			//Class.forName("org.postgresql.Driver");
			//conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
			//SendSMSAction.sendSMS("9618048663", "Mobile OTP for Login into CFMS helpdesk:45678", "1007713986799127731", null);
			String deptCode="AGC02";
			System.out.println(deptCode.substring(3,5));
			
			
			LocalDate newDate1 =  LocalDate.now().plusDays(1);  
			System.out.println(newDate1);
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
	        System.out.println(formatter.format(newDate1));			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(conn!=null)
				conn.close();
		}
	}
}
