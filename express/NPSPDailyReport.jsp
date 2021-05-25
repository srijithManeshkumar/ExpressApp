<%
            /**
             * NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO WINDSTREAM COMMUNICATIONS
             *		INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2009
             *				BY
             *			WINDSTREAM COMMUNICATIONS INC.
             */
            /**
             * MODULE:	NPSPDailyReport.jsp
             *
             * DESCRIPTION: Daily Detail Report for the Automated Activity
             *
             * AUTHOR:      Andy Wei
             *
             * DATE:        10-20-2009
             *
             * HISTORY:
             *
             */

%>

<%@ include file="i_header.jsp" %>
<%            String path = request.getContextPath();
%>
<script type='text/javascript' src='<%=path%>/jquery.js'></script>
<script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
<%
            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }

            Connection con = null;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid from date");
%>
<jsp:forward page="NPSPDateSelect.jsp"/>;
<%
                return;
            }
            String strStartDate = strStartYr + strStartMth + strStartDay;

            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            String strEndDay = alltelRequest.getParameter("to_due_date_dy");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0) || (strEndDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="NPSPDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="NPSPDateSelect.jsp"/>;
<%
                return;
            }

            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

            String DATE_FORMAT1 = "MM-dd-yyyy";

            String DATE_FORMAT2 = "yyyyMMdd";

            DateFormat sdf1 = new SimpleDateFormat(DATE_FORMAT1);

            DateFormat sdf2 = new SimpleDateFormat(DATE_FORMAT2);

            Log.write(Log.DEBUG_VERBOSE, "DailyReport: Date:" + strStartDate + " Date:" + strEndDate);

            con = DatabaseManager.getConnection();
            Statement stmt = null;
            Statement stmt1 = null;
            ResultSet rs = null;
            ResultSet rs1 = null;
            stmt = con.createStatement();
            stmt1 = con.createStatement();
            Statement stmt2 = null;
            Statement stmt3 = null;
            Statement stmt4 = null;
            Statement stmt5 = null;
            ResultSet rs2 = null;
            ResultSet rs3 = null;
            ResultSet rs4 = null;
            ResultSet rs5 = null;
            stmt2 = con.createStatement();
            stmt3 = con.createStatement();
            stmt4 = con.createStatement();
            stmt5 = con.createStatement();

            boolean bSpecificState = false;
            boolean bSpecificCustTyp = false;
            boolean bSpecificBroadband = false;
            boolean bSpecificVideo = false;
            boolean bSpecificGreenfield = false;
            boolean bSpecificCLEC = false;
            boolean bSpecificPartial = false;
            boolean bSpecificCircuit = false;
            boolean bSpecificCHC = false;

            String strState = "";
            String strStateWhere = "";
            String[] strStates = alltelRequest.getAttributeValue("STATE_CD");
            if (strStates != null) {
                for (int i = 0; i < strStates.length; i++) {
                    if (strStates[i].equals("ALL")) {
                        strState = "ALL";
                        break;
                    } else {
                        bSpecificState = true;
                        if (strState.length() > 0) {
                            strState += ",";
                        }
                        strState += "'" + strStates[i] + "'";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strState=[" + strState + "]");
            if (bSpecificState) {
                strStateWhere = " AND t2.OCN_STT IN (" + strState + ") ";
            }

            String strCustTypWhere = "";
            String strCustTyp = alltelRequest.getParameter("CUST_TYP");
            if (strCustTyp.equals("ALL")) {
                strCustTyp = "ALL";
            } else {
                bSpecificCustTyp = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strCustTyp=[" + strCustTyp + "]");
            if (bSpecificCustTyp) {
                strCustTypWhere = " AND (t2.CUS_TYP = '" + strCustTyp + "' or t2.CUS_TYP is null or t2.CUS_TYP = '') ";
            }

            String strBroadbandWhere = "";
            String strBroadband = alltelRequest.getParameter("BB");
            if (strBroadband.equals("ALL")) {
                strBroadband = "ALL";
            } else {
                bSpecificBroadband = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strBroadband=[" + strBroadband + "]");
            if (bSpecificBroadband) {
                strBroadbandWhere = " AND t8.CI_BB = '" + strBroadband + "' ";
            }

            String strVideoWhere = "";
            String strVideo = alltelRequest.getParameter("VI");
            if (strVideo.equals("ALL")) {
                strVideo = "ALL";
            } else {
                bSpecificVideo = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strVideo=[" + strVideo + "]");
            if (bSpecificVideo) {
                strVideoWhere = " AND t8.CI_VIDEO = '" + strVideo + "' ";
            }

            String strGreenfieldWhere = "";
            String strGreenfield = alltelRequest.getParameter("GF");
            if (strGreenfield.equals("ALL")) {
                strGreenfield = "ALL";
            } else {
                bSpecificGreenfield = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strGreenfield=[" + strGreenfield + "]");
            if (bSpecificGreenfield) {
                strGreenfieldWhere = " AND t8.CI_GF = '" + strGreenfield + "' ";
            }

            String strCLECWhere = "";
            String strCLECV = alltelRequest.getParameter("CLEC");
            if (strCLECV.equals("ALL")) {
                strCLECV = "ALL";
            } else {
                bSpecificCLEC = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strCLECV=[" + strCLECV + "]");
            if (bSpecificCLEC) {
                strCLECWhere = " AND t8.CI_CLEC = '" + strCLECV + "' ";
            }

            String strPartialWhere = "";
            String strPartial = alltelRequest.getParameter("PL");
            if (strPartial.equals("ALL")) {
                strPartial = "ALL";
            } else {
                bSpecificPartial = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strPartial=[" + strPartial + "]");
            if (bSpecificPartial) {
                strPartialWhere = " AND t8.CI_PARTIAL = '" + strPartial + "' ";
            }

            String strCircuitWhere = "";
            String strCircuit = alltelRequest.getParameter("CT");
            if (strCircuit.equals("ALL")) {
                strCircuit = "ALL";
            } else {
                bSpecificCircuit = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strCircuit=[" + strCircuit + "]");
            if (bSpecificCircuit) {
                strCircuitWhere = " AND t8.CI_CIRCUIT = '" + strCircuit + "' ";
            }

            String strCHCWhere = "";
            String strCHCV = alltelRequest.getParameter("CHC");
            if (strCHCV.equals("ALL")) {
                strCHCV = "ALL";
            } else {
                strCHCV = "Y";
                bSpecificCHC = true;
            }
            Log.write(Log.DEBUG_VERBOSE, "strCHCV=[" + strCHCV + "]");

            Log.write(Log.DEBUG_VERBOSE, "This is the report that is running");

            if (bSpecificCHC) {
                strCHCWhere = " AND t8.CI_CHC = '" + strCHCV + "' ";
            }

            Log.write(Log.DEBUG_VERBOSE, "This before form table");
%>

<table id="NPSPRTable" class="tablesorter" width=2100 border=1 align=center cellspacing=0 cellpadding=0>

    <br><center>
        <SPAN CLASS="header1">N&nbsp;P&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a&nbsp;n&nbsp;d&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;P&nbsp;S&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;I&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
        <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
        Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br><br>

        <thead>
        <th align=center width=50 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Bus/Res</font></th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		CLEC</font></th>

        <th align=center width=270 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Customer Name</font></th>

        <th align=center width=400 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Customer Address</font></th>

        <th align=center width=120 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		City</font></th>

        <th align=center width=10 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		State</font></th>

        <th align=center width=50 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Zip</font></th>

        <th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Porting</font></th>

        <th align=center width=50 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Disconnecting</th>

        <th align=center width=150 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Staying</th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Broadband</th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Video</th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Greenfield</th>

        <th align=center width=150 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Order numbers</th>

        <th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		PON</th>

        <th align=center width=60 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOCD</th>

        <th align=center width=60 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SRD</th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		DFDT</th>

        <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		CHC</th>

        <th align=center width=200 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Remarks</th>

        <th align=center width=200 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
                NP Remarks</th>
        
        <th align=center width=200 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Updates</th>

        </thead><tbody>

            <%

                        Log.write(Log.DEBUG_VERBOSE, "This before sql string");

                        try {
                            String strQry = "SELECT t2.cus_typ AS CusTyp, t8.ci_clec AS CLEC, t3.eu_la_name AS CusName, t3.eu_la_sano AS SANO, "
                                    + "t3.eu_la_sasn AS SASN, t3.eu_la_sath AS SATH, t3.eu_la_sass AS SASS, t3.eu_la_ld1 AS LD1, t3.eu_la_lv1 AS LV1, "
                                    + "t3.eu_la_city AS CITY, t2.ocn_stt AS ST, t3.eu_la_zip AS ZIP, t8.ci_stay AS STAY, t8.ci_bb AS BB, "
                                    + "t8.ci_video AS VI, t8.ci_gf AS GF, t6.lr_ord AS ORD, t2.rqst_pon AS PON, t6.lr_dd AS FOCDt, "
                                    + "to_char(to_date(t6.lr_dd,'mm/dd/yyyy') + 1,'yyyymmdd') AS SRD, "
                                    + "t8.ci_dfdt AS DFDT, decode(t8.ci_chc,'Y','Yes') AS CHC, t8.ci_remarks AS REMARKS, t9.np_remarks as NP_REMARKS, "
                                    + "t1.rqst_sqnc_nmbr AS RQST, t1.rqst_vrsn AS VRSN, t2.srvc_typ_cd as SVCTYPE ,t1.rqst_stts_cd_out as STTSCD,t7.LSR_SUP as SUPVALUE "
                                    + "FROM request_history_t t1, request_t t2, eu_la_t t3, lr_t t6, lsr_t t7, ci_t t8, np_t t9 "
                                    + "WHERE t1.rqst_stts_cd_out ='FOC'  "
                                    + "AND t1.rqst_hstry_dt_out BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') "
                                    + "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') "
                                    + "AND t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t2.rqst_vrsn "
                                    + "AND t2.srvc_typ_cd in ('C') AND t2.ICARE = 'N' "
                                    + "AND t1.rqst_sqnc_nmbr = t3.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t3.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t6.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t6.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t7.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t7.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t8.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t8.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t9.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t9.rqst_vrsn "
                                    + strStateWhere
                                    + strCustTypWhere
                                    + strBroadbandWhere
                                    + strVideoWhere
                                    + strGreenfieldWhere
                                    + strCLECWhere
                                    + strPartialWhere
                                    + strCircuitWhere
                                    + strCHCWhere
                                    + " UNION "
                                    + "SELECT t2.cus_typ AS CusTyp, t8.ci_clec AS CLEC, null AS CusName, null AS SANO, "
                                    + "' ' AS SASN, ' ' AS SATH, ' ' AS SASS, ' ' AS LD1, ' ' AS LV1, "
                                    + "null AS CITY, t2.ocn_stt AS ST, to_char(t3.sp_zip) AS ZIP, t8.ci_stay AS STAY, t8.ci_bb AS BB, "
                                    + "t8.ci_video AS VI, t8.ci_gf AS GF, t4.lr_ord AS ORD, t2.rqst_pon AS PON, t4.lr_sp_ddt AS FOCDt, "
                                    + "to_char(to_date(t4.lr_sp_ddt,'mm/dd/yyyy') + 1,'yyyymmdd') AS SRD, "
                                    + "t8.ci_dfdt AS DFDT, decode(t8.ci_chc,'Y','Yes') AS CHC, t8.ci_remarks AS REMARKS, t9.np_remarks as NP_REMARKS, "
                                    + "t1.rqst_sqnc_nmbr AS RQST, t1.rqst_vrsn AS VRSN, t2.srvc_typ_cd as SVCTYPE ,t1.rqst_stts_cd_out as STTSCD,null as SUPVALUE "
                                    + "FROM request_history_t t1, request_t t2, sp_t t3, lr_t t4, ci_t t8, np_t t9 "
                                    + "WHERE t1.rqst_stts_cd_out ='FOC' "
                                    + "AND t1.rqst_hstry_dt_out BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') "
                                    + "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') "
                                    + "AND t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t2.rqst_vrsn "
                                    + "AND t2.srvc_typ_cd in ('S') AND t2.ICARE = 'N' "
                                    + "AND t1.rqst_sqnc_nmbr = t3.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t3.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t4.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t4.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t8.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t8.rqst_vrsn "
                                    + "AND t1.rqst_sqnc_nmbr = t9.rqst_sqnc_nmbr "
                                    + "AND t1.rqst_vrsn = t9.rqst_vrsn "
                                    + strStateWhere
                                    + strCustTypWhere
                                    + strBroadbandWhere
                                    + strVideoWhere
                                    + strGreenfieldWhere
                                    + strCLECWhere
                                    + strPartialWhere
                                    + strCircuitWhere
                                    + strCHCWhere
                                    + "order by CusName ";

                            Log.write(Log.DEBUG_VERBOSE, "SQL to be executed: " + strQry);
                            rs = stmt.executeQuery(strQry);
                            Log.write(Log.DEBUG_VERBOSE, "SQL exectued");

                            String strCusTyp = "";
                            String strCLEC = "";
                            String strCusName = "";
                            String strSANO = "";
                            String strSASN = "";
                            String strSATH = "";
                            String strSASS = "";
                            String strLD1 = "";
                            String strLV1 = "";
                            String strCusAddr = "";
                            String strCITY = "";
                            String strST = "";
                            String strZIP = "";
                            String strStaying = "";
                            String strBB = "";
                            String strVI = "";
                            String strGF = "";
                            String strOrderNum = "";
                            String strPON = "";
                            String strFOCDate = "";
                            String strSRD = "";
                            String strDFDT = "";
                            String strCHC = "";
                            String strRemarks = "";
                            String strNPRemarks = "";
                            String strRQST = "";
                            String strVRSN = "";
                            int iRqst = 0;
                            int iVrsn = 0;
                            String strYr = "";
                            String strMth = "";
                            String strDay = "";
                            String strSvcType = "";
                            String supValue = "";
                            String sttsCd = "";
                            String strUpdates = "";

                            Calendar cal = Calendar.getInstance();

                            while (rs.next()) {
                               strSvcType = rs.getString("SVCTYPE");
                               strCusTyp = rs.getString("CusTyp");
								sttsCd=rs.getString("STTSCD");
				    supValue=rs.getString("SUPVALUE"); 	
                                if (sttsCd != null && supValue != null) {
                                    if (sttsCd.equals("FOC") && (supValue.equals("1"))) {
                                        strUpdates = "Cancel";
                                    } else if (sttsCd.equals("FOC") && (supValue.equals("2"))) {
                                        strUpdates = "Due Date Change";
                                    } else if (sttsCd.equals("FOC") && (supValue.equals("3"))) {
                                        strUpdates = "Misc Change";
                                    } else if (!sttsCd.equals("FOC")) {
                                        strUpdates = "&nbsp";
                                    }
                                }
                                if (sttsCd != null && supValue == null) {
                                    strUpdates = "&nbsp";
                                }
                                if (strCusTyp == null) {
                                    strCusTyp = "&nbsp";
                                }
                                strCLEC = rs.getString("CLEC");
                                if (strCLEC == null) {
                                    strCLEC = "&nbsp";
                                }
                                strCusName = rs.getString("CusName");
                                if (strCusName == null) {
                                    strCusName = "&nbsp";
                                }
                                strSANO = rs.getString("SANO");
                                strSASN = rs.getString("SASN");
                                strSATH = rs.getString("SATH");
                                strSASS = rs.getString("SASS");
                                strLD1 = rs.getString("LD1");
                                strLV1 = rs.getString("LV1");
                                if (strSANO != null) {
                                    strCusAddr += strSANO + " ";
                                }
                                if (strSASN != null) {
                                    strCusAddr += strSASN + " ";
                                }
                                if (strSATH != null) {
                                    strCusAddr += strSATH + " ";
                                }
                                if (strSASS != null) {
                                    strCusAddr += strSASS + " ";
                                }
                                if (strLD1 != null) {
                                    strCusAddr += strLD1 + " ";
                                }
                                if (strLV1 != null) {
                                    strCusAddr += strLV1 + " ";
                                }
                                strCITY = rs.getString("CITY");
                                if (strCITY == null) {
                                    strCITY = "&nbsp";
                                }
                                strST = rs.getString("ST");
                                strZIP = rs.getString("ZIP");
                                strStaying = rs.getString("STAY");
                                strBB = rs.getString("BB");
                                if (strBB == null) {
                                    strBB = "&nbsp";
                                }
                                strVI = rs.getString("VI");
                                if (strVI == null) {
                                    strVI = "&nbsp";
                                }
                                strGF = rs.getString("GF");
                                if (strGF == null) {
                                    strGF = "&nbsp";
                                }
                                strOrderNum = rs.getString("ORD");
                                if (strOrderNum == null) {
                                    strOrderNum = "&nbsp";
                                }
                                strPON = rs.getString("PON");
                                strFOCDate = rs.getString("FOCDt");
                                if (strFOCDate == null) {
                                    strFOCDate = "&nbsp";
                                }
                                strSRD = rs.getString("SRD");
                                strDFDT = rs.getString("DFDT");
                                if (strDFDT == null) {
                                    strDFDT = "&nbsp";
                                }
                                strCHC = rs.getString("CHC");
                                if (strCHC == null) {
                                    strCHC = "&nbsp";
                                }
                                strRemarks = rs.getString("REMARKS");
                                if (strRemarks == null) {
                                    strRemarks = "&nbsp";
                                }
                                strNPRemarks = rs.getString("NP_REMARKS");
                                if (strNPRemarks == null) {
                                    strNPRemarks = "&nbsp";
                                }
                                strRQST = rs.getString("RQST");
                                strVRSN = rs.getString("VRSN");
                                iRqst = Integer.parseInt(strRQST);
                                iVrsn = Integer.parseInt(strVRSN);
                                if (strSRD == null) {
                                    strSRD = "&nbsp";
                                } else {
                                    strYr = strSRD.substring(0, 4);
                                    Log.write(Log.DEBUG_VERBOSE, "strYr=[" + strYr + "]");
                                    strMth = strSRD.substring(4, 6);
                                    Log.write(Log.DEBUG_VERBOSE, "strMth=[" + strMth + "]");
                                    strDay = strSRD.substring(6);
                                    Log.write(Log.DEBUG_VERBOSE, "strDay=[" + strDay + "]");

                                    cal.set(Integer.parseInt(strYr), Integer.parseInt(strMth) - 1, Integer.parseInt(strDay), 0, 0, 0);

                                    String strCt4 = "";
                                    int iCt4 = 0;

                                    String strQry4 = "select count(hldy_dt) from holiday_t where to_char(hldy_dt,'yyyymmdd') = " + strSRD;

                                    rs4 = stmt4.executeQuery(strQry4);

                                    while (rs4.next()) {
                                        strCt4 = rs4.getString(1);
                                        iCt4 = Integer.parseInt(strCt4);
                                    }

                                    if (iCt4 == 1) {
                                        cal.add(Calendar.DATE, 1);

                                        int iDays = cal.get(Calendar.DAY_OF_WEEK);
                                        Log.write(Log.DEBUG_VERBOSE, "iDays=[" + iDays + "]");

                                        if (iDays == 7) {
                                            cal.add(Calendar.DATE, 2);
                                            strSRD = sdf1.format(cal.getTime());
                                        } else {
                                            strSRD = sdf1.format(cal.getTime());
                                        }
                                    } else {

                                        int iDays = cal.get(Calendar.DAY_OF_WEEK);
                                        Log.write(Log.DEBUG_VERBOSE, "iDays=[" + iDays + "]");

                                        if (iDays == 7) {
                                            cal.add(Calendar.DATE, 2);
                                        }

                                        String strCt5 = "";
                                        int iCt5 = 0;

                                        strSRD = sdf2.format(cal.getTime());

                                        String strQry5 = "select count(hldy_dt) from holiday_t where to_char(hldy_dt,'yyyymmdd') = " + strSRD;

                                        rs5 = stmt5.executeQuery(strQry5);

                                        while (rs5.next()) {
                                            strCt5 = rs5.getString(1);
                                            iCt5 = Integer.parseInt(strCt5);
                                        }

                                        if (iCt5 == 1) {
                                            cal.add(Calendar.DATE, 1);
                                            strSRD = sdf1.format(cal.getTime());
                                        } else {
                                            strSRD = sdf1.format(cal.getTime());
                                        }
                                    }
                                }
                                String strCt2 = "";
                                String strCt3 = "";
                                int iCt2 = 0;
                                int iCt3 = 0;

                                String strQry2 = "select count(np_sd_portednbr) from np_sd_t where rqst_sqnc_nmbr = " + iRqst + " and rqst_vrsn = " + iVrsn;

                                rs2 = stmt2.executeQuery(strQry2);

                                while (rs2.next()) {
                                    strCt2 = rs2.getString(1);
                                    iCt2 = Integer.parseInt(strCt2);
                                }
                                String strQry3 = "select count(eu_dd_discnbr) from eu_dd_t where rqst_sqnc_nmbr = " + iRqst + " and rqst_vrsn = " + iVrsn;

                                rs3 = stmt3.executeQuery(strQry3);

                                while (rs3.next()) {
                                    strCt3 = rs3.getString(1);
                                    iCt3 = Integer.parseInt(strCt3);
                                }

                                Log.write(Log.DEBUG_VERBOSE, "NPSPDailyReport: strCt2:" + strCt2 + " strCt3:" + strCt3);
                                String strQry1 = "";
                                Log.write(Log.DEBUG_VERBOSE, "!!!!!!!!!!!! strSvcType: " + strSvcType);
                                if ((iCt2 > iCt3) || (strSvcType.equals("S"))) {
                                    if (strSvcType.equals("S")) {
                                        strQry1 = "select sp_ptn, null from sp_t t1 where  t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
                                    } else {
                                        strQry1 = "select t1.np_sd_portednbr, t2.eu_dd_discnbr from np_sd_t t1 left outer join eu_dd_t t2 on (t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr and t1.rqst_vrsn = t2.rqst_vrsn and t1.frm_sctn_occ = t2.frm_sctn_occ) "
                                                + "where t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
                                    }

                                    Log.write(Log.DEBUG_VERBOSE, "sql  1" + strQry1);
                                    rs1 = stmt1.executeQuery(strQry1);

                                    String strPorting = "";
                                    String strDisconn = "";

                                    while (rs1.next()) {
                                        strPorting = rs1.getString(1);
                                        if (strPorting == null) {
                                            strPorting = "&nbsp";
                                        }
                                        strDisconn = rs1.getString(2);
                                        if (strDisconn == null) {
                                            strDisconn = "&nbsp";
                                        }
            %>
            <tr>
                <td align=left><%=strCusTyp%></td>
                <td align=left><%=strCLEC%></td>
                <td align=left><%=strCusName%></td>
                <td align=left><%=strCusAddr%></td>
                <td align=left><%=strCITY%></td>
                <td align=left><%=strST%></td>
                <td align=left><%=strZIP%></td>
                <td align=left><%=strPorting%></td>
                <td align=left><%=strDisconn%></td>
                <%
                                    if (strStaying == null || strStaying == "&nbsp") {
                                        strStaying = "&nbsp";
                %>
                <td align=left><%=strStaying%></td>
                <%
                                } else {
                %>
                <td>
                    <textarea type="text" rows="2" cols="30" readonly="readonly"><%=strStaying%></textarea>
                </td>
                <%
                                        strStaying = "&nbsp";
                                    }
                %>
                <td align=left><%=strBB%></td>
                <td align=left><%=strVI%></td>
                <td align=left><%=strGF%></td>
                <td align=left><%=strOrderNum%></td>
                <td align=left><%=strPON%></td>
                <td align=left><%=strFOCDate%></td>
                <td align=left><%=strSRD%></td>
                <td align=left><%=strDFDT%></td>
                <td align=left><%=strCHC%></td>
                <td align=left><%=strRemarks%></td>
            	<td align=left><%=strNPRemarks%></td>                
                <td align=left><%=strUpdates%></td>

            </tr>
            <%
                                    strCusTyp = "&nbsp";
                                    strCLEC = "&nbsp";
                                    strCusName = "&nbsp";
                                    strCusAddr = "&nbsp";
                                    strCITY = "&nbsp";
                                    strST = "&nbsp";
                                    strZIP = "&nbsp";
                                    strBB = "&nbsp";
                                    strVI = "&nbsp";
                                    strGF = "&nbsp";
                                    strOrderNum = "&nbsp";
                                    strPON = "&nbsp";
                                    strFOCDate = "&nbsp";
                                    strSRD = "&nbsp";
                                    strDFDT = "&nbsp";
                                    strCHC = "&nbsp";
                                    strRemarks = "&nbsp";
                                    strNPRemarks = "&nbsp";
                                }
                            } else {
                                Log.write(Log.DEBUG_VERBOSE, "!!!!!!!!!!!! strSvcType 2nd IF: " + strSvcType);
                                if (strSvcType.equals("S")) {
                                    strQry1 = "select '', sp_ptn from sp_t t1 where  t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
                                } else {
                                    strQry1 = "select t1.eu_dd_discnbr, t2.np_sd_portednbr from eu_dd_t t1 left outer join np_sd_t t2 on (t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr and t1.rqst_vrsn = t2.rqst_vrsn and t1.frm_sctn_occ = t2.frm_sctn_occ) "
                                            + "where t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
                                }
                                Log.write(Log.DEBUG_VERBOSE, "2nd IF sql  1" + strQry1);
                                rs1 = stmt1.executeQuery(strQry1);

                                String strPorting = "";
                                String strDisconn = "";

                                while (rs1.next()) {
                                    strPorting = rs1.getString(2);
                                    if (strPorting == null) {
                                        strPorting = "&nbsp";
                                    }
                                    strDisconn = rs1.getString(1);
                                    if (strDisconn == null) {
                                        strDisconn = "&nbsp";
                                    }
            %>
            <tr>
                <td align=left><%=strCusTyp%></td>
                <td align=left><%=strCLEC%></td>
                <td align=left><%=strCusName%></td>
                <td align=left><%=strCusAddr%></td>
                <td align=left><%=strCITY%></td>
                <td align=left><%=strST%></td>
                <td align=left><%=strZIP%></td>
                <td align=left><%=strPorting%></td>
                <td align=left><%=strDisconn%></td>
                <%
                                    if (strStaying == null || strStaying == "&nbsp") {
                                        strStaying = "&nbsp";
                %>
                <td align=left><%=strStaying%></td>
                <%
                                } else {
                %>
                <td>
                    <textarea type="text" rows="2" cols="30" readonly="readonly"><%=strStaying%></textarea>
                </td>
                <%
                                        strStaying = "&nbsp";
                                    }
                %>
                <td align=left><%=strBB%></td>
                <td align=left><%=strVI%></td>
                <td align=left><%=strGF%></td>
                <td align=left><%=strOrderNum%></td>
                <td align=left><%=strPON%></td>
                <td align=left><%=strFOCDate%></td>
                <td align=left><%=strSRD%></td>
                <td align=left><%=strDFDT%></td>
                <td align=left><%=strCHC%></td>
                <td align=left><%=strRemarks%></td>
            	<td align=left><%=strNPRemarks%></td>                
                <td align=left><%=strUpdates%></td>
            </tr>
            <%
                                        strCusTyp = "&nbsp";
                                        strCLEC = "&nbsp";
                                        strCusName = "&nbsp";
                                        strCusAddr = "&nbsp";
                                        strCITY = "&nbsp";
                                        strST = "&nbsp";
                                        strZIP = "&nbsp";
                                        strBB = "&nbsp";
                                        strVI = "&nbsp";
                                        strGF = "&nbsp";
                                        strOrderNum = "&nbsp";
                                        strPON = "&nbsp";
                                        strFOCDate = "&nbsp";
                                        strSRD = "&nbsp";
                                        strDFDT = "&nbsp";
                                        strCHC = "&nbsp";
                                        strRemarks = "&nbsp";
                                        strNPRemarks = "&nbsp";
                                    }
                                }
                            }

                            rs.close();
                            rs = null;
                            rs1.close();
                            rs1 = null;
                            rs2.close();
                            rs2 = null;
                            rs3.close();
                            rs3 = null;
                            rs4.close();
                            rs4 = null;
                            rs5.close();
                            rs5 = null;
                        } // try
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.write(Log.DEBUG_VERBOSE, e.toString());
                        } finally {
                            DatabaseManager.releaseConnection(con);
                        }
            %>
        </tbody></table>
</UL>
<BR>
<BR>
<center><a href=javascript:this.history.back()>GO BACK</a></center>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#NPSPRTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>




