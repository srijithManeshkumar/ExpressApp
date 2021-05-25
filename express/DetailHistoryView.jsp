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
 * MODULE:      DetailHistoryView.jsp
 *
 * DESCRIPTION: Generic View used to display Order History details
 *
 * AUTHOR:      psedlak
 *
 * DATE:        9-22-2003
 *
 * HISTORY:
 *
 */

%>

<%
String strSqncNmbr = (String) request.getAttribute("histseq");

Log.write(Log.DEBUG_VERBOSE, "HistoryView.jsp --- " + strSqncNmbr);
%>

<BR CLEAR=ALL>
<HR>

<%

Connection conHist = null;
Statement stmtHist = null;
ResultSet rsHist = null;

try {
	conHist = DatabaseManager.getConnection();
	stmtHist = conHist.createStatement();
	boolean bMoreChanges = false;

	String strQuery ="SELECT F.FRM_DSCRPTN as FRM_DSC, F.FRM_CD as FRM_CD, FS.FRM_SCTN_DSCRPTN as SCTN, FF.FLD_CD as FLD_CD, " +
		" O.FRM_SCTN_OCC as OCC, FF.FLD_DSCRPTN as FLD_DSC, O.OLD_VALUE as OLD, O.NEW_VALUE as NEW, TO_CHAR(OH.MDFD_DT,'" +
		PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "') as MDFD_DT, OH.MDFD_USERID" +
		" FROM ORDER_DETAIL_HISTORY_T O, FORM_T F, FORM_SECTION_T FS, FORM_FIELD_T FF, ORDER_HISTORY_T OH " +
		" WHERE O.DTL_HSTRY_SQNC_NMBR = " + strSqncNmbr + " AND O.FRM_SQNC_NMBR = F.FRM_SQNC_NMBR " +
		"  AND O.FRM_SQNC_NMBR = FS.FRM_SQNC_NMBR " +
		"  AND O.FRM_SCTN_SQNC_NMBR = FS.FRM_SCTN_SQNC_NMBR " +
		"  AND O.FRM_SQNC_NMBR = FF.FRM_SQNC_NMBR " +
		"  AND O.FRM_SCTN_SQNC_NMBR = FF.FRM_SCTN_SQNC_NMBR " +
		"  AND O.FRM_FLD_NMBR = FF.FRM_FLD_NMBR AND O.DTL_HSTRY_SQNC_NMBR = OH.DTL_HSTRY_SQNC_NMBR " +
		"  ORDER BY O.ITM_NMBR ";
	rsHist = stmtHist.executeQuery(strQuery);
	boolean bHead = false;
	boolean bSectionLvl = false;
	String strDesc, strDesc2;
	if (rsHist.next()==true) 
	{
		bMoreChanges = true;
	}
	else //have to check for section level changes...IE delete section
	{	
		bMoreChanges = false;	
		 strQuery ="SELECT F.FRM_DSCRPTN as FRM_DSC, F.FRM_CD as FRM_CD, FS.FRM_SCTN_DSCRPTN as SCTN,  " +
                " O.FRM_SCTN_OCC as OCC, O.OLD_VALUE as OLD, O.NEW_VALUE as NEW, TO_CHAR(OH.MDFD_DT,'" +
                PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "') as MDFD_DT, OH.MDFD_USERID" +
                " FROM ORDER_DETAIL_HISTORY_T O, FORM_T F, FORM_SECTION_T FS, ORDER_HISTORY_T OH " +
                " WHERE O.DTL_HSTRY_SQNC_NMBR = " + strSqncNmbr + " AND O.DTL_HSTRY_SQNC_NMBR = OH.DTL_HSTRY_SQNC_NMBR " +
		" AND O.FRM_SQNC_NMBR = F.FRM_SQNC_NMBR " +
                "  AND O.FRM_SQNC_NMBR = FS.FRM_SQNC_NMBR " +
                "  AND O.FRM_SCTN_SQNC_NMBR = FS.FRM_SCTN_SQNC_NMBR " +
                "  ORDER BY O.ITM_NMBR ";
	        rsHist = stmtHist.executeQuery(strQuery);
		if (rsHist.next()==true)
		{
			bSectionLvl = true;
			bMoreChanges = true;
		}
	}
	while (bMoreChanges)
	{
			if (!bHead) {	//Show header...start TABLE
	%>			&nbsp;&nbsp;User&nbsp;<b><%=rsHist.getString("MDFD_USERID")%></b>
					&nbsp;made the following changes at <b><%=rsHist.getString("MDFD_DT")%></b>
				<br><br>
				<TABLE border=2>
				<TR>
					<TH>Form&nbsp;Description</TH>
					<TH>Section</TH>
					<TH width =150>Field Cd/Name</TH>
					<TH width=250>Old Value</TH>
					<TH width=250>New Value</TH>
				</TR>
	<%			bHead = true;
		
			}
			strDesc  = rsHist.getString("FRM_CD") + "<br>" + rsHist.getString("FRM_DSC") ;
			if (bSectionLvl) {	
				strDesc2 = "<center>n/a</center>";
			} else {
				strDesc2 = rsHist.getString("FLD_CD") + "<br>" + rsHist.getString("FLD_DSC") ;
			}
	%>
			<TR>
			<TD><%=strDesc%></TD>
			<TD><%=rsHist.getString("SCTN")%>
	<%			if (rsHist.getInt("OCC") > 0) {	%>
					&nbsp;&nbsp;(&nbsp;<%=rsHist.getInt("OCC")%>&nbsp;)&nbsp;
	<%			}				%>
						
			</TD>
			<TD><%=strDesc2%></TD>
			<TD ><%=rsHist.getString("OLD")%></TD>
			<TD ><%=rsHist.getString("NEW")%></TD>
			</TR>
	<%
			if (rsHist.next()) 	bMoreChanges=true;
			else			bMoreChanges = false;
	}
} //try
catch (Exception e) {
        rsHist.close();
        rsHist=null;

        stmtHist.close();
        stmtHist = null;
}
finally {
        DatabaseManager.releaseConnection(conHist);
}
%>

</TABLE>
<BR>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   $
/*
/*Initial Checkin
*/
/* $Revision:  $
*/
%>
