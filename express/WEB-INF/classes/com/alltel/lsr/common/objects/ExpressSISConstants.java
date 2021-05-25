package com.alltel.lsr.common.objects;
		
public interface ExpressSISConstants
{
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"; 
	public static final String SIS_DOMAIN = "";	
	public static final String SIS_POST_ORDER_SCRIPT = "";
	public static final String SIS_PORT = "";
	
	// These are in property files as well. Constants will be used if 
	public static final String EXPRESS_SIS_URL = "http://sis.dsys.alltel.net/";
	//public static final String EXPRESS_SIS_URL = "http://sun30.alltel.com:8027/searchExpressCust.html";
	public static final String SIS_SEARCH_SCRIPT = "searchExpressCust_xml.php";
	public static final String SIS_WRITE_EXPRESS_ORDER = "writeExpressOrder_xml.php";
	public static final int INT_SITE_CONTACT_INFORMATION = 0;
	public static final int INT_REPORTING_CONTACT = 2;
	public static final int INT_PRIMARY_TECHNICAL_CONTACT = 1;
	public static final int INT_NOTIFICATION_CONTACT = 3;
	public static final int INT_AFTER_HOURS_CONTACT = 4;

	
	// Express SIS communications actions.
	public static final int CUSTOMER_LOOKUP_FORM = 0;
	public static final int	CUSTOMER_LOOKUP_FORM_SUBMIT = 1;
	public static final int	CUSTOMER_LOOKUP_FORM_CANCEL = 2;
	public static final int	CREATE_CUSTOMER_FROM_LIST = 3;
	
	
	public static final	String INSTITUTION = "INSTITUTION";		
	public static final	String DESIREDINSTALLDATE = "DESIREDINSTALLDATE";
	public static final	String SITENAME = "SITENAME";
	public static final	String STREETADDRESS = "STREETADDRESS";
	public static final	String SUBLOCATION = "SUBLOCATION";
	public static final	String CITY = "CITY";
	public static final	String STATE = "STATE";
	public static final	String ZIPCODE = "ZIPCODE";
	public static final	String SITEPHONENUMBER = "SITEPHONENUMBER";
	
	public static final	String REMARKS = "REMARKS";
	public static final	String  SISID =  "SISID";
	public static final	String  EXPRESSID =  "EXPRESSID";
	
	// Contact information tags
	public static final	String NAME = "NAME";
	public static final	String EMAIL = "EMAIL";
	public static final	String WORKPHONE = "WORKPHONE";
	public static final	String MOBILEPHONE = "MOBILEPHONE";
	public static final	String PAGER = "PAGER";
	public static final	String EXT = "EXT";
	
	
	// billing info
	public static final	String BILLTO = "BILLTO";
	public static final	String ATTN = "ATTN";
	public static final	String STREETADDRESS1 = "STREETADDRESS1";
	public static final	String STREETADDRESS2 = "STREETADDRESS2";
	public static final	String BILLPHONENUMBER = "PHONENUMBER";
	// CUSTOMER CONTACT XML BODY Sections 
	
	public static final	String SITE_INFORMATION = "SiteInformation";
	public static final	String SITE_CONTACT_INFORMATION = "SiteContactInformation";
	public static final	String PRIMARY_TECHNICAL_CONTACT = "PrimaryTechnicalContact";
	public static final	String REPORTING_CONTACTS = "ReportingContacts";
	public static final	String NOTIFICATION_CONTACTS = "NotificationContacts";
	public static final	String AFTER_HOURS_CONTACTS = "AfteHoursContacts";
	public static final	String SISMATCHES =  "Sismataches";
	public static final	String CUSTOMER_CONTACT =  "CustomerContact";
	public static final	String INTERNAL_DESCRIPTION =  "InternalDescription";
	public static final	String BILLINGINFORMATION =  "BillingInformation";
	
	public static final int ONE_K = 1024;
	public static final int TWO_K = 2048;
	public static final int THREE_K = 3072;
	public static final int FOUR_K = 4096;
	public static final int FIVE_K = 5120;
	public static final int SIX_K = 6144;
	public static final int SEVEN_K = 7168;
	public static final int EIGHT_K = 8192;
}
