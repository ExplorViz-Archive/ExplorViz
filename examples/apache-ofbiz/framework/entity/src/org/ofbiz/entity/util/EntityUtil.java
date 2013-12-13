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

package org.ofbiz.entity.util;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityDateFilterCondition;
import org.ofbiz.entity.condition.OrderByList;
import org.ofbiz.entity.model.ModelField;

/**
 * Helper methods when dealing with Entities, especially ones that follow certain conventions
 */
public class EntityUtil {

    public static final String module = EntityUtil.class.getName();

    public static <V> Map<String, V> makeFields(V... args) {
        Map<String, V> fields = FastMap.newInstance();
        if (args != null) {
            for (int i = 0; i < args.length;) {
                if (!(args[i] instanceof String)) throw new IllegalArgumentException("Key(" + i + "), with value(" + args[i] + ") is not a String.");
                String key = (String) args[i];
                i++;
                if (!(args[i] instanceof Comparable<?>)) throw new IllegalArgumentException("Value(" + i + "), with value(" + args[i] + ") does not implement Comparable.");
                if (!(args[i] instanceof Serializable)) throw new IllegalArgumentException("Value(" + i + "), with value(" + args[i] + ") does not implement Serializable.");
                fields.put(key, args[i]);
                i++;
            }
        }
        return fields;
    }


    public static GenericValue getFirst(List<GenericValue> values) {
        if ((values != null) && (values.size() > 0)) {
            return values.get(0);
        } else {
            return null;
        }
    }

    public static GenericValue getOnly(List<GenericValue> values) {
        if (values != null) {
            if (values.size() <= 0) {
                return null;
            }
            if (values.size() == 1) {
                return values.get(0);
            } else {
                throw new IllegalArgumentException("Passed List had more than one value.");
            }
        } else {
            return null;
        }
    }

    public static EntityCondition getFilterByDateExpr() {
        return EntityCondition.makeConditionDate("fromDate", "thruDate");
    }

    public static EntityCondition getFilterByDateExpr(String fromDateName, String thruDateName) {
        return EntityCondition.makeConditionDate(fromDateName, thruDateName);
    }

    public static EntityCondition getFilterByDateExpr(java.util.Date moment) {
        return EntityDateFilterCondition.makeCondition(new java.sql.Timestamp(moment.getTime()), "fromDate", "thruDate");
    }

    public static EntityCondition getFilterByDateExpr(java.sql.Timestamp moment) {
        return EntityDateFilterCondition.makeCondition(moment, "fromDate", "thruDate");
    }

    public static EntityCondition getFilterByDateExpr(java.sql.Timestamp moment, String fromDateName, String thruDateName) {
        return EntityDateFilterCondition.makeCondition(moment, fromDateName, thruDateName);
    }

    /**
     *returns the values that are currently active.
     *
     *@param datedValues GenericValue's that have "fromDate" and "thruDate" fields
     *@return List of GenericValue's that are currently active
     */
    public static <T extends GenericEntity> List<T> filterByDate(List<T> datedValues) {
        return filterByDate(datedValues, UtilDateTime.nowTimestamp(), null, null, true);
    }

    /**
     *returns the values that are currently active.
     *
     *@param datedValues GenericValue's that have "fromDate" and "thruDate" fields
     *@param allAreSame Specifies whether all values in the List are of the same entity; this can help speed things up a fair amount since we only have to see if the from and thru date fields are valid once
     *@return List of GenericValue's that are currently active
     */
    public static <T extends GenericEntity> List<T> filterByDate(List<T> datedValues, boolean allAreSame) {
        return filterByDate(datedValues, UtilDateTime.nowTimestamp(), null, null, allAreSame);
    }

    /**
     *returns the values that are active at the moment.
     *
     *@param datedValues GenericValue's that have "fromDate" and "thruDate" fields
     *@param moment the moment in question
     *@return List of GenericValue's that are active at the moment
     */
    public static <T extends GenericEntity> List<T> filterByDate(List<T> datedValues, java.util.Date moment) {
        return filterByDate(datedValues, new java.sql.Timestamp(moment.getTime()), null, null, true);
    }

    /**
     *returns the values that are active at the moment.
     *
     *@param datedValues GenericValue's that have "fromDate" and "thruDate" fields
     *@param moment the moment in question
     *@return List of GenericValue's that are active at the moment
     */
    public static <T extends GenericEntity> List<T> filterByDate(List<T> datedValues, java.sql.Timestamp moment) {
        return filterByDate(datedValues, moment, null, null, true);
    }

    /**
     *returns the values that are active at the moment.
     *
     *@param datedValues GenericValue's that have "fromDate" and "thruDate" fields
     *@param moment the moment in question
     *@param allAreSame Specifies whether all values in the List are of the same entity; this can help speed things up a fair amount since we only have to see if the from and thru date fields are valid once
     *@return List of GenericValue's that are active at the moment
     */
    public static <T extends GenericEntity> List<T> filterByDate(List<T> datedValues, java.sql.Timestamp moment, String fromDateName, String thruDateName, boolean allAreSame) {
        if (datedValues == null) return null;
        if (moment == null) return datedValues;
        if (fromDateName == null) fromDateName = "fromDate";
        if (thruDateName == null) thruDateName = "thruDate";

        List<T> result = FastList.newInstance();
        Iterator<T> iter = datedValues.iterator();

        if (allAreSame) {
            ModelField fromDateField = null;
            ModelField thruDateField = null;

            if (iter.hasNext()) {
                T datedValue = iter.next();

                fromDateField = datedValue.getModelEntity().getField(fromDateName);
                if (fromDateField == null) throw new IllegalArgumentException("\"" + fromDateName + "\" is not a field of " + datedValue.getEntityName());
                thruDateField = datedValue.getModelEntity().getField(thruDateName);
                if (thruDateField == null) throw new IllegalArgumentException("\"" + thruDateName + "\" is not a field of " + datedValue.getEntityName());

                java.sql.Timestamp fromDate = (java.sql.Timestamp) datedValue.dangerousGetNoCheckButFast(fromDateField);
                java.sql.Timestamp thruDate = (java.sql.Timestamp) datedValue.dangerousGetNoCheckButFast(thruDateField);

                if ((thruDate == null || thruDate.after(moment)) && (fromDate == null || fromDate.before(moment) || fromDate.equals(moment))) {
                    result.add(datedValue);
                }// else not active at moment
            }
            while (iter.hasNext()) {
                T datedValue = iter.next();
                java.sql.Timestamp fromDate = (java.sql.Timestamp) datedValue.dangerousGetNoCheckButFast(fromDateField);
                java.sql.Timestamp thruDate = (java.sql.Timestamp) datedValue.dangerousGetNoCheckButFast(thruDateField);

                if ((thruDate == null || thruDate.after(moment)) && (fromDate == null || fromDate.before(moment) || fromDate.equals(moment))) {
                    result.add(datedValue);
                }// else not active at moment
            }
        } else {
            // if not all values are known to be of the same entity, must check each one...
            while (iter.hasNext()) {
                T datedValue = iter.next();
                java.sql.Timestamp fromDate = datedValue.getTimestamp(fromDateName);
                java.sql.Timestamp thruDate = datedValue.getTimestamp(thruDateName);

                if ((thruDate == null || thruDate.after(moment)) && (fromDate == null || fromDate.before(moment) || fromDate.equals(moment))) {
                    result.add(datedValue);
                }// else not active at moment
            }
        }

        return result;
    }

    public static boolean isValueActive(GenericValue datedValue, java.sql.Timestamp moment) {
        return isValueActive(datedValue, moment, "fromDate", "thruDate");
    }

    public static boolean isValueActive(GenericValue datedValue, java.sql.Timestamp moment, String fromDateName, String thruDateName) {
        java.sql.Timestamp fromDate = datedValue.getTimestamp(fromDateName);
        java.sql.Timestamp thruDate = datedValue.getTimestamp(thruDateName);

        if ((thruDate == null || thruDate.after(moment)) && (fromDate == null || fromDate.before(moment) || fromDate.equals(moment))) {
            return true;
        } else {
            // else not active at moment
            return false;
        }
    }

    /**
     *returns the values that match the values in fields
     *
     *@param values List of GenericValues
     *@param fields the field-name/value pairs that must match
     *@return List of GenericValue's that match the values in fields
     */
    public static <T extends GenericEntity> List<T> filterByAnd(List<T> values, Map<String, ? extends Object> fields) {
        if (values == null) return null;

        List<T> result = null;
        if (UtilValidate.isEmpty(fields)) {
            result = FastList.newInstance();
            result.addAll(values);
        } else {
            result = FastList.newInstance();
            for (T value: values) {
                if (value.matchesFields(fields)) {
                    result.add(value);
                }// else did not match
            }
        }
        return result;
    }

    /**
     *returns the values that match all of the exprs in list
     *
     *@param values List of GenericValues
     *@param exprs the expressions that must validate to true
     *@return List of GenericValue's that match the values in fields
     */
    public static <T extends GenericEntity> List<T> filterByAnd(List<T> values, List<? extends EntityCondition> exprs) {
        if (values == null) return null;
        if (UtilValidate.isEmpty(exprs)) {
            // no constraints... oh well
            return values;
        }

        List<T> result = FastList.newInstance();
        for (T value: values) {
            boolean include = true;

            for (EntityCondition condition: exprs) {
                include = condition.entityMatches(value);
                if (!include) break;
            }
            if (include) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     *returns the values that match any of the exprs in list
     *
     *@param values List of GenericValues
     *@param exprs the expressions that must validate to true
     *@return List of GenericValue's that match the values in fields
     */
    public static <T extends GenericEntity> List<T> filterByOr(List<T> values, List<? extends EntityCondition> exprs) {
        if (values == null) return null;
        if (UtilValidate.isEmpty(exprs)) {
            return values;
        }

        List<T> result = FastList.newInstance();
        for (T value: values) {
            boolean include = false;

            for (EntityCondition condition: exprs) {
                include = condition.entityMatches(value);
                if (include) break;
            }
            if (include) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     *returns the values in the order specified
     *
     *@param values List of GenericValues
     *@param orderBy The fields of the named entity to order the query by;
     *      optionally add a " ASC" for ascending or " DESC" for descending
     *@return List of GenericValue's in the proper order
     */
    public static <T extends GenericEntity> List<T> orderBy(Collection<T> values, List<String> orderBy) {
        if (values == null) return null;
        if (values.size() == 0) return FastList.newInstance();
        if (UtilValidate.isEmpty(orderBy)) {
            List<T> newList = FastList.newInstance();
            newList.addAll(values);
            return newList;
        }

        List<T> result = FastList.newInstance();
        result.addAll(values);
        if (Debug.verboseOn()) Debug.logVerbose("Sorting " + values.size() + " values, orderBy=" + orderBy.toString(), module);
        Collections.sort(result, new OrderByList(orderBy));
        return result;
    }

    public static List<GenericValue> getRelated(String relationName, List<GenericValue> values) throws GenericEntityException {
        if (values == null) return null;

        List<GenericValue> result = FastList.newInstance();
        for (GenericValue value: values) {
            result.addAll(value.getRelated(relationName));
        }
        return result;
    }

    public static List<GenericValue> getRelatedCache(String relationName, List<GenericValue> values) throws GenericEntityException {
        if (values == null) return null;

        List<GenericValue> result = FastList.newInstance();
        for (GenericValue value: values) {
            result.addAll(value.getRelatedCache(relationName));
        }
        return result;
    }

    public static List<GenericValue> getRelatedByAnd(String relationName, Map<String, ? extends Object> fields, List<GenericValue> values) throws GenericEntityException {
        if (values == null) return null;

        List<GenericValue> result = FastList.newInstance();
        for (GenericValue value: values) {
            result.addAll(value.getRelatedByAnd(relationName, fields));
        }
        return result;
    }

    public static List<GenericValue> getRelatedByAndCache(String relationName, Map<String, ? extends Object> fields, List<GenericValue> values) throws GenericEntityException {
        if (values == null) return null;

        List<GenericValue> result = FastList.newInstance();
        for (GenericValue value: values) {
            result.addAll(value.getRelatedByAndCache(relationName, fields));
        }
        return result;
    }

    public static <T extends GenericEntity> List<T> filterByCondition(List<T> values, EntityCondition condition) {
        if (values == null) return null;

        List<T> result = FastList.newInstance();
        for (T value: values) {
            if (condition.entityMatches(value)) {
                result.add(value);
            }
        }
        return result;
    }

    public static <T extends GenericEntity> List<T> filterOutByCondition(List<T> values, EntityCondition condition) {
        if (values == null) return null;

        List<T> result = FastList.newInstance();
        for (T value: values) {
            if (!condition.entityMatches(value)) {
                result.add(value);
            }
        }
        return result;
    }

    public static List<GenericValue> findDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> search) throws GenericEntityException {
        return findDatedInclusionEntity(delegator, entityName, search, UtilDateTime.nowTimestamp());
    }

    public static List<GenericValue> findDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> search, Timestamp now) throws GenericEntityException {
        EntityCondition searchCondition = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition(search), EntityUtil.getFilterByDateExpr(now)));
        return delegator.findList(entityName, searchCondition, null, UtilMisc.toList("-fromDate"), null, false);
    }

    public static GenericValue newDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> search) throws GenericEntityException {
        return newDatedInclusionEntity(delegator, entityName, search, UtilDateTime.nowTimestamp());
    }

    public static GenericValue newDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> find, Timestamp now) throws GenericEntityException {
        Map<String, Object> search;
        List<GenericValue> entities = findDatedInclusionEntity(delegator, entityName, find, now);
        if (UtilValidate.isNotEmpty(entities)) {
            search = null;
            for (GenericValue entity: entities) {
                if (now.equals(entity.get("fromDate"))) {
                    search = FastMap.newInstance();
                    for (Map.Entry<String, ? super Object> entry: entity.getPrimaryKey().entrySet()) {
                        search.put(entry.getKey(), entry.getValue());
                    }
                    entity.remove("thruDate");
                } else {
                    entity.set("thruDate",now);
                }
                entity.store();
            }
            if (search == null) {
                search = FastMap.newInstance();
                search.putAll(EntityUtil.getFirst(entities));
            }
        } else {
            /* why is this being done? leaving out for now...
            search = new HashMap(search);
            */
            search = FastMap.newInstance();
            search.putAll(find);
        }
        if (now.equals(search.get("fromDate"))) {
            return EntityUtil.getOnly(delegator.findByAnd(entityName, search));
        } else {
            search.put("fromDate",now);
            search.remove("thruDate");
            return delegator.makeValue(entityName, search);
        }
    }

    public static void delDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> search) throws GenericEntityException {
        delDatedInclusionEntity(delegator, entityName, search, UtilDateTime.nowTimestamp());
    }

    public static void delDatedInclusionEntity(Delegator delegator, String entityName, Map<String, ? extends Object> search, Timestamp now) throws GenericEntityException {
        List<GenericValue> entities = findDatedInclusionEntity(delegator, entityName, search, now);
        for (GenericValue entity: entities) {
            entity.set("thruDate",now);
            entity.store();
        }
    }

    public static <T> List<T> getFieldListFromEntityList(List<GenericValue> genericValueList, String fieldName, boolean distinct) {
        if (genericValueList == null || fieldName == null) {
            return null;
        }
        List<T> fieldList = FastList.newInstance();
        Set<T> distinctSet = null;
        if (distinct) {
            distinctSet = FastSet.newInstance();
        }

        for (GenericValue value: genericValueList) {
            T fieldValue = UtilGenerics.<T>cast(value.get(fieldName));
            if (fieldValue != null) {
                if (distinct) {
                    if (!distinctSet.contains(fieldValue)) {
                        fieldList.add(fieldValue);
                        distinctSet.add(fieldValue);
                    }
                } else {
                    fieldList.add(fieldValue);
                }
            }
        }

        return fieldList;
    }

    public static <T> List<T> getFieldListFromEntityListIterator(EntityListIterator genericValueEli, String fieldName, boolean distinct) {
        if (genericValueEli == null || fieldName == null) {
            return null;
        }
        List<T> fieldList = FastList.newInstance();
        Set<T> distinctSet = null;
        if (distinct) {
            distinctSet = FastSet.newInstance();
        }

        GenericValue value = null;
        while ((value = genericValueEli.next()) != null) {
            T fieldValue = UtilGenerics.<T>cast(value.get(fieldName));
            if (fieldValue != null) {
                if (distinct) {
                    if (!distinctSet.contains(fieldValue)) {
                        fieldList.add(fieldValue);
                        distinctSet.add(fieldValue);
                    }
                } else {
                    fieldList.add(fieldValue);
                }
            }
        }

        return fieldList;
    }
}
