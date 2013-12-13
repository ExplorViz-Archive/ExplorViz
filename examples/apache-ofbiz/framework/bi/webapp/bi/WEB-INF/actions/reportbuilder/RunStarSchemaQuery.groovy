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

import java.util.*;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.model.ModelReader;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.model.ModelViewEntity;
import org.ofbiz.entity.model.ModelViewEntity.ModelAlias;

import javolution.util.FastSet;
import javolution.util.FastList;
import javolution.util.FastMap;

starSchemaName = parameters.starSchemaName;
selectedFieldList = UtilHttp.parseMultiFormData(parameters);

columnNames = FastSet.newInstance();
selectedFieldList.each { selectedField ->
  columnNames.add(selectedField.selectedFieldName);
}
context.columnNames = columnNames;
List conditionList = null;
EntityConditionList condition =  null;
List orderByFields = null;
EntityFindOptions findOptions = null;

List records = FastList.newInstance();

//conditionList.add(...);
//condition =  EntityCondition.makeCondition(conditionList, EntityOperator.AND);

orderByFields = null;

findOptions = new EntityFindOptions();
findOptions.setDistinct(false);

records = delegator.findList(starSchemaName, condition, context.columnNames, orderByFields, findOptions, false);

context.records = records;
