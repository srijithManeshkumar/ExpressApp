/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2016
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:	HelpDocCtlr.java
 * DESCRIPTION:
 * AUTHOR:
 * DATE:       10-25-2017
 *
 * HISTORY:
 *  10/25/2017 - Initial Creation of Help Document Controller.
 *
 */
package com.alltel.lsr.common.servlets;

import java.io.File;
import java.nio.file.Files;

import com.alltel.lsr.common.objects.AlltelRequest;
import com.alltel.lsr.common.objects.AlltelResponse;
import com.alltel.lsr.common.objects.AlltelServlet;

import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.PropertiesManager;

public class HelpDocCtlr extends AlltelServlet
{

        public void myservice (AlltelRequest request, AlltelResponse response) 
                        throws Exception
        {
            Log.write(Log.DEBUG_VERBOSE, "HelpDocCtlr()");
            File file = new File(PropertiesManager.getProperty("help.docs.path") + request.getHttpRequest().getServletPath());
            response.getHttpResponse().setHeader("Content-Type", "application/pdf");
            response.getHttpResponse().setHeader("Content-Length", String.valueOf(file.length()));
            response.getHttpResponse().setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
            Files.copy(file.toPath(), response.getOutputStream());

        }

	protected void populateVariables()
		throws Exception
	{
	}
}
