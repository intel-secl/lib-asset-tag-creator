/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag.integration;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.crypto.Sha384Digest;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.kunit.annotations.Integration;
import com.intel.mtwilson.supplemental.asset.tag.TagCertificateBuilder;
import com.intel.mtwilson.supplemental.asset.tag.TagSelectionBuilder;
import com.intel.mtwilson.supplemental.asset.tag.model.OID;
import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.operator.OperatorCreationException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import com.intel.mtwilson.core.common.tag.model.X509AttributeCertificate;

/**
 *
 * @author gdosunat
 */
public class AssetTagIntegrationTest {
    
    TagCertificateBuilder certificateBuilder = TagCertificateBuilder.factory();
    TagKvAttribute kvAttribute = new TagKvAttribute();
    TagSelectionBuilder selectionBuilder = new TagSelectionBuilder();
    
    @Integration(parameters={"Country","Mexico"})
    public TagKvAttribute createKvAttribute(String name, String value){
        return kvAttribute.createKvAttribute(name, value);
    }
    
    @Integration(parameters={"Roles"})
    public TagSelectionBuilder selectionName(String selectionName){
        return selectionBuilder.selectionName(selectionName);
    }
    
    @Integration(parameters={"\"This is a selection made of job roles\""})
    public TagSelectionBuilder selectionDescription(String selectionDescription){
        return selectionBuilder.selectionDescription(selectionDescription);
    }
    
    @Integration(parameters={"Developer", "GDC"})
    public TagSelectionBuilder textKvAttribute(String kvAttributeName, String kvAttributeValue){
        return selectionBuilder.textKvAttribute(kvAttributeName, kvAttributeValue);
    }
    
    @Integration
    public TagSelection buildSelection(){
        return selectionBuilder.build();
    }
   
    @Integration
    public TagSelection selectionFromJson() throws IOException{
        TagSelection selection = TagSelectionBuilder.factory()
                .selectionName("Developers")
                .selectionDescription("This Selection contains developers")
                .textKvAttribute("John", "FM")
                .textKvAttribute("Louis", "GDC").build();
         Gson gson = new GsonBuilder().create();
         String json = gson.toJson(selection);
        return certificateBuilder.selectionFromJson(json);
    }
    
    @Integration(parameters={"Country", "USA"})
    public TagCertificateBuilder attributeFromString(String attrName, String attrValue){
        return certificateBuilder.attribute(attrName, attrValue);
    }
    
    @Integration
    public TagCertificateBuilder attributeFromANS() {
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(OID.HOST_UUID);
        ASN1Encodable value = null;
        return certificateBuilder.attribute(oid,value);
    }
    
    @Integration
    public TagCertificateBuilder issuerNameFromX500Name(){
        return certificateBuilder.issuerName(new X500Name(new RDN[]{}));
    }
    
    @Integration
    public TagCertificateBuilder issuerNameFromX500Principal() {
        X500Principal principal = new X500Principal("CN=test, UID=cb614cb8-17e9-4e3f-8523-58a70a43c38");
        return certificateBuilder.issuerName(principal);
    }

    @Integration
    public TagCertificateBuilder issuerNameFromCertificate() throws NoSuchAlgorithmException {
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate issuerCertificate = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        return certificateBuilder.issuerName(issuerCertificate);
    }
    
    @Integration
    public TagCertificateBuilder issuerPrivateKey() throws NoSuchAlgorithmException{
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        PrivateKey issuerPrivateKey = cakey.getPrivate();
        return certificateBuilder.issuerPrivateKey(issuerPrivateKey);
    }
    
    @Integration(parameters={"76292708057987193002565060032465481997"})
    public TagCertificateBuilder serialNmber(BigInteger serialNumber){
        return certificateBuilder.serialNmber(serialNumber);
    }
    
    @Integration
    public TagCertificateBuilder dateSerial(){
        return certificateBuilder.dateSerial();
    }
        
    @Integration
    public TagCertificateBuilder subjectName(){
        return certificateBuilder.subjectName(new X500Name(new RDN[]{}));
    }
    
    @Integration(parameters={"1", "SECONDS"})
    public TagCertificateBuilder expires(long expiration, TimeUnit units) {
        return certificateBuilder.expires(expiration, units);
    }
     
    @Integration
    public byte[] buildCertificate() throws OperatorCreationException {
        return certificateBuilder.build();
    }
     
    @Integration(parameters={"3e55a51d-c6aa-4b4a-ac39-fbd2624a1f12"})
    public byte[] createCertificateX509(String hostname){ 
        return certificateBuilder.createCertificateX509(hostname);
    }

    @Integration
    public byte[] createCertificateX509FromJson(String issuer, String hostname, String selectionName, String selectionDesc, String[] attributes) throws IOException, OperatorCreationException, NoSuchAlgorithmException, CertificateException {
        TagSelectionBuilder builder = TagSelectionBuilder.factory()
                .selectionName(selectionName)
                .selectionDescription(selectionDesc);
        
        for(int i=0; i<attributes.length-1; i=i+2)  {
            builder.textKvAttribute(attributes[i], attributes[i+1]);
        }
        
         TagSelection selection = builder.build();
         Gson gson = new GsonBuilder().create();
         String json = gson.toJson(selection);

         KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
         X509Certificate cacert = X509Builder.factory().selfSigned(issuer, cakey).build();
         certificateBuilder = TagCertificateBuilder.factory()
                .issuerName(cacert)
                .issuerPrivateKey(cakey.getPrivate())
                .dateSerial()                
                .expires(1, TimeUnit.SECONDS);
         
         for(int i=0; i<attributes.length-1; i=i+2)  {
            certificateBuilder.attribute(attributes[i], attributes[i+1]);
        }
         
         byte[] certificate = certificateBuilder.createCertificateX509FromJson(hostname, json);
         System.out.println("===========================================================");
         System.out.println("                    Certificate JSON                      ");
         System.out.println("===========================================================");
         System.out.println(json);
         System.out.println("===========================================================");
         System.out.println();
         System.out.println("===========================================================");
         System.out.println("                       SHA1 Digest                    ");
         System.out.println("===========================================================");
         System.out.println(Sha1Digest.digestOf(certificate).toString());
         System.out.println("===========================================================");
         System.out.println();
         System.out.println("===========================================================");
         System.out.println("                       SHA256 Digest                    ");
         System.out.println("===========================================================");
         System.out.println(Sha256Digest.digestOf(certificate).toString());
         System.out.println("===========================================================");
         System.out.println();
        System.out.println("===========================================================");
        System.out.println("                       SHA384 Digest                    ");
        System.out.println("===========================================================");
        System.out.println(Sha384Digest.digestOf(certificate).toString());
        System.out.println("===========================================================");
        System.out.println();
         System.out.println("===========================================================");
         System.out.println("                   Attribute Certificate                  ");
         System.out.println("===========================================================");
         X509AttributeCertificate tagCer = X509AttributeCertificate.valueOf(certificate);
         System.out.println(tagCer.toString());
         System.out.println("===========================================================");
         return certificate;
    }
}
