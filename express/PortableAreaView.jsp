<% try{%>
<%@ include file="i_header.jsp" %>
<%
        final String CONTROLLER = "PortableAreaCtlr";
        if (!sdm.isAuthorized(CONTROLLER)) {
            Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
            alltelResponse.sendRedirect(SECURITY_URL);
        }
%>

<jsp:useBean id="portableAreaBean" scope="request" class="com.alltel.lsr.common.objects.PortableAreaBean" />

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">PORTABLE&nbsp;AREA&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="PortableAreaCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= portableAreaBean.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

       
         <tr>
             <td align=right>PORTABLE&nbsp;AREA&nbsp;NAME</td> 
            <td align=left><select NAME="PORTABLE_AREA_NAME_SQNC_NMBR">

                <%
                    Connection con = DatabaseManager.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM PORTABLE_AREA_NAME_T ORDER BY PORTABLE_AREA_NAME ASC");
               
                    while (rs.next() == true) {
                        if (portableAreaBean.getStrPortableAreaNameSqncNo().equals(rs.getString("PORTABLE_AREA_NAME_SQNC_NMBR"))) {
                    %>
                    <option value=<%= rs.getString("PORTABLE_AREA_NAME_SQNC_NMBR") %> SELECTED><%= rs.getString("PORTABLE_AREA_NAME") %>
                <%	}
                else
                {
                                %>
                                
                                <option value=<%= rs.getString("PORTABLE_AREA_NAME_SQNC_NMBR") %>><%= rs.getString("PORTABLE_AREA_NAME") %>
                <%
                }
                }
                        DatabaseManager.releaseConnection(con);
                        %>

                </td>
        </tr>

        <input type="hidden" NAME="MDFD_USERID" VALUE="<%= portableAreaBean.getMdfdUserid() %>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= portableAreaBean.getMdfdDt() %>">
        <input type="hidden" NAME="VENDOR_CONFIG_SQNC_NMBR" VALUE="<%= session.getAttribute("vendorConfigSqncNumber")%>">
        <input type="hidden" NAME="PORTABLE_AREA_SQNC_NO" VALUE="<%= portableAreaBean.getStrPortableAreaSqncNumber() %>">

        <%	if (portableAreaBean.getDbAction().equals("get") ||
                portableAreaBean.getDbAction().equals("UpdateRow") ||
                portableAreaBean.getDbAction().equals("DeleteRow")) {
        %>
        <tr>
            <td align=right>Modified&nbsp;Date:</td> 
            <td align=left><%= portableAreaBean.getMdfdDt() %></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td> 
            <td align=left><%= portableAreaBean.getMdfdUserid() %></td>
        </tr>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (portableAreaBean.getDbAction().equals("new") ||
                portableAreaBean.getDbAction().equals("InsertRow")) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      }
                    else if (portableAreaBean.getDbAction().equals("get") ||
                portableAreaBean.getDbAction().equals("UpdateRow") ||
                portableAreaBean.getDbAction().equals("DeleteRow")) {

            if (sdm.isAuthorized(portableAreaBean.getTblAdmnScrtyTgMod()) ) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%
                    }
                    if (sdm.isAuthorized(portableAreaBean.getTblAdmnScrtyTgDel()) ) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%
                    }
                    if (sdm.isAuthorized(portableAreaBean.getTblAdmnScrtyTgMod()) ) {
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

<%}catch(Exception e){
    e.printStackTrace();
}
    /* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UserGroupView.jsv  $
            /*
            /*   Rev 1.1   31 Jan 2002 14:47:12   sedlak
            /*
            /*
            /*   Rev 1.0   23 Jan 2002 11:06:58   wwoods
            /*Initial Checkin
              */

            /* $Revision:   1.1  $
              */
%>
