/*
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
 */

import org.ofbiz.base.util.*;
import org.ofbiz.order.order.*;

orderId = request.getParameter("orderId");
orderItemSeqId = request.getParameter("orderItemSeqId");
orderContentTypeId = request.getParameter("orderContentTypeId");

order = null;
if (orderId && !orderItemSeqId) {
    order = delegator.findByPrimaryKey("OrderHeader", [orderId : orderId]);
}
if (orderId && orderItemSeqId) {
    order = delegator.findByPrimaryKey("OrderItem", [orderId : orderId, orderItemSeqId : orderItemSeqId]);
}

wrapper = OrderContentWrapper.makeOrderContentWrapper(order, request);
context.wrapper = wrapper;
context.orderContentTypeId = orderContentTypeId;
