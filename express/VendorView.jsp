
<SCRIPT language=JavaScript>
    function showOCN(){
    document.VendorCtlr1.key1.value="ocn";
    document.VendorCtlr1.action1.value="InsertRow";
    document.VendorCtlr1.submit();  
   
    }
    
    function showState(){
    document.VendorCtlr1.key1.value="state";
    document.VendorCtlr1.action1.value="InsertRow";
    document.VendorCtlr1.submit();  
    }
    function showService(){
    document.VendorCtlr1.key1.value="service";
    document.VendorCtlr1.action1.value="InsertRow";
    document.VendorCtlr1.submit();  
    }
   
    function insertSubmit(){
    alert("You can add Portable Areas and/or ASOCs by clicking the appropriate and "+
    " links in the bottom of the Update Vendor Configuration Screen");
    document.VendorCtlr1.key1.value="";
    document.VendorCtlr1.action1.value="InsertRow";
    document.VendorCtlr1.submit();  
    }
    
    function updateSubmit(){
    document.VendorCtlr1.key1.value="";
    document.VendorCtlr1.action1.value="UpdateRow";
    document.VendorCtlr1.submit();  
    }
    
</SCRIPT>


<%@page import="java.util.List,java.util.Iterator,java.util.HashMap"%>
<%@ include file="i_header.jsp" %>
<jsp:useBean id="beanVendor1" scope="request" 
class="com.alltel.lsr.common.objects.VendorBean" />
<% try{
                                                                                                                                final String CONTROLLER =
                                                                                                                                        "VendorCtlr";
                                                                                                                                if (!sdm.isAuthorized(CONTROLLER)) {
                                                                                                                                    Log.write(Log.WARNING,sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
                                                                                                                                    alltelResponse.sendRedirect(SECURITY_URL);
                                                                                                                                }

                                                                                                                                boolean strget=beanVendor1.getDbAction().equals("get") ;
                                                                                                                                boolean strupdate=beanVendor1.getDbAction().equals("UpdateRow") ;
                                                                                                                                boolean strdelete=beanVendor1.getDbAction().equals("DeleteRow");
%>






<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">Vendor&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form action="VendorCtlr" method="post" name="VendorCtlr1">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg">
        <%=beanVendor1.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg">
        <% String strRec = request.getParameter("norecord");%>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>
        <input type="hidden" size=17 maxLength=15 NAME="VENDOR_CONFIG_SQNC_NMBR" 
        VALUE="<%=beanVendor1.getStrVendorConfigSqncNumber()%>" >
        <tr>
            <td align=right>Company:</td> 
            <td align=left>

              
                    
                <%  Connection con = DatabaseManager.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = null;
                    if(strget || strupdate || strdelete){ %>
                
                
                <% 

                    rs = stmt.executeQuery("SELECT OCN_T.CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ,OCN_T WHERE " +
                            "COMPANY_T.CMPNY_SQNC_NMBR=(select CMPNY_SQNC_NMBR from VENDOR_TABLE_CONFIG_T where VENDOR_CONFIG_SQNC_NMBR='"
                            + beanVendor1.getStrVendorConfigSqncNumber()+"') ORDER BY CMPNY_NM ASC");

                    if(rs.next()) {%>
                <input type=text value="<%=  rs.getString("CMPNY_NM")%>" NAME="CMPNY_SQNC_NMBR" size=40 maxLength=50 readonly>
                <% }} else{ %>
                <select NAME="CMPNY_SQNC_NMBR" onchange="showOCN();">                                                                                   
                   
              
                    <%  String cmpyValue = (String )session.getAttribute("cmpny");
                        if(cmpyValue==null){
                            cmpyValue="";
                        }
                        if(cmpyValue.equals("")){
                            out.println("<option value="+">Please Select Company </option>");
                        }
                        rs = stmt.executeQuery("SELECT * FROM COMPANY_T  " +
                                "WHERE COMPANY_T.CMPNY_SQNC_NMBR in ( SELECT distinct(CMPNY_SQNC_NMBR) from OCN_T) " +
                                "and (cmpny_typ='R' or cmpny_typ='C' or cmpny_typ='L') ORDER BY CMPNY_NM ASC");

                        while (rs.next() == true) {
                            if(strget || strupdate || strdelete){
                                if(beanVendor1.getStrCompSqncNumber().equals(rs.getString("CMPNY_SQNC_NMBR"))) {
                    %>
                    <option value="<%= rs.getString("CMPNY_SQNC_NMBR")%>" SELECTED><%= 
                        rs.getString("CMPNY_NM") %></option>
                    
                    <%	break;
                        }} else{
                            if(cmpyValue.equals(rs.getString("CMPNY_SQNC_NMBR"))){%>
                    
                    <option value="<%= rs.getString("CMPNY_SQNC_NMBR")%>" SELECTED><%= 
                        rs.getString("CMPNY_NM") %></option>
                    <% } else {
                    %>
                    
                    <option value="<%= rs.getString("CMPNY_SQNC_NMBR")%>" ><%= 
                        rs.getString("CMPNY_NM") %></option>
                    
                    <%
                        }}}}
                    %>
                </select>

            </td>
        </tr>
        <input type=hidden  name=key1> 
        <input type=hidden name=action1> 
        <tr>
            <td 
                align=right>&nbsp;Operating&nbsp;Company&nbsp;Number&nbsp;for&nbsp;selected&nbsp;Vendor:
            </td> 
            <td align=left>
    
                <%     
                    if(strget || strupdate || strdelete){%>
                <input type=text value="<%=beanVendor1.getStrOCN()%>" NAME="OCN_CD" size=40 maxLength=50 readonly>
                <% }else{ %>
                <select NAME="OCN_CD" onchange="showState();" >
                    <%     String OcnValue = (String )session.getAttribute("OcnValue");%>
                    
                    <%   if(OcnValue==null){
                            OcnValue="";
                        }
                        if(OcnValue.equals("")){
                            out.println("<option value="+">Please Select OCN </option>");
                        }

                        List listOcn =(List) session.getAttribute("listOcn1");

                        if(listOcn!=null){
                            Iterator itrOcn = listOcn.iterator();
                            String ocnStr="";
                            while (itrOcn.hasNext()) {
                                ocnStr=(String)itrOcn.next();
                                if (OcnValue.equals(ocnStr)) {
                    %>
                    <option value="<%=ocnStr%>" SELECTED ><%=ocnStr%> </option> 
                    <%	}
                        else {
                    %>
                    <option value="<%=ocnStr%>"><%=ocnStr%> 
                    </option>
                    <%
                        }}}}
                    %>
                </select>

            </td>
        </tr>

        
        <tr>
            <td align=right>&nbsp;State&nbsp;for&nbsp;selected&nbsp;OCN</td> 
            <td align=left>
               

                <% 
                    if(strget || strupdate || strdelete){ %>
                <input type=text value="<%=beanVendor1.getStrStateCode()%>" NAME="STT_CD" size=40 maxLength=50 readonly>
                <% }else{ %>
                <select NAME="STT_CD" > 
                    <%  HashMap hsState =(HashMap) session.getAttribute("hsState");

                        String stateValue = (String )session.getAttribute("state");

                        if(stateValue==null){
                            stateValue="";
                        }
                        if(hsState!=null){
                            List listStnm =(List)hsState.get("listStnm");
                            String strstNm="";
                            if(listStnm!=null){
                                for(int i=0;i<listStnm.size();i++){
                                    strstNm =(String)listStnm.get(i);
                                    if(stateValue.equals(strstNm)){

                    %>
                    <option value="<%= strstNm%>" selected><%= strstNm %></option> 
                    <% }else{%>
                                
                    <option value="<%= strstNm%>" ><%= strstNm %></option> 

                    <%
                        }
                        }
                        }
                        }
                    }
                    %>
                 
                </select>

            </td>
        </tr>
        
        <tr>
            <td align=right><%= beanVendor1.getWCNSPANStart() %>Windstream Company Number 
            (WCN):<%= beanVendor1.getWCNSPANEnd() %></td> 
            <td align=left><% if(strget || strupdate || strdelete){ %>
                <input type=text value="<%=beanVendor1.getStrWCN()%>" NAME="WCN" size=5 maxLength=3 readonly>
                <% } else{ %>
                <input type="TEXT" size=5 maxLength=3 NAME="WCN" VALUE="<%=beanVendor1.getStrWCN() %>">
                <%} %>
            </td>
        </tr>
       
        <tr>
            <td align=right>&nbsp;Service&nbsp;Type</td> 
            <td align=left>
                <% if(strget || strupdate || strdelete){ %>
                <input type=text value="<%=beanVendor1.getStrServiceType()%>" NAME="SRVC_TYP_CD" size=40 maxLength=50 readonly>
                <% }else{%>
                <select NAME="SRVC_TYP_CD" onchange="showService();">
                    <% String serValue=(String) session.getAttribute("service");
                        if(serValue==null){
                            serValue="";
                        }
                        if(serValue.equals("")){
                            out.println("<option value="+">Please Select Service </option>");
                        }
                        rs = stmt.executeQuery("SELECT * FROM SERVICE_TYPE_T where ACTVTY_TYP_MOX_IDX is not null and typ_ind='R' ORDER BY" +
                                " SRVC_TYP_CD ASC");
                        String serStr="";
                        String serStr1="";
                        while (rs.next() == true) {
                            serStr=rs.getString("SRVC_TYP_CD")+"-"+rs.getString("SRVC_TYP_DSCRPTN");
                            serStr1=serStr+"^"+rs.getString("ACTVTY_TYP_MOX_IDX");
                            if(serValue.equals(serStr1)){
                    %> 
                    <option value="<%=serStr+"^"+rs.getString("ACTVTY_TYP_MOX_IDX") %>" selected><%=serStr%></option> 
                    <% } else{%>
                    <option value="<%=serStr1%>"><%=serStr%></option> 
                    <%    }}}%>  
                </select>
                
                
            </td>
        </tr>

        <tr>
            <td align=right>Activity&nbsp; Type</td> 
            <td align=left>
               
                <% 
                    if(strget || strupdate || strdelete){ %>
                <input type=text value="<%=beanVendor1.getStrActivityType()%>" NAME="ACTVTY_TYP_CD" size=40 maxLength=50 readonly>
                <%                  
                    }else{   %>
                <select NAME="ACTVTY_TYP_CD">
                   
                    <%    
                        List listAct =(List) session.getAttribute("listAct");
                        if(listAct!=null){
                            String actStr="";
                            for(int i=0;i<listAct.size();i++){
                                actStr =(String)listAct.get(i);
                    %>
               
                    <option value="<%=actStr %>"><%=actStr%></option> 

                    <% }}}%>  
                </select>
                
                
            </td>
        </tr>

        <% DatabaseManager.releaseConnection(con); %>   

       
        <tr>
            <td align=right><%= beanVendor1.getBTNSPANStart() %> BTN:<%=beanVendor1.getBTNSPANEnd() %> </td>
            <td align=left><input type="TEXT" size=18 maxLength=17 NAME="BTN" VALUE="<%=beanVendor1.getStrBTN()%>"></td>
        </tr>
        <tr>
            <td align=right>Vendor Contact Information:</td>
            <td align=left><input type="TEXT" size=16 maxLength=12 NAME="CONTACTNUMBER" VALUE="<%=beanVendor1.getContactNo()%>"></td>
        </tr>
       
        <tr>
            <td align=right>&nbsp;Is&nbsp;Embargoed: </td> 
            <td align=left><select NAME="IS_EMBARGOED"> 

                <%String isEMBARGOE=beanVendor1.getIsEmbargoed()!=null?
                        beanVendor1.getIsEmbargoed():"";
                    if(isEMBARGOE.equals("Y")){ %>
                <option value="Y"  SELECTED>Y</option>
                <option value="N" >N</option>
                <% }else{%>
                <option value="N"  SELECTED>N</option>
                <option value="Y" >Y</option>
                <%} %>
 
            </td>
        </tr>
        
        <tr>
            <td align=right> <%= beanVendor1.getTXJURSPANStart() %>&nbsp;Tax&nbsp;Exemptions:
            <%= beanVendor1.getTXJURSPANEnd() %> </td> 
            <td align=left><input type="TEXT" size=15 maxLength=10 NAME="TXJUR" VALUE="<%=beanVendor1.getStrTXJUR() %>"></td>
        </tr>
        
        <tr>
            <td align=right> Has Directory : </td> 
            <td align=left><select NAME="IS_DIRECTORY">
                <%String hasDELETE=beanVendor1.getIsDirectory()!=null?
                        beanVendor1.getIsDirectory():"";
                    if(hasDELETE.equals("Y")){ %>
                <option value="Y"  SELECTED>Y</option>
                <option value="N" >N</option>
                <% } else{%>
                <option value="N"  SELECTED>N</option>
                <option value="Y" >Y</option>
                <%} %>
            </td>
          
        </tr>
        <tr>
            <td 
            align=right>&nbsp;Is&nbsp;eligible&nbsp;to&nbsp;delete&nbsp;Directory&nbsp;: </td> 
            <td align=left><select NAME="IS_ELIGIBLE_TO_DIR_DELETE">
                <%String isDELETE=beanVendor1.getIsEligibleToDeleteDir()!=null?
                        beanVendor1.getIsEligibleToDeleteDir():"";
                    if(isDELETE.equals("Y")){ %>
                <option value="Y"  SELECTED>Y</option>
                <option value="N" >N</option>
                <% } else{%>
                <option value="N"  SELECTED>N</option>
                <option value="Y" >Y</option>
                <%} %>
            </td>
        </tr>
        <tr>
            <td align=right>Processing Limit-Time of Day:</td> 
            <td align=left>
                <select NAME="VALID_TIME_OF_DAY_FOR_DDD">
             
                <%String dddstr=beanVendor1.getValidTimeOfDayDDD(); %>
                <% String str2="";
                    for(int i=0;i<=23;i++){
                        if(i<9){
                            str2="000"+Integer.toString(i);
                            if(dddstr!=null && dddstr.equals(str2)){
                %>
                <option value="000<%=i%>" selected> <%="0"+i+"00" %>
                <% } else { %>
                <option value="000<%=i%>"> <%="0"+i+"00" %>

                <%}}else{
                                str2="00"+Integer.toString(i);
                                if(dddstr!=null && dddstr.equals("00"+Integer.toString(i))){
                %>
                <option value="00<%=i%>" selected> <%=i+"00"%>
                <% } else { %>
                <option value="00<%=i%>"> <%=i+"00"%>

                <%}}
                        }  %>
                </td>
        </tr>
        <tr>
            <td 
            align=right>&nbsp;DDD&nbsp;Lower&nbsp;Limit&nbsp;[Number&nbsp;of&nbsp;Days]: </td> 
            <td align=left>
                <select NAME="DDD_INTERVAL_LOWER_LIMIT">
            
                <%String lowstr=beanVendor1.getDueDateLowerLimit();
                    for(int i=1;i<=99;i++){
                        if(lowstr!=null && lowstr.equals(Integer.toString(i))){
                %>
                <option value="<%=i%>" selected> <%=i%>
                <% } else {
                     if((lowstr==null || lowstr.trim().length()==0) && Integer.toString(i).equals("3")) {
                %>
                <option value="<%=i%>" selected> <%=i%>
                <%} else{%>
                <option value="<%=i%>"> <%=i%>
                
                <%}}}   %>
               
            </td>
        </tr>
        <tr>
            <td 
            align=right>&nbsp;DDD&nbsp;Upper&nbsp;Limit&nbsp;[Number&nbsp;of&nbsp;Days]</td> 
            <td align=left>
                <select NAME="DDD_INTERVAL_UPPER_LIMIT">
            
                <%String highstr=beanVendor1.getDueDateUpperLimit();
                    for(int i=1;i<=99;i++){
                        if(highstr!=null && highstr.equals(Integer.toString(i))){
                %>
                <option value="<%=i%>" selected> <%=i%>
                <% } else {
                        if((highstr==null || highstr.trim().length()==0) && Integer.toString(i).equals("30")) {%>
                
                <option value="<%=i%>" selected > <%=i%>
                <% }else{ %>
                <option value="<%=i%>"> <%=i%>
                <%}}}   %>
               
            </td>
        </tr>
        <tr>
            <td align=right>SLA Time  </td> 
            <td align=left>
                <select NAME="SLA_WAIT_TIME">
                <%String slastr=beanVendor1.getSLAWaitTime();
                    if(slastr!=null &&slastr.equals("48")){ %>
                <option value="48" selected>48
                <%} else {%>
                <option value="48" selected>48
                <% } %>
                <%
                    if(slastr!=null &&slastr.equals("12")){ %>
                <option value="12" selected>12
                <%} else {%>
                <option value="12">12
                <% } 
                    if(slastr!=null &&slastr.equals("24")){ %>
                <option value="24" selected>24
                <%} else {%>
                <option value="24">24
                <% } %>
                <%     if(slastr!=null &&slastr.equals("36")){ %>
                <option value="36" selected>36
                <%} else {%>
                <option value="36">36
                <% } %>
                <%
                    if(slastr!=null &&slastr.equals("15")){ %>
                <option value="15" selected>15
                <%} else {%>
                <option value="15">15
                <% } 
                    if(slastr!=null &&slastr.equals("39")){ %>
                <option value="39" selected>39
                <%} else {%>
                <option value="39">39
                <% } %>
            </td>
            
        </tr>
        <input type="hidden" NAME="MDFD_USERID" VALUE="<%= beanVendor1.getMdfdUserid() %>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= beanVendor1.getMdfdDt() %>">
    
        <%      if (beanVendor1.getDbAction().equals("get") ||
                            beanVendor1.getDbAction().equals("UpdateRow") ||
                            beanVendor1.getDbAction().equals("DeleteRow")) {
        %>
        <tr>
            <td align=right>Modified&nbsp;Date:</td>
            <td align=left><%= beanVendor1.getMdfdDt() %></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td>
            <td align=left><%= beanVendor1.getMdfdUserid() %></td>
        </tr>
        <%      }
        %> 

        <% 	if (beanVendor1.getDbAction().equals("get"))
            {
        %>
        
        <TR><TD align=center colspan=2><A 
        HREF="TableAdminCtlr?tblnmbr=29&rstrctsrch=yes&srchctgry=0&srchvl=<%=beanVendor1.getStrVendorConfigSqncNumber()
        %>">&nbsp;PORTABLE&nbsp;AREA&nbsp;Table</A></TD></TR>
        <TR><TD align=center colspan=2><A 
        HREF="TableAdminCtlr?tblnmbr=30&rstrctsrch=yes&srchctgry=0&srchvl=<%=beanVendor1.getStrVendorConfigSqncNumber()
        %>">&nbsp;VENDOR ASOC CONFIG&nbsp;Table</A></TD></TR>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (beanVendor1.getDbAction().equals("new") ||
                            beanVendor1.getDbAction().equals("InsertRow")) {
                %>
                <INPUT class=appButton TYPE="BUTTON" name="action" value="InsertRow" ONCLICK="insertSubmit();">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      }
                    else if (beanVendor1.getDbAction().equals("get") ||
                            beanVendor1.getDbAction().equals("UpdateRow") ||
                            beanVendor1.getDbAction().equals("DeleteRow")) {
                        if (sdm.isAuthorized(beanVendor1.getTblAdmnScrtyTgMod()) ) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%
                    }
                    if (sdm.isAuthorized(beanVendor1.getTblAdmnScrtyTgMod()) ) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%
                    }
                    if (sdm.isAuthorized(beanVendor1.getTblAdmnScrtyTgMod()) ) {
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
                                                                                                                Log.write(Log.WARNING, " =kums=== " + e);
                                                                                                                e.printStackTrace();
                                                                                                            }

%>
                                                                                            