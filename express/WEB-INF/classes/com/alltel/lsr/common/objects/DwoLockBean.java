/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		DwoLockBean.java
 * 
 * DESCRIPTION: Holds attributes and methods related to a data work order lock.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2004
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

public class DwoLockBean 
{
	private int 	m_iDwoSqncNmbr = 0;
	private String	m_strUserID = null;
	private String	m_strLockDateTime = null;
	private String	m_strTypInd = null;
	

        //Constructors - data work order sequence number is mandatory
	public DwoLockBean(String strTypeInd, int iDwoSqncNmbr)
	{
		this.m_iDwoSqncNmbr = iDwoSqncNmbr;
		this.m_strTypInd = strTypeInd;
	}
	public DwoLockBean(String strTypeInd, String strDwoSqncNmbr)
	{
		this.m_iDwoSqncNmbr = Integer.parseInt(strDwoSqncNmbr);
		this.m_strTypInd = strTypeInd;
	}

        // Public Getters
	public int getDwoSqncNmbr(){
		return this.m_iDwoSqncNmbr;
	}
	public String getUserID(){
		return this.m_strUserID;
	}
	public String getLockDateTime(){
		return this.m_strLockDateTime;
	}

	// Public Setters
	// Can't set date/time - let Database do that
	// Can't set rqst sqnc nmbr - because it's the constructors job.
	public void setUserId(String strUserID) {
	    this.m_strUserID = strUserID;
	}	
	
	// Public methods
	public int unlockDwo()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "DELETE FROM LOCK_T WHERE SQNC_NMBR = " + m_iDwoSqncNmbr + " AND TYP_IND = '" + m_strTypInd + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: data work order unlocked = " + m_iDwoSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {       
		Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: freeing resources in unlockDwo()");
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
                        " WHERE SQNC_NMBR=" + m_iDwoSqncNmbr + " AND TYP_IND = '" + m_strTypInd + "'" +
			"  AND USERID = '" + strUserID + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: existing lock updated = " + m_iDwoSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: freeing resources in updateLock()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
            setUserId(strUserID);
	    return SUCCESS;
	}
	
	public int lockDwo(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "INSERT INTO LOCK_T (SQNC_NMBR, TYP_IND, USERID, LCK_DT) " +
                                  " VALUES (" + m_iDwoSqncNmbr + ", '" + m_strTypInd + "', '" + strUserID + "', sysdate) ";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: data work order Locked = " + m_iDwoSqncNmbr);       
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
		Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: freeing resources in lockDwo()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
            setUserId(strUserID);
        
	    return SUCCESS;
	}
	 
	public int readDwo()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "SELECT SQNC_NMBR, USERID, TO_CHAR(LCK_DT, 'YYYYMMDD HH24MISS') " +
                        " FROM LOCK_T WHERE SQNC_NMBR = " + m_iDwoSqncNmbr + " AND TYP_IND = '" + m_strTypInd + "'";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(strQuery);
                if (rs.next())
                {
                    this.m_strUserID = rs.getString("USERID");
                    this.m_strLockDateTime = rs.getString(3);
                }
                else
                {
                    return ERROR;
                }            
		stmt.close();
            
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		Log.write(Log.DEBUG_VERBOSE, "DwoLockBean: freeing resources in readDwo()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
	    return SUCCESS;
	}

	public final int ERROR = 1;
	public final int SUCCESS = 0;
	private final int ORACLE_DUPLICATE = 1;
	 

}
