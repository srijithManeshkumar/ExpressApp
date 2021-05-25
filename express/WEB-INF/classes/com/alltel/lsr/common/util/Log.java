package com.alltel.lsr.common.util;

import weblogic.common.*;
//import com.alltel.lsr.common.util.*;
import weblogic.logging.NonCatalogLogger;

/** 
 * The Log class is the interface with Weblogic's logging services. It provides
 * the Log.write() method, which writes a message to the weblogic.log file in the 
 * server directory.
 *
 * CHANGE HISTORY:
 * 
 * 11/08/2001 pjs cloned from ewave
 * 11/13/2001 pjs added override of write()
 * 1/16/2004 pjs tweeked for WL 7.*
 * 
 */


public class Log {

	final static public int ERROR = 1;
	final static public int WARNING = 2;
	final static public int INFO = 3;
	final static public int DEBUG = 4;
	final static public int DEBUG_VERBOSE = 5;

	static public int g_iLevel = WARNING;
 
	private static NonCatalogLogger m_logger = null;
	private static final String SUB_SYSTEM = "EXPRESS";

    	/**
     	 * The Log.write() method writes a String to the weblogic log file
     	 * in the server directory
    	 */
	public static final void write(String strMessage) 
	{
		try {
			//T3ServicesDef t3s = T3Services.getT3Services();  
			//t3s.log().info(strMessage);
           		if (m_logger==null)
			{
				m_logger = new NonCatalogLogger(SUB_SYSTEM);
			}
			m_logger.info(strMessage);
		} catch (Exception t) {
			// do nothing, to avoid getting stuck in a loop trying to log
			// exceptions encountered in this Log class
		}
	}

	// Left below for backward compatibility
	public static final void write(int iErrorLevel, String strMessage) 
	{
		
		//Get logging value from properties
		try {
			g_iLevel = PropertiesManager.getIntegerProperty("lsr.logging.level", WARNING);
		} catch (Exception t) {
		}
		if (iErrorLevel > g_iLevel)
		{	return;		//skip message
		}

        	String strLog = "";
        	// Now add the error level
        	if (iErrorLevel == ERROR)
	            strLog += " ERROR: ";
		else
		if (iErrorLevel == WARNING)
			strLog += " WARNING: ";
		
        	strLog += strMessage;
		try {
			//T3ServicesDef t3s = T3Services.getT3Services();  
			//t3s.log().info(strLog);
           		if (m_logger==null)
			{
				m_logger = new NonCatalogLogger(SUB_SYSTEM);
			}
			if (iErrorLevel == ERROR)
			{	m_logger.error(strLog);
			}
			else if (iErrorLevel == WARNING)
			{	m_logger.warning(strLog);
			}
			else if (iErrorLevel ==  INFO)
			{	m_logger.info(strLog);
			}
			else	
			{	m_logger.debug(strLog);
			}

		} catch (Exception t) {
		}
	}

    
	// Constructor for Log class. Just want to track how often this static
	// class is instantiated, we'll get rid of this constructor later
	public Log() {
        	super();
		m_logger = new NonCatalogLogger(SUB_SYSTEM);
	        Log.write( "\n\n **** IN CONSTRUCTOR FOR LOG CLASS ***\n");
    	}

}  // end of Log class

