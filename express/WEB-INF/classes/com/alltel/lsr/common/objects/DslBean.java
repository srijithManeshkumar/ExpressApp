/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:		DslBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-05-2002
 *
 * HISTORY:
 *	10/15/2002 psedlak	history update chgd (hdr 165254)
 *
 *      11/21/2002 shussaini Change Request Navigation.(hd 200039)
 *                  Added m_strSttsCdFrom,  m_strTypInd, m_strRqstTypCd
 *                  and m_strSttsCdTo with its get Methods.
 *                  Change the sql to retrieve following columns
 *                  STTS_CD_FROM,TYP_IND, RQST_TYP_CD, ACTN from Action_T
 *	07/02/2003 psedlak	allow multiple service types
 *	09/19/2003 psedlak	use generic base
 *	05/27/2004 psedlak Added other DSL user types. The user type is driven by Security
 *		object. The DSL order will have a ServiceTypeCode that is dtermined by the
 *		DSL user type. The getSrvcTypCd() method gets the Service Type and set it.
 *	10/09/2004 #1641 DSL Qualification service changes
 *	10/21/2004 Cleanup items for #1641
 *	11/03/2004 pjs Trunc userid to 12 chars in DslQual XML
 *	11/19/2004 pjs populate DSL qual results field (had to overload changeStatus()
 *
 * 2/23/2005 EK: Permanently keep any dsl lookup in db, Decode dslQual results on first encounter and
 *		keep them in db for next use and possible swapping with express preferred messages.
 		Add functions
 *
 * 3/9/2005 EK: finish-up -> (pjs populate DSL qual results field (had to overload changeStatus() )
 *
 * 01/10/2008 Steve Korchnak
 * Idea5052   Modified broadband qualification (dsl loop qual) to utilize new
 *            BroadbandProductsQualification (BPQ) tool.
 *
 */

package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.util.*;
import com.windstream.uqualbpq.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import org.w3c.dom.*;

public class DslBean extends ExpressBean
{

        //These dictate what to pull from tables
    private DslOrder thisOrder = DslOrder.getInstance();
	private int iTermResist;
	private Date dActivityDts;
	private String strSrvDisposition;
	private String m_strQualificationResults;
	private String strPhone;
	private String strMessage;
	private String strResults;
	private String strMessageExpress;
	private String strResultsExpress;
	private String strDSLEnabled;

	final static public String GENERIC_RESULT_SHORT = "DEFAULT";
	final static public String GENERIC_RESULT_MESSAGE = "DEFAULT MESSAGE";
	// Added state - Nebraska(NE)
	public static final String NE_STATE = "NE";

	final static public int DEFAULT_SRVC_TYP_CD = 5;
        /* For VDSL2 and FttP Qualification in Express by - Sarath: Begin Declaration */
        final static public double DEFAULT_SPEED_IN_MB_FTTP = 25.0;
        final static public double DEFAULT_SPEED_IN_MB_VDSL2 = 12.0;
        /* For VDSL2 and FttP Qualification in Express by - Sarath: End Declaration */
        
        /* VDSL2 and FttP Qualification For Nebraska(NE) in Express : Begin Declaration */
        public static final double NE_DEFAULT_SPEED_IN_MB_FTTP = 50.0;
        public static final double NE_DEFAULT_SPEED_IN_MB_VDSL2 = 50.0;
        /* VDSL2 and FttP Qualification For Nebraska(NE) in Express : End Declaration */
        
//	final static public String INDIRECT_AGENT = "INDIRECT_DSL_AGT";
//	Others....OTHER_DSL_AGT, NATL_DSL_AGT, ISP_REF_DSL_AGT...


    public DslBean() {
    super.init(thisOrder);
	    Log.write(Log.DEBUG_VERBOSE, "DslBean: constructor");
		m_strQualificationResults="";
		clean();
    }
	private void clean(){


		iTermResist = -999;
		dActivityDts = null;
		strSrvDisposition = " ";
		m_strQualificationResults = " ";
		strPhone = " ";
		strMessage = " ";
		strResults = " ";
		strMessageExpress = " ";
		strResultsExpress = " ";
		strDSLEnabled="";
	}


	public void setResultsExpress( String In )
	{
		strResultsExpress = In;
	}

	public String getResultsExpress( )
	{
		return strResultsExpress;
	}
	public void setMessageExpress( String In )
	{
		strMessageExpress = In;
	}

	public String getMessageExpress( )
	{
		return strMessageExpress;
	}
	public void setResults( String In )
	{
		strResults = In;
	}

	public String getResults( )
	{
		return strResults;
	}

	public void setMessage( String In )
	{
		strMessage = In;
	}

	public String getMessage( )
	{
		return strMessage;
	}

	public void setPhone( String In )
	{
		strPhone = In;
	}

	public String getPhone( )
	{
		return strPhone;
	}

	public void setSrvDisposition( String In )
	{
		strSrvDisposition = In;
	}

	public String getSrvDisposition( )
	{
		return strSrvDisposition;
	}

	public void setActivityDts(Date In )
	{
		dActivityDts = In;
	}

	public  Date getActivityDts()
	{
		return dActivityDts;
	}

	public String getFmtActivityDts()
	{
		SimpleDateFormat formatter = new SimpleDateFormat( "MM-dd-yyyy");
		String dateString = formatter.format(dActivityDts);
		return dateString;
	}

	public void setTermResist( int In)
	{
		iTermResist= In;
	}

	public int getTermResist()
	{
		return iTermResist;
	}

	public String getQualResults()
	{
		return this.m_strQualificationResults;
	}
	public void setQualResults(String inResults)
	{
		this.m_strQualificationResults = inResults;
	}

	public String getSrvcTypCd()
	{
		return getSrvcTypCd( this.getUserid() );
	}
	public String getDSLEnabled()
	{
		return strDSLEnabled;
	}
	public void setDSLEnabled( String In )
	{
		strDSLEnabled = In;
		if( strDSLEnabled == null ){
			strDSLEnabled = "E";
			// note is for Expection, when we don't get response at all from webservices
		}
	}

	public String getSrvcTypCd(String strUserid)
	{
		String strSrvcTypCd = String.valueOf(DEFAULT_SRVC_TYP_CD);

		// Obtain the Service Type if there......if not, use default value
		// NOTE This assumes a user can only be one DSL user type
//		String strQuery = "SELECT S.SRVC_TYP_CD FROM SERVICE_TYPE_T S WHERE S.SRVC_TYP_DSCRPTN='" + INDIRECT_AGENT + "' " +
//			" AND S.TYP_IND='" + thisOrder.getTYP_IND() + "' AND S.SRVC_TYP_DSCRPTN IN " +
//			"   (SELECT DISTINCT SO.SCRTY_OBJCT_CD " +
//			"    FROM USERID_T U, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA, SECURITY_OBJECT_T SO  "+
//			"    WHERE U.USERID='" + strUserid + "' AND U.USERID=UGA.USERID AND UGA.SCRTY_GRP_CD=sga.SCRTY_GRP_CD "+
//			"    AND SGA.SCRTY_OBJCT_CD=SO.SCRTY_OBJCT_CD) ";
		String strQuery = "SELECT S.SRVC_TYP_CD FROM SERVICE_TYPE_T S WHERE S.TYP_IND='" +thisOrder.getTYP_IND()+ "' AND S.SRVC_TYP_DSCRPTN IN " +
			"   (SELECT DISTINCT SO.SCRTY_OBJCT_CD " +
			"    FROM USERID_T U, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA, SECURITY_OBJECT_T SO  "+
			"    WHERE U.USERID='" + strUserid + "' AND U.USERID=UGA.USERID AND UGA.SCRTY_GRP_CD=sga.SCRTY_GRP_CD "+
			"    AND SGA.SCRTY_OBJCT_CD=SO.SCRTY_OBJCT_CD) ";
		try
		{
			ResultSet rs = m_stmt.executeQuery(strQuery);
			if (rs.next())
			{	strSrvcTypCd = rs.getString("SRVC_TYP_CD");
			}
			rs.close();
			rs=null;
		}
		catch(SQLException e)
		{
			strSrvcTypCd = "";
			Log.write(Log.ERROR, "DslBean : No Srvc Type Cd for userid: " + strUserid);
		}
		if (strSrvcTypCd != null && strSrvcTypCd.length() > 0)
		{
			//change order
			thisOrder.setSRVC_TYP_CD(strSrvcTypCd);
		}

		Log.write(Log.DEBUG_VERBOSE, "DslBean.getSrvcTypCd() = "+ strSrvcTypCd);
		return strSrvcTypCd;
	}


	public int create(int x)
	{
		return -1;
	}

	public int create()
	{
		return create(  getSrvcTypCd() );
	}

	public int create(String strSrvcTypCd)
	{

		Log.write(Log.DEBUG_VERBOSE, "DslBean : Create New Dsl of type=" + strSrvcTypCd);

		int iReturnCode = 0;

		String strSttCd = "";
		int iCmpnySqncNmbr = 0;

		// Get company for this user
		String strQuery1 = "SELECT COMPANY_T.CMPNY_SQNC_NMBR FROM USERID_T, COMPANY_T WHERE USERID_T.USERID = '" + getUserid() + "' AND COMPANY_T.CMPNY_SQNC_NMBR = USERID_T.CMPNY_SQNC_NMBR";

		try
		{
			ResultSet rs1 = m_stmt.executeQuery( strQuery1 );

			if (rs1.next())
			{
				iCmpnySqncNmbr = rs1.getInt( "CMPNY_SQNC_NMBR" );
				rs1.close();
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection( m_conn );
				Log.write(Log.ERROR, "DslBean : Error finding valid Company Sqnc Nmbr");
				iReturnCode = -110;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "DslBean : DB Exception on Query : " + strQuery1);
			iReturnCode = -100;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		// Get the new Dsl Number
		int iDslSqncNmbr = 0;
		String strQueryTSN = "SELECT DSL_SEQ.nextval DSL_SQNC_NMBR_NEW FROM dual";

		try
		{
			ResultSet rsTSN = m_stmt.executeQuery(strQueryTSN);

			rsTSN.next();
			iDslSqncNmbr = rsTSN.getInt("DSL_SQNC_NMBR_NEW");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "DslBean : DB Exception on Query : " + strQueryTSN);
			iReturnCode = -100;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		// Insert new row into DSL_T
		String strInsert1 = "";
		try
		{
			strInsert1 = "INSERT INTO DSL_T VALUES(" + iDslSqncNmbr + ",0, 'INITIAL', 0, " + iCmpnySqncNmbr + ", ' ',' ',' ',' ',' ',' ','" + getUserid() + "'," + getTimeStamp() + ", '" + getUserid() + "', '" + strSrvcTypCd + "','')" ;
			m_stmt.executeUpdate(strInsert1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "DslBean : DB Exception on Insert : " + strInsert1);
			iReturnCode = -100;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "DslBean : Successful Insert of New Dsl");

		// generate a new History record
		int iDslHstrySqncNmbr = updateHistory(iDslSqncNmbr, 0, "INITIAL");
		if (iDslHstrySqncNmbr <= 0)
		{
			Log.write(Log.ERROR, "DslBean : Error Generating History for Dsl Sqnc Nmbr:" + iDslSqncNmbr);
			return(-125);
		}

		String strUpdate1 = "UPDATE DSL_T SET DSL_HSTRY_SQNC_NMBR = " + iDslHstrySqncNmbr + " WHERE DSL_SQNC_NMBR = " + iDslSqncNmbr;

		try
		{
			if (m_stmt.executeUpdate(strUpdate1) <= 0)
			{
				throw new SQLException();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "DslBean : DB Exception on Update : " + strUpdate1);
			iReturnCode = -100;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "DslBean : DSL_T updated with current History Sequence Number : " + strUpdate1);


		// if we got here, we have a new Dsl Sequence Number
		// now get the information we need to create all the required forms.
		// We need to loop through SERVICE_TYPE_FORM and create all the INITIAL FORMs

		//String strQuery3 = "SELECT * FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '" + DSL_SERVICE_TYPE + "' AND TYP_IND = 'D'";
		String strQuery3 = "SELECT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '" + strSrvcTypCd + "' AND TYP_IND = '" + thisOrder.getTYP_IND() + "'";
		int i_frms = 0;
		int i_frms_created = 0;
		int iFrmSqncNmbr = 0;
		boolean bFormCreated = false;

		try
		{
			ResultSet rs3 = m_stmt.executeQuery(strQuery3);

			while (rs3.next())
			{
				i_frms++;

				iFrmSqncNmbr = rs3.getInt("FRM_SQNC_NMBR");
Log.write(Log.DEBUG_VERBOSE, "DslBean: form = " + iFrmSqncNmbr);
				bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iDslSqncNmbr, 0);

				if (bFormCreated)
				{
					i_frms_created++;
				}
				else
				{
					Log.write(Log.ERROR, "DslBean : Error Generating Form for Dsl Sqnc Nmbr:" + iDslSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
					iReturnCode = -130;
				}

			}
			if ((i_frms_created == 0) || (i_frms_created != i_frms))
			{
				Log.write(Log.ERROR, "DslBean : Error Generating Forms for Dsl Sqnc Nmbr:" + iDslSqncNmbr);
				iReturnCode = -135;
			}

			rs3.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "DslBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW DSL FORM CREATION ");
			iReturnCode = -100;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "DslBean : All INITIAL Forms Generated for Dsl Sqnc Nmbr:" + iDslSqncNmbr);

		// return the new Dsl Sequence Number
		return(iDslSqncNmbr);

	}

        // Send the autoReply if necessary
        protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
        {
        }

        // Send the provider autoReply if necessary
        protected void sendProvReply(int iSqncNmbr, int iVrsn)
        {
        }

	// had to do to populate DSL qual results on submission
	/* EK: ChangeStatus overloads base ChangeStatus,
			so super.changeStatus(... ) is called first
			to perform general status change.
	 * FOR DETAILS ABOUT CHANGE STATUS
	   @see Super.changeStatus - ExpressBean.changeStatus (... )
	 * This function handles dsl_phone lookup  when statuses
	 		are changing to submitted.
	 * It performes the same series of events as dlsLookup.jsp page
	 		only here it displays only the results from either the database
	   or webserviceDSLQualification if number never been looked up before.
	 *
	 */
	public int changeStatus(AlltelRequest request,
					int iDslSqncNmbr, String strDslActn)
	{

		int iReturnCode = 0;
		int iHistorySequenceNumber = 0;

		// Call base class method first....
		iReturnCode = super.changeStatus(request, iDslSqncNmbr, strDslActn);

		if (iReturnCode < 0)
		{
			return iReturnCode;
		}
		iHistorySequenceNumber = iReturnCode;
		iReturnCode = 0;
		if ( getSttsCdTo().equals("SUBMITTED") )
		{
			String strQry1 = "SELECT ltrim( rtrim(DSL_RQST_DSL_SRVC_TELNO)) DSLPHONE from DSL_RQST_T where DSL_SQNC_NMBR = ? ";
			PreparedStatement pstmt1 = null;
			ResultSet rset = null;
			try{
				pstmt1 = m_conn.prepareStatement( strQry1 );
				pstmt1.clearParameters();
				pstmt1.setInt( 1, iDslSqncNmbr);
				rset = pstmt1.executeQuery();
				if ( rset.next() )
				{
					this.setPhone( rset.getString( 1 ) );
				}
			}
			 catch(SQLException e)
			{
			e.printStackTrace();
			Log.write(Log.ERROR, "DslBean.ChangeStatus() DSL Qual look up submit: "
				+ " ******ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP**** "
			 	+ e.toString() );
			} finally {	// Clean up
				try{
					if ( rset != null ){ rset.close(); }
					if ( pstmt1 != null ){ pstmt1.close(); }
				}catch (Exception e){
					e.printStackTrace();
					Log.write(Log.ERROR,
						"DslBean.ChangeStatus :  "
					+ " *****ERROR  IN FINALLY DATABASE  DSL LOOKUP ****** "
					+ e.toString() );
				}
			}
			int iVrsn = 0;
			String strXML = "";
			String strOriginalDslNum = strPhone;
			strPhone = ExpressUtil.findReplace( strPhone, "-", "" );
			// try to load the results from db
			dbloadResultByPhone( );
			Log.write(Log.DEBUG_VERBOSE, "DslBean.changeStatus(): "
				+ strPhone  + "Results are \t" +  this.strResults );

			if( this.strResults.equals( "0" )  ){
				 // try looking it up.
				//Here we need to build XML and push to ...
				// Web Service to get Qual results
				strXML = buildQualRequestXml( strPhone );
				// restore original phone format in case one needs to use it.
				setPhone( strOriginalDslNum );
				if ( !strXML.equals( "" ) )
				{
					try {
						if (this.webserviceDSLQualification(strXML) == 0)
						{
						// strResults is member variable already set...
						// but we will set again.
							this.setResults( dslQualInterpretResponse(
								this.getQualResults(), true ) );

						}
						else
						{
							this.setResults(  "unknown" );
						}
					}
					catch(Exception e)
	                {
						e.printStackTrace();
						Log.write(Log.DEBUG_VERBOSE,
							"DslBean.changeStatus(): DSL Qual error");
					}
						// store this lookup in the history table.
						dbInsert();

				}
				else
				{
					Log.write(Log.DEBUG_VERBOSE,
						"DslBean.changeStatus(): XML create error");
				}
			}
			String strUpdate1 = "";
			if (this.strResults.length() > 0)
			{
				strUpdate1 = "UPDATE DSL_RQST_T SET DSL_QUAL_RSLTS = ? "
					+ "  WHERE DSL_SQNC_NMBR = ? " ;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = m_conn.prepareStatement( strUpdate1 );
					pstmt.clearParameters();
					pstmt.setString( 1, this.strResults );
					pstmt.setInt( 2, iDslSqncNmbr );
					Log.write( Log.DEBUG_VERBOSE,
						"DslBean.changeStatus(): strUpdate=["+ strUpdate1 +"]");
					int iRows = pstmt.executeUpdate();
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					Log.write(Log.ERROR,
						"DslBean : DB Exception on Update : " + strUpdate1);
				}finally{
					try{
						if ( pstmt != null )
						{
							pstmt.close();
						}
					}catch (Exception e){
						e.printStackTrace();
						Log.write(Log.ERROR, "DslBean.ChangeStatus :  "
						+ " *****ERROR  Closing preparedstatement: ****** "
						+ e.toString() );
					}
				}
			}
		}

		//if we got here, we had a successful
		//Status Change and Generated a History Record.
		// Return the History Sequence Number
		return (iHistorySequenceNumber);
	}

	//
    // Accept TN and build XML string
	//
	public String buildQualRequestXml(String strNPANXXLINE)
	{
		String strXML = "";
		String strTransactionID = "";
		String strMessageType = "WINLOC";
		String strTimeStamp = "";
		String strTransactionType = "D";
		String strFindBy = "T";
		String strWebServer = "unknown";
		setPhone( strNPANXXLINE );
		try {
			strWebServer = PropertiesManager.getProperty("lsr.bpqlookup.orighost","unknown");
		}
		catch(Exception e) {
			Log.write(Log.ERROR, "DsTicketBean.buildQualRequestXml() Exception ["+e+"] caught getting properties");
		}
		strTransactionID = strWebServer + Toolkit.getDateTimeStamp("yyyyDDDHHmmssSSS");

		//Userids greater than 12 are causing Dsl Svc to crap out...
		String strTruncUserid = this.getUserid();
		       strTruncUserid = "BPQLITE";
		if ( strTruncUserid.length() > 12 ) strTruncUserid = strTruncUserid.substring(0,12);

		strXML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<DSLQualificationRequest>"+
			"<transactionID>"+ strTransactionID + "</transactionID>" +
			"<messageType>" + strMessageType + "</messageType>" +
			"<userID>" + strTruncUserid + "</userID>" +
			"<timeStamp>" + Toolkit.getDateTimeStamp("yyyyMMddhhmmssz") + "</timeStamp>" +
			"<transactionType>" + strTransactionType + "</transactionType>" +
			"<findBy>" + strFindBy + "</findBy>" +
			"<streetNumber/><streetName/><streetType/><unitNumber/><wireCenter/><townCode/><city/><state/>" +
			"<telephoneNumber>" + strNPANXXLINE + "</telephoneNumber>" +
			"<name/><facilitiesOrderNumber/><serviceKey/><dropNumber/>" +
			"</DSLQualificationRequest>";

		return strXML;
	}

	public QualificationRequest buildQualRequestObj(Document strXML)
	{
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj(" + strXML + ")");
		XMLUtility xmlUtility = new XMLUtility();

		QualificationRequest qualRqst = new QualificationRequest();

		qualRqst.setTransactionID(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.transactionID"));
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - done setting transactionID");
		qualRqst.setApplicationID("");
		qualRqst.setMessageType(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.messageType"));
		qualRqst.setUserID(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.userID"));
		qualRqst.setTimeStamp(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.timeStamp"));
		qualRqst.setTransactionType(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.transactionType"));
		qualRqst.setFindBy(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.findBy"));
		qualRqst.setStreetNumber(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.streetNumber"));
		qualRqst.setStreetName(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.streetName"));
		qualRqst.setStreetType(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.streetType"));
		qualRqst.setUnitNumber(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.unitNumber"));
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - setting WireCenterList");
		int j=1;
		WireCenterList[] wrCntrLst = new WireCenterList[j];
		for (j=0; j < wrCntrLst.length; j++)
		{
			wrCntrLst[j] = new WireCenterList();
			wrCntrLst[j].setWireCenter(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.wireCenter"));
//			wrCntrLst[j].setWireCenter("HDSN");
		}
		qualRqst.setWireCenterList(wrCntrLst);
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - done setting the WireCenterList");
//		qualRqst.getWireCenterList()[0].setWireCenter(new String(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.wireCenter")));
//		qualRqst.getWireCenterList()[0].setWireCenter(new String("HDSN"));
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - done setting the wireCenter to HDSN");
		qualRqst.setTownCode(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.townCode"));
		qualRqst.setCity(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.city"));
		qualRqst.setState(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.state"));
		qualRqst.setZip("");
		qualRqst.setTelephoneNumber(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.telephoneNumber"));
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - telephone number is " + qualRqst.getTelephoneNumber() + " after extracting from XML");
		qualRqst.setName("");
		qualRqst.setFacilitiesOrderNumber(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.facilitiesOrderNumber"));
		qualRqst.setServiceKey(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.serviceKey"));
		qualRqst.setDropNumber(xmlUtility.getNodeValue(strXML, "DSLQualificationRequest.dropNumber"));
		qualRqst.setBOID("");
		qualRqst.setBEX("");
		qualRqst.setMirorQueue("");
		qualRqst.setDRSWireCenter("");
		qualRqst.setBlock("");
		qualRqst.setPin("");
		qualRqst.setFtToBB("");
		qualRqst.setDedicatedFlag("");
		qualRqst.setAWGF("");
		qualRqst.setDRSExchange("");
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualRequestObj() - returning the object");

		return qualRqst;
	}

	public String buildQualResponseStr(Response qualResp) throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "WEBSERVICE RESPONSE =====> "+qualResp.toString());
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - building the return string");
		String strPrdLst = "<ProductList>";
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - length of response list is " + qualResp.getDslResponseList().length);
		if (qualResp.getDslResponseList()[0].getProductList() == null)
		{
			Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - product list is null");
		}
		else
		{
			Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - length of product list is " + qualResp.getDslResponseList()[0].getProductList().length);
			for (int i=0;i<qualResp.getDslResponseList()[0].getProductList().length;i++)
			{
				strPrdLst = strPrdLst + "<Product>"					+
										"<ProductID>" 				+
					qualResp.getDslResponseList()[0].getProductList()[i].getProductId()										+
										"</ProductID>" 				+
										"<ProductName>"				+
					qualResp.getDslResponseList()[0].getProductList()[i].getProductName()										+
										"</ProductName>";
//				Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) 01 - product list is " + strPrdLst);
				Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - length of Nsp list is " + qualResp.getDslResponseList()[0].getProductList()[i].getNspList().length);
				for (int j=0;j<qualResp.getDslResponseList()[0].getProductList()[i].getNspList().length;j++)
				{
					strPrdLst = strPrdLst + "<nsp>"					+
											"<nspId>"				+
						qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getNspId()						+
											"</nspId>" 				+
											"<nspName>"				+
						qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getNspName().replaceAll(" & "," and ")	+
											"</nspName>";
//					Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) 02 - product list is " + strPrdLst);
					Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - length of Csp list is " + qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getCpeList().length);
					for (int k=0;k<qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getCpeList().length;k++)
					{
						strPrdLst = strPrdLst + "<cpe>"				+
												"<cpeId>"			+
							qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getCpeList()[k].getCpeId()	+
												"</cpeId>"			+
												"<cpeName>"			+
							qualResp.getDslResponseList()[0].getProductList()[i].getNspList()[j].getCpeList()[k].getCpeName()	+
												"</cpeName>"		+
												"</cpe>";
//					Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) 03 - product list is " + strPrdLst);
					}
					strPrdLst = strPrdLst + "</nsp>"				+
											"<pinCount>"			+
						qualResp.getDslResponseList()[0].getProductList()[i].getPinCount()										+
											"</pinCount>"			+
											"<pinNotSupported>"	+
						qualResp.getDslResponseList()[0].getProductList()[i].getPinNotSupported()								+
											"</pinNotSupported>";
//					Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) 04 - product list is " + strPrdLst);
				}
				strPrdLst = strPrdLst + "</Product>";
			}
		}
		strPrdLst = strPrdLst + "</ProductList>";
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - done building the product string");
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) 05 - product list is " + strPrdLst);

                /***** For VDSL2 and FttP Qualification in Express by - Sarath: Begin *****/
                String strDslQualResult = qualResp.getDslResponseList()[0].getDslQualResult();
                String strDslQualMsg = qualResp.getDslResponseList()[0].getDslQualMessage();
                // Get the state and telephone number from BPQ service.
                String strState = qualResp.getDslResponseList()[0].getState();
                String strTelephone = qualResp.getDslResponseList()[0].getTelephone();
                
                double dSpeedInMb = 0;               
                //Extract only the digits from the string
		String strSpeed = strDslQualResult.replaceAll("\\D+","");		
                //Split the msg based on the digits
		String[] arrDslQualResult = strDslQualResult.split(strSpeed);		
                //Get the msg after the digit
		String strLastDslQualResult = arrDslQualResult[1].trim();
				
                //Check if the speed is "NOT" in Kb.
                //If it is in Kb, display the strDslQualResult and strDslQualMsg as it is from Web Service.
                if(!strLastDslQualResult.toLowerCase().startsWith("k")) {
                    //Check if the speed is in Mb or in Gb
                    if(strLastDslQualResult.toLowerCase().startsWith("m")) {
                            dSpeedInMb = Double.parseDouble(strSpeed);		
                    }
                    else {
                            //Convert Gb to Mb
                            dSpeedInMb = Double.parseDouble(strSpeed)*1024;		
                    }
                    
                    // If state is empty or null, then retrieve the state from Wincare DB by the telephone number.
                    if((strState == null || strState.length() == 0) && strTelephone != null && strTelephone.length() != 0){
                    	
                    	//Checking for Nebraska(NE) Customer.
                    	if (isNebraskaCustomer(strTelephone)){
                    		strState = NE_STATE;
                    	}
                    }
                    
                    // Nebraska(NE) - Set the default speed FTTP(50 MB) and VDSL2(50 MB)
                    // Non Nebraska(NE) - Set the default speed FTTP(25 MB) and VDSL2(12 MB)
                    if(NE_STATE.equalsIgnoreCase(strState)){
                    	// FttP (Fiber)
                        if(strDslQualResult.toLowerCase().indexOf("fiber") >= 0) {
                            if(dSpeedInMb > NE_DEFAULT_SPEED_IN_MB_FTTP) {
                                strDslQualResult = "Qualified up to 50 Mb Via Fiber";
                                strDslQualMsg = "Location qualified for up to 50 Mb Via Fiber";                          
                            }
                        }
                        // VDSL2
                        else {
                            if(dSpeedInMb > NE_DEFAULT_SPEED_IN_MB_VDSL2) {
                                strDslQualResult = "Qualified up to 50 Mb";
                                strDslQualMsg = "Location qualified for up to 50 Mb";                                                
                            }
                        } 
                    }else{
                    	// FttP (Fiber)
                        if(strDslQualResult.toLowerCase().indexOf("fiber") >= 0) {
                            if(dSpeedInMb > DEFAULT_SPEED_IN_MB_FTTP) {
                                strDslQualResult = "Qualified up to 25 Mb Via Fiber";
                                strDslQualMsg = "Location qualified for up to 25 Mb Via Fiber";                          
                            }
                        }
                        // VDSL2
                        else {
                            if(dSpeedInMb > DEFAULT_SPEED_IN_MB_VDSL2) {
                                strDslQualResult = "Qualified up to 12 Mb";
                                strDslQualMsg = "Location qualified for up to 12 Mb";                                                
                            }
                        }  
                    }
                }                
                /**** For VDSL2 and FttP Qualification in Express by - Sarath: End *****/
                
                
		String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"														+
			"<DSLQualificationResponse>"																				+
				"<resultList>"  																							+
					"<startStreetNumber>" 		+ qualResp.getDslResponseList()[0].getStartStreetNumber() 		+ "</startStreetNumber>" 	+
					"<stopStreetNumber>" 		+ qualResp.getDslResponseList()[0].getStopStreetNumber() 		+ "</stopStreetNumber>" 	+
					"<streetName>" 				+ qualResp.getDslResponseList()[0].getStreetName() 			+ "</streetName>" 			+
					"<streetType>" 				+ qualResp.getDslResponseList()[0].getStreetType() 			+ "</streetType>" 			+
					"<unitNumber>" 				+ qualResp.getDslResponseList()[0].getUnitNumber() 			+ "</unitNumber>" 			+
					"<wireCenter>" 				+ qualResp.getDslResponseList()[0].getWireCenter() 			+ "</wireCenter>" 			+
					"<townCode>"   				+ qualResp.getDslResponseList()[0].getTownCode() 				+ "</townCode>" 			+
					"<county>"   				+ qualResp.getDslResponseList()[0].getCounty() 				+ "</county>" 				+
					"<city>"       				+ qualResp.getDslResponseList()[0].getCity() 					+ "</city>" 				+
					"<state>"      				+ qualResp.getDslResponseList()[0].getState() 					+ "</state>" 				+
					"<zip>" 					+ qualResp.getDslResponseList()[0].getZip() 					+ "</zip>" 					+
					"<telephone>" 				+ qualResp.getDslResponseList()[0].getTelephone() 				+ "</telephone>" 			+
					"<name>" 					+ qualResp.getDslResponseList()[0].getName() 					+ "</name>" 				+
					"<serviceKey>" 				+ qualResp.getDslResponseList()[0].getServiceKey() 			+ "</serviceKey>" 			+
					"<dropNumber>" 				+ qualResp.getDslResponseList()[0].getDropNumber() 			+ "</dropNumber>" 			+
					"<centralOfficeEquipment>"	+ qualResp.getDslResponseList()[0].getCentralOfficeEquipment()	+ "</centralOfficeEquipment>" 			+
					"<mapField>" 				+ qualResp.getDslResponseList()[0].getMapField() 				+ "</mapField>" 			+
					"<serviceKey>" 				+ qualResp.getDslResponseList()[0].getServiceKey() 			+ "</serviceKey>" 			+
					"<serviceStatus>" 			+ qualResp.getDslResponseList()[0].getServiceStatus() 			+ "</serviceStatus>"		+
					"<cutInformation>"			+ qualResp.getDslResponseList()[0].getCutInformation()			+ "</cutInformation>"		+
					"<lictMDF>" 				+ qualResp.getDslResponseList()[0].getLictMDF() 				+ "</lictMDF>"	 			+
					"<lictLC>"	 				+ qualResp.getDslResponseList()[0].getLictLC() 				+ "</lictLC>"	 			+
					"<lictDrop>"	 			+ qualResp.getDslResponseList()[0].getLictDrop()				+ "</lictDrop>"	 			+
					"<pendingOrdNumber>"		+ qualResp.getDslResponseList()[0].getPendingOrdNumber() 		+ "</pendingOrdNumber>"		+
					"<pendingOrdDueDate>"		+ qualResp.getDslResponseList()[0].getPendingOrdDueDate()		+ "</pendingOrdDueDate>"	+
					"<pendingOrderType>"		+ qualResp.getDslResponseList()[0].getPendingOrderType() 		+ "</pendingOrderType>"		+
					"<mirorNotes>" 				+ qualResp.getDslResponseList()[0].getMirorNotes() 			+ "</mirorNotes>" 			+
					"<terminalResistance>"		+ qualResp.getDslResponseList()[0].getTerminalResistance()		+ "</terminalResistance>"	+
					"<dSLEnabled>" 				+ qualResp.getDslResponseList()[0].getDSLEnabled()				+ "</dSLEnabled>" 			+
					"<busRes>"	 				+ qualResp.getDslResponseList()[0].getBusRes()					+ "</busRes>"	 			+
					"<mdf>" 					+ qualResp.getDslResponseList()[0].getMdf()					+ "</mdf>"		 			+
					"<xconn>"	 				+ qualResp.getDslResponseList()[0].getXconn()					+ "</xconn>"	 			+
					"<dRSWireCenter>"			+ qualResp.getDslResponseList()[0].getDRSWireCenter()			+ "</dRSWireCenter>"		+
					"<block>" 					+ qualResp.getDslResponseList()[0].getBlock()					+ "</block>"	 			+
					"<pin>"		 				+ qualResp.getDslResponseList()[0].getPin()					+ "</pin>"		 			+
					"<ftToBB>"	 				+ qualResp.getDslResponseList()[0].getFtToBB()					+ "</ftToBB>"	 			+
					"<dedicatedFlag>" 			+ qualResp.getDslResponseList()[0].getDedicatedFlag()			+ "</dedicatedFlag>"		+
					"<aWGF>"	 				+ qualResp.getDslResponseList()[0].getAWGF()					+ "</aWGF>"		 			+
					"<boid>"		 			+ qualResp.getDslResponseList()[0].getBoid()					+ "</boid>"		 			+
					"<bex>"			 			+ qualResp.getDslResponseList()[0].getBex()					+ "</bex>"		 			+
					"<translatedQualificationResults>"																					+
						"<result>" 					+ strDslQualResult			+ "</result>"			+
						"<message>" 				+ strDslQualMsg			+ "</message>"			+
						"<disqualCode>" 			+ qualResp.getDslResponseList()[0].getDslDisqualCode()			+ "</disqualCode>"		+
						strPrdLst																										+
					"</translatedQualificationResults>"																					+
				"</resultList>"																											+
				"<otherInfo>"																											+
    				"<serviceDisposition>"		+ qualResp.getOther().getServiceDisposition()				+ "</serviceDisposition>"	+
    				"<mirorInfo>"																										+
    					"<transactionID>"			+ qualResp.getOther().getMirorInfo().getTransactionID()		+ "</transactionID>"	+
    					"<oid>"						+ qualResp.getOther().getMirorInfo().getOid()				+ "</oid>"				+
    					"<messageType>"				+ qualResp.getOther().getMirorInfo().getMessageType()		+ "</messageType>"		+
      					"<userID>"					+ qualResp.getOther().getMirorInfo().getUserID()			+ "</userID>"			+
      					"<timeStamp>"				+ qualResp.getOther().getMirorInfo().getTimeStamp()			+ "</timeStamp>"		+
      					"<orderType>"				+ qualResp.getOther().getMirorInfo().getOrderType()			+ "</orderType>"		+
      					"<statusCode>"				+ qualResp.getOther().getMirorInfo().getStatusCode()		+ "</statusCode>"		+
      					"<statusDescription>"		+ qualResp.getOther().getMirorInfo().getStatusDescription()	+ "</statusDescription>"+
      					"<transactionType>"			+ qualResp.getOther().getMirorInfo().getTransactionType()	+ "</transactionType>"	+
    				"</mirorInfo>"																										+
				"</otherInfo>"																											+
			"</DSLQualificationResponse>";
        strXML = strXML.replaceAll("&","and");
		Log.write(Log.DEBUG_VERBOSE, "DslBean.buildQualResponseStr(qualResp) - done building the product string");
		Log.write(Log.DEBUG_VERBOSE, "strXML is " +
		strXML);

		return strXML;
	}

	public String dslQualInterpretResponse(String strResp, boolean bShortResp) throws Exception
	{
		XMLUtility xmlUtility = new XMLUtility();
		return dslQualInterpretResponse( xmlUtility.inputStreamToXML(new java.io.ByteArrayInputStream(strResp.getBytes())) , bShortResp);
	}

	//
	//	Accept XML and try to interpret Dsl Qual Service response
	//
	public String dslQualInterpretResponse(String strResp) throws Exception
	{
		return dslQualInterpretResponse( strResp, false);
	}

	public String dslQualInterpretResponse(Document xmlDoc) throws Exception
	{
		return dslQualInterpretResponse ( xmlDoc, false);
	}

 	public String dslQualInterpretResponse(Document xmlDoc, boolean bShortResp) throws Exception
        {
		String strResultMsg = "";
		String strResultShortMsg = "unknown";
		String strProductList = "";
		String strProducts = "";
		//Log.write(Log.DEBUG_VERBOSE, "DslBean.dslQualification() XML = [" + strXML + "] ");

		String strFontColor  = "red";

		XMLUtility xmlUtility = new XMLUtility();
		String strDisp = xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.otherInfo.serviceDisposition");
		Log.write(Log.DEBUG_VERBOSE, "DslBean.dslQualInterpretResponse() Disposition="+ strDisp);
		//Can chk disposition of call here...ideally, s/b "COMPLETED_OK"
		int iTerminalResistance = 0;
		setDSLEnabled( xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.resultList.DSLEnabled" ) );

		if ( strDisp.equalsIgnoreCase("COMPLETED_OK") )
		{

			String strTerminalResistance = xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.resultList.terminalResistance");
			//check to see if the resistance value has been set
			if ( (strTerminalResistance != null) && (strTerminalResistance.length() > 0) )
			{
				//get the integer value of the resistance value field
				iTerminalResistance = Integer.parseInt(strTerminalResistance);
			}
			Log.write(Log.DEBUG_VERBOSE, "DslBean.dslQualInterpretResponse() TerminalResist="+ iTerminalResistance);
			//set the resistance value that will be stored in this class object
			setTermResist( iTerminalResistance );

			//get the short result message to be displayed from the XML
			strResultShortMsg = xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.resultList.translatedQualificationResults.result");
			//set the result message that will be stored in this class object
			this.setResults (   strResultShortMsg   );
			//set the result short message to be displayed in the center of the web page
			String strResult = "<b>" + strResultShortMsg + "</b>";

			//set the bGood value to true if the short result message begins with the indicated value
			boolean bGood = strResultShortMsg.startsWith("Qualif");
			if (!bGood)
			{
				bGood = strResultShortMsg.startsWith("Result");
			}
			if (bGood)
			{
				strFontColor = "green";
				strProductList = xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.resultList.translatedQualificationResults.ProductList");
				strProducts = "<b>" + strProductList + "</b>";
			}

			//get the result message to be displayed from the XML
			strResultMsg = xmlUtility.getNodeValue(xmlDoc, "DSLQualificationResponse.resultList.translatedQualificationResults.message");
			setMessage(  strResultMsg   );
			Log.write(Log.DEBUG_VERBOSE, "DslBean.dslQualInterpretResponse() statusDescription="+ strResult+" **DSLQUAL** "+ strResultMsg);

			String strDisplayMsg =  dbloadMessage();
			if( strDisplayMsg.length() > 1 )
			{ // seen before and stored in db. May have express preferred swap
				strResultMsg = "<font color=\"" + strFontColor +"\">" + strDisplayMsg + "</font>";
			}else
			{ // first time seeing this terminal Resistance code ...
					strResultMsg =  "<font color=\"" + strFontColor +"\">" + strResult + "<br>" + strResultMsg /*+ "</br>" + strProducts */+ "</font>";
			}
		}
		else
		{	String strError = "";

			if ( strDisp.equalsIgnoreCase("FAILED_NO_MIROR_QUEUE") || strDisp.equalsIgnoreCase("FAILED_MIROR_LOOKUP") )
			{	strError = PropertiesManager.getProperty("lsr.bpqlookup.TNnotfound","Error encountered. Please try later");
				strResultShortMsg = "TN not found";
			}
			else
			{	strError = PropertiesManager.getProperty("lsr.bpqlookup.errormsg","Error encountered. Please try later");
				strResultShortMsg = "Error";

			}
			strResultMsg = "<font color=\"red\">" + strError + "</font>";
			this.setResults (   strResultShortMsg   );
			setMessage( strError );
			setTermResist( iTerminalResistance );
		}


		setSrvDisposition( strDisp );


		if (bShortResp)
			return strResultShortMsg;
		else
			return strResultMsg;
        }

	public int webserviceDSLQualification( String strXMLRequest )
	{
		XMLUtility xmlUtility = new XMLUtility();
		int iReturnCode = 0;
		String strResult = "";
        setQualResults(strResult);

		// Setup the global JAXM message factory
                System.setProperty("javax.xml.soap.MessageFactory", "weblogic.webservice.core.soap.MessageFactoryImpl");
                // Setup the global JAX-RPC service factory
                System.setProperty( "javax.xml.rpc.ServiceFactory", "weblogic.webservice.core.rpc.ServiceFactoryImpl");

		try {
//			DSLQualification_Impl dslQual =  new DSLQualification_Impl();
            WebServiceInterfaceProxy dslQual =  new WebServiceInterfaceProxy();
			WebServiceInterface webSrvc = dslQual.getWebServiceInterface();
                        
                        
                        //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bpqlookup.url
            Projects_UqualWebServiceProject_initial_UqualWebServiceSoapBindingStub bpqStub = (Projects_UqualWebServiceProject_initial_UqualWebServiceSoapBindingStub)webSrvc;
            String urlString = new String(PropertiesManager.getProperty("lsr.bpqlookup.url","http://vms151.windstream.com:8090/webservice/Projects_UqualWebServiceProject_initial_UqualWebService?wsdl"));

            Log.write("BpqWebService URL prior to dynamic setting : "+dslQual.getEndpoint());
//            bpqStub.setEndpoint(urlString);
            dslQual.setEndpoint(urlString);
            Log.write("BpqWebService URL after dynamic setting : "+dslQual.getEndpoint());
                        

			Log.write(Log.DEBUG_VERBOSE, "DslBean.webserviceDSLQualification() xml=[" + strXMLRequest + "]");
			Log.write(Log.DEBUG_VERBOSE, "DslBean.webserviceDSLQualification() before web service invocation *********");
//			strResult = webSrvc.qualificationRequest(strXMLRequest);
			Response rsp = webSrvc.qualificationRequest(buildQualRequestObj(xmlUtility.validate(strXMLRequest)));
			Log.write(Log.DEBUG_VERBOSE, "DslBean.webserviceDSLQualification() after web service invocation **********");
            setQualResults(buildQualResponseStr(rsp));
		} catch (Exception ee)
		{
			Log.write(Log.DEBUG_VERBOSE, "DslBean.webserviceDSLQualification() ***** ERROR *****");
			Log.write(Log.DEBUG_VERBOSE, "DslBean.webserviceDSLQualification() Exception caught calling DSL qual Web Service");
			Log.write(Log.DEBUG_VERBOSE, "[" + ee + "]");
			iReturnCode = -1;
		}
		return iReturnCode;
	}

	/* EK.
	 dbGetMessageCode: checks whether message code is
	 available and if not create new one and
	 	return the new message code.
	 @see: dbInsert()
	 @param:
	 	con:  external connections please don't release.
	 	brCntrl: controls the recursive call after inserting
	 	 a new message in the DSL_LOOKUP_MESSAGES_T
	  @Exceptions: SQlException caught by caller ( dbInsert() )
	 */

	public  int dbGetMessageCode(  Connection con, boolean brCntrl )
		throws SQLException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String strQuery1  = "select MESSAGE_CD from DSL_LOOKUP_MESSAGES_T "
		 			+ " WHERE  terminal_rsstnc_cd  =  ? "
		 			+ " AND DSLEnabled =  ? ";

		String strQry1  = "Insert into "
				 + " DSL_LOOKUP_MESSAGES_T( TERMINAL_RSSTNC_CD,WBSRV_MESSAGE, DSLEnabled ) "
				 + " VALUES( ?, ?, ?  ) ";

		pstmt = con.prepareStatement( strQuery1 );
		pstmt.clearParameters();
		pstmt.setInt( 1, iTermResist );
		pstmt.setString( 2, strDSLEnabled );
		rs = pstmt.executeQuery();
		int iMsgCode = -99;
		if ( rs.next() )
		{
			iMsgCode = rs.getInt( 1 );
		}else if(brCntrl)
		{

			pstmt = con.prepareStatement( strQry1 );
			pstmt.clearParameters();
			pstmt.setInt( 1, iTermResist	);
			pstmt.setString( 2, strMessage );
			pstmt.setString( 3, strDSLEnabled );

			pstmt.executeUpdate();
			pstmt.close();
			rs.close();
			return dbGetMessageCode( con, false );
		}
		if ( rs != null ){  rs.close(); }
		if ( pstmt != null ){ pstmt.close(); }
		return iMsgCode;
	}

	/*EK.
	 dbGetResultsCode: checks whether results code is available
	 and if not create new one and
	 	return the new results code.
	 @see: dbInsert()
	 @param:
	 	con:  external connections please don't release.
	 	brCntrl: controls the recursive call after inserting a
	 	new results in the DSL_LOOKUP_MESSAGES_T
	  @Exceptions: SQlException caught by caller ( dbInsert() )
	 */


	public  int dbGetResultsCode(  Connection con, boolean brCntrl )
		throws SQLException {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String strQuery  = " select RESULTS_CD from DSL_LOOKUP_RESULTS_T "
		 		+ " WHERE  terminal_rsstnc_cd  =  ? "
		 		+ " AND DSLEnabled =  ? ";

		String strQry  = " Insert into "
				+ " DSL_LOOKUP_RESULTS_T( TERMINAL_RSSTNC_CD,WBSRV_RESULTS, DSLENABLED ) "
				+ " VALUES( ?, ? , ? )";

		pstmt = con.prepareStatement( strQuery );
		pstmt.clearParameters();
		pstmt.setInt( 1, iTermResist );
		pstmt.setString( 2, strDSLEnabled );
		rs = pstmt.executeQuery();
		int iMsgCode = -99;
		if ( rs.next() )		{
			iMsgCode = rs.getInt( 1 );
		}else if( brCntrl )
		{
			pstmt.close();
			pstmt = con.prepareStatement( strQry );
			pstmt.clearParameters();
			pstmt.setInt( 1, iTermResist	);
			pstmt.setString( 2, strResults );
			pstmt.setString( 3, strDSLEnabled );
			pstmt.executeUpdate();
			rs.close();
			pstmt.close();
			return dbGetResultsCode( con, false );
		}
		if ( rs != null ){  rs.close(); }
		if ( pstmt != null ){ pstmt.close(); }
		return iMsgCode;
	}


	/* EK
		dbInsert: Insert each lookup in the database.
		First it looks in the decode tables of existing
		decoded messages. If found, use  the code number in
		the history table else insert the non-existing
		message or results in its decode table and return values.

		********* NOTE THAT DB CONNECTION IS EXTERNAL,
		PLEASE DON't CLOSE, RELEASE IT********
	*/
	public boolean dbInsert()
	{

		PreparedStatement pstmt = null;
		int iMsgCode = -99;
		int iRsltCode = -99;
		int iInserts = 0;
		boolean bResults = false;
		// Build INSERT SQL statement
		String strIsertQuery = " Insert into DSL_LOOKUP_HISTORY_T "
			+ " ( USERID, PHONE, RESULTS_CD, "
			+ " MESSAGE_CD, TERMINAL_RSSTNC_CD, SRV_DISPOSITION,DSLENABLED  ) "
			+ " VALUES(?,?,?,?,?,?,? ) ";
		try{
			iRsltCode = dbGetResultsCode( m_conn, true );
			iMsgCode = dbGetMessageCode( m_conn, true );
			pstmt = m_conn.prepareStatement( strIsertQuery );
			pstmt.clearParameters();
			pstmt.setString( 1, getUserid() );
			pstmt.setString( 2 , strPhone );
			pstmt.setInt( 3, iRsltCode );
			pstmt.setInt( 4 , iMsgCode );
			pstmt.setInt( 5 , iTermResist );
			pstmt.setString( 6 , strSrvDisposition );
			pstmt.setString( 7 , strDSLEnabled );
			iInserts =  pstmt.executeUpdate();
			if(iInserts == 1 )
			{
				bResults = true;
			}else
			{
				Log.write(Log.ERROR, "DslBean.dbInsert():"
					+ " ERROR UPDATING DSL LOOKUP FOR \t" +  strPhone);
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 1: "+getUserid());
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 2: "+strPhone);
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 3: "+iRsltCode);
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 4: "+iMsgCode);
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 5: "+iTermResist);
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 6: "+strSrvDisposition);
			Log.write(Log.ERROR, "DslBean.dbInsert() Params 7: "+strDSLEnabled);
                        
			Log.write(Log.ERROR, "DslBean.dbInsert() : "
			 +	"ERROR UPDATING DSL LOOKUP FOR \t" +  e.toString() );
		}finally {	// Clean up
			try{
					if ( pstmt != null ){ pstmt.close(); }
			}catch (Exception e){
				e.printStackTrace();
				Log.write(Log.ERROR, "DslBean.dbloadMessage : "
				+	" ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP "
				+ e.toString() );
			}
		}
		return bResults;
	}

	/*EK.
	 * Get local message *
	 *@see  dslLookupGetMessage( terminal resistance code)
	 * plsql-function for details. The function looks for
	 *			Express message for  terminal resistance code,
	 *			if none exists, return default Web Service dslService_client
	 */
	public String dbloadMessage()
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String strQuery = " select dslLookupGetMessage( ?, ? ) from dual ";
		String msg = "";
		try{
			pstmt = m_conn.prepareStatement( strQuery );
			pstmt.clearParameters();
			pstmt.setInt( 1, iTermResist);
			pstmt.setString( 2, strDSLEnabled);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				msg = rs.getString( 1 );
				Log.write(Log.DEBUG_VERBOSE, "DslBean.dbloadMessage() msg="+ msg);
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
			Log.write(Log.ERROR, "DslBean.dbloadMessage :"
				+ "  ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP "
				+ e.toString() );
		} finally {	// Clean up
			try{
				if ( rs != null ){ rs.close(); }
				if ( pstmt != null ){ pstmt.close(); }
			}catch (Exception e){
				e.printStackTrace();
				Log.write(Log.ERROR, "DslBean.dbloadMessage : "
					+ " ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP "
					+ e.toString() );
			}
		}
		return msg;
	}


	public void dbloadResultByPhone( )
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String strQuery = " select dsllookupResultByPhn ( ? ) from dual ";
		String strRsts = "";
		try{
			pstmt = m_conn.prepareStatement( strQuery );
			pstmt.clearParameters();
			pstmt.setString( 1, strPhone);
			rs = pstmt.executeQuery();
			if ( rs.next() )
			{
				this.setResults( rs.getString( 1 ) );
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
			Log.write(Log.ERROR, "DslBean.dbGetMessageByPhone : "
				+ "  ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP "
			 	+ e.toString() );

		} finally {	// Clean up
			try{
				if ( rs != null ){ rs.close(); }
				if ( pstmt != null ){ pstmt.close(); }
			}catch (Exception e){
				e.printStackTrace();
				Log.write(Log.ERROR,
					"DslBean.dbGetMessageByPhone :  "
				+ " ERROR PERFORMING DATABASE ACTIVITY FOR DSL LOOKUP "
				+ e.toString() );
			}
		}
	}
	
    /**
     * This method will check the telephone number is Nebraska(NE) in WinCare Database.
     * @param tn(telephone number) - String
     * @return true - telephone number is Nebraska(NE) state, otherwise false
     */
    public boolean isNebraskaCustomer(String tn) {
    	
        Connection connCAMS = null;
        Statement stmtCAMS = null;
        ResultSet rs;
        String strQuery = "";
        boolean nebraskaTN = false;
        
        try {
            connCAMS = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
            stmtCAMS = connCAMS.createStatement();
            
            strQuery = "select sent_phone from kash.cams_sentt where org_state='NE' and sent_phone='"+tn+"'";
            
            Log.write("checkNebraskaCustomer strQuery: " + strQuery);
            rs = stmtCAMS.executeQuery(strQuery);
          
            if(rs.next()){
                Log.write(Log.DEBUG_VERBOSE, "Nebraska Customer ResultSet TN " + rs.getString("sent_phone"));
                nebraskaTN = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.write("Exception returned "+e);
        } finally {
            DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
        }
        return nebraskaTN;
    }

}




