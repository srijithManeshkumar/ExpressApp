/*
 * DateAuditData.java
 *
 * Copyright (c) ALLTEL Information Services, Inc.
 *
 * This software is the confidential and proprietary information of ALLTEL
 * Information Services, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with ALLTEL.
 */

package com.alltel.lsr.common.error.objects;
import java.util.Date;
import java.io.*;

/**
 * The DateAuditData implements AuditData interface.
 */

public class DateAuditData implements AuditData, Serializable 
{	
	private String m_strColumnName = null;
	private Date m_dateValue = null;
							   
	public DateAuditData(String strColumnName, Date dateValue)
	{
		m_strColumnName = strColumnName;
		m_dateValue = dateValue;
	}
	/**
	 * The getColumnName() returns the column name of the audit data. This can be useful when data items
	 * need to be uniquely identified.
	 * 
	 * @return  the name of this data object
	 */
	public String getColumnName()
	{
		return m_strColumnName;
	}
   
    
	/**
	 * The getValue() returns the value associated with the date audit data object. 
	 * 
	 * @return  label for this data object
	 */
	public Object getValue()
	{
		return m_dateValue;
	}
	
	private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
	{
		out.writeObject(m_dateValue);
	}
	private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
	{
		m_dateValue = (Date) in.readObject();
	}
}
