package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class TableAdminBean implements Serializable {

	private String m_strRstrctSrch;
	private String m_strRstrctSrchCtgry;
	private String m_strRstrctSrchVl;

	private String m_strRqstTblNmbr;
	private String m_strRqstSrtBy;
	private String m_strRqstSrtSqnc;
	private String m_strRqstSrchCtgry;
	private String m_strRqstSrchVl;
	private String m_strTblAdmnDbNmJoin;

	private int m_iTblAdmnSqncNmbr;
	private String m_strTblAdmnDscrptn;
	private String m_strTblAdmnDbNm;
	private String m_strTblAdmnSrtBy;
	private String m_strTblAdmnSrtSqnc;
	private String m_strTblAdmnCtlr;
	private String m_strTblAdmnCtlrIdx;

	private String m_strTblAdmnScrtyTgView;
	private String m_strTblAdmnScrtyTgAdd;
	private String m_strTblAdmnScrtyTgMod;
	private String m_strTblAdmnScrtyTgDel;

	private int m_iTblAdmnClmns;

	private String[] m_strTblAdmnClmnDscrptn = {"","","","","","","","","","","","","","",""};
	private String[] m_strTblAdmnClmnDbNm = {"","","","","","","","","","","","","","",""};
	private int[] m_iTblAdmnClmnWdth = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

	private int    w = 0;
	private String[] m_strWhereCriteria = {"","","","","","","","","","","","","","",""};
	private String m_strWhereClause = "";
	private String m_strOrderByClause = "";
	private String m_strQueryString = "";

	public String getRstrctSrch() { return this.m_strRstrctSrch; }
	public String getRstrctSrchCtgry() { return this.m_strRstrctSrchCtgry; }
	public String getRstrctSrchVl() { return this.m_strRstrctSrchVl; }

	public int    getTblAdmnSqncNmbr() { return this.m_iTblAdmnSqncNmbr; }
	public String getTblAdmnDscrptn() { return this.m_strTblAdmnDscrptn; }
	public String getTblAdmnDbNm() { return this.m_strTblAdmnDbNm; }
	public String getTblAdmnSrtBy() { return this.m_strTblAdmnSrtBy; }
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
	
	public String getRqstSrchVl() { return this.m_strRqstSrchVl;  }
	public String getQueryString() { return this.m_strQueryString; }

	public void setRstrctSrch(String x) { this.m_strRstrctSrch = x; }
	public void setRstrctSrchCtgry(String x) { this.m_strRstrctSrchCtgry = x; }
	public void setRstrctSrchVl(String x) { this.m_strRstrctSrchVl = x; }

	public void setQueryString(String x) { this.m_strQueryString = x; }
	public void setRqstTblNmbr(String x) { this.m_strRqstTblNmbr = x; }
	public void setRqstSrtSqnc(String x) { this.m_strRqstSrtSqnc = x; }
	public void setRqstSrtBy(String x) { this.m_strRqstSrtBy = x; }
	public void setRqstSrchCtgry(String x) { this.m_strRqstSrchCtgry = x; }
	public void setRqstSrchVl(String x) { this.m_strRqstSrchVl = x; }


	public void buildQueryString()
	{
		if ((m_strRqstSrtBy == null) || (m_strRqstSrtBy.length() == 0) 
			|| (m_strRqstSrtSqnc == null) || (m_strRqstSrtSqnc.length() == 0))
		{
			m_strOrderByClause = " ORDER BY " + m_strTblAdmnSrtBy + " " + m_strTblAdmnSrtSqnc;
		}
		else
		{
			m_strOrderByClause =  " ORDER BY " + m_strRqstSrtBy + " " + m_strRqstSrtSqnc;
		}

		if (	((m_strRqstSrchCtgry == null) || (m_strRqstSrchCtgry.length() == 0) || 
			 (m_strRqstSrchVl == null) || (m_strRqstSrchVl.length() == 0)) &&
			((m_strRstrctSrchCtgry == null) || (m_strRstrctSrchCtgry.length() == 0) || 
			 (m_strRstrctSrchVl == null) || (m_strRstrctSrchVl.length() == 0))	)
		{
		}
		else
		{
			if ( (m_strRstrctSrch != null) && (m_strRstrctSrch.equals("yes")) )
			{
				int x = Integer.parseInt(m_strRstrctSrchCtgry); 
				this.m_strWhereCriteria[w] = 
					"UPPER(" + m_strTblAdmnClmnDbNm[x] + ") = " + "'" + m_strRstrctSrchVl.toUpperCase() + "'";

				if ( (m_strRqstSrchCtgry != null) && (m_strRqstSrchCtgry.length() > 0) && 
				     (m_strRqstSrchVl != null) && (m_strRqstSrchVl.length() > 0) )
				{
					x = Integer.parseInt(m_strRqstSrchCtgry); 
					this.m_strWhereCriteria[w] = this.m_strWhereCriteria[w] + 
						"AND UPPER(" + m_strTblAdmnClmnDbNm[x] + ") LIKE " + "'" + m_strRqstSrchVl.toUpperCase() + "'";
				}
			}
			else if ( (m_strRqstSrchCtgry != null) && (m_strRqstSrchVl != null) )
			{
				int x = Integer.parseInt(m_strRqstSrchCtgry); 
				this.m_strWhereCriteria[w] = 
					"UPPER(" + m_strTblAdmnClmnDbNm[x] + ") LIKE " + "'" + m_strRqstSrchVl.toUpperCase() + "'";
			}
			this.w++;
		}
		
		for (int c = 0; c < w ; c++ )
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

		m_strQueryString = "SELECT * FROM " + m_strTblAdmnDbNmJoin + m_strWhereClause + m_strOrderByClause;
	}

	public int retrieveTableInfo()
	{	
	
		String m_strClmn = "";
		String m_strRfrncTbl = "";
		String m_strRfrncClmn = "";

		int pk = Integer.parseInt(m_strRqstTblNmbr);
		String strQuery = "SELECT * FROM TABLE_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk;

		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

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
				this.m_strTblAdmnSrtSqnc = rs.getString("TBL_ADMN_SRT_SQNC");
				this.m_strTblAdmnCtlr = rs.getString("TBL_ADMN_CTLR");
				this.m_strTblAdmnCtlrIdx = rs.getString("TBL_ADMN_CTLR_IDX");
				
				this.m_strTblAdmnScrtyTgView = rs.getString("TBL_ADMN_SCRTY_TG_VIEW");
				this.m_strTblAdmnScrtyTgAdd = rs.getString("TBL_ADMN_SCRTY_TG_ADD");
				this.m_strTblAdmnScrtyTgMod = rs.getString("TBL_ADMN_SCRTY_TG_MOD");
				this.m_strTblAdmnScrtyTgDel = rs.getString("TBL_ADMN_SCRTY_TG_DEL");

				this.m_strTblAdmnDbNmJoin = m_strTblAdmnDbNm;
				rs.close();

				strQuery = "SELECT * FROM TABLE_COLUMN_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk + " ORDER BY TBL_CLMN_ADMN_SQNC";
				rs = stmt.executeQuery(strQuery);

				int x=0;
				while (rs.next())
				{
					this.m_iTblAdmnClmnWdth[x] = rs.getInt("TBL_CLMN_ADMN_WDTH");
					this.m_strTblAdmnClmnDscrptn[x] = rs.getString("TBL_CLMN_ADMN_DSCRPTN");

					m_strClmn = rs.getString("TBL_CLMN_ADMN_DB_NM");
					m_strRfrncTbl = rs.getString("TBL_CLMN_ADMN_RFRNC_TBL");
					m_strRfrncClmn = rs.getString("TBL_CLMN_ADMN_RFRNC_CLMN");

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
					
					strQuery = "SELECT DISTINCT TBL_CLMN_ADMN_DB_NM, TBL_CLMN_ADMN_RFRNC_TBL FROM TABLE_COLUMN_ADMIN_T WHERE TBL_ADMN_SQNC_NMBR = " + pk;
					rs = stmt.executeQuery(strQuery);
					while (rs.next())
					{
						m_strClmn = rs.getString("TBL_CLMN_ADMN_DB_NM");
						m_strRfrncTbl = rs.getString("TBL_CLMN_ADMN_RFRNC_TBL");

						if ((m_strRfrncTbl == null) || (m_strRfrncTbl.equals("*")))
						{}
						else
						{
							this.m_strWhereCriteria[w] = 
								this.m_strTblAdmnDbNm + "." + m_strClmn + " = " + m_strRfrncTbl + "." + m_strClmn;
							this.w++;
						}
					}
					rs.close();

				}
				else
				{
					DatabaseManager.releaseConnection(con);
					return 1;
				}
			}
			else
			{
				DatabaseManager.releaseConnection(con);
				return 1;
			}
		}
		catch(Exception e)
		{
			DatabaseManager.releaseConnection(con);
			return 1;
		}
		finally
		{	
			DatabaseManager.releaseConnection(con);
		}

		return 0;
	}
}
