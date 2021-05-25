<%
/**
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *         COPYRIGHT (C) 2002
 *            BY
 *         Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:   TicketCreateView.jsp   
 * 
 * DESCRIPTION: JSP View used to create new Tickets
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        3-20-2002
 * 
 * HISTORY:
 *   11/20/2002   psedlak   Removed KY restriction.
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
final String EXPRESS_FUNCTION = "CREATE_TICKETS";
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


String m_strTcktNewErrorMsg = (String) request.getAttribute("tcktnew_errormsg");
if ((m_strTcktNewErrorMsg == null) || (m_strTcktNewErrorMsg.length() == 0))
{
   m_strTcktNewErrorMsg = "";
}

String m_strOCNSttSqncNmbr = request.getParameter("tcktnew_ocnsttsqnc");
if ((m_strOCNSttSqncNmbr == null) || (m_strOCNSttSqncNmbr.length() == 0))
{
   m_strOCNSttSqncNmbr = "";
}

%>

<FORM NAME="TicketCreateView" METHOD=POST ACTION="TicketCtlr">

<TABLE width=30% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;T&nbsp;I&nbsp;C&nbsp;K&nbsp;E&nbsp;T</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%=m_strTcktNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=right>OCN&nbsp;State:&nbsp;</TD>
   <TD align=left><SELECT NAME="tcktnew_ocnsttsqnc">
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
      // Restricting for Kentucky
      //strQuery = "SELECT OCN_STT_SQNC_NMBR, OCN_CD, STT_CD FROM OCN_STATE_T WHERE OCN_CD IN (" + strInClause + ") AND STT_CD = 'KY'";
      // 12-2-2002 Removing KY restriction   
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
   <INPUT class=appButton TYPE="SUBMIT" NAME="tcktnew" VALUE="Submit">&nbsp;&nbsp;
   <INPUT class=appButton TYPE="SUBMIT" NAME="tcktnew" VALUE="Cancel">
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
