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
package org.ofbiz.content.compdoc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.event.CoreEvents;


/**
 * CompDocEvents Class
 */
public class CompDocEvents {
    public static final String module = CompDocEvents.class.getName();

    /**
     *
     * @param request
     * @param response
     * @return
     *
     * Creates the topmost Content entity of a Composite Document tree.
     * Also creates an "empty" Composite Document Instance Content entity.
     * Creates ContentRevision/Item records for each, as well.
     */

    public static String persistRootCompDoc(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        Delegator delegator = (Delegator)request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Locale locale = UtilHttp.getLocale(request);
        HttpSession session = request.getSession();
        //Security security = (Security)request.getAttribute("security");
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String contentId = (String)paramMap.get("contentId");
        //String instanceContentId = null;

        if (UtilValidate.isNotEmpty(contentId)) {
            try {
                delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error running serviceName persistContentAndAssoc", module);
                String errMsg = UtilProperties.getMessage(CoreEvents.err_resource, "coreEvents.error_modelservice_for_srv_name", locale);
                request.setAttribute("_ERROR_MESSAGE_", "<li>" + errMsg + " [" + "persistContentAndAssoc" + "]: " + e.toString());
                return "error";
           }
        }

        ModelService modelService = null;
        try {
            modelService = dispatcher.getDispatchContext().getModelService("persistContentAndAssoc");
        } catch (GenericServiceException e) {
            String errMsg = "Error getting model service for serviceName, 'persistContentAndAssoc'. " + e.toString();
            Debug.logError(errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", "<li>" + errMsg + "</li>");
            return "error";
        }
        Map<String, Object> persistMap = modelService.makeValid(paramMap, ModelService.IN_PARAM);
        persistMap.put("userLogin", userLogin);
        try {
            Map<String, Object> persistResult = dispatcher.runSync("persistContentAndAssoc", persistMap);
            contentId = (String)persistResult.get("contentId");
            //request.setAttribute("contentId", contentId);
            for(Object obj : persistResult.keySet()) {
                Object val = persistResult.get(obj);
                request.setAttribute(obj.toString(), val);
            }
            // Update ContentRevision and ContentRevisonItem
            Map<String, Object> contentRevisionMap = FastMap.newInstance();
            contentRevisionMap.put("itemContentId", contentId);
            contentRevisionMap.put("contentId", contentId);
            contentRevisionMap.put("userLogin", userLogin);
            Map<String, Object> result = dispatcher.runSync("persistContentRevisionAndItem", contentRevisionMap);
            for(Object obj : result.keySet()) {
                Object val = result.get(obj);
                request.setAttribute(obj.toString(), val);
            }
            String errorMsg = ServiceUtil.getErrorMessage(result);
            if (UtilValidate.isNotEmpty(errorMsg)) {
                String errMsg = "Error running serviceName, 'persistContentRevisionAndItem'. " + errorMsg;
                Debug.logError(errMsg, module);
                request.setAttribute("_ERROR_MESSAGE_", "<li>" + errMsg + "</li>");
                return "error";
            }

        } catch (GenericServiceException e) {
            String errMsg = "Error running serviceName, 'persistContentAndAssoc'. " + e.toString();
            Debug.logError(errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", "<li>" + errMsg + "</li>");
            return "error";
        }
        return "success";
    }

    public static String padNumberWithLeadingZeros(Long num, Integer padLen) {
        String s = UtilFormatOut.formatPaddedNumber(num.longValue(), padLen.intValue());
        return s;
    }

    public static String genCompDocPdf(HttpServletRequest request, HttpServletResponse response) {
        String responseStr = "success";
        //ByteBuffer byteBuffer = null;
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        ServletContext servletContext = session.getServletContext();
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        String contentId = (String)paramMap.get("contentId");
        Locale locale = UtilHttp.getLocale(request);
        String rootDir = null;
        String webSiteId = null;
        String https = null;

        if (UtilValidate.isEmpty(rootDir)) {
            rootDir = servletContext.getRealPath("/");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            webSiteId = (String) servletContext.getAttribute("webSiteId");
        }
        if (UtilValidate.isEmpty(https)) {
            https = (String) servletContext.getAttribute("https");
        }

        Map<String, Object> mapIn = FastMap.newInstance();
        mapIn.put("contentId", contentId);
        mapIn.put("locale", locale);
        mapIn.put("rootDir", rootDir);
        mapIn.put("webSiteId", webSiteId);
        mapIn.put("https", https);
        mapIn.put("userLogin", userLogin);

        Map<String, Object> results = null;
        try {
            results = dispatcher.runSync("renderCompDocPdf", mapIn);
        } catch (ServiceAuthException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        } catch (GenericServiceException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        }

        if (ServiceUtil.isError(results)) {
            request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(results));
            return "error";
        }

        ByteBuffer outByteBuffer = (ByteBuffer) results.get("outByteBuffer");

        // setup content type
        String contentType = "application/pdf; charset=ISO-8859-1";

        ByteArrayInputStream bais = new ByteArrayInputStream(outByteBuffer.array());

        /*
        try {
            FileOutputStream fos = new FileOutputStream(FileUtil.getFile("/home/byersa/pdftest.pdf"));
            fos.write(outByteBuffer.getBytes());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        */
        try {
            UtilHttp.streamContentToBrowser(response, bais, outByteBuffer.limit(), contentType);
        } catch (IOException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        }
        return responseStr;
    }
    public static String genContentPdf(HttpServletRequest request, HttpServletResponse response) {
        String responseStr = "success";
        //ByteBuffer byteBuffer = null;
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        ServletContext servletContext = session.getServletContext();
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        String contentId = (String)paramMap.get("contentId");
        Locale locale = UtilHttp.getLocale(request);
        String rootDir = null;
        String webSiteId = null;
        String https = null;

        if (UtilValidate.isEmpty(rootDir)) {
            rootDir = servletContext.getRealPath("/");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            webSiteId = (String) servletContext.getAttribute("webSiteId");
        }
        if (UtilValidate.isEmpty(https)) {
            https = (String) servletContext.getAttribute("https");
        }

        Map<String, Object> mapIn = FastMap.newInstance();
        mapIn.put("contentId", contentId);
        mapIn.put("locale", locale);
        mapIn.put("rootDir", rootDir);
        mapIn.put("webSiteId", webSiteId);
        mapIn.put("https", https);
        mapIn.put("userLogin", userLogin);

        Map<String, Object> results = null;
        try {
            results = dispatcher.runSync("renderContentPdf", mapIn);
        } catch (ServiceAuthException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        } catch (GenericServiceException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        }

        if (ServiceUtil.isError(results)) {
            request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(results));
            return "error";
        }

        ByteBuffer outByteBuffer = (ByteBuffer) results.get("outByteBuffer");

        // setup content type
        String contentType = "application/pdf; charset=ISO-8859-1";

        ByteArrayInputStream bais = new ByteArrayInputStream(outByteBuffer.array());

        /*
        try {
            FileOutputStream fos = new FileOutputStream(FileUtil.getFile("/home/byersa/pdftest.pdf"));
            fos.write(outByteBuffer.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        */
        try {
            UtilHttp.streamContentToBrowser(response, bais, outByteBuffer.limit(), contentType);
        } catch (IOException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.toString());
            return "error";
        }
        return responseStr;
    }
}
