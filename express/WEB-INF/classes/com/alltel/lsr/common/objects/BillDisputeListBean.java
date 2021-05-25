/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	BillDisputeListBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        01-22-2003
 * 
 * HISTORY:
 *	03/15/2003  Initial Check-in 
 *	09/15/2003  psedlak -use base class
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class BillDisputeListBean extends ExpressListBean implements Serializable
{
	private BillDisputeOrder thisOrder = BillDisputeOrder.getInstance();
	public BillDisputeListBean()
	{
		super.init(thisOrder);
	}
}
