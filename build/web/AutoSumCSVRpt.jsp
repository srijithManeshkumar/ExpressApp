<%--
    Document   : AutoSumCSVRpt
    Created on : May 20, 2011, 3:39:01 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.AutoSummaryBean,java.util.*,java.io.FileWriter"  session="true"%>

<%
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + "AutoSummaryReport" + ".csv");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            out.write("Day");
            out.write(',');
            out.write("Manual Review");
            out.write(',');
            out.write("Manual Review %");
            out.write(',');
            out.write("Rejected Auto");
            out.write(',');
            out.write("Rejected Manual");
            out.write(',');
            out.write("Rejected");
            out.write(',');
            out.write("Rejected  %");
            out.write(',');
            out.write("FOC Auto");
            out.write(',');
            out.write("FOC Manual");
            out.write(',');
            out.write("FOC");
            out.write(',');
            out.write("FOC %");
            out.write(',');
            out.write("Simple");
            out.write(',');
            out.write("Non-Simple");
            out.write(',');
            out.write("Avg Response Time Auto");
            out.write(',');
            out.write("Avg Response Time Man");
            out.write(',');
            out.write("Avg Response Time");
            out.write(',');
            out.write("Total");
            out.write('\n');
            AutoSummaryBean totalautoSummaryBean = (AutoSummaryBean) session.getAttribute("totals");
            List autoSummaryList = (ArrayList) session.getAttribute("TableData");
            for (int i = 0; i < autoSummaryList.size(); i++) {
                AutoSummaryBean autoSummaryBean = (AutoSummaryBean) autoSummaryList.get(i);
                out.write(autoSummaryBean.getDate());
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getManRev()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getPerManrev()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getRejAuto()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getRejMan()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getRej()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getPerRej()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getFocAuto()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getFocMan()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getFoc()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getPerFoc()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getSimple()));
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getComplex()));
                out.write(',');
                out.write(autoSummaryBean.getAvgRespAuto());
                out.write(',');
                out.write(autoSummaryBean.getAvgRespMan());
                out.write(',');
                out.write(autoSummaryBean.getAvgResp());
                out.write(',');
                out.write(String.valueOf(autoSummaryBean.getHorTot()));
                out.write('\n');

            }
            out.write("Total");
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getManRevTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getPerManrevTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getRejAutoTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getRejManTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getRejTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getPerRejTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getFocAutoTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getFocManTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getFocTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getPerFocTot()));
 	     out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getSimpleTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getComplexTot()));
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getAvgRespAutoTot()));
            out.write(',');
            out.write(totalautoSummaryBean.getAvgRespManTot());
            out.write(',');
            out.write(totalautoSummaryBean.getAvgRespTot());
            out.write(',');
            out.write(String.valueOf(totalautoSummaryBean.getVerTot()));
%>

