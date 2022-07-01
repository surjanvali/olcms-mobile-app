package in.apcfss.struts.commons;

public class ApplicationVariables {
	
	//LIVE
	public static final String contextName="apolcms";
	public static final String filesepartor=System.getProperty("file.separator");
	//public static final String contextURL="https:"+filesepartor+filesepartor+"apsdri.ap.gov.in"+filesepartor;
	//public static final String contextURL="https:"+filesepartor+filesepartor+"audit.apcfss.in"+filesepartor+"APSDRI"+filesepartor;
	//public static final String contextURL="http:"+filesepartor+filesepartor+"localhost:8080"+filesepartor+contextName+filesepartor;
	
	
	public static final String contextPath=System.getProperty("catalina.base")+filesepartor+"webapps"+filesepartor+contextName+filesepartor;
	
	public static final String filesPath=contextPath+"files"+filesepartor;
	
	/* UPLOAD DOCUMENTS PATHS */
	
	public static final String ackPath = "uploads"+filesepartor+"acks"+filesepartor;
	// public static final String ackBarCodePath = "uploads"+filesepartor+"acksbarcodes"+filesepartor;
	//public static final String masterPassword = "39c9f399b436881ffabcb89a6c6ec9a4"; //aprcrp@2017

	// Production Database Details.
	static final String apolcmsDataBase = "jdbc:postgresql://10.96.54.54:6432/apolcms";
	static final String apolcmsUserName = "apolcms";
	static final String apolcmsPassword = "@p0l(m$";

}
