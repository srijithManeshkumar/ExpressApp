/*
 * NumberAuditData.java  
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

import java.io.*;

/**
 * The NumberAuditData implements AuditData interface.
 * Implemented Serializable interface
 */

public class NumberAuditData implements AuditData, Serializable 
{	
	private String m_strColumnName = null;
	private long m_lValue;
							   
	public NumberAuditData(String strColumnName, long lValue)
	{
		m_strColumnName = strColumnName;
		m_lValue = lValue;
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
	 * The getValue() returns the value associated with the audit data object. 
	 * 
	 * @return  label for this data object
	 */
	public Object getValue()
	{
		return new Long(m_lValue);
	}
	
	private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
	{
		out.writeLong(m_lValue);
	}
	private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
	{
		m_lValue = in.readLong();
	}
 
}
