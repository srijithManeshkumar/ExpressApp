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
 * MODULE:	RequestCreateView.jsp	
 * 
 * DESCRIPTION: JSP View used to create new Requests
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 *	02/06/2008 HD0000002472840 Steve Korchnak defaulted options when only 1 value 
 *                            returned for request/service type selections
 *
 *   09/13/2007 Scott Culbertson HD2400817
 *              Replace:
 *                 strQuery = "SELECT OCN_STT_SQNC_NMBR, OCN_CD, STT_CD
 *                             FROM OCN_STATE_T WHERE OCN_CD IN
 *                                  (" + strInClause + ") ";
 *              With:
 *                 strQuery = "SELECT " +
 *                               "OCN_STT_SQNC_NMBR, " +
 *                               "OCN_CD, " +
 *                               "STT_CD " +
 *                            "FROM " +
 *                               "OCN_STATE_T " +
 *                            "WHERE " +
 *                               "DISPLAY_IND = 'Y' AND " +
 *                               "OCN_CD IN (" + strInClause + ")";
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestCreateView.jsv  $
/*
/*   Rev 1.3   09 Apr 2002 15:53:02   dmartz
/* 
/*
/*   Rev 1.2   21 Feb 2002 12:31:56   sedlak
/* 
/*
/*   Rev 1.1   13 Feb 2002 14:20:36   dmartz
/*Release 1.1
/*
/*   Rev 1.0   23 Jan 2002 11:06:22   wwoods
/*

/*Initial Checkin
*/

/* $Revision:   1.3  $
*/
%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%

//Does user belong here?
final String EXPRESS_FUNCTION = "CREATE_REQUESTS";
if (!sdm.isAuthorized(EXPRESS_FUNCTION))
{
	Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + EXPRESS_FUNCTION);
	alltelResponse.sendRedirect(SECURITY_URL);
}

Connection conRC = null;
Statement stmtRC = null;
ResultSet rsRC = null;

conRC = DatabaseManager.getConnection();
stmtRC = conRC.createStatement();


String m_strRqstNewErrorMsg = (String) request.getAttribute("rqstnew_errormsg");
if ((m_strRqstNewErrorMsg == null) || (m_strRqstNewErrorMsg.length() == 0))
{
	m_strRqstNewErrorMsg = "";
}

String m_strRqstPON = request.getParameter("rqstnew_pon");
if ((m_strRqstPON == null) || m_strRqstPON.length() == 0)
{
	m_strRqstPON = "";
}

String m_strOCNSttSqncNmbr = request.getParameter("rqstnew_ocnsttsqnc");
if ((m_strOCNSttSqncNmbr == null) || (m_strOCNSttSqncNmbr.length() == 0))
{
	m_strOCNSttSqncNmbr = "";
}

String m_strRqstSrvcTyp = request.getParameter("rqstnew_srvctyp");
if ((m_strRqstSrvcTyp == null) || (m_strRqstSrvcTyp.length() == 0))
{
	m_strRqstSrvcTyp = "";
}

String m_strRqstRqstTyp = request.getParameter("rqstnew_rqsttyp");
if ((m_strRqstRqstTyp == null) || (m_strRqstRqstTyp.length() == 0))
{
	m_strRqstRqstTyp = "";
}

String m_strRqstActvtyTyp = request.getParameter("rqstnew_actvtytyp");
if ((m_strRqstActvtyTyp == null) || (m_strRqstActvtyTyp.length() == 0))
{
	m_strRqstActvtyTyp = "";
}

%>

<SCRIPT LANGUAGE="JavaScript">

var arrRqstTypCd = new Array();
var arrSrvcTypMOXIDX = new Array();
var arrSrvcTypCd = new Array();
var arrActvtyTypMOXIDX = new Array();

var arrSrvcTypCd = new Array();
var arrSrvcTypDesc = new Array();
var arrActvtyTypCd = new Array();
var arrActvtyTypDesc = new Array();

<%
rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' AND SRVC_TYP_CD != '1' and srvc_typ_dscrptn != 'Simple Port Service Request' ORDER BY SRVC_TYP_CD ASC");

int stc = 0;
while (rsRC.next() == true)
{
%>
	arrSrvcTypCd[<%=stc%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
	arrSrvcTypDesc[<%=stc%>] = "<%=rsRC.getString("SRVC_TYP_DSCRPTN")%>";
<%
	stc++;
}
%>

<%
rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'R' AND ACTVTY_TYP_DSCRPTN != 'Simple Port' ORDER BY ACTVTY_TYP_CD ASC");

int at = 0;
while (rsRC.next() == true)
{
%>
	arrActvtyTypCd[<%=at%>] = "<%=rsRC.getString("ACTVTY_TYP_CD")%>";
	arrActvtyTypDesc[<%=at%>] = "<%=rsRC.getString("ACTVTY_TYP_DSCRPTN")%>";
<%
	at++;
}
%>

function setSrvcTypSelect(RqstTypControl, SrvcTypControl)
{
  // Clear the options in the Activity Type Select Control
  for (var z=SrvcTypControl.options.length ; z >= 1 ; z--) SrvcTypControl.options[z]=null;

  var SrvcTypeOption ;

  var NbrInList = 0;

  for (var x = 0 ; x < arrRqstTypCd.length  ; x++ )
  {
	if ( arrRqstTypCd[x] == RqstTypControl.value )
    {
	  for (var y = 0 ; y < arrSrvcTypCd.length  ; y++ )
	  {
	    if (arrSrvcTypMOXIDX[x].indexOf(arrSrvcTypCd[y]) >= 0) 
		{
			SrvcTypeOption = document.createElement("option") ;
			SrvcTypeOption.value = arrSrvcTypCd[y] ;
			SrvcTypeOption.text = arrSrvcTypDesc[y];
			SrvcTypControl.add(SrvcTypeOption) ;

			NbrInList++;


		}
	  }
	}
  }
  //if only one element, then default to it
  if (NbrInList == 1)
  {
	SrvcTypControl.options[1].selected = true;
	setActvtyTypSelect(SrvcTypControl, RequestCreateView.rqstnew_actvtytyp);
  }
}

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

<FORM NAME="RequestCreateView" METHOD=POST ACTION="RequestCtlr">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;Q&nbsp;U&nbsp;E&nbsp;S&nbsp;T</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%=m_strRqstNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Purchase Order#:&nbsp;</TD>
	<TD align=left><input type=text maxLength=16 size=20 name=rqstnew_pon value=<%=m_strRqstPON%>></TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>OCN&nbsp;State:&nbsp;</TD>
	<TD align=left><SELECT NAME="rqstnew_ocnsttsqnc">
		<option value="">... Select an OCN/State ...</OPTION>
	<%

	// Get All OCN Codes for this users user groups
	String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
		"(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
	rsRC = stmtRC.executeQuery(strQuery);

	String strInClause = "";
	while (rsRC.next() == true)
	{
		if (rsRC.getString("OCN_CD").equals("*"))
		{
			String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + rsRC.getInt("CMPNY_SQNC_NMBR");
			Statement stmtRC2 = null;
			stmtRC2 = conRC.createStatement();
			ResultSet rsRC2 = stmtRC2.executeQuery(strSubQuery);
			while (rsRC2.next() == true)
			{
				strInClause = strInClause + "'" + rsRC2.getString("OCN_CD") + "',";
			}
			rsRC2.close();
		}
		else
		{
			strInClause = strInClause + "'" + rsRC.getString("OCN_CD") + "',";
		}
	}
	rsRC.close();

	// strip off last comma
	if (strInClause.endsWith(","))  
		strInClause = strInClause.substring(0,strInClause.length()-1);

	if (strInClause.length() > 0)
	{
		// Get the rest of the data
      strQuery = "SELECT " +
                    "OCN_STT_SQNC_NMBR, " +
                    "OCN_CD, " +
                    "STT_CD " +
                 "FROM " +
                    "OCN_STATE_T " +
                 "WHERE " +
                    "DISPLAY_IND = 'Y' AND " +
                    "OCN_CD IN (" + strInClause + ")";

		rsRC = stmtRC.executeQuery(strQuery);
		while (rsRC.next() == true)
		{
	%>
			<option value=<%= rsRC.getString("OCN_STT_SQNC_NMBR") %> <% if ( m_strOCNSttSqncNmbr.equals(rsRC.getString("OCN_STT_SQNC_NMBR"))) { %> SELECTED <% } %> ><%= rsRC.getString("OCN_CD") %> - <%= rsRC.getString("STT_CD") %></OPTION>
	<%
		}
		rsRC.close();
	}

	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Request&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="rqstnew_rqsttyp" NAME="rqstnew_rqsttyp" onchange="setSrvcTypSelect(this, RequestCreateView.rqstnew_srvctyp);">
		<option value="">... Select a Request Type ...</option>
	<%
	rsRC = stmtRC.executeQuery("SELECT RQST_TYP_CD, RQST_TYP_DSCRPTN, SRVC_TYP_MOX_IDX FROM REQUEST_TYPE_T WHERE SRVC_TYP_MOX_IDX != ' ' AND RQST_TYP_CD != 'M' AND RQST_TYP_DSCRPTN != 'Simple Number Port Request' ORDER BY RQST_TYP_CD ASC");
	int rt = 0;
	while (rsRC.next() == true)
	{
	%>
		<option value=<%= rsRC.getString("RQST_TYP_CD") %> <% if ( m_strRqstRqstTyp.equals(rsRC.getString("RQST_TYP_CD"))) { %> SELECTED <% } %>><%= rsRC.getString("RQST_TYP_CD") %> - <%= rsRC.getString("RQST_TYP_DSCRPTN") %></option>

		<SCRIPT LANGUAGE="JavaScript">
		arrRqstTypCd[<%=rt%>] = "<%=rsRC.getString("RQST_TYP_CD")%>";
		arrSrvcTypMOXIDX[<%=rt%>] = "<%=rsRC.getString("SRVC_TYP_MOX_IDX")%>";
		</SCRIPT>
	<%
		rt++;
	}
	rsRC.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Service&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="rqstnew_srvctyp" NAME="rqstnew_srvctyp" onchange="setActvtyTypSelect(this, RequestCreateView.rqstnew_actvtytyp);">
		<option value="">... Select a Service Type ...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
	<%
	rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN, ACTVTY_TYP_MOX_IDX FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' AND SRVC_TYP_CD != '1' AND SRVC_TYP_DSCRPTN != 'Simple Port Service Request' ORDER BY SRVC_TYP_CD ASC");
	int st = 0;
	while (rsRC.next() == true)
	{
	%>
		<option value=<%= rsRC.getString("SRVC_TYP_CD") %> <% if ( m_strRqstSrvcTyp.equals(rsRC.getString("SRVC_TYP_CD"))) { %> SELECTED <% } %>><%= rsRC.getString("SRVC_TYP_CD") %> - <%= rsRC.getString("SRVC_TYP_DSCRPTN") %></option>

		<SCRIPT LANGUAGE="JavaScript">
		arrSrvcTypCd[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
		arrActvtyTypMOXIDX[<%=st%>] = "<%=rsRC.getString("ACTVTY_TYP_MOX_IDX")%>";
		</SCRIPT>
	<%
		st++;
	}
	rsRC.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Activity Type:&nbsp;</TD>
	<TD align=left>
		<SELECT id="rqstnew_actvtytyp" NAME="rqstnew_actvtytyp">
			<option value="" SELECTED>... Select an Activity Type ...</option>
		</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=center colspan=2>
	<INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="rqstnew" VALUE="Submit">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="rqstnew" VALUE="Cancel">
	</TD>
  </TR>
 
</TABLE>

</FORM>
<%
DatabaseManager.releaseConnection(conRC);
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

