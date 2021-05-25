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
String strNewgroup =  alltelRequest.getParameter( "newgp" );
String strMethod = alltelRequest.getParameter( "mthd" );
String strCancel =  alltelRequest.getParameter( "CANCEL" );
strMethod =  strMethod == null ? "ps" :strMethod;
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
		String query1 = " Select USERGROUP_NAME,  USR_GRP_DSCRPTN  from  " 
			+ " USERGROUP_EMP_T WHERE USRGRP_EMP_SQNC_NMBR = ? ORDER BY 1 "; 
	 
	  try{
	  	conn = DatabaseManager.getConnection();
	  	pstmt = conn.prepareStatement( query1  );
		pstmt.clearParameters();
		pstmt.setInt( 1, iGroupId );
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
if( strNewgroup != null ){
%>
<!--********************** FORM SECTION ********************-->
<form action="EmployeeGroups.jsp" onSubmit="return beforeSubmitt();" onreset="return resetToDefault();" method="POST">
<table  align=center width="75%" cellspacing=2 cellpadding=1>
  <tr>
  	<td colspan=2 class=tHeader>
  		Add Employee Group Form
  	
  	</tr>
  	<tr>
  		<td class=rowheader>
  			Group Name:
   		</td>
	  	<td>
	  		<input type="TEXT" size="50" maxLength="49" NAME="grpname" VALUE="<%=(strGrpName==null?"":strGrpName )%>">
	  	</td>
  	</tr>
  	<tr>
  		<td class=rowheader>
  			Description
   		</td>
	  	<td>
	  		<TEXTAREA align=left valign=top maxLength=128 cols=50 rows=2 nowrap NAME="grpdesc"><%=(strGrpDesc==null?"":strGrpDesc )%></TEXTAREA>
	  	</td>
  	</tr>
	<tr><td colspan=2 align=center>		
		<INPUT class=appButton TYPE="RESET"   value="Clear Form">&nbsp;&nbsp;&nbsp;&nbsp;
		<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">
				
		</td>
	</tr>
	<tr><td colspan=2 align=center>				
			<A HREF="/EmployeeGroups.jsp"> Cancel </A>
	</td>
	</tr>
	
 </table>
 <%=printInputHidden( "action",  (strAction==null ? "insert":strAction ) )%> 
  <%=printInputHidden( "egid",  (strGroupId==null ? "-1":strGroupId ) )%>	
</form>
<%
}else{
%>
<table border=1 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
	  <tr><td align=center>
		<A HREF="/EmployeeGroups.jsp?newgp=new">&nbsp;Add&nbsp;NEW&nbsp;Employee Group&nbsp;</A>
	  </td></tr>
	</table>
<%
}

%>
<%
	
	Connection con = null;
	PreparedStatement pstmt = null;
	String strUserid =  sdm.getUser();
	int nType = 0;
	String query = "";	
	
	query = " Select USRGRP_EMP_SQNC_NMBR, USERGROUP_NAME,  USR_GRP_DSCRPTN  "
		+ "  FROM USERGROUP_EMP_T ORDER BY USERGROUP_NAME desc "; 
	
	String strInsertQry = " Insert into USERGROUP_EMP_T "
					+ " ( USERGROUP_NAME, USR_GRP_DSCRPTN, MDFD_USERID ) "
					+ " VALUES ( ?, ?, ? ) ";	
	
	String strUpdateQry = " UPDATE USERGROUP_EMP_T "
					+ " SET USERGROUP_NAME = ?, USR_GRP_DSCRPTN = ?, "
					+ " MDFD_USERID = ? WHERE  USRGRP_EMP_SQNC_NMBR = ? ";	 	
	
	String strDeleteQry = " DELETE FROM USERGROUP_EMP_T WHERE USRGRP_EMP_SQNC_NMBR = ? ";	 	
	
	ResultSet rs = null;	
	int iCounter = 0;
	try {
	
		con = DatabaseManager.getConnection();
	
		if( strMethod.equals("ps") ) {
			if( strAction != null ) {
				if( strAction.equals( "edit" ) ){
					pstmt = con.prepareStatement( strUpdateQry  );
					pstmt.clearParameters();
					pstmt.setString( 1, strGrpName.trim() );
					pstmt.setString( 2, strGrpDesc.trim() );
					pstmt.setString( 3, strUserid );
					pstmt.setInt( 4, iGroupId );
					int iRows = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;	
					Log.write(Log.DEBUG_VERBOSE, strUpdateQry  + " --------- 000" + strGrpName + "*******" + iRows );	
					if ( iRows > 0 ){
						strMsg = "Group information updated successful!"; 
					}
				}
				else if( strAction.equals( "delete" ) )
				{  					   
					pstmt = con.prepareStatement( strDeleteQry  );
					pstmt.clearParameters();
					pstmt.setInt( 1, iGroupId );
					int iRows = pstmt.executeUpdate();
					pstmt.close();
					pstmt = null;
					Log.write( Log.DEBUG_VERBOSE, strDeleteQry );
					if ( iRows > 0 ){
						strMsg = iRows + "Deleted!"; 
					}
				}
				else{
					if( ( strGrpName.length() > 1 ) && strAction.equals( "insert" ) ) {
						pstmt = con.prepareStatement( strInsertQry  );
						pstmt.clearParameters();
						pstmt.setString( 1, strGrpName.trim() );
						pstmt.setString( 2, strGrpDesc.trim() );
						pstmt.setString( 3, strUserid );
						int iRows = pstmt.executeUpdate();
						pstmt.close();
						pstmt = null;
						//Log.write(Log.DEBUG_VERBOSE, strInsertQry );
						if ( iRows > 0 ){
							strMsg = iRows + " Created successfully!"; 
						}
					}
				}
			}
		}
		pstmt = con.prepareStatement( query  );
		pstmt.clearParameters();
		//pstmt.setString( 1, strUserid );		
		rs = pstmt.executeQuery( );	
		//Log.write(Log.DEBUG_VERBOSE, query );
	%>
	<HR width=100%>
<center><div class=ExpressMsg>
<%=strMsg%>
</div>
</center>
	<TABLE width="90%" align="center" cellspacing="1" cellpadding="1">
			<TR class=tHeader><td width=20%>Group Name</td><td width=50% >Description</td><td width=30%>Actions</td></tr>
		<%	
		while( rs.next() == true )			
		{
			if(iCounter % 2 == 0 )
			{
				%>
				<TR class=roweven>
				<%
			}else
			{
				%>
					<TR class=rowodd>
				<%
			}
			%>
				
				<TD nowrap class=smaller align=left valign=top>
				<%=rs.getString(2)%>
				</td>
				<TD class=smallNote  align=left valign=top>
				<%=rs.getString(3)%>
				</td>
				<TD nowrap class=smaller align=center valign=top>
					 <A HREF="EmployeeGroups.jsp?egid=<%=rs.getString(1)%>&action=edit&mthd=gt&newgp=ext">Edit</a>&nbsp;|&nbsp;
					 <A HREF="EmployeeGroups.jsp?egid=<%=rs.getString(1)%>&action=delete" onclick="return confirm('Are you sure you want to delete?')">Delete</a>&nbsp;|&nbsp;
					 <A HREF="AddMembersToGrp.jsp?egid=<%=rs.getString(1)%>">Add/Remove/View Employees</a>
				</td>
				</TR>	
			<%
			iCounter++;
		} //while
		%>
			</TABLE>
			<BR CLEAR=ALL><BR>
	   <%
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

%>

<BR>

</body>
<script language = "JavaScript">

function displayMessage( ){
	document.write(
	'<%=strMsg%>' );
}

function beforeSubmitt()
{
	var m1 = document.forms[0].grpname.value;
	if( m1 == null || m1.length < 1 ){
		alert("Group Name is required, please enter the a group name" );
		document.forms[0].grpname.focus();
		return false;
	}
	
    return true;
   
}
function resetToDefault(){
	document.forms[0].action.value = 'insert';
	document.forms[0].grpdesc = '';
	document.forms[0].grpname = '';
}
</SCRIPT>
<jsp:include page="i_footer.htm" flush="true" />
</html>

