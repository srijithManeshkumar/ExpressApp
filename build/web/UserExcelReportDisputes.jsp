<%-- 
    Document   : UserExcelReportDisputes
    Created on : May 24, 2011, 2:36:52 PM
    Author     : satish.t
--%>
<%@ include file="ExpressUtil.jsp" %>
<%@ page import ="java.util.*, com.alltel.lsr.common.batch.UserReportInfo" %>
<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "UserReportDisputes" + ".xls");
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
            final long DAY_IN_SEC = (long) 86400;
            final long HOUR_IN_SEC = (long) 3600;
            final long MIN_IN_SEC = (long) 60;



            // Did they cancel?
            if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel")) {
%>
<jsp:forward page="Reports.jsp"/>;
<%		return;
            }

            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            long lIntervalTotals = 0;
            boolean bSpecificUserids = false;
            Hashtable m_hashUserids;
            Vector m_vSortedUsers = new Vector();	//use this to retreive hash in same ascending order every time

            String strCheckBox = alltelRequest.getParameter("keep_weekends");
            boolean bKeepWeekends = false;
            if ((strCheckBox == null) || (strCheckBox.length() < 1)) {
            } else {
                bKeepWeekends = true;
            }
            strCheckBox = alltelRequest.getParameter("count_weekends");
            boolean bCountWeekends = false;
            if ((strCheckBox == null) || (strCheckBox.length() < 1)) {
            } else {
                bCountWeekends = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "UserReport() Weekend options = " + bKeepWeekends + " " + bCountWeekends);

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "UserRpt Invalid from date");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
            }
            String strStartDate = strStartYr + strStartMth + strStartDay;

            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            String strEndDay = alltelRequest.getParameter("to_due_date_dy");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0) || (strEndDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
            }
            //Check days of month and adjust if necessary ...
            Calendar calStart = Calendar.getInstance();
            calStart.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, 1, 0, 0, 0);
            int iMaxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strStartDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid from date");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
            }
            calStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strStartDay));

            Calendar calEnd = Calendar.getInstance();
            calEnd.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
            }
            calEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strEndDay));
            calEnd.set(Calendar.HOUR_OF_DAY, 23);

            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
            DateFormat dowFmt = new SimpleDateFormat("MM/dd - EE");
            try {
                con = DatabaseManager.getConnection();
                Statement stmt2 = con.createStatement();

                String strUserid = "";
                String[] strUserids = alltelRequest.getAttributeValue("USERID");


                /***************Extract Employee groups Section ******************************/
                PreparedStatement pstmt = null;
                ResultSet rset = null;
                String[] strEmployeeGroups = alltelRequest.getAttributeValue("groupids");
                boolean egFlag = false;
                // Check if user selected all groups
                if (strEmployeeGroups != null) {
                    if (isElementOf (strEmployeeGroups, "ALL")) {
                        egFlag = true;
                    }

                    String strQry = " Select DISTINCT USERID FROM USR_USRGRP_LINK_T WHERE "
                            + " STATUS = ? ";
                    String strWhereClause = "";
                    int iIdCounter = 0, iTempCnt = 0;
                    Vector vEmpArr = new Vector(5);
                    // If all groups were not selected,
                    //build a binding statement for each group id selected

                    if (!egFlag) {
                        strWhereClause = " AND USRGRP_EMP_SQNC_NMBR IN (";
                        iIdCounter = strEmployeeGroups.length;
                        if (iIdCounter > iTempCnt) {
                            iTempCnt++;
                            strWhereClause += "?";
                        }
                        while (iIdCounter > iTempCnt) {
                            strWhereClause += ",? ";
                            iTempCnt++;
                        }
                        strWhereClause += " )";
                    }
                    strQry += strWhereClause;
                    String strActiveStatus = "N";
                    pstmt = con.prepareStatement(strQry);
                    pstmt.clearParameters();
                    pstmt.setString(1, strActiveStatus);
                    for (int j = 0; j < iIdCounter; j++) {
                        pstmt.setInt(j + 2, Integer.parseInt(strEmployeeGroups[j]));
                    }
                    rset = pstmt.executeQuery();
                    while (rset.next()) {
                        vEmpArr.add(rset.getString(1));
                    }

                    // expand user array
                    int iUsrInGroups = vEmpArr.size();
                    int ipos = 0;
                    String new_users[] = null;
                    if (strUserids == null) {
                        new_users = new String[iUsrInGroups];
                        ipos = 0;
                    } else {
                        new_users = new String[strUserids.length + iUsrInGroups];
                        System.arraycopy(strUserids, 0, new_users, 0, strUserids.length);
                        ipos = strUserids.length;
                    }
                    String strTempId = "";
                    for (int i = 0; i < iUsrInGroups; i++) {
                        strTempId = (String) vEmpArr.get(i);
                        //skip duplicates
                        if (!ExpressUtil.isElementOf(strUserids, strTempId)) {
                            new_users[ipos] = strTempId;
                            ipos++;
                        }
                    }
                    // Trim array
                    if (ipos > 0) {
                        strUserids = new String[ipos];
                        System.arraycopy(new_users, 0, strUserids, 0, ipos);
                    }
                    pstmt.close();
                    pstmt = null;
                    rset.close();
                    rset = null;
                }

                if (strUserids != null) {
                    for (int i = 0; i < strUserids.length; i++) {
                        if (strUserids[i].equals("ALL")) {
                            strUserid = "ALL";
                            break;
                        } else {
                            if (strUserid.length() > 0) {
                                strUserid += ",";
                            }
                            strUserid += "'" + strUserids[i] + "'";
                        }
                    }
                }

                String strQuery1 = "";
                if (strUserid.equals("ALL")) {
                    strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM "
                            + " FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA "
                            + " WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' "
                            + " AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD "
                            + " AND SGA.SCRTY_OBJCT_CD='PROV_DISPUTE_ACTIONS' ORDER BY U.LST_NM";
                } else {
                    bSpecificUserids = true;
                    strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM FROM USERID_T U, COMPANY_T C "
                            + " WHERE U.USERID IN (" + strUserid + ") AND U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR "
                            + " AND C.CMPNY_TYP='P'";
                }
                //Log.write(Log.DEBUG_VERBOSE, "Query=["+strQuery1+"]");
                m_hashUserids = new Hashtable();

                stmt = con.createStatement();
                rs = stmt.executeQuery(strQuery1);
                String strTemp;
                //Load hash table with possible userids
                while (rs.next()) {
                    strTemp = rs.getString(1);
                    UserReportInfo objURI = new UserReportInfo(strTemp, rs.getString(2), rs.getString(3));
                    m_hashUserids.put(strTemp, objURI);
                    m_vSortedUsers.addElement(strTemp);
                }

                if (m_hashUserids.size() < 1) {
                    alltelRequest.getHttpRequest().setAttribute("reportstat", "Invalid userid selected. Choose another");
                    Log.write(Log.DEBUG_VERBOSE, "UserRpt: Hash tbl empty ");
%>
<jsp:forward page="UserRptDateSelect.jsp"/>;
<%		return;
    }
%>

<STYLE TYPE="text/css">
    .break { page-break-before: always; }
</STYLE>

<br><center>
    <SPAN CLASS="header1">B&nbsp;i&nbsp;l&nbsp;l&nbsp;i&nbsp;n&nbsp;g&nbsp;&nbsp;&nbsp;D&nbsp;i&nbsp;s&nbsp;p&nbsp;u&nbsp;t&nbsp;e&nbsp;s&nbsp;</SPAN><br>
    <SPAN CLASS="header1"> U&nbsp;s&nbsp;e&nbsp;r&nbsp;&nbsp;&nbsp;S&nbsp;t&nbsp;a&nbsp;t&nbsp;i&nbsp;s&nbsp;t&nbsp;i&nbsp;c&nbsp;s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t</SPAN>
    <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
    Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br></center><br>

<table border=1 align=center cellspacing=0 cellpadding=1>
    <tr bgcolor="#DBDBDB">
        <th align=center>&nbsp;DATE&nbsp;</th>
        <%
            //Spin thru userids to create header
            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
        %>
        <th align=center>&nbsp;<%=objURI.getFirstName()%><br><%=objURI.getLastName()%><br>&nbsp;(<%=objURI.getUserid()%>)&nbsp;</th>
            <%
                }
            %>
        <th align=center>&nbsp;TOTAL&nbsp;</th>
    </tr>

    <%

        //Counters
        int iDays = 0;
        int iCount = 0;
        int iCount2 = 0;
        int iResp = 0;
        int iResolved = 0;
        int iCompleted = 0;
        int iResolvedTotals = 0;
        int iCompletedTotals = 0;
        Vector vFocRej;

        //Build query string to get our stats - this is run for each DAY
        String strStatsQuery = "SELECT DH.MDFD_USERID, COUNT(*) "
                + " FROM DISPUTE_HISTORY_T DH,  USERID_T U, COMPANY_T C, DSPT_RSPNS_DETAIL_T DR "
                + " WHERE DH.STTS_CD_IN IN ('RESOLVED') AND DH.STTS_CD_IN <> DH.STTS_CD_OUT "
                + " AND DH.MDFD_USERID = U.USERID AND U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' ";
        if (bSpecificUserids) {
            strStatsQuery += " AND DH.MDFD_USERID IN (" + strUserid + ") ";
        }
        strStatsQuery += " AND DH.HSTRY_DT_IN BETWEEN TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS')   "
                + " AND TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') AND DR.DSPT_SQNC_NMBR=DH.DSPT_SQNC_NMBR AND "
                + " DR.DSPT_VRSN=DH.DSPT_VRSN AND LENGTH(DR.DSPT_ADJSTD_AMNT) > 0 GROUP BY DH.MDFD_USERID ";

        Log.write("Query=[" + strStatsQuery + "]");
        PreparedStatement pStmt = con.prepareStatement(strStatsQuery);

        int iDOW = 0;
        int iWeek = calStart.get(Calendar.WEEK_OF_YEAR);
        int iPrevWeek = iWeek;
        int iMth = calStart.get(Calendar.MONTH);
        int iPrevMth = iMth;
        boolean bWE = false;
        //Loop for each day the report should be run....
        while (calStart.before(calEnd)) {
            bWE = false;
            iDOW = calStart.get(Calendar.DAY_OF_WEEK);
            if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY) {
                bWE = true;
            }
            if (!bWE || bCountWeekends) {
                iDays++;
            }

            // Start building stats for the users
            String strTmpDate = "" + (calStart.get(Calendar.MONTH) + 1);
            if (strTmpDate.length() == 1) {
                strTmpDate = "0" + strTmpDate;
            }
            String strTemp2 = "" + calStart.get(Calendar.DAY_OF_MONTH);
            if (strTemp2.length() == 1) {
                strTemp2 = "0" + strTemp2;
            }
            strTmpDate += "/" + strTemp2;
            strTmpDate += "/" + calStart.get(Calendar.YEAR);

            pStmt.setString(1, strTmpDate + " 00:00:00");		//start date
            pStmt.setString(2, strTmpDate + " 23:59:59");		//end date (same day since we only do a day at a time
            rs = pStmt.executeQuery();
            while (rs.next()) {
                strTemp = rs.getString(1);
                iCount = rs.getInt(2);
    //			Log.write(Log.DEBUG_VERBOSE, "query results: User:"+strTemp+" count:"+iCount);
                if (m_hashUserids.containsKey(strTemp)) {
                    UserReportInfo objURI = (UserReportInfo) m_hashUserids.get(strTemp);
                    objURI.addFOCed(iCount);	//reusing FOC bucket for our totals...
                    iResolved += iCount;
                } else {
                    Log.write(Log.ERROR, "UserReport: DB synch problem userid not found = " + strTemp);
                }
            }
            rs.close();

            //-----------------------------------------------------------
            //	Spit out daily totals (if they want em)
            //-----------------------------------------------------------
            if (!bWE || bKeepWeekends) {
                strTemp = dowFmt.format(calStart.getTime());
    %>
    <tr><td align=center
            <%			if (bWE) {%>
            bgcolor="#DBD000"
            <%			}%>
            ><b>&nbsp;<%=strTemp%></b>&nbsp;</td></tr>

    <tr><td align=center>&nbsp;Resolved&nbsp;</td>
        <%
                            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
        %>
        <td align=center>&nbsp;<%=objURI.getNbrFOCed()%>&nbsp;</td>
        <%			}
        %>
        <td bgcolor="#FFFFF0" align=right>&nbsp;<%=iResolved%>&nbsp;</td></tr>
    <TR border=0><TD border=0 colspan=<%= m_vSortedUsers.size() + 2%>>&nbsp;</TD></TR>
    <%
                }	//end-if (!bKeepWeekends)

                //Reset daily counts for the userids
                iResolvedTotals += iResolved;

                iResolved = 0;
                for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                    UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                    objURI.resetCounts();
                }

                //Increment day
                calStart.add(Calendar.DATE, 1);

                //New week?
                iWeek = calStart.get(Calendar.WEEK_OF_YEAR);
                iMth = calStart.get(Calendar.MONTH);

                //-----------------------------------------------------------
                //	Pump out Weekly figures
                //-----------------------------------------------------------
                if (iWeek != iPrevWeek) {
                    iPrevWeek = iWeek;
    %>			<tr><td align=center colspan=2 bgcolor="#DBEAF5"><b>&nbsp;Weekly&nbsp;Totals</b>&nbsp;</td>
    </tr><tr>
        <td align=center>&nbsp;Resolved&nbsp;</td>
        <%
                            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                                iResolved += objURI.getWeeklyFOCed();
        %>
        <td align=center>&nbsp;<%=objURI.getWeeklyFOCed()%>&nbsp;</td>
        <%			}
        %>
        <td bgcolor="#FFFFF0" align=right>&nbsp;<%=iResolved%>&nbsp;</td></tr>
    <tr border=0><TD border=0 colspan=<%= m_vSortedUsers.size() + 2%>>&nbsp;</td></tr>
            <%
                            iResolved = 0;
                            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                                objURI.resetWeeklyCounts();
                            }
                        }

                        if (iMth != iPrevMth) {
                            iPrevMth = iMth;

            %>			<tr><td align=center colspan=2 bgcolor="#DBEAF5"><b>&nbsp;Monthly&nbsp;Totals</b>&nbsp;</td>
    </tr><tr>
        <td align=center>&nbsp;Resolved&nbsp;</td>
        <%
                            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                                iResolved += objURI.getMonthlyFOCed();
        %>
        <td align=center>&nbsp;<%=objURI.getMonthlyFOCed()%>&nbsp;</td>
        <%			}
        %>
        <td bgcolor="#FFFFF0" align=right>&nbsp;<%=iResolved%>&nbsp;</td></tr>
    <tr border=0><TD border=0 colspan=<%= m_vSortedUsers.size() + 2%>>&nbsp;</td></tr>
            <%
                        iResolved = 0;
                        for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                            UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                            objURI.resetMonthlyCounts();
                        }
                    }

                }
                Log.write(Log.DEBUG_VERBOSE, "UserReport() Days in period=" + iDays);

            %>

    <tr>
        <td bgcolor="#3366cc" align=center><b><FONT color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();" ONMOUSEOVER="showpopupmsg('Totals for each user for| the date range selected.');">
                    &nbsp;TOTALS</FONT></b></td></tr>


    <tr  bgcolor="#FFFFF0">
        <td align=center>&nbsp;Resolved&nbsp;</td>
        <%
            for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
        %>
        <td align=center>&nbsp;<%=objURI.getTotalFOCed()%>&nbsp;</td>
        <%		iResolved += objURI.getTotalFOCed();
            }
        %>
        <td align=right>&nbsp;<%=iResolved%>&nbsp;</td>
    </tr>

    <tr  bgcolor="#FFFFF0">
        <td align=center>
            <FONT STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();" ONMOUSEOVER="showpopupmsg('Total disputes | divided by nbr of days | in reporting period. The nbr | of days is in parenthesis.');">
                &nbsp;AVG*&nbsp;(<%=iDays%>)&nbsp;</FONT></td>
                <%
                    DecimalFormat Avgfmt = new DecimalFormat("0.00");
                    int iTemp = 0;
                    int iTempTot = 0;
                    for (Iterator it = m_vSortedUsers.iterator(); it.hasNext();) {
                        UserReportInfo objURI = (UserReportInfo) m_hashUserids.get((String) it.next());
                        iTemp = objURI.getTotalFOCed();
                        if (iDays == 0) {
                %>
        <td align=center>&nbsp;N/A&nbsp;</td>
        <%		} else {
        %>
        <td align=center>&nbsp;<%= Avgfmt.format((float) iTemp / iDays)%>&nbsp;</td>
        <%		}
                iTempTot += iTemp;
            }
            if (iDays == 0) {
        %>
        <td align=center>&nbsp;N/A&nbsp;</td>
        <%      } else {
        %>
        <td align=right>&nbsp;<%= Avgfmt.format((float) iTempTot / iDays)%>&nbsp;</td>
        <%      }
        %>
    </tr>
</table>

<%	if (bCountWeekends) {
%>
* Weekends are included in AVG<br>
<%	} else {
%>
* Weekends are not include in AVG<br>
<%	}
            } catch (Exception e) {
                Log.write(Log.DEBUG_VERBOSE, "Caught Exception in main block. e=[" + e + "]");
            } finally {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception ee) {
                }
                DatabaseManager.releaseConnection(con);
            }


%>

<%!
    private void dumpTotals(String strHdr, String strType) {
        Log.write(Log.DEBUG_VERBOSE, "UserReport() dumpTotals()");
    }
%>

