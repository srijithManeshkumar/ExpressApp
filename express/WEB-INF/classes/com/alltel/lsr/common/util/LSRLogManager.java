/*
 * SOURCE FILE:
 *      LSRLogManager.java     
 * DATE:
 *      10/9/2001
 *
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                          COPYRIGHT (C) 2001
 *                                 BY
 *
 * HISTORY:
 * 10/09/2001		pjs	cloned from ewave
 */
package com.alltel.lsr.common.util;

import java.io.*;
import java.util.*;
import java.text.*;

// LSR imports
import com.alltel.lsr.common.util.*;

/**
 * This class is responsible for outputting messages to the log.  
 */
public class LSRLogManager
{
    static private File m_logFile;
    static private PrintWriter out;
    final static public int INFO = 0;
    final static public int ERROR = 1;
    final static public int WARNING = 2;
        
    
    /** 
     * Builds an empty LSRLogManager object. 
     */
    private LSRLogManager() 
    {
    }
    
    /** 
     * Builds a LSRLogManager object.   
     * @param strLogFileName a log file name to used
     * @param strLogFileHomeDir a log file home directory
     */
    public LSRLogManager(String strLogFileName) 
    {   
        //System.out.println("Open Log File = " + strLogFileName);
        m_logFile = new File((String)strLogFileName);
        try
        {
            if ( m_logFile.exists() ) 
            {
                //System.out.println("Log File " + strLogFileName 
                //                    + " is already exist!");
                // Append the file
                out = new PrintWriter(new BufferedWriter(new FileWriter(strLogFileName,true)));
            }
            else
            {
                //System.out.println("Log File " + strLogFileName 
                //                    + " is NOT exist YET!");
                
                out = new PrintWriter(new BufferedWriter(new FileWriter(m_logFile)));
            }
        }
        catch (IOException e)
        {
        }
        //Log.write("Opening new log file: " + (String)strLogFileName);
            
    }
    
    /**
     * This method writes the input message to the log.  The method takes 
     * two parameters: the message itself and an int describing whether or 
     * not the message is an error.
     * @param strMessage a String message that will be written to the log
     * @param iErrorLevel the level of severity of the message (ie. ERROR, WARNING, INFO)
     */
    public static void write(String strMessage, int iErrorLevel) 
    {
        // Begin the log message with the current time/date
        String strLog=DateFormat.getDateTimeInstance().format(new java.util.Date());
        
        // Now add the error level
        if (iErrorLevel == ERROR)
            strLog += " ERROR: ";
        else if (iErrorLevel == WARNING)
            strLog += " WARNING: ";
        else
            strLog += " INFO: ";
        
        // And end with the actual message
        strLog += strMessage;
        out.println(strLog);
        out.flush();
    }
    
	/**
     * This method writes the input message to the log.  The method takes 
     * one parameter: the message itself
     * @param strMessage a String message that will be written to the log
     */
    public static void write(String strMessage) 
    {
        out.println(strMessage);
        out.flush();
    }
	
}
