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
package org.ofbiz.order.order;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.content.data.DataResourceWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastMap;

/**
 * Order Events
 */
public class OrderEvents {

    public static final String module = OrderEvents.class.getName();

    public static String downloadDigitalProduct(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ServletContext application = session.getServletContext();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String dataResourceId = request.getParameter("dataResourceId");

        try {
            // has the userLogin.partyId ordered a product with DIGITAL_DOWNLOAD content associated for the given dataResourceId?
            List<GenericValue> orderRoleAndProductContentInfoList = delegator.findByAnd("OrderRoleAndProductContentInfo",
                    UtilMisc.toMap("partyId", userLogin.get("partyId"), "dataResourceId", dataResourceId, "productContentTypeId", "DIGITAL_DOWNLOAD", "statusId", "ITEM_COMPLETED"));

            if (orderRoleAndProductContentInfoList.size() == 0) {
                request.setAttribute("_ERROR_MESSAGE_", "No record of purchase for digital download found (dataResourceId=[" + dataResourceId + "]).");
                return "error";
            }

            GenericValue orderRoleAndProductContentInfo = orderRoleAndProductContentInfoList.get(0);

            // TODO: check validity based on ProductContent fields: useCountLimit, useTime/useTimeUomId

            if (orderRoleAndProductContentInfo.getString("mimeTypeId") != null) {
                response.setContentType(orderRoleAndProductContentInfo.getString("mimeTypeId"));
            }
            OutputStream os = response.getOutputStream();
            DataResourceWorker.streamDataResource(os, delegator, dataResourceId, "", application.getInitParameter("webSiteId"), UtilHttp.getLocale(request), application.getRealPath("/"));
            os.flush();
        } catch (GenericEntityException e) {
            String errMsg = "Error downloading digital product content: " + e.toString();
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        } catch (GeneralException e) {
            String errMsg = "Error downloading digital product content: " + e.toString();
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        } catch (IOException e) {
            String errMsg = "Error downloading digital product content: " + e.toString();
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }

        return "success";
    }

    public static String cancelSelectedOrderItems(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");

        Map<String, Object> resultMap = FastMap.newInstance();
        String  orderId = request.getParameter("orderId");
        String[] orderItemSeqIds = request.getParameterValues("selectedItem");

        if (orderItemSeqIds != null) {
            for (String orderItemSeqId : orderItemSeqIds) {
                try {
                    GenericValue orderItem = delegator.findOne("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId), false);
                    List<GenericValue> orderItemShipGroupAssocs = orderItem.getRelated("OrderItemShipGroupAssoc");
                    for (GenericValue orderItemShipGroupAssoc : orderItemShipGroupAssocs) {
                        GenericValue orderItemShipGroup = orderItemShipGroupAssoc.getRelatedOne("OrderItemShipGroup");
                        String shipGroupSeqId = orderItemShipGroup.getString("shipGroupSeqId");

                        Map<String, Object> contextMap = FastMap.newInstance();
                        contextMap.put("orderId", orderId);
                        contextMap.put("orderItemSeqId", orderItemSeqId);
                        contextMap.put("shipGroupSeqId", shipGroupSeqId);
                        contextMap.put("userLogin", userLogin);
                        try {
                            resultMap = dispatcher.runSync("cancelOrderItem", contextMap);

                            if (ServiceUtil.isError(resultMap)) {
                                String errorMessage = (String) resultMap.get("errorMessage");
                                Debug.logError(errorMessage, module);
                                request.setAttribute("_ERROR_MESSAGE_", errorMessage);
                                return "error";
                            }

                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            request.setAttribute("_ERROR_MESSAGE_", resultMap.get("errorMessage"));
                            return "error";
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return "error";
                }
            }
            return "success";
        } else {
            request.setAttribute("_ERROR_MESSAGE_", "No order item selected. Please select an order item to cancel");
            return "error";
        }
    }
}
