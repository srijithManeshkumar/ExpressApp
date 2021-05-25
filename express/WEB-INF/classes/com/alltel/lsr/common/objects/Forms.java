/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2003
 *                                      BY
 *                              ALLTEL COMMUNICATIONS INC.
 */
/*
 * MODULE:      Forms.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:	psedlak
 *
 * DATE:        01-10-2002
 *
 * HISTORY:
 *      11/14/2003 psedlak	added getForm() method
 *
 *      11/7/2007  S Korchnak   added order by to forms section retrieval
 */


package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds the static FORM information that
 * is retrieved from FORM_T and FORM_SECTION_T tables.
 */
public class Forms
{
	public static Forms m_instance;
	private Vector m_vForm;		//of type Form
	
	public Forms()
	{
		m_vForm = new Vector();
		
		Connection con = null;
		Statement stmt = null;
		Statement stmt2 = null;
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			String strQuery = "SELECT DISTINCT FRM_SQNC_NMBR, FRM_CD, FRM_TBL_NM, " +
				"LSOG_VRSN, SCRTY_OBJCT_CD, FRM_DSCRPTN, FRM_ACT_MOX_IDX " +
				"FROM FORM_T ORDER BY FRM_SQNC_NMBR";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{	
				int iForm= rs.getInt("FRM_SQNC_NMBR");
				Form objForm = new Form(rs.getInt("FRM_SQNC_NMBR"),
							rs.getString("FRM_CD"),
							rs.getString("FRM_TBL_NM"),
							rs.getInt("LSOG_VRSN"),
							rs.getString("SCRTY_OBJCT_CD"),
							rs.getString("FRM_DSCRPTN"),
							rs.getString("FRM_ACT_MOX_IDX")  );
				stmt2 = con.createStatement();
				String strQuery2 = "SELECT DISTINCT FRM_SCTN_SQNC_NMBR, FRM_SCTN_DSCRPTN, FRM_SCTN_RPT_IND " +
					" FROM FORM_SECTION_T WHERE FRM_SQNC_NMBR = " + iForm + " ORDER BY FRM_SCTN_SQNC_NMBR";
				ResultSet rs2 = stmt2.executeQuery(strQuery2);
				while (rs2.next())
				{
					objForm.addFormSection( rs2.getInt("FRM_SCTN_SQNC_NMBR"),
								rs2.getString("FRM_SCTN_DSCRPTN"),
								rs2.getString("FRM_SCTN_RPT_IND") );
				}
				stmt2.close();
				this.m_vForm.addElement(objForm);
				//Log.write(Log.DEBUG, "Forms() Added Form=" + iForm);
			}
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "Forms() trapped exception");
		}
		finally
		{
			Log.write(Log.DEBUG, "Forms() loaded");
			DatabaseManager.releaseConnection(con);
		}
	}
	
	/**
	 *  Return a vector of form sections for the given form sequence number.
	 *  NOTE: null returned if nothing here for given sequence number.
	 */
	public Vector getFormSections(int iFormSqncNmbr)
	{
		//spin thru our forms for match
		Iterator it = m_vForm.iterator();
		while (it.hasNext())
		{ 
			Form fv = (Form)it.next();
			if (fv.getFormSqncNmbr() == iFormSqncNmbr)
			{
				return fv.getFormSections();
			}//save some iterations here...assumes ORDER BY above
			else if ( fv.getFormSqncNmbr() > iFormSqncNmbr)
			{	return null;
			}
		}
		return null;

	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static Forms getInstance()
	{
		if (m_instance == null)
			m_instance = new Forms();
		
		return m_instance;
	}
	
	/**
	 *  Return a form object
	 *  NOTE: null returned if nothing here for given sequence number.
	 */
	public Form getForm(int iFormSqncNmbr)
	{
		Form fv = null;
		Iterator it = m_vForm.iterator();
		while (it.hasNext())
		{ 
			fv = (Form)it.next();
			if (fv.getFormSqncNmbr() == iFormSqncNmbr)
			{
				return fv;
			}//save some iterations here...assumes ORDER BY above
			else if ( fv.getFormSqncNmbr() > iFormSqncNmbr)
			{	return null;
			}
		}
		return null;
	}
}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/Forms.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:05:44   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
