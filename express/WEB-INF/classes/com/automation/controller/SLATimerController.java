/*
 * SLATimerController.java
 *
 * Created on June 22, 2009, 03:30 PM
 */

package com.automation.controller;

import com.alltel.lsr.common.objects.AlltelRequest;
import com.alltel.lsr.common.objects.AlltelResponse;
import com.alltel.lsr.common.objects.AlltelServlet;
import com.alltel.lsr.common.util.Log;

import com.automation.validator.SLATimer;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Vector;

/**
 *
 * @author antony
 * @version
 */
public class SLATimerController extends AlltelServlet {
    
    SLATimer slaTimer;

    protected void processRequest(AlltelRequest request, AlltelResponse response)
    throws ServletException, IOException {
        //do nothing
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init();
        Log.write("SLATimerController init calling ");
        Log.write("Setting up Timer...");

        slaTimer = new SLATimer();
    }
    
      /*
       * myservice method used for to get AlltelRequest,AlltelResponse input and send to
       * processRequest method.
       */
    
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        
        Log.write("SLATimerController myservice calling ");
        processRequest(request, response);
      
    }
    
    public void destroy() {
        Log.write("SLATimerController: Stopping Timer.....");
        slaTimer.cancelTimer();
        slaTimer = null;
    }
    
    protected void populateVariables()
    throws Exception {
        Log.write("SLATimerController populateVariables calling ");
    }
    
}
