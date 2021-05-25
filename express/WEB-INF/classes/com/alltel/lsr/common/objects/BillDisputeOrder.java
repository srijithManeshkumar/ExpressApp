/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	BillDisputeOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        9-9-2003
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.util.*;

/* Singleton class for Bill Dispute definition
*/
public class BillDisputeOrder extends ExpressOrder 
{
	final static public String TYP_IND = "B";
	static private BillDisputeOrder _instance; //one and only	

	private BillDisputeOrder() {
		super(TYP_IND);
	}
	private BillDisputeOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	

	static public BillDisputeOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new BillDisputeOrder();	
			//Log.write(Log.DEBUG_VERBOSE,"BillDisputeOrder - one instance creation");
		}
		return _instance;
	}
	static public BillDisputeOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new BillDisputeOrder(conn);	
			//Log.write(Log.DEBUG_VERBOSE,"BillDisputeOrder - one instance creation");
		}
		return _instance;
	}
}
