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
 
 <SCRIPT language=JavaScript>
    function automateOn_OFF(val){
    document.VendorAutomationCtlr.flagValue.value=val;
    document.VendorAutomationCtlr.action2.value="UpdateRow";
    document.VendorAutomationCtlr.submit();  
   
    }
 </SCRIPT>  
 
 
<%@page import="com.alltel.lsr.common.objects.VendorBean"%>
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

<form name="VendorAutomationCtlr" action="VendorAutomationCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2>&nbsp;<input type="hidden" name="action2">
        <input type="hidden" name="flagValue"> 
        </td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= 
            beanVendorAutomation1.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <% String key2 =request.getParameter("key2");
            if(key2!=null && key2.equals("noupdate")){ %>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg">
        <%=" Not updated Please Give Correct Values " %>&nbsp;</SPAN></td></tr>
        <%  }%>

        <tr><td colspan=2>&nbsp;</td></tr>
    </table>
    <table align=center border=1 width=500>
        <tr>
            <th></th> 
            <th>Company</th> 
            <th>OCN</th> 
            <th>State</th> 
            <th>Service&nbsp;Type</th> 
            <th>Activity&nbsp;Type</th> 
            <!-----//added the WCN in below line - fix for ISSASOI-2 - Antony - 05/26/2010---->
            <th>WCN</th> 
            <th>AutomateVendor</th> 
            <th>AutomateOCN</th> 
            <th>AutomateState</th> 
            <th>AutomateService</th> 
            <th>AutomateActivity</th> 
            <th>AutomateALL(YES/NO)</th> 
          
        </tr> 
        <% List listAuto =(List)session.getAttribute("listAuto1");%>
        <%   for(int i=0;i<listAuto.size();i++){
                VendorBean objVendorBean =(VendorBean)listAuto.get(i);
        %>
        <tr>
            <td><input type=hidden name="VENDOR_CONFIG_SQNC_NMBR" value="<%=objVendorBean.getStrVendorConfigSqncNumber()%>" ></td> 
            <td><%=objVendorBean.getStrCompSqncNumber()%></td> 
            <td><%=objVendorBean.getStrOCN()%></td> 
            <td><%=objVendorBean.getStrStateCode()%></td> 
            <td><%=objVendorBean.getStrServiceType()%></td> 
            <td><%=objVendorBean.getStrActivityType()%></td> 
            <!-----//added the WCN in below line - fix for ISSASOI-2 - Antony - 05/26/2010---->
            <td><%=objVendorBean.getStrWCN()%></td> 
           <td> <select name="VEDOR_AUTOMATE_FLAG">
               
                <% String varven="";
                    if(objVendorBean.getVedorAutomateFlag().equals("N")){
                        varven="Y";
                %>
                <% } else{
                        varven="N"; }%>
                <option value="<%=objVendorBean.getVedorAutomateFlag()%>" selected>
                    <%=objVendorBean.getVedorAutomateFlag()%>
                </option> 
                <option value="<%=varven%>"><%=varven%> </option> 
            </select>
            </td> 
            <td><select name="OCN_AUTOMATE_FLAG">
                
                <% String varOcn="";
                    if(objVendorBean.getOcnAutomateFlag().equals("N")){
                        varOcn="Y";
                %>
                <% } else{
                        varOcn="N"; }%>
                <option value="<%=objVendorBean.getOcnAutomateFlag()%>" selected>
                    <%=objVendorBean.getOcnAutomateFlag()%>
                </option> 
                <option value="<%=varOcn%>"><%=varOcn%> </option> 
              

            </select>
            </td> 
            <td><select name="STATE_AUTOMATE_FLAG">
                <% String varst="";
                    if(objVendorBean.getStateAutomateFlag().equals("N")){
                        varst="Y";
                %>
                <% } else{
                        varst="N"; }%>
                <option value="<%=objVendorBean.getStateAutomateFlag()%>" selected>
                    <%=objVendorBean.getStateAutomateFlag()%>
                </option> 
                <option value="<%=varst%>"><%=varst%> </option> 
                
                

            </select>
            </td> 
            <td><select name="SRVTYPE_AUTOMATE_FLAG">
                <% String varser="";
                    if(objVendorBean.getSrvtypeAutomateFlag().equals("N")){
                        varser="Y";
                %>
                <% } else{
                        varser="N"; }%>
                <option value="<%=objVendorBean.getSrvtypeAutomateFlag()%>" selected>
                    <%=objVendorBean.getSrvtypeAutomateFlag()%>
                </option> 
                <option value="<%=varser%>"><%=varser%> </option> 
                
                

            </select>
            </td> 
            <td><select name="ACTTYPE_AUTOMATE_FLAG">
                <% String varact="";
                    if(objVendorBean.getActtypeAutomateFlag().equals("N")){
                        varact="Y";
                %>
                <% } else{
                        varact="N"; }%>
                <option value="<%=objVendorBean.getActtypeAutomateFlag()%>" selected>
                    <%=objVendorBean.getActtypeAutomateFlag()%>
                </option> 
                <option value="<%=varact%>"><%=varact%> </option> 
            </select>
            </td> 
            <td><select name="ALLFLAG">
                <option value="" selected>Please Select</option>
                <option value="N" >N</option> 
                <option value="Y">Y</option> 
            </select>
            </td> 
           
        </tr> 
               
        <% }%>
                
        
       
    
        <%      if (beanVendorAutomation1.getDbAction().equals("get") ||
                    beanVendorAutomation1.getDbAction().equals("UpdateRow") ||
                    beanVendorAutomation1.getDbAction().equals("DeleteRow")) {
        %>
        
        <%      }
        %> 
    </table>
    <table align=center border=0 width=500>
        <tr>
            <td align=center colspan=2>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <INPUT class=appButton TYPE="BUTTON" name="action" value="Turn Automation ON for all Records" 
                                         ONCLICK="automateOn_OFF('Y')" >
                <INPUT class=appButton TYPE="BUTTON" name="action" value="Turn Automation OFF for all Records"
                                         ONCLICK="automateOn_OFF('N')">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
            </td>
        </tr>
    </table>
</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<% } catch(Exception e){
        e.printStackTrace();
    }

%>
    