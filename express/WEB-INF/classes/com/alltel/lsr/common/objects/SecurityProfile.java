package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds ALL security groups and their corresponding security objects.
 */
public class SecurityProfile
{
	public static SecurityProfile m_instance;
	private Hashtable m_hashSecurityProfile;
	
	public SecurityProfile()
	{
		m_hashSecurityProfile = new Hashtable();
		
		Connection con = null;
		Statement stmt = null;
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			
			String strQuery = "SELECT sg.scrty_grp_cd, so.scrty_objct_cd " +
				   "FROM security_group_t sg, security_group_assignment_t sga, security_object_t so " +
				    "where sg.scrty_grp_cd = sga.scrty_grp_cd " +
				    "and sga.scrty_objct_cd = so.scrty_objct_cd order by 1,2";
		//	Log.write(Log.DEBUG, "SecurityProfile() qry = " + strQuery);
			ResultSet rs = stmt.executeQuery(strQuery);
			while(rs.next())
			{
				String strSecurityGroup = rs.getString("SCRTY_GRP_CD");
				String strSecurityObject = rs.getString("SCRTY_OBJCT_CD");
				String strKey = strSecurityGroup + "_" + strSecurityObject;
				//Log.write(Log.DEBUG, "SecurityProfile() KEY=" + strKey);
				Boolean bPermission = new Boolean(true);
				m_hashSecurityProfile.put(strKey, bPermission);
			}
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "SecurityProfile() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
	}
	
	/**
	 * Is the user authorized to given object?
	 */
	public boolean isAuthorized(String strObject, String strSecurityGroup)
	{
		String strKey = strSecurityGroup + "_" + strObject;
		Boolean bPermission = (Boolean)m_hashSecurityProfile.get(strKey);
		if (bPermission != null)
		{	
			//Log.write(Log.DEBUG, "SecurityProfile() isAuthorized " + strKey + " TRUE");
			return true;
		}
		else
		{
			return false;
		}
	}

	/* Same as above, but this accepts a vector of security groups, since
	 * users can belong to multiple groups.
	 */
	public boolean isAuthorized(String strObject, Vector vSecurityGroups)
	{
		String strSecurityGroup = null;
		boolean bAuthorized = false;

		if (vSecurityGroups.size() <= 0) 	//empty vector case
		{	  
			Log.write(Log.DEBUG, "SecurityProfile() empty vector ");
			return bAuthorized;
		}

		for (int i = 0; i < vSecurityGroups.size(); i++)
		{
			String strSG = (String)vSecurityGroups.elementAt(i);
			//Log.write(Log.DEBUG, "SecurityProfile() checking Security Group=" + strSG + " object=" + strObject);
			if ( isAuthorized(strObject, strSG) )
			{	bAuthorized = true;
				break;
			}
		}//for()

		return bAuthorized;
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static SecurityProfile getInstance()
	{
		if (m_instance == null)
			m_instance = new SecurityProfile();
		
		return m_instance;
	}
}
