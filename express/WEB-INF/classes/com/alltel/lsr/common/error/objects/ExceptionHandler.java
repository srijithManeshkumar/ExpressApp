/*
 * NOTICE:         THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONG TO ALLTEL 
 *               INFORMATION SERVICES. ANY UNAUTHORIZED ACCESS, USE, DUPLICATION 
 *                               OR DISCLOSURE IS UNLAWFUL.
 *
 *			        COPYRIGHT (C) 2004
 *				        BY
 *			        ALLTEL INFORMATION SERVICES
 *
 *
 *Added by Kumar K
 * 
 * MODULE:		 ExceptionHandler
 * 
 * DESCRIPTION:  This class provides the handleException() method, which acts
 *               as the final catch-all for exceptions that modules choose not 
 *               to handle. Handling of exceptions is done in a standard and 
 *               configurable manner.
 * 
 *
 * CHANGE HISTORY:
 * pjs 10-18-2004 chgd for JDK 1.4.2
 *	getStackTrace() chngd to getStackTraceTop() (since getStackTrace() now returns array)
 */
 
package com.alltel.lsr.common.error.objects;

// JDK imports
import java.sql.*;

// EBusiness imports
import com.alltel.lsr.common.interfaces.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * The Exceptionhandler class provides the handleException() method, which acts
 * as the final catch-all for exceptions that modules choose not to handle.
 * Handling of exceptions is done in a standard and configurable manner, and
 * involves the following actions:
 * <UL>
 *   <LI>extracting user information from session data
 *   <LI>extracting error type, severity, message and stack trace from exception object
 *   <LI>dispatching ErrorAuditEventInfo object to audit engine
 *   <LI>displaying error page to user
 * </UL>
 * <P>
 */

public class ExceptionHandler {

   /**
     * The handleException() method is called when modules encounter exceptions they
     * cannot handle. This method uses other private methods to do the following:
     *
     * @param   exception       exception being handled
     * @param   alltelRequest   request object -- contains user information
     * @param   alltelResponse  response object -- used to display error page to user
     *
     * @return  void
     */
    public static void handleException( Exception exception,
                                        AlltelRequest  alltelRequest,
                                        AlltelResponse alltelResponse)
    {
	AlltelException alltelException = null;
	SessionDataManager sdm = alltelRequest.getSessionDataManager();
	String strUser = sdm.getUser();

        if (exception instanceof AlltelException) {
            // an AlltelException object was thrown
            alltelException = (AlltelException)exception;
        } else {
            // standard Java Exception object was thrown, create AlltelException object
            alltelException = new AlltelException(exception);
        }
            
        // log error in case we run into problems below
        Log.write("EXCEPTION HANDLER CALLED FOR : " + alltelException.getStackTraceTop());

        try {
		persistErrorMessage(alltelException, strUser);
         
        } catch (Exception newException) {
            // error auditing error event -- log it and continue
            logInternalException(newException);
        }
            
	try {
		sendNotificationEmail(alltelException, strUser);
	} catch (Exception newException) {
		// error sending email, log it and continue
		logInternalException(newException);
        }
             
        try {
            displayUserErrorPage(alltelException, alltelRequest, alltelResponse);
        } catch (Exception newException) {
            logInternalException(newException);
        }

    }     
    

    
/*start Expres 5072 new method
 *
 */
    
    public static void handleException(String errMsg, Exception e) {
        
        // retrieve exception's stack trace for inclusion in log message
        String strExceptionDesc = getStackTrace(e);
        // append user account information
        strExceptionDesc =  errMsg + "\n" + strExceptionDesc;
        // log the exception and associated user information
        Log.write( "\n\n **** EXCEPTION HANDLER: BEGIN EXCEPTION DESCRIPTION ***\n"
                + strExceptionDesc
                + "\n **** EXCEPTION HANDLER: END EXCEPTION DESCRIPTION ***\n");
        
    }
    
    /*end Expres 5072 new method
     *
     */
    
    /**
     *start Expres 5072 new method
     * The getStackTrace() method will return the full stack trace for this exception
     *
     * in the form of a String
     *
     */
    
    private static String getStackTrace(Exception e) {
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
        } catch (Exception newException) {
            Log.write("EXCEPTION IN EXCEPTION HANDLER WHILE READING STACK TRACE: "
                    + newException);
            // we do nothing with exceptions thrown in exception-handling routines
            
        }
        // return the stack trace in the form of a String
        return strResult;
        
    }
    
    
     /*end Expres 5072 new method
      *
      */
    
    
    private static void sendNotificationEmail( AlltelException alltelException, String strUserID)
		throws Exception 
    {
	Log.write("Can send email notification here if necessary");
	return;
    }


    /**
     * Displays a user-friendly error page. This prevents the web server error
     * page from appearing on the user's browser.
     *
     * @param   alltelException    exception being handled
     * @param   alltelRequest      request object -- contains user information
     * @param   alltelResponse     response object -- used to display error page to user
     *
     * @return  void
     */
    private static void displayUserErrorPage( AlltelException alltelException,
                                              AlltelRequest   alltelRequest,
                                              AlltelResponse  alltelResponse)
                                              throws Exception {
                                                
        SessionDataManager sdm = alltelRequest.getSessionDataManager();
       
        // retrieve user message from exception object 
        String strUserErrorMessage = alltelException.getUserMessage();

        // use sendRedirect() instead of RequestDispatcher to invoke UI engine
        // -- handler can be called from JSP pages, which do not have access 
        // to the dispatcher
        //alltelResponse.sendRedirect("lsrerr.htm");
        alltelResponse.sendRedirect("LsrErr.jsp");	//Displays message and redirects to Home Page
    
    }
    
    
    
    /**
    * This method persists the error info to a database table. 
    * @param  alltelException  	exception object
    * @param  userid		User who got error of course
    *
    * @exception  error writing audit event to database
    * @return void
    */
    private static void persistErrorMessage(AlltelException alltelException, String strUserID)
                                        throws Exception
    {
   
        // error audit trail will also contain error type, severity, message, stack trace and SP
        int iErrorType            = alltelException.getErrorType();
        int iErrorSeverityLevel   = alltelException.getErrorSeverityLevel();
        String strErrorMessage    = alltelException.getMessage();
        String strStackTrace      = alltelException.getStackTraceTop();

        // error audit trail will also contain current user, current time
	String strCurrentUser = strUserID;
        String strCurrentDate = Toolkit.getDateTime();

        Connection conn = null;
        Statement stmt  = null;
        
        // contruct insert statement for all the above information
        String strInsertStatement = 
        	"INSERT INTO AUDIT_ERROR_TRAIL_T ( USERID, ERR_DT, ERR_TYPE, SEVERITY_LVL, ERR_MSG, STACK_TRACE ) " +
          	"VALUES ( '" + strCurrentUser + "' , TO_DATE('" + strCurrentDate + "','YYYY-MM-DD HH24:MI:SS'), " +
		+ iErrorType + ", " + iErrorSeverityLevel + ", '" + strErrorMessage + "', '" + strStackTrace + "' )";
          
        try {
            	conn = DatabaseManager.getConnection();
            	stmt = conn.createStatement();
		stmt.executeUpdate(strInsertStatement);
            
        } catch (Exception e) {
            // toss error up to calling method
        } finally {
            // release database connection
            DatabaseManager.releaseConnection(conn);
        }
    }
    

    private static void logInternalException(Exception internalException) {
        
        String strStackTrace = null;
        if (internalException instanceof AlltelException) 
	{
            strStackTrace = ((AlltelException)internalException).getStackTraceTop();
        } 
	else
	{
            strStackTrace = new AlltelException(internalException).getStackTraceTop();
        }
        String strLogMessage = 
                "\n\n**** EXCEPTION IN EXCEPTION HANDLER ****\n"
              + "The exception handler encountered problems handling the error logged below.\n"
              + "Possible causes are:\n"
              + "      problems writing error msg record to database\n"
              + "      problems sending error notification email -- is email configured correctly?\n"
              + "      problems displaying standard error page.\n\n"
              + strStackTrace + "\n";
        Log.write(strLogMessage);
    }
                                                                       


    /**
     * Class static initializer.
     */
    static {
        Log.write("*** ENTERED CONSTRUCTOR FOR EXCEPTION HANDLER . . .");
    }

}  // end of ExceptionHandler class
