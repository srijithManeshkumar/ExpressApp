<%
/**
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *         COPYRIGHT (C) 2003
 *            BY
 *         Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:   BillDisputeCreateView.jsp   
 * 
 * DESCRIPTION: JSP View used to create new Billing Disputes
 * 
 * AUTHOR:      Vince Pavill
 * 
 * DATE:        1-14-2003
 * 
 * HISTORY:
 *   03/15/2003 Initial Check-in
 *
 *   09/13/2007 Scott Culbertson HD2400817
 *              Replace:
 *                 strQuery = "SELECT OCN_STT_SQNC_NMBR, OCN_CD, STT_CD
 *                             FROM OCN_STATE_T WHERE OCN_CD IN
 *                                  (" + strInClause + ") ";
 *              With:
 *                 strQuery = "SELECT " +
 *                               "OCN_STT_SQNC_NMBR, " +
 *                               "OCN_CD, " +
 *                               "STT_CD " +
 *                            "FROM " +
 *                               "OCN_STATE_T " +
 *                            "WHERE " +
 *                               "DISPLAY_IND = 'Y' AND " +
 *                               "OCN_CD IN (" + strInClause + ")";
 *
 */
%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%

//Does user belong here?
final String EXPRESS_FUNCTION = "CREATE_DISPUTES";
if (!sdm.isAuthorized(EXPRESS_FUNCTION))
{
   Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + EXPRESS_FUNCTION);
   alltelResponse.sendRedirect(SECURITY_URL);
}

Connection conTC = null;
Statement stmtTC = null;
ResultSet rsTC = null;

conTC = DatabaseManager.getConnection();
stmtTC = conTC.createStatement();


String m_strNewErrorMsg = (String) request.getAttribute("new_errormsg");
if ((m_strNewErrorMsg == null) || (m_strNewErrorMsg.length() == 0))
{
   m_strNewErrorMsg = "";
}

String m_strOCNSttSqncNmbr = request.getParameter("new_ocnsttsqnc");
if ((m_strOCNSttSqncNmbr == null) || (m_strOCNSttSqncNmbr.length() == 0))
{
   m_strOCNSttSqncNmbr = "";
}

%>

<FORM NAME="BillDisputeCreateView" METHOD=POST ACTION="BillDisputeCtlr">

<TABLE width=30% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;B&nbsp;I&nbsp;L&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;I&nbsp;S&nbsp;P&nbsp;U&nbsp;T&nbsp;E&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%=m_strNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=right>OCN&nbsp;State:&nbsp;</TD>
   <TD align=left><SELECT NAME="new_ocnsttsqnc">
      <option value="">... Select an OCN/State ...</OPTION>
   <%

   // Get All OCN Codes for this users user groups
   String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
      "(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
   rsTC = stmtTC.executeQuery(strQuery);

   String strInClause = "";
   while (rsTC.next() == true)
   {
      if (rsTC.getString("OCN_CD").equals("*"))
      {
         String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + rsTC.getInt("CMPNY_SQNC_NMBR");
         Statement stmtTC2 = null;
         stmtTC2 = conTC.createStatement();
         ResultSet rsTC2 = stmtTC2.executeQuery(strSubQuery);
         while (rsTC2.next() == true)
         {
            strInClause = strInClause + "'" + rsTC2.getString("OCN_CD") + "',";
         }
         rsTC2.close();
      }
      else
      {
         strInClause = strInClause + "'" + rsTC.getString("OCN_CD") + "',";
      }
   }
   rsTC.close();

   // strip off last comma
   if (strInClause.endsWith(","))  
      strInClause = strInClause.substring(0,strInClause.length()-1);

   if (strInClause.length() > 0)
   {
      // Get the rest of the data
      strQuery = "SELECT " +
                    "OCN_STT_SQNC_NMBR, " +
                    "OCN_CD, " +
                    "STT_CD " +
                 "FROM " +
                    "OCN_STATE_T " +
                 "WHERE " +
                    "DISPLAY_IND = 'Y' AND " +
                    "OCN_CD IN (" + strInClause + ")";
      
      rsTC = stmtTC.executeQuery(strQuery);
      while (rsTC.next() == true)
      {
   %>
         <option value=<%= rsTC.getString("OCN_STT_SQNC_NMBR") %> <% if ( m_strOCNSttSqncNmbr.equals(rsTC.getString("OCN_STT_SQNC_NMBR"))) { %> SELECTED <% } %> ><%= rsTC.getString("OCN_CD") %> - <%= rsTC.getString("STT_CD") %></OPTION>
   <%
      }
      rsTC.close();
   }

   %>
   </SELECT>
   </TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=center colspan=2>
   <INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
   <INPUT class=appButton TYPE="SUBMIT" NAME="disputenew" VALUE="Submit">&nbsp;&nbsp;
   <INPUT class=appButton TYPE="SUBMIT" NAME="disputenew" VALUE="Cancel">
   </TD>
  </TR>
 
</TABLE>

</FORM>


<%
DatabaseManager.releaseConnection(conTC);
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
