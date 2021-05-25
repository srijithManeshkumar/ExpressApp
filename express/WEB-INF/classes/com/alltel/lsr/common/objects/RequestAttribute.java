/* 
 * MODULE:		RequestAttribute
 * 
 * DESCRIPTION: The RequestAttribute class is used to store request parameter
 * 
 * AUTHOR:		pjs - cloned from eWave
 * 
 * DATE:		Nov 2001
 * 
 * HISTORY:
 * 
 */

package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;

public class RequestAttribute
{
	private String		strAttributeName;
	private String[]	strAttributeValues;
	
	public RequestAttribute(String strName, String[] strValues)
	{
		strAttributeName = strName;
		strAttributeValues = strValues;
	}
	
	public String getAttributeName()
	{
		return strAttributeName;
	}
	
	public String[] getAttributeValues()
	{
		return strAttributeValues;
	}
}
