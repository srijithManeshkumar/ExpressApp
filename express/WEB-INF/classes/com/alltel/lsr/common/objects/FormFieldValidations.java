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
 * MODULE:		FormFieldValidations.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        04-23-2002
 * 
 * HISTORY:
 */


package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds the static FORM_FIELD_VALIDATION_T information that
 * is retrieved from the FORM_FIELD_VALIDATION_T table.
 */
public class FormFieldValidations
{
	public static FormFieldValidations m_instance;
	private Vector m_vFormFieldValidations;
	
	public FormFieldValidations()
	{
		m_vFormFieldValidations = new Vector();
		
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

				String strQuery2 = "SELECT * FROM FORM_FIELD_VALIDATION_T WHERE FRM_SQNC_NMBR = " + iFrmSqncNmbr + " ORDER BY FRM_SQNC_NMBR, FRM_SCTN_SQNC_NMBR, FRM_FLD_NMBR, VLDTN_SRT_ORDR";
				ResultSet rs2 = stmt2.executeQuery(strQuery2);
				while (rs2.next())
				{	
					int iFrmSctnSqncNmbr = rs2.getInt("FRM_SCTN_SQNC_NMBR");
					String strFrmFldNmbr = rs2.getString("FRM_FLD_NMBR");
					int valSeq = rs2.getInt("VLDTN_SQNC_NMBR");
					int iFrmFldSrtSqnc = rs2.getInt("FRM_FLD_SRT_SQNC");
					int iVldtnSrtOrdr = rs2.getInt("VLDTN_SRT_ORDR");
					String field1 = rs2.getString("FLD1");
					String field1_val = rs2.getString("FLD1_VAL");
					int field1_pos = rs2.getInt("FLD1_POS");
					String field2 = rs2.getString("FLD2");
					String field2_val = rs2.getString("FLD2_VAL");
					int field2_pos = rs2.getInt("FLD2_POS");
					String field3 = rs2.getString("FLD3");
					String field3_val = rs2.getString("FLD3_VAL");
					int field3_pos = rs2.getInt("FLD3_POS");
					String field4 = rs2.getString("FLD4");
					String field4_val = rs2.getString("FLD4_VAL");
					int field4_pos = rs2.getInt("FLD4_POS");
					String field5 = rs2.getString("FLD5");
					String field5_val = rs2.getString("FLD5_VAL");
					int field5_pos = rs2.getInt("FLD5_POS");
					String con_true = rs2.getString("COND_TRUE");
					String con_false = rs2.getString("COND_FALSE");

					ValidationComponent vlcmp = new ValidationComponent(iFrmSqncNmbr, iFrmSctnSqncNmbr, strFrmFldNmbr, valSeq, iFrmFldSrtSqnc, iVldtnSrtOrdr, field1, field1_val, field1_pos, field2, field2_val, field2_pos, field3, field3_val, field3_pos, field4, field4_val, field4_pos, field5, field5_val, field5_pos, con_true, con_false);

					m_vFormFieldValidations.addElement(vlcmp);
				}
				rs2.close();
			}
			rs.close();
			stmt.close();
			stmt2.close();
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "FormFieldValidations() trapped exception");
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
	public Vector getFormFieldValidations(int iFormSqncNmbr, int iFormSctnSqncNmbr, String strFrmFldNmbr)
	{
		// Create new Vector
		Vector vFormFieldValidations = new Vector();

		//spin thru our forms for match
		Iterator it = m_vFormFieldValidations.iterator();
		while (it.hasNext())
		{ 
			//ValidationComponent vc = new ValidationComponent();
			//vc = (ValidationComponent)it.next();
			ValidationComponent vc = new ValidationComponent( (ValidationComponent)it.next() );	//use Copy Constructor
			if ((vc.getFrmSqncNmbr() == iFormSqncNmbr) && 
			    (vc.getFrmSctnSqncNmbr() == iFormSctnSqncNmbr) &&
			    (vc.getFrmFldNmbr().equals(strFrmFldNmbr)))
			{
				vFormFieldValidations.addElement(vc);
			}
		}
		return vFormFieldValidations;
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static FormFieldValidations getInstance()
	{
		if (m_instance == null)
			m_instance = new FormFieldValidations();
		
		return m_instance;
	}
}
