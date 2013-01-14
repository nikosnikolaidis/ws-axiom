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
package org.apache.axiom.ts.om.xop;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.AbstractTestCase;
import org.apache.axiom.om.MIMEResource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.TestConstants;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.util.StAXParserConfiguration;
import org.apache.axiom.ts.AxiomTestCase;

public class TestSerialize extends AxiomTestCase {
    private final boolean base64;
    
    public TestSerialize(OMMetaFactory metaFactory, boolean base64) {
        super(metaFactory);
        this.base64 = base64;
        addTestProperty("base64", String.valueOf(base64));
    }

    protected void runTest() throws Throwable {
        MIMEResource testMessage = TestConstants.MTOM_MESSAGE;

        // Read in message: SOAPPart and 2 image attachments
        InputStream inStream = AbstractTestCase.getTestResource(testMessage.getName());
        Attachments attachments = new Attachments(inStream, testMessage.getContentType());
        
        OMOutputFormat oof = new OMOutputFormat();
        oof.setDoOptimize(true);
        oof.setMimeBoundary(testMessage.getBoundary());
        oof.setRootContentId(testMessage.getStart());
        if (base64) {
            oof.setProperty(OMOutputFormat.USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS, 
                    Boolean.TRUE);
        }
        
        // Write out the message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MTOMXMLStreamWriter writer = new MTOMXMLStreamWriter(baos, oof);
        
        OMXMLParserWrapper builder =
            OMXMLBuilderFactory.createOMBuilder(metaFactory.getOMFactory(), StAXParserConfiguration.DEFAULT, attachments);
        OMElement om = builder.getDocumentElement();
        om.serialize(writer);
        om.close(false);
        String out = baos.toString();
        
        if (base64) {
            // Do a quick check to see if the data is base64 and is
            // writing base64 compliant code.
            assertTrue(out.indexOf("base64") != -1);
            assertTrue(out.indexOf("GBgcGBQgHBwcJCQgKDBQNDAsL") != -1);
        } else {
            assertTrue(out.indexOf("base64") == -1);
        }
    }
}