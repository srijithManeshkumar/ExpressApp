/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	DsTicketOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        3-17-2004
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

/* Singleton class */
public class DsTicketOrder extends ExpressOrder 
{
	final static public String TYP_IND = "S";
	static private DsTicketOrder _instance; //one and only	

	private DsTicketOrder() {
		super(TYP_IND);
	}
	
	private DsTicketOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	
	static public DsTicketOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new DsTicketOrder();	}
		return _instance;
	}

	static public DsTicketOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new DsTicketOrder(conn);	}
		return _instance;
	}
}
