package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds ALL field values for HTML SELECT boxes.
 * If data held within class needs to be refreshed - it may be necessary to
 * stop and restart web server (if classloader already loaded it).
 * NOTE: a hashtable key must be derived from 'Object' class, that's why key
 * is converted to 'Integer' on put.
 */
public class FieldValues 
{
	public static FieldValues m_instance;
	private Hashtable m_hashFieldValues;
	
	public FieldValues()
	{
		m_hashFieldValues = new Hashtable();
		
		Connection con = null;
		Statement stmt = null;
		try {
			int	iKey = 0;
			int	iPrevKey = -1;
			Vector	vCargoBay = new Vector();
			
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			
			String strQuery = "SELECT FLD_VLS_SQNC_NMBR, VLD_FLD_VL " +
				   "FROM FIELD_VALUES_T ORDER BY FLD_VLS_SQNC_NMBR, FLD_SRT_ORDR";
			ResultSet rs = stmt.executeQuery(strQuery);
			if (rs.next()==true) {
				iPrevKey = rs.getInt("FLD_VLS_SQNC_NMBR");	//prime it
				String strValidValue = rs.getString("VLD_FLD_VL");
				vCargoBay.addElement(strValidValue);
			}
			while(rs.next())
			{
				iKey =  rs.getInt("FLD_VLS_SQNC_NMBR");
				String strValidValue = rs.getString("VLD_FLD_VL");
				if (iKey != iPrevKey)
				{
					m_hashFieldValues.put( new Integer(iPrevKey), new Vector(vCargoBay) );
					vCargoBay.removeAllElements();
					iPrevKey = iKey;
				}
				vCargoBay.addElement(strValidValue);
			}
			if (iPrevKey != -1)	//theres data, so put last one
			{	m_hashFieldValues.put( new Integer(iKey), new Vector(vCargoBay) );
			}
			
			rs.close();
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "FieldValues() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
		Log.write(Log.DEBUG, "Hashtable of fieldvalues has " + m_hashFieldValues.size() + " elements ");
	}
	
	/**
	 *  Return a list of valid values for the given field sequence number.
	 *  NOTE: null returned if nothing here for given sequence number.
	 */
	public Vector getValidValues(int iFieldSqncNmbr)
	{
		Integer	IKey = new Integer(iFieldSqncNmbr);
		Vector vValues = (Vector)m_hashFieldValues.get(IKey);
		if (vValues != null)
		{
			return vValues;
		}
		else
		{
			Log.write(Log.DEBUG, "FieldValues() DID NOT find key = " + iFieldSqncNmbr);
			return null;
		}
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static FieldValues getInstance()
	{
		if (m_instance == null)
			m_instance = new FieldValues();
		
		return m_instance;
	}
	
	public static synchronized void reload() {
		m_instance = null;
		m_instance = new FieldValues();
	}
}
