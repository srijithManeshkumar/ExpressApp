<% try {%>
<%@ include file="i_header.jsp" %>
<%
     final String CONTROLLER = "VendorAsocCtlr";
     if (!sdm.isAuthorized(CONTROLLER)) {
         Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
         alltelResponse.sendRedirect(SECURITY_URL);
     }
%>

<SCRIPT language=JavaScript>

    function CheckFeeRate(val){

        var a1=document.VendorAsocCtlr.ASOC_FEE_RATE.value;
        var ind =a1.indexOf(".");
        var lent=a1.length ;
        var dec = 0;
        if(ind!=-1){
            dec = lent-ind;
            dec=dec-1;
        }
        if(lent<=3 && dec<3){
            document.VendorAsocCtlr.action1.value=val;
            document.VendorAsocCtlr.submit();
        } else if(lent>3 && (ind==-1 || ind > 3) || dec>=3){
            alert("ASOC FEE RATE is Three digit with two decimal number");
        } else if(lent >3 && ind <=3 && dec<3){
            document.VendorAsocCtlr.action1.value=val;
            document.VendorAsocCtlr.submit();
        }

    }
</SCRIPT>

<jsp:useBean id="vendorAsocBean" scope="request" class="com.alltel.lsr.common.objects.VendorAsocBean" />

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
            <SPAN class="barheader">VENDOR&nbsp;ASOC&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<form name="VendorAsocCtlr" action="VendorAsocCtlr" method="POST">

    <table align=center border=0 width=500>

        <tr><td colspan=2><input type=hidden name=action1 >&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= vendorAsocBean.getErrMsg()%>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

        <tr>
            <td align=right>VENDOR&nbsp;ASOC&nbsp;CATEGORY</td>
            <td align=left><select NAME="ASOC_TYPE_CONFIG_SQNC_NMBR">

                <%
     Connection con = DatabaseManager.getConnection();
     Statement stmt = con.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM ASOC_CATEGORY_T ORDER BY ASOC_TYPE ASC");

     while (rs.next() == true) {
         String str1 = vendorAsocBean.getStrAsocTypeConfigSeqNo();
         if (str1 != null && str1.equals(rs.getString("ASOC_TYPE_CONFIG_SQNC_NMBR"))) {
                %>
                <option value="<%= rs.getString("ASOC_TYPE_CONFIG_SQNC_NMBR")%>" SELECTED><%= rs.getString("ASOC_TYPE")%>
                <%	} else {
                %>

                <option value="<%= rs.getString("ASOC_TYPE_CONFIG_SQNC_NMBR")%>" ><%= rs.getString("ASOC_TYPE")%>
                <%
         }
     }
     DatabaseManager.releaseConnection(con);
                %>

            </td>
        </tr>

        <tr>
            <%
     String acCode[] = new String[24];
     acCode[0] = "SOCBR";
     acCode[1] = "SOCRR";
     acCode[2] = "SOTHL";
     acCode[3] = "ADLV";
     acCode[4] = "ADLRV";
     acCode[5] = "NO DA";
     acCode[6] = "INSIGHT";
     acCode[7]  = "ENPUB";
     acCode[8] = "NL";
     acCode[9] = "NLIST";
     acCode[10] = "NLNC";
     acCode[11] = "NLSTU";
     acCode[12] = "NLSTV";
     acCode[13] = "NLV";
     acCode[14] = "NPDL";
     acCode[15] = "NPNC";
     acCode[16] = "NPUB";
     acCode[17] = "NPUBU";
     acCode[18] = "NPUBV";
     acCode[19] = "ONP";
     acCode[20] = "VSOBR";
     acCode[21] = "VSORR";
     acCode[22] = "CSUP";
     acCode[23] = "RSCC";

            %>
            <td align=right>ASOC&nbsp;TYPE&nbsp;CODE</td>
            <td align=left><select NAME="ASOC_CD">
                <%String asoctype=vendorAsocBean.getStrAsocTypeCode();
            for(int i=0;i<acCode.length;i++){
               if(asoctype!=null && asoctype.equals(acCode[i])) {%>
                   <option value="<%= asoctype%>" SELECTED><%= asoctype%>
              <%} else{ %>
                    <option value="<%= acCode[i]%>" ><%= acCode[i]%>
             <% }}   %>
        </td>
        </tr>

        <tr>

            <%
     String acfee[] = new String[9];
     acfee[0] = "Per Access Line";
     acfee[1] = "After FOC";
     acfee[2] = "Each Version";
     acfee[3] = "SUP 1";
     acfee[4] = "After FOC - Each Version";
     acfee[5] = "Each Additional Listing";
     acfee[6] = "Each Non-Published Listing";
     acfee[7] = "Each Non-List Listing";
     acfee[8] = "ALL";
            %>
            <td align=right>How&nbsp;Asoc&nbsp;Fee&nbsp;Applies</td>
            <td align=left><select NAME="HOW_ASOC_FEE_APPLIES">
                   <%String asocfees=vendorAsocBean.getStrHowAsocFeeApplies();
            for(int i=0;i<acfee.length;i++){
               if(asocfees!=null && asocfees.equals(acfee[i])) {%>
                   <option value="<%= asocfees%>" SELECTED><%= asocfees%>
              <%} else{ %>
                    <option value="<%= acfee[i]%>" ><%= acfee[i]%>
             <% }}   %>
              </td>
        </tr>
        <tr>
            <td align=right>Asoc&nbsp;Fee&nbsp;Rate</td>
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="ASOC_FEE_RATE" VALUE="<%= vendorAsocBean.getStrAsocFeeRate()%>"></td>
        </tr>
        <tr>
            <td align=right>BUS/RES&nbsp;Indicator</td>
            <td align=left>
                <select NAME="BUS_RES_IND">
                <% String indi = vendorAsocBean.getFiledInd();

     if (indi != null && indi.trim().length() > 0) {
         if (indi.equals("BUS")) {%>
                <option value="<%=indi%>" selected>BUS
                <option value="RES">RES
                <option value="ALL">ALL
                <%  } else if (indi.equals("RES")) {%>
                <option value="<%=indi%>" selected>RES
                <option value="BUS">BUS
                <option value="ALL">ALL
                <%  } else if (indi.equals("ALL")) {%>
                <option value="<%=indi%>" selected>ALL
                <option value="BUS">BUS
                <option value="RES">RES
                <%  }
                } else {%>
                <option value="BUS" selected>BUS
                <option value="RES">RES
                <option value="ALL">ALL
                <%}%>
            </td>
        </tr>
        <input type="hidden" NAME="MDFD_USERID" VALUE="<%= vendorAsocBean.getMdfdUserid()%>">
        <input type="hidden" NAME="MDFD_DT" VALUE="<%= vendorAsocBean.getMdfdDt()%>">

        <input type="hidden" NAME="VENDOR_CONFIG_SQNC_NMBR" VALUE="<%= session.getAttribute("vendorConfigSqncNumber")%>">
        <input type="hidden" NAME="VENDOR_ASOC_CONFIG_SQNC_NMBR" VALUE="<%= vendorAsocBean.getStrVendorAsocConfigSeqNo()%>">

        <%	if (vendorAsocBean.getDbAction().equals("get") ||
             vendorAsocBean.getDbAction().equals("UpdateRow") ||
             vendorAsocBean.getDbAction().equals("DeleteRow")) {
        %>
        <tr>
            <td align=right>Modified&nbsp;Date:</td>
            <td align=left><%= vendorAsocBean.getMdfdDt()%></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td>
            <td align=left><%= vendorAsocBean.getMdfdUserid()%></td>
        </tr>
        <%	}
        %>

        <tr>
            <td align=center colspan=2>
                <%      if (vendorAsocBean.getDbAction().equals("new") ||
             vendorAsocBean.getDbAction().equals("InsertRow")) {
                %>
                <INPUT class=appButton TYPE="Button" name="action" value="InsertRow" ONCLICK="CheckFeeRate('InsertRow');">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%      } else if (vendorAsocBean.getDbAction().equals("get") ||
              vendorAsocBean.getDbAction().equals("UpdateRow") ||
              vendorAsocBean.getDbAction().equals("DeleteRow")) {

          if (sdm.isAuthorized(vendorAsocBean.getTblAdmnScrtyTgMod())) {
                %>
                <INPUT class=appButton TYPE="Button" name="action" value="UpdateRow" ONCLICK="CheckFeeRate('UpdateRow');">
                <%          }
          if (sdm.isAuthorized(vendorAsocBean.getTblAdmnScrtyTgDel())) {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%          }
          if (sdm.isAuthorized(vendorAsocBean.getTblAdmnScrtyTgMod())) {
                %>
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <%          }
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

<%} catch (Exception e) {
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
