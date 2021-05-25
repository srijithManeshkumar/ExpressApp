/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2003
 *					BY
 *                              ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	RequestListBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        01-01-2002
 *
 * HISTORY:
 *	psedlak 09-12-2003 Fix security hole
 *  ekalibala 05-31-2005 Add sla due date warning (add column to query)
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class RequestListBean extends ExpressListBean implements Serializable 
{
	private RequestOrder thisOrder = RequestOrder.getInstance();
	public RequestListBean()
	{
		super.init(thisOrder);
		super.setSELECTClauseBASE(" REQUEST_T.RQST_SQNC_NMBR, REQUEST_T.RQST_VRSN, TO_CHAR( REQUEST_T.SLA_DUE_DT, 'YYYYMMDD HH24MISS' )  SLA ");
	}

}
