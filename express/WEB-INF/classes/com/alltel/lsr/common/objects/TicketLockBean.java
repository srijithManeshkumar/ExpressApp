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
 * MODULE:		TicketLockBean.java
 * 
 * DESCRIPTION: Holds attributes and methods related to a ticket lock.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002  initial check-in.
 *
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class TicketLockBean 
{
	private int 	m_iTcktSqncNmbr = 0;
	private String	m_strUserID = null;
	private String	m_strLockDateTime = null;
	

        //Constructors - ticket sequence number is mandatory
	public TicketLockBean(int iTcktSqncNmbr)
	{
		this.m_iTcktSqncNmbr = iTcktSqncNmbr;
	}
	public TicketLockBean(String strTcktSqncNmbr)
	{
		this.m_iTcktSqncNmbr = Integer.parseInt(strTcktSqncNmbr);
	}

        // Public Getters
	public int getTcktSqncNmbr(){
		return this.m_iTcktSqncNmbr;
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
	public int unlockTicket()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "DELETE FROM LOCK_T WHERE SQNC_NMBR = " + m_iTcktSqncNmbr + " AND TYP_IND = 'T'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: ticket unlocked = " + m_iTcktSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {       
		Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: freeing resources in unlockTicket()");
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
                        " WHERE SQNC_NMBR=" + m_iTcktSqncNmbr + " AND TYP_IND = 'T'" +
			"  AND USERID = '" + strUserID + "'";
                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: existing lock updated = " + m_iTcktSqncNmbr);       
		stmt.close();
            }
            catch(Exception e) {
                return ERROR;
            }
            finally {
		Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: freeing resources in updateLock()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
            setUserId(strUserID);
	    return SUCCESS;
	}
	
	public int lockTicket(String strUserID)
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "INSERT INTO LOCK_T (SQNC_NMBR, TYP_IND, USERID, LCK_DT) " +
                                  " VALUES (" + m_iTcktSqncNmbr + ", 'T', '" + strUserID + "', sysdate) ";

                con = DatabaseManager.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(strQuery);
                Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: ticket Locked = " + m_iTcktSqncNmbr);       
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
		Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: freeing resources in lockTicket()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
            setUserId(strUserID);
        
	    return SUCCESS;
	}
	 
	public int readTicket()
	{
	    Connection con = null;
	    Statement stmt = null;
	    
            try {
                String strQuery = "SELECT SQNC_NMBR, USERID, TO_CHAR(LCK_DT, 'YYYYMMDD HH24MISS') " +
                        " FROM LOCK_T WHERE SQNC_NMBR = " + m_iTcktSqncNmbr + " AND TYP_IND = 'T'";

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
		Log.write(Log.DEBUG_VERBOSE, "TicketLockBean: freeing resources in readTicket()");
		stmt=null;
                DatabaseManager.releaseConnection(con);
            }
        
	    return SUCCESS;
	}

	public final int ERROR = 1;
	public final int SUCCESS = 0;
	private final int ORACLE_DUPLICATE = 1;
	 

}
