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
	Log.write(Log.DEBUG_VERBOSE, "EmployeeGroups.jsp: Caught exception e=[" + e.getMessage() + "]");
}	

if( strGrpName == null && strAction != null )
	if(  strAction.equals( "edit" ) && iGroupId > -1 ){	 
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String strUserid =  sdm.getUser();
		int nType = 0;
		ResultSet rset = null;
		
		String query_all  = " select usr.USERID, usr.FRST_NM ||' ' || usr.LST_NM  from " 
			+ " USERID_T usr, company_t cp where "
			+ " usr.CMPNY_SQNC_NMBR = cp.CMPNY_SQNC_NMBR "
			+ " AND cp.CMPNY_TYP = ? order by 1 ";
		
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
		Log.write(Log.DEBUG_VERBOSE, "EmployeeGroups.jsp: Caught exception e=[" + e.getMessage() + "]");
	}
	finally {
		try {
			rset.close(); rset=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) { eee.printStackTrace();}
		DatabaseManager.releaseConnection(conn);
	}
	Log.write(Log.DEBUG_VERBOSE, query1 );
}
%>
<table align=center width="100%" cellspacing=0 cellpadding=0>
  <tr>
    <TH width="100%" align=center bgcolor="#7AABDE"><SPAN class="barheader">Employee Groups</SPAN> </th>
    </th>
  </tr>
</table>
<%
		conn = null;
		pstmt = null;
		String strUserid =  sdm.getUser();
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
		Log.write(Log.DEBUG_VERBOSE, "EmployeeGroups.jsp: Caught exception e=[" + e.getMessage() + "]");
	}
	finally {
		try {
			rset.close(); rset=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) { eee.printStackTrace();}
		DatabaseManager.releaseConnection(conn);
	}
%>
<!--********************** FORM SECTION ********************-->
<form action="EmployeeGroups.jsp" onSubmit="return beforeSubmitt();" onreset="return resetToDefault();" method="POST">
<table  align=center width="75%" cellspacing=2 cellpadding=1>
  <tr>
  	<td colspan=2 class=tHeader>
  		 <%=strGrpName%>
  	
  	</tr>
  	<tr>
  	<td colspan=2 class=tHeader2>
  		Description:<br>
  		 <%=strGrpDesc%>
  		 </td>  	
  	</tr>
  	<tr>
  	<td colspan=2 class=tHeader>
  		&nbsp;  	
  	</tr>
  	
  	<td colspan=2>
  		&nbsp;  	
  	</tr>
  	<tr>
  		<td class=rowheader>
  			Employee ID
   		</td>
   		<td class=rowheader>
  			Name
   		</td>	  	
  	</tr>
  	<%
  	int i = 0;
  	for( ; i <= allUsersUserIds.size(); i++ )
  	{
  	%>
  	<tr><td>
  	<%  	
  		if( groupUsers.containsKey( allUsersUserIds.at(i)) ) {
  	%>
  		<%=	printInputCheckbox( allUsersUserIds.at(i), 
  		(string)allUsersUserIds.at(i), true )%>
  	<%
  	}
  	else{
  	%>
  		<%=	printInputCheckbox( allUsersUserIds.at(i), 
  		(string)allUsersUserIds.at(i), false )%>
  	<%
  	}
  	%>
  	</td>
	  	<td>
	  	<%=(String)allUsersName.at(i), true )%>
  		</td>
  	</tr>
  <%
  }
 %>
	<tr><td colspan=2 align=center>
		<INPUT class=appButton TYPE="RESET"   value="Clear Form">&nbsp;&nbsp;&nbsp;&nbsp;
		<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
		</td>
	</tr>
 </table>
  <%=printInputHidden( "action",  (strAction==null ? "insert":strAction ) )%> 
  <%=printInputHidden( "egid",  (strGroupId==null ? "-1":strGroupId ) )%>	
</form>
<%
if( strAction!= null)
{

	conn = null;
	pstmt = null;
	int[]  iBatchCount = null;
	int nType = 0;
	String query = "";		
	String updateQry = " insert into USR_USRGRP_LINK_T ( "
		+ " USERID, USR_USRGRP_LINK_T )	VALUES( ?, ? ) ";	
	int iCounter = 0;
	
	String updateQryRemoveAll = " DELETE FROM USR_USRGRP_LINK_T "
		+ " WHERE USRGRP_EMP_SQNC_NMBR = ? ";	

	pstmt
	
	try {	
		
		pstmt = conn.prepareStatement( strUpdateQry  );
		int iAllUsersCounter = allUsersUserIds.size();
		int i = 0;
		String tempUserId =  "";		
		
		while( iAllUsersCounter > i )
		{
			tempUserId = (String) allUsersUserIds.at(i);
			pstmt.clearParameters();
			pstmt.setString( 1, tempUserId );
			pstmt.setInt( 2, iGroupId );
			pstmt.addBatch();
			i++;
		}
		
		if( i > 0 ){
			iBatchCount = pstmt.executeBatch(  );  
		}		

	} //try
	catch(Exception e) {
		Log.write(Log.DEBUG_VERBOSE, "EmployeeGroups.jsp: Caught exception e=[" + e + "]");
	}
	finally {
		try {
			rs.close(); rs=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) {}
		DatabaseManager.releaseConnection(con);
	}
}
%>

<BR>
<%
if( iBatchCount != null ){
	alltelRequestDispatcher.forward("/EmployeeGroups.jsp?msg=iBatchCount.length" );
}else{
	alltelRequestDispatcher.forward("/EmployeeGroups.jsp?msg=0" );
}
%>
</body>

<jsp:include page="i_footer.htm" flush="true" />
</html>

