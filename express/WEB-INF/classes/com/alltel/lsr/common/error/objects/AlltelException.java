/*
 * 
 * MODULE:		 AlltelException
 * 
 * DESCRIPTION:  This object extends the standard Java exception object. In addition
 *               to the standard message and stack trace provided by the exception 
 *               object this class defines an error type (by implementing the 
 *               ErrorType interface) and a severity level (by implementing the 
 *               ErrorSeverityLevel interface). It also allows the caller to define 
 *               the service provider the user was engaged with when the error
 *               condition occurred. Lastly, the caller can optionally define a 
 *               user message that will be displayed by the exception handler on 
 *               the user's browser.
 * 
 *
 * CHANGE HISTORY:
 *
 *
 */
 
package com.alltel.lsr.common.error.objects;

// JDK imports
import java.io.*;

import com.alltel.lsr.common.util.*;

/**
 * The AlltelException object extends the standard Java exception object. In
 * addition to the standard message and stack trace provided by the exception object
 * this class defines an error type (by implementing the ErrorType interface) and
 * a severity level (by implementing the ErrorSeverityLevel interface). It also allows
 * the caller to define the service provider the user was engaged with when the error
 * condition occurred. Lastly, the caller can optionally define a user message that 
 * will be displayed by the exception handler on the user's browser.
 * <P>
 *
 */

public class AlltelException extends Exception implements ErrorType, ErrorSeverityLevel
{
	private int m_iErrorType = APPSERVER_ERROR;
	private int m_iErrorSeverityLevel = ERROR;
    	private String m_strErrorMessage = null;
    	private String m_strUserMessage  = null;
    	private String m_strStackTrace = null;
    
    /**
     * Class constructor.
     * This constructor takes in an error type, severity level, and error message. 
     * 
     * @param iErrorType                type for this error
     * @param iErrorSeverityLevel       severity of this error
     * @param strErrorMessage           message to be sent to error audit trail
     */
    public AlltelException( int iErrorType, int iErrorSeverityLevel, String strErrorMessage) 
    {
        m_iErrorType          = iErrorType;
        m_iErrorSeverityLevel = iErrorSeverityLevel;
        m_strErrorMessage     = strErrorMessage;     

    }
    
    
    /**
     * Class constructor.
     * This constructor takes in an error type, severity level, 
     * error message, and user message to be displayed on the remote browser.
     * 
     * @param iErrorType                type for this error
     * @param iErrorSeverityLevel       severity of this error
     * @param strErrorMessage           message to be sent to error audit trail
     * @param strUser Message           message to be displayed to user
     */
    public AlltelException(int iErrorType, int iErrorSeverityLevel, 
                           String strErrorMessage,    // message for audit trail
                           String strUserMessage)     // message for remote user
    {
        m_iErrorType          = iErrorType;
        m_iErrorSeverityLevel = iErrorSeverityLevel;
        m_strErrorMessage     = strErrorMessage;
        m_strUserMessage      = strUserMessage;

    }
  
    
    /**
     * Class constructor.
     * This constructor takes in a standard Java Exception object. It populates class
     * attributes with the message and stack trace contained in the Exception object.
     * 
     * @param exception  standard Java exception object
     */
    public AlltelException(Exception exception)
    {
         // retrieve message and stack trace from standard Exception object
         m_strErrorMessage = exception.getMessage();
         m_strStackTrace   = getStackTrace(exception);     
    }
 
    
    /** 
     * Returns the error type for this AlltelException object. This will be 
     * one of the error types defined by the ErrorType interface.
     *
     * @return  int  type for this error
     */
    public int getErrorType()
    {
        return m_iErrorType;
    }
    
    /** 
     * Returns the error severity level for this AlltelException object. This 
     * will be one of the levels defined by the ErrorSeverityLevel interface.
     *
     * @return  int  severity level for this error
     */
    public int getErrorSeverityLevel()
    {
        return m_iErrorSeverityLevel;
    }
    
    
    /**
     * Returns the error message stored by this AlltelException object. 
     * This message wil go into the error audit trail.
     * 
     * @return String  error message
     */
    public String getMessage() 
    {
        return m_strErrorMessage;
    }


    /**
     * Returns the user message stored by this AlltelException object. 
     * This message will be displayed to the remote user.
     * 
     * @return String  error message to be displayed to user
     */
    public String getUserMessage() 
    {
        return m_strUserMessage;
    } 

     
    /**
     * Returns the full execution stack trace for this exception.
     * 
     * @return String exception stack trace
     */
//pjs 9-6-2004--- create getStackTraceTop() since JDK1.4.2 chngd this guy
//    public String getStackTrace()
 //  {
        // if this AlltelExcetiob object was created from a standard Java Exception
        // object then the stack trace was already populated in the constructor
        // for this class
//        return (m_strStackTrace == null) ? getStackTrace(this) : m_strStackTrace;
//   }

     public String getStackTraceTop()
     {
	String strResponse="";
	StackTraceElement[] stackElements = this.getStackTrace();
        Log.write(" *** stackElements.length()=" + stackElements.length);
	for (int lcv = 0; lcv < stackElements.length; lcv++)
        {
	    strResponse += "\nfile:" + stackElements[lcv].getFileName();
            Log.write("Filename: " + stackElements[lcv].getFileName());
            Log.write("Line number: " + stackElements[lcv].getLineNumber());
	    strResponse += "\nline:" + stackElements[lcv].getLineNumber();
	    strResponse += "\nclass:" + stackElements[lcv].getClassName();
            String className = stackElements[lcv].getClassName(); 
	    //String packageName = extractPackageName (className);
            //String simpleClassName = extractSimpleClassName (className);

            //Log.write("Package name: " + ("".equals (packageName)? "[default package]" : packageName));
            Log.write("Full class name: " + className);
            //Log.write("Simple class name: " + simpleClassName);
            //Log.write("Unmunged class name: " + unmungeSimpleClassName (simpleClassName));
            //Log.write("Direct class name: " + extractDirectClassName (simpleClassName));

            Log.write("Method name: " + stackElements[lcv].getMethodName());
            Log.write("Native method?: " + stackElements[lcv].isNativeMethod());

            Log.write("toString(): " + stackElements[lcv].toString());
            Log.write("");
        }
	return strResponse;
     }
    

     /*
     * The getStackTrace() private method will return the full execution stack 
     * trace for an Exception object. It could be called on this AlltelException
     * object or on the standard Java Exception object that was passed to the
     * constructor of this AlltelException object.
     * 
     * @return String   exception stack trace
     */
    private String getStackTrace(Exception e) {
        
        String strResult = "";   // String to be returned
        
        // ByteArrayOutputStream to capture the stack trace
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // PrintWriter object to pass to exception's printStackTrace() method. We
        // will print the stack trace into the ByteArrayOutputStream declared above.
        PrintWriter printwriter   = new PrintWriter(out);

        try {
            e.printStackTrace(printwriter);
            printwriter.close();
            strResult = out.toString();
        }
        catch (Exception newException) {
            Log.write(" *** EXCEPTION IN EXCEPTION HANDLER WHILE READING STACK TRACE: \n"
                     + newException + "\n"
                     + " *** STACK TRACE SO FAR: \n"
                     + strResult);
        }

        // return the stack trace in the form of a String
        return strResult;
    }
    
    

}
