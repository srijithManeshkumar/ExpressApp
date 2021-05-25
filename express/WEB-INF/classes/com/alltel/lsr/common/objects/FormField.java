/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2003
 *                                        BY
 *                              ALLTEL COMMUNICATIONS INC.
 */
/*
 * MODULE:              FormField.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Mike Pelleschi
 *
 * DATE:        01-31-2002
 *
 * HISTORY:
 *      8.28.2003	psedlak Add bool to flag data change-this is used to only update chgs
 *			and build a history of what chgd.
 *      2.24.2004	dmartz - Added JscrptEvnt
 *			
*/
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/FormField.java  $
*/
/* $Revision:    $
*/

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class FormField {
	private int frm_sqnc_nmbr; 
	private int frm_sctn_sqnc_nmbr; 
	private int frm_sctn_occ; 
	private int frm_fld_srt_sqnc; 
	private String frm_fld_nmbr; 
	private String fld_cd;
	private String fld_data_typ;
	private int fld_lngth;
	private int req_seqno;
	private int req_versn;
	private int fld_dsply_sz;
	private String fld_dsply_actns;
	private String fld_dsply_typ;
	private String fld_frmt_msk;
	private int fld_vls_sqnc_nmbr;
	private String fld_dscrptn;
	private String db_tbl_nm; 
	private String db_clmn_nm; 
	private String src_ind; 
	private String src_db_tbl_nm; 
	private String src_db_clmn_nm; 
	private String base_table; 
	private String mox_arr; 
	private String imox_arr; 
	private String nodeData; 
	private String fld_vldtn_msg; 
	private boolean m_bDataChanged;
	private String jscrpt_evnt;

	FormField(){
		//why not?
	}
	
public 	FormField(int frs, int frsc, int fso, int ffsort, String flnum, String fldcd, String flddatatyp, int fldlngth, int flddsplysz, String flddsplyactns, String flddsplytyp, String fldfrmtmsk, int frmvlssqncnmbr, String fldesc, int reqseqno, int reqvers, String currtbl, String basetbl, String colname, String srcind, String srcdbtblnm, String srcdbclmnnm, String moxarray, String imoxarray, String fval, String je){
		this.frm_sqnc_nmbr = frs;
		this.frm_sctn_sqnc_nmbr = frsc;
		this.frm_sctn_occ = fso;
		this.frm_fld_srt_sqnc = ffsort;
		this.frm_fld_nmbr = flnum;
		this.fld_cd = fldcd;
		this.fld_data_typ = flddatatyp;
		this.fld_lngth = fldlngth;
		this.fld_dsply_sz = flddsplysz;
		this.fld_dsply_actns = flddsplyactns;
		this.fld_dsply_typ = flddsplytyp;
		this.fld_frmt_msk = fldfrmtmsk;
		this.fld_vls_sqnc_nmbr = frmvlssqncnmbr;
		this.fld_dscrptn = fldesc;
		this.req_seqno = reqseqno;
		this.req_versn = reqvers;
		this.base_table = basetbl;
		this.db_tbl_nm = currtbl;
		this.db_clmn_nm = colname;
		this.src_ind = srcind; 
		this.src_db_tbl_nm = srcdbtblnm;  
		this.src_db_clmn_nm = srcdbclmnnm; 
		this.mox_arr = moxarray; 
		this.imox_arr = imoxarray; 
		this.nodeData = (String)fval;
		this.m_bDataChanged = false;
		this.jscrpt_evnt = je;
	}
	
// Copy constructor
public 	FormField(FormField ff)
{
		this.frm_sqnc_nmbr = ff.getFrmSqncNmbr();
		this.frm_sctn_sqnc_nmbr = ff.getFrmSctnSqncNmbr();
		this.frm_sctn_occ = ff.getFrmSctnOcc();
		this.frm_fld_srt_sqnc = ff.getFrmFldSrtSqnc();
		this.frm_fld_nmbr = ff.getFrmFldNmbr();
		this.fld_cd = ff.getFldCd();
		this.fld_data_typ = ff.getFldDataTyp();
		this.fld_lngth = ff.getFldLngth();
		this.fld_dsply_sz = ff.getFldDsplySz();
		this.fld_dsply_actns = ff.getFldDsplyActns();
		this.fld_dsply_typ = ff.getFldDsplyTyp();
		this.fld_frmt_msk = ff.getFldFrmtMsk();
		this.fld_vls_sqnc_nmbr = ff.getFldVlsSqncNmbr();
		this.fld_dscrptn = ff.getFldDscrptn();
		this.req_seqno = ff.getRequestSeqno();
		this.req_versn = ff.getRequestVersn();
		this.base_table = ff.getBaseTable();
		this.db_tbl_nm = ff.getCurrentTable();
		this.db_clmn_nm = ff.getColumnName();
		this.src_ind = ff.getSrcInd();
		this.src_db_tbl_nm = ff.getSrcDbTblNm();
		this.src_db_clmn_nm = ff.getSrcDbClmnNm();
		this.mox_arr = ff.getMoxArray();
		this.imox_arr = ff.getImoxArray();
		this.nodeData = ff.getFieldData();
		this.m_bDataChanged = ff.didFieldDataChange();
		this.jscrpt_evnt = ff.getJscrptEvnt();
	}
	
public	FormField(int frs, int frsc, int fso, String flnum, String flddatatyp, int fldlngth, String fldfrmtmsk,  String fval){
		this.frm_sqnc_nmbr = frs;
		this.frm_sctn_sqnc_nmbr = frsc;
		this.frm_sctn_occ = fso;
		this.frm_fld_nmbr = flnum;
		this.fld_data_typ = flddatatyp;
		this.fld_lngth = fldlngth;
		this.fld_frmt_msk = fldfrmtmsk;
		this.nodeData = (String)fval;
		this.m_bDataChanged = false;
	}


	public int getFrmSqncNmbr(){
		return frm_sqnc_nmbr;
	}
	
	public int getFrmSctnSqncNmbr(){
		return frm_sctn_sqnc_nmbr;
	}

	public int getFrmSctnOcc(){
		return frm_sctn_occ;
	}

	public void setFrmSctnOcc(int fso){
		frm_sctn_occ = fso;
	}

	public int getFrmFldSrtSqnc(){
		return frm_fld_srt_sqnc;
	}

	public String getFrmFldNmbr(){
		return frm_fld_nmbr;
	}

	public String getFldCd(){
		return fld_cd;
	}
	
	public String getMoxArray(){
		return mox_arr;
	}
	
	public String getImoxArray(){
		return imox_arr;
	}

	public String getFldDataTyp(){
		return fld_data_typ;
	}

	public int getFldLngth(){
		return fld_lngth;
	}

	public int getFldDsplySz(){
		return fld_dsply_sz;
	}

	public String getFldDsplyActns(){
		return fld_dsply_actns;
	}

	public String getFldDsplyTyp(){
		return fld_dsply_typ;
	}

	public String getFldFrmtMsk(){
		return fld_frmt_msk;
	}

	public int getFldVlsSqncNmbr(){
		return fld_vls_sqnc_nmbr;
	}

	public String getFldDscrptn(){
		return fld_dscrptn;
	}

	public String getFieldData(){
		return nodeData;
	}

	public String getJscrptEvnt(){
		return jscrpt_evnt;
	}

	public void setFieldData(String fval){
		this.nodeData = (String)fval;
	}
	
	public String getBaseTable(){
		return base_table;
	}
	
	public void setBaseTable(String bt){
		base_table = bt;
	}
	
	public String getCurrentTable(){
		return db_tbl_nm;
	}
	
	public void setCurrentTable(String ct){
		db_tbl_nm = ct;
	}
	
	public String getColumnName(){
		return db_clmn_nm;
	}

	public String getSrcInd(){
		return src_ind;
	}
	public String getSrcDbTblNm(){
		return src_db_tbl_nm;
	}
	public String getSrcDbClmnNm(){
		return src_db_clmn_nm;
	}
	
	public int getRequestSeqno(){
		return req_seqno;
	}

	public void setRequestSeqno(int seqno){
		req_seqno = seqno;
	}

	public int getRequestVersn(){
		return req_versn;
	}
	
	public void setRequestVersn(int vrsn){
		req_versn = vrsn;
	}
	
	public void setFldVldtnMsg(String fldvldtnmsg){
		this.fld_vldtn_msg = fldvldtnmsg;
	}

	public String getFldVldtnMsg(){
		return this.fld_vldtn_msg;
	}

	public boolean didFieldDataChange() {
		return m_bDataChanged;
	}
	
	public void setFieldDataChanged() {
		m_bDataChanged = true;
	}	

	public String setJscrptEvnt() {
		return jscrpt_evnt;
	}	
}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/FormField.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:05:42   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
