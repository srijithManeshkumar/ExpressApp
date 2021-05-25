/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	DslListBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class DslListBean extends ExpressListBean implements Serializable {
	
	private DslOrder thisOrder = DslOrder.getInstance();
	public DslListBean()
	{
		super.init(thisOrder);
	}

	//DSL's may not have an OCN associagted with them (DSL_T ) doesnt have 
	//OCN either -so override base class here
	public void buildQueryString(String userid)
	{
		if ((m_strSrtBy == null) || (m_strSrtBy.length() == 0) 
			|| (m_strSrtSqnc == null) || (m_strSrtSqnc.length() == 0))
		{
			m_strOrderByClause = " ORDER BY " + m_strTblAdmnSrtBy + " " + m_strTblAdmnSrtSqnc;
		}
		else
		{
			m_strOrderByClause =  " ORDER BY " + m_strSrtBy + " " + m_strSrtSqnc;
		}

		if (	((m_strSrchCtgry == null) || (m_strSrchCtgry.length() == 0) || 
			 (m_strSrchVl == null) || (m_strSrchVl.length() == 0)) &&
			((m_strRstrctSrchCtgry == null) || (m_strRstrctSrchCtgry.length() == 0) || 
			 (m_strRstrctSrchVl == null) || (m_strRstrctSrchVl.length() == 0))	)
		{
		}
		else
		{
			if ( (m_strRstrctSrch != null) && (m_strRstrctSrch.equals("yes")) )
			{
				int x = Integer.parseInt(m_strRstrctSrchCtgry); 
				this.m_strWhereCriteria[iWhereCount] = 
					"UPPER(" + m_strTblAdmnClmnDbNm[x] + ") = " + "'" + m_strRstrctSrchVl.toUpperCase() + "'";

				if ( (m_strSrchCtgry != null) && (m_strSrchCtgry.length() > 0) && 
				     (m_strSrchVl != null) && (m_strSrchVl.length() > 0) )
				{
					x = Integer.parseInt(m_strSrchCtgry); 
					this.m_strWhereCriteria[iWhereCount] = this.m_strWhereCriteria[iWhereCount] + 
						"AND UPPER(" + m_strTblAdmnClmnDbNm[x] + ") LIKE " + "'" + m_strSrchVl.toUpperCase() + "'";
				}
			}
			else if ( (m_strSrchCtgry != null) && (m_strSrchVl != null) )
			{
				int x = Integer.parseInt(m_strSrchCtgry); 
				this.m_strWhereCriteria[iWhereCount] = 
					"UPPER(" + m_strTblAdmnClmnDbNm[x] + ") LIKE " + "'" + m_strSrchVl.toUpperCase() + "'";
			}
			this.iWhereCount++;
		}
		
		for (int c = 0; c < iWhereCount ; c++ )
		{
			if (c == 0)
			{
				this.m_strWhereClause = " WHERE " + this.m_strWhereCriteria[c];
			}
			else
			{
				this.m_strWhereClause = this.m_strWhereClause +  " AND " + this.m_strWhereCriteria[c];
			}
		}

		// If company is not a Provider, then only allow valid user
		String strQuery = "SELECT CMPNY_TYP, C.CMPNY_SQNC_NMBR FROM USERID_T U, COMPANY_T C WHERE " + 
			"U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND USERID = '" + userid + "'";

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strCmpnyTyp = null;
		String strCmpnySqncNmbr = null;

		// Build SELECT SQL statement

		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery);
				
			if (rs.next())
			{
				strCmpnyTyp = rs.getString("CMPNY_TYP");
				strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
			}

			DatabaseManager.releaseConnection(con);
		}
		catch(Exception e)
		{
			DatabaseManager.releaseConnection(con);
		}

		m_strQueryString = "SELECT * FROM " + m_strTblAdmnDbNmJoin + m_strWhereClause;

		if (! strCmpnyTyp.equals("P"))
		{
			m_strQueryString = m_strQueryString + " AND DSL_T.CMPNY_SQNC_NMBR = " + strCmpnySqncNmbr;
		}
	}
}
