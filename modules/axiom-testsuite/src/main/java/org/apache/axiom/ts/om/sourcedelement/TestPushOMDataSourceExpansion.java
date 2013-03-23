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
package org.apache.axiom.ts.om.sourcedelement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.om.OMSourcedElement;
import org.apache.axiom.om.ds.AbstractPushOMDataSource;
import org.apache.axiom.ts.AxiomTestCase;
import org.apache.axiom.ts.om.sourcedelement.push.PushOMDataSourceScenario;

/**
 * Tests the expansion of an {@link OMSourcedElement} backed by an {@link AbstractPushOMDataSource}.
 */
public class TestPushOMDataSourceExpansion extends AxiomTestCase {
    private final PushOMDataSourceScenario scenario;
    
    public TestPushOMDataSourceExpansion(OMMetaFactory metaFactory, PushOMDataSourceScenario scenario) {
        super(metaFactory);
        this.scenario = scenario;
        scenario.addTestParameters(this);
    }

    protected void runTest() throws Throwable {
        final Map testContext = new HashMap();
        OMFactory factory = metaFactory.getOMFactory();
        OMElement element = factory.createOMElement(new AbstractPushOMDataSource() {
            public void serialize(XMLStreamWriter writer) throws XMLStreamException {
                scenario.serialize(writer, testContext);
            }
            
            public boolean isDestructiveWrite() {
                return false;
            }
        });
        Iterator it = scenario.getNamespaceContext().entrySet().iterator();
        if (it.hasNext()) {
            Map.Entry binding = (Map.Entry)it.next();
            OMElement parent = factory.createOMElement("parent", factory.createOMNamespace((String)binding.getValue(), (String)binding.getKey()));
            while (it.hasNext()) {
                binding = (Map.Entry)it.next();
                parent.declareNamespace(factory.createOMNamespace((String)binding.getValue(), (String)binding.getKey()));
            }
            parent.addChild(element);
        }
        scenario.validate(element, true, testContext);
    }
}
