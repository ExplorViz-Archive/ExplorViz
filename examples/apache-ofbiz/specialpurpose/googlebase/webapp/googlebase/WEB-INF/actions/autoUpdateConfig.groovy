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
import javolution.util.FastList
import org.ofbiz.base.util.UtilProperties

configList = []
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStoreId")
productStoreIds = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.option.outOfStock")
outOfStock = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.option.backInStock")
backInStock = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.webSiteUrl")
webSiteUrl = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.actionType")
actionType = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.statusId")
statusId = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.testMode")
testMode = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.webSiteMountPoint")
webSiteMountPoint = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.countryCode")
countryCode = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.trackingCodeId")
trackingCodeId = str.split(",")
str = UtilProperties.getPropertyValue("autoUpdateToGoogleBase.properties", "autoUpdateGoogleBase.productStore.allowRecommended")
allowRecommended = str.split(",")

productStoreIds.eachWithIndex{ productStoreId, i ->
    configMap = [:]
    configMap.productStoreId = productStoreId
    configMap.outOfStock = outOfStock[i]
    configMap.backInStock = backInStock[i]
    configMap.webSiteUrl = webSiteUrl[i]
    configMap.actionType = actionType[i]
    configMap.statusId = statusId[i]
    configMap.testMode = testMode[i]
    configMap.webSiteMountPoint = webSiteMountPoint[i]
    configMap.countryCode = countryCode[i]
    configMap.trackingCodeId = trackingCodeId[i]
    configMap.allowRecommended = allowRecommended[i]
    configList.add(configMap)
}
context.configList = configList
