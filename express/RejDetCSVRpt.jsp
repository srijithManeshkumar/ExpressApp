<%-- 
    Document   : RejDetCSVRpt
    Created on : May 20, 2011, 5:13:17 PM
    Author     : satish.t
--%>

<%@page  import="com.automation.reports.bean.RejectionDetailBean,java.util.*"  session="true"%>

<%
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + "RejectionDetailReport" + ".csv");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            out.write("PON");
            out.write(',');
            out.write("TN");
            out.write(',');
            out.write("VendorName");
            out.write(',');
            out.write("OCN");
            out.write(',');
            out.write("State");
            out.write(',');
            out.write("Internal Rejection Reason Code");
            out.write(',');
            out.write("Reason (RDET)");
            out.write(',');
            out.write("CI Drop Down Reason");
            out.write(',');
            out.write("Submitted Date");
            out.write(',');
            out.write("Rejection Date");
            out.write("\n");

            List rejDetailList = (ArrayList) session.getAttribute("detailList");
            for (int i = 0; i < rejDetailList.size(); i++) {
                RejectionDetailBean rejectionDetailBean = (RejectionDetailBean) rejDetailList.get(i);
                out.write(rejectionDetailBean.getPon());
                out.write(',');
                out.write(rejectionDetailBean.getTn());
                out.write(',');
                out.write(rejectionDetailBean.getVendorname().replaceAll(",", " "));
                out.write(',');
                out.write(rejectionDetailBean.getOcn());
                out.write(',');
                out.write(rejectionDetailBean.getState());
                out.write(',');
                out.write(rejectionDetailBean.getRejreasoncode());
                out.write(',');
                out.write(rejectionDetailBean.getRejdet());
                out.write(',');
                out.write(rejectionDetailBean.getCireason());
                out.write(',');
                out.write(rejectionDetailBean.getSubmitteddate());
                out.write(',');
                out.write(rejectionDetailBean.getRejectiondate());
                out.write("\n");
            }
            out.close();
%>

