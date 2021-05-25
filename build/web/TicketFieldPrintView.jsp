<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002-03
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	TicketFieldPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	Rev 1.1   21 Feb 2003 02:08:00   dzasada
 *	09/19/2003 Use generic
 *
 */                        

%>

<%@ page 
	language="java"
	import = "java.util.*, java.text.*,
		java.sql.*,
		javax.sql.*,
		com.alltel.lsr.common.objects.*,
		com.alltel.lsr.common.util.*"
	session="true"
%>
<%
AlltelRequest alltelRequest = null;
AlltelResponse alltelResponse = null;
SessionDataManager sdm = null;
try
{
	alltelRequest = new AlltelRequest(request);
	alltelResponse = new AlltelResponse(response);
	sdm = alltelRequest.getSessionDataManager();
	if ( (sdm == null) || (!sdm.isUserLoggedIn()) )
	{
		alltelResponse.sendRedirect("LoginCtlr");
		return;
	}
}
catch (Exception e)
{
	Log.write(Log.ERROR, e.getMessage());
	Log.write(Log.ERROR, "Trapped in TicketFieldPrintView.jsp");
}
%>

<jsp:useBean id="ticketBean" scope="request" class="com.alltel.lsr.common.objects.TicketBean" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Windstream Express</title>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body bgcolor="#ffffff" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<%--  Top Header --%>

<%


Connection conPrint = null;
Statement stmtPrint = null;

conPrint = DatabaseManager.getConnection();
ticketBean.getConnection();
ticketBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strTcktSqncNmbr = (String) request.getAttribute("TCKT_SQNC_NMBR");
int m_iTcktSqncNmbr = Integer.parseInt(m_strTcktSqncNmbr);
String m_strTcktVrsn = (String) request.getAttribute("TCKT_VRSN");
int m_iTcktVrsn = Integer.parseInt(m_strTcktVrsn);

// Verify user has access to view this form
if (! ticketBean.hasAccessTo(m_iTcktSqncNmbr))
{
	alltelResponse.sendRedirect("LsrSecurity.jsp");
	return;
}

/* Code for Running Query for PRINT_IND on Table USERID_T */				/* dmz code */
String m_strPrintIndQry = "SELECT PRINT_IND FROM USERID_T WHERE USERID = '" + sdm.getUser() + "' ";
ResultSet rsPrintInd = stmtPrint.executeQuery(m_strPrintIndQry);
rsPrintInd.next();
String m_strPrintInd = rsPrintInd.getString("PRINT_IND");
rsPrintInd.close();															/* dmz code */

String m_strTcktHdrQry = "SELECT TCKT_STTS_CD FROM TICKET_T WHERE TCKT_SQNC_NMBR = " + m_iTcktSqncNmbr;

ResultSet rsTcktHdr = stmtPrint.executeQuery(m_strTcktHdrQry);
rsTcktHdr.next();
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=100>Ticket&nbsp;Number</th>
		<th align=left width=70>Status</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=m_iTcktSqncNmbr%></td>
		<td align=left><%=rsTcktHdr.getString("TCKT_STTS_CD")%></td>
	</tr>
</table>

<%
Log.write(Log.DEBUG_VERBOSE, "TicketFieldPrintView.jsp --- ");
%>

<table align=left width=100% border=0 cellspacing=1 cellpadding=0>
  <tr>
    <td>
      &nbsp;
    </td>
		
	<td>
		&nbsp;
		<%--  put javascript function here to print -OR- open new window with printable format --%> 
    </td>
  </tr>
</table>
<BR CLEAR=ALL>
<HR>
<BR>
<TABLE>

<%
// build a vector of all the fields used in all forms that are part of the ticket
int m_iFrmSqncNmbrEV = 0;
Vector m_vTcktFrmFld = new Vector();
Vector m_vFrmFld = new Vector();
FormField ff;
ResultSet rsErrVldQry = null;
String m_strErrVldQry = "";
boolean bMoreForms = true;

m_strErrVldQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM TICKET_T, SERVICE_TYPE_FORM_T WHERE TCKT_SQNC_NMBR = " + 
	m_iTcktSqncNmbr + " AND SERVICE_TYPE_FORM_T.SRVC_TYP_CD = '2' AND SERVICE_TYPE_FORM_T.TYP_IND = 'T'";
	
rsErrVldQry = stmtPrint.executeQuery(m_strErrVldQry);
if (rsErrVldQry.next())
{
	m_iFrmSqncNmbrEV = rsErrVldQry.getInt("FRM_SQNC_NMBR");
}
else
{
	bMoreForms = false;
}

int a = 0;
while (bMoreForms)
{
	m_vFrmFld = ticketBean.getFormFields(m_iFrmSqncNmbrEV, m_iTcktSqncNmbr, m_iTcktVrsn);

	for(int i=0 ; i < m_vFrmFld.size() ; i++)
	{
		ff = (FormField)m_vFrmFld.elementAt(i);
		m_vTcktFrmFld.addElement(ff);
		a++;
	}
	m_vFrmFld.clear();

	if (rsErrVldQry.next())
	{
		m_iFrmSqncNmbrEV = rsErrVldQry.getInt("FRM_SQNC_NMBR");
	}
	else
	{
		bMoreForms = false;
	}
}

rsErrVldQry.close();

int iFrmSqncNmbr_ErrVldSv = 0;
int iFrmSctnSqncNmbr_ErrVldSv = 0;
int iFrmSctnOcc_ErrVldSv = 0;

int iErrCnt = 0;
int iTcktErrCnt = 0;

// Print out the info
for (int i=0 ; i < m_vTcktFrmFld.size() ; i++)
{
	ff = (FormField)m_vTcktFrmFld.elementAt(i);

	if (ff.getFrmSqncNmbr() != iFrmSqncNmbr_ErrVldSv)
		{
%>
				</UL></UL>
<%
			iFrmSqncNmbr_ErrVldSv = ff.getFrmSqncNmbr();
			iFrmSctnSqncNmbr_ErrVldSv = ff.getFrmSctnSqncNmbr();
			iFrmSctnOcc_ErrVldSv = ff.getFrmSctnOcc();

			m_strErrVldQry = "SELECT * FROM FORM_T WHERE FRM_SQNC_NMBR = " + ff.getFrmSqncNmbr();
			rsErrVldQry = stmtPrint.executeQuery(m_strErrVldQry);
			rsErrVldQry.next();
%>
			<LI type=disc><%=rsErrVldQry.getString("FRM_DSCRPTN")%><UL>
<%
			rsErrVldQry.close();
			m_strErrVldQry = "SELECT * FROM FORM_SECTION_T WHERE FRM_SQNC_NMBR = " + ff.getFrmSqncNmbr() + " AND FRM_SCTN_SQNC_NMBR = " + ff.getFrmSctnSqncNmbr();
			rsErrVldQry = stmtPrint.executeQuery(m_strErrVldQry);
			rsErrVldQry.next();
			if (!rsErrVldQry.getString("FRM_SCTN_RPT_IND").equals("H") || ff.getFrmSctnOcc() == 1)
			{
%>
				<LI type=round><%=rsErrVldQry.getString("FRM_SCTN_DSCRPTN")%><%if (ff.getFrmSctnOcc() > 0) { %>(<%=ff.getFrmSctnOcc()%>)<%}%>
<%			}
%>
			<UL>

<%			rsErrVldQry.close();
		}
		else
		{
			if ((ff.getFrmSctnSqncNmbr() != iFrmSctnSqncNmbr_ErrVldSv) || (ff.getFrmSctnOcc() != iFrmSctnOcc_ErrVldSv))
			{
%>
					</UL>
<%
				iFrmSctnSqncNmbr_ErrVldSv = ff.getFrmSctnSqncNmbr();
				iFrmSctnOcc_ErrVldSv = ff.getFrmSctnOcc();
				m_strErrVldQry = "SELECT * FROM FORM_SECTION_T WHERE FRM_SQNC_NMBR = " + ff.getFrmSqncNmbr() + " AND FRM_SCTN_SQNC_NMBR = " + ff.getFrmSctnSqncNmbr();
				rsErrVldQry = stmtPrint.executeQuery(m_strErrVldQry);
				rsErrVldQry.next();
				if (!rsErrVldQry.getString("FRM_SCTN_RPT_IND").equals("H") || ff.getFrmSctnOcc() == 1)
				{
%>
					<LI type=round><%=rsErrVldQry.getString("FRM_SCTN_DSCRPTN")%><%if (ff.getFrmSctnOcc() > 0) { %>(<%=ff.getFrmSctnOcc()%>)<%}%>
<%				}
%>
				<UL>

<%				rsErrVldQry.close();
			}
		}
if (m_strPrintInd.equals("Y") || (m_strPrintInd.equals("N") && (ff.getFieldData() != null)))	
	{																						/* dmz code */
%>
		<LI type=square><%=ff.getFldCd()%> -- <%=ff.getFrmFldNmbr()%> : <%=ff.getFieldData()%>
<%
	}																						/* dmz code */
}

ticketBean.closeConnection();
DatabaseManager.releaseConnection(conPrint);
%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

