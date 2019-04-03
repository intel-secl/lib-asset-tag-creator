/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.supplemental.asset.tag;

import com.intel.mtwilson.supplemental.asset.tag.model.TagSelection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import com.intel.mtwilson.supplemental.asset.tag.common.UTF8NameValueSequence;
import java.security.PrivateKey;
import java.util.ArrayList;
import org.bouncycastle.asn1.x500.X500Name;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.validation.BuilderModel;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.supplemental.asset.tag.model.OID;
import com.intel.mtwilson.supplemental.asset.tag.model.TagKvAttribute;
import java.util.logging.Level;

/**
 *
 * @author gdosunat
 */
public class TagCertificateBuilder extends BuilderModel {

    private TagSelection selection = null;
    private final ArrayList<Attribute> attributes = new ArrayList<>();
    private X500Name issuerName = null;
    private PrivateKey issuerPrivateKey = null;
    private BigInteger serialNumber = null;
    private Date notBefore = null;
    private Date notAfter = null;
    private X500Name subjectName = null;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static class Attribute {

        public ASN1ObjectIdentifier oid;
        public ASN1Encodable value;

        public Attribute(ASN1ObjectIdentifier oid, ASN1Encodable value) {
            this.oid = oid;
            this.value = value;
        }
    }

    public static TagCertificateBuilder factory() {
        return new TagCertificateBuilder();
    }

    public TagCertificateBuilder selection(TagSelection selection) {
        this.selection = selection;
        for (TagKvAttribute attribute : selection.getSelection()) {
            attribute(attribute.getName(), attribute.getValue());
        }
        return this;
    }

    public TagSelection getSelection() {
        return this.selection;
    }

    public TagSelection selectionFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.selection = mapper.readValue(json, TagSelection.class);
        return this.selection;
    }

    public TagCertificateBuilder attribute(String attrName, String attrValue) {
        attributes.add(new Attribute(new ASN1ObjectIdentifier(UTF8NameValueSequence.OID), new UTF8NameValueSequence(attrName, attrValue)));
        return this;
    }

    public TagCertificateBuilder attribute(ASN1ObjectIdentifier oid, ASN1Encodable value) {
        attributes.add(new Attribute(oid, value));
        return this;
    }

    public TagCertificateBuilder issuerName(X500Name issuerName) {
        this.issuerName = issuerName;
        return this;
    }

    public TagCertificateBuilder issuerName(X500Principal principal) {
        issuerName = new X500Name(principal.getName()); // principal.getName() produces RFC 2253 output which we hope is compatible wtih X500Name directory name input
        return this;
    }

    public TagCertificateBuilder issuerName(X509Certificate issuerCertificate) {
        return issuerName(issuerCertificate.getSubjectX500Principal());
    }

    public TagCertificateBuilder issuerPrivateKey(PrivateKey issuerPrivateKey) {
        this.issuerPrivateKey = issuerPrivateKey;
        return this;
    }

    public TagCertificateBuilder serialNmber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public TagCertificateBuilder dateSerial() {
        serialNumber = new BigInteger(String.valueOf(Calendar.getInstance().getTimeInMillis()));
        return this;
    }

    public TagCertificateBuilder subjectUuid(UUID uuid) {
        DEROctetString uuidText = new DEROctetString(uuid.toByteArray().getBytes());
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(OID.HOST_UUID);
        AttributeTypeAndValue attr = new AttributeTypeAndValue(oid, uuidText);
        RDN rdn = new RDN(attr);
        subjectName = new X500Name(new RDN[]{rdn});
        return this;
    }

    public TagCertificateBuilder subjectName(X500Name subject) {
        this.subjectName = subject;
        return this;
    }

    public TagCertificateBuilder expires(long expiration, TimeUnit units) {
        notBefore = new Date();
        notAfter = new Date(notBefore.getTime() + TimeUnit.MILLISECONDS.convert(expiration, units));
        return this;
    }

    public byte[] build() throws OperatorCreationException {
        if (notBefore == null || notAfter == null) {
            expires(1, TimeUnit.DAYS); // 1 day default
        }
        if (serialNumber == null) {
            dateSerial();
        }
        if (subjectName == null) {
            fault("Subject name is missing");
        }
        if (issuerName == null) {
            fault("Issuer name is missing");
        }
        if (issuerPrivateKey == null) {
            fault("Issuer private key is missing");
        }
        if (attributes.isEmpty()) {
            fault("No attributes selected");
        }
        try {
            if (getFaults().isEmpty()) {
                AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
                AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
                if (issuerPrivateKey == null) {
                    return null;
                }
                ContentSigner authority = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(PrivateKeyFactory.createKey(issuerPrivateKey.getEncoded())); // create a bouncy castle content signer convert using our existing private key
                // second, prepare the attribute certificate
                AttributeCertificateHolder holder = new AttributeCertificateHolder(subjectName); // which is expected to be a UUID  like this: 33766a63-5c55-4461-8a84-5936577df450
                AttributeCertificateIssuer issuer = new AttributeCertificateIssuer(issuerName);
                X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(holder, issuer, serialNumber, notBefore, notAfter);
                for (Attribute attribute : attributes) {
                    builder.addAttribute(attribute.oid, attribute.value);
                }
                X509AttributeCertificateHolder cert = builder.build(authority);
                log.debug("cert: {}", Base64.encodeBase64String(cert.getEncoded())); // MIICGDCCAQACAQEwH6EdpBswGTEXMBUGAWkEEJKnGiKMF0UioYv9PtPQCzmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMTMwODA4MjIyMTEzWhgPMjAxMzA5MDgyMjIxMTNaMEMwEwYLKwYBBAG9hDcBAQExBAwCVVMwEwYLKwYBBAG9hDgCAgIxBAwCQ0EwFwYLKwYBBAG9hDkDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBBQUAA4IBAQCcN8KjjmR2H3LT5aL1SCFS4joy/7vAd3/xdJtkqrb3UAQHMdUUJQHf3frJsMJs22m0So0xs/f1sB15frC1LsQGF5+RYVXsClv0glStWbPYiqEfdM7dc/RDMRtrXKEH3sBlxMT7YS/g5E6qwmKZX9shQ3BYmeZi5A3DTzgHCbA3Cm4/MQbgWGjoamfWZ9EDk4Bww2y0ueRi60PfoLg43rcijr8Wf+JEzCRw040vIaH3DtFdmzvvGRdqE3YlEkrUL3gEIZNY3Po1NL4cb238vT5CHZTt9NyD7xSv0XkwOY4RbSUdYBsxfH3mEcdQ6LtJdfF1BUXfMThKN3TctFcY/dLF
    
                return cert.getEncoded(); //X509AttributeCertificate.valueOf(cert.getEncoded());            
            }
            return null;
        } catch (IOException | OperatorCreationException e) {
            fault(e, "cannot sign certificate");
            return null;
        } finally {
            done();
        }
    }

    public byte[] createCertificateX509(String hostname) {
        byte[] certificate;
        UUID host = UUID.valueOf(hostname);
        this.subjectUuid(host);

        try {
            certificate = this.build();
            return certificate;
        } catch (OperatorCreationException ex) {
            java.util.logging.Logger.getLogger(TagCertificateBuilder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public byte[] createCertificateX509FromJson(String hostname, String json) throws IOException, OperatorCreationException {
        this.selection = selectionFromJson(json);
        return createCertificateX509(hostname);
    }

}
