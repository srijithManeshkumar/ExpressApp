package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

public class QueueCriteria
{
	private String	m_strQueueName = null;
	private String	m_strQueryString = null;
	private String	m_strExtendedQueryString = null;	// will be UNION-ed with strQueryString
	private String	m_strClosedQuery = null;
	private String	m_strQuickSearchString = null;
	private String	m_strOrderByClause = null;
	private String	m_strDefaultQueueInd = "N";	//Y or N
	private String  m_strModifiedDateString = null;
	private String  m_strControlString = "N";

	//  Constructors
	public QueueCriteria(String strName, String strQuery, String strClosedQuery, String strOrder)
	{
		this.m_strQueueName = strName;
		this.m_strQueryString = strQuery;
		this.m_strClosedQuery = strClosedQuery;
		this.m_strOrderByClause = strOrder;
	}
	public QueueCriteria(String strName, String strQuery, String strClosedQuery, String strOrder, String strDefaultQueueYorN)
	{
		this(strName, strQuery, strClosedQuery, strOrder);
		this.m_strDefaultQueueInd = strDefaultQueueYorN;
	}
	public QueueCriteria()
	{
		this("none", "", "", "N");
	}

	//  GETs
	public String getQueueName() {
		return this.m_strQueueName;
	}
	public String getQueryString() {
		return this.m_strQueryString;
	}
	public String getExtendedQueryString() {
		return this.m_strExtendedQueryString;
	}
	public String getClosedQuery() {
		return this.m_strClosedQuery;
	}
	public String getQuickSearchString() {
		return this.m_strQuickSearchString;
	}
	public String getOrderByClause() {
		return this.m_strOrderByClause;
	}
	public String getControlString() {
		return this.m_strControlString;
	}
	public String getModifiedDateString() {
		return this.m_strModifiedDateString;
	}
	public String getFullQuery() {
		return this.m_strQueryString + " " + this.m_strQuickSearchString + " " +this.m_strModifiedDateString + " " + this.m_strClosedQuery + " " + this.m_strOrderByClause;
	}
	public String getFullExtendedQuery() {
		return	this.m_strQueryString + " " + this.m_strQuickSearchString + " " + this.m_strClosedQuery + " UNION " +
			this.m_strExtendedQueryString + " " + this.m_strQuickSearchString + " " + this.m_strClosedQuery + " " +
			this.m_strOrderByClause;
	}
	public boolean isThisDefaultQuery() {
		return (m_strDefaultQueueInd.equals("Y") ? true : false);
	}

	//  SETs
	public void setQueueName(String strQueueName) {
		this.m_strQueueName = strQueueName;
	}
	public void setQueryString(String strQueryString) {
		this.m_strQueryString = strQueryString;
	}
	public void setExtendedQueryString(String strExtQueryString) {
		this.m_strExtendedQueryString = strExtQueryString;
	}
	public void setClosedQuery(String strClosedQuery) {
		this.m_strClosedQuery = strClosedQuery;
	}
	public void setQuickSearchString(String strQuickSearchString) {
		this.m_strQuickSearchString = strQuickSearchString;
	}
	public void setOrderByClause(String strOrderByClause) {
		this.m_strOrderByClause = strOrderByClause;
	}
	public void setDefaultQueueInd(String strYorN) {
		this.m_strDefaultQueueInd = (strYorN.substring(1,1).equalsIgnoreCase("Y")) ? "Y" : "N";
	}
	public void setControlString(String strControlString) {
		this.m_strControlString = strControlString;
	}
	public void setModifiedDateString(String strModifiedDateString) {
		this.m_strModifiedDateString = strModifiedDateString;
	}

	// OTHER FUNCTIONs

}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/QueueCriteria.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:14   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
