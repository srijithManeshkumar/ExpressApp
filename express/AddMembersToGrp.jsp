<%
/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2005
 *                                      BY
 *                              Alltel Communications Inc.
 */
/*
 * MODULE:      EmployeeGroups.jsp
 *
 * DESCRIPTION: Display employee groups by creator Id and form to create new.
 *
 * AUTHOR:      Express devel team
 *
 * DATE:        04-04-2005
 *
 * HISTORY:
 */
%>

<%@ include file="i_header.jsp" %>
<%@ include file="ExpressUtil.jsp" %>
<%@ page import ="java.util.*" %> 
<%@ page import ="java.lang.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<%
final String SECURITY_OBJECT = "PROV_REPORTS";
String strGrpDesc = alltelRequest.getParameter( "grpdesc" );
String strGrpName = alltelRequest.getParameter( "grpname" );
String strAction =  alltelRequest.getParameter( "action" );
String strGroupId =  alltelRequest.getParameter( "egid" );
Vector allUsersNames = new Vector( 100);
Vector allUsersUserIds = new Vector( 100);
Hashtable groupUsers = new Hashtable( 100);

final String Windstream_EMPLOYEE_TYPE = "P";
int iGroupId = -1;
String strMsg = "";
if (!sdm.isAuthorized(SECURITY_OBJECT))
{
	Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
	alltelResponse.sendRedirect(SECURITY_URL);
}
	
try{
	if( strGroupId != null )
	{
		iGroupId = Integer.parseInt( strGroupId );
	}
}catch(Exception e) 
{
	e.printStackTrace();
	Log.write(Log.DEBUG_VERBOSE, "AddMembersToGrp.jsp: Caught exception e=[" + e.getMessage() + "]");
}	
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	String strUserid =  sdm.getUser();
	ResultSet rset = null;
	
	String query_all  = " select usr.USERID, usr.FRST_NM ||' ' || usr.LST_NM  from " 
		+ " USERID_T usr, company_t cp where "
		+ " usr.CMPNY_SQNC_NMBR = cp.CMPNY_SQNC_NMBR "
		+ " AND cp.CMPNY_TYP = ? order by  usr.LST_NM  ";
	
	String query_group = " select  empg.userid, usr.frst_nm || ' ' || "
		+ "	usr.lst_nm from USR_USRGRP_LINK_T empg, "
		+ "	USERID_T usr where  usr.userid = empg.userid and "
		+ "	empg.STATUS = ? AND empg.USRGRP_EMP_SQNC_NMBR = ? ";

	try{
	  	conn = DatabaseManager.getConnection();
	  	pstmt = conn.prepareStatement( query_all  );
		pstmt.clearParameters();
		pstmt.setString( 1, Windstream_EMPLOYEE_TYPE );
		rset = pstmt.executeQuery();
		while( rset.next() ){
			allUsersUserIds.add( rset.getString( 1) );
			allUsersNames.add( rset.getString( 2 ) );
		}
		pstmt.close();
		rset.close();
		pstmt = conn.prepareStatement( query_group  );
		pstmt.clearParameters();
		pstmt.setString( 1, "N" );
		pstmt.setInt( 2, iGroupId );
		rset = pstmt.executeQuery();
		while( rset.next() ){
			groupUsers.put( rset.getString( 1), rset.getString( 1) ) ;
		}
	} //try
	catch(Exception e) {
		e.printStackTrace();
		Log.write(Log.DEBUG_VERBOSE, "AddMembersToGrp.jsp: Caught exception e=[" + e.getMessage() + "]");
	}
	finally {
		try {
			rset.close(); rset=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) { eee.printStackTrace();}
		DatabaseManager.releaseConnection(conn);
	}
%>
<table align=center width="100%" cellspacing=0 cellpadding=0>
  <tr>
    <TH width="100%" align=center bgcolor="#7AABDE"><SPAN class="barheader">Employee Groups</SPAN> </th>
    </TH>
  </tr>
</table>
<%
		conn = null;
		pstmt = null;
		int nType = 0;
		 rset = null;
		String strQuery1 = " Select USERGROUP_NAME,  USR_GRP_DSCRPTN  from  " 
			+ " USERGROUP_EMP_T WHERE MDFD_USERID  = ?  AND USRGRP_EMP_SQNC_NMBR = ? "; 
	 
	  try{
	  	conn = DatabaseManager.getConnection();
	  	pstmt = conn.prepareStatement( strQuery1  );
		pstmt.clearParameters();
		pstmt.setString( 1, strUserid );
		pstmt.setInt( 2, iGroupId );
		rset = pstmt.executeQuery();
		if( rset.next() ){
			strGrpName = rset.getString( 1);
			strGrpDesc = rset.getString( 2 );
		}
	} //try
	catch(Exception e) {
		e.printStackTrace();
		Log.write(Log.DEBUG_VERBOSE, "AddMembersToGrp.jsp: Caught exception e=[" + e.getMessage() + "]");
	}
	finally {
		try {
			rset.close(); rset=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) { eee.printStackTrace();}
		DatabaseManager.releaseConnection(conn);
	}
%>
<%
int i = 0;
conn = null;
pstmt = null;
String strDividerRow = "";
int[]  iBatchCount = null;
if( strAction!= null)
{	
	String updateQry = " insert into USR_USRGRP_LINK_T ( "
		+ " USERID, USRGRP_EMP_SQNC_NMBR )	VALUES( ?, ? ) ";	
		
	String updateQryRemoveAll = " DELETE FROM USR_USRGRP_LINK_T "
		+ " WHERE USRGRP_EMP_SQNC_NMBR = ? ";	

	
	int iCounter = 0;
	try {	
		conn = DatabaseManager.getConnection();	
		pstmt = conn.prepareStatement( updateQryRemoveAll  );
		pstmt.clearParameters();
		pstmt.setInt( 1, iGroupId );
		pstmt.executeUpdate();
		pstmt.close(); 
		pstmt = null;
		
		pstmt = conn.prepareStatement( updateQry  );
		int iAllUsersCounter = allUsersUserIds.size();
		String tempUserId =  "";
		while( iAllUsersCounter > iCounter )
		{
			
			tempUserId = (String) allUsersUserIds.get(iCounter);
			tempUserId = alltelRequest.getParameter( tempUserId );
			if( tempUserId != null )
			{
				pstmt.clearParameters();
				pstmt.setString( 1, tempUserId );
				pstmt.setInt( 2, iGroupId );
				pstmt.addBatch();
			}
			iCounter++;
		}
		
		if( iCounter > 0 ){
			iBatchCount = pstmt.executeBatch(  );  
		}		

	} //try
	catch(Exception e) {
		Log.write(Log.DEBUG_VERBOSE, "AddMembersToGrp.jsp: Caught exception e=[" + e + "]");
	}
	finally {
		try {
			pstmt.close(); pstmt=null;
		} catch (Exception eee) {}
		DatabaseManager.releaseConnection(conn);
	}
	
	if( iBatchCount != null ){
		alltelResponse.sendRedirect("/EmployeeGroups.jsp?msg=iBatchCount.length" );
	}else{
		alltelResponse.sendRedirect("/EmployeeGroups.jsp?msg=0" );
	}
}
%>
<!--********************** FORM SECTION ********************-->
<form action="AddMembersToGrp.jsp" onSubmit="return beforeSubmitt();" onreset="return resetToDefault();" method="POST">
<table 	cellspacing=0 cellpadding=0 background="#cccccc" class=grouptable align=center   width="80%">
  <tr>
  	<td align=left colspan=5 >
  	 <span class=tHeader>Group Name:</span><b>&nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &nbsp; &nbsp; &nbsp; <%=strGrpName%> </b>
   	</td>
   	</tr>
  	<tr>
  	<td align=left colspan=5>
  	 <span class=tHeader>Group Description:</span>
  		<b>&nbsp; &nbsp; &nbsp;&nbsp; &nbsp; <%=strGrpDesc%></b>
  		 </td>  	
  	</tr>
  	<tr>
  	<td colspan=5>
  		&nbsp;  	
  	</td></tr> 
  	<tr> 	
	  	<td colspan=5 class=smallNote> To add an employee to a group, check the box corresponding to the name. 
	  	<BR> Remove Employees from current group by unchecking the boxes. <br>Press the submit button when finished to save 
	  	your changes.
	  	<br><br>
	  	</td>  	 	
  	</tr>
  	<tr class=tHeader>
  		<td width="10%">
  			Row#
   		</td>
  		<td width="39%">
  			Employee Name (Employee Id)
   		</td>
   		<td width="1%">
  			&nbsp;
   		</td>
   		<td width="10%">
  			Row#
   		</td>
   		<td width="40%">
  			Employee Name (Employee Id)
   		</td>	  	
  	</tr>
  	<%
  	 i = 0;
  	 int rowCounter = 0;
  	 int evenCounter = 0;
  	 int oddCounter = 0;
  	for( ; i < allUsersUserIds.size(); i++ )
  	{
	  	if(i % 2 == 0 )	{
				
			%>  	
		  		<TR>	  	
		  		<td  align=center><%=i+1%></td><td>
		  	<% 	
		 } 	else{ 	 
		  		%>
		  			<td class=tHeader>&nbsp;</td><td align=center><%=i+1%></td><td>
		  		<%
		  		strDividerRow = "<tr><td colspan=2><HR></td><td class=tHeader>&nbsp;</td><td colspan=2><HR></td></tr>";
	  		 	
	  	}  	 	
	  	if( groupUsers.containsKey( allUsersUserIds.get(i)) ) {
  		%>
	  		<%=	printInputCheckbox( (String)allUsersUserIds.get(i), 
	  		(String)allUsersUserIds.get(i), true )%>
	  		<%=(String)allUsersNames.get(i)%>
	  		(<%=(String)allUsersUserIds.get(i)%>)
	  	<%
	  	}
	  	else{
	  	%>
	  		<%=	printInputCheckbox( (String)allUsersUserIds.get(i), 
	  		(String)allUsersUserIds.get(i), false )%>
	  		<%=(String)allUsersNames.get(i)%>
	  		(<%=(String)allUsersUserIds.get(i)%>)
  		<%
  		}
  		if(i % 2 == 0 )
		{
			%>  	
		  		</td>
		  	<%	  	
		 } 	else{ 	 
			%>  	
		  		</td></tr>
		  		<%=strDividerRow%>
		  	<%	  	 	
	  	}
  	}
	if(i % 2 != 0 ){
		%>  	
		<td>&nbsp;</td></tr>
		<%	
	}
 %>
	<tr><td colspan=5 align=center>
	<br>
		<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
		</td>
	</tr>
 </table>
 <br><br>
 <table align=center border=1 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
	  <tr><td align=center>
		<A HREF="/EmployeeGroups.jsp">View&nbsp;Employee Group List</A>
	  </td></tr>
</table>
  <%=printInputHidden( "action",  (strAction==null ? "insert":strAction ) )%> 
  <%=printInputHidden( "egid",  (strGroupId==null ? "-1":strGroupId ) )%>	
</form>
<BR>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

