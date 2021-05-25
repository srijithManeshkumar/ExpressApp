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
 * MODULE:	ExpressCabsUpload.java
 *
 * DESCRIPTION: Batch job which will read a CABS input file and output a file prepared
 *			for sqlldr.
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-01-2002
 *
 * HISTORY:
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import com.alltel.lsr.common.util.*;

public class ExpressCabsUpload
{
	public static void main(String[] args)
	{
		int iSrtSqnc = 1;
		String strRow = "";
		String strBan = "";
		String strCurrDt = "";
		Vector vTmp = new Vector();
		boolean bTryingToFigureOut = true;

		// Get the current date
		if (args.length == 3)
			strCurrDt = args[2];
		else
			strCurrDt = getCurrDate();

		try
		{
			// Open files for processing
			ExpressInputFile in = new ExpressInputFile(args[0]);
			ExpressOutputFile out = new ExpressOutputFile(args[1]);

			// Read thru the file, line by line
			while ((strRow = in.getLine()) != null)
			{
				// Did we hit a new page break
				if (strRow.indexOf("break") > 0)
				{
					// Were we trying to figure it out?
					if (bTryingToFigureOut)
					{
						// Clear out the holding and start fresh
						vTmp.clear();
					}

					// Are we on a Bill Page?
					if (strRow.indexOf("ACCOUNT NO") > 0)
					{
						// Get BAN
						strBan = strRow.substring(86);
						strBan = strBan.replace(' ','_');
						bTryingToFigureOut = false;
					}
					else // We need to figure out what this is
					{
						bTryingToFigureOut = true;
					}
				}


				// Are we tryng to figure out what we have?
				if (bTryingToFigureOut)
				{
					// Is this a Customer Service Page
					if (strRow.indexOf("(CSR)") > 0)
					{
						// get BAN
						strBan = strRow.substring(59,69) + strRow.substring(72);
						strBan = strBan.replace(' ','_');

						// Print out holding
						Iterator it = vTmp.iterator();
						String strTmp = "";
						while (it.hasNext())
						{
							strTmp = (String)it.next();
							out.putLine("\"" + strBan +
								"\"," + iSrtSqnc++ +
								",\"" + strTmp + "\",\"" +
								strCurrDt + "\"");
						}
						vTmp.clear();

						// Reset Flag
						bTryingToFigureOut = false;
					}
					else // Add to Holding
					{
						String strHolding = new String(strRow);
						vTmp.addElement(strHolding);
					}
				}

				// If we know what we have, then write out the line
				if (!bTryingToFigureOut)
				{
					out.putLine("\"" + strBan + "\"," + iSrtSqnc++ +
						",\"" + strRow + "\",\"" + strCurrDt + "\"");
				}
			}

			// Housekeeping...
			in.cleanup();
			out.cleanup();
		}
		catch (Exception e)
		{
			System.out.println("Caught in main, e.printStackTrace()");
			e.printStackTrace();
			System.exit(1);
		}

		return;
	}

        public static String getCurrDate()
        {
		Calendar cal = Calendar.getInstance();
		String strDateTime = "";

		// switch month count from 0-11 to 1-12
		int iMonth =  cal.get(Calendar.MONTH) + 1;

		//  return current date in MM-DD-YYYY format
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


