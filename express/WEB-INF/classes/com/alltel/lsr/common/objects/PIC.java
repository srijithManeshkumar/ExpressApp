/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS, INC.
 */

/* 
 * MODULE:		PIC.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        05-30-2002
 * 
 * HISTORY:
 *	5/30/2002  psedlak	Initial Express 2.0
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/PIC.java  $
/* 
*/

/* $Revision:     $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class PIC
{
	private String m_strPIC = " ";
	private String m_strPICName = " ";
	private String m_strFResp = " ";

	public PIC()
	{
	}

	public PIC(String aPic, String aPicName, String aFResp)
	{
		this.m_strPIC = aPic;
		this.m_strPICName = aPicName;
		this.m_strFResp = aFResp;
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getPIC() { return m_strPIC; }
	public String getPICName() { return m_strPICName; }
	public String getFResp() { return m_strFResp; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setPIC(String aPic) 
	{ 
		if (aPic != null)
			this.m_strPIC = aPic.trim(); 
		else
			this.m_strPIC = aPic; 
	}

	public void setPICName(String aPicName) 
	{ 
		if (aPicName != null)
			this.m_strPICName = aPicName.trim(); 
		else
			this.m_strPICName = aPicName; 
	}

	public void setFResp(String aFResp) 
	{ 
		if (aFResp != null)
			this.m_strFResp = aFResp.trim(); 
		else
			this.m_strFResp = aFResp; 
	}

}// end of PIC()
