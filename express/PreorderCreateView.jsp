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
 * MODULE:   PreorderCreateView.jsp   
 * 
 * DESCRIPTION: JSP View used to create new Preorders
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *   06/06/2002 psedlak   Default Activity to only element if only one item in drop down box
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
final String EXPRESS_FUNCTION = "CREATE_PREORDERS";
if (!sdm.isAuthorized(EXPRESS_FUNCTION))
{
   Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + EXPRESS_FUNCTION);
   alltelResponse.sendRedirect(SECURITY_URL);
}

Connection conRC = null;
Statement stmtRC = null;
ResultSet rsRC = null;

conRC = DatabaseManager.getConnection();
stmtRC = conRC.createStatement();


String m_strPreNewErrorMsg = (String) request.getAttribute("prenew_errormsg");
if ((m_strPreNewErrorMsg == null) || (m_strPreNewErrorMsg.length() == 0))
{
   m_strPreNewErrorMsg = "";
}

String m_strOCNSttSqncNmbr = request.getParameter("prenew_ocnsttsqnc");
if ((m_strOCNSttSqncNmbr == null) || (m_strOCNSttSqncNmbr.length() == 0))
{
   m_strOCNSttSqncNmbr = "";
}

String m_strPreSrvcTyp = request.getParameter("prenew_srvctyp");
if ((m_strPreSrvcTyp == null) || (m_strPreSrvcTyp.length() == 0))
{
   m_strPreSrvcTyp = "";
}

String m_strPreActvtyTyp = request.getParameter("prenew_actvtytyp");
if ((m_strPreActvtyTyp == null) || (m_strPreActvtyTyp.length() == 0))
{
   m_strPreActvtyTyp = "";
}

%>

<SCRIPT LANGUAGE="JavaScript">

var arrSrvcTypCd = new Array();
var arrActvtyTypMOXIDX = new Array();

var arrSrvcTypCd = new Array();
var arrSrvcTypDesc = new Array();
var arrActvtyTypCd = new Array();
var arrActvtyTypDesc = new Array();

<%
rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'P' ORDER BY SRVC_TYP_CD ASC");

int stc = 0;
while (rsRC.next() == true)
{
%>
   arrSrvcTypCd[<%=stc%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
   arrSrvcTypDesc[<%=stc%>] = "<%=rsRC.getString("SRVC_TYP_DSCRPTN")%>";
<%
   stc++;
}
%>

<%
rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'P' ORDER BY ACTVTY_TYP_CD ASC");

int at = 0;
while (rsRC.next() == true)
{
%>
   arrActvtyTypCd[<%=at%>] = "<%=rsRC.getString("ACTVTY_TYP_CD")%>";
   arrActvtyTypDesc[<%=at%>] = "<%=rsRC.getString("ACTVTY_TYP_DSCRPTN")%>";
<%
   at++;
}
%>

function setActvtyTypSelect(PreTypControl, ActvtyTypControl)
{
  // Clear the options in the Activity Type Select Control
  for (var z=ActvtyTypControl.options.length ; z >= 1 ; z--) ActvtyTypControl.options[z]=null;

  var ActvtyTypeOption ;
  var NbrInList = 0;

  for (var x = 0 ; x < arrSrvcTypCd.length  ; x++ )
  {
   if ( arrSrvcTypCd[x] == PreTypControl.value )
    {
     for (var y = 0 ; y < arrActvtyTypCd.length  ; y++ )
     {
       if (arrActvtyTypMOXIDX[x].indexOf(arrActvtyTypCd[y]) >= 0) 
      {
         ActvtyTypeOption = document.createElement("option") ;
         ActvtyTypeOption.value = arrActvtyTypCd[y] ;
         ActvtyTypeOption.text = arrActvtyTypDesc[y];
         ActvtyTypControl.add(ActvtyTypeOption) ;
         NbrInList++;
      }
     }
   }
  }
  //if only one element, then default to it
  if (NbrInList == 1)
  {
   ActvtyTypControl.options[1].selected = true;
  }
}

</SCRIPT>

<FORM NAME="PreorderCreateView" METHOD=POST ACTION="PreorderCtlr">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;P&nbsp;R&nbsp;E&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%=m_strPreNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=right>OCN&nbsp;State:&nbsp;</TD>
   <TD align=left><SELECT NAME="prenew_ocnsttsqnc">
      <option value="">... Select an OCN/State ...</OPTION>
   <%

   // Get All OCN Codes for this users user groups
   String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
      "(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
   rsRC = stmtRC.executeQuery(strQuery);

   String strInClause = "";
   while (rsRC.next() == true)
   {
      if (rsRC.getString("OCN_CD").equals("*"))
      {
         String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + rsRC.getInt("CMPNY_SQNC_NMBR");
         Statement stmtRC2 = null;
         stmtRC2 = conRC.createStatement();
         ResultSet rsRC2 = stmtRC2.executeQuery(strSubQuery);
         while (rsRC2.next() == true)
         {
            strInClause = strInClause + "'" + rsRC2.getString("OCN_CD") + "',";
         }
         rsRC2.close();
      }
      else
      {
         strInClause = strInClause + "'" + rsRC.getString("OCN_CD") + "',";
      }
   }
   rsRC.close();

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

      rsRC = stmtRC.executeQuery(strQuery);
      while (rsRC.next() == true)
      {
   %>
         <option value=<%= rsRC.getString("OCN_STT_SQNC_NMBR") %> <% if ( m_strOCNSttSqncNmbr.equals(rsRC.getString("OCN_STT_SQNC_NMBR"))) { %> SELECTED <% } %> ><%= rsRC.getString("OCN_CD") %> - <%= rsRC.getString("STT_CD") %></OPTION>
   <%
      }
      rsRC.close();
   }

   %>
   </SELECT>
   </TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=right>PreOrder&nbsp;Transaction&nbsp;Type:&nbsp;</TD>
   <TD align=left><SELECT id="prenew_srvctyp" NAME="prenew_srvctyp" onchange="setActvtyTypSelect(this, PreorderCreateView.prenew_actvtytyp);">
      <option value="">... Select a Service Type ...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
   <%
   rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN, ACTVTY_TYP_MOX_IDX FROM SERVICE_TYPE_T WHERE TYP_IND = 'P' ORDER BY SRVC_TYP_CD ASC");
   int st = 0;
   while (rsRC.next() == true)
   {
   %>
      <option value=<%= rsRC.getString("SRVC_TYP_CD") %> <% if ( m_strPreSrvcTyp.equals(rsRC.getString("SRVC_TYP_CD"))) { %> SELECTED <% } %>><%= rsRC.getString("SRVC_TYP_CD") %> - <%= rsRC.getString("SRVC_TYP_DSCRPTN") %></option>

      <SCRIPT LANGUAGE="JavaScript">
      arrSrvcTypCd[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
      arrActvtyTypMOXIDX[<%=st%>] = "<%=rsRC.getString("ACTVTY_TYP_MOX_IDX")%>";
      </SCRIPT>
   <%
      st++;
   }
   rsRC.close();
   %>
   </SELECT>
   </TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=right>Activity Type:&nbsp;</TD>
   <TD align=left>
      <SELECT id="prenew_actvtytyp" NAME="prenew_actvtytyp">
         <option value="" SELECTED>... Select an Activity Type ...</option>
      </SELECT>
   </TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
   <TD align=center colspan=2>
   <INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
   <INPUT class=appButton TYPE="SUBMIT" NAME="prenew" VALUE="Submit">&nbsp;&nbsp;
   <INPUT class=appButton TYPE="SUBMIT" NAME="prenew" VALUE="Cancel">
   </TD>
  </TR>
 
</TABLE>

</FORM>
<%
DatabaseManager.releaseConnection(conRC);
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
