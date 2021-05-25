 <%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	i_DslHeader.jsp	
 * 
 * DESCRIPTION: Header for any Dsl related forms.
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *	10/30/2002      psedlak hd181581 - add confirmation to actions
 *	01/03/2003 psedlak Add 'Note' indicator and try/catch block
 *	05/03/2003 psedlak put in generic locking
 *	05/04/2006 gao change Dsl to Broadband naming 
 */

%>

<%@ include file="i_header.jsp" %>

<%
String m_CTLR = "DslCtlr";
%>

<FORM NAME="ExpressFormView" onSubmit="return checkOnSubmit();" METHOD=POST ACTION="<%=m_CTLR%>">

<%

Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- ");

String m_strFrmScrtyTg = "";
String m_strActnScrtyTgSave = "";


String m_strSqncNmbr = (String) request.getAttribute("DSL_SQNC_NMBR"); 
int m_iSqncNmbr = Integer.parseInt(m_strSqncNmbr); 

String m_strVrsn = (String) request.getAttribute("DSL_VRSN");

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

String m_strForm = (String) request.getAttribute("dslform"); 
if ((m_strForm == null) || (m_strForm.length() == 0))
{
	m_strForm = "";
}
String m_strFormType = (String) request.getAttribute("dslformtype"); 

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

Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- m_strSqncNmbr = " + m_strSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- m_strVrsn = " + m_strVrsn);
Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- m_strFrmSqncNmbr = " + m_strFrmSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- m_strForm = " + m_strForm);
Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- m_strFormType = " + m_strFormType);

Connection connHdr = null;
Statement stmtHdr = null;
ResultSet rsHdr = null;

int m_iCurrentVrsn = 0;
int m_iVrsn = 0;
final String m_strTypInd = "D";

// Get fields from DSL_T that will show up on the top of each Dsl related page.
final String m_strHdrQry = "SELECT A.DSL_STTS_CD, A.LST_MDFD_CSTMR, " +
	" NVL(L.USERID, '_NOT_LOCKED_'), TO_CHAR(NVL(L.LCK_DT, sysdate), 'MM/DD/YYYY @ HH24:MI:SS'), " +
	" C.CMPNY_NM, A.SRVC_TYP_CD, S.DTL_ORDR_HSTRY_IND " + 
	" FROM DSL_T A, LOCK_T L, COMPANY_T C, STATUS_T S " +
	" WHERE A.DSL_SQNC_NMBR = ? AND A.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND A.DSL_STTS_CD=S.STTS_CD " +
	" AND S.TYP_IND='" + m_strTypInd + "' " +
	" AND L.SQNC_NMBR (+) = A.DSL_SQNC_NMBR" +
	" AND L.TYP_IND (+) = '" + m_strTypInd + "'";

try {

connHdr = DatabaseManager.getConnection();
stmtHdr = connHdr.createStatement();


PreparedStatement pstmt = connHdr.prepareStatement(m_strHdrQry);
pstmt.setInt(1, m_iSqncNmbr);

//Do 'Notes' exist for request?
ResultSet rsNote = stmtHdr.executeQuery("SELECT COUNT(*) FROM DSL_NOTES_T WHERE DSL_SQNC_NMBR="+ m_iSqncNmbr);
rsNote.next();
int iNoteCount = rsNote.getInt(1);
rsNote.close();
rsNote=null;

rsHdr = pstmt.executeQuery();
rsHdr.next();

String strSrvcTypCd = rsHdr.getString("SRVC_TYP_CD");

String strSttsCd = rsHdr.getString("DSL_STTS_CD");
String strIsp = rsHdr.getString("CMPNY_NM");
String strLstMdfdCstmr = rsHdr.getString("LST_MDFD_CSTMR");
String strRecordHistory = rsHdr.getString("DTL_ORDR_HSTRY_IND");

// check if order is locked by someone
boolean bOrderLocked = false; 
boolean bSelfLocked = false;
String strLockedUserID = rsHdr.getString(3);
String strLockedDate = "";
Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- strLockedUserID = " + strLockedUserID);
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
	<th align=left width=120>Broadband&nbsp;Number</th>
	<th align=left width=80>Status</th>
	<th align=left width=150>ISP/Agent</th>
	<th align=left width=120>Submitter</th>
<%
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
	<td align=left><%=strIsp%></td>
	<td align=left><%=strLstMdfdCstmr%></td>
<%	if (strSttsCd.equals("PENDING_DISPATCH"))
	{
%>
		<td align=left>1-800-937-3202</td>
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
<%		if (sdm.isAuthorized("DSL_NOTES_VIEW") ) {
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
		<A HREF="<%=m_CTLR%>?DSL_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;DSL_VRSN=<%=m_iVrsn%>&amp;print=Print" target=_blank>
		<img src="images/field_print.gif" alt="Field Print" border=0>
		</A>
	</td>
	<td align=center>
		<A HREF="<%=m_CTLR%>?DSL_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;DSL_VRSN=<%=m_iVrsn%>&amp;print2=Print2" target=_blank>
		<img src="images/form_print.gif" alt="Form Print" border=0>
		</A>
	</td>
  </tr>	
  </table>
</td>
</tr>
</table>
<INPUT TYPE="HIDDEN" NAME="DSL_SQNC_NMBR" VALUE="<%=m_strSqncNmbr%>">
<INPUT TYPE="HIDDEN" NAME="DSL_VRSN" VALUE="<%=m_iVrsn%>">
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
			<INPUT class=appButton TYPE="SUBMIT" NAME="dslform" VALUE="<%=rsHdr.getString("FRM_CD")%>">
			</td>
<% 
		} //while()
		rsHdr.close();
		
		//Override security tag if record is locked - effectively making it READONLY
		if ( bOrderLocked ) 
		{
			Log.write(Log.DEBUG_VERBOSE, "i_DslHeader.jsp --- Tweaked security tag since its locked");
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
			String strActionQry = "SELECT ACTN, SCRTY_OBJCT_CD, CNFRM_ACTN_IND, CNFRM_ACTN_TXT "+
				" FROM ACTION_T WHERE TYP_IND = '" + m_strTypInd + "' AND RQST_TYP_CD = '" + m_strTypInd + "' AND STTS_CD_FROM = '" + strSttsCd + "'";
			rsHdr = stmtHdr.executeQuery(strActionQry);
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
                                                            Log.write(Log.ERROR, "i_DslHeader.jsp --- Error updating locked DSL =" + m_iSqncNmbr);
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
							Log.write(Log.ERROR, "i_DslHeader.jsp --- Error locking DSL order=" + m_iSqncNmbr);
							//Lock probably failed becuase someone was a little quickier than us and they got it.
							//So we're going to bounce out of this while() and make READONLY.
%>								
							<td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN></td> 								
<%							break;
                                                    }
                                                    else
                                                    {
							bAlreadyLocked = true;
                                                        //Put the DSL order lock in user's session object now
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
                                                <INPUT class=appButton TYPE="SUBMIT" NAME="action" VALUE="Validate">
                                                </td>
<%
						if (m_strFrmScrtyTg != "" && sdm.isAuthorized(m_strFrmScrtyTg))
						{
							m_strActnScrtyTgSave = rsHdr.getString("SCRTY_OBJCT_CD");
%>
							<td align=center>
							<INPUT class=appButton TYPE="SUBMIT" NAME="action" VALUE="<%=strAction%>" <%=strConfirmationText%> >
							</td>
<% 
						}
					}
					else
					{
%>
						<td align=center>
						<INPUT class=appButton TYPE="SUBMIT" NAME="action" VALUE="<%=strAction%>" <%=strConfirmationText%> >
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
        //rsHdr.close();
        //rsHdr = null;

        stmtHdr.close();
        stmtHdr=null;
        DatabaseManager.releaseConnection(connHdr);
}
 
%>

<BR CLEAR=ALL>
