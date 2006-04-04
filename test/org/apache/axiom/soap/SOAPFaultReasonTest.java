/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axiom.soap;

import org.apache.axiom.om.OMAbstractFactory;

public class SOAPFaultReasonTest extends SOAPFaultReasonTestCase {

    public SOAPFaultReasonTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    //SOAP 1.1 Fault Reason Test (Programaticaly Created)
    public void testSOAP11SetSOAPText() {
        soap11FaultReason.addSOAPText(
                soap11Factory.createSOAPFaultText(soap11FaultReason));
        assertFalse(
                "SOAP 1.1 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap11FaultReason.getFirstSOAPText() == null);
        try {
            soap11FaultReason.addSOAPText(
                    soap12Factory.createSOAPFaultText(soap12FaultReason));
            fail("SOAP12FaultText should not be added to SOAP11FaultReason");

        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP11GetSOAPText() {
        assertTrue(
                "SOAP 1.1 FaultReason Test : - After creating SOAP11FaultReason, it has a SOAPFaultText",
                soap11FaultReason.getFirstSOAPText() == null);
        soap11FaultReason.addSOAPText(
                soap11Factory.createSOAPFaultText(soap11FaultReason));
        assertFalse(
                "SOAP 1.1 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap11FaultReason.getFirstSOAPText() == null);
    }

    //SOAP 1.2 Fault Reason Test (Programaticaly Created)
    public void testSOAP12SetSOAPText() {
        soap12FaultReason.addSOAPText(
                soap12Factory.createSOAPFaultText(soap12FaultReason));
        assertFalse(
                "SOAP 1.2 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap12FaultReason.getFirstSOAPText() == null);
        try {
            soap12FaultReason.addSOAPText(
                    soap11Factory.createSOAPFaultText(soap11FaultReason));
            fail("SOAP11FaultText should not be added to SOAP12FaultReason");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testSOAP12GetSOAPText() {
        assertTrue(
                "SOAP 1.2 FaultReason Test : - After creating SOAP12FaultReason, it has a SOAPFaultText",
                soap12FaultReason.getFirstSOAPText() == null);
        soap12FaultReason.addSOAPText(
                soap12Factory.createSOAPFaultText(soap12FaultReason));
        assertFalse(
                "SOAP 1.2 FaultReason Test : - After calling addSOAPText, getFirstSOAPText returns null",
                soap12FaultReason.getFirstSOAPText() == null);
    }

    //SOAP 1.1 Fault Reason Test (With Parser)
    public void testSOAP11GetSOAPTextWithParser() {
        assertFalse(
                "SOAP 1.1 FaultReason Test With Parser : - getFirstSOAPText method returns null",
                soap11FaultReasonWithParser.getFirstSOAPText() == null);
    }

    //SOAP 1.2 Fault Reason Test (With Parser)
    public void testSOAP12GetSOAPTextWithParser() {
        assertFalse(
                "SOAP 1.2 FaultReason Test With Parser : - getFirstSOAPText method returns null",
                soap12FaultReasonWithParser.getFirstSOAPText() == null);
    }

//    public void testMultipleSOAPReasonTexts() {
//        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
//
//        SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
////        soap
//    }
}
