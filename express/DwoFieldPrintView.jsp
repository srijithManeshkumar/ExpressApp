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
 * MODULE:	DwoFieldPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
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
	Log.write(Log.ERROR, "Trapped in DwoFieldPrintView.jsp");
}

HttpSession objSession = alltelRequest.getSession();
String strPJVN = (String)objSession.getAttribute("DwOcHoIcE");
String strTypInd = "";
if (strPJVN == null) {  strPJVN=""; }
if ( strPJVN.equals("Bdp") )
{
        strTypInd = "X";
}
else
        strTypInd = "W";

DwoBean dwoBean = new DwoBean(strTypInd);

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Windstream Express</title>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body bgcolor="#ffffff" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<%--  Top Header --%>

<%
//added try block here as there was no try catch finally for db exceptions
// Antony - 09/17/2012

Log.write(Log.DEBUG_VERBOSE, "DwoFieldPrintView.jsp --- before try block. ");

try {

Connection conPrint = null;
Statement stmtPrint = null;

conPrint = DatabaseManager.getConnection();
dwoBean.getConnection();
dwoBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strDwoSqncNmbr = (String) request.getAttribute("DWO_SQNC_NMBR");
int m_iDwoSqncNmbr = Integer.parseInt(m_strDwoSqncNmbr);
String m_strDwoVrsn = (String) request.getAttribute("DWO_VRSN");
int m_iDwoVrsn = Integer.parseInt(m_strDwoVrsn);

// Verify user has access to view this form
if (! dwoBean.hasAccessTo(m_iDwoSqncNmbr))
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

String m_strDwoHdrQry = "SELECT DWO_STTS_CD, D.SRVC_TYP_CD, OCN_CD, SRVC_TYP_DSCRPTN FROM DWO_T D, SERVICE_TYPE_T S WHERE D.SRVC_TYP_CD = S.SRVC_TYP_CD AND S.TYP_IND = '" + strTypInd + "' AND DWO_SQNC_NMBR = " + m_iDwoSqncNmbr;

ResultSet rsDwoHdr = stmtPrint.executeQuery(m_strDwoHdrQry);
rsDwoHdr.next();
String m_strSrvcTypCd = rsDwoHdr.getString("SRVC_TYP_CD");
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=120>Data&nbsp;Work&nbsp;Order</th>
		<th align=left width=80>Status</th>
		<th align=left width=80>OCN</th>
		<th align=left width=100>Service&nbsp;Type</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=m_iDwoSqncNmbr%></td>
		<td align=left><%=rsDwoHdr.getString("DWO_STTS_CD")%></td>
		<td align=left><%=rsDwoHdr.getString("OCN_CD")%></td>
		<td align=left><%=rsDwoHdr.getString("SRVC_TYP_DSCRPTN")%></td>
	</tr>
</table>

<%
Log.write(Log.DEBUG_VERBOSE, "DwoFieldPrintView.jsp --- ");
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
// build a vector of all the fields used in all forms that are part of the dwo
int m_iFrmSqncNmbrEV = 0;
Vector m_vDwoFrmFld = new Vector();
Vector m_vFrmFld = new Vector();
FormField ff;
ResultSet rsErrVldQry = null;
String m_strErrVldQry = "";
boolean bMoreForms = true;

m_strErrVldQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM DWO_T, SERVICE_TYPE_FORM_T WHERE DWO_SQNC_NMBR = " + 
	m_iDwoSqncNmbr + " AND SERVICE_TYPE_FORM_T.SRVC_TYP_CD = '" + m_strSrvcTypCd + 
	"' AND SERVICE_TYPE_FORM_T.TYP_IND = '" + strTypInd + "'";
	
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
	m_vFrmFld = dwoBean.getFormFields(m_iFrmSqncNmbrEV, m_iDwoSqncNmbr, m_iDwoVrsn);

	for(int i=0 ; i < m_vFrmFld.size() ; i++)
	{
		ff = (FormField)m_vFrmFld.elementAt(i);
		m_vDwoFrmFld.addElement(ff);
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
int iDwoErrCnt = 0;

// Print out the info
for (int i=0 ; i < m_vDwoFrmFld.size() ; i++)
{
	ff = (FormField)m_vDwoFrmFld.elementAt(i);

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

dwoBean.closeConnection();

Log.write("DwoFieldPrintView.jsp ! Releasing connection object :"+conPrint.toString());

DatabaseManager.releaseConnection(conPrint);

Log.write("DwoFieldPrintView.jsp ! Released connection object.");

}//end of second try -- Added catch and finally blocks below -- Antony -- 09/17/2012
catch(Exception e) {
    Log.write("Exception in DwoFieldPrintView.jsp. Error Message :"+e.getMessage());
    
} /*finally { 
        
        if(!conPrint.isClosed()) {
            Log.write("Inside finally block in DwoFieldPrintView.jsp ! Releasing connection object :"+conPrint.toString());
        
            DatabaseManager.releaseConnection(conPrint);
        
            Log.write("Inside finally block in DwoFieldPrintView.jsp ! Released connection object.");
        }
}*/


%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

