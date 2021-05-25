/***************************************************************/
/* A L L T E L    C O P Y R I G H T    S T A T E M E N T        */
/****************************************************************/
/*                                                              */
/*  NOTICE: THIS SOFTWARE CONTAINS TRADE SECRETS THAT BELONG TO */
/*          ALLTEL INFORMATION SERVICES, INC. AND IS LICENSED   */
/*          BY AN AGREEMENT.  ANY UNAUTHORIZED ACCESS, USE,     */
/*          DUPLICATION OR DISCLOSURE IS UNLAWFUL.              */
/*                                                              */
/*  COPYRIGHT (C) 2004 ALLTEL COMMUNICATIONS, INC.         */
/*  ALL RIGHTS RESERVED.                                        */
/****************************************************************/
package com.alltel.lsr.common.util;

import java.io.*;
import java.util.*;
import javax.naming.*;

/**
 * The PropertiesManager class services requests for application properties. It
 * holds these properties in a standard Properties object. Since this class
 * expects to be invoked statically it always checks to make sure its Properties
 * object is not null. If it is null it will reload it from the application
 * properties file. It will look for this file in the applicaion server directory,
 * and it will expect the file name to have been passed in as the properties.file
 * application startup parameter ( -Dproperties.file=alltel.properties ). If this
 * parameter is not present it will use the hardcoded filename "hyper.properties".
 *
 * CHANGE HISTORY:
 *
 *  1/16/2004 pjs redid (cloned) for WL 7.*
 */
/*
* As per weblogic 12.2.1 - properties.file.path should be set in setDomainEnv.sh file in the JAVA_OPTIONS

Example:
JAVA_OPTIONS="${JAVA_OPTIONS} -Dproperties.file.path=/lsr00d10/wl_d26a/Oracle/Middleware/wlserver/props/express/lsr.properties -Durl=$T3URL"
export JAVA_OPTIONS
*/


public class PropertiesManager {


    static{
       Properties env = new Properties();
       String propsFilePath=System.getProperty("properties.file.path");
       Log.write("propsFile: " + propsFilePath);
       //System.out.println("propsFilePath11: " + propsFilePath);
       //propsFilePath="/lsr00d10/wl_d26a/Oracle/Middleware/wlserver/props/express/lsr.properties";
       //Log.write("propsFilePath222: " + propsFilePath);
       //System.out.println("propsFilePath222: " + propsFilePath);
       Context ctx = null;
       String url = System.getProperty("url");
       //String url="t3://vms122.windstream.com:7004";
       env.put(Context.PROVIDER_URL, url);
       env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");

       try
       {
           ctx = (Context) new InitialContext(env);
           Context subCtx = ctx.createSubcontext("properties");
           subCtx.rebind("lsr", propsFilePath);
       }
       catch(NamingException ne)
       {
           //Log.write("PropertiesManager.static: " + ne);
           System.out.println("PropertiesManager.static: " + ne);
       }
       finally
       {
           try
            {
                ctx.close();
            }
            catch(Exception e)
            {
                //Log.write("PropertiesManager.static: " + e);
            }
       }

    }

    /**
     * The PropertiesManager.getProperty() static method returns the value of a property
     * in the application properties file in the form of a String. It holds these values
     * in a standard Properties object. If this Properties object is null (either because
     * this is the first request or because it has been garbage collected) the class
     * reloads the properties from properties file read from the Weblogic server startup
     * parameters.
     *
     * @param   strPropertyName    property whose value is to be returned
     *
     * @return  string value for suplied property
     */
    public static String getProperty( String strPropertyName)
                                      throws Exception
    {
        loadProperties();
        return properties.getProperty(strPropertyName);
    }


    /**
     * This version of the getProperty() method accepts a default value and returns it if
     * the requested property is not found.
     *
     * @param      strPropertyName   property whose value is to be returned
     * @param      strDefaultValue   string value to be returned if property not found
     *
     * @exception  Exception         error accessing or loading properties
     *
     * @return     string value for suplied property
     */
    public static String getProperty( String strPropertyName, String strDefaultValue)
                                      throws Exception
    {
        loadProperties();
        return properties.getProperty(strPropertyName, strDefaultValue);
    }

   /**
    * Return current properties object
    */
    public static Properties getAllProperties()
        throws Exception
    {

        loadProperties();

        return properties;
    }


    /**
     * the PropertiesManager.getIntegerProperty() static method returns the value of a
     * property in the application properties file as an integer. If the internal
     * Properties object is null (either because this is the first request or because it
     * has been garbage collected) the class reloads the properties from properties file
     * read from the Weblogic server startup parameters.
     * NOTE: If the Property is not found (or is not a valid integer) the hardcoded
     * value of NO_SUCH_PROPERTY is returned. Callers should therefore always check
     * the return value against PropertiesManager.NO_SUCH_PROPERTY, or better yet,
     * call the second version of this method which takes an additional integer
     * parameter to use as a default value.
     *
     * @param      strPropertyName   property whose value is to be returned
     * @param      iDefaultValue     integer value to be returned if property not found
     *
     * @exception  Exception         error accessing or loading properties
     *
     * @return     integer value for suplied property
     */
    public static int getIntegerProperty( String strPropertyName)
                                          throws Exception
    {
       loadProperties();

       int iReturnVal;

       try
        {
            String strPropertyValue = properties.getProperty(strPropertyName);
            iReturnVal = Integer.parseInt(strPropertyValue);
        }
        catch (Exception e)
        {
            // NullPointerException if not found, NumberFormatException if not an integer
            iReturnVal = NO_SUCH_PROPERTY;
        }

        return iReturnVal;
    }


     /**
     * This version of the getIntegerProperty() method accepts a default value and returns
     * it if the requested integer property is not found.
     *
     * @param      strPropertyName   property whose value is to be returned
     *
     * @exception  Exception         error accessing or loading properties
     *
     * @return     integer value for suplied property
     */
    public static int getIntegerProperty( String strPropertyName, int iDefaultValue)
                                          throws Exception
    {

        loadProperties();
        int iReturnVal = iDefaultValue;   // return default value if property not found
        try
        {
            String strPropertyValue = properties.getProperty(strPropertyName);
            iReturnVal = Integer.parseInt(strPropertyValue);
        }
        catch (Exception e)
        {
            // NullPointerException if not found, NumberFormatException if not an
            // integer -- do nothing, method will return default value passed in
        }

        return iReturnVal;
    }


     /**
     * The getBooleanProperty() method returns true if the value in the properties file
     * for the supplied property name is "Y". It returns false otherwise.    <BR>
     * Note that in this scheme the caller will have no way of determining whether
     * the property was not found at all or whether it was found but had a value other
     * than "Y".
     *
     * @param      strPropertyName   property whose value is to be returned
     * @param      iDefaultValue     integer value to be returned if property not found
     *
     * @exception  Exception         error accessing or loading properties
     *
     * @return     integer value for suplied property
     */
    public static boolean getBooleanProperty( String strPropertyName, boolean bDefaultValue)
                                              throws Exception
    {
	boolean bReturnVal = bDefaultValue;

        loadProperties();
        // if property not found return default value
	if (null == properties.getProperty(strPropertyName)) 
		return bDefaultValue;
	// else return the boolean rep of the property true = "TrUe || YeS"
	try {
		if (properties.getProperty(strPropertyName).equalsIgnoreCase("Y")  || 
		    properties.getProperty(strPropertyName).equalsIgnoreCase("true"))
		{
			bReturnVal = true;   // property present in properties file with value "Y"
		}
		else
			bReturnVal =false;
	}
	catch (Exception e)
        {	//return default 
        }
		
	return bReturnVal;
		

    }


    /**
     * The loadProperties() private method opens the application properties file and loads
     * its contents into a standard Properties object. This method is synchronized because
     * we are writing to a class member of this static class.
     *
     * @exception   Exception   error accessing Tengah services or reading properties file
     *
     * @return      void
     */
    private static void loadProperties()
                                     throws Exception
    {
                 if (null == propertyfile) init();

		 if (propertyfile.lastModified() != lastmodified) {
			  debug("properties file : ["+propertyfile.getName()+"]");
			  FileInputStream propertiesFile = new FileInputStream(propertyfile);
			  properties = new Properties();
			  properties.load(propertiesFile);
			  propertiesFile.close();
			  // Load any subsequent includes found
			  String include = null;
			  int no=1;
			  for (include=properties.getProperty("include."+(no));
			  		 include !=null;
			  		 include=properties.getProperty("include."+(no))) {
				  no++;
				  File f = new File(include);
				  if (f.isFile()) {
					  FileInputStream subpropertiesFile = new FileInputStream(include);
					  properties.load(subpropertiesFile);
					  debug("Loaded SubProperty File :"+include);
					  subpropertiesFile.close();
				  }else {
					debug("Error: Loading SubProperty File :"+include+" Failed");
				  }
		  	  }
        	  debug("properties loaded");
        	  lastmodified=propertyfile.lastModified();
		 }
    }

    private static void init() throws Exception {

        String strPropertiesFileName=null;
        Context ctx = null;
        // try and get the property file name from jndi
        try {
           Properties env = new Properties();
           //String url="t3://vms122.windstream.com:7004";
           String url = System.getProperty("url");

           env.put(Context.PROVIDER_URL, url);
           env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");

           //Log.debug("PropertiesManager.init(): performing jndi lookup");

           ctx = (Context) new InitialContext(env).lookup("properties");
           strPropertiesFileName = (String)ctx.lookup(PROPERTIES_FILE_NAME);

           //Log.debug("PropertiesManager.init()-->props file name: " + strPropertiesFileName);
        } catch (Exception e) {
            /* ignore all exceptions */
            //Log.write("PropertiesManager.init(): " + e.getMessage());
            throw new Exception(e.toString());
        } finally {
            try {
                ctx.close();
            } catch (NamingException ne) {
                // do nothing
            }

        }

        // properties not found switch to default name
        if (null == strPropertiesFileName)
            strPropertiesFileName=DEFAULT_PROPERTIES_FILE_NAME;

        propertyfile = new File(strPropertiesFileName);
    }

    /**
     * Constructor for the PropertiesManager class. This is a utility class. single instance only
     */
     private  PropertiesManager(){}

     public static synchronized void setProperty(String property,String value) {
         properties.setProperty(property,value);
     }

    public static String getMessage(int messageno) { return getMessage(messageno,"General"); }

    public static String getMessage(int messageno,String msgid) { return getMessage(msgid+"."+messageno,0); }

    private static String getMessage(String msgid,int depth) {
        if (depth>1000) return "messageno<"+msgid+"> is invalid. Check for loop in the message file";
            String CONST = "Message.";
            String message = null;
            try {
                message = properties.getProperty(CONST+msgid);
                if (message!=null && message.startsWith(CONST)) {
                    String newmsgid = message.substring(CONST.length(),message.length());
                    message = getMessage(newmsgid,depth+1);
                }
                if (message==null) message = "["+msgid+"] is an invalid message no.Contact SysAdmin";
                    return message;
            } catch (Exception e) {
                return "messageno["+msgid+"] Message file corrupted.Contact SysAdmin";
            } catch (Error e) {
                return "messageno["+msgid+"] Message file corrupted.Contact SysAdmin";
        }
    }

    private final static void debug(String message) {
        System.out.println("PropertiesManager{debug}: "+message);
    }

    /**
     * Hardcoded application properties file name. The properties file name
     * should be passed in as an application startup parameter. If the parameter
     * is not found this hardcoded value is used instead.
     */
    private final static String DEFAULT_PROPERTIES_FILE_NAME = "lsr.properties";
    private final static String PROPERTIES_FILE_NAME = "lsr";

    /**
     * Value returned by the getIntegerProperty() method if a requested integer
     * property is not found. Callers should always check the return value from
     * the getIntegerProperty() method against PropertiesManager.NO_SUCH_PROPERTY.
     */
    public static final int NO_SUCH_PROPERTY = -1;

    /** Standard Properties object that holds the application properties. */
    private static Properties properties;

    private static long lastmodified=0;
    private static File propertyfile=null;

}  // end of PropertiesManager class
