package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class FormSection {
	private int m_iFrmSqncNmbr; 
	private int m_iFrmSctnSqncNmbr; 
	private String m_strFrmSctnDscrptn; 
	private String m_strFrmSctnRptInd; 

	FormSection(){
	}

	FormSection(int iFrmSqncNmbr, int iFrmSctnSqncNmbr, String strFrmSctnDscrptn, String strFrmSctnRptInd){
		this.m_iFrmSqncNmbr = iFrmSqncNmbr;
		this.m_iFrmSctnSqncNmbr = iFrmSctnSqncNmbr;
		this.m_strFrmSctnDscrptn = strFrmSctnDscrptn;
		this.m_strFrmSctnRptInd = strFrmSctnRptInd;
	}

	public int getFrmSqncNmbr(){
		return m_iFrmSqncNmbr;
	}
	
	public int getFrmSctnSqncNmbr(){
		return m_iFrmSctnSqncNmbr;
	}

	public String getFrmSctnDscrptn(){
		return m_strFrmSctnDscrptn;
	}

	public String getFrmSctnRptInd(){
		return m_strFrmSctnRptInd;
	}

}