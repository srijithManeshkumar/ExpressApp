<%
    /**
                                         * NOTICE:
                                         *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
                                         *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
                                         *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
                                         *
                                         *                      COPYRIGHT (C) 2004
                                         *                              BY
                                         *                      Windstream COMMUNICATIONS INC.
                                         */
    /**
                                         * MODULE:      RequestSearchView.jsp
                                         *
                                         * DESCRIPTION: JSP View used to perform Request/Order advanced searches
                                         *
                                         * AUTHOR:      Express Development Team
                                         *
                                         * DATE:
                                         *
                                         * HISTORY:
                                         *
                                         *	02/06/2008 HD0000002472840 Steve Korchnak renamed AN column title to AN/SPAN
                                         *                            and ATN column to ATN/SPP TN
                                         *
                                         *      12-2-2004 pjs Added search by SUBMITTED DATE, & some cleanup (try/catch, and pulled
                                         *			companytype from session)
                                         */
%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.text.SimpleDateFormat" %>
<%@ page import ="java.util.Date" %>
<%
    String day = new SimpleDateFormat("dd").format(new Date());
    String month = new SimpleDateFormat("MM").format(new Date());
    String year = new SimpleDateFormat("yyyy").format(new Date());
    int yr = Integer.parseInt(year);
    yr = yr - 1;
    String yearp = Integer.toString(yr);
%>

<script language = "JavaScript">

    function validate(frm) 
    {

    if (frm.from_lst_mdfd_mnth.value.length==0 || frm.from_lst_mdfd_dy.value.length==0 || frm.from_lst_mdfd_yr.value.length==0)
    {
    alert("Please enter From Last Mod Date.");
    return false;
    }
    else if (frm.to_lst_mdfd_mnth.value.length==0 || frm.to_lst_mdfd_dy.value.length==0 || frm.to_lst_mdfd_yr.value.length==0)
    {
    alert("Please enter To Last Mod Date.");
    return false;
    }
    else if (frm.from_lst_mdfd_yr.value > frm.to_lst_mdfd_yr.value)
    {
    alert("To Last Mod Date Greater than From Last Mod Date.");
    return false;
    }
    else if ((frm.to_lst_mdfd_yr.value - frm.from_lst_mdfd_yr.value) > 1)
    {
    alert("Rate range Less than or Equal to one year.");  
    return false;
    }
    else
    {
    return true;
    }
    }
</script>

<%
    Connection conSrch = null;
    Statement stmtSrch = null;
    ResultSet rsSrch = null;

    try {

        conSrch = DatabaseManager.getConnection();
        stmtSrch = conSrch.createStatement();
%>

<FORM NAME="RequestSearchView" METHOD=POST ACTION="RequestListCtlr?rqstsrch=advsrch" onSubmit="return validate(RequestSearchView)">

    <TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR><TD align=center colspan=2><SPAN CLASS="header1">S&nbsp;E&nbsp;A&nbsp;R&nbsp;C&nbsp;H&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;Q&nbsp;U&nbsp;E&nbsp;S&nbsp;T</SPAN></TD></TR>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR>
            <TD align=right>PON:&nbsp;</TD>
            <TD align=left><input type=text maxLength=16 size=25 name=pon><BR><BR></TD>
        </TR>
        <TR>
            <TD align=right valign=top>Order Status:&nbsp;</TD>
            <TD align=left>
            <input type="checkbox" name="order_status" value="">ALL
            <br>
            <%
                rsSrch = stmtSrch.executeQuery("SELECT STTS_CD FROM STATUS_T WHERE TYP_IND = 'R' ORDER BY STTS_CD ASC");

                while (rsSrch.next() == true) {
            %>
            <input type="checkbox" name="order_status" value="<%= rsSrch.getString("STTS_CD") %>"><%= rsSrch.getString("STTS_CD") %>
            <br>
            <%
                }
            %>
            </SELECT><BR><BR></TD>
        </TR>
      <%   String strCompanyType =  sdm.getLoginProfileBean().getUserBean().getCmpnyTyp(); 
      if(strCompanyType!=null && strCompanyType.equalsIgnoreCase("P")){
      %>
       <TR>
        
            <TD align=right valign=top>Internal Status:&nbsp;</TD>
            <TD align=left>
            <input type="checkbox" name="inStatus" value="SUBMITTED">SUBMITTED 
            <br>
            <input type="checkbox" name="inStatus" value="MANUAL-SUBMITTED">MANUAL-SUBMITTED 
            <br>
            <input type="checkbox" name="inStatus" value="PRE-FOC">PRE-FOC 
            <br>
            <input type="checkbox" name="inStatus" value="MANUAL-REVIEW">MANUAL-REVIEW
            <br>
            <input type="checkbox" name="inStatus" value="FOC">FOC
            <br>
            <input type="checkbox" name="inStatus" value="REJECTED">REJECTED
            <br>
            <input type="checkbox" name="inStatus" value="PRE-REJECT">PRE-REJECT
            <br>
            <input type="checkbox" name="inStatus" value="COMPLETED">COMPLETED
            <br>
            <input type="checkbox" name="inStatus" value="IN-REVIEW">IN-REVIEW 
            <br>
            <input type="checkbox" name="inStatus" value="MANUAL-REJECT">MANUAL-REJECT
            <br>
            <input type="checkbox" name="inStatus" value="MANUAL-FOC">MANUAL-FOC
            <br>
                <input type="checkbox" name="inStatus" value="CI_REVIEW">CI_REVIEW
                <br>
                <input type="checkbox" name="inStatus" value="BOT FALLOUT">BOT FALLOUT
	 	<br>
	 
            </SELECT><BR><BR></TD>
        </TR>
  <% }%>
        <TR>
            <TD align=right valign=top>Service&nbsp;Type:</TD>
            <TD align=left>
            <input type="checkbox" name="srvc_type" value="">ALL
            <br>
            <%
                rsSrch = stmtSrch.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' ORDER BY SRVC_TYP_DSCRPTN ASC");

                while (rsSrch.next() == true) {
            %>
            <input type="checkbox" name="srvc_type" value=<%= rsSrch.getString("SRVC_TYP_CD") %>><%= rsSrch.getString("SRVC_TYP_DSCRPTN") %>
            <br>
            <%
                }
            %>
            </SELECT><BR><BR></TD>
        </TR>
        <TR>
            <TD align=right valign=top>Activity&nbsp;Type:</TD>
            <TD align=left>
            <input type="checkbox" name="act_type" value="">ALL
            <br>
            <%
                rsSrch = stmtSrch.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'R' ORDER BY ACTVTY_TYP_DSCRPTN ASC");

                while (rsSrch.next() == true) {
            %>
            <input type="checkbox" name="act_type" value=<%= rsSrch.getString("ACTVTY_TYP_CD") %>><%= rsSrch.getString("ACTVTY_TYP_DSCRPTN") %>
            <br>
            <%
                }
            %>
            </SELECT><BR><BR></TD>
        </TR>
        
	<%   	//added Simple Order Flag search field for Simple Ports Project - Antony - 12/15/2010
		if(strCompanyType!=null && strCompanyType.equalsIgnoreCase("P")){
       %>
       <TR>
		<TD align=right>&nbsp;Simple&nbsp;Order:&nbsp;&nbsp;</TD>
		<TD align=left><SELECT NAME="spFlag">
                        <option value="NULL"></OPTION>
			<option value="Y">Y</OPTION>
			<option value="N">N</OPTION>
			<option value="N/A">N/A</OPTION>
            	</SELECT><BR><BR></TD>
        </TR>
  	<% 	}%>

	 <TR>
        <TD align=right valign=top>Expedite:</TD>
        <td>
        <input type="checkbox" name="exp" value="Yes"> Yes</td>
        </tr>

	 <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR>
            <TD align=right>&nbsp;Order&nbsp;Type :&nbsp;</TD>
            <TD align=left><SELECT NAME="orderFlag">
                    <option value="ALL">ALL </OPTION>
                    <option value="N">CAMS</OPTION>
                    <option value="Y">ICARE</OPTION>
                </SELECT></TD>
        </TR>
        <TR>
        <TD colspan=2 align=right valign=top>&nbsp;</TD></tr>
        <TR>
            <TD align=right valign=top>Company:</TD>
            <TD align=left><select MULTIPLE size=3 NAME="company">
            <option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

            <%
                // pjs - 12-2 pull from session obj, removed query
                String strCT =  sdm.getLoginProfileBean().getUserBean().getCmpnyTyp();

                if (strCT.equals("P")) {
                    rsSrch = stmtSrch.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ORDER BY CMPNY_NM");
                } else {
                    rsSrch = stmtSrch.executeQuery("SELECT C.CMPNY_SQNC_NMBR CMPNY_SQNC_NMBR, CMPNY_NM " +
                            "FROM COMPANY_T C, USERID_T U WHERE  C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR " +
                            "AND USERID = '" + sdm.getUser() + "' ORDER BY CMPNY_NM ASC");
                }

                while (rsSrch.next() == true) {
            %>
            <option value=<%= rsSrch.getString("CMPNY_SQNC_NMBR") %>><%= rsSrch.getString("CMPNY_NM") %>
            <%
                    }
            %>
            </SELECT><BR><BR></TD>
        </TR>
        <TR>
            <TD align=right valign=top>OCN:</TD>
            <TD align=left><select MULTIPLE size=3 NAME="ocn">
            <option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

            <%

                if (strCT.equals("P")) {
                        rsSrch = stmtSrch.executeQuery("SELECT DISTINCT OCN_CD FROM OCN_T WHERE OCN_CD NOT LIKE 'K%' ORDER BY OCN_CD");
                        while (rsSrch.next() == true) {
            %>
            <option value=<%= rsSrch.getString("OCN_CD") %>><%= rsSrch.getString("OCN_CD") %>
            <%
                }
                } else {

                        // Get All OCN Codes for this users user groups
                        String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
                                "(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
                        rsSrch = stmtSrch.executeQuery(strQuery);

                        String strInClause = "";
                        while (rsSrch.next() == true) {
                            if (rsSrch.getString("OCN_CD").equals("*")) {
                                String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " +
                                        rsSrch.getInt("CMPNY_SQNC_NMBR") + " ORDER BY OCN_CD";
                                Statement substmt = null;
                                substmt = conSrch.createStatement();
                                ResultSet subrs = substmt.executeQuery(strSubQuery);
                                while (subrs.next() == true) {
            %>
            <option value=<%= subrs.getString("OCN_CD") %>><%= subrs.getString("OCN_CD") %>
            <%
                                    }
                                } else {
            %>
            <option value=<%= rsSrch.getString("OCN_CD") %>><%= rsSrch.getString("OCN_CD") %>
            <%
                }
                            }
                    }

            %>
            </SELECT><BR><BR></TD>
        </TR>
        <TR>
            <TD align=right valign=top>State:</TD>
            <TD align=left><select MULTIPLE size=3 NAME="state">
            <option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

            <%
                rsSrch = stmtSrch.executeQuery("SELECT STT_CD, STT_NM FROM STATE_T ORDER BY STT_NM ASC");

                while (rsSrch.next() == true) {
            %>
            <option value=<%= rsSrch.getString("STT_CD") %>><%= rsSrch.getString("STT_NM") %>
            <%
                }
                DatabaseManager.releaseConnection(conSrch);
            %>
            </SELECT><BR><BR></TD>
        </TR>
        <TR>
            <TD align=right>City:</TD>
            <TD align=left><input type=text maxLength=32 size=32 name=city></TD>
        </TR>
        <TR>
            <TD align=right>User&nbsp;ID:</TD>
            <TD align=left><input type=text maxLength=15 size=25 name=userid></TD>
        </TR>
        <TR>
            <TD align=right>Service&nbsp;Order&nbsp;#:</TD>
            <TD align=left><input type=text maxLength=20 size=25 name=so_num></TD>
        </TR>
        <TR>
            <TD align=right>Customer&nbsp;Name&nbsp;#:</TD>
            <TD align=left><input type=text maxLength=25 size=25 name=cust_name></TD>
        </TR>
        <TR>

            <TD align=right>AN/SPAN:&nbsp;</TD>

            <TD align=left><input type=text maxLength=20 size=25 name=an></TD>
        </TR>
        <TR>

            <TD align=right>ATN/SPP TN:&nbsp;</TD>

            <TD align=left><input type=text maxLength=12 size=25 name=atn></TD>
        </TR>
        <TR>
        <%
            if (strCT.equals("P")){
        %>
        <TR>
            <TD align=right>TNS:&nbsp;</TD>
            <TD align=left><input type=text maxLength=12 size=25 name=tns></TD>
        </TR>
        <TR>
            <%
            }

            %>
            <TD align=right>From&nbsp;Due&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:</TD>
            <TD align=left>
                <input type="TEXT" size=3 maxLength=2 NAME="from_due_date_mnth" VALUE="">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="from_due_date_dy" VALUE="">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="from_due_date_yr" VALUE="">
                &nbsp;&nbsp;&nbsp;To&nbsp;Due&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:
                <input type="TEXT" size=3 maxLength=2 NAME="to_due_date_mnth" VALUE="">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="to_due_date_dy" VALUE="">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="to_due_date_yr" VALUE="">
            </TD>
        </TR>
        <TR>
            <TD align=right>From&nbsp;Last&nbsp;Mod&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:</TD>
            <TD align=left>
                <input type="TEXT" size=3 maxLength=2 NAME="from_lst_mdfd_mnth" VALUE="<%=month%>">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="from_lst_mdfd_dy" VALUE="<%=day%>">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="from_lst_mdfd_yr" VALUE="<%=yearp%>">
                &nbsp;&nbsp;&nbsp;To&nbsp;Last&nbsp;Mod&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:
                <input type="TEXT" size=3 maxLength=2 NAME="to_lst_mdfd_mnth" VALUE="<%=month%>">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="to_lst_mdfd_dy" VALUE="<%=day%>">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="to_lst_mdfd_yr" VALUE="<%=year%>">
            </TD>
        </TR>

        <%
            Log.write(Log.DEBUG_VERBOSE, "RequestSearchView: here HERE");
            if (strCT.equals("P"))	// Allow Submitted Date search if Alltel
            {
        %>
        <TR>
            <TD align=right>From&nbsp;Submitted&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:</TD>
            <TD align=left>
                <input type="TEXT" size=3 maxLength=2 NAME="sub_from_mnth" VALUE="">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="sub_from_dy" VALUE="">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="sub_from_yr" VALUE="">
                &nbsp;&nbsp;&nbsp;To&nbsp;Submitted&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:
                <input type="TEXT" size=3 maxLength=2 NAME="sub_to_mnth" VALUE="">
                /
                <input type="TEXT" size=3 maxLength=2 NAME="sub_to_dy" VALUE="">
                /
                <input type="TEXT" size=5 maxLength=4 NAME="sub_to_yr" VALUE="">
            </TD>
        </TR>
        <%	}
        %>

        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR>
            <TD align=center colspan=2><INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;
            &nbsp;<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">&nbsp;
            &nbsp;<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Cancel"></TD>
        </TR>

        <%

        }//end of try
        catch(Exception e) {
            Log.write(Log.DEBUG_VERBOSE, "RequestSearchView: Caught exception e=[" + e + "]");
        } finally {
            DatabaseManager.releaseConnection(conSrch);
        }

        %>
    </TABLE>
</FORM>
    <jsp:include page="i_footer.htm" flush="true" />
<script>
function resetLeapYearDate() {
  var inputText = document.getElementsByName("from_lst_mdfd_mnth")[0].value + "-" + document.getElementsByName("from_lst_mdfd_dy")[0].value + "-" + document.getElementsByName("from_lst_mdfd_yr")[0].value;
  var dateformat = /^(0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])[\/\-]\d{4}$/;  
  // Match the date format through regular expression  
  if(inputText.match(dateformat))  
  {  
	  //Check which seperator is used '/' or '-'  
	  var opera1 = inputText.split('/');  
	  var opera2 = inputText.split('-');  
	  lopera1 = opera1.length;  
	  lopera2 = opera2.length;  
	  // Extract the string into month, date and year  
	  if (lopera1>1) {  
	  	var pdate = inputText.split('/');  
	  }  
	  else if (lopera2>1) {  
	  	var pdate = inputText.split('-');  
	  }  
	  var mm  = parseInt(pdate[0]);  
	  var dd = parseInt(pdate[1]);  
	  var yy = parseInt(pdate[2]);  
	  // Create list of days of a month [assume there is no leap year by default]  
	  var ListofDays = [31,28,31,30,31,30,31,31,30,31,30,31];  
  
	if (mm==2) {  
		var lyear = false;  
		if ( (!(yy % 4) && yy % 100) || !(yy % 400)) {  
			lyear = true;  
		}  
		if ((lyear==false) && (dd>=29)) {  
			document.getElementsByName("from_lst_mdfd_mnth")[0].value = "03";
			document.getElementsByName("from_lst_mdfd_dy")[0].value = "01";
		}  
		if ((lyear==true) && (dd>29)) {  
	       	document.getElementsByName("from_lst_mdfd_mnth")[0].value = "03";
       		document.getElementsByName("from_lst_mdfd_dy")[0].value = "01";
		}  
	}  
  }  
}

if(document.getElementsByName("from_lst_mdfd_mnth")[0].value == "02" || document.getElementsByName("from_lst_mdfd_mnth")[0].value == "2") {
	resetLeapYearDate();
}

</script>        
</body>
</html>

<%
                                                                                                                                                                                                                                                                /* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/JSP/RequestSearchView.jsv  $
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*   Rev 1.4   22 May 2002 08:55:56   dmartz
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*   Rev 1.2   21 Mar 2002 11:26:02   dmartz
                                                                                                                                                                                                                                                                    /*Consolidate Locks, Actions, Statuses
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*   Rev 1.1   31 Jan 2002 14:52:32   sedlak
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*
                                                                                                                                                                                                                                                                    /*   Rev 1.0   23 Jan 2002 11:06:30   wwoods
                                                                                                                                                                                                                                                                    /*Initial Checkin
                                                                                                                                                                                                                                                                           */

                                                                                                                                                                                                                                                                    /* $Revision:   1.4  $
                                                                                                                                                                                                                                                                           */

%>

