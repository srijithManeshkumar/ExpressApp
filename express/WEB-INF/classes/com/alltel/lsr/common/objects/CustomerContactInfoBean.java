	/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:		CustomerSearBean.java
 * 
 * DESCRIPTION: CustomerContactInfoBean
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-08-2005
 * 
 * HISTORY:
 *	6/21/2005 Edris Kalibala created
 *
package com.alltel.lsr.common.objects;
*/
package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import com.alltel.lsr.common.util.*;
public class CustomerContactInfoBean extends ExpressSISBean
{	
	
	private Vector vContacts;	
	private String strInstitution;
	private String strDesiredInstallDate;
	private String strSiteName;
	private String strStreetAddress;
	private String strSubLocation;
	private String strCity;
	private String strState;
	private String strZip;
	private String strSitePhoneNumber;	
	private String strRemarks;
	private int iSeqNumber;
	private String strSISId;	
	private String strUserId;
	// add billing information
	private String strBillTo;
	private String strBillAttn;
	private String strBillAddress1;
	private String strBillAddress2;
	private String strBillCity;
	private String strBillState;
	private String strBillZip;
	private String strBillPhone;
	// contructor	
	public CustomerContactInfoBean( String xml )
	{
		 super( xml );
		 clean();
	}
	
	// clean and initialize member variables
	public void clean(){
		strInstitution = "";
		strDesiredInstallDate = "";
		strSiteName = "" ;
		strStreetAddress = "";
		strSubLocation= "";
		strCity= "";
		strState = "";
		strZip = "";
		strSitePhoneNumber = "";
		strRemarks = "";
		iSeqNumber = -1;
		vContacts  = new Vector( 20 );
		strSISId = "";
		strUserId = "";
		strBillTo= "";
		strBillAttn= "";
		strBillAddress1= "";
		strBillAddress2= "";
		strBillCity= "";
		strBillState= "";
		strBillZip= "";
		strBillPhone = "";
		
		
	}
		
	public int getSeqNumber()
	{
	return iSeqNumber;
	}	
	
	public void setSeqNumber( int In )
	{
	 iSeqNumber = In;
	}	
	 
	public  void setSeqNumber( String In )
	 {
	 	 try{
	 	 	iSeqNumber = Integer.parseInt( In );
	 	 
	 	 }catch (Exception ex){
				ex.printStackTrace();
				Log.write(Log.DEBUG_VERBOSE, "CustomerContactInfoBean.setSeqNumber():Exception  " +ex.toString() );
		} 	 
	 }	
	
	
	public void setSISId( String In )
	{
		strSISId = In;	
	}
	
	public String getSISId(){
		return	strSISId;		
	}	
	
	public void setUserId( String In )
	{
		strUserId = In;	
	}
	
	public String getUserId(){
		return	strUserId;		
	}
	
	/* Add contact to vector vector
	 * @see ContactNode class below
	 */	
	public void addContact( String name, String email, 
		String wphone, String mphone, String pager, String ext, int iType )
	{			
			
			ContactNode tempContact = new ContactNode( name, email, wphone, mphone, pager,ext  );
			tempContact.setContactType( iType);
			vContacts.add( tempContact );
					
	}		

	public void setInstitution( String In )
	{
		strInstitution = In;	
	}
	
	public String getInstitution(){
		return	ExpressUtil.escapeHTML( strInstitution );		
	}
	
	public void setDesiredInstallDate( String In )
	{
		
		strDesiredInstallDate = In;	
	}
	
	public String getDesiredInstallDate (){
		return	ExpressUtil.escapeHTML( strDesiredInstallDate );		
	}
	
	public void setStreetAddress( String In )
	{
		strStreetAddress = In;	
	}
	
	public String getStreetAddress (){
		return	ExpressUtil.escapeHTML(strStreetAddress);		
	}
	
	public void setSiteName( String In )
	{
		strSiteName = In;	
	}
	
	public String getSiteName(){
		return	ExpressUtil.escapeHTML(strSiteName);		
	}		
		
	public void setSubLocation( String In )
	{
		strSubLocation = In;	
	}
	
	public String getSubLocation(){
		return	ExpressUtil.escapeHTML(strSubLocation);		
	}
	
	public void setCity( String In )
	{
		strCity = In;	
	}
	
	public String getCity(){
		return	ExpressUtil.escapeHTML( strCity);		
	}
	
	public void setState( String In )
	{
		strState = In;	
	}
	
	public String getState(){
		return	ExpressUtil.escapeHTML( strState );		
	}
	
	public void setZip( String In )
	{
		strZip = In;	
	}
	
	public String getZip(){
		return	ExpressUtil.escapeHTML( strZip );		
	}
	
	public void setSitePhoneNumber( String In )
	{
		strSitePhoneNumber = In;	
	}	
	
	public String getSitePhoneNumber(){
		return	ExpressUtil.escapeHTML( strSitePhoneNumber );		
	}
	
	public void setRemarks( String In )
	{
		strRemarks = In;	
	}	
	
	public String getRemarks(){
		return	ExpressUtil.escapeHTML( strRemarks );		
	}
	
	public void setBillTo( String In )
	{
		strBillTo = In;	
	}	
	public String getBillTo() { 
		return ExpressUtil.escapeHTML( strBillTo );
	}
	
	public void setBillAttn( String In )
	{
		strBillAttn = In;	
	}	
	
	public String getBillAttn() { 
		return ExpressUtil.escapeHTML( strBillAttn ); 
	}
	
	public void setBillAddress1( String In )
	{
		strBillAddress1 = In;	
	}
	
	public String getBillAddress1() { 
		return ExpressUtil.escapeHTML( strBillAddress1 ); 
	}
		
	public void setBillAddress2( String In )
	{
		strBillAddress2 = In;	
	}	
	public String getBillAddress2() { 
		return ExpressUtil.escapeHTML( strBillAddress2 ); 
	}
	
	
	public void setBillCity( String In)
	{
		strBillCity = In;
	}
	
	public String getBillCity(){ 
		return ExpressUtil.escapeHTML( strBillCity ); 
	}
	
	public void setBillState( String In )
	{
		strBillState = In;
	}	
	
	public String getBillState() { 
		return ExpressUtil.escapeHTML( strState ); 
	}
		
	public void setBillZip( String In )
	{
		strBillZip = In;
	}	

	public String getBillZip() { 
		return ExpressUtil.escapeHTML( strBillZip );
	}
	
	public void setBillPhone( String In )
	{
		strBillPhone = In;
	}	

	public String getBillPhone() { 
		return ExpressUtil.escapeHTML( strBillPhone);
	}
	
	public String createXml(){
		StringBuffer sb = new StringBuffer(512);		
		sb.append( "<SiteInformation>\n" );
		sb.append( "<INSTITUTION> " + strInstitution +"</INSTITUTION>\n" );
		sb.append( "<DESIREDINSTALLDATE>"+ strDesiredInstallDate + "</DESIREDINSTALLDATE>\n") ;
		sb.append( "<SITENAME>" + strSiteName + "</SITENAME>\n" );
		sb.append( "<STREETADDRESS>" + strStreetAddress + "</STREETADDRESS>\n" );
		sb.append( "<SUBLOCATION>" + strSubLocation + "</SUBLOCATION>\n" );
		sb.append( "<CITY>" + strCity + "</CITY>\n" );
		sb.append( "<STATE>" + strState + "</STATE>\n" );
		sb.append( "<ZIPCODE>" + strZip + "</ZIPCODE>\n" );
		sb.append( "<SITEPHONENUMBER>" + strSitePhoneNumber + "</SITEPHONENUMBER>\n" );
		sb.append( "</SiteInformation><!--Section -->\n" );		
		int iCounter = 0;
		ContactNode tempNode = null;
		while( vContacts.size() > iCounter ){
			tempNode = (ContactNode)vContacts.get( iCounter );
			sb.append( tempNode.createXml() );
			iCounter++;
		}
		sb.append( "<BillingInformation>\n" );
		sb.append( "<BILLTO> " + strInstitution +"</BILLTO>\n" );
		sb.append( "<ATTN>"+ strBillAttn + "</ATTN>\n") ;
		sb.append( "<STREETADDRESS1>" + strBillAddress1+ "</STREETADDRESS1>\n" );
		sb.append( "<STREETADDRESS2>" + strBillAddress2 + "</STREETADDRESS2>\n" );
		sb.append( "<CITY>" + strBillCity + "</CITY>\n" );
		sb.append( "<STATE>" + strBillState + "</STATE>\n" );
		sb.append( "<ZIPCODE>" + strBillZip + "</ZIPCODE>\n" );
		sb.append( "<PHONENUMBER>" + strBillPhone + "</SITEPHONENUMBER>\n" );
		sb.append( "</BillingInformation><!--Section -->\n" );		
		sb.append( "<remarks>\n" );
		sb.append( "<<remarks>" + strRemarks + "</remarks>" );	
		return sb.toString();
	}
	
	/*Required by super class, this function use XMLUtility (DOM) to extract xml 
	 * 	string passed in into of a vector of contacts.
	 *
	 *@see XMLUtility, org.w3c.dom and this.addContact(.... ), xsd for sis Results.
	 *@param xmlDocument, is an xml string received from SIS in super class.
	 *@return vector of customerContactInfoBeans
	 */
	 
	public  Vector extractXML( String xmlDocument )
	{
		// create a document
		Vector vCustomerBeans = new Vector(5);
		String[] sectionNames = {
			"SiteContactInformation","PrimaryTechnicalContact",
			"ReportingContacts","NotificationContacts","AfterHoursContacts" };
		String strCrrntNodeName = "";
		
		CustomerContactInfoBean ccIBeansTemp = null;
		XMLUtility xmlUtility = new XMLUtility();
		
		// FYI: The xml schema definition xsd file is located at 
		// G:\everyone\aciweb\express\express3.0\sisxml\SISMatches.txt		
		try{
		
			Document xmlDoc = xmlUtility.inputStreamToXML(new java.io.ByteArrayInputStream(xmlDocument.getBytes()));
			Node root = xmlDoc.getFirstChild();
			NodeList nodeList = null;
			int childCount = 0;
			// extract individual customers
			Node currNode = root.getFirstChild();
			while ( currNode.getNodeType() == Node.ELEMENT_NODE) {
	        	ccIBeansTemp = new CustomerContactInfoBean( "" );
	        	ccIBeansTemp.setInstitution( xmlUtility.getNodeValue( currNode, 
	        		SITE_INFORMATION + "." + INSTITUTION ) );
	        	ccIBeansTemp.setDesiredInstallDate( xmlUtility.getNodeValue( currNode, 
	        		SITE_INFORMATION + "." + DESIREDINSTALLDATE ) );
	        	ccIBeansTemp.setSiteName( xmlUtility.getNodeValue( currNode, 
	        		SITE_INFORMATION + "." + SITENAME ) );
	        	ccIBeansTemp.setStreetAddress( xmlUtility.getNodeValue( currNode, 
	        		SITE_INFORMATION + "." + STREETADDRESS ) );
	        	ccIBeansTemp.setSubLocation( xmlUtility.getNodeValue( currNode, 
	        		SITE_INFORMATION + "." + SUBLOCATION ) );
	      		ccIBeansTemp.setCity( xmlUtility.getNodeValue( currNode, 
	      			SITE_INFORMATION + "." + CITY ) );
	      		ccIBeansTemp.setState( xmlUtility.getNodeValue( currNode, 
	      			SITE_INFORMATION + "." + STATE ) );
	      		ccIBeansTemp.setZip( xmlUtility.getNodeValue( currNode, 
	      			SITE_INFORMATION + "." + ZIPCODE ) );
	      		ccIBeansTemp.setSitePhoneNumber( xmlUtility.getNodeValue( currNode, 
	      			SITE_INFORMATION + "." + SITEPHONENUMBER ) );
	      		ccIBeansTemp.setRemarks(  xmlUtility.getNodeValue( currNode, 
	      			REMARKS + "." + REMARKS ) );
	      		ccIBeansTemp.setSISId( xmlUtility.getNodeValue( currNode, 
	      			INTERNAL_DESCRIPTION + "." + SISID ) );
	      		// add billing information
	      		ccIBeansTemp.setBillTo( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + BILLTO ) );
	      		ccIBeansTemp.setBillAttn( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + ATTN ) );
	      		ccIBeansTemp.setBillAddress1( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + STREETADDRESS1 ) );
	      		ccIBeansTemp.setBillAddress2( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + STREETADDRESS2 ) );
	      		ccIBeansTemp.setBillCity( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + CITY ) );
	      		ccIBeansTemp.setBillState( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + STATE ) );
	      		ccIBeansTemp.setBillZip( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + ZIPCODE ) );
	      		ccIBeansTemp.setBillPhone( xmlUtility.getNodeValue( currNode, 
	      			BILLINGINFORMATION + "." + BILLPHONENUMBER ) );      		
	      		nodeList = currNode.getChildNodes();
	      		int pos = -1;
				for(  childCount = 0; childCount < nodeList.getLength(); childCount++ )
				{
					Node node = nodeList.item( childCount );
					if ( node != null ) 
					{
						
						strCrrntNodeName = node.getNodeName();
							Log.write(Log.ERROR, strCrrntNodeName );
						if(   0 <= (pos = ExpressUtil.getElementPos( sectionNames, strCrrntNodeName ) ) )
						{      				
							
							ccIBeansTemp.addContact(
								xmlUtility.getNodeValue( node,  NAME ),
								xmlUtility.getNodeValue( node,  EMAIL ),
								xmlUtility.getNodeValue( node,   WORKPHONE ),
								xmlUtility.getNodeValue( node,  MOBILEPHONE ),
								xmlUtility.getNodeValue( node,  PAGER ), 
								xmlUtility.getNodeValue( node,  EXT ), pos );
								
						}
					}    	
				}		
				ccIBeansTemp.setUserId( getUserId() );	
				currNode = currNode.getNextSibling();
				vCustomerBeans.add( ccIBeansTemp );
				if( currNode == null )
				{
					break;
				}
			}
		}catch (IOException e){
				e.printStackTrace();
				Log.write(Log.ERROR, "CustomerContactInfoBean.recieveXml():IOException  " + e.toString() );
		}catch (SAXException esax){
				esax.printStackTrace();
				Log.write(Log.ERROR, "CustomerContactInfoBean.recieveXml():SAXException  " +esax.toString() );
		}catch (Exception e){
				e.printStackTrace();
				Log.write(Log.ERROR, "CustomerContactInfoBean.recieveXml():Exception  " +e.toString() );
		}	
		Log.write(Log.DEBUG_VERBOSE, "Successfully parse search result xml document from SIS:\t" + iSeqNumber );
		return vCustomerBeans;	
	}  

	/*  No match returned by SIS
	*/
	
	public String processSISNoMatches( String xmlDocument )	
	{
		XMLUtility xmlUtility = new XMLUtility();		
		// FYI: The xml schema definition xsd file is located at 
		// G:\everyone\aciweb\express\express3.0\sisxml\	
		String strUserMessage = "";
		String strSystemMessage = "";
		try{		
			Document xmlDoc = xmlUtility.inputStreamToXML(new java.io.ByteArrayInputStream(xmlDocument.getBytes()));
			strUserMessage = xmlUtility.getNodeValue( xmlDoc, "SIS_NoMatchResponse.systemmessage.message" );
	  	
	  	}catch (IOException e){
			e.printStackTrace();
			Log.write(Log.ERROR, "CustomerContactInfoBean.processSISNoMatches():IOException  " + e.toString() );
		}catch (SAXException esax){
			esax.printStackTrace();
			Log.write(Log.ERROR, "CustomerContactInfoBean.processSISNoMatches():SAXException  " +esax.toString() );
		}catch (Exception e){
			e.printStackTrace();
			Log.write(Log.ERROR, "CustomerContactInfoBean.processSISNoMatches():Exception  " +e.toString() );
		}			
	  	Log.write(Log.ERROR, strUserMessage );
	  	return strUserMessage == null ? "" : "<span class=sismessage>" + strUserMessage + "</span>";
	  	
	 } 	
	 
	 
	/* Save match in db
	 * @see db store procedure: DB_INSERT_SIS_RESULTES()
	 * 		stored proced return seq number
	 *@param conn, db connection expection caught be the caller.
	 *@returns new seq number for the saved match
	 */
	
	public int dbSave( Connection conn ) throws SQLException , Exception 
	{
		
	
		String strQry =  "  { call  DB_INSERT_SIS_RESULTES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )} ";	
		CallableStatement cstmt  = conn.prepareCall( strQry );
		cstmt.setString( 1, strInstitution );
		cstmt.setString( 2, strSiteName );
		cstmt.setString( 3, strDesiredInstallDate );
		cstmt.setString( 4, strStreetAddress );
		cstmt.setString( 5, strSubLocation );
		cstmt.setString( 6, strCity );
		cstmt.setString( 7, strState );
		cstmt.setString( 8, strZip);
		cstmt.setString( 9, strSitePhoneNumber);
		cstmt.setString( 10, strSISId );
		cstmt.setString( 11, strRemarks );
		cstmt.setString( 12, strUserId );
		cstmt.setString( 13, strBillTo );
		cstmt.setString( 14, strBillAttn );
		cstmt.setString( 15, strBillAddress1 );
		cstmt.setString( 16, strBillAddress2 );
		cstmt.setString( 17, strBillCity );
		cstmt.setString( 18, strBillState );
		cstmt.setString( 19, strBillZip );
		cstmt.setString( 20, strBillPhone );
		cstmt.setInt( 21, iSeqNumber );
		cstmt.registerOutParameter( 21, java.sql.Types.INTEGER );		
		cstmt.executeUpdate();
		setSeqNumber( cstmt.getInt( 21 ) );		
		cstmt.close(); cstmt=null;
		// save contacts
		dbSaveContacts( conn );
		Log.write(Log.INFO, "Successfully saved search result match with seq number:\t" + iSeqNumber );
		return getSeqNumber();	
	
	}
	
	/* When a user select one of the results return by SIS, this function is called to to 
	 * copy the values in new order field.
	 */
	public boolean dbUpdateExpress( Connection conn, int iDwoSqncNmbr) throws SQLException , Exception 
	{
		String strQryMain =  " UPDATE DWO_SITE_ADM_T  SET "
			+ " INSTITUTION = ?, "
			+ " SITE_NAME = ?, " 
			+ " INSTALL_DATE = ?, "			
			+ " SITE_ADDR1 = ?, "
			+ " SITE_ADDR2 = ?, "
			+ " SITE_CITY = ?, "
			+ " SITE_STATE = ?, " 
			+ " SITE_ZIP= ?, " 
			+ " SITE_PHONE = ?, "
			+ " REMARKS = ?, " 
			+ " BILL_TO = ?, "
			+ " BILL_TO_ATTN = ?, "
			+ " BILL_TO_ADDR1 = ?, "
			+ " BILL_TO_ADDR2 = ?, "
			+ " BILL_TO_CITY = ?, "
			+ " BILL_TO_ST = ?, "
			+ " BILL_TO_ZIP = ?, "
			+ " BILL_TO_MBTN = ? "
			+ " Where DWO_SQNC_NMBR = ?";	
		PreparedStatement pstmt  = conn.prepareStatement( strQryMain );
		pstmt = conn.prepareStatement( strQryMain );
		pstmt.clearParameters();
		pstmt.setString( 1, strInstitution );
		pstmt.setString( 2, strSiteName );
		pstmt.setString( 3, strDesiredInstallDate );
		pstmt.setString( 4, strStreetAddress );
		pstmt.setString( 5, strSubLocation );
		pstmt.setString( 6, strCity );
		pstmt.setString( 7, strState );
		pstmt.setString( 8, strZip );		
		pstmt.setString( 9, strSitePhoneNumber);
		pstmt.setString( 10, strRemarks );
		pstmt.setString( 11, strBillTo );
		pstmt.setString( 12, strBillAttn );
		pstmt.setString( 13, strBillAddress1 );
		pstmt.setString( 14, strBillAddress2 );
		pstmt.setString( 15, strBillCity );
		pstmt.setString( 16, strBillState );
		pstmt.setString( 17, strBillZip );
		pstmt.setString( 18, strBillPhone );
		pstmt.setInt( 19, iDwoSqncNmbr );				
		pstmt.executeUpdate();
		pstmt.close(); pstmt=null;
		 //SAVE site contact
		dbUpdateContacts( conn, iDwoSqncNmbr, INT_SITE_CONTACT_INFORMATION );
		// SAVE site contact
		dbUpdateContacts( conn, iDwoSqncNmbr, INT_PRIMARY_TECHNICAL_CONTACT );
		//ReportingContacts
		dbUpdateContacts(  conn, iDwoSqncNmbr, INT_REPORTING_CONTACT ); 
		//NotificationContacts
		dbUpdateContacts(  conn, iDwoSqncNmbr, INT_NOTIFICATION_CONTACT );
		// AfteHoursContacts
		dbUpdateContacts(  conn, iDwoSqncNmbr, INT_AFTER_HOURS_CONTACT );		
		Log.write(Log.INFO, "Successfully updated search values in db for order:\t" + iDwoSqncNmbr );
		return true;		
	}
	
	/* Copy data stored in ContactNodes into new order by type.
	 * @param conn, db connection
	 * @param  iDwoSqncNmbr  new order primary key value to update with.
	 * @param iType, type of contact ( 0-4 ). 
	 * @see this.dbUpdateExpress()
	 */ 
	public void dbUpdateContacts( Connection conn, int iDwoSqncNmbr, int iType ) throws SQLException , Exception 
	{
					
		PreparedStatement pstmt = null;				
		String strContactsUpdate =  null;		
		int iCount = 0;
		int iSectionCounter = 1;
		ContactNode contactNode = null;
		while( iCount < vContacts.size() )
		{
			contactNode = (ContactNode) vContacts.get(iCount);
			strContactsUpdate =  "UPDATE DWO_SITE_ADM_T  SET ";	
			if( contactNode == null )
			{ 	
				continue;
			}
			if( iType == contactNode.getContactType() & iSectionCounter < 4 )
				{
					
					switch ( iType )
					{
					
					case INT_SITE_CONTACT_INFORMATION:
					case INT_PRIMARY_TECHNICAL_CONTACT:	
					{
						strContactsUpdate += getContactDbField( iType ) + "NAME = ?, "
								+ getContactDbField( iType ) + "EMAIL = ?, "
								+ getContactDbField( iType ) + "WORK_PHONE = ?,"
								+ getContactDbField( iType ) + "MOBILE_PHONE = ?, "
								+ getContactDbField( iType ) + "PAGER = ? "
								+ " Where DWO_SQNC_NMBR = ?";
						pstmt = conn.prepareStatement( strContactsUpdate );
						pstmt.clearParameters();					
						pstmt.setString( 1,	contactNode.strName );
						pstmt.setString( 2, contactNode.strEmail );
						pstmt.setString( 3, contactNode.strWorkPhone );
						pstmt.setString( 4, contactNode.strMobilePhone  );
						pstmt.setString( 5, contactNode.strPager );
						pstmt.setInt( 6, iDwoSqncNmbr );
					//Log.write(Log.INFO, strContactsUpdate );	
						pstmt.executeUpdate();	
						break;
					}
					case INT_AFTER_HOURS_CONTACT:	{				
						strContactsUpdate += getContactDbField( iType ) 
							+ iSectionCounter + "_NAME = ?, "
							+ getContactDbField( iType ) +  iSectionCounter + "_WORK_PHONE = ?,"
							+ getContactDbField( iType ) +  iSectionCounter + "_MOBILE_PHONE = ? "
							+ " Where DWO_SQNC_NMBR = ?";
						pstmt = conn.prepareStatement( strContactsUpdate );
						pstmt.clearParameters();					
						pstmt.setString( 1,	contactNode.strName );
						pstmt.setString( 2, contactNode.strWorkPhone );
						pstmt.setString( 3, contactNode.strMobilePhone  );
						pstmt.setInt( 4, iDwoSqncNmbr );
					//Log.write(Log.INFO, strContactsUpdate );	
						pstmt.executeUpdate();	
						break;	
					}
					case INT_REPORTING_CONTACT:
					case INT_NOTIFICATION_CONTACT:
					{					
						strContactsUpdate += getContactDbField( iType ) + iSectionCounter + "_NAME = ?, "
							+ getContactDbField( iType ) + iSectionCounter + "_EMAIL = ?, "
							+ getContactDbField( iType ) + iSectionCounter + "_WORK_PHONE = ?,"
							+ getContactDbField( iType ) + iSectionCounter + "_WORK_EXT = ?,"
							+ getContactDbField( iType ) + iSectionCounter + "_MOBILE_PHONE = ?, "
							+ getContactDbField( iType ) + iSectionCounter + "_PAGER = ? "
							+ " Where DWO_SQNC_NMBR = ?";
					//Log.write(Log.INFO, strContactsUpdate );	
						pstmt = conn.prepareStatement( strContactsUpdate );
						pstmt.clearParameters();					
						pstmt.setString( 1,	contactNode.strName );
						pstmt.setString( 2, contactNode.strEmail );
						pstmt.setString( 3, contactNode.strWorkPhone );
						pstmt.setString( 4, contactNode.strWorkPhoneExt );
						pstmt.setString( 5, contactNode.strMobilePhone  );
						pstmt.setString( 6, contactNode.strPager );
						pstmt.setInt( 7, iDwoSqncNmbr );
						pstmt.executeUpdate();	
						pstmt.close(); 
						pstmt=null;							
						iSectionCounter++;
					}
						default: { // do nothing 
					}	// end look for  current conde type passed in
				} //end switch
			}
			iCount++;
		} // end while	
	}	
	
	/* Load bean from db
	 *@param Conn, db conncection
	 */
	public boolean dbLoad( Connection conn ) throws SQLException , Exception 
	{
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		boolean	bResult	= true;
		String strQry =	"SELECT INSTITUTION, INSTALL_DT, "
			+ " SITE_NAME, STREET_ADDRESS, SUB_LOCATION, CITY, STATE, ZIPCODE, " 
			+ " PHONE, REMARKS, BILL_TO, BILL_TO_ATTN, BILL_TO_ADDR1, BILL_TO_ADDR2, "
			+ " BILL_TO_CITY, BILL_TO_ST, BILL_TO_ZIP, BILL_TO_MBTN "
			+ " FROM  SIS_CUSTOMER_SEARCH_T "
			+ " WHERE SIS_SEARCH_SEQ_NMBR = ? ";
		pstmt = conn.prepareStatement(strQry);
		pstmt.clearParameters();
		pstmt.setInt( 1, iSeqNumber );
		rset = pstmt.executeQuery();
		if( rset.next() )
		{
		 	setInstitution( rset.getString( 1 ) );
			setDesiredInstallDate(  rset.getString( 2)  );
			setSiteName( rset.getString( 3 ) );
			setStreetAddress( rset.getString( 4 ) );				
			setSubLocation( rset.getString( 5 ) );
			setCity( rset.getString( 6 ) );
			setState( rset.getString( 7 ) );
			setZip( rset.getString( 8 ) );
			setSitePhoneNumber( rset.getString( 9 ) );
			setRemarks( rset.getString( 10 ) );
			setBillTo( rset.getString( 11 ));
			setBillAttn( rset.getString( 12 ));
			setBillAddress1( rset.getString( 13 ));
			setBillAddress2( rset.getString( 14 ));
			setBillCity( rset.getString( 15 ));
			setBillState( rset.getString( 16 ));
			setBillZip( rset.getString( 17 ));
			setBillPhone( rset.getString( 18 ));			
			bResult = true;
		}
		if ( !dbLoadContacts( conn ) )
		{
			bResult = false;
		}
		rset.close();
		rset = null;
		pstmt.close();
		pstmt = null;
		return bResult;			
	}		
	
	/* load contacts belonging to this bean.
	 * @param dbContacts
	 */		
	private boolean dbLoadContacts( Connection conn ) 	throws SQLException , Exception	
	{
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		boolean	bResult	= true;
		String strQry =	"SELECT NAME, EMAIL, WORKPHONE, "
			+ " MOBILEPHONE, PAGER, CONTACT_TYPE, WORKPHONE_EXT "
		 	+ " FROM CUSTOMER_CONTACT_T WHERE SIS_SEARCH_SEQ_NMBR = ? ";
		 pstmt = conn.prepareStatement(strQry);
		 pstmt.clearParameters();
		 pstmt.setInt( 1, iSeqNumber );
		 rset = pstmt.executeQuery();
		while( rset.next() )
		{
		 	addContact( rset.getString(1),
			 	rset.getString(2),
			 	rset.getString(3),
			 	rset.getString(4),
			 	rset.getString(5),			 	
			 	rset.getString(7),
			 	rset.getInt(6) );				 	
			 	
			bResult = true;
		}	
		rset.close();
		rset = null;
		pstmt.close();
		pstmt = null;	
		return bResult;	
	}	
	
	/* This function save results match (contacts) in the customer contact table
	 *@see 	dbSave
	 *
	 */
	private void dbSaveContacts( Connection conn) throws SQLException , Exception 
	{
		
		String sqlQry = " Insert into CUSTOMER_CONTACT_T( "
				+ " SIS_SEARCH_SEQ_NMBR, NAME, EMAIL, WORKPHONE, "
				+ " MOBILEPHONE, PAGER, CONTACT_TYPE, WORKPHONE_EXT ) "
				+ " Values ( ?,?,?,?,?,?,?,? ) ";
		
		PreparedStatement pstmt = conn.prepareStatement( sqlQry );
		int iCount = 0;
		ContactNode contactNode = null;
		while( iCount < vContacts.size() )
		{	
			contactNode = (ContactNode) vContacts.get(iCount);
			if( contactNode != null )
			{ 								
				pstmt.clearParameters();		
				pstmt.setInt( 1, iSeqNumber );
				pstmt.setString( 2,	contactNode.strName );
				pstmt.setString( 3, contactNode.strEmail );
				pstmt.setString( 4, contactNode.strWorkPhone );
				pstmt.setString( 5, contactNode.strMobilePhone  );
				pstmt.setString( 6, contactNode.strPager );
				pstmt.setInt( 7, contactNode.icontactType );
				pstmt.setString( 8, contactNode.strWorkPhoneExt );
				pstmt.addBatch();
				
			}
			iCount++;				
		}
		if( iCount > 0 ){
			//Execute batch
			int[]  iBatchCount = pstmt.executeBatch(  );  			
		}
		if ( pstmt != null )
		{ 
				pstmt.close(); 
		}
		//Log.write(Log.ERROR, "CustomerContactInfoBean:dbSaveContacts\t  " + iBatchCount.length );
		
			
	}
	
	
	// ............Utility Function ***********/
	
	public static Hashtable getDbFieldContactsNames(){
		Hashtable hContacts = new Hashtable( 10 );
		hContacts.put( "0", "SITE_CNTCT_" );
		hContacts.put( "1", "PRI_TECH_" ) ;
		hContacts.put( "2", "RPT_CNTCT_" ) ;
		hContacts.put( "3", "NOT_CNTCT_" ) ;
		hContacts.put( "4", "AHRS_CNTCT_" ) ;
		return hContacts;
	}
		
	public static String getContactDbField( int iType )
	{
		return (String)getDbFieldContactsNames().get( "" + iType );	
	}
	
	
	
	/* Print a single line on the html page started in the servlet. ( controller).
	 *
	 */
	public String displayHtml(){
		String strCreateLink = "<A HREF=\"DwoCtlr?dwocreate=view&seqnum=" + iSeqNumber
						+ "\" onclick=\"return confirm('Are you sure you want to create a new order with this customer information?');\">Create Order</a>";
		
		StringBuffer sbHtml = new StringBuffer( 128 );	
		sbHtml.append("<tr>" );
		sbHtml.append("<td>&nbsp;"+ getSiteName() + "</td>" );
		sbHtml.append("<td nowrap>&nbsp;"+ getSitePhoneNumber() + "</td>" );
		sbHtml.append("<td>&nbsp;"+ getStreetAddress() + " &nbsp&nbsp&nbsp" );
		sbHtml.append( getCity() + ",&nbsp" );
		sbHtml.append( getState() + "&nbsp" +  getZip() );
		sbHtml.append("</td><td>&nbsp;"+ strCreateLink + "</td></tr>\n" );				
		return sbHtml.toString();	
	}
	
} 

/* Contact node
 */
 
class ContactNode	{
	protected String strName;
	protected String strEmail;
	protected String strWorkPhone;
	protected String strWorkPhoneExt;
	protected String strMobilePhone;
	protected String strPager;
	protected int icontactType;
	
	public ContactNode(String name, String email, String wphone, String
	mphone, String pager, String strWphoneExt ) {
		
		strName = name == null ? "": name;
		strEmail =  email == null ? "": email;
		strWorkPhone = wphone == null ? "": wphone;
		strMobilePhone = mphone == null ? "": mphone;
		strPager =  pager == null ? "": pager; 
		strWorkPhoneExt = strWphoneExt == null ? "": strWphoneExt;
	
	}	
	public void setContactType(int In ) 
	{
		icontactType = In;
	}		
	public int getContactType()
	{
		return icontactType;	
	}		
	public void clean(){
		strName = "";
		strEmail = "";
		strWorkPhone = "";
		strMobilePhone = "";
		strPager = "";		
	}	
	public String createXml( ){		
		StringBuffer sb = new StringBuffer( 512);
		String strSectionName = "";
		String[] sectionNames = {
			"SiteContactInformation","PrimaryTechnicalContact",
			"ReportingContacts","NotificationContacts","AfterHoursContacts" };
		strSectionName = sectionNames[getContactType()];
		sb.append( "\t<" + strSectionName + ">\n" );
		sb.append( "\t\t<NAME>" + strName+ "</NAME>\n" );
		sb.append( "\t\t<EMAIL>" + strEmail + "</EMAIL>\n" );
		sb.append( "\t\t<WORKPHONE>" + strWorkPhone + "</WORKPHONE>\n" );
		sb.append( "\t\t<EXT>" + strWorkPhoneExt + "</EXT>\n" );
		sb.append( "\t\t<MOBILEPHONE>" + strMobilePhone + "</MOBILEPHONE>\n" );
		sb.append( "\t\t<PAGER>" + strPager + "</PAGER>\n");
		sb.append( "\t</" + strSectionName + ">\n" );		
		return sb.toString();
	}
}
