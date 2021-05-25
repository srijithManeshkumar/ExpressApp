<%-- 
    Document   : RejDetExcelRpt
    Created on : May 20, 2011, 2:53:28 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.RejectionDetailBean,java.util.*"  session="true"%>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "RejectionDetailReport" + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

%>

<table  border=1>
    <tr bgcolor=##3366cc>
        <td nowrap>PON</td>
        <td nowrap>TN</td>
        <td nowrap>VendorName</td>
        <td nowrap>OCN</td>
        <td nowrap>State</td>
        <td nowrap>Internal Rejection Reason Code</td>
        <td nowrap>Reason (RDET)</td>
        <td nowrap>CI Drop Down Reason</td>
        <td nowrap>Submitted Date</td>
        <td nowrap>Rejection Date</td>
    </tr>
    <%
                StringBuffer sb = new StringBuffer(256);
                List rejDetailList = (ArrayList) session.getAttribute("detailList");
                for (int i = 0; i < rejDetailList.size(); i++) {
                    RejectionDetailBean rejectionDetailBean = (RejectionDetailBean) rejDetailList.get(i);
                    sb.append("<tr>");
                    sb.append("<td>&nbsp;" + rejectionDetailBean.getPon() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getTn() + "</td>\n");
                    sb.append("<td>&nbsp;" + rejectionDetailBean.getVendorname() + "</td>\n");
                    sb.append("<td>&nbsp;" + rejectionDetailBean.getOcn() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getState() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getRejreasoncode() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getRejdet() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getCireason() + "</td>\n");
                    sb.append("<td nowrap>&nbsp;" + rejectionDetailBean.getSubmitteddate() + "</td>\n");
                    sb.append("<td>&nbsp;" + rejectionDetailBean.getRejectiondate() + "</td>\n");
                    sb.append("</tr>");
                }

    %>
    <%=sb.toString()%>
    