/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/**
 * MODULE:	StaticBDPReport.java
 * Business Data Product Singleton
 *
 *
 * AUTHOR:      Edris Kalibala
 *
 * DATE:       11/02/2005
 *
 * HISTORY:
 *	11/02/2005	Started
 *  12/01/2006  Steve Korchnak	- Added trailers for WG Interval report statistics section
 */
package com.alltel.lsr.common.util;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.util.Date;

public class StaticBDPReportData
{
	private static String CLASSNAME = "StaticBDPReportData";

	private static Hashtable hProducts;
	private static Hashtable hOrderTypes;
	private static Hashtable hChangeTypes;
	private static Hashtable hSubChangeTypes;
	private final static long DAY_IN_SEC = (long) 86400;
	private final static long HOUR_IN_SEC = (long) 3600;
	private final static long MIN_IN_SEC = (long) 60;
	final private static  String TYP_IND = "X";
 	/**
	 * Private Constructor
	 */
	private StaticBDPReportData() {
		init();
	}
	// Create the initial instance
	private static StaticBDPReportData instance = new StaticBDPReportData();

	public static synchronized StaticBDPReportData getInstance() {
		return instance;
	}

	public static  synchronized Hashtable getProducts(){
		return hProducts;
	}

	public static  synchronized Hashtable getChangeTypes(){
		return hChangeTypes;
	}

	public static  synchronized Hashtable getOrderTypes(){
		return hOrderTypes;
	}

	public static  synchronized Hashtable getSubChangeTypes(){
		return hSubChangeTypes;
	}

	public static synchronized void reload() {
		init();
	}

	private static void init() {
		final String FNAME = "init";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		hProducts = new Hashtable( 10);
		hChangeTypes  = new Hashtable( 10);
		hSubChangeTypes  = new Hashtable( 10);
		hOrderTypes = 	 new Hashtable( 10);
		String strPrdQry = " SELECT PRDCT_TYP_CD,PRDCT_DSCRPTN  from PRODUCT_T where TYP_IND = ?";
		String strOrdTQry = " SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM "
			+ " SERVICE_TYPE_T WHERE TYP_IND = ? ORDER BY SRVC_TYP_CD ASC ";
		String strChngTQry = "SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T "
			+ " WHERE TYP_IND = ? ORDER BY ACTVTY_TYP_CD ASC ";
		String strChngSubTQry ="SELECT SUB_ACTVTY_TYP_CD, SUB_ACTVTY_TYP_DSCRPTN "
			+ " FROM SUB_ACTIVITY_TYPE_T WHERE TYP_IND = ? ORDER BY SUB_ACTVTY_TYP_CD ASC ";
		try {
			conn  = DatabaseManager.getConnection();
			// first load products
			pstmt = conn.prepareStatement( strPrdQry  );
			pstmt.clearParameters();
			pstmt.setString(	1, TYP_IND );
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				hProducts.put(  rset.getString(1),  rset.getString(2) );
			}
			rset.close();
			pstmt.close();
			// load product types
			pstmt = conn.prepareStatement( strOrdTQry  );
			pstmt.clearParameters();
			pstmt.setString(	1, TYP_IND );
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				hOrderTypes.put(  rset.getString(1),  rset.getString(2) );
			}
			rset.close();
			pstmt.close();
			// load change types
			pstmt = conn.prepareStatement( strChngTQry  );
			pstmt.clearParameters();
			pstmt.setString(	1, TYP_IND );
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				hChangeTypes.put(  rset.getString(1),  rset.getString(2) );
			}
			rset.close();
			pstmt.close();

			// load change sub types
			pstmt = conn.prepareStatement( strChngSubTQry  );
			pstmt.clearParameters();
			pstmt.setString(	1, TYP_IND );
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				hSubChangeTypes.put(  rset.getString(1),  rset.getString(2) );
			}
			rset.close();
			pstmt.close();
		} catch ( SQLException e ) {
			e.printStackTrace();
			System.err.println( e.toString() );

		} catch ( Exception e ) {
			e.printStackTrace();
			System.err.println( e.toString() );
		} finally {
			// Clean up
			try {
				if(rset != null){ rset.close(); rset = null;}
				if(pstmt != null){ pstmt.close(); pstmt = null;}
			// Close the connection
			} catch(Exception eee) {}
		 	DatabaseManager.releaseConnection(conn);
			conn = null;
		}
	}

/********************Utility functions coded here to make BDPReportBean a little cleaner. */
	/* EK get one product name
	 */
	public static String getProductsName( String prdType, Hashtable hProducts ){
		if (prdType == null || prdType.length() < 1 )
			return "";
		return (String)hProducts.get(prdType);
	}


	/*getProductNames: get product names selected, currently the report run for one product
	 * 	put this will change very soon, so that explains why we are using the an array here.
	 *
	 */
	public static String getProductNames( String[] prdType, Hashtable hProducts  ){
		if (prdType == null )
			return "";
		String strProducts = "Products:&nbsp;&nbsp;";
		for(int i = 0; i < prdType.length; i++ )
		{
			if(prdType[i].length() > 0 ){
				strProducts += (String)hProducts.get( prdType[i] );
				strProducts += "&nbsp;&nbsp;";
			}
		}

		return strProducts;
	}

	/* get one order type name
	*/
	public static String getOrderTypeName( String orderType, Hashtable hOrderTypes ){
		if (orderType == null || orderType.length() < 1 )
			return "";
		return (String)hOrderTypes.get(orderType);
	}

	/* get order type names selected
	 */
	public static String getOrderTypeNames( String[] orderTypes, Hashtable hOrderTypes  ){
		if (orderTypes == null )
		{
			return "";
		}
		if( ExpressUtil.isElementOf( orderTypes, "ALL" ) ){
			return "Order Types: All";
		}
		String strOrderTypes = "Order Types:&nbsp;&nbsp;";
		for(int i = 0; i < orderTypes.length; i++ )
		{
			if(orderTypes[i].length() > 0 ){
				strOrderTypes += (String)hOrderTypes.get( orderTypes[i] );
				strOrderTypes += "&nbsp;&nbsp;";
			}
		}
		return strOrderTypes;
	}


	/* EK
	 * get one Change type name
	 */
	public static String getChangeTypeName( String chTyp, Hashtable hChangeTypes ){
		if (chTyp == null || chTyp.length() < 1 )
			return "";
		return (String)hChangeTypes.get(chTyp);
	}

	/* EK:
	 * get Changes type names selected
	 */
	public static String getChangeTypeNames( String[] chTyps, Hashtable hChangeTypes  ){
		if ( chTyps == null )
			return "";
		String strChangeTypes = "Change Types:&nbsp;&nbsp;";

		if( ExpressUtil.isElementOf( chTyps, "ALL" ) ){
			return "Change Types: All";
		}
		for(int i = 0; i < chTyps.length; i++ )
		{
			if(chTyps[i].length() > 0 ){
				strChangeTypes += (String)hChangeTypes.get( chTyps[i] );
				strChangeTypes += "&nbsp;&nbsp;";
			}
		}
		return strChangeTypes;
	}

	/* EK:
	 *  get one sub-Change type name
	 */
	public static String getChangeSubTypeName( String schTyp, Hashtable hSubChangeTypes  ){
		if (schTyp == null || schTyp.length() < 1 )
			return "";
		return (String)hSubChangeTypes.get(schTyp);
	}

	/* EK:
	 * get subChanges type names selected
	 */
	public static String getChangeSubTypes( String[] sbchTyps,  Hashtable hSubChangeTypes ){
		if ( sbchTyps == null )
			return "";
		if( ExpressUtil.isElementOf( sbchTyps, "ALL" ) ){
			return "Change Sub Types: All";
		}

		String strSubChangeTypes = "Change Sub Types:&nbsp;&nbsp;";
		for(int i = 0; i < sbchTyps.length; i++ )
		{
			if( sbchTyps[i].length() > 0 ){
				strSubChangeTypes += (String)hSubChangeTypes.get( sbchTyps[i] );
				strSubChangeTypes += "&nbsp;&nbsp;";
			}
		}
		return strSubChangeTypes;
	}

	/* EK:
	 * fulFillmentAHeader1A(), prints the table header row.
	 *
	 */
	public static String fulFillmentAHeader1A(){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<tr bgcolor=\"#efefef\"><th width=5% align=center>&nbsp;State&nbsp;</th>");
		hbHtml.append("<th width=5% align=center>Order Number</th>");
		hbHtml.append("<th width=25% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=5% align=center>Location Name</th>");
		hbHtml.append("<th width=8% align=center>Order Type</th>" );
		hbHtml.append("<th width=12% align=center>Submitted DTS</th>");
		hbHtml.append("<th width=12% align=center>Service Complete DTS</th>");
		hbHtml.append("<th width=9% align=center>Bill Effective DTS</th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From Submitted to Bill Effective DTS (Days)</span></th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From Submitted to Service Complete (Days)</span></th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From Service Complete Bill Effective DTS (Days)</span></th></tr>");
		return hbHtml.toString();
	}

	/* EK:
	 * fulFillmentHeader1B(), prints the table header row.
	 *
	 */
	public static String fulFillmentHeader1B(){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<tr bgcolor=\"#efefef\"><th width=5% align=center>&nbsp;State&nbsp;</th>");
		hbHtml.append("<th width=5% align=center>Order Number</th>");
		hbHtml.append("<th width=20% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=8% align=center>Location Name</th>");
		hbHtml.append("<th width=5% align=center>Order Type</th>" );
		hbHtml.append("<th width=8% align=center>Change Type</th>" );
		hbHtml.append("<th width=8% align=center>Change Sub Type</th>" );
		hbHtml.append("<th width=8% align=center>Submitted Time Stamp</th>");
		hbHtml.append("<th width=8% align=center>IP Test Complete Time Stamp</th>");
		hbHtml.append("<th width=8% align=center>Bill Effective DTS</th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From Submitted to Bill Effective DTS (Days)</span></th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From Submitted to IP Test Complete (Days)</span></th>");
		hbHtml.append("<th width=5% align=center><span class=\"smallNote\">Elapsed Time From IP Test Complete Bill Effective DTS (Days)</span></th></tr>");
		return hbHtml.toString();
	}


	/*EK
	 *  Work group report, header for Dedicated internet
	 */
	public static String wgReportHeaderDI( String strHeader ){
		StringBuffer hbHtml = new StringBuffer( 128 );

		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=wgRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=14>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=6% align=center>&nbsp;Order Number&nbsp;</th>");
		hbHtml.append("<th width=17% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=5% align=center>Location Name</th>");
		hbHtml.append("<th width=6% align=center>Initial Status DTS</th>" );
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"DE Complete to Mkt Pending Circuit\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Mkt Pending Circuit to Mkt Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Mkt Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Complete to Billing\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Bill Complete DTS</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");
		return hbHtml.toString();
	}

	/*Steve Korchnak
	 *  Work group report, trailer for Dedicated internet
	 */
	public static String wgReportTrailerDI( String strHeader ){
		StringBuffer hbHtml = new StringBuffer( 128 );

		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=wgRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=14>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=34% align=center></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"DE Complete to Mkt Pending Circuit\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Mkt Pending Circuit to Mkt Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Mkt Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Complete to Billing\"</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\"></span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");
		return hbHtml.toString();
	}
	/*EK:
	 * Work group Report  Adsl static Ip, Additional Ip Space, and T1 Clec bundle
	 *
	 */
	public static String wgReportHeader1A( String strHeader){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=12>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=10% align=center>&nbsp;Order Number&nbsp;</th>");
		hbHtml.append("<th width=19% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=5% align=center>Location Name</th>");
		hbHtml.append("<th width=8% align=center>Initial Status DTS</th>" );
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"DE Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Service Complete to Billing\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=8% align=center><span class=\"smallNote\">Bill Complete DTS</span></th>");
		hbHtml.append("<th width=7%  align=center><span class=\"smallNote\">Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");


		return hbHtml.toString();
	}

	/*Steve Korchnak
	 * Work group Report  Adsl static Ip, Additional Ip Space, and T1 Clec bundle
	 *
	 */
	public static String wgReportTrailer1A( String strHeader){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=12>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=42% align=center></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Service Complete to Billing\"</span></th>");
		hbHtml.append("<th width=7% align=center><span class=\"smallNote\">Avg Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=8% align=center><span class=\"smallNote\"></span></th>");
		hbHtml.append("<th width=7%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");


		return hbHtml.toString();
	}

	/*EK:
	 * Work group Report IP VPN change or move and Metroe-E Change or move
	 *
	 */
	public static String wgReportHeader1B( String strHeader){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=14>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=10% align=center>&nbsp;Order Number&nbsp;</th>");
		hbHtml.append("<th width=16% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=6% align=center>Location Name</th>");
		hbHtml.append("<th width=6% align=center>Change Type</th>" );
		hbHtml.append("<th width=8% align=center>Change Sub Type</th>" );
		hbHtml.append("<th width=8% align=center>Initial Status DTS</th>" );
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"DE Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Service Complete to Billing\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Bill Complete DTS</span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");


		return hbHtml.toString();
	}

	/*Steve Korchnak
	 * Work group Report IP VPN change or move and Metroe-E Change or move
	 *
	 */
	public static String wgReportTrailer1B( String strHeader){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=14>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=54% align=center></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Accepted to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Service Complete to Billing\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\">Avg Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=6% align=center><span class=\"smallNote\"></span></th>");
		hbHtml.append("<th width=6%  align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Bill Complete\"</span></th></tr>");


		return hbHtml.toString();
	}

	/*EK
	 * Work group report, header for IP VPN New or disconnect and metro-e
	 */
	public static String wgReportHeaderIPVPN( String strHeader ){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=17>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=4% align=center>&nbsp;Order Number&nbsp;</th>");
		hbHtml.append("<th width=8% align=center>Main Business/Customer Name</th>");
		hbHtml.append("<th width=8% align=center>Location Name</th>");
		hbHtml.append("<th width=8% align=center>Change Type</th>");
		hbHtml.append("<th width=4% align=center>Change Sub Type</th>");
		hbHtml.append("<th width=4% align=center>Initial Status DTS</th>" );
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"DE Complete to Mkt Pending Circuit\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Mkt Pending Circuit to Mkt Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Mkt Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC Accepted to DSTAC IP Test Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"DSTAC IP Test Complete to Billing\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Bill Complete to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">DSTAC Service Complete DTS</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Elapsed Time \"Initial to DSTAC Service Complete\"</span></th></tr>");
		return hbHtml.toString();
	}

	/*Steve Korchnak
	 * Work group report, trailer for IP VPN New or disconnect and metro-e
	 */
	public static String wgReportTrailerIPVPN( String strHeader ){
		StringBuffer hbHtml = new StringBuffer( 128 );
		hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT>");
		hbHtml.append("<TR bgcolor=\"#efefef\"><TD align=center colspan=17>&nbsp;" + strHeader + "</td>\n</tr>" );
		hbHtml.append("<tr><th width=36% align=center></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to Submitted\"</span></th>" );
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Submitted to DE In-Progress\"</span></th>" );
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE In-Progress to DE Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"DE Complete to Mkt Pending Circuit\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Mkt Pending Circuit to Mkt Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Mkt Complete to DSTAC Accepted\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC Accepted to DSTAC IP Test Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"DSTAC IP Test Complete to Billing\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Billing to Bill Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Bill Complete to DSTAC Service Complete\"</span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\"></span></th>");
		hbHtml.append("<th width=4% align=center><span class=\"smallNote\">Avg Elapsed Time \"Initial to DSTAC Service Complete\"</span></th></tr>");
		return hbHtml.toString();
	}

	/* EK:
	 * printElapsedTime: calculates and returns the days from passed in seconds.
	 *
	 */
	public static String printElapsedTime( long lSeconds ){
		String strInterval = "";
		if( lSeconds < 1 ){
			return 	strInterval;
		}
		strInterval = "" + (lSeconds / DAY_IN_SEC) + " Days:" ;
		strInterval += "&nbsp; "+ ( (lSeconds % DAY_IN_SEC) / HOUR_IN_SEC ) + " Hrs:";

		int min = (int)(((lSeconds % DAY_IN_SEC) % HOUR_IN_SEC)  / MIN_IN_SEC );
		strInterval += "&nbsp; " + (min == 59 ?  min : ( min + 1)  ) + " Min" ;

		return strInterval;

	}

	/* EK:
	 * printElapsedTime: calculates and returns the days from passed in seconds.
	 * NOTE, this was an adition to calculate elapsed time from initial to service complete or date complete, if use the above method, the final number will be off by
	 * several minutes because we are adding a minute at every interval.
	 */
	public static String printElapsedTime( long lSeconds, int iNumStatuses ){
		String strInterval = "";
		if( lSeconds < 1 ){
			return 	strInterval;
		}
		strInterval = "" + (lSeconds / DAY_IN_SEC) + " Days:" ;
		strInterval += "&nbsp; "+ ( (lSeconds % DAY_IN_SEC) / HOUR_IN_SEC ) + " Hrs:";
		int min = ((int)(((lSeconds % DAY_IN_SEC) % HOUR_IN_SEC)  / MIN_IN_SEC ) + iNumStatuses );
		while( min > 59 ){
			min--;
		}
		strInterval += "&nbsp; " + min +  " Min" ;


		return strInterval;

	}

	/* Decide if IP_VPN or Metro E product were selected.
	 */
	public static boolean isVPNorMetroE( String[] pdcts )
	{
		String[] strVpnMtre = {"I", "M"  };
		int i = 0;
		boolean bFoundIt = false;
		while( i < strVpnMtre.length )
		{
			if( ExpressUtil.isElementOf(pdcts, strVpnMtre[i] ) )
			{
				bFoundIt = true;
				i = strVpnMtre.length;
			}
			i++;
		}
		return bFoundIt;
	}

}





