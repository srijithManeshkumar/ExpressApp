package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;


public class ValidationResult {
	private boolean verdict; 
	private String vmessage; 

	ValidationResult(){
		this.verdict = false;
		this.vmessage = null;
	}

	ValidationResult(boolean status, String msg){
		this.verdict = status;
		this.vmessage = msg;
	}

	public boolean getValidationResult(){
		return verdict;
	}
	
	public String getValidationMessage(){
		return vmessage;
	}
	
	public void setValidationResult( boolean res){
		this.verdict = res;
	}
	
	public void setValidationMessage(String msg){
		this.vmessage = msg;
	}

}
