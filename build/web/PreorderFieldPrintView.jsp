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
 * MODULE:	PreorderFieldPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        02-15-2002
 * 
 * HISTORY:
 * Rev 1.1   22 Feb 2003 02:22:00   dzasada 
 *	09/19/2003 psedlak use generic
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
	Log.write(Log.ERROR, "Trapped in PreorderFieldPrintView.jsp");
}
%>

<jsp:useBean id="preorderBean" scope="request" class="com.alltel.lsr.common.objects.PreorderBean" />

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
preorderBean.getConnection();
preorderBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strPreSqncNmbr = (String) request.getAttribute("PRE_ORDR_SQNC_NMBR");
int m_iPreSqncNmbr = Integer.parseInt(m_strPreSqncNmbr);

// Verify user has access to view this form
if (! preorderBean.hasAccessTo(m_iPreSqncNmbr))
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

String m_strPreHdrQry = "SELECT A.PRE_ORDR_SQNC_NMBR, A.PRE_ORDR_STTS_CD, A.SRVC_TYP_CD, A.ACTVTY_TYP_CD, B.SRVC_TYP_DSCRPTN, C.ACTVTY_TYP_DSCRPTN FROM PREORDER_T A, SERVICE_TYPE_T B, ACTIVITY_TYPE_T C WHERE A.PRE_ORDR_SQNC_NMBR = " + m_iPreSqncNmbr + " AND A.SRVC_TYP_CD = B.SRVC_TYP_CD AND A.ACTVTY_TYP_CD = C.ACTVTY_TYP_CD AND B.TYP_IND = 'P' AND C.TYP_IND = 'P'";

ResultSet rsPreHdr = stmtPrint.executeQuery(m_strPreHdrQry);
rsPreHdr.next();
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=80>TXNUM</th>
		<th align=left width=100>Status</th>
		<th align=left width=130>Service&nbsp;Type</th>
		<th align=left width=130>Activity</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=rsPreHdr.getString("PRE_ORDR_SQNC_NMBR")%></td>
		<td align=left><%=rsPreHdr.getString("PRE_ORDR_STTS_CD")%></td>
		<td align=left><%=rsPreHdr.getString("SRVC_TYP_DSCRPTN")%></td>
		<td align=left><%=rsPreHdr.getString("ACTVTY_TYP_DSCRPTN")%></td>
	</tr>
</table>

<%
Log.write(Log.DEBUG_VERBOSE, "PreorderFieldPrintView.jsp --- ");
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
// build a vector of all the fields used in all forms that are part of the preorder
int m_iFrmSqncNmbrEV = 0;
Vector m_vPreFrmFld = new Vector();
Vector m_vFrmFld = new Vector();
FormField ff;
ResultSet rsErrVldQry = null;
String m_strErrVldQry = "";
boolean bMoreForms = true;

m_strErrVldQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM PREORDER_T, SERVICE_TYPE_FORM_T WHERE PRE_ORDR_SQNC_NMBR = " + 
	m_iPreSqncNmbr + " AND PREORDER_T.SRVC_TYP_CD = SERVICE_TYPE_FORM_T.SRVC_TYP_CD AND SERVICE_TYPE_FORM_T.TYP_IND = 'P'";
	
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
	m_vFrmFld = preorderBean.getFormFields(m_iFrmSqncNmbrEV, m_iPreSqncNmbr, 0);

	for(int i=0 ; i < m_vFrmFld.size() ; i++)
	{
		ff = (FormField)m_vFrmFld.elementAt(i);
		m_vPreFrmFld.addElement(ff);
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
int iPreErrCnt = 0;

// Print out the info
for (int i=0 ; i < m_vPreFrmFld.size() ; i++)
{
	ff = (FormField)m_vPreFrmFld.elementAt(i);

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

preorderBean.closeConnection();
DatabaseManager.releaseConnection(conPrint);
%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
