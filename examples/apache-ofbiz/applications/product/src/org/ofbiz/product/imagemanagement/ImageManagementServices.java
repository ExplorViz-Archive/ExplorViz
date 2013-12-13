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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jdom.JDOMException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.FileUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.common.image.ImageTransform;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.event.EventHandlerException;


/**
 * Product Services
 */
public class ImageManagementServices {
    
    public static final String module = ImageManagementServices.class.getName();
    public static final String resource = "ProductErrorUiLabels";
    private static List<Map<String,Object>> josonMap = null;
    private static int imageCount = 0;
    private static String imagePath;
    
    public static Map<String, Object> addMultipleuploadForProduct(DispatchContext dctx, Map<String, ? extends Object> context)
    throws IOException, JDOMException {
        
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        productId = productId.trim();
        String productContentTypeId = (String) context.get("productContentTypeId");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        String uploadFileName = (String) context.get("_uploadedFile_fileName");
        String imageResize = (String) context.get("imageResize");
        Locale locale = (Locale) context.get("locale");
        
        if (UtilValidate.isNotEmpty(uploadFileName)) {
            String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.format");
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
            String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.url"), context);
            String rootTargetDirectory = imageServerPath;
            File rootTargetDir = new File(rootTargetDirectory);
            if (!rootTargetDir.exists()) {
                boolean created = rootTargetDir.mkdirs();
                if (!created) {
                    String errMsg = "Cannot create the target directory";
                    Debug.logFatal(errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
            
            String sizeType = null;
            if (UtilValidate.isNotEmpty(imageResize)) {
                sizeType = imageResize;
            }
            
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
            
            String contentId = (String) contentResult.get("contentId");
            result.put("contentFrameId", contentId);
            result.put("contentId", contentId);
            
            // File to use for original image
            FlexibleStringExpander filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
            String fileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "type", sizeType, "id", contentId));
            String filenameToUse = fileLocation;
            if (fileLocation.lastIndexOf("/") != -1) {
                filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
            }
            
            String fileContentType = (String) context.get("_uploadedFile_contentType");
            if (fileContentType.equals("image/pjpeg")) {
                fileContentType = "image/jpeg";
            } else if (fileContentType.equals("image/x-png")) {
                fileContentType = "image/png";
            }
            
            List<GenericValue> fileExtension = FastList.newInstance();
            try {
                fileExtension = delegator.findByAnd("FileExtension", UtilMisc.toMap("mimeTypeId", fileContentType ));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            GenericValue extension = EntityUtil.getFirst(fileExtension);
            if (extension != null) {
                filenameToUse += "." + extension.getString("fileExtensionId");
            }
            
            // Create folder product id.
            String targetDirectory = imageServerPath + "/" + productId;
            File targetDir = new File(targetDirectory);
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (!created) {
                    String errMsg = "Cannot create the target directory";
                    Debug.logFatal(errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
            
            File file = new File(imageServerPath + "/" + productId + "/" + uploadFileName);
            String imageName = null;
            imagePath = imageServerPath + "/" + productId + "/" + uploadFileName;
            file = checkExistsImage(file);
            if (UtilValidate.isNotEmpty(file)) {
                imageName = file.getPath();
                imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
            }
            
            if (UtilValidate.isEmpty(imageResize)) {
                // Create image file original to folder product id.
                try {
                    RandomAccessFile out = new RandomAccessFile(file, "rw");
                    out.write(imageData.array());
                    out.close();
                } catch (FileNotFoundException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                            "ProductImageViewUnableWriteFile", UtilMisc.toMap("fileName", file.getAbsolutePath()), locale));
                } catch (IOException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                            "ProductImageViewUnableWriteBinaryData", UtilMisc.toMap("fileName", file.getAbsolutePath()), locale));
                }
            }
            // Scale Image in different sizes 
            if (UtilValidate.isNotEmpty(imageResize)) {
                File fileOriginal = new File(imageServerPath + "/" + productId + "/" + imageName);
                fileOriginal = checkExistsImage(fileOriginal);
                uploadFileName = fileOriginal.getName();
                
                try {
                    RandomAccessFile outFile = new RandomAccessFile(fileOriginal, "rw");
                    outFile.write(imageData.array());
                    outFile.close();
                } catch (FileNotFoundException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                            "ProductImageViewUnableWriteFile", UtilMisc.toMap("fileName", fileOriginal.getAbsolutePath()), locale));
                } catch (IOException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                            "ProductImageViewUnableWriteBinaryData", UtilMisc.toMap("fileName", fileOriginal.getAbsolutePath()), locale));
                }
                
                Map<String, Object> resultResize = FastMap.newInstance();
                try {
                    resultResize.putAll(ImageManagementServices.scaleImageMangementInAllSize(context, imageName, sizeType, productId));
                } catch (IOException e) {
                    String errMsg = "Scale additional image in all different sizes is impossible : " + e.toString();
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                } catch (JDOMException e) {
                    String errMsg = "Errors occur in parsing ImageProperties.xml : " + e.toString();
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
            
            Map<String, Object> contentThumbnail = createContentThumbnail(dctx, context, userLogin, imageData, productId, imageName);
            String filenameToUseThumb = (String) contentThumbnail.get("filenameToUseThumb");
            String contentIdThumb = (String) contentThumbnail.get("contentIdThumb");
            
            String imageUrl = imageServerUrl + "/" + productId + "/" + imageName;
            String imageUrlThumb = imageServerUrl + "/" + productId + "/" + filenameToUseThumb;
            
            createContentAndDataResource(dctx, userLogin, imageName, imageUrl, contentId, fileContentType);
            createContentAndDataResource(dctx, userLogin, filenameToUseThumb, imageUrlThumb, contentIdThumb, fileContentType);
            
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
            productContentCtx.put("productContentTypeId", productContentTypeId);
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
            
            String autoApproveImage = UtilProperties.getPropertyValue("catalog", "image.management.autoApproveImage");
            if (autoApproveImage.equals("Y")) {
                Map<String, Object> autoApproveCtx = FastMap.newInstance();
                autoApproveCtx.put("contentId", contentId);
                autoApproveCtx.put("userLogin", userLogin);
                autoApproveCtx.put("checkStatusId", "IM_APPROVED");
                try {
                    dispatcher.runSync("updateStatusImageManagement", autoApproveCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        return result;
    }
    
    public static Map<String, Object> removeImageFileForImageManagement(DispatchContext dctx, Map<String, ? extends Object> context){
        String productId = (String) context.get("productId");
        String contentId = (String) context.get("contentId");
        String dataResourceName = (String) context.get("dataResourceName");
        
        try {
            if (UtilValidate.isNotEmpty(contentId)) {
                String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
                File file = new File(imageServerPath + "/" + productId + "/" + dataResourceName);
                file.delete();
            }
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> scaleImageMangementInAllSize(Map<String, ? extends Object> context, String filenameToUse, String resizeType, String productId)
        throws IllegalArgumentException, ImagingOpException, IOException, JDOMException {
        
        /* VARIABLES */
        Locale locale = (Locale) context.get("locale");
        List<String> sizeTypeList = null;
        if (UtilValidate.isNotEmpty(resizeType)) {
            sizeTypeList  = UtilMisc.toList(resizeType);
        } else {
            sizeTypeList = UtilMisc.toList("small","100x75", "150x112", "320x240", "640x480", "800x600", "1024x768", "1280x1024", "1600x1200");
        }
        
        int index;
        Map<String, Map<String, String>> imgPropertyMap = FastMap.newInstance();
        BufferedImage bufImg, bufNewImg;
        double imgHeight, imgWidth;
        Map<String, String> imgUrlMap = FastMap.newInstance();
        Map<String, Object> resultXMLMap = FastMap.newInstance();
        Map<String, Object> resultBufImgMap = FastMap.newInstance();
        Map<String, Object> resultScaleImgMap = FastMap.newInstance();
        Map<String, Object> result = FastMap.newInstance();
        
        /* ImageProperties.xml */
        String imgPropertyFullPath = System.getProperty("ofbiz.home") + "/applications/product/config/ImageProperties.xml";
        resultXMLMap.putAll(ImageTransform.getXMLValue(imgPropertyFullPath, locale));
        if (resultXMLMap.containsKey("responseMessage") && resultXMLMap.get("responseMessage").equals("success")) {
            imgPropertyMap.putAll(UtilGenerics.<Map<String, Map<String, String>>>cast(resultXMLMap.get("xml")));
        } else {
            String errMsg = UtilProperties.getMessage(resource, "ScaleImage.unable_to_parse", locale) + " : ImageProperties.xml";
            Debug.logError(errMsg, module);
            result.put("errorMessage", errMsg);
            return result;
        }
        
        /* IMAGE */
        // get Name and Extension
        index = filenameToUse.lastIndexOf(".");
        String imgExtension = filenameToUse.substring(index + 1);
        // paths
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
        String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.url"), context);
        
        
        /* get original BUFFERED IMAGE */
        resultBufImgMap.putAll(ImageTransform.getBufferedImage(imageServerPath + "/" + productId + "/" + filenameToUse, locale));
        
        if (resultBufImgMap.containsKey("responseMessage") && resultBufImgMap.get("responseMessage").equals("success")) {
            bufImg = (BufferedImage) resultBufImgMap.get("bufferedImage");
            
            // get Dimensions
            imgHeight = (double) bufImg.getHeight();
            imgWidth = (double) bufImg.getWidth();
            if (imgHeight == 0.0 || imgWidth == 0.0) {
                String errMsg = UtilProperties.getMessage(resource, "ScaleImage.one_current_image_dimension_is_null", locale) + " : imgHeight = " + imgHeight + " ; imgWidth = " + imgWidth;
                Debug.logError(errMsg, module);
                result.put("errorMessage", errMsg);
                return result;
            }
            
            /* scale Image for each Size Type */
            for(String sizeType : sizeTypeList) {
                resultScaleImgMap.putAll(ImageTransform.scaleImage(bufImg, imgHeight, imgWidth, imgPropertyMap, sizeType, locale));
                
                if (resultScaleImgMap.containsKey("responseMessage") && resultScaleImgMap.get("responseMessage").equals("success")) {
                    bufNewImg = (BufferedImage) resultScaleImgMap.get("bufferedImage");
                    
                    // write the New Scaled Image
                    
                    String targetDirectory = imageServerPath + "/" + productId;
                    File targetDir = new File(targetDirectory);
                    if (!targetDir.exists()) {
                        boolean created = targetDir.mkdirs();
                        if (!created) {
                            String errMsg = UtilProperties.getMessage(resource, "ScaleImage.unable_to_create_target_directory", locale) + " - " + targetDirectory;
                            Debug.logFatal(errMsg, module);
                            return ServiceUtil.returnError(errMsg);
                        }
                    }
                    
                    // write new image
                    try {
                        ImageIO.write((RenderedImage) bufNewImg, imgExtension, new File(imageServerPath + "/" + productId + "/" + filenameToUse));
                        File deleteFile = new File(imageServerPath + "/"  + filenameToUse);
                        deleteFile.delete();
                        //FIXME can be removed ?
                        //  boolean check = deleteFile.delete();
                    } catch (IllegalArgumentException e) {
                        String errMsg = UtilProperties.getMessage(resource, "ScaleImage.one_parameter_is_null", locale) + e.toString();
                        Debug.logError(errMsg, module);
                        result.put("errorMessage", errMsg);
                        return result;
                    } catch (IOException e) {
                        String errMsg = UtilProperties.getMessage(resource, "ScaleImage.error_occurs_during_writing", locale) + e.toString();
                        Debug.logError(errMsg, module);
                        result.put("errorMessage", errMsg);
                        return result;
                    }
                    
                    /* write Return Result */
                    String imageUrl = imageServerUrl + "/" + productId + "/" + filenameToUse;
                    imgUrlMap.put(sizeType, imageUrl);
                    
                } // scaleImgMap
            } // sizeIter
            
            result.put("responseMessage", "success");
            result.put("imageUrlMap", imgUrlMap);
            result.put("original", resultBufImgMap);
            return result;
            
        } else {
            String errMsg = UtilProperties.getMessage(resource, "ScaleImage.unable_to_scale_original_image", locale) + " : " + filenameToUse;
            Debug.logError(errMsg, module);
            result.put("errorMessage", errMsg);
            return ServiceUtil.returnError(errMsg);
        }
    }
   
    public static Map<String, Object> createContentAndDataResource(DispatchContext dctx, GenericValue userLogin, String filenameToUse, String imageUrl, String contentId, String fileContentType){
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        Map<String, Object> dataResourceCtx = FastMap.newInstance();
        
        dataResourceCtx.put("objectInfo", imageUrl);
        dataResourceCtx.put("dataResourceName", filenameToUse);
        dataResourceCtx.put("userLogin", userLogin);
        dataResourceCtx.put("dataResourceTypeId", "IMAGE_OBJECT");
        dataResourceCtx.put("mimeTypeId", fileContentType);
        dataResourceCtx.put("isPublic", "Y");
        
        Map<String, Object> dataResourceResult = FastMap.newInstance();
        try {
            dataResourceResult = dispatcher.runSync("createDataResource", dataResourceCtx);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        String dataResourceId = (String) dataResourceResult.get("dataResourceId");
        result.put("dataResourceFrameId", dataResourceId);
        result.put("dataResourceId", dataResourceId);
        
        Map<String, Object> contentUp = FastMap.newInstance();
        contentUp.put("contentId", contentId);
        contentUp.put("dataResourceId", dataResourceResult.get("dataResourceId"));
        contentUp.put("contentName", filenameToUse);
        contentUp.put("userLogin", userLogin);
        try {
            dispatcher.runSync("updateContent", contentUp);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        GenericValue content = null;
        try {
            content = delegator.findOne("Content", UtilMisc.toMap("contentId", contentId), false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        if (content != null) {
            GenericValue dataResource = null;
            try {
                dataResource = content.getRelatedOne("DataResource");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            if (dataResource != null) {
                dataResourceCtx.put("dataResourceId", dataResource.getString("dataResourceId"));
                try {
                    dispatcher.runSync("updateDataResource", dataResourceCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        return result;
    }
    
    public static Map<String, Object> createContentThumbnail(DispatchContext dctx, Map<String, ? extends Object> context, GenericValue userLogin, ByteBuffer imageData, String productId, String imageName){
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //FIXME can be removed ?
        // String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.format");
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
        String nameOfThumb = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.nameofthumbnail"), context);
        
        // Create content for thumbnail
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
        result.put("contentIdThumb", contentIdThumb);
        
        /*// File to use for image thumbnail
        FlexibleStringExpander filenameExpanderThumb = FlexibleStringExpander.getInstance(imageFilenameFormat);
        String fileLocationThumb = filenameExpanderThumb.expandString(UtilMisc.toMap("location", "products", "type", "small", "id", contentIdThumb));
        String filenameToUseThumb = fileLocationThumb;
        if (fileLocationThumb.lastIndexOf("/") != -1) {
            filenameToUseThumb = fileLocationThumb.substring(fileLocationThumb.lastIndexOf("/") + 1);
        }
        
        String fileContentType = (String) context.get("_uploadedFile_contentType");
        if (fileContentType.equals("image/pjpeg")) {
            fileContentType = "image/jpeg";
        } else if (fileContentType.equals("image/x-png")) {
            fileContentType = "image/png";
        }
        
        List<GenericValue> fileExtensionThumb = FastList.newInstance();
        try {
            fileExtensionThumb = delegator.findByAnd("FileExtension", UtilMisc.toMap("mimeTypeId", fileContentType));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        GenericValue extensionThumb = EntityUtil.getFirst(fileExtensionThumb);
        if (extensionThumb != null) {
            filenameToUseThumb += "." + extensionThumb.getString("fileExtensionId");
        }*/
        //String uploadFileName = (String) context.get("_uploadedFile_fileName");
        String filenameToUseThumb = imageName.substring(0 , imageName.indexOf(".")) + nameOfThumb;
        String fileContentType = (String) context.get("_uploadedFile_contentType");
        if (fileContentType.equals("image/pjpeg")) {
            fileContentType = "image/jpeg";
        } else if (fileContentType.equals("image/x-png")) {
            fileContentType = "image/png";
        }
        
        List<GenericValue> fileExtensionThumb = FastList.newInstance();
        try {
            fileExtensionThumb = delegator.findByAnd("FileExtension", UtilMisc.toMap("mimeTypeId", fileContentType));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        GenericValue extensionThumb = EntityUtil.getFirst(fileExtensionThumb);
        if (extensionThumb != null) {
            
            filenameToUseThumb += "." + extensionThumb.getString("fileExtensionId");
        }
        result.put("filenameToUseThumb", filenameToUseThumb);
        // Create image file thumbnail to folder product id.
        File fileOriginalThumb = new File(imageServerPath + "/" + productId + "/" + filenameToUseThumb);
        try {
            RandomAccessFile outFileThumb = new RandomAccessFile(fileOriginalThumb, "rw");
            outFileThumb.write(imageData.array());
            outFileThumb.close();
        } catch (FileNotFoundException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "ProductImageViewUnableWriteFile", 
                    UtilMisc.toMap("fileName", fileOriginalThumb.getAbsolutePath()), locale));
        } catch (IOException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
                    "ProductImageViewUnableWriteBinaryData", 
                    UtilMisc.toMap("fileName", fileOriginalThumb.getAbsolutePath()), locale));
        }
        
        Map<String, Object> resultResizeThumb = FastMap.newInstance();
        try {
            resultResizeThumb.putAll(ImageManagementServices.scaleImageMangementInAllSize(context, filenameToUseThumb, "thumbnail", productId));
        } catch (IOException e) {
            String errMsg = "Scale additional image in all different sizes is impossible : " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } catch (JDOMException e) {
            String errMsg = "Errors occur in parsing ImageProperties.xml : " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        return result;
    }
    
    public static Map<String, Object> resizeImageThumbnail(BufferedImage bufImg, double imgHeight, double imgWidth) {
        
        /* VARIABLES */
        BufferedImage bufNewImg;
        double defaultHeight, defaultWidth, scaleFactor;
        Map<String, Object> result = FastMap.newInstance();
        
        /* DIMENSIONS from ImageProperties */
        defaultHeight = 100;
        defaultWidth = 100;
        
        /* SCALE FACTOR */
        // find the right Scale Factor related to the Image Dimensions
        if (imgHeight > imgWidth) {
            scaleFactor = defaultHeight / imgHeight;
            
            // get scaleFactor from the smallest width
            if (defaultWidth < (imgWidth * scaleFactor)) {
                scaleFactor = defaultWidth / imgWidth;
            }
        } else {
            scaleFactor = defaultWidth / imgWidth;
            // get scaleFactor from the smallest height
            if (defaultHeight < (imgHeight * scaleFactor)) {
                scaleFactor = defaultHeight / imgHeight;
            }
        }
        
        int bufImgType;
        if (BufferedImage.TYPE_CUSTOM == bufImg.getType()) {
            // apply a type for image majority
            bufImgType = BufferedImage.TYPE_INT_ARGB_PRE;
        } else {
            bufImgType = bufImg.getType();
        }
        
        // scale original image with new size
        Image newImg = bufImg.getScaledInstance((int) (imgWidth * scaleFactor), (int) (imgHeight * scaleFactor), Image.SCALE_SMOOTH);
        
        bufNewImg = ImageTransform.toBufferedImage(newImg, bufImgType);
        
        result.put("bufferedImage", bufNewImg);
        result.put("scaleFactor", scaleFactor);
        return result;
    }
    
    public static String multipleUploadImage(HttpServletRequest request, HttpServletResponse response) throws IOException, JDOMException {
        HttpSession session = request.getSession(true);
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        Map<String, String> formInput = FastMap.newInstance();
        ServletFileUpload fu = new ServletFileUpload(new DiskFileItemFactory(10240, FileUtil.getFile("runtime/tmp")));
        List<FileItem> lst = null;
        try {
           lst = UtilGenerics.checkList(fu.parseRequest(request));
        } catch (FileUploadException e4) {
            return e4.getMessage();
        }
                
        FileItem fi = null;
        FileItem imageFi = null;
        byte[] imageBytes = {};
        for (int i=0; i < lst.size(); i++) {
            fi = lst.get(i);
            String fieldName = fi.getFieldName();
            if (fi.isFormField()) {
                String fieldStr = fi.getString();
                formInput.put(fieldName, fieldStr);
            } else if (fieldName.startsWith("imageData")) {
                Map<String, Object> passedParams = FastMap.newInstance();
                Map<String, Object> contentLength = FastMap.newInstance();
                if(josonMap == null){
                     josonMap = FastList.newInstance();
                }
                imageFi = fi;
                String fileName = fi.getName();
                String contentType = fi.getContentType();
                imageBytes = imageFi.get();
                ByteBuffer byteWrap = ByteBuffer.wrap(imageBytes);
                passedParams.put("userLogin", userLogin);
                passedParams.put("productId", formInput.get("productId"));
                passedParams.put("productContentTypeId", "IMAGE");
                passedParams.put("_uploadedFile_fileName", fileName);
                passedParams.put("_uploadedFile_contentType", contentType);
                passedParams.put("uploadedFile", byteWrap);
                passedParams.put("imageResize", formInput.get("imageResize"));
                contentLength.put("imageSize", imageFi.getSize());
                josonMap.add(contentLength);
                
                if (passedParams.get("productId") != null) {
                    try {
                        dispatcher.runSync("addMultipleuploadForProduct", passedParams);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return e.getMessage();
                    }
                }
                
            }
        }
        return "success";
    }
    
    public static String progressUploadImage(HttpServletRequest request, HttpServletResponse response) throws EventHandlerException{
        toJsonObjectList(josonMap,response);
        josonMap.clear();
        return "success";
    }
    
    public static void toJsonObject(Map<String,Object> attrMap, HttpServletResponse response){
        JSONObject json = JSONObject.fromObject(attrMap);
        String jsonStr = json.toString();
        if (jsonStr == null) {
            Debug.logError("JSON Object was empty; fatal error!",module);
        }
        // set the X-JSON content type
        response.setContentType("application/json");
        // jsonStr.length is not reliable for unicode characters
        try {
            response.setContentLength(jsonStr.getBytes("UTF8").length);
        } catch (UnsupportedEncodingException e) {
            Debug.logError("Problems with Json encoding",module);
        }
        // return the JSON String
        Writer out;
        try {
            out = response.getWriter();
            out.write(jsonStr);
            out.flush();
        } catch (IOException e) {
            Debug.logError("Unable to get response writer",module);
        }
    }
    
    public static void toJsonObjectList(List<Map<String,Object>> list, HttpServletResponse response) throws EventHandlerException {
        JSONObject json = null;
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        if (list != null) {
            for (Map<String,Object> val : list) {
                json = new JSONObject();
                for (String rowKey: val.keySet()) {
                    json.put(rowKey, val.get(rowKey));
                }
                jsonList.add(json);
            }
            String jsonStr = jsonList.toString();
            if (jsonStr == null) {
                throw new EventHandlerException("JSON Object was empty; fatal error!");
            }
            // set the X-JSON content type
            response.setContentType("application/json");
            // jsonStr.length is not reliable for unicode characters
            try {
                response.setContentLength(jsonStr.getBytes("UTF8").length);
            } catch (UnsupportedEncodingException e) {
                throw new EventHandlerException("Problems with Json encoding", e);
            }
            // return the JSON String
            Writer out;
            try {
                out = response.getWriter();
                out.write(jsonStr);
                out.flush();
            } catch (IOException e) {
                throw new EventHandlerException("Unable to get response writer", e);
            } 
        }
    }
    
    public static File checkExistsImage(File file) {
        if (!file.exists()) {
            imageCount = 0;
            imagePath = null;
            return file;
        }
        imageCount++;
        String filePath = imagePath.substring(0, imagePath.indexOf("."));
        String type = imagePath.substring(imagePath.indexOf(".") + 1);
        file = new File(filePath + "(" + imageCount + ")." + type);
        return checkExistsImage(file);
    }
    
    public static Map<String, Object> resizeImage(BufferedImage bufImg, double imgHeight, double imgWidth, double resizeHeight, double resizeWidth) {
        
        /* VARIABLES */
        BufferedImage bufNewImg;
        double defaultHeight, defaultWidth, scaleFactor;
        Map<String, Object> result = FastMap.newInstance();
        
        /* DIMENSIONS from ImageProperties */
        defaultHeight = resizeHeight;
        defaultWidth = resizeWidth;
        
        /* SCALE FACTOR */
        // find the right Scale Factor related to the Image Dimensions
        if (imgHeight > imgWidth) {
            scaleFactor = defaultHeight / imgHeight;
            
            // get scaleFactor from the smallest width
            if (defaultWidth < (imgWidth * scaleFactor)) {
                scaleFactor = defaultWidth / imgWidth;
            }
        } else {
            scaleFactor = defaultWidth / imgWidth;
            // get scaleFactor from the smallest height
            if (defaultHeight < (imgHeight * scaleFactor)) {
                scaleFactor = defaultHeight / imgHeight;
            }
        }
        
        int bufImgType;
        if (BufferedImage.TYPE_CUSTOM == bufImg.getType()) {
            // apply a type for image majority
            bufImgType = BufferedImage.TYPE_INT_ARGB_PRE;
        } else {
            bufImgType = bufImg.getType();
        }
        
        // scale original image with new size
        Image newImg = bufImg.getScaledInstance((int) (imgWidth * scaleFactor), (int) (imgHeight * scaleFactor), Image.SCALE_SMOOTH);
        
        bufNewImg = ImageTransform.toBufferedImage(newImg, bufImgType);
        
        result.put("bufferedImage", bufNewImg);
        result.put("scaleFactor", scaleFactor);
        return result;
    }
    
    public static Map<String, Object> createNewImageThumbnail(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
        String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.url"), context);
        String productId = (String) context.get("productId");
        String contentId = (String) context.get("contentId");
        String dataResourceName = (String) context.get("dataResourceName");
        String width = (String) context.get("sizeWidth");
        String imageType = ".jpg";
        int resizeWidth = Integer.parseInt(width);
        int resizeHeight = resizeWidth;
        
        try {
            BufferedImage bufImg = ImageIO.read(new File(imageServerPath + "/" + productId + "/" + dataResourceName));
            double imgHeight = bufImg.getHeight();
            double imgWidth = bufImg.getWidth();
            if (dataResourceName.lastIndexOf(".") > 0 && dataResourceName.lastIndexOf(".") < dataResourceName.length()) {
                imageType = dataResourceName.substring(dataResourceName.lastIndexOf("."));
            }
            
            String filenameToUse = dataResourceName.substring(0, dataResourceName.length() - 4) + "-" + resizeWidth + imageType;
            
            if (dataResourceName.length() > 3) {
                String mimeType = dataResourceName.substring(dataResourceName.length() - 3, dataResourceName.length());
                Map<String, Object> resultResize = ImageManagementServices.resizeImage(bufImg, imgHeight, imgWidth, resizeHeight, resizeWidth);
                ImageIO.write((RenderedImage) resultResize.get("bufferedImage"), mimeType, new File(imageServerPath + "/" + productId + "/" + filenameToUse));
                
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
                String imageUrlThumb = imageServerUrl + "/" + productId + "/" + filenameToUse;
                ImageManagementServices.createContentAndDataResource(dctx, userLogin, filenameToUse, imageUrlThumb, contentIdThumb, "image/jpeg");
                
                Map<String, Object> createContentAssocMap = FastMap.newInstance();
                createContentAssocMap.put("contentAssocTypeId", "IMAGE_THUMBNAIL");
                createContentAssocMap.put("contentId", contentId);
                createContentAssocMap.put("contentIdTo", contentIdThumb);
                createContentAssocMap.put("userLogin", userLogin);
                createContentAssocMap.put("mapKey", width);
                try {
                    dispatcher.runSync("createContentAssoc", createContentAssocMap);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        String successMsg = "Create new thumbnail size successful";
        return ServiceUtil.returnSuccess(successMsg);
    }
    
    public static Map<String, Object> resizeImageOfProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
        String productId = (String) context.get("productId");
        String dataResourceName = (String) context.get("dataResourceName");
        String width = (String) context.get("resizeWidth");
        int resizeWidth = Integer.parseInt(width);
        int resizeHeight = resizeWidth;
        
        try {
            BufferedImage bufImg = ImageIO.read(new File(imageServerPath + "/" + productId + "/" + dataResourceName));
            double imgHeight = bufImg.getHeight();
            double imgWidth = bufImg.getWidth();
            String filenameToUse = dataResourceName;
            String mimeType = dataResourceName.substring(dataResourceName.length() - 3, dataResourceName.length());
            Map<String, Object> resultResize = ImageManagementServices.resizeImage(bufImg, imgHeight, imgWidth, resizeHeight, resizeWidth);
            ImageIO.write((RenderedImage) resultResize.get("bufferedImage"), mimeType, new File(imageServerPath + "/" + productId + "/" + filenameToUse));
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        String successMsg = "Resize images successful";
        return ServiceUtil.returnSuccess(successMsg);
    }
    
    public static Map<String, Object> renameImage(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.path"), context);
        String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.management.url"), context);
        String productId = (String) context.get("productId");
        String contentId = (String) context.get("contentId");
        String filenameToUse = (String) context.get("drDataResourceName");
        String imageType = filenameToUse.substring(filenameToUse.lastIndexOf("."));
        String imgExtension = filenameToUse.substring(filenameToUse.length() - 3, filenameToUse.length());
        String imageUrl = imageServerUrl + "/" + productId + "/" + filenameToUse;
        
        try {
            List<GenericValue> productContentList = delegator.findByAnd("ProductContentAndInfo", UtilMisc.toMap("productId", productId, "contentId", contentId, "productContentTypeId", "IMAGE"));
            GenericValue productContent = EntityUtil.getFirst(productContentList);
            String dataResourceName = (String) productContent.get("drDataResourceName");
            String mimeType = filenameToUse.substring(filenameToUse.lastIndexOf("."));
            
            if (imageType.equals(mimeType)) {
                BufferedImage bufImg = ImageIO.read(new File(imageServerPath + "/" + productId + "/" + dataResourceName));
                ImageIO.write((RenderedImage) bufImg, imgExtension, new File(imageServerPath + "/" + productId + "/" + filenameToUse));
                
                File file = new File(imageServerPath + "/" + productId + "/" + dataResourceName);
                file.delete();
                
                Map<String, Object> contentUp = FastMap.newInstance();
                contentUp.put("contentId", contentId);
                contentUp.put("contentName", filenameToUse);
                contentUp.put("userLogin", userLogin);
                try {
                    dispatcher.runSync("updateContent", contentUp);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                GenericValue content = null;
                try {
                    content = delegator.findOne("Content", UtilMisc.toMap("contentId", contentId), false);
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (content != null) {
                    GenericValue dataResource = null;
                    try {
                        dataResource = content.getRelatedOne("DataResource");
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                    
                    if (dataResource != null) {
                        Map<String, Object> dataResourceCtx = FastMap.newInstance();
                        dataResourceCtx.put("dataResourceId", dataResource.getString("dataResourceId"));
                        dataResourceCtx.put("objectInfo", imageUrl);
                        dataResourceCtx.put("dataResourceName", filenameToUse);
                        dataResourceCtx.put("userLogin", userLogin);
                        try {
                            dispatcher.runSync("updateDataResource", dataResourceCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                    }
                }
                
                List<GenericValue> contentAssocList = delegator.findByAnd("ContentAssoc", UtilMisc.toMap("contentId", contentId, "contentAssocTypeId", "IMAGE_THUMBNAIL"));
                if (contentAssocList.size() > 0) {
                    for (int i = 0; i < contentAssocList.size(); i++) {
                        GenericValue contentAssoc = contentAssocList.get(i);
                        
                        List<GenericValue> dataResourceAssocList = delegator.findByAnd("ContentDataResourceView", UtilMisc.toMap("contentId", contentAssoc.get("contentIdTo")));
                        GenericValue dataResourceAssoc = EntityUtil.getFirst(dataResourceAssocList);
                        
                        String drDataResourceNameAssoc = (String) dataResourceAssoc.get("drDataResourceName");
                        String filenameToUseAssoc = filenameToUse.substring(0, filenameToUse.length() - 4) + "-" + contentAssoc.get("mapKey") + imageType;
                        String imageUrlAssoc = imageServerUrl + "/" + productId + "/" + filenameToUseAssoc;
                        
                        BufferedImage bufImgAssoc = ImageIO.read(new File(imageServerPath + "/" + productId + "/" + drDataResourceNameAssoc));
                        ImageIO.write((RenderedImage) bufImgAssoc, imgExtension, new File(imageServerPath + "/" + productId + "/" + filenameToUseAssoc));
                        
                        File fileAssoc = new File(imageServerPath + "/" + productId + "/" + drDataResourceNameAssoc);
                        fileAssoc.delete();
                        
                        Map<String, Object> contentAssocMap = FastMap.newInstance();
                        contentAssocMap.put("contentId", contentAssoc.get("contentIdTo"));
                        contentAssocMap.put("contentName", filenameToUseAssoc);
                        contentAssocMap.put("userLogin", userLogin);
                        try {
                            dispatcher.runSync("updateContent", contentAssocMap);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                        GenericValue contentAssocUp = null;
                        try {
                            contentAssocUp = delegator.findOne("Content", UtilMisc.toMap("contentId", contentAssoc.get("contentIdTo")), false);
                        } catch (GenericEntityException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                        if (contentAssocUp != null) {
                            GenericValue dataResourceAssocUp = null;
                            try {
                                dataResourceAssocUp = contentAssocUp.getRelatedOne("DataResource");
                            } catch (GenericEntityException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }
                            
                            if (dataResourceAssocUp != null) {
                                Map<String, Object> dataResourceAssocMap = FastMap.newInstance();
                                dataResourceAssocMap.put("dataResourceId", dataResourceAssocUp.getString("dataResourceId"));
                                dataResourceAssocMap.put("objectInfo", imageUrlAssoc);
                                dataResourceAssocMap.put("dataResourceName", filenameToUseAssoc);
                                dataResourceAssocMap.put("userLogin", userLogin);
                                try {
                                    dispatcher.runSync("updateDataResource", dataResourceAssocMap);
                                } catch (GenericServiceException e) {
                                    Debug.logError(e, module);
                                    return ServiceUtil.returnError(e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        String successMsg = "Rename image successfully.";
        return ServiceUtil.returnSuccess(successMsg);
    }
}