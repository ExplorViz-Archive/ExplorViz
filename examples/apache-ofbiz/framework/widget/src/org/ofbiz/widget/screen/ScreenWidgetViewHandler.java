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
package org.ofbiz.widget.screen;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilJ2eeCompat;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.template.FreeMarkerWorker;
import org.ofbiz.webapp.view.AbstractViewHandler;
import org.ofbiz.webapp.view.ViewHandlerException;
import org.ofbiz.widget.html.HtmlFormRenderer;
import org.ofbiz.widget.html.HtmlScreenRenderer;
import org.ofbiz.widget.html.HtmlTreeRenderer;
import org.xml.sax.SAXException;

import freemarker.template.TemplateModelException;
import freemarker.template.utility.StandardCompress;

/**
 * Handles view rendering for the Screen Widget
 */
public class ScreenWidgetViewHandler extends AbstractViewHandler {

    public static final String module = ScreenWidgetViewHandler.class.getName();

    protected ServletContext servletContext = null;
    protected HtmlScreenRenderer htmlScreenRenderer = new HtmlScreenRenderer();

    /**
     * @see org.ofbiz.webapp.view.ViewHandler#init(javax.servlet.ServletContext)
     */
    public void init(ServletContext context) throws ViewHandlerException {
        this.servletContext = context;
    }

    /**
     * @see org.ofbiz.webapp.view.ViewHandler#render(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void render(String name, String page, String info, String contentType, String encoding, HttpServletRequest request, HttpServletResponse response) throws ViewHandlerException {
        Writer writer = null;
        try {
            // use UtilJ2eeCompat to get this setup properly
            boolean useOutputStreamNotWriter = false;
            if (this.servletContext != null) {
                useOutputStreamNotWriter = UtilJ2eeCompat.useOutputStreamNotWriter(this.servletContext);
            }
            if (useOutputStreamNotWriter) {
                ServletOutputStream ros = response.getOutputStream();
                writer = new OutputStreamWriter(ros, "UTF-8");
            } else {
                writer = response.getWriter();
            }

            // compress HTML output if configured to do so
            String compressHTML = UtilProperties.getPropertyValue("widget", "compress.HTML");
            if (UtilValidate.isEmpty(compressHTML) && this.servletContext != null) {
                compressHTML = (String) this.servletContext.getAttribute("compressHTML");
            }
            if ("true".equals(compressHTML)) {
                // StandardCompress defaults to a 2k buffer. That could be increased
                // to speed up output.
                writer = new StandardCompress().getWriter(writer, null);
            }

            ScreenRenderer screens = new ScreenRenderer(writer, null, htmlScreenRenderer);
            screens.populateContextForRequest(request, response, servletContext);
            // this is the object used to render forms from their definitions
            FreeMarkerWorker.getSiteParameters(request, screens.getContext());
            screens.getContext().put("formStringRenderer", new HtmlFormRenderer(request, response));
            screens.getContext().put("treeStringRenderer", new HtmlTreeRenderer());
            screens.getContext().put("simpleEncoder", StringUtil.htmlEncoder);
            htmlScreenRenderer.renderScreenBegin(writer, screens.getContext());
            screens.render(page);
            htmlScreenRenderer.renderScreenEnd(writer, screens.getContext());
            writer.flush();
        } catch (IOException e) {
            throw new ViewHandlerException("Error in the response writer/output stream: " + e.toString(), e);
        } catch (SAXException e) {
            throw new ViewHandlerException("XML Error rendering page: " + e.toString(), e);
        } catch (ParserConfigurationException e) {
            throw new ViewHandlerException("XML Error rendering page: " + e.toString(), e);
        } catch (GeneralException e) {
            throw new ViewHandlerException("Lower level error rendering page: " + e.toString(), e);
        } catch (TemplateModelException e) {
            throw new ViewHandlerException("Whitespace compression error rendering page: " + e.toString(), e);
        }
    }
}
