/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                      BY
 *                              ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	RequestListCtlr.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Express team
 *
 * DATE:        01-01-2002
 *
 * HISTORY:
 *      08/29/2002 psedlak emer HD99494/CC30028 -catch exceptions that occur in forwarded JSP.
 *	09/16/2003  psedlak use generic listbean
 *	12-2-04  psedlak Added SUBMITTED DATE to LSAPC adv searches
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import java.text.*;

public class RequestListCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{
		Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
        String strCmpnySqncNmbr = sdm.getCompanySqncNbr();
        String strUser = sdm.getUser();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Set up Table Admin stuff
		RequestListBean listBean = new RequestListBean();

		listBean.setTblNmbr("11");

		String strRqstForm = request.getParameter("rqstsrch");
		if ((strRqstForm == null) || (strRqstForm.length() == 0))
		{
			strRqstForm = "mainpage";
		}

		listBean.setSrchCtgry(request.getParameter("srchctgry"));
		listBean.setSrtBy(request.getParameter("srtby"));
		listBean.setSrtSqnc(request.getParameter("srtsqnc"));

		String strSrchVal = request.getParameter("srchvl");

		listBean.setSrchVl(Toolkit.wildCardIt(strSrchVal));


		listBean.retrieveTableInfo();
		listBean.buildQueryString(sdm.getUser(), sdm.getCompanyType(), sdm.getCompanySqncNbr() );
		request.getHttpRequest().setAttribute("requestlistbean", listBean);

		String strClosedQuery = " AND RQST_STTS_CD != 'CLOSED' ";

	    Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() 2");
		// Set up Queue in session
		QueueCriteria qc;
		if (! sdm.doesUserHaveRequestQueue())
		{
			qc = new QueueCriteria("a",listBean.getQueryString(), strClosedQuery, listBean.getOrderBy());
			sdm.setRequestQueueCriteria(qc);
			request.putSessionDataManager(sdm);
		}
		else
		{
			qc = sdm.getRequestQueueCriteria();
			qc.setOrderByClause(listBean.getOrderBy());
		}

		// Clear out the quick search
		qc.setQuickSearchString(" ");
		qc.setModifiedDateString(" ");
		String strControl = qc.getControlString();
		if (strRqstForm.equals("quicksrch"))
		{
			Log.write(Log.DEBUG_VERBOSE, "strUser: "+ strUser);
			// Build the Quick Search String
			if ((listBean.getSrchCtgry() != null) && (listBean.getSrchCtgry().length() > 0))
			{
				qc.setQuickSearchString("AND UPPER(" + listBean.getTblAdmnClmnDbNm(Integer.parseInt(listBean.getSrchCtgry())) + ") LIKE UPPER('" + listBean.getSrchVl() + "')");
			}
			else if ((strCmpnySqncNmbr.equals("1")) && (strControl.equals("N")))
		    {
				String strMdfdDt = null;
				Calendar cal = Calendar.getInstance();
			    strMdfdDt = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
                qc.setModifiedDateString(" AND (REQUEST_T.MDFD_DT > TO_DATE('" + strMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))");
		    }
			// Pass the request and response to the JSP
			sdm.setRequestQueueCriteria(qc);
			request.putSessionDataManager(sdm);
			alltelRequestDispatcher.forward("/RequestListView.jsp");
			return;
		}
		else if (strRqstForm.equals("advsrch"))
		{
			Log.write(Log.DEBUG_VERBOSE, "strUser: "+ strUser);
	        Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() 3");
			String strQuery = listBean.getQueryString();
			String strExtendedQuery = "";	//Extended query into Multi-orders
			boolean bIncludeMulti=false;	//Flag used to determine if extended searching into Multi Orders is needed

                        Log.write("Before SUBMITBUTTON if block....");
                        
			// Retrieve Search Parameters
			if (request.getParameter("SUBMITBUTTON").equals("Cancel"))
			{
                            
                            Log.write("Before strcontrol = N if if block....");
				if ((strCmpnySqncNmbr.equals("1")) && (strControl.equals("N")))
				{
                                
                                Log.write("Inside strcontrol = N if if block....");
					String strMdfdDt = null;
					Calendar cal = Calendar.getInstance();
					strMdfdDt = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
				    qc.setModifiedDateString(" AND (REQUEST_T.MDFD_DT > TO_DATE('" + strMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))");
                                }
                            
                            Log.write("Before forward....");
				// Forward onto RequestListView
				sdm.setRequestQueueCriteria(qc);
				request.putSessionDataManager(sdm);
				alltelRequestDispatcher.forward("/RequestListView.jsp");
                                
                                Log.write("After forward....");
				return;
			}
			else
			{
				qc.setControlString("Y");
			}

			boolean bGetClosed = false;
			String[] strOrderStatus = request.getAttributeValue("order_status");
			if (strOrderStatus != null)
			{
				String strTmp = strQuery;
				strQuery = strQuery + " AND (";
				for (int i=0; i<strOrderStatus.length; i++)
				{
					// Was "ALL" selected
					if (strOrderStatus[i].length() == 0)
					{
						bGetClosed = true;
						strQuery = strTmp;
						break;
					}

					if (strOrderStatus[i].equals("CLOSED"))
					{
						bGetClosed = true;
					}

					strQuery = strQuery + "REQUEST_T.RQST_STTS_CD = '" +
						strOrderStatus[i] + "' OR ";
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

					strQuery = strQuery + "REQUEST_T.CMPNY_SQNC_NMBR = " +
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

					strQuery = strQuery + "REQUEST_T.OCN_CD = '" +
						strOcn[i] + "' OR ";
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
						bIncludeMulti=true;
						break;
					}

					strQuery = strQuery + "REQUEST_T.SRVC_TYP_CD = '" +
						strSrvcTyp[i] + "' OR ";
					if (strSrvcTyp[i].equals("1"))
					{ bIncludeMulti=true; }
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}
			else
			{	bIncludeMulti=true; }

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

					strQuery = strQuery + "REQUEST_T.ACTVTY_TYP_CD = '" +
						strActTyp[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

            //start new Added code for Q & V by kumar
            String[] inStts = request.getAttributeValue("inStatus");
            if (inStts != null) {
                String strTmp = strQuery;
                strQuery = strQuery + " AND (";
                for (int i=0; i<inStts.length; i++) {
                    // Was "ALL" selected
                    if (inStts[i].length() == 0) {
                        strQuery = strTmp;
                        break;
                    }
                    
                    strQuery = strQuery + "REQUEST_T.INN_STTS= '" +
                            inStts[i] + "' OR ";
                }
                
                if (strQuery != strTmp)
                    strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
            }
            //end new Added code for Q & V by kumar
            
            
            
            //start new Added code for Simple ports project search field for Simple Order - Antony - 12/15/2010
            String spFlag = (String)request.getParameter("spFlag");
            
            
            if(spFlag == null || spFlag.equals("NULL")) {
                spFlag = null;
            }
            
            if (spFlag != null && spFlag.length() > 0) {
                Log.write("spFlag :"+spFlag);
                String strTmp = strQuery;
                strQuery = strQuery + " AND ";

                strQuery = strQuery + "REQUEST_T.SIMPLE_PORT_FLAG= '" +
                            spFlag + "'";

                Log.write("Inside if strQuery for Simple Port Field: "+strQuery);

                /*
                if (strQuery != strTmp)
                    strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
                 **/
            
            }
                
            //end new Added code for SP Flag field

            //start new Added code for Order Type(ICARE or CAMS)- Vijay - 01/19/2012
            String strOrderFlag = request.getParameter("orderFlag");
            
            Log.write(Log.DEBUG_VERBOSE, "strOrderFlag=[" + strOrderFlag + "]");

            if (strOrderFlag.equals("ALL")) {
                strOrderFlag = "ALL";
            } else {
                strQuery = strQuery + " AND REQUEST_T.ICARE='" + strOrderFlag + "'";

            }
 //END new Added code for Order Type(ICARE or CAMS)- Vijay - 01/19/2012
            Log.write("Query outside if"+strQuery);
            
                       String strCity = request.getParameter("city");
                       if ((strCity != null) && (strCity.length() > 0))
                       {
                               strQuery = strQuery + " AND UPPER(REQUEST_T.CITY) LIKE UPPER('" +
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

					strQuery = strQuery + "REQUEST_T.OCN_STT = '" +
						strState[i] + "' OR ";
				}

				if (strQuery != strTmp)
					strQuery = strQuery.substring(0,strQuery.length()-4) + ")";
			}

			String strUserID = request.getParameter("userid");
			if ((strUserID != null) && (strUserID.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(REQUEST_T.MDFD_USERID) LIKE UPPER('" +
					Toolkit.wildCardIt(strUserID) + "')";
			}
		// HD0000001776068
			String strEXP = request.getParameter( "exp" );
			if( (strEXP != null) && (strEXP.length() > 0 ) ){
			 	if ( strEXP.equals("Yes") ){
					strQuery  +=  " AND REQUEST_T.RQST_EXP = 'Y' ";
				}
			}
			String strSoNum = request.getParameter("so_num");
			if ((strSoNum != null) && (strSoNum.length() > 0))
			{
				strQuery = strQuery + " AND TO_CHAR(REQUEST_T.SRVC_RDR_NBR) LIKE UPPER('" +
					Toolkit.wildCardIt(strSoNum) + "')";
			}

			String strCustName = request.getParameter("cust_name");
			if ((strCustName != null) && (strCustName.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(REQUEST_T.CUST) LIKE UPPER('" +
					Toolkit.wildCardIt(strCustName) + "')";
			}

			String strAn = request.getParameter("an");
			if ((strAn != null) && (strAn.length() > 0))
			{
				strQuery = strQuery + " AND REQUEST_T.AN LIKE '" +
					Toolkit.wildCardIt(strAn) + "'";
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

			// Build the To Due Date
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

			strQuery = strQuery + " AND (REQUEST_T.MDFD_DT BETWEEN TO_DATE('" + strFromMdfdDt +
				"','MM/DD/YYYY') AND TO_DATE('" + strToMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))";

			// Build the From Due Date
			String strFromDueDate = null;

			String strFromDueDateMnth = request.getParameter("from_due_date_mnth");
			String strFromDueDateDy = request.getParameter("from_due_date_dy");
			String strFromDueDateYr = request.getParameter("from_due_date_yr");
			if ((strFromDueDateMnth.length() == 0) || (strFromDueDateDy.length() == 0) || (strFromDueDateYr.length() == 0))
			{
				strFromDueDate = "0";
			}
			else
			{
				if (strFromDueDateMnth.length() == 1)  strFromDueDateMnth = "0" + strFromDueDateMnth;
				if (strFromDueDateDy.length() == 1)  strFromDueDateDy = "0" + strFromDueDateDy;
				if (strFromDueDateYr.length() == 2)  strFromDueDateYr = "20" + strFromDueDateYr;

				strFromDueDate = strFromDueDateYr + strFromDueDateMnth + strFromDueDateDy;
			}

			// Build the To Due Date
			String strToDueDate = null;

			String strToDueDateMnth = request.getParameter("to_due_date_mnth");
			String strToDueDateDy = request.getParameter("to_due_date_dy");
			String strToDueDateYr = request.getParameter("to_due_date_yr");
			if ((strToDueDateMnth.length() == 0) || (strToDueDateDy.length() == 0) || (strToDueDateYr.length() == 0))
			{
				strToDueDate = "99999999";
			}
			else
			{
				if (strToDueDateMnth.length() == 1)  strToDueDateMnth = "0" + strToDueDateMnth;
				if (strToDueDateDy.length() == 1)  strToDueDateDy = "0" + strToDueDateDy;
				if (strToDueDateYr.length() == 2)  strToDueDateYr = "20" + strToDueDateYr;

				strToDueDate = strToDueDateYr + strToDueDateMnth + strToDueDateDy;
			}

			if ( (!strFromDueDate.equals("0")) || (!strToDueDate.equals("99999999")) )
			{
				strQuery = strQuery +
					" AND ((SUBSTR(REQUEST_T.DUE_DT, 7, 4) || SUBSTR(REQUEST_T.DUE_DT, 1, 2) || SUBSTR(REQUEST_T.DUE_DT, 4, 2)) " +
					"BETWEEN '" + strFromDueDate + "' AND '" + strToDueDate + "')";
			}

			// Build the SUBMITTED DATE (available to LSPAC "P" users only)
			String strSUBMITTEDQuery = "";
			String strSubmittedDate = null;
			String strSubmittedToDate = null;
			String strTempMnth = request.getParameter("sub_from_mnth");
			String strTempDy = request.getParameter("sub_from_dy");
			String strTempYr = request.getParameter("sub_from_yr");
			if ( strTempMnth == null ) strTempMnth="";
			if ( strTempDy == null ) strTempDy="";
			if ( strTempMnth == null ) strTempYr="";

			if ((strTempMnth.length() == 0) || (strTempDy.length() == 0) || (strTempYr.length() == 0))
			{
				strSubmittedDate = "";
			}
			else
			{
				if (strTempMnth.length() == 1)  strTempMnth = "0" + strTempMnth;
				if (strTempDy.length() == 1)  strTempDy = "0" + strTempDy;
				if (strTempYr.length() == 2)  strTempYr = "20" + strTempYr;
				strSubmittedDate = strTempYr + strTempMnth + strTempDy;

				strTempMnth = request.getParameter("sub_to_mnth");
				strTempDy = request.getParameter("sub_to_dy");
				strTempYr = request.getParameter("sub_to_yr");
				if ((strTempMnth.length() == 0) || (strTempDy.length() == 0) || (strTempYr.length() == 0))
				{	strSubmittedToDate = strSubmittedDate;//if bogus, we override
				} else
				{
					if (strTempMnth.length() == 1)  strTempMnth = "0" + strTempMnth;
					if (strTempDy.length() == 1)  strTempDy = "0" + strTempDy;
					if (strTempYr.length() == 2)  strTempYr = "20" + strTempYr;
					strSubmittedToDate = strTempYr + strTempMnth + strTempDy;
				}

				Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() -- SUBMITTED DATE Search ["+
					strSubmittedDate +"-"+strSubmittedToDate+"]");
//HERE HERE
				//Build this
//AND EXISTS (SELECT 1 FROM REQUEST_HISTORY_T RH WHERE RH.RQST_SQNC_NMBR=REQUEST_T.RQST_SQNC_NMBR
//	      AND RH.RQST_VRSN=REQUEST_T.RQST_VRSN AND RH.RQST_STTS_CD_IN='SUBMITTED'
//	      AND TRUNC(RH.RQST_HSTRY_DT_IN)=TRUNC(TO_DATE('<entered date>','YYYYMMDD'))
				strSUBMITTEDQuery = " AND EXISTS (SELECT 1 FROM REQUEST_HISTORY_T RH " +
					" WHERE RH.RQST_SQNC_NMBR=REQUEST_T.RQST_SQNC_NMBR AND RH.RQST_VRSN=REQUEST_T.RQST_VRSN AND " +
					" RH.RQST_STTS_CD_IN='SUBMITTED' AND TRUNC(RH.RQST_HSTRY_DT_IN) >= TRUNC(TO_DATE('" + strSubmittedDate +
					"','YYYYMMDD')) AND TRUNC(RH.RQST_HSTRY_DT_IN) <= TRUNC(TO_DATE('" + strSubmittedToDate + "','YYYYMMDD'))) ";
				strQuery = strQuery + strSUBMITTEDQuery;
				Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() -- SUB qry=[" + strSUBMITTEDQuery + "]");
			}


			String strPon = request.getParameter("pon");
			String strAtn = request.getParameter("atn");

			// Include extended adv searching to Multi orders if a particular PON or ATN is entered AND (Service Type was not entered
			// or All service types wanted or they specified Multi orders)
			if ( bIncludeMulti && ( ((strPon != null) && (strPon.length() > 0)) || ((strAtn != null) && (strAtn.length() > 0)) ) )
			{	bIncludeMulti = true;
				strExtendedQuery = " " + strQuery + " AND (REQUEST_T.SRVC_TYP_CD = '1') "; //pick up only Multi's
			}
			else
				bIncludeMulti = false;

			// PON
			if ((strPon != null) && (strPon.length() > 0))
			{
				strQuery = strQuery + " AND UPPER(REQUEST_T.RQST_PON) LIKE UPPER('" +
					Toolkit.wildCardIt(strPon) + "')";

				if (bIncludeMulti)
				{ 	strExtendedQuery += " AND EXISTS (SELECT 1 FROM RS_MULTI_DETAIL_T RS " +
						" WHERE RS.RQST_SQNC_NMBR=REQUEST_T.RQST_SQNC_NMBR AND RS.RQST_VRSN=REQUEST_T.RQST_VRSN AND " +
						" UPPER(RS.RS_MULTI_DETAIL_PON) LIKE UPPER('" +  Toolkit.wildCardIt(strPon) + "') ) ";
				}
			}

			// ATN
			if ((strAtn != null) && (strAtn.length() > 0))
			{
				strQuery = strQuery + " AND REQUEST_T.ATN LIKE '" +
					Toolkit.wildCardIt(strAtn) + "'";
				if (bIncludeMulti)
				{	strExtendedQuery += " AND EXISTS (SELECT 1 FROM RS_MULTI_DETAIL_T RS " +
						" WHERE RS.RQST_SQNC_NMBR=REQUEST_T.RQST_SQNC_NMBR AND RS.RQST_VRSN=REQUEST_T.RQST_VRSN AND " +
						" UPPER(RS.RS_MULTI_DETAIL_ATN) LIKE '" + Toolkit.wildCardIt(strAtn) + "') ";
				}
			}


			String strTNS = request.getParameter( "tns" );

			if (strTNS != null )
			  if( strTNS.length()  > 0 )
			{
				strQuery  +=  " AND REQUEST_T.RQST_SQNC_NMBR IN  "
					+ " (SELECT distinct  LRCD.RQST_SQNC_NMBR FROM LR_CD_T  LRCD "
					+ " WHERE UPPER(LRCD.LR_CD_TNS) LIKE '" + Toolkit.wildCardIt(strTNS )
					+ "') ";
			}


			// Store Query to Beans
			listBean.setQueryString(strQuery);
			qc.setQueryString(strQuery);

			if (bIncludeMulti)
			{
				qc.setExtendedQueryString(strExtendedQuery);
				Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() -- must include extended advsrch to MULTI orders");
				Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() strExtendedQuery=\n["+strExtendedQuery+"]");
			}else
			{
				qc.setExtendedQueryString("");
				Log.write(Log.DEBUG_VERBOSE, "RequestListCtlr() cleared strExtendedQuery");
			}

			sdm.setRequestQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Forward onto RequestListView
			alltelRequestDispatcher.forward("/RequestListView.jsp");
			return;
		}
		else if (strRqstForm.equals("advanced"))
		{
            Log.write("This is advanced search...");
            Log.write("query criteria :"+qc.getFullQuery());
            
			sdm.setRequestQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the Advanced Search JSP
			alltelRequestDispatcher.forward("/RequestSearchView.jsp");
			return;
		}
		else
		{
            Log.write(Log.DEBUG_VERBOSE, "strUser: "+ strUser);
            if ((strCmpnySqncNmbr.equals("1")) && (strControl.equals("N")))
            {
               String strMdfdDt = null;
			   Calendar cal = Calendar.getInstance();
			   strMdfdDt = new SimpleDateFormat("MM/dd/yyyy").format(cal.getTime());
               qc.setModifiedDateString(" AND (REQUEST_T.MDFD_DT > TO_DATE('" + strMdfdDt + " 23:59:59','MM/DD/YYYY HH24:MI:SS'))");
			}

			sdm.setRequestQueueCriteria(qc);
			request.putSessionDataManager(sdm);

			// Pass the request and response to the JSP

			//NOTE: HD99494 - Users were hitting advanced search upon navigation to RequestList
			//page without waiting for page to be complete. This caused IOException:"Broken Pipe"
			//followed by IllegalStateException. These were causing database connections to be
			//gradually lost and unrecoverable (since we can't guarantee when GC will run).
			try {
				//Log.write("RequestListCtlr: before forward");
				alltelRequestDispatcher.forward("/RequestListView.jsp");
			}
			//other exceptions are not caught
			catch (IOException e) {
				Log.write("RequestListCtlr: IOException caught on forward() ");
			}
			return;
		}
	}

        protected void populateVariables()
		throws Exception
	{
	}
}



/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/RequestListCtlr.java  $
/*
/*   Rev 1.7   Aug 30 2002 08:16:54   sedlak
/*
/*
/*   Rev 1.5   20 Mar 2002 10:51:26   dmartz
/*Correct 'CLOSED' portion of Adv Srch
/*
/*   Rev 1.1   19 Feb 2002 15:11:58   dmartz
/*
/*
/*   Rev 1.0   23 Jan 2002 11:06:28   wwoods
/*Initial Checkin
*/

/* $Revision:   1.7  $
*/
