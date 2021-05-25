/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			ALLTEL Communications Inc
 */

/* 
 * MODULE:	LockBean.java
 * 
 * DESCRIPTION: Holds attributes and methods related to an Express object lock. Get rid 
 *		of code replication.
 * 
 * AUTHOR:      pjs
 * 
 * DATE:        4.24.2003
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class LockBean 
{
	private int 	m_iSqncNmbr = 0;
	private String	m_strUserID = null;
	private String	m_strLockDateTime = null;
	private String	m_strType="";

        //Constructors
	public LockBean (String strTypeOfLock, int iSqncNmbr)
	{
		this.m_strType = strTypeOfLock;
		this.m_iSqncNmbr = iSqncNmbr;
	}
	public LockBean (String strTypeOfLock, String strSqncNmbr)
	{
		this.m_strType = strTypeOfLock;
		this.m_iSqncNmbr = Integer.parseInt(strSqncNmbr);
	}
	public LockBean ()
	{
	}

        // Public Getters
	public int getSqncNmbr(){
		return this.m_iSqncNmbr;
	}
	public String getUserID(){
		return this.m_strUserID;
	}
	public String getLockDateTime(){
		return this.m_strLockDateTime;
	}
	public String getType(){
		return this.m_strType;
	}

	// Public Setters
	// Can't set date/time - let Database do that
	// Can't set sqnc nmbr - because it's the constructors job.
	public void setUserId(String strUserID) {
	    this.m_strUserID = strUserID;
	}	
	
	// Public methods
	public int unlock()
	{
	    Connection con = null;
	    Statement stmt = null;
	    //if no type or seq, bail out
	    if ((m_strType==null) || (m_iSqncNmbr <= 0))
	    {
                Log.write(Log.ERROR, "LockBean: unlock() called with invalid Type or Seq#");
		return ERROR;
	    }

	    try {
                String strQuery = "DELETE FROM LOCK_T WHERE SQNC_NMBR = " + m_iSqncNmbr +
				  " AND TYP_IND = '" + m_strType + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "LockBean: unlocked ["+ m_strType + ":" + m_iSqncNmbr+"]");
		stmt.close();
            }
            catch(Exception e) {
		try { 	stmt.close();
		} catch (Exception ee) {}
                return ERROR;
            }
            finally {       
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
	    return SUCCESS;
	}
	
	public int updateLock(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "UPDATE LOCK_T SET LCK_DT=sysdate " +
                        " WHERE SQNC_NMBR=" + m_iSqncNmbr + " AND TYP_IND = '" + m_strType + "'" +
			"  AND USERID = '" + strUserID + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "LockBean: existing lock updated " + m_strType + ":" + m_iSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
		try { 	stmt.close();
		} catch (Exception ee) {}
                return ERROR;
            }
            finally {
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
            setUserId(strUserID);
	    return SUCCESS;
	}
	
	public int lock(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "INSERT INTO LOCK_T (SQNC_NMBR, TYP_IND, USERID, LCK_DT) " +
                                  " VALUES (" + m_iSqncNmbr + ", '" + m_strType + "', '" +
				  strUserID + "', sysdate) ";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "LockBean: Locked " + m_strType + ":" + m_iSqncNmbr);       
		stmt.close();
            }
            catch(SQLException sqle) {
                //another user may have got the darn thing or we already have locked
                if (sqle.getErrorCode() == ORACLE_DUPLICATE)
                {   
		    stmt=null;
                    return updateLock(strUserID);
                }
                return ERROR;
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
            setUserId(strUserID);
        
	    return SUCCESS;
	}
	 
	public final int ERROR = 1;
	public final int SUCCESS = 0;
	private final int ORACLE_DUPLICATE = 1;
	 

}
