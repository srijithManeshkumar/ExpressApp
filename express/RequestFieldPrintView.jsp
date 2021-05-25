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
 * MODULE:	RequestFieldPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        02-15-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestFieldPrintView.jsv  $
/*
/*   Rev 1.7   Mar 13 2003 17:14:46   e0069884
/* 
/*
/*   Rev 1.7   20 Feb 2003 00:36:00   dzasada
/*
/*   Rev 1.6   09 Apr 2002 15:57:12   dmartz
/* 
/*
/*   Rev 1.5   21 Feb 2002 12:30:40   sedlak
/* 
/*
/*   Rev 1.4   13 Feb 2002 14:20:42   dmartz
/*Release 1.1
/*
*/
/** $Revision:   1.7  $
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
	Log.write(Log.ERROR, "Trapped in RequestFieldPrintView.jsp");
}
%>

<jsp:useBean id="requestBean" scope="request" class="com.alltel.lsr.common.objects.RequestBean" />

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
requestBean.getConnection();
requestBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strRqstSqncNmbr = (String) request.getAttribute("RQST_SQNC_NMBR");
int m_iRqstSqncNmbr = Integer.parseInt(m_strRqstSqncNmbr);
String m_strRqstVrsn = (String) request.getAttribute("RQST_VRSN");
int m_iRqstVrsn = Integer.parseInt(m_strRqstVrsn);

// Verify user has access to view this form
if (! requestBean.hasAccessTo(m_iRqstSqncNmbr))
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

String m_strRqstHdrQry = "SELECT A.RQST_SQNC_NMBR, A.RQST_STTS_CD, A.RQST_PON, A.SRVC_TYP_CD, A.ACTVTY_TYP_CD, A.RQST_VRSN, A.RQST_TYP_CD, B.SRVC_TYP_DSCRPTN, C.ACTVTY_TYP_DSCRPTN FROM REQUEST_T A, SERVICE_TYPE_T B, ACTIVITY_TYPE_T C WHERE A.RQST_SQNC_NMBR = " + m_iRqstSqncNmbr + " AND A.SRVC_TYP_CD = B.SRVC_TYP_CD AND A.ACTVTY_TYP_CD = C.ACTVTY_TYP_CD AND B.TYP_IND = 'R' AND C.TYP_IND = 'R'";

ResultSet rsRqstHdr = stmtPrint.executeQuery(m_strRqstHdrQry);
rsRqstHdr.next();
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=80>PON</th>
		<th align=left width=100>Status</th>
		<th align=left width=130>Service&nbsp;Type</th>
		<th align=left width=130>Activity</th>
		<th align=left width=130>Version</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=rsRqstHdr.getString("RQST_PON")%></td>
		<td align=left><%=rsRqstHdr.getString("RQST_STTS_CD")%></td>
		<td align=left><%=rsRqstHdr.getString("SRVC_TYP_DSCRPTN")%></td>
		<td align=left><%=rsRqstHdr.getString("ACTVTY_TYP_DSCRPTN")%></td>
		<td align=left><%=rsRqstHdr.getInt("RQST_VRSN")%></td>
	</tr>
</table>

<%
Log.write(Log.DEBUG_VERBOSE, "RequestFieldPrintView.jsp --- ");
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
// build a vector of all the fields used in all forms that are part of the request
int m_iFrmSqncNmbrEV = 0;
Vector m_vRqstFrmFld = new Vector();
Vector m_vFrmFld = new Vector();
FormField ff;
ResultSet rsErrVldQry = null;
String m_strErrVldQry = "";
boolean bMoreForms = true;

m_strErrVldQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM REQUEST_T, SERVICE_TYPE_FORM_T WHERE RQST_SQNC_NMBR = " + 
	m_iRqstSqncNmbr + " AND REQUEST_T.SRVC_TYP_CD = SERVICE_TYPE_FORM_T.SRVC_TYP_CD AND SERVICE_TYPE_FORM_T.TYP_IND = 'R'";
	
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
	m_vFrmFld = requestBean.getFormFields(m_iFrmSqncNmbrEV, m_iRqstSqncNmbr, m_iRqstVrsn);

	for(int i=0 ; i < m_vFrmFld.size() ; i++)
	{
		ff = (FormField)m_vFrmFld.elementAt(i);
		m_vRqstFrmFld.addElement(ff);
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
int iRqstErrCnt = 0;

// Print out the info
for (int i=0 ; i < m_vRqstFrmFld.size() ; i++)
{
	ff = (FormField)m_vRqstFrmFld.elementAt(i);

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

requestBean.closeConnection();
DatabaseManager.releaseConnection(conPrint);
%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestFieldPrintView.jsv  $
/*
/*   Rev 1.2   31 Jan 2002 06:51:34   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 06:46:14   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.7  $
*/

%>
