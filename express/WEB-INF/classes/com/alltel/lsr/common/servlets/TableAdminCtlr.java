package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class TableAdminCtlr extends AlltelServlet
{	
	public void myservice(AlltelRequest request, AlltelResponse response)
			throws Exception
	{	
		Log.write(Log.DEBUG_VERBOSE, "TableAdminCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// validate request parameters that are required or that need edits performed
		String m_strRqstTblNmbr	= request.getParameter("tblnmbr");
		if ((m_strRqstTblNmbr == null) || (m_strRqstTblNmbr.length() == 0))
		{
			alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
			return;
		}
	
		TableAdminBean beanTableAdmin = new TableAdminBean();
	
		beanTableAdmin.setRqstTblNmbr(request.getParameter("tblnmbr"));
		beanTableAdmin.setRstrctSrch(request.getParameter("rstrctsrch"));
		beanTableAdmin.setRqstSrchCtgry(request.getParameter("srchctgry"));
		beanTableAdmin.setRqstSrtBy(request.getParameter("srtby"));
		beanTableAdmin.setRqstSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");

		if ((beanTableAdmin.getRstrctSrch() != null) && (beanTableAdmin.getRstrctSrch().equals("yes")))
		{
			beanTableAdmin.setRstrctSrchCtgry(request.getParameter("rstrctsrchctgry"));
			if (beanTableAdmin.getRstrctSrchCtgry() == null)
				beanTableAdmin.setRstrctSrchCtgry(request.getParameter("srchctgry"));
				
			beanTableAdmin.setRstrctSrchVl(request.getParameter("rstrctsrchvl"));
			if (beanTableAdmin.getRstrctSrchVl() == null)
				beanTableAdmin.setRstrctSrchVl(request.getParameter("srchvl"));
		}

		beanTableAdmin.setRqstSrchVl(Toolkit.wildCardIt(strSrchVal));

		beanTableAdmin.retrieveTableInfo();
		// Check status of retrieve to determine if look was ok and if we can continue
	
		beanTableAdmin.buildQueryString();
	
		// Pass the request and response to the JSP 
		request.getHttpRequest().setAttribute("tableadminbean", beanTableAdmin);
		alltelRequestDispatcher.forward("/TableAdminView.jsp");
		return;
	}

        protected void populateVariables()
		throws Exception
	{
	}
}
