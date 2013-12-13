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

import org.ofbiz.entity.*
import org.ofbiz.base.util.*
import org.ofbiz.content.report.*

shipmentId = request.getParameter("shipmentId");
shipment = delegator.findOne("Shipment", [shipmentId : shipmentId], false);

if (shipment) {
    shipmentPackageRouteSegs = shipment.getRelated("ShipmentPackageRouteSeg", null, ['shipmentRouteSegmentId', 'shipmentPackageSeqId']);
    shipmentPackageDatas = [] as LinkedList;
    if (shipmentPackageRouteSegs) {
        shipmentPackageRouteSegs.each { shipmentPackageRouteSeg ->
            shipmentPackages = shipmentPackageRouteSeg.getRelated("ShipmentPackage", null, ['shipmentPackageSeqId']);
            shipmentRouteSegment = shipmentPackageRouteSeg.getRelatedOne("ShipmentRouteSegment");
            if (shipmentPackages) {
                shipmentPackages.each { shipmentPackage ->
                    shipmentItemsDatas = [] as LinkedList;
                    shipmentPackageContents = shipmentPackage.getRelated("ShipmentPackageContent", null, ['shipmentItemSeqId']);
                    if (shipmentPackageContents) {
                        shipmentPackageContents.each { shipmentPackageContent ->
                            shipmentItemsData = [:];
                            packageQuantity = shipmentPackageContent.getDouble("quantity");
                            shipmentItem = shipmentPackageContent.getRelatedOne("ShipmentItem");
                            if (shipmentItem) {
                                shippedQuantity = shipmentItem.getDouble("quantity");
                                shipmentItemsData.shipmentItem = shipmentItem;
                                shipmentItemsData.shippedQuantity = shippedQuantity;
                                shipmentItemsData.packageQuantity = packageQuantity;
                                shipmentItemsDatas.add(shipmentItemsData);
                            }
                        }
                    }
                    shipmentPackageData = [:];
                    shipmentPackageData.shipmentPackage = shipmentPackage;
                    shipmentPackageData.shipmentRouteSegment = shipmentRouteSegment;
                    shipmentPackageData.shipmentItemsDatas = shipmentItemsDatas;
                    shipmentPackageDatas.add(shipmentPackageData);
                }
            }
        }
    }
    context.shipmentPackageDatas = shipmentPackageDatas;
}
context.shipmentId = shipmentId;
context.shipment = shipment;
