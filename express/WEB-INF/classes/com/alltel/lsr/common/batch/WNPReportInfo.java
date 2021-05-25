package com.alltel.lsr.common.batch;

public class WNPReportInfo
{
  private String m_strOCN;
  private String m_strOCNName;
  private int m_iNbrType1N;
  private int m_iNbrType1Y;
  private int m_iNbrTotal;
  private int m_iMthType1N;
  private int m_iMthType1Y;
  private int m_iMthTotal;
  private int m_iTotalType1N;
  private int m_iTotalType1Y;
  private int m_iTotalTotal;
  
  public WNPReportInfo(String strOcn, String strOcnName)
  {
    this.m_strOCN = strOcn;
    this.m_strOCNName = strOcnName;
    this.m_iTotalType1N = 0;
    this.m_iTotalType1Y = 0;
    this.m_iTotalTotal = 0;
    resetCounts();
  }
  
  public void resetCounts()
  {
    this.m_iNbrType1N = 0;
    this.m_iNbrType1Y = 0;
    this.m_iNbrTotal = 0;
  }
  
  public void resetMonthlyCounts()
  {
    this.m_iMthType1N = 0;
    this.m_iMthType1Y = 0;
    this.m_iMthTotal = 0;
  }
  
  public String getOCN()
  {
    return this.m_strOCN;
  }
  
  public String getOCNName()
  {
    return this.m_strOCNName;
  }
  
  public String getName()
  {
    return getOCN() + " " + getOCNName();
  }
  
  public int getNbrType1N()
  {
    return this.m_iNbrType1N;
  }
  
  public int getNbrType1Y()
  {
    return this.m_iNbrType1Y;
  }
  
  public int getNbrTotal()
  {
    return this.m_iNbrTotal;
  }
  
  public int getMonthlyType1N()
  {
    return this.m_iMthType1N;
  }
  
  public int getMonthlyType1Y()
  {
    return this.m_iMthType1Y;
  }
  
  public int getMonthlyTotal()
  {
    return this.m_iMthTotal;
  }
  
  public int getTotalType1N()
  {
    return this.m_iTotalType1N;
  }
  
  public int getTotalType1Y()
  {
    return this.m_iTotalType1Y;
  }
  
  public int getTotalTotal()
  {
    return this.m_iTotalTotal;
  }
  
  public void addType1N(int i)
  {
    this.m_iNbrType1N += i;
    this.m_iMthType1N += i;
    this.m_iTotalType1N += i;
  }
  
  public void addType1Y(int i)
  {
    this.m_iNbrType1Y += i;
    this.m_iMthType1Y += i;
    this.m_iTotalType1Y += i;
  }
  
  public void addTotal(int i)
  {
    this.m_iNbrTotal += i;
    this.m_iMthTotal += i;
    this.m_iTotalTotal += i;
  }
}
