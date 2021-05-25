/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2004
 *                                       BY
 *                              ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	ExpressListBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:
 *
 * DATE:        09-09-2003
 *
 * HISTORY:
 *	psedlak 09-12-2003 Built this base class & Fix security hole
 *	psedlak 04-16-2004 added notify ind logic
 *	psedlak 8-25-2004 Only SELECT cols you need (not *), added SELECTClause mbr vars/methods
 *
 *		NOTE NOTE NOTE -- IN order to handle extended searching/sorting, the TBL_CLMN_ADMN_SQNC
 *			value from TABLE_COLUMN_ADMIN_T is used for SORTING !!!  This means that the value
 *			in TABLE_ADMIN_T (col TBL_ADMN_SRT_BY) must exist as one of the columns in that
 *			table's definitino in TABLE_COLUMN_ADMIN_T.
 *
 *	psedlak 4-13-2005 removed extraneous resultset closes
 *	psedlak 6-21-2005 remove extra rs.close(), overrode buildQueryString() to use compnay type from session
 *		object instead of querying every time list is viewed, other clean up
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public abstract class ExpressListBean implements Serializable {

	private ExpressOrder myOrder;

	public ExpressListBean()
	{
		m_strSELECTClauseBASE = "";
	}

	public void init(ExpressOrder order)
	{	this.myOrder = order;
	}

	protected String m_strRstrctSrch;
	protected String m_strRstrctSrchCtgry;
	protected String m_strRstrctSrchVl;

	protected String m_strTblNmbr;
	protected String m_strSrtBy;
	protected String m_strSrtSqnc;
	protected String m_strSrchCtgry;
	protected String m_strSrchVl;
	protected String m_strTblAdmnDbNmJoin;

	protected int m_iTblAdmnSqncNmbr;
	protected String m_strTblAdmnDscrptn;
	protected String m_strTblAdmnDbNm;
	protected String m_strTblAdmnSrtBy;
	protected int m_iTblAdmnSrtBy;	//Number representing m_strTblAdmnSrtBy
	protected String m_strTblAdmnSrtSqnc;
	protected String m_strTblAdmnCtlr;
	protected String m_strTblAdmnCtlrIdx;

	protected String m_strTblAdmnScrtyTgView;
	protected String m_strTblAdmnScrtyTgAdd;
	protected String m_strTblAdmnScrtyTgMod;
	protected String m_strTblAdmnScrtyTgDel;

	private int m_iTblAdmnClmns;

	protected String[] m_strTblAdmnClmnDscrptn = {"","","","","","","","","","","","","","","","","","","","",""};
	protected String[] m_strTblAdmnClmnDbNm = {"","","","","","","","","","","","","","","","","","","","",""};
	protected int[] m_iTblAdmnSeq = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	protected int[] m_iTblAdmnClmnWdth = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

	protected int    iWhereCount = 0;
	protected String[] m_strWhereCriteria = {"","","","","","","","","","","","","","","","","","","","",""};
	protected String m_strSELECTClauseBASE;
	protected String m_strSELECTClause = "";
	protected String m_strWhereClause = "";
	protected String m_strOrderByClause = "";
	protected String m_strQueryString = "";

	//set with NO trailing comma
	public void setSELECTClauseBASE(String requiredColumns)
	{	this.m_strSELECTClauseBASE = requiredColumns; }
	public String getSELECTClauseBASE() { return this.m_strSELECTClauseBASE; }

	public String getRstrctSrch() { return this.m_strRstrctSrch; }
	public String getRstrctSrchCtgry() { return this.m_strRstrctSrchCtgry; }
	public String getRstrctSrchVl() { return this.m_strRstrctSrchVl; }

	public int    getTblAdmnSqncNmbr() { return this.m_iTblAdmnSqncNmbr; }
	public String getTblAdmnDscrptn() { return this.m_strTblAdmnDscrptn; }
	public String getTblAdmnDbNm() { return this.m_strTblAdmnDbNm; }
	public String getTblAdmnSrtBy() { return this.m_strTblAdmnSrtBy; }
	public int getTblAdmnSrtBySeq() { return this.m_iTblAdmnSrtBy; }
	public String getTblAdmnSrtSqnc() { return this.m_strTblAdmnSrtSqnc; }
	public String getTblAdmnCtlr() { return this.m_strTblAdmnCtlr; }
	public String getTblAdmnCtlrIdx() { return this.m_strTblAdmnCtlrIdx; }
	public int    getTblAdmnClmns() { return this.m_iTblAdmnClmns; }

	public String getTblAdmnScrtyTgView() { return this.m_strTblAdmnScrtyTgView; }
	public String getTblAdmnScrtyTgAdd() { return this.m_strTblAdmnScrtyTgAdd; }
	public String getTblAdmnScrtyTgMod() { return this.m_strTblAdmnScrtyTgMod; }
	public String getTblAdmnScrtyTgDel() { return this.m_strTblAdmnScrtyTgDel; }

	public String getTblAdmnClmnDscrptn(int x) { return this.m_strTblAdmnClmnDscrptn[x]; }
	public String getTblAdmnClmnDbNm(int x) { return this.m_strTblAdmnClmnDbNm[x]; }
	public int getTblAdmnClmnWdth(int x) { return this.m_iTblAdmnClmnWdth[x]; }
	public int getTblAdmnSortSeq(int x) { return this.m_iTblAdmnSeq[x]; }

	public String getSrchVl() { return this.m_strSrchVl;  }
	public String getSrchCtgry() { return this.m_strSrchCtgry;  }
	public String getQueryString() { return this.m_strQueryString; }
	public String getOrderBy() { return this.m_strOrderByClause; }

	public void setRstrctSrch(String x) { this.m_strRstrctSrch = x; }
	public void setRstrctSrchCtgry(String x) { this.m_strRstrctSrchCtgry = x; }
	public void setRstrctSrchVl(String x) { this.m_strRstrctSrchVl = x; }

	public void setQueryString(String x) { this.m_strQueryString = x; }
	public void setOrderBy(String x) { this.m_strOrderByClause = x; }
	public void setTblNmbr(String x) { this.m_strTblNmbr = x; }
	public void setSrtSqnc(String x) { this.m_strSrtSqnc = x; }
	public void setSrtBy(String x) { this.m_strSrtBy = x; }
	public void setSrchCtgry(String x) { this.m_strSrchCtgry = x; }
	public void setSrchVl(String x) { this.m_strSrchVl = x; }


	public void buildQueryString(String userid, String strCompanyType, String strCompanySeq)
	{

		Connection con = null;	//Wont open unless needed
		Statement stmt = null;
		ResultSet rs = null;
		String strCmpnyTyp = strCompanyType;
		String strCmpnySqncNmbr = strCompanySeq;

/****	Dont do unless necessary....
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
		}
		catch(Exception e)
		{
			Log.write(Log.ERROR,"ExpressListBean.buildQueryString() e=["+e+"]");
			DatabaseManager.releaseConnection(con);
			return;
		}
******/
		// m_strSrtBy is from Quck Search...
		if ((m_strSrtBy == null) || (m_strSrtBy.length() == 0)
			|| (m_strSrtSqnc == null) || (m_strSrtSqnc.length() == 0))
		{
			// m_strOrderByClause = " ORDER BY " + m_strTblAdmnSrtBy + " " + m_strTblAdmnSrtSqnc;
			m_strOrderByClause = " ORDER BY " + m_iTblAdmnSrtBy + " " + m_strTblAdmnSrtSqnc;
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

		//Added for Notify Indicator Logic
		m_strWhereClause = m_strWhereClause + " AND NOTIFY_T.SQNC_NMBR (+)="+ getTblAdmnCtlrIdx() +
				   " AND NOTIFY_T.TYP_IND (+) = '"+ myOrder.getTYP_IND() +"' ";

		// Only query cols we need....
		//m_strQueryString = "SELECT * FROM " + m_strTblAdmnDbNmJoin + ", NOTIFY_T " + m_strWhereClause;
		m_strQueryString = "SELECT " + m_strSELECTClause + ", NOTIFY_T.NTFY_SQNC_NMBR " +
			" FROM " + m_strTblAdmnDbNmJoin + ", NOTIFY_T " + m_strWhereClause;
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: m_strQueryString=["+m_strQueryString+"]");
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: m_strWhereClause=["+m_strWhereClause+"]");

		if (! strCmpnyTyp.equals("P"))
		{
			//Fix security hole - if OCN table is not set up w/ sepcifici OCNs,the
			// user was able to view all order for all OCNs in xxxListView. We stop
			// that here by restricting by comp seq numnber too.
			if (strCmpnyTyp.equals("W") &&  myOrder.getTYP_IND().equals("S")) // KPEN exception for Trbl Tix
			{}
			else {
				m_strQueryString = m_strQueryString + " AND " +myOrder.getTBL_NAME()+".CMPNY_SQNC_NMBR = " + strCmpnySqncNmbr + " ";
			}

			String strInClause = "";

			// Get all OCN Codes for this users user groups
			String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
				"(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + userid + "')";

			try {
				con = DatabaseManager.getConnection();
				stmt = con.createStatement();
				rs = stmt.executeQuery(strQuery);

				while (rs.next())
				{
					if (rs.getString("OCN_CD").equals("*"))
					{
						String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " +
							rs.getInt("CMPNY_SQNC_NMBR");
						Statement substmt = null;
						substmt = con.createStatement();
						ResultSet subrs = substmt.executeQuery(strSubQuery);
						while (subrs.next() == true)
						{
							strInClause = strInClause + "'" + subrs.getString("OCN_CD") + "',";
						}
						subrs.close();
						subrs=null;
						substmt.close();
						substmt=null;
					}
					else
					{
						strInClause = strInClause + "'" + rs.getString("OCN_CD") + "',";
					}
				}
				rs.close();
				stmt.close();
				rs=null;
				stmt=null;
				DatabaseManager.releaseConnection(con);
			}
			catch(Exception e)
			{
				Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: released conn in buildQuery 2nd Catch()");
				DatabaseManager.releaseConnection(con);
			}

			if ((strInClause.length() > 0) && (!myOrder.getTYP_IND().equals("W")) && (!myOrder.getTYP_IND().equals("X")))
			{
				// strip off last comma
				if (strInClause.endsWith(","))
					strInClause = strInClause.substring(0,strInClause.length()-1);

				m_strQueryString = m_strQueryString + " AND OCN_CD IN (" + strInClause + ")";
			}
			Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: m_strQueryString=["+m_strQueryString+"]");
			Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: m_strWhereClause=["+m_strWhereClause+"]");
		}

	}

	public void buildQueryString(String userid)
	{
		//Just get company type and seq # , and call the overrode method

		// If company is not a Provider, then only allow valid OCNs
		String strQuery = "SELECT CMPNY_TYP, C.CMPNY_SQNC_NMBR FROM USERID_T U, COMPANY_T C WHERE " +
			"U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND USERID = '" + userid + "'";

		Connection con = null;
		Statement stmt = null;
		String strCmpnyTyp = null;
		String strCmpnySqncNmbr = "";

		// Build SELECT SQL statement

		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				strCmpnyTyp = rs.getString("CMPNY_TYP");
				strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
			}
			rs.close(); rs=null;
			stmt.close();  stmt=null;
			DatabaseManager.releaseConnection(con);
		}
		catch(Exception e)
		{
			Log.write(Log.ERROR,"ExpressListBean: e=["+e+"]");
			DatabaseManager.releaseConnection(con);
			return;
		}

		this.buildQueryString(userid, strCmpnyTyp, strCmpnySqncNmbr);
	}

	public int retrieveTableInfo()
	{

		String m_strClmn = "";
		int m_iColSequnceANDSortSeq = 0;
		String m_strRfrncTbl = "";
		String m_strRfrncTypInd = "";
		String m_strRfrncClmn = "";

		int pk = Integer.parseInt(m_strTblNmbr);
		String strQuery = "SELECT * FROM TABLE_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk;

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		m_strSELECTClause="";
		// Get DB Connection
		try {
			// Build SELECT SQL statement

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_iTblAdmnSqncNmbr = rs.getInt("TBL_ADMN_SQNC_NMBR");
				this.m_strTblAdmnDscrptn = rs.getString("TBL_ADMN_DSCRPTN");
				this.m_strTblAdmnDbNm = rs.getString("TBL_ADMN_DB_NM");
				this.m_strTblAdmnSrtBy = rs.getString("TBL_ADMN_SRT_BY");
				// NOTE - m_strTblAdmnSrtBy is converted to a column # to handle extended searching/sorting

				this.m_strTblAdmnSrtSqnc = rs.getString("TBL_ADMN_SRT_SQNC");
				this.m_strTblAdmnCtlr = rs.getString("TBL_ADMN_CTLR");
				this.m_strTblAdmnCtlrIdx = rs.getString("TBL_ADMN_CTLR_IDX");

				this.m_strTblAdmnScrtyTgView = rs.getString("TBL_ADMN_SCRTY_TG_VIEW");
				this.m_strTblAdmnScrtyTgAdd = rs.getString("TBL_ADMN_SCRTY_TG_ADD");
				this.m_strTblAdmnScrtyTgMod = rs.getString("TBL_ADMN_SCRTY_TG_MOD");
				this.m_strTblAdmnScrtyTgDel = rs.getString("TBL_ADMN_SCRTY_TG_DEL");

				this.m_strTblAdmnDbNmJoin = m_strTblAdmnDbNm;
				rs.close();

				// only SELECT what we use here...
				strQuery = "SELECT TBL_CLMN_ADMN_WDTH, TBL_CLMN_ADMN_DSCRPTN, TBL_CLMN_ADMN_DB_NM, TBL_CLMN_ADMN_RFRNC_TBL, " +
					"TBL_CLMN_ADMN_RFRNC_CLMN, TBL_CLMN_ADMN_SQNC FROM TABLE_COLUMN_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk +
					" ORDER BY TBL_CLMN_ADMN_SQNC";
				rs = stmt.executeQuery(strQuery);

				int x=0;
				while (rs.next())
				{
					this.m_iTblAdmnClmnWdth[x] = rs.getInt("TBL_CLMN_ADMN_WDTH");
					this.m_strTblAdmnClmnDscrptn[x] = rs.getString("TBL_CLMN_ADMN_DSCRPTN");

					m_strClmn = rs.getString("TBL_CLMN_ADMN_DB_NM");
					m_strRfrncTbl = rs.getString("TBL_CLMN_ADMN_RFRNC_TBL");
					m_strRfrncClmn = rs.getString("TBL_CLMN_ADMN_RFRNC_CLMN");
					m_iColSequnceANDSortSeq = rs.getInt("TBL_CLMN_ADMN_SQNC"); 	//pjs added for sorting purposes
					m_iTblAdmnSeq[x] = m_iColSequnceANDSortSeq;

					if ((m_strRfrncTbl == null) || (m_strRfrncTbl.equals("*")) ||
					    (m_strRfrncClmn == null) || (m_strRfrncClmn.equals("*")))
						{
							this.m_strTblAdmnClmnDbNm[x] =
								this.m_strTblAdmnDbNm + "." + m_strClmn;
						}
					else
						{
							this.m_strTblAdmnClmnDbNm[x] =
								m_strRfrncTbl + "." + m_strRfrncClmn;
						}

					x++;
				}

				if (x > 0)
				{
					for (int cc = 0; cc < x; cc++ )
					{	if (cc==0)
						{	if (m_strSELECTClauseBASE!=null && m_strSELECTClauseBASE.length()>1)
								m_strSELECTClause = m_strSELECTClauseBASE + ", " + this.m_strTblAdmnClmnDbNm[cc];
							else
								m_strSELECTClause = " " + this.m_strTblAdmnClmnDbNm[cc];
						}
						else
						{	m_strSELECTClause = m_strSELECTClause + ", "+ this.m_strTblAdmnClmnDbNm[cc];
						}
						//Find and assign default sort by option
						if ( m_strTblAdmnSrtBy.equals( this.m_strTblAdmnClmnDbNm[cc] ) )
						{
							m_iTblAdmnSrtBy = m_iTblAdmnSeq[cc];	//Using col # for sorting purposes
Log.write(Log.DEBUG_VERBOSE,"ExprssListBean() ---------------------- Default sort [" + m_strTblAdmnSrtBy + " col:"+m_iTblAdmnSrtBy+"]");
						}
					}
Log.write(Log.DEBUG_VERBOSE,"ExprssListBean() ---------------------- SELECT ITEMS ["+ m_strSELECTClause + "]");

					// everything was successful to this point, we have basic query info available
					// with at least 1 column of data to build a table view with.

					this.m_iTblAdmnClmns = x;
					rs.close();

					// Now look for and add any Secondary Reference Table Names to the FROM Clause

					strQuery = "SELECT DISTINCT TBL_CLMN_ADMN_RFRNC_TBL FROM TABLE_COLUMN_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk;
					rs = stmt.executeQuery(strQuery);
					while (rs.next())
					{
						m_strRfrncTbl = rs.getString("TBL_CLMN_ADMN_RFRNC_TBL");
						if ((m_strRfrncTbl == null) || (m_strRfrncTbl.equals("*")))
						{}
						else
						{
							this.m_strTblAdmnDbNmJoin = this.m_strTblAdmnDbNmJoin + ", " + m_strRfrncTbl;
						}
					}
					rs.close();

					// Now get the JOIN critiria built for the where clause to join the primary
					// and secondary tables with reference columns

					strQuery = "SELECT DISTINCT TBL_CLMN_ADMN_DB_NM, TBL_CLMN_ADMN_RFRNC_TBL, TBL_CLMN_ADMN_RFRNC_TYP_IND FROM TABLE_COLUMN_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk;
					rs = stmt.executeQuery(strQuery);
					while (rs.next())
					{
						m_strClmn = rs.getString("TBL_CLMN_ADMN_DB_NM");
						m_strRfrncTbl = rs.getString("TBL_CLMN_ADMN_RFRNC_TBL");
						m_strRfrncTypInd = rs.getString("TBL_CLMN_ADMN_RFRNC_TYP_IND");

						if ((m_strRfrncTbl == null) || (m_strRfrncTbl.equals("*")))
						{}
						else if (m_strRfrncTypInd.equals("*"))
						{
							this.m_strWhereCriteria[iWhereCount] =
								this.m_strTblAdmnDbNm + "." + m_strClmn + " = " + m_strRfrncTbl + "." + m_strClmn;
							this.iWhereCount++;
						}
						else
						{
							this.m_strWhereCriteria[iWhereCount] =
								this.m_strTblAdmnDbNm + "." + m_strClmn + " = " + m_strRfrncTbl + "." +
								m_strClmn + " AND " + m_strRfrncTbl + ".TYP_IND = '" + myOrder.getTYP_IND() + "'";
							this.iWhereCount++;
						}
					}
//4-13-05					rs.close();

					//It may be possible there are no joins, if so, then build bogus WHERE
                                        if (iWhereCount==0)
                                        {
                                                //Log.write(Log.DEBUG_VERBOSE,"ListBean() Bogus WHERE being used");
                                                this.m_strWhereCriteria[iWhereCount] = " 1=1 ";
                                                this.iWhereCount++;
                                        }

				}
				else
				{
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: released conn in else()");
					DatabaseManager.releaseConnection(con);
					return 1;
				}
			}
			else
			{
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: released conn in 2nd else()");
				DatabaseManager.releaseConnection(con);
				return 1;
			}
			rs.close();
			rs=null;
			stmt.close();
			stmt=null;
		}
		catch(Exception e)
		{
e.printStackTrace();
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: released conn in Catch()");
			DatabaseManager.releaseConnection(con);
			return 1;
		}
		finally
		{
Log.write(Log.DEBUG_VERBOSE,"ExpressListBean: released conn in finally()");
			DatabaseManager.releaseConnection(con);
		}

		return 0;
	}
}


/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/ExpressListBean.java  $

      Rev 1.2   Apr 13 2005 14:18:24   e0069884


      Rev 1.1   Aug 27 2004 13:54:48   e0069884
   Express tracking #86 and #89
/*
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
