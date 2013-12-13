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
package org.ofbiz.webapp.webdav;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.CachedClassLoader;
import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.security.Security;
import org.ofbiz.security.SecurityFactory;
import org.ofbiz.security.authz.Authorization;
import org.ofbiz.security.authz.AuthorizationFactory;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.LocalDispatcher;

/** Implements a WebDAV servlet. The servlet simply forwards WebDAV requests
 * to a <code>RequestHandlerFactory</code> instance, whose class is specified
 * in the web application's web.xml file:<p><code>
 * &lt;context-param&gt;<br>
 * &nbsp;&nbsp;&lt;param-name&gt;requestHandlerFactoryClass&lt;/param-name&gt;<br>
 * &nbsp;&nbsp;&lt;param-value&gt;com.mydomain.MyWebDAVFactory&lt;/param-value&gt;<br>
 * &lt;/context-param&gt;</code></p>
 */
@SuppressWarnings("serial")
public class WebDavServlet extends GenericServlet {

    public static final String module = WebDavServlet.class.getName();

    protected Authorization authz = null;
    protected Delegator delegator = null;
    protected LocalDispatcher dispatcher = null;
    protected RequestHandlerFactory handlerFactory = null;
    protected Security security = null;

    @Override
    public void init(ServletConfig config) throws ServletException{
        try {
            super.init(config);
            ClassLoader loader = new CachedClassLoader(Thread.currentThread().getContextClassLoader(), null);
            Thread.currentThread().setContextClassLoader(loader);
            ServletContext context = this.getServletContext();
            String delegatorName = context.getInitParameter("entityDelegatorName");
            this.delegator = DelegatorFactory.getDelegator(delegatorName);
            String dispatcherName = context.getInitParameter("localDispatcherName");
            this.dispatcher = GenericDispatcher.getLocalDispatcher(dispatcherName, this.delegator);
            this.security = SecurityFactory.getInstance(this.delegator);
            this.authz = AuthorizationFactory.getInstance(this.delegator);
            String factoryClassName = context.getInitParameter("requestHandlerFactoryClass");
            this.handlerFactory = (RequestHandlerFactory) loader.loadClass(factoryClassName).newInstance();
        } catch (Exception e) {
            Debug.logError(e, "Error while initializing WebDAV servlet: ", module);
            throw new ServletException(e);
        }
        if (Debug.verboseOn()) {
            StringBuilder buff = new StringBuilder("WebDAV servlet initialized: delegator = ");
            buff.append(this.delegator.getDelegatorName());
            buff.append(", dispatcher = ");
            buff.append(this.dispatcher.getName());
            buff.append(", security = ");
            buff.append(this.security.getClass().getName());
            buff.append(", authz = ");
            buff.append(this.authz.getClass().getName());
            buff.append(", handler factory = ");
            buff.append(this.handlerFactory.getClass().getName());
            Debug.logVerbose(buff.toString(), module);
        }
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute("delegator", this.delegator);
        request.setAttribute("dispatcher", this.dispatcher);
        request.setAttribute("security", this.security);
        request.setAttribute("authz", this.authz);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        RequestHandler handler = this.handlerFactory.getHandler(httpRequest.getMethod());
        try {
            handler.handleRequest(httpRequest, (HttpServletResponse) response, this.getServletContext());
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            HttpSession session = httpRequest.getSession();
            session.invalidate();
        }
    }

}
