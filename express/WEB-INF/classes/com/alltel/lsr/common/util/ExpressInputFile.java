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
 * MODULE:	ExpressInputFile.java
 * 
 * DESCRIPTION: Class to handle file input
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

public class ExpressInputFile
{
	private BufferedReader in;

	public ExpressInputFile(String fname) throws Exception
	{
		try
		{
			in = new BufferedReader(new FileReader(fname));
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
				in.close();
			}
			catch(IOException e2)
			{
				System.out.println("in.close() Unsuccessful");
			}
			throw e;
		}
		finally
		{
		}
	}

	public String getLine()
	{
		String s;
		try
		{
			s = in.readLine();
		}
		catch(IOException e)
		{
			System.out.println("in.readLine() Unsuccessful");
			s = "failed";
		}
	
		return s;
	}

	public void cleanup()
	{
		try
		{
			in.close();
		}
		catch(IOException e2)
		{
			System.out.println("in.close() Unsuccessful");
		}
	}
}
