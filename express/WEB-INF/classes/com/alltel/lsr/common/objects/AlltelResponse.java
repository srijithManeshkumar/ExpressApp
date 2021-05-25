/* 
 * MODULE:		AlltelResponse
 * 
 * DESCRIPTION: The AlltelResponse is used to encapsulate HttpResponse
 * 
 * AUTHOR:		kxz255, User Garage
 * 
 * DATE:		Nov. 1, 2001
 * 
 * HISTORY:
 *	11/2/2001	pjs	cloned from ewave
 */

package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;

// JSDK import
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * AlltelRequest is used to encapsulate HttpServletRequest object
 * @author	kxz255
 */

public class AlltelResponse
{
	HttpServletResponse response;
	
	public AlltelResponse(HttpServletResponse res)
	{		response = res;
	}
	
	public PrintWriter getPrintWriter()
		throws Exception
	{
		return response.getWriter();
	}
	
	/**
	 * public method to redirect to another servlet
	 * @author	kxz255, User Garage
	 * @param	strModule	the destination servlet
	 */
	public void sendRedirect(String strModule)
		throws Exception
	{		response.sendRedirect(strModule);
	}

	/**
	 * public method to add a cookie
	 * @author	kxz255, User Garage
	 * @param	ck		the Cookie object to be added
	 */
	 public void addCookie(Cookie ck) 
		throws Exception
	{
		response.addCookie(ck); 
	}

	/**
	 * public method to sst ContentType
	 * @author	Added by YXJ252 for temp use only, Survey Garage
	 * @param	Sting		strType -- httpType
	 */
	 public void setContentType(String strType)
	 {
		response.setContentType (strType);
	}

	/**
	 * public method to retrieve output stream
	 * @return	OutputStream	the output stream object
	 */
	public OutputStream getOutputStream()
		throws IOException
	{
		return response.getOutputStream();	
	}
	
	public HttpServletResponse getHttpResponse()
	{
		return this.response;	
	}
}
