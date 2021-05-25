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
 * MODULE:		DslLockBean.java
 * 
 * DESCRIPTION: Holds attributes and methods related to a dsl order lock.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-05-2002
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

public class DslLockBean 
{
	private int 	m_iDslSqncNmbr = 0;
	private String	m_strUserID = null;
	private String	m_strLockDateTime = null;
	

        //Constructors - dsl order sequence number is mandatory
	public DslLockBean(int iDslSqncNmbr)
	{
		this.m_iDslSqncNmbr = iDslSqncNmbr;
	}
	public DslLockBean(String strDslSqncNmbr)
	{
		this.m_iDslSqncNmbr = Integer.parseInt(strDslSqncNmbr);
	}

        // Public Getters
	public int getDslSqncNmbr(){
		return this.m_iDslSqncNmbr;
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
	public int unlockDsl()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "DELETE FROM LOCK_T WHERE SQNC_NMBR = " + m_iDslSqncNmbr + " AND TYP_IND = 'D'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DslLockBean: dsl order unlocked = " + m_iDslSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {       
		Log.write(Log.DEBUG_VERBOSE, "DslLockBean: freeing resources in unlockDsl()");
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
                        " WHERE SQNC_NMBR=" + m_iDslSqncNmbr + " AND TYP_IND = 'D'" +
			"  AND USERID = '" + strUserID + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DslLockBean: existing lock updated = " + m_iDslSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		Log.write(Log.DEBUG_VERBOSE, "DslLockBean: freeing resources in updateLock()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
            setUserId(strUserID);
	    return SUCCESS;
	}
	
	public int lockDsl(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "INSERT INTO LOCK_T (SQNC_NMBR, TYP_IND, USERID, LCK_DT) " +
                                  " VALUES (" + m_iDslSqncNmbr + ", 'D', '" + strUserID + "', sysdate) ";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "DslLockBean: dsl order Locked = " + m_iDslSqncNmbr);       
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
		Log.write(Log.DEBUG_VERBOSE, "DslLockBean: freeing resources in lockDsl()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
            setUserId(strUserID);
        
	    return SUCCESS;
	}
	 
	public int readDsl()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "SELECT SQNC_NMBR, USERID, TO_CHAR(LCK_DT, 'YYYYMMDD HH24MISS') " +
                        " FROM LOCK_T WHERE SQNC_NMBR = " + m_iDslSqncNmbr + " AND TYP_IND = 'D'";

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
		Log.write(Log.DEBUG_VERBOSE, "DslLockBean: freeing resources in readDsl()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
	    return SUCCESS;
	}

	public final int ERROR = 1;
	public final int SUCCESS = 0;
	private final int ORACLE_DUPLICATE = 1;
	 

}
