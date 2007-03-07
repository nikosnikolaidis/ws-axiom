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

package org.apache.axiom.soap.impl.llom;

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPProcessingException;

public abstract class SOAPFaultCodeImpl extends SOAPElement implements SOAPFaultCode {


    protected SOAPFaultCodeImpl(String localName, OMNamespace ns, SOAPFactory factory) {
        super(localName, ns, factory);
    }

    protected SOAPFaultCodeImpl(OMNamespace ns, SOAPFactory factory) {
        this(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, ns, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent, String localName, OMXMLParserWrapper builder,
            SOAPFactory factory) {
        super(parent, localName, builder, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent, OMXMLParserWrapper builder,
            SOAPFactory factory) {
        this(parent, SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, builder,
                factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent,
                             String localName,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        super(parent,
                localName,
                extractNamespaceFromParent, factory);
    }

    public SOAPFaultCodeImpl(SOAPFault parent,
                             boolean extractNamespaceFromParent,
                             SOAPFactory factory) throws SOAPProcessingException {
        this(parent,
                SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME,
                extractNamespaceFromParent, factory);
    }

    public void setValue(SOAPFaultValue value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, value, value);
    }

    public SOAPFaultValue getValue() {
        return (SOAPFaultValue) ElementHelper.getChildWithName(this,
                SOAP12Constants.SOAP_FAULT_VALUE_LOCAL_NAME);
    }

    public void setSubCode(SOAPFaultSubCode value) throws SOAPProcessingException {
        ElementHelper.setNewElement(this, getSubCode(), value);
    }

    public SOAPFaultSubCode getSubCode() {
        return (SOAPFaultSubCode) ElementHelper.getChildWithName(this,
                SOAP12Constants.SOAP_FAULT_SUB_CODE_LOCAL_NAME);
    }

}
