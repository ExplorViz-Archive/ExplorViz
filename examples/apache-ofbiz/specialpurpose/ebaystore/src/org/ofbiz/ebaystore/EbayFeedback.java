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
package org.ofbiz.ebaystore;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;

import java.util.*;

import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.call.GetFeedbackCall;
import com.ebay.sdk.call.GetItemsAwaitingFeedbackCall;
import com.ebay.sdk.call.GetUserCall;
import com.ebay.sdk.call.LeaveFeedbackCall;
import com.ebay.soap.eBLBaseComponents.CommentTypeCodeType;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.FeedbackDetailType;
import com.ebay.soap.eBLBaseComponents.FeedbackRatingDetailCodeType;
import com.ebay.soap.eBLBaseComponents.ItemRatingDetailArrayType;
import com.ebay.soap.eBLBaseComponents.ItemRatingDetailsType;
import com.ebay.soap.eBLBaseComponents.PaginatedTransactionArrayType;
import com.ebay.soap.eBLBaseComponents.SiteCodeType;
import com.ebay.soap.eBLBaseComponents.TransactionArrayType;
import com.ebay.soap.eBLBaseComponents.TransactionType;

import javolution.util.FastList;
import javolution.util.FastMap;

public class EbayFeedback {

    public static final String resource = "EbayUiLabels";

    public static Map<String, Object> loadFeedback(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        String productStoreId = (String) context.get("productStoreId");

        ApiContext apiContext = EbayStoreHelper.getApiContext(productStoreId, locale, delegator);
        try {
            Map<String, Object> inMap = FastMap.newInstance();
            inMap.put("productStoreId", productStoreId);
            inMap.put("userLogin", userLogin);
            Map<String, Object> resultUser = dispatcher.runSync("getEbayStoreUser", inMap);
            String userID = (String)resultUser.get("userLoginId");
            GetFeedbackCall feedbackCall = new GetFeedbackCall();
            feedbackCall.setApiContext(apiContext);
            SiteCodeType siteCodeType = EbayStoreHelper.getSiteCodeType(productStoreId,locale, delegator);
            feedbackCall.setSite(siteCodeType);
            feedbackCall.setUserID(userID);
            DetailLevelCodeType[] detailLevelCodeType = {DetailLevelCodeType.RETURN_ALL};
            feedbackCall.setDetailLevel(detailLevelCodeType);
            FeedbackDetailType[] feedback = feedbackCall.getFeedback();
            if (feedback != null) {
                String partyId = null;
                GenericValue userLoginEx = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userID));
                if (userLoginEx == null) {
                    //Party
                    GenericValue party =  delegator.makeValue("Party");
                    partyId = delegator.getNextSeqId("Party");
                    party.put("partyId", partyId);
                    party.put("partyTypeId", "PERSON");
                    party.create();
                    //UserLogin
                    userLoginEx =  delegator.makeValue("UserLogin");
                    userLoginEx.put("userLoginId", userID);
                    userLoginEx.put("partyId", partyId);
                    userLoginEx.create();
                } else {
                    partyId = userLoginEx.getString("partyId");
                }
                //PartyRole For eBay User
                List<GenericValue> partyRoles = delegator.findByAnd("PartyRole", UtilMisc.toMap("partyId", partyId, "roleTypeId", "OWNER"));
                if (partyRoles.size() == 0) {
                    GenericValue partyRole =  delegator.makeValue("PartyRole");
                    partyRole.put("partyId", partyId);
                    partyRole.put("roleTypeId", "OWNER");
                    partyRole.create();
                }
                int feedbackLength = feedback.length;
                for (int i = 0; i < feedbackLength; i++) {
                    //convert to ofbiz
                    String contentId = feedback[i].getFeedbackID();
                    Date eBayDateTime = feedback[i].getCommentTime().getTime();
                    GenericValue contentCheck = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
                    if (contentCheck != null) {
                        continue;
                    }
                    String textData = feedback[i].getCommentText();
                    String commentingUserId= feedback[i].getCommentingUser();
                    String commentingPartyId = null;
                    List<GenericValue> CommentingUserLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", commentingUserId));
                    if (CommentingUserLogins.size() == 0) {
                        //Party
                        GenericValue party =  delegator.makeValue("Party");
                        commentingPartyId = delegator.getNextSeqId("Party");
                        party.put("partyId", commentingPartyId);
                        party.put("partyTypeId", "PERSON");
                        party.create();
                        //UserLogin
                        userLoginEx =  delegator.makeValue("UserLogin");
                        userLoginEx.put("userLoginId", commentingUserId);
                        userLoginEx.put("partyId", commentingPartyId);
                        userLoginEx.create();
                    } else {
                        userLoginEx = CommentingUserLogins.get(0);
                        commentingPartyId = userLoginEx.getString("partyId");
                    }
                    //DataResource
                    GenericValue dataResource =  delegator.makeValue("DataResource");
                    String dataResourceId = delegator.getNextSeqId("DataResource");
                    dataResource.put("dataResourceId", dataResourceId);
                    dataResource.put("dataResourceTypeId", "ELECTRONIC_TEXT");
                    dataResource.put("mimeTypeId", "text/html");
                    dataResource.create();
                    //ElectronicText
                    GenericValue electronicText =  delegator.makeValue("ElectronicText");
                    electronicText.put("dataResourceId", dataResourceId);
                    electronicText.put("textData", textData);
                    electronicText.create();
                    //Content
                    GenericValue content =  delegator.makeValue("Content");
                    content.put("contentId", contentId);
                    content.put("contentTypeId", "DOCUMENT");
                    content.put("dataResourceId", dataResourceId);
                    content.put("createdDate", UtilDateTime.toTimestamp(eBayDateTime));
                    content.create();
                    //ContentPurpose
                    GenericValue contentPurpose =  delegator.makeValue("ContentPurpose");
                    contentPurpose.put("contentId", contentId);
                    contentPurpose.put("contentPurposeTypeId", "FEEDBACK");
                    contentPurpose.create();
                    //PartyRole For eBay Commentator
                    List<GenericValue> commentingPartyRoles = delegator.findByAnd("PartyRole", UtilMisc.toMap("partyId", commentingPartyId, "roleTypeId", "COMMENTATOR"));
                    if (commentingPartyRoles.size() == 0) {
                        GenericValue partyRole =  delegator.makeValue("PartyRole");
                        partyRole.put("partyId", commentingPartyId);
                        partyRole.put("roleTypeId", "COMMENTATOR");
                        partyRole.create();
                    }
                    //ContentRole for eBay User
                    List<GenericValue> contentRoles = delegator.findByAnd("ContentRole", UtilMisc.toMap("partyId", partyId, "roleTypeId", "OWNER", "contentId", contentId));
                    if (contentRoles.size() == 0) {
                        GenericValue contentRole =  delegator.makeValue("ContentRole");
                        contentRole.put("contentId", contentId);
                        contentRole.put("partyId", partyId);
                        contentRole.put("roleTypeId", "OWNER");
                        contentRole.put("fromDate", UtilDateTime.nowTimestamp());
                        contentRole.create();
                    }
                    //ContentRole for Commentator
                    List<GenericValue> commentingContentRoles = delegator.findByAnd("ContentRole", UtilMisc.toMap("partyId", commentingPartyId, "roleTypeId", "COMMENTATOR", "contentId", contentId));
                    if (commentingContentRoles.size() == 0) {
                        GenericValue contentRole =  delegator.makeValue("ContentRole");
                        contentRole.put("contentId", contentId);
                        contentRole.put("partyId", commentingPartyId);
                        contentRole.put("roleTypeId", "COMMENTATOR");
                        contentRole.put("fromDate", UtilDateTime.nowTimestamp());
                        contentRole.create();
                    }
                }
            }
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SdkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String successMsg = "Load eBay Feedback Successfull.";
        result = ServiceUtil.returnSuccess(successMsg);
        return result;
    }

    public static Map<String, Object> getItemsAwaitingFeedback(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        String productStoreId = (String) context.get("productStoreId");
        ApiContext apiContext = EbayStoreHelper.getApiContext(productStoreId, locale, delegator);
        List<Map<String, Object>> itemsResult = FastList.newInstance();
        try {
            GetItemsAwaitingFeedbackCall awaitingFeedbackCall = new GetItemsAwaitingFeedbackCall();
            awaitingFeedbackCall.setApiContext(apiContext);
            awaitingFeedbackCall.getItemsAwaitingFeedback();
            PaginatedTransactionArrayType itemsAwaitingFeedback = awaitingFeedbackCall.getReturnedItemsAwaitingFeedback();
            TransactionArrayType items = itemsAwaitingFeedback.getTransactionArray();
            GetUserCall getUserCall = new GetUserCall(apiContext);
            String commentingUser = getUserCall.getUser().getUserID();
            for (int i = 0;i < items.getTransactionLength(); i++) {
                Map<String, Object> entry = FastMap.newInstance();
                TransactionType transection = items.getTransaction(i);
                entry.put("itemID", transection.getItem().getItemID());
                entry.put("commentingUser", commentingUser);
                entry.put("title", transection.getItem().getTitle());
                entry.put("transactionID", transection.getTransactionID());
                if (transection.getBuyer() != null) {
                    entry.put("userID", transection.getBuyer().getUserID());
                    entry.put("role", "buyer");
                }

                if (transection.getItem().getSeller() != null) {
                    entry.put("userID", transection.getItem().getSeller().getUserID());
                    entry.put("role", "seller");
                }
                if (transection.getShippingDetails()!=null) {
                    entry.put("shippingCost", transection.getShippingDetails().getDefaultShippingCost().getValue());
                    entry.put("shippingCurrency", transection.getShippingDetails().getDefaultShippingCost().getCurrencyID().name());
                }

                if (transection.getFeedbackLeft() != null) {
                    entry.put("commentType", transection.getFeedbackLeft().getCommentType().name());
                }
                itemsResult.add(entry);
            }
            result.put("itemsAwaitingFeedback", itemsResult);
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SdkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String, Object> leaveFeedback(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object>result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        String productStoreId = (String) context.get("productStoreId");
        ApiContext apiContext = EbayStoreHelper.getApiContext(productStoreId, locale, delegator);

        String itemId = (String) context.get("itemId");
        String targetUser = (String) context.get("targetUser");
        String transactionId = (String) context.get("transactionId");
        String commentingUser = (String) context.get("commentingUser");
        String role = (String) context.get("role");
        String commentType = (String) context.get("commentType");
        String commentText = (String) context.get("commentText");
        String ratingItem = (String) context.get("ratingItem");
        String ratingComm = (String) context.get("ratingComm");
        String ratingShip = (String) context.get("ratingShip");
        String ratingShipHand = (String) context.get("ratingShipHand");
        try {
            if (commentType != null) {
                LeaveFeedbackCall leaveFeedbackCall = new LeaveFeedbackCall();
                leaveFeedbackCall.setApiContext(apiContext);
                leaveFeedbackCall.setTargetUser(targetUser);
                leaveFeedbackCall.setTransactionID(transactionId);

                if (role.equals("seller")) {
                    ItemRatingDetailArrayType sellerItemRatingDetailArray = new ItemRatingDetailArrayType();

                    //The item description
                    ItemRatingDetailsType itemRatingDetailsType1 = new ItemRatingDetailsType();
                    int ratingItemValue = 0;
                    if (UtilValidate.isInteger(ratingItem)) {
                        ratingItemValue = Integer.parseInt(ratingItem);
                    }
                    if (ratingItemValue < 3) {
                        /*
                        String AqItemAsDescribed = null;
                        int AqItemAsDescribedId = Integer.parseInt((String) context.get("AqItemAsDescribedId"));
                        switch (AqItemAsDescribedId) {
                        case 5:
                            AqItemAsDescribed = "ItemNotReceived";
                            break;
                        case 6:
                            AqItemAsDescribed = "ItemBadQuality";
                            break;
                        case 2:
                            AqItemAsDescribed = "ItemDamaged";
                            break;
                        case 1:
                            AqItemAsDescribed = "ItemIneligible";
                            break;
                        case 3:
                            AqItemAsDescribed = "ItemLost";
                            break;
                        default:
                            AqItemAsDescribed = "Other";
                            break;
                        }
                        */
                    }
                    itemRatingDetailsType1.setRating(ratingItemValue);
                    itemRatingDetailsType1.setRatingDetail(FeedbackRatingDetailCodeType.ITEM_AS_DESCRIBED);

                    //The seller's communication
                    ItemRatingDetailsType itemRatingDetailsType2 = new ItemRatingDetailsType();
                    int ratingCommValue = 0;
                    if (UtilValidate.isInteger(ratingComm)) {
                        ratingCommValue = Integer.parseInt(ratingComm);
                    }
                    itemRatingDetailsType2.setRating(ratingCommValue);
                    itemRatingDetailsType2.setRatingDetail(FeedbackRatingDetailCodeType.COMMUNICATION);

                    //the seller ship the item
                    ItemRatingDetailsType itemRatingDetailsType3 = new ItemRatingDetailsType();
                    int ratingShipValue = 0;
                    if (UtilValidate.isInteger(ratingShip)) {
                        ratingShipValue = Integer.parseInt(ratingShip);
                    }
                    itemRatingDetailsType3.setRating(ratingShipValue);
                    itemRatingDetailsType3.setRatingDetail(FeedbackRatingDetailCodeType.SHIPPING_TIME);

                    //the shipping and handling charges
                    ItemRatingDetailsType itemRatingDetailsType4 = new ItemRatingDetailsType();
                    int ratingShipHandValue = 0;
                    if (UtilValidate.isInteger(ratingShipHand)) {
                        ratingShipHandValue = Integer.parseInt(ratingShipHand);
                    }
                    itemRatingDetailsType4.setRating(ratingShipHandValue);
                    itemRatingDetailsType4.setRatingDetail(FeedbackRatingDetailCodeType.SHIPPING_AND_HANDLING_CHARGES);

                    //Rating Summary
                    ItemRatingDetailsType[] itemRatingDetailsType = {itemRatingDetailsType1, itemRatingDetailsType2, itemRatingDetailsType3, itemRatingDetailsType4};
                    sellerItemRatingDetailArray.setItemRatingDetails(itemRatingDetailsType);

                    leaveFeedbackCall.setSellerItemRatingDetailArray(sellerItemRatingDetailArray);
                }
                FeedbackDetailType feedbackDetail = new FeedbackDetailType();
                feedbackDetail.setItemID(itemId);
                feedbackDetail.setCommentingUser(commentingUser);
                feedbackDetail.setCommentText(commentText);
                feedbackDetail.setCommentTime(Calendar.getInstance());
                if (commentType.equals("positive")) {
                    feedbackDetail.setCommentType(CommentTypeCodeType.POSITIVE);
                } else if (commentType.equals("neutral")) {
                    feedbackDetail.setCommentType(CommentTypeCodeType.NEUTRAL);
                } else if (commentType.equals("negative")) {
                    feedbackDetail.setCommentType(CommentTypeCodeType.NEGATIVE);
                }
                leaveFeedbackCall.setFeedbackDetail(feedbackDetail);
                leaveFeedbackCall.leaveFeedback();
            }
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SdkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = ServiceUtil.returnSuccess();
        return result;
    }
}
