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
package org.apache.axiom.om.impl.common.serializer;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMSerializable;
import org.apache.axiom.om.impl.common.IChildNode;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class StAXSerializer extends Serializer {
    private static final Log log = LogFactory.getLog(StAXSerializer.class);
    
    private final XMLStreamWriter writer;
    
    public StAXSerializer(OMSerializable contextNode, XMLStreamWriter writer) {
        super(contextNode);
        this.writer = writer;
    }

    public XMLStreamWriter getWriter() {
        return writer;
    }

    /**
     * Method serializeEndpart.
     *
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public void serializeEndpart() throws XMLStreamException {
        writer.writeEndElement();
    }

    protected void writeStartElement(String prefix, String namespaceURI, String localName) throws OutputException {
        try {
            writer.writeStartElement(prefix, localName, namespaceURI);
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }

    protected void writeNamespace(String prefix, String namespaceURI) throws OutputException {
        try {
            if (prefix.length() != 0) {
                writer.writeNamespace(prefix, namespaceURI);
            } else {
                writer.writeDefaultNamespace(namespaceURI);
            }
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }

    protected void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws OutputException {
        try {
            writer.writeAttribute(prefix, namespaceURI, localName, value);
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }

    public void serializeByPullStream(OMElement element, boolean cache) throws XMLStreamException {
        XMLStreamReader reader = element.getXMLStreamReader(cache);
        try {
            new StreamingOMSerializer().serialize(reader, writer);
        } finally {
            reader.close();
        }
    }

    public void serializeChildren(OMContainer container, boolean cache) throws XMLStreamException, OutputException {
        if (cache) {
            IChildNode child = (IChildNode)container.getFirstOMChild();
            while (child != null) {
                child.internalSerialize(this, true);
                child = (IChildNode)child.getNextOMSibling();
            }
        } else {
            IChildNode child = (IChildNode)container.getFirstOMChild();
            while (child != null) {
                if ((!(child instanceof OMElement)) || child.isComplete() ||
                        ((OMElement)child).getBuilder() == null) {
                    child.internalSerialize(this, false);
                } else {
                    OMElement element = (OMElement) child;
                    element.getBuilder().setCache(false);
                    serializeByPullStream(element, cache);
                }
                child = (IChildNode)child.getNextOMSiblingIfAvailable();
            }
        }
    }

    protected void setPrefix(String prefix, String namespaceURI) throws OutputException {
        try {
            if (prefix.length() == 0) {
                writer.setDefaultNamespace(namespaceURI);
            } else {
                writer.setPrefix(prefix, namespaceURI);
            }
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }

    /**
     * @param prefix 
     * @param namespace
     * @return true if the prefix is associated with the namespace in the current context
     */
    protected boolean isAssociated(String prefix, String namespace) throws OutputException {
        try {
            // The "xml" prefix is always (implicitly) associated. Returning true here makes sure that
            // we never write a declaration for the xml namespace. See AXIOM-37 for a discussion
            // of this issue.
            if ("xml".equals(prefix)) {
                return true;
            }
            
            // NOTE: Calling getNamespaceContext() on many XMLStreamWriter implementations is expensive.
            // Please use other writer methods first.
            
            // For consistency, convert null arguments.
            // This helps get around the parser implementation differences.
            // In addition, the getPrefix/getNamespace methods cannot be called with null parameters.
            prefix = (prefix == null) ? "" : prefix;
            namespace = (namespace == null) ? "" : namespace;
            
            if (namespace.length() > 0) {
                // QUALIFIED NAMESPACE
                // Get the namespace associated with the prefix
                String writerPrefix = writer.getPrefix(namespace);
                if (prefix.equals(writerPrefix)) {
                    return true;
                }
                
                // It is possible that the namespace is associated with multiple prefixes,
                // So try getting the namespace as a second step.
                if (writerPrefix != null) {
                    NamespaceContext nsContext = writer.getNamespaceContext();
                    if(nsContext != null) {
                        String writerNS = nsContext.getNamespaceURI(prefix);
                        return namespace.equals(writerNS);
                    }
                }
                return false;
            } else {
                // UNQUALIFIED NAMESPACE
                
                // Neither XML 1.0 nor XML 1.1 allow to associate a prefix with an unqualified name (see also AXIOM-372).
                if (prefix.length() > 0) {
                    throw new OMException("Invalid namespace declaration: Prefixed namespace bindings may not be empty.");  
                }
                
                // Get the namespace associated with the prefix.
                // It is illegal to call getPrefix with null, but the specification is not
                // clear on what happens if called with "".  So the following code is 
                // protected
                try {
                    String writerPrefix = writer.getPrefix("");
                    if (writerPrefix != null && writerPrefix.length() == 0) {
                        return true;
                    }
                } catch (Throwable t) {
                    if (log.isDebugEnabled()) {
                        log.debug("Caught exception from getPrefix(\"\"). Processing continues: " + t);
                    }
                }
                
                
                
                // Fallback to using the namespace context
                NamespaceContext nsContext = writer.getNamespaceContext();
                if (nsContext != null) {
                    String writerNS = nsContext.getNamespaceURI("");
                    if (writerNS != null && writerNS.length() > 0) {
                        return false;
                    }
                }
                return true;
            }
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }

    public void writeText(int type, String data) throws OutputException {
        try {
            if (type == OMNode.CDATA_SECTION_NODE) {
                writer.writeCData(data);
            } else {
                writer.writeCharacters(data);
            }
        } catch (XMLStreamException ex) {
            throw new OutputException(ex);
        }
    }
}
