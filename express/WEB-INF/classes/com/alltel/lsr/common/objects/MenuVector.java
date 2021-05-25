/*
 * This class holds the information for a particular menu.  It holds the menu sequence number
 * and the list of menu items contained within that menu.
 * It will be loaded after a successful login.
*/

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class MenuVector 
{
	private int	m_iMenuSqncNmbr = 0;
	private String	m_strMenuDescription = null;
	private Vector	m_vMenuItems = new Vector();	//Of type MenuItem

	MenuVector(int iMenu, String strMenuDesc)
	{
		this.m_iMenuSqncNmbr = iMenu;
		this.m_strMenuDescription = strMenuDesc;
	}

	public int getMenuSqncNmbr()
	{
		return this.m_iMenuSqncNmbr;
	}
	
	public String getMenuDescription()
	{
		return this.m_strMenuDescription;
	}
	
	public Vector getMenuItemVector()
	{
		return this.m_vMenuItems;
	}
	
	public boolean addMenuItem(int iItem, String strDesc, String strHyperLink)
	{
		MenuItem objMenuItem = new MenuItem(m_iMenuSqncNmbr, iItem, strDesc, strHyperLink);
		this.m_vMenuItems.addElement(objMenuItem);
		return true;
	}
	
}
