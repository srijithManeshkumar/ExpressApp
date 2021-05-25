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
 * MODULE:	DwoListBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	7-11.2005 pjs Reuse for multiple products
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class DwoListBean extends ExpressListBean implements Serializable 
{
	private DwoOrder thisOrder = null;
	private String m_strProductQuery;

	public DwoListBean(String strTypInd)
	{
		thisOrder = DwoOrder.getInstance(strTypInd);
		super.init(thisOrder);
		m_strProductQuery = "";
	}
	
	public DwoListBean(String strTypInd, String strProductSpecificQuery)
	{
		thisOrder = DwoOrder.getInstance(strTypInd);
		super.init(thisOrder);
		m_strProductQuery = strProductSpecificQuery;
	}
	
	public void setProductQuery(String strQ)
	{
		m_strProductQuery = strQ;
	}
	public String getProductQuery() {
		return m_strProductQuery;
	}
	
	public String getQueryString()
	{
		String strQuery = super.getQueryString();
		//if Product specific, make sure its part of query
		if (m_strProductQuery.length()>0)
			return strQuery+" "+m_strProductQuery;
		else	
			return strQuery;
	}
	
}
