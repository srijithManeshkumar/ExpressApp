/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL Communications Inc
 */

/*
 * MODULE:	FieldValidationBean.java
 *
 * DESCRIPTION:
 *              Routines to validation LSR form/fields.
 * AUTHOR:
 *              Mike Pelleschi
 *
 * DATE:        01-31-2002
 *
 * HISTORY:
 *
 *	02/11/2008  HD0000002472840 Steve Korchnak added case 33, 34 and corresponding methods..
 *
 *	02/25/2002  psedlak - Added data type of "S" to allow special characters.
 *	04/09/2003  psedlak - Added monetary validation, type 27
 *	08/04/2003  psedlak - Added validation 28
 *	10/14/2003  psedlak - documented code and added detail to error message text
 *	11/14/2003  psedlak - documented code and added detail to error message text (round 2),
 *				also added validation 29, 30
 *	01/23/2004  psedlak - Added validation 31. If conditions on 31 are true, then future validations
 *			on same field are bypassed.
 *			Also added validation 32 - "field must not contain this text"
 *
 */

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/FieldValidationBean.java  $
/*
/*   Rev 1.14   Aug 27 2004 13:55:02   e0069884
/*Express tracking #86 and #89
/*   Rev 1.11   Dec 15 2003 14:05:40   e0069884
/*DSL field validations #29/30 and more descriptive msgs added
/*
/*   Rev 1.10   Nov 10 2003 15:55:06   e0069884
/*Add detail info to Validation messages
/*
/*   Rev 1.9   Aug 11 2003 08:56:24   e0069884
/*Added NPA state code validation routine
/*
/*   Rev 1.8   May 09 2003 14:19:52   e0069884
/*SER 20476
/*
/*   Rev 1.7   Jul 09 2002 11:47:26   dmartz
/*
/*
/*   Rev 1.6   23 Apr 2002 10:16:44   dmartz
/*Singleton
/*
/*   Rev 1.5   15 Mar 2002 11:38:20   dmartz
/*Modified KEY for Hash Table to keep it unique
/*
/*   Rev 1.4   25 Feb 2002 16:24:22   sedlak
/*
/*
/*   Rev 1.3   21 Feb 2002 08:28:42   sedlak
/*Fixed UAT issue with Rel 1.1
/*
/*   Rev 1.2   11 Feb 2002 13:31:12   sedlak
/*release 1.1
/*
/*   Rev 1.1   31 Jan 2002 07:39:30   sedlak
/*
/*
/*   Rev 1.0   23 Jan 2002 11:05:38   wwoods
/*Initial Checkin
*/

/* $Revision:   1.14  $
*/

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class FieldValidationBean {

	private Vector vEmptySet = new Vector();
	private Vector vSection = new Vector();
	private String dataDesc = null;
	private Hashtable vDataHash = null;
	private int currFormSeq = -1;
	private String currFormMox = null;
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private FormFields m_ffs = null;	//Used for detailed validation msgs
	private Forms m_Forms = null;		//Used for detailed validation msgs

	private String m_strDetailMessage = null;	//This defines a detailed validation error message
	private String m_strVerdictMessage = null;	//This defines the result of the verdict

	//Constructor
	public FieldValidationBean()
	{
		m_ffs = FormFields.getInstance();
		m_Forms = Forms.getInstance();
	}

	private void setDetailMessage(String msg) {
		m_strDetailMessage = msg;
	}
	private	String getDetailMessage() {
		return m_strDetailMessage;
	}
	private boolean haveDetailMessage() {
		if (m_strDetailMessage == null) return false;
		if (m_strDetailMessage.length() < 1) return false;
		return true;
	}
	private void setVerdictMessage(String msg) {
		m_strVerdictMessage = msg;
	}
	private	String getVerdictMessage() {
		return m_strVerdictMessage;
	}
	private boolean haveVerdictMessage() {
		if (m_strVerdictMessage == null) return false;
		if (m_strVerdictMessage.length() < 1) return false;
		return true;
	}

	public boolean setDBConnectivity(Connection connect){
		try {
			this.conn = connect;
			this.stmt = this.conn.createStatement();
		} catch(Exception exc) {
			exc.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, ": ERROR CREATING CONNECTION/STATEMENT in FieldValidationBean.");
			return(false);
		}

		return(true);
	}


	public Vector getFieldValidations(int form_seqno, int form_sect_seqno, String form_fld_no)
		throws Exception
	{
	        Vector valV = new Vector();

		// get FormFields instance
		FormFieldValidations ffv = FormFieldValidations.getInstance();

		valV = ffv.getFormFieldValidations(form_seqno, form_sect_seqno, form_fld_no);

		return(valV);
	}

	public boolean setMoxParm(int form_seqno)
		throws Exception
	{
		String query = "SELECT frm_act_mox_idx FROM form_t WHERE frm_sqnc_nmbr = " + form_seqno;
		rs = this.stmt.executeQuery(query);
		if(rs.next()){
			String  frMox = rs.getString("frm_act_mox_idx");
			Log.write(Log.DEBUG_VERBOSE, "setMoxParm: mox idx = " + frMox);
			this.currFormMox = frMox;
			this.currFormSeq = form_seqno;
			Log.write(Log.DEBUG_VERBOSE, "MOX_PARM UPDATE: mox = " + this.currFormMox + " and form seq = " + this.currFormSeq);
		}
		rs.close();

		Log.write(Log.DEBUG_VERBOSE, "setMoxParms: returning true.");
		return(true);
	}

	public ValidationResult validateField(FormField ffo)
		throws Exception
	{
		boolean verdict;
		String dType = null;
		String dFldCd = null;
		String dDspType = null;
		String dMask = null;
		String dMox = null;
		String dImox = null;
		int form_seqno;
		int form_sect_seqno;
		int occ;
		String form_fld_no;
		String theValue = null;
		int dFval;
		int dLen;
	        ValidationResult vC = new ValidationResult();
	        Vector vValidations = new Vector();

		setDetailMessage("");

		form_seqno = ffo.getFrmSqncNmbr();
		form_sect_seqno = ffo.getFrmSctnSqncNmbr();
		occ = ffo.getFrmSctnOcc();
		form_fld_no = ffo.getFrmFldNmbr();
		theValue = ffo.getFieldData();
		if(isNull(theValue)){
			//Log.write(Log.DEBUG_VERBOSE, "NULL detected for " + form_seqno + "," + form_sect_seqno + "," +  form_fld_no);
                        //Commenting out unnecessary logs - Antony 05/18/10
			theValue = ""; // TREAT AS 0-LENGTH STRING FOR VALIDATION PURPOSES.
		}
		dLen = ffo.getFldLngth();
		dType = ffo.getFldDataTyp();
		dMask = ffo.getFldFrmtMsk();
		dFval = ffo.getFldVlsSqncNmbr();
		dMox = ffo.getMoxArray();
		dImox = ffo.getImoxArray();
		dDspType = ffo.getFldDsplyTyp();
		dFldCd = ffo.getFldCd();

		//Log.write(Log.DEBUG_VERBOSE, "validateField: DEBUG_VF: data type  =  " + dType + "fld code = " + dFldCd + " mask  = " + dMask + " field values seq = " + dFval + " FIELD LENGTH = " + dLen + " mox array = " + dMox + " imox array = " + dImox + " display type = " + dDspType);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		// CHECK DISPLAY TYPE -- IF READONLY, ACCEPT AS VALID.
		  //Log.write(Log.DEBUG_VERBOSE, "Validating " + form_seqno + "," + form_sect_seqno + "," +  occ + "," + form_fld_no); 
                  //Commenting out unnecessary logs - Antony 05/18/10
                 
		if(dDspType.equals("READONLY")){
			vC.setValidationResult(true);
			return(vC);
		}

		int vEx = verifyExistence(form_seqno,form_sect_seqno, occ, dMox, dImox, theValue);
		if(vEx == 1){
			vC.setValidationResult(true);
			Log.write(Log.DEBUG_VERBOSE, "validateField: non-required field with zero length");
			return(vC);
		}

		if(vEx == 2){
			vC.setValidationResult(false);
			vC.setValidationMessage("This field is required.");
			Log.write(Log.DEBUG_VERBOSE, "validateField: required field with zero length");
			return(vC);
		}

		if(!verifyDatatype(dType, dFldCd, theValue)){
			vC.setValidationResult(false);
			vC.setValidationMessage(theValue + " data type validation failed. " + getDataDesc() + " expected.");
			Log.write(Log.DEBUG_VERBOSE, "validateField:: verifyDatatype returns: false for " + theValue + " data type of " + dType + " expected.");
			return(vC);
		}


		if(!verifyLength(dLen, theValue, dFldCd)){
			vC.setValidationResult(false);
			if(dFldCd.equals("DCRIS ORDR NBR"))
			{
				vC.setValidationMessage(theValue + ": invalid: length of 13 or 15 expected.");
			}
			else
			{
				vC.setValidationMessage(theValue + ": invalid: length of " + dLen + " or less expected.");
			}
			Log.write(Log.DEBUG_VERBOSE, "validateField: verifyLength returns: false for " + theValue + " length of " + dLen + " expected.");
			return(vC);
		}

		//Get all the field validations for this field -validations are returned in sorted order.
	        Vector fVals = getFieldValidations(form_seqno, form_sect_seqno, form_fld_no);
		//Log.write(Log.DEBUG_VERBOSE, "validateField: vector Length of getFieldValidations() " + fVals.size());
                //Commenting out unnecessary logs - Antony 05/18/10
                                
		for(int i=0;i<fVals.size();i++)
		{
			ValidationComponent valcomp = (ValidationComponent)fVals.elementAt(i);
			int ivSeq = valcomp.getValSqncNmbr();
			
                        //Log.write(Log.DEBUG_VERBOSE, "FIELD= " + form_seqno +":"+ form_sect_seqno +":" + form_fld_no + " VAL_SEQNO = " + ivSeq);
                        //Commenting out unnecessary logs - Antony 05/18/10
                        
			if(ivSeq == -1){
				vC.setValidationResult(false);
				vC.setValidationMessage(" an exception occurred while attempting to read from form_field_validation.");
				return(vC);
			}
			switch(ivSeq){
				case 1:
					//Log.write("validateField:: Calling phone format validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validatePhoneFormat(theValue)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Phone format of " + dMask + " or N was expected.");
						return(vC);
					}
					break;
				case 2:
					//Log.write("validateField:: Calling Date validation for " + theValue);
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!hasLength(theValue)){
						vC.setValidationResult(true);
						return(vC);
					}
					if(!Validate.isValidDate(theValue, '-')){
						vC.setValidationResult(false);
						vC.setValidationMessage("Date format of MM-DD-YYYY was expected.");
						return(vC);
					}
					break;
				case 3:
					//Log.write("validateField:: Calling extended phone format validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateExtendedPhoneFormat(theValue)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Phone format of " + dMask + " or N was expected.");
						return(vC);
					}
					break;
				case 4:
					//Log.write("validateField:: Calling DateTime validation for " + theValue);
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateDateTime(theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Date/time format of MM-DD-YYYY-HHMMAM/PM was expected.");
						}
						return(vC);
					}
					break;
				case 5:
					//Log.write("validateField:: Calling Pg ## OF ## validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateMultExist(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 6:
					//Log.write("validateField:: Calling NOR (##-##) validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateNOR(theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("NOR format of "  + dMask + " was expected.");
						}
						return(vC);
					}
					break;
				case 7:
					//Log.write("validateField:: Calling time spent on job format validation (###.##)");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateTimeSpent(occ,theValue, dMask)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("This format was expected: " + dMask);
						}
						return(vC);
					}
					break;
				case 8:
					//Log.write("validateField:: Calling material cost format validation (######.##)");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateMaterialCost(occ,theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Field optional when MTL is populated - otherwise prohibited. Field format is " + dMask);
						}
						return(vC);
					}
					break;
				case 9:
					//Log.write("validateField:: Calling LSR/AN validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!unitOfMeasure(occ,theValue,valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Last character of field must be unit of measure.");
						}
						return(vC);
					}
					break;
				case 10:
					//Log.write("validateField:: Calling LSR/ATN validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateAndOrNotExist(occ,theValue,valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 11:
					//Log.write("validateField:: Calling LSR/DSPTCH validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateAndOrContains(occ,theValue,valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 12:
					//Log.write("validateField:: Calling LSR/DFDT validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateLsrDfdt(occ,theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("DFDT: prohibited if LSR/REQTYP[0] is 'G', 'H', or 'J'. Otherwise optional.");
						}
						return(vC);
					}
					break;
				case 13:
					//Log.write("validateField:: Calling LSR/DFDT validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateLsrAct(occ,theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("ACT: prohibited if LSR/REQTYP[0] is 'G', 'H', or 'J'. Otherwise optional.");
						}
						return(vC);
					}
					break;
				case 14:
					//Log.write("validateField:: Calling TIND validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!tindTimeCheck(occ,theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Field optional if TIND is populated. Otherwise prohibited.");
						}
						return(vC);
					}
					break;
				case 15:
					//Log.write("validateField:: Calling ONSP validation");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!tindTimeCheck(occ,theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Field optional if TIND is populated. Otherwise prohibited.");
						}
						return(vC);
					}
					break;
				case 20:
					if(!subsetOf(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 21:
					if(!eitherOne(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 22:
					if(!isPopulated(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Validation failed for this field based on edit conditions");
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 23:
					if(!eitherContains(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 24:
					if(!posOne(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Validation failed for this field based on edit conditions");
						return(vC);
					}
					break;
				case 25:
					Log.write("value of occ is " + occ + ", value of theValue is " + (String)theValue);
					if(!valueCompare(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Validation failed for this field based on edit conditions");
						return(vC);
					}
					break;
				case 26:
					if(!ifAandBandC(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Validation failed for this field based on edit conditions");
						return(vC);
					}
					break;
				case 27:
					if(!isMoney(theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Monetary value expected with 2 decimal places, no commas or $, for example <b>1.23</b>");
						}
						return(vC);
					}
					break;
				case 28:
					if(!doesNPAMatchState(occ, theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("NPA does not match state code defined for this request");
						}
						return(vC);
					}
					break;
				case 29:
					if(!validDslUserid(theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Invalid userid");
						}
						return(vC);
					}
					break;
				case 30:
					if(!validDslPassword(theValue)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Invalid password");
						}
						return(vC);
					}
					break;
				case 31:
					if(subsetOf(occ, theValue, valcomp))
					{
						Log.write("Val 31: Conditions met-rest of validations for this field skipped");
						vC.setValidationResult(true);
						vC.setValidationMessage("Condition met -rest of field validations skipped");
						return(vC);
					}
					Log.write("Val 31 condition not met, do rest of vals");
					break; //jump out of switch and get next validation
				case 32:
					if(!valuesProhibited(theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Value is prohibited");
						}
						return(vC);
					}
					break;
//HD0000002472840S
				case 33:
					//Log.write("validateField:: value a not exist and b exist or in set ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!notExistA_AndB_ExistOrInSet(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 34:
					//Log.write("validateField:: value a not exist and b not exist or b not in set ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!notExistA_AndB_NotExistOrB_NotInSet(occ,theValue, valcomp)){
						vC.setValidationResult(false);
						if (haveDetailMessage()) {
							vC.setValidationMessage(getDetailMessage());
						}
						else {
							vC.setValidationMessage("Validation failed for this field based on edit conditions");
						}
						return(vC);
					}
					break;
				case 35:
					//Log.write("validateField:: Calling mandatory phone format validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateMandatoryPhoneFormat(theValue)){
						vC.setValidationResult(false);
						vC.setValidationMessage("Phone format of " + dMask + " was expected.");
						return(vC);
					}
					break;
				case 36:
					//Log.write("validateField:: Calling ZIP Code format validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateZIPCodeFormat(theValue)){
						vC.setValidationResult(false);
						vC.setValidationMessage("ZIP code format of " + dMask + " was expected.");
					    return(vC);
					}
				    break;
				case 37:
					//Log.write("validateField:: Calling NNSP Code format validation ");
                                        //Commenting out unnecessary logs - Antony 05/18/10
                                    
					if(!validateNNSPCodeFormat(theValue)){
						vC.setValidationResult(false);
						vC.setValidationMessage("NNSP code format of " + dMask + " was expected.");
						return(vC);
					}
				    break;

//HD0000002472840F
				default:
					Log.write("validateField:: Indeterminate validation ");
					vC.setValidationResult(false);
					vC.setValidationMessage("!!Unable to determine the type of validation!!");
					return(vC);
			}
		}

		vC.setValidationResult(true);
		return(vC);
	}

	private String getFieldDesc(int iForm, int iSection, String strFieldNbr)
	{
		String strDesc = (m_ffs.getFormField(iForm,iSection,strFieldNbr)).getFldCd() +
				" on " + (m_Forms.getForm(iForm)).getFormCd() + " form";
		return strDesc;
	}

	/**
        * subsetOf() method is validation routine #20.
	* @param       int			Form Section Occurence
	* @param       String			value of field
	* @param       ValidationComponent	Validation components (FORM_FIELD_VALIDATION_T values for this field)
        */
	public boolean subsetOf(int occ, String value, ValidationComponent vc)
	{

		boolean thisCond = false;
		boolean bBlankAllowed = false;
		String k3 = null;
		int theocc = -1;
		String newocc;
		int intocc;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();

		String fld1Val= vc.getFld1Val();
		//String fld2Val= vc.getFld2Val();

		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();
		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		int pos1= vc.getFld1Pos();
		int pos5= vc.getFld5Pos();

		if(isNull(fld1) || isNull(fld1Val)  ||
				 isNull(condtrue) || isNull(condfalse) ){
			boolean cond = false;
			Log.write(Log.ERROR, "FieldValidationBean.subsetOf() Validation components null for " +
				vc.getFrmSqncNmbr() + ":" + vc.getFrmSctnSqncNmbr() + ":" + vc.getFrmFldNmbr() );
			return true;
		}
		String strMsgDesc="";

		if(!isNull(fld5)){
			if(fld5.equals("BLANKALLOWED"))
				bBlankAllowed = true;
		}
		setDetailMessage("");

		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld1.substring(c2+1);
		  theocc = occ;
		}else{
			newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);

		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		//Get actual data for field 1
		String hashData = getHashData(fs, fss, theocc, k3);
		//Log.write(Log.DEBUG_VERBOSE, "subsetOf(): field = "+ fs + " " + fss + " " + theocc + " " + k3);
		//Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData = " + hashData);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		//--------      Field 1 checks   -----------------
		// thisCond set to True if:
		//	Field1 is blank and blanks are allowed ("BLANKALLOWED" in FLD5)
		//  -or Field1 has data, and Fld1_Val is contained in that data
		// Else
		// 	thisCond is false
		if(isNull(hashData))
		{	Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData search returns null");
			if(bBlankAllowed)
			{	thisCond = true;
				setDetailMessage(" if blanks allowed in " + getFieldDesc(fs,fss,k3) + " ") ;
			}
			else
			{	thisCond = false;
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ") ;
			}
		}
		else
		{  hashData = hashData.trim();
		   if(!hasLength(hashData))
		   {	if(bBlankAllowed)
			{    	thisCond = true;
				setDetailMessage(" if blanks allowed in " + getFieldDesc(fs,fss,k3) + " ") ;
			}
			else
			{	thisCond = false;
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ") ;
			}
		   }
		   else
		   {
			int subIdx = fld1Val.indexOf(hashData);

			//Evaluate condition based on subIdx and condtrue/false
			if(subIdx >= 0)
			{	thisCond = true;
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " in (" + fld1Val + ") " );
			}
			else
			{	thisCond = false;
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " not in (" + fld1Val + ") " );
			}
		   }
		}

		// If field 1 checks returned true, then continue with field 2 -if it's populated
		//--------      Field 2 checks   -----------------
		// thisCond set to True if:
		//	Field2 is blank and blanks are allowed ("-1" in FLD5_POS)
		//  -or Field2 has ANY data
		// Else
		// 	thisCond is false
		if(thisCond && fld2 != null)
		{
			Log.write(Log.DEBUG_VERBOSE, "Evaluating another portion...");
			c1 = fld2.indexOf(',',1);
			c2 = fld2.indexOf(',',c1 + 1);
			c3 = fld2.indexOf(',',c2 + 1);

			k1 = fld2.substring(0,c1);
			k2 = fld2.substring(c1+1, c2);
			if(c3 == -1)
			{	k3 = fld2.substring(c2+1);
				theocc = occ;
			}
			else
			{	newocc = fld2.substring(c2+1, c3);
				intocc = Integer.parseInt(newocc);
				theocc = intocc;
				k3 = fld2.substring(c3+1);
			}

			fs = Integer.parseInt(k1);
			fss = Integer.parseInt(k2);

			//Get actual data for field 2
			hashData = getHashData(fs, fss, theocc, k3);

			if(isNull(hashData))
			{	if(pos5 == -1) //like a BLANKALLOWED, but for fld 2
				{	thisCond = true;
				}
				else
				{	thisCond = false;
				}
				strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
			}
			else
			{	hashData = hashData.trim();
				if(!hasLength(hashData))
				{	if(pos5 == -1) //like a BLANKALLOWED, but for fld 2
					{	thisCond = true;
					}
					else
					{	thisCond = false;
					}
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				}
				else{
					if(pos5 == -1)	//like a BLANKALLOWED, but for fld 2
					{	thisCond = false;
					}
					else
					{	thisCond = true;
					}
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is populated ";
				}
			}
		}//end of field 2 checks

		// If field1 criteria are meet (& field 2 is populated and its criteria are also met),
		// then thisCond set to True
		// else its false
		if(thisCond)
		{
			Log.write(Log.DEBUG_VERBOSE, "subsetOf: base premise TRUE, additional check....");
			// Ok - we met our criteria, see if value we have is forbidden or accepted now
			if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  		}
		  	}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		//Log.write(Log.DEBUG_VERBOSE, "subsetOf END:: returns " + fnlVrdct);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		return fnlVrdct;

	}

	/**
        * valuesProhibited() method is validation routine #32.
	* @param       String			value of field
	* @param       ValidationComponent	Validation components (FORM_FIELD_VALIDATION_T values for this field)
        */
	public boolean valuesProhibited(String strValue, ValidationComponent vc)
	{

		boolean thisCond = true;
		boolean bBlankAllowed = false;
		boolean bCaseSensitive = true;
		String strCheckValue="";

		Log.write("FieldValidationBean.valuesProhibited() value =["+ strValue + "]");
		if (!hasLength(strValue))
		{	return true;
		}

		// these fields contain the forbidden field values
		String fld1Val= vc.getFld1Val();
		String fld2Val= vc.getFld2Val();
		String fld3Val= vc.getFld3Val();
		String fld4Val= vc.getFld4Val();
		String fld5Val= vc.getFld5Val();
		String fld5= vc.getFld5();	//Optional -additional text msg

		int iPos5 = vc.getFld5Pos();	// 0 or empty, then case sensitive check, 1= case insensitive

		if (iPos5 > 0) bCaseSensitive = false;
		if (bCaseSensitive)
			strCheckValue = strValue;
		else
		{
			strCheckValue = strValue.toUpperCase();
		}

		if( isNull(fld1Val)  )
		{	Log.write(Log.ERROR, "FieldValidationBean.valuesProhibited() Validation components null for " +
				vc.getFrmSqncNmbr() + ":" + vc.getFrmSctnSqncNmbr() + ":" + vc.getFrmFldNmbr() );
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");
		setDetailMessage(" Field cannot contain " + fld1Val + " " + fld5);
		if (!bCaseSensitive)	fld1Val = fld1Val.toUpperCase();

		fld1Val = fld1Val.trim();
		int iHit = -1;
		iHit = strCheckValue.indexOf(fld1Val);
		if(iHit >= 0)
		{	thisCond = false;
		}
		else if ( !isNull(fld2Val) )
		{	setDetailMessage(" Field cannot contain " + fld2Val + " " + fld5);
			if (!bCaseSensitive)    fld2Val = fld2Val.toUpperCase();
			iHit = strCheckValue.indexOf(fld2Val);
			if (iHit >=0)
			{	thisCond = false;
			}
			else if ( !isNull(fld3Val) )
			{	setDetailMessage(" Field cannot contain " + fld3Val + " " + fld5);
				if (!bCaseSensitive)    fld3Val = fld3Val.toUpperCase();
				iHit = strCheckValue.indexOf(fld3Val);
				if (iHit >=0)
				{	thisCond = false;
				}
				else if ( !isNull(fld4Val) )
				{	setDetailMessage(" Field cannot contain " + fld4Val + " " + fld5);
					if (!bCaseSensitive)    fld4Val = fld4Val.toUpperCase();
					iHit = strCheckValue.indexOf(fld4Val);
					if (iHit >=0)
					{	thisCond = false;
					}
					else if ( !isNull(fld5Val) )
					{	setDetailMessage(" Field cannot contain " + fld5Val + " " + fld5);
						if (!bCaseSensitive)    fld5Val = fld5Val.toUpperCase();
						iHit = strCheckValue.indexOf(fld5Val);
						if (iHit >=0)
						{	thisCond = false;
						}
					}
				}
			}
		}

		return thisCond;

	}

	public boolean valueCompare(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		String k3 = null;
		int theocc = -1;

		String fld1 = vc.getFld1();
		String fld1Val= vc.getFld1Val();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();
		int pos1= vc.getFld1Pos();

		 if(!allDigits(fld1Val)){
			Log.write(Log.WARNING, "BOOLEAN_NON_DIGIT_ARG_ALERT:  " + fld1 + "," + fld1Val);
			return true;
		 }

		if(isNull(fld1) || isNull(fld1Val)  ||
				 isNull(condtrue) || isNull(condfalse) ){
			Log.write(Log.WARNING,"BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + condtrue + "," + condfalse + "," + fld1Val);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld1.substring(c2+1);
		  Log.write(Log.DEBUG_VERBOSE, "BOOLEAN subsetOf - k3 = " + k3);
		  theocc = occ;
		}else{
			String newocc = fld1.substring(c2+1, c3);
		        Log.write(Log.DEBUG_VERBOSE, "subsetOf long_form : newocc = " + newocc);
			int intocc = Integer.parseInt(newocc);
		        Log.write(Log.DEBUG_VERBOSE, "subsetOf long_form : intocc = " + intocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);

		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);
		int dbValue = Integer.parseInt(fld1Val);

		String hashData = getHashData(fs, fss, theocc, k3);

		Log.write(Log.DEBUG_VERBOSE, "BOOLEAN HASH DATA = " + hashData);

		if(isNull(hashData)){
			 Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData search returns null");
			thisCond = false;
		}else{
		   hashData = hashData.trim();
		   if(!hasLength(hashData)){
			 Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData has zero len");
			thisCond = false;
		   }else{

			if(!allDigits(hashData)){
			   Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NON_DIGIT_ARG_ALERT:  " + fld1 + "," + fld1Val);
			   thisCond = false;
		        }else{
		          int fieldValue = Integer.parseInt(hashData);
			  if(fieldValue > dbValue)
			     thisCond = true;
			  else
			    thisCond = false;
			  }
		   }
		}

		Log.write(Log.DEBUG_VERBOSE, "valueCompare thisCond " + thisCond);
		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "valueCompare returns " + fnlVrdct);
		return fnlVrdct;

	}

	public boolean posOne(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		String k3 = null;
		int theocc = -1;
		String newocc;
		int intocc;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();
		String fld5 = vc.getFld5();

		String fld1Val= vc.getFld1Val();
		String fld2Val= vc.getFld2Val();
		String fld5Val= vc.getFld5Val();
		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();
		int pos1= vc.getFld1Pos();

		if(isNull(fld1) || isNull(fld1Val)  ||
				 isNull(condtrue) || isNull(condfalse) ){
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + condtrue + "," + condfalse + "," + fld1Val);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld1.substring(c2+1);
		  theocc = occ;
		}else{
			newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);

		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData = getHashData(fs, fss, theocc, k3);

		Log.write(Log.DEBUG_VERBOSE, "BOOLEAN HASH DATA (POS) = " + hashData);

		if(isNull(hashData)){
			Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData search returns null");
			thisCond = false;
		}else{
		   hashData = hashData.trim();
		   if(!hasLength(hashData)){
			 Log.write(Log.DEBUG_VERBOSE, "subsetOf(): hashData has zero len");
			thisCond = false;
		   }else{
			char thisPos = hashData.charAt(pos1);
			int subIdx = fld1Val.lastIndexOf(thisPos);

			 Log.write(Log.DEBUG_VERBOSE, "thisPos: hash = " + hashData + " pos " + pos1 + "  at " + fld1Val + "  = " + thisPos);

			//Evaluate condition based on subIdx and condtrue/false
			if(subIdx >= 0)
			   thisCond = true;
			else
			   thisCond = false;
		   }
		}

	        Log.write(Log.DEBUG_VERBOSE, "thisCond after eval 1: " + thisCond);
		if(thisCond && fld2 != null){
			 Log.write(Log.DEBUG_VERBOSE, "Evaluating another portion...");
		  c1 = fld2.indexOf(',',1);
		  c2 = fld2.indexOf(',',c1 + 1);
		  c3 = fld2.indexOf(',',c2 + 1);

		  k1 = fld2.substring(0,c1);
		  k2 = fld2.substring(c1+1, c2);
		  if(c3 == -1){
		    k3 = fld2.substring(c2+1);
		    theocc = occ;
		  }else{
			newocc = fld2.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld2.substring(c3+1);
		  }

		  fs = Integer.parseInt(k1);
		  fss = Integer.parseInt(k2);

		  hashData = getHashData(fs, fss, theocc, k3);
		  Log.write(Log.DEBUG_VERBOSE, "thisPos(2): hashData = " + hashData );

		  if(isNull(hashData)){
			   thisCond = false;
		  }else{
		       hashData = hashData.trim();
		       if(!hasLength(hashData)){
			    thisCond = false;
		       }else{
			    int subIdx = fld2Val.indexOf(hashData);
			    Log.write(Log.DEBUG_VERBOSE, "thisPos(3): index of " + hashData +  " for fld2Val " + subIdx);
			    if(subIdx >= 0)
			       thisCond = true;
			    else
			       thisCond = false;
		       }
		  }

		}
		if(thisCond){
		   if(fld5 != null){
			if(filterValue(fld5,fld5Val,value))
			{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
				return false;
		  	}
		   }
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		 Log.write(Log.DEBUG_VERBOSE, "posOne returns " + fnlVrdct);
		return fnlVrdct;

	}

	public boolean eitherOne(int occ, String value, ValidationComponent vc)
	{

		boolean thisCond = false;
		int theocc, intocc;
		String newocc, k3;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();
		String fld3 = vc.getFld3();
		String fld4 = vc.getFld4();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld2)  || isNull(fld3) || isNull(fld4) ||
				 isNull(condtrue) || isNull(condfalse) ){
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT: " + fld1 + "," + fld2 + "," + fld3 + "," + fld4 + "," + condtrue + "," + condfalse);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld1.substring(c2+1);
		  theocc = occ;
		}else{
			newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);
		String strField1Desc = getFieldDesc(fs,fss,k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld2.substring(c2+1);
		  theocc = occ;
		}else{
			newocc = fld2.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld2.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);
		String strField2Desc = getFieldDesc(fs,fss,k3);

		//Evaluate condition based on fld4
		if(fld4.equals("N"))
		{	Log.write(Log.DEBUG_VERBOSE, "BOOLEAN either not exist");

			if(isNull(hashData1))
			{	thisCond = true;
				setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is empty ") ;
			}
			else
			{	hashData1 = hashData1.trim();
				if(!hasLength(hashData1))
				{	thisCond = true;
					setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is empty ") ;
				}
				else
				{	thisCond = false;
				}
			}

			if(!thisCond)
			{	if(isNull(hashData2))
				{	thisCond = true;
					setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is empty ") ;
				}
				else
				{	hashData2 = hashData2.trim();
					if(!hasLength(hashData2))
					{	thisCond = true;
						setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is empty ") ;
					}
					else
					{	thisCond = false;
						setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are populated ") ;
					}
				}
			}
		}

		if(fld4.equals("E"))
		{	Log.write(Log.DEBUG_VERBOSE, "BOOLEAN either exist");

			if(isNull(hashData1))
			{	thisCond = false;
			}
			else
			{	hashData1 = hashData1.trim();
				if(hasLength(hashData1))
				{	thisCond = true;
					setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is populated ") ;
				}
				else
				{	thisCond = false;
				}
			}
			if(!thisCond)
			{
				if(isNull(hashData2))
				{	thisCond = false;
					setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are empty ") ;
				}
				else
				{	hashData2 = hashData2.trim();
					if(hasLength(hashData2))
					{	thisCond = true;
						setDetailMessage(" if " + strField1Desc + " OR " + strField2Desc + " is populated ") ;
		   			}
					else
					{	thisCond = false;
						setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are empty ") ;
		   			}
		   		}
			}
		}

		if(fld4.equals("B"))
		{	Log.write(Log.DEBUG_VERBOSE, "BOOLEAN both must exist");

			if(isNull(hashData1))
			{	thisCond = false;
				setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are not populated ") ;
			}
			else
			{	hashData1 = hashData1.trim();
				if(hasLength(hashData1))
				{	thisCond = true;	}
				else
				{	thisCond = false;
					setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are not populated ") ;
		  		}
		  	}

			if(thisCond)
			{
				if(isNull(hashData2))
				{	thisCond = false;
					setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are not populated ") ;
				}
				else
				{	hashData2 = hashData2.trim();
					if(hasLength(hashData2))
					{	thisCond = true;
						setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are populated ") ;
					}
					else
					{	thisCond = false;
						setDetailMessage(" if " + strField1Desc + " AND " + strField2Desc + " are not populated ") ;
					}
				}
			}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "eitherNot returns " + fnlVrdct);
		return fnlVrdct;

	}

	public boolean validateAndOrNotExist(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		boolean bBlankAllowed = false;
		String k1,k2,k3,newocc,hashData;
		int theocc = -1;
		int c1,c2,c3,intocc,fs,fss,subIdx;

		String fld1 = vc.getFld1();
		String fld1Val= vc.getFld1Val();

		String fld2 = vc.getFld2();
		String fld3 = vc.getFld3();
		String fld4 = vc.getFld4();
		String fld5 = vc.getFld5();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();
		String strMsgDesc="";

		setDetailMessage("");

		if(isNull(fld1) || isNull(fld1Val)  ||
		  isNull(fld2) || isNull(fld3)  || isNull(fld4) ||
				 isNull(condtrue) || isNull(condfalse) ){
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + condtrue + "," + condfalse + "," + fld1Val);
			return true;
		}

		if(!isNull(fld5)){
			if(fld5.equals("BLANKALLOWED"))
				bBlankAllowed = true;
		}

		c1 = fld1.indexOf(',',1);
		c2 = fld1.indexOf(',',c1 + 1);
		c3 = fld1.indexOf(',',c2 + 1);

		k1 = fld1.substring(0,c1);
		k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
		  k3 = fld1.substring(c2+1);
		  theocc = occ;
		}else{
			newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		hashData = getHashData(fs, fss, theocc, k3);
		Log.write(Log.DEBUG_VERBOSE, "AND_OR HASH DATA(1) = " + hashData);

		if(isNull(hashData))
		{	if(bBlankAllowed) {
				setDetailMessage(" if blanks allowed in " +  getFieldDesc(fs,fss,k3) + " ") ;
				thisCond = true;
			}
			else {
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ");
			        thisCond = false;
			}
		}
		else
		{
		   hashData = hashData.trim();
		   if(!hasLength(hashData)){
			if(bBlankAllowed) {
				setDetailMessage(" if blanks allowed in " + getFieldDesc(fs,fss,k3) + " ") ;
			    	thisCond = true;
			}
			else {
			    setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ");
			    thisCond = false;
			}
		   }else{
			subIdx = fld1Val.indexOf(hashData);
			if(subIdx >= 0) {
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " in (" + fld1Val + ") " );
				thisCond = true;
			}
			else {
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is not in (" + fld1Val + ") ");
				thisCond = false;
		   	}
		   }
		}
		Log.write(Log.DEBUG_VERBOSE, "AND_OR thisCond(1) = " + thisCond);

		if(thisCond)
		{
			c1 = fld2.indexOf(',',1);
			c2 = fld2.indexOf(',',c1 + 1);
			c3 = fld2.indexOf(',',c2 + 1);

			k1 = fld2.substring(0,c1);
			k2 = fld2.substring(c1+1, c2);
			if(c3 == -1){
			    k3 = fld2.substring(c2+1);
			    theocc = occ;
			}else{
				newocc = fld2.substring(c2+1, c3);
				intocc = Integer.parseInt(newocc);
				theocc = intocc;
				k3 = fld2.substring(c3+1);
			}
			fs = Integer.parseInt(k1);
			fss = Integer.parseInt(k2);

			hashData = getHashData(fs, fss, theocc, k3);

			if(isNull(hashData)){
				strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				thisCond = true;
			}else{
			       hashData = hashData.trim();
			       if(!hasLength(hashData)){
				    strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				    thisCond = true;
			       }else{
				     thisCond = false;
				     strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is populated ";
			       }
			}
			Log.write(Log.DEBUG_VERBOSE, "AND_OR thisCond(2) = " + thisCond);

			if(!thisCond)
			{	c1 = fld3.indexOf(',',1);
				c2 = fld3.indexOf(',',c1 + 1);
				c3 = fld3.indexOf(',',c2 + 1);

				k1 = fld3.substring(0,c1);
				k2 = fld3.substring(c1+1, c2);
				if(c3 == -1){
					k3 = fld3.substring(c2+1);
					theocc = occ;
				}else{
					newocc = fld3.substring(c2+1, c3);
					intocc = Integer.parseInt(newocc);
					theocc = intocc;
					k3 = fld3.substring(c3+1);
				}

				fs = Integer.parseInt(k1);
				fss = Integer.parseInt(k2);

				hashData = getHashData(fs, fss, theocc, k3);

				if(isNull(hashData)){
				    	strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					thisCond = true;
				}else{
					hashData = hashData.trim();
					if(!hasLength(hashData)){
				    		strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
						thisCond = true;
					}else{
				     		strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is populated ";
						thisCond = false;
					}
				}
			}
			Log.write(Log.DEBUG_VERBOSE, "AND_OR thisCond(3) = " + thisCond);

			if(!thisCond)
			{	c1 = fld4.indexOf(',',1);
				c2 = fld4.indexOf(',',c1 + 1);
				c3 = fld4.indexOf(',',c2 + 1);

				k1 = fld4.substring(0,c1);
				k2 = fld4.substring(c1+1, c2);
				if(c3 == -1){
					k3 = fld4.substring(c2+1);
					theocc = occ;
				}else{
					newocc = fld4.substring(c2+1, c3);
					intocc = Integer.parseInt(newocc);
					theocc = intocc;
					k3 = fld4.substring(c3+1);
				}

				fs = Integer.parseInt(k1);
				fss = Integer.parseInt(k2);

				hashData = getHashData(fs, fss, theocc, k3);
				Log.write(Log.DEBUG_VERBOSE, "AND_OR HASH DATA(4) = " + hashData);

				if(isNull(hashData)){
				    	strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					thisCond = true;
				}else{
					hashData = hashData.trim();
					if(!hasLength(hashData)){
				    		strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
						thisCond = true;
					}else{
				     		strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is populated ";
						thisCond = false;
					}
				}
			 }
		}
		Log.write(Log.DEBUG_VERBOSE, "AND_OR thisCond(FINAL) = " + thisCond);

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "AND_OR ENDS:: returns " + fnlVrdct);
		return fnlVrdct;

	}

	public boolean validateAndOrContains(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		boolean bBlankAllowed = false;
		String k1,k2,k3,newocc,hashData;
		int theocc = -1;
		int c1,c2,c3,intocc,fs,fss,subIdx;

		String fld1 = vc.getFld1();
		String fld1Val= vc.getFld1Val();
		String fld2Val= vc.getFld2Val();
		String fld3Val= vc.getFld3Val();
		String fld4Val= vc.getFld4Val();
		String fld5Val= vc.getFld5Val();
		int pos5 = vc.getFld5Pos();

		String fld2 = vc.getFld2();
		String fld3 = vc.getFld3();
		String fld4 = vc.getFld4();
		String fld5 = vc.getFld5();
		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();
		int pos1= vc.getFld1Pos();

		if(isNull(fld1) || isNull(fld1Val)  || isNull(fld2Val)  || isNull(fld2) || isNull(fld3) ||
				 isNull(fld3Val) || isNull(condtrue) || isNull(condfalse) ){
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + condtrue + "," + condfalse + "," + fld1Val);
			return true;
		}
		String strMsgDesc = "";

		if(!isNull(fld5)){
			if(fld5.equals("BLANKALLOWED"))
				bBlankAllowed = true;
		}
		setDetailMessage("");

		c1 = fld1.indexOf(',',1);
		c2 = fld1.indexOf(',',c1 + 1);
		c3 = fld1.indexOf(',',c2 + 1);

		k1 = fld1.substring(0,c1);
		k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
			k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		hashData = getHashData(fs, fss, theocc, k3);

		if(isNull(hashData))
		{	if(bBlankAllowed) {
				thisCond = true;
				setDetailMessage(" if blanks allowed in " + getFieldDesc(fs,fss,k3) + " ") ;
			}
			else {
			        thisCond = false;
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ");
			}
		}
		else
		{
			hashData = hashData.trim();
			if(!hasLength(hashData))
			{	if(bBlankAllowed)
				{
					thisCond = true;
					setDetailMessage(" if blanks allowed in " + getFieldDesc(fs,fss,k3) + " ") ;
				}
				else {
					thisCond = false;
					setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is empty ");
				}
			}
			else
			{	if(!isNull(fld5Val))
				{	if(fld5Val.equals("USEPOSINDEX"))
					{	char thisPos = hashData.charAt(pos1);
			  			subIdx = fld1Val.lastIndexOf(thisPos);
			  		}
					else
						subIdx = fld1Val.indexOf(hashData);
		      		}
				else
				{	subIdx = fld1Val.indexOf(hashData);
		      		}
				if(subIdx >= 0) {
					setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " in (" + fld1Val + ") " );
					thisCond = true;
				}
				else {
					thisCond = false;
					setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is not in (" + fld1Val + ") ");
				}
			}
		}

		if(thisCond)
		{
			c1 = fld2.indexOf(',',1);
			c2 = fld2.indexOf(',',c1 + 1);
			c3 = fld2.indexOf(',',c2 + 1);

			k1 = fld2.substring(0,c1);
			k2 = fld2.substring(c1+1, c2);
			if(c3 == -1){
				k3 = fld2.substring(c2+1);
				theocc = occ;
			}else{
				newocc = fld2.substring(c2+1, c3);
				intocc = Integer.parseInt(newocc);
				theocc = intocc;
				k3 = fld2.substring(c3+1);
			}
		  	fs = Integer.parseInt(k1);
			fss = Integer.parseInt(k2);

			hashData = getHashData(fs, fss, theocc, k3);

			if(pos5 <= 0)
			{	if(isNull(hashData))
				{	thisCond = false;
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				}
				else
				{	hashData = hashData.trim();
					if(!hasLength(hashData))
					{	thisCond = false;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					}
					else
					{	subIdx = fld2Val.indexOf(hashData);
						if(subIdx >= 0)
						{	thisCond = true;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " in (" + fld2Val + ") ";
						}
						else
						{	thisCond = false;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " not in (" + fld2Val + ") ";
						}
					}
				}
			}
			else
			{	if(isNull(hashData))
				{	thisCond = true;
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				}
				else
				{	hashData = hashData.trim();
					if(!hasLength(hashData))
					{	thisCond = true;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
		       			}
					else
					{	subIdx = fld2Val.indexOf(hashData);
						if(subIdx >= 0)
						{	thisCond = false;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " in (" + fld2Val + ") ";
						}
						else
						{	thisCond = true;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " not in (" + fld2Val + ") ";
						}
					}
				}
			}

			if(!thisCond )
			{
				c1 = fld3.indexOf(',',1);
				c2 = fld3.indexOf(',',c1 + 1);
				c3 = fld3.indexOf(',',c2 + 1);

				k1 = fld3.substring(0,c1);
				k2 = fld3.substring(c1+1, c2);
				if(c3 == -1){
					k3 = fld3.substring(c2+1);
					theocc = occ;
				}
				else
				{	newocc = fld3.substring(c2+1, c3);
					intocc = Integer.parseInt(newocc);
					theocc = intocc;
					k3 = fld3.substring(c3+1);
				}

				fs = Integer.parseInt(k1);
				fss = Integer.parseInt(k2);

				hashData = getHashData(fs, fss, theocc, k3);
				Log.write(Log.DEBUG_VERBOSE, "FLD3=["+hashData+"]");
				if(pos5 <= 0)
				{	if(isNull(hashData))
					{	thisCond = false;
						Log.write(Log.DEBUG_VERBOSE, "FLD3 f1");
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					}
					else
					{	hashData = hashData.trim();
						if(!hasLength(hashData))
						{	thisCond = false;
							Log.write(Log.DEBUG_VERBOSE, "FLD3 f2");
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
						}
						else
						{	subIdx = fld3Val.indexOf(hashData);
							if(subIdx >= 0)
							{	thisCond = true;
								strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" in (" + fld3Val + ") ";
							}
							else
							{	thisCond = false;
								strMsgDesc += " and "+ getFieldDesc(fs,fss,k3) +" not in (" + fld3Val + ") ";
							}
						}
					}
				}
				else
				{	if(isNull(hashData)){
						thisCond = true;
						Log.write(Log.DEBUG_VERBOSE, "FLD3 t3");
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					}
					else
					{	hashData = hashData.trim();
						if(!hasLength(hashData))
						{	thisCond = true;
							Log.write(Log.DEBUG_VERBOSE, "FLD3 t4");
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" is not populated ";
						}
						else
						{	subIdx = fld3Val.indexOf(hashData);
							if(subIdx >= 0)
							{	thisCond = false;
								strMsgDesc += " and "+ getFieldDesc(fs,fss,k3) +" in (" + fld3Val + ") ";
							}
							else
							{	thisCond = true;
								strMsgDesc += " and "+ getFieldDesc(fs,fss,k3) +" not in (" + fld3Val + ") ";
							}
						}
					}
				}
			}

			if(!thisCond && !isNull(fld4) && !isNull(fld4Val) )
			{
				c1 = fld4.indexOf(',',1);
				c2 = fld4.indexOf(',',c1 + 1);
				c3 = fld4.indexOf(',',c2 + 1);

				k1 = fld4.substring(0,c1);
				k2 = fld4.substring(c1+1, c2);
				if(c3 == -1){
				    k3 = fld4.substring(c2+1);
				    theocc = occ;
				}else{
					newocc = fld4.substring(c2+1, c3);
					intocc = Integer.parseInt(newocc);
					theocc = intocc;
					k3 = fld4.substring(c3+1);
				}

				fs = Integer.parseInt(k1);
				fss = Integer.parseInt(k2);

				hashData = getHashData(fs, fss, theocc, k3);

				if(pos5 <= 0)
				{	if(isNull(hashData))
					{	thisCond = false;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					}
					else
					{	hashData = hashData.trim();
						if(!hasLength(hashData))
						{	thisCond = false;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" is not populated ";
						}
						else
						{	subIdx = fld4Val.indexOf(hashData);
							if(subIdx >= 0)
							{	thisCond = true;
								strMsgDesc += " and "+ getFieldDesc(fs,fss,k3) +" in (" + fld4Val + ") ";
							}
							else
							{	thisCond = false;
								strMsgDesc += " and "+ getFieldDesc(fs,fss,k3) +" not in (" + fld4Val + ") ";
							}
						}
					}
				}
				else
				{	if(isNull(hashData) )
					{	thisCond = true;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3)+" is not populated ";
					}
					else
					{	hashData = hashData.trim();
						if(!hasLength(hashData))
						{	thisCond = true;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" is not populated ";
						}
						else
						{	if(!isNull(fld4Val))
							{	subIdx = fld4Val.indexOf(hashData);
								if(subIdx >= 0)
								{	thisCond = false;
									strMsgDesc +=" and " +getFieldDesc(fs,fss,k3)+" in ("+fld4Val+") ";
								}
								else
								{	thisCond = true;
									strMsgDesc += " and " +getFieldDesc(fs,fss,k3) +" not in ("+fld4Val+
												 ") ";
								}
							}
						}
					}
				}
			}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}

		return fnlVrdct;
        }

	public boolean validateMultExist(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		int theocc, intocc;
		String newocc, k3;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();
		String fld2Val = vc.getFld2Val();
		String fld3 = vc.getFld3();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld2) || isNull(condtrue) || isNull(condfalse) )
		{
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT: " + fld1 + "," + fld2 + "," + condtrue + "," + condfalse);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		// get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);
		String strField1Desc = getFieldDesc(fs,fss,k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld2.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld2.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld2.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);
		String strField2Desc = getFieldDesc(fs,fss,k3);

		//Evaluate condition based on subIdx and condtrue/false
		if(isNull(hashData1))
		{	thisCond = false;
			setDetailMessage(" if " + strField1Desc + " is empty ");
		}
		else
		{	hashData1 = hashData1.trim();
			if(hasLength(hashData1))
			{	thisCond = true;
				setDetailMessage(" if " + strField1Desc + " is populated ");
			}
			else
			{	thisCond = false;
				setDetailMessage(" if " + strField1Desc + " is empty ");
			}
		}

		if(thisCond)
		{
			if(isNull(hashData2))
			{	thisCond = false;
				strMsgDesc += " and " + strField2Desc + " is empty ";
		   	}
			else
			{	hashData2 = hashData2.trim();
				if(hasLength(hashData2))
				{	thisCond = true;
					if(!isNull(fld2Val))
					{
						fld2Val = fld2Val.trim();  //ALTER construct to evaluate the data at field 2 as well
						if(hasLength(fld2Val))
						{	int subIdx = fld2Val.indexOf(hashData2);
							Log.write(Log.DEBUG_VERBOSE, "SUBNEG: index of  = " + hashData2 + " in " + fld2Val + " = " + subIdx);
							if(subIdx >= 0)
							{	thisCond = true;
								strMsgDesc += " and " + strField2Desc + " in (" + fld2Val + ") ";
							}
							else
							{	thisCond = false;
								strMsgDesc += " and " + strField2Desc + " not in (" + fld2Val + ") ";
							}
						}
					}
					else
					{
						strMsgDesc += " and " + strField2Desc + " is populated ";
					}
				}
				else
				{	thisCond = false;
					strMsgDesc += " and " + strField2Desc + " is empty ";
				}
			}
		}

		if(thisCond)
		{
			Log.write(Log.DEBUG_VERBOSE, "validateMisc: base premise TRUE");
			if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  	}
		  }

		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "validateMisc returns " + fnlVrdct);
		return fnlVrdct;

	}

//HD0000002472840S
	public boolean notExistA_AndB_ExistOrInSet(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		int theocc, intocc;
		String newocc, k3;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();
		String fld2Val = vc.getFld2Val();
		String fld3 = vc.getFld3();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld2) || isNull(condtrue) || isNull(condfalse) )
		{
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT: " + fld1 + "," + fld2 + "," + condtrue + "," + condfalse);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		// get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);
		String strField1Desc = getFieldDesc(fs,fss,k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld2.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld2.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld2.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);
		String strField2Desc = getFieldDesc(fs,fss,k3);

		//Evaluate condition based on subIdx and condtrue/false
		if(isNull(hashData1))
		{	thisCond = false;
			setDetailMessage(" if " + strField1Desc + " is empty ");
		}
		else
		{	hashData1 = hashData1.trim();
			if(hasLength(hashData1))
			{	thisCond = false;
				setDetailMessage(" if " + strField1Desc + " is populated ");
			}
			else
			{	thisCond = true;
				setDetailMessage(" if " + strField1Desc + " is empty ");
			}
		}

		if(thisCond)
		{
			if(isNull(hashData2))
			{	thisCond = false;
				strMsgDesc += " and " + strField2Desc + " is empty ";
		   	}
			else
			{	hashData2 = hashData2.trim();
				if(hasLength(hashData2))
				{	thisCond = true;
					if(!isNull(fld2Val))
					{
						fld2Val = fld2Val.trim();  //evaluate the data at field 2
						if(hasLength(fld2Val))
						{	int subIdx = fld2Val.indexOf(hashData2);
							Log.write(Log.DEBUG_VERBOSE, "SUBNEG: index of  = " + hashData2 + " in " + fld2Val + " = " + subIdx);
							if(subIdx >= 0)
							{	thisCond = true;
								strMsgDesc += " and " + strField2Desc + " in (" + fld2Val + ") ";
							}
							else
							{	thisCond = false;
								strMsgDesc += " and " + strField2Desc + " not in (" + fld2Val + ") ";
							}
						}
					}
					else
					{
						strMsgDesc += " and " + strField2Desc + " is populated ";
					}
				}
				else
				{	thisCond = false;
					strMsgDesc += " and " + strField2Desc + " is empty ";
				}
			}
		}

		if(thisCond)
		{
			Log.write(Log.DEBUG_VERBOSE, "validateMisc: base premise TRUE");
			if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  	}
		  }

		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "validateMisc returns " + fnlVrdct);
		return fnlVrdct;

	}

	public boolean notExistA_AndB_NotExistOrB_NotInSet(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		int theocc, intocc;
		String newocc, k3;

		String fld1 = vc.getFld1();
		String fld2 = vc.getFld2();
		String fld2Val = vc.getFld2Val();
		String fld3 = vc.getFld3();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld2) || isNull(condtrue) || isNull(condfalse) )
		{
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT: " + fld1 + "," + fld2 + "," + condtrue + "," + condfalse);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		// get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld1.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);
		String strField1Desc = getFieldDesc(fs,fss,k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld2.substring(c2+1);
			theocc = occ;
		}
		else
		{	newocc = fld2.substring(c2+1, c3);
			intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld2.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);
		String strField2Desc = getFieldDesc(fs,fss,k3);

		//Evaluate condition based on subIdx and condtrue/false
		if(isNull(hashData1))
		{	thisCond = false;
			setDetailMessage(" if " + strField1Desc + " is empty ");
		}
		else
		{	hashData1 = hashData1.trim();
			if(hasLength(hashData1))
			{	thisCond = false;
				setDetailMessage(" if " + strField1Desc + " is populated ");
			}
			else
			{	thisCond = true;
				setDetailMessage(" if " + strField1Desc + " is empty ");
			}
		}

		if(thisCond)
		{
			if(isNull(hashData2))
			{	thisCond = true;
				strMsgDesc += " and " + strField2Desc + " is empty ";
		   	}
			else
			{	hashData2 = hashData2.trim();
				if(hasLength(hashData2))
				{	thisCond = false;
					if(!isNull(fld2Val))
					{
						fld2Val = fld2Val.trim();  //evaluate the data at field 2
						if(hasLength(fld2Val))
						{	int subIdx = fld2Val.indexOf(hashData2);
							Log.write(Log.DEBUG_VERBOSE, "SUBNEG: index of  = " + hashData2 + " in " + fld2Val + " = " + subIdx);
							if(subIdx >= 0)
							{	thisCond = false;
								strMsgDesc += " and " + strField2Desc + " in (" + fld2Val + ") ";
							}
							else
							{	thisCond = true;
								strMsgDesc += " and " + strField2Desc + " not in (" + fld2Val + ") ";
							}
						}
					}
					else
					{
						strMsgDesc += " and " + strField2Desc + " is populated ";
					}
				}
				else
				{	thisCond = true;
					strMsgDesc += " and " + strField2Desc + " is empty ";
				}
			}
		}

		if(thisCond)
		{
			Log.write(Log.DEBUG_VERBOSE, "validateMisc: base premise TRUE");
			if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  	}
		  }

		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "validateMisc returns " + fnlVrdct);
		return fnlVrdct;

	}
//HD0000002472840F

   	public boolean eitherContains(int occ, String value, ValidationComponent vc)
	{

		boolean thisCond = false;
		int theocc = -1;
		String k3;

		String fld1 = vc.getFld1();
		String fld1Val = vc.getFld1Val();
		String fld2 = vc.getFld2();
		String fld2Val = vc.getFld2Val();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();
		int fld5Pos = vc.getFld5Pos();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld1Val) || isNull(fld2) ||
				isNull(fld2Val) || isNull(condtrue) || isNull(condfalse) ){
			boolean cond = false;
			Log.write(Log.WARNING, "eitherContains() BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + fld2 + "," + condtrue + "," + condfalse + "," + fld1Val + "," + fld2Val);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);

		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	String newocc = fld1.substring(c2+1, c3);
			int intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}
		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);
		Log.write(Log.DEBUG_VERBOSE, "eitherContains() fs=" +fs+" fss="+ fss+" fld="+k3+" occ="+theocc+" data["+hashData1+"]");
		String strField1 = getFieldDesc(fs,fss,k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld2.substring(c2+1);
			theocc = occ;
		}
		else
		{	String newocc2 = fld2.substring(c2+1, c3);
			int intocc2 = Integer.parseInt(newocc2);
			theocc = intocc2;
			k3 = fld2.substring(c3+1);
		}
		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);

		if(isNull(hashData1))
		{	thisCond = false;
			setDetailMessage(" if " + strField1 + " is empty ");
		}
		else
		{
			hashData1 = hashData1.trim();
			if(hasLength(hashData1))
			{	int subIdx = fld1Val.indexOf(hashData1);
				if(subIdx >= 0)
				{	thisCond = true;
					setDetailMessage(" if " + strField1 + " in (" + fld1Val + ") ");
				}
				else
				{
					thisCond = false;
					setDetailMessage(" if " + strField1 + " not in (" + fld1Val + ") ");
				}
			}
			else
			{	thisCond = false;
				setDetailMessage(" if " + strField1 + " is empty ");
			}
		}

		if(fld5Pos <= 0)
		{	if(!thisCond)
			{	if(isNull(hashData2))
				{	thisCond = false;
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				}
				else
				{	hashData2 = hashData2.trim();
					if(hasLength(hashData2))
					{	int subIdx = fld2Val.indexOf(hashData2);
						if(subIdx >= 0)
						{	thisCond = true;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " in (" + fld2Val + ") ";
						}
						else
						{	thisCond = false;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " not in (" + fld2Val + ") ";
						}
					}
					else
					{	thisCond = false;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
					}
				}

			}
		}
		else
		{	if(thisCond)
			{	if(isNull(hashData2))
				{	thisCond = false;
					strMsgDesc += " and " + getFieldDesc(fs,fss,k3) + " is not populated ";
				}
				else
				{	hashData2 = hashData2.trim();
					if(hasLength(hashData2))
					{	int subIdx = fld2Val.indexOf(hashData2);
						if(subIdx >= 0)
						{	thisCond = true;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" in ("+ fld2Val + ") ";
						}
						else
						{	thisCond = false;
							strMsgDesc += " and " + getFieldDesc(fs,fss,k3) +" not in ("+ fld2Val + ") ";
						}
					}
					else
					{	thisCond = false;
						strMsgDesc += " and " + getFieldDesc(fs,fss,k3)+ " is not populated ";
					}
				}
			}
		}

		if(thisCond)
		{	if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{	setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  		}
			}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}

		Log.write(Log.DEBUG_VERBOSE, "BOOLEAN eitherIn returns " + fnlVrdct);
		return fnlVrdct;

	}


	//
	// Ok - we met our criteria, now see if value we have is forbidden or accepted now.
	// This is usually doen by placing your restrictions in FLD5 and FLD5_VAL.
	//
	// Returns TRUE if VALUEISFORBIDDEN and field value matches one of the forbidden values
	// Returns TRUE if VALUEACCEPTED and field value not in list of acceptable values
	// Returns FALSE if condition is not set correctly, and above conditions aren't hit.
	//
	public boolean filterValue(String condition, String csvList, String value)
	{
		boolean result = false;
		String tok;
		setVerdictMessage("");

		Log.write("filterValue: value " + value + " condition " + condition + " csvList " + csvList);
		if(condition.equals("VALUEFORBIDDEN") && csvList != null && value != null)
		{
			StringTokenizer st = new StringTokenizer(csvList,",");
			while(st.hasMoreTokens())
			{ tok = st.nextToken();
			  int valueIdx = value.indexOf(tok);
		          Log.write(Log.DEBUG_VERBOSE, "index of VALUEFORBIDDEN:  " + valueIdx + " for " + csvList + " containing " + tok);
			  if(valueIdx != -1)
		          {     Log.write(Log.DEBUG_VERBOSE, "filterValue: returning true: VALUEFORBIDDEN:  " + value);
				result = true;
				setVerdictMessage("Value is prohibited ");
				break;
			  }
			}
		}

		if(condition.equals("VALUEACCEPTED") && csvList != null && value != null)
		{
			StringTokenizer st = new StringTokenizer(csvList,",");
			while(st.hasMoreTokens())
			{ tok = st.nextToken();
		          Log.write(Log.DEBUG_VERBOSE, "VALUEACCEPTED: value = " + value + " token: " + tok);
			  int valueIdx = value.indexOf(tok);
		          Log.write(Log.DEBUG_VERBOSE, "index of VALUEACCEPTED:  " + valueIdx + " for " + csvList + " containing " + tok);
			  if(valueIdx >= 0)
		          {     Log.write(Log.DEBUG_VERBOSE, "filterValue: returning false: VALUE IS OK:  " + value);
				result = false;
				setVerdictMessage("Value is accepted ");
				break;
			  }
			  else
			  {
				result = true;
				setVerdictMessage("Acceptable values are (" + csvList + ") ");
			  }
			}
		}
		Log.write(Log.DEBUG_VERBOSE, "filterValue: returning " + result);

		return result;

	}

	public boolean isPopulated(int occ, String value, ValidationComponent vc)
	{
				//Commenting out unnecessary logs - Antony 05/18/10
                /*
		Log.write("isPopulated(" + occ + "," + value + ","	+ vc.getFrmSqncNmbr() + "/" +
														 vc.getFrmSctnSqncNmbr() + "/" +
														 vc.getFrmFldNmbr() + "/" +
														 vc.getValSqncNmbr() + "/" +
														 vc.getFrmFldSrtSqnc() + "/" +
														 vc.getVldtnSrtOrdr() + "/" +
														 vc.getFld1() + "/" +
														 vc.getFld1Val() + "/" +
														 vc.getFld1Pos() + "/" +
														 vc.getFld2() + "/" +
														 vc.getFld3() + "/" +
														 vc.getFld3Val() + "/" +
														 vc.getFld3Pos() + "/" +
														 vc.getFld4() + "/" +
														 vc.getFld4Val() + "/" +
														 vc.getFld4Pos() + "/" +
														 vc.getFld5() + "/" +
														 vc.getFld5Val() + "/" +
														 vc.getFld5Pos() + "/" +
														 vc.getCondTrue() + "/" +
														 vc.getCondFalse() + ")");
                */
            
		boolean thisCond = false;
		String k3;
		int theocc;

		String fld1 = vc.getFld1();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();

		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1))
		{	boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + condtrue + "," + condfalse);
			return true;
		}
		String strMsgDesc="";
		setDetailMessage("");

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			theocc = occ;
		}
		else
		{	String newocc2 = fld1.substring(c2+1, c3);
			int intocc2 = Integer.parseInt(newocc2);
			theocc = intocc2;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData = getHashData(fs, fss, theocc, k3);

		if(hashData == null)
		{	thisCond = false;
			Log.write(Log.DEBUG_VERBOSE, " if " + getFieldDesc(fs,fss,k3) + " is not populated *");
			setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is not populated ") ;
		}
		else
		{	hashData = hashData.trim();
			if(hasLength(hashData))
			{	thisCond = true;
				Log.write(Log.DEBUG_VERBOSE, " if " + getFieldDesc(fs,fss,k3) + " is populated **");
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is populated ") ;
			}
			else
			{	thisCond = false;
				Log.write(Log.DEBUG_VERBOSE, " if " + getFieldDesc(fs,fss,k3) + " is not populated ***");
				setDetailMessage(" if " + getFieldDesc(fs,fss,k3) + " is not populated ") ;
			}
		}

		if(thisCond)
		{	if(fld5 != null)
			{	if(filterValue(fld5,fld5Val,value))
				{
					Log.write(getVerdictMessage() + getDetailMessage() + strMsgDesc + "****");
					setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
					return false;
		  		}
		  	}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{	Log.write(getVerdictMessage() + getDetailMessage() + strMsgDesc + "*****");
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}
		
                //Log.write(Log.DEBUG_VERBOSE, "BOOLEAN isPopulated returns " + fnlVrdct);
                //Commenting out unnecessary logs - Antony 05/18/10

		return fnlVrdct;

	}

	private boolean finishedVerdict(boolean thisCond, String value, String condtrue, String condfalse)
	{
		//Log.write("finishedVerdict(" + thisCond + "," + value + "," + condtrue + "," + condfalse + ")");
                //Commenting out unnecessary logs - Antony 05/18/10
            
		setVerdictMessage("");

		if(thisCond){
			//Log.write(Log.DEBUG_VERBOSE, "finishedVerdict : true");
                        //Commenting out unnecessary logs - Antony 05/18/10
                    
			if(condtrue.equals("REQ") &&  isNull(value) ){
				Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict true, REQ, null");
				setVerdictMessage("Field is required ");
				return false;
			}
			if(condtrue.equals("REQ") &&  !hasLength(value)){
				 Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict true, REQ, 0 len");
				setVerdictMessage("Field is required ");
				return false;
			}
			if(condtrue.equals("PRO") &&  !isNull(value) ) {
				if(hasLength(value)){
			          	Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict true, PRO, len");
					setVerdictMessage("Field is prohibited ");
					return false;
				}
			}

		}else{
			if(condfalse.equals("REQ") &&  isNull(value) ){
				 Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict false, REQ, null");
				setVerdictMessage("Field is required ");
				return false;
			}
			if(condfalse.equals("REQ") &&  !hasLength(value) ){
				 Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict false, REQ, 0 len");
				setVerdictMessage("Field is required ");
				return false;
			}
			if(condfalse.equals("PRO") &&  !isNull(value)) {
				if(hasLength(value)){
					Log.write(Log.DEBUG_VERBOSE, "BOOLEAN finishedVerdict false, PRO, len");
					setVerdictMessage("Field is prohibited ");
				  	return false;
				}
			}
		}
		setVerdictMessage("Value is optional ");

		return true;

	}


	public char getReqtyp0(int occ){

		String tData1 = getHashData(1, 1, occ, "23");

		if(isNull(tData1)){
			Log.write(Log.DEBUG_VERBOSE, "getReqtyp0: hash lookup :no match for LSR/REQTYP field ");
			return ' ';
		}

		if(!hasLength(tData1)){
			Log.write(Log.DEBUG_VERBOSE, "getReqtyp0: zero length for LSR/REQTYP field ");
			return ' ';
		}

		char req0 = tData1.charAt(0);

		Log.write(Log.DEBUG_VERBOSE, "getReqtyp0:  LSR/REQTYP[0]: " + req0);

		return(req0);
	}

	public boolean validateLsrAct(int occ, String value)
	{
		String strTemp;
		Log.write("validateLsrAct: value =" + value);
		setDetailMessage("");

		int vLen = 0;

		if(isNull(value)){
		   vLen = value.length();
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrAct: the value length= " + vLen);
		}

		char req0 = getReqtyp0(occ);

		if(req0 == ' ')
		{
			setDetailMessage("LSR/REQTYP is not populated yet so validation cannot be completed");
			return false;
		}

	        if( req0 == 'B' && value.equals("T"))
		{	Log.write(Log.DEBUG_VERBOSE, "validateLsrDsptch: DSPTCH Field must NOT equal 'T' Check REQTYP" );
			setDetailMessage("If LSR/REQTYP is 'B' then field cannot be 'T' ");
			return false;
	        }

	        if( (req0 == 'C' || req0 == 'H' || req0 == 'F' || req0 == 'G' || req0 == 'J') &&
		    (value.equals("T") || value.equals("M") ) )
		{
			Log.write(Log.DEBUG_VERBOSE, "validateLsrDsptch: DSPTCH Field must NOT equal 'T' Check REQTYP" );
			setDetailMessage("If LSR/REQTYP is 'C','F','G','H','J' then field cannot be 'T' or 'M' ");
			return false;
	        }

	        if( req0 == 'G' || req0 == 'H' || req0 == 'J' &&  vLen >= 1)
		{	Log.write(Log.DEBUG_VERBOSE, "validateLsrDsptch: DSPTCH Field must NOT be populated! Check REQTYP" );
			setDetailMessage("Field prohibited if LSR/REQTYP[ is 'G', 'H', or 'J'");
			return false;
	        }
		return(true);
	}

	public boolean tindTimeCheck(int occ, String value)
	{
		String s1;
		Log.write("tindTimeCheck: value =" + value);

		String tData1 = getHashData(15, 1, occ, "12");

		if(isNull(tData1))
		{	Log.write(Log.DEBUG_VERBOSE, "tindTimeCheck: hash lookup :no match for LSRCM/TIND field ");
			setDetailMessage("Validation failed because LSRCM/TIND is empty");
			return false;
		}
		Log.write(Log.DEBUG_VERBOSE, "tindTimeCheck: LSRCM/TIND: tData1 =" + tData1);

		int vLen = 0;

		if(value != null){
		   vLen = value.length();
		   Log.write(Log.DEBUG_VERBOSE, "tindTimeCheck: the value length= " + vLen);
		}

		if( tData1.length() <= 0  && vLen >= 1)
		{	Log.write(Log.DEBUG_VERBOSE, "tindTimeCheck: TIME Field must NOT be populated!" );
			setDetailMessage("Field prohibited if LSRCM/TIND is empty");
			return false;
		}

		return(true);
	}

	public boolean validateLsrDfdt(int occ, String value)
	{
		String strTemp;
		Log.write("validateLsrDfdt: value =" + value);

		int vLen = 0;
		if(value != null){
		   vLen = value.length();
		   Log.write(Log.DEBUG_VERBOSE, "isLsrDfdt: the value length= " + vLen);
		}


		char req0 = getReqtyp0(occ);
		if(req0 == ' ')
		{	setDetailMessage("LSR/REQTYP is not populated yet so validation cannot be completed");
			return false;
		}

	        if( req0 == 'G' || req0 == 'H' || req0 == 'J' &&  vLen >= 1)
		{	Log.write(Log.DEBUG_VERBOSE, "validateLsrDfdt: DSPTCH Field must NOT be populated! Check REQTYP" );
			setDetailMessage("If LSR/REQTYP is 'G', 'H' or 'J' then field must NOT be populated");
			return false;
	        }

		return(true);
	}

	private int verifyExistence(int fseq, int fss, int occ, String mox, String imox, String value)
		throws Exception
	{

		String hTyp = null;

		if(mox == null)
			return 0;

		if(mox.equals("x"))
			return 0;

		if(mox.equals("REQUIRED")){
			if(!hasLength(value))
				return(2);
			else
				return(0);
		}

		 Log.write(Log.DEBUG_VERBOSE, "verifyExistence: contents of mox -> " + mox + "input fseq = " + fseq + ", current fseq = " + this.currFormSeq);
		if(this.currFormSeq != fseq){
			boolean umox = setMoxParm(fseq);
			if(!umox) return(-1);
		        Log.write(Log.DEBUG_VERBOSE, "verifyExistence: new MOX_PARMS:" + currFormSeq + ", current mox = " + this.currFormMox);
		}

		//Check format of imox

		Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX = " + imox);

		if(imox == null)
			return 0;

		int c1 = imox.indexOf(',');
		Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - comma  ?= " + c1 + "imox = " + imox + "mox = " + mox);
		if(c1 != -1){
		   int c2 = imox.indexOf(',',c1 + 1);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - comma2 = " + c2);
		   int c3 = imox.indexOf(',',c2 + 1);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - comma3 = " + c3);
		   String k1 = imox.substring(0,c1);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - k1 = " + k1);
		   String k2 = imox.substring(c1+1, c2);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - k2 = " + k2);
		   String k3 = imox.substring(c2+1,c3);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - k3 = " + k3);
		   String k4 = imox.substring(c3+1);
		   Log.write(Log.DEBUG_VERBOSE, "verifyExistence -- IMOX_FORMAT - k4 = " + k4);

		   int fs = Integer.parseInt(k1);
		   int fssq = Integer.parseInt(k2);
		   int focc = Integer.parseInt(k3);

		   hTyp = getHashData(fs, fssq, focc, k4);
		}else{
		   hTyp = getHashData(fseq, fss, occ, imox);

		}

	        Log.write(Log.DEBUG_VERBOSE, "verifyExistence: AFTER hashLookup ");

		if(isNull(hTyp)){
	        	 Log.write(Log.DEBUG_VERBOSE, "verifyExistence: hash lookup(type) returned null");
			return(-1);
		}
	        Log.write(Log.DEBUG_VERBOSE, "verifyExistence: hashLookup (type) -> " + hTyp);
		int typIdx = currFormMox.indexOf(hTyp);
		if(typIdx == -1){
	        	Log.write(Log.DEBUG_VERBOSE, "verifyExistence: index lookup(type) returned -1");
			return(-1);
		}
	        Log.write(Log.DEBUG_VERBOSE, "verifyExistence: index of " + hTyp + " in " + currFormMox + " is " + typIdx);
		char fStat = mox.charAt(typIdx);
	        Log.write(Log.DEBUG_VERBOSE, "verifyExistence: field requirement = "  + fStat);

		if(fStat == 'O' && !hasLength(value))
			return(1);

		if(fStat == 'R' && !hasLength(value))
			return(2);


		return 0;

	}

	public boolean isNull(String value){

		if(value == null)
			return(true);

		return(false);
	}

	public boolean hasLength(String value)
	{
		if(value == null)
			return(false);

		if(value.length() > 0)
			return(true);

		return(false);
	}


	public boolean verifyDatatype(String dt, String fc, String value){

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		if(dt.equals("X"))
		{
			Log.write("verifyDatatype: Alpha numeric data type: " + value);
			setDataDesc("letters and numbers, no special characters");
			if(Validate.containsSpecialChars(value)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: contains special chars FAILED " + value);
			   return false;
			}
			if(!checkSpecialIdent(value, fc)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: check special Ident FAILED " + value);
			   return false;
			}

		}
		else if(dt.equals("A"))
		{
			Log.write("verifyDatatype: Alpha data type: " + value);
			setDataDesc("letters only (with no special characters) ");
			if(Validate.containsSpecialChars(value)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: contains special chars failed " + value);
			   return false;
			}
			if(!allLetters(value)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: allLetters test failed " + value);
			   return false;
			}

		}
		else if(dt.equals("N"))
		{
			Log.write("verifyDatatype: numeric data type: " + value);
			setDataDesc("numeric characters only ");
			if(!allDigits(value)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: allDigits test failed " + value);
			   return false;
			}
		}
		else if(dt.equals("S"))
		{
			Log.write("verifyDatatype: Alpha numeric data type: " + value);
			setDataDesc(" Letters and numbers and special characters (no < or > allowed ");
			if(Validate.containsSpecialChars(value)){
			   Log.write(Log.DEBUG_VERBOSE, "verifyDatatype: contains special chars FAILED " + value);
			   return false;
			}

		}

		return(true);
	}

	public boolean verifyLength(int len, String value,  String FldCd){

	        //Log.write("verifyLength: value = " + value + " len = " + len);
                //Commenting out unnecessary logs - Antony 05/18/10
            
		if(value.length() > len){
	        	Log.write(Log.DEBUG_VERBOSE, "verifyLength: length exceeds max " + value.length() + " return false" );
			return false;
		}
                
                //Code change to Validate lengths for Dcris Order Number - Saravanan
		if(FldCd.equals("DCRIS ORDR NBR") && value.length() < len-2){
                        Log.write(Log.DEBUG_VERBOSE, "verifyLength: length less than limit " + value.length() + " return false" );
                        return false;
                }
                return(true);
	}

	public boolean validateDateTime(String value)
	{
		String strTemp;

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......
		Log.write("validateDateTime: " + value);

		if(value.length() !=  17)
			return false;

		strTemp = value.substring(0,10);

		if(!Validate.isValidDate(strTemp, '-')) {
			setDetailMessage("Format MM-DD-YYYY-HHMMAM/PM was expected.  Invalid date component");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "validateDateTime: date validation passed");
		if(value.charAt(10) != '-')
			return false;

		strTemp = value.substring(11);

		String t1 = strTemp.substring(0,4);
		Log.write(Log.DEBUG_VERBOSE, "validateDateTime: time(1) " + t1);

		if(!allDigits(t1))
			return false;
		String t2 = strTemp.substring(4);

		if(!t2.equals("AM") && !t2.equals("PM")) {
			setDetailMessage("Date/time format of MM-DD-YYYY-HHMMAM/PM was expected.  AM or PM must be specified in uppercase");
			return false;
		}

		String u1 = t1.substring(0,2);
		Log.write(Log.DEBUG_VERBOSE, "validateDateTime: time(hr) " + u1);
		String u2 = t1.substring(2);
		Log.write(Log.DEBUG_VERBOSE, "validateDateTime: time(min) " + u2);

		Integer gI = new Integer(0);

		int hour = gI.parseInt(u1);
		int min = gI.parseInt(u2);

		if(hour < 0 || hour > 12) {
			setDetailMessage("Date/time format of MM-DD-YYYY-HHMMAM/PM was expected.  Hours are invalid");
			return false;
		}

		if(min < 0 || min > 59) {
			setDetailMessage("Date/time format of MM-DD-YYYY-HHMMAM/PM was expected.  Minutes are invalid");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "validateDateTime returns true");
		return(true);
	}

	public boolean validatePhoneFormat(String value)
	{

		String strTemp;
		//Log.write("validatePhoneFormat: value =" + value);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		if(value.length() == 1)
		{
			if(value.equals("N"))
				return true;
			else
				return false;
		}

		if(value.length() != 12)
			return false;

		if(value.charAt(3) != '-')
			return false;

		if(value.charAt(7) != '-')
			return false;

		strTemp = value.substring(0,3);
		//Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: npa " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!allDigits(strTemp)){
			Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: npa digit test failed  ");
			return false;
		}
		strTemp = value.substring(4,7);
		//Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: nxx " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: nxx digit test failed  ");
			return false;
		}

		strTemp = value.substring(8);
		//Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: line " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: line digit test failed  ");
			return false;
		}

		//Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: returns true");
                //Commenting out unnecessary logs - Antony 05/18/10
                
		return(true);
	}

	public boolean validateMandatoryPhoneFormat(String value)
	{

		String strTemp;
		//Log.write("validateMandatoryPhoneFormat: value =" + value);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		if(value.length() != 12)
			return false;

		if(value.charAt(3) != '-')
			return false;

		if(value.charAt(7) != '-')
			return false;

		strTemp = value.substring(0,3);
		//Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: npa " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		if(!allDigits(strTemp)){
			Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: npa digit test failed  ");
			return false;
		}
		strTemp = value.substring(4,7);
		//Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: nxx " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: nxx digit test failed  ");
			return false;
		}

		strTemp = value.substring(8);
		//Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: line " + strTemp);
                //Commenting out unnecessary logs - Antony 05/18/10
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validatePhoneFormat: line digit test failed  ");
			return false;
		}

		//Log.write(Log.DEBUG_VERBOSE, "validateMandatoryPhoneFormat: returns true");
                //Commenting out unnecessary logs - Antony 05/18/10
		return(true);
	}

	public boolean validateZIPCodeFormat(String value)
	{

		Log.write("validateZIPCodeFormat: value =" + value);

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		if(value.length() != 5)
			return false;

		Log.write(Log.DEBUG_VERBOSE, "validateZIPCodeFormat: returns true");
		return(true);
	}

	public boolean validateNNSPCodeFormat(String value)
	{

		Log.write("validateNNSPCodeFormat: value =" + value);

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		if(value.length() != 4)
			return false;

		Log.write(Log.DEBUG_VERBOSE, "validateNNSPCodeFormat: returns true");
		return(true);
	}


	public boolean validateTimeSpent(int occ, String value, String dMask)
	{
		String strTemp;
		Log.write("validateTimeSpent: value =" + value);

		if(!hasLength(value)){
			Log.write(Log.DEBUG_VERBOSE, "validateTimeSpent: no length -- return true");
			return true;
		}

		int vLen = value.length();

		if(vLen < 3)
			return false;

		if(value.charAt(vLen - 3) != '.')
			return false;

		if(vLen > 3)
		{	String s2 = value.substring(0,vLen-3);
			Log.write(Log.DEBUG_VERBOSE, "validateTimeSpent: pre-decimal value =" + s2);
			if(!allDigits(s2))
			{	setDetailMessage("Format " + dMask + " expected with only digits and '.' allowed");
				return false;
			}
		}

		strTemp = value.substring(vLen-2);

		Log.write(Log.DEBUG_VERBOSE, "validateTimeSpent:  " + strTemp);
		if(!allDigits(strTemp))
			return false;

		int tUnit = Integer.parseInt(strTemp);

		if(Math.IEEEremainder(tUnit, 25) != 0)
		{	setDetailMessage("Time should be recorded to nearest quarter hour (ie 15 minutes = .25)");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "validateTimeSpent: returns true");
		return(true);
	}

	private boolean unitOfMeasure(int occ, String value, ValidationComponent vc)
	{
		String unitVal = vc.getFld1Val();

		if(isNull(unitVal)){
			return true;
		}

		if(isNull(value)){
			return true;
		}

		value = value.trim();
		if(!hasLength(value)){
			return true;
		}

		int vLen = value.length();
		if(vLen < 2)
			return false;

		String s1 = value.substring(0,vLen-1);
		String s2 = value.substring(vLen-1);
		Log.write(Log.DEBUG_VERBOSE, "s1 = ["+s1+"] s2=["+s2+"]");

		if(!allDigits(s1))
		{	boolean bDecHit = false;
			//Spin thru and check -Only digits and decimal point allowed
			for (int i=0; i < s1.length(); i++)
			{
				if ( Character.isDigit( s1.charAt(i) ) ) {}
				else {
					String strDec = ""+ s1.charAt(i);
					if ( strDec.equals(".") ) {
						if (bDecHit)
						{	setDetailMessage("Only one decimal point allowed ###.##U format");
							return false;
						} else	bDecHit = true;
					}
					else {
						setDetailMessage("Expected ###U or ##.##U format where # is digit " +
							" and U is unit of measure ("+ unitVal + ")");
						return false;
					}
				}
			}
		}

		int frmtIdx = unitVal.indexOf(s2);
		if(frmtIdx == -1)
		{	setDetailMessage("Last character of field must define unit of measure. Acceptable values (" + unitVal + ")");
			return false;
		}

		return true;
	}

	public boolean validateLsrAN(int occ, String value){

		String s1;

		Log.write("validateLsrAN: value =" + value);

		if(value != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: value length= " + value.length());

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		String tData1 = getHashData(2, 4, occ, "40");

		if(isNull(tData1)){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: hash lookup :no match for EU/EAN field ");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "EU/EAN: tData1 =" + tData1);

		if(value != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: tData1 length= " + tData1.length());

		String tData2 = getHashData(1, 1, occ, "8");

		if(isNull(tData2)){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: hash lookup: no match for LSR/ATN field ");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "LSR/ATN: tData2=" + tData2);

		if(tData2 != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: tData2 length= " + tData2.length());


		int vLen = 0;

		if(value != null){
		   vLen = value.length();
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: the value length= " + vLen);
		}

		if( (tData1.length() < 1 || tData2.length() < 1) &&  vLen <= 1){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: AN Field must be populated!" );
			return false;
		}

		return(true);
	}

	public boolean validateLsrATN(int occ, String value){

		String s1;

		Log.write("validateLsrATN: value =" + value);

		if(value != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: value length= " + value.length());

		if(!hasLength(value))
			return true; //assume "OPTIONAL" for now......

		String tData1 = getHashData(2, 4, occ, "41");

		if(isNull(tData1)){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: hash lookup :no match for EU/EATN field ");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: EU/EATN: tData1 =" + tData1);

		if(value != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: tData1 length= " + tData1.length());

		String tData2 = getHashData(1, 1, occ, "7");

		if(isNull(tData2)){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrAN: hash lookup: no match for LSR/AN field ");
			return false;
		}

		Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: LSR/AN: tData2=" + tData2);

		if(tData2 != null)
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: tData2 length= " + tData2.length());

		int vLen = 0;

		if(value != null){
		   vLen = value.length();
		   Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: the value length= " + vLen);
		}

		if( (tData1.length() < 1 || tData2.length() < 1) &&  vLen <= 1){
			Log.write(Log.DEBUG_VERBOSE, "validateLsrATN: ATN Field must be populated!" );
			return false;
		}

		return(true);

	}


	public boolean validateMaterialCost(int occ, String value){

		String s1;

		Log.write("validateMaterialCost: value =" + value);

		if(!hasLength(value)){
			Log.write(Log.DEBUG_VERBOSE, "validateMaterialCost: len 0");
			return true;
		}

		String tinData = getHashData(15, 1, occ, "14");

		if(isNull(tinData))
		{	Log.write(Log.DEBUG_VERBOSE, "validateMaterialCost: no match for MTL field ");
			setDetailMessage("Field is prohibited when MTL field is not populated");
			return false;
		}

		int vLen = value.length();
		Log.write(Log.DEBUG_VERBOSE, "validateMaterialCost: len " + vLen);

		if(tinData.length() <=  0 && vLen >= 1)
		{	Log.write(Log.DEBUG_VERBOSE, "validateMaterialCost: no data for MTL field = prohibited MCOST field");
			setDetailMessage("Field is prohibited when MTL field is not populated");
			return false;
		}

		if(vLen < 3)
			return false;

		if(value.charAt(vLen - 3) != '.')
			return false;

		if(vLen > 3){
			String s2 = value.substring(0,vLen-3);
			Log.write(Log.DEBUG_VERBOSE, "validateMaterialCost: pre-decimal value =" + s2);
			if(!allDigits(s2))
				return false;
		}

		return(true);
	}

	public boolean isMoney(String strFieldValue)
	{
		Log.write("isMoney: Value =" + strFieldValue);
		if(!hasLength(strFieldValue))
		{	//empty field passes edit
			Log.write(Log.DEBUG_VERBOSE, "isMoney: len 0");
			return true;
		}

		int iLen = strFieldValue.length();
		Log.write(Log.DEBUG_VERBOSE, "isMoney: len " + iLen);

		if(iLen < 3) 	//accept .## at a minimum
		{	Log.write(Log.DEBUG_VERBOSE, "isMoney: length < 3");
			return false;
		}

		if(strFieldValue.charAt(iLen - 3) != '.')
		{	Log.write(Log.DEBUG_VERBOSE, "isMoney: no Decimal point");
			return false;
		}

		if(iLen >= 3)
		{	String strDollars = "";
			String strCents = "";
			strDollars = strFieldValue.substring(0,iLen-3);
			strCents = strFieldValue.substring(iLen-2);
			Log.write(Log.DEBUG_VERBOSE, "isMoney: Dollars["+strDollars+"] Cents["+strCents+"]");
			if(!allDigits(strCents))
				return false;
			if ( (!isNull(strDollars)) && (strDollars.length() > 1) && (!allDigits(strDollars)) )
				return false;
		}

		return(true);
	}

	public boolean validDslUserid(String strFieldValue)
	{
		if(!hasLength(strFieldValue))
		{	//empty field passes edit
			return true;
		}

		int iLen = strFieldValue.length();

		if(iLen < 3)
		{
			setDetailMessage("Userid must be at least 3 characters long");
			return false;
		}
		if(iLen > 30)
		{
			setDetailMessage("Userid cannot exceed 30 characters");
			return false;
		}

		String str1st3 = strFieldValue.substring(0,3);
		if (!allLetters(str1st3))
		{	Log.write(Log.DEBUG_VERBOSE, "validDslUserid() 1st 3 must be letters");
			setDetailMessage("Userid must begin with 3 letters");
			return false;
		}

		return(true);
	}

	public boolean validDslPassword(String strFieldValue)
	{
		if(!hasLength(strFieldValue))
		{	//empty field passes edit
			return true;
		}

		int iLen = strFieldValue.length();

		if(iLen < 6)
		{
			setDetailMessage("Password must be at least 6 characters long");
			return false;
		}
		if(iLen > 20)
		{
			setDetailMessage("Password cannot exceed 20 characters");
			return false;
		}

		return(true);
	}

	public boolean doesNPAMatchState(int iOcc, String strFieldValue, ValidationComponent vc)
		throws SQLException
	{
		boolean thisCond = false;
		int 	iTheOcc = -1;

                if( isNull(strFieldValue) || !hasLength(strFieldValue))
                {       //empty field passes edit
                        return true;
                }
		if ( strFieldValue.length() < 3 )
		{
			setDetailMessage("NPA is not associated with state code defined on this order");
                        return false;
		}

		String fld1 = vc.getFld1();
		String fld1Val = vc.getFld1Val();
		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1))	//Validation not set up correctly, we need field1
		{	boolean cond = false;
			Log.write(Log.WARNING,"FieldValidationBean doesNPAMatchState() table data incomplete 1");
			return true;
		}

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		String k3;
		if(c3 == -1)
		{	k3 = fld1.substring(c2+1);
			iTheOcc = iOcc;
		}else
		{	String newocc = fld1.substring(c2+1, c3);
			iTheOcc = Integer.parseInt(newocc);
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);
		Log.write(Log.DEBUG_VERBOSE, "doesNPAMatchState() NPA chk c1="+c1+" c2="+c2+" c3="+c3+" k1="+k1+" k2="+k2+" k3="+k3+"  fs="+fs+" fss="+fss+"  iTheOcc="+iTheOcc);

		String strDBStateCode ="";
		String strStateCode = getHashData(fs, fss, iTheOcc, k3);
		String strNPA = strFieldValue.substring(0,3);
		Log.write(Log.DEBUG_VERBOSE, "doesNPAMatchState() TN:"+ strFieldValue + " NPA="+ strNPA + " STcode on FLD1=" + strStateCode);

		if( strStateCode == null){	//field were comparing to is null....cant go on
                        Log.write(Log.DEBUG_VERBOSE, "State code on FLD1 is null");
			return false;
		}
		strStateCode = strStateCode.toUpperCase().trim();

		String strQuery = "SELECT UPPER(STT_CD) AS STT_CD FROM NPA_T WHERE NPA='" + strNPA + "'";
                rs = this.stmt.executeQuery(strQuery);
                if(rs.next()){
                        strDBStateCode = rs.getString("STT_CD");
                        Log.write(Log.DEBUG_VERBOSE, "doesNPAMatchState() State="+strDBStateCode+" for NPA="+strNPA);
                }
                rs.close();

		if ( (isNull(strDBStateCode)) || (strDBStateCode.length() < 2) )
		{
			Log.write(Log.DEBUG_VERBOSE, "NPA " + strNPA + " not found in NPA_T table");
			setDetailMessage("NPA "+strNPA+" is not associated with state code ("+strStateCode+") defined on this order");
			return false;
		}

		if(strStateCode.equals(strDBStateCode))
		{	Log.write(Log.DEBUG_VERBOSE, "doesNPAMatchState() State code test passed OK");
		}
		else
		{
			Log.write(Log.DEBUG_VERBOSE, "NPA "+strNPA+" assoc w state "+strDBStateCode+" conflicts with state code ("+strStateCode+
				") defined on this order");
			setDetailMessage("NPA "+strNPA+" is associated with state "+strDBStateCode+" which conflicts with the state code ("+
				strStateCode+") defined on this order");
			return false;
		}

		return(true);
	}

	public boolean validatePgOF(String value, int sc){

		String s1;

		Log.write("validatePgOF: value =" + value);

		if(value.length() != 11 )
			return false;

		if(value.charAt(2) != ' ')
			return false;

		if(value.charAt(5) != ' ')
			return false;

		if(value.charAt(8) != ' ')
			return false;

		s1 = value.substring(3,5);
		Log.write(Log.DEBUG_VERBOSE, "validatePgOF: Pg " + s1);
		if(!allDigits(s1))
			return false;

		s1 = value.substring(8);
		Log.write(Log.DEBUG_VERBOSE, "validatePgOF: OF " + s1);
		if(!allDigits(s1))
			return false;

		return(true);
	}

	public boolean validateNOR(String value)
	{
		String strTemp;

		if(!hasLength(value)){
			Log.write("validateNOR: len 0");
			return true;
		}
		if(value.length() < 3 )
			return false;

		int dash = value.indexOf('-');

		if(dash == -1)
			return false;

		strTemp = value.substring(0,dash);
		Log.write(Log.DEBUG_VERBOSE, "validateNOR segment 1 " + strTemp);
		if(!allDigits(strTemp))
			return false;

		strTemp = value.substring(dash+1);
		Log.write(Log.DEBUG_VERBOSE, "validateNOR: segment 2 " + strTemp);
		if(!allDigits(strTemp))
			return false;

		return(true);
	}

	public boolean validatePgOF(String value){

		String s1;

		Log.write("validatePgOF: value =" + value);

		if(!allDigits(value))
			return false;

		return(true);
	}

	public boolean validateExtendedPhoneFormat(String value)
	{
		String strTemp;
		Log.write("validateExtendedPhoneFormat: value =" + value);

		if(!hasLength(value)){
			return true;
		}

		if(value.length() == 1){

			if(value.equals("N"))
				return true;
			else
				return false;
		}

		if(value.length() < 12)
			return false;

		if(value.charAt(3) != '-')
			return false;

		if(value.charAt(7) != '-')
			return false;


		strTemp = value.substring(0,3);
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validateExtendedPhoneFormat: npa digit test failed  ");
			return false;
		}

		strTemp = value.substring(4,7);
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validateExtendedPhoneFormat: nxx digit test failed  ");
			return false;
		}

		strTemp = value.substring(8,12);
		if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validateExtendedPhoneFormat: line digit test failed  ");
			return false;
		}

		if(value.length() > 12){
		   if(value.charAt(12) != '-')
			return false;
		   if(value.length() > 13){
		      strTemp = value.substring(13);
		      if(!allDigits(strTemp)) {
			Log.write(Log.DEBUG_VERBOSE, "validateExtendedPhoneFormat: range digit test failed  ");
			return false;
		      }
		   }

		}

		Log.write(Log.DEBUG_VERBOSE, "validateExtendedPhoneFormat returns true");
		return(true);
	}

	private void setDataDesc(String value){

		this.dataDesc = value;
	}

	public String getDataDesc(){

		return(this.dataDesc);
	}

	public boolean allDigits(String value)
	{
		boolean verdict = true;

		for(int i=0;i<value.length(); i++){
			if(!Character.isDigit(value.charAt(i))){
				verdict = false;
				break;
			}
		}

		return verdict;
	}

	public boolean allLetters(String value)
	{
		boolean verdict = true;

		for(int i=0;i<value.length(); i++){
			if(!Character.isLetter(value.charAt(i))){
				verdict = false;
				break;
			}
		}

		return verdict;
	}

	public boolean checkSpecialIdent(String value, String fc){
		// Including '-', etc ... for now....
		boolean verdict = true;
		Log.write(value + " : checkSpecialIdent len: " + fc + " : " + value.length());

		if(fc.equals("REMARKS") || fc.equals("RDET") || fc.equals("CUST") || fc.indexOf("NAME") != -1 || fc.indexOf("NM") != -1 || fc.indexOf("CITY") != -1 || fc.indexOf("STATE") != -1 || fc.indexOf("EMAIL") != -1 || fc.indexOf("FLOOR") != -1) {
			Log.write(fc + " : checkSpecialIdent FREEFORM: " + fc);
			return verdict;
		}
                //Code change to Validate Special Characters for Dcris Order Number - Saravanan
		if(!fc.equals("DCRIS ORDR NBR"))
		{
                        for(int i=0;i<value.length(); i++){
                                if(!Character.isJavaIdentifierPart(value.charAt(i)) && value.charAt(i) != '-' && value.charAt(i) != '/' && value.charAt(i) != '.'  && value.charAt(i) != ' '){

                                        Log.write(Log.DEBUG_VERBOSE, "isJavaIdentifierPart test failed:  " + value.charAt(i));
                                        verdict = false;
                                        break;
                                }
                        }
                        Log.write(value + " : checkSpecialIdent returns:  " + verdict);
                        return verdict;
                }
		else
		{
                        for(int i=0;i<value.length(); i++){
				if((!Character.isJavaIdentifierPart(value.charAt(i)) || value.charAt(i) == '$' || value.charAt(i) == '_') && value.charAt(i) != '-'){

					Log.write(Log.DEBUG_VERBOSE, "isJavaIdentifierPart test failed:  " + value.charAt(i));
					verdict = false;
					break;
				}
			}
			Log.write(value + " : checkSpecialIdent returns:  " + verdict);
                        return verdict;
			
		}
	}

	public boolean containsDigit(String value){
		boolean verdict = false;

		for(int i=0;i<value.length(); i++){
			if(Character.isDigit(value.charAt(i))){
				verdict = true;
				break;
			}
		}

		return verdict;
	}

	public int createVectorHashtable(Vector v){

		this.vDataHash = new Hashtable(v.size() * 2);

		for(int i=0;i<v.size(); i++){
			FormField f = (FormField)v.elementAt(i);
			String vData = f.getFieldData();
			if(isNull(vData))
				vData = "";
			String hashkey = "VKEY" + f.getFrmSqncNmbr() + "S" + f.getFrmSctnSqncNmbr() + "O" + f.getFrmSctnOcc() + "F" + f.getFrmFldNmbr();
			vDataHash.put(hashkey, vData);
		 	//Log.write(Log.DEBUG_VERBOSE, "Created: Hash key = " + hashkey + " Data["+vData+"]");-- commented unnecessary log message - Antony 05/18/10
		}
		return(vDataHash.size());
	}

	public String getHashData(int fsn, int fssn, int occ, String fld){

		if( vDataHash == null){
			Log.write("getHashData: HASHTABLE = NULL!");
			return(null);
		}
		String hashkey = "VKEY" + fsn + "S" + fssn + "O" + occ + "F" + fld;
                
                //Log.write(Log.DEBUG_VERBOSE, "getHashData: fsn="+fsn+"  fssn="+fssn+"  occ="+occ+"  fld="+fld);
                //Log.write(Log.DEBUG_VERBOSE, "getHashData: LOOKUP: Hash key = " + hashkey);
                //Commenting out unnecessary logs - Antony 05/18/10
                
		return((String)vDataHash.get(hashkey));
	}

	public boolean ifAandBandC(int occ, String value, ValidationComponent vc)
	{
		boolean thisCond = false;
		int theocc = -1;
		String k3;

		String fld1 = vc.getFld1();
		String fld1Val = vc.getFld1Val();
		String fld2 = vc.getFld2();
		String fld2Val = vc.getFld2Val();
		String fld3 = vc.getFld3();
		String fld3Val = vc.getFld3Val();
		String fld5 = vc.getFld5();
		String fld5Val = vc.getFld5Val();
		int fld5Pos = vc.getFld5Pos();
		String condtrue= vc.getCondTrue();
		String condfalse= vc.getCondFalse();

		if(isNull(fld1) || isNull(fld1Val) || isNull(fld2) ||
		   isNull(fld2Val) || isNull(condtrue) || isNull(condfalse) ){
			boolean cond = false;
			Log.write(Log.DEBUG_VERBOSE, "BOOLEAN_NULL_ARG_ALERT:  " + fld1 + "," + fld2 + "," + condtrue + "," + condfalse + "," + fld1Val + "," + fld2Val);
			return true;
		}
		String strMsgDesc="";

		//get field1 key
		int c1 = fld1.indexOf(',',1);
		int c2 = fld1.indexOf(',',c1 + 1);
		int c3 = fld1.indexOf(',',c2 + 1);

		String k1 = fld1.substring(0,c1);
		String k2 = fld1.substring(c1+1, c2);
		if(c3 == -1){
			k3 = fld1.substring(c2+1);
			theocc = occ;
		}else{
			String newocc = fld1.substring(c2+1, c3);
			int intocc = Integer.parseInt(newocc);
			theocc = intocc;
			k3 = fld1.substring(c3+1);
		}

		int fs = Integer.parseInt(k1);
		int fss = Integer.parseInt(k2);

		String hashData1 = getHashData(fs, fss, theocc, k3);

		//get field2 key
		c1 = fld2.indexOf(',',1);
		c2 = fld2.indexOf(',',c1 + 1);
		c3 = fld2.indexOf(',',c2 + 1);

		k1 = fld2.substring(0,c1);
		k2 = fld2.substring(c1+1, c2);
		if(c3 == -1){
			k3 = fld2.substring(c2+1);
			theocc = occ;
		}else{
			String newocc2 = fld2.substring(c2+1, c3);
			int intocc2 = Integer.parseInt(newocc2);
			theocc = intocc2;
			k3 = fld2.substring(c3+1);
		}

		fs = Integer.parseInt(k1);
		fss = Integer.parseInt(k2);

		String hashData2 = getHashData(fs, fss, theocc, k3);

		//get field3 key
		String hashData3 = null;

		if (!isNull(fld3) && !isNull(fld3Val))
		{
			c1 = fld3.indexOf(',',1);
			c2 = fld3.indexOf(',',c1 + 1);
			c3 = fld3.indexOf(',',c2 + 1);

			k1 = fld3.substring(0,c1);
			k2 = fld3.substring(c1+1, c2);
			if(c3 == -1){
				k3 = fld3.substring(c2+1);
				theocc = occ;
			}else{
				String newocc = fld3.substring(c2+1, c3);
				int intocc = Integer.parseInt(newocc);
				theocc = intocc;
				k3 = fld3.substring(c3+1);
			}

			fs = Integer.parseInt(k1);
			fss = Integer.parseInt(k2);

			hashData3 = getHashData(fs, fss, theocc, k3);
		}

		if(isNull(hashData1)){
			thisCond = false;
		}else{
			hashData1 = hashData1.trim();
			if(hasLength(hashData1)){
				int subIdx = fld1Val.indexOf(hashData1);
				if(subIdx >= 0)
					thisCond = true;
				else
					thisCond = false;
			}
			else
				thisCond = false;
		}

		if (thisCond)
		{
			if(isNull(hashData2)){
				thisCond = false;
			}else{
				hashData2 = hashData2.trim();
				if(hasLength(hashData2)){
					int subIdx = fld2Val.indexOf(hashData2);
					if(subIdx >= 0)
						thisCond = true;
					else
						thisCond = false;
				}
				else
					thisCond = false;
			}
		}

		if (thisCond)
		{
			if(isNull(hashData3)){
				thisCond = false;
			}else{
				hashData3 = hashData3.trim();
				if(hasLength(hashData3)){
					int subIdx = fld3Val.indexOf(hashData3);
					if(subIdx >= 0)
						thisCond = true;
					else
						thisCond = false;
				}
				else
					thisCond = false;
			}
		}

		boolean fnlVrdct =  finishedVerdict(thisCond, value, condtrue, condfalse);
		if (!fnlVrdct) {
			if (haveVerdictMessage())
			{
				setDetailMessage( getVerdictMessage() + getDetailMessage() + strMsgDesc );
			}
		}

		return fnlVrdct;
	}


}

