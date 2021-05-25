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
             *  04/21/2011  Added java script function for greying out fields by SATISH
             */

%>

<%@ page import ="com.alltel.lsr.common.objects.FormSection,com.alltel.lsr.common.util.DatabaseManager" %>
<%@ page import ="com.alltel.lsr.common.objects.FormField,com.alltel.lsr.common.util.Log" %>
<%@ page import ="java.util.ArrayList" %>
<%@ page import ="com.alltel.lsr.common.objects.SessionDataManager" session="true" %>
<%@ page import ="com.alltel.lsr.common.objects.AlltelRequest" %>

<%            String path = request.getContextPath();
%>
<script type='text/javascript' src='<%=path%>/jquery.js'></script>
<script type='text/javascript' src='<%=path%>/FieldValidationForEngineering.js'></script>

<%
            //Added for greying out fields for supplementals- END
            String selectName = "";
            String remarksNameLSR = "";
            String remarksNameOther = "";
            String dddName = "";
            String reqtyp = "";
            String wifiCheckBox = "";
            String cssService = "";
            String wirelssDataBckup = "";
            String mrcRate = "";
            String managedRouter = "";
            String winNtwkPrtl = "";
            String productType = "";
            String wnpEmailAdrss1 = "";
            String wnpEmailAdrss2 = "";
            String wnpEmailAdrss3 = "";
            String wnpEmailAdrss4 = "";
            String wnpEmailAdrss5 = "";
            String wnpEmailAdrss6 = "";
            String wnpEmailAdrss7 = "";
            String wnpEmailAdrss8 = "";
            String wnpEmailAdrss9 = "";
            String wnpEmailAdrss10 = "";
			String aarEmailAdrss1 = "";
			String aarEmailAdrss2 = "";
			String aarEmailAdrss3 = "";
			String aarEmailAdrss4 = "";
			String aarEmailAdrss5 = "";
			String aarEmailAdrss6 = "";
			String aarEmailAdrss7 = "";
			String aarEmailAdrss8 = "";
			String aarEmailAdrss9 = "";
			String aarEmailAdrss10 = "";


            String act = "";
            ArrayList rdetName = new ArrayList();
            //Added for greying out fields for supplementals- END
            Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- ");

            int iReturnCode = myorderBean.getConnection();
            myorderBean.setUserid(sdm.getUser());

// Verify user has access to view this form

            if (!myorderBean.hasAccessTo(m_iSqncNmbr)) {
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

<%--   Added method for Encoding the XSS attack Characters --%>
<%!
public String replaceXSSChars(String fieldValue, String m_ctrl){
	
	Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- encodeXSSChar() = " + fieldValue);
	
	if ("RequestCtlr".equalsIgnoreCase(m_ctrl) && fieldValue != null && (fieldValue.contains("'") || fieldValue.contains("INTO") || fieldValue.contains("into"))){
		fieldValue = fieldValue.replace("'", "&0x27;");
                fieldValue = fieldValue.replace("INTO", "&0x28;");
		fieldValue = fieldValue.replace("into", "&0x29;");
	} 
	return fieldValue;
}
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

                if (sdm.isAuthorized(m_strFrmScrtyTg) && sdm.isAuthorized(m_strActnScrtyTgSave)) {
                    bUserIsAuthorized = true;
                    bUserIsNotAuthorized = false;
%>
<INPUT TYPE="HIDDEN" NAME="FRM_AUTHORIZATION" VALUE="AUTHORIZED">
<%
                } else {
                    bUserIsAuthorized = false;
                    bUserIsNotAuthorized = true;
                }

                FormSection fs;
                FormField ff;

%>



<INPUT TYPE="HIDDEN" NAME="FRM_SQNC_NMBR" VALUE="<%=m_iFrmSqncNmbr%>">

<INPUT TYPE="HIDDEN" NAME="NEWRECORD" VALUE="<%=m_strNewrec%>" >
<!--   Added for greying out fields for sup changes  by SATISH - start -->
<% String SUPPSTATUSvar = "0";

                String strQueryforSUP = "SELECT LSR_SUP from LSR_T WHERE RQST_SQNC_NMBR=" + m_iSqncNmbr + " AND RQST_VRSN=" + m_iVrsn;
                rsSrcFld = stmtFrm.executeQuery(strQueryforSUP);
                while (rsSrcFld.next()) {
                    SUPPSTATUSvar = rsSrcFld.getString("LSR_SUP");
                }
                rsSrcFld.close();

                if (request.getParameter("SUPPSTATUS") != null) {
                    SUPPSTATUSvar = request.getParameter("SUPPSTATUS");
                }

%>

<INPUT TYPE="HIDDEN" NAME="SUPPSTATUS" VALUE="<%=SUPPSTATUSvar%>">
<!--   Added for greying out fields for sup changes  by SATISH - end -->

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

                                    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- 2 m_vFrmSctn.size()=" + m_vFrmSctn);
                                    for (int fs_idx = 0; fs_idx < m_vFrmSctn.size(); fs_idx++) {
                                        Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp ---  m_iFrmSqncNmbr=" + m_iFrmSqncNmbr + " fs_idx=" + fs_idx);
                                        fs = (FormSection) m_vFrmSctn.elementAt(fs_idx);

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
                int iFrmSctnVectorIndex = 0;
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
                for (int ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++) {
                    ff = (FormField) m_vFrmFld.elementAt(ff_idx);
                    strFldData = ff.getFieldData();

                    if (bUserIsNotAuthorized && (strFldData == null || strFldData.length() == 0)) {
                        // suppress showing the field on the form
                    } else {
                        // Capture first NON-READONLY field, so that we can place focus there
                        if (bFirstTime == true && ff.getFldDsplyTyp() != null && !(ff.getFldDsplyTyp().equals("READONLY")) && (m_iVrsn < 1)) {
                            if (m_iNewSection > 0) //User just added a new section/occ, so put cursor there.
                            {
                                if ((m_iNewSection == ff.getFrmSctnSqncNmbr()) && (m_iNewOcc == ff.getFrmSctnOcc())) {
                                    strFocusField = "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldNmbr();
                                    bFirstTime = false;
                                }
                            } else {
                                strFocusField = "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldNmbr();
                                bFirstTime = false;
                            }

                        }

                        if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) || (ff.getFrmSctnOcc() != iSvFrmSctnOcc)) {

                            if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr) && strSvFrmSctnRptInd.equals("Y") && bUserIsAuthorized) {

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
             <%
                if("RequestCtlr".equalsIgnoreCase(m_CTLR)){
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="BUTTON" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section" onClick="return submitForm(this);"></SPAN>
            <%
                 }else{            
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
            <%
                }
            %>
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
                            if (strSvFrmSctnRptInd.equals("H") && bUserIsAuthorized) {
                                if ((ff.getFrmSctnSqncNmbr() != iSvFrmSctnSqncNmbr)) {
                                    String strProp = "lsr.show.addsection." + m_iFrmSqncNmbr + "." + iSvFrmSctnSqncNmbr;
                                    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- strProp = " + strProp);
                                    if ((PropertiesManager.getProperty(strProp, "false")).equals("true")) {	//show "Add Section"
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
             <%
                if("RequestCtlr".equalsIgnoreCase(m_CTLR)){
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="BUTTON" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section" onClick="return submitForm(this);"></SPAN>
            <%
                }else{            
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
            <%
                }
            %>
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
                            fs = (FormSection) m_vFrmSctn.elementAt(iFrmSctnVectorIndex);

                            strSvFrmSctnDscrptn = fs.getFrmSctnDscrptn();
                            strSvFrmSctnRptInd = fs.getFrmSctnRptInd();
                            iCharacterCount = 0;

%>
<%--	PRINT OUT A HEADER FOR THE SECTION     --%>


<%			// "H" represents a repeating section that has it's individual
//     section headers hidden.
                            if ((!strSvFrmSctnRptInd.equals("H")) || (iSvFrmSctnOcc == 1)) {
%>
<BR CLEAR=ALL>
<TABLE ALIGN="left" WIDTH="100%" border=0 cellspacing=0 cellpadding=0>
    <TR>
        <TH width=400 align=left bgcolor="#7AABDE" >
            <A NAME="<%=iSvFrmSctnSqncNmbr%>">&nbsp;</A>
            <SPAN class="barheader"><%=strSvFrmSctnDscrptn%>&nbsp;<%if (iSvFrmSctnOcc > 0 && (!strSvFrmSctnRptInd.equals("H"))) {%> (<%=iSvFrmSctnOcc%>)<%}%></SPAN>
        </TH>
        <%			}

                                    if (strSvFrmSctnRptInd.equals("Y") && (iSvFrmSctnOcc > 1) && bUserIsAuthorized) {
        %>
        <TH width=200 align=left bgcolor="#7AABDE" >
             <%
                if("RequestCtlr".equalsIgnoreCase(m_CTLR)){
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="BUTTON" NAME="del_sctn_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>" VALUE="Delete Section" onClick="return submitForm(this);"></SPAN>
            <%
                }else{            
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="del_sctn_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>" VALUE="Delete Section"></SPAN>
            <%
                }
            %>
        </TH>
        <%
                                            } else {
        %>
        <TH width=200 align=left bgcolor="#7AABDE" >
            <SPAN class="barheader">&nbsp;</SPAN>
        </TH>
        <%        }

                                    if ((!strSvFrmSctnRptInd.equals("H")) || (iSvFrmSctnOcc == 1)) {
        %>
        <TH align=right bgcolor="#7AABDE" >
            <A HREF="#topofpage"><SPAN class="barheaderlink">&nbsp;Top&nbsp;&nbsp;</SPAN></A>
        </TH>
    </TR>
</TABLE>
<%			}
                            if (strSvFrmSctnRptInd.equals("H")) {	// Put Section # on page
%>
<BR CLEAR=ALL>
<TABLE align=left border=0 cellspacing=2 cellpadding=1><TR>
        <TD align=left><%=iSvFrmSctnOcc%>.&nbsp;</TD></TR>
</TABLE>
<%
                            } else {
%>
<BR CLEAR=ALL>
<%          }
                        }

                        iDsplyFldWidth = iFldBuffer;

                        if (ff.getFldCd().length() > ff.getFldDsplySz()) {
                            iDsplyFldWidth = iDsplyFldWidth + ff.getFldCd().length();
                        } else {
                            iDsplyFldWidth = iDsplyFldWidth + ff.getFldDsplySz();
                        }

                        if (iCharacterCount != 0) {
                            if (((iCharacterCount + iDsplyFldWidth) > iMaxCharactersPerLine) || ((ff.getFldDsplyActns() != null) && (ff.getFldDsplyActns().indexOf("b") >= 0))) {
                                iCharacterCount = 0;%><BR CLEAR=ALL><%
//If "H" type and new line, then bump over to the right to help things line up better...
                                                                if (strSvFrmSctnRptInd.equals("H")) {
                                                                    String strTemp = iSvFrmSctnOcc + ". ";
                                                                    iCharacterCount = strTemp.length();
%>				<TABLE align=left border=0 cellspacing=2 cellpadding=1><TR><TD>
            <%				for (int i = 0; i < iCharacterCount; i++) {
            %>					&nbsp;
            <%    }
            %>					</TD></TR></TABLE> <%
                                            }

                                        }
                                    }

                                    iCharacterCount = iCharacterCount + iDsplyFldWidth;

                                    if (ff.getFldDsplySz() > iMaxCharactersPerLine) {
                                        iDsplyFldLngth = iMaxCharactersPerLine;
                                    } else {
                                        iDsplyFldLngth = ff.getFldDsplySz();
                                    }

                                    if ((ff.getFldDataTyp().equals("X")) || (ff.getFldDataTyp().equals("S"))) {
                                        strFldDataTypDesc = "Alphanumeric";
                                    } else {
                                        if (ff.getFldDataTyp().equals("A")) {
                                            strFldDataTypDesc = "Alpha";
                                        } else {
                                            if (ff.getFldDataTyp().equals("N")) {
                                                strFldDataTypDesc = "Numeric";
                                            } else {
                                                if (ff.getFldDataTyp().equals("D")) {
                                                    strFldDataTypDesc = "Date (system generated)";
                                                } else {
                                                    if (ff.getFldDataTyp().equals("C")) {
                                                        strFldDataTypDesc = "Checkbox";
                                                    }
                                                }
                                            }
                                        }
                                    }
            %>

<TABLE align=left border=0 cellspacing=2 cellpadding=1>
    <TR><TD align=left><FONT STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();" ONMOUSEOVER="showpopupmsg('<%=ff.getFldCd()%> -- <%=ff.getFrmFldNmbr()%> |<%=ff.getFldDscrptn()%>|Type: <%=strFldDataTypDesc%>|Length: <%=ff.getFldLngth()%><%if (ff.getFldFrmtMsk() != null && ff.getFldFrmtMsk().length() > 0) {%>|Format: <%=ff.getFldFrmtMsk()%><%}%>');">
                <%
                                        // Business Data Products Request for special message on expdite check box.
                                        //HD
                                        if ((ff.getFrmSqncNmbr() == 824) && (ff.getFldDscrptn().equals("Expedite"))) {
                %><%=ff.getFldCd()%><span class="expdt">&nbsp;&nbsp;(To Request an Expedite, you must send an email with Management Approval to the Data Engineering and DSTAC management)</span><br></FONT></TD></TR>
                <%} else if (ff.getFrmSqncNmbr() == 850 && (strFldData == null || strFldData.length() == 0)) {
                %>
</FONT></TD></TR>
<%} else {%>
<%=ff.getFldCd()%></FONT></TD></TR>
<%
                                        }
%>
<TR><TD <% if (bUserIsAuthorized && !ff.getFldDsplyTyp().equals("READONLY") && (ff.getMoxArray() != null && ff.getMoxArray().equals("REQUIRED"))) {%> bgcolor="#ff99cc" <%}%> >
        <%

                                if (ff.getFldDsplyTyp().equals("TEXTAREA")) {
                                    if (ff.getFldCd().equalsIgnoreCase("RDET")) {
                                        rdetName.add("_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr());
                                    }
                                    if (ff.getFldCd().equalsIgnoreCase("REMARKS") && m_iVrsn > 0 && m_iFrmSqncNmbr == 1) {
                                        remarksNameLSR = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                    } else {
                                        remarksNameOther = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                    }


        %>
        <%-- need to handle a readonly text area by making it a TABLE--%>

        <TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) {%> class="readonly"<%}%> NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="3" COLS="<%=iMaxCharactersPerLine - 10%>" onChange="maxCheck('ExpressFormView', '<%=ff.getFldCd()%>', '_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>', <%=ff.getFldLngth()%>);"
                                                                                            WRAP
                                                                                            ><%=replaceXSSChars(strFldData, m_CTLR)%></TEXTAREA>

        <%
                                        } else if (ff.getFldDsplyTyp().equals("TEXTAREA5") && !(strFldData == null || strFldData.length() == 0)) {

        %>
        <%-- need to handle a readonly text area by making it a TABLE--%>

        <TEXTAREA class="readonly" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="5" COLS="<%=iMaxCharactersPerLine - 10%>" onChange="maxCheck('ExpressFormView', '<%=ff.getFldCd()%>', '_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>', <%=ff.getFldLngth()%>);"
                  WRAP readonly='readonly'><%=replaceXSSChars(strFldData, m_CTLR)%></TEXTAREA>
        <%
                                        } else if (ff.getFldDsplyTyp().equals("TEXTAREA10")) {

        %>
        <%-- need to handle a readonly text area by making it a TABLE--%>

        <TEXTAREA <%if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) {%> class="readonly"<%}%> NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" ROWS="10" COLS="<%=iMaxCharactersPerLine - 10%>" onChange="maxCheck('ExpressFormView', '<%=ff.getFldCd()%>', '_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>', <%=ff.getFldLngth()%>);"
                                                                                            WRAP><%=replaceXSSChars(strFldData, m_CTLR)%></TEXTAREA>
        <%
                                        } else {
                                            if (bUserIsAuthorized && ff.getSrcInd().equals("R")
                                                    && ((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY")))) {
                                                m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm()
                                                        + " WHERE " + myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND "
                                                        + myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn;
                                                //Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strSrcFldQry = " + m_strSrcFldQry);

                                                rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);

                                                if (rsSrcFld.next() == true) {
                                                    strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

                                                }
                                                rsSrcFld.close();
                                            }
                                            // "O" is same as "R" , but if the field is already populated, it wont reset...it will keep original value
                                            if (bUserIsAuthorized && ff.getSrcInd().equals("O")
                                                    && ((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY")))) {
                                                if ((ff.getFldDsplyTyp().equals("READONLY")) && (strFldData != null)) {
                                                    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : Field already filled [" + strFldData + "]...(dont reset)");

                                                } else {
                                                    m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm()
                                                            + " WHERE " + myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND "
                                                            + myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn;
                                                    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView.jsp --- m_strSrcFldQry = " + m_strSrcFldQry);

                                                    rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);

                                                    if (rsSrcFld.next() == true) {
                                                        strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

                                                    }
                                                    rsSrcFld.close();
                                                }
                                            }

                                            if (bUserIsAuthorized && ff.getSrcInd().equals("S")
                                                    && ((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY")))) {
                                                m_strSrcFldQry = "SELECT " + ff.getSrcDbClmnNm() + " FROM " + ff.getSrcDbTblNm()
                                                        + " WHERE " + myorderBean.getExpressOrder().getSQNC_COLUMN() + " = " + m_iSqncNmbr + " AND "
                                                        + myorderBean.getExpressOrder().getVRSN_COLUMN() + " = " + m_iCurrentVrsn + " AND FRM_SCTN_OCC = " + iSvFrmSctnOcc;

                                                rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);

                                                if (rsSrcFld.next() == true) {
                                                    strFldData = rsSrcFld.getString(ff.getSrcDbClmnNm());

                                                }
                                                rsSrcFld.close();
                                            }

                                            if (bUserIsAuthorized && ff.getSrcInd().equals("D")
                                                    && ((strFldData == null || strFldData.length() == 0) || (ff.getFldDsplyTyp().equals("READONLY")))) {     //autofill date time field
                                                // pjs 3-29-2004 dont refill with current date/time if READ_ONLY and already populated
                                                if ((ff.getFldDsplyTyp().equals("READONLY")) && (strFldData != null)) {
                                                    Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : autodate already filled [" + strFldData + "]...(dont reset)");

                                                } else {
                                                    try {
                                                        Log.write(Log.DEBUG_VERBOSE, "ExpressFormView : Filling autodate in view");
                                                        m_strSrcFldQry = "SELECT to_char(sysdate,'"
                                                                + PropertiesManager.getProperty("lsr.autofill.datefmt", "MM-DD-YYYY-HHMIAM") + "') FROM DUAL";
                                                        rsSrcFld = stmtFrm.executeQuery(m_strSrcFldQry);
                                                        if (rsSrcFld.next() == true) {
                                                            strFldData = rsSrcFld.getString(1);
                                                        }
                                                        rsSrcFld.close();
                                                    } catch (Exception e) {
                                                        Log.write(Log.ERROR, "ExpressFormView: Filling autodate in view");
                                                    }
                                                }
                                            }


                                            if (ff.getFldDsplyTyp().equals("READONLY") || bUserIsNotAuthorized) {
                                                if ((ff.getFrmSqncNmbr() == 850 && !(strFldData == null || strFldData.length() == 0)) || (ff.getFrmSqncNmbr() != 850)) {
                                                    //Added for greying out fields for sup changes  by SATISH - START
                                                    if (ff.getFldCd().equals("REQTYP")) {
                                                        reqtyp = strFldData;
                                                    }
                                                    if (ff.getFldCd().equals("ACT")) {
                                                        act = strFldData;
                                                    }
                                                    //Added for greying out fields for sup changes  by SATISH - END
        %>
        <INPUT class="readonly" TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=replaceXSSChars(strFldData, m_CTLR)%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" READONLY
               <%
                                                                   if (m_iVrsn > 0) {
               %> disabled="true"
               <%}
               %>>
        <%
                                                        }
                                                    } else {
                                                        if (ff.getFldDsplyTyp().equals("INPUT")) {
                                                            //Added for greying out fields for sup changes  by SATISH - START
                                                            if (ff.getFldCd().equalsIgnoreCase("DDD")) {
                                                                dddName = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                            }
                                                            //Added for greying out fields for sup changes  by SATISH - END
                                                            //Fix for LR form DD field 
        if (ff.getFldCd().equalsIgnoreCase("DD") && m_iFrmSqncNmbr == 14) {
        %>
        <INPUT class = "lrDDval" TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" id="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=replaceXSSChars(strFldData, m_CTLR)%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>"
		<%  
		if (m_iVrsn > 0) {
               %> disabled="true"
               <%}
               %>
               > <td id="validationErr" style="display:none; color:red"> &nbsp;Field should be either empty or 'mm-dd-yyy' Format</td>
               <% }  	 else
					{        
 		%>
        <INPUT TYPE="text" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" id="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" VALUE="<%=replaceXSSChars(strFldData, m_CTLR)%>" MAXLENGTH="<%=ff.getFldLngth()%>" SIZE="<%=iDsplyFldLngth%>" <%=ff.getJscrptEvnt()%>
               <%
                                                                           if (ff.getFldCd().equalsIgnoreCase("MRC Rate $")) {
                                                                               mrcRate = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                             // Added Windstream Network Portal Indicator for RIS-11595314 By Vijay 12/14/12 - Start
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#1")) {
                                                                               wnpEmailAdrss1 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#2")) {
                                                                               wnpEmailAdrss2 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#3")) {
                                                                               wnpEmailAdrss3 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#4")) {
                                                                               wnpEmailAdrss4 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#5")) {
                                                                               wnpEmailAdrss5 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#6")) {
                                                                               wnpEmailAdrss6 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#7")) {
                                                                               wnpEmailAdrss7 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#8")) {
                                                                               wnpEmailAdrss8 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#9")) {
                                                                               wnpEmailAdrss9 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                                           if (ff.getFldCd().equalsIgnoreCase("WNP Email Address#10")) {
                                                                               wnpEmailAdrss10 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                           }
                                                             // Added Windstream Network Portal Indicator for RIS-11595314 By Vijay 12/14/12 - End
																		/* AAR form changes - start - 01-25-13
																			  */
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#1")) {
                                                                               aarEmailAdrss1 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#2")) {
                                                                               aarEmailAdrss2 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#3")) {
                                                                               aarEmailAdrss3 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#4")) {
                                                                               aarEmailAdrss4 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#5")) {
                                                                               aarEmailAdrss5 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#6")) {
                                                                               aarEmailAdrss6 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#7")) {
                                                                               aarEmailAdrss7 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#8")) {
                                                                               aarEmailAdrss8 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#9")) {
                                                                               aarEmailAdrss9 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			if (ff.getFldCd().equalsIgnoreCase("AAR Email Address#10")) {
                                                                               aarEmailAdrss10 = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                            }
																			/* AAR form changes - end - 01-25-13
																			  */
                                                                           if (m_iVrsn > 0) {
               %> disabled="true"
               <%}
               %>
               >
        <%}
                                                                } else {
                                                                    if (ff.getFldDsplyTyp().equals("SELECT")) {
                                                                        //Added for greying out fields for sup changes  by SATISH - START
                                                                        String endOfSelect = ">";
                                                                        if (ff.getFldCd().equalsIgnoreCase("CSS SERVICE TYPE WITH OPTIONAL FEATURES")) {
                                                                            cssService = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                        }
                                                                        if (ff.getFldCd().equalsIgnoreCase("Wireless Data Backup Required?")) {
                                                                            wirelssDataBckup = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                        }
                                                                 // Added Windstream Network Portal Indicator for RIS-11595314 By Vijay 12/14/12 - Start
                                                                        if (ff.getFldCd().equalsIgnoreCase("Windstream to provide Managed Router?")) {
                                                                            managedRouter = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                        }
                                                                        if (ff.getFldCd().equalsIgnoreCase("Product Type")) {
                                                                            productType = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                        }
                                                                        if (ff.getFldCd().equalsIgnoreCase("Windstream Network Portal")) {
                                                                            winNtwkPrtl = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                         }
                                                                  // Added Windstream Network Portal Indicator for RIS-11595314 By Vijay 12/14/12 - End

                                                                        if (m_iVrsn > 0) {
                                                                            if (ff.getFldCd().equalsIgnoreCase("SUP") || ff.getFldCd().equalsIgnoreCase("DSUP") || ff.getFldCd().equalsIgnoreCase("SPSUP")) {
                                                                                endOfSelect = "onChange='return makeAllReadOnlyExceptSubmit(this)'>";
                                                                                selectName = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();

                                                                            }
                                                                            //Added for greying out fields for sup changes  by SATISH - END

                                                                        }
        %>


        <SELECT  ID="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" SIZE="1" <%=endOfSelect%>
                 <%
                                                                                         if (m_iVrsn > 0) {
                 %> disabled="true"
                 <%}
                 %>>
            <OPTION VALUE=""></OPTION>
            <%
                                                                                    m_vVV = fv.getValidValues(ff.getFldVlsSqncNmbr());	//Vector of valid values for this field
                                                                                    for (int i = 0; i < m_vVV.size(); i++) {
                                                                                        m_strFldVl = (String) m_vVV.elementAt(i);
            %>
            <OPTION VALUE="<%=m_strFldVl%>" <% if (m_strFldVl.equals(strFldData)) {%> SELECTED<%}%>><%=m_strFldVl%></OPTION>
            <%
                                                                                    }
            %>
        </SELECT>
        <%
                                                                            } else {
                                                                                if (ff.getFldDsplyTyp().equals("CHECKBOX")) {
                                                                                    String strChecked = "";
                                                                                    Log.write(Log.DEBUG_VERBOSE, " Checkbox ");
                                                                                    if ((strFldData != null) && (strFldData.equals("on"))) {
                                                                                        strChecked = "CHECKED";
                                                                                        Log.write(Log.DEBUG_VERBOSE, " Checkbox CHECKED");
                                                                                    }
        %>
        <INPUT TYPE="CHECKBOX" NAME="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>"  id="_FF_<%=iSvFrmSctnSqncNmbr%>_<%=iSvFrmSctnOcc%>_<%=ff.getFrmFldNmbr()%>" <%=strChecked%>
               <%
                                                                                                   if (ff.getFldCd().equalsIgnoreCase("WIFI")) { // Added for managed network security Form
                                                                                                       wifiCheckBox = "_FF_" + iSvFrmSctnSqncNmbr + "_" + iSvFrmSctnOcc + "_" + ff.getFrmFldNmbr();
                                                                                                   }
                                                                         
                                                                                                   if (m_iVrsn > 0) {
               %> disabled="true"
               <%}
               %>>
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
                if (strSvFrmSctnRptInd.equals("Y") && bUserIsAuthorized) {

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
             <%
                if("RequestCtlr".equalsIgnoreCase(m_CTLR)){
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="BUTTON" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section" onClick="return submitForm(this);"></SPAN>
            <%
                }else{            
            %>
            <SPAN class="barheader"><INPUT class=appButton TYPE="SUBMIT" NAME="add_sctn_<%=iSvFrmSctnSqncNmbr%>" VALUE="Add Section"></SPAN>
            <%
                }
            %>
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
<!--   Added for greying out fields for sup changes  by SATISH - START -->

<%
                if (request.getParameter("reqtyp") != null) {
                    reqtyp = request.getParameter("reqtyp");
                }
                if (request.getParameter("act") != null) {
                    act = request.getParameter("act");
                }
                //code for LR form DD fix to set service type and activity type
                if((reqtyp != null && !reqtyp.trim().isEmpty()) && (act != null && !act.trim().isEmpty())){
                sdm.setSerTyp(reqtyp);
                sdm.setActTyp(act);
                }
%>


<INPUT class="lrSerTyp" TYPE="HIDDEN" NAME="reqtyp" VALUE="<%=reqtyp%>">
<INPUT class="lrActTyp" TYPE="HIDDEN" NAME="act" VALUE="<%=act%>">
<INPUT TYPE="HIDDEN" NAME="vrsn" VALUE="<%=m_iVrsn%>">
<INPUT TYPE="HIDDEN" NAME="securitytag" VALUE="<%=m_strFrmScrtyTg%>">


<!--   Added for greying out fields for sup changes  by SATISH - END -->
</FORM>

<SCRIPT LANGUAGE="JavaScript">
    <%      if (strFocusField != null && strFocusField.length() > 0) {
    %>
        document.ExpressFormView.<%= strFocusField%>.focus();
    <%      }
    %>
        // Script added for greying out fields by SATISH --- Start
        function makeAllReadOnlyExceptSubmit(thiss)
        {

		
//            var elem = document.getElementById('ExpressFormView').elements; -- change #1
            var elem = document.forms["ExpressFormView"].elements;

            //  var str
            for(var i = 0; i < elem.length; i++)
            {
                elem[i].disabled=false;
                // str+=elem[i].name+":";

            }

//            var f = document.getElementById("ExpressFormView"); -- change #2
            var f = document.forms["ExpressFormView"];
            var reqtyp=f.reqtyp.value;
            var act=f.act.value;
            var version=f.vrsn.value;

            //if(!(f.FRM_SQNC_NMBR.value ==14)||!(f.securitytag.value=='PROVIDER_FORM')){	// This is replaced if only LR  form should be enabled
            if(f.securitytag.value!='PROVIDER_FORM'){
                if ((reqtyp =='C') && (act =='V') && version > 0 ) {
                    if(thiss.value == 1 || f.SUPPSTATUS.value ==1){
                        var inputs = f.getElementsByTagName("input");
                        for(var i = 0; i < inputs.length; i++){
                            if(inputs[i].type != 'submit' && inputs[i].type != "button" ){
                                if(!inputs[i].getAttribute('readOnly')){
				    inputs[i].readOnly = true;
                                    inputs[i].className='readonlysup';

                                }
                            }
                            if(inputs[i].value == "Add Section" || inputs[i].value == "Delete Section" ){
                                inputs[i].disabled = true;
                            }
                        }
                        var lists= f.getElementsByTagName("select");
                        f.SUPPSTATUS.value=thiss.value;
                        for(var i = 0; i < lists.length; i++){
                            lists[i].disabled = true;
                        }

			var textareas= f.getElementsByTagName("textarea");
                        for(var i = 0; i < textareas.length; i++){

                            if(textareas[i].name!='<%=remarksNameLSR%>'){
				textareas[i].className='readonlysup';
                                textareas[i].readOnly = true;
                            }
			}

                        var f1 = document.getElementById("<%=selectName%>");
                        if(f1!=null){
                            f1.disabled = false;
                            f1.className="";
                        }
                        var f4 = document.getElementById("<%=remarksNameLSR%>");
                        if(f4!=null){
                            f4.readOnly=false;
                            f4.className="";
                            f4.value="";

                        }


    <% for (int i = 0; i < rdetName.size(); ++i) {%>
                        var f3=document.getElementById("<%=(String) rdetName.get(i)%>");
                        if(f3!=null){
                            f3.readOnly = true;
                        }
    <%}%>
                        f.SUPPSTATUS.value=1;
                    }
                    if(thiss.value == "" || f.SUPPSTATUS.value == 0)
                    {
                        var inputs = f.getElementsByTagName("input");
                        for(var i = 0; i < inputs.length; i++){
                            if(inputs[i].type != "submit" && inputs[i].type != "button" ){
                                if(!inputs[i].getAttribute('readOnly')){
                                    inputs[i].className='readonlysup';
                                    inputs[i].readOnly = true;
                                }
                            }
                            if(inputs[i].value == "Add Section" || inputs[i].value == "Delete Section" ){
                                inputs[i].disabled = true;
                            }
                        }

                        var lists= f.getElementsByTagName("select");
                        f.SUPPSTATUS.value=thiss.value;
                        for(var i = 0; i < lists.length; i++){
                            lists[i].disabled = true;

                        }
                        var textareas= f.getElementsByTagName("textarea");
                        for(var i = 0; i < textareas.length; i++){
                            textareas[i].className='readonlysup';
                            textareas[i].readOnly = true;
			}
                        var f1 = document.getElementById("<%=selectName%>");
                        if(f1!=null){
                            f1.disabled = false;

                            f1.className="";
                        }
                        var f4 = document.getElementById("<%=remarksNameLSR%>");
 			if(f4!=null){
                            f4.value=" ";
                        }


                        f.SUPPSTATUS.value=0;
                    }

                    if(thiss.value == 2 || f.SUPPSTATUS.value == 2)
                    {
                        var inputs = f.getElementsByTagName("input");
                        for(var i = 0; i < inputs.length; i++){
                            if(inputs[i].type != 'submit' && inputs[i].type != "button" ){
                                if(!inputs[i].getAttribute('readOnly')){
                                    inputs[i].className='readonlysup';
                                    inputs[i].readOnly = true;
                                }
                            }
                            if(inputs[i].value == "Add Section" || inputs[i].value == "Delete Section" ){
                                inputs[i].disabled = true;
                            }
                        }
                        var lists= f.getElementsByTagName("select");
                        f.SUPPSTATUS.value=thiss.value;
                        for(var i = 0; i < lists.length; i++){
                            lists[i].disabled = true;
                        }
                        var textareas= f.getElementsByTagName("textarea");
			for(var i = 0; i < textareas.length; i++){

                            if(textareas[i].name!='<%=remarksNameLSR%>'){
				textareas[i].className='readonlysup';
                                textareas[i].readOnly = true;
                            }
			}

                        var f1 = document.getElementById("<%=selectName%>");
                        var f2 = document.getElementById("<%=dddName%>");
                        var f4 = document.getElementById("<%=remarksNameLSR%>");
                        if(f1!=null)
                            f1.disabled = false;
                        if(f2!=null){
                            f2.readOnly = false;
                            f2.className="";
                        }

                        if(f4!=null){
                            f4.readOnly=false;
                            f4.className="";
                            f4.value="";

                        }

    <% for (int i = 0; i < rdetName.size(); ++i) {%>
                        var f3=document.getElementById("<%=(String) rdetName.get(i)%>");
                        if(f3!=null){
                            f3.readOnly = true;
                        }
    <%}%>
                        f.SUPPSTATUS.value=2;
                    }
                    if(thiss.value == 3|| f.SUPPSTATUS.value == 3){

                        var inputs = f.getElementsByTagName("input");
                        for(var i = 0; i < inputs.length; i++){
                            if(inputs[i].type != "submit" && inputs[i].type != "button" ){

                                if(inputs[i].className=='readonlysup'){
                                    inputs[i].className='';
                                    inputs[i].readOnly = false;
                                }
                            }
                            if(inputs[i].value == "Add Section" || inputs[i].value == "Delete Section" ){
                                inputs[i].disabled = false;
                            }
                        }

                        var lists= f.getElementsByTagName("select");
                        f.SUPPSTATUS.value=thiss.value;
                        for(var i = 0; i < lists.length; i++){
                            lists[i].disabled = false;
                        }
                        var textareas= f.getElementsByTagName("textarea");
                        for(var i = 0; i < textareas.length; i++){
                            textareas[i].className='';
                            textareas[i].readOnly = false;
			}
                        var f1 = document.getElementById("<%=selectName%>");
                        if(f1!=null)
                            f1.disabled = false;
                        f.SUPPSTATUS.value=3;
                    }
                }
            }
        }

        makeAllReadOnlyExceptSubmit(this);
        // Script added for greying out fields by SATISH --- end

</SCRIPT>
<%

            } //try
            catch (Exception e) {
                rsSrcFld.close();
                rsSrcFld = null;
                Log.write(Log.ERROR, "ExpressFormView.jsp : Exception caught [" + e + "]");
                //apply log message to display exception here - Antony - 09/05/2012
                Log.write("Exception in ExpressFormView.jsp : " + e.getMessage());
                stmtFrm.close();
                stmtFrm = null;
            } finally {

                Log.write("Releasing connection object in ExpressFormView.jsp for conn object: " + connFrm.toString());
                DatabaseManager.releaseConnection(connFrm);
                Log.write("After releasing connection object in ExpressFormView.jsp.");
            }

%>

<script type="text/javascript">
// Script added for form field validation for RIS-11595314 by Vijay- Start
 $(document).ready(function() {
   $selectFormNumber = <%=m_iFrmSqncNmbr%>;
  if ($selectFormNumber == 841) {

  ManagedNetworkSecurity.fieldValidationMngdScrty([$("#<%=cssService%>"),$("#<%=wifiCheckBox%>")]);
  }

  if ($selectFormNumber == 860) {
   ManagedRouterForm.fieldValidationMngdRtr([$(".appButton"),$("#<%=wirelssDataBckup%>"),$("#<%=mrcRate%>"),
       $("#<%=wnpEmailAdrss1%>"),$("#<%=wnpEmailAdrss2%>"),$("#<%=wnpEmailAdrss3%>"),$("#<%=wnpEmailAdrss4%>"),$("#<%=wnpEmailAdrss5%>"),
     $("#<%=wnpEmailAdrss6%>"),$("#<%=wnpEmailAdrss7%>"),$("#<%=wnpEmailAdrss8%>"),$("#<%=wnpEmailAdrss9%>"),$("#<%=wnpEmailAdrss10%>"),$("#<%=managedRouter%>"), $("#<%=winNtwkPrtl%>")]);
		}
  if ($selectFormNumber == 852) {

	  BillingProduct.fieldValidationBlngPrdt([$("#<%=productType%>"),$("#<%=winNtwkPrtl%>")]);
  }	
  /* AAR form changes - start - 01-25-13
             */
	if ($selectFormNumber == 861) {

	  AAR.fieldValidationAARPrtl([$(".appButton"),$("#<%=aarEmailAdrss1%>"),$("#<%=aarEmailAdrss2%>"),$("#<%=aarEmailAdrss3%>"),$("#<%=aarEmailAdrss4%>"),		  $("#<%=aarEmailAdrss5%>"),$("#<%=aarEmailAdrss6%>"),$("#<%=aarEmailAdrss7%>"),$("#<%=aarEmailAdrss8%>"),$("#<%=aarEmailAdrss9%>"),		  $("#<%=aarEmailAdrss10%>")]);
  }
/* AAR form changes - end - 01-25-13
             */
  });
  // Script added for form field validation for RIS-11595314 by Vijay - End
  
  //Script added for EU field size restriction to 25 characters - Start
    if(null != document.getElementsByName("_FF_1_0_16")[0] && null != document.getElementsByName("_FF_2_0_35")[0]) {
      var emptySpaces = "                         ";
      var ciaNameOnly = document.getElementsByName("_FF_1_0_16")[0].value;
      var ciaName25 = (ciaNameOnly+emptySpaces).substring(0,25);
      document.getElementsByName("_FF_2_0_35")[0].value = document.getElementsByName("_FF_2_0_35")[0].value.replace(ciaNameOnly, ciaName25);
    }
  //Script added for EU field size restriction to 25 characters - End
  
  // Script added for LR form DD field validation
	function ddValidateFunction(ddField) {
	var lrDD = ddField;
	var dateformat = /^(0?[1-9]|1[012])[\-](0?[1-9]|[12][0-9]|3[01])[\-]\d{4}$/;
	
	if(!dateformat.test(lrDD) && lrDD != null && lrDD != '')
		{
		document.getElementById('validationErr').style.display='block';
		return true;
	    }
	    else {
	    document.getElementById('validationErr').style.display='none';
	    }
	    return false;
}
  
</script>

