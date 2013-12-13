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
package org.ofbiz.entity.config;

import java.util.List;

import org.w3c.dom.Element;
import org.ofbiz.base.util.UtilXml;

import javolution.util.FastList;

/**
 * Misc. utility method for dealing with the entityengine.xml file
 *
 */
public abstract class ResourceInfo extends NamedInfo {
    public List<Element> resourceElements = FastList.newInstance();

    public ResourceInfo(Element element) {
        super(element);
        resourceElements.addAll(UtilXml.childElementList(element, "resource"));
    }

    public ResourceInfo(String name) {
        super(name);
    }
}
