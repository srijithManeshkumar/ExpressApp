/*
 * StringAuditData.java 
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
 * The StringAuditData implements AuditData interface.
 */

public class StringAuditData implements AuditData, Serializable 
{	
	private String m_strColumnName = null;
	private String m_strValue = null;
							   
	public StringAuditData(String strColumnName, String strValue)
	{
		m_strColumnName = strColumnName;
		m_strValue = strValue;
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
		return m_strValue;
	}
	
	private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
	{
		out.writeObject(m_strValue);
	}
	
	private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
	{
		m_strValue = (String) in.readObject();
	}
	
}
