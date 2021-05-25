<%@ include file="i_header.jsp" %>
<%
        final String CONTROLLER = "AsocTypeCtlr";
        if (!sdm.isAuthorized(CONTROLLER)) {
            Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
            alltelResponse.sendRedirect(SECURITY_URL);
        }
%>

<jsp:useBean id="asocTypeBean1" scope="request" class="com.alltel.lsr.common.objects.AsocTypeBean" />

<%
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
%>

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">ASOC CATEGORY&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="AsocTypeCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= asocTypeBean1.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

         <tr>
            <td align=right>ASOC&nbsp;CATEGORY</td> 
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="ASOC_TYPE" VALUE="<%= asocTypeBean1.getStrAsocType() %>"></td>
        </tr>
        
         <tr>
            <td align=right>ASOC&nbsp;DESCRIPTION</td> 
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="ASOC_DESCRIPTION" VALUE="<%= asocTypeBean1.getStrAsocDescrption() %>"></td>
        </tr>

       
 

        <input type="hidden" NAME="MDFD_USERID" VALUE="<%= asocTypeBean1.getMdfdUserid() %>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= asocTypeBean1.getMdfdDt() %>">
        <input type="hidden" NAME="ASOC_TYPE_CONFIG_SQNC_NMBR" VALUE="<%= asocTypeBean1.getStrAsocTypeConfigSeqNo() %>">
      
      
  
        <%	if (asocTypeBean1.getDbAction().equals("get") ||
        asocTypeBean1.getDbAction().equals("UpdateRow") ||
        asocTypeBean1.getDbAction().equals("DeleteRow")) {
                                %>
                                <tr>
        <td align=right>Modified&nbsp;Date:</td> 
            <td align=left><%= asocTypeBean1.getMdfdDt() %></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td> 
            <td align=left><%= asocTypeBean1.getMdfdUserid() %></td>
        </tr>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (asocTypeBean1.getDbAction().equals("new") ||
                asocTypeBean1.getDbAction().equals("InsertRow")) {
                %>
                                    <INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      }
                else if (asocTypeBean1.getDbAction().equals("get") ||
                asocTypeBean1.getDbAction().equals("UpdateRow") ||
                                    asocTypeBean1.getDbAction().equals("DeleteRow")) {

                                if (sdm.isAuthorized(asocTypeBean1.getTblAdmnScrtyTgMod()) ) {
                                    %>
                                    <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%
                }
                if (sdm.isAuthorized(asocTypeBean1.getTblAdmnScrtyTgDel()) ) {
                        %>
                        <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%
                    }
                    if (sdm.isAuthorized(asocTypeBean1.getTblAdmnScrtyTgMod()) ) {
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
 