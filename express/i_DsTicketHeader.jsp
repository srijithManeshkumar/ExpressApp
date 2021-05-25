<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2004
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	i_DsTicketHeader.jsp	
 * 
 * DESCRIPTION: Header for any Ticket related forms.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004 pjs init
 */

%>

<%@ include file="i_header.jsp" %>
<%
String m_CTLR = "DsTicketCtlr";
%>

<FORM NAME="ExpressFormView" onSubmit="return checkOnSubmit();" METHOD=POST ACTION="<%=m_CTLR%>">

<%

Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- ");

String m_strFrmScrtyTg = "";
String m_strActnScrtyTgSave = "";



String m_strSqncNmbr = (String) request.getAttribute("TCKT_SQNC_NMBR"); 
int m_iSqncNmbr = Integer.parseInt(m_strSqncNmbr); 

String m_strVrsn = (String) request.getAttribute("TCKT_VRSN");

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

String m_strForm = (String) request.getAttribute("tcktform"); 
if ((m_strForm == null) || (m_strForm.length() == 0))
{
	m_strForm = "";
}
String m_strFormType = (String) request.getAttribute("tcktformtype"); 

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

Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strSqncNmbr = " + m_strSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strVrsn = " + m_strVrsn);
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strFrmSqncNmbr = " + m_strFrmSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strForm = " + m_strForm);
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strFormType = " + m_strFormType);

Connection connHdr = null;
Statement stmtHdr = null;
ResultSet rsHdr = null;
int m_iCurrentVrsn = 0;
int m_iVrsn = 0;
final String m_strTypInd = "S";

// Get fields from DSTICKET_T that will show up on the top of each Ticket related page.
final String m_strHdrQry = "SELECT A.STTS_CD, A.TELNO, NVL(L.USERID, '_NOT_LOCKED_'), " +
	" TO_CHAR(NVL(L.LCK_DT, sysdate), 'MM/DD/YYYY @ HH24:MI:SS'), S.DTL_ORDR_HSTRY_IND " +
	" FROM DSTICKET_T A, LOCK_T L, STATUS_T S " +
	" WHERE A.TCKT_SQNC_NMBR = ? AND A.STTS_CD = S.STTS_CD AND S.TYP_IND='" +  m_strTypInd + "' " +
	" AND L.SQNC_NMBR (+) = A.TCKT_SQNC_NMBR" +
	" AND L.TYP_IND (+) = '" + m_strTypInd + "'";
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strHdrQry=["+m_strHdrQry+"]");

try {

connHdr = DatabaseManager.getConnection();
stmtHdr = connHdr.createStatement();


PreparedStatement pstmt = connHdr.prepareStatement(m_strHdrQry);
pstmt.setInt(1, m_iSqncNmbr);

//Do 'Notes' exist for request?
ResultSet rsNote = stmtHdr.executeQuery("SELECT COUNT(*) FROM DSTICKET_NOTES_T WHERE TCKT_SQNC_NMBR="+ m_iSqncNmbr);
rsNote.next();
int iNoteCount = rsNote.getInt(1);
rsNote.close();
rsNote=null;

rsHdr = pstmt.executeQuery();
rsHdr.next();

String strSrvcTypCd = "T";
String strSttsCd = rsHdr.getString("STTS_CD");
String strTelNo = rsHdr.getString("TELNO");
String strRecordHistory = rsHdr.getString("DTL_ORDR_HSTRY_IND");

// check if order is locked by someone
boolean bOrderLocked = false; 
boolean bSelfLocked = false;
String strLockedUserID = rsHdr.getString(3);
String strLockedDate = "";
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- strLockedUserID = " + strLockedUserID);
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
		strLockedDate = rsHdr.getString(4);
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
	<th align=left width=100>Ticket&nbsp;Number</th>
	<th align=left width=120>Status</th>
	<th align=left width=120>Telephone&nbsp;Number</th>
<%	if (strSttsCd.equals("PENDING_DISPATCH"))
	{
%>
		<th align=left width=120>Service&nbsp;Center&nbsp;Number</th>
<%	}

	if ( bOrderLocked )
	{
%>
		<th align=left width=130>Locked&nbsp;By</th>
<%
	}
%>
	</tr>
	<tr>
	<td>&nbsp;</td>
	<td align=left><%=m_strSqncNmbr%></td>
	<td align=left><%=strSttsCd%></td>
	<td align=left><%=strTelNo%></td>
<%	if (strSttsCd.equals("PENDING_DISPATCH"))
	{
		String strSvcCenterTN =  PropertiesManager.getProperty("lsr.servicecenter.number", "1-800-937-3202");
%>
		<td align=left><%=strSvcCenterTN%></td>
<%	}

	if ( bOrderLocked )
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
<%		if (sdm.isAuthorized("DSTCKT_NOTES_VIEW") ) {
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
		<INPUT class=appButton TYPE="SUBMIT" NAME="hist" VALUE="History">
	</td>

	<td align=center>
		<A HREF="<%=m_CTLR%>?TCKT_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;TCKT_VRSN=<%=m_iVrsn%>&amp;print=Print" target=_blank>
		<img src="images/field_print.gif" alt="Field Print" border=0>
		</A>
	</td>
	<td align=center>
		<A HREF="<%=m_CTLR%>?TCKT_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;TCKT_VRSN=<%=m_iVrsn%>&amp;print2=Print2" target=_blank>
		<img src="images/form_print.gif" alt="Form Print" border=0>
		</A>
	</td>
  </tr>	
  </table>
</td>
</tr>
</table>
<INPUT TYPE="HIDDEN" NAME="TCKT_SQNC_NMBR" VALUE="<%=m_strSqncNmbr%>">
<INPUT TYPE="HIDDEN" NAME="TCKT_VRSN" VALUE="<%=m_iVrsn%>">
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
		/* Build this FORM list dynamically based on service type & activity type  */

		String strFrmQry = "SELECT DISTINCT A.FRM_SQNC_NMBR, A.FRM_CD, A.SCRTY_OBJCT_CD, B.SRVC_TYP_FRM_SQNC " +
								 " FROM FORM_T A, SERVICE_TYPE_FORM_T B " +
								 " WHERE B.SRVC_TYP_CD = '" + strSrvcTypCd + "' AND A.FRM_SQNC_NMBR = B.FRM_SQNC_NMBR " +
								 " AND A.LSOG_VRSN = " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") +
								 " AND B.TYP_IND = '" + m_strTypInd + "'" +
								 " ORDER BY B.SRVC_TYP_FRM_SQNC";
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- strFrmQry=["+strFrmQry+"]");
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
			<INPUT class=appButton TYPE="SUBMIT" NAME="tcktform" VALUE="<%=rsHdr.getString("FRM_CD")%>">
			</td>
<% 
		} //while()
		rsHdr.close();
		
		//Override security tag if record is locked - effectively making it READONLY
		if ( bOrderLocked ) 
		{
			Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- Tweaked security tag since its locked");
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
			boolean bAlreadyLocked = false;
			String strAction = ""; 
			String strConfirmationText = "";
                        String strConfirm = "N";
			String strActnQry = "SELECT ACTN, SCRTY_OBJCT_CD,  CNFRM_ACTN_IND, CNFRM_ACTN_TXT "+
				" FROM ACTION_T WHERE TYP_IND = '" + m_strTypInd + "' AND RQST_TYP_CD = 'T' AND STTS_CD_FROM = '" + strSttsCd + "'";
Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- strActnQry=["+strActnQry+"]");
			rsHdr = stmtHdr.executeQuery(strActnQry);
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
                                                            Log.write(Log.ERROR, "i_DsTicketHeader.jsp --- Error updating locked seq=" + m_iSqncNmbr);
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
							Log.write(Log.ERROR, "i_DsTicketHeader.jsp --- Error locking seq=" + m_iSqncNmbr);
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
					if (strConfirm != null && strConfirm.equals("Y"))
					{
						strConfirmationText =" onClick=\"if(!confirm('" + rsHdr.getString("CNFRM_ACTN_TXT") +
							"')) return false;\" ";

					}
					
	//gao debug
	Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- strAction = " + strAction);
	Log.write(Log.DEBUG_VERBOSE, "i_DsTicketHeader.jsp --- m_strFrmScrtyTg = " + m_strFrmScrtyTg);
					if (strAction.equals("Save"))
					{
						if (m_strFrmScrtyTg != "" && sdm.isAuthorized(m_strFrmScrtyTg))
						{
							m_strActnScrtyTgSave = rsHdr.getString("SCRTY_OBJCT_CD");
%>
							<td align=center>
							<INPUT class=appButton TYPE="SUBMIT" NAME="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
							</td>
<% 
						}
					}
					else
					{
%>
						<td align=center>
						<INPUT class=appButton TYPE="SUBMIT" NAME="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
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
}
finally {
        rsHdr.close();
        rsHdr = null;

	stmtHdr.close();
	stmtHdr=null;
	DatabaseManager.releaseConnection(connHdr);
}
%>

<BR CLEAR=ALL>
