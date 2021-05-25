/*
 * SOAHealthCheckTimer.java
 *
 * Created on June 22, 2009, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.validator;
import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.EmailManager;

import com.automation.dao.LSRdao;
import com.automation.validator.SOAHelper;

import java.util.List;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.net.URL;


import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author Antony Rajan
 */
/**
 * Schedule a task that calls the SOA Health Check function once in 15 mins 
 * to keep the SOA LTA and RTA objects in session.
 */

public class SOAHealthCheckTimer {
    Timer soaHealthCheckTimer;
    LSRdao lsrDao;
    String returnStr;
    SOAHelper soaHelper;
    Vector tnList;
    
    public SOAHealthCheckTimer() {
        lsrDao = new LSRdao();
        
        try {
            //insert try catch add log messages
            soaHealthCheckTimer = new Timer(true);//set name and make it run as a daemon thread
            
            soaHealthCheckTimer.schedule(new SOAHCTimerTask(),
                           15*1000,        //initial delay of 60 seconds for connection pool to be created -- increase to 1 min
                           15*60*1000);  //current rate - once every 15 minutes
        } catch (IllegalStateException ex) {
            Log.write("Inside SOA Health Check IllegalStateException block...: "+ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            Log.write("Inside SOA Health Check Exception block...: "+ex.getMessage());
            ex.printStackTrace();
        } catch(Throwable t) {
              //Log message here
              Log.write("Inside SOA Health Check catch for Throwable...");
              Log.write("SOA Health Check Throwable caught : "+t.getMessage());
              t.printStackTrace();
        }

    }
    
    public void setSOAHelper(SOAHelper soaHelperObj) {
        soaHelper = soaHelperObj;
    }
    
    public void cancelTimer() {
            soaHealthCheckTimer.cancel(); 
            soaHealthCheckTimer = null;

    }

    class SOAHCTimerTask extends TimerTask {
	
       public void run() {
          
          Thread.currentThread().setName("SOAHLTHCHKTIMER");
          
          try {
                    
                    Log.write("Calling SOA getproviderNames to make sure that SOA RTA/LTA session timeout does not happen..");

                    if(soaHelper.checkSOAConnection()) {
                        Log.write("SOA LTA/RTA Objects are alive. Session is active in SOA.");
                        
                        //calling gettransaction responses method to get PUSH NPAC completion / final status
                        soaHelper.updateTransactionResponse();
                    } else {
                        
                        Log.write("SOA LTA/RTA Objects are inactive. Closing and logging in again...");
                        //close all SOA connections and login again
                        boolean connectionsClosed = soaHelper.closeAllSOAConnections();
                        
                        soaHelper.initialize();
                        
                        //check connections again

                        if(!soaHelper.checkSOAConnection()) {

                            //send email to group that SOA connection is invalid
                            Log.write("Error: SOA LTA/RTA Objects are inactive. Session is not active in SOA.");

                            //send email
                            String strSingleEmail = PropertiesManager.getProperty("lsr.SOA.passwordexpiry.emailid");

                            StringBuffer strMessage = new StringBuffer();
                            strMessage.append("Error: SOA LTA/RTA Objects are inactive. Session is not active in SOA.\n");

                            try
                            {
                                    EmailManager.send(null, strSingleEmail, "Express Email on SOA Connections logged out: ", strMessage.toString());
                            }
                            catch (Exception e)
                            {
                                    e.printStackTrace();
                                    Log.write("Error while checking SOA LTA/RTA connections: Failed on SOA session check EmailManager.send()");
                            }
                        } else {
                            Log.write("SOA connections now available. LTA / RTA objects were created again as they were time out.");
                                                      
                        }   
                    }
                
                    
          } catch(Exception e) {
                e.printStackTrace();
                Log.write("Exception in SOA Health Check Timer :"+e.getMessage());
                
          } catch(Throwable t) {
              //Log message here
              Log.write("Inside catch for Throwable in SOA Health Check Timer ...");
              Log.write("Throwable caught in SOA Health Check Timer : "+t.getMessage());
              t.printStackTrace();
          }
       }
    }
}



