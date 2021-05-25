Description<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "ReasonCodeCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="reasoncodebean" scope="request" class="com.alltel.lsr.common.objects.ReasonCodeBean" />

<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;

con = DatabaseManager.getConnection();
stmt = con.createStatement();

String m_strRqstSrvcTyp = request.getParameter("SRVC_TYP_DSCRPTN");
if ((m_strRqstSrvcTyp == null) || (m_strRqstSrvcTyp.length() == 0))
{
	m_strRqstSrvcTyp = "";
}

%>

<SCRIPT LANGUAGE="JavaScript">

var arrActvtyTypMOXIDX = new Array();

var arrSrvcTypCd = new Array();
var arrSrvcTypDesc = new Array();
var arrActvtyTypCd = new Array();
var arrActvtyTypDesc = new Array();

<%
rs = stmt.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' ORDER BY SRVC_TYP_CD ASC");

int stc = 0;
while (rs.next() == true)
{
%>
	arrSrvcTypCd[<%=stc%>] = "<%=rs.getString("SRVC_TYP_CD")%>";
	arrSrvcTypDesc[<%=stc%>] = "<%=rs.getString("SRVC_TYP_DSCRPTN")%>";
<%
	stc++;
}
%>

<%
rs = stmt.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'R' ORDER BY ACTVTY_TYP_CD ASC");

int at = 0;
while (rs.next() == true)
{
%>
	arrActvtyTypCd[<%=at%>] = "<%=rs.getString("ACTVTY_TYP_CD")%>";
	arrActvtyTypDesc[<%=at%>] = "<%=rs.getString("ACTVTY_TYP_DSCRPTN")%>";
<%
	at++;
}
%>

function setActvtyTypSelect(RqstTypControl, ActvtyTypControl)
{
  // Clear the options in the Activity Type Select Control
  for (var z=ActvtyTypControl.options.length ; z >= 1 ; z--) ActvtyTypControl.options[z]=null;

  var ActvtyTypeOption ;

  var NbrInList = 0;

  for (var x = 0 ; x < arrSrvcTypCd.length  ; x++ )
  {
	if ( arrSrvcTypCd[x] == RqstTypControl.value )
    {
	  for (var y = 0 ; y < arrActvtyTypCd.length  ; y++ )
	  {
	    if (arrActvtyTypMOXIDX[x].indexOf(arrActvtyTypCd[y]) >= 0) 
		{
			ActvtyTypeOption = document.createElement("option") ;
			ActvtyTypeOption.value = arrActvtyTypCd[y] ;
			ActvtyTypeOption.text = arrActvtyTypDesc[y];
			ActvtyTypControl.add(ActvtyTypeOption) ;

			NbrInList++;

		}
	  }
	}
  }

  //if only one element, then default to it
  if (NbrInList == 1)
  {
	ActvtyTypControl.options[1].selected = true;
  }

}

</SCRIPT>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Reason&nbsp;Code&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form NAME="ReasonCodeView" action="ReasonCodeCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= reasoncodebean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<input type="hidden" size=17 maxLength=15 NAME="RSN_CD_SQNC_NMBR" VALUE="<%= reasoncodebean.getRsnCdSqncNmbr() %>">

<%	if (reasoncodebean.getDbAction().equals("get") ||
	    reasoncodebean.getDbAction().equals("UpdateRow") ||
	    reasoncodebean.getDbAction().equals("DeleteRow"))
	{
%>

<input type="hidden" NAME="RSN_CD" VALUE="<%= reasoncodebean.getRsnCd() %>">
		<tr>
		<td align=right>Code&nbsp;Type:</td> 
		<td align=left><%= reasoncodebean.getRsnCd() %></td>
		</tr>
	
<%	}
	else
	{
%>

<tr>
<td align=right>Code&nbsp;Type:</td>
<td align=left><SELECT NAME="RSN_CD">
<option value="">... Select a Code Type ...</option>

<%
     rs = stmt.executeQuery("SELECT RSN_CD, RSN_CD_TYP_DSCRPTN FROM CODE_TYPE_T ORDER BY RSN_CD_TYP_DSCRPTN ASC");

while (rs.next() == true)
{
	if (reasoncodebean.getRsnCd().equals(rs.getString("RSN_CD")))
	{
%>
		<option value=<%= rs.getString("RSN_CD") %> SELECTED><%= rs.getString("RSN_CD_TYP_DSCRPTN") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("RSN_CD") %>><%= rs.getString("RSN_CD_TYP_DSCRPTN") %>
<%
	}
}
rs.close();
%>
</SELECT>
</td>
</tr>
<%	}
%>

<tr>
<td align=right>Reason&nbsp;Code:</td>
<td align=left><SELECT NAME="RSN_CD_TYP">
<option value="">... Select a Reason Code ...</option>

<%
     rs = stmt.executeQuery("SELECT RSN_CD_TYP, RSN_CD_TYP_DSCRPTN FROM REASON_CODE_TYPE_T ORDER BY RSN_CD_TYP_DSCRPTN ASC");

while (rs.next() == true)
{
	if (reasoncodebean.getRsnCdTyp().equals(rs.getString("RSN_CD_TYP")))
	{
%>
		<option value=<%= rs.getString("RSN_CD_TYP") %> SELECTED><%= rs.getString("RSN_CD_TYP_DSCRPTN") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("RSN_CD_TYP") %>><%= rs.getString("RSN_CD_TYP_DSCRPTN") %>
<%
	}
}
rs.close();
%>
</SELECT>
</td>
</tr>

<tr>
<td align=right><%= reasoncodebean.getRsnCdDscrptnSPANStart() %>Reason&nbsp;Code&nbsp;Description:<%= reasoncodebean.getRsnCdDscrptnSPANEnd() %></td>
<td align=left><input type="TEXT" size=112 maxLength=110 NAME="RSN_CD_DSCRPTN" VALUE="<%= reasoncodebean.getRsnCdDscrptn() %>"></td>
</tr>
  
<%	if (reasoncodebean.getDbAction().equals("get") ||
	    reasoncodebean.getDbAction().equals("UpdateRow") ||
	    reasoncodebean.getDbAction().equals("DeleteRow"))
	{
%>

<input type="hidden" NAME="SRVC_TYP_DSCRPTN" VALUE="<%= reasoncodebean.getSrvcTypDscrptn() %>">

		<tr>
		<td align=right>Service&nbsp;Type:</td> 
		<td align=left><%= reasoncodebean.getSrvcTypDscrptn() %></td>
		</tr>
		
<input type="hidden" NAME="ACTVTY_TYP_DSCRPTN" VALUE="<%= reasoncodebean.getActvtyTypDscrptn() %>">

		<tr>
		<td align=right>Activity&nbsp;Type:</td> 
		<td align=left><%= reasoncodebean.getActvtyTypDscrptn() %></td>
		</tr>
	
<%	}
	else
	{
%>

  <TR>
	<TD align=right>Service&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="SRVC_TYP_DSCRPTN" NAME="SRVC_TYP_DSCRPTN" onchange="setActvtyTypSelect(this, ReasonCodeView.ACTVTY_TYP_DSCRPTN);">
		<option value="">... Select a Service Type ...</option>
	<%
	rs = stmt.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN, AUTO_ACTVTY_TYP_MOX_IDX FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' AND AUTO_FLAG = 'Y' ORDER BY SRVC_TYP_CD ASC");
	int st = 0;
	while (rs.next() == true)
	{
	%>
		<option value=<%= rs.getString("SRVC_TYP_CD") %> <% if ( m_strRqstSrvcTyp.equals(rs.getString("SRVC_TYP_CD"))) { %> SELECTED <% } %>><%= rs.getString("SRVC_TYP_DSCRPTN") %></option>

		<SCRIPT LANGUAGE="JavaScript">
		arrSrvcTypCd[<%=st%>] = "<%=rs.getString("SRVC_TYP_CD")%>";
		arrActvtyTypMOXIDX[<%=st%>] = "<%=rs.getString("AUTO_ACTVTY_TYP_MOX_IDX")%>";
		</SCRIPT>
	<%
		st++;
	}
	rs.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR>
	<TD align=right>Activity Type:&nbsp;</TD>
	<TD align=left>
		<SELECT id="ACTVTY_TYP_DSCRPTN" NAME="ACTVTY_TYP_DSCRPTN">
			<option value="" SELECTED>... Select an Activity Type ...</option>
		</SELECT>
	</TD>
  </TR>
<%	}
%>
  
<%	if (reasoncodebean.getDbAction().equals("get") ||
	    reasoncodebean.getDbAction().equals("UpdateRow") ||
	    reasoncodebean.getDbAction().equals("DeleteRow"))
	{
%>

<input type="hidden" NAME="FRM_CD" VALUE="<%= reasoncodebean.getFrmCd() %>">

		<tr>
		<td align=right>Form&nbsp;Name:</td> 
		<td align=left><%= reasoncodebean.getFrmCd() %></td>
		</tr>
	
<%	}
	else
	{
%>
  
<tr>
<td align=right>Form Name:&nbsp;</td>
<td align=left><SELECT NAME="FRM_CD">
<option value="">... Select a Form Name ...</option>

<%
     rs = stmt.executeQuery("SELECT FRM_CD, FRM_DSCRPTN FROM FORM_NAME_T ORDER BY FRM_CD ASC");

while (rs.next() == true)
{
	if (reasoncodebean.getFrmCd().equals(rs.getString("FRM_CD")))
	{
%>
		<option value=<%= rs.getString("FRM_CD") %> SELECTED><%= rs.getString("FRM_DSCRPTN") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("FRM_CD") %>><%= rs.getString("FRM_DSCRPTN") %>
<%
	}
}
rs.close();
%>
</SELECT>
</td>
</tr>

<%	}
%>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= reasoncodebean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= reasoncodebean.getMdfdDt() %>">

<%	if (reasoncodebean.getDbAction().equals("get") ||
	    reasoncodebean.getDbAction().equals("UpdateRow") ||
	    reasoncodebean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= reasoncodebean.getMdfdDt() %></td>
		</tr>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= reasoncodebean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (reasoncodebean.getDbAction().equals("new") ||
	    reasoncodebean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (reasoncodebean.getDbAction().equals("get") ||
	         reasoncodebean.getDbAction().equals("UpdateRow") ||
	         reasoncodebean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(reasoncodebean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		
		if (sdm.isAuthorized(reasoncodebean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
<%
		}
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
%>

</td>
</tr>

</table>

</form>
<%
DatabaseManager.releaseConnection(con);
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/ReasonCodeView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:24:06   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:16   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
