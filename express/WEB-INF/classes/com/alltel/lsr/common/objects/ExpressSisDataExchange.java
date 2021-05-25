/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 * MODULE:		ExpressSisDataExchange.java
 * 
 * DESCRIPTION: ExpressSisDataExchange
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:       07-2005
 * 
 * HISTORY:
 *	 07-2005 Edris Kalibala created
 *
*/
// Source File Name:   ExpressSisDataExchange.java

package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.util.ExpressUtil;
import com.alltel.lsr.common.util.Log;
import java.sql.*;
import java.util.Vector;

// Referenced classes of package com.alltel.lsr.common.objects:
//            ExpressSISBean, FormSection, FormField, DwoBean, 
//            Forms, Form

public class ExpressSisDataExchange extends ExpressSISBean
{

	
    DwoBean dwobean;
    String strDwoStatus;
    String strServiceType;
    String strOcn;
    String strSrvDescription;
    int iDwoSqncNmbr;
    String strTypeInd;
    String strProductType; // HD0000002249896
    String strProductName; // HD0000002249896
    
    public ExpressSisDataExchange(String s)
    {
        super(s);
        clean();
    }

    public void clean()
    {
        dwobean = null;
        strDwoStatus = "";
        strServiceType = "";
        strOcn = "";
        strTypeInd = "";
        strSrvDescription = "";
        iDwoSqncNmbr = -9999;
        strProductType = ""; // HD0000002249896
        strProductName = ""; // HD0000002249896
    }

    public void setDwoBean(DwoBean dwobean1)
    {
        dwobean = dwobean1;
    }

    public void setDwoStatus(String s)
    {
        strDwoStatus = s;
    }

    public String getDwoStatus()
    {
        return strDwoStatus;
    }

    public void setServiceType(String s)
    {
        strServiceType = s;
    }

    public String getServiceType()
    {
        return strServiceType;
    }

    public void setOcn(String s)
    {
        strOcn = s;
    }

    public String getOcn()
    {
        return strOcn;
    }

    public void setSrvDescription(String s)
    {
        strSrvDescription = s;
    }

    public String getSrvDescription()
    {
        return strSrvDescription;
    }

    public void setDwoSqncNmbr(int i)
    {
        iDwoSqncNmbr = i;
    }

    public void setDwoSqncNmbr(String s)
    {
        try
        {
            iDwoSqncNmbr = Integer.parseInt(s);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            Log.write(5, exception.toString());
        }
    }

    public int getDwoSqncNmbr()
    {
        return iDwoSqncNmbr;
    }

    public void setTypeInd(String s)
    {
        strTypeInd = s;
    }

    public String getTypeInd()
    {
        return strTypeInd;
    }

    public void setProductType(String s) // HD0000002249896
    {
        strProductType = s;
    }

    public String getProductType() // HD0000002249896
    {
        return strProductType;
    }

    public void setProductName(String s) // HD0000002249896
    {
        strProductName = s;
    }

    public String getProductName() // HD0000002249896
    {
        return strProductName;
    }

    public boolean dbLoadDwoData(Connection connection)
        throws SQLException, Exception
    {
        ResultSet resultset = null;
        PreparedStatement preparedstatement = null;
        boolean flag = false;
// HD0000002249896
        String s = "SELECT DWO_STTS_CD, D.SRVC_TYP_CD, OCN_CD, SRVC_TYP_DSCRPTN, D.PRDCT_TYP_CD, PRDCT_DSCRPTN FROM DWO_T D, SERVICE_TYPE_T S, PRODUCT_T P  WHERE D.SRVC_TYP_CD = S.SRVC_TYP_CD AND D.PRDCT_TYP_CD = P.PRDCT_TYP_CD AND  DWO_SQNC_NMBR = ? AND S.TYP_IND = ? AND P.TYP_IND = ? ";
        preparedstatement = connection.prepareStatement(s);
        preparedstatement.clearParameters();
        preparedstatement.setInt(1, iDwoSqncNmbr);
        preparedstatement.setString(2, strTypeInd);
        preparedstatement.setString(3, strTypeInd);
        resultset = preparedstatement.executeQuery();
        int i;
        for(i = 0; resultset.next(); i++)
        {
            setDwoStatus(resultset.getString(1));
            setServiceType(resultset.getString(2));
            setOcn(resultset.getString(3));
            setSrvDescription(resultset.getString(4));
            setProductType(resultset.getString(5)); // HD0000002249896
            setProductName(resultset.getString(6)); // HD0000002249896
        }

        resultset.close();
        resultset = null;
        preparedstatement.close();
        preparedstatement = null;
        if(i != 0)
            flag = true;
        return flag;
    }

    public Vector dbLoadForms(Connection connection)
        throws SQLException, Exception
    {
        ResultSet resultset = null;
        PreparedStatement preparedstatement = null;
        Vector vector = new Vector(3);
        String s = " SELECT DISTINCT FRM_SQNC_NMBR FROM DWO_T,  SERVICE_TYPE_FORM_T WHERE DWO_SQNC_NMBR = ?  AND SERVICE_TYPE_FORM_T.SRVC_TYP_CD = ?  AND SERVICE_TYPE_FORM_T.TYP_IND = ? ";
        preparedstatement = connection.prepareStatement(s);
        preparedstatement.clearParameters();
        preparedstatement.setInt(1, iDwoSqncNmbr);
        preparedstatement.setString(2, getServiceType());
        preparedstatement.setString(3, strTypeInd);
        for(resultset = preparedstatement.executeQuery(); resultset.next(); vector.add(resultset.getString(1)));
        resultset.close();
        resultset = null;
        preparedstatement.close();
        preparedstatement = null;
        return vector;
    }

    public String createXml(int i)
    {
        StringBuffer stringbuffer = new StringBuffer(2048);
        if(dwobean == null)
            return null;
        if(dwobean.m_conn == null)
            dwobean.getConnection();
        Vector vector = new Vector();
        Object obj = null;
        Forms forms = Forms.getInstance();
        Object obj1 = null;
        Vector vector2 = new Vector();
        StringBuffer stringbuffer1 = new StringBuffer(2048);
        try
        {
            Vector vector4 = null;
            if(dbLoadDwoData(dwobean.m_conn))
                vector4 = dbLoadForms(dwobean.m_conn);
            stringbuffer1.append(printXMLheader());
            int j = 0;
            boolean flag = false;
            boolean flag1 = false;
            boolean flag2 = false;
            for(; j < vector4.size(); j++)
            {
                int k = Integer.parseInt((String)vector4.get(j));
                Form form = forms.getForm(k);
                stringbuffer1.append("<" + ExpressUtil.makeValidXmlTag(form.getFormCd()) + ">\n" );
                Vector vector3 = forms.getFormSections(k);
                Vector vector1 = dwobean.getFormFields(k, iDwoSqncNmbr, i);
                String strTempSection = "";
                for(int l = 0; l < vector3.size(); l++)
                {
                    FormSection formsection = (FormSection)vector3.get(l);
                    if( formsection.getFrmSctnDscrptn().equalsIgnoreCase("REMARKS") ){
                         strTempSection  =  form.getFormCd();
                  	}  else
                  	{
                  		 strTempSection  =  formsection.getFrmSctnDscrptn();
                  	}
                    stringbuffer1.append( "<!--" + strTempSection + "-->\n" );
                    stringbuffer1.append( "<" + ExpressUtil.makeValidXmlTag( strTempSection ) + ">\n" );
                    for(int i1 = 0; i1 < vector1.size(); i1++)
                    {
                        FormField formfield = (FormField)vector1.get(i1);
                        if(formfield.getFrmSctnSqncNmbr() == formsection.getFrmSctnSqncNmbr())
                        {
                           stringbuffer1.append("<!--" + formfield.getFldDscrptn() + "-->\n");
                            stringbuffer1.append("<" + ExpressUtil.makeValidXmlTag(strTempSection) + "_" + ExpressUtil.makeValidXmlTag(formfield.getFldCd()) + ">");
                            stringbuffer1.append( ExpressUtil.escapeHTML(formfield.getFieldData() ) );
                            stringbuffer1.append("</" + ExpressUtil.makeValidXmlTag(strTempSection) + "_" + ExpressUtil.makeValidXmlTag(formfield.getFldCd()) + ">\n");
                        }
                    }

                    stringbuffer1.append("</" + ExpressUtil.makeValidXmlTag(strTempSection) + ">\n");
                }

                stringbuffer1.append("</" + ExpressUtil.makeValidXmlTag(form.getFormCd()) + ">\n");
            	strTempSection = "";
            }

        }
        catch( Exception exce)
        {
            exce.printStackTrace();
            Log.write(1, "ExpressSisDataExchange:createXML Caught Exception\n e=[" + exce.toString() + "]");
        }        
        stringbuffer1.append("</orderData>\n");
        return stringbuffer1.toString();
    }

    public Vector extractXML(String s)
    {
        return null;
    }

    private String printXMLheader()
    {
        StringBuffer stringbuffer = new StringBuffer(1024);
        stringbuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        stringbuffer.append("<orderData>\n");
        stringbuffer.append("<RecordDescription>\n");
        stringbuffer.append("<SISID>\n");
        stringbuffer.append("</SISID>\n");
        stringbuffer.append("<EXPRESSID>\n");
        stringbuffer.append(iDwoSqncNmbr);
        stringbuffer.append("</EXPRESSID>\n");
        stringbuffer.append("<SERVICETYPE>\n");
        stringbuffer.append(getServiceType());
        stringbuffer.append("</SERVICETYPE>\n");
        stringbuffer.append("<OCN>\n");
        stringbuffer.append(getOcn());
        stringbuffer.append("</OCN>\n");
        stringbuffer.append("<STATUS>\n");
        stringbuffer.append(getDwoStatus());
        stringbuffer.append("</STATUS>\n");
        stringbuffer.append("<SERVICEDESCRIPTION>\n");
        stringbuffer.append(getSrvDescription());
        stringbuffer.append("</SERVICEDESCRIPTION>\n");
        stringbuffer.append("<PRODUCTTYPE>\n"); // HD0000002249896
        stringbuffer.append(getProductType());  // HD0000002249896
        stringbuffer.append("</PRODUCTTYPE>\n"); // HD0000002249896
        stringbuffer.append("<PRODUCTNAME>\n"); // HD0000002249896
        stringbuffer.append(getProductName());  // HD0000002249896
        stringbuffer.append("</PRODUCTNAME>\n"); // HD0000002249896
        stringbuffer.append("</RecordDescription>\n");
        return stringbuffer.toString();
    }

}
