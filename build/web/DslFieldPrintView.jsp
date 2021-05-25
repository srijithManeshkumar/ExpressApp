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
 * MODULE:	DslFieldPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *   Rev 1.1   21 Feb 2003 02:41:00   dzasada
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
	Log.write(Log.ERROR, "Trapped in DslFieldPrintView.jsp");
}
%>

<jsp:useBean id="dslBean" scope="request" class="com.alltel.lsr.common.objects.DslBean" />

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
dslBean.getConnection();
dslBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strDslSqncNmbr = (String) request.getAttribute("DSL_SQNC_NMBR");
int m_iDslSqncNmbr = Integer.parseInt(m_strDslSqncNmbr);
String m_strDslVrsn = (String) request.getAttribute("DSL_VRSN");
int m_iDslVrsn = Integer.parseInt(m_strDslVrsn);

// Verify user has access to view this form
if (! dslBean.hasAccessTo(m_iDslSqncNmbr))
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

String m_strDslHdrQry = "SELECT DSL_STTS_CD, LST_MDFD_CSTMR, CMPNY_NM FROM DSL_T, COMPANY_T WHERE DSL_T.CMPNY_SQNC_NMBR = COMPANY_T.CMPNY_SQNC_NMBR AND DSL_SQNC_NMBR = " + m_iDslSqncNmbr;

ResultSet rsDslHdr = stmtPrint.executeQuery(m_strDslHdrQry);
rsDslHdr.next();
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=100>Dsl&nbsp;Number</th>
		<th align=left width=80>Status</th>
		<th align=left width=150>ISP</th>
		<th align=left width=120>Submitter</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=m_iDslSqncNmbr%></td>
		<td align=left><%=rsDslHdr.getString("DSL_STTS_CD")%></td>
		<td align=left><%=rsDslHdr.getString("CMPNY_NM")%></td>
		<td align=left><%=rsDslHdr.getString("LST_MDFD_CSTMR")%></td>
	</tr>
</table>

<%
Log.write(Log.DEBUG_VERBOSE, "DslFieldPrintView.jsp --- ");
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
// build a vector of all the fields used in all forms that are part of the DSL order
int m_iFrmSqncNmbrEV = 0;
Vector m_vDslFrmFld = new Vector();
Vector m_vFrmFld = new Vector();
FormField ff;
ResultSet rsErrVldQry = null;
String m_strErrVldQry = "";
boolean bMoreForms = true;

m_strErrVldQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM DSL_T, SERVICE_TYPE_FORM_T WHERE DSL_SQNC_NMBR = " + 
	m_iDslSqncNmbr + " AND SERVICE_TYPE_FORM_T.SRVC_TYP_CD = '5' AND SERVICE_TYPE_FORM_T.TYP_IND = 'D'";
	
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
	m_vFrmFld = dslBean.getFormFields(m_iFrmSqncNmbrEV, m_iDslSqncNmbr, m_iDslVrsn);

	for(int i=0 ; i < m_vFrmFld.size() ; i++)
	{
		ff = (FormField)m_vFrmFld.elementAt(i);
		m_vDslFrmFld.addElement(ff);
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
int iDslErrCnt = 0;

// Print out the info
for (int i=0 ; i < m_vDslFrmFld.size() ; i++)
{
	ff = (FormField)m_vDslFrmFld.elementAt(i);

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

dslBean.closeConnection();
DatabaseManager.releaseConnection(conPrint);
%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

