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
 * MODULE:	RequestOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        9-9-2003
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

/* Singleton class */
public class RequestOrder extends ExpressOrder 
{
	final static public String TYP_IND = "R";
	static private RequestOrder _instance; //one and only	

	private RequestOrder() {
		super(TYP_IND);
	}
	
	private RequestOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	
	static public RequestOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new RequestOrder();	}
		return _instance;
	}
	static public RequestOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new RequestOrder(conn);	}
		return _instance;
	}
}
