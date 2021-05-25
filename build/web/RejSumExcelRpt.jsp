<%-- 
    Document   : RejSumExcelRpt
    Created on : May 20, 2011, 2:19:02 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.RejectionBean,java.util.*"  session="true"%>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "RejectionSummaryReport" + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

%>

<table  border=1>
     <tr bgcolor=##3366cc>
        <td nowrap>Internal Rejection Code</td>
        <td nowrap>Total</td>
        <td >%</td>
    </tr>
    <%
                StringBuffer sb = new StringBuffer(128);
                RejectionBean totalRejSummaryBean = (RejectionBean) session.getAttribute("total");
                List rejSummaryList = (ArrayList) session.getAttribute("sumList");
                for (int i = 0; i < rejSummaryList.size(); i++) {
                    RejectionBean rejectionBean = (RejectionBean) rejSummaryList.get(i);
                    sb.append("<tr>");
                    sb.append("<td>&nbsp;" + rejectionBean.getRejDesc() + "</td>\n");
                    sb.append("<td>&nbsp;" + rejectionBean.getRejcount() + "</td>\n");
                    sb.append("<td>&nbsp;" + (int)rejectionBean.getRejPer() + "</td>\n");
                    sb.append("</tr>");
                }
                sb.append("<tr>");
                sb.append("<td>&nbsp;Total</td>\n");
                sb.append("<td>&nbsp;" + totalRejSummaryBean.getRejTot() + "</td>\n");
                sb.append("<td>&nbsp;" + (int)totalRejSummaryBean.getRejPerTot() + "</td>\n");
                sb.append("</tr>");


    %>
    <%=sb.toString()%>
    