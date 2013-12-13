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

package org.ofbiz.workeffort.workeffort;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.common.KeywordSearchUtil;
import org.ofbiz.content.data.DataResourceWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;

public class WorkEffortKeywordIndex {
    public static final String module = WorkEffortKeywordIndex.class.getName();
    public static void indexKeywords(GenericValue workEffort) throws GenericEntityException {
        if (workEffort == null) return;

        Delegator delegator = workEffort.getDelegator();
        if (delegator == null) return;
        String workEffortId = workEffort.getString("workEffortId");
        String separators = KeywordSearchUtil.getSeparators();
        String stopWordBagOr = KeywordSearchUtil.getStopWordBagOr();
        String stopWordBagAnd = KeywordSearchUtil.getStopWordBagAnd();
        boolean removeStems = KeywordSearchUtil.getRemoveStems();
        Set<String> stemSet = KeywordSearchUtil.getStemSet();

        Map<String, Long> keywords = new TreeMap<String, Long>();
        List<String> strings = FastList.newInstance();
        int widWeight = 1;
        try {
            widWeight = Integer.parseInt(UtilProperties.getPropertyValue("workeffortsearch", "index.weight.WorkEffort.workEffortId", "1"));
        } catch (Exception e) {
            Debug.logWarning("Could not parse weight number: " + e.toString(), module);
        }
        keywords.put(workEffort.getString("workEffortId").toLowerCase(), Long.valueOf(widWeight));

        addWeightedKeywordSourceString(workEffort, "workEffortName", strings);
        addWeightedKeywordSourceString(workEffort, "workEffortTypeId", strings);
        addWeightedKeywordSourceString(workEffort, "currentStatusId", strings);

        if (!"0".equals(UtilProperties.getPropertyValue("workeffortsearch", "index.weight.WorkEffortNoteAndData.noteInfo", "1"))) {
            List<GenericValue> workEffortNotes = delegator.findByAnd("WorkEffortNoteAndData", UtilMisc.toMap("workEffortId", workEffortId));
            for (GenericValue workEffortNote : workEffortNotes) {
                addWeightedKeywordSourceString(workEffortNote, "noteInfo", strings);
                }
        }
        //WorkEffortAttribute
        if (!"0".equals(UtilProperties.getPropertyValue("workeffortsearch", "index.weight.WorkEffortAttribute.attrName", "1")) ||
                !"0".equals(UtilProperties.getPropertyValue("workeffortsearch", "index.weight.WorkEffortAttribute.attrValue", "1"))) {
            List<GenericValue> workEffortAttributes = delegator.findByAnd("WorkEffortAttribute", UtilMisc.toMap("workEffortId", workEffortId));
            for (GenericValue workEffortAttribute : workEffortAttributes) {
                addWeightedKeywordSourceString(workEffortAttribute, "attrName", strings);
                addWeightedKeywordSourceString(workEffortAttribute, "attrValue", strings);
            }
        }

        String workEffortContentTypes = UtilProperties.getPropertyValue("workeffortsearch", "index.include.WorkEffortContentTypes");
        for (String workEffortContentTypeId: workEffortContentTypes.split(",")) {
            int weight = 1;
            try {
                weight = Integer.parseInt(UtilProperties.getPropertyValue("workeffortsearch", "index.weight.WorkEffortContent." + workEffortContentTypeId, "1"));
            } catch (Exception e) {
                Debug.logWarning("Could not parse weight number: " + e.toString(), module);
            }

            List<GenericValue> workEffortContentAndInfos = delegator.findByAnd("WorkEffortContentAndInfo", UtilMisc.toMap("workEffortId", workEffortId, "workEffortContentTypeId", workEffortContentTypeId), null);
            for (GenericValue workEffortContentAndInfo: workEffortContentAndInfos) {
                addWeightedDataResourceString(workEffortContentAndInfo, weight, strings, delegator, workEffort);
                List<GenericValue> alternateViews = workEffortContentAndInfo.getRelated("ContentAssocDataResourceViewTo", UtilMisc.toMap("caContentAssocTypeId", "ALTERNATE_LOCALE"), UtilMisc.toList("-caFromDate"));
                alternateViews = EntityUtil.filterByDate(alternateViews, UtilDateTime.nowTimestamp(), "caFromDate", "caThruDate", true);
                for (GenericValue thisView: alternateViews) {
                    addWeightedDataResourceString(thisView, weight, strings, delegator, workEffort);
                }
            }
        }
        for (String str: strings) {
            // call process keywords method here
            KeywordSearchUtil.processKeywordsForIndex(str, keywords, separators, stopWordBagAnd, stopWordBagOr, removeStems, stemSet);
        }

        List<GenericValue> toBeStored = FastList.newInstance();
        for (Map.Entry<String, Long> entry: keywords.entrySet()) {
            if (entry.getKey().length() < 60) { // ignore very long strings, cannot be stored anyway
                GenericValue workEffortKeyword = delegator.makeValue("WorkEffortKeyword", UtilMisc.toMap("workEffortId", workEffort.getString("workEffortId"), "keyword", entry.getKey(), "relevancyWeight", entry.getValue()));
                toBeStored.add(workEffortKeyword);
            }
        }
        if (toBeStored.size() > 0) {
            if (Debug.verboseOn()) Debug.logVerbose("WorkEffortKeywordIndex indexKeywords Storing " + toBeStored.size() + " keywords for workEffortId " + workEffort.getString("workEffortId"), module);
            delegator.storeAll(toBeStored);
        }

    }

    public static void addWeightedDataResourceString(GenericValue dataResource, int weight, List<String> strings, Delegator delegator, GenericValue workEffort) {
        Map<String, Object> workEffortCtx = UtilMisc.<String, Object>toMap("workEffort", workEffort);
        try {
            String contentText = DataResourceWorker.renderDataResourceAsText(delegator, dataResource.getString("dataResourceId"), workEffortCtx, null, null, false);
            for (int i = 0; i < weight; i++) {
                strings.add(contentText);
            }
        } catch (IOException e1) {
            Debug.logError(e1, "Error getting content text to index", module);
        } catch (GeneralException e1) {
            Debug.logError(e1, "Error getting content text to index", module);
        }
    }
    public static void addWeightedKeywordSourceString(GenericValue value, String fieldName, List<String> strings) {
        if (value.getString(fieldName) != null) {
            int weight = 1;

            try {
                weight = Integer.parseInt(UtilProperties.getPropertyValue("workeffortsearch", "index.weight." + value.getEntityName() + "." + fieldName, "1"));
            } catch (Exception e) {
                Debug.logWarning("Could not parse weight number: " + e.toString(), module);
            }

            for (int i = 0; i < weight; i++) {
                strings.add(value.getString(fieldName));
            }
        }
    }

}
