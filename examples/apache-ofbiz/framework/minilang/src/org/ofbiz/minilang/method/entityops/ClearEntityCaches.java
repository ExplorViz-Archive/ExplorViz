/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.minilang.method.entityops;

import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangValidate;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Implements the &lt;clear-entity-caches&gt; element.
 * 
 * @see <a href="https://cwiki.apache.org/OFBADMIN/mini-language-reference.html#Mini-languageReference-{{%3Cclearentitycaches%3E}}">Mini-language Reference</a>
 */
public final class ClearEntityCaches extends MethodOperation {

    public ClearEntityCaches(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        if (MiniLangValidate.validationOn()) {
            NamedNodeMap nnm = element.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++) {
                String attributeName = nnm.item(i).getNodeName();
                MiniLangValidate.handleError("Attribute name \"" + attributeName + "\" is not valid.", simpleMethod, element);
            }
            MiniLangValidate.noChildElements(simpleMethod, element);
        }
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        methodContext.getDelegator().clearAllCaches();
        return true;
    }

    @Override
    public String toString() {
        return "<clear-entity-caches/>";
    }

    /**
     * A factory for the &lt;clear-entity-caches&gt; element.
     */
    public static final class ClearEntityCachesFactory implements Factory<ClearEntityCaches> {
        @Override
        public ClearEntityCaches createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new ClearEntityCaches(element, simpleMethod);
        }

        @Override
        public String getName() {
            return "clear-entity-caches";
        }
    }
}
