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
package org.ofbiz.webapp.event;

import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralRuntimeException;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.webapp.control.ConfigXMLReader;
import org.ofbiz.webapp.control.RequestHandler;

/**
 * EventFactory - Event Handler Factory
 */
public class EventFactory {

    public static final String module = EventFactory.class.getName();

    protected RequestHandler requestHandler = null;
    protected ServletContext context = null;
    protected Map<String, EventHandler> handlers = null;

    public EventFactory(RequestHandler requestHandler) {
        handlers = FastMap.newInstance();
        this.requestHandler = requestHandler;
        this.context = requestHandler.getServletContext();

        // pre-load all event handlers
        try {
            this.preLoadAll();
        } catch (EventHandlerException e) {
            Debug.logError(e, module);
            throw new GeneralRuntimeException(e);
        }
    }

    private void preLoadAll() throws EventHandlerException {
        Set<String> handlers = this.requestHandler.getControllerConfig().getEventHandlerMap().keySet();
        if (handlers != null) {
            for (String type: handlers) {
                this.handlers.put(type, this.loadEventHandler(type));
            }
        }
    }

    public EventHandler getEventHandler(String type) throws EventHandlerException {
        // check if we are new / empty and add the default handler in
        if (handlers.size() == 0) {
            this.preLoadAll();
        }

        // attempt to get a pre-loaded handler
        EventHandler handler = handlers.get(type);

        if (handler == null) {
            synchronized (EventHandler.class) {
                handler = handlers.get(type);
                if (handler == null) {
                    handler = this.loadEventHandler(type);
                    handlers.put(type, handler);
                }
            }
            if (handler == null)
                throw new EventHandlerException("No handler found for type: " + type);
        }
        return handler;
    }

    public void clear() {
        handlers.clear();
    }

    private EventHandler loadEventHandler(String type) throws EventHandlerException {
        EventHandler handler = null;
        String handlerClass = this.requestHandler.getControllerConfig().getEventHandlerMap().get(type);
        if (handlerClass == null) {
            throw new EventHandlerException("Unknown handler type: " + type);
        }

        try {
            handler = (EventHandler) ObjectType.getInstance(handlerClass);
            handler.init(context);
        } catch (NoClassDefFoundError e) {
            throw new EventHandlerException("No class def found for handler [" + handlerClass + "]", e);
        } catch (ClassNotFoundException cnf) {
            throw new EventHandlerException("Cannot load handler class [" + handlerClass + "]", cnf);
        } catch (InstantiationException ie) {
            throw new EventHandlerException("Cannot get instance of the handler [" + handlerClass + "]", ie);
        } catch (IllegalAccessException iae) {
            throw new EventHandlerException(iae.getMessage(), iae);
        }
        return handler;
    }

    public static String runRequestEvent(HttpServletRequest request, HttpServletResponse response, String requestUri)
            throws EventHandlerException {
        ServletContext application = ((ServletContext) request.getAttribute("servletContext"));
        RequestHandler handler = (RequestHandler) application.getAttribute("_REQUEST_HANDLER_");
        ConfigXMLReader.ControllerConfig controllerConfig = handler.getControllerConfig();
        ConfigXMLReader.RequestMap requestMap = controllerConfig.getRequestMapMap().get(requestUri);
        return handler.runEvent(request, response, requestMap.event, requestMap, "unknown");
    }
}
