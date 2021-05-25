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
 * MODULE:      HistoryView.jsp
 *
 * DESCRIPTION: Generic view to display history summary
 *
 * AUTHOR:  	psedlak
 *
 * DATE:        09-23-2003
 *
 * HISTORY:
 */
%>

<BR CLEAR=ALL>
<HR>
<BR>
<TABLE>

<%

String m_strSvVrsn = "";

Connection conHist = null;
Statement stmtHist = null;
ResultSet rsHist = null;
try {

        conHist = DatabaseManager.getConnection();
        stmtHist = conHist.createStatement();

        String strQuery = "SELECT 0 as HIST_SEQ, " + thisOrder.getVRSN_COLUMN() + " as VRSN, " + thisOrder.getAttribute("HSTRY_STTS_CD_IN") +
		" as STTS, TO_CHAR(" + thisOrder.getAttribute("HSTRY_DT_IN") + ", '" +
                PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "') as HIST_DATE, " +
		thisOrder.getAttribute("HSTRY_DT_IN") + ", MDFD_USERID " +
                " FROM " + thisOrder.getAttribute("HSTRY_TBL_NAME") + " WHERE " + thisOrder.getSQNC_COLUMN() + " = " +
		m_strSqncNmbr +
                " UNION " +
                " SELECT O.DTL_HSTRY_SQNC_NMBR, 9999, '', TO_CHAR(O.MDFD_DT,'" +
                PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "'), O.MDFD_DT, O.MDFD_USERID " +
                " FROM ORDER_HISTORY_T O WHERE O.TYP_IND='" + thisOrder.getTYP_IND() + "' and O.SQNC_NMBR = " + m_strSqncNmbr +
                " ORDER BY 5, 1 DESC ";
        Log.write(Log.DEBUG_VERBOSE, " Histry query=["+ strQuery + "]");
        rsHist = stmtHist.executeQuery(strQuery);

        while(rsHist.next()==true)
        {	
		String strTempVrsn = rsHist.getString("VRSN");
		if ( (!strTempVrsn.equals(m_strSvVrsn)) && (!strTempVrsn.equals("9999")) )
		{
	%>
			<TR>
				<TD colspan=4>&nbsp;</TD>
			</TR>
			<TR>
				<TH colspan=4 align=left>VERSION&nbsp;<%=strTempVrsn%>&nbsp;<A HREF="<%=thisOrder.getAttribute("CONTROLLER")%>?seqget=<%=m_strSqncNmbr%>&amp;<%=thisOrder.getVRSN_COLUMN()%>=<%=strTempVrsn%>">View</A></TH>
			</TR>
	<%
			m_strSvVrsn = strTempVrsn;
		}
                String strDtlSeq = rsHist.getString("HIST_SEQ");
                String strStatus = rsHist.getString("STTS");
                String strMdfd = rsHist.getString("HIST_DATE") + " EST "  ;
                String strUserid = rsHist.getString("MDFD_USERID");
                if (strDtlSeq.equals("0"))      //Status change
                {
%>
                <TR>
                        <TD width=25>&nbsp;</TD>
                        <TD align=left>Status change:&nbsp;</TD>
                        <TD width=25>&nbsp;</TD>
                        <TD align=left><%=strStatus%></TD>
                        <TD width=25>&nbsp;</TD>
                        <TD align=left><%=strMdfd%></TD>
                        <TD align=left>&nbsp;&nbsp;&nbsp;<%=strUserid%></TD>
                        <TD width=25>&nbsp;</TD>
                </TR>
<%              } else {
%>
                        <TD width=25>&nbsp;</TD>
                        <TD align=right>Data:&nbsp;</TD>
                        <TD width=25>&nbsp;</TD>
                        <TD width=25 align=center>"</TD>
                        <TD width=25>&nbsp;</TD>
                        <TD align=left><%=strMdfd%></TD>
                        <TD align=left>&nbsp;&nbsp;&nbsp;<%=strUserid%></TD>
                        <TD >
                        <A HREF="<%=thisOrder.getAttribute("CONTROLLER")%>?<%=thisOrder.getSQNC_COLUMN()%>=<%=m_strSqncNmbr%>&amp;<%=thisOrder.getVRSN_COLUMN()%>=<%=m_iVrsn%>&amp;dtlhist=<%=strDtlSeq%>">&nbsp;details&nbsp;</A>
                        </TD>
                </TR>

<%              }

        }
} //try
catch (Exception e) {
        //apply log message to display exception here - Antony - 09/05/2012
        Log.write("Exception in HistoryView.jsp : "+e.getMessage());
            
        rsHist.close();
        rsHist=null;

        stmtHist.close();
        stmtHist = null;
}
finally {
        
        Log.write("Releasing connection object in HistoryView.jsp for conn object: "+conHist.toString());
        DatabaseManager.releaseConnection(conHist);
        Log.write("After releasing connection object in HistoryView.jsp.");
}

%>

</TABLE>
<BR>
<BR>
<BR>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/HistoryView.jsv  $
   
      Rev 1.0   Oct 06 2003 12:00:46   e0069884
   Make generic History views
/*
/* $Revision:   1.0  $
*/
%>
