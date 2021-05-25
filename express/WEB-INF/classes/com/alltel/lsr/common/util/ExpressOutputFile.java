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
 * MODULE:	ExpressOutputFile.java
 * 
 * DESCRIPTION: Class to handle file output
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.util;
import java.io.*;

public class ExpressOutputFile
{
	private PrintWriter out;

	public ExpressOutputFile(String fname) throws Exception
	{
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter(fname)));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Could not open " + fname);
			throw e;
		}
		catch(Exception e)
		{
			try
			{
				out.close();
			}
			catch(Exception e2)
			{
				System.out.println("out.close() Unsuccessful");
			}
			throw e;
		}
		finally
		{
		}
	}

	public int putLine(String s)
	{
		try
		{
			out.println(s);
		}
		catch(Exception e)
		{
			System.out.println("out.println() Unsuccessful");
			return -1;
		}
	
		return 0;
	}

	public void cleanup()
	{
		try
		{
			out.close();
		}
		catch(Exception e2)
		{
			System.out.println("out.close() Unsuccessful");
		}
	}
}
