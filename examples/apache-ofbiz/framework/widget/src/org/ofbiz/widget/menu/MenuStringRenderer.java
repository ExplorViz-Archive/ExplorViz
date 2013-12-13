/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") throws IOException ; you may not use this file except in compliance
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
package org.ofbiz.widget.menu;

import java.io.IOException;
import java.util.Map;


/**
 * Widget Library - Form String Renderer interface
 */
public interface MenuStringRenderer {
    public void renderMenuItem(Appendable writer, Map<String, Object> context, ModelMenuItem menuItem) throws IOException ;
    public void renderMenuOpen(Appendable writer, Map<String, Object> context, ModelMenu menu) throws IOException ;
    public void renderMenuClose(Appendable writer, Map<String, Object> context, ModelMenu menu) throws IOException ;
    public void renderFormatSimpleWrapperOpen(Appendable writer, Map<String, Object> context, ModelMenu menu) throws IOException ;
    public void renderFormatSimpleWrapperClose(Appendable writer, Map<String, Object> context, ModelMenu menu) throws IOException ;
    public void renderFormatSimpleWrapperRows(Appendable writer, Map<String, Object> context, Object menu) throws IOException ;
    public void setUserLoginIdHasChanged(boolean b) throws IOException ;
    public void renderLink(Appendable writer, Map<String, Object> context, ModelMenuItem.Link link) throws IOException ;
    public void renderImage(Appendable writer, Map<String, Object> context, ModelMenuItem.Image image) throws IOException ;
}
