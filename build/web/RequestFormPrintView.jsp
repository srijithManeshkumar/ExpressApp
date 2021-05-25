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
 * MODULE:	RequestFormPrintView.jsp	
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

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestFormPrintView.jsv  $
/*
/*   Rev 1.7   Mar 13 2003 17:14:52   e0069884
/* 
/*
/*   Rev 1.7   15 Feb 2003 00:10:00   dzasada 
/*
/*   Rev 1.6   09 Apr 2002 15:57:38   dmartz
/* 
/*
/*   Rev 1.5   26 Feb 2002 11:05:10   dmartz
/* 
/*
/*   Rev 1.4   21 Feb 2002 12:32:46   sedlak
/* 
/*
/*   Rev 1.3   13 Feb 2002 14:20:48   dmartz
/*Release 1.1
/*
*/
/** $Revision:   1.7  $
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
	Log.write(Log.ERROR, "Trapped in RequestFormPrintView.jsp");
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

<%
   

Log.write(Log.DEBUG_VERBOSE, "RequestFormPrintView.jsp --- ");

Connection conRqstFrm = null;
Statement stmtRqstFrm = null;
Statement stmtPrint = null;
int iReturnCode = 0;
ResultSet rsPrintInd = null;
ResultSet rsSrcFld = null;
ResultSet rsRqstHdr = null;
ResultSet rsPrintQry = null;
   
//fix for connection pool leak -- Antony -- 06102010

try {//enclosed whole jsp content inside a try


conRqstFrm = DatabaseManager.getConnection();
iReturnCode = requestBean.getConnection();
stmtRqstFrm = conRqstFrm.createStatement();
stmtPrint = conRqstFrm.createStatement();

requestBean.setUserid(sdm.getUser());

// Retrieve Parameters
String m_strRqstSqncNmbr = (String) request.getAttribute("RQST_SQNC_NMBR");
int m_iRqstSqncNmbr = Integer.parseInt(m_strRqstSqncNmbr);
String m_strRqstVrsn = (String) request.getAttribute("RQST_VRSN");
int m_iRqstVrsn = Integer.parseInt(m_strRqstVrsn);
String m_strRqstFormType = (String) request.getAttribute("rqstformtype");
String m_strFrmSqncNmbr = (String) request.getAttribute("FRM_SQNC_NMBR");
if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
{
	m_strFrmSqncNmbr = "0";
}
int m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr);

// Verify user has access to view this form
if (! requestBean.hasAccessTo(m_iRqstSqncNmbr))
{
	//fix for connection pool leak by Antony -- 06/08/2010
        iReturnCode = requestBean.closeConnection();
        stmtRqstFrm.close();
        stmtPrint.close();
        DatabaseManager.releaseConnection(conRqstFrm);
        
        alltelResponse.sendRedirect("LsrSecurity.jsp");
	return;
}

/* Code for Running Query for PRINT_IND on Table USERID_T */				/* dmz code */
String m_strPrintIndQry = "SELECT PRINT_IND FROM USERID_T WHERE USERID = '" + sdm.getUser() + "' ";
rsPrintInd = stmtPrint.executeQuery(m_strPrintIndQry);
rsPrintInd.next();
String m_strPrintInd = rsPrintInd.getString("PRINT_IND");
rsPrintInd.close();															/* dmz code */

String m_strRqstHdrQry = "SELECT A.RQST_SQNC_NMBR, A.RQST_STTS_CD, A.RQST_PON, A.SRVC_TYP_CD, A.ACTVTY_TYP_CD, A.RQST_VRSN, A.RQST_TYP_CD, B.SRVC_TYP_DSCRPTN, C.ACTVTY_TYP_DSCRPTN FROM REQUEST_T A, SERVICE_TYPE_T B, ACTIVITY_TYPE_T C WHERE A.RQST_SQNC_NMBR = " + m_iRqstSqncNmbr + " AND A.SRVC_TYP_CD = B.SRVC_TYP_CD AND A.ACTVTY_TYP_CD = C.ACTVTY_TYP_CD AND B.TYP_IND = 'R' AND C.TYP_IND = 'R'";

rsRqstHdr = stmtPrint.executeQuery(m_strRqstHdrQry);
rsRqstHdr.next();

int m_iCurrentRqstVrsn = rsRqstHdr.getInt("RQST_VRSN");
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
<BR CLEAR=ALL>
<HR>

<%
// Begin to create printable form
String m_strFldVl = "";


String m_strSrcFldQry = "";
String m_strSrcFldVl = "";

FieldValues fv = FieldValues.getInstance();
Vector m_vVV = new Vector();

%>

<%--
	Security Validation to determine if user can view or update on the FORM being displayed

	"FrmScrtyTg" determines access to update fields on the specific FORM we are looking at
	"ActnScrtyTgSave" determines if we have ability to make changes based on the Request Status
--%>

<%
Log.write(Log.DEBUG_VERBOSE, "RequestFormView.jsp --- m_strRqstFormType = " + m_strRqstFormType);

FormSection fs;
FormField ff;
boolean bMoreForms = true;

String m_strPrintQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM REQUEST_T, SERVICE_TYPE_FORM_T WHERE RQST_SQNC_NMBR = " +
	m_iRqstSqncNmbr + " AND REQUEST_T.SRVC_TYP_CD = SERVICE_TYPE_FORM_T.SRVC_TYP_CD AND SERVICE_TYPE_FORM_T.TYP_IND = 'R'";

rsPrintQry = stmtPrint.executeQuery(m_strPrintQry);
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
			rsSrcFld = stmtRqstFrm.executeQuery(m_strSrcFldQry);
				
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
	
	Vector m_vFrmFld = requestBean.getFormFields(m_iFrmSqncNmbr, m_iRqstSqncNmbr, m_iRqstVrsn);
	String strMdfdDt = requestBean.getMdfdDt();
	
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
			m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() + " WHERE RQST_SQNC_NMBR = " + m_iRqstSqncNmbr + " AND RQST_VRSN = " + m_iCurrentRqstVrsn;
				
			rsSrcFld = stmtRqstFrm.executeQuery(m_strSrcFldQry);
				
			if (rsSrcFld.next()==true) 	
			{
				strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());
			}
			rsSrcFld.close();
		  }
		  
		  if (ff.getSrcInd().equals("S")) 
		  {
			m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() + " WHERE RQST_SQNC_NMBR = " + m_iRqstSqncNmbr + " AND RQST_VRSN = " + m_iCurrentRqstVrsn + " AND FRM_SCTN_OCC = " + iSvFrmSctnOcc;
				
			rsSrcFld = stmtRqstFrm.executeQuery(m_strSrcFldQry);
				
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

   //Added rs and stmt close statements as they were never there before -- Antony -- 06102010
   rsRqstHdr.close();rsRqstHdr = null;
   rsPrintQry.close();rsPrintQry = null;
   stmtRqstFrm.close();stmtRqstFrm = null;
   stmtPrint.close();stmtPrint = null;
      
} catch(Exception e) {//end of first try -- Added catch and finally blocks below -- Antony -- 06102010
    Log.write("Exception in RequestFormPrintView.jsp. Error Message :"+e.getMessage());
    rsPrintInd.close();rsPrintInd = null;
    rsSrcFld.close();rsSrcFld = null;
    rsRqstHdr.close();rsRqstHdr = null;
    rsPrintQry.close();rsPrintQry = null;
    stmtRqstFrm.close();stmtRqstFrm = null;
    stmtPrint.close();stmtPrint = null;
   
    //we need to close here as because of the exception the close statement in code above may have been skipped
    
} finally {
        Log.write("Inside finally block in RequestFormPrintView.jsp ! Releasing connection object....");
        iReturnCode = requestBean.closeConnection();
        DatabaseManager.releaseConnection(conRqstFrm);

}

%>

<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestFormPrintView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 06:50:10   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.7  $
*/

%>
