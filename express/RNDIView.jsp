<% try{
        /**
      * NOTICE:
      *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
      *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
      *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
      *
      *			COPYRIGHT (C) 2002
      *				BY
      *			Windstream COMMUNICATIONS INC.
      */
        /**
      * MODULE:	RNDIView.jsp
      *
      * DESCRIPTION: JSP View used to maintain the RNDI_T table
      *
      * AUTHOR:      Express Development Team
      *
      * DATE:        2-1-2002
      *
      * HISTORY:
      *	01/01/2002 dmartz	initial
      *	05/29/2002 psedlak	Added cols to RNDI_t
      *
      */

%>

<%@ include file="i_header.jsp" %>

<%
        final String CONTROLLER = "RNDICtlr";
        if (!sdm.isAuthorized(CONTROLLER)) {
            Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
            alltelResponse.sendRedirect(SECURITY_URL);
        }
%>

<jsp:useBean id="rndibean" scope="request" class="com.alltel.lsr.common.objects.RNDIBean" />

<table width="100%" align=left>
    <tr>
        <TH align=center bgcolor="#7AABDE" >
        <SPAN class="barheader">RNDI&nbsp;Table&nbsp;Maintenance</SPAN>
        </th>
    </tr>
</table>

<br clear=ALL>

<FORM action="RNDICtlr" method="POST">

    <table align=center border=0>
        <input type="hidden" size=17 maxLength=15 NAME="CMPNY_SQNC_NMBR" VALUE="<%= rndibean.getCmpnySqncNmbr() %>">

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= rndibean.getErrMsg() %>&nbsp;</SPAN></td></tr>
        <tr><td colspan=2>&nbsp;</td></tr>

        <tr>
            <td align=right></td> 
            <td align=left><select NAME="CMPNY_TYP">

            </select>
            </td>
        </tr>

        <tr>
            <td align=right><%= rndibean.getCmpnyNmSPANStart() %>RNDI&nbsp;Name:<%= rndibean.getCmpnyNmSPANEnd() %></td> 
            <td align=left><input type="TEXT" size=52 maxLength=50 NAME="CMPNY_NM" VALUE="<%= rndibean.getCmpnyNm() %>"></td>
        </tr>







        <% 
            if (rndibean.getDbAction().equals("get") ||
                    rndibean.getDbAction().equals("UpdateRow") ||
                    rndibean.getDbAction().equals("DeleteRow")) {
            %>
        <tr>
            <td align=right>Modified&nbsp;Date:</td> 
            <td align=left><%= rndibean.getMdfdDt() %></td>
        </tr>
        <tr>
            <td align=right>Modified&nbsp;Userid:</td> 
            <td align=left><%= rndibean.getMdfdUserid() %></td>
        </tr>
        <%	}
        %>

        <tr>
            <td colspan=2 align=center>

                <%	if (rndibean.getDbAction().equals("new") ||
                rndibean.getDbAction().equals("InsertRow"))
                    {
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
                <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%	}
                    else if (rndibean.getDbAction().equals("get") ||
                    rndibean.getDbAction().equals("UpdateRow") ||
                    rndibean.getDbAction().equals("DeleteRow")) {


                if (sdm.isAuthorized(rndibean.getTblAdmnScrtyTgMod()) ) {
                %>

                <INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
                <%
                }

                    if (sdm.isAuthorized(rndibean.getTblAdmnScrtyTgDel())) {
                        %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
                <%
                }

                    if (sdm.isAuthorized(rndibean.getTblAdmnScrtyTgMod()) ) {
                        %>
                    <INPUT class=appButton TYPE="RESET" name="action" value="Reset">
                <%
                    }
                %>
                <INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
                <%	}
                %>

            </td>
        </tr>
    </table>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RNDIView.jsv  $
    /*
    /*   Rev 1.3   May 30 2002 07:48:36   sedlak
    /*Add targus userid/psswrd to RNDI table.
    /*
    /*   Rev 1.1   31 Jan 2002 14:17:04   sedlak
    /*
    /*
    /*   Rev 1.0   23 Jan 2002 11:05:34   wwoods
    /*Initial Checkin
    */

    /* $Revision:   1.3  $
    */
    }catch(Exception e){
        e.printStackTrace();
    }
    %>
