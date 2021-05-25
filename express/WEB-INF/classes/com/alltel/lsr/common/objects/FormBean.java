/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */
/* 
 * MODULE:	FormBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        01-10-2002
 * 
 * HISTORY:
 *	03/07/2002 psedlak	Made generic for Requests and Trouble tickets.
 *	03/04/2002 psedlak	Handle creating multiple occurences of a repeating section when SUPP-ing
 *				a request.  Changed generateNewForm() for non-basetable creation.
 *	01/16/2003 psedlak	Rel 2.1
 *	09/09/2003 psedlak	remove hardcode -and have using class pass in specifics (ExpressOrder)
 *				also chgd to only update what has changed not every form/field.
 *	06/21/2005 psedlak	Dont save if nothing changed....(ie if user is justing bopping their forms,
 *				dont update it)
 *	09/21/2005 dmartz	Only allow a preset maximum number of sections to be added to a form
 *	10/17/2005 EK: Modify code to bypass version field when updating form per tables as it updates 
 				RQST_VERSION field automatically when status changes
 */

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/FormBean.java  $
/*
/*   Rev 1.3.1.7   Jul 06 2005 09:52:40   e0069884
/* 
/*
/*   Rev 1.3.1.4   Jul 31 2002 08:20:02   sedlak
/*PVCS cleanup (promote across)
/*
/*   Rev 1.3.1.3   Jun 06 2002 12:34:30   sedlak
/*Correct prepopulate -was still referencing RQST_SQNC_NMBR
/*
/*   Rev 1.3.1.2   Jun 05 2002 14:53:32   dmartz
/* 
/*
/*   Rev 1.3.1.0   22 Mar 2002 07:52:42   sedlak
/*Made generic for LSRs and TTs
/*
/*   Rev 1.4   05 Mar 2002 07:26:48   sedlak
/*Modified generateNewForm() to create multiple occurences
/*if a Request is being SUPP-ed.
/*
/*   Rev 1.3   19 Feb 2002 10:55:26   sedlak
/*added closes to Statements and ResultSets
/*
/*   Rev 1.2   13 Feb 2002 14:18:58   dmartz
/*Release 1.1
/*
/*   Rev 1.0   23 Jan 2002 11:05:40   wwoods
/*Initial Checkin
*/

/* $Revision:   1.3.1.7  $
*/

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class FormBean {

	ExpressOrder myOrder;	//Defines what kind of forms & tables we'll use

	private Vector vSection = new Vector();
	private Vector vFormDetail = new Vector();
	private Vector vEmptySet = new Vector();
	private String m_strUserid = "N/A";
	private String m_strTimeStamp = "" ;
	private int m_iStoreVrsn = 0;
	private Connection conn = null;

	public FormBean (ExpressOrder expressOrder)
	{
		myOrder = expressOrder;
		Log.write(Log.DEBUG_VERBOSE, "FormBean -- Constructor 1");
	}

	public FormBean (int iFormCategory)
	{
		Log.write(Log.DEBUG_VERBOSE, "FormBean -- Constructor 2 --OBSOLETE ! ");
	}

	public void setFormUserid(String strUserid) {

		m_strUserid = strUserid;
	
	}

	public void setFormTimeStamp(String strTimeStamp) {

		m_strTimeStamp = strTimeStamp;
	
	}

	public void setFormConnection(Connection connection) {

		this.conn = connection;
	
	}

	public Vector getFormFields(int form_seqno, int iSeqno, int req_vers) 
		throws SQLException
	{        
		Vector vFormFields = new Vector();

                int iFrmSeqNumber=0;
                int iFrmSectnSeq=0;
                int iFrmFieldSort=0;
                int iFieldLength=0;
                int iFrmFieldDsplySz=0;
                int iFrmFieldVlsSqncNmbr=0;
                int iFrmSctnOcc=0;
                
                String strFrmFieldNum="";
                String strFrmFieldCd = "";
                String strFrmFieldDataTyp = "";
                String strFrmFieldDsplyActns = "";
                String strFrmFieldDsplyTyp = "";
                String strFrmFieldFrmtMsk = "";
                String strFieldDescr = "";
		String strFieldSrcInd = "";
                String strFieldMoxArray = "";
                String strFieldIMoxArray = "";
                String strFieldSrcDbTblNm = "";
                String strFieldSrcDbClmnNm = "";
                String strFieldData = "";
                
		vFormDetail.clear();

		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();

		// get FormFields instance
		FormFields ffs = FormFields.getInstance();

                String fquery = "SELECT frm_tbl_nm FROM form_t WHERE frm_sqnc_nmbr = " + form_seqno;
		Log.write(Log.DEBUG_VERBOSE, "pjs FormBean -- fquery="+fquery);
		ResultSet frs = stmt.executeQuery(fquery);
                if ( frs.next() == false)
                {
                    Log.write(Log.ERROR, "FormBean.getFormFields() on frs query");
                    return null;
                }
		String formtablename = frs.getString("frm_tbl_nm");
		Log.write(Log.DEBUG_VERBOSE, "FormBean: getFormData: Form Table name: " + formtablename);
                frs.close();
                frs=null;
                
		String sectqry = "select frm_sctn_dscrptn, frm_sctn_sqnc_nmbr, db_tbl_nm from form_section_t where frm_sqnc_nmbr = " + form_seqno + " ORDER BY frm_sctn_sqnc_nmbr";
	        ResultSet sect_modelrs = stmt.executeQuery(sectqry);
		Log.write(Log.DEBUG_VERBOSE, "pjs FormBean -- sectqry="+sectqry);

		String mformqry = "select * from " + formtablename + " where " + myOrder.getSQNC_COLUMN() + " = " + iSeqno + " and " +
			 myOrder.getVRSN_COLUMN() + " = " + req_vers;
		Log.write(Log.DEBUG_VERBOSE, "pjs FormBean -- mformqry="+mformqry);
	        ResultSet form_rs = stmt2.executeQuery(mformqry);
		form_rs.next();

		//NOTE NOTE NOTE - This assumes SEQUENCE NUMBER is the FIRST column in TABLE !! 
		int thisformreq = form_rs.getInt(1);
		//Log.write(Log.DEBUG_VERBOSE, "FormBean: getFormData: run main block for " + formtablename + " seq number " + thisformreq);
                
		while(sect_modelrs.next())
                {
		    //Log.write(Log.DEBUG_VERBOSE, "pjs FormBean: cycling thru sect_modelrs");
                    String fsdesc = sect_modelrs.getString("frm_sctn_dscrptn");
		    int thisfsseq = sect_modelrs.getInt("frm_sctn_sqnc_nmbr");
		    String fstblnm = sect_modelrs.getString("db_tbl_nm");
		    //Log.write("getFormData: form section " + fsdesc + " table name: " + fstblnm);
		    if(fstblnm.equals(formtablename))
                    {
			// Time to fill in the holes in FormFields for this specific form and section
			vFormFields.clear();
			vFormFields = ffs.getFormFields(form_seqno, thisfsseq);
			Iterator it = vFormFields.iterator();
                        while (it.hasNext())
                        {
			    FormField ff = new FormField();
			    ff = (FormField)it.next();
			    ff.setRequestSeqno(iSeqno);
			    ff.setRequestVersn(req_vers);
			    ff.setCurrentTable(fstblnm);
			    ff.setBaseTable(formtablename);
			    ff.setFieldData((String)form_rs.getString(ff.getColumnName()));

                            vFormDetail.addElement(ff);
					
			} // end WHILE
                    } 
                    else 
                    {
                        //Log.write("getFormData: table name for section is not equal to the primary table " + formtablename + " ; getting data from section table");
			String strSctnQry2 = "select * from " + fstblnm + " where " + myOrder.getSQNC_COLUMN() + " = " +
						 iSeqno + " and " +   myOrder.getVRSN_COLUMN() + " = " + req_vers + " order by frm_sctn_occ" ;
                        Statement stmt4 = conn.createStatement();
                        ResultSet sctn_modelrs2 = stmt4.executeQuery(strSctnQry2);
                        //Log.write(Log.DEBUG_VERBOSE,"PJS ["+ strSctnQry2 + "]"); -- commented unnecessary log message - Antony 05/18/10
			// Get FormFields for this form and section
			vFormFields.clear();
                        vFormFields = ffs.getFormFields(form_seqno, thisfsseq);

			// Loop thru all OCCs
			while(sctn_modelrs2.next())
                        {
			    // Time to fill in the holes in FormFields for this specific form and section
                            iFrmSctnOcc = sctn_modelrs2.getInt("frm_sctn_occ");
                            Iterator it = vFormFields.iterator();

			    // Loop thru each FormField for this particular OCC
                            while (it.hasNext())
                            {
				// Grab next FormField from iterator
				FormField ff = new FormField((FormField)it.next());

				// Set data specific to this form,section,occ
				ff.setFrmSctnOcc(iFrmSctnOcc);
                            	ff.setRequestSeqno(iSeqno);
                            	ff.setRequestVersn(req_vers);
                            	ff.setCurrentTable(fstblnm);
                            	ff.setBaseTable(formtablename);
                                //Log.write(Log.DEBUG_VERBOSE,"PJS ["+ ff.getColumnName() +  " ]");-- commented unnecessary log message - Antony 05/18/10

                            	ff.setFieldData((String)sctn_modelrs2.getString(ff.getColumnName()));

				// Add updated FormField element to Vector
                            	vFormDetail.addElement(ff);

                            } //while()
                        } // end WHILE
                        sctn_modelrs2.close();
                        sctn_modelrs2=null;
                        stmt4.close();
                        stmt4=null;
                        
                    } // end-else
                    
		} //while()
                form_rs.close();
                form_rs=null;
                stmt2.close();
                stmt2 = null;
        
                sect_modelrs.close();
                sect_modelrs = null;
                stmt.close();
                stmt = null;
                     
		return(this.vFormDetail);
	}
	
	public boolean generateNewForm(int form_seqno, int req_seq, int req_ver) 
		throws SQLException
	{
		Statement stmt1 = null;
		Statement stmt3 = null;
		String  insrt = null;
		String basetable = null;
		Vector vScan = new Vector();
		Log.write(Log.DEBUG_VERBOSE,"pjs new form="+form_seqno+" req_seq="+req_seq+" req_ver="+req_ver);
                
                int iReqOcc = 1;
                Vector vOccs = new Vector(); //Holds occurences numbers for repeating sections
               
		stmt1 = conn.createStatement();

		String query = "SELECT frm_tbl_nm FROM form_t WHERE frm_sqnc_nmbr = " + form_seqno ;
		//Log.write(Log.DEBUG_VERBOSE,"pjs query="+query);
		ResultSet rs = stmt1.executeQuery(query);
		if(rs.next()){
			basetable = rs.getString("frm_tbl_nm");
			Log.write(Log.DEBUG_VERBOSE,"pjs 1");
		}else{
		        Log.write(Log.DEBUG_VERBOSE, "FormBean: generateNewForm: empty result set: form_t ");
                        stmt1.close();  //will close rs too
			return(false);
		}
		Log.write(Log.DEBUG_VERBOSE,"pjs basetable="+basetable);
                
                stmt3 = conn.createStatement();
                
		String query1 = "SELECT DISTINCT db_tbl_nm, frm_sctn_sqnc_nmbr, frm_sctn_rpt_ind " +
                                " FROM form_section_t WHERE frm_sqnc_nmbr = " + form_seqno + " order by frm_sctn_sqnc_nmbr";
		Log.write(Log.DEBUG_VERBOSE,"pjs query1="+query1);
                
		rs = stmt1.executeQuery(query1);
		while(rs.next()){
			int vFlag = 0;
			String thistable = rs.getString("db_tbl_nm");
			String strRepeat = rs.getString("frm_sctn_rpt_ind");
			//Log.write(Log.DEBUG_VERBOSE,"pjs thistable="+thistable + "   strRepeat="+strRepeat);-- commented unnecessary log message - Antony 05/18/10
			for(int i =0;i<vScan.size();i++){
				String tName = (String)vScan.elementAt(i);
				//Log.write("vScan " + i + " equals " + tName);
				if(thistable.equals(tName)){
				        //Log.write(tName + " already entered");
					vFlag++;
				}
			}
			if(vFlag == 0)
			{
			  Log.write(Log.DEBUG_VERBOSE,"pjs vFlag=0");
			  // Query needed for defaults
                          String strDefaultQuery = "SELECT * FROM DEFAULT_T D, FORM_FIELD_T F " +
				" WHERE F.FRM_SQNC_NMBR = " + form_seqno + " AND F.FRM_SQNC_NMBR = D.FRM_SQNC_NMBR" +
				" AND F.FRM_SCTN_SQNC_NMBR = D.FRM_SCTN_SQNC_NMBR" +
				" AND F.FRM_FLD_NMBR = D.FRM_FLD_NMBR AND F.DB_TBL_NM = '" + thistable + "' " +
				" ORDER BY F.FRM_SCTN_SQNC_NMBR, F.FRM_FLD_SRT_SQNC";
                
                          String strDefaultQuery2 = "SELECT * FROM DEFAULT_USERID_T D, FORM_FIELD_T F " +
				" WHERE F.FRM_SQNC_NMBR = " + form_seqno + " AND F.FRM_SQNC_NMBR = D.FRM_SQNC_NMBR" +
				" AND F.FRM_SCTN_SQNC_NMBR = D.FRM_SCTN_SQNC_NMBR" +
				" AND F.FRM_FLD_NMBR = D.FRM_FLD_NMBR" +
				" AND F.DB_TBL_NM = '" + thistable + "' AND D.USERID = '" + m_strUserid + "' " +
                                " ORDER BY F.FRM_SCTN_SQNC_NMBR, F.FRM_FLD_SRT_SQNC";
                          
 			  if(thistable.equals(basetable))
			  {
			   	Log.write(Log.DEBUG_VERBOSE,"pjs tbls =");
				// INSERT new row
				insrt = "insert into " + thistable +" (" + myOrder.getSQNC_COLUMN() + "," +  myOrder.getVRSN_COLUMN() +
					 ", mdfd_dt, mdfd_userid) " + " values(" + req_seq + "," + req_ver + "," +
					 m_strTimeStamp + ", '" + m_strUserid + "')";
		          	stmt3.executeUpdate(insrt);

				// Build UPDATE Statement for DEFAULT_T
				ResultSet rs2 = stmt3.executeQuery(strDefaultQuery);
				String strUpdate = "UPDATE " + thistable + " SET ";
				boolean bFound = false;
				while (rs2.next())
				{
					strUpdate = strUpdate + rs2.getString("DB_CLNM_NM") + " = '" + rs2.getString("DFLT_VL") + "',";
					bFound = true;
				}

				if (bFound)
				{
					if (strUpdate.endsWith(","))
						strUpdate = strUpdate.substring(0,strUpdate.length()-1);
					strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq +
						 " AND " +  myOrder.getVRSN_COLUMN() + " = " + req_ver; 
					stmt3.executeUpdate(strUpdate);
				}

				// Build UPDATE Statement for DEFAULT_USERID_T
				rs2 = stmt3.executeQuery(strDefaultQuery2);
				strUpdate = "UPDATE " + thistable + " SET ";
				bFound = false;
				while (rs2.next())
				{
					strUpdate = strUpdate + rs2.getString("DB_CLNM_NM") + " = '" + rs2.getString("DFLT_VL") + "',";
					bFound = true;
				}

				if (bFound)
				{
					if (strUpdate.endsWith(","))
						strUpdate = strUpdate.substring(0,strUpdate.length()-1);
					strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq + 
						" AND " +  myOrder.getVRSN_COLUMN() + " = " + req_ver; 
					stmt3.executeUpdate(strUpdate);
				}
			  }
			  else
			  {
				Log.write(Log.DEBUG_VERBOSE,"pjs tbls =");
                                vOccs.removeAllElements();
                                
                                //If not basetable AND the section can repeat AND this is not the original request creation,
                                //then we need to create a repeating occurence for each occurrence that was in the prior version of the
                                //request.
                                if (req_ver > 0 && strRepeat.equals("Y")) 
                                {
				    Log.write(Log.DEBUG_VERBOSE,"pjs strRepeat=Y");
                                    int iOldReq = req_ver - 1;
                                    //Build vector to hold our original occurences. We do this becuase the original request may have
                                    //had occur 1, 2, 4, 5  (due to add/delect sections functionality)
                                    String strGetOccurences = "SELECT FRM_SCTN_OCC FROM " + thistable + " WHERE " + myOrder.getSQNC_COLUMN() +
								 " = " + req_seq + " AND " +  myOrder.getVRSN_COLUMN() + " = " + iOldReq;
				    Log.write(Log.DEBUG_VERBOSE,"pjs strGetOccurences=["+strGetOccurences+"]");
                                    ResultSet rs2 = stmt3.executeQuery(strGetOccurences);
                                    while (rs2.next())
                                    {
                                            vOccs.addElement( new Integer(rs2.getInt("FRM_SCTN_OCC")) );
                                    }
                                    rs2.close();
                                 }
                                else
                                {   vOccs.addElement( new Integer(1) ); 
                                }
                                
				Log.write(Log.DEBUG_VERBOSE,"pjs vOccs.size()="+vOccs.size());
                                for (int i=0; i < vOccs.size(); i++)
                                {
                                    iReqOcc = ((Integer)vOccs.elementAt(i)).intValue();
                                    Log.write(Log.DEBUG_VERBOSE, " INSERTing repeating section <" +thistable+">  occur = " + iReqOcc);
                                    
                                    // INSERT new row
                                    insrt = "insert into " + thistable +"(" + myOrder.getSQNC_COLUMN() + ", " + myOrder.getVRSN_COLUMN() +
					    ", frm_sctn_occ, mdfd_dt, mdfd_userid) " +
                                            " values(" + req_seq + "," + req_ver + "," + iReqOcc + "," + m_strTimeStamp + ", '" + m_strUserid + "')";
				    Log.write(Log.DEBUG_VERBOSE, "pjs insrt=["+insrt+"]");
                                    stmt3.executeUpdate(insrt);

                                    // Build UPDATE Statement for DEFAULT_T
                                    ResultSet rs2 = stmt3.executeQuery(strDefaultQuery);
                                    String strUpdate = "UPDATE " + thistable + " SET ";
                                    boolean bFound = false;
                                    while (rs2.next())
                                    {
					strUpdate = strUpdate + rs2.getString("DB_CLNM_NM") + " = '" + rs2.getString("DFLT_VL") + "',";
					bFound = true;
                                    }   

                                    if (bFound)
                                    {
					if (strUpdate.endsWith(","))
						strUpdate = strUpdate.substring(0,strUpdate.length()-1);
					strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq + " AND " +
						 myOrder.getVRSN_COLUMN() + " = " + req_ver +
						" AND FRM_SCTN_OCC = 1"; 
					stmt3.executeUpdate(strUpdate);
                                    }

                                    // Build UPDATE Statement for DEFAULT_USERID_T
                                    rs2 = stmt3.executeQuery(strDefaultQuery2);
                                    strUpdate = "UPDATE " + thistable + " SET ";
                                    bFound = false;
                                    while (rs2.next())
                                    {
					strUpdate = strUpdate + rs2.getString("DB_CLNM_NM") + " = '" + rs2.getString("DFLT_VL") + "',";
					bFound = true;
                                    }

                                    if (bFound)
                                    {
					if (strUpdate.endsWith(","))
						strUpdate = strUpdate.substring(0,strUpdate.length()-1);
					strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq + " AND " +
						 myOrder.getVRSN_COLUMN() + " = " + req_ver +
						" AND FRM_SCTN_OCC = 1"; 
					stmt3.executeUpdate(strUpdate);
                                    }
                                    
                                }// end of for()
                                
			  }
			  
			  vScan.addElement(thistable);
			}
		}
                rs.close();
		stmt1.close();
		stmt3.close();

                vScan.removeAllElements();
                
		return(true);
	}
	
	public boolean deleteForm(int form_seqno, int req_seq, int req_ver) 
		throws SQLException
	{
		Statement stmt1 = null;
		Statement stmt2 = null;
		Statement stmt3 = null;
		String basetable = null;
		Vector vScan = new Vector();

		stmt1 = conn.createStatement();
		String query = "SELECT frm_tbl_nm FROM form_t WHERE frm_sqnc_nmbr = " + form_seqno ;
		ResultSet rs1 = stmt1.executeQuery(query);
		if(rs1.next()){
			basetable = rs1.getString("frm_tbl_nm");
		}else{
		        Log.write(Log.DEBUG_VERBOSE, "FormBean: deleteNewForm: empty result set: form_t ");
			return(false);
		}
		stmt2 = conn.createStatement();
		String query1 = "SELECT DISTINCT db_tbl_nm, frm_sctn_sqnc_nmbr FROM form_section_t WHERE frm_sqnc_nmbr = " + form_seqno + " order by frm_sctn_sqnc_nmbr desc";
		ResultSet rs = stmt2.executeQuery(query1);
		while(rs.next()){
			int vFlag = 0;
			String thistable = rs.getString("db_tbl_nm");
			int thisfsseq = rs.getInt("frm_sctn_sqnc_nmbr");
			for(int i =0;i<vScan.size();i++){
				String tName = (String)vScan.elementAt(i);
				//Log.write("vScan (delete)" + i + " equals " + tName);
				if(thistable.equals(tName)){
				        //Log.write(tName + " already deleted!");
					vFlag++;
				}
			}
			if(thisfsseq > 1 && thistable.equals(basetable)){
			     // Drop the base table last....skip the delete for now.
			     //Log.write(thistable + " sect seq: " + thisfsseq + " : skip");
			     vFlag++;
			}
			if(vFlag == 0){
			  stmt3 = conn.createStatement();
			  String delrec = "delete from " + thistable + " where " + myOrder.getSQNC_COLUMN() + " = " + req_seq + 
					" and " + myOrder.getVRSN_COLUMN() + " = " + req_ver;
		          stmt3.executeUpdate(delrec);
			  vScan.addElement(thistable);
			  //Log.write("Section insert vector: " + vScan.size());
			}
		}
	        	
		return(true);
	}
	
	public int generateSection(int form_seqno, int req_seq, int req_ver, int sect_seq) 
		throws SQLException
	{
		Statement stmt1 = null;
		int currOcc = -1;

		stmt1 = conn.createStatement();
		String query1 = "SELECT db_tbl_nm from form_section_t WHERE frm_sqnc_nmbr = " + form_seqno + " and frm_sctn_sqnc_nmbr = " + sect_seq;
		ResultSet rs1 = stmt1.executeQuery(query1);
		if(!rs1.next())
		{
			Log.write(Log.ERROR, "FormBean: generateSection: ERROR: Could not find section sequence number" + sect_seq + " (FORM NUMBER " + form_seqno + ")" );
	        	return(-1);
		}

		String thistable = rs1.getString("db_tbl_nm");
		rs1.close();

		query1 = "SELECT max(frm_sctn_occ) as maxocc from " + thistable + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq +
			 " and " + myOrder.getVRSN_COLUMN() + " = " + req_ver;
		rs1 = stmt1.executeQuery(query1);
		if(!rs1.next())
		{
	   		Log.write(Log.ERROR,"FormBean: generateSection: ERROR: Could not find max section occurrence for " + req_seq + " (SECTION " + sect_seq + ")" );
           		return(-1);
		}

		currOcc = rs1.getInt("maxocc");
		rs1.close();

		// Only create a new section, if the max has not yet been met
		int iMaxNumOccs;
		try
		{
			iMaxNumOccs = PropertiesManager.getIntegerProperty("lsr.maxsections",100); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
			iMaxNumOccs = 100;
		}

		if (currOcc >= iMaxNumOccs)
		{
			rs1 = null;
			stmt1.close();
			stmt1 = null;
	
			return(currOcc);
		}

		currOcc++;
		String insrec = "insert into " + thistable +"(" + myOrder.getSQNC_COLUMN() + ", " + myOrder.getVRSN_COLUMN() +
			 ", frm_sctn_occ, mdfd_dt, mdfd_userid) values(" + req_seq + "," + req_ver + "," + currOcc +
			 "," + m_strTimeStamp + ", '" + m_strUserid + "')" ;
		stmt1.executeUpdate(insrec);

		// Query needed for defaults
		String strQuery = "SELECT * FROM DEFAULT_T D, FORM_FIELD_T F " +
			" WHERE F.FRM_SQNC_NMBR = " + form_seqno + 
			" AND F.FRM_SQNC_NMBR = D.FRM_SQNC_NMBR" +
			" AND F.FRM_SCTN_SQNC_NMBR = D.FRM_SCTN_SQNC_NMBR" +
			" AND F.FRM_FLD_NMBR = D.FRM_FLD_NMBR" +
			" AND F.DB_TBL_NM = '" + thistable + "'" +
			" ORDER BY F.FRM_SCTN_SQNC_NMBR, F.FRM_FLD_SRT_SQNC";

		String strQuery2 = "SELECT * FROM DEFAULT_USERID_T D, FORM_FIELD_T F " +
			" WHERE F.FRM_SQNC_NMBR = " + form_seqno + 
			" AND F.FRM_SQNC_NMBR = D.FRM_SQNC_NMBR" +
			" AND F.FRM_SCTN_SQNC_NMBR = D.FRM_SCTN_SQNC_NMBR" +
			" AND F.FRM_FLD_NMBR = D.FRM_FLD_NMBR" +
			" AND F.DB_TBL_NM = '" + thistable + "'" +
			" AND D.USERID = '" + m_strUserid + "'" +
			" ORDER BY F.FRM_SCTN_SQNC_NMBR, F.FRM_FLD_SRT_SQNC";

		// Build UPDATE Statement for DEFAULT_T
		rs1 = stmt1.executeQuery(strQuery);
		String strUpdate = "UPDATE " + thistable + " SET ";
		boolean bFound = false;
		while (rs1.next())
		{
		   	strUpdate = strUpdate + rs1.getString("DB_CLNM_NM") + " = '" + rs1.getString("DFLT_VL") + "',";
		   	bFound = true;
		}
		rs1.close();

		if (bFound)
		{
		   	if (strUpdate.endsWith(","))
		   		strUpdate = strUpdate.substring(0,strUpdate.length()-1);
		   	strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq + " AND " +
				 myOrder.getVRSN_COLUMN() + " = " + req_ver +
		   		" AND FRM_SCTN_OCC = " + currOcc; 
		   	stmt1.executeUpdate(strUpdate);
		}
   
   		// Build UPDATE Statement for DEFAULT_USERID_T
   		rs1 = stmt1.executeQuery(strQuery2);
   		strUpdate = "UPDATE " + thistable + " SET ";
   		bFound = false;
	   	while (rs1.next())
   		{
   		   	strUpdate = strUpdate + rs1.getString("DB_CLNM_NM") + " = '" + rs1.getString("DFLT_VL") + "',";
   		   	bFound = true;
   		}

		if (bFound)
		{
		   	if (strUpdate.endsWith(","))
		   		strUpdate = strUpdate.substring(0,strUpdate.length()-1);
		   	strUpdate = strUpdate + " WHERE " + myOrder.getSQNC_COLUMN() + " = " + req_seq + " AND " +
				 myOrder.getVRSN_COLUMN() + " = " + req_ver +
		   		" AND FRM_SCTN_OCC = " + currOcc; 
		   	stmt1.executeUpdate(strUpdate);
		}
	 
		rs1.close();
		rs1 = null;
		stmt1.close();
		stmt1 = null;

		return(currOcc);
	}
	
	public boolean deleteSection(int form_seqno, int req_seq, int req_ver, int sect_seq, int occ) 
		throws SQLException
	{
		Statement stmt1 = null;
		Statement stmt2 = null;

		stmt1 = conn.createStatement();
		String query1 = "SELECT db_tbl_nm from form_section_t WHERE frm_sqnc_nmbr = " + form_seqno + " and frm_sctn_sqnc_nmbr = " + sect_seq;
		ResultSet rs1 = stmt1.executeQuery(query1);
		if(rs1.next()){
			String thistable = rs1.getString("db_tbl_nm");
			stmt2 = conn.createStatement();
			String delrec = "delete from " + thistable + " where " + myOrder.getSQNC_COLUMN() + " = " + req_seq + " and " +
				 myOrder.getVRSN_COLUMN() + " = " + req_ver + " and frm_sctn_occ = " + occ;
			 stmt2.executeUpdate(delrec);
		}else{
		   Log.write(Log.ERROR,"FormBean: deleteSection: ERROR: Could not find table name for sequence number" + sect_seq + " (FORM NUMBER " + form_seqno + ")" );
	           return(false);
		}
	        	
		return(true);
	}
	
	public void setStoreFormVrsn(int iVrsn) {
		m_iStoreVrsn = iVrsn;
	}

	public boolean storeForm(Vector v) 
		throws SQLException
	{
		return this.storeForm(v, false);	
	}

	//If newversion is set to 'true', then logic to only update fields that changd is bypassed and all fields are
	// updated (in new version of form).
	public boolean storeForm(Vector v, boolean bNewVersion) 
		throws SQLException
	{
		Statement stmt1 = null;
		boolean bSomethingChanged = false;	// On if new version or anthing changed!
		
		stmt1 = conn.createStatement();
		Log.write(Log.DEBUG_VERBOSE, "FormBean.storeForm() --- new ver = " + bNewVersion);
		  
		int cRs = 0;
		int cRv = 0;
		int cOc = 0;

		String q1 = null;
		String uParms = "";	//builds UPDATE stmts - and used to determine if UPDATE is necessary!
		if(v.size() <= 0){
		    Log.write(Log.ERROR,"FormBean.storeForm(): !ERROR! Vector dimensions invalid.");;
		    return(false);
		}
		FormField iNode = (FormField)v.elementAt(0);
		String tCurrent = iNode.getCurrentTable();
		String tbase = iNode.getBaseTable();
		int tOcc = iNode.getFrmSctnOcc();
		//Log.write(Log.DEBUG_VERBOSE, "storeForm: vector size = " + v.size());
		//Log.write(Log.DEBUG_VERBOSE, "storeForm: initial table = " + tCurrent);

		for(int i = 0; i<v.size(); i++)
		{
			FormField fNode = (FormField)v.elementAt(i);
			int fsn = fNode.getFrmSqncNmbr();
			int fssn = fNode.getFrmSctnSqncNmbr();
			int ffss = fNode.getFrmFldSrtSqnc();
			int rsn = fNode.getRequestSeqno();
			int rvn = m_iStoreVrsn;
			int occ = fNode.getFrmSctnOcc();
			String btbl = fNode.getBaseTable();
			String ctbl = fNode.getCurrentTable();
			String coln = fNode.getColumnName();
			String ffn = fNode.getFrmFldNmbr();
			String thedata = fNode.getFieldData();

			// pjs Change to only build update stmts for data that actually changed...
			if ( (fNode.didFieldDataChange() || bNewVersion)
				&& !coln.equalsIgnoreCase(myOrder.getVRSN_COLUMN()) )
			{
				bSomethingChanged = true;
Log.write(Log.DEBUG_VERBOSE, "storeForm: OCC VALUE: " + coln);
Log.write(Log.DEBUG_VERBOSE, "storeForm: BASE TABLE: " + myOrder.getVRSN_COLUMN());
				if (!bNewVersion) {
					Log.write(Log.DEBUG_VERBOSE, "FormBean.storeForm(): field "+fsn+":"+fssn+":"+occ+":"+ffn+
						" DID CHANGE - data=["+thedata+"]");
				}
		//		Log.write(Log.DEBUG_VERBOSE, "storeForm: info: " + fsn + "," + fssn + "," + ffss + "," + ffn + "," + rsn + "," + rvn);
		//		Log.write(Log.DEBUG_VERBOSE, "storeForm: OCC VALUE: " + occ);
		//		Log.write(Log.DEBUG_VERBOSE, "storeForm: BASE TABLE: " + btbl);
		//		Log.write(Log.DEBUG_VERBOSE, "storeForm: CURRENT TABLE: " + ctbl);
		//		Log.write(Log.DEBUG_VERBOSE, "storeForm: COLUMN NAME: " + coln);
				if(thedata == null){
				   thedata = "";
				}
				thedata = thedata.trim();
				//Log.write(Log.DEBUG_VERBOSE, "storeForm: POST trim len : " + thedata.length());
				thedata = replaceSpecialChars(thedata);
				//Log.write(Log.DEBUG_VERBOSE, "storeForm: POST replaceSpecialChars : " + thedata);
				thedata = Toolkit.replaceSingleQwithDoubleQ(thedata);

				//Make sure length of FORM data doesnt exceed our database column width.
				//This can happen with TEXTAREAs especially. Here we truncate data to
				//specified max length.
				if (thedata.length() > fNode.getFldLngth()) {
					Log.write(Log.DEBUG_VERBOSE,"FormBean.storeForm(): data to be truncated ! Field=" +
						ffn + " Column=" + coln + " len=" + thedata.length() +
						" Allowable len=" + fNode.getFldLngth());
					String truncdata = thedata.substring(0, fNode.getFldLngth());
					thedata = truncdata;
					//Log.write(Log.DEBUG_VERBOSE,"storeForm: new length= " + thedata.length());
				}
			}

			
			//Log.write(Log.DEBUG_VERBOSE, "storeForm: tCurrent = " + tCurrent + " ctbl = " + ctbl + " base = " + btbl);
			if(ctbl.equals(tCurrent) && occ == tOcc){
				//Log.write(Log.DEBUG_VERBOSE, "TABLE_EQUALITY: ctbl = tCurrent: ctbl = " + ctbl + "tCurrent = " + tCurrent);
				
				if (fNode.didFieldDataChange() || bNewVersion
					&& !coln.equalsIgnoreCase(myOrder.getVRSN_COLUMN()) ) {
					uParms = uParms + coln + " = " + "'" + thedata + "'" + ",";
					Log.write(Log.DEBUG_VERBOSE, "Revised uParms = " + uParms);
				}
				cRs = rsn;
				cRv = rvn;
				cOc = occ;
			}else{
				//Log.write(Log.DEBUG_VERBOSE, "TABLE_INEQUALITY (UPDATE REQUIRED): ctbl = tCurrent: ctbl = " +
				// ctbl + "tCurrent = " + tCurrent);

				if(tCurrent.equals(tbase)){
			   	    //Log.write(Log.DEBUG_VERBOSE, "BASETABLE EQUALITY:  " + tCurrent + "," + tbase);
				    q1 = "update " + tCurrent + " set " + uParms + " MDFD_DT = " + m_strTimeStamp + ", MDFD_USERID = '" +
					 m_strUserid + "' where " + myOrder.getSQNC_COLUMN() + " = " + cRs + " and " +
					 myOrder.getVRSN_COLUMN() + " = " + cRv;
				}else{
			   	    //Log.write(Log.DEBUG_VERBOSE, "BASETABLE INEQUALITY:  " + tCurrent + "," + btbl);
				    q1 = "update " + tCurrent + " set " + uParms + " MDFD_DT = " + m_strTimeStamp + ", MDFD_USERID = '" +
					 m_strUserid + "' where " + myOrder.getSQNC_COLUMN() + " = " + cRs + " and " +
					 myOrder.getVRSN_COLUMN() + " = " + cRv + " and frm_sctn_occ = " + tOcc;
				}

				tCurrent = ctbl;
				tOcc = occ;
				tbase = btbl;
			        //Log.write(Log.DEBUG_VERBOSE, "GLOBAL update: tCurrent now set to " + tCurrent +
				// ", tOcc now set to " + tOcc + " cOc set to " + cOc);
				// Only update if something changes man
				if (uParms != null && uParms.length() > 0)
				{
					Log.write(Log.DEBUG_VERBOSE,"\n0000000000   FormBean.storeForm(): UPDATE=["+q1+"]");
					if (stmt1.executeUpdate(q1) <= 0)
					{
						throw new SQLException(null,null,100);
					}
				}
				uParms = "";
				q1 = "";

				if ( (fNode.didFieldDataChange() || bNewVersion )
						&& !coln.equalsIgnoreCase(myOrder.getVRSN_COLUMN() ) )   {
					uParms = uParms + coln + " = " + "'" + thedata + "'" + ",";
			   		//Log.write(Log.DEBUG_VERBOSE, "END OF ELSE: uParms:  " + uParms);
				}
			}
			
	       }

		if(tCurrent.equals(tbase)){
		   	//Log.write(Log.DEBUG_VERBOSE, "tCurrent_EQUALITY:  " + tCurrent + "," + tbase);
			q1 = "update " + tCurrent + " set " + uParms + " MDFD_DT = " + m_strTimeStamp + ", MDFD_USERID = '" +
				 m_strUserid + "' where " + myOrder.getSQNC_COLUMN() + " = " + cRs + " and " + myOrder.getVRSN_COLUMN() +
				 " = " + cRv;
		}else{
			//Log.write(Log.DEBUG_VERBOSE, "tCurrent_INEQUALITY:  " + tCurrent + "," + tbase);
			q1 = "update " + tCurrent + " set " + uParms + " MDFD_DT = " + m_strTimeStamp + ", MDFD_USERID = '" +
				 m_strUserid + "' where " + myOrder.getSQNC_COLUMN() + " = " + cRs + " and " +
				 myOrder.getVRSN_COLUMN() + " = " + cRv + " and frm_sctn_occ = " + cOc;
		}
		Log.write(Log.DEBUG_VERBOSE, "\n** FormBean.storeForm(): STORE_FORM_LAST: " + q1+"\n");

		// Only update if something changes man
		if (uParms != null && uParms.length() > 0)
		{
			Log.write(Log.DEBUG_VERBOSE,"\n****** FormBean:last UPDATE=["+q1+"]\n\n");
			if (stmt1.executeUpdate(q1) <= 0)
			{
				throw new SQLException(null,null,100);
			}
		}

		stmt1.close();

		//Log.write(Log.DEBUG_VERBOSE, "FormBean: storeForm: return value = true");
		return(true);
	}

	public String replaceSpecialChars(String value){

		String tvalue;

		tvalue = value.replace('<', ' ');
		tvalue = tvalue.replace('>', ' ');
		tvalue = tvalue.replace('=', ' ');
		return (tvalue);

	}
	
}
