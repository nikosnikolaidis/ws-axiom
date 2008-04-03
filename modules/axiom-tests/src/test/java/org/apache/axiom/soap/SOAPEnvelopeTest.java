/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.soap;

import org.apache.axiom.om.OMAbstractFactory;


public class SOAPEnvelopeTest extends SOAPTestCase {
    protected SOAPEnvelope soap11Envelope;
    protected SOAPEnvelope soap12Envelope;

    public SOAPEnvelopeTest(String testName) {
        super(testName);
        soap11Envelope = soap11Factory.getDefaultEnvelope();
        soap12Envelope = soap12Factory.getDefaultEnvelope();
    }

    //SOAP 1.1 Envelope Test (Programaticaly Created)-----------------------------------------------
    public void testSOAP11GetHeader() {
        SOAPHeader header = soap11Envelope.getHeader();
        assertTrue("SOAP 1.1 Header Test : - Header local name mismatch",
                   header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.1 Header Test : - Header namespace mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    public void testSOAP11GetBody() {
        SOAPBody body = soap11Envelope.getBody();
        assertTrue("SOAP 1.1 Body Test : - Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.1 Body Test : - Body namespace mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    //SOAP 1.2 Envelope Test (Programaticaly Created)-------------------------------------------------
    public void testSOAP12GetHeader() {
        SOAPHeader header = soap12Envelope.getHeader();
        assertTrue("SOAP 1.2 Header Test : - Header local name mismatch",
                   header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.2 Header Test : - Header namespace mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    public void testSOAP12GetBody() {
        SOAPBody body = soap12Envelope.getBody();
        assertTrue("SOAP 1.2 Body Test : - Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.2 Body Test : - Body namespace mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    //SOAP 1.1 Envelope Test (With Parser)-----------------------------------------------------------------
    public void testSOAP11GetHeaderWithParser() {
        SOAPHeader header = soap11EnvelopeWithParser.getHeader();
        assertTrue("SOAP 1.1 Header Test : - Header local name mismatch",
                   header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.1 Header Test : - Header namespace mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    public void testSOAP11GetBodyWithParser() {
        SOAPBody body = soap11EnvelopeWithParser.getBody();
        assertTrue("SOAP 1.1 Body Test : - Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.1 Body Test : - Body namespace mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    //SOAP 1.2 Envelope Test (With Parser)--------------------------------------------------------------------
    public void testSOAP12GetHeaderWithParser() {
        SOAPHeader header = soap12EnvelopeWithParser.getHeader();
        assertTrue("SOAP 1.2 Header Test : - Header local name mismatch",
                   header.getLocalName().equals(SOAPConstants.HEADER_LOCAL_NAME));
        assertTrue("SOAP 1.2 Header Test : - Header namespace mismatch",
                   header.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    public void testSOAP12GetBodyWithParser() {
        SOAPBody body = soap12EnvelopeWithParser.getBody();
        assertTrue("SOAP 1.2 Body Test : - Body local name mismatch",
                   body.getLocalName().equals(SOAPConstants.BODY_LOCAL_NAME));
        assertTrue("SOAP 1.2 Body Test : - Body namespace mismatch",
                   body.getNamespace().getNamespaceURI().equals(
                           SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
    }

    public void testRandomSOAPHeader() {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope defaultEnvelope = soapFac.getDefaultEnvelope();
        defaultEnvelope.build();
        defaultEnvelope.getHeader().detach();

        soapFac.createSOAPHeader(defaultEnvelope);

    }

    // Make sure order of header/body creation doesn't matter
    public void testBodyHeaderOrder() throws Exception {
//        SOAPFactory soapFac = new org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory();
        SOAPFactory soapFac = new org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory();
        SOAPEnvelope env = soapFac.createSOAPEnvelope();
        SOAPBody body = soapFac.createSOAPBody(env);
        SOAPHeader header = soapFac.createSOAPHeader(env);
        assertTrue("Header isn't the first child!", env.getFirstElement() instanceof SOAPHeader);
    }
}
