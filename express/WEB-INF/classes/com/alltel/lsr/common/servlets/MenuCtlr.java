package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class MenuCtlr extends AlltelServlet
{

    public void myservice (AlltelRequest request, AlltelResponse response) 
		       throws Exception
    {
           Log.write(Log.DEBUG_VERBOSE, "MenuCtlr()");
           SessionDataManager sdm = request.getSessionDataManager();
           AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

	   HttpSession session = request.getSession();

	   String sectype = (String)session.getAttribute("security");
	   String thisuser = (String)session.getAttribute("theuser");
	   String extent = (String)request.getParameter("mevent");

/*	  if(extent != null){
	      response.setContentType("text/html");
	   PrintWriter out = response.getWriter();
	   out.println("<HTML><HEAD><TITLE>Menu Stuff</HEAD></TITLE><BODY>");
	   out.println("<h1> extent: " + extent + "</h1>");
	   out.println("</BODY>");
	   out.println("</HTML>");
	     return;
	   }
*/
	   if(extent == null){
	   	extent = "base";
	   }
	   if(sectype == null || thisuser == null){ // no session!
	       alltelRequestDispatcher.forward("/LoginCtlr");
	       return;
	   }
           
	   request.getHttpRequest().setAttribute("who", thisuser);
	   request.getHttpRequest().setAttribute("accesslevel", sectype);
	   request.getHttpRequest().setAttribute("mextent", extent);
	   alltelRequestDispatcher.forward("/MenuView.jsp");
	   return;


    }
    
    //private MenuProfileBean MenuBean () {


	 // Insert DB Lookup stuff here??? -- right now just filler

	 //LoginProfileBean lpBean = new LoginProfileBean();
		
	 //return lpBean ;
	 
    //}

	protected void populateVariables()
		throws Exception
	{
	}
}
