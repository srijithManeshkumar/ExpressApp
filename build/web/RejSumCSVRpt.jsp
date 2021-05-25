<%-- 
    Document   : RejSumCSVRpt
    Created on : May 20, 2011, 5:09:18 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.RejectionBean,java.util.*"  session="true"%>

<%
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + "RejectionSummaryReport" + ".csv");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

            out.write("Internal Rejection Code");
            out.write(',');
            out.write("Total");
            out.write(',');
            out.write("%");
            out.write("\n");

            RejectionBean totalRejSummaryBean = (RejectionBean) session.getAttribute("total");
            List rejSummaryList = (ArrayList) session.getAttribute("sumList");
            for (int i = 0; i < rejSummaryList.size(); i++) {
                RejectionBean rejectionBean = (RejectionBean) rejSummaryList.get(i);

                out.write(rejectionBean.getRejDesc());
                out.write(',');
                out.write(String.valueOf(rejectionBean.getRejcount()));
                out.write(',');
                out.write(String.valueOf((int) rejectionBean.getRejPer()));
                out.write("\n");
            }

            out.write("Total");
            out.write(',');
            out.write(String.valueOf(totalRejSummaryBean.getRejTot()));
            out.write(',');
            out.write(String.valueOf((int) totalRejSummaryBean.getRejPerTot()));

            out.close();
%>

