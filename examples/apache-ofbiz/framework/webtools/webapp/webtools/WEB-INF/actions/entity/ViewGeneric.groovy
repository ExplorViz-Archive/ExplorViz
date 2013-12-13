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
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.security.Security;
import org.ofbiz.entity.model.ModelReader;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.model.ModelFieldType;
import org.ofbiz.entity.model.ModelRelation;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import javolution.util.FastList;
import javolution.util.FastMap;

String entityName = parameters.get("entityName");
context.put("entityName", entityName);

ModelReader reader = delegator.getModelReader();
ModelEntity entity = reader.getModelEntity(entityName);

context.put("entity", entity);
context.put("plainTableName", entity.getPlainTableName());

boolean hasAllView = security.hasEntityPermission("ENTITY_DATA", "_VIEW", session);
boolean hasAllCreate = security.hasEntityPermission("ENTITY_DATA", "_CREATE", session);
boolean hasAllUpdate = security.hasEntityPermission("ENTITY_DATA", "_UPDATE", session);
boolean hasAllDelete = security.hasEntityPermission("ENTITY_DATA", "_DELETE", session);
boolean hasViewPermission = hasAllView || security.hasEntityPermission(entity.getPlainTableName(), "_VIEW", session);
boolean hasCreatePermission = hasAllCreate || security.hasEntityPermission(entity.getPlainTableName(), "_CREATE", session);
boolean hasUpdatePermission = hasAllUpdate || security.hasEntityPermission(entity.getPlainTableName(), "_UPDATE", session);
boolean hasDeletePermission = hasAllDelete || security.hasEntityPermission(entity.getPlainTableName(), "_DELETE", session);

context.put("hasAllView", hasAllView);
context.put("hasAllCreate", hasAllCreate);
context.put("hasAllUpdate", hasAllUpdate);
context.put("hasAllDelete", hasAllDelete);
context.put("hasViewPermission", hasViewPermission);
context.put("hasCreatePermission", hasCreatePermission);
context.put("hasUpdatePermission", hasUpdatePermission);
context.put("hasDeletePermission" , hasDeletePermission);

boolean useValue = true;
String curFindString = "entityName=" + entityName;
GenericPK findByPK = delegator.makePK(entityName);
Iterator pkIterator = entity.getPksIterator();
while (pkIterator.hasNext()) {
    ModelField field = pkIterator.next();
    ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());
    String fval = parameters.get(field.getName());
    if (UtilValidate.isNotEmpty(fval)) {
        curFindString = curFindString + "&" + field.getName() + "=" + fval;
        findByPK.setString(field.getName(), fval);
    }
}
context.put("findByPk", findByPK.toString());

curFindString = UtilFormatOut.encodeQuery(curFindString);
context.put("curFindString", curFindString);

GenericValue value = null;
//only try to find it if this is a valid primary key...
if (findByPK.isPrimaryKey()) {
    value = delegator.findOne(findByPK.getEntityName(), findByPK, false);
}
context.put("value", value);

if (value == null) {
    useValue = false;
}

if (value != null) {
    List fieldList = FastList.newInstance();
    Iterator fieldIterator = entity.getFieldsIterator();
    while (fieldIterator.hasNext()) {
        Map mapField = FastMap.newInstance();

        ModelField field = fieldIterator.next();
        ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());

        String fieldValue = "";
        if (type.getJavaType().equals("Timestamp") || type.getJavaType().equals("java.sql.Timestamp")) {
            Timestamp dtVal = value.getTimestamp(field.getName());
            fieldValue = (dtVal == null) ? "" : dtVal.toString();
        } else if (type.getJavaType().equals("Date") || type.getJavaType().equals("java.sql.Date")) {
            Date dateVal = value.getDate(field.getName());
            fieldValue = (dateVal == null) ? "" : dateVal.toString();
        } else if (type.getJavaType().equals("Time") || type.getJavaType().equals("java.sql.Time")) {
            Time timeVal = value.getTime(field.getName());
            fieldValue = (timeVal == null) ? "" : timeVal.toString();
        } else if (type.getJavaType().indexOf("Integer") >= 0) {
            fieldValue = UtilFormatOut.safeToString((Integer)value.get(field.getName()));
        } else if (type.getJavaType().indexOf("Long") >= 0) {
            fieldValue = UtilFormatOut.safeToString((Long)value.get(field.getName()));
        } else if (type.getJavaType().indexOf("Double") >= 0) {
            fieldValue = UtilFormatOut.safeToString((Double)value.get(field.getName()));
        } else if (type.getJavaType().indexOf("Float") >= 0) {
            fieldValue = UtilFormatOut.safeToString((Float)value.get(field.getName()));
        } else if (type.getJavaType().indexOf("BigDecimal") >= 0) {
            fieldValue = UtilFormatOut.safeToString((BigDecimal)value.get(field.getName()));
        } else if (type.getJavaType().indexOf("String") >= 0) {
            fieldValue = UtilFormatOut.checkNull((String)value.get(field.getName()));
        }
        mapField.put("name", field.getName());
        mapField.put("value", fieldValue);

        fieldList.add(mapField);
    }
    context.put("fields", fieldList);
}

GenericValue valueSave = value;
boolean pkNotFound = false;
if (value == null && (findByPK.getAllFields().size() > 0)) {
    pkNotFound = true;
}
context.put("pkNotFound", pkNotFound);

String lastUpdateMode = parameters.get("UPDATE_MODE");
if ((session.getAttribute("_ERROR_MESSAGE_") != null || request.getAttribute("_ERROR_MESSAGE_") != null) &&
    lastUpdateMode != null && !lastUpdateMode.equals("DELETE")) {
    //if we are updating and there is an error, do not use the entity data for the fields, use parameters to get the old value
    useValue = false;
}
context.put("useValue", useValue);

List newFieldPkList = FastList.newInstance();
pkIterator = entity.getPksIterator();
while (pkIterator.hasNext()) {
    Map mapField = FastMap.newInstance();

    ModelField field = pkIterator.next();
    ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());

    String fieldValue = "";
    String fieldType = "";
    String stringLength = "";
    if (type.getJavaType().equals("Timestamp") || type.getJavaType().equals("java.sql.Timestamp")) {
        String dateTimeString = null;
        if (findByPK != null && useValue) {
            Timestamp dtVal = findByPK.getTimestamp(field.getName());
            if (dtVal != null) {
              dateTimeString = dtVal.toString();
            }
        } else if (!useValue) {
            dateTimeString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(dateTimeString);
        fieldType = "DateTime";
    } else if (type.getJavaType().equals("Date") || type.getJavaType().equals("java.sql.Date")) {
        String dateString = null;
        if (findByPK != null && useValue) {
            Date dateVal = findByPK.getDate(field.getName());
            dateString = (dateVal == null) ? "" : dateVal.toString();
        } else if (!useValue) {
            dateString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(dateString);
        fieldType = "Date";
    } else if (type.getJavaType().equals("Time") || type.getJavaType().equals("java.sql.Time")) {
        String timeString = null;
        if (findByPK != null && useValue) {
            Time timeVal = findByPK.getTime(field.getName());
            timeString = (timeVal == null) ? "" : timeVal.toString();
        } else if (!useValue) {
            timeString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(timeString);
        fieldType = "Time";
    } else if (type.getJavaType().indexOf("Integer") >= 0) {
        fieldValue = (findByPK != null && useValue) ? UtilFormatOut.safeToString((Integer)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
        fieldType = "Integer";
    } else if (type.getJavaType().indexOf("Long") >= 0) {
        fieldValue = (findByPK != null && useValue) ? UtilFormatOut.safeToString((Long)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
        fieldType = "Long";
    } else if (type.getJavaType().indexOf("Double") >= 0) {
        fieldValue = (findByPK != null && useValue) ? UtilFormatOut.safeToString((Double)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
        fieldType = "Double";
    } else if (type.getJavaType().indexOf("Float") >= 0) {
        fieldValue = (findByPK != null && useValue) ? UtilFormatOut.safeToString((Float)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
        fieldType = "Float";
    } else if (type.getJavaType().indexOf("String") >= 0) {
        if (type.stringLength() <= 80) {
            fieldValue = (findByPK != null && useValue) ? UtilFormatOut.checkNull((String)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
            fieldType = "StringOneRow";
        } else if (type.stringLength() <= 255) {
            fieldValue = (findByPK != null && useValue) ? UtilFormatOut.checkNull((String)findByPK.get(field.getName())) : (useValue ? "" : UtilFormatOut.checkNull(parameters.get(field.getName())));
            fieldType = "String";
        } else {
            fieldValue = (findByPK != null && useValue) ? UtilFormatOut.checkNull((String)findByPK.get(field.getName())):(useValue?"":UtilFormatOut.checkNull(parameters.get(field.getName())));
            fieldType = "Textarea";
        }
        stringLength = type.stringLength().toString();
    }
    mapField.put("name", field.getName());
    mapField.put("value", fieldValue);
    mapField.put("fieldType", fieldType);
    mapField.put("stringLength", stringLength);

    newFieldPkList.add(mapField);
}
context.put("newFieldPkList", newFieldPkList);

List newFieldNoPkList = FastList.newInstance();
Iterator noPkIterator = entity.getNopksIterator();
while (noPkIterator.hasNext()) {
    Map mapField = FastMap.newInstance();

    ModelField field = noPkIterator.next();
    ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());

    String fieldValue = "";
    String fieldType = "";
    String stringLength = "";
    if (type.getJavaType().equals("Timestamp") || type.getJavaType().equals("java.sql.Timestamp")) {
        String dateTimeString = null;
        if (value != null && useValue) {
            Timestamp dtVal = value.getTimestamp(field.getName());
            if (dtVal != null) {
              dateTimeString = dtVal.toString();
            }
        } else if (!useValue) {
            dateTimeString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(dateTimeString);
        fieldType = "DateTime";
    } else if (type.getJavaType().equals("Date") || type.getJavaType().equals("java.sql.Date")) {
        String dateString = null;
        if (value != null && useValue) {
            Date dateVal = value.getDate(field.getName());
            dateString = (dateVal == null) ? "" : dateVal.toString();
        } else if (!useValue) {
            dateString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(dateString);
        fieldType = "Date";
    } else if (type.getJavaType().equals("Time") || type.getJavaType().equals("java.sql.Time")) {
        String timeString = null;
        if (value != null && useValue) {
            Time timeVal = value.getTime(field.getName());
            timeString = (timeVal == null) ? "" : timeVal.toString();
        } else if (!useValue) {
            timeString = parameters.get(field.getName());
        }
        fieldValue = UtilFormatOut.checkNull(timeString);
        fieldType = "Time";
    } else if (type.getJavaType().indexOf("Integer") >= 0) {
        fieldValue = (value != null && useValue) ? UtilFormatOut.safeToString((Integer)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
        fieldType = "Integer";
    } else if (type.getJavaType().indexOf("Long") >= 0) {
        fieldValue = (value != null && useValue) ? UtilFormatOut.safeToString((Long)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
        fieldType = "Long";
    } else if (type.getJavaType().indexOf("Double") >= 0) {
        fieldValue = (value != null && useValue) ? UtilFormatOut.safeToString((Double)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
        fieldType = "Double";
    } else if (type.getJavaType().indexOf("Float") >= 0) {
        fieldValue = (value != null && useValue) ? UtilFormatOut.safeToString((Float)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
        fieldType = "Float";
    } else if (type.getJavaType().indexOf("BigDecimal") >= 0) {
        fieldValue = (value != null && useValue) ? UtilFormatOut.safeToString((BigDecimal)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
        fieldType = "BigDecimal";
    } else if (type.getJavaType().indexOf("String") >= 0) {
        if (type.stringLength() <= 80) {
            fieldValue = (value != null && useValue) ? UtilFormatOut.checkNull((String)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
            fieldType = "StringOneRow";
        } else if (type.stringLength() <= 255) {
            fieldValue = (value != null && useValue) ? UtilFormatOut.checkNull((String)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
            fieldType = "String";
        } else {
            fieldValue = (value != null && useValue) ? UtilFormatOut.checkNull((String)value.get(field.getName())):UtilFormatOut.checkNull(parameters.get(field.getName()));
            fieldType = "Textarea";
        }
        stringLength = type.stringLength().toString();
    }
    mapField.put("name", field.getName());
    mapField.put("value", fieldValue);
    mapField.put("fieldType", fieldType);
    mapField.put("stringLength", stringLength);

    newFieldNoPkList.add(mapField);
}
context.put("newFieldNoPkList", newFieldNoPkList);

List relationFieldList = FastList.newInstance();
for (int relIndex = 0; relIndex < entity.getRelationsSize(); relIndex++) {
    Map mapRelation = FastMap.newInstance();

    ModelRelation relation = entity.getRelation(relIndex);
    ModelEntity relatedEntity = reader.getModelEntity(relation.getRelEntityName());

    boolean relCreate = false;
    if (security.hasEntityPermission(relatedEntity.getPlainTableName(), "_CREATE", session)) {
        relCreate = true;
    }

    mapRelation.put("type", relation.getType());
    mapRelation.put("title", relation.getTitle());
    mapRelation.put("relEntityName", relation.getRelEntityName());
    mapRelation.put("sortName", relation.getTitle() + relation.getRelEntityName());
    mapRelation.put("relatedTable", relatedEntity.getEntityName());
    mapRelation.put("relCreate", relCreate);

    if ("one".equals(relation.getType()) || "one-nofk".equals(relation.getType())) {
        if (value != null) {
            if (hasAllView || security.hasEntityPermission(relatedEntity.getPlainTableName(), "_VIEW", session)) {
                Iterator tempIter = UtilMisc.toIterator(value.getRelated(relation.getTitle() + relatedEntity.getEntityName()));
                GenericValue valueRelated = null;
                if (tempIter != null && tempIter.hasNext()) {
                    valueRelated = (GenericValue) tempIter.next();
                }

                List relatedFieldsList = FastList.newInstance();
                Iterator relFieldIterator = relatedEntity.getFieldsIterator();
                while (relFieldIterator.hasNext()) {
                    Map mapRelatedFields = FastMap.newInstance();
                    ModelField field = relFieldIterator.next();
                    ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());

                    String fieldValue = "";
                    String fieldType = "";
                    if (type.getJavaType().equals("Timestamp") || type.getJavaType().equals("java.sql.Timestamp")) {
                        Timestamp dtVal = null;
                        if (valueRelated != null) {
                            dtVal = valueRelated.getTimestamp(field.getName());
                        }
                        fieldValue = (dtVal == null) ? "" : dtVal.toString();
                        fieldType = "DateTime";
                    } else if (type.getJavaType().equals("Date") || type.getJavaType().equals("java.sql.Date")) {
                        Date dateVal = null;
                        if (valueRelated != null) {
                            dateVal = valueRelated.getDate(field.getName());
                        }
                        fieldValue = (dateVal == null) ? "" : dateVal.toString();
                        fieldType = "Date";
                    } else if (type.getJavaType().equals("Time") || type.getJavaType().equals("java.sql.Time")) {
                        Time timeVal = null;
                        if (valueRelated != null) {
                            timeVal = valueRelated.getTime(field.getName());
                        }
                        fieldValue = (timeVal == null) ? "" : timeVal.toString();
                        fieldType = "Time";
                    } else if (type.getJavaType().indexOf("Integer") >= 0) {
                        if (valueRelated != null) {
                            fieldValue = UtilFormatOut.safeToString((Integer)valueRelated.get(field.getName()));
                        }
                        fieldType = "Integer";
                    } else if (type.getJavaType().indexOf("Long") >= 0) {
                        if (valueRelated != null) {
                            fieldValue = UtilFormatOut.safeToString((Long)valueRelated.get(field.getName()));
                        }
                        fieldType = "Long";
                    } else if (type.getJavaType().indexOf("Double") >= 0) {
                        if (valueRelated != null) {
                            fieldValue = UtilFormatOut.safeToString((Double)valueRelated.get(field.getName()));
                        }
                        fieldType = "Double";
                    } else if (type.getJavaType().indexOf("Float") >= 0) {
                        if (valueRelated != null) {
                            fieldValue = UtilFormatOut.safeToString((Float)valueRelated.get(field.getName()));
                        }
                        fieldType = "Float";
                    } else if (type.getJavaType().indexOf("String") >= 0) {
                        if (valueRelated != null) {
                            fieldValue = UtilFormatOut.checkNull((String)valueRelated.get(field.getName()));
                        }
                        fieldType = "String";
                    }

                    mapRelatedFields.put("name", field.getName());
                    mapRelatedFields.put("type", fieldType);
                    mapRelatedFields.put("value", fieldValue);
                    relatedFieldsList.add(mapRelatedFields);
                }

                mapRelation.put("valueRelated", valueRelated);
                if (valueRelated != null) {
                    mapRelation.put("valueRelatedPk", valueRelated.getPrimaryKey().toString());
                }
                mapRelation.put("relatedFieldsList", relatedFieldsList);
                mapRelation.put("relType", "one");

                String findString = "entityName=" + relatedEntity.getEntityName();
                for (int knum = 0; knum < relation.getKeyMapsSize(); knum++) {
                    ModelKeyMap keyMap = relation.getKeyMap(knum);
                    if (value.get(keyMap.getFieldName()) != null) {
                        findString += "&" + keyMap.getRelFieldName() + "=" + value.get(keyMap.getFieldName());
                    }
                }
                String encodeFindString = UtilFormatOut.encodeQuery(findString);
                mapRelation.put("encodeRelatedEntityFindString", encodeFindString);

                relationFieldList.add(mapRelation);
            }
        }
    } else if (relation.getType().equalsIgnoreCase("many")) {
        if (value != null) {
            if (hasAllView || security.hasEntityPermission(relatedEntity.getPlainTableName(), "_VIEW", session)) {
                mapRelation.put("relType", "many");

                String findString = "entityName=" + relatedEntity.getEntityName();
                for (int knum = 0; knum < relation.getKeyMapsSize(); knum++) {
                    ModelKeyMap keyMap = relation.getKeyMap(knum);
                    if (value.get(keyMap.getFieldName()) != null) {
                        findString += "&" + keyMap.getRelFieldName() + "=" + value.get(keyMap.getFieldName());
                    }
                }
                String encodeFindString = UtilFormatOut.encodeQuery(findString);
                mapRelation.put("encodeRelatedEntityFindString", encodeFindString);

                relationFieldList.add(mapRelation);
            }
        }
    }
}
context.put("relationFieldList", UtilMisc.sortMaps(relationFieldList, UtilMisc.toList("sortName")));
context.put("relSize", relationFieldList.size() + 2);
