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
 * MODULE:		HomePageBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        02-22-2002
 * 
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *
 */

/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class HomePageBean extends AlltelCtlrBean 
{
	final private String m_strTableName = "HOME_PAGE_NOTES_T";
	final private String strDateFormat = "MM-DD-YYYY";

	private String m_strNoteSqncNmbr = "";

	private String m_strNoteStrtDt = "          ";
	private String m_strNoteStrtDtSPANStart = " ";
	private String m_strNoteStrtDtSPANEnd = " ";

	private String m_strNoteTitle = " ";
	private String m_strNoteTitleSPANStart = " ";
	private String m_strNoteTitleSPANEnd = " ";

	private String m_strNoteEndDt = "          ";
	private String m_strNoteEndDtSPANStart = " ";
	private String m_strNoteEndDtSPANEnd = " ";

	private String m_strNoteMsg = " ";
	private String m_strNoteMsgSPANStart = " ";
	private String m_strNoteMsgSPANEnd = " ";

	private String m_strCmpnyTypList = " ";
	private String m_strCmpnyTypListSPANStart = " ";
	private String m_strCmpnyTypListSPANEnd = " ";
	
	private String m_strNoteTyp = "-1";
	private String m_strNoteTypSPANStart = " ";
	private String m_strNoteTypSPANEnd = " ";
	
	private String m_strNoteStates[] = null;
	private String m_strNoteStatesSPANStart = " ";
	private String m_strNoteStatesSPANEnd = " ";

	public HomePageBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getNoteSqncNmbr() { return m_strNoteSqncNmbr; }

	public String getNoteTitle() { return m_strNoteTitle; }
	public String getNoteTitleSPANStart() { return m_strNoteTitleSPANStart; }
	public String getNoteTitleSPANEnd() { return m_strNoteTitleSPANEnd; }


	public String getNoteStrtDt() { return m_strNoteStrtDt; }
	public String getNoteStrtDtSPANStart() { return m_strNoteStrtDtSPANStart; }
	public String getNoteStrtDtSPANEnd() { return m_strNoteStrtDtSPANEnd; }
	
	public String getNoteEndDt() { return m_strNoteEndDt; }
	public String getNoteEndDtSPANStart() { return m_strNoteEndDtSPANStart; }
	public String getNoteEndDtSPANEnd() { return m_strNoteEndDtSPANEnd; }

	public String getNoteMsg() { return m_strNoteMsg; }
	public String getNoteMsgSPANStart() { return m_strNoteMsgSPANStart; }
	public String getNoteMsgSPANEnd() { return m_strNoteMsgSPANEnd; }

	public String getCmpnyTypList() { return m_strCmpnyTypList; }
	public String getCmpnyTypListSPANStart() { return m_strCmpnyTypListSPANStart; }
	public String getCmpnyTypListSPANEnd() { return m_strCmpnyTypListSPANEnd; }



	public String[] getNoteStates() { return m_strNoteStates; }
	public String getNoteStatesSPANStart() { return m_strNoteStatesSPANStart; }
	public String getNoteStatesSPANEnd() { return m_strNoteStatesSPANEnd; }

	public String getNoteTyp() { return m_strNoteTyp; }
	public String getNoteTypSPANStart() { return m_strNoteTypSPANStart; }
	public String getNoteTypSPANEnd() { return m_strNoteTypSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setNoteSqncNmbr(String aNoteSqncNmbr) 
	{ 
		if (aNoteSqncNmbr != null)
			this.m_strNoteSqncNmbr = aNoteSqncNmbr.trim(); 
		else
			this.m_strNoteSqncNmbr = aNoteSqncNmbr; 
	}
	public void setNoteStrtDt(String aNoteStrtDt)
	{ 
		if (aNoteStrtDt != null)
			this.m_strNoteStrtDt = aNoteStrtDt.trim(); 
		else
			this.m_strNoteStrtDt = aNoteStrtDt; 
	}
	public void setNoteEndDt(String aNoteEndDt)
	{ 
		if (aNoteEndDt != null)
			this.m_strNoteEndDt = aNoteEndDt.trim(); 
		else
			this.m_strNoteEndDt = aNoteEndDt; 
	}

	public int getColumnSize(String strColumn)
	{
		return getColumnSize(m_strTableName, strColumn);
	}

	public void setNoteMsg(String aNoteMsg)
	{ 
		if (aNoteMsg != null)
			this.m_strNoteMsg = aNoteMsg.trim(); 
		else
			this.m_strNoteMsg = aNoteMsg; 

		int iMaxSize = getColumnSize( "NOTE_MSG" );
		if (iMaxSize > 0)
		{
			if (this.m_strNoteMsg.length() > iMaxSize)
			{
				this.m_strNoteMsg = this.m_strNoteMsg.substring(0,iMaxSize);
			}
		}

	}
	
	public void setNoteTyp(String aNoteTyp )
	{ 
		if (aNoteTyp != null)
			m_strNoteTyp = ""+( aNoteTyp.trim()).charAt(0); 
		else
			m_strNoteTyp = ""; 
	}
	
	

	public void setNoteTitle(String aNoteTitle )
	{ 
		if ( aNoteTitle != null)
			m_strNoteTitle = aNoteTitle.trim(); 
		else
			m_strNoteTitle = ""; 
	}
	
	
	public void setNoteStates(String[] aNoteStates )
	{ 
		int iCounter = 0;
		String strTemp = "";
		
		if ( aNoteStates != null )
		{
			m_strNoteStates = new String[aNoteStates.length];
			for(;  iCounter < aNoteStates.length; iCounter++ )
			{
				strTemp = aNoteStates[iCounter].trim();
				if(	strTemp.length() > 0)
				{
					m_strNoteStates[iCounter] = strTemp;
					strTemp = "";
				}
			}				
		}
	}
	

	public void setCmpnyTypList(String aCmpnyTypList)
	{ 
		this.m_strCmpnyTypList = aCmpnyTypList;
	}

	public int deleteHomePageBeanFromDB()
	{	
		Connection con = null;
		PreparedStatement pstmt = null;
		String strQuery = "DELETE HOME_PAGE_NOTES_T WHERE NOTE_SQNC_NMBR =  ? " ;
		// Get DB Connection
		try {
			// Build DELETE SQL statement
			int iNoteSqncNmbr = Integer.parseInt(m_strNoteSqncNmbr);
			con = DatabaseManager.getConnection();
			pstmt = con.prepareStatement( strQuery );
			pstmt.clearParameters();		
			pstmt.setInt( 1, iNoteSqncNmbr );
			pstmt.executeUpdate();	 // unlink first;
			pstmt.close();
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public int retrieveHomePageBeanFromDB()
	{	
		Connection con = null;
		PreparedStatement pstmt = null;		
		String strQuery = " SELECT TO_CHAR(NOTE_STRT_DT, 'MM-DD-YYYY') STRT_DT, " 
						+ " TO_CHAR(NOTE_END_DT, 'MM-DD-YYYY') END_DT, NOTE_MSG, " 
						+ " CMPNY_TYP_LIST, MDFD_DT, MDFD_USERID, " 
						+ " NOTE_TYP_CD, NOTE_TITLE FROM HOME_PAGE_NOTES_T WHERE NOTE_SQNC_NMBR = ? ";
	 
	 	String strQuery2 = " SELECT STT_CD FROM NOTES_STATES_LINK_T WHERE NOTE_SQNC_NMBR = ? ";
	 	
	 	ResultSet rs  = null;
		Vector vState = new Vector( 50 );
		// Get DB Connection
		try {
			// Build SELECT SQL statement
			int iNoteSqncNmbr = Integer.parseInt(m_strNoteSqncNmbr);
			con = DatabaseManager.getConnection();
			pstmt = con.prepareStatement(  strQuery ); 			
			pstmt.clearParameters();
			pstmt.setInt( 1, iNoteSqncNmbr );				
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				this.m_strNoteStrtDt = rs.getString("STRT_DT");
				this.m_strNoteEndDt = rs.getString("END_DT");
				this.m_strNoteMsg = rs.getString("NOTE_MSG");
				this.m_strCmpnyTypList = rs.getString("CMPNY_TYP_LIST");
				this.m_strMdfdDt = rs.getString("MDFD_DT");
				this.m_strMdfdUserid = rs.getString("MDFD_USERID");
				setNoteTyp( rs.getString( "NOTE_TYP_CD")   );
				setNoteTitle( rs.getString("NOTE_TITLE" ) );
				rs.close();
				// now load the array af states.
				pstmt = con.prepareStatement(  strQuery2 ); 			
				pstmt.clearParameters();
				pstmt.setInt( 1, iNoteSqncNmbr );				
				rs = pstmt.executeQuery();
				while( rs.next() )
				{
					vState.add( rs.getString( 1 ) );
				}
			
			}
			else
			{
				return 1;
			}
			pstmt.close();
			rs.close();
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	
			DatabaseManager.releaseConnection(con);
		}
		
		// load array of states ....
		if ( vState.size() > 0 )
		{
			int iArrIndex = 0;
			m_strNoteStates = new String[vState.size()];
			while(  iArrIndex < vState.size()  )
			 {
				m_strNoteStates[iArrIndex] = (String) vState.get(iArrIndex);
			 	iArrIndex++;	
			 }
		}		
		return 0;
	}

	public int updateHomePageBeanToDB()
	{	
		Connection con = null;
		PreparedStatement pstmt = null;
		int iBatchLinks = 0;
		String strQuery = " UPDATE HOME_PAGE_NOTES_T SET  "
				+ " NOTE_STRT_DT = TO_DATE( ?, ? ),  NOTE_END_DT = TO_DATE(?, ? ), NOTE_MSG = ?, "
				+ " MDFD_USERID = ?, CMPNY_TYP_LIST = ? , NOTE_TYP_CD = ? , NOTE_TITLE = ? " 			
				+ " WHERE NOTE_SQNC_NMBR = ? ";
		
		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iNoteSqncNmbr = Integer.parseInt(m_strNoteSqncNmbr);
			con = DatabaseManager.getConnection();
			pstmt = con.prepareStatement(  strQuery ); 
			pstmt.clearParameters();				
			pstmt.setString( 1, m_strNoteStrtDt );
			pstmt.setString( 2, strDateFormat );
			pstmt.setString( 3, m_strNoteEndDt );
			pstmt.setString( 4, strDateFormat );
			pstmt.setString( 5, Toolkit.replaceSingleQwithDoubleQ(m_strNoteMsg) );
			pstmt.setString( 6, Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) );			
			pstmt.setString( 7, m_strCmpnyTypList );
			pstmt.setString( 8, m_strNoteTyp );
			pstmt.setString( 9, Toolkit.replaceSingleQwithDoubleQ( m_strNoteTitle ) );
			pstmt.setInt( 10, iNoteSqncNmbr );			
	
			
			if( pstmt.executeUpdate( ) > 0 ) 
			{ 
				// relink states
				iBatchLinks = dbUpdateLinkStateToNote( iNoteSqncNmbr, con );
			}
			else
			{
				throw new SQLException(null,null,100);
			}
			pstmt.close();
			
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	
			DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public int saveHomePageBeanToDB()
	{	
		Connection con = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		ResultSet rs = null;
		String strQuery1  = " select HOME_PAGE_NOTES_SEQ.nextval from dual";
		
		// Build INSERT SQL statement
		String strQuery = " INSERT INTO HOME_PAGE_NOTES_T "
						+ " (NOTE_SQNC_NMBR, NOTE_STRT_DT, NOTE_END_DT, NOTE_MSG, MDFD_DT, MDFD_USERID, CMPNY_TYP_LIST, NOTE_TYP_CD, NOTE_TITLE )"
						+ "  VALUES (?,TO_DATE(?,?),TO_DATE(?,?),?,sysdate,?,?,?,? ) ";
			
		
		int iStateslinked = 0;
		int iTempNoteSqncNmbr = -1;
		// Get DB Connection
		try {
			
			con = DatabaseManager.getConnection();			
			pstmt1 = con.prepareStatement( strQuery1 );
			rs = pstmt1.executeQuery();	
			if(	rs.next() )
			{
				setNoteSqncNmbr( rs.getString(1) );
				iTempNoteSqncNmbr = rs.getInt( 1 );
			}			
			pstmt1.close();
			if( iTempNoteSqncNmbr  > 0 )
			{
				pstmt = con.prepareStatement(  strQuery ); 
				pstmt.clearParameters();
				pstmt.setInt( 1, iTempNoteSqncNmbr );
				pstmt.setString( 2, m_strNoteStrtDt );
				pstmt.setString( 3, strDateFormat );
				pstmt.setString( 4, m_strNoteEndDt );
				pstmt.setString( 5, strDateFormat );
				pstmt.setString( 6, Toolkit.replaceSingleQwithDoubleQ(m_strNoteMsg) );
				pstmt.setString( 7, Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) );			
				pstmt.setString( 8, m_strCmpnyTypList );
				pstmt.setString( 9, m_strNoteTyp ) ;		
				pstmt.setString( 10, Toolkit.replaceSingleQwithDoubleQ( m_strNoteTitle ) );			
				pstmt.executeUpdate( );
				iStateslinked = dbUpdateLinkStateToNote(iTempNoteSqncNmbr, con );
			
			}
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	
			DatabaseManager.releaseConnection(con);
		}

		return 0;
	}


	public boolean validateHomePageBean()
	{
		boolean rc = true;

		m_strNoteStrtDtSPANStart = getSPANStart();
		m_strNoteStrtDtSPANEnd = getSPANEnd();
		m_strNoteEndDtSPANStart = getSPANStart();
		m_strNoteEndDtSPANEnd = getSPANEnd();
		m_strNoteMsgSPANStart = getSPANStart();
		m_strNoteMsgSPANEnd = getSPANEnd();
		m_strCmpnyTypListSPANStart = getSPANStart();
		m_strCmpnyTypListSPANEnd = getSPANEnd();
		
		m_strNoteTypSPANStart =  getSPANStart();
		m_strNoteTypSPANEnd = getSPANEnd();
		m_strNoteStatesSPANStart =  getSPANStart();
		m_strNoteStatesSPANEnd = getSPANEnd();
		
		m_strNoteTitleSPANStart =  getSPANStart();
		m_strNoteTitleSPANEnd = getSPANEnd();
		
		// Validate Note Start Date
		if ((m_strNoteStrtDt == null) || (m_strNoteStrtDt.length() != 10))
		{
			m_strNoteStrtDtSPANStart = getErrSPANStart();
			m_strNoteStrtDtSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isValidDate(m_strNoteStrtDt))
		{
			m_strNoteStrtDtSPANStart = getErrSPANStart();
			m_strNoteStrtDtSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Note End Date
		if ((m_strNoteEndDt == null) || (m_strNoteEndDt.length() != 10))
		{
			m_strNoteEndDtSPANStart = getErrSPANStart();
			m_strNoteEndDtSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isValidDate(m_strNoteEndDt))
		{
			m_strNoteEndDtSPANStart = getErrSPANStart();
			m_strNoteEndDtSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Note Message
		if ((m_strNoteMsg == null) || (m_strNoteMsg.length() == 0))
		{
			m_strNoteMsgSPANStart = getErrSPANStart();
			m_strNoteMsgSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsChar(m_strNoteMsg, '"')) 
		{
			m_strNoteMsgSPANStart = getErrSPANStart();
			m_strNoteMsgSPANEnd = getErrSPANEnd();
			rc = false;
		}

		if (rc == false)
			m_strErrMsg = "ERROR:  Please review the data";

		return rc;
	}

	public boolean validateMdfdDt()
	{	
		Connection con = null;
		Statement stmt = null;
		String strMdfdDt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iNoteSqncNmbr = Integer.parseInt(m_strNoteSqncNmbr);
			String strQuery = "SELECT MDFD_DT FROM HOME_PAGE_NOTES_T " + 
				"WHERE NOTE_SQNC_NMBR = " + iNoteSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			if (rs.next())
			{
				strMdfdDt = rs.getString("MDFD_DT");
				rs.close();
			}
			else
			{
				throw new SQLException(null,null,100);
			}
			stmt.close();
		}
		catch(SQLException sqle)
		{
			handleSQLError(sqle.getErrorCode());
			return false;
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{	
			DatabaseManager.releaseConnection(con);
		}

		// As long as the dates are equal, all is well in the world and no one has changed the record
System.out.println("STACY: strMdfdDt: " + strMdfdDt);
System.out.println("STACY: m_strMdfdDt: " + m_strMdfdDt);
		if (strMdfdDt.equals(m_strMdfdDt))
		{
			return true;
		}
		else
		{
			m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. " +
				"Please CANCEL and retrieve the row again.";
			return false;
		}
	}
	
	/* dbUpdateLinkStateToNote: Links states to a note. 
	 * Description:
	 		First, all states to current note will be unlinked and then relink with incoming array of states. There should be 
	 		a note on the form telling the users that this is a multiple select field. Use the control key to select
	 		more than one state or to unselect a states.
	
	
	*/
	public  int dbUpdateLinkStateToNote( int nNoteNmbr, Connection con ) throws SQLException, Exception {
		
	
		PreparedStatement pstmt = null;
		String strQry1 =	null;	
		strQry1 = " DELETE from NOTES_STATES_LINK_T WHERE NOTE_SQNC_NMBR = ? "; 
		pstmt = con.prepareStatement( strQry1 );
		pstmt.clearParameters();		
		pstmt.setInt( 1, nNoteNmbr );
		pstmt.executeUpdate();	 // unlink first;
		int iCount = 0;
		if( m_strNoteStates  != null )
		{
			
			strQry1 = "INSERT INTO NOTES_STATES_LINK_T(NOTE_SQNC_NMBR, STT_CD ) VALUES( ?,? )";
			pstmt = con.prepareStatement( strQry1 );
			con.setAutoCommit(false);
			while( iCount < m_strNoteStates.length )
			{	
				pstmt.clearParameters();		
				pstmt.setInt( 1, nNoteNmbr );
				pstmt.setString( 2, m_strNoteStates[ iCount ].trim());
				pstmt.addBatch();
				iCount++;
				
			}	
		}
		
		// use this array ints returned by executeBatch for debugging.
		// Note: the array contains number updates or inserts
		// per each statement on the batch.
		int[]  iBatchCount = pstmt.executeBatch(  );  
		con.commit();
		con.setAutoCommit(true);

		if ( pstmt != null )
		{ 
			pstmt.close(); 
		}
		
		return iCount;
	}
	

}// end of HomePageBean()
