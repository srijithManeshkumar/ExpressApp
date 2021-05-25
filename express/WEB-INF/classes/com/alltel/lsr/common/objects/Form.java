package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/*
 * This class holds the information for a particular form.  It holds all the attributes for
 * that form, including its sections.
*/

public class Form
{
	private int	m_iFormSqncNmbr = 0;
	private	String	m_strFormCd = null;
	private	String	m_strFormTblNm = null;
	private int	m_iLSOGVrsn = 0;
	private	String	m_strScrtyObjctCd = null;
	private	String	m_strFormDscrptn = null;
	private	String	m_strFormActMoxIdx = null;

	private	Vector	m_vSections = new Vector(); 	// of type FormSection
	
	//Populate all attributes in constructor (Except sections)
	Form(	int	iFormSqncNmbr,
		String	strFormCd,
		String	strFormTblNm,
		int	iLSOGVrsn,
		String	strScrtyObjctCd,
		String	strFormDscrptn,
		String	strFormActMoxIdx)
	{
		this.m_iFormSqncNmbr = iFormSqncNmbr;
		this.m_strFormCd = strFormCd;
		this.m_strFormTblNm = strFormTblNm;
		this.m_iLSOGVrsn = iLSOGVrsn;
		this.m_strScrtyObjctCd = strScrtyObjctCd;
		this.m_strFormDscrptn = strFormDscrptn;
		this.m_strFormActMoxIdx = strFormActMoxIdx;
	}

	public int getFormSqncNmbr() {
		return this.m_iFormSqncNmbr;
	}
	public String getFormCd() {
		return this.m_strFormCd;
	}
	public String getFormTableName() {
		return this.m_strFormTblNm;
	}
	public int getFormLSOGVersion() {
		return this.m_iLSOGVrsn;
	}
	public String getFormSecurityObject() {
		return this.m_strScrtyObjctCd;
	}
	public String getFormDescription() {
		return this.m_strFormDscrptn;
	}
	public String getFormActMoxIdx() {
		return this.m_strFormActMoxIdx;
	}
	public Vector getFormSections() {
		return this.m_vSections;
	}
	
	public boolean addFormSection(int iSection, String strDesc, String strRepeatInd)
	{
		FormSection objFormSection = new FormSection(	this.m_iFormSqncNmbr,
								iSection,
								strDesc,
								strRepeatInd);
		this.m_vSections.addElement(objFormSection);
		return true;
	}
	
}
