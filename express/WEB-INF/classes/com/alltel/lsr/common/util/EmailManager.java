package com.alltel.lsr.common.util;


import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.* ;
import java.io.* ;

import weblogic.common.*;


/**
 * The EmailManager class provides the EmailManager.send() method, which is
 * used to invoke javamail's mail API. This API requires the name of
 * the SMTP mail host, and the send() method of this class will ask the
 * PropertiesManager class for it.
 *
 * CHANGE HISTORY:
 *
 * 09/07/01 KBJ260  Converted from WebLogic's depricated sendMail API.
 * 07/24/02 Epay7.0  add sending attachment functionality
 * 02/21/03 AXM275	ePay 10.0	- created method sendMultipleAttachments() to send
 *					an email with multiple attachments;
 *					modified sendAttachment() to call sendMultipleAttachments()
 */

public class EmailManager {

  // if no From address is supplied AND the EmailManager.DefaultFromAddress
  // application property is not present this From address will be used
  private final static String DEFAULT_FROM_ADDRESS = " ";

  // if no email subject header is supplied AND the
  // EmailManager.DefaultSubjectHeader application property is not present
  // this value will be used
  private final static String DEFAULT_SUBJECT_HEADER = " ";


  /**
   * The EmailManager.send() method invokes javamail's mail API. This API
   * call requires a From and To address for the email, as well as the email
   * subject header and message body. It also requires an SMTP host name, which
   * this method will read from the EmailManager.SMTPHostName application
   * properties entry. If a To address is not provided the method will throw
   * an exception. If the From address is not provided the method will use the
   * EmailManager.DefaultFromAddress application property value. This value
   * should be a valid email address in environments where return-receipts will
   * be generated for all emails sent out. If this value is not found a blank
   * space will be used so as not to pass null values to Weblogic's sendmail API.
   * Similarly, if no subject header for the email is supplied the application
   * will query the EmailManager.DefaultSubjectHeader property value and use
   * a blank space if it is not present
   */

  public static void send(String strFromAddress, String strToAddress,
                          String strSubjectHdr, String strMsgBody)
      throws Exception
  {
    String strSMTPHostName = PropertiesManager.getProperty("EmailManager.SMTPHostName");

    send(strSMTPHostName, strFromAddress, strToAddress, strSubjectHdr, strMsgBody);
  }

  /**
   * Same as the previous send except the SMTP Host Name must be supplied.
   */

  public static void send(String strSMTPHostName, String strFromAddress, String strToAddress,
                          String strSubjectHdr, String strMsgBody)
      throws Exception
  {
    if (strToAddress == null) {
      // throw exception if To address not supplied
      throw new Exception("EmailManager: No destination email address supplied.");
    }

    if (strSMTPHostName == null) {
      // throw exception if SMTP host name not found
      throw new Exception("EmailManager: SMTP Host Name not found.");
    }

    if (strFromAddress == null) {
      // use application's default FROM address, or blank space if not found
      strFromAddress = PropertiesManager.getProperty("EmailManager.DefaultFromAddress",
          DEFAULT_FROM_ADDRESS);
    }

    if (strSubjectHdr == null) {
      // use application's default Subject Header, or blank space if not found
      strSubjectHdr = PropertiesManager.getProperty("EmailManager.DefaultSubjectHeader",
          DEFAULT_SUBJECT_HEADER);
    }

    // sendAll can handle multiple recipients, but send can only handle one.
    String recipients[] = new String[1];
    recipients[0] = strToAddress;

    sendAll(strSMTPHostName, strFromAddress, recipients, strSubjectHdr, strMsgBody);
  }


  public static void sendAll(String host, String from, String recipients[],
                             String subject,	String message)
      throws Exception
  {
    boolean debug = false;

    try
    {
      //Set the host smtp address
      Properties props = new Properties();
      props.put("mail.smtp.host", host);

      // create some properties and get the default Session
      Session session = Session.getDefaultInstance(props, null);
      session.setDebug(debug);

      // create a message
      Message msg = new MimeMessage(session);

      // set the from and to address
      InternetAddress addressFrom = new InternetAddress(from);
      msg.setFrom(addressFrom);


      InternetAddress[] addressTo = new InternetAddress[recipients.length];
      for (int i = 0; i < recipients.length; i++)
      {
        addressTo[i] = new InternetAddress(recipients[i]);
      }
      msg.setRecipients(Message.RecipientType.TO, addressTo);


      // Setting the Subject and Content Type
      msg.setSubject(subject);
      msg.setContent(message, "text/plain");
      Transport.send(msg);
    }
    catch (MessagingException me)
    {
      throw new Exception("EmailManager: " + me);
    }
  }



  //send the attachement
  public static void sendAttachment(String host, String from, String recipients[],
                                    String subject,	String message,String filename,String displayName)throws Exception

  {


    String [] m_filename = {filename};
    String [] m_displayName = {displayName};

    sendMultipleAttachments(host, from, recipients,
                                      subject,	message, m_filename, m_displayName);


  }

	/**
		 * This sends multiple attachments in an email
		 * @author Ayalah Moshay
		 * @return	void
	 */


   public static void sendMultipleAttachments(String host, String from, String recipients[],
                                      String subject,	String message,String filename[],String displayName[])throws Exception

    {

      try
      {
        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", host);

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        //session.setDebug(debug);

        // create a message
        Message msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        //set recipients
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++)
        {
          addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);


        // Setting the Subject
        msg.setSubject(subject);

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        messageBodyPart.setText(message);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // add attachment
        for (int iFile = 0; iFile < filename.length; iFile++)
        {
        	messageBodyPart = new MimeBodyPart();
        	DataSource source = new FileDataSource(filename[iFile]);
        	messageBodyPart.setDataHandler(new DataHandler(source));
        	messageBodyPart.setFileName(displayName[iFile]);
        	multipart.addBodyPart(messageBodyPart);
		}

        // Put parts in message
        msg.setContent(multipart);
        //send message
        Transport.send(msg);
      }
      catch (MessagingException me)
      {
        throw new Exception("EmailManager: sendMultipleAttachments()" + me);
      }




  }//end sendMulitpleAttachments



}
