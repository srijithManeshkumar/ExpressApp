/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		Toolkit
 * 
 * DESCRIPTION: A toolbox of useful utility methods used to calculate SLAs.
 * 
 * AUTHOR:      pjs
 * 
 * DATE:        01-10-2002
 * 
 * HISTORY:
 *	01/30/2002  psedlak Modified SLA calculation to use partial days.
 *		    Edris added methods getSLAStatuses()
 *	6/7/2005    psedlak changed to accept db connectino, so it can be used from batch reporting. The Holidays
 *			singleton was not working while runnign in batch (HD 1437767)
 *
 */

package com.alltel.lsr.common.objects;
import java.util.*;
import java.io.*;
import java.text.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

/**
 * This class contains methods used to calculate SLA durations.
 */ 
public class SLATools {
    
	 private final static String BUSINESS_START_TIME = "070000";
	 private final static int iBUSINESS_START_HOUR = Integer.parseInt(BUSINESS_START_TIME.substring(0,2));
	 private final static int iBUSINESS_START_MIN  = Integer.parseInt(BUSINESS_START_TIME.substring(2,4));
	 private final static int iBUSINESS_START_SEC  = Integer.parseInt(BUSINESS_START_TIME.substring(4,6));

	 private final static String BUSINESS_END_TIME   = "150000";
	 private final static int iBUSINESS_END_HOUR = Integer.parseInt(BUSINESS_END_TIME.substring(0,2));
	 private final static int iBUSINESS_END_MIN  = Integer.parseInt(BUSINESS_END_TIME.substring(2,4));
	 private final static int iBUSINESS_END_SEC  = Integer.parseInt(BUSINESS_END_TIME.substring(4,6));

	 private final static double SAME_DAY   = .33;

	 private final static long DAY_IN_SEC  = 86400;     //seconds in a day
    
	public static String getSLAStartDateTime(String strSubmittedYYYYMMDD, String strSubmittedHH24MMSS) 
	{
		return getSLAStartDateTime( strSubmittedYYYYMMDD, strSubmittedHH24MMSS, null);
	}

	/**
	 * Returns the starting SLA clock date and time. The input arguments represent the date/time
	 * the order was submitted. 
	 *
	 * @return  String representing the date/time the SLA clock began. In the
	 *          format of "YYYYMMDD HHMMSS".
	 */
	public static String getSLAStartDateTime(String strSubmittedYYYYMMDD, String strSubmittedHH24MMSS, Connection dbConn) 
	{
		boolean bHoliday = true;
		boolean bWeekend = true;
		int	iDOW = 0;
		String strSLAStart = strSubmittedYYYYMMDD + " " + strSubmittedHH24MMSS;
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLAStart1 = " + strSLAStart);
		
        Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(strSubmittedYYYYMMDD.substring(0,4)),
			Integer.parseInt(strSubmittedYYYYMMDD.substring(4,6)) - 1,
			Integer.parseInt(strSubmittedYYYYMMDD.substring(6,8)),
			Integer.parseInt(strSubmittedHH24MMSS.substring(0,2)),
			Integer.parseInt(strSubmittedHH24MMSS.substring(2,4)),
			Integer.parseInt(strSubmittedHH24MMSS.substring(4,6)) );

		//If time before start of business day, bump to start of current business day
		if ( strSubmittedHH24MMSS.compareTo(BUSINESS_START_TIME) < 0 )
		{	//Log.write(Log.DEBUG_VERBOSE, "SLATools: Before SOB, bump time");
			cal.set(Calendar.HOUR_OF_DAY, iBUSINESS_START_HOUR);
			cal.set(Calendar.MINUTE, iBUSINESS_START_MIN);
			cal.set(Calendar.SECOND, iBUSINESS_START_SEC);
		}
		//If after end of business day, bump to start of next business day
		if ( strSubmittedHH24MMSS.compareTo(BUSINESS_END_TIME) > 0 )
		{
			//Log.write(Log.DEBUG_VERBOSE, "SLATools: After EOB, bump one day");
			cal.set(Calendar.HOUR_OF_DAY, iBUSINESS_START_HOUR);
			cal.set(Calendar.MINUTE, iBUSINESS_START_MIN);
			cal.set(Calendar.SECOND, iBUSINESS_START_SEC);
			cal.add(Calendar.DATE, 1);
		}

		Holidays h = null;
		if (dbConn == null) 	//running on j2ee server that has db conn pool
		{	h = Holidays.getInstance();
		}
		else
		{	h = Holidays.getInstance(dbConn);	//conn was passed in -probably being called by batch
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		while (bHoliday || bWeekend)
		{
			//If Weekend (Sat, Sun), bump to monday at start of business day
			iDOW = cal.get(Calendar.DAY_OF_WEEK);
			//Log.write(Log.DEBUG_VERBOSE, "SLATools: Cal DOW:" + iDOW);
			if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
			{
				//Log.write(Log.DEBUG_VERBOSE, "SLATools: Weekend - bump a day");
				bWeekend = true;
				cal.set(Calendar.HOUR_OF_DAY, iBUSINESS_START_HOUR);
				cal.set(Calendar.MINUTE, iBUSINESS_START_MIN);
				cal.set(Calendar.SECOND, iBUSINESS_START_SEC);
				cal.add(Calendar.DATE, 1);	//bump a day
				continue;
			}
			else
				bWeekend = false;

			//If Holiday, bump a day
			if (h.isHoliday( formatter.format( cal.getTime() ) ) ) 
			{	bHoliday = true;
				//Log.write(Log.DEBUG_VERBOSE, "SLATools: Holiday - bump a day");
				cal.set(Calendar.HOUR_OF_DAY, iBUSINESS_START_HOUR);
				cal.set(Calendar.MINUTE, iBUSINESS_START_MIN);
				cal.set(Calendar.SECOND, iBUSINESS_START_SEC);
				cal.add(Calendar.DATE, 1);	//bump a day
				continue;
			}
			bHoliday = false;
		}
		
		DateFormat formatter2 = new SimpleDateFormat("yyyyMMdd HHmmss");
		strSLAStart = formatter2.format( cal.getTime() );
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA Clock starts= " + strSLAStart);

		return strSLAStart;
	}

	
	public static String getSLAExpectedEndDateTime( String strSLAStart, int nSLADays ) 
	{
		return getSLAExpectedEndDateTime( strSLAStart , nSLADays, null);
	}
	/**
	 * EK: 5/24/2005
	 * Generates the expected sla date given the  sla start date and number
	 * expected sla time ( in days ). 
	 *
	 * @return  String representing the date/time the SLA date expected.
	 *          format of "YYYYMMDD HHMMSS".
	 * @param   strSLAStart YYYYMMDD HHMMSS, the returned by (getSLAStartDateTime) 
	 * @param   nSLADays  Number of days system expects to complete an order..
	 */
	public static String getSLAExpectedEndDateTime( String strSLAStart, int nSLADays, Connection dbConn ) 
	{
		boolean bHoliday = true;
		boolean bWeekend = true;
		int	iDOW = 0;
		int lTotalDays = nSLADays;
		String strSLADueDate = strSLAStart;
		Calendar cal = Calendar.getInstance();
		int nSlaYear = 1900; 
		int nSlaMonth  = 01;
		int nSlaDay  = 01;
		int nSlaHour = 07;
		int nSlaMinute = 0; 
		int nSlaSeconds = 0;
		try{
			 nSlaYear = Integer.parseInt( strSLAStart.substring( 0, 4 ) );
			 nSlaMonth =  Integer.parseInt( strSLAStart.substring( 4, 6 ) ) - 1;
			 nSlaDay  = Integer.parseInt( strSLAStart.substring( 6, 8 ) );
			 nSlaHour = Integer.parseInt( strSLAStart.substring( 9, 11 ) );
			 nSlaMinute =Integer.parseInt( strSLAStart.substring( 11, 13 ) );
			 nSlaSeconds = 	Integer.parseInt( strSLAStart.substring( 13,15 ) ) ;
		
		}catch(Exception e)
		{       
			e.printStackTrace();	
			Log.write( Log.DEBUG_VERBOSE,"getSLAExpectedEndDateTime:\n" + e.toString() );
		}
		
		cal.set( nSlaYear, nSlaMonth, nSlaDay,nSlaHour,nSlaMinute,  nSlaSeconds );
		
		Holidays h = null;
		if (dbConn == null) 	//running on j2ee server that has db conn pool
		{	h = Holidays.getInstance();
		}
		else
		{	h = Holidays.getInstance(dbConn);	//conn was passed in -probably being called by batch
		}

		DateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
		int lSLATemp = 0;
		while ( lTotalDays > lSLATemp )
		{	
			
			if ( ( cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
				cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY )
				|| h.isHoliday( formatter.format( cal.getTime() ) ) )
			{
				cal.set( Calendar.HOUR_OF_DAY, nSlaHour );
				cal.set( Calendar.MINUTE, nSlaMinute );
				cal.set( Calendar.SECOND, nSlaSeconds );
				cal.add( Calendar.DATE, 1 );
			}else{
				cal.set( Calendar.HOUR_OF_DAY, nSlaHour );
				cal.set( Calendar.MINUTE, nSlaMinute );
				cal.set( Calendar.SECOND, nSlaSeconds );
				cal.add( Calendar.DATE, 1 );
				lSLATemp++;
			}
		}	
		
		// lastly,make sure it is not a weekend date or holiday
		
		while( true ){
			if ( ( cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
				cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY )
				|| h.isHoliday( formatter.format( cal.getTime() ) ) )
			{
				cal.set( Calendar.HOUR_OF_DAY, nSlaHour );
				cal.set( Calendar.MINUTE, nSlaMinute );
				cal.set( Calendar.SECOND, nSlaSeconds );
				cal.add( Calendar.DATE, 1 );
			}else{
				break;
			}
		}
			
		DateFormat formatter2 = new SimpleDateFormat("yyyyMMdd HHmmss");
		strSLADueDate = formatter2.format( cal.getTime() );
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA Clock start
		return strSLADueDate;
    }


	/**
	 * Returns the revised ENDING SLA clock date and time. The input arguments should represent the 
	 *  date/time the order was completed (FOC or REJECTED). 
	 *
	 * @return  String representing the date/time the SLA clock stops. In the
	 *          format of "YYYYMMDD HHMMSS".
	 * @param   strCompletedYYYYMMDD   Date order was completed (YYYYMMDD)
	 * @param   strCompletedHH24MMSS   Time order was completed (HHMMSS).
	 */
    public static String getSLAEndDateTime(String strCompletedYYYYMMDD, String strCompletedHH24MMSS) 
	{
		String strSLAEnd = strCompletedYYYYMMDD + " " + strCompletedHH24MMSS;
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA Clock ends= " + strSLAEnd);
		return strSLAEnd;
    }
    
	public static long calculateSLA(String strStartYYYYMMDD_HHMMSS, String strEndYYYYMMDD_HHMMSS)
	{
		return calculateSLA(strStartYYYYMMDD_HHMMSS, strEndYYYYMMDD_HHMMSS, null);
	}
	/**
	 * Returns the number of seconds it took to complete request. The input arguments represent 
	 *  the already-adjusted SLA start and end date/times. The input arguments are in the format 
	 *  of "YYYYMMDD HHMMSS".
	 *
	 * @return  Nbr of seconds it took to respond to request.
	 * @param   strStartYYYYMMDD_HHMMSS     SLA start date/time in format of "YYYYMMDD HHMMSS".
	 * @param   strEndYYYYMMDD_HHMMSS       SLA END date/time in format of "YYYYMMDD HHMMSS".
	 * @param   dbConn       		DB connection
	 */
	public static long calculateSLA(String strStartYYYYMMDD_HHMMSS, String strEndYYYYMMDD_HHMMSS, Connection dbConn)
	{
		long 	lSLA = 0;
		int	    iDOW = 0;

		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA Range: " + strStartYYYYMMDD_HHMMSS + " -> " +
		//	strEndYYYYMMDD_HHMMSS);
		// If SLA clock starts after End time, then SLA = 0 days.
		// If SLA clock starts at End time, then SLA = 0 days.
		if ( (strStartYYYYMMDD_HHMMSS.compareTo(strEndYYYYMMDD_HHMMSS) > 0) ||
		     (strStartYYYYMMDD_HHMMSS.equals(strEndYYYYMMDD_HHMMSS)) )
		{
			//Log.write(Log.DEBUG_VERBOSE, "SLATools: Start >= End - so SLA = 0");
			lSLA = 0;
			return lSLA;
		}

		// Must convert to calendar objects to perform rest...
        	Calendar calStart = Calendar.getInstance();
        	Calendar calEnd = Calendar.getInstance();
		calStart.set(Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(0,4)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(4,6)) - 1,
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(6,8)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(9,11)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(11,13)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(13,15)) );

		calEnd.set(Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(0,4)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(4,6)) - 1,
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(6,8)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(9,11)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(11,13)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(13,15)) );

		Holidays h = null;
		if (dbConn == null) 	//running on j2ee server that has db conn pool
		{	h = Holidays.getInstance();
		}
		else
		{	h = Holidays.getInstance(dbConn);	//conn was passed in -probably being called by batch
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

		//To reach here, End date had to be after start date.
		//Here we keep bumping and counting until start>=end date/time.
		if ( calStart.before(calEnd) )
		{
			//If same day, get partial day and get out
			if (( calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) ) &&
			    ( calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR) ) )
	           	{
        			long lFROM = (long) ( (calStart.getTime().getTime()) * 0.001 );
		        	long lTO =  (long) ( (calEnd.getTime().getTime()) * 0.001 );
		        	lSLA += (lTO - lFROM);
        			//Log.write(Log.DEBUG_VERBOSE, "SLATools: lFROM=" + lFROM + " lTO=" + lTO + " Seconds:=" + lSLA);
			}
			else
			{
				while ( calStart.before(calEnd) )
				{	
					calStart.add(Calendar.DATE, 1);
					// Is end date greater now - will happen if less than 24 hours
					if ( calEnd.before(calStart) )
					{	
						calStart.add(Calendar.DATE, -1);//backup, get partial day and get out
						long lFROM = (long) ( (calStart.getTime().getTime()) * 0.001 );
						long lTO =  (long) ( (calEnd.getTime().getTime()) * 0.001 );
						lSLA += (lTO - lFROM);
						//Log.write(Log.DEBUG_VERBOSE, "SLATools: lFROM2=" + lFROM + " lTO2=" + lTO + " Seconds:=" + lSLA);
						break;
					}

					//If same day, get partial day and get out
					if (( calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) ) &&
					    ( calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR) ) )
					{
						lSLA += DAY_IN_SEC;	//add day
						long lFROM = (long) ( (calStart.getTime().getTime()) * 0.001 );
						long lTO =  (long) ( (calEnd.getTime().getTime()) * 0.001 );
						lSLA += (lTO - lFROM);	//now add some hours, minutes, seconds
						//Log.write(Log.DEBUG_VERBOSE, "SLATools: lFROM=" + lFROM + " lTO=" + lTO + " Seconds:=" + lSLA);
						break;        
					}

					iDOW = calStart.get(Calendar.DAY_OF_WEEK);
					if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
					{	
						continue;	//skip weekends -we don't count them
					}
					//If curr day isn't a Holiday, add a day to count
					if (!h.isHoliday(formatter.format(calStart.getTime()))) 
					{
						//add a day  -its not weekend or holiday
						lSLA += DAY_IN_SEC;
					}
				} //while()
			}
		}
		else
		{
			lSLA = -1;	//shouldnt happen, but a precaution
		}
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA in seconds = " + lSLA);
		
		return lSLA;
	}
	
	/*SLA over 1 day over due. This function is used to for for flagging (requestlist).
	 *
	 */
	public static boolean isSLAOverDue( String strStartYYYYMMDD_HHMMSS, String strEndYYYYMMDD_HHMMSS )
	{
		boolean lSLA = false;
		// Must convert to calendar objects to perform rest...
        	Calendar calStart = Calendar.getInstance();
        	Calendar calEnd = Calendar.getInstance();
		calStart.set(Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(0,4)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(4,6)) - 1,
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(6,8)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(9,11)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(11,13)),
			     Integer.parseInt(strStartYYYYMMDD_HHMMSS.substring(13,15)) );

		calEnd.set(Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(0,4)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(4,6)) - 1,
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(6,8)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(9,11)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(11,13)),
			   Integer.parseInt(strEndYYYYMMDD_HHMMSS.substring(13,15)) );
		if (  calEnd.before(calStart) ){
			lSLA = true;			
			
		}
		return lSLA;
	}
	
	public static double calculateSLADays(String strStartYYYYMMDD, String strEndYYYYMMDD)
	{
		return calculateSLADays(strStartYYYYMMDD, strEndYYYYMMDD, null);
	}
	/**
	 * Returns the number of days to complete request. The input arguments represent 
	 *  the modified start SLA date/time and the ending SLA date/time (when it was FOC-ed
	 *  or REJECT-ed). The input arguments are in the format of "YYYYMMDD".
	 *
	 * @return  SLA Days it took to respond to request.
	 * @param   strStartYYYYMMDD     Modified (by above routine) SLA start date/time in
	 *                                      format of "YYYYMMDD HHMMSS".
	 * @param   strEndYYYYMMDD       Ending SLA date/time in format of "YYYYMMDD".
	 * @param   dbConn		 Db connectino
	 */
	public static double calculateSLADays(String strStartYYYYMMDD, String strEndYYYYMMDD, Connection dbConn)
	{
		double 	fSLA = 0;
		int	iDOW = 0;

		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA Range: " + strStartYYYYMMDD+ " -> " +
		//	strEndYYYYMMDD);

		// If Start and End are same day, then SLA = .33 days.
		if ( (strStartYYYYMMDD.equals(strEndYYYYMMDD)) )
		{
			//Log.write("SAME DAY");
			return SAME_DAY;
		}
		
		Holidays h = null;
		if (dbConn == null) 	//running on j2ee server that has db conn pool
		{	h = Holidays.getInstance();
		}
		else
		{	h = Holidays.getInstance(dbConn);	//conn was passed in -probably being called by batch
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

		// Must convert to calendar objects to perform rest...
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.set(Integer.parseInt(strStartYYYYMMDD.substring(0,4)),
			     Integer.parseInt(strStartYYYYMMDD.substring(4,6)) - 1,
			     Integer.parseInt(strStartYYYYMMDD.substring(6,8)) );

		calEnd.set(Integer.parseInt(strEndYYYYMMDD.substring(0,4)),
			   Integer.parseInt(strEndYYYYMMDD.substring(4,6)) - 1,
			   Integer.parseInt(strEndYYYYMMDD.substring(6,8)) );

		if ( calStart.before(calEnd) )
		{
			//Log.write("Start is BEFORE End");
			while ( calStart.before(calEnd) )
			{	
				calStart.add(Calendar.DATE, 1);

				//If same day, get partial day and get out
				if (( calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) ) &&
				    ( calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR) ) )
				{
					//Log.write("Adding a Day");
					fSLA++;	
					break;        
				}

				iDOW = calStart.get(Calendar.DAY_OF_WEEK);
				if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
				{	
					continue;	//skip weekends -we don't count them
				}

				//If curr day isn't a Holiday, add a day to count
				if (!h.isHoliday(formatter.format(calStart.getTime()))) 
				{
					//add a day  -its not weekend or holiday
					//Log.write("Adding a Day");
					fSLA++;
				}
			}
		}
		else if ( calStart.after(calEnd) )
		{
			//Log.write("Start is AFTER End");
			while ( calStart.after(calEnd) )
			{	
				calEnd.add(Calendar.DATE, 1);

				//If same day, get partial day and get out
				if ( calStart.equals(calEnd) )
				{
					//Log.write("Subtracting a Day");
					fSLA--;	
					break;        
				}

				iDOW = calEnd.get(Calendar.DAY_OF_WEEK);
				if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
				{	
					continue;	//skip weekends -we don't count them
				}

				//If curr day isn't a Holiday, add a day to count
				if (!h.isHoliday(formatter.format(calEnd.getTime()))) 
				{
					//subtract a day  -its not weekend or holiday
					//Log.write("Subtracting a Day");
					fSLA--;
				}
			}
		}
		else
		{
			//Log.write("Do not know what Start and End are...");
			fSLA = 0;	//shouldnt happen, but a precaution
		}
		
		//Log.write(Log.DEBUG_VERBOSE, "SLATools: SLA days = " + fSLA);
		
		return fSLA;
	}

	public static Vector getMultiFocRej(String strRqstSqncNmbr, String strStts)
	{
		Vector vFocRej = new Vector();
		Connection con = null;
		try {
			con = DatabaseManager.getConnection();
			vFocRej = getMultiFocRej(strRqstSqncNmbr , strStts, con);
		}
		catch(Exception e)
		{       
			Log.write(Log.DEBUG_VERBOSE,"getMultiFocRej() has FAILED....check db logs");
		}
		finally
		{       DatabaseManager.releaseConnection(con);
		}
		return vFocRej;
	}

	/**
	 * Given a RQST_SQNC_NMBR, this method will return the number of orders FOC'd
	 *    and the number rejected.
	 *
	 * @return  0 - success  1 - failure
	 * @param   strRqstSqncNmbr     Request Sequence Number
	 * @param   strStts       	Status of Request
	 * @param   iNumFoc       	Number of orders Foc'd
	 * @param   iNumRej       	Number of orders Rejected
	*/
	public static Vector getMultiFocRej(String strRqstSqncNmbr, String strStts, Connection con)
	{
		Vector vFocRej = new Vector();

		// Verify valid RqstSqncNmbr
		if ((strRqstSqncNmbr == null) || (strRqstSqncNmbr.length() == 0))
		{
			return vFocRej;
		}

		// Get stats
		Statement stmt = null;
		ResultSet rs = null;

		String strTmp = "";

		int iNumFoc = 0;
		int iNumRej = 0;

                try 
		{
			String strQuery = "SELECT LR_MULTI_DETAIL_REJ FROM LR_MULTI_DETAIL_T WHERE RQST_SQNC_NMBR = " + strRqstSqncNmbr + " AND LR_MULTI_DETAIL_PON IS NOT NULL";

			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery);

			while (rs.next())
			{
				if (strStts.equals("REJECTED"))
				{
					iNumRej++;
				}
				else
				{
					strTmp = rs.getString("LR_MULTI_DETAIL_REJ");
					if (strTmp != null && strTmp.equals("Y"))
						iNumRej++;
					else
						iNumFoc++;
				}
			}
		}
		catch(Exception e)
		{       
			//Log.write("getMultiFocRej() has FAILED....check db logs");
		}
		finally
		{       try {	rs.close();
				rs = null;
				stmt.close();
				stmt=null;
			} catch(Exception eee) {}
		}

		Integer iNF = new Integer(iNumFoc);
		Integer iNR = new Integer(iNumRej);

		vFocRej.addElement(iNF);
		vFocRej.addElement(iNR);

		return vFocRej;
	}
	
	
	/*
	 * EK 5/25/2005
	 * This function  returns an array of status that resets the SLA back to specified days.
	 * &note: Connection and exceptions handled by caller.
	 */
	 
	public static Vector getSLAStatuses( Connection conn, String strSTTStype   )  
		throws SQLException, Exception 
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;	
		Vector vSlaStatus = new Vector( 5 );
		String strQuery = " select STTS_CD FROM STATUS_T where  TYP_IND = ?  AND SLA_RESET = ? ";
		String strRsts = "";
		pstmt = conn.prepareStatement( strQuery );
		pstmt.clearParameters();
		pstmt.setString( 1, strSTTStype  );
		pstmt.setString( 2, "Y" );
		rs = pstmt.executeQuery();	 
		while( rs.next() )		
		{
				vSlaStatus.add( rs.getString( 1 ) );
		}
		Log.write(Log.DEBUG_VERBOSE, "getSLAStatuses:\n" + strQuery  + "\n"  );
		rs.close(); rs= null;
		pstmt.close(); pstmt= null;
		return vSlaStatus;	
	}
	
	/*
	 * EK 6/1/2005
	 * This function  returns an array of status that resets or displays the SLA.
	 * &note: Connection and exceptions handled by caller.
	 */
	 
	public static Vector getSLADispayStatuses( Connection conn, String strSTTStype   )  
		throws SQLException, Exception 
	{

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String strSLAUpdateType = "Y";
		String strSLADisplayType = "D";
		
		Vector vSlaStatus = new Vector( 5 );
		String strQuery = " select STTS_CD FROM STATUS_T where  TYP_IND = ?  AND SLA_RESET = ?  OR SLA_RESET = ? ";
		String strRsts = "";
		pstmt = conn.prepareStatement( strQuery );
		pstmt.clearParameters();
		pstmt.setString( 1, strSTTStype  );
		pstmt.setString( 2, strSLAUpdateType );
		pstmt.setString( 3, strSLADisplayType );
		rs = pstmt.executeQuery();	 
		while( rs.next() )		
		{
				vSlaStatus.add( rs.getString( 1 ) );
		}
		Log.write(Log.DEBUG_VERBOSE, "getSLADispayStatuses:\n" + strQuery  + "\n"  );
		rs.close(); rs= null;
		pstmt.close(); pstmt= null;
		return vSlaStatus;	
	}
	
	
}

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/SLATools.java  $
/*
/*   Rev 1.2   22 May 2002 06:57:16   dmartz
/* 
/*
/*   Rev 1.1   30 Jan 2002 14:49:16   sedlak
/*rel 1.0 base
/*
/*   Rev 1.0   23 Jan 2002 11:06:34   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
 

