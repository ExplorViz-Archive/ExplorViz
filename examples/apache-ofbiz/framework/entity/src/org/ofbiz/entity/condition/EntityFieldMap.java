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
package org.ofbiz.entity.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.context.ObjectFactory;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.entity.util.EntityUtil;

/**
 * Encapsulates simple expressions used for specifying queries
 *
 */
@SuppressWarnings("serial")
public class EntityFieldMap extends EntityConditionListBase<EntityExpr> {

    protected static final ObjectFactory<EntityFieldMap> entityFieldMapFactory = new ObjectFactory<EntityFieldMap>() {
        @Override
        protected EntityFieldMap create() {
            return new EntityFieldMap();
        }
    };

    protected Map<String, ? extends Object> fieldMap = null;

    protected EntityFieldMap() {
        super();
    }

    public static <V> List<EntityExpr> makeConditionList(EntityComparisonOperator<?,V> op, V... keysValues) {
        return makeConditionList(EntityUtil.makeFields(keysValues), op);
    }

    public static <V> List<EntityExpr> makeConditionList(Map<String, V> fieldMap, EntityComparisonOperator<?,V> op) {
        if (fieldMap == null) return new ArrayList<EntityExpr>();
        List<EntityExpr> list = new ArrayList<EntityExpr>(fieldMap.size());
        for (Map.Entry<String, ? extends Object> entry: fieldMap.entrySet()) {
            list.add(EntityCondition.makeCondition(entry.getKey(), op, entry.getValue()));
        }
        return list;
    }

    public <V> void init(EntityComparisonOperator<?,?> compOp, EntityJoinOperator joinOp, V... keysValues) {
        super.init(makeConditionList(EntityUtil.makeFields(keysValues), UtilGenerics.<EntityComparisonOperator<String,V>>cast(compOp)), joinOp);
        this.fieldMap = EntityUtil.makeFields(keysValues);
        if (this.fieldMap == null) this.fieldMap = FastMap.newInstance();
        this.operator = joinOp;
    }

    public <V> void init(Map<String, V> fieldMap, EntityComparisonOperator<?,?> compOp, EntityJoinOperator joinOp) {
        super.init(makeConditionList(fieldMap, UtilGenerics.<EntityComparisonOperator<String,V>>cast(compOp)), joinOp);
        this.fieldMap = fieldMap;
        if (this.fieldMap == null) this.fieldMap = FastMap.newInstance();
        this.operator = joinOp;
    }

    @Override
    public void reset() {
        super.reset();
        this.fieldMap = null;
    }

    public Object getField(String name) {
        return this.fieldMap.get(name);
    }

    public boolean containsField(String name) {
        return this.fieldMap.containsKey(name);
    }

    public Iterator<String> getFieldKeyIterator() {
        return Collections.unmodifiableSet(this.fieldMap.keySet()).iterator();
    }

    public Iterator<Map.Entry<String, Object>> getFieldEntryIterator() {
        return Collections.unmodifiableMap(this.fieldMap).entrySet().iterator();
    }

    @Override
    public void accept(EntityConditionVisitor visitor) {
        visitor.acceptEntityFieldMap(this);
    }
}
