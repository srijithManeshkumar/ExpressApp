<%-- 
    Document   : ComplexAsocTypeView
    Created on : May 25, 2011, 12:01:50 PM
    Author     : satish.t
--%>
<%@ include file="i_header.jsp" %>
<%
            final String CONTROLLER = "ComplexAsocTypeCtlr";
            if (!sdm.isAuthorized(CONTROLLER)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
                alltelResponse.sendRedirect(SECURITY_URL);
            }
%>

<jsp:useBean id="complexasocTypeBean" scope="request" class="com.alltel.lsr.common.objects.ComplexAsocTypeBean" />

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
            <SPAN class="barheader">COMPLEX ASOCS &nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="ComplexAsocTypeCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= complexasocTypeBean.getErrMsg()%>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

        <tr>
            <td align=right>ASOC&nbsp;CATEGORY</td>
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="ASOC_TYPE" VALUE="<%= complexasocTypeBean.getStrAsocType()%>"></td>
        </tr>

        <tr>
            <td align=right>ASOC&nbsp;DESCRIPTION</td>
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="ASOC_DESCRIPTION" VALUE="<%= complexasocTypeBean.getStrAsocDescrption()%>"></td>
        </tr>




        <input type="hidden" NAME="MDFD_USERID" VALUE="<%= complexasocTypeBean.getMdfdUserid()%>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= complexasocTypeBean.getMdfdDt()%>">
        <input type="hidden" NAME="COMPLEX_ASOC_SQNC_NMBR" VALUE="<%= complexasocTypeBean.getStrAsocTypeConfigSeqNo()%>">



        <%	if (complexasocTypeBean.getDbAction().equals("get")
                            || complexasocTypeBean.getDbAction().equals("UpdateRow")
                            || complexasocTypeBean.getDbAction().equals("DeleteRow")) {
        %>
        <tr>
            <td align=right>Modified&nbsp;Date:</td>
            <td align=left><%= complexasocTypeBean.getMdfdDt()%></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td>
            <td align=left><%= complexasocTypeBean.getMdfdUserid()%></td>
        </tr>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (complexasocTypeBean.getDbAction().equals("new")
                                    || complexasocTypeBean.getDbAction().equals("InsertRow")) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      } else if (complexasocTypeBean.getDbAction().equals("get")
                              || complexasocTypeBean.getDbAction().equals("UpdateRow")
                              || complexasocTypeBean.getDbAction().equals("DeleteRow")) {

                          if (sdm.isAuthorized(complexasocTypeBean.getTblAdmnScrtyTgMod())) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%                          }
                          if (sdm.isAuthorized(complexasocTypeBean.getTblAdmnScrtyTgDel())) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%                          }
                          if (sdm.isAuthorized(complexasocTypeBean.getTblAdmnScrtyTgMod())) {
                %>
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <%                          }
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

