package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class FormNodeElement {
	private int form_field_sort; 
	private int form_section_seq; 
	private int field_seq; 
	private int form_seq; 
	private String form_fld_num; 
	private int form_section_num; 
	private String mox; 
	private String imox; 
	private String fdes;
	private String table; 
	private String column; 
	private String nodeData; 

	FormNodeElement(){
		//why not?
	}
	
	FormNodeElement(int frs, int frsc, String flnum, int ffsort, int fsnum, String fldesc, String fval){
		this.form_seq = frs;
		this.form_section_seq = frsc;
		this.form_fld_num = flnum;
		this.form_field_sort = ffsort;
		this.fdes = fldesc;
		this.form_section_num = fsnum;
		this.nodeData = (String)fval;
		Log.write("FormNodeElement:form seq: " + getFormSeqno());
		Log.write("FormNodeElement:form field: " + getFormFieldNo());
		Log.write("FormNodeElement:form field sort: " + getFormFieldSort());
		Log.write("FormNodeElement:form section : " + getFormSectionNum());
		Log.write("FormNodeElement:form field desc: " + getFieldDesc());
		Log.write("FormNodeElement:form field data: " + getFieldData());
	}
	
	public String getFieldData(){
		return nodeData;
	}
	
	public int getFormSeqno(){
		return form_seq;
	}
	
	public int getFormSectSeqno(){
		return form_section_seq;
	}
	
	public String getFormFieldNo(){
		return form_fld_num;
	}
	
	public int getFormSectionNum(){
		return form_section_num;
	}
	
	public int getFormFieldSort(){
		return form_field_sort;
	}
	
	public String getFieldDesc(){
		return fdes;
	}

}
