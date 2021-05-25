/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	TicketListBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/17/2004  psedlak init
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class DsTicketListBean extends ExpressListBean implements Serializable 
{
	private DsTicketOrder thisOrder = DsTicketOrder.getInstance();
	public DsTicketListBean()
	{
		super.init(thisOrder);
	}
}
