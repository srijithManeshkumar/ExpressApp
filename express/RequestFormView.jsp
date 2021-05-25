<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	RequestFormView.jsp	
 * 
 * DESCRIPTION: Display Request forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        01-02-2002
 * 
 * HISTORY:
 *	02/01/2002 dmartz/psedlak Release 1.1
 *	10/10/2002 psedlak -Put cursor in 1st field of newly added section
 *	09/19/2003 psedlak -made generic
 *
 */

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestFormView.jsv  $
/*
/*   Rev 1.0.1.4   Oct 15 2002 10:55:36   e0069884
/* 
/*
/*   Rev 1.0.1.3   May 31 2002 11:26:08   dmartz
/* 
/*
/*   Rev 1.0.1.1   12 Mar 2002 09:59:28   dmartz
/*Correct auto-populate for READONLY fields
/*
/*   Rev 1.0.1.0   26 Feb 2002 11:05:16   dmartz
/*
/*
/*   Rev 1.5   19 Feb 2002 15:12:24   dmartz
/* 
/*
/*   Rev 1.4   13 Feb 2002 15:21:10   dmartz
/* 
/*
/*   Rev 1.2   31 Jan 2002 06:56:04   sedlak
/* 
/*
/*   Rev 1.1   30 Jan 2002 15:26:30   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:24   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0.1.4  $
*/
%>

<%@ include file="i_RequestHeader.jsp" %>

<%

RequestBean myorderBean = new RequestBean();
Log.write(Log.DEBUG_VERBOSE, "RequestFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
