/* EK: Remedy/Dnoc project. This is a utility file for values mapping. Map Node 
 */
package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class RemedyExpMapNode {
	private String strExpressServ; 
	private String strExpressPrty; 	
	private String strRemedyImpct; 
	private String strRemedyUrg; 
	private String strRemedyPrty;
		
	RemedyExpMapNode(){
		clean();
	}
	void clean(){
		strExpressServ = ""; 
		strExpressPrty = "";  	
		strRemedyImpct = "";  
		strExpressServ = ""; 	
		strRemedyUrg = "";  
		strRemedyPrty = ""; 
	}

	RemedyExpMapNode( String xprssServ, String xpressPrty, String rmdyImpct, String rmdyUrg, String rmdyPrty ){
		strExpressServ = xprssServ; 
		strExpressPrty = xpressPrty;  	
		strRemedyImpct = rmdyImpct;  
		strRemedyUrg =  rmdyUrg;  
		strRemedyPrty = rmdyPrty; 

	}
	public String getExpressServ(){	return strExpressServ;}	
	public String getExpressPrty(){ return strExpressPrty; }
	public String getstrRemedyImpct(){ return strRemedyImpct; }
	public String getstrRemedyUrg(){ return strRemedyUrg; }
	public String getstrRemedyPrty(){ return strRemedyPrty; }
}
