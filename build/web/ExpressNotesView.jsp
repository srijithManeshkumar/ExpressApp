<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	ExpressNotesView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        09-29-2003
 *  EK     05/21/2006 added windstream Rebranding
 * 
 * HISTORY:
 *
 */
%>

<%
Log.write(Log.DEBUG_VERBOSE, "ExpressNotesView.jsp --- ");
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

<%
if (sdm.isAuthorized(UPDATE_NOTES_TAG))
{
%>
	<table>
	<tr>
	<td width=25>&nbsp;</td>
	<td colspan=3><TEXTAREA NAME=notestext ROWS=5 COLS=100 WRAP></TEXTAREA></td>
	</tr>
	<tr>
	<td width=25>&nbsp;</td>
	<%
	if("RequestCtlr".equalsIgnoreCase(m_CTLR)){
	%>
	<td><INPUT class=appButton TYPE="BUTTON" NAME="notes_update" VALUE="Save Notes" onClick="return submitForm(this);"></td>
	<%
	}else{
	%>
	<td><INPUT class=appButton TYPE="SUBMIT" NAME="notes_update" VALUE="Save Notes"></td>
	<%
	}
	%>
	<td width=25>&nbsp;</td>
	<td><INPUT class=appButton  TYPE="RESET"></td>
	</tr>
	</table>
	<BR CLEAR=ALL>
	<HR>
	<BR>
<%
}
%>

<UL>
<%
Connection conNotes = null;
Statement stmtNotes = null;
ResultSet rsNotes = null;
try {
	conNotes = DatabaseManager.getConnection();
	stmtNotes = conNotes.createStatement();

	String m_strNotesQry = "SELECT MDFD_DT, NTS_TXT, MDFD_USERID FROM " + myorder.getAttribute("NOTES_TBL_NAME") + " WHERE " +
		myorder.getSQNC_COLUMN() + " = " + m_iSqncNmbr + " ORDER BY MDFD_DT";
	rsNotes = stmtNotes.executeQuery(m_strNotesQry);

	while(rsNotes.next()==true) 
	{
%>
	<LI type=disc><%=rsNotes.getString("MDFD_DT")%>&nbsp;-&nbsp;<%=rsNotes.getString("MDFD_USERID")%>
	<UL>
		<LI type=round><%=rsNotes.getString("NTS_TXT")%>
	</UL>
	<BR>
<%
	}
} //try
catch (Exception e) {
	Log.write(Log.ERROR, "ExpressNotesView.jsp trapped exception [" + e +"]" );
        //apply log message to display exception here - Antony - 09/05/2012
        Log.write("Exception in ExpressNotesView.jsp : "+e.getMessage());
        
        rsNotes.close();
        rsNotes=null;

        stmtNotes.close();
        stmtNotes = null;
}
finally {
    
        Log.write("Releasing connection object in ExpressNotesView.jsp for conn object: "+conNotes.toString());
        DatabaseManager.releaseConnection(conNotes);
        Log.write("After releasing connection object in ExpressNotesView.jsp.");
}



%>
</UL>
<BR>
<BR>
