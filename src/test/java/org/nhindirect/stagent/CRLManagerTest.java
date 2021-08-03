package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.nhindirect.stagent.cert.RevocationManager;
import org.nhindirect.stagent.cert.impl.CRLRevocationManager;
import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;

/**
 * Test class for the CRLManager class.
 * 
 * TODO: Convert this test class to the test suite setup used by the other Agent
 * tests.
 * 
 * @author beau
 */
public class CRLManagerTest 
{
    private static final String KEY_STORE_PASSWORD = "h3||0 wor|d";
    private static final String PRIVATE_KEY_PASSWORD = "pKpa$$wd";

    /**
     * Test the CRLManager class with normal and non-normal input.
     */
    @Test
    public void testCrlManager() 
    {
        String tmp = this.getClass().getClassLoader().getResource("crl/certs.crl").getPath();
        final String workingDir = tmp.substring(0, tmp.lastIndexOf("/") + 1).replaceAll("%20", " ");

        
        String internalKeystoreFile = workingDir + "keystore";
        KeyStoreCertificateStore service = new KeyStoreCertificateStore(internalKeystoreFile, KEY_STORE_PASSWORD, PRIVATE_KEY_PASSWORD);

        RevocationManager crlManager = new CRLRevocationManager() 
        {
            @Override
            protected String getNameString(String generalNameString) 
            {
                String s = super.getNameString(generalNameString);
                return s.replace("http://JUNIT", "file://" + workingDir);
            }
        };
        
        assertEquals(false, crlManager.isRevoked(null));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("valid")));
        assertEquals(true, crlManager.isRevoked(service.getByAlias("revoked")));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("gm2552")));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("missing")));        

        // Hit cache
        assertEquals(false, crlManager.isRevoked(null));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("valid")));
        assertEquals(true, crlManager.isRevoked(service.getByAlias("revoked")));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("gm2552")));
        assertEquals(false, crlManager.isRevoked(service.getByAlias("missing")));
    }
}
