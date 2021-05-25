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
<%@ include file="ExpressUtil.jsp" %>
<%@ include file="i_header.jsp" %>
<jsp:usebean id="customersearchbean" scope="request" class="com.alltel.lsr.common.objects.CustomerSearchBean" />
<%
	
	String strState = alltelRequest.getParameter( "state" );
%>	
<br><center>
<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
<tr><td>
	<A HREF="DwoCtlr?dwocreate=view">Create&nbsp;NEW&nbsp;Order&nbsp;
	without Customer Match.</A>
</tr></td>
</table>	
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
			<input type="TEXT" size="15" maxLength="15" NAME="phone" VALUE="<%=customersearchbean.getPhone()%>">
		</td>
		<td> 
			&nbsp; 
		</td>
		 
	</tr>
	<tr>
		<td align=right nowrap>
			Business Name
		</td><td>  	
			<input type="TEXT" size="30" maxLength="200" NAME="bname" VALUE="<%=customersearchbean.getBusinessName()%>">
		</td>
		<td> 
			&nbsp; 
		</td>				
	</tr>
	<tr>
		<td  align=right nowrap>
			Last Name</td>
		<td>  	
			<input type="TEXT" size="30" maxLength="50" NAME="lname" VALUE="<%=customersearchbean.getLastName()%>">
		</td><td align=left nowrap>  	
			First Name
			<input type="TEXT" size="20" maxLength="50" NAME="fname" VALUE="<%=customersearchbean.getFirstName()%>">
		</td>		
	</tr>	
	<tr>
		<td align=right nowrap>
			Street Address
		</td>
		<td>  	
			<input type="TEXT" size="30" maxLength="200" NAME="address" VALUE="<%=customersearchbean.getAddress()%>">
	 	</td>
	 	 <td> 
	 		&nbsp; 
	 	</td>	
  </tr>
   <tr>
	  	<td align=right nowrap>
	  		City</td>
	  	<td>  	
  			<input type="TEXT" size="30" maxLength="200" NAME="city" VALUE="<%=customersearchbean.getCity()%>">
   		</td>
   		<td align=left nowrap>State&nbsp<%=printSelectBoxState("state", strState,  1  )%>  		
  			&nbsp;&nbsp;&nbsp;&nbsp;Zip:&nbsp;&nbsp;
  			<input type="TEXT" size="7" maxLength="5" NAME="zip" VALUE="<%=customersearchbean.getZip()%>">	  	
	 	</td>	
  </tr>
	<tr><td colspan=3 align=center>
			<INPUT TYPE="hidden" name="ExpressSISAction" value="1">
			<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
		</td>
	</tr>
</table>
<HR width=100%>
	<%=customersearchbean.getHTMLResults()%>
</form>
<br clear=all><br>
<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
<tr><td>
	<A HREF="DwoCtlr?dwocreate=view">Create&nbsp;NEW&nbsp;Order&nbsp;
	without Customer Match.</A>
</tr></td>
</table>			
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
