<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	i_DwoHeader.jsp	
 * 
 * DESCRIPTION: Header for any Dwo related forms.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	pjs 4-20-2005 Logical Chg Orders/ Business Data Product
 */

%>

<%@ include file="i_header.jsp" %>
<%
String m_CTLR = "DwoCtlr";
%>

<FORM NAME="ExpressFormView" id ="ExpressFormView" onSubmit="return checkOnSubmit();" METHOD=POST ACTION="<%=m_CTLR%>">

<%

Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- ");

String m_strFrmScrtyTg = "";
String m_strActnScrtyTgSave = "";

HttpSession objSession = alltelRequest.getSession();
String strPJVN = (String)objSession.getAttribute("DwOcHoIcE");
String m_strTypInd = "";
if (strPJVN == null) {  strPJVN=""; }
if ( strPJVN.equals("Bdp") )
{
        m_strTypInd = "X";
}
else
        m_strTypInd = "W";
String strPrdctType = "";
String m_strSqncNmbr = (String) request.getAttribute("DWO_SQNC_NMBR"); 
int m_iSqncNmbr = Integer.parseInt(m_strSqncNmbr); 
String m_strVrsn = (String) request.getAttribute("DWO_VRSN");

//User just added this section/occ if it has a value -else set it to '0'
String strNewSection = (String) request.getAttribute("NEW_SECTION");
if ((strNewSection == null) || (strNewSection.length() == 0))
{
        strNewSection = "0";
}
String strNewOcc = (String) request.getAttribute("NEW_OCC");
if ((strNewOcc == null) || (strNewOcc.length() == 0))
{
        strNewOcc = "0";
}
int m_iNewSection = Integer.parseInt(strNewSection);
int m_iNewOcc = Integer.parseInt(strNewOcc);

String m_strFrmSqncNmbr = (String) request.getAttribute("FRM_SQNC_NMBR");
if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
{
	m_strFrmSqncNmbr = "0";
}
int m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr);

String m_strForm = (String) request.getAttribute("dwoform"); 
if ((m_strForm == null) || (m_strForm.length() == 0))
{
	m_strForm = "";
}
String m_strFormType = (String) request.getAttribute("dwoformtype"); 

if ((m_strFormType == null) || (m_strFormType.length() == 0))
{
	m_strFormType = "";
}
else
{
	if ( m_strFormType.equals("_FRM_SQNC_") && m_iFrmSqncNmbr == 0)
	{
		m_strFormType = "_FIRST_";
	}
}

Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- m_strSqncNmbr = " + m_strSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- m_strVrsn = " + m_strVrsn);
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- m_strFrmSqncNmbr = " + m_strFrmSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- m_strForm = " + m_strForm);
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- m_strFormType = " + m_strFormType);

Connection connHdr = null;
Statement stmtHdr = null;
ResultSet rsHdr = null;
int m_iCurrentVrsn = 0;
int m_iVrsn = 0;

// Get fields from DWO_T that will show up on the top of each Dwo related page.
final String m_strHdrQry = "SELECT A.DWO_STTS_CD, NVL(L.USERID, '_NOT_LOCKED_'), " +
	" TO_CHAR(NVL(L.LCK_DT, sysdate), 'MM/DD/YYYY @ HH24:MI:SS'), S.DTL_ORDR_HSTRY_IND, A.SRVC_TYP_CD, " +
	" T.SRVC_TYP_DSCRPTN, ATT.ACTVTY_TYP_DSCRPTN, SATT.SUB_ACTVTY_TYP_DSCRPTN, A.OCN_NM, A.OCN_CD, A.ORIG_SRVC_TYP_CD " +
	" FROM DWO_T A, LOCK_T L, STATUS_T S, SERVICE_TYPE_T T, ACTIVITY_TYPE_T ATT, SUB_ACTIVITY_TYPE_T SATT " +
	" WHERE A.DWO_SQNC_NMBR = ? AND A.DWO_STTS_CD = S.STTS_CD AND S.TYP_IND='" +  m_strTypInd + "' " +
	" AND A.SRVC_TYP_CD = T.SRVC_TYP_CD AND T.TYP_IND = '" + m_strTypInd + "'" +
	" AND A.DWO_SQNC_NMBR = L.SQNC_NMBR(+) AND L.TYP_IND(+) = '" + m_strTypInd + "' " +
	" AND A.ACTVTY_TYP_CD = ATT.ACTVTY_TYP_CD(+) AND ATT.TYP_IND(+) = '" + m_strTypInd + "' "  +
	" AND A.SUB_ACTVTY_TYP_CD = SATT.SUB_ACTVTY_TYP_CD(+) AND SATT.TYP_IND(+) = '" + m_strTypInd + "' ";
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- ["+ m_strHdrQry + "]");
try {

connHdr = DatabaseManager.getConnection();
stmtHdr = connHdr.createStatement();

PreparedStatement pstmt = connHdr.prepareStatement(m_strHdrQry);
pstmt.setInt(1, m_iSqncNmbr);

//Do 'Notes' exist for request?
ResultSet rsNote = stmtHdr.executeQuery("SELECT COUNT(*) FROM DWO_NOTES_T WHERE DWO_SQNC_NMBR="+ m_iSqncNmbr);
rsNote.next();
int iNoteCount = rsNote.getInt(1);
rsNote.close();
rsNote=null;

//Expedited Order?
String strExpediteThisOne="";
rsNote = stmtHdr.executeQuery("SELECT nvl(EXPDT_IND,'off') FROM DWO_SITE_ADM_T D WHERE D.DWO_SQNC_NMBR="+ m_iSqncNmbr);
if (rsNote.next())
{	String strE = rsNote.getString(1);
	if (strE.equals("on")) {
		strExpediteThisOne = "Expedite!";
	}
}
rsNote.close();
rsNote=null;

rsHdr = pstmt.executeQuery();
rsHdr.next();

String strSrvcTypCd = rsHdr.getString("SRVC_TYP_CD");
String strSttsCd = rsHdr.getString("DWO_STTS_CD");
String strRecordHistory = rsHdr.getString("DTL_ORDR_HSTRY_IND");
String strOrigSrvcTypCd = rsHdr.getString("ORIG_SRVC_TYP_CD");

request.setAttribute("strSttsCd",strSttsCd);

// With Logical Change Orders, we built a xref reference to a 'new' Srvc Type...
// If they differ, use the new one
//if ( !strSrvcTypCd.equals(strNewSrvcTypCd) )
//{	Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- SrvcType swap old[" + strSrvcTypCd + "] new["+ strNewSrvcTypCd +"]");
//	strSrvcTypCd = strNewSrvcTypCd;
//}

// check if order is locked by someone
boolean bOrderLocked = false; 
boolean bSelfLocked = false;
String strLockedUserID = rsHdr.getString(2);
String strLockedDate = "";
Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- strLockedUserID = " + strLockedUserID);
if ( strLockedUserID.equals("_NOT_LOCKED_") )
{	bOrderLocked = false;
}
else
{	//Am I locking it?
	if ( strLockedUserID.equals(sdm.getUser()) )
	{	bOrderLocked = false; //it's only me
                bSelfLocked = true;
	}
	else
	{	bOrderLocked = true; //someone else
		strLockedDate = rsHdr.getString(3);
	}
}

String strNotes = (String) request.getAttribute("notes"); 
String strHist = (String) request.getAttribute("hist"); 
String strPrint = (String) request.getAttribute("print"); 
String strPrint2 = (String) request.getAttribute("print2"); 
String strActionAtt = (String) request.getAttribute("action"); 

%>
<table align=left width="100%" border=0 cellspacing=0 cellpadding=0>
<tr>
<td align=left valign=top>
  <table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
	<th width=10>&nbsp;</th>
	<th align=left width=120>Data&nbsp;Work&nbsp;Order</th>
	<th align=left width=80>Status</th>
<%	if (m_strTypInd.equals("X"))
	{
		// Do nothing at this time
	}
	else
	{
%>
		<th align=left width=300>Site</th>
<%	}
%>
        <th align=left width=100>Order&nbsp;Type</th>
<%	if ( bOrderLocked )
	{
%>
		<th align=left width=130>Locked&nbsp;By</th>
<%
	}
%>
	</tr>
<%
	if ( strExpediteThisOne.length()>0 )
	{
%>
		<tr><td width=10>&nbsp;</td><td class="expedite"><%=strExpediteThisOne%></td></tr>
<%
	}
%>     <INPUT TYPE="HIDDEN" NAME="Frm_Status" ID="Frm_Status" VALUE="<%=strSttsCd%>"> 
	<tr>
	<td>&nbsp;</td>
	<td align=left><%=m_strSqncNmbr%></td>
	<td align=left><%=strSttsCd%></td>
<%	if (m_strTypInd.equals("X"))
	{
		// Do nothing at this time
	}
	else 
	{
		if (rsHdr.getString("OCN_CD").equals("New"))
		{
%>
		        <td align=left><%=rsHdr.getString("OCN_CD")%></td>
<%		}
		else
		{
%>
 		       <td align=left><%=rsHdr.getString("OCN_NM")%></td>
<%		}
	}

	String strChgTypChgSubType ="";
	if (rsHdr.getString("ACTVTY_TYP_DSCRPTN") != null)
	{ 
		strChgTypChgSubType ="("+ rsHdr.getString("ACTVTY_TYP_DSCRPTN") +"/"+ rsHdr.getString("SUB_ACTVTY_TYP_DSCRPTN") + ")";
	}
%>
        <td align=left><%=rsHdr.getString("SRVC_TYP_DSCRPTN")%>&nbsp;&nbsp;<%=strChgTypChgSubType%></td>
<% 	if ( bOrderLocked )
	{
%>
		<td align=left><font color=red><%=strLockedUserID%>&nbsp;@&nbsp;<%=strLockedDate%></font></td>
<%
	}
%>

	</tr>
  </table>
</td>
<td valign=top align=right>
  <table border=0 cellspacing=1 cellpadding=2>
  <tr>
<%--	ONLY ALLOW ACCESS TO VIEW/UPDATE NOTES FOR USERS WITH PROPER ACCESS.--%>
<%		if (sdm.isAuthorized("DWO_NOTES_VIEW") ) {
%>
			<td align=center
				<%if (strNotes != null) 
				 { if (strNotes.equals("Internal Notes"))
					{ %> bgcolor=yellow <% }}
				   else if (iNoteCount>0) { %> bgcolor=red <% } %> >
				<INPUT class=appButton TYPE="SUBMIT" NAME="notes" VALUE="Internal Notes">
			</td>
<%		}
%>
	<td align=center
		<%if (strHist != null) 
			 { if (strHist.equals("History"))
				{ %> bgcolor=yellow <% }} %> >
		<INPUT class=appButton class=appButton TYPE="SUBMIT" NAME="hist" VALUE="History">
	</td>

	<td align=center>
		<A HREF="<%=m_CTLR%>?DWO_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;DWO_VRSN=<%=m_iVrsn%>&amp;print=Print" target=_blank>
		<img src="images/field_print.gif" alt="Field Print" border=0>
		</A>
	</td>	
	<td align=center>
		<A HREF="<%=m_CTLR%>?DWO_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;DWO_VRSN=<%=m_iVrsn%>&amp;print2=Print2" target=_blank>
		<img src="images/form_print.gif" alt="Form Print" border=0>
		</A>
	</td>
  </tr>	
  </table>
</td>
</tr>
</table>
<INPUT TYPE="HIDDEN" NAME="DWO_SQNC_NMBR" VALUE="<%=m_strSqncNmbr%>">
<INPUT TYPE="HIDDEN" NAME="DWO_VRSN" VALUE="<%=m_iVrsn%>">
<INPUT TYPE="HIDDEN" NAME="REC_HST" VALUE="<%=strRecordHistory%>">

<% 
rsHdr.close();

/** 
 *  The heading row is now complete.  Now continue to display specifics, like forms and available  actions.
 */
 
%>
 
<BR CLEAR=ALL>
<HR>
<table align=left width="100%" border=0 cellspacing=1 cellpadding=0>
  <tr>
    <td valign=top>
	  <table align=left border=0 cellspacing=1 cellpadding=2>
        <tr>
		
<%
		/* Build this FORM list dynamically based on service type  */

		String strFrmQry = "SELECT DISTINCT A.FRM_SQNC_NMBR, A.FRM_CD, A.SCRTY_OBJCT_CD, B.SRVC_TYP_FRM_SQNC " +
				" FROM FORM_T A, SERVICE_TYPE_FORM_T B WHERE B.SRVC_TYP_CD = '" + strSrvcTypCd +
				"' AND A.FRM_SQNC_NMBR = B.FRM_SQNC_NMBR " +
			   	" AND A.LSOG_VRSN = " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") +
				" AND B.TYP_IND = '" + m_strTypInd + "'  ORDER BY B.SRVC_TYP_FRM_SQNC";

Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- strFrmQry=[" +strFrmQry+"]");
		rsHdr = stmtHdr.executeQuery(strFrmQry);

		while(rsHdr.next()==true) 
		{
%>
			<td align=center 
<%
			if ((m_strFormType.equals("_FIRST_")) && (rsHdr.getInt("SRVC_TYP_FRM_SQNC") == 1))
			{ 
				m_iFrmSqncNmbr = rsHdr.getInt("FRM_SQNC_NMBR");
				m_strFrmScrtyTg = rsHdr.getString("SCRTY_OBJCT_CD");
%> 
				bgcolor=yellow 
<% 
			}					
			else if ((m_strFormType.equals("_FRM_CD_"))  && (m_strForm.equals(rsHdr.getString("FRM_CD")))) 
			{ 
				m_iFrmSqncNmbr = rsHdr.getInt("FRM_SQNC_NMBR");
				m_strFrmScrtyTg = rsHdr.getString("SCRTY_OBJCT_CD");
%> 
				bgcolor=yellow
<% 
			}
			else if ((m_strFormType.equals("_FRM_SQNC_")) && (m_iFrmSqncNmbr == rsHdr.getInt("FRM_SQNC_NMBR"))) 
			{ 
				m_iFrmSqncNmbr = rsHdr.getInt("FRM_SQNC_NMBR");
				m_strFrmScrtyTg = rsHdr.getString("SCRTY_OBJCT_CD");
%> 
				bgcolor=yellow
<% 
			}
%>				
			>
			<INPUT class=appButton TYPE="SUBMIT" NAME="dwoform" VALUE="<%=rsHdr.getString("FRM_CD")%>">
			</td>
<% 
		} //while()
		rsHdr.close();
		
		//Override security tag if record is locked - effectively making it READONLY
		if ( bOrderLocked ) 
		{
			Log.write(Log.DEBUG_VERBOSE, "i_DwoHeader.jsp --- Tweaked security tag since its locked");
			m_strFrmScrtyTg = "_READ_ONLY_";	//this security object doesn't exist
		}
%>
		<%-- end Form Button loop --%>
        </tr>
      </table>

    </td>  
		
	<td valign=top>
	  <table align=right border=0 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
        <tr>
		  <td>&nbsp;</td>
<%

		if ( bOrderLocked )	/* If locked, can't do anything so don't */
		{	/*    build the ACTION list.			    */
%>
			<td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN>
			</td> 
<%
		}
		else
		{
			
			String strActnQry = "select distinct PRDCT_TYP_CD from DWO_T where "
				+ " DWO_SQNC_NMBR = " + m_strSqncNmbr;
			
			rsHdr = stmtHdr.executeQuery(strActnQry);
			if( rsHdr.next() ){
				strPrdctType = rsHdr.getString( 1 );
			}
			rsHdr.close();					
			strActnQry = "";
			boolean bAlreadyLocked = false;
			String strAction = ""; 
			String strConfirmationText = "";
            String strConfirm = "N";			
			strActnQry = "SELECT ACTN, SCRTY_OBJCT_CD,  CNFRM_ACTN_IND, CNFRM_ACTN_TXT "+
				" FROM ACTION_T WHERE TYP_IND = '" + m_strTypInd + 
				"' AND RQST_TYP_CD = '" + m_strTypInd + 
				"' AND STTS_CD_FROM = '" + strSttsCd + 
				"' AND PRDCT_TYP_CD = '" + strPrdctType + "'";
			rsHdr = stmtHdr.executeQuery(strActnQry);
			System.out.println("\n\n\n" + strActnQry + "\n\n\n" );
			while(rsHdr.next()==true) 		
			{
				if (sdm.isAuthorized(rsHdr.getString("SCRTY_OBJCT_CD")))
				{
					/* If authorized to any actions, we need to LOCK record for potential status change or SAVE */
					if (!bAlreadyLocked)
					{
						LockBean lockedObj = new LockBean(m_strTypInd, m_iSqncNmbr);
                                                if (bSelfLocked)        //We have it, so just update existing lock
                                                {
                                                        if (lockedObj.updateLock(sdm.getUser()) > 0)
                                                        {
                                                            //lock failed!
                                                            Log.write(Log.ERROR, "i_DwoHeader.jsp --- Error updating locked seq=" + m_iSqncNmbr);
                                                            //So we're going to bounce out of this while() and make READONLY.
%>							
                                                            <td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN></td> 								
<%                                                          break;
                                                        }
                                                        else
                                                        {
                                                            bAlreadyLocked = true;
                                                            //make sure lock is in user's session object
                                                            sdm.setLock(m_strTypInd, m_iSqncNmbr);
                                                         }
                                                }
                                                else
                                                {
                                                    if (lockedObj.lock(sdm.getUser()) > 0)
                                                    {
							//lock failed!
							Log.write(Log.ERROR, "i_DwoHeader.jsp --- Error locking seq=" + m_iSqncNmbr);
							//Lock probably failed becuase someone was a little quickier than us and they got it.
							//So we're going to bounce out of this while() and make READONLY.
%>								
							<td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN></td> 								
<%							break;
                                                    }
                                                    else
                                                    {
							bAlreadyLocked = true;
                                                        //Put the lock in user's session object now
                                                        sdm.setLock(m_strTypInd, m_iSqncNmbr);
                                                        alltelRequest.putSessionDataManager(sdm); //persist
                                                    }

                                                 }//else
					}
						
					strAction = rsHdr.getString("ACTN");
					strConfirm = rsHdr.getString("CNFRM_ACTN_IND");
					strConfirmationText = "";
					Log.write(Log.ERROR,"-------\n\n"  + rsHdr.getString( 1)  + "-----\n\n"  );
					request.setAttribute("strAction",strAction);
					if (strConfirm != null && strConfirm.equals("Y"))
					{
						strConfirmationText =" onClick=\"if(!confirm('" + rsHdr.getString("CNFRM_ACTN_TXT") +
							"')) return false;\" ";

					}

					if (strAction.equals("Save"))
					{
%>
                                                <td align=center
<%
                                                if (strActionAtt != null)
                                                {       if (strActionAtt.equals("Validate"))
                                                        {
%>
                                                                bgcolor=yellow
<%
                                                        }
                                                }
%>
                                                >
                                                <INPUT  class=appButton TYPE="SUBMIT" NAME="action" VALUE="Validate">
                                                </td>
<%						if (m_strFrmScrtyTg != "" && sdm.isAuthorized(m_strFrmScrtyTg))
						{
							m_strActnScrtyTgSave = rsHdr.getString("SCRTY_OBJCT_CD");
%>
							<td align=center>
							<INPUT class=appButton TYPE="SUBMIT" NAME="action" ID="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
							</td>
<% 
						}
					}
					else
					{
%>
						<td align=center>
						<INPUT class=appButton TYPE="SUBMIT" NAME="action" ID="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
						</td>
<% 
					}
				} //end-if
			} //while()
			rsHdr.close();
		} //end-else 
%>	

        </tr>
      </table>
	</td>
  </tr>
</table>

<%

} //try
catch (Exception e) {
Log.write(Log.DEBUG_VERBOSE, " ut oh e=["+e+"]");

Log.write("Exception in i_DwoHeader.jsp. Error Message :"+e.getMessage());

}
finally {
        //rsHdr.close();
        //rsHdr = null;

	stmtHdr.close();
	stmtHdr=null;
        
        Log.write("Inside finally block in i_DwoHeader.jsp ! Releasing connection object :"+connHdr.toString());
        
	DatabaseManager.releaseConnection(connHdr);
        
        Log.write("Inside finally block in i_DwoHeader.jsp ! Released connection object.");
}
%>

<BR CLEAR=ALL>
