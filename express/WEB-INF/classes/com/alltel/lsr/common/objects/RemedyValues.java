/* EK: Remedy/Dnoc project. This is a utility file for values mapping.
 * This singleton is used for mapping Remedy serverity levels with Express trouble 
 * Type Levels.
 */
package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton 
 */
public class RemedyValues 
{
	public static RemedyValues m_instance;
	private Hashtable hRemedyValues;
	
	private RemedyValues(){
		init();
	}
	
	public void init()
	{
		hRemedyValues = new Hashtable( 10);
		RemedyExpMapNode mapNode = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String strQuery = "SELECT EXPRESS_SERV, EXPRESS_PRTY, REMEDY_IMPCT, REMEDY_URG, REMEDY_PRTY " +
				   "FROM REM_EXP_MAP_T ORDER BY EXPRESS_SERV, EXPRESS_PRTY";
		ResultSet rset = null;
		
		try {
			conn = DatabaseManager.getConnection();
			pstmt = conn.prepareStatement( strQuery );
			pstmt.clearParameters();			
			rset = pstmt.executeQuery();	 
			while ( rset.next() )		
			{
				mapNode = new RemedyExpMapNode( rset.getString( 1 ),  rset.getString( 2 ), 
					rset.getString( 3), rset.getString( 4 ), rset.getString( 5 ));
				hRemedyValues.put( (String )( rset.getString( 1 ) + "-" + rset.getString( 2 )), mapNode );
			}
		} catch(Exception e) 
		{
			e.printStackTrace();	
			Log.write(Log.ERROR, "RemedyValues.init() :  ERROR PERFORMING DATABASE ACTIVITY " + e.toString() );
		} finally {	// Clean up			
			try{
				if ( rset != null ){ rset.close(); }
				if ( pstmt != null ){ pstmt.close(); }
			}catch (Exception e){
				e.printStackTrace();
				Log.write(Log.ERROR, " RemedyValues.init():\n "   + e.toString() );
			}
			DatabaseManager.releaseConnection(conn);
		}			
		
	}
	
	/**
	 *  Return returns a matching Node with express values and Remedy values.
	 *  NOTE: null returned if nothing here matches express values..
	 */
	public RemedyExpMapNode getMapNode( String xpressServ, String xpressPrty )
	{
		String strHashKey  = xpressServ+ "-" + xpressPrty;
		RemedyExpMapNode rNode = (RemedyExpMapNode)hRemedyValues.get(strHashKey);
		if (rNode != null)
		{
			return rNode;
		}
		else
		{
			Log.write(Log.DEBUG, "Error, no remedy values matche these express values: " + xpressServ + "-" + xpressPrty );
			return null;
		}
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static RemedyValues getInstance()
	{
		if (m_instance == null)
			m_instance = new RemedyValues();
		
		return m_instance;
	}
	
	/* Back door re-initialation of static values
	 */
	public static synchronized void reload() {
		m_instance = null;
		m_instance = new RemedyValues();
	}
}
