/*
 * NOTICE:         THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONG TO ALLTEL 
 *               INFORMATION SERVICES. ANY UNAUTHORIZED ACCESS, USE, DUPLICATION 
 *                               OR DISCLOSURE IS UNLAWFUL.
 *
 *			        COPYRIGHT (C) 2001
 *				        BY
 *			        ALLTEL INFORMATION SERVICES
 *
 * 
 * MODULE:		 ErrorSeverityLevel
 * 
 * DESCRIPTION:  This interface defines the severity levels recognized for 
 *               exceptions in the system.
 * 
 *
 * CHANGE HISTORY:
 *
 *
 */
 
 package com.alltel.lsr.common.error.objects;

/**
 * The ErrorSeverityLevel interface is an interface that defines the severity
 * levels recognized for exceptions in the system. Currently there are four
 * levels defined:
 * <UL>
 *   <LI> FATAL
 *   <LI> ERROR
 *   <LI> WARNING
 *   <LI> INFORMATION
 * </UL>
 * <P>
 * Any AlltelException object created within the system must specify one of these
 * levels.
 * <P>
 *
 * Configuration parameters used:
 * <UL>
 *   <LI>None (this is an interface).
 * </UL>
 */

public interface ErrorSeverityLevel
{
    /** 
     * Returns error severity level for an AlltelException object. This will be 
     * one of the levels defined by this interface.
     *
     * @return  int  error severity level for AlltelException object
     */
    public int getErrorSeverityLevel();
    
    /** Error level for informational non-serious conditions. */
	public static final int INFORMATION = 0;
    
    /** Error level for warning conditions. */
	public static final int WARNING     = 1;
    
    /** Error level for non-fatal error conditions. */
	public static final int ERROR       = 2;
    
    /** Error level for fatal error conditions. */
	public static final int FATAL       = 3;
}
