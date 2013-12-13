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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.model.ModelReader;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.model.ModelViewEntity;
import org.ofbiz.entity.model.ModelViewEntity.ModelAlias;

import javolution.util.FastList;
import javolution.util.FastMap;

starSchemaName = parameters.starSchemaName;

starSchemaFields = FastList.newInstance();
if (starSchemaName) {
    reader = delegator.getModelReader();
    starSchema = reader.getModelEntity(starSchemaName);
    if (starSchema) {
        fieldsIt = starSchema.getAliasesIterator();
        while (fieldsIt.hasNext()) {
            field = fieldsIt.next();
            fieldMap = FastMap.newInstance();
            fieldMap.name = field.getName();
            description = field.getDescription();
            if (!description) {
                aliasedEntity = starSchema.getAliasedEntity(field.getEntityAlias(), reader);
                if (aliasedEntity) {
                    aliasedField = starSchema.getAliasedField(aliasedEntity, field.getField(), reader);
                    description = aliasedField.getDescription();
                }
            }
            fieldMap.description = description;
            starSchemaFields.add(fieldMap);
        }
    }
}
context.starSchemaName = starSchemaName;
context.starSchemaFields = starSchemaFields;
