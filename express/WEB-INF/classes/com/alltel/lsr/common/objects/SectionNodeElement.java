package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class SectionNodeElement {
	private String desc; 
	private int seqno;

	SectionNodeElement(){
	}

	SectionNodeElement(String dsc, int fseq){
		this.desc = dsc;
		this.seqno = fseq;
		Log.write("SectionNodeElement: desc = " + dsc);
		Log.write("SectionNodeElement: form seq = " + fseq);
	}

	public String getSectionName(){
		return desc;
	}
	
	public int getSectionSeqno(){
		return seqno;
	}

}
