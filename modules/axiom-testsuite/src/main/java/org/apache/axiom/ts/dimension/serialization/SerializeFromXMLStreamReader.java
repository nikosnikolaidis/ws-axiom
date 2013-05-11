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
package org.apache.axiom.ts.dimension.serialization;

import java.io.StringWriter;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.testutils.suite.MatrixTestCase;

/**
 * Serializes an {@link OMContainer} by processing the result of
 * {@link OMContainer#getXMLStreamReader(boolean)}.
 */
public class SerializeFromXMLStreamReader implements SerializationStrategy {
    private final boolean cache;
    
    public SerializeFromXMLStreamReader(boolean cache) {
        this.cache = cache;
    }

    public void addTestParameters(MatrixTestCase testCase) {
        testCase.addTestParameter("serializationStrategy", "XMLStreamReader");
        testCase.addTestParameter("cache", String.valueOf(cache));
    }

    public XML serialize(OMContainer container) throws Exception {
        StringWriter sw = new StringWriter();
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXOMBuilder(container.getOMFactory(), container.getXMLStreamReader(cache));
        builder.getDocument().serialize(sw);
        builder.close();
        return new XMLAsString(sw.toString());
    }

    public boolean isPush() {
        return false;
    }

    public boolean isCaching() {
        return cache;
    }

    public boolean supportsInternalSubset() {
        return true;
    }
}