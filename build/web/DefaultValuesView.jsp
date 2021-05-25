<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2003
 *                              BY
 *                      Windstream COMMUNICATIONS INC.
 */
/**
 * MODULE:      DefaultValuesView.jsp
 *
 * DESCRIPTION: Display form sections to populate default values for users
 *
 * AUTHOR:
 *
 * DATE:        
 *
 * HISTORY:
 *      03/20/2002 Initial Check-in
 *      01/27/2004 pjs  added code to limit forms by type
 *
 */
%>

<%@ include file="i_header.jsp" %>

<%@ page import ="com.alltel.lsr.common.objects.FormSection" %> 
<%@ page import ="com.alltel.lsr.common.objects.FormField" %> 

<FORM NAME="DefaultValuesForm" METHOD=POST ACTION="DefaultValuesCtlr">

<table align=center width="100%">
<tr>
	<TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Default&nbsp;User&nbsp;Values</SPAN>
	</th>
</tr>
</table>
<br clear=ALL>

<table border=0 cellpadding=2  align=center>
<tr>
	<td><INPUT class=appButton TYPE="BUTTON" name="action" value="Cancel" onClick="return submitForm(this);"></td>
	<td><INPUT class=appButton TYPE="RESET" name="action" value="Reset"></td>
	<td><INPUT class=appButton TYPE="BUTTON" name="action" value="Submit" onClick="return submitForm(this);"></td>
</tr>
</table>

<%

Log.write(Log.DEBUG_VERBOSE, "DefaultValuesView.jsp --- ");
final String INDIRECT_AGENT = "INDIRECT_DSL_AGT";

Connection conDfltVls = null;
Statement stmtDfltVls = null;
Statement stmtDfltVls2 = null;

conDfltVls = DatabaseManager.getConnection();
stmtDfltVls = conDfltVls.createStatement();
stmtDfltVls2 = conDfltVls.createStatement();
ResultSet rs;
ResultSet rs2;
ResultSet rsSrcFld;

String m_strFldVl = "";
String m_strSrcFldQry = "";

FieldValues fv = FieldValues.getInstance();
Vector m_vVV = new Vector();
Vector m_vFrmFld = new Vector();	

FormSection fs;
FormField ff;

String m_strQry = "";
int iFrmSqncNmbr = 0;
int iSvFrmSctnSqncNmbr = 0;
boolean bMoreForms = true;

// Get Company Type
String strCmpySqncNmbr = sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr();
rs = stmtDfltVls.executeQuery("SELECT CMPNY_TYP FROM COMPANY_T WHERE CMPNY_SQNC_NMBR="+strCmpySqncNmbr);
rs.next();
String strCmpyTyp = rs.getString(1);
rs.close();
rs=null;
Log.write(Log.DEBUG_VERBOSE,"DefaultValuesView.jsp CompanyType=" + strCmpyTyp);


//Build WHERE clause based on Company Type - this is cheezy, but a quick solution for now....
// We just want to show forms that make sense to the user type
if (strCmpyTyp.equals("D"))
{	String strQuery = "SELECT S.SRVC_TYP_CD FROM SERVICE_TYPE_T S WHERE S.TYP_IND='D' AND S.SRVC_TYP_DSCRPTN IN " +
                        "   (SELECT DISTINCT SO.SCRTY_OBJCT_CD " +
                        "    FROM USERID_T U, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA, SECURITY_OBJECT_T SO  "+
                        "    WHERE U.USERID='" + sdm.getUser() + "' AND U.USERID=UGA.USERID AND UGA.SCRTY_GRP_CD=sga.SCRTY_GRP_CD "+
                        "    AND SGA.SCRTY_OBJCT_CD=SO.SCRTY_OBJCT_CD) ";
	String strSrvcTypCd="";
	try
	{	rs = stmtDfltVls.executeQuery(strQuery);
		if (rs.next())
		{       strSrvcTypCd = rs.getString("SRVC_TYP_CD");
		}
		else
		{	strSrvcTypCd="5";//default
			Log.write(Log.DEBUG_VERBOSE,"DefaultValuesView.jsp defaulting srv type to '5'");
		}
		rs.close();
		rs=null;
		if (strSrvcTypCd == null)	strSrvcTypCd="5";//default
	}
	catch (Exception e)
	{	Log.write(Log.ERROR,"DefaultValuesView.jsp exception caught");
	}

	m_strQry =  " SELECT DISTINCT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE TYP_IND='D' and SRVC_TYP_CD ='" + strSrvcTypCd +"' ";
}
else if (strCmpyTyp.equals("W"))
{	m_strQry =  " SELECT DISTINCT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE TYP_IND IN ('W','S') ";
}
else 
{	m_strQry =  " SELECT DISTINCT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE TYP_IND IN ('P','R','T','B','X') ";
}


// Get all the Forms
//m_strQry = "SELECT DISTINCT FRM_SQNC_NMBR FROM FORM_T;
rs = stmtDfltVls.executeQuery(m_strQry);

// Loop thru each Form.  Get it's fields.  Build the FormField object.  Add FormField object to Vector.
while (rs.next())
{
	// Get the Form Sequence Number to be processed
	iFrmSqncNmbr = rs.getInt("FRM_SQNC_NMBR");

	// Obtain all fields for this form
	String fieldqry = 
	"SELECT * FROM FORM_FIELD_T F, DEFAULT_USERID_T D " +
	"WHERE F.FRM_SQNC_NMBR = " + iFrmSqncNmbr + 
	" AND F.FRM_SCTN_SQNC_NMBR IN " +
		"(SELECT FRM_SCTN_SQNC_NMBR FROM FORM_SECTION_T WHERE FRM_SQNC_NMBR = " + iFrmSqncNmbr + " AND SRC_IND = 'D') " + 
	" AND F.FRM_SQNC_NMBR = D.FRM_SQNC_NMBR(+)" +
	" AND F.FRM_SCTN_SQNC_NMBR = D.FRM_SCTN_SQNC_NMBR(+)" +
	" AND F.FRM_FLD_NMBR = D.FRM_FLD_NMBR(+)" +
	" AND D.USERID(+) = '" + sdm.getUser() + "'" +
	" ORDER BY F.FRM_SCTN_SQNC_NMBR, F.FRM_FLD_SRT_SQNC";

	rs2 = stmtDfltVls2.executeQuery(fieldqry);
	while(rs2.next())
	{
		int mfrmseqnumber = rs2.getInt("FRM_SQNC_NMBR");
		int mfrmsectnseq = rs2.getInt("FRM_SCTN_SQNC_NMBR");
		int mfrmfieldsort = rs2.getInt("FRM_FLD_SRT_SQNC");
		String mfrmfieldnum = rs2.getString("FRM_FLD_NMBR");
		String mfrmfieldcd = rs2.getString("FLD_CD");
		String mfrmfielddatatyp = rs2.getString("FLD_DATA_TYP");
		int mfieldlength = rs2.getInt("FLD_LNGTH");
		int mfrmfielddsplysz = rs2.getInt("FLD_DSPLY_SZ");
		String mfrmfielddsplyactns = rs2.getString("FLD_DSPLY_ACTNS");
		String mfrmfielddsplytyp = rs2.getString("FLD_DSPLY_TYP");
		String mfrmfieldfrmtmsk = rs2.getString("FLD_FRMT_MSK");
		int mfrmfieldvlssqncnmbr = rs2.getInt("FLD_VLS_SQNC_NMBR");
		String mfielddescr = rs2.getString("FLD_DSCRPTN");
		String mfieldcolnm = rs2.getString("DB_CLNM_NM");
		String mfieldsrcind = rs2.getString("SRC_IND");
		String mfieldMoxArray = rs2.getString("MOX_ARR");
		String mfieldIMoxArray = rs2.getString("IMOX_ARR");
		String mfieldsrcdbtblnm = rs2.getString("SRC_DB_TBL_NM");
		String mfieldsrcdbclmnnm = rs2.getString("SRC_DB_CLMN_NM");
		String mfielddata = rs2.getString("DFLT_VL");
		String mje = rs2.getString("JSCRPT_EVNT");

		FormField fieldnode = new FormField(mfrmseqnumber, mfrmsectnseq, 0, mfrmfieldsort, mfrmfieldnum, mfrmfieldcd, mfrmfielddatatyp, mfieldlength, mfrmfielddsplysz, mfrmfielddsplyactns, mfrmfielddsplytyp, mfrmfieldfrmtmsk, mfrmfieldvlssqncnmbr, mfielddescr, 0, 0, "", "", mfieldcolnm, mfieldsrcind, mfieldsrcdbtblnm, mfieldsrcdbclmnnm, mfieldMoxArray, mfieldIMoxArray,mfielddata, mje);
		m_vFrmFld.addElement(fieldnode);
	} 
}
%>

<%-- 
	=============================================================================
	Using the prepopulated Vector of FormField classes (m_vFrmFld) the View will 
	be built and formatted.
	=============================================================================
	This portion of the jsp is reused.
	=============================================================================
--%>

<%
String strSvFrmSctnDscrptn = "";
int iFrmSctnVectorIndex = 0;
String strSvFrmSctnRptInd = "";
int iCharacterCount = 0;
int iMaxCharactersPerLine = 120;
int iFldBuffer = 4;
int iDsplyFldLngth = 0;
int iDsplyFldWidth = 0;
String strFldData = "";
String strFldDataTypDesc = "";
Vector m_vFrmSctn = new Vector();
boolean bFrmChg = false;

for(int ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++)
{
	ff = (FormField)m_vFrmFld.elementAt(ff_idx);
	strFldData = ff.getFieldData();

	Forms f = Forms.getInstance();

	// If we hit a new form, print out the header
	if (ff.getFrmSqncNmbr() != iFrmSqncNmbr)
	{
		bFrmChg = true;

		iFrmSqncNmbr = ff.getFrmSqncNmbr();

		// Get Form Section
		m_vFrmSctn = f.getFormSections(iFrmSqncNmbr);
	}

	if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) || bFrmChg)
	{
		bFrmChg = false;

		iSvFrmSctnSqncNmbr = ff.getFrmSctnSqncNmbr();

		iFrmSctnVectorIndex = iSvFrmSctnSqncNmbr - 1;
		fs = (FormSection)m_vFrmSctn.elementAt(iFrmSctnVectorIndex);

		strSvFrmSctnDscrptn = fs.getFrmSctnDscrptn();
		strSvFrmSctnRptInd = fs.getFrmSctnRptInd();
		iCharacterCount = 0;

		// Retrieve FRM_CD for this form
		m_strSrcFldQry = "SELECT FRM_CD FROM FORM_T WHERE FRM_SQNC_NMBR = " + iFrmSqncNmbr;
		rs = stmtDfltVls.executeQuery(m_strSrcFldQry);
		rs.next();

%>
		<%--	PRINT OUT A HEADER FOR THE SECTION     --%>

		<BR CLEAR=ALL>
		<TABLE ALIGN="left" WIDTH=100% border=0 cellspacing=0 cellpadding=0>
		<TR>
			<TH width=400 align=left bgcolor="#7AABDE" >
			<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
			<SPAN class="barheader"><%= rs.getString("FRM_CD") %>&nbsp;:&nbsp;<%=strSvFrmSctnDscrptn%></SPAN>
			</TH>
			<TH width=200 align=left bgcolor="#7AABDE" >
			<SPAN class="barheader">&nbsp;</SPAN>
			</TH>
			<TH align=right bgcolor="#7AABDE" >
			<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
			</TH>
		</TR>
		</TABLE>
		<BR CLEAR=ALL>
<%
	}

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
		
		<TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY")) {%> class="readonly"<%}%> NAME="_FF_<%=iFrmSqncNmbr%>_<%=ff.getFrmSctnSqncNmbr()%>_<%=ff.getFrmFldNmbr()%>" ROWS="3" COLS="<%=iMaxCharactersPerLine - 10%>" WRAP><%=strFldData%></TEXTAREA>
<%
	}
	else
	{
		if (ff.getFldDsplyTyp().equals("READONLY"))
		{
%>
			<INPUT class="readonly" TYPE="text" NAME="_FF_<%=iFrmSqncNmbr%>_<%=ff.getFrmSctnSqncNmbr()%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=strFldData%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" READONLY>
<%	  
		}
		else
		{
			if (ff.getFldDsplyTyp().equals("INPUT"))
			{
%>
				<INPUT TYPE="text" NAME="_FF_<%=iFrmSqncNmbr%>_<%=ff.getFrmSctnSqncNmbr()%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=strFldData%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>">
<%
			}
			else
			{
				if (ff.getFldDsplyTyp().equals("SELECT"))
				{
%>
					<SELECT  NAME="_FF_<%=iFrmSqncNmbr%>_<%=ff.getFrmSctnSqncNmbr()%>_<%=ff.getFrmFldNmbr()%>" SIZE="1">
						<OPTION VALUE=""></OPTION>
<%
					m_vVV = fv.getValidValues( ff.getFldVlsSqncNmbr() );	//Vector of valid values for this field
					for (int i=0; i < m_vVV.size(); i++)
					{
						m_strFldVl = (String)m_vVV.elementAt(i);
%>
						<OPTION VALUE="<%=m_strFldVl%>" <% if (m_strFldVl.equals(strFldData)) {%> SELECTED<%}%>><%=m_strFldVl%></OPTION>
<%
					}
%>
					</SELECT>
<%
				}
			}
		}
	}
%>
	</TD></TR>
	</TABLE>
<%
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
		<A NAME="<%=iFrmSqncNmbr%>">&nbsp;</A>
		<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
		</TH>
		<TH width=200 align=left bgcolor="#7AABDE" >
		<SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="rqst_add_sctn_<%=iFrmSqncNmbr%>" VALUE="Add Section"></SPAN>
		</TH>
		<TH align=right bgcolor="#7AABDE" >
		<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
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
	<TH align=right bgcolor="#7AABDE" >
	<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
	</TH>
</TR>
</TABLE>
<BR CLEAR=ALL>
</FORM>

<%
DatabaseManager.releaseConnection(conDfltVls);
%>

<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/DefaultValuesView.jsv  $
/*
/*   Rev 1.3   19 Feb 2002 10:21:48   dmartz
/* 
/*
/*   Rev 1.2   31 Jan 2002 14:20:04   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 06:57:36   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.3  $
*/
%>
