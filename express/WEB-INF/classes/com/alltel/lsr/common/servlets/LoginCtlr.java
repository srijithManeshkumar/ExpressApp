package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;
import com.automation.dao.LSRdao;
import java.text.SimpleDateFormat;

public class LoginCtlr extends AlltelServlet {

	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception	{
		
		//This creates session if one doesnt exist and builds SDM if one doesnt exist
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
		
           	String transaction = request.getParameter("sevent");
		Log.write(Log.DEBUG, "LoginCtlr() transaction = " + transaction);
		if (transaction == null) {
			transaction = "login";
		}
	
		if (request.doDisplay())		//GET
		{
			if (transaction.equals("logout")) {

    				logOff(request, sdm);
				transaction = "login";
			}
				
	   		request.getHttpRequest().setAttribute("loginstat", "Please enter your username and password.");
			request.getHttpRequest().setAttribute("levent", transaction);
			alltelRequestDispatcher.forward(sdm.getLoginPage());
			return;
		}
		else
		{
			if(transaction.equals("Login")) {

			     String userId = request.getParameter("userid");
			     String passWd = request.getParameter("password");
			     
			     boolean isData = checkForContent(userId);
			     boolean isPass = checkForContent(passWd);
			     // if false return.

			     if(!isData || !isPass){
				Log.write("LoginCtlr() isData : false");
				request.getHttpRequest().setAttribute("levent", "login");
				request.getHttpRequest().setAttribute("loginstat", "You must enter a userid and a password.");
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     LoginProfileBean validLogin = getBean(userId, sdm);
			     
			     boolean goodLogin = validLogin.validateLogin(userId, passWd);
			    
			     if(!goodLogin){
				Log.write("LoginCtlr(): goodLogin : false");
				request.getHttpRequest().setAttribute("levent", "login");
				if (validLogin.isUserDisabled())
				{
					request.getHttpRequest().setAttribute("loginstat", "UserID has been disabled! Contact help desk");
				}
				else
				{
					request.getHttpRequest().setAttribute("loginstat", "User validation failed -- Check spelling and try again");
				}
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }

			     createSession(userId, validLogin, sdm);
			     request.putSessionDataManager(sdm);
			     
			     //See if user is reqd to change password now!
			     if(validLogin.doesUserHaveToChangePassword())
			     {
				Log.write(Log.DEBUG, "LoginCtlr() forcing passwd chg");
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "You are required to change password");
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }

 				/*                
               //start added for Q and V for 5072 satish
                
                LSRdao objLSRdao = new LSRdao();
                boolean holidayflag = false;
                Vector holidayVector = objLSRdao.loadHolidayTable();
                Calendar currentcal = Calendar.getInstance();
                String userType = sdm.getCompanyType();
                int currenthour = currentcal.get(currentcal.HOUR_OF_DAY);
                if ((currentcal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                        currentcal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                        holidayVector.contains(dateToStringYYMMDD(currentcal.getTime())))) {
                    holidayflag = true;
                }
                if (userType != null &&
                        (userType.equalsIgnoreCase("R") || userType.equalsIgnoreCase("C") || userType.equalsIgnoreCase("L"))) {
                    if ((currenthour >= 19 || currenthour < 7) || holidayflag) {
                        Log.write("=user (R or C or L)= logout between 7 pm -7am)");
                        logOff(request, sdm);
                        request.getHttpRequest().setAttribute("levent", "login");
                        if (holidayflag) {
                            request.getHttpRequest().setAttribute("loginstat", "You can not login  on holidays");
                        } else {
                            request.getHttpRequest().setAttribute("loginstat", "You can not login between 7 pm -7am time");
                        }
                        alltelRequestDispatcher.forward(sdm.getLoginPage());
                        return;
                    }
                }


                //end added for Q and V for 5072 satish
				*/
                
			     //alltelRequestDispatcher.forward("/MenuView.jsp?menunmbr=0");
                             Log.write("SUCCESSFULLY LOGGEDIN :: " + userId);
			     alltelRequestDispatcher.forward(sdm.getHomePage());
			     
			     return;
			    

			}
			else if (transaction.equals("Logout")) {

			   request.getHttpRequest().setAttribute("levent", "login");
			   request.getHttpRequest().setAttribute("loginstat", "loggedout");
			   alltelRequestDispatcher.forward(sdm.getLoginPage());
			   return;
			
			}
			else if (transaction.equals("ForgetPassword")) {

				String userId = request.getParameter("userid");
				boolean isData = checkForContent(userId);
				if(!isData){
					request.getHttpRequest().setAttribute("levent", "login");
					request.getHttpRequest().setAttribute("loginstat", "You must enter a userid.");
					alltelRequestDispatcher.forward(sdm.getLoginPage());
					return;
			     	}
				LoginProfileBean LPBean = getBean(userId, sdm); 

			   	String quest = LPBean.getPQuestion();
			   	request.getHttpRequest().setAttribute("levent", "forget");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
			  
			   	request.getHttpRequest().setAttribute("loginstat", "Please enter the requested infornation.");
			   	alltelRequestDispatcher.forward(sdm.getLoginPage());
			   
			   	return;
			
			}
			else if (transaction.equals("ChangePassword")) {

				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "  ");
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			}
			else if (transaction.equals("ChangeLogin")){
				
			     String userId = request.getParameter("userid");
			     String oldpass = request.getParameter("oldpassword");
			     String newpass = request.getParameter("newpassword1");
			     String newpassv = request.getParameter("newpassword2");
			     boolean isData = checkForContent(userId);
			     // if false return.

			     if(!isData){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "You must enter a userid.");
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     boolean isDataPass1 = checkForContent(newpass);
			     boolean isDataPass2 = checkForContent(newpassv);
			     boolean isDataPass3 = checkForContent(oldpass);
			     // if false return.

			     if(!isDataPass1 || !isDataPass2 || !isDataPass3){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "You must enter data in ALL password fields.");
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     
			     LoginProfileBean validLogin = getBean(userId, sdm);
			     
			     boolean pVer = newpass.equals(newpassv);
			     // if no match here, return.

			     if(!pVer){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "new passwords must match");
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     boolean chgLogin = validLogin.validateLogin(userId,oldpass);
			     if(!chgLogin){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "old password not valid");
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     boolean writeLogin = validLogin.changeLogin(userId, newpassv);
			     if(!writeLogin){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "Change attempt failed.");
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }

			     createSession(userId, validLogin, sdm);
			     request.putSessionDataManager(sdm);

			     alltelRequestDispatcher.forward(sdm.getHomePage());
			     
			     return;

			} 
			else if (transaction.equals("ResetLogin")){
				
			     String userId = request.getParameter("userid");
			     String answer = request.getParameter("answer");
			     String newpass = request.getParameter("newpassword1");
			     String newpassv = request.getParameter("newpassword2");
			     String quest = request.getParameter("secretquestion");
			     
			     boolean isAnswerEntered = checkForContent(answer);
			     boolean isDataPass1 = checkForContent(newpass);
			     boolean isDataPass2 = checkForContent(newpassv);
			     // if false return.

			     if(!isAnswerEntered){
				request.getHttpRequest().setAttribute("levent", "forget");
				request.getHttpRequest().setAttribute("loginstat", "Valid Answer must be entered");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     if(!isDataPass1 || !isDataPass2){
				request.getHttpRequest().setAttribute("levent", "forget");
				request.getHttpRequest().setAttribute("loginstat", "You must enter new password twice ");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }

			     boolean pVer = newpass.equals(newpassv);
			     // if no match here, return.
			     if(!pVer){
				request.getHttpRequest().setAttribute("levent", "forget");
				request.getHttpRequest().setAttribute("loginstat", "new passwords must match");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     
			     LoginProfileBean validLogin = getBean(userId, sdm);
			     if (!validLogin.doesUserExist())
			     {	//This user does even exist!
				request.getHttpRequest().setAttribute("levent", "login");
				request.getHttpRequest().setAttribute("loginstat", "User validation failed -- Check spelling and try again");
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;	
			     }

			     boolean isAnswer = validLogin.validateAnswer(userId, answer);
			     if(!isAnswer){
				request.getHttpRequest().setAttribute("levent", "forget");
				request.getHttpRequest().setAttribute("loginstat", "Your answer is incorrect");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     boolean writeLogin = validLogin.changeLogin(userId, newpass);
			     if(!writeLogin){
				request.getHttpRequest().setAttribute("levent", "pchange");
				request.getHttpRequest().setAttribute("loginstat", "Change attempt failed.");
			   	request.getHttpRequest().setAttribute("theUser", userId);
			   	request.getHttpRequest().setAttribute("theQuestion", quest);
			        alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			     }
			     
			     createSession(userId, validLogin, sdm);
			     request.putSessionDataManager(sdm);

			     alltelRequestDispatcher.forward(sdm.getHomePage());
			     
			     return;

			}
			else // unknown transaction
			{
				//should never reach here
				Log.write("LoginCtlr() Why am I here ");
				request.getHttpRequest().setAttribute("loginstat", "Please enter your username and password.");
				request.getHttpRequest().setAttribute("levent", transaction);
				alltelRequestDispatcher.forward(sdm.getLoginPage());
				return;
			}

		} // else

	}  // end of myservice()


	private LoginProfileBean getBean(String usr, SessionDataManager sdm)
	{
	     	LoginProfileBean lpBean = sdm.getLoginProfileBean();
		if (lpBean == null)
		{
			Log.write(Log.DEBUG, "LoginCtlr() sdm is null");
			lpBean = new LoginProfileBean();
		}
		else
		{
	     		lpBean.loadLoginProfileBean (usr);
		}

		return lpBean ;
	 
   	}

    
    	/**
     	* The createSession() method takes the user's valid credentials and puts them into the SessionDataManager
	* object.
     	* @param	usr	the user
     	* @param	lpBean	the LoginProfile bean
     	* @param	sdm	SessionDataManager object
     	*/
    	private void createSession(String usr,
				   LoginProfileBean lpBean,
				   SessionDataManager sdm)
	{

	     // All credentials are put into session via SessionDataManager 
	     sdm.setUser(usr);
	     sdm.setLoginProfileBean(lpBean);
	    
    	}

    	private void logOff(AlltelRequest request, SessionDataManager sdm) {
    
		String	strNextVarName = null;
		
		sdm.logoff();
		
		try {
			Enumeration enumuration = request.getSession().getAttributeNames();	//all session vars
			while (enumuration.hasMoreElements())
			{
				strNextVarName = (String)enumuration.nextElement();
				if (strNextVarName.compareTo("ActiveSessionTimeStamp") != 0) {
					Log.write(Log.DEBUG_VERBOSE, "LoginCtlr() logOff removing = " + strNextVarName);
					request.getSession().removeAttribute(strNextVarName);
				}
			}
		}
		catch(Exception e)
		{	//can happen if session already invalidated
		}
		
		request.putSessionDataManager(sdm);
	}

	private boolean checkForContent(String s1) {
		if ( (s1 == null) || (s1.length() <= 0) )
			return false;
		else
			return true;
	}

	protected void populateVariables()
		throws Exception {
	}


}
