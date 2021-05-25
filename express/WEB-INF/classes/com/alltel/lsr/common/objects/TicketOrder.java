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
 * MODULE:	TicketOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        9-9-2003
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

/* Singleton class */
public class TicketOrder extends ExpressOrder 
{
	final static public String TYP_IND = "T";
	static private TicketOrder _instance; //one and only	

	private TicketOrder() {
		super(TYP_IND);
	}
	
	private TicketOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	
	static public TicketOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new TicketOrder();	}
		return _instance;
	}

	static public TicketOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new TicketOrder(conn);	}
		return _instance;
	}
}
