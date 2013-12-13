/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package org.ofbiz.content.cms;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.GeneralRuntimeException;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilJ2eeCompat;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.webapp.control.RequestHandler;
import org.ofbiz.webapp.website.WebSiteWorker;
import org.ofbiz.widget.form.FormStringRenderer;
import org.ofbiz.widget.form.MacroFormRenderer;
import org.ofbiz.widget.screen.ScreenRenderer;

import freemarker.template.TemplateException;


/**
 * CmsEvents
 */
public class CmsEvents {

    public static final String module = CmsEvents.class.getName();

    public static String cms(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ServletContext servletContext = request.getSession().getServletContext();
        HttpSession session = request.getSession();

        String webSiteId = (String) session.getAttribute("webSiteId");
        if (webSiteId == null) {
            webSiteId = WebSiteWorker.getWebSiteId(request);
            if (webSiteId == null) {
                request.setAttribute("_ERROR_MESSAGE_", "Not able to run CMS application; no webSiteId defined for WebApp!");
                return "error";
            }
        }

        // is this a default request or called from a defined request mapping
        String targetRequest = (String) request.getAttribute("targetRequestUri");
        String actualRequest = (String) request.getAttribute("thisRequestUri");
        if (targetRequest != null) {
            targetRequest = targetRequest.replaceAll("\\W", "");
        } else {
            targetRequest = "";
        }
        if (actualRequest != null) {
            actualRequest = actualRequest.replaceAll("\\W", "");
        } else {
            actualRequest = "";
        }

        // place holder for the content id
        String contentId = null;
        String mapKey = null;

        String pathInfo = null;

        // If an override view is present then use that in place of request.getPathInfo()
        String overrideViewUri = (String) request.getAttribute("_CURRENT_CHAIN_VIEW_");
        if (UtilValidate.isNotEmpty(overrideViewUri)) {
            pathInfo = overrideViewUri;
        } else {
            pathInfo = request.getPathInfo();
            if (targetRequest.equals(actualRequest) && pathInfo != null) {
                // was called directly -- path info is everything after the request
                String[] pathParsed = pathInfo.split("/", 3);
                if (pathParsed.length > 2) {
                    pathInfo = pathParsed[2];
                } else {
                    pathInfo = null;
                }
            } // if called through the default request, there is no request in pathinfo
        }

        // if path info is null; check for a default content
        if (pathInfo == null) {
            List<GenericValue> defaultContents = null;
            try {
                defaultContents = delegator.findByAndCache("WebSiteContent", UtilMisc.toMap("webSiteId", webSiteId,
                        "webSiteContentTypeId", "DEFAULT_PAGE"), UtilMisc.toList("-fromDate"));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            defaultContents = EntityUtil.filterByDate(defaultContents);
            GenericValue defaultContent = EntityUtil.getFirst(defaultContents);
            if (defaultContent != null) {
                pathInfo = defaultContent.getString("contentId");
            }
        }

        // check for path alias first
        if (pathInfo != null) {
            // clean up the pathinfo for parsing
            pathInfo = pathInfo.trim();
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            if (pathInfo.endsWith("/")) {
                pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
            }
            Debug.logInfo("Path INFO for Alias: " + pathInfo, module);

            GenericValue pathAlias = null;
            try {
                pathAlias = delegator.findByPrimaryKeyCache("WebSitePathAlias", UtilMisc.toMap("webSiteId", webSiteId, "pathAlias", pathInfo));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            if (pathAlias != null) {
                String alias = pathAlias.getString("aliasTo");
                contentId = pathAlias.getString("contentId");
                mapKey = pathAlias.getString("mapKey");
                if (contentId == null && UtilValidate.isNotEmpty(alias)) {
                    if (!alias.startsWith("/")) {
                       alias = "/" + alias;
                    }

                    RequestDispatcher rd = request.getRequestDispatcher(request.getServletPath() + alias);
                    try {
                        rd.forward(request, response);
                    } catch (ServletException e) {
                        Debug.logError(e, module);
                        return "error";
                    } catch (IOException e) {
                        Debug.logError(e, module);
                        return "error";
                    }

                    return null; // null to not process any views
                }
            }

            // process through CMS -- using the mapKey (for now)
            Locale locale = UtilHttp.getLocale(request);

            // get the contentId/mapKey from URL
            if (contentId == null) {
                if (Debug.verboseOn()) Debug.logVerbose("Current PathInfo: " + pathInfo, module);
                String[] pathSplit = pathInfo.split("/");
                if (Debug.verboseOn()) Debug.logVerbose("Split pathinfo: " + pathSplit.length, module);
                contentId = pathSplit[0];
                if (pathSplit.length > 1) {
                    mapKey = pathSplit[1];
                }
            }

            // verify the request content is associated with the current website
            int statusCode = -1;
            boolean hasErrorPage = false;

            try {
                statusCode = verifyContentToWebSite(delegator, webSiteId, contentId);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                throw new GeneralRuntimeException(e.getMessage(), e);
            }

            // We try to find a specific Error page for this website concerning the status code
            if (statusCode != HttpServletResponse.SC_OK) {
                List<GenericValue> errorContainers = null;
                try {
                    errorContainers = delegator.findByAndCache("WebSiteContent",
                            UtilMisc.toMap("webSiteId", webSiteId, "webSiteContentTypeId", "ERROR_ROOT"),
                            UtilMisc.toList("-fromDate"));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }

                errorContainers = EntityUtil.filterByDate(errorContainers);
                if (UtilValidate.isNotEmpty(errorContainers)) {
                    if (Debug.verboseOn()) Debug.logVerbose("Found error containers: " + errorContainers, module);
                    GenericValue errorContainer = EntityUtil.getFirst(errorContainers);

                    List<GenericValue> errorPages = null;
                    try {
                        errorPages = delegator.findByAnd("ContentAssocViewTo", UtilMisc.toMap("contentIdStart", errorContainer.getString("contentId"), "caContentAssocTypeId", "TREE_CHILD", "contentTypeId", "DOCUMENT", "caMapKey", String.valueOf(statusCode)));
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                    }
                    errorPages = EntityUtil.filterByDate(errorPages);
                    if (UtilValidate.isNotEmpty(errorPages)) {
                        if (Debug.verboseOn()) Debug.logVerbose("Found error pages " + statusCode + " : " + errorPages, module);
                        contentId = EntityUtil.getFirst(errorPages).getString("contentId");
                    } else {
                        if (Debug.verboseOn()) Debug.logVerbose("No specific error page, falling back to the Error Container for " + statusCode, module);
                        contentId = errorContainer.getString("contentId");
                    }
                    mapKey = null;
                    hasErrorPage=true;
                }
                // We try to find a generic content Error page concerning the status code
                if (!hasErrorPage) {
                    try {
                        GenericValue errorPage = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId", "CONTENT_ERROR_" + statusCode));
                        if (errorPage != null) {
                            Debug.logVerbose("Found generic page " + statusCode, module);
                            contentId = errorPage.getString("contentId");
                            mapKey = null;
                            hasErrorPage = true;
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                    }
                }

            }

            if (statusCode == HttpServletResponse.SC_OK || hasErrorPage) {
                // create the template map
                MapStack<String> templateMap = MapStack.create();
                ScreenRenderer.populateContextForRequest(templateMap, null, request, response, servletContext);
                templateMap.put("statusCode", statusCode);

                // make the link prefix
                ServletContext ctx = (ServletContext) request.getAttribute("servletContext");
                RequestHandler rh = (RequestHandler) ctx.getAttribute("_REQUEST_HANDLER_");
                templateMap.put("_REQUEST_HANDLER_", rh);

                response.setStatus(statusCode);

                // NOTE DEJ20080817: this is done in the ContentMapFacade class now to avoid problems with the jsessionid being in the middle of the URL and such
                //String contextLinkPrefix = rh.makeLink(request, response, "", true, false, true);
                //templateMap.put("_CONTEXT_LINK_PREFIX_", contextLinkPrefix);

                Writer writer;
                try {
                    // use UtilJ2eeCompat to get this setup properly
                    boolean useOutputStreamNotWriter = false;
                    if (servletContext != null) {
                        useOutputStreamNotWriter = UtilJ2eeCompat.useOutputStreamNotWriter(servletContext);
                    }
                    if (useOutputStreamNotWriter) {
                        ServletOutputStream ros = response.getOutputStream();
                        writer = new OutputStreamWriter(ros, "UTF-8");
                    } else {
                        writer = response.getWriter();
                    }
                    // TODO: replace "screen" to support dynamic rendering of different output
                    FormStringRenderer formStringRenderer = new MacroFormRenderer(UtilProperties.getPropertyValue("widget", "screen.formrenderer"), request, response);
                    templateMap.put("formStringRenderer", formStringRenderer);
                    //include DOCTYPE for cms screens
                    writer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
                    
                    // if use web analytics
                    List<GenericValue> webAnalytics = delegator.findByAnd("WebAnalyticsConfig", UtilMisc.toMap("webSiteId", webSiteId));
                    
                    // render
                    if (UtilValidate.isNotEmpty(webAnalytics) && hasErrorPage) {
                        ContentWorker.renderContentAsText(dispatcher, delegator, contentId, writer, templateMap, locale, "text/html", null, null, true, webAnalytics);
                    } else if (UtilValidate.isEmpty(mapKey)) {
                        ContentWorker.renderContentAsText(dispatcher, delegator, contentId, writer, templateMap, locale, "text/html", null, null, true);
                    } else {
                        ContentWorker.renderSubContentAsText(dispatcher, delegator, contentId, writer, mapKey, templateMap, locale, "text/html", true);
                    }

                } catch (TemplateException e) {
                    throw new GeneralRuntimeException("Error creating form renderer", e);
                } catch (IOException e) {
                    throw new GeneralRuntimeException("Error in the response writer/output stream: " + e.toString(), e);
                } catch (GeneralException e) {
                    throw new GeneralRuntimeException("Error rendering content: " + e.toString(), e);
                }

                return "success";
            } else {
                String contentName = null;
                String siteName = null;
                try {
                    GenericValue content = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId", contentId));
                    if (content != null && UtilValidate.isNotEmpty(content)) {
                        contentName = content.getString("contentName");
                    } else {
                        request.setAttribute("_ERROR_MESSAGE_", "Content: " + contentName + " [" + contentId + "] is not a publish point for the current website: [" + webSiteId + "]");
                        return "error";
                    }
                    siteName = delegator.findByPrimaryKeyCache("WebSite", UtilMisc.toMap("webSiteId", webSiteId)).getString("siteName");
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                request.setAttribute("_ERROR_MESSAGE_", "Content: " + contentName + " [" + contentId + "] is not a publish point for the current website: " + siteName + " [" + webSiteId + "]");
                return "error";
            }
        }
        String siteName = null;
        GenericValue webSite = null;
        try {
            webSite = delegator.findByPrimaryKeyCache("WebSite", UtilMisc.toMap("webSiteId", webSiteId));
            if (webSite != null) {
            	siteName = webSite.getString("siteName");
            }
            if (siteName == null) {
            	siteName = "Not specified";
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        if (webSite != null) {
        	request.setAttribute("_ERROR_MESSAGE_", "Not able to find a page to display for website: " + siteName + " [" + webSiteId + "] not even a default page!");
        } else {
        	request.setAttribute("_ERROR_MESSAGE_", "Not able to find a page to display, not even a default page AND the website entity record for WebSiteId:" + webSiteId + " could not be found");
        }
        return "error";
    }

    protected static int verifyContentToWebSite(Delegator delegator, String webSiteId, String contentId) throws GeneralException {
        // first check if the passed in contentId is a publish point for the web site
        List<GenericValue> publishPoints = null;
        boolean hadContent = false;
        try {
            publishPoints = delegator.findByAndCache("WebSiteContent",
                    UtilMisc.toMap("webSiteId", webSiteId, "contentId", contentId, "webSiteContentTypeId", "PUBLISH_POINT"),
                    UtilMisc.toList("-fromDate"));
        } catch (GenericEntityException e) {
            throw e;
        }
        if (UtilValidate.isNotEmpty(publishPoints)) {
            hadContent = true;
        }
        publishPoints = EntityUtil.filterByDate(publishPoints);
        if (UtilValidate.isNotEmpty(publishPoints)) {
            if (Debug.verboseOn()) Debug.logVerbose("Found publish points: " + publishPoints, module);
            return HttpServletResponse.SC_OK;
        } else {
            // the passed in contentId is not a publish point for the web site;
            // however we will publish its content if it is a node of one of the trees that have a publish point as the root
            List<GenericValue> topLevelContentValues = delegator.findByAndCache("WebSiteContent",
                UtilMisc.toMap("webSiteId", webSiteId, "webSiteContentTypeId", "PUBLISH_POINT"), UtilMisc.toList("-fromDate"));
            topLevelContentValues = EntityUtil.filterByDate(topLevelContentValues);

            if (topLevelContentValues != null) {
                for (GenericValue point: topLevelContentValues) {
                    int subContentStatusCode = verifySubContent(delegator, contentId, point.getString("contentId"));
                    if (subContentStatusCode == HttpServletResponse.SC_OK) {
                        return HttpServletResponse.SC_OK;
                    } else if (subContentStatusCode == HttpServletResponse.SC_GONE) {
                        hadContent = true;
                    }
                }
            }
        }
        int responseCode;
        if (hadContent) {
            responseCode = HttpServletResponse.SC_GONE;
        } else {
            responseCode = HttpServletResponse.SC_NOT_FOUND;
        }
        Debug.logWarning("Could not verify contentId [" + contentId + "] to webSiteId [" + webSiteId + "], returning code: " + responseCode, module);
        return responseCode;
    }

    protected static int verifySubContent(Delegator delegator, String contentId, String contentIdFrom) throws GeneralException {
        List<GenericValue> contentAssoc = delegator.findByAndCache("ContentAssoc", UtilMisc.toMap("contentId", contentIdFrom, "contentIdTo", contentId, "contentAssocTypeId", "SUB_CONTENT"));
        boolean hadContent = false;
        if (UtilValidate.isNotEmpty(contentAssoc)) {
            hadContent = true;
        }
        contentAssoc = EntityUtil.filterByDate(contentAssoc);
        if (UtilValidate.isEmpty(contentAssoc)) {
            List<GenericValue> assocs = delegator.findByAndCache("ContentAssoc", UtilMisc.toMap("contentId", contentIdFrom));
            assocs = EntityUtil.filterByDate(assocs);
            if (assocs != null) {
                for (GenericValue assoc : assocs) {
                    int subContentStatusCode = verifySubContent(delegator, contentId, assoc.getString("contentIdTo"));
                    if (subContentStatusCode == HttpServletResponse.SC_OK) {
                        return HttpServletResponse.SC_OK;
                    } else if (subContentStatusCode == HttpServletResponse.SC_GONE) {
                        hadContent = true;
                    }
                }
            }
        } else {
            if (Debug.verboseOn()) Debug.logVerbose("Found assocs: " + contentAssoc, module);
            return HttpServletResponse.SC_OK;
        }
        if (hadContent) return HttpServletResponse.SC_GONE;
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
