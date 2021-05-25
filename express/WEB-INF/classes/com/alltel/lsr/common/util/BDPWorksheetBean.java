/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO WINDSTREAM
 *      COMMUNICATIONS, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED
 *      ACCESS, USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2006
 *				BY
 *			WINDSTREAM COMMUNICATIONS INC.
 */
/**
 * MODULE:	StaticBDPWorsheetData.java
 * Business Data Product Singleton
 *
 *
 * AUTHOR:  Steve Korchnak
 *
 * DATE:    12/15/2006
 *
 * HISTORY:
 */
package com.alltel.lsr.common.util;
import java.util.*;
import java.io.*;
import java.text.*;

public class BDPWorksheetBean
{
	private static String CLASSNAME = "BDPWorksheetBean";
 	/**
	 * Private Constructor
	 */
	//private BDPWorksheetBean() {
	//	init();
	//}
	// Create the initial instance
	private static BDPWorksheetBean instance = new BDPWorksheetBean();

	public static synchronized BDPWorksheetBean getInstance() {
		return instance;
	}

	public static String wgWrkshtCell(String val)
	{
		Log.write(Log.DEBUG_VERBOSE,  "BDPWorksheetBean:" +  "wgWrkshtCell(" + val);
		return "<td nowrap>" + val + "</td>\n";
	}

	/*Steve Korchnak
	 *  Work group worksheet, header for Dedicated internet
	 */
	public static String wgWrkshtHeaderDI(){
		Log.write(Log.DEBUG_VERBOSE,  "BDPWorksheetBean:" +  "Dedicated internet");
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>"													);
		sb.append("<td nowrap>State</td>\n"										);
		sb.append("<td nowrap>Product Nm</td>\n"									);
		sb.append("<td nowrap>Ordr Typ</td>\n"										);
		sb.append("<td nowrap>Order Number</td>\n"									);
		sb.append("<td nowrap>Main Business/Customer Name<\td>\n"					);
		sb.append("<td nowrap>Location Name</td>\n"								);
		sb.append("<td nowrap>Initial DTS</td>\n"           						);
		sb.append("<td nowrap>Initial->Submitted SLA secs</td>\n"  	 	       	);
		sb.append("<td nowrap>Submitted User</td>\n"                    	    	);
		sb.append("<td nowrap>Submitted->DE In-Progress SLA secs</td>\n"    		);
		sb.append("<td nowrap>DE In-Progress User</td>\n"                   		);
		sb.append("<td nowrap>DE In-Progress->DE Complete SLA secs</td>\n"  		);
		sb.append("<td nowrap>DE Complete User</td>\n"                      		);
		sb.append("<td nowrap>DE Complete->Mkt Pending Circuit SLA secs</td>\n"	);
		sb.append("<td nowrap>Mkt Pending Circuit User</td>\n"						);
		sb.append("<td nowrap>Mkt Pending Circuit->Mkt Complete SLA secs</td>\n"	);
		sb.append("<td nowrap>Mkt Complete User</td>\n"							);
		sb.append("<td nowrap>Mkt Complete->DSTAC Accepted SLA secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Accepted User</td>\n"							);
		sb.append("<td nowrap>DSTAC Accepted->DSTAC Complete SLA secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Complete User</td>\n"							);
		sb.append("<td nowrap>DSTAC Complete->Billing SLA secs</td>\n"				);
		sb.append("<td nowrap>Billing User</td>\n"									);
		sb.append("<td nowrap>Billing->Bill Complete SLA secs</td>\n"				);
		sb.append("<td nowrap>Bill Complete User</td>\n"             		       	);
		sb.append("<td nowrap>Bill Complete DTS</td>\n"							);
		sb.append("<\tr><tr>"												);
    	return sb.toString();
	}

	/*Steve Korchnak
	 *  Work group worksheet, header for Adsl static Ip, Additional Ip Space, and T1 Clec bundle
	 */
	public static String wgWrkshtHeader1A(){
		Log.write(Log.DEBUG_VERBOSE,  "BDPWorksheetBean:" +  "Adsl static Ip, Additional Ip Space, and T1 Clec bundle");
		StringBuffer sb = new StringBuffer();
//              sb.append("<tr>"													);
		sb.append("<td nowrap>State</td>\n"                            	    	);
		sb.append("<td nowrap>Product Nm</td>\n"                        	    	);
		sb.append("<td nowrap>Ordr Typ</td>\n"                     	       		);
		sb.append("<td nowrap>Order Number</td>\n"                          		);
		sb.append("<td nowrap>Main Business/Customer Name</td>\n"           		);
		sb.append("<td nowrap>Location Name</td>\n"                         		);
//		sb.append("<td nowrap>Change Type</td>\n"           						);
//		sb.append("<td nowrap>Change Sub Type</td>\n"         						);
		sb.append("<td nowrap>Initial DTS</td>\n"                           		);
		sb.append("<td nowrap>Initial->Submitted SLA secs</td>\n"           		);
		sb.append("<td nowrap>Submitted User</td>\n"                        		);
		sb.append("<td nowrap>Submitted->DE In-Progress SLA secs</td>\n"    		);
		sb.append("<td nowrap>DE In-Progress User</td>\n"                   		);
		sb.append("<td nowrap>DE In-Progress->DE Complete SLA secs</td>\n"  		);
		sb.append("<td nowrap>DE Complete User</td>\n"                      		);
		sb.append("<td nowrap>DE Complete->DSTAC Accepted SLA secs</td>\n"  		);
		sb.append("<td nowrap>DSTAC Accepted User</td>\n"                   		);
		sb.append("<td nowrap>DSTAC Accepted->DSTAC Complete SLA secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Complete User</td>\n"                   		);
		sb.append("<td nowrap>DSTAC Complete->Billing SLA secs</td>\n"      		);
		sb.append("<td nowrap>Billing User</td>\n"                          		);
		sb.append("<td nowrap>Billing->Bill Complete SLA secs</td>\n"       		);
		sb.append("<td nowrap>Bill Complete User</td>\n"                    		);
		sb.append("<td nowrap>Bill Complete DTS</td>\n"							);
                sb.append("<td nowrap>Service Complete DTS</td>\n"				);
                sb.append("</tr>"												);
    	return sb.toString();
	}

	/*Steve Korchnak
	 *  Work group worksheet IP VPN change or move and Metroe-E Change or move
	 */
	public static String wgWrkshtHeader1B(){
		Log.write(Log.DEBUG_VERBOSE,  "BDPWorksheetBean:" +  "IP VPN change or move and Metroe-E Change or move");
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>"													);
		sb.append("<td nowrap>State</td>\n"                            	    	);
		sb.append("<td nowrap>Product Nm</td>\n"                        	    	);
		sb.append("<td nowrap>Ordr Typ</td>\n"	                            		);
		sb.append("<td nowrap>Order Number</td>\n"                          		);
		sb.append("<td nowrap>Main Business/Customer Name</td>\n"           		);
		sb.append("<td nowrap>Location Name</td>\n"                         		);
		sb.append("<td nowrap>Change Type</td>\n"           						);
		sb.append("<td nowrap>Change Sub Type</td>\n"         						);
		sb.append("<td nowrap>Initial DTS</td>\n"                           		);
		sb.append("<td nowrap>Initial->Submitted SLA secs</td>\n"           		);
		sb.append("<td nowrap>Submitted User</td>\n"                        		);
		sb.append("<td nowrap>Submitted->DE In-Progress SLA secs</td>\n"    		);
		sb.append("<td nowrap>DE In-Progress User</td>\n"                   		);
		sb.append("<td nowrap>DE In-Progress->DE Complete SLA secs</td>\n"  		);
		sb.append("<td nowrap>DE Complete User</td>\n"                      		);
		sb.append("<td nowrap>DE Complete->DSTAC Accepted SLA secs</td>\n"  		);
		sb.append("<td nowrap>DSTAC Accepted User</td>\n"                   		);
		sb.append("<td nowrap>DSTAC Accepted->DSTAC Complete SLA secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Complete User</td>\n"                   		);
		sb.append("<td nowrap>DSTAC Complete->Billing SLA secs</td>\n"      		);
		sb.append("<td nowrap>Billing User</td>\n"                          		);
		sb.append("<td nowrap>Billing->Bill Complete SLA secs</td>\n"       		);
		sb.append("<td nowrap>Bill Complete User</td>\n"                    		);
		sb.append("<td nowrap>Bill Complete DTS</td>\n"							);
		sb.append("<\tr><tr>"												);
    	return sb.toString();
	}

	/*Steve Korchnak
	 *  Work group worksheet, header for IP VPN New or disconnect and metro-e
	 */
	public static String wgWrkshtHeaderIPVPN(){
		Log.write(Log.DEBUG_VERBOSE,  "BDPWorksheetBean:" +  "Writing wgWrkshtHeaderIPVPN");
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>"													);
		sb.append("<td nowrap>State</td>\n"										);
		sb.append("<td nowrap>Product Nm</td>\n"									);
		sb.append("<td nowrap>Ordr Typ</td>\n"										);
		sb.append("<td nowrap>Order Number</td>\n"									);
		sb.append("<td nowrap>Main Business/Customer Name<\td>\n"					);
		sb.append("<td nowrap>Location Name</td>\n"									);
		sb.append("<td nowrap>Initial DTS</td>\n"           						);
		sb.append("<td nowrap>Initial->Submitted SLA secs</td>\n"  	 		       	);
		sb.append("<td nowrap>Submitted User</td>\n"                    	    	);
		sb.append("<td nowrap>Submitted->DE In-Progress SLA secs</td>\n"    		);
		sb.append("<td nowrap>DE In-Progress User</td>\n"                   		);
		sb.append("<td nowrap>DE In-Progress->DE Complete SLA secs</td>\n"  		);
		sb.append("<td nowrap>DE Complete User</td>\n"                      		);
		sb.append("<td nowrap>DE Complete->Mkt Pending Circuit SLA secs</td>\n"		);
		sb.append("<td nowrap>Mkt Pending Circuit User</td>\n"						);
		sb.append("<td nowrap>Mkt Pending Circuit->Mkt Complete SLA secs</td>\n"	);
		sb.append("<td nowrap>Mkt Complete User</td>\n"								);
		sb.append("<td nowrap>Mkt Complete->DSTAC Accepted SLA secs</td>\n"			);
		sb.append("<td nowrap>DSTAC Accepted User</td>\n"							);
		sb.append("<td nowrap>DSTAC Accepted->DSTAC Complete SLA secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Complete User</td>\n"							);
		sb.append("<td nowrap>DSTAC Complete->Billing SLA secs</td>\n"				);
		sb.append("<td nowrap>Billing User</td>\n"									);
		sb.append("<td nowrap>Billing->Bill Complete SLA secs</td>\n"				);
		sb.append("<td nowrap>Bill Complete User</td>\n"             		       	);
		sb.append("<td nowrap>BilComplete->DSTAC Service Complete secs</td>\n"		);
		sb.append("<td nowrap>DSTAC Service Complete User</td>\n"             	   	);
		sb.append("<td nowrap>Bill Complete DTS</td>\n"								);
		sb.append("<\tr><tr>"														);
    	return sb.toString();
	}
}
