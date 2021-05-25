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
 * MODULE:	Action.java
 * 
 * DESCRIPTION: Hold specifics for a single Action (from ACTION_T table).
 * 
 * AUTHOR:      Syed Hussaini
 * 
 * DATE:        11-15-2002
 * 
 * HISTORY:
 *	6-1-2004 psedlak Added ntfy_sqnc_nmbr to Action_t table
 */

/* $Log:     $
*/
/* $Revision:  $
*/

package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.objects.Action;

/**
 * This class is a Singleton - and holds the static ACTION information that
 * is retrieved from ACTION_T table. Each row which is an Action() object
 * is stored into a hash table with the key of following columns
 * STTS_CD_FROM, TYP_IND, RQST_TYP_CD, STTS_CD_TO, ACTN
 */
public class Actions
{
	public static Actions m_instance;
        
	private Hashtable m_hashActions;
	
	public Actions()
	{
		m_hashActions = new Hashtable();
		
		Connection con = null;
		Statement stmt = null;
		Statement stmt2 = null;
		try {
            String strHashKey, strSttsCdFrom, strTypInd, strRqstTypCd, strSttsCdTo, strActn;
			String strPrdTypCd = "0";
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			String strQuery = "SELECT STTS_CD_FROM, TYP_IND, RQST_TYP_CD, STTS_CD_TO, ACTN, ACTN_VRSN_IND, " +
		        "ACTN_DSTNTN, ACTN_EXPRTN_DYS, SCRTY_OBJCT_CD, MDFD_DT, MDFD_USERID, ACTN_SND_CUST_RPLY," +
		        "ACTN_SND_PROV_RPLY, CNFRM_ACTN_IND, CNFRM_ACTN_TXT, NTFY_SQNC_NMBR,  PRDCT_TYP_CD " +
				"FROM ACTION_T ORDER BY TYP_IND, STTS_CD_FROM ASC";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{	
				strSttsCdFrom = rs.getString("STTS_CD_FROM");
			    strTypInd = rs.getString("TYP_IND");
			    strRqstTypCd = rs.getString("RQST_TYP_CD");
			    strSttsCdTo = rs.getString("STTS_CD_TO");
			    strActn = rs.getString("ACTN");
			    strPrdTypCd = rs.getString("PRDCT_TYP_CD");
			    strHashKey = strSttsCdFrom + strTypInd + strRqstTypCd + strSttsCdTo + strActn +  strPrdTypCd;
 				Action objAction = new Action ( strSttsCdFrom,
                        strTypInd,
						strRqstTypCd,
						strSttsCdTo,
						strActn,
						rs.getString("ACTN_VRSN_IND"),
						rs.getString("ACTN_DSTNTN"),
						rs.getString("ACTN_EXPRTN_DYS"),
						rs.getString("SCRTY_OBJCT_CD"),
						rs.getDate("MDFD_DT"), 
						rs.getString("MDFD_USERID"),
						rs.getString("ACTN_SND_CUST_RPLY"),
						rs.getString("ACTN_SND_PROV_RPLY"),
						rs.getString("CNFRM_ACTN_IND"),
						rs.getString("CNFRM_ACTN_TXT"),
						rs.getString("NTFY_SQNC_NMBR"),
						strPrdTypCd
						);
				this.m_hashActions.put(strHashKey, objAction);
			}
                        rs.close();
                        rs=null;
                    Log.write(Log.DEBUG_VERBOSE, "Actions - Build the Singleton Instance");
                                
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "Actions() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
	}

       	/**
	 *  Return an Action() object for the given current status, type indicator
         * type code, status to, and action.
	 *  NOTE: null returned if no match found.
	 */
	public Action getAction(String strSttsCdFrom, String strTypInd, String strRqstTypCd, String strSttsCdTo, String strActn, String strPrdTpCd )
	{
          String strBuildHashKey = strSttsCdFrom + strTypInd + strRqstTypCd + strSttsCdTo + strActn + strPrdTpCd;
          if (strBuildHashKey.length() > 0) {
            if (this.m_hashActions.containsKey(strBuildHashKey)) {
                Action objActionSend = (Action)this.m_hashActions.get(strBuildHashKey);
                return objActionSend;
            } else {
                return null;
            }
          } else {
              return null;
          }
        }

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static Actions getInstance()
	{
		if (m_instance == null)
			m_instance = new Actions();
		
		return m_instance;
	}
	
	public static synchronized void reload() {
		m_instance = null;
		m_instance = new Actions();
	}
}
