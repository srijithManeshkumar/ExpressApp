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
 * MODULE:	DslOrder.java
 * DESCRIPTION: 
 * AUTHOR:      psedlak
 * DATE:        9-9-2003
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

/* Singleton class */
public class DslOrder extends ExpressOrder 
{
	final static public String TYP_IND = "D";
	static private DslOrder _instance; //one and only	

	private DslOrder() {
		super(TYP_IND);
	}
	
	private DslOrder(java.sql.Connection conn) {
		super(TYP_IND, conn);
	}
	
	static public DslOrder getInstance()
	{
		if ( _instance == null )
		{	_instance = new DslOrder();	}
		return _instance;
	}

	static public DslOrder getInstance(java.sql.Connection conn)
	{
		if ( _instance == null )
		{	_instance = new DslOrder(conn);	}
		return _instance;
	}
}
