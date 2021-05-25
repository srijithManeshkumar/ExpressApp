<%@ include file="i_header.jsp" %>
<%
        final String CONTROLLER = "PortableAreaNameCtlr";
        if (!sdm.isAuthorized(CONTROLLER)) {
            Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
            alltelResponse.sendRedirect(SECURITY_URL);
        }
%>

<jsp:useBean id="portableAreaNameBean1" scope="request" class="com.alltel.lsr.common.objects.PortableAreaNameBean" />

<%
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
%>

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">PortableAreaName&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="PortableAreaNameCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= portableAreaNameBean1.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

         <tr>
            <td align=right>PORTABLE&nbsp;AREA&nbsp;NAME</td> 
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="PORTABLE_AREA_NAME" VALUE="<%= portableAreaNameBean1.getStrPortableAreaName() %>"></td>
        </tr>
        
         <input type="hidden" NAME="MDFD_USERID" VALUE="<%= portableAreaNameBean1.getMdfdUserid() %>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= portableAreaNameBean1.getMdfdDt() %>">
        <input type="hidden" NAME="PORTABLE_AREA_NAME_SQNC_NMBR" VALUE="<%= portableAreaNameBean1.getStrPortableAreaNameConfigSeqNo() %>">
      
      
  
        <%	if (portableAreaNameBean1.getDbAction().equals("get") ||
        portableAreaNameBean1.getDbAction().equals("UpdateRow") ||
        portableAreaNameBean1.getDbAction().equals("DeleteRow")) {
                                %>
                                <tr>
        <td align=right>Modified&nbsp;Date:</td> 
            <td align=left><%= portableAreaNameBean1.getMdfdDt() %></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td> 
            <td align=left><%= portableAreaNameBean1.getMdfdUserid() %></td>
        </tr>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (portableAreaNameBean1.getDbAction().equals("new") ||
                portableAreaNameBean1.getDbAction().equals("InsertRow")) {
                %>
                                    <INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      }
                else if (portableAreaNameBean1.getDbAction().equals("get") ||
                portableAreaNameBean1.getDbAction().equals("UpdateRow") ||
                                    portableAreaNameBean1.getDbAction().equals("DeleteRow")) {

                                if (sdm.isAuthorized(portableAreaNameBean1.getTblAdmnScrtyTgMod()) ) {
                                    %>
                                    <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%
                }
                if (sdm.isAuthorized(portableAreaNameBean1.getTblAdmnScrtyTgDel()) ) {
                        %>
                        <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%
                    }
                    if (sdm.isAuthorized(portableAreaNameBean1.getTblAdmnScrtyTgMod()) ) {
                %>
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <%
                }
                %>
                                    <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      }
                %>

            </td>
        </tr>

    </table>

</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
 