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
 * MODULE:	Action.java
 * 
 * DESCRIPTION: Hold specifics for a single Action (Row from ACTION_T table).
 * 
 * AUTHOR:      Syed Hussaini
 * 
 * DATE:        11-15-2002
 * 
 * HISTORY:
 *	6-1-2004 psedlak Added ntfy_sqnc_nmbr column
 */

/* $Log:     $
*/
/* $Revision:  $
*/


package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;

import com.alltel.lsr.common.util.*;

/*
 * This class holds the information for a row of Action_T table. 
*/

public class Action
{
	private	String	m_strSttsCdFrom = null;
	private	String	m_strTypInd = null;
	private	String	m_strRqstTypCd = null;
	private	String	m_strSttsCdTo = null;
    private	String	m_strActn = null;
    private	String	m_strActnVrsnInd = null;
    private	String	m_strActnDstntn = null;
    private	String	m_strActnExprtnDys = null;
    private	String	m_strScrtyObjctCd = null;
    private	Date	m_dtMdfdDt = null;
    private	String	m_strMdfdUserid = null;
    private	String	m_strSndCustRply = null;
    private	String	m_strSndProvRply = null;
    private	String	m_strCnfrmActnInd = null;
    private	String	m_strCnfrmActnTxt = null;
    private	String	m_strNtfySqncNmbr = null;	
  	private	String	m_strPrdctTypeCD = null;	
    //number to string
 
        //Build and populate in Constructor
	public Action	(String strSttsCdFrom, String strTypInd, String strRqstTypCd,
			 String strSttsCdTo, String strActn, String strActnVrsnInd,
			 String strActnDstntn, String strActnExprtnDys, String strScrtyObjctCd,
			 Date dtMdfdDt, String strMdfdUserid, String strSndCustRply, String strSndProvRply,
             String strCnfrmActnInd, String strCnfrmActnTxt, String strNtfySqncNmbr,
             String strPrdTpCd
			)
	{
		this.m_strSttsCdFrom = strSttsCdFrom;
		this.m_strTypInd = strTypInd;
		this.m_strRqstTypCd = strRqstTypCd;
		this.m_strSttsCdTo = strSttsCdTo;
		this.m_strActn = strActn;
		this.m_strActnVrsnInd = strActnVrsnInd;
		this.m_strActnDstntn = strActnDstntn;
		this.m_strActnExprtnDys = strActnExprtnDys;
		this.m_strScrtyObjctCd = strScrtyObjctCd;
		this.m_dtMdfdDt = dtMdfdDt;
		this.m_strMdfdUserid = strMdfdUserid;
		this.m_strSndCustRply = strSndCustRply;
		this.m_strSndProvRply = strSndProvRply;
		this.m_strCnfrmActnInd = strCnfrmActnInd;
		this.m_strCnfrmActnTxt = strCnfrmActnTxt;
		this.m_strPrdctTypeCD = strPrdTpCd;
		if ( strNtfySqncNmbr == null || strNtfySqncNmbr.length() < 1 )
		{	this.m_strNtfySqncNmbr = "";
		}
		else {
                	this.m_strNtfySqncNmbr = strNtfySqncNmbr;
		}
                
 	}

	public String getSttsCdFrom() {
		return this.m_strSttsCdFrom;
	}
	public String getTypInd() {
		return this.m_strTypInd;
	}
	public String getRqstTypCd() {
		return this.m_strRqstTypCd;
	}
	public String getSttsCdTo() {
		return this.m_strSttsCdTo;
	}
	public String getActn() {
		return this.m_strActn;
	}
	public String getActnVrsnInd() {
		return this.m_strActnVrsnInd;
	}
/*
 *Return the next jsp or servlet that RequestDispatcher will forward
 * to after a change request.
 */        
	public String getActnDstntn() {
		return this.m_strActnDstntn;
	}
	public String getActnExprtnDys() {
		return this.m_strActnExprtnDys;
	}
	public String getScrtyObjctCd() {
		return this.m_strScrtyObjctCd;
	}
        
	public Date getMdfdDt() {
		return this.m_dtMdfdDt;
	}
 
	public String getMdfdUserid() {
		return this.m_strMdfdUserid;
  	}
 
	public String getSndCustRply() {
		return this.m_strSndCustRply;
	}
	public String getSndProvRply() {
		return this.m_strSndProvRply;
	}

	public String getCnfrmActnInd() {
		return this.m_strCnfrmActnInd;
	}
	public String getCnfrmActnTxt() {
		return this.m_strCnfrmActnTxt;
	}

	public String getNtfySqncNmbr() {
		return this.m_strNtfySqncNmbr;
	}
	public String getPrdctTypeCD() {
		return this.m_strPrdctTypeCD;
	}
        
}
