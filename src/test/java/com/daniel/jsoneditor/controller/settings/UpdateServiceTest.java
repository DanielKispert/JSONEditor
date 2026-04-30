package com.daniel.jsoneditor.controller.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class UpdateServiceTest
{
    @Test
    void isNewer_newerVersionDetected()
    {
        assertTrue(UpdateService.isNewer("0.18.0", "0.17.0"));
        assertTrue(UpdateService.isNewer("0.17.1", "0.17.0"));
        assertTrue(UpdateService.isNewer("1.0.0", "0.17.0"));
    }
    
    @Test
    void isNewer_sameOrOlderVersion()
    {
        assertFalse(UpdateService.isNewer("0.17.0", "0.17.0"));
        assertFalse(UpdateService.isNewer("0.16.0", "0.17.0"));
        assertFalse(UpdateService.isNewer("0.17.0", "1.0.0"));
    }
    
    @Test
    void isNewer_handlesNullAndBlank()
    {
        assertFalse(UpdateService.isNewer(null, "0.17.0"));
        assertFalse(UpdateService.isNewer("", "0.17.0"));
        assertFalse(UpdateService.isNewer("  ", "0.17.0"));
        assertFalse(UpdateService.isNewer("0.18.0", null));
        assertFalse(UpdateService.isNewer("0.18.0", "unknown"));
    }
    
    @Test
    void isNewer_handlesMalformedVersions()
    {
        assertFalse(UpdateService.isNewer("abc", "0.17.0"));
        assertFalse(UpdateService.isNewer("0.17.0", "abc"));
    }
    
    @Test
    void isNewer_handlesPartialVersions()
    {
        assertTrue(UpdateService.isNewer("1.2", "0.17.0"));
        assertFalse(UpdateService.isNewer("0.17", "0.17.0"));
    }
}


