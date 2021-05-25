/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/** 
 * MODULE:	UserReportInfo.java
 * 
 * DESCRIPTION: User Report Info class. 
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        12-04-2002
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.batch;

public class UserReportInfo
{
	private String	m_strUserid;
	private String	m_strLastName;
	private String	m_strFirstName;

	private int	m_iNbrFOCed;
	private int	m_iNbrRejected;
	private int	m_iNbrCompleted;

	private int	m_iWkFOCed;
	private int	m_iWkRejected;
	private int	m_iWkCompleted;

	private int	m_iMthFOCed;
	private int	m_iMthRejected;
	private int	m_iMthCompleted;

	private int	m_iTotalFOCed;
	private int	m_iTotalRejected;
	private int	m_iTotalCompleted;

	public UserReportInfo(String strUserId, String strLastName, String strFirstName)
	{
		this.m_strUserid=strUserId;
		this.m_strFirstName=strFirstName;
		this.m_strLastName=strLastName;
		this.m_iTotalFOCed = 0;
		this.m_iTotalRejected = 0;
		this.m_iTotalCompleted = 0;
		resetCounts();
	}

	public void resetCounts()
	{	m_iNbrFOCed = 0;
		m_iNbrRejected = 0;
		m_iNbrCompleted = 0;
	}
	public void resetWeeklyCounts()
	{	m_iWkFOCed = 0;
		m_iWkRejected = 0;
		m_iWkCompleted = 0;
	}
	public void resetMonthlyCounts()
	{	m_iMthFOCed = 0;
		m_iMthRejected = 0;
		m_iMthCompleted = 0;
	}
	public String	getUserid()	{ return m_strUserid; }
	public String	getFirstName()	{ return m_strFirstName; }
	public String	getLastName()	{ return m_strLastName; }
	public String	getName()	
	{
		return getFirstName()+" "+getLastName();
	}
	public int getNbrFOCed() 	{ return m_iNbrFOCed; }
	public int getNbrRejected() 	{ return m_iNbrRejected; }
	public int getNbrCompleted() 	{ return m_iNbrCompleted; }

	public int getWeeklyFOCed() 	{ return m_iWkFOCed; }
	public int getWeeklyRejected() 	{ return m_iWkRejected; }
	public int getWeeklyCompleted()	{ return m_iWkCompleted; }

	public int getMonthlyFOCed() 	{ return m_iMthFOCed; }
	public int getMonthlyRejected() { return m_iMthRejected; }
	public int getMonthlyCompleted(){ return m_iMthCompleted; }

	public int getTotalFOCed() 	{ return m_iTotalFOCed; }
	public int getTotalRejected() 	{ return m_iTotalRejected; }
	public int getTotalCompleted() 	{ return m_iTotalCompleted; }

	public void addFOCed(int i)
 	{	this.m_iNbrFOCed+=i;
		this.m_iWkFOCed+=i;
		this.m_iMthFOCed+=i;
		this.m_iTotalFOCed+=i;
	}
	public void addRejected(int i)
 	{	this.m_iNbrRejected+=i;
		this.m_iWkRejected+=i;
		this.m_iMthRejected+=i;
		this.m_iTotalRejected+=i;
	}
	public void addCompleted(int i)
	{	this.m_iNbrCompleted+=i;
		this.m_iWkCompleted+=i;
		this.m_iMthCompleted+=i;
		this.m_iTotalCompleted+=i;
	}


}
