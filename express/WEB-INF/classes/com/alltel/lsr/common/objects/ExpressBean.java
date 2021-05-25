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
 * MODULE:	ExpressBean.java
 *
 * DESCRIPTION: Base class for 'order type' objects in Express -ie Reqeusts, Preorders, Trbl Tickets....
 *		to reduce duplication of code -and multiple maintenance.
 *
 * AUTHOR:      pjs
 *
 * DATE:        9.9.2003
 *
 * HISTORY:
 *  02/11/2008 HD0000002472840 Steve Korchnak uncommented Log.write line to display field validation info (VERBOSE)
 *
 *	pjs 4/6/04	"D" auto populate if src tbl/col populated only
 *	pjs 6/1/2004	setNotifyInd() method added
 *	pjs 12/23/04 Allow email to provider to reference a list -works w/ EMAIL_DIST_T entry
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

//added for simpleportflag update
import com.automation.dao.LSRdao;

public abstract class ExpressBean {

	//These dictate what to pull from tables
	private	ExpressOrder myOrder;
	private FormBean formBean;

	private String m_strUserid = "";
	private String m_strCmpnyTyp = "";

	protected Connection m_conn = null;
	protected Statement m_stmt = null;

	private boolean m_bNoteNeeded = false;
	private String m_strTimeStamp = "" ;
	private String m_strDtsntStamp = "" ;
	private String m_strMdfdDt = "";
	private String m_strReplyMdfdDt = "";

	private	String m_strSttsCdTo = "";
	private	String m_strSttsCdFrom = "";
//	private	String m_strTypInd = "";	//this is part of ExpressOrder and unchangable
	private	String m_strSrvcTypCd= "";	//this can change
	private	String m_strRqstTypCd = "";
	private String m_strPrdctTypCd  = "0";
	private int m_PrevSqncNmbr;
	private int m_iVrsn;			//used by changeStatus() only
	private boolean m_bAccessOk;
        //added boolean field to set for first Ported Number found in NP form - Antony - 06/30/2011
        boolean firstPtdNbrFound = false;

	public ExpressBean()
	{
	}

	/**
	* Init class
	* @param	ExpressOrder	Order type
	* @return 	void
	*/
	public void init(ExpressOrder expressOrder)
	{
		this.myOrder = expressOrder;

		setSrvcTypCd( myOrder.getSRVC_TYP_CD() );	//default Service Type

		//get a FormBean to use for all operations on Forms
		this.formBean = new FormBean(myOrder);
		m_PrevSqncNmbr=-1;
		m_bAccessOk=false;
		m_iVrsn = 0;
		m_strPrdctTypCd  = "0";
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean() type = " + expressOrder);
	}

	/**
	* Get connection from DB pool
	* @return 	int
	*/
	public int getConnection()
	{
		int iReturnCode = 0;
		Log.write(Log.DEBUG_VERBOSE, "** getConnection() ");
		try
		{
			this.m_conn = DatabaseManager.getConnection();
			this.m_stmt = m_conn.createStatement();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean :  ERROR getting database connection ");
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		this.formBean.setFormConnection(this.m_conn);

		return (iReturnCode);
	}

	/**
	* Set userid of  Express user
	* @param	String 	Userid
	* @return 	void
	*/
	public void setUserid(String strUserid)
	{
		this.m_strUserid = strUserid;
		this.formBean.setFormUserid(strUserid);
	}

	/**
	* Init class
	* @param	ExpressOrder	Order type
	* @return 	void
	*/
	public String getCmpnyTyp()
	{
		// If already found, don't do it again
		if (m_strCmpnyTyp.length() > 0)
		{
			return m_strCmpnyTyp;
		}

		// obtain Company Type for this userid
		String strQuery = "SELECT CMPNY_TYP FROM COMPANY_T C, USERID_T I" +
			" WHERE C.CMPNY_SQNC_NMBR = I.CMPNY_SQNC_NMBR " +
			" AND USERID = '" + m_strUserid + "'";

		try
		{
			ResultSet rs = m_stmt.executeQuery(strQuery);
			rs.next();
			this.m_strCmpnyTyp = rs.getString("CMPNY_TYP");
			rs.close();
			rs=null;
		}
		catch(SQLException e)
		{
			this.m_strCmpnyTyp = "";
			Log.write(Log.ERROR, "ExpressBean : No Company Type for userid: " + m_strUserid);
		}

		return m_strCmpnyTyp;
	} // end getCmpnyTyp()

	public String getPrdctTypCd()
	{
		return m_strPrdctTypCd;
	}
	public int beginTransaction()
	{
		int iReturnCode = 0;

		Log.write(Log.DEBUG_VERBOSE, "** beginTransaction() ");

		// get a default Date to use for transaction synchronization
		String strQueryTS = "SELECT TO_CHAR(sysdate,'MM/DD/YYYY HH24:MI:SS') THE_TIMESTAMP, " +
				    "TO_CHAR(sysdate,'MM-DD-YYYY-HHMIAM') DTSNT_TIMESTAMP FROM dual";
		//Log.write(Log.DEBUG_VERBOSE, "** beginTransaction() ["+strQueryTS+"]");
		try
		{
			ResultSet rsTS = m_stmt.executeQuery(strQueryTS);
			if (rsTS.next())
			{

				this.m_strTimeStamp = "TO_DATE('" + rsTS.getString("THE_TIMESTAMP") + "' ,'MM/DD/YYYY HH24:MI:SS')";
				this.formBean.setFormTimeStamp(this.m_strTimeStamp);
				this.m_strMdfdDt = m_strTimeStamp;

				this.m_strReplyMdfdDt = rsTS.getString("THE_TIMESTAMP");

				//  get date sent  ///////////////////////////////////////////
				this.m_strDtsntStamp = rsTS.getString("DTSNT_TIMESTAMP");
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : Error getting Transaction TimeStamp ");
				iReturnCode = DATETIMESTAMP_ERROR;
			}
			rsTS.close();
			rsTS=null;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + strQueryTS);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{	return iReturnCode;
		}
		Log.write(Log.DEBUG_VERBOSE, "** beginTransaction() this.m_strTimeStamp="+this.m_strTimeStamp+" this.m_strDtsntStamp="+this.m_strDtsntStamp);
		try
		{
			m_conn.setAutoCommit(false);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception when setting Auto Commit.");
			iReturnCode = DB_ERROR;
		}

		return (iReturnCode);

	} //end beginTransaction()

	public int beginTransaction(int iSqncNmbr, String strMdfdDt)
	{
		int iReturnCode = 0;
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean --- Begin Transaction with existing order ");

		iReturnCode = beginTransaction();

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		String strLstMdfdDt = "";
		String strQuery1 = "SELECT TO_CHAR(MDFD_DT,'MM/DD/YYYY HH24:MI:SS') MDFD_DT " +
			" FROM " + myOrder.getTBL_NAME() + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		try
		{
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);
			if (rs1.next())
			{
				strLstMdfdDt = rs1.getString("MDFD_DT");
			}
			else
			{
				rollbackTransaction();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : Error finding Order ");
				iReturnCode = -140;
			}
			rs1.close();
			rs1=null;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + strQuery1);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		if (!strMdfdDt.equals(strLstMdfdDt))
		{
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			//Log.write(Log.DEBUG_VERBOSE, "ExpressBean:BeginTransaction() - make sure we dont lose connection here...");
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean : Mdfd DT not equal Last Mdfd DT, Transaction not Allowed");
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean : user probably used 'back' button and hit action button");
			iReturnCode = MDFD_MISMATCH;
			return (iReturnCode);
		}

		String strUpdate2 = "";
		if (getCmpnyTyp().equals("P"))
		{
			strUpdate2 = "UPDATE " + myOrder.getTBL_NAME() + " SET LST_MDFD_PRVDR = '" + m_strUserid + "', MDFD_DT = " +
			 m_strTimeStamp + ", MDFD_USERID = '" + m_strUserid + "' WHERE " +
			myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		}
		else
		{
			strUpdate2 = "UPDATE " + myOrder.getTBL_NAME() + " SET LST_MDFD_CSTMR = '" + m_strUserid + "', MDFD_DT = " +
			 m_strTimeStamp + ", MDFD_USERID = '" + m_strUserid + "' WHERE " +
			myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		}

		try
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.BeginTransaction() UPDATE=["+strUpdate2+"]");
			if (m_stmt.executeUpdate(strUpdate2) <= 0)
			{
				throw new SQLException();
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Update of lst-mdfd: " + strUpdate2);
			iReturnCode = DB_ERROR;
		}
		return (iReturnCode);

	} // end  beginTransaction(int iSqncNmbr, String strMdfdDt)

	public int commitTransaction()
	{
		int iReturnCode = 0;
		Log.write(Log.DEBUG_VERBOSE, "** commitTransaction() ");
		try
		{
			m_conn.commit();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception when performing a Commit.");
			iReturnCode = DB_ERROR;
		}

		return(iReturnCode);
	}  // end commitTransaction()

	public int rollbackTransaction()
	{
		int iReturnCode = 0;
		try
		{
			Log.write(Log.DEBUG_VERBOSE, "** ExpressBean.rollback()");
                        //block added by Antony - 06142010 - to check if conn is open and close only if open
                        if(!m_conn.isClosed())
                            m_conn.rollback();
                        else
                            Log.write(Log.DEBUG_VERBOSE, "** ExpressBean.rollback(): Connection already closed !");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception when performing a Rollback.");
			iReturnCode = DB_ERROR;
		}

		return(iReturnCode);
	} // end rollbackTransaction()

	public int closeConnection()
	{
		Log.write(Log.DEBUG_VERBOSE, "** closeTransaction() ");
		if( this.m_conn != null ) {
			DatabaseManager.releaseConnection(this.m_conn);
		}
		return(0);
	} // end closeConnection()

        /**
        * Abstract method to force subsclasses to create their own create() method
	* @param	int 		some seq number
        * @return	int
        */
        protected abstract int create(int i);


	/**
	* Yank request data from httprequest variables and store Form data.
	* Also trickle updates elsewhere...and record a detailed history of what changed.
	*
	* @param	AlltelRequest	HTTP Request
	* @param	int 		Form Sequence number we are storing
	* @param	int 		Order sequence number
	* @param	int 		Order version number
	* @return int
	*/
	public int storeForm(AlltelRequest request, int iFrmSqncNmbr, int iSqncNmbr, int iVrsn)
	{

		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm() Save data posted " + myOrder.getTYP_IND()+
				iSqncNmbr+":"+iVrsn);

		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		Vector vHistoryOfChanges = new Vector();	//this holds our VALUE clause for INSERTS for history log

		String strMdfdDt = "";

		Vector vFrmFld = new Vector();

		//See if we should record chg details -(this is set in i_xxxx.jsp as a HIDDEN field)
		String strRecHistYorN = request.getParameter("REC_HST");
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm() Save history ==" + strRecHistYorN);
		if (strRecHistYorN == null) {
			strRecHistYorN = "Y";	//if we don't know, record it.....
		}
		else {
			strRecHistYorN = strRecHistYorN.toUpperCase();
		}

		try
		{
			vFrmFld = formBean.getFormFields(iFrmSqncNmbr, iSqncNmbr, iVrsn);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.storeForm(): DB Exception in formBean.getFormFields().");
			return(DB_ERROR);
		}

		FormField ff;
		String str_ff_ParameterName = "";
		String str_ff_ParameterValue = "";
		String strUpdtData = "";
		String strUpdate1 = "";
		int ff_idx;

		//loop through the vector and get all the fields from the posted parameters
		boolean isNew = false;
		String strNewRec = request.getParameter("NEWRECORD");
		if( strNewRec != null )
		{
			isNew = strNewRec.equals("1") ? true: false;

		}
		for(ff_idx = 0; ff_idx < vFrmFld.size(); ff_idx++)
		{
			ff = (FormField)vFrmFld.elementAt(ff_idx);
			str_ff_ParameterName = "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldNmbr();

			//find the form field in the posted parameters
			// if it is not there it will be null (unless a CHECKBOX);
			// if empty it will be an empty string ;
			str_ff_ParameterValue = request.getParameter(str_ff_ParameterName);

			if ( (str_ff_ParameterValue == null) && (ff.getFldDsplyTyp().equals("CHECKBOX")) )
			{	//checkbox was unchecked!
				str_ff_ParameterValue="";
			}

			if (str_ff_ParameterValue != null)
			{
				String strNoSpace = str_ff_ParameterValue.trim();
				str_ff_ParameterValue = strNoSpace;

				// found the form field ; get the value and update the vector
				// also update any fields on other Forms or Tables based on the SRC indicator

				// If data changed, then turn on flag
				String strOldData = ff.getFieldData();
				if (strOldData == null) {
					strOldData="";
				}

				if ( strOldData.equals(str_ff_ParameterValue) && !isNew ) {}
				else {
Log.write(Log.DEBUG_VERBOSE, "HISTORY: "+str_ff_ParameterName+" was ["+strOldData+"] New["+ str_ff_ParameterValue + "]");
					ff.setFieldDataChanged();	//turn on change indicator

					vHistoryOfChanges.addElement(", "  + vHistoryOfChanges.size() +
						", " + iFrmSqncNmbr + ", " + ff.getFrmSctnSqncNmbr() + ", " + ff.getFrmSctnOcc() +
						", '" + ff.getFrmFldNmbr() + "', '" + Toolkit.replaceSingleQwithDoubleQ(strOldData) +
						"', '" + Toolkit.replaceSingleQwithDoubleQ(str_ff_ParameterValue) + "') ");

				}
				ff.setFieldData(str_ff_ParameterValue);
				vFrmFld.set(ff_idx, ff);

				if (ff.getSrcInd().equals("U") && (ff.didFieldDataChange()))
				{
					strUpdtData = str_ff_ParameterValue;
					if ((strUpdtData == null) || (strUpdtData.length() == 0))
					{
						strUpdtData = " ";
					}

					strUpdate1 = "UPDATE " + ff.getSrcDbTblNm() + " SET " + ff.getSrcDbClmnNm() + " = '" +
						 Toolkit.replaceSingleQwithDoubleQ(strUpdtData) + "', MDFD_DT = " + m_strTimeStamp +
						 ", MDFD_USERID = '" + m_strUserid + "' WHERE " + myOrder.getSQNC_COLUMN() + " = " +
						 iSqncNmbr + " AND " + myOrder.getVRSN_COLUMN() + " = " + iVrsn;
					Log.write(Log.DEBUG_VERBOSE, "UPDATE=["+strUpdate1+"]");
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
						Log.write(Log.ERROR, "ExpressBean.storeForm(): Exception on Update:" + strUpdate1);
						iReturnCode = DB_ERROR;
					}

					if (iReturnCode != 0)
					{
						return (iReturnCode);
					}
				}

				//If "D", then field is autofilled with date/time. If SrcDbTblNm and SrcDbClmnNm are filled, then
                                // also propagate field value to that table/column.
                                if (ff.getSrcInd().equals("D"))
                                {
                                        strUpdtData = str_ff_ParameterValue;
                                        if ( (ff.getSrcDbTblNm() == null) || (ff.getSrcDbClmnNm() == null) )
					{}
                                        else if ( ff.getSrcDbTblNm().length() > 0 && ff.getSrcDbClmnNm().length() > 0 )
                                        {       //Put value into these destinations too
                                                Log.write(Log.DEBUG_VERBOSE, "ExpressBean : Trickle AutoFill date to other sources");
                                                strUpdate1 = "UPDATE " + ff.getSrcDbTblNm() + " SET " + ff.getSrcDbClmnNm() + " = '" +
							Toolkit.replaceSingleQwithDoubleQ(strUpdtData) + "', MDFD_DT = " + m_strTimeStamp +
							", MDFD_USERID = '" + m_strUserid + "' WHERE " +
							myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr + " AND " + myOrder.getVRSN_COLUMN() +
							" = " + iVrsn;
Log.write(Log.DEBUG_VERBOSE, "UPDATE datatype='D'=["+strUpdate1+"]");
                                                try
                                                {       if (m_stmt.executeUpdate(strUpdate1) <= 0)
                                                        {
                                                                throw new SQLException();
                                                        }
                                                }
                                                catch(SQLException e)
                                                {       e.printStackTrace();
                                                        Log.write(Log.ERROR, "ExpressBean : DB Exception on Update2: " + strUpdate1);
                                                        iReturnCode = DB_ERROR;
                                                }
                                                if (iReturnCode != 0)
                                                {       return (iReturnCode);
                                                }
                                        }
                                } //"D"

			}
			else
			{
				//probably need an error here since one of the form field names wasn't found
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm(): Field not found in parameter list: " + "_FF_" + ff.getFrmSctnSqncNmbr() + "_" + ff.getFrmSctnOcc() + "_" + ff.getFrmFldSrtSqnc() + "_" + ff.getFrmFldNmbr() );
			}
		}

		formBean.setStoreFormVrsn(iVrsn);

		boolean bFrmStored;

		try
		{
			bFrmStored = formBean.storeForm(vFrmFld);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.storeForm(): DB Exception in formBean.storeForm().");
			return(DB_ERROR);
		}

		if (bFrmStored)
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm(): Save Successful");
			// handle telling the user we saved the form successfully
		}
		else
		{
			Log.write(Log.ERROR, "ExpressBean.storeForm(): Error Saving Form");
			// handle telling the user we had an error !!!
			return (-150);
		}

                String strStatus = request.getParameter("Frm_Status"); // Vijay - 20-Feb-12
		// Now update ORDER History based on what they changed
		if ( (vHistoryOfChanges.size() > 0) && (strRecHistYorN.equals("Y")) && (iFrmSqncNmbr != 850) )
		{
			int iSeq = 0;
			String strHistUpdate="";
                        String strDwoHistUpdate = "";
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm(): about to record history on " + iSqncNmbr );
			try {

	                        ResultSet rs = m_stmt.executeQuery("SELECT ORDER_DETAIL_HISTORY_SEQ.NEXTVAL MYSEQ FROM DUAL");
				rs.next();
				iSeq = rs.getInt("MYSEQ");
				strHistUpdate = "INSERT INTO ORDER_HISTORY_T VALUES (" + iSeq + ", '" + getTypInd() + "', " +
					iSqncNmbr + "," + iVrsn + "," + getMdfdDt() + ", '" + m_strUserid + "') ";
				m_stmt.executeUpdate(strHistUpdate);
                           
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.storeForm(): Detail history Ok seq="+iSeq);
				for(ff_idx = 0; ff_idx < vHistoryOfChanges.size(); ff_idx++)
				{
					strHistUpdate = "INSERT INTO ORDER_DETAIL_HISTORY_T VALUES (" + iSeq +
						 (String)vHistoryOfChanges.elementAt(ff_idx);

					m_stmt.executeUpdate( strHistUpdate );
				}

                                // Vijay - 20-Feb-12
                                 strDwoHistUpdate = "INSERT INTO DWO_ORDER_T VALUES (" + iSeq + ", '" + getTypInd() + "', " +
					iSqncNmbr + "," + iVrsn + "," + getMdfdDt() + ", '" + m_strUserid + "', '" + strStatus + "') "; 
				m_stmt.executeUpdate(strDwoHistUpdate);

                             

			}
                        catch(SQLException e)
                        {       e.printStackTrace();
				rollbackTransaction();
                        	Log.write(Log.ERROR, "PreorderBean : DB Exception on History logging failed qry["+strHistUpdate+"]");
                        	Log.write(Log.ERROR, "PreorderBean : DB Exception ["+e+"]");
                        	iReturnCode =  DB_ERROR;
                        }
		}


		return (iReturnCode);

	}  // end storeForm(AlltelRequest request, int iFrmSqncNmbr, int iSqncNmbr, int iVrsn)


	public int changeStatus(AlltelRequest request, int iSqncNmbr, String strActn)
	{

		int iReturnCode = 0;
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.changeStatus() --- ");
		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";

		// Get the Status Code we need to change the <order> to based on the Action Code we recieved.
		// Also, get the Current Version and the Indicator that will tell us if we need a new version.

		String strQuery1 = "SELECT A.STTS_CD_TO, A.STTS_CD_FROM, A.TYP_IND, A.RQST_TYP_CD, "+
			" A.ACTN, A.ACTN_VRSN_IND, A.ACTN_SND_CUST_RPLY, A.ACTN_SND_PROV_RPLY, B." +
			myOrder.getAttribute("VRSN_COLUMN") +
			" AS THE_VRSN, B.LST_MDFD_CSTMR FROM " + myOrder.getTBL_NAME() + " B, ACTION_T  A " +
			" WHERE B." + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr + " AND A.STTS_CD_FROM ="+
			" B." + myOrder.getAttribute("STTS_COLUMN") +
			"  AND A.TYP_IND = '" + myOrder.getTYP_IND() + "' AND A.ACTN = '" + strActn + "'" +
			" AND A.PRDCT_TYP_CD = '" +  m_strPrdctTypCd + "'" ;


		String strSttsCd = "";
		String strActnVrsnInd = "";
		boolean bSendEmail = false;
		boolean bSendProvEmail = false;
		String strEmailRcpt = "";
		String strEmailDistList = null;
		int iVrsn = 0;

		try
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.changeStatus(): strQuery1=["+strQuery1+"]");
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);
			if (rs1.next())
			{
				strSttsCd = rs1.getString("STTS_CD_TO");
				iVrsn = rs1.getInt("THE_VRSN");
				strEmailRcpt = rs1.getString("LST_MDFD_CSTMR");
                                m_strSttsCdTo = strSttsCd;
                                m_strSttsCdFrom = rs1.getString("STTS_CD_FROM");
                                //m_strTypInd = rs1.getString("TYP_IND");
                                m_strRqstTypCd = rs1.getString("RQST_TYP_CD");
				strActnVrsnInd = rs1.getString("ACTN_VRSN_IND");
				String strSendReply = rs1.getString("ACTN_SND_CUST_RPLY");
				if (strSendReply.toUpperCase().equals("Y")) {
					bSendEmail = true;
				}
				String strSendProvReply = rs1.getString("ACTN_SND_PROV_RPLY");
				// If "Y" or contains an email dist list from EMAIL_DIST_T
				if ((strSendProvReply.toUpperCase().equals("N")) && (strSendProvReply.length() == 1))
				{}
				else if ((strSendProvReply.toUpperCase().equals("Y")) && (strSendProvReply.length() == 1))
				{	bSendProvEmail = true;
				}
				else if ((!strSendProvReply.toUpperCase().equals("Y")) && (strSendProvReply.length() > 1))
				{	bSendProvEmail = true;
					strEmailDistList = strSendProvReply;
					Log.write(Log.DEBUG_VERBOSE, "ExpressBean.changeStatus(): send email to list["+ strEmailDistList + "]");
				}
				rs1.close();
				rs1=null;
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.changeStatus(): Action Selected is not Allowed ");
				iReturnCode = -155;
			}
		}
		catch(SQLException e)
		{
			Log.write(Log.ERROR, "ExpressBean.changeStatus(): SQLException e=[" + e + "]");
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + strQuery1);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}
		m_iVrsn = iVrsn;

		int iFldVldtnErrs = validateFields(request, iSqncNmbr, iVrsn, "V", strSttsCd);
		if (iFldVldtnErrs != 0)
		{
			Log.write(Log.DEBUG_VERBOSE,"ExpressBean.changeStatus(): Cannot perform status chg becuz validation errors.");
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			iReturnCode = VALIDATION_ERROR;
			return (iReturnCode);
		}

		// See if this status change requires us to create a new version of the order
        if (strActnVrsnInd.equals("Y"))
        {
			Log.write(Log.DEBUG_VERBOSE,"ExpressBean.changeStatus(): Curr Vers="+iVrsn+" New Version REQD");
			m_iVrsn = createVersion(iSqncNmbr, iVrsn);
			if (m_iVrsn <= 0)
			{
				Log.write(Log.ERROR, "ExpressBean.changeStatus(): Error Generating new Version for :" + iSqncNmbr);
				iReturnCode = m_iVrsn;
				m_iVrsn = iVrsn; //restore old value
			}
			if (iReturnCode != 0) {
				return (iReturnCode);
			}
                }

		// generate a new History record
		int iHstrySqncNmbr = updateHistory(iSqncNmbr, iVrsn, strSttsCd);
		if (iHstrySqncNmbr <= 0)
		{
			Log.write(Log.ERROR, "ExpressBean.changeStatus(): Error Generating History for Sqnc Nmbr:" + iSqncNmbr);
			iReturnCode = -165;
		}
		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		String strUpdate1 = "UPDATE " + myOrder.getTBL_NAME() + " SET " + myOrder.getAttribute("STTS_COLUMN") + " = '" +
			strSttsCd + "', " + myOrder.getAttribute("HSTRY_COLUMN") + " = " + iHstrySqncNmbr + ", " +
			myOrder.getVRSN_COLUMN() + " = " + iVrsn + ", MDFD_DT = " + m_strTimeStamp +
			", MDFD_USERID = '" + m_strUserid + "' " ;
		if (getCmpnyTyp().equals("P")) {
			 strUpdate1 += ", LST_MDFD_PRVDR = '" + m_strUserid + "' ";
		}
		else {
			 strUpdate1 += ", LST_MDFD_CSTMR = '" + m_strUserid + "' ";
		}
		strUpdate1 += " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;

		try
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.changeStatus():strUpdate=["+strUpdate1+"]");
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
			Log.write(Log.ERROR, "ExpressBean.changeStatus(): DB Exception on Update : " + strUpdate1);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		if (bSendEmail)
		{
			this.sendReply(iSqncNmbr, iVrsn, strEmailRcpt);
		}

		if (bSendProvEmail)
		{
			if ( strEmailDistList == null )
			{	this.sendProvReply(iSqncNmbr, iVrsn);
			}
			else
			{	this.sendProvReply(iSqncNmbr, iVrsn, strEmailDistList);
			}
		}

		//if we got here, we had a successful Status Change and Generated a History Record.
		// Return the History Sequence Number

		return (iHstrySqncNmbr);

	} // end changeStatus()

        /**
        * method must be overidden by subclasses
        * @author    psedlak
        * @exception Exception
        */
        protected abstract void sendReply(int iSqncNmbr, int iVrsn, String strUserID);

        /**
        * method must be overidden by subclasses
        * @author    psedlak
        * @exception Exception
        */
        protected abstract void sendProvReply(int iSqncNmbr, int iVrsn);
        protected void sendProvReply(int iSqncNmbr, int iVrsn, String strEmailDistList)
	{
	}


	protected int createVersion(int iSqncNmbr,int iCurrentVer)
	{
		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";
		int	iOldVersion = iCurrentVer;
		int	iNewVersion = 0;
		String strSrvcTypCd = "";

		// Get the current version number
		String strQuery1 = "SELECT SRVC_TYP_CD FROM " + myOrder.getTBL_NAME() +
			" WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		//Log.write(Log.DEBUG_VERBOSE, "ExpressBean.createVersion() strQuery1=["+strQuery1+"]");
		try
		{
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);
			if (rs1.next())
			{
				strSrvcTypCd = rs1.getString("SRVC_TYP_CD");
				rs1.close();
			}
			else
			{
				rollbackTransaction();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean.createVersion() : Error finding current Version for SqncNmbr:" +
					 iSqncNmbr );
				iReturnCode = VERSION_ERROR;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.createVersion() : DB Exception on Query : " + strQuery1);
			iReturnCode = VERSION_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		// Increment the Version by 1 and then create the FORMs as required

		iNewVersion = iOldVersion + 1;
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.createVersion(): new = ["+iNewVersion+"]");
		String strQuery2 = "SELECT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '" + strSrvcTypCd +
			 "' AND TYP_IND = '" +  myOrder.getTYP_IND() + "'";

		int i_frms = 0;
		int i_frms_created = 0;
		int iFrmSqncNmbr = 0;
		boolean bFormCreated = false;
		Vector m_vFrmFld = new Vector();

		try
		{
			ResultSet rs2 = m_stmt.executeQuery(strQuery2);
			while (rs2.next())
			{
				i_frms++;
				iFrmSqncNmbr = rs2.getInt("FRM_SQNC_NMBR");
//				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.createVersion(): form="+iFrmSqncNmbr+
//					" newver="+iNewVersion);
				// Get the data from the current version of the form
				m_vFrmFld = formBean.getFormFields(iFrmSqncNmbr, iSqncNmbr, iOldVersion);
				bFormCreated = formBean.generateNewForm(iFrmSqncNmbr, iSqncNmbr, iNewVersion);

				if (bFormCreated)
				{
					// populate the FORM data from the old version to the new version
					formBean.setStoreFormVrsn(iNewVersion);
					boolean bFrmStored = formBean.storeForm(m_vFrmFld, true);
					if (bFrmStored)
					{
						i_frms_created++;
					}
					else
					{
						Log.write(Log.ERROR, "ExpressBean.createVersion() --- Error Saving Form Data " +
							"for new Version ; sqncnmbr = " + iSqncNmbr + " ; formsqncnmbr = " +
							iFrmSqncNmbr + " ; vrsn = " + iNewVersion);
						iReturnCode = VERSION_ERROR;	// handle telling the user we had an error !!!
					}
				}
				else
				{
					Log.write(Log.ERROR, "ExpressBean.createVersion(): Error Generating Form for Sqnc Nmbr:" +
						iSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr + " ; Version = " + iNewVersion);
					iReturnCode = VERSION_ERROR;
				}

			}
			if ((i_frms_created == 0) || (i_frms_created != i_frms))
			{
				Log.write(Log.ERROR, "ExpressBean.createVersion(): Error Generating Forms for Sqnc Nmbr:" +
					iSqncNmbr + " ; Version: " + iNewVersion);
				iReturnCode = VERSION_ERROR;
			}
			rs2.close();
			rs2=null;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.createVersion():  ERROR PERFORMING DATABASE ACTIVITY FOR NEW VERSION " +
				iNewVersion + " FORM CREATION ");
			iReturnCode = VERSION_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		// return the new Request Version Number

		return(iNewVersion);
	}


	public int updateNotes(int iSqncNmbr, String strNotes)
	{
		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";

		String strInsert1 = "INSERT INTO " +  myOrder.getAttribute("NOTES_TBL_NAME") +
			 " VALUES(" + iSqncNmbr + ",'" + Toolkit.replaceSingleQwithDoubleQ(strNotes) + "', " +
			 m_strTimeStamp + ", '" + m_strUserid + "')";

		try
		{
			m_stmt.executeUpdate(strInsert1);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.updateNotes(): DB Exception on Insert : " + strInsert1);
			iReturnCode = DB_ERROR;
		}

		return (iReturnCode);

	}// end updateNotes()


	public Vector getFormFields(int iFrmSqncNmbr, int iSqncNmbr, int iVrsn)
	{
		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (new Vector());
		}
Log.write(Log.DEBUG_VERBOSE," pjs ExpressBean.getFormFields():  in ");
		String strMdfdDt = "";
		String strQuery1 = "SELECT TO_CHAR(MDFD_DT,'MM/DD/YYYY HH24:MI:SS') MDFD_DT " +
			" FROM " + myOrder.getTBL_NAME() + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;

		try
		{
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);

			if (rs1.next())
			{
				strMdfdDt = rs1.getString("MDFD_DT");

				rs1.close();
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : Error finding current History Sequence Number ");
				iReturnCode = -170;
			}

		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + strQuery1);
			iReturnCode = DB_ERROR;
		}
Log.write(Log.DEBUG_VERBOSE," pjs ExpressBean.getFormFields():  1");
Log.write(Log.DEBUG_VERBOSE," pjs ExpressBean.getFormFields():  iFrmSqncNmbr="+ iFrmSqncNmbr+ " iSqncNmbr="+iSqncNmbr+" iVrsn="+iVrsn);

		if (iReturnCode != 0)
		{
			return (new Vector());
		}
		this.m_strMdfdDt = strMdfdDt;

		Vector tmpFormFields = new Vector();

		try
		{
			tmpFormFields = formBean.getFormFields(iFrmSqncNmbr, iSqncNmbr, iVrsn);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception in formBean.getFormFields().");
			return (new Vector());
		}
Log.write(Log.DEBUG_VERBOSE," pjs ExpressBean.getFormFields():  out ");

		return (tmpFormFields);

	} // end getFormFields()

	public int generateSection(int iFrmSqncNmbr, int iSqncNmbr, int iVrsn, int iFrmSctnSqncNmbr)
	{
		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";

		int iFrmSctnSqncNew;

		try
		{
			iFrmSctnSqncNew = formBean.generateSection(iFrmSqncNmbr, iSqncNmbr, iVrsn, iFrmSctnSqncNmbr);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception in formBean.generateSection().");
			return(DB_ERROR);
		}

		if (iFrmSctnSqncNew <= 0)
		{
			iReturnCode = -225;
			return(iReturnCode);
		}

		return(iFrmSctnSqncNew);

	} //end generateSection()

	public int deleteSection(int iFrmSqncNmbr, int iSqncNmbr, int iVrsn, int iFrmSctnSqncNmbr, int iSctnOcc)
	{

		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";

		try
		{
			if (formBean.deleteSection(iFrmSqncNmbr, iSqncNmbr, iVrsn, iFrmSctnSqncNmbr, iSctnOcc) == false)
			{
				iReturnCode = SECTION_ERROR;
			}
			int iSeq = 0;
			String strHistUpdate="";
                        Log.write(Log.DEBUG_VERBOSE, "ExpressBean.deleteSection(): about to record history on " + iSqncNmbr );
                        try {
                                ResultSet rs = m_stmt.executeQuery("SELECT ORDER_DETAIL_HISTORY_SEQ.NEXTVAL MYSEQ FROM DUAL");
                                rs.next();
                                iSeq = rs.getInt("MYSEQ");
                                strHistUpdate = "INSERT INTO ORDER_HISTORY_T VALUES (" + iSeq + ", '" + getTypInd() + "', " +
                                        iSqncNmbr + "," + iVrsn + "," + getMdfdDt() + ", '" + m_strUserid + "') ";
                                m_stmt.executeUpdate(strHistUpdate);
                                Log.write(Log.DEBUG_VERBOSE, "ExpressBean.deleteSection(): Detail history Ok seq="+iSeq);
                                strHistUpdate = "INSERT INTO ORDER_DETAIL_HISTORY_T VALUES (" + iSeq + ", 1, " + iFrmSqncNmbr +
					", " + iFrmSctnSqncNmbr + ", " + iSctnOcc + ", 0,'','Section occurence " + iSctnOcc +" deleted') ";
				m_stmt.executeUpdate( strHistUpdate );
                                Log.write(Log.DEBUG_VERBOSE, "ExpressBean.deleteSection(): Detail history Ok 2");
                        }
                        catch(SQLException e)
                        {       e.printStackTrace();
                                rollbackTransaction();
                                Log.write(Log.ERROR, "PreorderBean : DB Exception on History logging failed qry["+strHistUpdate+"]");
                                Log.write(Log.ERROR, "PreorderBean : DB Exception ["+e+"]");
                                iReturnCode = DB_ERROR;
                        }
                }
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception in formBean.deleteSection().");
			iReturnCode = DB_ERROR;
		}

		return(iReturnCode);

	} // end deleteSection()

	public int validateFields(AlltelRequest request, int iSqncNmbr, int iVrsn, String strVldtnInd, String strSttsCd)
	{
		int iReturnCode = 0;

		int iFrmSqncNmbrEV = 0;
		Vector vFrmFld = new Vector();
		Vector vTmpFrmFld = new Vector();
		FormField ff;

		ResultSet rsErrVldQry = null;
		String m_strErrVldQry = "";
		boolean bMoreForms = true;

                //added flag to identify simple ports - Antony - 11/15/2010
                boolean simpleOrderFlag = true;
                boolean isValidStatus = false;
                LSRdao lsrDao = new LSRdao();
                
                String currentReqStatus = lsrDao.getCurrentExtStatus(String.valueOf(iSqncNmbr));
                
                if(currentReqStatus.equals("INITIAL") || currentReqStatus.equals("SUPP"))
                    isValidStatus = true;
                
                
		// build a vector of all the fields used in all forms that are part of the request

		m_strErrVldQry = "SELECT DISTINCT S.FRM_SQNC_NMBR FROM " + myOrder.getTBL_NAME() + " X, SERVICE_TYPE_FORM_T S " +
			" WHERE X." + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr + " AND X.SRVC_TYP_CD = S.SRVC_TYP_CD  " +
			 " AND S.TYP_IND = '" + myOrder.getTYP_IND() + "'";
Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields() validate query=["+m_strErrVldQry+"]");
                
		try
		{
			rsErrVldQry = m_stmt.executeQuery(m_strErrVldQry);
			if (rsErrVldQry.next())
			{
				iFrmSqncNmbrEV = rsErrVldQry.getInt("FRM_SQNC_NMBR");
			}
			else
			{
				bMoreForms = false;
			}

		}
		catch(SQLException e)
		{
			e.printStackTrace();
			//rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.validateFields() DB Exception Query : " + m_strErrVldQry);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}
		int a = 0;
		while (bMoreForms)
		{
			//Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields() validate form ["+iFrmSqncNmbrEV+"]");
			try
			{
				vTmpFrmFld = formBean.getFormFields(iFrmSqncNmbrEV, iSqncNmbr, iVrsn);
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				//rollbackTransaction();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean.validateFields() Exception in formBean.getFormFields().");
				return(DB_ERROR);
			}

			//Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields() vTmpFrmFld.size = " + vTmpFrmFld.size());

			for(int i=0 ; i < vTmpFrmFld.size() ; i++)
			{
				ff = (FormField)vTmpFrmFld.elementAt(i);
				vFrmFld.addElement(ff);
				a++;

			}
			vTmpFrmFld.clear();

			try
			{
				if (rsErrVldQry.next())
				{
					iFrmSqncNmbrEV = rsErrVldQry.getInt("FRM_SQNC_NMBR");
				}
				else
				{
					bMoreForms = false;
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
				//rollbackTransaction();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + m_strErrVldQry);
				iReturnCode = DB_ERROR;
			}

			if (iReturnCode != 0)
			{
				return (iReturnCode);
			}

		}
		try
		{
			rsErrVldQry.close();
	//		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields(): rsErrVldQry.close()");

		}
		catch(SQLException e)
		{
			e.printStackTrace();
			//rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.validateFields(): DB Exception on Query : " + m_strErrVldQry);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

                //method to check for simple order fields - Antony - 11/15/2010
                //send vFrmFlds vector for simple order verification
                //first check for LSR form
                //simpleOrderFlag = checkIfLSRIsSimpleOrder(vFrmFld);
                
                //if simpleorderflag still true
                //next check for EU form
                
                //if simpleorderflag still true
                //finally check for NP form
                
                //if simpleorderflag still true
                //finally check for DL form
                
                //if simpleOrderFlag is still true then this is a simple order
                //so the validation errors have to be removed for NP to be submitted
                //only with the 15 Simple Order fields populated
                
                
		FieldValidationBean valBean = new FieldValidationBean();
		boolean setCon = valBean.setDBConnectivity(m_conn);
		int vHash = valBean.createVectorHashtable(vFrmFld);
		boolean bFrmVldtnRqrd = false;

		ValidationResult m_vrVldtnRslt;
		Vector vFldVldtnErr = new Vector();

		int iFrmSqncNmbr_Sv = 0;
		int iVldtnErrCnt = 0;

		for(int j=0 ; j < vFrmFld.size() ; j++)
		{
			ff = (FormField)vFrmFld.elementAt(j);
			if (ff.getFrmSqncNmbr() != iFrmSqncNmbr_Sv)
			{
				if ((strSttsCd.length() != 0) && strVldtnInd.equals("V"))
				// validation is needed for the forms based on the status code we are going to
				{
					m_strErrVldQry = "SELECT 1 FROM FORM_STATUS_T WHERE FORM_STATUS_T.FRM_SQNC_NMBR = " +
						ff.getFrmSqncNmbr() + " AND FORM_STATUS_T.FRM_STTS_IND = '" + strVldtnInd +
						"' AND FORM_STATUS_T.RQST_STTS_CD = '" + strSttsCd + "'";
				}
				else
				// do validation for all the forms available based on the current request status code
				{
					m_strErrVldQry = "SELECT 1 FROM "+ myOrder.getTBL_NAME() + " X, FORM_STATUS_T F " +
						" WHERE X."+ myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr  +
						" AND X." + myOrder.getAttribute("STTS_COLUMN") + " = F.RQST_STTS_CD AND " +
						" F.FRM_SQNC_NMBR = " + ff.getFrmSqncNmbr() +
						" AND F.FRM_STTS_IND = '" + strVldtnInd + "' ";
				}
				try
				{
//HD0000002472840S
					Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields(): qry=["+m_strErrVldQry+"]");
//HD0000002472840F

					rsErrVldQry = m_stmt.executeQuery(m_strErrVldQry);
					if (rsErrVldQry.next())
					{	bFrmVldtnRqrd = true;}
					else
					{	bFrmVldtnRqrd = false;}
					rsErrVldQry.close();
					Log.write(Log.DEBUG_VERBOSE, "ExpressBean.validateFields(): form=" + ff.getFrmSqncNmbr()+
						 " val reqd="+bFrmVldtnRqrd);
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					//rollbackTransaction();
					//DatabaseManager.releaseConnection(m_conn);
					Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + m_strErrVldQry);
					iReturnCode = DB_ERROR;
				}
				if (iReturnCode != 0)
				{
					return (iReturnCode);
				}

				iFrmSqncNmbr_Sv = ff.getFrmSqncNmbr();
			}

                         
                        /*Log.write("Before checking for CCNA...");
                        
                        if(ff.getFrmSqncNmbr() == 12 && ff.getFrmSctnSqncNmbr() == 1 && ff.getFrmFldNmbr().equals("1"))
                        {
                            simpleOrderFlag = true;
                            Log.write("Found CCNA and simpleOrderFlag set to true !");
                        }*/
                        
                        //simpleOrderFlag = checkIfLSRIsSimpleOrder(vFrmFld);
                        
                        
                        
			if (bFrmVldtnRqrd)
			{
				try
				{
                                        if(simpleOrderFlag && isValidStatus)
                                            simpleOrderFlag = checkIfLSRIsSimpleOrder(ff.getFrmSqncNmbr(),
                                                                                      ff.getFrmSctnSqncNmbr(),
                                                                                      ff.getFrmFldNmbr(),
                                                                                      ff.getFieldData(),
                                                                                      ff.getFldDscrptn());
                                        /*Log.write("Value of FF object :"+ff.getFrmSqncNmbr()+","+
                                                                                      ff.getFrmSctnSqncNmbr()+","+
                                                                                      ff.getFrmFldNmbr()+","+
                                                                                      ff.getFieldData()+","+
                                                                                      ff.getFldDscrptn());
                                                                                      */
                                        
					m_vrVldtnRslt = valBean.validateField(ff);

                                        //send formField object to check Simple Order method 
                                        //if it is found that one of the simple order fields are not populated
                                        //and if one of the non-simple order fields are populated
                                        //method returns false and the check is complete 
                                        //if after checking for all fields simpleorderflag is still true 
                                        //then it is determined to be a simple order
                                        
                                        //call only if true already if even one condition had failed
                                        //and it was made false for a field then we need not call again
                                        //as then it is not a simpleorder
                                        
                                        
					if (!m_vrVldtnRslt.getValidationResult())
					{
						iVldtnErrCnt++;
						// add the field to the vector we will use to display the message to the user
						ff.setFldVldtnMsg(m_vrVldtnRslt.getValidationMessage());
						vFldVldtnErr.addElement(ff);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					//rollbackTransaction();
					//DatabaseManager.releaseConnection(m_conn);
					Log.write(Log.ERROR, "ExpressBean : Exception on valBean.validateField()");
					iReturnCode = DB_ERROR;
				}
			}
		}

		vFrmFld.clear();

		// add the validation error vector to the request to be available to the view
		request.getHttpRequest().setAttribute("FLD_VLDTN_ERR", vFldVldtnErr);

                //FormField tmpFF = (FormField) vFldVldtnErr.get(0);
                //Log.write("First Error in Error Vector is: "+tmpFF.getFldDscrptn());
                
                String serviceType = lsrDao.getServiceType(String.valueOf(iSqncNmbr));
                String activityType = lsrDao.getActivityType(String.valueOf(iSqncNmbr));
                                
                Log.write("About to update simple order flag. ST :"+serviceType+"AT :"+activityType);
                                
                boolean npFlag = serviceType.equals("C") && activityType.equals("V");
                
                if(simpleOrderFlag && isValidStatus && npFlag) {
                    Log.write("This could be a Simple Order.....");
                    
                    lsrDao.updateSimpleOrderFlag(String.valueOf(iSqncNmbr),String.valueOf(iVrsn),"Y");
                }
                else if(isValidStatus && npFlag) {
                    Log.write("This is not a Simple Order but an NP complex order.");
                    //update simplePortFlag as N here
                        lsrDao.updateSimpleOrderFlag(String.valueOf(iSqncNmbr),String.valueOf(iVrsn),"N");
                } else if(isValidStatus) {
                    Log.write("This is not a NP PON.");
                    //update simplePortFlag as N/A here
                        lsrDao.updateSimpleOrderFlag(String.valueOf(iSqncNmbr),String.valueOf(iVrsn),"N/A");
                }
                
                //if simpleOrderFlag is true - Antony - 11/15/2010
                //look for field sequence number for non-simple order fields 
                //remove validation errors for the non-simple order fields
                //repeat this for LSR,EU,NP and DL forms
                
                //repeat for the remaining 12 non-simple order fields
                
                //list of validation errors to be removed if order is a simple order
                //LSR form
                //1. INIT
                
                //EU form
                //1. LOCNUM
                //2. NAME
                //3. SASN
                //4. CITY
                //5. ERL
                //6. EAN
                //7. EATN
                
                //DL form
                //1. MTN
                //2. LTN
                
                //NP form
                //1. LOCNUM
                //2. LNUM
                //3. LNA
                
                Vector errVector = new Vector();
                                
                Log.write("Size of validation error vector :"+vFldVldtnErr.size());
                
                Log.write("value of iVldtnErrCnt:"+iVldtnErrCnt);
                                
                if(simpleOrderFlag && isValidStatus)
                {
                
                    Log.write("Removing all upfront validation edits except those for LSR:CCNA,AN,DDD,NNSP,TELNO(INIT),EU:ZIP,NP:PORTED NBR format errors.....");
                    
                    //for(int r = 0; r < iVldtnErrCnt; r++)
                    int r = 0;
                
                    while(r < vFldVldtnErr.size())
                    {

                        Log.write("Retrieving validation error object for element index# "+r);
                        FormField errFF = (FormField) vFldVldtnErr.get(r);

                        if(errFF != null) {
                            Log.write("Found field "+errFF.getFldDscrptn()+" with value: "+errFF.getFieldData()+" Field Index:"+
                                        +errFF.getFrmSqncNmbr()+","+errFF.getFrmSctnSqncNmbr()+","+errFF.getFrmFldNmbr()+","+r);

                            if((errFF.getFrmSqncNmbr() == 1 && errFF.getFrmSctnSqncNmbr() == 1 && errFF.getFrmFldNmbr().equals("1")) ||//LSR:CCNA
                               (errFF.getFrmSqncNmbr() == 1 && errFF.getFrmSctnSqncNmbr() == 1 && errFF.getFrmFldNmbr().equals("14")) ||//LSR:DDD     
                               (errFF.getFrmSqncNmbr() == 1 && errFF.getFrmSctnSqncNmbr() == 1 && errFF.getFrmFldNmbr().equals("25")) ||//LSR:SUP     
                               (errFF.getFrmSqncNmbr() == 1 && errFF.getFrmSctnSqncNmbr() == 3 && errFF.getFrmFldNmbr().equals("82")) ||//LSR:TELNO(INIT)     
                               (errFF.getFrmSqncNmbr() == 4 && errFF.getFrmSctnSqncNmbr() == 2 && errFF.getFrmFldNmbr().equals("15"))) {//NP:PORTEDNBR

                                Log.write("Found "+errFF.getFldDscrptn()+"field with value: "+errFF.getFieldData()+" Field Index:"+
                                        +errFF.getFrmSqncNmbr()+","+errFF.getFrmSctnSqncNmbr()+","+errFF.getFrmFldNmbr()+","+r);
                                Log.write("Not removing error from Validation Vector as this is a Simple Order field.");
                                errVector.addElement(errFF);
                            } else {//error found in non-simple order field
                                Log.write("Error record for Non-Simple order field found. Proceeding to remove error from error Vector...");
                                vFldVldtnErr.remove(r);
                                
                            }
                        }

                        r++;
                        Log.write("Error Count: "+r);

                    }   
                    
                    iVldtnErrCnt = errVector.size();
                    
                    Log.write("Actual Error Vector size :"+vFldVldtnErr.size());
                    Log.write("Cleaned up error Vector size :"+errVector.size());
                    
                    r=0;
                    
                    vFldVldtnErr.removeAllElements();
                    
                    while (r < errVector.size()) {
                        vFldVldtnErr.add(errVector.get(r));
                        r++;
                    }
                }   
                    
		return(iVldtnErrCnt);
	} //end validateFields()


	protected int updateHistory(int iSqncNmbr, int iVrsn, String strSttsCd)
	{
		int iReturnCode = 0;

		// VALIDATE SECURITY HERE
		if ( ! hasAccessTo(iSqncNmbr) )
		{
			return (SECURITY_ERROR);
		}

		String strMdfdDt = "";
		int iHstrySqncNmbrOld = 0;
		int iHstrySqncNmbrNew = 0;

		String strQuery1 = "SELECT " + myOrder.getAttribute("HSTRY_COLUMN") + " FROM  " +
			 myOrder.getTBL_NAME() + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		Log.write(Log.DEBUG_VERBOSE,"ExpressBean.updateHistory() query=["+strQuery1+"]");

		try
		{
			ResultSet rs1 = m_stmt.executeQuery(strQuery1);
			if (rs1.next())
			{
				iHstrySqncNmbrOld = rs1.getInt(""+myOrder.getAttribute("HSTRY_COLUMN"));
				rs1.close();
				rs1=null;
			}
			else
			{
				rollbackTransaction();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean.updateHistory() Error finding current History Sequence Number ");
				iReturnCode = HISTORY_ERROR;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.updateHistory() : DB Exception on Query : " + strQuery1);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		if (iHstrySqncNmbrOld != 0)
		{
			//update the existing History record using the given key

			String strUpdate1 = "UPDATE " + myOrder.getAttribute("HSTRY_TBL_NAME") + " SET " +
				myOrder.getAttribute("HSTRY_STTS_CD_OUT") + " = '" + strSttsCd + "', " +
				myOrder.getAttribute("HSTRY_DT_OUT") + " = " + m_strTimeStamp + ", MDFD_DT = " +
				m_strTimeStamp + "  WHERE " + myOrder.getAttribute("HSTRY_SQNC_COLUMN") + " = " + iHstrySqncNmbrOld;
Log.write(Log.DEBUG_VERBOSE, "ExpressBean.updateHistory(): update old HIstory ["+strUpdate1+"]");
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
				Log.write(Log.ERROR, "ExpressBean.updateHistory() DB Exception on Update : " + strUpdate1);
				iReturnCode = DB_ERROR;
			}
			if (iReturnCode != 0)
			{
				return (iReturnCode);
			}

			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.updateHistory(): Successful History Update for SqncNmbr = " + iSqncNmbr + " ; HstrySqncNmbr = " + iHstrySqncNmbrOld);

		}

		// create a new history record

		String strQuery2 = "SELECT " + myOrder.getAttribute("HSTRY_SEQUENCE")+".nextval HSTRY_SQNC_NMBR_NEW FROM dual";

		try
		{
			ResultSet rs2 = m_stmt.executeQuery(strQuery2);
			if (rs2.next())
			{
				iHstrySqncNmbrNew = rs2.getInt("HSTRY_SQNC_NMBR_NEW");
				rs2.close();
			}
			else
			{
				rollbackTransaction();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : Error getting next History Sequence Number ");
				iReturnCode = -185;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Query : " + strQuery2);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		String strInsert1="";
		//For whatever reason....the history tbls were built in different column orders
		//so do this cheesy stuff....
		strInsert1 = "INSERT INTO " + myOrder.getAttribute("HSTRY_TBL_NAME") +
				" ("+myOrder.getAttribute("HSTRY_SQNC_COLUMN")+","+
                                myOrder.getSQNC_COLUMN()+","+
                                myOrder.getVRSN_COLUMN()+","+
                                myOrder.getAttribute("HSTRY_STTS_CD_IN")+","+
                                myOrder.getAttribute("HSTRY_STTS_CD_OUT")+","+
                                myOrder.getAttribute("HSTRY_DT_IN")+","+
                                myOrder.getAttribute("HSTRY_DT_OUT")+","+
                                " MDFD_DT, MDFD_USERID) " +
			     " VALUES(" + iHstrySqncNmbrNew + ", " + iSqncNmbr + ", " + iVrsn +
				",'" + strSttsCd + "', 'N/A', " +
				m_strTimeStamp + ", " + m_strTimeStamp + ", " + m_strTimeStamp + ", '" + m_strUserid + "')" ;

		try
		{
Log.write(Log.DEBUG_VERBOSE, "ExpressBean.updateHistory() INSERT ["+strInsert1+"]");
			m_stmt.executeUpdate(strInsert1);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean : DB Exception on Insert : " + strInsert1);
			iReturnCode = DB_ERROR;
		}

		if (iReturnCode != 0)
		{
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "ExpressBean : Successful Insert of New History SqncNmbr = " + iSqncNmbr + " ; HstrySqncNmbr = " + iHstrySqncNmbrNew);

		// if we got here, we have a new History Sequence Number
		// return the new History Sequence Number

		return (iHstrySqncNmbrNew);

	}//end updateHistory()

	//
	// This Sqnc Number represents anytype of order ie Request, Preorder, etc.
	//
	public boolean hasAccessTo(int iSqncNmbr)
	{
		ResultSet rs = null;
		String strCmpnyTyp = null;
		int iCmpnySqncNmbr = -1;
		boolean bRC = false;

		//If we JUST checked this same seq#, no need to query the db again!
		if (iSqncNmbr == m_PrevSqncNmbr)
		{	Log.write(Log.DEBUG_VERBOSE,"ExpressBean.hasAccessTo() repeat customer ("+m_bAccessOk+")");
			return m_bAccessOk;
		}
		else	{
			m_PrevSqncNmbr = iSqncNmbr;
			m_bAccessOk = false;
		}


//CANT we get this from SDM ??
// ...using sdm.getLoginProfileBean().getUserBean().getCmpnyTyp()

		// Obtain the Company Type
		String strQuery = "SELECT CMPNY_TYP, C.CMPNY_SQNC_NMBR FROM USERID_T U, COMPANY_T C WHERE " +
			"U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND USERID = '" + m_strUserid + "'";

		// Build SELECT SQL statement
		try {
			rs = m_stmt.executeQuery(strQuery);
			if (rs.next())
			{
				strCmpnyTyp = rs.getString("CMPNY_TYP");
				iCmpnySqncNmbr = rs.getInt("CMPNY_SQNC_NMBR");
				rs.close();
				rs = null;
			}
			else
			{
				Log.write(Log.ERROR, "ExpressBean.hasAccessTo() No Company Type for UserId: " + m_strUserid);
				//DatabaseManager.releaseConnection(m_conn);
				m_bAccessOk = false;
				return m_bAccessOk;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "ExpressBean.hasAccessTo() : DB Exception on Query : " + strQuery);
			return false;
		}

		if (strCmpnyTyp.equals("P"))
		{ // Allowed to view everything
			bRC = true;
		}
		else
		{
			String strInClause = "";

			// Get all OCN Codes for this users user groups
			strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
				"(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + m_strUserid + "')";

			try {
				rs = m_stmt.executeQuery(strQuery);
				while (rs.next())
				{
					// GET ALL OCNs  and build a list like this  '1234','2345'
					if (rs.getString("OCN_CD").equals("*"))
					{
						String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " +
							rs.getInt("CMPNY_SQNC_NMBR");
						Statement substmt = null;
						substmt = m_conn.createStatement();
						ResultSet subrs = substmt.executeQuery(strSubQuery);
						while (subrs.next() == true)
						{
							strInClause = strInClause + "'" + subrs.getString("OCN_CD") + "',";
						}
						subrs.close();
						subrs = null;
						substmt.close();
						substmt = null;
					}
					else
					{
						strInClause = strInClause + "'" + rs.getString("OCN_CD") + "',";
					}
				}
				rs.close();
				rs = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				//DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : DB Exception on Query in hasAccessTo().");
				return false;
			}

			// Find out if order belongs to a Company & OCN that this user has access to.
			strQuery = "SELECT " + myOrder.getSQNC_COLUMN() + " FROM " + myOrder.getTBL_NAME() +
				   " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
				//Fix security hole - if OCN table is not set up w/ specific OCNs, the
	                        // user was able to view all order for all OCNs in xxxListView. We stop
				// that here by restricting by comp seq number too.

	//TEMP Skip for KPEN Trbl Tix ---unless they're set up with '*' OCN only...then we cant skip
	if ( !myOrder.getTYP_IND().equals("S") || (myOrder.getTYP_IND().equals("S") && (strInClause.length() == 0)) )
	{
                        	   strQuery += " AND " + myOrder.getTBL_NAME()+".CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr + " ";
	}

			// DSL agents & KPEN users dont have OCNs, so this check can be skipped....
			if ( (strInClause.length() > 0) && (!myOrder.getTYP_IND().equals("D")) && (!myOrder.getTYP_IND().equals("W")) && (!myOrder.getTYP_IND().equals("X")))
			{
				// Strip off last comma
				if (strInClause.endsWith(","))
				{	strInClause = strInClause.substring(0,strInClause.length()-1);
				}
				strQuery = strQuery + " AND OCN_CD IN (" + strInClause + ")";
			}
			try {
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.hasAccessTo() query=["+strQuery+"]");
				rs = m_stmt.executeQuery(strQuery);
				if (rs.next())
				{
					bRC = true;
				}
				else
				{
					bRC = false;
				}
				rs.close();
				rs = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				DatabaseManager.releaseConnection(m_conn);
				Log.write(Log.ERROR, "ExpressBean : DB Exception on Query in hasAccessTo().");
				return false;
			}

		}
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.hasAccessTo() " + bRC);
		m_bAccessOk = bRC;

		return bRC;

	} // end hasAccessTo()


	public ExpressOrder getExpressOrder() {
		return this.myOrder;
	}

	public String getMdfdDt() {
		return(this.m_strMdfdDt);
	}
	public String getSttsCdFrom() {
		return this.m_strSttsCdFrom;
	}
	public String getTypInd() {
		//return this.m_strTypInd;
		return myOrder.getTYP_IND();
	}
	public String getSrvcTypCd() {
		return this.m_strSrvcTypCd;
	}
	public String getRqstTypCd() {
		return this.m_strRqstTypCd;
	}
	public String getSttsCdTo() {
		return this.m_strSttsCdTo;
	}
	public FormBean getFormBean() {
		return this.formBean;
	}
	public String getDtsntStamp() {
		return this.m_strDtsntStamp;
	}
	public String getTimeStamp() {
		return this.m_strTimeStamp;
	}
	public String getUserid() {
		return this.m_strUserid;
	}
	public String getReplyMdfdDt() {
		return m_strReplyMdfdDt;
	}
	public int getNewVersion() {
		return m_iVrsn;
	}

	public void setSrvcTypCd(String strNewSrvcTypCd) {
		this.m_strSrvcTypCd = strNewSrvcTypCd;
	}
	public void setRqstTypCd(String strNewRqstTypCd) {
		this.m_strRqstTypCd = strNewRqstTypCd;
	}
	public void setTimeStamp(String strNewTimeStamp) {
		this.m_strTimeStamp = strNewTimeStamp;
	}
	public void setSttsCdTo(String strNewSttsCdTo) {
		this.m_strSttsCdTo = strNewSttsCdTo;
	}
	public void setSttsCdFrom(String strNewSttsCdFrom) {
		this.m_strSttsCdFrom = strNewSttsCdFrom;
	}
	public void setNewVersion(int iNewVersion) {
		this.m_iVrsn = iNewVersion;
	}

	public int setNotifyInd(int iSqncNmbr, int iNotifySqncNmbr)
	{
		return setNotifyInd(iSqncNmbr,  (new Integer(iNotifySqncNmbr)).toString() );
	}

	public int setNotifyInd(int iSqncNmbr, String strNotifySqncNmbr)
	{

		int iReturnCode = 0;

		String strInsert = "INSERT INTO NOTIFY_T A  VALUES (" + iSqncNmbr + ",'"+ myOrder.getTYP_IND() +"'," +
			strNotifySqncNmbr + ", sysdate, '" + m_strUserid + "')";
Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotifyInd(): insert=["+ strInsert +"]");
		try
		{
			if (m_stmt.executeUpdate(strInsert) <= 0)
			{
				throw new SQLException();
			}
		}
		catch(SQLException e)
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotifyInd(): Error code =["+ e.getErrorCode() + "]");
			if (e.getErrorCode() == DUPLICATE_ERR)
			{
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotifyInd(): DUPLICATE...ignore "+ iSqncNmbr + " " + myOrder.getTYP_IND());
				iReturnCode = 0;
			}
			else
			{
				e.printStackTrace();
				Log.write(Log.ERROR, "ExpressBean.setNotifyInd() DB Exception on Insert : " + strInsert);
				iReturnCode = DB_ERROR;
			}
		}

		return (iReturnCode);
	}

	public int setNotify(int iSqncNmbr, String strAction)
        {
		int iReturnCode = 0;
		String strNtfySqncNmbr = null;
		String strRqstTypCd = "";

		String strQuery = "SELECT " +  myOrder.getAttribute("STTS_COLUMN");
		if (myOrder.getTYP_IND().equals("R") )
		{	strQuery += ", RQST_TYP_CD ";
		}
		strQuery += " FROM " +  myOrder.getTBL_NAME() + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
		String strStatus = "";
Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotify() SELECT ("+ strQuery + ")");
		try
		{
			ResultSet rs = m_stmt.executeQuery(strQuery);
			if (rs.next())
			{	strStatus = rs.getString(1);
				if (myOrder.getTYP_IND().equals("R") )
				{       strRqstTypCd = rs.getString("RQST_TYP_CD");
				}
				else	strRqstTypCd = myOrder.getTYP_IND();
			}
			rs.close();
			rs=null;
		}
		catch(SQLException e)
		{
			Log.write(Log.ERROR, "ExpressBean.setNotify() SELECT error ("+ strQuery + ")");
			return -1;
		}
		catch(Exception ee)
		{
			Log.write(Log.ERROR, "Exception=[" + ee + "]");
			return -1;
		}

		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotify() before ActionManager()" );
				ActionManager am = new ActionManager();
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotify() after ActionManager()" );

		// Check if this is instance of DwoBean to consider product Type )
		if( m_strPrdctTypCd.equals("0") )
		{
			strNtfySqncNmbr = am.getActionNotificationDwo(strStatus, myOrder.getTYP_IND(), strRqstTypCd , strStatus, strAction, m_strPrdctTypCd );
		}
		else
		{
			strNtfySqncNmbr = am.getActionNotification(strStatus, myOrder.getTYP_IND(), strRqstTypCd , strStatus, strAction);
		}
		Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotify() after am.getActionNotification()" );

		if (strNtfySqncNmbr != null && (strNtfySqncNmbr.length() > 0) )
		{
				Log.write(Log.DEBUG_VERBOSE, "ExpressBean.setNotify() should turn on "+ strNtfySqncNmbr);
                                iReturnCode = setNotifyInd(iSqncNmbr, strNtfySqncNmbr);
		}
		return iReturnCode;

	}

	public int turnNotifyOff(String strSqncNmbr, String strCmpnyTyp)
	{
		int iReturnCode = 0;

		if (strCmpnyTyp.equals("P") )
			return iReturnCode;
		try
		{
			Log.write(Log.DEBUG_VERBOSE, "ExpressBean.turnNotifyOff() "+ myOrder.getTYP_IND()+ strSqncNmbr );
			if (m_stmt == null)
			{	getConnection();
				m_stmt.executeUpdate("DELETE FROM NOTIFY_T WHERE SQNC_NMBR="+strSqncNmbr+" AND TYP_IND='"+myOrder.getTYP_IND()+"'");
				closeConnection();
			}
			else
			{
				m_stmt.executeUpdate("DELETE FROM NOTIFY_T WHERE SQNC_NMBR="+strSqncNmbr+" AND TYP_IND='"+myOrder.getTYP_IND()+"'");
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			Log.write(Log.ERROR, "ExpressBean.turnNotifyOff() seq="+strSqncNmbr+" "+ myOrder.getTYP_IND());
			iReturnCode = DB_ERROR;
		}
		return iReturnCode;
	}

	public void setDwoPrdProd( String In )
	{
		m_strPrdctTypCd = In;
	}

	//EK
	public void setDwoPrdProd( int iSqncNmbr )	{
		if( iSqncNmbr >=0 ){
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String strQuery = " Select PRDCT_TYP_CD from DWO_T where DWO_SQNC_NMBR = ? ";
			try{
				pstmt = m_conn.prepareStatement( strQuery );
				pstmt.clearParameters();
				pstmt.setInt( 1, iSqncNmbr );
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					m_strPrdctTypCd = rs.getString(1 );
				}
			}catch(SQLException e)
			{
				e.printStackTrace();
				Log.write(Log.ERROR, "DwoBean : DB Exception In setPrdctTypCd : " + e.toString() );
			}finally{
				try{
					if ( rs != null ){ rs.close(); }
					if ( pstmt != null ){ pstmt.close(); }
				}catch (Exception e){
					e.printStackTrace();
					Log.write(Log.ERROR, "DsTicketBean.CreateRemedy() DB Exception on Query" + e.toString() );
				}
				// don't close if not local connection.
			}
		}
	}
        
        //method to check if this is a simple order coming in as a NP PON - Antony - 11/15/2010
        public boolean checkIfLSRIsSimpleOrder(int frmSqncNmbr,int frmSctnNmbr,String frmFldSqncNmbr,
                                               String frmFieldData,String frmFldDscptn) {
            
            //boolean ifReqTypeIsC = false;                                   
            //all simple order fields have to be populated
            //and all non-simple order fields have to be null or empty
            //if so return true
            
            //first of all check if check if Request Type has value "C" and Activity Type has value of "V"
            //to make sure if PON is an NP
            
            //check Request Type
            if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("23"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form Request Type field...");

                if(frmFieldData != null && frmFieldData.length() > 0 && frmFieldData.equals("C")) {
                    Log.write("Found valid value for Request Type field in LSR form. This is definetely an NP PON and could be a Simple Port.");
                    //populate boolean flag saying Request Type is a C
                    //ifReqTypeIsC = true;
                } else {
                    Log.write("This is not an NP PON. Skipping Simple Port check ....");
                    return false;
                }
            }
                        
            //check Activity Type
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("24"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form Service Type field...");

                if(frmFieldData != null && frmFieldData.length() > 0 && frmFieldData.equals("V") /*&& ifReqTypeIsC*/) {
                    Log.write("Found valid value for Activity Type field in LSR form. This is definetely an NP PON and could be a Simple Port.");
                } else {
                    Log.write("This is not an NP PON. Skipping Simple Port check ....");
                    return false;
                }
            }
            
            //simple order field list
            //1. CCNA

            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("1"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form CCNA field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for CCNA field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for CCNA field in LSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }

            

            //2. AN
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("7"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form AN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for AN field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for AN field in LSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }

            //3. DDD
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("14"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form DDD field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for DDD field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for DDD field in LSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }

            //4. NNSP
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("30"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form NNSP field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for NNSP field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for NNSP field in LSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }

            //5. TELNO(INIT)
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 3 && frmFldSqncNmbr.equals("82"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form TELNO(INIT) field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for TELNO(INIT) field in LSR form.This could be a Simple Port.");
                    Log.write("All required LSR form fields populated.");
                } else {
                    Log.write("Found null or blank for TELNO(INIT) field in LSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }

            //non-simple order fields
            //all fields in LSR form except the 5 above and below have to be empty
            //this is a qn to bus / Annmarie for now just check if INIT is empty
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 3 && frmFldSqncNmbr.equals("81"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form INIT field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for INIT field in LSR form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for INIT field in LSR form. This may be a Simple Port.");
                }
            }

           
            //next check for EU form by checking form sequence number
            //all simple order fields have to be populated
            //and all non-simple order fields have to be null or empty
            //if so return true
            
            //simple order field list
            //1. ZIP
            
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("26"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form ZIP field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for ZIP field in EU form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for ZIP field in EU form. This should be an upfront Validation Error.");
                    return false;
                }
            }
            //2. AGAUTH field
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("35"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form AGAUTH field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for AGAUTH field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for AGAUTH field in ELSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }
            
            //3. NPDI field
            else if(frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("59"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for LSR form NPDI field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for NPDI field in LSR form. This could be a Simple Port.");
                } else {
                    Log.write("Found null or blank for NPDI field in ELSR form. This should be an upfront Validation Error.");
                    return false;
                }
            }
            
            //non-simple order fields
            //all fields in EU form except the 1 above and below have to be empty
            //pre-populated fields list -- these are populated by the system
            //1. VER
            //2. PON
            //3. STATE
                      
            //4. LOCNUM
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("7"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form LOCNUM field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for LOCNUM field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for LOCNUM field in EU form. This may be a Simple Port.");
                }
            }
            
            //5. NAME
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("8"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form NAME field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for NAME field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for NAME field in EU form. This may be a Simple Port.");
                }
            }
            
            //6. SASN
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("14"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form SASN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for SASN field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for SASN field in EU form. This may be a Simple Port.");
                }
            }
            
            //7. CITY
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("24"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form CITY field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for CITY field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for CITY field in EU form. This may be a Simple Port.");
                }
            }
            
            //8. ERL
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("34"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form ERL field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for ERL field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for ERL field in EU form. This may be a Simple Port.");
                }
            }
            
            //9. EAN
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 4 && frmFldSqncNmbr.equals("40"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form EAN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for EAN field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for EAN field in EU form. This may be a Simple Port.");
                }
            }
            
            //10. EATN
            else if(frmSqncNmbr == 2 && frmSctnNmbr == 4 && frmFldSqncNmbr.equals("41"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for EU form EATN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for EATN field in EU form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for EATN field in EU form. This may be a Simple Port.");
                }
            }
            
            //next check for NP form by checking form sequence number
            //all simple order fields have to be populated
            //and all non-simple order fields have to be null or empty
            //if so return true
            
            //simple order field list
            //1. PORTEDNBR
             
            else if(frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("15"))
            {
            
                //fix for SPIRA incident 3719 - added additional if block to direct to Complex flow if more than one 
                //ported number field is entered - Antony - 06/29/2011
            
                if(!firstPtdNbrFound) {

                //simpleOrderFlag = true;
                Log.write("Before checking for NP form PORTEDNBR field...");

                    Log.write("frmnumber :"+frmSqncNmbr+" frmSecNmbr :"+frmSctnNmbr+" frmFldNmbr :"+frmFldSqncNmbr);

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for PORTEDNBR field in NP form. This could be a Simple Port.");
                        firstPtdNbrFound = true;
                } else {
                    Log.write("Found null or blank for PORTEDNBR field in NP form. This should be an upfront Validation Error.");
                    return false;
                }
                } else {
                    Log.write("Before checking for NP form PORTEDNBR field in section other than first section...");

                    if(frmFieldData != null && frmFieldData.length() > 0) {
                        Log.write("Found valid value for PORTEDNBR field in section other than first section in NP form.This cannot be a Simple Port.");
                        return false;
                    } else {
                        Log.write("Found null or blank for PORTEDNBR field in section other than first section in NP form. This may be a Simple Port.");
                        //we still have to return false as second section with no value ported number filled in should result in a complex order
                        return false;
                    }   
                }
            }
            
            //non-simple order fields
            //all fields in NP form except the 1 above and below have to be empty
            //pre-populated fields list -- these are populated by the system
            //and user cannot modify them so we need not check if they are empty
            //1. VER
            //2. PON
              
            //1. LOCNUM
                  
            
            else if(frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("7"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for NP form LOCNUM field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for LOCNUM field in NP form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for LOCNUM field in NP form. This may be a Simple Port.");
                }
            }
            
            //2. LNUM
            
            else if(frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("8"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for NP form LNUM field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for LNUM field in NP form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for LNUM field in NP form. This may be a Simple Port.");
                }
            }
            
            //3. LNA
            
            else if(frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("10"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for NP form LNA field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for LNA field in NP form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for LNA field in NP form. This may be a Simple Port.");
                }
            }
            
            //next check for DL form by checking form sequence number
            
            //simple order fields
            
            //1. MTN 
            
            else if(frmSqncNmbr == 13 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("21"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for DL form MTN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for MTN field in DL form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for MTN field in DL form. This may be a Simple Port.");
                }
            }
            
            //2. LTN 
            
            else if(frmSqncNmbr == 13 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("39"))
            {
                //simpleOrderFlag = true;
                Log.write("Before checking for DL form LTN field...");

                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Found valid value for LTN field in DL form.This cannot be a Simple Port.");
                    return false;
                } else {
                    Log.write("Found null or blank for LTN field in DL form. This may be a Simple Port.");
                }
            }

            //pre-populated fields list -- these are populated by the system
            //and user cannot modify them so we have to exclude these fields from the check if they are empty
            //1. VER
            //2. PON
            //3. ACT
            //4. REQTYP
            //5. CC
            //6. LSRNO
            //pre-populated but user editable fields
            //7. ACNA -- has a default value of ZZZ - but still user can make it empty?
            //8. EA
            //SP valid field but not mandatory
            //9. NPDI
            //10. AGAUTH
            //if any field in any form other than the above are populated then it is a complex order
            //send false in else block
            
            else if((frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("2")) ||//LSR:PON
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//LSR:VER
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("4")) ||//LSR:LSRNO
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("9")) ||//LSR:SC
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("10")) ||//LSR:PG
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("12")) ||//LSR:D/TSENT
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("25")) ||//LSR:SUP
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("28")) ||//LSR:RTR
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("29")) ||//LSR:CC
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("59")) ||//LSR:NPDI
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("35")) ||//LSR:AGAUTH
                    (frmSqncNmbr == 1 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("60")) ||//LSR:PSSWD_PIN
                    (frmSqncNmbr == 1 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("64")) ||//LSR:ACNA
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("1")) ||//EU:PON
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("2")) ||//EU:VER
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//EU:AN
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("4")) ||//EU:ATN
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("5")) ||//EU:DQTY
                    (frmSqncNmbr == 2 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("6")) ||//EU:PG
                    (frmSqncNmbr == 2 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("25")) ||//EU:STATE
                    (frmSqncNmbr == 2 && frmSctnNmbr == 5 && frmFldSqncNmbr.equals("54")) ||//EU:DNUM
                    (frmSqncNmbr == 2 && frmSctnNmbr == 5 && frmFldSqncNmbr.equals("59.1")) ||//EU:TCTOSEC
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("1")) ||//NP:PON
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("2")) ||//NP:VER
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//NP:AN
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("4")) ||//NP:ATN
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("5")) ||//NP:NPQTY
                    (frmSqncNmbr == 4 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("6")) ||//NP:PG
                    (frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("28.1")) ||//NP:TCTOSEC
                    (frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("13")) ||//NP:TDT
                    (frmSqncNmbr == 4 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("18")) ||//NP:NPT
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("1")) ||//DSR:CCNA
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("2")) ||//DSR:PON
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//DSR:VER
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("4")) ||//DSR:DSRNO
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("5")) ||//DSR:LOCQTY
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("6")) ||//DSR:AN
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("7")) ||//DSR:ATN
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("8")) ||//DSR:EAN
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("9")) ||//DSR:EATN
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("10")) ||//DSR:SC1
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("11")) ||//DSR:SC2
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("12")) ||//DSR:PG
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("13")) ||//DSR:D/TSENT
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("16")) ||//DSR:PROJECT
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("19")) ||//DSR:REQTYP
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("20")) ||//DSR:ACT
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("23")) ||//DSR:RTR
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("24")) ||//DSR:CC
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("25")) ||//DSR:AGAUTH
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("26")) ||//DSR:DATED
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("27")) ||//DSR:AUTHNM
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("28.a")) ||//DSR:TOS1
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("28.b")) ||//DSR:TOS2
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("28.c")) ||//DSR:TOS3
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("28.d")) ||//DSR:TOS4
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("29")) ||//DSR:DLORD
                    (frmSqncNmbr == 12 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("38")) ||//DSR:DLQTY
                    (frmSqncNmbr == 12 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("43")) ||//DSR:ACNA
                    (frmSqncNmbr == 12 && frmSctnNmbr == 2 && frmFldSqncNmbr.equals("52")) ||//DSR:STATE(DBILLNM)
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("1")) ||//DL:CCNA
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("2")) ||//DL:PON
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//DL:VER
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("4")) ||//DL:DSRNO
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("5")) ||//DL:ATN
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("6")) ||//DL:AN
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("7")) ||//DL:SC1
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("8")) ||//DL:SC2
                    (frmSqncNmbr == 13 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("9")) ||//DL:PG
                    (frmSqncNmbr == 13 && frmSctnNmbr == 3 && frmFldSqncNmbr.equals("107")) ||//DL:PG
                    (frmSqncNmbr == 14 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//LR:VER
                    (frmSqncNmbr == 14 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("16")) ||//LR:TELNO
                    (frmSqncNmbr == 15 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("3")) ||//LSRCM:VER
                    (frmSqncNmbr == 15 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("10")) ||//LSRCM:VER
                    (frmSqncNmbr == 15 && frmSctnNmbr == 1 && frmFldSqncNmbr.equals("17")))//LSRCM:VER
            {
                //simpleOrderFlag = true;
                Log.write("Pre-populated field found. Field Description:"+frmFldDscptn+".Skipping field check..");
                return true;
            }
            
            else {//A field other than the simple order fields, non simple order fields, prepop fields has been found
            
                Log.write("A field other than the simple order fields, non simple order fields, prepop fields has been found Field Description:"+frmFldDscptn);
                
                if(frmFieldData != null && frmFieldData.length() > 0) {
                    Log.write("Field :"+frmFldDscptn+" found with a not null value. Then this is not a Simple Port.Sending to Complex Port flow...");
                    return false;
                }
            }    
            return true;
        }
       

	//	Error Messages
	public final int DB_ERROR = -100;		//Database Error Catch
	public final int DATETIMESTAMP_ERROR = -105;	// Error getting Timestamp
	public final int OCN_VALIDATION_ERROR = -110;//Error validating OCN and State
	public final int DUP_PON = -115;		//Duplicate
	public final int HISTORY_ERROR = -125;	//Error generating History
	public final int MDFD_MISMATCH = -145;	//Last Mdfd datetime mismatch
	public final int VERSION_ERROR = -200;	//Error generating new version
	public final int SECTION_ERROR = -230;	//Error generating section
	public final int VALIDATION_ERROR = -260;	//Validation errors exist
	public final int SECURITY_ERROR = -325;	//Access issue
	public final int DUPLICATE_ERR = 1;

}
