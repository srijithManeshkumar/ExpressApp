<%
/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2004
 *                                      BY
 *                              Alltel Communications Inc.
 */
/*
 * MODULE:      ExpressHomeArchive.jsp
 *
 * DESCRIPTION: Display archive notes only
 *
 * AUTHOR:      Express devel team
 *
 * DATE:        12-13-2004
 *  EK     05/21/2006 added windstream Rebranding
 * HISTORY:
 */
%>

<%@ include file="i_header.jsp" %>
<%@ include file="ExpressUtil.jsp" %>
<%@ page import ="java.util.*" %> 
<%@ page import ="java.lang.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<%

	final String PRODUCT_SERVICE_NOTES = "PRODUCT_SERVICE_NOTES";
	final String PROCESS_NOTES = "PRODUCT_SERVICE_NOTES";
	final String strStartDate = "startDate";
	final String strDateFormat = "MM-DD-YYYY";
	final String strEndDate = "endDate";
	// note we have only two types, there is no need to make them dynamic
	// Read from the database.
	

	String arrNoticeTypes_values[] = { "-1", "0", "1", "2"};
	String arrNoticeTypes_choices[] = {"Select Notice Type", "Product and Service Notice", "Process Notice", "Express Enhancement Notice"};

	String strSD_yr = null;
	String strSD_mnth = null;
	String strSD_dy = null;
	String strSD_yr_end = null;
	String strSD_mnth_end = null;
	String strSD_dy_end = null;
	
	String strED_yr = null;
	String strED_mnth = null;
	String strED_dy = null;
	
	String strED_yr_end = null;
	String strED_mnth_end = null;
	String strED_dy_end = null;
	
	String strTitle =null;
	String strStartDate1 = null;
	String strStartDate2 = null;
	String strNoteType = null;	
	String strState =null;
	String strResetflag = alltelRequest.getParameter( "resetflag" );
	 
	 if(  strResetflag != null )
	 {
	 	if( !strResetflag.equals("yes" ) )
	 	{
			 strSD_yr = alltelRequest.getParameter("startDate_yr");
			 strSD_mnth = alltelRequest.getParameter("startDate_mnth");
			 strSD_dy = alltelRequest.getParameter("startDate_dy");
			
			 strSD_yr_end = alltelRequest.getParameter("startDate_yr_end");
			 strSD_mnth_end = alltelRequest.getParameter("startDate_mnth_end");
			 strSD_dy_end = alltelRequest.getParameter("startDate_dy_end");
			
			 strED_yr = alltelRequest.getParameter("endDate_yr");
			 strED_mnth = alltelRequest.getParameter("endDate_mnth");
			 strED_dy = alltelRequest.getParameter("endDate_dy");
			
			 strED_yr_end = alltelRequest.getParameter("endDate_yr_end");
			 strED_mnth_end = alltelRequest.getParameter("endDate_mnth_end");
			 strED_dy_end = alltelRequest.getParameter("endDate_dy_end");
			
			 strTitle = alltelRequest.getParameter("titlefield");
			 strStartDate1 = alltelRequest.getParameter("nt_startDate1");
			 strStartDate2 = alltelRequest.getParameter("nt_startDate2");
		 	 strNoteType = alltelRequest.getParameter("Noticetype");	
		 	 strState = alltelRequest.getParameter( "state" );
		 }
	 }
%>
<script language = "JavaScript">
// Edris, validating dates on client side.
var dtStr = "-"
function daysInFebruary (year){
    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
}
function daysInMonth( month, year ) {
	if (month == 4 || month ==6 ||  month == 9||  month == 11) {
		return 30;
	}
	else if (month == 2) { 
		return daysInFebruary(year);
	}
	return 31;
}
function isNumber( str ){
	var i;
	
    for (i = 0; i < str.length; i++){   
        var chr = str.charAt(i);
        if (((chr < "0") || (chr > "9"))) 
        return false;
    }
    return true;
}

function isValidDate( strDate){
	// rid of whitespace in the date string.
	if( strDate.length > 10 || strDate.length < 6 ){
		alert("Wrong Date format! Please use the following format and submit again: mm/dd/yyyy")
		return false
	}
	var separator1_pos = strDate.indexOf(dtStr);
	var separator2_pos = strDate.indexOf(dtStr, separator1_pos+1);
	var strMonth = strDate.substring(0,separator1_pos);
	var strDay = strDate.substring(separator1_pos+1,separator2_pos);
	var strYear = strDate.substring(separator2_pos+1);
	var nMonth = parseInt(strMonth, 10)
	var nDay = parseInt(strDay, 10 )
	var nYear = parseInt(strYear,10)
	if( separator1_pos <=  -1 || separator2_pos <= -1){
		alert("Please use the following format when entering dates: mm/dd/yyyy")
		return false	
	}
	if ( strMonth.length < 1 || nMonth < 1 || nMonth > 12 ||  !isNumber(strMonth)){	
		alert("Please enter a valid Month")	
		return false
	}

	if (strDay.length<1 || nDay < 1 || nDay > 31 || ( nMonth == 2 && nDay > daysInFebruary(nYear)) || nDay > daysInMonth(nMonth, nYear) ||  !isNumber(strDay)){
		alert("Please enter a valid Day of the Month")
		return false
	}
	if ( strYear.length != 4 || nYear==0 ||  !isNumber(strYear)){
		alert(strYear +" is not a valid year, please enter a 4 digit year between "+minYear+" and "+maxYear)
		return false
	}	
	return true
}

function beforeSubmitArchive()
{
	var m1 = document.forms[0].startDate_mnth.options[document.forms[0].startDate_mnth.selectedIndex].value;
	var d1 = 0;
	var y1 = document.forms[0].startDate_yr.options[document.forms[0].startDate_yr.selectedIndex].value;
	var m2 = document.forms[0].startDate_mnth_end.options[document.forms[0].startDate_mnth_end.selectedIndex].value;
	var d2 = 0;
	var y2 = document.forms[0].startDate_yr_end.options[document.forms[0].startDate_yr_end.selectedIndex].value;
   
   	d2 = daysInMonth( m2, y2 ); // make to date being the last day of the month;
   	d1 = daysInMonth( m1. y1 ) ;
    d1 = d1 / d1; // make from date being the 1st of the month;
    var strDate1 = m1 + "-" + d1  + "-" + y1;
    var strDate2 = m2 + "-" + d2  + "-" + y2;
    if ( isValidDate( strDate1 )==false || isValidDate(strDate2) == false ) {
			return false
	}
	document.forms[0].nt_startDate1.value = strDate1;
	document.forms[0].nt_startDate2.value = strDate2; 	
    return true;
   
}

function resetToDefault(){
	document.forms[0].resetflag.value = 'yes';
	document.forms[0].submit();
} 
</SCRIPT>

<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD align=center width=50%><SPAN class="header1">Express&nbsp;Archived&nbsp;Messages</SPAN></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<BR>
<!--
<BR CLEAR=ALL>

	<TABLE width=75% align=center border=1 cellspacing=0 cellpadding=0>
		<TR><TD align=center><b>Messages from last 3 months (most recently expired listed first)</b><BR></TD></TR>
	</TABLE><BR CLEAR=ALL>
-->
<!--********************** FORM SECTION ********************-->
<form action="ExpressHomeArchive.jsp" onSubmit="return beforeSubmitArchive();" method="POST">
<table  border=0 align=center width="75%" cellspacing=2 cellpadding=1>
<tr>
  	<td colspan=2 class=tHeader>
  		<SPAN class="barheader">Express Archived Messages Form</span>
  </td>
  </tr>
<tr><td>
<table   align=center width="75%" cellspacing=2 cellpadding=1>
  
  	<tr>
  		<td class=rowheader>
  			Message Title:
   		</td>
	  	<td>
	  		<input type="TEXT" size="50" maxLength="200" NAME="titlefield" VALUE="<%=(strTitle==null?"":strTitle )%>">
	  	</td>
  	</tr>
  	<tr>
  		<TD align=left; class=rowheader >Posted Date <i>(MM/YYYY)</i>:</TD>
		<TD>From<%=printDateSelect( strStartDate, false, false, strSD_mnth, strSD_yr )%>&nbsp;&nbsp;&nbsp;To&nbsp;&nbsp;&nbsp;
			<%=printDateSelect( strStartDate, false, true, strSD_mnth_end, strSD_yr_end )%><br>
		</TD>
	</tr>
	
	<tr>
	<td align=left; class=rowheader>Notice Type:</td>
	<td>
			<%=printSelectBox("Noticetype", 1, arrNoticeTypes_values, arrNoticeTypes_choices, (strNoteType == null? "-1": strNoteType  ) )%>
		</td>
	</tr>	
	<td align=left; class=rowheader>State:</td>
	<td>
			<%=printSelectBoxState("state", strState,  1  )%>
		</td>
	</tr>	
	
	
	<tr><td colspan=2 align=center>
		<INPUT class=appButton TYPE="RESET" onclick="resetToDefault();"  value="Clear Form">&nbsp;&nbsp;&nbsp;&nbsp;
		<INPUT class=appButton  TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
		</td>
	</tr>
 </table>	
 </td></tr></table>
<%=printInputHidden( "nt_startDate1", "")%> 
<%=printInputHidden( "nt_startDate2", "")%> 
<%=printInputHidden( "resetflag", "")%> 
</form>
<%
	
	// build whereclaue:	
	int nType = 0;
	String query = "";
	boolean stateIncluded = false;
	String strUserid =  sdm.getUser();
	String strTempTP =  " NOTE_TYP_CD =  ? ";
	if( strNoteType != null )
	{
	
		
		if( !strNoteType.equals("-1" )  ) 
		{
			
			nType = Integer.parseInt( strNoteType );
			if( nType == 2)
				strState = "__";
			
			
		}else 
		{
			strTempTP = " NOTE_TYP_CD >=  ? ";
		}
		
		
		
		if( strTitle == null || strTitle.length() <= 0 )
		{
			strTitle = "%";
		}
		else
		{
			strTitle = "%" + findReplace( strTitle, "'", "_" ) + "%";
		}
		
		if( !strState.equals("__" ) )
		{
			stateIncluded = true;			
			query = " SELECT  hp.NOTE_TITLE, to_char(hp.NOTE_STRT_DT,'Day Month DD, YYYY hh:mi AM')," 
				+ " hp.NOTE_SQNC_NMBR, hp.NOTE_MSG FROM HOME_PAGE_NOTES_T hp, NOTES_STATES_LINK_T NS WHERE " 
				+ " hp.NOTE_SQNC_NMBR = NS.NOTE_SQNC_NMBR "
				+ " AND hp.NOTE_STRT_DT BETWEEN TO_DATE( ?, ? ) AND TO_DATE( ?, ? )  " 
				+ " AND upper(hp.NOTE_TITLE) like upper(?) AND " + strTempTP
				+ " AND upper( hp.CMPNY_TYP_LIST )  LIKE upper( ? ) "
				+ " AND NS.STT_CD = ?  ORDER by hp.NOTE_END_DT desc, hp.NOTE_STRT_DT desc "; 
				
		}else
		{
		
			query = " SELECT  NOTE_TITLE, to_char(NOTE_STRT_DT,'Day Month DD, YYYY hh:mi AM')," 
				+ " NOTE_SQNC_NMBR, NOTE_MSG FROM HOME_PAGE_NOTES_T WHERE " 
				+ " NOTE_STRT_DT BETWEEN TO_DATE( ?, ? ) AND TO_DATE( ?, ? )  " 
				+ " AND upper(NOTE_TITLE) like upper(?) AND " + strTempTP
				+ " AND upper( CMPNY_TYP_LIST )  LIKE upper( ? ) ORDER by NOTE_END_DT desc, NOTE_STRT_DT desc "; 
		}
	
	
	
	}else
	{
	
		query = " SELECT  NOTE_TITLE, to_char(NOTE_STRT_DT,'Day Month DD, YYYY hh:mi AM')," 
			+ " NOTE_SQNC_NMBR, NOTE_MSG FROM HOME_PAGE_NOTES_T WHERE " 
			+ " NOTE_STRT_DT between sysdate - 90 AND Sysdate " 
			+ " AND upper( CMPNY_TYP_LIST )  LIKE upper( ? ) ORDER by NOTE_END_DT desc, NOTE_STRT_DT desc "; 
	
	
	}
	Connection con = null;
	PreparedStatement pstmt = null;
	ResultSet rs =null;
	String strCmpnyTyp = "%" + sdm.getLoginProfileBean().getUserBean().getCmpnyTyp() + "%";
	int iCounter = 0;
	try {
		con = DatabaseManager.getConnection();
		pstmt = con.prepareStatement( query  );
		pstmt.clearParameters();
		if( strNoteType == null )
		{
			pstmt.setString( 1, strCmpnyTyp );
			strNoteType = "-1";
		} else
		{
		
			pstmt.setString( 1, strStartDate1 );	
			pstmt.setString( 2, strDateFormat );
			pstmt.setString( 3, strStartDate2 );	
			pstmt.setString( 4, strDateFormat );
			pstmt.setString( 5, strTitle );
			pstmt.setInt( 6, nType );
			pstmt.setString( 7, strCmpnyTyp );
			if(stateIncluded)
			{
				pstmt.setString( 8, strState );
			}
		}
		rs = pstmt.executeQuery( );				
		
		%><TABLE width="75%" align="center" cellspacing="1" cellpadding="1">
			<TR class=tHeader>Posted Date / Title / Note</td></tr>
		<%	
		while( rs.next() == true )			
		{
			if(iCounter % 2 == 0 )
			{
				%>
				<TR class=roweven>
				<%
			}else
			{
				%>
					<TR class= rowodd>
				<%
			}
			%>
				
				<TD class=smaller align=center valign=top>
				<span class=smallNote><%= rs.getString(2)%></span><br><br>
				<span class=notetitle><%= rs.getString(1)%></span><br><Br>
				<%= rs.getString(4)%>
				</td>
				</TR>	
				<tr><td>&nbsp;</td></tr>	
			<%
			iCounter++;
		} //while
		%>
			</TABLE>
			<BR CLEAR=ALL><BR>
	   <%
	} //try
	catch(Exception e) {
		Log.write(Log.DEBUG_VERBOSE, "ExpressHomeArchive: Caught exception e=[" + e + "]");
	}
	finally {
		try {
			rs.close(); rs=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) {}
		DatabaseManager.releaseConnection(con);
	}

%>

<BR>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/ExpressHome.jsv  $
/*
/*   Rev 1.4   Jun 04 2004 09:02:58   e0069884
/* 
/*
/*   Rev 1.1   31 Jan 2002 14:36:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:36   wwoods
/*Initial Checkin
*/

/* $Revision:   1.4  $
*/
%>

