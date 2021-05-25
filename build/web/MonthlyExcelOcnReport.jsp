<%-- 
    Document   : MonthlyExcelOcnReport
    Created on : May 24, 2011, 1:03:28 PM
    Author     : satish.t
--%>
<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "MonthlyOcnReport" + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

            AlltelRequest alltelRequest = null;
            AlltelResponse alltelResponse = null;
            SessionDataManager sdm = null;
            try {
                alltelRequest = new AlltelRequest(request);
                alltelResponse = new AlltelResponse(response);
                sdm = alltelRequest.getSessionDataManager();
                if ((sdm == null) || (!sdm.isUserLoggedIn())) {
                    alltelResponse.sendRedirect("LoginCtlr");
                    return;
                }
            } catch (Exception e) {
                Log.write(Log.ERROR, e.getMessage());
                Log.write(Log.ERROR, "Trapped in i_header.jsp");
            }
	

	Connection con = null;
	Statement  stmt = null;
	int	   iCurrentWeek = 0;
	int	   iPrevWeek = 0;
	int	   iMth = 0;
	int	   iPrevMth = 0;
	int    iDOM = 0;
	int    iDOW = 0;
	final int LSRS = 0;
	final int REJECTS = 1;
	final int CANCELS = 2;

    //Our lovely counters
	int	   iOCNCount = 0;
	int	   iDailyLSR= 0;
	int	   iWkLSR= 0;
	int	   iMthLSR= 0;
	int[][]  arDOMCount = new int[3][31];   //Day of the month counters
	String[] arTotHeadings = {"LSRs&nbsp;SUBMITTED", "REJECTS", "CANCEL&nbsp;PONs" };


	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "MthOcnRpt: Invalid from date");
%>
		<jsp:forward page="MthDateSelect.jsp?MonthlyOcnReport.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + "01";

	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "MthOcnRpt: Invalid to date");
%>
		<jsp:forward page="MthDateSelect.jsp?MonthlyOcnReport.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + "01";
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "MontlyReport: Invalid to date");
%>
		<jsp:forward page="MthDateSelect.jsp?MonthlyOcnReport.jsp"/>;
<%
		return;
	}
	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

	Log.write(Log.DEBUG_VERBOSE, "MthOcnRpt: Date:" + strStartDate + " Date:" + strEndDate);

	//Build from and to calendar ranges
	Calendar calFrom = Calendar.getInstance();
	calFrom.set(Integer.parseInt(strStartDate.substring(0,4)),
	 	    Integer.parseInt(strStartDate.substring(4,6)) - 1,
		    1, 0, 0, 0);
	iPrevWeek = calFrom.get(Calendar.WEEK_OF_MONTH);
	iCurrentWeek = iPrevWeek;
	iMth = calFrom.get(Calendar.MONTH);
	iPrevMth = iMth;

	Calendar calTo = Calendar.getInstance();
	calTo.set(Integer.parseInt(strEndDate.substring(0,4)),
	 	    Integer.parseInt(strEndDate.substring(4,6)) - 1,
		    1, 23, 59, 59);
	int iMaxDays = calTo.getActualMaximum(Calendar.DAY_OF_MONTH);
	calTo.set(Calendar.DAY_OF_MONTH, iMaxDays);

	Log.write(Log.DEBUG_VERBOSE, "MthOcnRpt: From:" + calFrom.getTime() + " To:" + calTo.getTime());


	con = DatabaseManager.getConnection();
	PreparedStatement pstmt = con.prepareStatement("SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM " +
                " FROM OCN_T O, OCN_STATE_T OS WHERE O.OCN_CD = OS.OCN_CD ORDER BY O.OCN_NM");

    String strOCNQuery = "SELECT RH.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN, TO_CHAR(RH.RQST_HSTRY_DT_IN,'YYYYMMDD HH24MISS') " +
                  " FROM REQUEST_T R, REQUEST_HISTORY_T RH WHERE R.OCN_CD = ? AND R.OCN_STT=? AND " +
                  " R.RQST_SQNC_NMBR = RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT " +
                  " AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
                                " WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
                                " AND RH2.RQST_STTS_CD_IN IN ('FOC', 'REJECTED') " +
                                " AND RH2.RQST_HSTRY_DT_IN BETWEEN " +
                                " TO_DATE(?, 'YYYYMMDD HH24:MI:SS') AND " +
                                " TO_DATE(?, 'YYYYMMDD HH24:MI:SS') ) " +
                  " ORDER BY RH.RQST_SQNC_NMBR, RH.RQST_HSTRY_DT_IN DESC";
    PreparedStatement pstmt2 = con.prepareStatement(strOCNQuery);

	//Loop month by month and create this table
	DecimalFormat df = new DecimalFormat("##0.00");
	DateFormat weFmt = new SimpleDateFormat("MM/dd/yyyy");
	DateFormat YYYYMMDDFmt = new SimpleDateFormat("yyyyMMdd");
	DateFormat mthFmt = new SimpleDateFormat("MMM yyyy");

%>


<br><center>
<SPAN CLASS="header1">
M&nbsp;O&nbsp;N&nbsp;T&nbsp;H&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;V&nbsp;E&nbsp;N&nbsp;D&nbsp;O&nbsp;R&nbsp;</SPAN>
<br><b>Date&nbsp;Range:&nbsp;<%=mthFmt.format(calFrom.getTime())%>&nbsp;-&nbsp;<%=mthFmt.format(calTo.getTime())%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br><br>

<%

	while ( calFrom.before(calTo) )
	{
	    for (int j=0; j < arDOMCount.length;j++) {
		    for (int i=0; i < arDOMCount[j].length; i++)         arDOMCount[j][i]=0;
		}

%>
		<center><font size="+2"><b><%=mthFmt.format(calFrom.getTime())%></b></font></center><br>
		<font size=-1>
		<table border=1 align=center cellspacing=0 cellpadding=1 RULES="ROWS">
        <tr>
<%
//<table border=1 align=center cellspacing=0 cellpadding=1>
        //---------------------------------------------------
        //Build table headers for the month we are processing
        //---------------------------------------------------
        Calendar cal;
        cal = (Calendar)calFrom.clone();

        String strWE = "";
        cal = (Calendar)calFrom.clone();
        while ( cal.get(Calendar.MONTH) == calFrom.get(Calendar.MONTH) )
        {
                iCurrentWeek = cal.get(Calendar.WEEK_OF_MONTH);
                if ( iPrevWeek != iCurrentWeek )
                {
%>
			        <th align=center nowrap bgcolor="#efefef"><%=strWE%></th>
            		<th align=left bgcolor="#efefef">S&nbsp;</th>
            		<th align=left bgcolor="#efefef">M&nbsp;</th>
            		<th align=left bgcolor="#efefef">T&nbsp;</th>
            		<th align=left bgcolor="#efefef">W&nbsp;</th>
	            	<th align=left bgcolor="#efefef">T&nbsp;</th>
	            	<th align=left bgcolor="#efefef">F&nbsp;</th>
	            	<th align=left bgcolor="#efefef">S&nbsp;</th>
	            	<th align=left bgcolor="#efefef">Totals</th>
	            	<th align=left>&nbsp;&nbsp;&nbsp;&nbsp;</th>
<%
                }
                iPrevWeek = iCurrentWeek;

                strWE = "&nbsp;&nbsp;Wk&nbsp;Ending<br>&nbsp;&nbsp;" + weFmt.format( cal.getTime() );
                cal.add(Calendar.DATE, 1);
                if ( cal.get(Calendar.MONTH) != calFrom.get(Calendar.MONTH) )
                {
                    //do final week of mth
%>
			        <th align=center nowrap  bgcolor="#efefef"><%=strWE%></th>
            		<th align=left bgcolor="#efefef">S&nbsp;</th>
            		<th align=left bgcolor="#efefef">M&nbsp;</th>
            		<th align=left bgcolor="#efefef">T&nbsp;</th>
            		<th align=left bgcolor="#efefef">W&nbsp;</th>
	            	<th align=left bgcolor="#efefef">T&nbsp;</th>
	            	<th align=left bgcolor="#efefef">F&nbsp;</th>
	            	<th align=left bgcolor="#efefef">S&nbsp;</th>
	            	<th align=left bgcolor="#efefef">Totals</th>
	            	<th align=left>&nbsp;&nbsp;&nbsp;</th>
	            	<th align=center bgcolor="#efefef">&nbsp;Mthly&nbsp;Totals</th>
<%
                }
        }//while ()
%>
			    	</tr>
<%
        //---------------------------------------------------
        // Now spin thru each OCN and get/print statistics
        //---------------------------------------------------
        ResultSet rsOCNs = pstmt.executeQuery();
        while (rsOCNs.next() == true)
        {
            iOCNCount++;
            cal = (Calendar)calFrom.clone();    //Start of date range
            iDOW = cal.get(Calendar.DAY_OF_WEEK);

            //int iOCN = rsOCNs.getInt("OCN_CD");
            String strOCN = rsOCNs.getString("OCN_CD");
            String strOCNState = rsOCNs.getString("STT_CD");
            String strOCNName = rsOCNs.getString("OCN_NM");
            strOCNName += " (" + strOCN + "-" + strOCNState + ")";

         	iCurrentWeek = cal.get(Calendar.WEEK_OF_MONTH);
	        iPrevWeek = 0;
		    iMth = cal.get(Calendar.MONTH);
		    iPrevMth = iMth;

		    while ( cal.get(Calendar.MONTH) == calFrom.get(Calendar.MONTH) )
		    {
			    while (iCurrentWeek == iPrevWeek)
			    {
    		        //Gather statistics for this day
    		        //First get the day of mth
    		        iDOM = cal.get(Calendar.DAY_OF_MONTH) - 1; //remove 1 for array use
	        	    iDailyLSR=0;
	    	        //pstmt2.setInt(1, iOCN);
	    	        pstmt2.setString(1, strOCN);
    		        pstmt2.setString(2, strOCNState);
    		        pstmt2.setString(3, YYYYMMDDFmt.format(cal.getTime()) + " 00:00:00");
	        	    pstmt2.setString(4, YYYYMMDDFmt.format(cal.getTime()) + " 23:59:59");
	    	        ResultSet rs2 = pstmt2.executeQuery();
    		        while (rs2.next() == true)
         		    {
            			String strStatus = rs2.getString("RQST_STTS_CD_IN");
           				if ( strStatus.equals("FOC") )
           				{	iDailyLSR++;
           				    arDOMCount[LSRS][iDOM]++;
		            	}
           				if ( strStatus.equals("REJECTED") )
		            	{	arDOMCount[REJECTS][iDOM]++;
           				}
           				if ( strStatus.equals("CANCELLED") )        //NOTE: Also needs to get SUPPs with Xcl flag set
		            	{	arDOMCount[CANCELS][iDOM]++;
           				}
    		        } // while()
%>
		    		<td align=right><%=iDailyLSR%>&nbsp;</td>
<%
    			    //Add statistics to weekly total
	    		    iWkLSR += iDailyLSR;
    			    cal.add(Calendar.DATE, 1);
    			    iCurrentWeek = cal.get(Calendar.WEEK_OF_MONTH);
    			    iMth = cal.get(Calendar.MONTH);
			    }
			    //If new week, then Spit out stats for the week we just completed
			    if (iPrevWeek == 0) //first week
			    {
%>
				    <tr>
			    	<td nowrap align=left><%=strOCNName%></td>
<%
			    	//skip buckets until we reach 1st of month
			    	for (int i=1; i < iDOW; i++)
			    	{
%>
			    	    <td align=right>&nbsp;</td>
<%
			    	}
                }
                else
                {
        			if (iMth != iPrevMth)
        			{
        			    iDOW = cal.get(Calendar.DAY_OF_WEEK);
    			    	//skip buckets past month end
	    		    	for (int i=iDOW; i < Calendar.SATURDAY+1; i++)
		    	    	{
%>
			        	    <td align=right>&nbsp;</td>
<%
			        	}
%>
    		    		<td align=right bgcolor="#efefef"><%=iWkLSR%></td><td>&nbsp;</td>
<%

           			}
           			else
           			{
%>
	    	    		<td align=right bgcolor="#efefef"><%=iWkLSR%></td><td>&nbsp;</td><td align=left>&nbsp;</td>
<%
           			}

	    	    	//Add weekly total to MTD
    	    		iMthLSR += iWkLSR;
	       			//reset weekly counters
        			iWkLSR = 0;
                }
                iPrevWeek = iCurrentWeek;
	    		if (iMth != iPrevMth)
    			{
%>
				    	<td align=right bgcolor="#efefef"><b><%=iMthLSR%></b></td>
    					</tr>
<%
					    iMthLSR = 0;
				}

			}  // while() months equal

        } // while() for OCNs

//////////////////////
        // Spit out the montly LSR totals by day of month
        for (int iTot = 0; iTot < arDOMCount.length; iTot++)
        {
            cal = (Calendar)calFrom.clone();
            iDOW = cal.get(Calendar.DAY_OF_WEEK);
       	    iCurrentWeek = cal.get(Calendar.WEEK_OF_MONTH);
            iPrevWeek = 0;
    	    iMth = cal.get(Calendar.MONTH);
	        iPrevMth = iMth;
	        while ( cal.get(Calendar.MONTH) == calFrom.get(Calendar.MONTH) )
    	    {
			        while (iCurrentWeek == iPrevWeek)
			        {
                                    iDOM = cal.get(Calendar.DAY_OF_MONTH) - 1; //remove 1 for array use
%>
	    	    		<td align=right bgcolor="#efefef"><%=arDOMCount[iTot][iDOM]%>&nbsp;</td>
<%
        			    iWkLSR += arDOMCount[iTot][iDOM];
                                    cal.add(Calendar.DATE, 1);
                                    iCurrentWeek = cal.get(Calendar.WEEK_OF_MONTH);
                                    iMth = cal.get(Calendar.MONTH);
                                }
    			    //If new week, then Spit out stats for the week we just completed
			        if (iPrevWeek == 0) //first week
			        {
%>
		    		    <tr>
                                    <td nowrap align=right bgcolor="#efefef"><b><%=arTotHeadings[iTot]%></b></td>
<%
                                    //skip buckets until we reach 1st of month
			    	    for (int i=1; i < iDOW; i++)
                                    {
%>
			        	    <td align=right bgcolor="#efefef">&nbsp;</td>
<%
                                    }
                                }
                                else
                                {
                                    if (iMth != iPrevMth)
                                    {
                                        iDOW = cal.get(Calendar.DAY_OF_WEEK);
        			    	//skip buckets past month end
                                        for (int i=iDOW; i < Calendar.SATURDAY+1; i++)
                                        {
%>
			        	        <td align=right bgcolor="#efefef">&nbsp;</td>
<%
			        	}
%>
                                        <td align=right bgcolor="#efefef"><%=iWkLSR%></td><td>&nbsp;</td>
<%

                                        }
                                        else
                                        {
%>
                	    	    		<td align=right bgcolor="#efefef"><%=iWkLSR%></td><td>&nbsp;</td>
                                		<td nowrap align=right bgcolor="#efefef"><b><%=arTotHeadings[iTot]%></b></td>
<%
                                        }

                                        //Add weekly total to MTD
                                        iMthLSR += iWkLSR;
                                        //reset weekly counters
                                        iWkLSR = 0;
                                }
                                iPrevWeek = iCurrentWeek;
                                if (iMth != iPrevMth)
                                {
%>
				    	<td align=right bgcolor="#efefef"><b><%=iMthLSR%></b></td>
    					</tr>
<%
			    		    iMthLSR = 0;
		    		}

    		}  // while() months equal
        } //for()

//////////////////////

		calFrom = (Calendar)cal.clone();    //bump From date and clear counters
	    for (int j=0; j < arDOMCount.length;j++) {
		    for (int i=0; i < arDOMCount[j].length; i++)         arDOMCount[j][i]=0;
		}

%>
        </table></font><br><br>
<%

	} //while()
	DatabaseManager.releaseConnection(con);
%>

</UL>
<BR>
<BR>
</FORM>

</BODY>
</HTML>