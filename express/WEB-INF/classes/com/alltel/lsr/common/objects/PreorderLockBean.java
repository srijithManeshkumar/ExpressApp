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
 * MODULE:		PreorderLockBean.java
 * 
 * DESCRIPTION: (Release 1.1) Holds attributes and methods related to a request lock.
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        02-01-2002
 * 
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class PreorderLockBean 
{
	private int 	m_iPreSqncNmbr = 0;
	private String	m_strUserID = null;
	private String	m_strLockDateTime = null;
	

        //Constructors - request sequence number is mandatory
	public PreorderLockBean(int iPreSqncNmbr)
	{
		this.m_iPreSqncNmbr = iPreSqncNmbr;
	}
	public PreorderLockBean(String strPreSqncNmbr)
	{
		this.m_iPreSqncNmbr = Integer.parseInt(strPreSqncNmbr);
	}

        // Public Getters
	public int getPreSqncNmbr(){
		return this.m_iPreSqncNmbr;
	}
	public String getUserID(){
		return this.m_strUserID;
	}
	public String getLockDateTime(){
		return this.m_strLockDateTime;
	}

	// Public Setters
	// Can't set date/time - let Database do that
	// Can't set pre sqnc nmbr - because it's the constructors job.
	public void setUserId(String strUserID) {
	    this.m_strUserID = strUserID;
	}	
	
	// Public methods
	public int unlockPreorder()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "DELETE FROM LOCK_T WHERE SQNC_NMBR = " + m_iPreSqncNmbr + " AND TYP_IND = 'P'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: request unlocked = " + m_iPreSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {       
		Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: freeing resources in unlockPreorder()");
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
                        " WHERE SQNC_NMBR=" + m_iPreSqncNmbr + " AND TYP_IND = 'P' AND USERID = '" + strUserID + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: existing lock updated = " + m_iPreSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: freeing resources in updateLock()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
            setUserId(strUserID);
	    return SUCCESS;
	}
	
	public int lockPreorder(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "INSERT INTO LOCK_T (SQNC_NMBR, TYP_IND, USERID, LCK_DT) " +
                                  " VALUES (" + m_iPreSqncNmbr + ", 'P', '" + strUserID + "', sysdate) ";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: request Locked = " + m_iPreSqncNmbr);       
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
		Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: freeing resources in lockPreorder()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
            setUserId(strUserID);
        
	    return SUCCESS;
	}
	 
	public int readPreorder()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "SELECT SQNC_NMBR, USERID, TO_CHAR(LCK_DT, 'YYYYMMDD HH24MISS') " +
                        " FROM LOCK_T WHERE SQNC_NMBR = " + m_iPreSqncNmbr + " AND TYP_IND = 'P'";

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
		Log.write(Log.DEBUG_VERBOSE, "PreorderLockBean: freeing resources in readPreorder()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
	    return SUCCESS;
	}

	public final int ERROR = 1;
	public final int SUCCESS = 0;
	private final int ORACLE_DUPLICATE = 1;
	 

}
