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
 * MODULE:	DslQualCtlr.java
 *
 * DESCRIPTION: 	Idea 1641 project. Replace Targus DSL lookup with ACI DSL qualification
 *			service.
 *
 * AUTHOR:      Paul Sedlak
 *
 * DATE:        9-27-2004
 *
 * HISTORY:
 *
 * 01/10/2008 Steve Korchnak
 * Idea5052   Modified broadband qualification (dsl loop qual) to utilize new
 *            BroadbandProductsQualification (BPQ) tool.
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DslQualCtlr extends AlltelServlet
{
	private String m_strDSLServiceURL = "";

	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{

        	Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr()");

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		String strUSERID = sdm.getUser();
		String strResults = "";

		String strNPANXXLINE = request.getParameter("skey");
		if ((strNPANXXLINE == null) || (strNPANXXLINE.length() != 10))
		{
			Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- no TN");
			strResults = "<font color=red> Telephone number is invalid (must be 10 digits)! </font>";
			if (strNPANXXLINE != null)
			{	request.getHttpRequest().setAttribute("skey", strNPANXXLINE);
			}
			request.getHttpRequest().setAttribute("_DSL_QUAL_RESULTS_", strResults);
			alltelRequestDispatcher.forward("/DSLLookup.jsp");
			return;
		}
		//populate, so when use goes back to page, their last phone nbr is there...
		request.getHttpRequest().setAttribute("skey", strNPANXXLINE);

		String m_strDslAction = request.getParameter("action");
		if ((m_strDslAction == null) || (m_strDslAction.length() == 0))
		{
			Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- no action...default as Submit");
			m_strDslAction="Submit";
			//alltelRequestDispatcher.forward("/DSLLookup.jsp");
			//return;
		}

		if (m_strDslAction.equals("Reset"))
		{
			Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- Reset");
//			request.getHttpRequest().setAttribute("_DSL_QUAL_RESULTS_", strResults);
			request.getHttpRequest().setAttribute("_DSL_QUAL_RESULTS_", "");
			alltelRequestDispatcher.forward("/DSLLookup.jsp");
			return;
		}
		Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- Submit");

		DslBean dslBean = new DslBean();
		dslBean.setUserid(strUSERID);
		int iReturnCode = dslBean.getConnection();
		String strXML = dslBean.buildQualRequestXml(strNPANXXLINE);
		Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- XML=["+strXML+"]");
		if (strXML == null)
		{
			Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- Error building XML for TN="+strNPANXXLINE);
			strResults = "<font color=red>Application Error has occurred while performing DSL qualification. (Code 1)</font>";
			request.getHttpRequest().setAttribute("_DSL_QUAL_RESULTS_", strResults);
			alltelRequestDispatcher.forward("/DSLLookup.jsp");
			return;
		}

		//PostEvent postEvt = new PostEvent();
		//postEvt.setURL(m_strDSLServiceURL);
		//int iReturnCode = postEvt.sendXMLRequestGET(strXML);
		iReturnCode = dslBean.webserviceDSLQualification(strXML);

		if (iReturnCode == 0)
		{
			Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- dslBean.getQualResults()=[" +
					dslBean.getQualResults() +"]");
			strResults = dslBean.dslQualInterpretResponse( dslBean.getQualResults() );
		}
		else if (iReturnCode < 0)
		{
			Log.write(Log.ERROR, "DslQualCtlr() --- Error during XML post");
			strResults = "<font color=red>Connectivity Error has occurred while performing DSL qualification. (Code 2)</font>";

//TEMP
/*
String strBogus ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                        "<DSLQualificationResponse>"+
			"<resultList/>"+
			"<otherInfo>"+
				"<serviceDisposition>COMPLETED_OK</serviceDisposition>"+
				"<mirorInfo><transactionID>sun30123</transactionID><oid>OID0000000000</oid>" +
				"<messageType>DSLLOC</messageType>" +
				"<userID>DSL_GUY</userID>" +
				"<timeStamp>20040927131313</timeStamp>" +
				"<orderType>order1</orderType>"+
				"<statusCode>Qualified</statusCode><statusDescription>Good News --Fake Qualified</statusDescription>"+
				"<transactionType>D</transactionType>"+
				"</mirorInfo>"+
			"</otherInfo>" +
                        "</DSLQualificationResponse>";
*/
		}
		// store response results in db.
		dslBean.dbInsert();
		dslBean.closeConnection();
		request.getHttpRequest().setAttribute("_DSL_QUAL_RESULTS_", strResults);
		alltelRequestDispatcher.forward("/DSLLookup.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
		m_strDSLServiceURL = PropertiesManager.getProperty("lsr.bpqlookup.url", "");
		Log.write(Log.DEBUG_VERBOSE, "DslQualCtlr() --- ServiceURL=["+ m_strDSLServiceURL + "]");
	}
}

