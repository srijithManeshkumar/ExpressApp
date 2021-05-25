package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class MenuProfileBean {

	private Vector mainmenu;
	private Vector reportmenu;
	private Vector requestmenu;
	private Vector uadminmenu;
	private Vector tadminmenu;
	

// gets, sets, etc.......	
	
	
	public Vector getMainMenuSet() {
		return(this.mainmenu);
	}
	
	public Vector getReportMenuSet() {
		return(this.reportmenu);
	}
	
	public boolean generateMainMenu(String seclevel) {
		Connection conn = null;
		Statement stmt = null;
		mainmenu = new Vector();
		reportmenu = new Vector();
		tadminmenu = new Vector();
		
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.createStatement();
//			String query = "SELECT mn_itm_dscrptn, mn_sqnc_nmbr,mn_itm_hyprlnk from menu_item_t where mn_itm_scrty_tg <= '" + seclevel + "'" + " AND mn_itm_sqnc = 0" ;
			String query = "SELECT mn_itm_dscrptn, mn_sqnc_nmbr,mn_itm_hyprlnk from menu_item_t where mn_itm_sqnc = 0" ;
			ResultSet mrs = stmt.executeQuery(query);
			while(mrs.next()){
			   String menuname =mrs.getString("mn_itm_dscrptn"); 
			   String link =mrs.getString("mn_itm_hyprlnk"); 
			   int theseq = mrs.getInt("mn_sqnc_nmbr");
			   String mInfo = menuname + "|" + link;
			   loadMainMenuVector(mInfo);
			   int ridx = menuname.indexOf("Report");
			   Log.write("generateMainMenu: ridx = " + ridx);
			   int uidx = menuname.indexOf("User");
			   Log.write("generateMainMenu: uidx = " + uidx);
			   int tidx = menuname.indexOf("Table");
			   Log.write("generateMainMenu: tidx = " + tidx);
			   int qidx = menuname.indexOf("Request");
			   Log.write("generateMainMenu: qidx = " + qidx);
			   if(ridx >= 0) generateReportVector(theseq);
			   if(uidx >= 0) generateUadminVector();
			   if(tidx >= 0) generateTadminVector(theseq);
			   if(qidx >= 0) generateRequestVector();
			}
		} catch(Exception e) {
			e.printStackTrace();
			DatabaseManager.releaseConnection(conn);
		        return false;
		} finally {
			DatabaseManager.releaseConnection(conn);
		}
		Vector testvec = getReportMenuSet();
		Log.write("generateMainMenu: at end, report vector size = " + testvec.size());

		return true;
	}

	private boolean generateReportVector(int theseq) {
		Log.write("generateReportVector: sequence= " + theseq);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.createStatement();
		//	String query = "SELECT mn_itm_dscrptn, mn_itm_hyprlnk from menu_item_t where mn_itm_scrty_tg <= '" + seclevel + "'" + " AND mn_itm_sqnc > 0" + " AND mn_sqnc_nmbr = " + theseq ;
			String query = "SELECT mn_itm_dscrptn, mn_itm_hyprlnk from menu_itm_t where mn_itm_sqnc > 0" + " AND mn_sqnc_nmbr = " + theseq ;
			ResultSet mrs = stmt.executeQuery(query);
			while(mrs.next()){
			   String menuname =mrs.getString("mn_itm_dscrptn"); 
			   String link =mrs.getString("mn_itm_hyprlnk"); 
			   String mInfo = menuname + "|" + link;
		           Log.write("generateReportVector: loading vector with  " + mInfo);
			   loadReportVector(mInfo);
			}
		} catch(Exception e) {
			e.printStackTrace();
			DatabaseManager.releaseConnection(conn);
		        return false;
		} finally {
			DatabaseManager.releaseConnection(conn);
		}
		
		return true;
	}

	private boolean generateTadminVector(int theseq) {
		Log.write("generateTadminVector: sequence= " + theseq);
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.createStatement();
			// String query = "SELECT mn_itm_dscrptn, mn_itm_hyprlnk from menu_item_t where mn_itm_scrty_tg <= '" + seclevel + "'" + " AND mn_itm_sqnc > 0" + " AND mn_sqnc_nmbr = " + theseq ;
			 String query = "SELECT mn_itm_dscrptn, mn_itm_hyprlnk from menu_item_t where mn_itm_sqnc > 0" + " AND mn_sqnc_nmbr = " + theseq ;
			ResultSet mrs = stmt.executeQuery(query);
			while(mrs.next()){
			   String menuname =mrs.getString("mn_itm_dscrptn"); 
			   String link =mrs.getString("mn_itm_hyprlnk"); 
			   String mInfo = menuname + "|" + link;
		           Log.write("generateTadminVector: loading vector with  " + mInfo);
			   loadTadminVector(mInfo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		        Log.write("generateTadminVector: ERROR");
			DatabaseManager.releaseConnection(conn);
		        return false;
		} finally {
			DatabaseManager.releaseConnection(conn);
		}
		
		return true;
	}
	
	private void loadMainMenuVector(String theinfo){

		mainmenu.addElement(theinfo);
		return;
	}
	private void loadReportVector(String theinfo){

           Log.write("loadReportVector: loading vector with  " + theinfo);
           Log.write("loadReportVector: vector size: " + reportmenu.size());
		reportmenu.addElement(theinfo);
		return;
	}
	
	private void loadTadminVector(String theinfo){
           Log.write("loadTadminVector: loading vector with  " + theinfo);
           Log.write("loadTadminVector: vector size: " + tadminmenu.size());
		tadminmenu.addElement(theinfo);
		return;
	}

	private void generateRequestVector() {

		return;
	}
	
	private void generateUadminVector() {

		return;
	}
	
}
