<%@ include file="i_header.jsp" %>
<% try{
                                                                            final String CONTROLLER =
                                                                                    "VendorAutomationCtlr";
                                                                            if (!sdm.isAuthorized(CONTROLLER)) {
                                                                                Log.write(Log.WARNING,
                                                                                        sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);

                                                                                alltelResponse.sendRedirect(SECURITY_URL);
                                                                            }
%>

<jsp:useBean id="beanVendorAutomation1" scope="request" 
class="com.alltel.lsr.common.objects.VendorBean" />

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">Vendor&nbsp;Automation&nbsp;Configuration&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="VendorAutomationCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= 
            beanVendorAutomation1.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <% String action1= request.getParameter("action1");
            if(action1!=null && action1.equals("norecords")){%>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%="No Records Available.Please modify search criteria and try again." %>
        &nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>                                                                  
        <%} else if(action1!=null && action1.equals("nosearch")){%>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%="Please Select the combo box" %>
        &nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>                                                                  
        <%} else if(action1!=null && action1.equals("updatesucess")){%>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%="Successfully Updated" %>
        &nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>                                                                  
        <%}%>   
   
       
     
        <tr>
            <td align=right>Company:</td> 
            <td align=left><select NAME="CMPNY_SQNC_NMBR" multiple=true>
              
                <%
                    Connection con = DatabaseManager.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(" SELECT * FROM COMPANY_T WHERE CMPNY_SQNC_NMBR IN(SELECT distinct(CMPNY_SQNC_NMBR) FROM " +
                            "VENDOR_TABLE_CONFIG_T) ORDER BY CMPNY_NM ASC  ");

                    while (rs.next() == true) {

                %>
                <option value="<%= rs.getString("CMPNY_SQNC_NMBR") %>" ><%= 
                    rs.getString("CMPNY_NM") %>
                <% }%>
                
            </td>
        </tr>
        <tr>
            <td 
                align=right>&nbsp;Operating&nbsp;Company&nbsp;Number&nbsp;for&nbsp;selected&nbsp;Vendor
            </td> 
            <td align=left><select NAME="OCN_CD" multiple=true>
                   
                <% 
                    rs = stmt.executeQuery("SELECT distinct(OCN_CD) FROM VENDOR_TABLE_CONFIG_T order by OCN_CD ");
                    String ocnStr="";
                    while (rs.next() == true) {
                        ocnStr=rs.getString("OCN_CD");
                %>
                <option value="<%=ocnStr%>" ><%=ocnStr%>
                <%	} %>
                   
                
            </td>
        </tr>
        <tr>
            <td align=right>&nbsp;State&nbsp;for&nbsp;selected&nbsp;OCN</td> 
            <td align=left><select NAME="STT_CD" multiple=true>
                     
                <% 
                    rs = stmt.executeQuery("SELECT distinct(STT_CD) FROM VENDOR_TABLE_CONFIG_T");
                    while (rs.next() == true) {
                %>
                <option value="<%=rs.getString("STT_CD") %>" ><%= 
                    rs.getString("STT_CD") %>
                <% }%>
                
            </td>
        </tr>
        <tr>
            <td align=right>&nbsp;Service&nbsp;Type</td> 
            <td align=left><select NAME="SRVC_TYP_CD" multiple=true>
                    
                <% 
                    rs = stmt.executeQuery("SELECT distinct(SRVC_TYP_CD) FROM VENDOR_TABLE_CONFIG_T order by SRVC_TYP_CD");
                    String serStr="";
                    while (rs.next() == true) {
                        serStr=rs.getString("SRVC_TYP_CD");
                %>
                <option value="<%=serStr%>" ><%=serStr%>
                <%	} %>
                  
                
            </td>
        </tr>
        <tr>
            <td align=right>Activity&nbsp; Type</td> 
            <td align=left><select NAME="ACTVTY_TYP_CD" multiple=true>
                   
                <% 
                    rs = stmt.executeQuery("SELECT distinct(ACTVTY_TYP_CD) FROM VENDOR_TABLE_CONFIG_T order by ACTVTY_TYP_CD");
                    String actStr="";
                    while (rs.next() == true) {
                        actStr=rs.getString("ACTVTY_TYP_CD");
                %>
                <option value="<%=actStr %>" ><%=actStr%>
                <% } %>
                   
                
            </td>
        </tr>
        <% DatabaseManager.releaseConnection(con); %>   
        <tr>
            <td align=center> 
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Search">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action1" value="Cancel">
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

%>
    