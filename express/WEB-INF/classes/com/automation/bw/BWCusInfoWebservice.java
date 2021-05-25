/*
 * BWCusInfoWebservice.java
 *
 * Created on June 19, 2009, 11:43 AM
 * Adding this line to check in same file version into clear case to apply
 * same label - Antony - 02/28/2014
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bw;

import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.alltel.lsr.common.util.Log;

import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;

import com.windstream.winexpcustprof.WinExpDataProxy;
import com.windstream.winexpcustprof.WinExpDataProxy_Impl;
import com.windstream.winexpcustprof.WinExpWebSrvcIntrfc;
import com.windstream.winexpcustprof.WinExpWebSrvcIntrfc_Stub;

import com.windstream.winexpcustprof.ServiceDataRequest;
import com.windstream.winexpcustprof.ServiceDataResponse;
import com.windstream.winexpcustprof.StatusDataRequest;
import com.windstream.winexpcustprof.StatusDataResponse;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author kumar.k
 */
public class BWCusInfoWebservice {
    
    private  ValidationDataBean validationBean;
        
    /** Creates a new instance of BWCusInfoWebservice */
    public BWCusInfoWebservice() {
    }
    
    /*
     * webServiceInvoke method used for invoking
     * buisness ware webservice and give input and get the ouput from b/w
     */
    
    public void webServiceInvoke(LSRDataBean lsrDataBean){
        try {
            Log.write("BWCusInfoWebservice webServiceInvoke calling: ");
            System.out.println("BWCusInfoWebservice webServiceInvoke calling: ");
            WinExpDataProxy objWinExpDataProxy = new WinExpDataProxy_Impl();
            WinExpWebSrvcIntrfc objWinExpWebSrvcIntrfc= objWinExpDataProxy.getWinExpWebSrvcIntrfc();

            //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bwcamslookup.URL
            WinExpWebSrvcIntrfc_Stub  wewStub = (WinExpWebSrvcIntrfc_Stub )objWinExpWebSrvcIntrfc;
            URL urlString = new URL(PropertiesManager.getProperty("lsr.bwcamslookup.URL",""));

            Log.write("BW StatusRequest URL prior to dynamic setting : "+wewStub._getTargetEndpoint());
            System.out.println("BW StatusRequest URL prior to dynamic setting : "+wewStub._getTargetEndpoint());
            wewStub._setTargetEndpoint(urlString);
            Log.write("BW StatusRequest URL after to dynamic setting : "+wewStub._getTargetEndpoint());
            System.out.println("BW StatusRequest URL after to dynamic setting : "+wewStub._getTargetEndpoint());

            ServiceDataRequest servicedataRst = getServiceDataRequest(lsrDataBean);
            Log.write("\n =BWCusInfoWebservice== servicedataRst: Input Values: "+servicedataRst);
            System.out.println("\n =BWCusInfoWebservice== servicedataRst: Input Values: "+servicedataRst);
            
            StatusDataRequest statusdataRst = getStatusDataRequest(lsrDataBean);
            Log.write("\n BWCusInfoWebservice statusdataRst Input Values:"+statusdataRst);
            System.out.println("\n BWCusInfoWebservice statusdataRst Input Values:"+statusdataRst);
            
            StatusDataResponse statusdataRes = objWinExpWebSrvcIntrfc.opStatusDataRequest(statusdataRst);
            
            Log.write("\n =BWCusInfoWebservice statusdataRes Output Values: "+statusdataRes);
            System.out.println("\n =BWCusInfoWebservice statusdataRes Output Values: "+statusdataRes);
            
            if(statusdataRes!=null) {
                servicedataRst.setCamsId(statusdataRes.getCamsId());
                servicedataRst.setCustDatabase(statusdataRes.getCustDatabase());
            }
                
            Log.write("\n =BWCusInfoWebservice== servicedataRst: Input Values: "+servicedataRst);
            System.out.println("\n =BWCusInfoWebservice== servicedataRst: Input Values: "+servicedataRst);
            
            ServiceDataResponse servicedataRes =objWinExpWebSrvcIntrfc.opServiceDataRequest(servicedataRst);
            
            Log.write("\n =BWCusInfoWebservice==servicedataRes=="+servicedataRes);
            System.out.println("\n =BWCusInfoWebservice==servicedataRes=="+servicedataRes);
            
            createValidationDataBean(statusdataRes,servicedataRes);
        } catch (RemoteException re) {
            System.out.println("Before Remote Trace---1111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222");
            re.printStackTrace();
            Log.write("1111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222");
            System.out.println("After Remote Trace - 1111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222");
        } catch(Exception ex) {
            ex.printStackTrace();
            ExceptionHandler.handleException("Exception:BW ",ex);
        }
        
    }
    
     /*
      * createValidationDataBean method used for setting  b/w data into
      * validation bean
      */
    
    
    public void createValidationDataBean(StatusDataResponse statusdataRes,
            ServiceDataResponse servicedataRes){
        
        ValidationDataBean objValidationBean = new ValidationDataBean();
        
        objValidationBean.setCustStatus(statusdataRes.getCustStatus());
        objValidationBean.setCustTraitList(statusdataRes.getCustTraitList());
        objValidationBean.setCamsId(statusdataRes.getCamsId());
        objValidationBean.setCustType(statusdataRes.getCustType());
        objValidationBean.setCustName(statusdataRes.getCustName());
        objValidationBean.setCustAddress(statusdataRes.getCustAddress());
        objValidationBean.setCustTaxJuris(statusdataRes.getCustTaxJuris());
        objValidationBean.setCustOrgId(statusdataRes.getCustOrgId());
        objValidationBean.setCustDatabase(statusdataRes.getCustDatabase());
        objValidationBean.setPilotNo(statusdataRes.getCustPilotTn());
        
        objValidationBean.setCustSag(servicedataRes.getCustSag());
        objValidationBean.setComplex(servicedataRes.getComplex());
        objValidationBean.setGiftService(servicedataRes.getGiftService());
        objValidationBean.setImpctdAppList(servicedataRes.getImpctdAppList());
        objValidationBean.setCustPndgOrderList(servicedataRes.getCustPndgOrderList());
        objValidationBean.setCustAsocList(servicedataRes.getCustAsocList());
        objValidationBean.setGtnlTnList(servicedataRes.getGtnlTnList());
        objValidationBean.setErrorInfo(statusdataRes.getErrorInfo());
        Log.write("=BWCusInfoWebservice==objValidationBean=="+objValidationBean);
        
        /* Added new getter setter method for videoType field from BW to be pre-populated in CI form Video field -- 12/19/2013 */
        
        System.out.println("Video Type :"+servicedataRes.getVideoType());
        objValidationBean.setVideoType(servicedataRes.getVideoType());
                
        /* Antony -- end of code changes -- 12/19/2013 */
        
        if(statusdataRes!=null && statusdataRes.getCustStatus()!=null &&
                statusdataRes.getCustStatus().trim().length()>0 ){
            setValidationBean(objValidationBean);
        }else{
            setValidationBean(null);
        }
        
        
    }

    /*
       * getServiceDataRequest method used for setting  LSRDataBean data into
       * ServiceDataRequest bean
       */
    
    public ServiceDataRequest getServiceDataRequest(LSRDataBean lsrDataBean) {
        ServiceDataRequest serDataRst = new ServiceDataRequest() ;
        
        Log.write("=BWCusInfoWebservice==getServiceDataRequest=="+lsrDataBean);
        
        serDataRst.setPon(lsrDataBean.getReqstPon());
        serDataRst.setOcnId(lsrDataBean.getOCNcd());
        serDataRst.setPov(lsrDataBean.getReqstVer());
        serDataRst.setAppId("Fqyc@tUFosBi4xY0FnM34FLRW8!") ;
        serDataRst.setMsgType("SR");
        serDataRst.setUserId(lsrDataBean.getMdfdUserid()) ;
        
         //parse the string into Date object      
        Date date = new Date();

        //create SimpleDateFormat object with desired date format CCYYMMDDHHMMSSTMZ
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

        //parse the date into another format
        String timestamp = sdf.format(date)+"EDT";  
            
        
        serDataRst.setTimeStamp(timestamp);
        serDataRst.setTransType("I");
        serDataRst.setAtn(lsrDataBean.getAccountTelephoneNo());//2702303500 0745151978 2702372000
        serDataRst.setCamsId(lsrDataBean.getAccountNo());
        //serDataRst.setCustDatabase(validationBean.getCustDatabase()); -- validationBean not populated at this time

        return serDataRst;
    }
    
    /*
     * getStatusDataRequest method used for setting  LSRDataBean data into
     * StatusDataRequest bean
     */
    
    public StatusDataRequest getStatusDataRequest(LSRDataBean lsrDataBean) {
        StatusDataRequest stsDataRst = new StatusDataRequest() ;
        
        stsDataRst.setPon(lsrDataBean.getReqstPon());
        stsDataRst.setOcnId(lsrDataBean.getOCNcd());
        stsDataRst.setPov(lsrDataBean.getReqstVer());
        stsDataRst.setAppId("abc") ;
        stsDataRst.setMsgType("abc");
        stsDataRst.setUserId(lsrDataBean.getMdfdUserid()) ;
        stsDataRst.setTimeStamp("20090616121200EST");
        stsDataRst.setTransType("a");
        stsDataRst.setAtn(lsrDataBean.getAccountTelephoneNo());
        
        return stsDataRst;
    }
    
    
     /*
      * convertXMLDataRequest method used for converting Java object into
      * XML object.
      */
    
    public String convertXMLDataRequest(LSRDataBean lsrDataBean, String str){
        
        if(str.equals("servicedata")){
            String str1= "<ServiceDataRequest>" +
                    "<pon>"+lsrDataBean.getPurchaseON()+ "</pon>" +
                    "<ocnId>"+lsrDataBean.getOCNcd()+ "</ocnId>" +
                    "<pov>"+lsrDataBean.getPurVerNum()+ "</pov>" +
                    "<appId>"+"WINEXP"+"</appId>" +
                    "<msgType>"+"CUSTINQ"+ "</msgType>" +
                    "<userId> "+lsrDataBean.getMdfdUserid()+ "</userId>" +
                    "<timeStamp> "+lsrDataBean.getMdfdDt()+ "</timeStamp>" +
                    "<transType> "+"LSRI"+ "</transType>" +
                    "<camsId> "+lsrDataBean.getAccountNo()+ "</camsId>" +
                    "<custDatabase> "+ "</custDatabase>" +
                    "</ServiceDataRequest>" ;
            return str1;
        }
        if(str.equals("statusdata")){
            String str2= "<StatusDataRequest>" +
                    "<pon>"+lsrDataBean.getPurchaseON()+ "</pon>" +
                    "<ocnId>"+lsrDataBean.getOCNcd()+ "</ocnId>" +
                    "<pov>"+lsrDataBean.getPurVerNum()+ "</pov>" +
                    "<appId>"+"WINEXP"+"</appId>" +
                    "<msgType>"+"CUSTINQ"+ "</msgType>" +
                    "<userId> "+lsrDataBean.getMdfdUserid()+ "</userId>" +
                    "<timeStamp> "+lsrDataBean.getMdfdDt()+ "</timeStamp>" +
                    "<transType> "+"LSRI"+ "</transType>" +
                    "<atn>"+lsrDataBean.getAccountNo()+"</atn>"+
                    "</StatusDataRequest>";
            return str2;
        }
        return "";
    }
    
    public String ConvertXMLtoJavaObject(){
        return "";
    }
    
    public ValidationDataBean getValidationBean() {
        return validationBean;
    }
    
    public void setValidationBean(ValidationDataBean validationBean) {
        this.validationBean = validationBean;
    }
}
