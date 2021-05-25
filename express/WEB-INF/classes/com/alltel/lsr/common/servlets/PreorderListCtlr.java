/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	PreorderListCtlr.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        03-20-2002
 *
 * HISTORY:
 *	03/20/2002  initial check-in.
 *	09/16/2003  psedlak use generic listbean
 *
 */
package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class PreorderListCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "PreorderListCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Set up Table Admin stuff
		PreorderListBean listBean = new PreorderListBean();

		listBean.setTblNmbr("14");

		String strPreForm = request.getParameter("presrch");
		if ((strPreForm == null) || (strPreForm.length() == 0))
		{
			strPreForm = "mainpage";
		}

		listBean.setSrchCtgry(request.getParameter("srchctgry"));
		listBean.setSrtBy(request.getParameter("srtby"));
		listBean.setSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");

		listBean.setSrchVl(Toolkit.wildCardIt(strSrchVal));

		listBean.retrieveTableInfo();
		listBean.buildQueryString(sdm.getUser(), sdm.getCompanyType(), sdm.getCompanySqncNbr() );
		request.getHttpRequest().setAttribute("preorderlistbean", listBean);

		String strClosedQuery = " AND PRE_ORDR_STTS_CD != 'CLOSED' ";

		// Set up Queue in session
		QueueCriteria qc;
		if (! sdm.doesUserHavePreorderQueue())
		{
			qc = new QueueCriteria("a",listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			sdm.setPreorderQueueCriteria(qc);
			request.putSessionDataManager(sdm);
		}
		else
		{
			qc = sdm.getPreorderQueueCriteria();
			qc.setOrderByClause(listBean.getOrderBy());
		}

		// Clear out the quick search
		qc.setQuickSearchString(" ");
        qc.setModifiedDateString(" ");
		if (strPreForm.equals("quicksrch"))
		{
			// Build the Quick Search String
			if ((listBean.getSrchCtgry() != null) && (listBean.getSrchCtgry().length() > 0))
			{
				qc.setQuickSearchString("AND UPPER(" + listBean.getTblAdmnClmnDbNm(Integer.parseInt(listBean.getSrchCtgry())) + ") LIKE UPPER('" + listBean.getSrchVl() + "')");
			}

			// Pass the pre and response to the JSP
			sdm.setPreorderQueueCriteria(qc);
			request.putSessionDataManager(sdm);
			alltelRequestDispatcher.forward("/PreorderListView.jsp");
			return;
		}
		else if (strPreForm.equals("advsrch"))
		{
			String strBaseQuery = listBean.getQueryString();
			String strQuery = strBaseQuery;

			// Retrieve Search Parameters
			if (request.getParameter("SUBMITBUTTON").equals("Cancel"))
			{
				// Forward onto PreorderListView
				sdm.setPreorderQueueCriteria(qc);
				request.putSessionDataManager(sdm);
				alltelRequestDispatcher.forward("/PreorderListView.jsp");
				return;
			}

			String strTxNum = request.getParameter("txnum");
			if ((strTxNum != null) && (strTxNum.length() > 0))
			{
				strQuery = strQuery + " AND PREORDER_T.PRE_ORDR_SQNC_NMBR LIKE '" +
					Toolkit.wildCardIt(strTxNum) + "'";
			}

			boolean bGetClosed = false;
			String[] strStatus = request.getAttributeValue("status");
			if (strStatus != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strStatus.length; i++)
				{
					// Was "ALL" selected
					if (strStatus[i].length() == 0)
					{
						bGetClosed = true;
						strQuery = strTmp;
						break;
					}

					if (strStatus[i].equals("CLOSED"))
					{
						bGetClosed = true;
					}

					strQuery = strQuery + "PREORDER_T.PRE_ORDR_STTS_CD = '" +
						strStatus[i] + "' OR ";
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

					strQuery = strQuery + "PREORDER_T.OCN_CD = '" +
						strOcn[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
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

					strQuery = strQuery + "PREORDER_T.CMPNY_SQNC_NMBR = " +
						strCompany[i] + " OR ";
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

					strQuery = strQuery + "PREORDER_T.OCN_STT = '" +
						strState[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String[] strSrvcTyp = request.getAttributeValue("srvc_type");
			if (strSrvcTyp != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strSrvcTyp.length; i++)
				{
					// Was "ALL" selected
					if (strSrvcTyp[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "PREORDER_T.SRVC_TYP_CD = '" +
						strSrvcTyp[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String[] strActTyp = request.getAttributeValue("act_type");
			if (strActTyp != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strActTyp.length; i++)
				{
					// Was "ALL" selected
					if (strActTyp[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "PREORDER_T.ACTVTY_TYP_CD = '" +
						strActTyp[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strUserID = request.getParameter("userid");
			if ((strUserID != null) && (strUserID.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(PREORDER_T.MDFD_USERID) LIKE UPPER('" +
					Toolkit.wildCardIt(strUserID) + "')";
			}

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

			strQuery = strQuery + " AND (PREORDER_T.MDFD_DT BETWEEN TO_DATE('" + strFromMdfdDt +
				"','MM/DD/YYYY') AND TO_DATE('" + strToMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Build the From Date Sent
			String strFromDateSent = null;

			String strFromDateSentMnth = request.getParameter("from_date_sent_mnth");
			String strFromDateSentDy = request.getParameter("from_date_sent_dy");
			String strFromDateSentYr = request.getParameter("from_date_sent_yr");
			if ((strFromDateSentMnth.length() == 0) || (strFromDateSentDy.length() == 0) || (strFromDateSentYr.length() == 0))
			{
				strFromDateSent = "01/01/1900";
			}
			else
			{
				if (strFromDateSentMnth.length() == 1)  strFromDateSentMnth = "0" + strFromDateSentMnth;
				if (strFromDateSentDy.length() == 1)  strFromDateSentDy = "0" + strFromDateSentDy;
				if (strFromDateSentYr.length() == 2)  strFromDateSentYr = "20" + strFromDateSentYr;

				strFromDateSent = strFromDateSentMnth + "/" + strFromDateSentDy + "/" + strFromDateSentYr;
			}

			// Build the To Date Sent
			String strToDateSent = null;
			String strToDateSentMnth = request.getParameter("to_date_sent_mnth");
			String strToDateSentDy = request.getParameter("to_date_sent_dy");
			String strToDateSentYr = request.getParameter("to_date_sent_yr");
			if ((strToDateSentMnth.length() == 0) || (strToDateSentDy.length() == 0) || (strToDateSentYr.length() == 0))
			{
				strToDateSent = "12/31/4444";
			}
			else
			{
				if (strToDateSentMnth.length() == 1)  strToDateSentMnth = "0" + strToDateSentMnth;
				if (strToDateSentDy.length() == 1)  strToDateSentDy = "0" + strToDateSentDy;
				if (strToDateSentYr.length() == 2)  strToDateSentYr = "20" + strToDateSentYr;

				strToDateSent = strToDateSentMnth + "/" + strToDateSentDy + "/" + strToDateSentYr;
			}

			strQuery = strQuery + " AND (TO_DATE(DECODE(PREORDER_T.DTSENT, ' ', '01-01-1899-0100AM', PREORDER_T.DTSENT), 'MM-DD-YYYY-HHMIAM') BETWEEN TO_DATE('" + strFromDateSent + "','MM/DD/YYYY') AND TO_DATE('" + strToDateSent + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Store Query to Beans
			listBean.setQueryString(strQuery);
			qc.setQueryString(strQuery);
			sdm.setPreorderQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Forward onto PreorderListView
			alltelRequestDispatcher.forward("/PreorderListView.jsp");
			return;
		}
		else if (strPreForm.equals("advanced"))
		{
			sdm.setPreorderQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the Advanced Search JSP
			alltelRequestDispatcher.forward("/PreorderSearchView.jsp");
			return;
		}
		else
		{
			sdm.setPreorderQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the JSP
			alltelRequestDispatcher.forward("/PreorderListView.jsp");
			return;
		}
	}

        protected void populateVariables()
		throws Exception
	{
	}
}

