/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:		DslListCtlr.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        06-05-2002
 *
 * HISTORY:	9-17-2003 psedlak listbean chgs
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DslListCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "DslListCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Set up Table Admin stuff
		DslListBean listBean = new DslListBean();

		listBean.setTblNmbr("17");

		String strDslForm = request.getParameter("dslsrch");
		if ((strDslForm == null) || (strDslForm.length() == 0))
		{
			strDslForm = "mainpage";
		}

		listBean.setSrchCtgry(request.getParameter("srchctgry"));
		listBean.setSrtBy(request.getParameter("srtby"));
		listBean.setSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");

		listBean.setSrchVl(Toolkit.wildCardIt(strSrchVal));

		listBean.retrieveTableInfo();
		listBean.buildQueryString(sdm.getUser(), sdm.getCompanyType(), sdm.getCompanySqncNbr() );
		request.getHttpRequest().setAttribute("dsllistbean", listBean);

		String strClosedQuery = " AND DSL_STTS_CD != 'CLOSED' ";

		// Set up Queue in session
		QueueCriteria qc;
		if (! sdm.doesUserHaveDslQueue())
		{
			qc = new QueueCriteria("a",listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			sdm.setDslQueueCriteria(qc);
			request.putSessionDataManager(sdm);
		}
		else
		{
			qc = sdm.getDslQueueCriteria();
			qc.setOrderByClause(listBean.getOrderBy());
		}

		// Clear out the quick search
		qc.setQuickSearchString(" ");
		qc.setModifiedDateString(" ");

		if (strDslForm.equals("quicksrch"))
		{
			// Build the Quick Search String
			if ((listBean.getSrchCtgry() != null) && (listBean.getSrchCtgry().length() > 0))
			{
				qc.setQuickSearchString("AND UPPER(" + listBean.getTblAdmnClmnDbNm(Integer.parseInt(listBean.getSrchCtgry())) + ") LIKE UPPER('" + listBean.getSrchVl() + "')");
			}

			// Pass the dsl and response to the JSP
			sdm.setDslQueueCriteria(qc);
			request.putSessionDataManager(sdm);
			alltelRequestDispatcher.forward("/DslListView.jsp");
			return;
		}
		else if (strDslForm.equals("advsrch"))
		{
			String strBaseQuery = listBean.getQueryString();
			String strQuery = strBaseQuery;

			// Retrieve Search Parameters
			if (request.getParameter("SUBMITBUTTON").equals("Cancel"))
			{
				// Forward onto DslListView
				sdm.setDslQueueCriteria(qc);
				request.putSessionDataManager(sdm);
				alltelRequestDispatcher.forward("/DslListView.jsp");
				return;
			}

			String strDSN = request.getParameter("dsl_sqnc_nmbr");
			if ((strDSN != null) && (strDSN.length() > 0))
			{
				strQuery = strQuery + " AND TO_CHAR(DSL_T.DSL_SQNC_NMBR) LIKE '" +
					Toolkit.wildCardIt(strDSN) + "'";
			}

			boolean bGetClosed = false;
			String[] strDslStatus = request.getAttributeValue("dsl_status");
			if (strDslStatus != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strDslStatus.length; i++)
				{
					// Was "ALL" selected
					if (strDslStatus[i].length() == 0)
					{
						bGetClosed = true;
						strQuery = strTmp;
						break;
					}

					if (strDslStatus[i].equals("CLOSED"))
					{
						bGetClosed = true;
					}

					strQuery = strQuery + "DSL_T.DSL_STTS_CD = '" +
						strDslStatus[i] + "' OR ";
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

			String strRqstTyp = request.getParameter("dsl_rqst_typ");
			if ((strRqstTyp != null) && (strRqstTyp.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSL_T.RQST_TYP) LIKE UPPER('" +
					Toolkit.wildCardIt(strRqstTyp) + "')";
			}

			String[] strISP = request.getAttributeValue("isp");
			if (strISP != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strISP.length; i++)
				{
					// Was "ALL" selected
					if (strISP[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DSL_T.CMPNY_SQNC_NMBR = " +
						strISP[i] + " OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strCity = request.getParameter("city");
			if ((strCity != null) && (strCity.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSL_T.CITY) LIKE UPPER('" +
					Toolkit.wildCardIt(strCity) + "')";
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

					strQuery = strQuery + "DSL_T.STATE = '" +
						strState[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strUserID = request.getParameter("userid");
			if ((strUserID != null) && (strUserID.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSL_T.MDFD_USERID) LIKE UPPER('" +
					Toolkit.wildCardIt(strUserID) + "')";
			}

			String strDSLTelno = request.getParameter("dsltelno");
			if ((strDSLTelno != null) && (strDSLTelno.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSL_T.DSL_SRVC_TELNO) LIKE UPPER('" +
					Toolkit.wildCardIt(strDSLTelno) + "')";
			}

			String strSO = request.getParameter("so");
			if ((strSO != null) && (strSO.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DSL_T.ORDR_NMBR) LIKE UPPER('" +
					Toolkit.wildCardIt(strSO) + "')";
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

			strQuery = strQuery + " AND (DSL_T.MDFD_DT BETWEEN TO_DATE('" + strFromMdfdDt +
				"','MM/DD/YYYY') AND TO_DATE('" + strToMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Store Query to Beans
			listBean.setQueryString(strQuery);
			qc.setQueryString(strQuery);
			sdm.setDslQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Forward onto DslListView
			alltelRequestDispatcher.forward("/DslListView.jsp");
			return;
		}
		else if (strDslForm.equals("advanced"))
		{
			sdm.setDslQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the Advanced Search JSP
			alltelRequestDispatcher.forward("/DslSearchView.jsp");
			return;
		}
		else
		{
			sdm.setDslQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the dsl and response to the JSP
			alltelRequestDispatcher.forward("/DslListView.jsp");
			return;
		}
	}

        protected void populateVariables()
		throws Exception
	{
	}
}

