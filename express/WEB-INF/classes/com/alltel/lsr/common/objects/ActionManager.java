/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	ActionManager.java
 * 
 * DESCRIPTION: Return Action Destination based on the Action_T table.
 * 
 * AUTHOR:      Syed Hussaini
 * 
 * DATE:        11-15-2002
 * 
 * HISTORY:
 *	6-1-2004 psedlak 
 */

/* $Log:     $
*/
/* $Revision:  $
*/


package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

import com.alltel.lsr.common.objects.Action;
import com.alltel.lsr.common.objects.Actions;

/*
 * This class holds the information for a navigation Action based on the rules 
 * from Action_T table. When a status change occurs in 
 * DslCtlr, RequestCtlr, PreorderCtlr or TicketCtlr then 
 * ActionManager.getActionDestination() method is called with appropiate
 * parameters necessary to build the hash key and access the Action object
 * from Actions Singleton and retrieve the destination jsp or servlet by
 * using Action.getActnDstntn().
*/
public class ActionManager {

    /** Creates a new instance of ActionManager */
    public ActionManager() {
    }
    
    public String getActionDestination(String strSttsCdFrom, String strTypInd, String strRqstTypCd, String strSttsCdTo, String strActn ) {
// get the instance to Actions Singleton object
        Actions m_instance = Actions.getInstance();
        Action objAction = (Action)m_instance.getAction(strSttsCdFrom,strTypInd,strRqstTypCd,strSttsCdTo,strActn, "0" );
        String strActnDstntn = objAction.getActnDstntn();
	Log.write(Log.DEBUG_VERBOSE, "ActionManager - Next action is: "+strActnDstntn);
        return strActnDstntn;
    }
    
    public String getActionNotification(String strSttsCdFrom, String strTypInd, String strRqstTypCd, String strSttsCdTo, String strActn ) {
// get the instance to Actions Singleton object
        Actions m_instance = Actions.getInstance();
        Action objAction = (Action)m_instance.getAction(strSttsCdFrom,strTypInd,strRqstTypCd,strSttsCdTo,strActn,  "0" );
        String strNtfySqncNmbr = objAction.getNtfySqncNmbr();
	//Log.write(Log.DEBUG_VERBOSE, "ActionManager - Notify["+ strNtfySqncNmbr +"]");
        return strNtfySqncNmbr;
    }
    
     /* EK. These function is used only in DWO files to support type controlled actions. And not imposing
      type restrictions on other Express products.
     */
      public String getActionDestinationDwo(String strSttsCdFrom, String strTypInd, String strRqstTypCd, String strSttsCdTo, String strActn, String strPrdTpCd ) {
        Actions m_instance = Actions.getInstance();
        Action objAction = (Action)m_instance.getAction(strSttsCdFrom,strTypInd,strRqstTypCd,strSttsCdTo,strActn, strPrdTpCd );
        String strActnDstntn = objAction.getActnDstntn();
		Log.write(Log.DEBUG_VERBOSE, "ActionManager - Next action is: "+strActnDstntn);
        return strActnDstntn;
    }
    
      public String getActionNotificationDwo(String strSttsCdFrom, String strTypInd, String strRqstTypCd, String strSttsCdTo, String strActn, String strPrdTpCd ) {
// get the instance to Actions Singleton object
        Actions m_instance = Actions.getInstance();
        Action objAction = (Action)m_instance.getAction(strSttsCdFrom,strTypInd,strRqstTypCd,strSttsCdTo,strActn, strPrdTpCd );
        String strNtfySqncNmbr = objAction.getNtfySqncNmbr();
	//Log.write(Log.DEBUG_VERBOSE, "ActionManager - Notify["+ strNtfySqncNmbr +"]");
        return strNtfySqncNmbr;
    }
    
}
