<%-- 
    Document   : AutoDetExcelRpt
    Created on : May 20, 2011, 12:25:52 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.AutoDetailBean,java.util.*"  session="true"%>

<%
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-Disposition","attachment; filename=" + "AutoActivityDetail"+".xls");
response.setHeader("Pragma", "public");
response.setHeader("Cache-Control", "max-age=0");

%>

<table  border=1>
    <tr bgcolor=##3366cc>
	<td nowrap>PON</td>
	<td nowrap>OCN</td>
	<td nowrap>Company</td>
	<td nowrap>External Status</td>
	<td nowrap>Version</td>
	<td nowrap>State</td>
	<td nowrap>TN</td>
	<td nowrap>Service Type</td>
	<td nowrap>Activity Type</td>
	<td nowrap>Submitted Date</td>
	<td nowrap>FOC/Reject/Manual Review Date</td>
	<td nowrap>Automation Status</td>
	<td nowrap>Simple/Non-Simple</td>
	<td nowrap>Actual Response Time</td>

</tr>
<%
        StringBuffer sb= new StringBuffer( 256 );
        List autoDetailList=(ArrayList)session.getAttribute("detailList");
	for(int i=0;i<autoDetailList.size();i++){
		AutoDetailBean autoDetailBean=(AutoDetailBean)autoDetailList.get(i);
			sb.append( "<tr>");
                        sb.append( "<td>&nbsp;" + autoDetailBean.getPon()  + "</td>\n");
			sb.append( "<td>&nbsp;" + autoDetailBean.getOcn()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoDetailBean.getCompany() + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoDetailBean.getExternalstatus()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getVersion()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getState() + "</td>\n");
                        sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getTn()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getServicetype()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getActivitytype()  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getSubmitteddate() + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  autoDetailBean.getFocrejMandate()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoDetailBean.getAutoStatus()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoDetailBean.getSimpleflag()  + "</td>\n");
			sb.append( "<td>&nbsp;" +  autoDetailBean.getActResponseTime()  + "</td>\n");
			sb.append( "</tr>");
	}

%>
<%=sb.toString()%>
