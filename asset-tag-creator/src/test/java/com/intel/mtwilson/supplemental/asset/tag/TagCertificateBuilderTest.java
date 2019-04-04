/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag;

import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import org.junit.Test;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import com.intel.dcsg.cpg.io.UUID;
import java.security.cert.CertificateException;
import org.bouncycastle.operator.OperatorCreationException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author gdosunat
 */
public class TagCertificateBuilderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateBuilderTest.class);
    
    @Test
    public void convertJsonToTagSelectionTest() throws IOException{
        String json = "{\"selection\":[{\"name\":\"Developer\",\"value\":\"Lupita\"},{\"name\":\"Developer\",\"value\":\"Zahedi\"},{\"name\":\"Developer\",\"value\":\"Adolfo\"}],\"selectionName\":\"GDC\",\"selectionDescription\":\"This is a selection of developers from GDC\"}";
        TagCertificateBuilder builder = TagCertificateBuilder.factory();
        TagSelection selection = new TagSelection();
        
        assertEquals(selection.getClass(),builder.selectionFromJson(json).getClass());
        ArrayList<TagKvAttribute> list = selection.getSelection();
    }
    
    
    @Test
    public void createX509CertificateTest() throws NoSuchAlgorithmException, OperatorCreationException, IOException, CertificateException{
        String json = "{\"selection\":[{\"name\":\"Developer\",\"value\":\"Lupita\"},{\"name\":\"Developer\",\"value\":\"Zahedi\"},{\"name\":\"Developer\",\"value\":\"Adolfo\"}],\"selectionName\":\"GDC\",\"selectionDescription\":\"This is a selection of developers from GDC\"}";
        TagCertificateBuilder builderS = TagCertificateBuilder.factory();
        TagSelection selection = builderS.selectionFromJson(json);
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        
        TagCertificateBuilder builder = TagCertificateBuilder.factory()
                .selection(selection)
                .issuerName(cacert)
                .issuerPrivateKey(cakey.getPrivate())
                .dateSerial()                
                .expires(1, TimeUnit.SECONDS);
                
        String hostname = new UUID().toString();
        byte[] certificate = builder.createCertificateX509(hostname);
        
        log.debug("tag certificate {}", certificate);
    }
  

}
        
        