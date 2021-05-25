package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;

public class MenuItem {
	private int	m_iMenuSqncNmbr = 0;
	private int	m_iItemSqncNmbr = 0;
	private String	m_strMenuItemDescription = null;
	private String	m_strHyperlink = null;

	MenuItem(int iMenu, int iItem, String strItemDesc, String strHyperlink)
	{
		this.m_iMenuSqncNmbr = iMenu;
		this.m_iItemSqncNmbr = iItem;
		this.m_strMenuItemDescription = strItemDesc;
		this.m_strHyperlink = strHyperlink;
	}

	public int getMenuSqncNmbr(){
		return this.m_iMenuSqncNmbr;
	}
	
	public int getItemSqncNmbr(){
		return this.m_iItemSqncNmbr;
	}
	
	public String getMenuItemDescription(){
		return this.m_strMenuItemDescription;
	}

	public String getHyperlink(){
		return this.m_strHyperlink;
	}

}
