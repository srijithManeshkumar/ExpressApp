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
 * MODULE:	DisputeFormPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *   03/15/2003 Initial Check-in
 *
 */

%>

<%@ page import ="com.alltel.lsr.common.objects.FormSection" %> 
<%@ page import ="com.alltel.lsr.common.objects.FormField" %> 
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
	Log.write(Log.ERROR, "Trapped in BillDisputeFormPrintView.jsp");
}
%>

<jsp:useBean id="billdisputeBean" scope="request" class="com.alltel.lsr.common.objects.BillDisputeBean" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Windstream Express</title>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body bgcolor="#ffffff" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<%
Log.write(Log.DEBUG_VERBOSE, "BillDisputeFormPrintView.jsp --- ");

Connection conDsptFrm = null;
Statement stmtDsptFrm = null;
Statement stmtPrint = null;

conDsptFrm = DatabaseManager.getConnection();
int iReturnCode = billdisputeBean.getConnection();
stmtDsptFrm = conDsptFrm.createStatement();
stmtPrint = conDsptFrm.createStatement();

billdisputeBean.setUserid(sdm.getUser());

// Retrieve Parameters
String m_strDsptSqncNmbr = (String) request.getAttribute("DSPT_SQNC_NMBR");
int m_iDsptSqncNmbr = Integer.parseInt(m_strDsptSqncNmbr);
String m_strDsptVrsn = (String) request.getAttribute("DSPT_VRSN");
int m_iDsptVrsn = Integer.parseInt(m_strDsptVrsn);
String m_strDsptFormType = (String) request.getAttribute("dsptformtype");
String m_strFrmSqncNmbr = (String) request.getAttribute("FRM_SQNC_NMBR");
if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
{
	m_strFrmSqncNmbr = "0";
}
int m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr);

// Verify user has access to view this form
if (! billdisputeBean.hasAccessTo(m_iDsptSqncNmbr))
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

String m_strDsptHdrQry = "SELECT DSPT_STTS_CD FROM DISPUTE_T WHERE DSPT_SQNC_NMBR = " + m_iDsptSqncNmbr;

ResultSet rsDsptHdr = stmtPrint.executeQuery(m_strDsptHdrQry);
rsDsptHdr.next();

int m_iCurrentDsptVrsn = 0;
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
	<tr>
		<th width=10>&nbsp;</th>
		<th align=left width=100>Dispute&nbsp;Number</th>
		<th align=left width=70>Status</th>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=left><%=m_iDsptSqncNmbr%></td>
		<td align=left><%=rsDsptHdr.getString("DSPT_STTS_CD")%></td>
	</tr>
</table>
<BR CLEAR=ALL>
<HR>

<%
// Begin to create printable form
String m_strFldVl = "";

ResultSet rsSrcFld;
String m_strSrcFldQry = "";
String m_strSrcFldVl = "";

FieldValues fv = FieldValues.getInstance();
Vector m_vVV = new Vector();

%>

<%--
	Security Validation to determine if user can view or update on the FORM being displayed

	"FrmScrtyTg" determines access to update fields on the specific FORM we are looking at
	"ActnScrtyTgSave" determines if we have ability to make changes based on the Dispute Status
--%>

<%
Log.write(Log.DEBUG_VERBOSE, "BillDisputeFormView.jsp --- m_strDsptFormType = " + m_strDsptFormType);

FormSection fs;
FormField ff;
boolean bMoreForms = true;

String m_strPrintQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM DISPUTE_T, SERVICE_TYPE_FORM_T WHERE DSPT_SQNC_NMBR = " +
	m_iDsptSqncNmbr + " AND SERVICE_TYPE_FORM_T.SRVC_TYP_CD = '3' AND SERVICE_TYPE_FORM_T.TYP_IND = 'B'";

ResultSet rsPrintQry = stmtPrint.executeQuery(m_strPrintQry);
if (rsPrintQry.next())
{
	m_iFrmSqncNmbr = rsPrintQry.getInt("FRM_SQNC_NMBR");
}
else
{
	bMoreForms = false;
}

while (bMoreForms)
{
%>
	<table align=left width=100% border=0 cellspacing=1 cellpadding=0>
	  <tr>
	    <td>
	      <table align=left  border=2 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
	        <tr>
			<%
			// Retrieve FRM_CD for this form
			m_strSrcFldQry = "SELECT FRM_CD FROM FORM_T WHERE FRM_SQNC_NMBR = " + m_iFrmSqncNmbr;
			rsSrcFld = stmtDsptFrm.executeQuery(m_strSrcFldQry);
				
			if (rsSrcFld.next()==true) 	
			{
			%>
				<td><b>Form: <%= rsSrcFld.getString("FRM_CD") %></b></td>
			<%
			}
			%>
	        </tr>
	      </table>
	    </td>
	  </tr>
	</table>
	
	<%-- 
		Obtain Form Sections
	--%>

	<%
	Forms f = Forms.getInstance();
	Vector m_vFrmSctn = f.getFormSections(m_iFrmSqncNmbr);
	%>
	
	<%-- 
		Get a vector with all the form detail and loop through the FormField Objects
		to build a View of the Form and build Section Headers as needed.
	--%>
	
	<%
	
	Vector m_vFrmFld = billdisputeBean.getFormFields(m_iFrmSqncNmbr, m_iDsptSqncNmbr, m_iDsptVrsn);
	String strMdfdDt = billdisputeBean.getMdfdDt();
	
	int iSvFrmSctnSqncNmbr = 0;
	String strSvFrmSctnDscrptn = "";
	int iFrmSctnVectorIndex =0;
	int iSvFrmSctnOcc = 0;
	String strSvFrmSctnRptInd = "";
	int iCharacterCount = 0;
	int iMaxCharactersPerLine = 120;
	int iFldBuffer = 4;
	int iDsplyFldLngth = 0;
	int iDsplyFldWidth = 0;
	String strFldData = "";
	String strFldDataTypDesc = "";
	
	for(int ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++)
	{
	  ff = (FormField)m_vFrmFld.elementAt(ff_idx);
	  strFldData = ff.getFieldData();
	
	    if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) || (ff.getFrmSctnOcc() != iSvFrmSctnOcc))
	    {
	
			if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) && strSvFrmSctnRptInd.equals("Y"))
			{
	
	%>
			<%--	PRINT OUT A BAR LINE THAT LETS USER ADD AN ADDITIONAL SECTION OCCURRENCE  --%>
			<%--	THIS IS ONLY FOR REPEATABLE SECTIONS  --%>
	
			
				<BR CLEAR=ALL>
				<TABLE ALIGN="left" WIDTH=100% border=0 cellspacing=0 cellpadding=0>
				<TR>
				<TH width=400 align=left bgcolor="#7AABDE" >
				<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
				<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
				</TH>
				</TH>
				</TR>
				</TABLE>
				<BR CLEAR=ALL>
	<%
			}
	
			iSvFrmSctnSqncNmbr = ff.getFrmSctnSqncNmbr();
			iSvFrmSctnOcc = ff.getFrmSctnOcc();
	
			iFrmSctnVectorIndex = iSvFrmSctnSqncNmbr - 1;
			fs = (FormSection)m_vFrmSctn.elementAt(iFrmSctnVectorIndex);
	
			strSvFrmSctnDscrptn = fs.getFrmSctnDscrptn();
			strSvFrmSctnRptInd = fs.getFrmSctnRptInd();
			iCharacterCount = 0;
	
	%>
			<%--	PRINT OUT A HEADER FOR THE SECTION     --%>
	
<%                      if ((! strSvFrmSctnRptInd.equals("H")) || (iSvFrmSctnOcc == 1))
			{
%>
				<BR CLEAR=ALL>
				<TABLE ALIGN="left" WIDTH=100% border=0 cellspacing=0 cellpadding=0>
				<TR>
					<TH width=400 align=left bgcolor="#7AABDE" >
					<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
					<SPAN class="barheader"><%=strSvFrmSctnDscrptn%>&nbsp;<%if (iSvFrmSctnOcc > 0) {%> (<%=iSvFrmSctnOcc%>)<%}%></SPAN>
					</TH>
					<TH width=200 align=left bgcolor="#7AABDE" >
					<SPAN class="barheader">&nbsp;</SPAN>
					</TH>
				</TR>
				</TABLE>
<%			}
%>
			<BR CLEAR=ALL>
	<%
	    }

	if (m_strPrintInd.equals("Y") || (m_strPrintInd.equals("N") && (ff.getFieldData() != null)))	
	{																						/* dmz code */
	
		iDsplyFldWidth = iFldBuffer;
	
		if (ff.getFldCd().length() > ff.getFldDsplySz())
		{
			iDsplyFldWidth = iDsplyFldWidth + ff.getFldCd().length();
		}
		else
		{
			iDsplyFldWidth = iDsplyFldWidth + ff.getFldDsplySz();
		}
	
		if (iCharacterCount != 0)
		{
			if (((iCharacterCount + iDsplyFldWidth) > iMaxCharactersPerLine) || ((ff.getFldDsplyActns() != null) && (ff.getFldDsplyActns().indexOf("b") >= 0)))
			{
				iCharacterCount = 0;  %><BR CLEAR=ALL><%
			}
		}
	
		iCharacterCount = iCharacterCount + iDsplyFldWidth;
	
		if (ff.getFldDsplySz() > iMaxCharactersPerLine)
		{
			iDsplyFldLngth = iMaxCharactersPerLine;
		}
		else
		{
			iDsplyFldLngth = ff.getFldDsplySz();
		}
	
		if ((ff.getFldDataTyp().equals("X")) || (ff.getFldDataTyp().equals("S")))
		{
			strFldDataTypDesc = "Alphanumeric";
		}
		else
		{
			if (ff.getFldDataTyp().equals("A"))
			{
				strFldDataTypDesc = "Alpha";
			}
			else
			{
				if (ff.getFldDataTyp().equals("N"))
				{
					strFldDataTypDesc = "Numeric";
				}
			}
		}
		%>
	
		<TABLE align=left border=0 cellspacing=2 cellpadding=1>
		<TR><TD align=left><FONT STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();" ONMOUSEOVER="showpopupmsg('<%=ff.getFldCd()%> -- <%=ff.getFrmFldNmbr()%> |<%=ff.getFldDscrptn()%>|Type: <%=strFldDataTypDesc%>|Length: <%=ff.getFldLngth()%><%if (ff.getFldFrmtMsk() != null && ff.getFldFrmtMsk().length()>0){%>|Format: <%=ff.getFldFrmtMsk()%><%}%>');"><%=ff.getFldCd()%></FONT></TD></TR>
	
		<TR><TD>
	<%	
	
		if (ff.getFldDsplyTyp().equals("TEXTAREA"))
		{
	%>
			<%-- need to handle a readonly text area by making it a TABLE--%>
			
			<TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY")) {%> class="readonly"<%}%> NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="3" COLS="<%=iMaxCharactersPerLine - 10%>" WRAP><%=strFldData%></TEXTAREA>
	<%
		}
		else
		{
		  if (ff.getSrcInd().equals("R")) 
		  {
			m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() + " WHERE DSPT_SQNC_NMBR = " + m_iDsptSqncNmbr + " AND DSPT_VRSN = " + m_iCurrentDsptVrsn;

				
			rsSrcFld = stmtDsptFrm.executeQuery(m_strSrcFldQry);
				
			if (rsSrcFld.next()==true) 	
			{
				strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());
	
			}
			rsSrcFld.close();
		  }
		  
		  if (ff.getSrcInd().equals("S")) 
		  {

			m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() + " WHERE DSPT_SQNC_NMBR = " + m_iDsptSqncNmbr + " AND DSPT_VRSN = " + m_iCurrentDsptVrsn;  

			/* AND DSPT_VRSN = " + m_iCurrentDsptVrsn + " AND FRM_SCTN_OCC = " + iSvFrmSctnOcc; */

				
			rsSrcFld = stmtDsptFrm.executeQuery(m_strSrcFldQry);
				
			if (rsSrcFld.next()==true) 	
			{
				strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());
	
			}
			rsSrcFld.close();
		  }
		  
	%>
		<INPUT class="readonly" TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=strFldData%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" READONLY>
	<%	  
		}
	%>
		</TD></TR>
	
		</TABLE>
	
	<%
		}																					/* dmz code */
	}

	if (strSvFrmSctnRptInd.equals("Y"))
	{

	%>
			<%--	PRINT OUT A BAR LINE THAT LETS USER ADD AN ADDITIONAL SECTION OCCURRENCE  --%>
			<%--	THIS IS ONLY FOR REPEATABLE SECTIONS  --%>
	
				<BR CLEAR=ALL>
				<TABLE ALIGN="left" WIDTH=100% border=0 cellspacing=0 cellpadding=0>
				<TR>
				<TH width=400 align=left bgcolor="#7AABDE" >
				<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
				<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
				</TH>
				</TR>
				</TABLE>
				<BR CLEAR=ALL>
	<%
	}
	%>
	
	<BR CLEAR=ALL>
	<TABLE ALIGN="left" WIDTH=100% border=0 cellspacing=0 cellpadding=0>
	<TR>
		<TH width=400 align=left bgcolor="#7AABDE" >
		<SPAN class="barheader">&nbsp;End of Form</SPAN>
		</TH>
		<TH width=200 align=left bgcolor="#7AABDE" >
		<SPAN class="barheader">&nbsp;</SPAN>
		</TH>
	</TR>
	</TABLE>
	
	<BR CLEAR=ALL>

<%	if (rsPrintQry.next())
	{
		m_iFrmSqncNmbr = rsPrintQry.getInt("FRM_SQNC_NMBR");

	}
	else
	{
		bMoreForms = false;
	}
}

iReturnCode = billdisputeBean.closeConnection();
DatabaseManager.releaseConnection(conDsptFrm);
%>

<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
