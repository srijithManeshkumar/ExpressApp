<%--
    Document   : AutoSumExcelRpt
    Created on : Apr 12, 2011, 4:44:59 PM
    Author     : satish.t
--%>
<%@page  import="com.automation.reports.bean.AutoSummaryBean,java.util.*"  session="true"%>

<%
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-Disposition","attachment; filename=" + "AutoSummaryReport"+".xls");
response.setHeader("Pragma", "public");
response.setHeader("Cache-Control", "max-age=0");

%>

<table  border=1>
    <tr bgcolor=##3366cc>
	<td nowrap>Day</td>
	<td nowrap>Manual Review</td>
	<td nowrap>Manual Review %</td>
	<td nowrap>Rejected, Auto</td>
	<td nowrap>Rejected, Manual</td>
	<td nowrap>Rejected</td>
	<td nowrap>Rejected  %</td>
	<td nowrap>FOC, Auto</td>
	<td nowrap>FOC, Manual</td>
	<td nowrap>FOC %</td>
	<td nowrap>FOC %</td>
	<td nowrap>Simple</td>
	<td nowrap>Non-Simple</td>
	<td nowrap>Avg Response Time, Auto</td>
	<td nowrap>Avg Response Time, Man</td>
	<td nowrap>Avg Response Time</td>
	<td nowrap>Total</td>

</tr>
<%
        StringBuffer sb= new StringBuffer( 256 );
      	AutoSummaryBean totalautoSummaryBean=(AutoSummaryBean)session.getAttribute("totals");
        List autoSummaryList=(ArrayList)session.getAttribute("TableData");
	for(int i=0;i<autoSummaryList.size();i++){
		AutoSummaryBean autoSummaryBean=(AutoSummaryBean)autoSummaryList.get(i);
			sb.append( "<tr>");
                     sb.append( "<td>&nbsp;" + autoSummaryBean.getDate()  + "</td>\n");
			sb.append( "<td>&nbsp;" + autoSummaryBean.getManRev()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getPerManrev() + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getRejAuto()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getRejMan()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getRej() + "</td>\n");
                     sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getPerRej()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getFocAuto()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getFocMan()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getFoc()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoSummaryBean.getPerFoc()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getSimple()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getComplex()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getAvgRespAuto()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getAvgRespMan()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getAvgResp()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoSummaryBean.getHorTot()  + "</td>\n");
			sb.append( "</tr>");
	}
                     sb.append( "<tr>");
                     sb.append( "<td>&nbsp;Total</td>\n");
			sb.append( "<td>&nbsp;" + totalautoSummaryBean.getManRevTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getPerManrevTot() + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getRejAutoTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getRejManTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getRejTot() + "</td>\n");
                     sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getPerRejTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getFocAutoTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getFocManTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getFocTot()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  totalautoSummaryBean.getPerFocTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getSimpleTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getComplexTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getAvgRespAutoTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getAvgRespManTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getAvgRespTot()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  totalautoSummaryBean.getVerTot()  + "</td>\n");
			sb.append( "</tr>");


%>
<%=sb.toString()%>
