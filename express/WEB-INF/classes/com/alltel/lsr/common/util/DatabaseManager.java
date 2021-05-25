package com.alltel.lsr.common.util;

import java.sql.*;
import com.alltel.lsr.common.util.PropertiesManager;

/** 
 * The DatabaseManager class is the interface with Weblogic's JDBC connection pool.
 * It provides methods that allow requestors to retrieve connections from the pool
 * and release them.
 * Modules that call getConnection() should call releaseConnection() when they
 * are done with the connection. This will ensure the connection is made immediately
 * available to other modules.
 * 
 * CHANGE HISTORY:
 * 
 * 10/08/2001 PJS Cloned from eWave and customized to LSR
 * 8/08/2002 PJS Changed to allow multiple connection pools to multiple data sources
 *          (CAMS and FrontWare).
 * 8/26/2002 pjs -set connection object to null on close() and got rid of ^Ms.
 * 10/11/2002 pjs -added stuff to reset connections if necessary
 * 1/16/2004 pjs -tweeked for WL 7.*
 */


public class DatabaseManager {

    /**
     * The getConnection() method is used to retrieve a connection from one of the
     * connection pools. The method is declared synchronized so that the open 
     * connection counter is accurate.
     *
     * @exception Exception     database error
     *
     * @return    Connection    connection to database
     */
	public synchronized static final Connection getConnection(int iWhichConnectionPool)
    throws Exception {
        //Log.write(" getConnection" );
        //System.out.println("getConnection");
        Connection con = null;
        try{
        // create instance of Weblogic's JDBC pool driver
		Class.forName("weblogic.jdbc.pool.Driver").newInstance();
                Driver myDriver = (Driver) Class.forName("weblogic.jdbc.pool.Driver").newInstance();
		
		// extra check in case this static class was garbage collected for some reason
		if ((m_aNumOpenConnections == null) || (m_aConnectionPoolNames == null))
		{
		    loadArrays();
		}
	
        if (iWhichConnectionPool == LSRRP_CONNECTION)
        {
            // retrieve connection from lsrrp connection pool
            //OLD WAY   con = DriverManager.getConnection("jdbc:weblogic:pool:lsrrp", null);            
                //jdbc:oracle:thin:@165.143.129.177:1526:debpp
            
                 // for dev this has to be lsrrp_dev2
                 //con =  myDriver.connect("jdbc:weblogic:pool:lsrrp_dev2",null);
                 // for staging this has to be lsrrp
                
                 String cpName = PropertiesManager.getProperty("lsr.SSISource.cpname","");
                 con =  myDriver.connect("jdbc:weblogic:pool:"+cpName,null);
                
            } else
                if (iWhichConnectionPool == CAMSP_CONNECTION) {
            con =  myDriver.connect("jdbc:weblogic:pool:camsp",null);
                } else if (iWhichConnectionPool == FWP_CONNECTION) {
            con =  myDriver.connect("jdbc:weblogic:pool:fwp",null);                
                }else if (iWhichConnectionPool == LERG_CONNECTION) {
                con =  myDriver.connect("jdbc:weblogic:pool:lerg",null);
                }else if (iWhichConnectionPool == FWRDEV_CONNECTION) {
                con =  myDriver.connect("jdbc:weblogic:pool:fwr_dev",null);
        }

        m_aNumOpenConnections[iWhichConnectionPool] ++;      // increment open connections counter
        Log.write("Opened " + m_aConnectionPoolNames[iWhichConnectionPool] + " database connection. " 
                 + m_aNumOpenConnections[iWhichConnectionPool] + " "
                 + m_aConnectionPoolNames[iWhichConnectionPool]+ " connections now open.");
            Log.write(" getConnection = closed or not "+con.isClosed());
            // con =null;
            Log.write("getConnection con"+con.toString());
		return con;
        }catch(Exception e){
            Log.write("Exception in DatabaseManager.getConnection: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
	}


    /**
     * The releaseConnection() method is used to release a connection back to the 
     * appropriate connection pool and decrement its connections counter. If the
     * connection is null or already closed this method will record this fact in the log
     * file. The connections counter is decremented even in these situations.
     *
     * @return    void
     */
	public synchronized static void releaseConnection(Connection con, int iWhichConnectionPool)
	{
	    // extra check in case this static class was garbage collected for some reason
		if ((m_aNumOpenConnections == null) || (m_aConnectionPoolNames == null))
		{
		    loadArrays();
		}

		try 
		{
			if (con != null)
			{
			    if (con.isClosed())
			    {
			        // connection was already closed. This can happen if a module releases the
			        // connection but also has a finally block that calls releaseConnection()
                    Log.write(m_aConnectionPoolNames[iWhichConnectionPool] + " connection already closed. " 
                             + m_aNumOpenConnections[iWhichConnectionPool] + " "
                             + m_aConnectionPoolNames[iWhichConnectionPool] + " connections now open.");
			    }
			    else
			    {
			        con.close();
				con=null;	//speeds up GC
			        // close this connection
				m_aNumOpenConnections[iWhichConnectionPool] --;      // decrement counter
				Log.write("Closed " + m_aConnectionPoolNames[iWhichConnectionPool] 
                             	+ " database connection. " 
                             	+ m_aNumOpenConnections[iWhichConnectionPool] + " "
                             	+ m_aConnectionPoolNames[iWhichConnectionPool] 
                             	+ " connections now open.");
			    }
			}
			else
			{
			    // connection was null. This can happen if a module did not call getConnection()
			    // for its connection but has a finally block that calls releaseConnection()
                Log.write(m_aConnectionPoolNames[iWhichConnectionPool] + " connection was null. " 
                         + m_aNumOpenConnections[iWhichConnectionPool] + " "
                         + m_aConnectionPoolNames[iWhichConnectionPool] + " connections now open.");
		    }
		}
		catch (Exception e)
		{
			// log this exception
			Log.write("Exception in DatabaseManager.releaseConnection: "+e.getMessage());
		}
	}
 
 
 
    /**
     * This version of the getConnection() method is used to retrieve a connection
     * from the lsrrp connection pool.
     *
     * @exception Exception     database error
     *
     * @return    Connection    connection to database
     */
	public static final Connection getConnection()
					               throws Exception
	{
        return getConnection(LSRRP_CONNECTION);
	}

    /**
     * This version of the releaseConnection() method is used to release a connection
     * back to the lsrrp connection pool.
     *
     * @return    void
     */
	public static void releaseConnection(Connection con)
	{
		releaseConnection(con, LSRRP_CONNECTION);
	}

    /**
     * This resets the connection ppol
     * @return    void
     */
	public static synchronized void resetPool(Connection con)
	{
        	Log.write("Application requested a pool refresh!");
		try {
// line wouldnt compile with WL 8.1
//			((weblogic.jdbc.pool.Connection)con).refresh();
		} catch (Exception e ) {}
	}
	public static String getTestSQLString( int iWhichConnectionPool )
	{
		return m_aSureFireSQL[iWhichConnectionPool];
	}
 
 
    private static synchronized void loadArrays()
    {
        // initialize open connections counters
        m_aNumOpenConnections = new int[NUM_CONNECTION_POOLS];
        for (int i=0;i< NUM_CONNECTION_POOLS;i++)
            m_aNumOpenConnections[i]  = 0;
        Log.write("Initialized open connection counters.");
        
        // intialize connection pool names list
        m_aConnectionPoolNames = new String[NUM_CONNECTION_POOLS];
        m_aConnectionPoolNames[LSRRP_CONNECTION] = LSRRP_POOL_NAME;
        m_aConnectionPoolNames[CAMSP_CONNECTION] = CAMSP_POOL_NAME;
        m_aConnectionPoolNames[FWP_CONNECTION] = FWP_POOL_NAME;
        m_aConnectionPoolNames[LERG_CONNECTION] = LERG_POOL_NAME;
        m_aConnectionPoolNames[FWRDEV_CONNECTION] = FWRDEV_POOL_NAME;
        Log.write("Initialized connection pool lists.");

	// Initialize SQL 
	m_aSureFireSQL = new String[NUM_CONNECTION_POOLS];
	m_aSureFireSQL[LSRRP_CONNECTION] = " SELECT 1 FROM DUAL";	//Oracle
	m_aSureFireSQL[CAMSP_CONNECTION] = " SELECT CURRENT DATE FROM SYSIBM.SYSDUMMY1";//DB2
	m_aSureFireSQL[FWP_CONNECTION] = " SELECT 1 FROM DUAL";		//Oracle
        m_aSureFireSQL[LERG_CONNECTION] = " SELECT 1 FROM DUAL";
        m_aSureFireSQL[FWRDEV_CONNECTION] = " SELECT 1 FROM DUAL";
    }


    /**
     * Constructor for the DatabaseManager class.
     */
    public DatabaseManager()
    {
        Log.write( "\n\n **** CONSTRUCTED DATABASE MANAGER ***\n");
    }
   
        
    /**
     * Array of counters to keep track of open connections. This array contains
     * one integer counter for each available connection pool.
     */
    private static int [] m_aNumOpenConnections;
    /**
     * Array of names for the available connection pools. These are used for logging.
     */
    private static String [] m_aConnectionPoolNames;
    
    /**
     * Array of SQL that are guarantted to work if connection is valid
     */
    private static String [] m_aSureFireSQL;
    
    /** Identifier for pool connections. */
    public static final int NUM_CONNECTION_POOLS = 5;//added 1 for LERG and 1 for FWRDEV; 3+2
    public static final int LSRRP_CONNECTION = 0;
    public static final int CAMSP_CONNECTION = 1;
    public static final int FWP_CONNECTION = 2;
    public static final int LERG_CONNECTION = 3;
    public static final int FWRDEV_CONNECTION = 4;
    
    /** Name used (in log messages) to identify pool connections. */
    // for dev this has to be lsrrp_dev2
    //private static final String LSRRP_POOL_NAME = "lsrrp_dev2";
    // for staging this has to be lsrrp
    private static final String LSRRP_POOL_NAME = "lsrrp";
    private static final String CAMSP_POOL_NAME = "camsp";
    private static final String FWP_POOL_NAME = "fwp";
    // added by kumar
    private static final String LERG_POOL_NAME = "lerg";
    // added by Antony
    private static final String FWRDEV_POOL_NAME = "fwr_dev";
  
}  // end of DatabaseManager class


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UTIL/DatabaseManager.java  $
/*
/*   Rev 1.2   Aug 26 2002 14:40:54   sedlak
/* 
/*
/*   Rev 1.1   08 Apr 2002 14:27:38   sedlak
/*Added support for connection pools to CAMS data warehouse
/*and FrontWare oracle db.
/*
/*   Rev 1.0   23 Jan 2002 11:05:34   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
