<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	CustomerLookup.jsp	
 * 
 * DESCRIPTION: Customer lookup via SIS for existing customer during order creation.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-12-2002
 * 
 */

%>
<%@ include file="i_header.jsp"%>
<%@ include file="ExpressUtil.jsp"%>
<%
	
	
	String strState = alltelRequest.getParameter( "state" );
	/* if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		// alltelResponse.sendRedirect(SECURITY_URL);
	}*/
%>	
<br><center>
<BR>
<form action="ExpressSISCtlr" method="POST">
<table  align=center width="75%" cellspacing=2 cellpadding=1>
	<tr>
		<td colspan=3 align=center class=tHeader>
			Customer Search  	
		</td>
	</tr>
	<tr>
		<td align=right nowrap>
			Telephone Number
		</td>
		<td>  	
			<input type="TEXT" size="15" maxLength="15" NAME="phone" VALUE="">
		</td>
		<td> 
			&nbsp; 
		</td>
		 
	</tr>
	<tr>
		<td align=right nowrap>
			Business Name
		</td><td>  	
			<input type="TEXT" size="30" maxLength="200" NAME="bname" VALUE="">
		</td>
		<td> 
			&nbsp; 
		</td>				
	</tr>
	<tr>
		<td  align=right nowrap>
			Last Name</td>
		<td>  	
			<input type="TEXT" size="30" maxLength="50" NAME="lname" VALUE="">
		</td><td align=left nowrap>  	
			First Name
			<input type="TEXT" size="20" maxLength="50" NAME="fname" VALUE="">
		</td>		
	</tr>	
	<tr>
		<td align=right nowrap>
			Street Address
		</td>
		<td>  	
			<input type="TEXT" size="30" maxLength="200" NAME="address" VALUE="">
	 	</td>
	 	 <td> 
	 		&nbsp; 
	 	</td>	
  </tr>
   <tr>
	  	<td align=right nowrap>
	  		City</td>
	  	<td>  	
  			<input type="TEXT" size="30" maxLength="200" NAME="city" VALUE="">
   		</td>
   		<td align=left nowrap>State&nbsp<%=printSelectBoxState("state", strState,  1  )%>  		
  			&nbsp;&nbsp;&nbsp;&nbsp;Zip:&nbsp;&nbsp;
  			<input type="TEXT" size="7" maxLength="5" NAME="zip" VALUE="">	  	
	 	</td>	
  </tr>
	<tr><td colspan=3 align=center>
			<INPUT TYPE="hidden" name="ExpressSISAction" value="1">
			<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
		</td>
	</tr>
</table>
</form>
</center>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</HTML>
