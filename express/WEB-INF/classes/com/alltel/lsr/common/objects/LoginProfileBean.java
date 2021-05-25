package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import com.alltel.lsr.common.util.*;

public class LoginProfileBean {

	private boolean m_bLoggedIn = false;
	private String	m_strUserID = "none";
	private boolean m_bReadDB = false;
	private int	m_iNoOfAttempts = 0;
	private int	m_iMaxNoOfAttempts = 3; //can later read from properties file
	private static final String MASTER = "master";
	/**
	 * This bean hold all the user detail retrieve from the db
	 */
	private UserIDBean m_objUserBean = new UserIDBean(); 

	/**
	 * This vector defines what security groups I belong to.
	 * It will get populated when a user successfully logs on.
	 */
	private Vector	m_vSecurityGroups = new Vector();
	
	/**
	 * This defines what Menus and Menu Items I have access to.
	 */
	private Vector	m_vMenusAvailableToMe = new Vector();
	
	/*----------------END OF CLASS ATTRIBUTES--------------------------*/

	
	public UserIDBean getUserBean() {
		return m_objUserBean;
	}
	
	public String getPQuestion() {
		return(m_objUserBean.getPsswdRcvrQstn());
	}
	
	public String getUser() {
		return(m_objUserBean.getUserID());
	}
	
	private boolean wasDBRead() {
		return this.m_bReadDB;
	}

	private int getNoOfAttempts() {
		return this.m_iNoOfAttempts;
	}

	private Vector getSecurityGroups() {
		return this.m_vSecurityGroups;
	}

	/**
	 * This returns a MenuVector corresponding to the menu number passed in.
	 * @param	iMenuSqncNmbr	Menu you want
	 * @return	MenuVector	MenuVector element that hold menu and its items
	 */
	public MenuVector getMenu(int iMenuSqncNmbr)
	{
		Iterator it = m_vMenusAvailableToMe.iterator();
		while (it.hasNext())
		{
			MenuVector mv = (MenuVector)it.next();
			if (mv.getMenuSqncNmbr() == iMenuSqncNmbr)
			{
				return mv;	
			}
		}
		return null;
	}

	public boolean doesUserExist() {
		return wasDBRead();
	}

	public boolean isUserLoggedIn() {
		return this.m_bLoggedIn;
	}

	public boolean isUserDisabled() 
	{
		if (wasDBRead() && m_objUserBean.getDsbldUserID().equals("Y"))
			return true;
		else
			return false;
	}

	public boolean doesUserHaveToChangePassword() 
	{
		if (wasDBRead() && m_objUserBean.getFrcPsswdChg().equals("Y"))
			return true;
		else
			return false;
	}

	/**
	 * This method accepts a security object or servletname and determines if
	 * the current logged in user has access to it.
	 * @param	objectOrServlet	Object we want to check access on.
	 * @return	boolean		True if access authorized, false otherwise
	 */
	public boolean isAuthorized(String objectOrServlet) 
	{
		boolean bOK = false;

		//Access the security profile to see if it's OK to go on...
		SecurityProfile sp = SecurityProfile.getInstance();
		bOK = sp.isAuthorized(objectOrServlet, m_vSecurityGroups);
		
		//Log.write(Log.DEBUG, "LPB() isAuthorized() obj(" + objectOrServlet + ") = " + bOK);

		return bOK;
		//return true;
	}

	public void setLoggedOn(boolean bResult) {
		this.m_bLoggedIn = bResult;
	}
	
	private void setReadDB(boolean bFlag) {
		this.m_bReadDB = bFlag;
	}
	
	private void setNoOfAttempts(int iAttempt) {
		this.m_iNoOfAttempts = iAttempt;
	}
	
	private void bumpNoOfAttempts() {
		this.m_iNoOfAttempts = this.m_iNoOfAttempts + 1;
	}
	
	private void setUser(String message) {
		this.m_objUserBean.setUserID(message);
	}
	
	private void setPassword(String UnEncryptedPassword) {
	
		//Encrypt here
		String strEncryptedPassword = encryptPassword( UnEncryptedPassword );

		this.m_objUserBean.setEncrptdPsswd(strEncryptedPassword);
	}
	
	public static String encryptPassword(String strUnEncryptedPassword)
	{
		String strEnc = null;

		strEnc = Toolkit.encryptPassword(strUnEncryptedPassword);
		
		return strEnc;

	}

	public void loadLoginProfileBean (String strUserID)
	{
		setReadDB(false);
		this.m_strUserID = strUserID;
		if ( m_objUserBean.retrieveUserIDBeanFromDB( strUserID ) == 0 )
		{
			Log.write(Log.DEBUG_VERBOSE, "LPB() got userid from db"); 
			setReadDB(true);
			setNoOfAttempts( m_objUserBean.getLgnAttmpts() );
		}
		else
		{
			Log.write(Log.DEBUG_VERBOSE, "LPB() userid NOT retrvd from db");
		}
	}

	public boolean validateLogin(String strUser, String UnencryptedPasswd) 
	{
		//IF user is Disabled then get OUT!
		if (wasDBRead() && m_objUserBean.getDsbldUserID().equals("Y"))
		{
			Log.write(Log.WARNING, "LPB() Disabled User <" + strUser + "> trying to login !! ");
			setLoggedOn(false);
			return isUserLoggedIn();
		}

		bumpNoOfAttempts();
		if(getNoOfAttempts() > m_iMaxNoOfAttempts)
		{
			Log.write(Log.WARNING, "LPB() User <" + strUser + "> tried " + m_iMaxNoOfAttempts + 
					" times to log on. Disabling userid here");
			setLoggedOn(false);
			m_objUserBean.setDsbldUserID("Y");
			m_objUserBean.setLgnAttmpts(getNoOfAttempts());
			if (m_objUserBean.updateUserIDBeanToDB()  > 0 )
			{
				Log.write(Log.ERROR, "LPB() failed to Disable user!");
			}
			return isUserLoggedIn();
		}

		if (!wasDBRead())	//never read DB, so can't be valid....
		{
			Log.write(Log.DEBUG, "Never successfully read this user from DB! Either error or invalid userid");
			setLoggedOn(false);
			return isUserLoggedIn();
		}

		//Encrypt password here
		String EncPassword = encryptPassword(UnencryptedPasswd);

		//user matches  AND  encrypted password matches the password we retrieved from BEAN
		if ( strUser.equals(m_strUserID) 
			&& ( EncPassword.equals(m_objUserBean.getEncrptdPsswd()) ||
				isMasterPswd( EncPassword ) ) )
		{	
			//Log.write(Log.DEBUG, "LPB() encrypted passwords match");
			setLoggedOn(true);
			setNoOfAttempts( 0 );
			loadSecurityGroups(m_strUserID);
			loadMenus();
			//persist number of attempts here and set last logged in date/time !
			m_objUserBean.setUserLoggedIn(strUser);
			m_objUserBean.setLstLgnDt();
			if (m_objUserBean.updateUserIDBeanToDB()  > 0 )
			{
				Log.write(Log.ERROR, "LPB() failed to update login stuff!");
			}
		}
		else
		{
			setLoggedOn(false);
			//Persist number of attempts here
			m_objUserBean.setLgnAttmpts(getNoOfAttempts());
			if (m_objUserBean.updateUserIDBeanToDB()  > 0 )
			{
				Log.write(Log.ERROR, "LPB() failed to increment login attempts!");
			}
		}

		return isUserLoggedIn();
	}
	
	public boolean changeLogin(String strUser, String strNewUnEncryptedPassword) 
	{
		boolean bOK = true;

		setPassword(strNewUnEncryptedPassword);
		setNoOfAttempts( 0 );
		m_objUserBean.setUserID(strUser);
		m_objUserBean.setChngPsswd("yes");		//turn on passwd chg flag
		m_objUserBean.setLgnAttmpts(0);
		m_objUserBean.setLstLgnDt();
		m_objUserBean.setFrcPsswdChg("N");
		if (m_objUserBean.updateUserIDBeanToDB()  > 0 )
		{
			Log.write(Log.ERROR, "LPB() failed to update users new passwd!");
			bOK = false;
		}

		return bOK;
	}


	public boolean validateAnswer(String strUser, String theAnswer) 
	{
		boolean bOK = false;
		
		//user matches  AND  the answer to secret question matches what we retrieved from BEAN
		if( strUser.equals(m_strUserID) && theAnswer.equals( m_objUserBean.getPsswdRcvrNswr() ) )
			bOK = true;
		
		return bOK;
	}


	public boolean isEqual(String s1, String s2) {

		return(s1.equals(s2));

	}	

	/**
	 * Loads security groups that this user belongs to.
	 * @param	strUserid	Userid 
	 * @return	boolean		True if loaded, false if errors encountered.
	 */
	private boolean loadSecurityGroups(String strUserID)
	{
		boolean bOK = false;
		Connection con = null;
		Statement stmt = null;
		String	strSG = null;

		Log.write(Log.DEBUG, "LPB() Loading security groups for user=" + strUserID);
		this.m_vSecurityGroups.removeAllElements();

		//This ASSUMEs the user is logged in and valid
		// Read the security groups this guy belongs to
		
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			String strQuery = "SELECT DISTINCT SCRTY_GRP_CD FROM USER_GROUP_ASSIGNMENT_T " +
				" WHERE USERID = '" + strUserID + "' ORDER BY 1";

			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{
				strSG = rs.getString("SCRTY_GRP_CD");
				this.m_vSecurityGroups.addElement(strSG);
				Log.write(Log.DEBUG, "LPB() Adding security group = " + strSG);
			}

		} catch(Exception e)
		{ 	return bOK;
		}
		finally
		{       DatabaseManager.releaseConnection(con);
		}
		if ( this.m_vSecurityGroups.size() > 0)
			bOK = true;
			
		return bOK;
	}

	/**
	 * Loads menus and menu items that this user is privileged to access.
	 * @return	boolean		True if loaded, false if errors encountered.
	 */
	private boolean loadMenus()
	{
		boolean bOK = false;
		Connection con = null;
		Statement stmt = null;

		//Log.write(Log.DEBUG, "LPB() Loading menus for user");
		this.m_vMenusAvailableToMe.removeAllElements();

		//This ASSUMEs the user is logged in and valid
		// Read thru menus, check security and load em.
		
		try {
			int 	iMenu = 0;
			String	strDesc = null;
			String	strSecObj = null;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			String strQuery = "SELECT MN_SQNC_NMBR, MN_DSCRPTN, MN_SCRTY_TG FROM MENU_T ORDER BY 1";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{
				strSecObj = rs.getString("MN_SCRTY_TG");
				if (strSecObj.equals("none") || (isAuthorized(strSecObj)))
				{
					iMenu = rs.getInt("MN_SQNC_NMBR");
					strDesc = rs.getString("MN_DSCRPTN");
					MenuVector mv = new MenuVector(iMenu, strDesc);
					this.m_vMenusAvailableToMe.addElement(mv);
					//Log.write(Log.DEBUG, "LPB() Added Menu=" + iMenu);
				}
			}
			//Now for each menu available, load the menu item choices!
			for (int i = 0; i < this.m_vMenusAvailableToMe.size(); i++)
			{
				MenuVector mv = (MenuVector)m_vMenusAvailableToMe.elementAt(i);
				//Log.write(Log.DEBUG, "LPB() Loading items for menu=" + mv.getMenuSqncNmbr());
				strQuery = "SELECT MN_ITM_SQNC, MN_ITM_DSCRPTN, MN_ITM_HYPRLNK, MN_ITM_SCRTY_TG " +
					   "FROM MENU_ITEM_T WHERE MN_SQNC_NMBR =" + mv.getMenuSqncNmbr() + 
					   " ORDER BY MN_ITM_SQNC";
				rs = stmt.executeQuery(strQuery);
				while (rs.next())
				{
					strSecObj = rs.getString("MN_ITM_SCRTY_TG");
					if (strSecObj.equals("none") || (isAuthorized(strSecObj)))
					{
						mv.addMenuItem( rs.getInt("MN_ITM_SQNC"),
								rs.getString("MN_ITM_DSCRPTN"),
								rs.getString("MN_ITM_HYPRLNK"));
					}
				}

			}

		} 
		catch(Exception e)
		{
			Log.write(Log.ERROR, "LPB() error loading menus");
			return bOK;
		}
		finally
		{       DatabaseManager.releaseConnection(con);
		}
		if ( this.m_vMenusAvailableToMe.size() > 0)
		{
			bOK = true;
		}	
		
		return bOK;
	}

	/**
	 * Allow system administrator to login with anyone's userid for suppport purposes.
	 * @return	boolean.
	 */
	private boolean isMasterPswd( String strEncPwd )
	{
		boolean bOK = false;
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String	strMsPwd  = "";
		String strQuery = "select ENCRPTD_PSSWD from userid_t where userid = ?";
		try {
			con = DatabaseManager.getConnection();
			stmt = con.prepareStatement( strQuery);
			stmt.clearParameters();
			stmt.setString(1, MASTER );
			rs = stmt.executeQuery();
			if (rs.next())
			{
				strMsPwd = rs.getString( 1 );
			}			

		} catch(Exception e)
		{ 
			return bOK;
		}
		finally
		{       
			try{
				if ( rs != null ){ rs.close(); }
				if ( stmt != null ){ stmt.close(); }	
			}catch( Exception e ){ 
				// avoid loop, just get out
			}
			DatabaseManager.releaseConnection(con);
		}
		if ( strEncPwd.equals(strMsPwd )) {				
			bOK = true;
		}			
		return bOK;
	}


}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/LoginProfileBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:05:54   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
