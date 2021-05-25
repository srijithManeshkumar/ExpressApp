/*
 * EventInfo.java
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
import java.util.Vector;
/**
 * The AuditData interface is an interface that all audit data objects need to implement
 * before they can be passed to the audit Engine.
 */

public interface AuditEventInfo
{
	
	/**
	 * The getCategory() returns the category of the event information. 
	 * 
	 * @return  the category of this event info object
	 */
	public String getCategory();
   
    
	/**
	 * The getData() returns an array of audit data object. 
	 * 
	 * @return  label for this data object
	 */
	public Vector getAuditData();
    
	/**
	 * the getDestination returns the table name where event information is stored.
	 * @return String	the table name
	 */
    public String getDestination();
	
}
