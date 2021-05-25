package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 */
public class Out
{
	private String name, baseURL, numResultsSuffix;
	private static Out[] commonSpecs =
		{ new Out("google", "http://www.google.com/search?q=", "&num="),
		  new Out("infoseek", "http://infoseek.go.com/Titles=?qt=", "&nh=")
		};
	public Out(String name, String baseURL, String numResultsSuffix)
	{	this.name = name;
		this.baseURL = baseURL;
		this.numResultsSuffix = numResultsSuffix;
	}
	public String makeURL(String searchString, String numResults)
	{	return (baseURL + searchString + numResultsSuffix + numResults);
	}
	public String getName()
	{	return(name);
	}
	public static Out[] getCommonSpecs()
	{	return (commonSpecs);
	}

}
