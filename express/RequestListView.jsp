<%
/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2004
 *					BY
 *                              Alltel Communications Inc.
 */
/*
 * MODULE:	RequestListView.jsp
 *
 * DESCRIPTION: Displays list of Requests
 *
 * AUTHOR:      Express devel team
 *
 * DATE:        01/01/2002
 *
 * HISTORY:
 *      pjs     7/25/2002 Correct date/time display after Oracle driver change.
 *      pjs     8/29/2002 try/catch to catch exception and close db connections.
 *		HD99494/CC30028.
 *	pjs	4/16/2004 Notify ind logic
 *	pjs	8/25/2004 Use getFullExtendedQuery (Enh #86)
 *  EK 	4/2005 Duplicate first column to the end of the row for: Add PON link at end of each row as it appears on the most left side.
 *  EK	5/2005 Modify to calculate and display of orders with SLA time near due.
 */
%>

<%-- 
kumar changed code for Express 5072 Q & V
added below code
 int tbladmnclmns= requestlistbean.getTblAdmnClmns();
  boolean lspacFlag=sdm.getLoginProfileBean().getUserBean().getCmpnyTyp().equals("P") ;
 if(!lspacFlag){
 tbladmnclmns=tbladmnclmns-5;
}

--%>
<%@ include file="i_header.jsp" %>
<jsp:useBean id="requestlistbean" scope="request" class="com.alltel.lsr.common.objects.RequestListBean" />
<% QueueCriteria queuebean = sdm.getRequestQueueCriteria(); %>

<% Log.write(Log.DEBUG_VERBOSE, "This is it.....Query = " + queuebean.getFullQuery()); %>
<% if (queuebean.getExtendedQueryString() != null && queuebean.getExtendedQueryString().length()>2) Log.write(Log.DEBUG_VERBOSE, "Query = " + queuebean.getFullExtendedQuery()); %>

<%

DateFormat AppDateFormat = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
Timestamp dtsFromDB = new Timestamp(System.currentTimeMillis());

int iRecordCount = 0;
int iSlaDueHrs = 0;
Connection con = null;
Statement stmt = null;
Statement stmt2 = null;
String strFootNoteItems="";
Vector vFootNoteItems = new Vector();
ResultSet rs=null;
ResultSet rs2=null;		//this is TEMPORARY, asnd will be replace by Singleton later
try {
        int tbladmnclmns= requestlistbean.getTblAdmnClmns();
        boolean lspacFlag=sdm.getLoginProfileBean().getUserBean().getCmpnyTyp().equals("P") ;
        if(!lspacFlag){
            tbladmnclmns=tbladmnclmns-5;
        }
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();
	stmt2 = con.createStatement();
	if (queuebean.getExtendedQueryString() == null || queuebean.getExtendedQueryString().length()< 3)
	{	rs = stmt.executeQuery(queuebean.getFullQuery());
	}
	else
	{
		for (int x = 0; x < tbladmnclmns ; x++) 
		{	//chg order by clause to a number if Extended query...
			//Log.write("PJS " + requestlistbean.getTblAdmnClmnDbNm(x));
		}
		rs = stmt.executeQuery(queuebean.getFullExtendedQuery());
	}

	iSlaDueHrs  = Integer.parseInt( 
			PropertiesManager.getProperty("lsr.deadline.warning.hours", "3" ) );
%>
	
<table width=800 align=left>
  <tr>
    <td width=100>
	<%	if ( sdm.isAuthorized(requestlistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="RequestCtlr?rqstcreate=view">&nbsp;NEW&nbsp;REQUEST&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="RequestListView" METHOD="POST" ACTION="RequestListCtlr?rqstsrch=queue">
			<tr>
			  <td>
				&nbsp;
			  </td>
		  </tr>
		</FORM>
		</table>			
	</td>
  </tr>
  <tr>
	<td>
		<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="RequestListCtlr?rqstsrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="RequestListView" METHOD="POST" ACTION="RequestListCtlr?rqstsrch=quicksrch">
			<tr>
			  <td>
				&nbsp;Quick&nbsp;Search&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchctgry">
					<OPTION VALUE="" SELECTED>...Category...</OPTION>
					<%  for (int x = 0; x < tbladmnclmns ; x++) { %>
					<OPTION VALUE="<%=x%>"><%=requestlistbean.getTblAdmnClmnDscrptn(x)%></OPTION>
					<% } %>
				</SELECT>

				&nbsp;Value:
				<input type=text maxLength=30 size=32 name=srchvl>
			  </td>
			  <td>
			  <INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="GO">
			</td></tr>
		</FORM>
		</table>
	</td>
  </tr>
</table>
<BR CLEAR=ALL>

<table width=2200 align=left border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=2>
<tr>
<% 	
	String strTempPon = " <th align=left valign=top width=" + requestlistbean.getTblAdmnClmnWdth(0)+ ">" + requestlistbean.getTblAdmnClmnDscrptn(0) 
	 + "<BR><a href=\"/RequestListCtlr?tblnmbr=" + requestlistbean.getTblAdmnSqncNmbr()+ "&amp;srtby=" + requestlistbean.getTblAdmnSortSeq(0) + "&amp;srtsqnc=ASC\"> \n"
	 + "<IMG NAME=\"Sort Ascending\" SRC=\"images/arrow_asc.gif\" BORDER=0></a>"
	 + "<a href=\"/RequestListCtlr?tblnmbr=" + requestlistbean.getTblAdmnSqncNmbr() + "&amp;srtby=" + requestlistbean.getTblAdmnSortSeq(0) + "&amp;srtsqnc=DESC\"> \n"
	 + "<IMG NAME=\"Sort Descending\" SRC=\"images/arrow_desc.gif\" BORDER=0></a></th> ";
	 	
	for (int x = 0; x < tbladmnclmns ; x++) 
	{
	
	
%>
		<th align=left valign=top width=<%=requestlistbean.getTblAdmnClmnWdth(x)%>><%=requestlistbean.getTblAdmnClmnDscrptn(x)%>
		<BR>
		<a href="/RequestListCtlr?tblnmbr=<%=requestlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=requestlistbean.getTblAdmnSortSeq(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/RequestListCtlr?tblnmbr=<%=requestlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=requestlistbean.getTblAdmnSortSeq(x)%>&amp;srtsqnc=DESC">
		<IMG NAME="Sort Descending" SRC="images/arrow_desc.gif" BORDER=0>
		</a>
		</th>
<% 	} 
%>
<%=strTempPon %>
</tr>
<%
	
	Vector vSTTSes = SLATools.getSLADispayStatuses( con, "R"  );
	while(rs.next()==true) 
	{
		iRecordCount++;
		String strVrsn = "";
		String strNotifyInd="";
%>
	
	
<% 	
	String strPon2 = "";
	String strPon3Img = "";
	String strTodayDate = "";
	String strSLADate = "";
	long nSlaTime = 0;
	String strcssClass = "listRow";			
	//Log.write(Log.DEBUG_VERBOSE, queuebean.getFullExtendedQuery() );
	for (int x = 0; x < tbladmnclmns ; x++) 
	{ 
		int tmpIndex = requestlistbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 
		strcssClass = "listRow";
		if (x == 0)
		{
			if( sdm.getLoginProfileBean().getUserBean().getCmpnyTyp().equals("P") )
			{	
				strSLADate = rs.getString( 3 );
				if( strSLADate != null && strSLADate.length() >= 10 )
				{
					if( vSTTSes != null )
					{
						if( vSTTSes.indexOf( rs.getString("RQST_STTS_CD") ) > -1  )
						{	
							strTodayDate = SLATools.getSLAStartDateTime(ExpressUtil.getCurrentDateYYYYMMDDD_HH24MMSS().substring(0,8), 
								ExpressUtil.getCurrentDateYYYYMMDDD_HH24MMSS().substring(9,15));
						
							if( !SLATools.isSLAOverDue( strTodayDate, rs.getString("SLA")) ){
							
								nSlaTime = SLATools.calculateSLA( strTodayDate, rs.getString("SLA"));
								//Log.write(Log.DEBUG_VERBOSE,  "\n SLA In seconds:********" + nSlaTime + "*********\n" );
								nSlaTime = nSlaTime / (3600);
								//Log.write(Log.DEBUG_VERBOSE,  strTodayDate + "\n SLA In Hours:********" + nSlaTime + "*********\n" + rs.getString("SLA") );
								
								if( (nSlaTime < iSlaDueHrs) && (nSlaTime >= 0) )
								 {
									strcssClass ="slawarning";							
								
								}
							}
							else 
							{
								strcssClass ="slaDue";	
							}
							
						}
					}
				}
			}
			String strNtfy = "";
			strVrsn = "(" + rs.getString("RQST_VRSN") + ")";
			if (strVrsn.equals("(0)"))
			{	strVrsn = "";	}
			strNotifyInd = rs.getString("NTFY_SQNC_NMBR");
			if ( (strNotifyInd != null) && (strNotifyInd.length()>0) )
			{	strNtfy = "&amp;_ntfy_=Y";	}
			strPon2 = "<A HREF=\""+ requestlistbean.getTblAdmnCtlr() + "?seqget=" + rs.getString(requestlistbean.getTblAdmnCtlrIdx()) + strNtfy+ "\">&nbsp;" + rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) + "&nbsp;" + strVrsn + "</A>\n";
%>
			<tr class="<%=strcssClass%>" >
			<td align=left valign=top>
			<A HREF="<%=requestlistbean.getTblAdmnCtlr()%>?seqget=<%=rs.getString(requestlistbean.getTblAdmnCtlrIdx())%><%=strNtfy%>">&nbsp;<%=rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))%>&nbsp;<%= strVrsn %></A>

<%
			try {
				if ( (strNotifyInd != null) && (strNotifyInd.length()>0) )
				{	
					//here we'd yank the image or indicator and mouse over text from singleton
					//if notify type is image, then include appropriate IMG html, if superscript...then ditto
					rs2 = stmt2.executeQuery("SELECT NTFY_CD, NTFY_TYP, FILE_NM FROM NOTIFY_TYPE_T WHERE NTFY_SQNC_NMBR="+strNotifyInd);
					rs2.next();
					if (rs2.getString("NTFY_TYP").equals("image")) 
					
					{	
						strPon3Img = "&nbsp;<A HREF=\"#footnotes\"><IMG name=\"f1\" height=\"14\" width=\"15\" SRC=\"images/"+ rs2.getString("FILE_NM") + "\" BORDER=0></A>&nbsp";
%>						&nbsp;<A HREF="#footnotes"><IMG name="f1" height="14" width="15" SRC="images/<%=rs2.getString("FILE_NM")%>" BORDER=0></A>&nbsp;
<%					}
					else {	
						strPon3Img = "&nbsp;<A HREF=\"#footnotes\">" + rs2.getString("NTFY_CD")+ "</A>&nbsp";
%>						&nbsp;<A HREF="#footnotes"><%=rs2.getString("NTFY_CD")%></A>&nbsp;
<%					}
					// hold all items for footnotes
					if (vFootNoteItems.size() > 0)
					{	String strT="";
						boolean bFnd = false;
						for (int i=0;i<vFootNoteItems.size();i++)
						{	strT = (String)vFootNoteItems.elementAt(i);
							if (strT.equals(strNotifyInd)) 
							{	bFnd=true;
								break;
							}
						}
						if (!bFnd)	vFootNoteItems.addElement(strNotifyInd);
					} 
					else
					{	vFootNoteItems.addElement(strNotifyInd);
					}
					//strFootNoteItems += strNotifyInd+", ";
				}
				else {
%>					</A>	
<%				}
			} catch (Exception ee) { 
				Log.write(Log.DEBUG_VERBOSE, "RequestListView: Caught exception ee=[" + ee + "]");
			
			}
%>
</td>
<%		}
		else
		{
%>
			<td align=left valign=top>
<%
			if ((requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			{
				dtsFromDB = Timestamp.valueOf(rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) );
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%			}
			else if ((requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("ICARE"))
			{
%>
			<% String iCare = rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)); %>
<%
                            if(iCare.trim().equals("Y")){ %>
                                <%="ICARE"%> <% }
                                else {%>
                             
                                 <%="CAMS"%> 
			<% } }
			else if ((requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("RQST_SUBMIT_DATE"))
			{
			
			  String submit_date = rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1));
			  
			  if (submit_date != null) 
			  {
			      dtsFromDB = Timestamp.valueOf(rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) );
%>
			      <%=AppDateFormat.format(dtsFromDB)%>
				
<%                        }
                        }

                        else
                            {
 %>                           <%=rs.getString(requestlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
  <%                          }
%>
			&nbsp;</td>
<%		}
%>
	

<% 	} 
%>
<td align=left valign=top>
<%=strPon2%><%=strPon3Img%>
</td>
	</tr>
<% }
   if (vFootNoteItems.size()>0)
   {	for (int i =0; i<vFootNoteItems.size(); i++)
	{	strFootNoteItems += (String)vFootNoteItems.elementAt(i) + ", ";	}

	strFootNoteItems = strFootNoteItems.substring(0,strFootNoteItems.length()-2);
	strFootNoteItems = " SELECT DISTINCT NTFY_CD, NTFY_TYP, FILE_NM, MS_OVR_TXT, DSCRPTN FROM NOTIFY_TYPE_T " +
		" WHERE NTFY_SQNC_NMBR IN (" + strFootNoteItems + ")";
	Log.write(Log.DEBUG_VERBOSE, "RequestListView: strFootNoteItems=["+strFootNoteItems+"]");
	rs = stmt.executeQuery(strFootNoteItems);
	strFootNoteItems="<A NAME=\"footnotes\"></A>";
   	while(rs.next()==true) 
   	{	//build footnote now
		if (rs.getString("NTFY_TYP").equals("image"))
		{	strFootNoteItems+="<IMG name=\"_f1\" SRC=\"images/" + rs.getString("FILE_NM") + "\" BORDER=0> " +
				rs.getString("DSCRPTN") +"<br>";
		}
		else
		{	strFootNoteItems+="<b>" + rs.getString("NTFY_CD") +"</b> "+ rs.getString("DSCRPTN") +"<br>";
   		}
   	}
   }
}//end of try
catch(Exception e) {
	Log.write(Log.DEBUG_VERBOSE, "RequestListView: Caught exception e=[" + e + "]");
}
finally {
	try {
		rs.close(); rs=null;
		stmt.close(); stmt=null;
	} catch (Exception eee) {}
        DatabaseManager.releaseConnection(con);
}

%>

</table>
<br clear=all><br>&nbsp;Records displayed: <b><%=iRecordCount%></b><br><br>
<%=strFootNoteItems%>
<A HREF="#topofpage">&nbsp;Back&nbsp;to&nbsp;Top&nbsp;</A>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>


<%
/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestListView.jsv  $
/*
/*   Rev 1.10   Aug 27 2004 13:46:30   e0069884
/* 
/*
/*   Rev 1.5   Jul 25 2002 16:35:24   sedlak
/*Correct time display after JDBC driver change.
/*
/*
/*   Rev 1.4   23 Apr 2002 10:14:38   dmartz
/* 
/*
/*   Rev 1.3   20 Mar 2002 11:24:10   dmartz
/* 
/*
/*   Rev 1.2   31 Jan 2002 13:21:42   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 08:18:02   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:28   wwoods
/*Initial Checkin
*/

/* $Revision:   1.10  $
*/
%>
