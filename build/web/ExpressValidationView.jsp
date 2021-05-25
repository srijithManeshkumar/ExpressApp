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
 * MODULE:      ExpressValidationView.jsp
 *
 * DESCRIPTION: JSP View used to display Validation Errors. Combined mods, add try-catch
 *	and reduce queries.
 *
 * AUTHOR:      psedlak
 *
 * DATE:        9-29-2003
 *
 * HISTORY:
 *
 */
%>

<%
Log.write(Log.DEBUG_VERBOSE, "ExpressValidationView.jsp --- ");
%>

<table align=left width=100% border=0 cellspacing=1 cellpadding=0>
  <tr>
    <td>
      &nbsp;
    </td>
  </tr>
</table>
<BR CLEAR=ALL>
<HR>

<!-- perform validation edits and show the errors by Form/Section -->
<%

Connection conErrVld = null;
Statement stmtErrVld = null;
ResultSet rsErrVldQry = null;
try 
{
	conErrVld = DatabaseManager.getConnection();
	stmtErrVld = conErrVld.createStatement();

	String m_strErrVldQry = "";

	int iFrmSqncNmbr_ErrVldSv = 0;
	int iFrmSctnSqncNmbr_ErrVldSv = 0;
	int iFrmSctnOcc_ErrVldSv = 0;

	int iErrCnt = 0;
	int iRqstErrCnt = 0;

	Vector m_vValidationErrs = (Vector)request.getAttribute("FLD_VLDTN_ERR");
	if (m_vValidationErrs.size() > 0)
	{
	%>
		<H4>The Request has the following Field Validation Errors</H4>
	<%
	}
	else
	{
	%>
		<H4>The Request has no Field Validation Errors</H4>
	<%
	}
%>
<UL>
<%
	FormField ff;
	for(int i=0 ; i < m_vValidationErrs.size() ; i++)
	{
		ff = (FormField)m_vValidationErrs.elementAt(i);
		Log.write(Log.DEBUG_VERBOSE, "ExpressValidationView :  Field# = " + i + " ; FieldCd = " + ff.getFrmFldNmbr());

		if (ff.getFrmSqncNmbr() != iFrmSqncNmbr_ErrVldSv)
		{
%>
					</UL></UL>
<%
			iFrmSqncNmbr_ErrVldSv = ff.getFrmSqncNmbr();
			iFrmSctnSqncNmbr_ErrVldSv = ff.getFrmSctnSqncNmbr();
			iFrmSctnOcc_ErrVldSv = ff.getFrmSctnOcc();

			m_strErrVldQry = "SELECT F.FRM_DSCRPTN, FS.FRM_SCTN_DSCRPTN FROM FORM_T F, FORM_SECTION_T FS " +
				" WHERE F.FRM_SQNC_NMBR = " + ff.getFrmSqncNmbr() + " AND F.FRM_SQNC_NMBR =FS.FRM_SQNC_NMBR " +
				" AND FS.FRM_SCTN_SQNC_NMBR = " +  ff.getFrmSctnSqncNmbr();

			rsErrVldQry = stmtErrVld.executeQuery(m_strErrVldQry);
			rsErrVldQry.next();
%>
			<LI type=disc><%=rsErrVldQry.getString("FRM_DSCRPTN")%><UL>
			<LI type=round><%=rsErrVldQry.getString("FRM_SCTN_DSCRPTN")%><%if (ff.getFrmSctnOcc() > 0) { %>(<%=ff.getFrmSctnOcc()%>)<%}%><UL>
<%	
			rsErrVldQry.close();
			rsErrVldQry = null;
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
				m_strErrVldQry = "SELECT FRM_SCTN_DSCRPTN FROM FORM_SECTION_T WHERE FRM_SQNC_NMBR = " +
					ff.getFrmSqncNmbr() + " AND FRM_SCTN_SQNC_NMBR = " + ff.getFrmSctnSqncNmbr();
				rsErrVldQry = stmtErrVld.executeQuery(m_strErrVldQry);
				rsErrVldQry.next();
%>
				<LI type=round><%=rsErrVldQry.getString("FRM_SCTN_DSCRPTN")%><%if (ff.getFrmSctnOcc() > 0) { %>(<%=ff.getFrmSctnOcc()%>)<%}%><UL>
<%
				rsErrVldQry.close();
				rsErrVldQry = null;
			}
		}
%>
		<LI type=square><%=ff.getFldCd()%> -- <%=ff.getFrmFldNmbr()%> : <%=ff.getFldVldtnMsg()%>
<%
	}

} //try
catch (Exception e) {
        //apply log message to display exception here - Antony - 09/05/2012
        Log.write("Exception in ExpressValidationView.jsp : "+e.getMessage());
            
        rsErrVldQry.close();
        rsErrVldQry=null;

        stmtErrVld.close();
        stmtErrVld = null;
}
finally {
    
        Log.write("Releasing connection object in ExpressValidationView.jsp for conn object: "+conErrVld.toString());
        DatabaseManager.releaseConnection(conErrVld);
        Log.write("After releasing connection object in ExpressValidationView.jsp.");
}

%>
</UL></UL>
<BR>
<BR>
<BR>

<%
/* $Log:   $
/*
*/

/* $Revision:    $
*/

%>
