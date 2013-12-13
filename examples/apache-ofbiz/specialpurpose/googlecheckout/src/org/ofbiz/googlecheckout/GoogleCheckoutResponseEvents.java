/**
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
**/
package org.ofbiz.googlecheckout;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.entity.Delegator;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.LocalDispatcher;
import org.w3c.dom.Document;

import com.google.checkout.CheckoutException;
import com.google.checkout.notification.AuthorizationAmountNotification;
import com.google.checkout.notification.ChargeAmountNotification;
import com.google.checkout.notification.ChargebackAmountNotification;
import com.google.checkout.notification.NewOrderNotification;
import com.google.checkout.notification.OrderStateChangeNotification;
import com.google.checkout.notification.RefundAmountNotification;
import com.google.checkout.notification.RiskInformationNotification;
import com.google.checkout.util.Utils;

public class GoogleCheckoutResponseEvents {

    public static final String module = GoogleCheckoutResponseEvents.class.getName();

    public static String checkNotification(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        GoogleCheckoutHelper helper = new GoogleCheckoutHelper(dispatcher, delegator);

        // check and parse the document
        Document document = null;
        try {
            document = Utils.newDocumentFromInputStream(request.getInputStream());
        } catch (CheckoutException e) {
            Debug.logError(e, module);
            sendResponse(response, null, e);
        } catch (IOException e) {
            Debug.logError(e, module);
            sendResponse(response, null, e);
        }

        // check the document type and process
        if (document != null) {
            String nodeValue = document.getDocumentElement().getNodeName();
            if ("new-order-notification".equals(nodeValue)) {
                // handle create new order
                NewOrderNotification info = new NewOrderNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.createOrder(info, ProductStoreWorker.getProductStoreId(request), ProductStoreWorker.getStoreLocale(request));
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("order-state-change-notification".equals(nodeValue)) {
                OrderStateChangeNotification info = new OrderStateChangeNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processStateChange(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("risk-information-notification".equals(nodeValue)) {
                RiskInformationNotification info = new RiskInformationNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processRiskNotification(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("authorization-amount-notification".equals(nodeValue)) {
                AuthorizationAmountNotification info = new AuthorizationAmountNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processAuthNotification(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("charge-amount-notification".equals(nodeValue)) {
                ChargeAmountNotification info = new ChargeAmountNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processChargeNotification(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("chargeback-amount-notification".equals(nodeValue)) {
                ChargebackAmountNotification info = new ChargebackAmountNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processChargeBackNotification(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else if ("refund-amount-notification".equals(nodeValue)) {
                RefundAmountNotification info = new RefundAmountNotification(document);
                String serialNumber = info.getSerialNumber();
                try {
                    helper.processRefundNotification(info);
                    sendResponse(response, serialNumber, null);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    sendResponse(response, serialNumber, e);
                    return null;
                }
            } else {
                Debug.logWarning("Unsupported document type submitted by Google; [" + nodeValue + "] has not yet been implemented.", module);
            }
        }

        return null;
    }

    private static void sendResponse(HttpServletResponse response, String serialNumber, Exception error) {
        if (error != null) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.getMessage());
            } catch (IOException e) {
                Debug.logError(e, module);
            }
        } else {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                Debug.logError(e, module);
            }
            if (out != null) {
                out.println("<notification-acknowledgment xmlns=\"http://checkout.google.com/schema/2\" serial-number=\"" + serialNumber + "\"/>");
                out.close();
            }
        }
    }
}
