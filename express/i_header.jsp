<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	i_header.jsp	
 * 
 * DESCRIPTION: Common routines, menu header and security checks to be included in all
 *		Express jsp files.
 * 
 * AUTHOR:      
 * 
 * DATE:        01-02-2002
 * 
 * HISTORY:
 *	02/04/2002  psedlak Release 1.1 Added unlock Request functionality.
 *	10/02/2002  psedlak Added checkOnSubmit() to catch multiple form submissions.
 *	5/1/2003    psedlak put in generic locks
 */

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/i_header.jsv  $
/*
/*   Rev 1.13   Oct 03 2002 15:09:34   sedlak
/*Check multiple submits on forms (HD 142788)
/*
/*   Rev 1.12   Jun 12 2002 14:29:06   dmartz
/* 
/*
/*   Rev 1.10   May 30 2002 14:50:52   dmartz
/* 
/*
/*   Rev 1.7   09 Apr 2002 15:59:04   dmartz
/* 
/*
/*   Rev 1.6   20 Mar 2002 10:53:38   dmartz
/*Remove Ticket Locks
/*
/*   Rev 1.4   11 Feb 2002 09:05:36   sedlak
/*Release 1.1
/*
/*   Rev 1.3   01 Feb 2002 15:27:52   sedlak
/* 
/*
/*   Rev 1.2   31 Jan 2002 13:17:38   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 07:07:02   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:06   wwoods
/*Initial Checkin
*/

/* $Revision:   1.13  $
*/

%>

<%-- VALIDATE SECURITY HERE AND SEND USER TO LOGIN PAGE IF NO VALID LOGIN EXISTS FOR SESSION --%>

<%@ page
	language="java"
	import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
	session="true"
%>
<%            String conPath = request.getContextPath();
%>
<script type='text/javascript' src='<%=conPath%>/jquery.js'></script>
<script type='text/javascript' src='<%=conPath%>/json2.js'></script>
<%
	final String SECURITY_URL = "LsrSecurity.jsp";
	AlltelRequest alltelRequest = null; 
	AlltelResponse alltelResponse = null;
	SessionDataManager sdm = null;
	try 
	{
		alltelRequest = new AlltelRequest(request);
		alltelResponse = new AlltelResponse(response);
		sdm = alltelRequest.getSessionDataManager();
		if ( (sdm == null) || (!sdm.isUserLoggedIn()) ) 
		{
			alltelResponse.sendRedirect("LoginCtlr");
			return;
		}
	}
	catch (Exception e)
	{
		Log.write(Log.ERROR, e.getMessage());
		Log.write(Log.ERROR, "Trapped in i_header.jsp");
	}
	LoginProfileBean lpbean = sdm.getLoginProfileBean();
	MenuVector mvec = lpbean.getMenu(0); //0 is main menu
	Vector mivec = mvec.getMenuItemVector();
	MenuItem mitem;

        //Rel 1.1 If any requests are locked then unlock here -since user navigated to a new page.
        //If page is one of the exceptions defined in applications properties file, then don't unlock.
        if ( PropertiesManager.getProperty("lsr.keeplocks.view", "unlockAll").indexOf(alltelRequest.getURLNoBackSlash()) < 0 )
        {  	//need to unlock requests, etc.
		sdm.removeLocks();
        }
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<title>Windstream Express</title>
<SCRIPT LANGUAGE = "JavaScript">
var sessiontimer = null;

var secs = (<%= PropertiesManager.getProperty("lsr.inactivity.timeout", "45") %> * 60);
var alertMsg = "";

function showSessionTimer()
{
	if (secs==0)
	{
		window.status = "[SESSION EXPIRED]";

		alertMsg = "expressAlert.jsp?msg=" + "Your session has expired due to inactivity.<br>You will need to login again to perform any activity.<br>";
		showModelessDialog(alertMsg, window, "dialogWidth:500px;dialogHeight:120px;center:yes;status:no;resizable:no;help:no");
	}
	else
	{
		mi = Math.floor(secs/60);
		ss = secs % 60;
		if (ss < 10) 
		{
			ss = "0" + ss;
		}

		window.status = " [Session Timer " + mi + ":" + ss + "]";
		sessiontimer = self.setTimeout("showSessionTimer()", 1000)
		secs = secs - 1;

		if (secs == 120)
		{
			alertMsg = "expressAlert.jsp?msg=" + "Due to inactivity your Express Session will expire in 2 minutes.<br>Please perform an Express action, otherwise you will be required to log in again.<br>";

			showModelessDialog(alertMsg, window, "dialogWidth:500px;dialogHeight:120px;center:yes;status:no;resizable:no;help:no");
		}
	}
}

showSessionTimer();

</SCRIPT>
<style type="text/css">
<!--
#banner {width:100%; min-width:800px;} /* for most modern browsers */
#prop {width:800px;} /* IE work around */
-->
</style>
<head>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<table bgcolor="#ffffff"  width="2200" align=left border=0 cellspacing=0 cellpadding=1>
<tr>
	<td width="260" align=left><A NAME="topofpage"><img width="100%"src="images/winstreamexpress2.png">
        <div id=""prop><div id=""banner"></div></div>
        </td>

	<td width="75%" valign=center align=left>
        <div id=""prop><div id=""banner"></div></div>
	<table width="70%" border=0 cellspacing=0 cellpadding=5>

<%	
	for(int i=0; i <  mivec.size(); i++,i++)
	{
		if( i < mivec.size())
		{	mitem=(MenuItem)mivec.elementAt(i);
%>
		&nbsp;&nbsp;<A HREF="<%=mitem.getHyperlink()%>">&nbsp;<%=mitem.getMenuItemDescription()%></A>&nbsp;
<%
		}
		else
		{
%>
<%
		}
		if( (i+1) < mivec.size())
		{	mitem=(MenuItem)mivec.elementAt(i+1);
%>
		&nbsp;&nbsp;<A HREF="<%=mitem.getHyperlink()%>">&nbsp;<%=mitem.getMenuItemDescription()%></A>&nbsp;
<%
		}
		else
		{
%>
		<!--<tr><td>&nbsp;&nbsp;</td></tr> -->
<%
		}
%>

<%
	} //end of for()
%>
</td>
<td width="5%" valign=botom align=center>
&nbsp;&nbsp;<a href="ExpressHome.jsp">Home </A>
&nbsp;&nbsp;<A HREF="ExpressHomeArchive.jsp">Archived&nbsp;Messages&nbsp;</A>
UserID:&nbsp;<%=sdm.getUser()%></td></tr>
		</table>
	</td></tr>
</table>
	<BR CLEAR=ALL>
<img src="images/fadeBottom.gif" width="2200" height="10" border="0" />
<script language = "JavaScript">
<!-- hide me
function maxCheck(formName, elementDesc, element, maxlen)
{
	var q = eval("window.document." + formName + "." + element + ".value.length");
	if (q > maxlen)
	{
		var msg="Input field " + elementDesc + " has a maximum length of " + maxlen + " characters." +
			" Current value has " + q + " characters. Please reduce size or data will be truncated.";
		alert(msg);
		return false;
	}
	return true;
}

var submitcount=0;
function checkOnSubmit()
{
        if (submitcount ==0)
        {       submitcount=1;
                return true;
        }
        else
        {       alert("Please wait...Form was already submitted !!");
                return false;
        }
}

// show me -->
//Submit the form with large input data
function submitForm(_btn)
{
        //Code for LR DD field validation
	var srvcTyp = "<%=sdm.getSerTyp()%>";
	var actvtTyp = "<%=sdm.getActTyp()%>";
		
		if ($('.lrSerTyp').val() == 'C' && $('.lrActTyp').val() == 'V' && typeof $('.lrDDval').val() != 'undefined') {
			var lrValid = ddValidateFunction($('.lrDDval').val());
			if (lrValid) {
				return false;
			}
		}else if(srvcTyp == 'C' && actvtTyp == 'V' &&  typeof $('.lrDDval').val() != 'undefined'){
			var lrValid = ddValidateFunction($('.lrDDval').val());
			if (lrValid) {
				return false;
			}
		}
                
        if(!checkOnSubmit()){
            return false;
	}
	var globalParam = {};
		//For IE Compatability
		if (!String.prototype.startsWith) {
		  	String.prototype.startsWith = function(searchString, position) {
		    position = position || 0;
		    return this.indexOf(searchString, position) === position;
		  		};
			}
			
			if (!Object.keys) {
 				 Object.keys = function(obj) {
    			 var keys = [];

			    for (var i in obj) {
			      if (obj.hasOwnProperty(i)) {
			        keys.push(i);
			      }
			    }

    			return keys;
			  };
			}
                        
	//Replacing the XSS characters		
	String.prototype.replaceAll = function(search, replacement) {
            var target = this;
            return target.replace(new RegExp(search, 'g'), replacement);
	};
        
        $.fn.serializeObject = function(f) {
        	    var o = {};
        	    
        	    var a = f.serializeArray();
        	    $.each(a, function() {
        	        if (o[this.name]) {
        	            if (!o[this.name].push) {
        	                o[this.name] = [o[this.name]];
        	            }
        	            o[this.name].push(this.value || '');
        	        } else {
        	        
	        	        if(this.name.startsWith('_FF_') || 'notestext'==this.name){
	        	        
                                o[this.name] = this.value || '';
	        	        
	        	        }else{
	        	            globalParam[this.name] = this.value || '';
	        	            }
        	        }
        	    });
                    o[_btn.name] = _btn.value;
        	    return o;
        	};
    
        var newForm = document.createElement("form");
        newForm.setAttribute('method',"post");
        var jsonString;
        if($('form[name="DefaultValuesForm"]').attr('name') == "DefaultValuesForm"){
        newForm.setAttribute('action',"DefaultValuesCtlr");
        newForm.setAttribute('name',"DefaultValuesNewForm");
        
        jsonString = JSON.stringify($(this).serializeObject($('form[name="DefaultValuesForm"]')));
        }else{
        newForm.setAttribute('action',"RequestCtlr");
        newForm.setAttribute('id',"ExpressNewFormView");
        
        jsonString = JSON.stringify($(this).serializeObject($("#ExpressFormView")));
        }
        
        // Fix for XSS attacks characters
	jsonString = jsonString.replaceAll("'", "&0x27;");
        jsonString = jsonString.replaceAll("INTO", "&0x28;");
	jsonString = jsonString.replaceAll("into", "&0x29;");
        jsonString = jsonString.replaceAll("#", "&0x23;");
        
        var jsonExp = document.createElement("input");
        jsonExp.setAttribute('type',"hidden");
        jsonExp.setAttribute('name',"expJsonData");
        jsonExp.setAttribute('value',jsonString);
      
        newForm.appendChild(jsonExp);
        
        
        for(var key in globalParam) {
        
     	var globalElem = document.createElement("input");
        globalElem.setAttribute('type',"hidden");
        globalElem.setAttribute('name',key);
        globalElem.setAttribute('value',globalParam[key]);
        
        newForm.appendChild(globalElem);
		}
        
        document.body.appendChild(newForm);
        newForm.submit();
        document.body.removeChild(newForm);
        
        return true;
 }
 
//Replace XSS attacks Characters for ExpressFormView
function replaceXSSChars(){
    var expForm = document.getElementById('ExpressFormView');
    if(expForm){
        var elements = expForm.elements;
        if(elements){
		
        //Replacing the XSS attacks characters		
        String.prototype.replaceAll = function(search, replacement) {
        var target = this;
	     return target.replace(new RegExp(search, 'g'), replacement);
	};
		
	for(var i = 0; i < elements.length; i++) {
            if((elements[i].value) && ((elements[i].value.indexOf("&0x27;") > -1) || (elements[i].value.indexOf("&0x28;") > -1) || (elements[i].value.indexOf("&0x29;") > -1))) {
		//Decoding the XSS attacks Characters
		elements[i].value = elements[i].value.replaceAll("&0x27;", "'");
                elements[i].value = elements[i].value.replaceAll("&0x28;", "INTO");
		elements[i].value = elements[i].value.replaceAll("&0x29;", "into");
            }
	}
      }
    }
}
$(document).ready(function(){
    replaceXSSChars();
});
</script>

<BR CLEAR=ALL>
