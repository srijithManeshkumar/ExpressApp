<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	i_RequestHeader.jsp	
 * 
 * DESCRIPTION: Header for any Request related forms.
 * 
 * AUTHOR:      
 * 
 * DATE:        01-02-2002
 * 
 * HISTORY:
 *	02/01/2002	psedlak Release 1.1 - to include request locks/unlocks to
 *		prevent users from attempting to modify same request.
 *	04/17/2002 psedlak removed SELECT *
 *	10/10/2002 psedlak added NEW_SECTION,NEW_OCC
 *      10/30/2002 psedlak hd181581 - add confirmation to actions
 *      01/03/2003 psedlak Add 'Note' indicator and try/catch block
 *	05/01/2003 psedlak added generic locking
 */

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/i_RequestHeader.jsv  $
/*
/*   Rev 1.16   Oct 08 2003 09:35:00   e0069884
/* 
/*
/*   Rev 1.13   Jan 09 2003 12:00:38   e0069884
/*Indicate if 'Notes' exist
/*
/*   Rev 1.12   Oct 30 2002 16:56:58   e0069884
/* 
/*
/*   Rev 1.11   Oct 15 2002 10:54:26   e0069884
/* 
/*
/*   Rev 1.10   Oct 03 2002 15:09:20   sedlak
/*Check multiple submits on forms (HD 142788)
/*
/*   Rev 1.9   17 Apr 2002 14:51:00   sedlak
/*Changed SELECT * to use column names
/*
/*   Rev 1.8   09 Apr 2002 15:58:36   dmartz
/* 
/*
/*   Rev 1.7   21 Mar 2002 11:26:20   dmartz
/*Consolidate Locks, Actions, Statuses
/*
/*   Rev 1.6   19 Feb 2002 10:43:44   sedlak
/*change to update lock for locks held by current user.
/*
/*   Rev 1.5   18 Feb 2002 12:13:54   sedlak
/* 
/*
/*   Rev 1.4   13 Feb 2002 14:20:54   dmartz
/*Release 1.1
/*
/*   Rev 1.2   31 Jan 2002 13:19:26   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 07:06:58   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:04   wwoods
/*Initial Checkin
*/

/** $Revision:   1.16  $
*/

%>

<%@ include file="i_header.jsp" %>
<%
String m_CTLR = "RequestCtlr";
%>

<FORM NAME="ExpressFormView" ID="ExpressFormView" onSubmit="return checkOnSubmit();" METHOD=POST ACTION="<%=m_CTLR%>">

    <%-- 
kumar changed code for Express 5072 Q & V
added internal status and check with company type.
i have changed code and added code

 String strLockedUserID = rsHdr.getString(11);

--%>
<%

Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- ");

String m_strFrmScrtyTg = "";
String m_strActnScrtyTgSave = "";


String m_strSqncNmbr = (String) request.getAttribute("RQST_SQNC_NMBR"); 
int m_iSqncNmbr = Integer.parseInt(m_strSqncNmbr); 

String m_strVrsn = (String) request.getAttribute("RQST_VRSN");

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

String m_strForm = (String) request.getAttribute("rqstform"); 
if ((m_strForm == null) || (m_strForm.length() == 0))
{
	m_strForm = "";
}
String m_strFormType = (String) request.getAttribute("rqstformtype"); 

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

Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- m_strSqncNmbr = " + m_strSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- m_strVrsn = " + m_strVrsn);
Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- m_strFrmSqncNmbr = " + m_strFrmSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- m_strForm = " + m_strForm);
Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- m_strFormType = " + m_strFormType);

int m_iVrsn = 0;
int m_iCurrentVrsn = 0;

Connection connHdr = null;
Statement stmtHdr = null;
ResultSet rsHdr = null;
final String m_strTypInd = "R";

// Get fields from REQUEST_T that will show up on the top of each Request related page.
final String m_strHdrQry = "SELECT A.RQST_STTS_CD,A.INN_STTS,A.SIMPLE_PORT_FLAG,A.ICARE,A.RQST_PON, A.SRVC_TYP_CD, A.ACTVTY_TYP_CD, A.RQST_VRSN, A.RQST_TYP_CD, " +	
	" B.SRVC_TYP_DSCRPTN, C.ACTVTY_TYP_DSCRPTN, NVL(L.USERID, '_NOT_LOCKED_'), " +
	" TO_CHAR(NVL(L.LCK_DT, sysdate), 'MM/DD/YYYY @ HH24:MI:SS'), S.DTL_ORDR_HSTRY_IND  " +
	" FROM REQUEST_T A, SERVICE_TYPE_T B, ACTIVITY_TYPE_T C, LOCK_T L, STATUS_T S " +
	" WHERE A.RQST_SQNC_NMBR = ? AND A.SRVC_TYP_CD = B.SRVC_TYP_CD AND A.ACTVTY_TYP_CD = C.ACTVTY_TYP_CD " +
	" AND A.RQST_STTS_CD = S.STTS_CD AND S.TYP_IND = '" + m_strTypInd + "' " +
	" AND L.SQNC_NMBR (+) = A.RQST_SQNC_NMBR" +
	" AND B.TYP_IND (+) = '" + m_strTypInd + "'" +
	" AND C.TYP_IND (+) = '" + m_strTypInd + "'" +
	" AND L.TYP_IND (+) = '" + m_strTypInd + "'";

try {

connHdr = DatabaseManager.getConnection();
stmtHdr = connHdr.createStatement();

//1.1
PreparedStatement pstmt = connHdr.prepareStatement(m_strHdrQry);
pstmt.setInt(1, m_iSqncNmbr);

//Do 'Notes' exist for request?
ResultSet rsNote = stmtHdr.executeQuery("SELECT COUNT(*) FROM REQUEST_NOTES_T WHERE RQST_SQNC_NMBR="+
		m_iSqncNmbr);
rsNote.next();
int iNoteCount = rsNote.getInt(1);
rsNote.close();
rsNote=null;

//1.1 ResultSet rsHdr = stmtHdr.executeQuery(m_strHdrQry);
rsHdr = pstmt.executeQuery();
rsHdr.next();

String strSrvcTypCd = rsHdr.getString("SRVC_TYP_CD");
String instatus = rsHdr.getString("INN_STTS");
String spFlag = rsHdr.getString("SIMPLE_PORT_FLAG");
String strICARE = rsHdr.getString("ICARE");
String strActvtyTypCd = rsHdr.getString("ACTVTY_TYP_CD");
String strSttsCd = rsHdr.getString("RQST_STTS_CD");
String strRqstTypCd = rsHdr.getString("RQST_TYP_CD");
String strRecordHistory = rsHdr.getString("DTL_ORDR_HSTRY_IND");

//1.1 check if order is locked by someone
boolean bOrderLocked = false; 
boolean bSelfLocked = false;
String strLockedUserID = rsHdr.getString(12);
String strLockedDate = "";
String strFrmQry = "";
String strCT = sdm.getLoginProfileBean().getUserBean().getCmpnyTyp();
Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- strLockedUserID = " + strLockedUserID);
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
		strLockedDate = rsHdr.getString(11);
	}
}
//end of 1.1	

m_iCurrentVrsn = rsHdr.getInt("RQST_VRSN");

if ((m_strVrsn == null) || (m_strVrsn.length() == 0))
{
	m_iVrsn = m_iCurrentVrsn;
}
else
{
	m_iVrsn = Integer.parseInt(m_strVrsn);
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
	<th align=left width=80>PON</th>
	<th align=left width=100>Status</th>
    <% if(strCT.equals("P")){ %>
    <th align=left width=100>Internal Status</th>
    <th align=left width=100>Simple Order</th>
    <th align=center width=20>ICARE</th>
    <%} %>
	<th align=left width=130>Service&nbsp;Type</th>
	<th align=left width=130>Activity</th>
	<th align=left width=80>Version</th>
<%
//1.1
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
	<td align=center><%=rsHdr.getString("RQST_PON")%></td>
	<td align=center><%=strSttsCd%></td>
       <% if(strCT.equals("P")){ %>
        <td align=center><%=instatus%></td>
        <td align=center><%=spFlag%></td>
        <td align=center><%=strICARE%></td>
        <%} %>
	<td align=center><%=rsHdr.getString("SRVC_TYP_DSCRPTN")%></td>
	<td align=center><%=rsHdr.getString("ACTVTY_TYP_DSCRPTN")%></td>
	<td align=center><%=m_iCurrentVrsn%></td>
<%
//1.1
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
<!--      BOT Fallout button view-->
      <%	boolean compType = "P".equals(strCT) ;
                boolean statusReq = ("IN-REVIEW".equals(strSttsCd) && "IN-REVIEW".equals(instatus)) ||      //After Open for Review
                    ("IN-REVIEW".equals(strSttsCd) && "null".equals(instatus)) ||                           //After Reset to In Review
                    ("JEOPARDY".equals(strSttsCd) && "null".equals(instatus)) ||                            //After Jeopardy
                    ("FOC".equals(strSttsCd) && "MANUAL-FOC".equals(instatus)) ||                            //After Foc
                    ("FOC".equals(strSttsCd) && "null".equals(instatus));
		boolean servActType = "C".equals(strSrvcTypCd) && "V".equals(strActvtyTypCd);
		boolean serviceType = "H".equals(strSrvcTypCd) || "G".equals(strSrvcTypCd) || "J".equals(strSrvcTypCd);
		boolean activityType = "N".equals(strActvtyTypCd) || "C".equals(strActvtyTypCd) || "D".equals(strActvtyTypCd) || "R".equals(strActvtyTypCd);
									
		if (compType && statusReq && (servActType || (serviceType && activityType))) {
                         Log.write("i_RequestHeader.jsp --- Clicked by BOT Fallout process");
%>
			<td align=center>
				<INPUT class=appButton TYPE="BUTTON" NAME="action" VALUE="BOT Fallout" onClick="return submitForm(this);">
			</td>
<%		}
%>
      
<%--	ONLY ALLOW ACCESS TO VIEW/UPDATE NOTES FOR USERS WITH PROPER ACCESS.--%>
<%		if (sdm.isAuthorized("RQST_NOTES_VIEW") ) {
%>
			<td align=center
				<%if (strNotes != null) 
				 { if (strNotes.equals("Notes"))
				   { %> bgcolor=yellow <% }}
				   else if (iNoteCount>0) { %> bgcolor=red <% } %> >
				<INPUT class=appButton class=appButton TYPE="BUTTON" NAME="notes" VALUE="Notes" onClick="return submitForm(this);">
			</td>
<%		}
%>
	<td align=center
		<%if (strHist != null) 
			 { if (strHist.equals("History"))
				{ %> bgcolor=yellow <% }} %> >
		<INPUT class=appButton class=appButton TYPE="BUTTON" NAME="hist" VALUE="History" onClick="return submitForm(this);">
	</td>

<%	if (strRqstTypCd.equals("M") && sdm.isAuthorized("RQST_NOTES_VIEW"))
	{
%>
		<td align=center>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="<%=m_CTLR%>?RQST_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;RQST_VRSN=<%=m_iVrsn%>&amp;atnprint=Print3" target=_blank>ATN</A>
			</td></tr>
			</table>
		</td>
<%	}
%>

	<td align=center>
		<A HREF="<%=m_CTLR%>?RQST_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;RQST_VRSN=<%=m_iVrsn%>&amp;print=Print" target=_blank>
		<img src="images/field_print.gif" alt="Field Print" border=0>
		</A>
	</td>
	<td align=center>
		<A HREF="<%=m_CTLR%>?RQST_SQNC_NMBR=<%=m_strSqncNmbr%>&amp;RQST_VRSN=<%=m_iVrsn%>&amp;print2=Print2" target=_blank>
		<img src="images/form_print.gif" alt="Form Print" border=0>
		</A>
	</td>
  </tr>	
  </table>
</td>
</tr>
</table>
<INPUT TYPE="HIDDEN" NAME="RQST_SQNC_NMBR" VALUE="<%=m_strSqncNmbr%>">
<INPUT TYPE="HIDDEN" NAME="RQST_VRSN" VALUE="<%=m_iVrsn%>">
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
		
            if (strCT.equals("P"))
            {
		strFrmQry = "SELECT DISTINCT A.FRM_SQNC_NMBR, A.FRM_CD, A.SCRTY_OBJCT_CD, B.SRVC_TYP_FRM_SQNC " +
								 " FROM FORM_T A, SERVICE_TYPE_FORM_T B " +
								 " WHERE B.SRVC_TYP_CD = '" + strSrvcTypCd + "' AND A.FRM_SQNC_NMBR = B.FRM_SQNC_NMBR " +
								 " AND A.LSOG_VRSN = " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") +
								 " AND B.TYP_IND = '" + m_strTypInd + "' " +
								 " ORDER BY B.SRVC_TYP_FRM_SQNC";
	    }
	    else
	    {
	        strFrmQry = "SELECT DISTINCT A.FRM_SQNC_NMBR, A.FRM_CD, A.SCRTY_OBJCT_CD, B.SRVC_TYP_FRM_SQNC " +
	       						         " FROM FORM_T A, SERVICE_TYPE_FORM_T B " +
	       							 " WHERE B.SRVC_TYP_CD = '" + strSrvcTypCd + "' AND A.FRM_SQNC_NMBR = B.FRM_SQNC_NMBR " +
	       							 " AND A.LSOG_VRSN = " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") +
	       							 " AND B.TYP_IND = '" + m_strTypInd + "' AND B.FORM_DISPLAY IS NULL " +
								 " ORDER BY B.SRVC_TYP_FRM_SQNC";
	    }	        
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
			<INPUT class=appButton TYPE="BUTTON" NAME="rqstform" VALUE="<%=rsHdr.getString("FRM_CD")%>" onClick="return submitForm(this);">
			</td>
<% 
		} //while()
		rsHdr.close();
		
		//1.1 Override security tag if record is locked - effectively making it READONLY
		if ( bOrderLocked ) 
		{
			Log.write(Log.DEBUG_VERBOSE, "i_RequestHeader.jsp --- Tweaked security tag since its locked");
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
		if (m_iVrsn == m_iCurrentVrsn)
		{
			/*  If we are working with the current version of the request		*/
			/*  build this ACTION list dynamically based on ACTION_T.	*/ 
			/*  Only show the actions that the user has access to.				*/

			if ( bOrderLocked )	/*1.1 If locked, can't do anything so don't */
			{						/*    build the ACTION list.			    */
%>
				<td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN>
				</td> 
<%
			}
			else
			{
			        String strInnStts = "";
			        String strInnSttsInd = "N";
			        String strInnSttsQry = "SELECT INN_STTS FROM REQUEST_T WHERE RQST_SQNC_NMBR = '" + m_iSqncNmbr + "'";
			        rsHdr = stmtHdr.executeQuery(strInnSttsQry);
			        while(rsHdr.next()==true)
			        {
			          strInnStts = rsHdr.getString("INN_STTS");
			          
			          if (strInnStts.equals("PRE-FOC") || strInnStts.equals("PRE-REJECT"))
				  {
				      strInnSttsInd = "Y";
			          }
			         
			        }
			        rsHdr.close();
			        
				boolean bAlreadyLocked = false;
				String strAction = ""; 
				String strConfirmationText = "";
                                String strConfirm = "N";
				String strActionQry = "SELECT ACTN, SCRTY_OBJCT_CD, CNFRM_ACTN_IND, CNFRM_ACTN_TXT "+
					" FROM ACTION_T WHERE STTS_CD_FROM = '" + strSttsCd + "' AND TYP_IND = '" + m_strTypInd + "' AND RQST_TYP_CD = '" +
					 strRqstTypCd + "' AND ACTN <> 'BOT Fallout'";
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
                                                                    Log.write(Log.ERROR, "i_RequestHeader.jsp --- Error updating locked request=" + m_iSqncNmbr);
                                                                    //So we're going to bounce out of this while() and make READONLY.
%>								
                                                                    <td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN></td> 								
<%                                                                  break;
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
								Log.write(Log.ERROR, "i_RequestHeader.jsp --- Error locking request=" + m_iSqncNmbr);
								//Lock probably failed becuase someone was a little quickier than us and they got it.
								//So we're going to bounce out of this while() and make READONLY.
%>								
								<td><SPAN class="errormsg">READ&nbsp;ONLY&nbsp;&nbsp;&nbsp;</SPAN></td> 								
<%								break;
                                                            }
                                                            else
                                                            {
								bAlreadyLocked = true;
                                                                //Put the request lock in user's session object now
                                                                sdm.setLock(m_strTypInd, m_iSqncNmbr);
                                                                alltelRequest.putSessionDataManager(sdm); //persist
                                                            }

                                                         }//else
						}
						
						strAction = rsHdr.getString("ACTN");
						strConfirm = rsHdr.getString("CNFRM_ACTN_IND");
                                                strConfirmationText = " onClick=\"return submitForm(this);\" ";
                                                
                                                if (strAction.equals("Open for Review"))
                                                {
                                                   
                                                   if (strConfirm != null && strConfirm.equals("Y") && strInnSttsInd.equals("Y"))
                                                   {
                                                       strConfirmationText = " onClick=\"if(!confirm('" + rsHdr.getString("CNFRM_ACTN_TXT") +
                                                       		"')){return false;}else{return submitForm(this);}\" ";
                                                   }
                                                }
                                                else
                                                {                                                
                                                   if (strConfirm != null && strConfirm.equals("Y"))
                                                   {
                                                        strConfirmationText = " onClick=\"if(!confirm('" + rsHdr.getString("CNFRM_ACTN_TXT") +
                                                        		"')){return false;}else{return submitForm(this);}\" ";
                                                   }
                                                }

						if (strAction.equals("Save"))
						{
%>
							<td align=center
<%	
							if (strActionAtt != null) 
							{	if (strActionAtt.equals("Validate"))
								{ 
%>
									bgcolor=yellow 
<%
								}
							}
%>
							>
							<INPUT class=appButton TYPE="BUTTON" NAME="action" VALUE="Validate" onClick="return submitForm(this);">
							</td>
<% 
							if (m_strFrmScrtyTg != "" && sdm.isAuthorized(m_strFrmScrtyTg))
							{
								m_strActnScrtyTgSave = rsHdr.getString("SCRTY_OBJCT_CD");
%>
								<td align=center>
								<INPUT  class=appButton TYPE="BUTTON" NAME="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
								</td>
<% 
							}
						}
						else
						{
%>
							<td align=center>
							<INPUT  class=appButton TYPE="BUTTON" NAME="action" VALUE="<%=strAction%>"  <%=strConfirmationText%> >
							</td>
<% 
						}
					} //end-if
				} //while()
				rsHdr.close();
			} //end-else 
			
		} //end-if
		else
		{
%>
			<td><SPAN class="errormsg">VIEWING&nbsp;VERSION&nbsp;<%=m_iVrsn%>&nbsp;&nbsp;</SPAN>
			</td> 
<%
		}
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
	//rsHdr.close();   //will get closed by stmt close() below
	//rsHdr = null;

	stmtHdr.close();
	stmtHdr=null;
	DatabaseManager.releaseConnection(connHdr);
}

%>

<BR CLEAR=ALL>
