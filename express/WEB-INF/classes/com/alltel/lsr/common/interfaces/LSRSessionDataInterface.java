package com.alltel.lsr.common.interfaces;

import java.util.*;

// By creating this as an 'interface', we are able to change what's 
// contained in our session without ever having to change the 
// mechanism or class that maintains our session (ie SessionDataManager).

public interface LSRSessionDataInterface
{
	public String 	getUserId();

	public Object	getRequestInfo();

}
