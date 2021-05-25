<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				Windstream INFORMATION SERVICES
 */
/** 
 * MODULE:		DwoLockView.jsp
 * 
 * DESCRIPTION: Displays a list of locked dwos and provides a link to unlock it.
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002  Initial Check-in
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 
<%
	final String SECURITY_OBJECT = "UNLOCK_DWOS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}

String strPJVN = alltelRequest.getParameter("pjvn");
String m_strTypInd = "";

if (strPJVN == null)
{
        Log.write(Log.WARNING, sdm.getUser() + " is hitting this page without order type set in session");
        alltelResponse.sendRedirect(SECURITY_URL);
}
else if (strPJVN.equals("19")) // KPEN
{
	m_strTypInd = "W";
	alltelRequest.getSession().setAttribute("DwOcHoIcE", new String("19"));
}
else  // BDP
{
	m_strTypInd = "X";
	alltelRequest.getSession().setAttribute("DwOcHoIcE", new String("21"));
}
	
%>

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Dwo&nbsp;Locks</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>

<TABLE width="75%" align=center border=1 cellspacing=0 cellpadding=0>
<TH align=center>Dwo&nbsp;Number</TH>
<TH align=center>Userid</TH>
<TH align=center>User&nbsp;Name</TH>
<TH align=center>OCN&nbsp;Code,&nbsp;State&nbsp;and&nbsp;Description</TH>
<TH align=center>Lock&nbsp;Date/Time</TH>
<TH align=center>UNLOCK</TH>

<%
   //added try block here as there was no try catch finally for db exceptions
// Antony - 09/17/2012

Log.write(Log.DEBUG_VERBOSE, "DwoLockView.jsp --- before try block. ");

try {
	String strQuery = "SELECT TL.SQNC_NMBR, TL.USERID,  " +
		" U.FRST_NM, U.LST_NM, T.OCN_CD, O.OCN_NM, TO_CHAR(TL.LCK_DT, 'MM/DD/YYYY @ HH24:MI:SS') " +
		" FROM LOCK_T TL, USERID_T u, DWO_T T, ocn_t o " +
		" WHERE TL.USERID = U.USERID AND TL.SQNC_NMBR = T.DWO_SQNC_NMBR AND TL.TYP_IND = '" + m_strTypInd + "'" +
		" AND T.OCN_CD = O.OCN_CD " +
		" ORDER BY T.DWO_SQNC_NMBR ASC";
	Connection con = DatabaseManager.getConnection();
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery);
	while (rs.next() == true)
	{
%>
		<TR>
			<TD align=left>&nbsp;<%= rs.getString(1) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(2) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(4) %>,&nbsp;<%= rs.getString(3) %></TD>
			<TD align=center><%=rs.getString(5)%>-<%=rs.getString(6)%></TD>
			<TD align=center><%= rs.getString(7) %></TD>
			<TD align=center><A HREF="DwoUnlockCtlr?dwo=<%=rs.getString(1)%>">&nbsp;Unlock&nbsp;Dwo&nbsp;</A></TD>
			
		</TR>
<%
	} //while
        
        Log.write("DwoLockView.jsp ! Releasing connection object :"+con.toString());
        
	DatabaseManager.releaseConnection(con);
        
        Log.write("DwoLockView.jsp ! Released connection object.");
        
    }//end of second try -- Added catch and finally blocks below -- Antony -- 09/17/2012
    catch(Exception e) {
        Log.write("Exception in DwoLockView.jsp. Error Message :"+e.getMessage());

    } finally { 

            if(!con.isClosed()) {
                Log.write("Inside finally block in DwoLockView.jsp ! Releasing connection object :"+con.toString());

                DatabaseManager.releaseConnection(con);

                Log.write("Inside finally block in DwoLockView.jsp ! Released connection object.");
            }
    }
    
        
        
%>
</TABLE>
<BR CLEAR=ALL><BR>
<BR>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
