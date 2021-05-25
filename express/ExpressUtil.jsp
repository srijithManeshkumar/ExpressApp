<%!
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DslReport.jsp	
 * 
 * DESCRIPTION: Utility functions that don't depend on the database. 
 * 				Use this file to add function specifically Express and are used useable in more than one files/ script or class.
 *				 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        02-02-20005
 * 
 * HISTORY:
 * EK: File started on 02-02-20005.
 */

%>



<%@ page import ="java.io.*" %>
<%@ page import ="java.text.*" %>
<%@ page import ="java.util.Date"%>
<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%!	
	public  String printDateSelect( String strName,  boolean bFieldFlag, boolean bFlag, String strStartMth, String strStartYr  ){
 	 	
 	 	String strEndStart = bFlag ? "_end" : "";
 	 	String strDatesel = "<SELECT name=\"" + strName + "_mnth" + strEndStart + "\">\n";
		String y;
		Calendar cal = Calendar.getInstance();
		
		// set the default range for to six month back for starting dates
		// and set it to 3 months back for ending dates.
		
		if(!bFlag && !bFieldFlag )
		{
			cal.add(Calendar.MONTH, -3);
		}else if( !bFlag && bFieldFlag ){
			cal.add(Calendar.MONTH, -3);
		}
		int iMth = cal.get(Calendar.MONTH)+1;
		int iYear = cal.get(Calendar.YEAR);		
		String strCurrentMonth = ( strStartMth == null ? "":strStartMth) ;			
		String strCurrentYear = (strStartYr==null ? "": strStartYr) ;
		boolean mSel = false;
		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			
			if(y.equals(strCurrentMonth) )
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
				mSel = true;
			}else if (x == iMth && !mSel) 
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
			} else {
				strDatesel += "<OPTION value=\"" + y + "\">" + y ;
			}
		}
		
		strDatesel += "</SELECT><SELECT name=\""+ strName + "_yr" + strEndStart + "\"> ";
		boolean ySel = false;
		for (int x = 2001; x <= iYear ; x++)
		{
			if( strCurrentYear.equals(""+x ))
			{
				ySel = true;
				strDatesel += "<OPTION SELECTED value=\"" + x + "\">" + x ;
			}else if (x==iYear && !ySel) 
			{
				strDatesel += "<OPTION SELECTED value=\"" + x + "\">" + x ;
			} else {
				strDatesel += "<OPTION value=\"" + x + "\">" + x ;

			}
		}
		strDatesel += "</SELECT> ";
		return strDatesel;
}	

	
public  String printDTSelect( String strNameMn, String strNameDy, String strNameYr  )
{
 	 	
  	StringBuffer sb = new StringBuffer(20);  
  	sb.append( "<SELECT name="+strNameMn + ">" );
	String y;
	Calendar cal = Calendar.getInstance();
	int iMth = cal.get(Calendar.MONTH)+1;
	int iDay = cal.get(Calendar.DAY_OF_MONTH);
	int iYear = cal.get(Calendar.YEAR);
	for (int x = 1; x < 13 ; x++)
	{	y = "" + x;
		if (y.length()==1) y="0"+x;
		if (x == iMth) {
			 sb.append( "<OPTION SELECTED value=\"" + y +"\">"+y);
		} else {
			sb.append( "<OPTION  value=\"" + y +"\">"+y);
		}
	}
	sb.append( "</SELECT>" );
	sb.append( "<SELECT name="+strNameDy + ">" );
	for (int x = 1; x < 32 ; x++)
	{	y = "" + x;
		if (y.length()==1) y="0"+x;
		if (x==iDay) {
			sb.append( "<OPTION SELECTED value=\"" + y +"\">"+y);

		} else {	
			sb.append( "<OPTION  value=\"" + y +"\">"+y);
		}
	}
	sb.append( "</SELECT>" );
	sb.append( "<SELECT name=" + strNameYr + ">" );
	for (int x = 2001; x <= iYear ; x++)
	{
		if (x==iYear) {
				sb.append( "<OPTION SELECTED value=\"" + x +"\">"+x);
		} else {
			sb.append( "<OPTION  value=\"" + x +"\">"+x);
		}
	}
	
	sb.append( "</SELECT>" );
	return sb.toString();
}
	/*@Description: This method walks through a string txt, 
					finds all occurrence of fnd and replaces them with rplc
					
	 String and replace it.
	 *@param	String txt String to search in
	 *@param	String fnd String to search for
	 *@param 	String rplc String to replace with.
	 */
	 	
	public  String findReplace(String txt, String fnd, String rplc)
	{
		StringBuffer nstr;
		int nsubs = 0;	
		int subs = 0;	
		int fndl = fnd.length();
		int txtl = txt.length();
		nstr = new StringBuffer(txtl);
		while(-1 != ( nsubs = txt.indexOf( fnd, subs ) ) )	
		{
			nstr.append( txt.substring( subs, nsubs ) );	
			nstr.append( rplc );	
			subs = nsubs + fndl;	
		}
		nstr.append(txt.substring(subs));
		return nstr.toString();
	}
	
	
	/*
	/*text box with int value
	/*
	/**********************************************/
	public  String printInputText(String name, int size, int maxlength, int value)
	{
		return printInputText(name,size,maxlength,String.valueOf (value));
	}
	
	public  String printInputText(String name, int size, int maxlength, String value)
	{
		return "<input type=\"text\" name=\"" + name + "\" size=\"" + size + "\" maxlength=\"" + maxlength + "\" value=\"" + value + "\">";
	}
	
	/* Prints out an html select box in servlet or jsp page.
	/*
	/*
	/**********************************************/
	public  String printSelectBox(String name, int size, String values[], String option_names[], String val_selected)
	{
		StringBuffer s = new StringBuffer(1024);
		s.append("<select name=\"" + name + "\" size=" + size + ">\n");
		for (int i = 0; i < values.length; ++i) {
			
			s.append("<option value=\"" + values[i] + "\"" + (values[i].equalsIgnoreCase(val_selected) ? " selected" : "") + ">" + option_names[i] + "</option>\n");
		}
		s.append("</select>\n");		
		return s.toString();
	}	
	
	
	/* EK. Multiple select with Array of selected values
	 * This function creates a multiple box with multiple 
	 * selected values.
	 */	
	public  String printMultipleSelectedValues( String name, int size, Object values[], Object option_names[], String val_selected[] )
	{
		StringBuffer s = new StringBuffer(1024);
		s.append("<select name=\"" + name + "\" size=" + size + " MULTIPLE>\n");			
		int nValLen = values.length;
		int nSelectLen =  val_selected == null ? 0 : val_selected.length;
		boolean bSelected;	
		String strTemVal = "";
		for ( int i = 0; i < nValLen; ++i ) {
			strTemVal = (String)values[i];
			bSelected = false;
			for( int j = 0; j < nSelectLen; ++j ){
				if( strTemVal.equals( val_selected[j] ) ){
				 	bSelected = true;
				}
			}	
			s.append("<option value=\"" + strTemVal + "\""+  (bSelected ? " selected ": "" ) + ">" + (String)option_names[i] +  "</option>\n");
		}
		s.append("</select>\n");
		
		return s.toString();
	}
	
	/** EK
    * Creates a Listbox containing states.
    *
    * @param  name           defines the name for the selected state
    * @param  val_selected   defines the value for the selected state
    * @return                a new state selection Listbox
    */
	public String printSelectBoxStates( String name, String[] val_selected, int iSize ) {
		
		
		Object values[] = { "AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		
		//options	
		Object option_names[] = {"Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
	
		return printMultipleSelectedValues(name,iSize, values, option_names, val_selected  );
	}
		
		
	public String printSelectBoxStates_ALLOp( String name, String[] val_selected, int iSize ) {
		
		
		Object values[] = {  "__","AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		
		//options	
		Object option_names[] = { "All States", "Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
	
		return printMultipleSelectedValues(name,iSize, values, option_names, val_selected  );
	}	
	public String printSelectBoxState( String name, String val_selected, int iSize ) {
			//NSS, no state Included.	
		String values[] = { "__","AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		
		//options	
		String option_names[] = {"All States", "Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
			return printSelectBox(name, 1, values, option_names, val_selected);
	}

	public String getStateFullNames( String[] state_cds ){
		String states = "";
		int sttcounter = 0;
		while( sttcounter < state_cds.length){
			states += getStateFullName(state_cds[sttcounter] );
			states += "&nbsp;&nbsp;";
			sttcounter++;
		}
		return states;	
	}

	public String getStateFullName( String state_cd ){
	
		String values[] = { "__","AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		int i = 0;
		for ( ; i < values.length; i++ ){
			if( state_cd.equalsIgnoreCase(values[i] ) )
			{
				break;
			}
		 }
		String option_names[] = {"All States", "Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};

		return option_names[i];
	}
		


/*
	/*hidden string input
	/*
	/**********************************************/
	public  String printInputHidden(String name, String value)
	{
		return "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\">";
	}
	
	/*
	/*Submit box with string value
	/*
	/**********************************************/
	public  String printSubmitButton(String name, String value)
	{
		return "<input type=\"Submit\" name=\"" + name + "\" value=\"" + value + "\"" + ">";
	}
	
	/*
	/*checkbox with string value
	/*
	/**********************************************/
	public  String printInputCheckbox(String name, String value, boolean checked)
	{
		return "<input type=\"checkbox\" name=\"" + name + "\" value=\"" + value + "\"" + (checked ? " checked" : "") + ">";
	}
	
	 // check if string exists in array.
 public  boolean isElementOf( String[] arr, String otarget){
		if( arr == null )
		{
			return false;
		}
		if(arr.length < 0  ){
			return false;
		}
		int i = 0 ;
		while( i < arr.length ){
			if( otarget.equalsIgnoreCase(arr[i]) ){
				return true;
			}
			i++;
		}
		return false;	
	
	 }
	 
	// Debug util, print content of array
 public  String printArray( String[] arr ){
		if( arr == null )
		{
			return "";
		}
		if(arr.length < 0  ){
			return "";
		}
		int i = 0 ;
		StringBuffer sb = new StringBuffer( 512 );
		while( i < arr.length ){
			sb.append( i + "&nbsp;&nbsp;" + arr[i] + "\n" ) ;
			i++;
		}
		return sb.toString();	
	
	 }
	 
	 public  String fixNullStr( String s ){
		if (s == null){
			return "";
		}
		return s;
	}
	
	 public String getCurrentDate_Simpe() {	
		GregorianCalendar cal = new GregorianCalendar();
		SimpleDateFormat formatter = new SimpleDateFormat( "MM-dd-yyyy");
		java.util.Date dTime =  cal.getTime();
		String dateString = formatter.format(dTime);
		return dateString;
	}
%>
