/*
 * AuditData.java
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

/**
 * The AuditData interface is an interface that all audit data objects need to implement
 * before they can be passed to the audit Engine.
 */

public interface AuditData
{
	
	/**
	 * The getColumnName() returns the column name of the audit data. This can be useful when data items
	 * need to be uniquely identified.
	 * 
	 * @return  the name of this data object
	 */
	public String getColumnName();
   
    
	/**
	 * The getValue() returns the value associated with the audit data object. 
	 * 
	 * @return  label for this data object
	 */
	public Object getValue();
    
    
	
}
