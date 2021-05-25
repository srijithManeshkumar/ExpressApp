/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	BillDisputeBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Vince Pavill
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	03/15/2003  initial check-in.
 *	5/9/2003    psedlak	calc total amounts on status change
 *	9/9/2003    psedlak rewrote to extend base to reduce duplication of code
 *	2/11/2005   psedlak Reset DTSENT on SUBMITTED
 *
 */

package com.alltel.lsr.common.objects;

import java.sql.*;
import com.alltel.lsr.common.util.*;

public class BillDisputeBean extends ExpressBean {

	//These dictate what to pull from tables        
	private BillDisputeOrder thisOrder = BillDisputeOrder.getInstance();

	public BillDisputeBean() {
		
		super.init(thisOrder);
		Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean: constructor");
	}
	
	public int create(int iOCNSttSqncNmbr)
	{	return this.create(iOCNSttSqncNmbr, 0);
	}

	public int create(int iOCNSttSqncNmbr, int iCmpnySqncNmbr) 
	{

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean : Create New BillDispute");

		int iReturnCode = 0;

		String strOCNCd = "";
		String strSttCd = "";
		//int iCmpnySqncNmbr = 0;

		String strQuery1 = "SELECT OCN_STATE_T.OCN_CD, OCN_STATE_T.STT_CD FROM OCN_STATE_T, OCN_T WHERE OCN_STT_SQNC_NMBR = " + iOCNSttSqncNmbr + " AND OCN_STATE_T.OCN_CD = OCN_T.OCN_CD AND OCN_T.CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;

		try 
		{
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);
			
			if (rs1.next())
			{
				strOCNCd = rs1.getString("OCN_CD");
				strSttCd = rs1.getString("STT_CD");
				//iCmpnySqncNmbr = rs1.getInt("CMPNY_SQNC_NMBR");
				rs1.close();
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "BillDisputeBean : Error finding valid OCN Code and State ");
				iReturnCode = OCN_VALIDATION_ERROR;		
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "BillDisputeBean : DB Exception on Query : " + strQuery1);
			iReturnCode = DB_ERROR;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		// Get the new BillDispute Number
		int iDsptSqncNmbr = 0;
		String strQueryDSN = "SELECT DISPUTE_SEQ.nextval DSPT_SQNC_NMBR_NEW FROM dual";

		try 
		{
			ResultSet rsDSN = m_stmt.executeQuery(strQueryDSN);
			
			rsDSN.next();
			iDsptSqncNmbr = rsDSN.getInt("DSPT_SQNC_NMBR_NEW");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "BillDisputeBean : DB Exception on Query : " + strQueryDSN);
			iReturnCode = DB_ERROR;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		// Insert new row into DISPUTE_T
		String strInsert1 = "";
		try 
		{

			strInsert1 = "INSERT INTO DISPUTE_T VALUES(" + iDsptSqncNmbr + ",0, 'INITIAL', 0, '" + strOCNCd + "', '" +
				strSttCd + "', " + iOCNSttSqncNmbr + ", " + iCmpnySqncNmbr + ", ' ', '" + getUserid() + "', ' ','" + 
			  	getDtsntStamp() + "', " + getTimeStamp() + ", '" + getUserid() + "', 0.00, 0.00,'" +
			  	thisOrder.getSRVC_TYP_CD() + "')" ;
			m_stmt.executeUpdate(strInsert1);

		}
		catch(Exception e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "BillDisputeBean : DB Exception on Insert : " + strInsert1);
			iReturnCode = DB_ERROR;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean : Successful Insert of New Dispute");

		// generate a new History record 
		int iDsptHstrySqncNmbr = super.updateHistory(iDsptSqncNmbr, 0, "INITIAL");
		if (iDsptHstrySqncNmbr <= 0)
		{
			Log.write(Log.ERROR, "BillDisputeBean : Error Generating History for BillDispute Sqnc Nmbr:" + iDsptSqncNmbr);
			return(HISTORY_ERROR);		
		}

		String strUpdate1 = "UPDATE DISPUTE_T SET DSPT_HSTRY_SQNC_NMBR = " + iDsptHstrySqncNmbr + " WHERE DSPT_SQNC_NMBR = " + iDsptSqncNmbr;

		try 
		{
			if (m_stmt.executeUpdate(strUpdate1) <= 0)
			{
				throw new SQLException();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "BillDisputeBean : DB Exception on Update of hist seq: " + strUpdate1);
			iReturnCode = DB_ERROR;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean : DISPUTE_T updated with current History Sequence Number : " + strUpdate1);


		// if we got here, we have a new BillDispute Sequence Number
		// now get the information we need to create all the required forms.
		// We need to loop through SERVICE_TYPE_FORM and create all the INITIAL FORMs 

		String strQuery3 = "SELECT * FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '"+ thisOrder.getSRVC_TYP_CD() +
				   "' AND TYP_IND = '" + thisOrder.getTYP_IND() + "'";
		int i_frms = 0;
		int i_frms_created = 0;
		int iFrmSqncNmbr = 0;
		boolean bFormCreated = false;

		try 
		{
			ResultSet rs3 = m_stmt.executeQuery(strQuery3);

			while (rs3.next())
			{
				i_frms++;

				iFrmSqncNmbr = rs3.getInt("FRM_SQNC_NMBR");

				bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iDsptSqncNmbr, 0);

				if (bFormCreated)
				{
					i_frms_created++;
				}
				else
				{
					Log.write(Log.ERROR, "BillDisputeBean : Error Generating Form for BillDispute Sqnc Nmbr:" + iDsptSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
					iReturnCode = -130;		
				}

			}
			if ((i_frms_created == 0) || (i_frms_created != i_frms))
			{
				Log.write(Log.ERROR, "BillDisputeBean : Error Generating Forms for BillDispute Sqnc Nmbr:" + iDsptSqncNmbr);
				iReturnCode = -135;		
			}

			rs3.close();
		} 
		catch(SQLException e) 
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "BillDisputeBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW DISPUTE FORM CREATION ");
			iReturnCode = DB_ERROR;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}
		
		Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean : All INITIAL Forms Generated for BillDispute Sqnc Nmbr:" + iDsptSqncNmbr); 

		// return the new BillDispute Sequence Number
		return(iDsptSqncNmbr); 

	}

        // Send the autoReply if necessary
        protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
        {
        }

        // Send the provider autoReply if necessary
        protected void sendProvReply(int iSqncNmbr, int iVrsn)
        {
        }

	public int changeStatus(AlltelRequest request, int iDsptSqncNmbr, String strDsptActn)
	{

		int iReturnCode = 0;
		int iHistorySequenceNumber = 0;

		// Call base class method first....
		iReturnCode = super.changeStatus(request, iDsptSqncNmbr, strDsptActn);
		if (iReturnCode < 0)
		{	return iReturnCode;
		}
		iHistorySequenceNumber = iReturnCode;
		iReturnCode = 0;

		//Here we need to calculate the total disputed amount and adjusted amount....
		int iVrsn = 0;
		String strAmountSQL ="";
		if ( getSttsCdTo().equals("SUBMITTED") )
		{
			//Calc the total disputed amount
			Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean.changeStatus(): Calc total disputed amount");
			float fTot=0;
			ResultSet rs=null;
			try {
				rs = m_stmt.executeQuery("SELECT SUM(TO_NUMBER(DSPT_AMNT)) FROM DSPT_DETAIL_T " +
				   " WHERE DSPT_SQNC_NMBR="+ iDsptSqncNmbr + " AND DSPT_VRSN="+iVrsn+" AND LENGTH(DSPT_AMNT) >0 ");
                        	if (rs.next())
                        	{
                                	fTot = rs.getFloat(1);
				}
				rs.close();
				rs=null;
			}
			catch (Exception e) {
				Log.write(Log.ERROR, "BillDisputeBean.changeStatus(): Err calculating total disputed amt seq="+iDsptSqncNmbr);
				e.printStackTrace();
			}
			try {
			strAmountSQL=" TTL_DSPTD_AMNT="+fTot+" , DTSENT=TO_CHAR(sysdate, '"+
					 PropertiesManager.getProperty("lsr.autofill.datefmt", "MM-DD-YYYY-HHMIAM") + "') ";
			}
			catch (Exception e) {
				Log.write(Log.ERROR, "BillDisputeBean.changeStatus(): Err filling autodate seq="+iDsptSqncNmbr);
				e.printStackTrace();
			}
		}
		else if ( getSttsCdTo().equals("RESOLVED") )
		{
			//Calc the creditted amount
			Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean.changeStatus(): Calc total creditted amount");
			float fTot=0;
			ResultSet rs=null;
			try {
				rs = m_stmt.executeQuery("SELECT SUM(TO_NUMBER(DSPT_ADJSTD_AMNT))+SUM(TO_NUMBER(DSPT_ADJSTD_TAXES)) " +
				   " FROM DSPT_RSPNS_DETAIL_T WHERE DSPT_SQNC_NMBR="+ iDsptSqncNmbr + " AND DSPT_VRSN="+
				   iVrsn+" AND (LENGTH(DSPT_ADJSTD_AMNT) >0 OR LENGTH(DSPT_ADJSTD_TAXES) >0)");
                        	if (rs.next())
                        	{
                                	fTot = rs.getFloat(1);
				}
				rs.close();
				rs=null;
			}
			catch (Exception e) {
				Log.write(Log.ERROR, "BillDisputeBean : Err calculating total creditted amt seq="+iDsptSqncNmbr);
				e.printStackTrace();
			}
			strAmountSQL=" TTL_CRDTTD_AMNT="+fTot+" ";
		}
	
		String strUpdate1 = "";
		if (strAmountSQL.length() > 0)
		{
			strUpdate1 = "UPDATE DISPUTE_T SET " + strAmountSQL + " WHERE DSPT_SQNC_NMBR = " + iDsptSqncNmbr;
			try 
			{
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeBean.changeStatus(): strUpdate=["+strUpdate1+"]");
				if (m_stmt.executeUpdate(strUpdate1) <= 0)
				{
					throw new SQLException();
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "BillDisputeBean : DB Exception on Update : " + strUpdate1);
				iReturnCode = DB_ERROR;		
			}
		}
		
		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		//if we got here, we had a successful Status Change and Generated a History Record.
		// Return the History Sequence Number
		return (iHistorySequenceNumber);
	}

        public int storeForm(AlltelRequest request, int iFrmSqncNmbr, int iSqncNmbr, int iVrsn)
        {
		// Call base class method first....
		int iReturnCode = super.storeForm(request, iFrmSqncNmbr, iSqncNmbr, iVrsn);
		if (iReturnCode < 0)
		{	return iReturnCode;
		}

		// Save Credit amount to DISPUTE_T
		float fTot=0;
		ResultSet rs=null;
		try 
		{
			// Calculate the total amount credited
			rs = m_stmt.executeQuery("SELECT SUM(TO_NUMBER(DSPT_ADJSTD_AMNT))+SUM(TO_NUMBER(DSPT_ADJSTD_TAXES))" +
				" FROM DSPT_RSPNS_DETAIL_T WHERE DSPT_SQNC_NMBR="+ iSqncNmbr + " AND DSPT_VRSN="+
				iVrsn+" AND (LENGTH(DSPT_ADJSTD_AMNT) >0 OR LENGTH(DSPT_ADJSTD_TAXES) >0)");

			if (rs.next())
			{
				fTot = rs.getFloat(1);
			}
			rs.close();
			rs=null;

			// Update DISPUTE_T with total amount credited.
			m_stmt.executeUpdate("UPDATE DISPUTE_T SET TTL_CRDTTD_AMNT = " + fTot + " WHERE DSPT_SQNC_NMBR = " + iSqncNmbr + " AND DSPT_VRSN = " + iVrsn);
		}
		catch (Exception e) 
		{
			Log.write(Log.ERROR, "BillDisputeBean : Err calculating/updating total creditted amt seq="+iSqncNmbr);
			e.printStackTrace();
		}

		return(0);
	}
}
