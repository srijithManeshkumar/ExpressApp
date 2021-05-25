	/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/** 
 * MODULE:	BDPSLAReportByUserBean.java
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.batch.UserReportInfo;

public class BDPSLAReportByUserBean extends BDPReportBean
{
	
	public BDPSLAReportByUserBean()
	{	
		super();
	}    	

	public String runReport( Connection conn ) throws Exception
	{
		
		StringBuffer strBuff = new StringBuffer();	
		String strStates = "States:&nbsp;&nbsp;";
		String whereClause = "";
		String strQry1 = "";
		String orderBy = "";		
		String strBZName ="";
		String strSTATE_CD="";
		String strSRVC_TYP_CD="";
		String strACTVTY_TYP_CD="";
		String strUserid = "";
		Vector m_vSortedUsers = new Vector( 5);   //use this to retreive hash in same ascending order every time
		
	 	strQry1 = " SELECT count( DWO_SQNC_NMBR) num, STT_CD, "
				+ " PRDCT_TYP_CD,  ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD,"
				+ " SUB_ACTVTY_TYP_CD, DWO_STTS_CD_IN,MDFD_USERID "
				+ " From BDP_REPORT_V WHERE MDFD_USERID = ? ";
		orderBy =" order by STT_CD, PRDCT_TYP_CD, ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD, SUB_ACTVTY_TYP_CD, DWO_STTS_CD_IN ";
				
		boolean isVpnMetroE = super.isVPNorMetroE();
		int nBindCounter = 0;		
		
		if( super.m_strProducts != null  && !ExpressUtil.isElementOf( super.m_strProducts,  ALL_ITEMS ) ) {
			// bind products
			if ( super.m_strProducts[0].length() > 0 )
			{
				whereClause += " AND  PRDCT_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < super.m_strProducts.length; i++ ){
					if ( super.m_strProducts[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		
		// bind types
		if( super.m_strOrderType != null && !ExpressUtil.isElementOf( super.m_strOrderType, ALL_ITEMS ) ){
			if ( super.m_strOrderType[0].length() > 0 )
			{
				whereClause += " AND  ORIG_SRVC_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < super.m_strOrderType.length; i++ )
				{
					if ( super.m_strOrderType[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind states 
		if( super.m_strSTATE_CDs != null && !ExpressUtil.isElementOf(super.m_strSTATE_CDs, "__" ) )
		{
			if ( super.m_strSTATE_CDs[0].length() > 0 )
			{
				whereClause += " AND  STT_CD IN ( ?";
				nBindCounter++;
				strStates += super.m_strSTATE_CDs[0]+"&nbsp;&nbsp;";
				for ( int i = 1; i < super.m_strSTATE_CDs.length; i++ )
				{
					if ( super.m_strSTATE_CDs[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
						strStates += super.m_strSTATE_CDs[i]+"&nbsp;&nbsp;";
					}
				}
				whereClause += " )";
			}
		}
			
	// bind Change types
		if( isVpnMetroE && super.m_strChangeType != null && !ExpressUtil.isElementOf( super.m_strChangeType,  ALL_ITEMS ) ) 
		{
			if ( super.m_strChangeType[0].length() > 0 )
			{
				whereClause += " AND  ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < super.m_strChangeType.length; i++ )
				{
					if ( super.m_strChangeType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind Change sub types 
		// nReportType determines the type of fulfillment report.
		// report 1a is 0 and report > 0 is 1b
		if( isVpnMetroE  && super.m_strChangeType != null && !ExpressUtil.isElementOf( super.m_strChangeType, ALL_ITEMS ) ) 
		{
			if ( super.m_strChangeType[0].length() > 0 )
			
			{
				whereClause += " AND  SUB_ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < super.m_strChangeType.length; i++ )
				{
					if ( super.m_strChangeType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}			
		}
	whereClause += " AND exists (SELECT  dh2.DWO_SQNC_NMBR FROM DWO_HISTORY_T dh2 "
			+ " WHERE BDP_REPORT_V.DWO_SQNC_NMBR = dh2.DWO_SQNC_NMBR "
			+ " AND  dh2.DWO_STTS_CD_IN  = ?  "
			+ " AND dh2.DWO_HSTRY_DT_IN BETWEEN " 
			+ " TO_DATE( ?, ? ) AND  TO_DATE(?, ?) ) " 
			+ " GROUP by STT_CD, PRDCT_TYP_CD,  ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD,"
 			+ " SUB_ACTVTY_TYP_CD, DWO_STTS_CD_IN,MDFD_USERID, "
			+ " DWO_STTS_CD_IN " + orderBy;
	 // include selected users.	
	 PreparedStatement stmt2 = null;
	 ResultSet rs2 = null;	
	 ResultSet rs = null;
		if ( super.m_strUserids != null)	
		{	for (int i=0;i< super.m_strUserids.length;i++)
			{	if (super.m_strUserids[i].equals("ALL"))
				{	strUserid="ALL";
					break;
				}
				else
				{	if(strUserid.length()>0)  strUserid += ",";
					strUserid += " ?";
				}
			}
		}
		else if ( (strUserid == null) || (strUserid.length()<1) )
		{
			strUserid="ALL";
		}

		String strCount = "";
		String strQuery1 = "";
		boolean bSpecificUserids = false;
		int iHashSize=0;
		if (strUserid.equals("ALL"))
		{
			
			strQuery1 = "SELECT distinct U.USERID, U.LST_NM, U.FRST_NM "
 				+ " FROM USERID_T U, DWO_HISTORY_T dh "
				+ " WHERE U.USERID = dh.MDFD_USERID ORDER BY U.LST_NM ";
		}
		else
		{
			bSpecificUserids = true;
			strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM FROM USERID_T U" +
				" WHERE  U.USERID IN (" + strUserid + ")  ORDER BY U.LST_NM " ;
		}
		PreparedStatement pstmt1 = null;
		ResultSet rset1 = null;
		try {
			pstmt1 = conn.prepareStatement( strQuery1 );			
			if(bSpecificUserids){
				pstmt1.clearParameters();
				 for( int j=0; j < super.m_strUserids.length; j++ )
				 {
				 	pstmt1.setString(j+1, super.m_strUserids[j] );
				 }
			}
			rset1 = pstmt1.executeQuery();			
			String strTemp="";
			while (rset1.next())
			{
				strTemp = rset1.getString(1);
				UserReportInfo objName = new UserReportInfo( strTemp, rset1.getString(2), rset1.getString(3) );
				m_vSortedUsers.addElement(objName);	//use data struct for name
								
			}
			
		}
		catch(Exception e)
		{
			throw new Exception("Error extracting users:\n" + e.toString() );
		}finally {
			try {
				if( rset1 != null ){ rset1.close(); rset1=null; }
				if( pstmt1 != null ){ pstmt1.close(); pstmt1=null; }
			} catch (Exception eee) { eee.printStackTrace();}
		}

			
		PreparedStatement pstmt = null;		
		ResultSet rset = null;				
		Hashtable hHistory = new Hashtable( 3);
		DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
		int nCurrentSeq = -9999;
		int nPrevSeq = -9999;
		StaticBDPReportData data = StaticBDPReportData.getInstance();
		hProducts = data.getProducts() ;
		hOrderTypes  = StaticBDPReportData.getOrderTypes();
		hChangeTypes = StaticBDPReportData.getChangeTypes();
		hSubChangeTypes = StaticBDPReportData.getSubChangeTypes();			
		String strST  = ""; 
		int nSnqNun  = 0; 
		String strProdType  = ""; 
		String strOrdType = "";
		String strChgType = ""; 
		String strChgSubType  = "";
		String strStatus = "";
		System.err.println( strQry1 + whereClause);			
		String strKey = "";
		String strKeyPrev = "";
		// 4. Start iterating over users' array and running main query for each users.
		Iterator ittr = 	m_vSortedUsers.iterator();
		m_strStartDate = m_strStartDate + " 00:00:00";
		m_strEndDate = m_strEndDate + " 23:59:59";
		//System.out.println( "\n\n \t\t\t" +  m_vSortedUsers.size() + "\n" );
		strBuff.append("<br><center><SPAN CLASS=\"header1\"> B&nbsp;D&nbsp;P &nbsp;&nbsp;S&nbsp;A&nbsp;L&nbsp;E&nbsp;S &nbsp;&nbsp; A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp; R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>" +
					"<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr +
					"&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +
					"</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) + 
					"<br>" + (ExpressUtil.isElementOf(m_strSTATE_CDs, "__" )? " States: All": strStates )+ "<br>" + StaticBDPReportData.getProductNames(m_strProducts, hProducts ) + "<br>" +
					 StaticBDPReportData.getOrderTypeNames(m_strOrderType, hOrderTypes) ) ;		
		while( ittr.hasNext()  ){
			strST  = ""; 
			nSnqNun  = 0; 
			strProdType  = ""; 
			strOrdType = "";			
			strChgType = ""; 
			strChgSubType  = "";
			strStatus = "";
			strKeyPrev = "";
			//NOTE the <P CLASS=page> is to put page breaks in if this thing is printed....
			UserReportInfo objURI =  (UserReportInfo)ittr.next();
			strBuff.append("<P CLASS=page><br><table border=1 align=center cellspacing=0 cellpadding=1><tr><th align=left colspan=8  bgcolor=\"#efefef\">" + objURI.getFirstName() + "&nbsp;" + objURI.getLastName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;("+ objURI.getUserid() + ")<tr>");
			//System.out.println("\nUser being processed="+ objURI.getUserid());
			strBuff.append("<tr><th align=center>&nbsp;State&nbsp;</th>" );
			strBuff.append("<th align=center>Product Type</th>" );
			strBuff.append(" <th align=center>&nbsp;Order Type&nbsp;</th>");
			strBuff.append(" <th align=center>&nbsp;Change Type&nbsp;</th> " );
			strBuff.append("<th align=center>&nbsp;Change Subtype&nbsp;</th>");
			strBuff.append(" <th align=center>&nbsp;Orders Entered<br>\"Initial\" Status&nbsp;</th>" );
			strBuff.append(" <th align=center>&nbsp;Orders \"Submitted\"&nbsp;</th>");
			strBuff.append(" <th align=center>&nbsp;Total Orders to <br>\"Bill Complete\"</th></tr>");
			strBuff.append("<style>\n P.page{page-break-after: always }\n</style>\n");
			
			pstmt = conn.prepareStatement( strQry1 + whereClause );
			pstmt.clearParameters();
			pstmt.setString( 1,objURI.getUserid() );
			nBindCounter = 2;
			if (super.m_strProducts != null && !ExpressUtil.isElementOf(super.m_strProducts, ALL_ITEMS ) )
			{
				for ( int i = 0; i < super.m_strProducts.length; i++ ){
					if ( super.m_strProducts[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, super.m_strProducts[i] );
					}
				}
			}
			if (super.m_strOrderType != null && !ExpressUtil.isElementOf(super.m_strOrderType, ALL_ITEMS ) )
			{
				for ( int i = 0; i < super.m_strOrderType.length; i++ ){
					if ( super.m_strOrderType[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, super.m_strOrderType[i] );
					}
				}
			}
			if ( super.m_strSTATE_CDs != null && 
				!ExpressUtil.isElementOf(super.m_strSTATE_CDs, "__" ))
			{
				for ( int i = 0; i < super.m_strSTATE_CDs.length; i++ ){
					if ( super.m_strSTATE_CDs[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, super.m_strSTATE_CDs[i] );
					}
				}
			}
			//	
			if( isVpnMetroE  && super.m_strChangeType != null  && 
				!ExpressUtil.isElementOf(super.m_strChangeType, ALL_ITEMS )) 
			{
				for ( int i = 0; i < super.m_strChangeType.length; i++ ){
					if ( super.m_strChangeType[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, super.m_strChangeType[i] );
					}
				}
			}
			if( isVpnMetroE && super.m_strChangeType != null && 
				!ExpressUtil.isElementOf(super.m_strChangeType, ALL_ITEMS ) ) 
			{
				for ( int i = 0; i < super.m_strChangeType.length; i++ ){
					if ( super.m_strChangeType[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, super.m_strChangeType[i] );
					}
				}
			}
			pstmt.setString( nBindCounter++, INITIAL );
			pstmt.setString( nBindCounter++, m_strStartDate  );
			pstmt.setString( nBindCounter++, DATE_FORMAT );	
			pstmt.setString( nBindCounter++, m_strEndDate  );
			pstmt.setString( nBindCounter++, DATE_FORMAT );					
			rs2 = pstmt.executeQuery();					
			while( rs2.next()==true )
			{						
				strKey = ExpressUtil.fixNullStr( rs2.getString( 2 ) ) 
					+ "_" + ExpressUtil.fixNullStr( rs2.getString( 3 ) )
					+ "_" + ExpressUtil.fixNullStr( rs2.getString( 4 ) )
					+ "-" + ExpressUtil.fixNullStr( rs2.getString( 5 ) )
					+ "-" + ExpressUtil.fixNullStr( rs2.getString( 6 ) );								
					
				if( !strKeyPrev.equals( strKey  )  && !strKeyPrev.equals( "" ) )
				{
					strBuff.append( printLine( strST, strProdType, strOrdType, strChgType, strChgSubType, hHistory ) );
					hHistory =  null;
					hHistory =  new Hashtable( 3);		
				}			
				strKeyPrev = strKey;
				strST = ExpressUtil.fixNullStr( rs2.getString( 2 ) );
				strProdType  = ExpressUtil.fixNullStr( rs2.getString( 3 ) ); 
				strOrdType = ExpressUtil.fixNullStr( rs2.getString( 4 ) );
				strChgType =  ExpressUtil.fixNullStr( rs2.getString( 5 ) ); 
				strChgSubType  =  ExpressUtil.fixNullStr( rs2.getString( 6 ) );		
				strStatus =  ExpressUtil.fixNullStr( rs2.getString( 7 ) )	;
				hHistory.put( strStatus, rs2.getString( 1 ) );	
			}					
			if( rs2 != null ){rs2.close(); rs2=null; }
			if( pstmt != null ){pstmt.close(); pstmt=null; }
			if( strKeyPrev.equals( strKey  )  && !strKeyPrev.equals( "" ) )
			{
				strBuff.append( printLine( strST, strProdType, strOrdType, strChgType, strChgSubType, hHistory ) );
			}		
			
			strBuff.append("</table></P>");
		} 	
				
		//5. return page including all users' reports.
		return strBuff.toString();
	}
	
	String printLine( String strST, String strProdType, String strOrdType, 
		String strChgType, String strChgSubType,  Hashtable hHistory){	
		StringBuffer strBuff = new StringBuffer( 128);
		strBuff.append( "<tr><td>&nbsp;" + strST+ "</td>" );
		strBuff.append( "<td>&nbsp;" + StaticBDPReportData.getProductsName( strProdType, hProducts ) + "</td>" );
		strBuff.append( "<td>&nbsp;" + StaticBDPReportData.getOrderTypeName(strOrdType, hOrderTypes ) + "</td>" );
		if( isVPNorMetroE() ){
			strBuff.append( "<td>&nbsp;" + StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes ) + "</td>" );
			strBuff.append( "<td>&nbsp;" + StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes ) + "</td>" );	
		}else{
			strBuff.append( "<td>&nbsp;N/A</td>" );
			strBuff.append( "<td>&nbsp;N/A</td>" );
		}
		strBuff.append( "<td>&nbsp;" + ( hHistory.containsKey( (Object)INITIAL  )? (String) hHistory.get( INITIAL) : "0") + "</td>" );
		strBuff.append( "<td>&nbsp;" + ( hHistory.containsKey( (Object)SUBMITTED ) ? (String)  hHistory.get( SUBMITTED) : "0") +"</td>" );
		strBuff.append( "<td>&nbsp;" + ( hHistory.containsKey( (Object)BILL_COMPLETE ) ? (String)  hHistory.get( BILL_COMPLETE) : "0")  + "</td>" );
		strBuff.append( "</tr>");
		return strBuff.toString();
	}
	
}

