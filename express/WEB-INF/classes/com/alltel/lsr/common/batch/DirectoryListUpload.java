/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:	DirectoryListUpload.java
 *
 * DESCRIPTION: Batch job which will read a DECRIS input file and output a file prepared
 *			for sqlldr.
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-01-2002
 *
 * HISTORY:
 *	09-19-2002 psedlak	Direcotry record format change and the addition of a hdr/trailer to file.
 *	01-20-2003 psedlak	Remove obsolete column
 *  02-19-2003 mranzan  Modified to not load "O" records
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import com.alltel.lsr.common.util.*;

public class DirectoryListUpload
{
	final static String HEADER_LITERAL  = "00000000000";
	final static String TRAILER_LITERAL = "99999999998";
	final static int MIN_CONTROL_RECORD_LENGTH = 23;
	final static int MIN_DATA_RECORD_LENGTH = 46;

	final static int TN_POS = 0;
	final static int TN_LEN = 10;
	final static int REC_TYP_POS = 44;
	final static int REC_TYP_LEN = 3;
	final static int SEQ_NBR_POS = 47;
	final static int SEQ_NBR_LEN = 5;
	final static int DTL_POS = 52;
	final static int DTL_LEN = 60;
	final static int S13_CNT_POS = 13;
	final static int S13_CNT_LEN = 7;
	final static int REC_CNT_POS = 21;
	final static int REC_CNT_LEN = 9;

//added for Internal Enhancement:
	final static int ORD_TYP_POS = 26;
	final static int ORD_TYP_LEN = 1;


	public static void main(String[] args)
	{
		int iDetailRecordCount = 0;
		int iTrailerCount = 0;
		int iBypassRecordCount = 0;
		int iTotalCount = 0;
		int iS13Count = 0;

		int iSrtSqnc = 1;
		String strRow = "";
		String strRcrdCd = "";
		String strSqncNmbr = "";
		String strBillTn = "";
		String strServiceTn = "";
		String strDetail = "";

//added for Internal Enhancement:
		String strOrdType = "";

		String strPrevBillTn = "";
		String strPrevServiceTn = "";

		String strCurrDate = getCurrDate();

		// Get the current date
		if (args.length != 2)
		{
			System.out.println("Usage:  java DirectoryListUpload <inputfile> <outputfile>");
			System.exit(1);
			return;
		}

		try
		{
			// Open files for processing
			ExpressInputFile in = new ExpressInputFile(args[0]);
			ExpressOutputFile out = new ExpressOutputFile(args[1]);

			// Read thru the input file, line by line
			while ((strRow = in.getLine()) != null)
			{
				// Scrap short records ...we're assuming its junk
				if (strRow.length() < MIN_DATA_RECORD_LENGTH)
				{
					if (strRow.length() < MIN_CONTROL_RECORD_LENGTH)
					{
						System.out.println("Record of length " + strRow.length() + " was bypassed ");
						System.out.println("["+strRow+"]");
						continue;
					}
					else
					{
						System.out.println("Header or trailer encountered");
						System.out.println("["+strRow+"]");
						if ( strRow.substring(0, 0+TRAILER_LITERAL.length()).equals(TRAILER_LITERAL) )
						{
							iS13Count=0;
							String strTemp =  strRow.substring(S13_CNT_POS, S13_CNT_POS + S13_CNT_LEN);
							iS13Count = Integer.parseInt(strTemp);
							iTrailerCount=0;
							strTemp =  strRow.substring(REC_CNT_POS, REC_CNT_POS + REC_CNT_LEN);
							iTrailerCount = Integer.parseInt(strTemp);
							//System.out.println("strTemp [" +strTemp+ "]");

							System.out.println("Trailer details: S13[" + iS13Count +
									   "]  Detail[" + iTrailerCount + "]");
							System.out.println("Program details: Records Uploaded[" + iDetailRecordCount + "]  Records Bypassed [" +iBypassRecordCount+ "]");

							//System.out.println("this is iBypassRecordCount [" +iBypassRecordCount+ "]");
							iTotalCount = iDetailRecordCount + iBypassRecordCount;
							System.out.println("Program Details Total: [" +iTotalCount+ "]");

							if (iTrailerCount != iTotalCount)
							{
							System.out.println("***\n\nAudit Counts DO NOT MATCH - aborting !\n\n***\n\n");
							System.exit(2);
							return;
							}
						}
						continue;
					}
				}
//added for Internal Enhancement:


				strOrdType = strRow.substring(ORD_TYP_POS, ORD_TYP_POS + ORD_TYP_LEN);

				if (strOrdType.equals("O"))
				{
					iBypassRecordCount++;
					continue;
				}



				// Get the record code
				strRcrdCd = strRow.substring(REC_TYP_POS, REC_TYP_POS + REC_TYP_LEN);

				// Weed out the ones we don't want
				if (!strRcrdCd.equals("S13") && !strRcrdCd.equals("S14"))
					continue;


				iDetailRecordCount++;

				// Parse out the individual pieces
				strBillTn = strRow.substring(TN_POS, TN_POS + TN_LEN);
	//obsolete		strServiceTn = strRow.substring(20,30);
				strServiceTn = strBillTn;
				strSqncNmbr = strRow.substring(SEQ_NBR_POS, SEQ_NBR_POS + SEQ_NBR_LEN);
				strDetail = strRow.substring(DTL_POS);
				strDetail = strDetail.replace('"',' ');

				// If we moved on, then restart the sequence
				if (!strBillTn.equals(strPrevBillTn) ||
				    !strServiceTn.equals(strPrevServiceTn))
				{
					iSrtSqnc = 1;
					strPrevBillTn = strBillTn;
					strPrevServiceTn = strServiceTn;
				}

				// Write out the sqlldr line
//				out.putLine("\"" + strBillTn + "\",\"" + strServiceTn + "\"," + iSrtSqnc++ + ",\"" + strRcrdCd + "\",\"" + strSqncNmbr + "\",\"" + strDetail + "\",\"" + strCurrDate + "\"");
				out.putLine("\"" + strBillTn + "\"," + iSrtSqnc++ + ",\"" + strRcrdCd + "\",\"" + strSqncNmbr + "\",\"" + strDetail + "\",\"" + strCurrDate + "\"");
			}

			// Housekeeping...
			in.cleanup();
			out.cleanup();
		}
		catch (Exception e)
		{
			System.out.println("Caught in main, e.printStackTrace()");
			e.printStackTrace();
			System.exit(100);
		}

		return;
	}

        public static String getCurrDate()
        {
		Calendar cal = Calendar.getInstance();
		String strDateTime = "";

		// switch month count from 0-11 to 1-12
		int iMonth =  cal.get(Calendar.MONTH) + 1;

		//  return current time in MM-DD-YYYY
		if (iMonth<10)
			strDateTime += "0";
		strDateTime += iMonth + "-";

		if (cal.get(Calendar.DAY_OF_MONTH) < 10)
			strDateTime += "0";
		strDateTime += cal.get(Calendar.DAY_OF_MONTH) + "-";

		strDateTime += cal.get(Calendar.YEAR);

		return strDateTime;
	}
}


