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
 * MODULE:	PreorderOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        9-9-2003
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

/* Singleton class */
public class PreorderOrder extends ExpressOrder 
{
	final static public String TYP_IND = "P";
	static private PreorderOrder _instance; //one and only	

	private PreorderOrder() {
		super(TYP_IND);
	}
	
	private PreorderOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	
	static public PreorderOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new PreorderOrder();	}
		return _instance;
	}

	static public PreorderOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new PreorderOrder(conn);	}
		return _instance;
	}
}
