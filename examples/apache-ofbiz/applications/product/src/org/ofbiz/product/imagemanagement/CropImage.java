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
package org.ofbiz.product.imagemanagement;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import javolution.util.FastMap;

import org.jdom.JDOMException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class CropImage {

    public static final String module = CropImage.class.getName();
    public static final String resource = "ProductErrorUiLabels";

    public static Map<String, Object> imageCrop(DispatchContext dctx, Map<String, ? extends Object> context)
    throws IOException, JDOMException {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String nameOfThumb = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.nameofthumbnail"), context);
        
        String productId = (String) context.get("productId");
        String imageName = (String) context.get("imageName");
        String imageX = (String) context.get("imageX");
        String imageY = (String) context.get("imageY");
        String imageW = (String) context.get("imageW");
        String imageH = (String) context.get("imageH");
        
        if (UtilValidate.isNotEmpty(imageName)) {
            Map<String, Object> contentCtx = FastMap.newInstance();
            contentCtx.put("contentTypeId", "DOCUMENT");
            contentCtx.put("userLogin", userLogin);
            Map<String, Object> contentResult = FastMap.newInstance();
            try {
                contentResult = dispatcher.runSync("createContent", contentCtx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            Map<String, Object> contentThumb = FastMap.newInstance();
            contentThumb.put("contentTypeId", "DOCUMENT");
            contentThumb.put("userLogin", userLogin);
            Map<String, Object> contentThumbResult = FastMap.newInstance();
            try {
                contentThumbResult = dispatcher.runSync("createContent", contentThumb);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            String contentIdThumb = (String) contentThumbResult.get("contentId");
            String contentId = (String) contentResult.get("contentId");
            String filenameToUse = (String) contentResult.get("contentId") + ".jpg";
            String filenameTouseThumb = (String) contentResult.get("contentId") + nameOfThumb + ".jpg";
            
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
            String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.url"), context);
            BufferedImage bufImg = ImageIO.read(new File(imageServerPath + "/" + productId + "/" + imageName));
            
            int x = Integer.parseInt(imageX);
            int y = Integer.parseInt(imageY);
            int w = Integer.parseInt(imageW);
            int h = Integer.parseInt(imageH);
            
            BufferedImage bufNewImg = bufImg.getSubimage(x, y, w, h);
            String mimeType = imageName.substring(imageName.lastIndexOf(".") + 1);
            ImageIO.write((RenderedImage) bufNewImg, mimeType, new File(imageServerPath + "/" + productId + "/" + filenameToUse));
            
            double imgHeight = bufNewImg.getHeight();
            double imgWidth = bufNewImg.getWidth();
            
            Map<String, Object> resultResize = ImageManagementServices.resizeImageThumbnail(bufNewImg, imgHeight, imgWidth);
            ImageIO.write((RenderedImage) resultResize.get("bufferedImage"), mimeType, new File(imageServerPath + "/" + productId + "/" + filenameTouseThumb));
            
            String imageUrlResource = imageServerUrl + "/" + productId + "/" + filenameToUse;
            String imageUrlThumb = imageServerUrl + "/" + productId + "/" + filenameTouseThumb;
            
            ImageManagementServices.createContentAndDataResource(dctx, userLogin, filenameToUse, imageUrlResource, contentId, "image/jpeg");
            ImageManagementServices.createContentAndDataResource(dctx, userLogin, filenameTouseThumb, imageUrlThumb, contentIdThumb, "image/jpeg");
            
            Map<String, Object> createContentAssocMap = FastMap.newInstance();
            createContentAssocMap.put("contentAssocTypeId", "IMAGE_THUMBNAIL");
            createContentAssocMap.put("contentId", contentId);
            createContentAssocMap.put("contentIdTo", contentIdThumb);
            createContentAssocMap.put("userLogin", userLogin);
            createContentAssocMap.put("mapKey", "100");
            try {
                dispatcher.runSync("createContentAssoc", createContentAssocMap);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            Map<String, Object> productContentCtx = FastMap.newInstance();
            productContentCtx.put("productId", productId);
            productContentCtx.put("productContentTypeId", "IMAGE");
            productContentCtx.put("fromDate", UtilDateTime.nowTimestamp());
            productContentCtx.put("userLogin", userLogin);
            productContentCtx.put("contentId", contentId);
            productContentCtx.put("statusId", "IM_PENDING");
            try {
                dispatcher.runSync("createProductContent", productContentCtx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            Map<String, Object> contentApprovalCtx = FastMap.newInstance();
            contentApprovalCtx.put("contentId", contentId);
            contentApprovalCtx.put("userLogin", userLogin);
            try {
                dispatcher.runSync("createImageContentApproval", contentApprovalCtx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            String errMsg = "Please select Image.";
            Debug.logFatal(errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        String successMsg = "Crop image successfully.";
        Map<String, Object> result = ServiceUtil.returnSuccess(successMsg);
        return result;
    }
}
