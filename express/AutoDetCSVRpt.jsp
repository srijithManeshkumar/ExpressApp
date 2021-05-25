<%-- 
    Document   : AutoDetCSVRpt
    Created on : May 20, 2011, 4:59:58 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.AutoDetailBean,java.util.*"  session="true"%>

<%
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + "AutoActivityDetail" + ".csv");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            out.write("PON");
            out.write(',');
            out.write("OCN");
            out.write(',');
            out.write("Company");
            out.write(',');
            out.write("External Status");
            out.write(',');
            out.write("Version");
            out.write(',');
            out.write("State");
            out.write(',');
            out.write("TN");
            out.write(',');
            out.write("Service Type");
            out.write(',');
            out.write("Activity Type");
            out.write(',');
            out.write("Submitted Date");
            out.write(',');
            out.write("FOC/Reject/Manual Review Date");
            out.write(',');
            out.write("Automation Status");
            out.write(',');
            out.write("Simple/Non-Simple");
            out.write(',');
            out.write("Actual Response Time");
            out.write("\n");
            StringBuffer sb = new StringBuffer(256);
            List autoDetailList = (ArrayList) session.getAttribute("detailList");
            for (int i = 0; i < autoDetailList.size(); i++) {
                AutoDetailBean autoDetailBean = (AutoDetailBean) autoDetailList.get(i);
                out.write(autoDetailBean.getPon());
                out.write(',');
                out.write(autoDetailBean.getOcn());
                out.write(',');
                out.write(autoDetailBean.getCompany().replaceAll(",", " "));
                out.write(',');
                out.write(autoDetailBean.getExternalstatus());
                out.write(',');
                out.write(autoDetailBean.getVersion());
                out.write(',');
                out.write(autoDetailBean.getState());
                out.write(',');
                out.write(autoDetailBean.getTn());
                out.write(',');
                out.write(autoDetailBean.getServicetype());
                out.write(',');
                out.write(autoDetailBean.getActivitytype());
                out.write(',');
                out.write(autoDetailBean.getSubmitteddate());
                out.write(',');
                out.write(autoDetailBean.getFocrejMandate());
                out.write(',');
                out.write(autoDetailBean.getAutoStatus());
                out.write(',');
                out.write(autoDetailBean.getSimpleflag());
                out.write(',');
                out.write(autoDetailBean.getActResponseTime());
                out.write("\n");
            }
            out.close();
%>
