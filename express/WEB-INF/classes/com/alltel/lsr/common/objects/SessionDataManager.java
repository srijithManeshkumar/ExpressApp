/**
 * MODULE:		SessionDataManager
 *
 * DESCRIPTION: 	SessionDataManager is used to store data that needs to be persisted in the session object.
 *			It will be written to session as a single object.
 *
 * AUTHOR:		psedlak
 *
 * DATE:		11/2/2001	
 *
 * HISTORY:
 *  2/5/2002 psedlak    (Release 1.1) Added RequestLockBean to session
 *  2/25/2002 psedlak   SER 20476 Adding Billing Disputes, used generic LockBean
 *  3/20/2004 dmartz    SER ????  Added Data Work Orders
 *  3/20/2004 psedlak 	SER ????  Added Data Svc Tix
 *  6/21/2005 psedlak 	made Serializable, added some helper methods for Company Type/Seq#
 *  7/11/2005 psedlak 	doesUserHaveDwoQueue() changes
 */

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/SessionDataManager.java  $
/*
/*   Rev 1.7   Jul 06 2005 09:47:10   e0069884
/* 
/*
/*   Rev 1.6   Dec 28 2004 15:45:08   e0069884
/* 
/*
/*   Rev 1.3   22 Mar 2002 13:23:32   dmartz
/*Removed unnecessary log messages
/*
/*   Rev 1.2   20 Mar 2002 10:59:24   dmartz
/*Separate Request and Tickets
/*
/*   Rev 1.1   11 Feb 2002 09:17:58   sedlak
/*release 1.1
/*
/*   Rev 1.0   23 Jan 2002 11:06:44   wwoods
/*Initial Checkin
*/

/* $Revision:   1.7  $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.io.Serializable;

import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.interfaces.*;

public class SessionDataManager  implements Serializable 
{

	private LoginProfileBean 	m_objLoginProfile;

	private Vector  m_vRequestQueues = new Vector();	//Vector to hold my Request queue criteria
	private Vector  m_vPreorderQueues = new Vector();	//Vector to hold my Preorder queue criteria
	private Vector  m_vDsTicketQueues = new Vector();	//Vector to hold my DsTicket queue criteria
	private Vector  m_vTicketQueues = new Vector();		//Vector to hold my Ticket queue criteria
	private Vector  m_vDwoQueues = new Vector();		//Vector to hold my Data Work Order queue criteria
	private Vector  m_vDslQueues = new Vector();		//Vector to hold my Dsl queue criteria
	private Vector  m_vDisputeQueues = new Vector();	//Vector to hold my Dispute queue criteria

	private Vector  m_vLocks = new Vector();         	//Vector to hold my locks

	private QueueCriteria		m_objRequestQueue;
	private QueueCriteria		m_objPreorderQueue;
	private QueueCriteria		m_objDsTicketQueue;
	private QueueCriteria		m_objTicketQueue;
	private QueueCriteria		m_objDwoQueue;
	private QueueCriteria		m_objDslQueue;
	private QueueCriteria		m_objDisputeQueue;

  	private String		  	m_strLoginPage;		//User's login page
  	private String		  	m_strHomePage;		//User's home page
  	private String		  	m_strUserID;		//User name (only set if logged in OK)
        
        private String		  	m_strSerTyp;
  	private String		  	m_strActTyp;
  
  	public SessionDataManager ()
  	{
		try
		{
			m_strLoginPage = "/LoginView.jsp";		//Default login page
			m_strHomePage = "/ExpressHome.jsp";		//Default home page
			m_strUserID = " ";
			m_objLoginProfile = new LoginProfileBean();
		}
		catch (Exception e)
		{
		// exception handling to be added when needed
		}
  	}

	// 		 GETs
	public LoginProfileBean getLoginProfileBean() {
		return this.m_objLoginProfile;
	}
	public String getUser() {
		return this.m_strUserID;
	}
	public String getLoginPage() {
		return this.m_strLoginPage ;
	}
	public String getHomePage() {
		return this.m_strHomePage ;
	}
	public String getCompanyType() {
		return getLoginProfileBean().getUserBean().getCmpnyTyp();
	}
	public String getCompanySqncNbr() {
		return getLoginProfileBean().getUserBean().getCmpnySqncNmbr();
	}
        //code for LR form DD field fix to get service and activity type
        public String getSerTyp() {
		return this.m_strSerTyp;
	}
	public String getActTyp() {
		return this.m_strActTyp;
	}
	public String setSerTyp(String ser) {
		return this.m_strSerTyp = ser;
	}
	public String setActTyp(String act) {
		return this.m_strActTyp = act;
	}

	
	public QueueCriteria getRequestQueueCriteria() {
		return this.m_objRequestQueue;
	}
	public QueueCriteria getPreorderQueueCriteria() {
		return this.m_objPreorderQueue;
	}
	public QueueCriteria getTicketQueueCriteria() {
		return this.m_objTicketQueue;
	}
	public QueueCriteria getDsTicketQueueCriteria() {
		return this.m_objDsTicketQueue;
	}
	public QueueCriteria getDwoQueueCriteria(String qName) {
		if (this.m_vDwoQueues.isEmpty())
		{	return null;
		}
		else
		{
			for (int i =0;i< m_vDwoQueues.size(); i++)
			{
				QueueCriteria aQC = (QueueCriteria)m_vDwoQueues.elementAt(i);
				Log.write(Log.DEBUG_VERBOSE, "SDM() interrogating qc contents at ["+i+"]");
				if ( qName.equals( aQC.getQueueName() ) )
				{	Log.write(Log.DEBUG_VERBOSE, "SessionDataMgr() This QC is there " + qName );
					this.m_objDwoQueue = aQC;
				}
			}
		}
		return this.m_objDwoQueue;
	}
	public QueueCriteria getDwoQueueCriteria() {
		return this.m_objDwoQueue;
	}
	public QueueCriteria getDslQueueCriteria() {
		return this.m_objDslQueue;
	}
	public QueueCriteria getDisputeQueueCriteria() {
		return this.m_objDisputeQueue;
	}
	public Vector getRequestQueues() {
		return this.m_vRequestQueues;
	}
	public Vector getPreorderQueues() {
		return this.m_vPreorderQueues;
	}
	public Vector getTicketQueues() {
		return this.m_vTicketQueues;
	}
	public Vector getDsTicketQueues() {
		return this.m_vDsTicketQueues;
	}
	public Vector getDwoQueues() {
		return this.m_vDwoQueues;
	}
	public Vector getDslQueues() {
		return this.m_vDslQueues;
	}
	public Vector getDisputeQueues() {
		return this.m_vDisputeQueues;
	}

	public Vector getLocks() {
		return this.m_vLocks;
	}



	//		 SETs
	public void setLoginProfileBean(LoginProfileBean bean) {
		this.m_objLoginProfile = bean;
	}
	public void setUser(String theuser) {
		this.m_strUserID = theuser;
	}

	public void setLoginPage(String url) {
		this.m_strLoginPage = url;
	}
		
	public void setHomePage(String url) {
		this.m_strHomePage = url;
	}

	public void setRequestQueueCriteria(QueueCriteria bean) {
		this.m_objRequestQueue = bean;

		//Put this bean into our Vector 
		if (m_vRequestQueues.size() > 0)
		{
			this.m_vRequestQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vRequestQueues.addElement( bean );
		}
	}

	public void setPreorderQueueCriteria(QueueCriteria bean) {
		this.m_objPreorderQueue = bean;

		//Put this bean into our Vector 
		if (m_vPreorderQueues.size() > 0)
		{
			this.m_vPreorderQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vPreorderQueues.addElement( bean );
		}
	}

	public void setDsTicketQueueCriteria(QueueCriteria bean) {
		this.m_objDsTicketQueue = bean;

		//Put this bean into our Vector 
		if (m_vDsTicketQueues.size() > 0)
		{
			this.m_vDsTicketQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vDsTicketQueues.addElement( bean );
		}
	}

	public void setTicketQueueCriteria(QueueCriteria bean) {
		this.m_objTicketQueue = bean;

		//Put this bean into our Vector 
		if (m_vTicketQueues.size() > 0)
		{
			this.m_vTicketQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vTicketQueues.addElement( bean );
		}
	}

	public void setDwoQueueCriteria(QueueCriteria bean) {
		this.m_objDwoQueue = bean;

		//Put this bean into our Vector 
		if (m_vDwoQueues.size() > 0)
		{
//			this.m_vDwoQueues.setElementAt(bean, 0);	//just temp -overlaying first element
			for (int i =0;i< m_vDwoQueues.size(); i++)
			{
				QueueCriteria aQC = (QueueCriteria)m_vDwoQueues.elementAt(i);
				Log.write(Log.DEBUG_VERBOSE, "SDM().setQC- interrogating qc contents at ["+i+"]");
				String qName = bean.getQueueName();
				if ( qName.equals( aQC.getQueueName() ) )
				{	Log.write(Log.DEBUG_VERBOSE, "SessionDataMgr() This QC is there " + qName );
					this.m_vDwoQueues.setElementAt(bean, i);
				}
			}
		}
		else
		{	this.m_vDwoQueues.addElement( bean );
		}
	}

	public void setDslQueueCriteria(QueueCriteria bean) {
		this.m_objDslQueue = bean;

		//Put this bean into our Vector 
		if (m_vDslQueues.size() > 0)
		{
			this.m_vDslQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vDslQueues.addElement( bean );
		}
	}

	public void setDisputeQueueCriteria(QueueCriteria bean) {
		this.m_objDisputeQueue = bean;

		//Put this bean into our Vector 
		if (m_vDisputeQueues.size() > 0)
		{
			this.m_vDisputeQueues.setElementAt(bean, 0);	//just temp -overlaying first element
		}
		else
		{	this.m_vDisputeQueues.addElement( bean );
		}
	}

       	public void setLock(String strType, int iSqncNmbr) {

		//nothing here yet, then insert first lock
		String strKey = strType+iSqncNmbr;
                if (m_vLocks.size() <= 0)        //empty vector case
                {   Log.write(Log.DEBUG, "SessionDataMgr() added first lock=["+strKey+"]");
                    this.m_vLocks.addElement(strKey);
                    return;
                }
		//Was this request locked by me before? If so, just exit
		for (int i =0;i< m_vLocks.size(); i++)
		{
			String strLock = (String)m_vLocks.elementAt(i);
			Log.write(Log.DEBUG_VERBOSE, "SessionDataMgr() Lock info =" + strLock);
			if ( strLock.equals(strKey) )
			{	Log.write(Log.DEBUG_VERBOSE, "SessionDataMgr() lock there already " + strType +":"+iSqncNmbr);
				return;
			}
		}
		//to get here,  we don't have lock...
 		this.m_vLocks.addElement(strKey);
                Log.write(Log.DEBUG, "SessionDataMgr() added " + strKey+" lock to vector");
        }


	//  OTHER METHODS
	public boolean isAuthorized(String strServletOrObject) {
		if (!isUserLoggedIn())
		{	Log.write(Log.DEBUG, "User maybe authorized, but is not logged in!");
			return false;
		}
		return getLoginProfileBean().isAuthorized(strServletOrObject);
	}

	public boolean isUserLoggedIn() {
		return this.m_objLoginProfile.isUserLoggedIn();
	}

       	public void removeLocks() 
        {
		for (int i = 0; i < m_vLocks.size(); i++)
		{	String strLock = (String)m_vLocks.elementAt(i);
			Log.write("SB Lock type=["+  strLock.substring(0,1) +"] seq=[" +  strLock.substring(1) +"]");
			LockBean unlockMe = new LockBean( strLock.substring(0,1), Integer.parseInt(strLock.substring(1)) );

			unlockMe.unlock();
		}//for()
		this.m_vLocks.removeAllElements();
	}

	public void logoff()
	{
                removeLocks();
                
		this.m_objLoginProfile.setLoggedOn(false);
		this.setUser("");

		//If user has existing queue criteria, clear it
		this.m_vRequestQueues.removeAllElements();
		this.m_vPreorderQueues.removeAllElements();
		this.m_vDsTicketQueues.removeAllElements();
		this.m_vTicketQueues.removeAllElements();
		this.m_vDwoQueues.removeAllElements();
		this.m_vDslQueues.removeAllElements();
		this.m_vDisputeQueues.removeAllElements();
                
		//can clear the UserIDBean here if we want to
	}

	public boolean doesUserHaveRequestQueue() 
	{
		//if (this.m_objRequestQueue == null)
		if (this.m_vRequestQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHavePreorderQueue() 
	{
		//if (this.m_objPreorderQueue == null)
		if (this.m_vPreorderQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHaveTicketQueue() 
	{
		//if (this.m_objTicketQueue == null)
		if (this.m_vTicketQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHaveDsTicketQueue() 
	{
		//if (this.m_objDsTicketQueue == null)
		if (this.m_vDsTicketQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHaveDwoQueue(String qName)
	{
		if (this.m_vDwoQueues.isEmpty())
		{	return false;
		}
		else
		{
			for (int i =0;i< m_vDwoQueues.size(); i++)
			{
				QueueCriteria aQC = (QueueCriteria)m_vDwoQueues.elementAt(i);
				Log.write(Log.DEBUG_VERBOSE, "SDM() interrogating qc contents at ["+i+"]");
				if ( qName.equals( aQC.getQueueName() ) )
				{	Log.write(Log.DEBUG_VERBOSE, "SessionDataMgr() This QC is there " + qName );
					return true;
				}
			}
		}
		return false;
	}

	public boolean doesUserHaveDwoQueue() 
	{
		if (this.m_vDwoQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHaveDslQueue() 
	{
		//if (this.m_objDslQueue == null)
		if (this.m_vDslQueues.isEmpty())
			return false;
		else
			return true;
	}

	public boolean doesUserHaveDisputeQueue() 
	{
		//if (this.m_objDisputeQueue == null)
		if (this.m_vDisputeQueues.isEmpty())
			return false;
		else
			return true;
	}


} //end of SessionDataManager()
