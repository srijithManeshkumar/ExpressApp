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
 * MODULE:	DwoCreateView2.jsp	
 * 
 * DESCRIPTION: JSP View used to create new Data Work Orders-Business Data Products
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        7-14-2005
 * 
 * HISTORY:
 * 	pjs Cloned from DwoCreateView.jsp;  this is for BDP only, the orginal is for KPEN
 *		orders.  Cerated new TYP_IND of "X" for Bus Data Prods
 * 
 */
%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%

//Does user belong here?
final String EXPRESS_FUNCTION = "CREATE_BDPS";
final String BDP_QC = "Bdp";

if (!sdm.isAuthorized(EXPRESS_FUNCTION))
{
	Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + EXPRESS_FUNCTION);
	alltelResponse.sendRedirect(SECURITY_URL);
}

Connection conRC = null;
Statement stmtRC = null;
ResultSet rsRC = null;
boolean bKpen = true;

try { 

conRC = DatabaseManager.getConnection();
stmtRC = conRC.createStatement();

HttpSession objSession = alltelRequest.getSession();
String strPJVN = (String)objSession.getAttribute("DwOcHoIcE");
if (strPJVN == null) {  strPJVN=""; }
if ( !strPJVN.equals(BDP_QC) )
{       bKpen = false;
        Log.write(Log.WARNING, sdm.getUser() + " is hitting this page without order type set in session");
        alltelResponse.sendRedirect(SECURITY_URL);
}


String m_strDwoNewErrorMsg = (String) request.getAttribute("dwonew_errormsg");
if ((m_strDwoNewErrorMsg == null) || (m_strDwoNewErrorMsg.length() == 0))
{
	m_strDwoNewErrorMsg = "";
}

String m_strOCNCd = request.getParameter("dwonew_ocncd");
if ((m_strOCNCd == null) || (m_strOCNCd.length() == 0))
{
	m_strOCNCd = "";
	// For Sales Engineers, they dont pick a site for each order - they search for customers (from SIS)
	// So here, we're defaulting the OCN to the first available for Sales Engineers
	rsRC = stmtRC.executeQuery("SELECT OCN_CD FROM OCN_T O WHERE O.CMPNY_SQNC_NMBR=" + sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr() );
	rsRC.next();
	m_strOCNCd = rsRC.getString("OCN_CD");
	rsRC.close();
	alltelRequest.getHttpRequest().setAttribute("dwonew_ocncd", m_strOCNCd);
}

String m_strPrdctTyp = request.getParameter("dwonew_prdcttyp");
if ((m_strPrdctTyp == null) || (m_strPrdctTyp.length() == 0))
{
	m_strPrdctTyp = "";
}


String m_strDwoSrvcTyp = request.getParameter("dwonew_srvctyp");
if ((m_strDwoSrvcTyp == null) || (m_strDwoSrvcTyp.length() == 0))
{
	m_strDwoSrvcTyp = "";
}


String m_strDwoActTyp = request.getParameter("dwonew_acttyp");
if ((m_strDwoActTyp == null) || (m_strDwoActTyp.length() == 0))
{
	m_strDwoActTyp = "";
}

String m_strDwoSubActTyp = request.getParameter("dwonew_acttyp");
if ((m_strDwoSubActTyp == null) || (m_strDwoSubActTyp.length() == 0))
{
	m_strDwoSubActTyp = "";
}
String strSearchSeqNum = request.getParameter("seqnum" );
%>

<SCRIPT LANGUAGE="JavaScript">

var arrProductTypCd = new Array();
var arrProductTypMOXIDX = new Array();
var arrOrdrTypCd = new Array();
var arrChgTypDesc = new Array();
var arrChgTypMOXIDX = new Array();
var arrChgSubTypMOXIDX = new Array();

var arrActTypCd = new Array();
var arrActTypDesc = new Array();

var arrSubActTypCd = new Array();
var arrSubActTypDesc = new Array();

<%
rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN, SUB_ACTVTY_TYP_MOX_IDX FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY ACTVTY_TYP_CD ASC");
int iAct = 0;
while (rsRC.next() == true)
{
%>
        arrActTypCd[<%=iAct%>] = "<%=rsRC.getString("ACTVTY_TYP_CD")%>";
        arrActTypDesc[<%=iAct%>] = "<%=rsRC.getString("ACTVTY_TYP_DSCRPTN")%>";
        arrChgSubTypMOXIDX[<%=iAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_MOX_IDX")%>";
<%
	iAct++;
}
rsRC.close();

rsRC = stmtRC.executeQuery("SELECT SUB_ACTVTY_TYP_CD, SUB_ACTVTY_TYP_DSCRPTN FROM SUB_ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY SUB_ACTVTY_TYP_CD ASC");
int iSubAct = 0;
while (rsRC.next() == true)
{
%>
        arrSubActTypCd[<%=iSubAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_CD")%>";
        arrSubActTypDesc[<%=iSubAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_DSCRPTN")%>";
<%
	iSubAct++;
}
rsRC.close();
%>

function setOrderTypSelect(RqstTypControl, OrderTypControl)
{
  // Clear the options in the "Order Type" dropdown --- drive by MOX_IDX in PRODUCT_T table...
  for (var z=OrderTypControl.options.length ; z >= 1 ; z--) OrderTypControl.options[z]=null;
  var OrderTypeOption ;

  for (var x = 0 ; x < arrProductTypCd.length  ; x++ )
  {
        if ( arrProductTypCd[x] == RqstTypControl.value )
    {
          for (var y = 0 ; y < arrOrdrTypCd.length  ; y++ )
          {
            if (arrProductTypMOXIDX[x].indexOf(arrOrdrTypCd[y]) >= 0)
                {
                        OrderTypeOption = document.createElement("option") ;
                        OrderTypeOption.value = arrOrdrTypCd[y] ;
                        OrderTypeOption.text = arrChgTypDesc[y];
                        OrderTypControl.add(OrderTypeOption) ;
                }
          }
        }
  }
}

function setChgTypSelect(RqstTypControl, SrvcTypControl)
{
  // Clear the options in the "Change Type" dropdown
  for (var z=SrvcTypControl.options.length ; z >= 1 ; z--) SrvcTypControl.options[z]=null;

  var SrvcTypeOption ;

  for (var x = 0 ; x < arrOrdrTypCd.length  ; x++ )
  {
        if ( arrOrdrTypCd[x] == RqstTypControl.value )
    {
          for (var y = 0 ; y < arrActTypCd.length  ; y++ )
          {
            if (arrChgTypMOXIDX[x].indexOf(arrActTypCd[y]) >= 0)
                {
                        SrvcTypeOption = document.createElement("option") ;
                        SrvcTypeOption.value = arrActTypCd[y] ;
                        SrvcTypeOption.text = arrActTypDesc[y];
                        SrvcTypControl.add(SrvcTypeOption) ;
                }
          }
        }
  }
}

function setChgSubTypSelect(RqstTypControl, ActvtyTypControl)
{
  // Clear the options in the Activity Type Select Control
  for (var z=ActvtyTypControl.options.length ; z >= 1 ; z--) ActvtyTypControl.options[z]=null;

  var ActvtyTypeOption ;

  for (var x = 0 ; x < arrActTypCd.length  ; x++ )
  {
        if ( arrActTypCd[x] == RqstTypControl.value )
    {
          for (var y = 0 ; y < arrSubActTypCd.length  ; y++ )
          {
            if (arrChgSubTypMOXIDX[x].indexOf(arrSubActTypCd[y]) >= 0)
                {
                        ActvtyTypeOption = document.createElement("option") ;
                        ActvtyTypeOption.value = arrSubActTypCd[y] ;
                        ActvtyTypeOption.text = arrSubActTypDesc[y];
                        ActvtyTypControl.add(ActvtyTypeOption) ;
                }
          }
        }
  }
}

function checkValues()
{
  // some form edits
  var opt_val = window.document.DwoCreateView.dwonew_srvctyp.options[window.document.DwoCreateView.dwonew_srvctyp.selectedIndex].value;
  if (window.document.DwoCreateView.dwonew_ocncd.value == "New")
  {	//if new, then new order type
	if ( opt_val != "D" )
	{
		alert("Order type must be New for an Add New Site order");
		return false;
	}
  }
  else if (window.document.DwoCreateView.dwonew_ocncd.value == "")
  {	// Site must be picked
	alert("Site must be selected");
	return false;
  }
  if (opt_val == "")
  {	// Order type must be picked
	alert("Order Type must be selected");
	return false;
  }
  else if (window.document.DwoCreateView.dwonew_ocncd.value == "")
  {	// must pick a site for non-New order
	alert("Site must be selected for this Order Type");
	return false;
  }
  return true;
}

</SCRIPT>







<FORM NAME="DwoCreateView" METHOD=POST ACTION="DwoCtlr" onSubmit="return checkValues();">
<body onLoad="setOrderTypSelect(DwoCreateView.dwonew_prdcttyp, DwoCreateView.dwonew_srvctyp);">
<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR><TD align=center colspan=4><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R</SPAN></TD></TR>
  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR><TD colspan=4 align=center>&nbsp;<SPAN class="errormsg"><%=m_strDwoNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=4>&nbsp;</TD></TR>

  <TR><TD colspan=4>&nbsp;</TD></TR>
  <INPUT TYPE="HIDDEN" NAME="dwonew_ocncd" VALUE="<%=m_strOCNCd%>">

  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Product:&nbsp;</TD>
	<TD align=left>
	<SELECT id="dwonew_prdcttyp" NAME="dwonew_prdcttyp" onchange="setOrderTypSelect(this, DwoCreateView.dwonew_srvctyp);">
		<option value="">... Select a Product ...</option>
	<%

	rsRC = stmtRC.executeQuery("SELECT PRDCT_TYP_CD, PRDCT_DSCRPTN, SCRTY_OJBCT_CD, ORDR_TYP_MOX_IDX FROM PRODUCT_T WHERE TYP_IND = 'X' ORDER BY PRDCT_TYP_CD ASC");
	int pt = 0;
	while (rsRC.next() == true)
	//{	if (sdm.isAuthorized(rsRC.getString("SCRTY_OJBCT_CD")))
	{	if (EXPRESS_FUNCTION.equals( (rsRC.getString("SCRTY_OJBCT_CD"))))  //load only BDP choices -security was verified up top
		{
			Log.write(Log.DEBUG_VERBOSE," --loading product ="+rsRC.getString("PRDCT_DSCRPTN")+" mox:"+rsRC.getString("ORDR_TYP_MOX_IDX"));
	%>
			<option value=<%= rsRC.getString("PRDCT_TYP_CD") %> <% if ( (m_strPrdctTyp.equals(rsRC.getString("PRDCT_TYP_CD"))) || pt==0) { %> SELECTED <% } %>>&nbsp; <%= rsRC.getString("PRDCT_DSCRPTN") %></option>
			<SCRIPT LANGUAGE="JavaScript">
			arrProductTypCd[<%=pt%>] = "<%=rsRC.getString("PRDCT_TYP_CD")%>";
			arrProductTypMOXIDX[<%=pt%>] = "<%=rsRC.getString("ORDR_TYP_MOX_IDX")%>";
			</SCRIPT>

	<%
			pt++;
		}
	}
	rsRC.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=5>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Order&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="dwonew_srvctyp" NAME="dwonew_srvctyp" onchange="setChgTypSelect(this, DwoCreateView.dwonew_acttyp);">
		<option value="">... Select an Order Type ...</option>
	<%
	rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN, ACTVTY_TYP_MOX_IDX FROM SERVICE_TYPE_T WHERE TYP_IND = 'X' ORDER BY SRVC_TYP_CD ASC");
	int st = 0;
	while (rsRC.next() == true)
	{
	%>
		<option value=<%= rsRC.getString("SRVC_TYP_CD") %> <% if ( m_strDwoSrvcTyp.equals(rsRC.getString("SRVC_TYP_CD"))) { %> SELECTED <% } %>><%= rsRC.getString("SRVC_TYP_CD") %> - <%= rsRC.getString("SRVC_TYP_DSCRPTN") %></option>
		<SCRIPT LANGUAGE="JavaScript">
                arrOrdrTypCd[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
		arrChgTypDesc[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_DSCRPTN")%>";
                arrChgTypMOXIDX[<%=st%>] = "<%=rsRC.getString("ACTVTY_TYP_MOX_IDX")%>";
                </SCRIPT>

	<%
		st++;
	}
	rsRC.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;NOTE:&nbsp;&nbsp;&nbsp;</TD><TD align=left>Expedited Orders will result in additional charges.</TD></TR>


  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Change&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="dwonew_acttyp" NAME="dwonew_acttyp" onchange="setChgSubTypSelect(this, DwoCreateView.dwonew_actsubtyp);">
		<option value="">... Select a Change Type ...</option>
	<%
	rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY ACTVTY_TYP_CD ASC");
	iAct = 0;
	while (rsRC.next() == true)
	{
	%>
		<option value=<%= rsRC.getString("ACTVTY_TYP_CD") %> <% if ( m_strDwoActTyp.equals(rsRC.getString("ACTVTY_TYP_CD"))) { %> SELECTED <% } %>><%= rsRC.getString("ACTVTY_TYP_DSCRPTN") %></option>

	<%
		iAct++;
	}
	rsRC.close();
	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Change&nbsp;Sub&nbsp;Type:&nbsp;</TD>
	<TD align=left><SELECT id="dwonew_actsubtyp" NAME="dwonew_actsubtyp">
		<option value="">... Select a Change Sub-Type ...</option>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=4>&nbsp;</TD></TR>
  <TR>
	<TD align=center colspan=4>
	<INPUT TYPE="hidden" name="seqnum" value="<%=strSearchSeqNum%>">
	<INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="dwonew" VALUE="Submit">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="dwonew" VALUE="Cancel">
	</TD>
  </TR>
 
</TABLE>

</FORM>
<%
} // try 
catch (Exception e) {//apply log message to display exception here - Antony - 09/05/2012
    Log.write("Exception in DwoCreateView3.jsp : "+e.getMessage());

}
finally {
	stmtRC.close();
	stmtRC=null;
        
        //display connection object.toString here to ensure the logs show proof that the conn
        //is closed- Antony - 09/05/2012
    
        Log.write("Releasing connection object in DwoCreateView3.jsp for conn object: "+conRC.toString());
	DatabaseManager.releaseConnection(conRC);
        Log.write("After releasing connection object in DwoCreateView3.jsp.");   
	
}
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   $
/*
/*Initial Checkin
*/

/* $Revision:     $
*/
%>
