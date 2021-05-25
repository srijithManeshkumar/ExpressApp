/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                        BY
 *                              ALLTEL Communications, Inc.
 */
/*
 * MODULE:	ValidationComponent.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        01-31-2002
 *
 * HISTORY:
 *      1/31/2002  initial check-in.
*	7/3/2002 copy constructor added
 *
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class ValidationComponent {
	private int m_iFrmSqncNmbr;
	private int m_iFrmSctnSqncNmbr;
	private String m_strFrmFldNmbr;
	private int val_seqno;
	private int m_iFrmFldSrtSqnc;
	private int m_iVldtnSrtOrdr;
	private String fld1; 
	private String fld1_val;
	private int fld1_pos;
	private String fld2; 
	private String fld2_val;
	private int fld2_pos;
	private String fld3; 
	private String fld3_val;
	private int fld3_pos;
	private String fld4; 
	private String fld4_val;
	private int fld4_pos;
	private String fld5; 
	private String fld5_val;
	private int fld5_pos;
	private String contrue; 
	private String confalse; 

	ValidationComponent(){
	}
	
	//Copy Constructor
	public ValidationComponent( ValidationComponent sourceVC )
	{
		this.m_iFrmSqncNmbr = sourceVC.getFrmSqncNmbr();
		this.m_iFrmSctnSqncNmbr = sourceVC.getFrmSctnSqncNmbr();
		this.m_strFrmFldNmbr = sourceVC.getFrmFldNmbr();
		this.val_seqno = sourceVC.getValSqncNmbr();
		this.m_iFrmFldSrtSqnc = sourceVC.getFrmFldSrtSqnc();
		this.m_iVldtnSrtOrdr = sourceVC.getVldtnSrtOrdr();
		this.fld1 = sourceVC.getFld1();
		this.fld1_val = sourceVC.getFld1Val();
		this.fld1_pos = sourceVC.getFld1Pos();
		this.fld2 = sourceVC.getFld2();
		this.fld2_val = sourceVC.getFld2Val();
		this.fld2_pos = sourceVC.getFld2Pos();
		this.fld3 = sourceVC.getFld3();
		this.fld3_val = sourceVC.getFld3Val();
		this.fld3_pos = sourceVC.getFld3Pos();
		this.fld4 = sourceVC.getFld4();
		this.fld4_val = sourceVC.getFld4Val();
		this.fld4_pos = sourceVC.getFld4Pos();
		this.fld5 = sourceVC.getFld5();
		this.fld5_val = sourceVC.getFld5Val();
		this.fld5_pos = sourceVC.getFld5Pos();
		this.contrue = sourceVC.getCondTrue();
		this.confalse = sourceVC.getCondFalse();
	}
	
	public ValidationComponent(int valseq){
		this.val_seqno = valseq;
	}

public 	ValidationComponent(int iFrmSqncNmbr, int iFrmSctnSqncNmbr, String strFrmFldNmbr, int valseq, int iFrmFldSrtSqnc, int iVldtnSrtOrdr, String f1, String f1val, int f1pos, String f2, String f2val, int f2pos, String f3, String f3val, int f3pos, String f4, String f4val, int f4pos, String f5, String f5val, int f5pos, String cot, String cof){
		this.m_iFrmSqncNmbr = iFrmSqncNmbr;
		this.m_iFrmSctnSqncNmbr = iFrmSctnSqncNmbr;
		this.m_strFrmFldNmbr = strFrmFldNmbr;
		this.val_seqno = valseq;
		this.m_iFrmFldSrtSqnc = iFrmFldSrtSqnc;
		this.m_iVldtnSrtOrdr = iVldtnSrtOrdr;
		this.fld1 = f1;
		this.fld1_val = f1val;
		this.fld1_pos = f1pos;
		this.fld2 = f2;
		this.fld2_val = f2val;
		this.fld2_pos = f2pos;
		this.fld3 = f3;
		this.fld3_val = f3val;
		this.fld3_pos = f3pos;
		this.fld4 = f4;
		this.fld4_val = f4val;
		this.fld4_pos = f4pos;
		this.fld5 = f5;
		this.fld5_val = f5val;
		this.fld5_pos = f5pos;
		this.contrue = cot; 
		this.confalse = cof;
	}
	
	public int getFrmSqncNmbr(){
		return m_iFrmSqncNmbr;
	}
	
	public int getFrmSctnSqncNmbr(){
		return m_iFrmSctnSqncNmbr;
	}

	public String getFrmFldNmbr(){
		return m_strFrmFldNmbr;
	}
	
	public int getValSqncNmbr(){
		return val_seqno;
	}
	
	public int getFrmFldSrtSqnc(){
		return m_iFrmFldSrtSqnc;
	}

	public int getVldtnSrtOrdr(){
		return m_iVldtnSrtOrdr;
	}

	public String getFld1(){
		return fld1;
	}
	
	public String getFld1Val(){
		return fld1_val;
	}
	
	public int getFld1Pos(){
		return fld1_pos;
	}
	
	public String getFld2(){
		return fld2;
	}
	
	public String getFld2Val(){
		return fld2_val;
	}
	
	public int getFld2Pos(){
		return fld2_pos;
	}
	
	public String getFld3(){
		return fld3;
	}
	
	public String getFld3Val(){
		return fld3_val;
	}
	
	public int getFld3Pos(){
		return fld3_pos;
	}
	
	public String getFld4(){
		return fld4;
	}
	
	public String getFld4Val(){
		return fld4_val;
	}
	
	public int getFld4Pos(){
		return fld4_pos;
	}
	
	public String getFld5(){
		return fld5;
	}
	
	public String getFld5Val(){
		return fld5_val;
	}
	
	public int getFld5Pos(){
		return fld5_pos;
	}

	public String getCondTrue(){
		return contrue;
	}
	
	public String getCondFalse(){
		return confalse;
	}
	
}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OBJECT/ValidationComponent.java  $
/*
/*   Rev 1.1   23 Apr 2002 10:17:22   dmartz
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:02   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
