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
 * MODULE:	ExpressFormView.jsp	
 * 
 * DESCRIPTION: Generic form view
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        09-29-2003
 * 
 * HISTORY:
 *	3/29/2004 pjs "D" datatype - if populated and READ-ONLY -dont update with current date
 *	4/27/2004 pjs Added "O" datatype - same as "R", but only set "O"nce. So if populated, doesnt 
 *			get reset.
 */

%>

<%@ page import ="com.alltel.lsr.common.objects.FormSection" %> 
<%@ page import ="com.alltel.lsr.common.objects.FormField" %> 

<%

Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- ");

int iReturnCode = myorderBean.getConnection();
myorderBean.setUserid(sdm.getUser());

// Verify user has access to view this form
if (! myorderBean.hasAccessTo(m_iSqncNmbr))
{
	alltelResponse.sendRedirect("LsrSecurity.jsp");
	return;
}

Connection connFrm = null;
Statement stmtFrm = null;
ResultSet rsSrcFld = null;

try {

connFrm = DatabaseManager.getConnection();
stmtFrm = connFrm.createStatement();

String m_strFldVl = "";

String m_strSrcFldQry = "";
String m_strSrcFldVl = "";

FieldValues fv = FieldValues.getInstance();
Vector m_vVV = new Vector();
String m_strNewrec = (String) request.getAttribute("NEWRECORD");
%>

<%--
	Security Validation to determine if user can view or update on the FORM being displayed

	"m_strFrmScrtyTg" determines access to update fields on the specific FORM we are looking at.
	"m_strActnScrtyTgSave" determines if we have ability to make changes based on the Status.

NOTE: If an order is locked by another user, i_xxxxHeader.jsp will change these security tags
to make the form/actions READONLY.

--%>

<%
Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strFrmScrtyTg = " + m_strFrmScrtyTg);
Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strActnScrtyTgSave = " + m_strActnScrtyTgSave);

boolean bUserIsAuthorized;
boolean bUserIsNotAuthorized;

if (sdm.isAuthorized(m_strFrmScrtyTg) && sdm.isAuthorized(m_strActnScrtyTgSave))
{
	bUserIsAuthorized = true;
	bUserIsNotAuthorized = false;
%>
	<INPUT TYPE="HIDDEN" NAME="FRM_AUTHORIZATION" VALUE="AUTHORIZED">
<%
}
else
{
	bUserIsAuthorized = false;
	bUserIsNotAuthorized = true;
}

FormSection fs;
FormField ff;

%>

<INPUT TYPE="HIDDEN" NAME="FRM_SQNC_NMBR" VALUE="<%=m_iFrmSqncNmbr%>">
<INPUT TYPE="HIDDEN" NAME="NEWRECORD" VALUE="<%=m_strNewrec%>" >
<table align=left width="100%" border=0 cellspacing=1 cellpadding=0>
  <tr>
    <td>
      <table align=left  border=2 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
        <tr>

		<%-- find out what the FORM SEQUENCE NUMBER is that we are building a form for and --%> 
		<%-- then build this FORM SECTION HEADER list dynamically  --%> 

		<%
		Forms f = Forms.getInstance();
		Vector m_vFrmSctn = f.getFormSections(m_iFrmSqncNmbr);

		//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- 2 m_vFrmSctn.size()=" +m_vFrmSctn.size());
		for(int fs_idx = 0; fs_idx < m_vFrmSctn.size(); fs_idx++)
		{
			//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp ---  m_iFrmSqncNmbr=" + m_iFrmSqncNmbr + " fs_idx=" + fs_idx);
			fs = (FormSection)m_vFrmSctn.elementAt(fs_idx);
		
		%>
			<td><A HREF="#<%=fs.getFrmSctnSqncNmbr()%>">&nbsp;<%=fs.getFrmSctnDscrptn()%>&nbsp;</A></td>
		
		<%
		}
		%>

        </tr>
      </table>
    </td>
	
		
	<td>
		<%--  put javascript function here to print -OR- open new window with printable data --%> 
		  &nbsp;
    </td>
  </tr>
</table>

<%-- 
	Get a vector with all the form detail and loop through the FormField Objects
	to build a View of the Form and build Section Headers as needed.
--%>

<%

//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- 3");
Vector m_vFrmFld = myorderBean.getFormFields(m_iFrmSqncNmbr, m_iSqncNmbr, m_iVrsn);
//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- 4");

String strMdfdDt = myorderBean.getMdfdDt();
iReturnCode = myorderBean.closeConnection();

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
boolean bFirstTime = true;
String strFocusField = "";	 //NOTE this will be overridden if user just added a new section

//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- 5");
for(int ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++)
{
  ff = (FormField)m_vFrmFld.elementAt(ff_idx);
  strFldData = ff.getFieldData();

  if (bUserIsNotAuthorized && (strFldData == null || strFldData.length() == 0) )
  {
	// suppress showing the field on the form
  }
  else
  {
    // Capture first NON-READONLY field, so that we can place focus there
    if (bFirstTime == true && ff.getFldDsplyTyp() != null && !ff.getFldDsplyTyp().equals("READONLY"))
    {
        if (m_iNewSection > 0)  //User just added a new section/occ, so put cursor there.
        {
                if ( (m_iNewSection == ff.getFrmSctnSqncNmbr()) && (m_iNewOcc == ff.getFrmSctnOcc() ) )
                {       strFocusField = "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldNmbr(
);
                        bFirstTime = false;
                }
        }
        else
        {
                strFocusField = "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldNmbr();
                bFirstTime = false;
        }
    }

    if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) || (ff.getFrmSctnOcc() != iSvFrmSctnOcc))
    {

		if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) && strSvFrmSctnRptInd.equals("Y") && bUserIsAuthorized)
		{

%>
		<%--	PRINT OUT A BAR LINE THAT LETS USER ADD AN ADDITIONAL SECTION OCCURRENCE  --%>
		<%--	THIS IS ONLY FOR REPEATABLE SECTIONS  --%>

		
			<BR CLEAR=ALL>
			<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
			<TR>
			<TH width=400 align=left bgcolor="#7AABDE" >
			<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
			<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
			</TH>
			<TH width=200 align=left bgcolor="#7AABDE" >
			<SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
			</TH>
			<TH align=right bgcolor="#7AABDE" >
			<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
			</TH>
			</TR>
			</TABLE>
			<BR CLEAR=ALL>
<%
		}
		// On certain "H" type repeating sections, we wish to show the "Add Section" button. It's drive by existance of lsr.properties value
		if ( strSvFrmSctnRptInd.equals("H") && bUserIsAuthorized )
		{	if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr))
			{	String strProp = "lsr.show.addsection." + m_iFrmSqncNmbr +"."+ iSvFrmSctnSqncNmbr;
				Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- strProp = " + strProp);
				if ( (PropertiesManager.getProperty(strProp, "false")).equals("true") )
				{	//show "Add Section"
					Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- strProp = " + strProp + " is true - so show add section");
            
%>
			<BR CLEAR=ALL>
			<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
			<TR>
			<TH width=400 align=left bgcolor="#7AABDE" >
			<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
			<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
			</TH>
			<TH width=200 align=left bgcolor="#7AABDE" >
			<SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
			</TH>
			<TH align=right bgcolor="#7AABDE" >
			<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
			</TH>
			</TR>
			</TABLE>
			<BR CLEAR=ALL>
<%
				}
			}
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

<%			// "H" represents a repeating section that has it's individual
			//     section headers hidden.
			if ((! strSvFrmSctnRptInd.equals("H")) || (iSvFrmSctnOcc == 1))
			{
%>
				<BR CLEAR=ALL>
				<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
				<TR>
					<TH width=400 align=left bgcolor="#7AABDE" >
					<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
					<SPAN class="barheader"><%=strSvFrmSctnDscrptn%>&nbsp;<%if (iSvFrmSctnOcc > 0 && (! strSvFrmSctnRptInd.equals("H"))) {%> (<%=iSvFrmSctnOcc%>)<%}%></SPAN>
				</TH>
<%			}

			if (strSvFrmSctnRptInd.equals("Y") && (iSvFrmSctnOcc > 1) && bUserIsAuthorized)
			{
%>
					<TH width=200 align=left bgcolor="#7AABDE" >
					<SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="del_sctn_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>" VALUE="Delete Section"></SPAN>
					</TH>
<%
			}
			else
			{
%>
					<TH width=200 align=left bgcolor="#7AABDE" >
					<SPAN class="barheader">&nbsp;</SPAN>
					</TH>
<%
			}

			if ((! strSvFrmSctnRptInd.equals("H")) || (iSvFrmSctnOcc == 1))
			{
%>
					<TH align=right bgcolor="#7AABDE" >
					<A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
					</TH>
				</TR>
				</TABLE>
<%			}
			if (strSvFrmSctnRptInd.equals("H"))
			{	// Put Section # on page
%>
				<BR CLEAR=ALL>
				<TABLE align=left border=0 cellspacing=2 cellpadding=1><TR>
				<TD align=left><%=iSvFrmSctnOcc%>.&nbsp;</TD></TR>
				</TABLE>
<%			
			}
			else
			{
%>
				<BR CLEAR=ALL>
<%
			}
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
			//If "H" type and new line, then bump over to the right to help things line up better...
			if (strSvFrmSctnRptInd.equals("H"))
			{
				String strTemp = iSvFrmSctnOcc + ". ";
				iCharacterCount = strTemp.length();
%>				<TABLE align=left border=0 cellspacing=2 cellpadding=1><TR><TD>
<%				for (int i=0; i < iCharacterCount; i++)
				{
%>					&nbsp;
<%
				}
%>					</TD></TR></TABLE> <%
			}

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
                        else
                        {
                            if (ff.getFldDataTyp().equals("D"))
                            {
                                strFldDataTypDesc = "Date (system generated)";
                            }
			    else
			    {
				if (ff.getFldDataTyp().equals("C"))
				{
                                	strFldDataTypDesc = "Checkbox";
				}
			    }
                        }
		}
	}
	%>

	<TABLE align=left border=0 cellspacing=2 cellpadding=1>
	<TR><TD align=left><FONT STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();" ONMOUSEOVER="showpopupmsg('<%=ff.getFldCd()%> -- <%=ff.getFrmFldNmbr()%> |<%=ff.getFldDscrptn()%>|Type: <%=strFldDataTypDesc%>|Length: <%=ff.getFldLngth()%><%if (ff.getFldFrmtMsk() != null && ff.getFldFrmtMsk().length()>0){%>|Format: <%=ff.getFldFrmtMsk()%><%}%>');">
	<%
		// Business Data Products Request for special message on expdite check box. 
		//HD
		if ( ( ff.getFrmSqncNmbr() == 824 ) && (ff.getFldDscrptn().equals("Expedite"))   ){
	%><%=ff.getFldCd()%><span class="expdt">&nbsp;&nbsp;(To Request an Expedite, you must send an email with Management Approval to the Data Engineering and DSTAC management)</span><br></FONT></TD></TR>
	<%} else {%>
	 <%=ff.getFldCd()%></FONT></TD></TR>
	<%
	}
	%>
	<TR><TD <% if (bUserIsAuthorized && !ff.getFldDsplyTyp().equals("READONLY") && (ff.getMoxArray() != null && ff.getMoxArray().equals("REQUIRED")) ) {%> bgcolor="#ff99cc" <%}%> >
<%	

	if (ff.getFldDsplyTyp().equals("TEXTAREA"))
	{
%>
		<%-- need to handle a readonly text area by making it a TABLE--%>
		
		<TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) {%> class="readonly"<%}%> NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="3" COLS="<%=iMaxCharactersPerLine - 10%>" onChange="maxCheck('ExpressFormView', '<%=ff.getFldCd()%>', '_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>', <%=ff.getFldLngth()%>);"
 WRAP><%=strFldData%></TEXTAREA>
<%
	}
	else if (ff.getFldDsplyTyp().equals("TEXTAREA10"))
	{
%>
		<%-- need to handle a readonly text area by making it a TABLE--%>
		
		<TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) {%> class="readonly"<%}%> NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="10" COLS="<%=iMaxCharactersPerLine - 10%>" onChange="maxCheck('ExpressFormView', '<%=ff.getFldCd()%>', '_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>', <%=ff.getFldLngth()%>);"
 WRAP><%=strFldData%></TEXTAREA>
<%
	}
	else
	{
	  if ( bUserIsAuthorized && ff.getSrcInd().equals("R") && 
		((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY"))) ) 
	  {
		m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() +
			" WHERE " + myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND " +
			myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn;
		//Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strSrcFldQry = " + m_strSrcFldQry);
			
		rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);
			
		if (rsSrcFld.next()==true) 	
		{
			strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

		}
		rsSrcFld.close();
	  }
	// "O" is same as "R" , but if the field is already populated, it wont reset...it will keep original value
	  if ( bUserIsAuthorized && ff.getSrcInd().equals("O") && 
		((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY"))) ) 
	  {
		if ( (ff.getFldDsplyTyp().equals("READONLY")) && (strFldData != null) )
		{	Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : Field already filled ["+strFldData+"]...(dont reset)");
	
		}
		else 
		{	
			m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() +
				" WHERE " + myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND " +
				myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn;
	Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strSrcFldQry = " + m_strSrcFldQry);
				
			rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);
				
			if (rsSrcFld.next()==true) 	
			{
				strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

			}
			rsSrcFld.close();
		}
	  }

	  if ( bUserIsAuthorized && ff.getSrcInd().equals("S") && 
		((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY"))) ) 
	  {
		m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm() +
			" WHERE " +  myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND " +
			myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn + " AND FRM_SCTN_OCC = " + iSvFrmSctnOcc;
			
		rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);
			
		if (rsSrcFld.next()==true) 	
		{
			strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

		}
		rsSrcFld.close();
	  }
	  
	  if (  bUserIsAuthorized && ff.getSrcInd().equals("D") &&
		((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY"))) )
	  {     //autofill date time field
		// pjs 3-29-2004 dont refill with current date/time if READ_ONLY and already populated
		if ( (ff.getFldDsplyTyp().equals("READONLY")) && (strFldData != null) )
		{	Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : autodate already filled ["+strFldData+"]...(dont reset)");
	
		}
		else 
		{	
			try {
			    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : Filling autodate in view");
			    m_strSrcFldQry = "SELECT to_char(sysdate,'" +
				PropertiesManager.getProperty("lsr.autofill.datefmt", "MM-DD-YYYY-HHMIAM") + "') FROM DUAL";
			    rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);
			    if (rsSrcFld.next()==true)
			    {
				strFldData = rsSrcFld.getString(1);
			    }
			    rsSrcFld.close();
			}
			catch (Exception e) {
				Log.write(Log.ERROR, "ExpressFormView: Filling autodate in view");
			}
		}
	  }


	  if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) 
	  {
%>
		<INPUT class="readonly" TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=strFldData%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" READONLY>
<%	  
	  }
	  else
	  {
		if (ff.getFldDsplyTyp().equals("INPUT"))
		{
%>
		  <INPUT TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=strFldData%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" <%=ff.getJscrptEvnt()%>>
<%
		}
		else
		{
		  if (ff.getFldDsplyTyp().equals("SELECT"))
		  {
%>
		    <SELECT  NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" SIZE="1">
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
		  else
		  {
			  if (ff.getFldDsplyTyp().equals("CHECKBOX"))
			  {	String strChecked="";
				Log.write(Log.DEBUG_VERBOSE, " Checkbox ");
				if ( (strFldData != null) && (strFldData.equals("on")) )
				{	strChecked="CHECKED";
					Log.write(Log.DEBUG_VERBOSE, " Checkbox CHECKED");
				}
%>
				<INPUT TYPE="CHECKBOX" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" <%=strChecked%> >
<%
				
			  }
		  }
		}
	  }
	}
%>
	</TD></TR>

	</TABLE>

<%
  }
}
if (strSvFrmSctnRptInd.equals("Y") && bUserIsAuthorized)
{

%>
		<%--	PRINT OUT A BAR LINE THAT LETS USER ADD AN ADDITIONAL SECTION OCCURRENCE  --%>
		<%--	THIS IS ONLY FOR REPEATABLE SECTIONS  --%>

			<BR CLEAR=ALL>
			<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
			<TR>
			<TH width=400 align=left bgcolor="#7AABDE" >
			<A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
			<SPAN class="barheader"><%=strSvFrmSctnDscrptn%></SPAN>
			</TH>
			<TH width=200 align=left bgcolor="#7AABDE" >
			<SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
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
<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
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

<TABLE align=left border=0 cellspacing=2 cellpadding=1>
<TR><TD>&nbsp;</TD></TR>
<TR>
<TD bgcolor="#ff99cc"><INPUT TYPE="text" SIZE=15 READONLY DISABLED>&nbsp;Shading indicates Required Field</TD>
</TR>
</TABLE>

<BR CLEAR=ALL>
<INPUT TYPE="HIDDEN" NAME="mdfddt" VALUE="<%=strMdfdDt%>">

</FORM>

<SCRIPT LANGUAGE="JavaScript">
<%      if (strFocusField != null && strFocusField.length() > 0)
        {
%>
                document.ExpressFormView.<%= strFocusField %>.focus();
<%      }
%>
</SCRIPT>

<%

} //try
catch (Exception e) {
        rsSrcFld.close();
        rsSrcFld=null;
	Log.write(Log.ERROR, "ExpressFormView2.jsp : Exception caught ["+ e + "]");
        //apply log message to display exception here - Antony - 09/05/2012
        Log.write("Exception in ExpressFormView2.jsp : "+e.getMessage());
                
        stmtFrm.close();
        stmtFrm = null;
}
finally {
        Log.write("Releasing connection object in ExpressFormView2.jsp for conn object: "+connFrm.toString());
        DatabaseManager.releaseConnection(connFrm);
        Log.write("After releasing connection object in ExpressFormView2.jsp.");
}

%>
