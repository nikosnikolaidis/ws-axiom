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

package org.apache.axiom.util.stax.xop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.ext.stax.datahandler.DataHandlerReader;

/**
 * {@link XMLStreamReader} wrapper that encodes XOP. It assumes that the underlying reader
 * implements the extension defined by {@link DataHandlerReader} so that it can identify the
 * information items to optimize (by looking for
 * {@link javax.xml.stream.XMLStreamConstants#CHARACTERS} events for which
 * {@link DataHandlerReader#isBinary()} returns <code>true</code>). The {@link DataHandler}
 * objects for the parts referenced by {@code xop:Include} element information items produced by
 * an instance of this class can be retrieved using the {@link #getDataHandler(String)} method.
 * <p>
 * Note that the primary purpose of this class is not to serialize an XML infoset to an XOP package
 * (this is better done using {@link XOPEncodingStreamWriter}), but rather to optimize interaction
 * (by exchanging {@link DataHandler} objects instead of base64 encoded representations) with
 * databinding frameworks that understand XOP, but that are not aware of the
 * {@link DataHandlerReader} extension.
 * <p>
 * This class defers loading of {@link DataHandler} objects until {@link #getDataHandler(String)} is
 * called, except if this is not supported by the underlying stream.
 */
public class XOPEncodingStreamReader extends XOPEncodingStreamWrapper implements XMLStreamReader {
    /**
     * Wrapper that adds the XOP namespace to another namespace context.
     */
    private static class NamespaceContextWrapper implements NamespaceContext {
        private static final List<String> xopPrefixList = Arrays.asList(new String[] {
                XOPConstants.DEFAULT_PREFIX });
        
        private final NamespaceContext parent;

        public NamespaceContextWrapper(NamespaceContext parent) {
            this.parent = parent;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return XOPConstants.DEFAULT_PREFIX.equals(prefix)
                    ? XOPConstants.NAMESPACE_URI
                    : parent.getNamespaceURI(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return XOPConstants.NAMESPACE_URI.equals(namespaceURI)
                    ? XOPConstants.DEFAULT_PREFIX
                    : parent.getPrefix(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            Iterator<String> prefixes = parent.getPrefixes(namespaceURI);
            if (XOPConstants.NAMESPACE_URI.equals(namespaceURI)) {
                if (!prefixes.hasNext()) {
                    return xopPrefixList.iterator();
                } else {
                    // This case is very unusual
                    List<String> prefixList = new ArrayList<String>();
                    do {
                        prefixList.add((String)prefixes.next());
                    } while (prefixes.hasNext());
                    prefixList.add(XOPConstants.DEFAULT_PREFIX);
                    return prefixList.iterator();
                }
            } else {
                return prefixes;
            }
        }
    }
    
    private static final int STATE_PASS_THROUGH = 0;
    private static final int STATE_XOP_INCLUDE_START_ELEMENT = 1;
    private static final int STATE_XOP_INCLUDE_END_ELEMENT = 2;
    
    private final XMLStreamReader parent;
    private final DataHandlerReader dataHandlerReader;
    private int state = STATE_PASS_THROUGH;
    private String currentContentID;

    /**
     * Constructor.
     * 
     * @param parent
     *            The XML stream to encode. The reader must implement the extension defined by
     *            {@link DataHandlerReader}.
     * @param contentIDGenerator
     *            used to generate content IDs for the binary content exposed as
     *            {@code xop:Include} element information items
     * @param optimizationPolicy
     *            the policy to apply to decide which binary content to optimize
     * 
     * @throws IllegalArgumentException
     *             if the provided {@link XMLStreamReader} doesn't implement the extension defined
     *             by {@link DataHandlerReader}
     */
    public XOPEncodingStreamReader(XMLStreamReader parent, ContentIDGenerator contentIDGenerator,
            OptimizationPolicy optimizationPolicy) {
        super(contentIDGenerator, optimizationPolicy);
        this.parent = parent;
        DataHandlerReader dataHandlerReader;
        try {
            dataHandlerReader = (DataHandlerReader)parent.getProperty(DataHandlerReader.PROPERTY);
        } catch (IllegalArgumentException ex) {
            dataHandlerReader = null;
        }
        if (dataHandlerReader == null) {
            throw new IllegalArgumentException("The supplied XMLStreamReader doesn't implement the DataHandlerReader extension");
        }
        this.dataHandlerReader = dataHandlerReader;
    }

    @Override
    public int next() throws XMLStreamException {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                state = STATE_XOP_INCLUDE_END_ELEMENT;
                return END_ELEMENT;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                state = STATE_PASS_THROUGH;
                currentContentID = null;
                // Fall through
            default:
                int event = parent.next();
                if (event == CHARACTERS && dataHandlerReader.isBinary()) {
                    String contentID;
                    try {
                        if (dataHandlerReader.isDeferred()) {
                            contentID = processDataHandler(
                                    dataHandlerReader.getDataHandlerProvider(),
                                    dataHandlerReader.getContentID(),
                                    dataHandlerReader.isOptimized());
                        } else {
                            contentID = processDataHandler(
                                    dataHandlerReader.getDataHandler(),
                                    dataHandlerReader.getContentID(),
                                    dataHandlerReader.isOptimized());
                        }
                    } catch (IOException ex) {
                        throw new XMLStreamException("Error while processing data handler", ex);
                    }
                    if (contentID != null) {
                        currentContentID = contentID;
                        state = STATE_XOP_INCLUDE_START_ELEMENT;
                        return START_ELEMENT;
                    } else {
                        return CHARACTERS;
                    }
                } else {
                    return event;
                }
        }
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return state == STATE_PASS_THROUGH ? parent.hasNext() : true;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                state = STATE_XOP_INCLUDE_END_ELEMENT;
                return END_ELEMENT;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                currentContentID = null;
                // Fall through
            default:
                return parent.nextTag();
        }
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (state == STATE_PASS_THROUGH) {
            parent.require(type, namespaceURI, localName);
        } else {
            if (state == STATE_XOP_INCLUDE_START_ELEMENT && type != START_ELEMENT
                    || state == STATE_XOP_INCLUDE_END_ELEMENT && type != END_ELEMENT
                    || namespaceURI != null && !namespaceURI.equals(XOPConstants.NAMESPACE_URI)
                    || localName != null && !localName.equals(XOPConstants.INCLUDE)) {
                throw new XMLStreamException();
            }
        }
    }

    @Override
    public Location getLocation() {
        return parent.getLocation();
    }

    @Override
    public void close() throws XMLStreamException {
        parent.close();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return parent.getProperty(name);
    }

    @Override
    public String getEncoding() {
        return parent.getEncoding();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return parent.getCharacterEncodingScheme();
    }

    @Override
    public String getVersion() {
        return parent.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return parent.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return parent.standaloneSet();
    }

    @Override
    public String getPIData() {
        return parent.getPIData();
    }

    @Override
    public String getPITarget() {
        return parent.getPITarget();
    }

    @Override
    public int getAttributeCount() {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                return 1;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeCount();
        }
    }

    @Override
    public String getAttributeLocalName(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return XOPConstants.HREF;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeLocalName(index);
        }
    }

    @Override
    public QName getAttributeName(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return new QName(XOPConstants.HREF);
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeName(index);
        }
    }

    @Override
    public String getAttributeNamespace(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return null;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeNamespace(index);
        }
    }

    @Override
    public String getAttributePrefix(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return null;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributePrefix(index);
        }
    }

    @Override
    public String getAttributeType(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return "CDATA";
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeType(index);
        }
    }

    @Override
    public String getAttributeValue(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                // We don't use full URL encoding here, because this might cause
                // interoperability issues. The specs (RFC 2111 and 2392) are not very clear
                // on which characters should be URL encoded, but one can consider that '%'
                // is the only really unsafe character.
                return "cid:" + currentContentID.replaceAll("%", "%25");
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeValue(index);
        }
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if (index != 0) {
                    throw new IllegalArgumentException();
                }
                return true;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.isAttributeSpecified(index);
        }
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                if ((namespaceURI == null || namespaceURI.length() == 0)
                        && localName.equals(XOPConstants.HREF)) {
                    return "cid:" + currentContentID;
                } else {
                    return null;
                }
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getAttributeValue(namespaceURI, localName);
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                state = STATE_XOP_INCLUDE_END_ELEMENT;
                return "";
            case STATE_XOP_INCLUDE_END_ELEMENT:
                throw new IllegalStateException();
            default:
                return parent.getElementText();
        }
    }

    @Override
    public int getEventType() {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT:
                return START_ELEMENT;
            case STATE_XOP_INCLUDE_END_ELEMENT:
                return END_ELEMENT;
            default:
                return parent.getEventType();
        }
    }

    @Override
    public String getNamespaceURI() {
        return state == STATE_PASS_THROUGH ? parent.getNamespaceURI() : XOPConstants.NAMESPACE_URI;
    }

    @Override
    public String getLocalName() {
        return state == STATE_PASS_THROUGH ? parent.getLocalName() : XOPConstants.INCLUDE;
    }

    @Override
    public String getPrefix() {
        return state == STATE_PASS_THROUGH ? parent.getPrefix() : XOPConstants.DEFAULT_PREFIX;
    }

    @Override
    public QName getName() {
        return state == STATE_PASS_THROUGH ? parent.getName() : XOPConstants.INCLUDE_QNAME;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        NamespaceContext ctx = parent.getNamespaceContext();
        if (state != STATE_PASS_THROUGH) {
            ctx = new NamespaceContextWrapper(ctx);
        }
        return ctx;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (state != STATE_PASS_THROUGH && XOPConstants.DEFAULT_PREFIX.equals(prefix)) {
            return XOPConstants.NAMESPACE_URI;
        } else {
            return parent.getNamespaceURI(prefix);
        }
    }

    @Override
    public int getNamespaceCount() {
        return state == STATE_PASS_THROUGH ? parent.getNamespaceCount() : 1;
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (state == STATE_PASS_THROUGH) {
            return parent.getNamespacePrefix(index);
        } else if (index != 0) {
            throw new IllegalArgumentException();
        } else {
            return XOPConstants.DEFAULT_PREFIX;
        }
    }
    
    @Override
    public String getNamespaceURI(int index) {
        if (state == STATE_PASS_THROUGH) {
            return parent.getNamespaceURI(index);
        } else if (index != 0) {
            throw new IllegalArgumentException();
        } else {
            return XOPConstants.NAMESPACE_URI;
        }
    }

    @Override
    public String getText() {
        if (state == STATE_PASS_THROUGH) {
            return parent.getText();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int getTextStart() {
        if (state == STATE_PASS_THROUGH) {
            return parent.getTextStart();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int getTextLength() {
        if (state == STATE_PASS_THROUGH) {
            return parent.getTextLength();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public char[] getTextCharacters() {
        if (state == STATE_PASS_THROUGH) {
            return parent.getTextCharacters();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        if (state == STATE_PASS_THROUGH) {
            return parent.getTextCharacters(sourceStart, target, targetStart, length);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean hasName() {
        return state == STATE_PASS_THROUGH ? parent.hasName() : true;
    }

    @Override
    public boolean hasText() {
        return state == STATE_PASS_THROUGH ? parent.hasText() : false;
    }

    @Override
    public boolean isCharacters() {
        return state == STATE_PASS_THROUGH ? parent.isCharacters() : false;
    }

    @Override
    public boolean isWhiteSpace() {
        return state == STATE_PASS_THROUGH ? parent.isWhiteSpace() : false;
    }

    @Override
    public boolean isStartElement() {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT: return true;
            case STATE_XOP_INCLUDE_END_ELEMENT: return false;
            default: return parent.isStartElement();
        }
    }

    @Override
    public boolean isEndElement() {
        switch (state) {
            case STATE_XOP_INCLUDE_START_ELEMENT: return false;
            case STATE_XOP_INCLUDE_END_ELEMENT: return true;
            default: return parent.isEndElement();
        }
    }
}
