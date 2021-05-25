package com.alltel.lsr.common.objects;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/* Wrapper class for RequestDispatcher class */

public class AlltelRequestDispatcher
{
	private AlltelRequest alltelRequest = null;
	private AlltelResponse alltelResponse = null;
	private AlltelServlet alltelServlet = null;

	public AlltelRequestDispatcher(	AlltelServlet alltelServlet, 
					AlltelRequest alltelRequest, 
					AlltelResponse alltelResponse)
	{
		this.alltelServlet = alltelServlet;
		this.alltelRequest = alltelRequest;
		this.alltelResponse = alltelResponse;
	}
	
	public void forward(String url) throws Exception
	{
		ServletContext sc = alltelServlet.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher(url);
		rd.forward(alltelRequest.request, alltelResponse.response);
	}
	
	public void forward(RequestDispatcher rd) throws Exception
	{
		rd.forward(alltelRequest.request, alltelResponse.response);
	}

	public void include(String url) throws Exception
	{
		ServletContext sc = alltelServlet.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher(url);
		rd.include(alltelRequest.request, alltelResponse.response);
	}
}	
