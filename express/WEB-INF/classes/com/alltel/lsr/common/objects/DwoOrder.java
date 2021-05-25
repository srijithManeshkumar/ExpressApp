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
 * MODULE:	DwoOrder.java
 * DESCRIPTION: 
 * AUTHOR:      
 * DATE:        3-20-2004
 * HISTORY:	TYP_IND is now passed in thru the constructor
 */

package com.alltel.lsr.common.objects;
import com.alltel.lsr.common.util.*;

/* Singleton class */
public class DwoOrder extends ExpressOrder 
{
	static public String TYP_IND; 
	static private DwoOrder _instance; //one and only	

	static private String currentTypInd = "";

	private DwoOrder(String strTypInd) {
		super(strTypInd);
		TYP_IND = strTypInd;
		currentTypInd = strTypInd;
	}
	
	private DwoOrder(String strTypInd, java.sql.Connection conn) {
		super(strTypInd, conn);
		TYP_IND = strTypInd;
	}
	
	static public DwoOrder getInstance(String strTypInd)
	{
		if ( _instance == null || !currentTypInd.equals(strTypInd))
		{	
			_instance = new DwoOrder(strTypInd);	
		}
		return _instance;
	}

	static public DwoOrder getInstance(String strTypInd, java.sql.Connection conn)
	{
		if ( _instance == null || !currentTypInd.equals(strTypInd))
		{	_instance = new DwoOrder(strTypInd,conn);	}
		return _instance;
	}
}
