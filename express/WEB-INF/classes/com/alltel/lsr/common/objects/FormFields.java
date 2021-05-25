/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL Communications, Inc.
 */

/* 
 * MODULE:		FormFields.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  initial check-in.
 *	11/7/2003 psedlak added getFormField() method
 *
 */


package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds the static FORM_FIELD_T information that
 * is retrieved from the FORM_FIELD_T table.
 */
public class FormFields
{
	private Vector m_vFormFields;
	//NOTE can make m_vFormFields a vector of vectors with each vector holding formfields for a form seq,
	// this may increase lookup times...or make it a hashtable keyed by form and section
	
	//public static FormFields m_instance;
	private static FormFields m_instance = new FormFields();

	
	//public FormFields()
	private  FormFields()
	{
		m_vFormFields = new Vector();
		
		Connection con = null;
		Statement stmt = null;
		Statement stmt2 = null;
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			stmt2 = con.createStatement();
			String strQuery = "SELECT FRM_SQNC_NMBR FROM FORM_T ORDER BY FRM_SQNC_NMBR";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{	
				int iFrmSqncNmbr = rs.getInt("FRM_SQNC_NMBR");
Log.write("FormFields() loading singleton form="+iFrmSqncNmbr);

				String strQuery2 = "SELECT * FROM FORM_FIELD_T WHERE FRM_SQNC_NMBR = " + iFrmSqncNmbr +
					" ORDER BY FRM_SQNC_NMBR, FRM_SCTN_SQNC_NMBR, FRM_FLD_SRT_SQNC";
					//NOTE IF ORDER BY changed -it effects below methods !!!

				ResultSet rs2 = stmt2.executeQuery(strQuery2);
				while (rs2.next())
				{	
					FormField fieldnode = new FormField(
						rs2.getInt("FRM_SQNC_NMBR"), 
						rs2.getInt("FRM_SCTN_SQNC_NMBR"), 
						0,  // may be populated by FormBean - iFrmSctnOcc
						rs2.getInt("FRM_FLD_SRT_SQNC"), 
						rs2.getString("FRM_FLD_NMBR"), 
						rs2.getString("FLD_CD"), 
						rs2.getString("FLD_DATA_TYP"), 
						rs2.getInt("FLD_LNGTH"), 
						rs2.getInt("FLD_DSPLY_SZ"), 
						rs2.getString("FLD_DSPLY_ACTNS"), 
						rs2.getString("FLD_DSPLY_TYP"), 
						rs2.getString("FLD_FRMT_MSK"), 
						rs2.getInt("FLD_VLS_SQNC_NMBR"), 
						rs2.getString("FLD_DSCRPTN"), 
						0,  // will be populated by FormBean - iSeqno
						0,  // will be populated by FormBean - req_vers
						"", // will be populated by FormBean - fstblnm 
						"", // will be populated by FormBean - formtablename
						rs2.getString("DB_CLNM_NM"), 
						rs2.getString("SRC_IND"), 
						rs2.getString("SRC_DB_TBL_NM"), 
						rs2.getString("SRC_DB_CLMN_NM"), 
						rs2.getString("MOX_ARR"), 
						rs2.getString("IMOX_ARR"),
						"",  // will be populated by FormBean - strFieldData
						rs2.getString("JSCRPT_EVNT")
				    );
       		                    m_vFormFields.addElement(fieldnode);
				}
				rs2.close();
			}
			rs.close();
			stmt.close();
			stmt2.close();
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "FormFields() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
	}
	
	/**
	 *  Return a vector of form fields for the given form sequence number
	 *     and form section sequence number.
	 *  NOTE: null returned if nothing here for given sequence number and
	 *     section sequence number.
	 */
	public Vector getFormFields(int iFormSqncNmbr, int iFormSectSqncNmbr)
	{
		// Create new Vector
		Vector vFormFields = new Vector();

		//spin thru our forms for match
		Iterator it = m_vFormFields.iterator();
		while (it.hasNext())
		{ 
			//FormField ffv = new FormField();
			//ffv = (FormField)it.next();
			FormField ffv = new FormField( (FormField)it.next() );	//copy constructor
			if ((ffv.getFrmSqncNmbr() == iFormSqncNmbr) && 
			    (ffv.getFrmSctnSqncNmbr() == iFormSectSqncNmbr))
			{
				vFormFields.addElement(ffv);
			}
			else if ( ffv.getFrmSqncNmbr() > iFormSqncNmbr )
			{	//get out -instead of spinning thru rest -save some iterations
				//NOTE this assumes ORDER BY on load is in ascending form seq order
				break;
			}
		}
		return vFormFields;
	}
	
	
	/**
	 *  Return a  single form field for given arguments.
	 *  NOTE: null returned if nothing here matches
	 */
	public FormField getFormField(int iFormSqncNmbr, int iFormSectSqncNmbr, String strFormFieldNmbr)
	{
		FormField ff = null;

		//spin thru our forms for match
		Iterator it = m_vFormFields.iterator();
		while (it.hasNext())
		{ 
			FormField ffv = new FormField( (FormField)it.next() );	//copy constructor
			if ((ffv.getFrmSqncNmbr() == iFormSqncNmbr) && (ffv.getFrmSctnSqncNmbr() == iFormSectSqncNmbr)
				&& (ffv.getFrmFldNmbr().equals(strFormFieldNmbr)) )
			{
				ff = new FormField(ffv);
				break;	
			}
		}
		return ff;
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static FormFields getInstance()
	{
		if (m_instance == null)
		{
			m_instance = new FormFields();
			Log.write("singleton(). new instance created in getInstance() ???");
		}
		
		return m_instance;
	}
	
	public static synchronized void reload() {
		m_instance = null;
		m_instance = new FormFields();
	}
}
