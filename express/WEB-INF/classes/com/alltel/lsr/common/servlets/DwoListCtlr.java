/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	DwoListCtlr.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        03-20-2004
 *
 * HISTORY:
 *	pjs 7-12  Business Data Product changes to be able to reuse same modules for KPEN orders and
 *		Bus data product orders.  When a user hits a top menu bar choice - the products query and
 *		table choice is set below (for use in listbean and QC), and the choice is pushed to the
 *		http session.  So things like the list view pull from session to see what it should do.
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DwoListCtlr extends AlltelServlet
{
	private final static String KPEN_TBL = "19";
	private final static String KPEN_QC = "Kpen";
	private final static String KPEN_PRDCTS = " AND DWO_T.PRDCT_TYP_CD='V' ";

	private final static String BDP_TBL = "21";
	private final static String BDP_QC = "Bdp";
	private final static String BDP_PRDCTS = " AND DWO_T.PRDCT_TYP_CD!='V' ";

	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "DwoListCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		//Determine the type of list for the data products....either Business data prods or regular KPEN
		String strProductsQuery = KPEN_PRDCTS;
		String queueName = "";
		String strTbl = request.getParameter("pjvn");
Log.write(Log.DEBUG_VERBOSE, "DwoListCtlr() BB strTbl="+strTbl);


		//This guy gets set in arrow sorts....
		String strTblNmbr = request.getParameter("tblnmbr");

		String strPJVN = (String) request.getSession().getAttribute("DwOcHoIcE");
		if (strPJVN==null) strPJVN="";

		if (strTbl == null)
		{	if (strPJVN.equals(KPEN_QC))
			{	Log.write(Log.DEBUG_VERBOSE, "DwoListCtlr() strTbl is null");
				strTbl=KPEN_TBL;
			}
			else if (strPJVN.equals(BDP_QC))
			{	strTbl=BDP_TBL;
			}
		}

		// KPEN or BDP?
		String strTypInd = "";
		if (strTbl.equals(BDP_TBL))
		{
			strTypInd = "X";
		}
		else
		{
			strTypInd = "W";
		}

		// Set up Table Admin stuff
		DwoListBean listBean = new DwoListBean(strTypInd);

		if (strTbl.equals(KPEN_TBL))
                {
			request.getSession().setAttribute("DwOcHoIcE", new String(KPEN_QC));
listBean.setProductQuery(strProductsQuery);
			listBean.setTblNmbr(KPEN_TBL);
			queueName = KPEN_QC;
			Log.write(Log.DEBUG_VERBOSE, "DwoListCtlr() ="+ KPEN_TBL);
		}
		else if (strTbl.equals(BDP_TBL))
		{
			//Push choice (KPEN or BusDataProds) to session
			request.getSession().setAttribute("DwOcHoIcE", new String(BDP_QC));
			strProductsQuery = BDP_PRDCTS;
listBean.setProductQuery(strProductsQuery);
			listBean.setTblNmbr(BDP_TBL);
			queueName = BDP_QC;
			Log.write(Log.DEBUG_VERBOSE, "DwoListCtlr() ="+BDP_TBL);
		}

		String strDwoForm = request.getParameter("dwosrch");
		if ((strDwoForm == null) || (strDwoForm.length() == 0))
		{
			strDwoForm = "mainpage";
		}

		listBean.setSrchCtgry(request.getParameter("srchctgry"));
		listBean.setSrtBy(request.getParameter("srtby"));
		listBean.setSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");

		listBean.setSrchVl(Toolkit.wildCardIt(strSrchVal));

		listBean.retrieveTableInfo();
		listBean.buildQueryString(sdm.getUser(), sdm.getCompanyType(), sdm.getCompanySqncNbr() );
//	String strStarterQuery = listBean.getQueryString();	// + strProductsQuery;
//		listBean.setQueryString(strStarterQuery);

		request.getHttpRequest().setAttribute("dwolistbean", listBean);

		// Obtain number of days to show closed dwos and then set
		int iClsdDys = PropertiesManager.getIntegerProperty("lsr.displaydwo.closedstatus", 3);
		String strClosedQuery = " AND (DWO_STTS_CD != 'CLOSED' OR (DWO_T.MDFD_DT > (sysdate - " + iClsdDys + ")))";

		// Set up Queue in session
		QueueCriteria qc;
		//if (! sdm.doesUserHaveDwoQueue())
		if (! sdm.doesUserHaveDwoQueue( queueName ))
		{
			//qc = new QueueCriteria("a", listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			qc = new QueueCriteria(queueName, listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			sdm.setDwoQueueCriteria(qc);
			request.putSessionDataManager(sdm);
		}
		else
		{
			qc = sdm.getDwoQueueCriteria( queueName );
			qc.setOrderByClause(listBean.getOrderBy());
		}

		// Clear out the quick search
		qc.setQuickSearchString(" ");
		qc.setModifiedDateString(" ");

		if (strDwoForm.equals("quicksrch"))
		{
			// Build the Quick Search String
			if ((listBean.getSrchCtgry() != null) && (listBean.getSrchCtgry().length() > 0))
			{
				qc.setQuickSearchString("AND UPPER(" + listBean.getTblAdmnClmnDbNm(Integer.parseInt(listBean.getSrchCtgry())) + ") LIKE UPPER('" + listBean.getSrchVl() + "')");
			}

			// Pass the dwo and response to the JSP
			sdm.setDwoQueueCriteria(qc);
			request.putSessionDataManager(sdm);
			alltelRequestDispatcher.forward("/DwoListView.jsp");
			return;
		}
		else if (strDwoForm.equals("advsrch"))
		{
			String strBaseQuery = listBean.getQueryString();
			String strQuery = strBaseQuery;
//strQuery = strQuery + strProductsQuery;
			// Retrieve Search Parameters
			if (request.getParameter("SUBMITBUTTON").equals("Cancel"))
			{
				// Forward onto DwoListView
				sdm.setDwoQueueCriteria(qc);
				request.putSessionDataManager(sdm);
				alltelRequestDispatcher.forward("/DwoListView.jsp");
				return;
			}

			String strDwoSqncNbr = request.getParameter("dwo_nbr");
			if ((strDwoSqncNbr != null) && (strDwoSqncNbr.length() > 0))
			{
				strQuery = strQuery + " AND TO_CHAR(DWO_T.DWO_SQNC_NMBR) LIKE '" +
					Toolkit.wildCardIt(strDwoSqncNbr) + "'";
			}

			boolean bGetClosed = false;
			String[] strDwoStatus = request.getAttributeValue("dwo_status");
			if (strDwoStatus != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strDwoStatus.length; i++)
				{
					// Was "ALL" selected
					if (strDwoStatus[i].length() == 0)
					{
						bGetClosed = true;
						strQuery = strTmp;
						break;
					}

					if (strDwoStatus[i].equals("CLOSED"))
					{
						bGetClosed = true;
					}

					strQuery = strQuery + "DWO_T.DWO_STTS_CD = '" +
						strDwoStatus[i] + "' OR ";
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

			String[] strDwoOrderType = request.getAttributeValue("dwo_ordertype");
			if (strDwoOrderType != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strDwoOrderType.length; i++)
				{
					// Was "ALL" selected
					if (strDwoOrderType[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DWO_T.SRVC_TYP_CD = '" +
						strDwoOrderType[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String[] strDwoProductType = request.getAttributeValue("dwo_producttype");
			if (strDwoProductType != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strDwoProductType.length; i++)
				{
					// Was "ALL" selected
					if (strDwoProductType[i].length() == 0)
					{
						strQuery = strTmp;
						break;
					}

					strQuery = strQuery + "DWO_T.PRDCT_TYP_CD = '" +
						strDwoProductType[i] + "' OR ";
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

					strQuery = strQuery + "DWO_T.CMPNY_SQNC_NMBR = " +
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

					strQuery = strQuery + "DWO_T.OCN_CD = '" +
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

					strQuery = strQuery + "DWO_T.STT_CD = '" +
						strState[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strLocName = request.getParameter("locname");
			if ((strLocName != null) && (strLocName.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DWO_T.LCTN_NM) LIKE UPPER('" +
					Toolkit.wildCardIt(strLocName) + "')";
			}

			String strBusiName = request.getParameter("businame");
			if ((strBusiName != null) && (strBusiName.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DWO_T.BSNSS_NM) LIKE UPPER('" +
					Toolkit.wildCardIt(strBusiName) + "')";
			}

			String strUserID = request.getParameter("userid");
			if ((strUserID != null) && (strUserID.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(DWO_T.MDFD_USERID) LIKE UPPER('" +
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

			strQuery = strQuery + " AND (DWO_T.MDFD_DT BETWEEN TO_DATE('" + strFromMdfdDt +
				"','MM/DD/YYYY') AND TO_DATE('" + strToMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Store Query to Beans
			listBean.setQueryString(strQuery);
			qc.setQueryString(strQuery);
			sdm.setDwoQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Forward onto DwoListView
			alltelRequestDispatcher.forward("/DwoListView.jsp");
			return;
		}
		else if (strDwoForm.equals("advanced"))
		{
			sdm.setDwoQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the Advanced Search JSP
			alltelRequestDispatcher.forward("/DwoSearchView.jsp");
			return;
		}
		else
		{
			sdm.setDwoQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the dwo and response to the JSP
			alltelRequestDispatcher.forward("/DwoListView.jsp");
			return;
		}
	}

        protected void populateVariables()
		throws Exception
	{
	}
}

