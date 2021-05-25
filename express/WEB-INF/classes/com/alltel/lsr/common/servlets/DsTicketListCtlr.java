/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	TicketListCtlr.java
 * DESCRIPTION:
 * AUTHOR:
 * DATE:        03-20-2004
 *
 * HISTORY:
 *	03/20/2004  initial
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DsTicketListCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "DsTicketListCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Set up Table Admin stuff
		DsTicketListBean listBean = new DsTicketListBean();

		listBean.setTblNmbr("20");

		String strTcktForm = request.getParameter("tcktsrch");
		if ((strTcktForm == null) || (strTcktForm.length() == 0))
		{
			strTcktForm = "mainpage";
		}

		listBean.setSrchCtgry(request.getParameter("srchctgry"));
		listBean.setSrtBy(request.getParameter("srtby"));
		listBean.setSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");
		if ( (strSrchVal == null) || (strSrchVal.length() == 0))
		{}
		else
		{	strSrchVal = strSrchVal.trim();//get rid of leading and trailing whitespace
		}


		listBean.setSrchVl(Toolkit.wildCardIt(strSrchVal));

		listBean.retrieveTableInfo();
		listBean.buildQueryString(sdm.getUser(), sdm.getCompanyType(), sdm.getCompanySqncNbr() );
		request.getHttpRequest().setAttribute("ticketlistbean", listBean);

		// Obtain number of days to show closed tickets and then set
		int iClsdDys = PropertiesManager.getIntegerProperty("lsr.displaytt.closedstatus", 3);
		String strClosedQuery = " AND (STTS_CD != 'CLOSED' OR (DSTICKET_T.MDFD_DT > (sysdate - " + iClsdDys + ")))";

		// Set up Queue in session
		QueueCriteria qc;
		if (! sdm.doesUserHaveDsTicketQueue())
		{
			qc = new QueueCriteria("a",listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			sdm.setDsTicketQueueCriteria(qc);
			request.putSessionDataManager(sdm);
		}
		else
		{
			qc = sdm.getDsTicketQueueCriteria();
			qc.setOrderByClause(listBean.getOrderBy());
		}

		// Clear out the quick search
		qc.setQuickSearchString(" ");
		qc.setModifiedDateString(" ");

		if (strTcktForm.equals("quicksrch"))
		{
			// Build the Quick Search String
			if ((listBean.getSrchCtgry() != null) && (listBean.getSrchCtgry().length() > 0))
			{
				qc.setQuickSearchString("AND UPPER(" + listBean.getTblAdmnClmnDbNm(Integer.parseInt(listBean.getSrchCtgry())) + ") LIKE UPPER('" + listBean.getSrchVl() + "')");
			}

			// Pass the ticket and response to the JSP
			sdm.setDsTicketQueueCriteria(qc);
			request.putSessionDataManager(sdm);
			alltelRequestDispatcher.forward("/DsTicketListView.jsp");
			return;
		}
		else if (strTcktForm.equals("advsrch"))
		{
			String strBaseQuery = listBean.getQueryString();
			String strQuery = strBaseQuery;

			// Retrieve Search Parameters
			if (request.getParameter("SUBMITBUTTON").equals("Cancel"))
			{
				// Forward onto DsTicketListView
				sdm.setDsTicketQueueCriteria(qc);
				request.putSessionDataManager(sdm);
				alltelRequestDispatcher.forward("/DsTicketListView.jsp");
				return;
			}

			String strTtn = request.getParameter("ttn");
			if ((strTtn != null) && (strTtn.length() > 0))
			{
				strQuery = strQuery + " AND TO_CHAR(DSTICKET_T.TCKT_SQNC_NMBR) LIKE '" +
					Toolkit.wildCardIt(strTtn) + "'";
			}

			boolean bGetClosed = false;
			String[] strTicketStatus = request.getAttributeValue("ticket_status");
			if (strTicketStatus != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strTicketStatus.length; i++)
				{
					// Was "ALL" selected
					if (strTicketStatus[i].length() == 0)
					{
						bGetClosed = true;
						strQuery = strTmp;
						break;
					}

					if (strTicketStatus[i].equals("CLOSED"))
					{
						bGetClosed = true;
					}

					strQuery = strQuery + "DSTICKET_T.STTS_CD = '" +
						strTicketStatus[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			// Do we need to add 'CLOSED' clause
			if (bGetClosed)
			{
				qc.setClosedQuery(" ");
			}
			else
			{
				qc.setClosedQuery(strClosedQuery);
			}

			String[] strCompany = request.getAttributeValue("company");
			if (strCompany != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strCompany.length; i++)
				{
					// Was "ALL" selected
					if (strCompany[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DSTICKET_T.CMPNY_SQNC_NMBR = " +
						strCompany[i] + " OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String[] strOcn = request.getAttributeValue("ocn");
			if (strOcn != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strOcn.length; i++)
				{
					// Was "ALL" selected
					if (strOcn[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DSTICKET_T.OCN_CD = '" +
						strOcn[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String[] strState = request.getAttributeValue("state");
			if (strState != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strState.length; i++)
				{
					// Was "ALL" selected
					if (strState[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DSTICKET_T.OCN_STT = '" +
						strState[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strUserID = request.getParameter("userid");
			if ((strUserID != null) && (strUserID.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSTICKET_T.MDFD_USERID) LIKE UPPER('" +
					Toolkit.wildCardIt(strUserID) + "')";
			}

			String strTMSNmbr = request.getParameter("tmsttno");
			if ((strTMSNmbr != null) && (strTMSNmbr.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSTICKET_T.TCKT_ID) LIKE UPPER('" + Toolkit.wildCardIt(strTMSNmbr) + "')";
			}

//			String strTeleNmbr = request.getParameter("teleno");
//			if ((strTeleNmbr != null) && (strTeleNmbr.length() > 0))
//			{
//				strQuery = strQuery + " AND UPPER(DSTICKET_T.TELNO) LIKE UPPER('" +
//					Toolkit.wildCardIt(strTeleNmbr) + "')";
//			}

                        // Build the From Modified Date
			String strFromMdfdDt = null;
			String strFromMdfdDtMnth = request.getParameter("from_lst_mdfd_mnth");
			String strFromMdfdDtDy = request.getParameter("from_lst_mdfd_dy");
			String strFromMdfdDtYr = request.getParameter("from_lst_mdfd_yr");
			if ((strFromMdfdDtMnth.length() == 0) || (strFromMdfdDtDy.length() == 0) || (strFromMdfdDtYr.length() == 0))
			{
				strFromMdfdDt = "01/01/1900";
			}
			else
			{
				if (strFromMdfdDtMnth.length() == 1)  strFromMdfdDtMnth = "0" + strFromMdfdDtMnth;
				if (strFromMdfdDtDy.length() == 1)  strFromMdfdDtDy = "0" + strFromMdfdDtDy;
				if (strFromMdfdDtYr.length() == 2)  strFromMdfdDtYr = "20" + strFromMdfdDtYr;

				strFromMdfdDt = strFromMdfdDtMnth + "/" + strFromMdfdDtDy + "/" + strFromMdfdDtYr;
			}

			// Build the To Modified Date
			String strToMdfdDt = null;
			String strToMdfdDtMnth = request.getParameter("to_lst_mdfd_mnth");
			String strToMdfdDtDy = request.getParameter("to_lst_mdfd_dy");
			String strToMdfdDtYr = request.getParameter("to_lst_mdfd_yr");
			if ((strToMdfdDtMnth.length() == 0) || (strToMdfdDtDy.length() == 0) || (strToMdfdDtYr.length() == 0))
			{
				strToMdfdDt = "12/31/4444";
			}
			else
			{
				if (strToMdfdDtMnth.length() == 1)  strToMdfdDtMnth = "0" + strToMdfdDtMnth;
				if (strToMdfdDtDy.length() == 1)  strToMdfdDtDy = "0" + strToMdfdDtDy;
				if (strToMdfdDtYr.length() == 2)  strToMdfdDtYr = "20" + strToMdfdDtYr;

				strToMdfdDt = strToMdfdDtMnth + "/" + strToMdfdDtDy + "/" + strToMdfdDtYr;
			}

			strQuery = strQuery + " AND (DSTICKET_T.MDFD_DT BETWEEN TO_DATE('" + strFromMdfdDt +
				"','MM/DD/YYYY') AND TO_DATE('" + strToMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Store Query to Beans
			listBean.setQueryString(strQuery);
			qc.setQueryString(strQuery);
			sdm.setDsTicketQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Forward onto DsTicketListView
			alltelRequestDispatcher.forward("/DsTicketListView.jsp");
			return;
		}
		else if (strTcktForm.equals("advanced"))
		{
			sdm.setDsTicketQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the Advanced Search JSP
			alltelRequestDispatcher.forward("/DsTicketSearchView.jsp");
			return;
		}
		else
		{
			sdm.setDsTicketQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the ticket and response to the JSP
			alltelRequestDispatcher.forward("/DsTicketListView.jsp");
			return;
		}
	}

        protected void populateVariables()
		throws Exception
	{
	}
}

