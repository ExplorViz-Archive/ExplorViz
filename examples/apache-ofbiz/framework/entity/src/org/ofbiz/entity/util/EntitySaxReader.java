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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.text.CharArray;
import javolution.text.Text;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.sax.Attributes;
import javolution.xml.sax.XMLReaderImpl;

import org.ofbiz.base.location.FlexibleLocation;
import org.ofbiz.base.util.Base64;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.eca.EntityEcaHandler;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;

/**
 * SAX XML Parser Content Handler for Entity Engine XML files
 */
public class EntitySaxReader implements javolution.xml.sax.ContentHandler, ErrorHandler {

    public static final String module = EntitySaxReader.class.getName();
    public static final int DEFAULT_TX_TIMEOUT = 7200;

    protected org.xml.sax.Locator locator;
    protected Delegator delegator;
    protected EntityEcaHandler<?> ecaHandler = null;
    protected GenericValue currentValue = null;
    protected CharSequence currentFieldName = null;
    protected CharSequence currentFieldValue = null;
    protected long numberRead = 0;

    protected int valuesPerWrite = 100;
    protected int valuesPerMessage = 1000;
    protected int transactionTimeout = 7200;
    protected boolean useTryInsertMethod = false;
    protected boolean maintainTxStamps = false;
    protected boolean createDummyFks = false;
    protected boolean checkDataOnly = false;
    protected boolean doCacheClear = true;
    protected boolean disableEeca = false;
    protected List<Object> messageList = null;

    protected List<GenericValue> valuesToWrite = new ArrayList<GenericValue>(valuesPerWrite);

    protected boolean isParseForTemplate = false;
    protected CharSequence templatePath = null;
    protected Node rootNodeForTemplate = null;
    protected Node currentNodeForTemplate = null;
    protected Document documentForTemplate = null;

    protected EntitySaxReader() {}

    public EntitySaxReader(Delegator delegator, int transactionTimeout) {
        // clone the delegator right off so there is no chance of making change to the initial object
        this.delegator = delegator.cloneDelegator();
        this.transactionTimeout = transactionTimeout;
    }

    public EntitySaxReader(Delegator delegator) {
        this(delegator, DEFAULT_TX_TIMEOUT);
    }

    public int getValuesPerWrite() {
        return this.valuesPerWrite;
    }

    public void setValuesPerWrite(int valuesPerWrite) {
        this.valuesPerWrite = valuesPerWrite;
    }

    public int getValuesPerMessage() {
        return this.valuesPerMessage;
    }

    public void setValuesPerMessage(int valuesPerMessage) {
        this.valuesPerMessage = valuesPerMessage;
    }

    public int getTransactionTimeout() {
        return this.transactionTimeout;
    }

    public void setUseTryInsertMethod(boolean value) {
        this.useTryInsertMethod = value;
    }

    public void setTransactionTimeout(int transactionTimeout) throws GenericTransactionException {
        if (this.transactionTimeout != transactionTimeout) {
            TransactionUtil.setTransactionTimeout(transactionTimeout);
            this.transactionTimeout = transactionTimeout;
        }
    }

    public boolean getMaintainTxStamps() {
        return this.maintainTxStamps;
    }

    public void setMaintainTxStamps(boolean maintainTxStamps) {
        this.maintainTxStamps = maintainTxStamps;
    }

    public boolean getCreateDummyFks() {
        return this.createDummyFks;
    }

    public void setCreateDummyFks(boolean createDummyFks) {
        this.createDummyFks = createDummyFks;
    }

    public boolean getCheckDataOnly() {
        return this.checkDataOnly;
    }

    public void setCheckDataOnly(boolean checkDataOnly) {
        this.checkDataOnly = checkDataOnly;
    }

    public boolean getDoCacheClear() {
        return this.doCacheClear;
    }

    public void setDoCacheClear(boolean doCacheClear) {
        this.doCacheClear = doCacheClear;
    }

    public boolean getDisableEeca() {
        return this.disableEeca;
    }

    public List<Object> getMessageList() {
        if (this.checkDataOnly && this.messageList == null) {
            messageList = FastList.newInstance();
        }
        return this.messageList;
    }

    public void setMessageList(List<Object> messageList) {
        this.messageList = messageList;
    }

    public void setDisableEeca(boolean disableEeca) {
        this.disableEeca = disableEeca;
        if (disableEeca) {
            if (this.ecaHandler == null) {
                this.ecaHandler = delegator.getEntityEcaHandler();
            }
            this.delegator.setEntityEcaHandler(null);
        } else {
            if (ecaHandler != null) {
                this.delegator.setEntityEcaHandler(ecaHandler);
            }
        }
    }

    public long parse(String content) throws SAXException, java.io.IOException {
        if (content == null) {
            Debug.logWarning("content was null, doing nothing", module);
            return 0;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes("UTF-8"));

        return this.parse(bis, "Internal Content");
    }

    public long parse(URL location) throws SAXException, java.io.IOException {
        if (location == null) {
            Debug.logWarning("location URL was null, doing nothing", module);
            return 0;
        }
        Debug.logImportant("Beginning import from URL: " + location.toExternalForm(), module);
        InputStream is = null;
        long numberRead = 0;
        try {
            is = location.openStream();
            numberRead = this.parse(is, location.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch(Exception e) {}
            }
        }
        return numberRead;
    }

    public long parse(InputStream is, String docDescription) throws SAXException, java.io.IOException {

        /* NOTE: this method is not used because it doesn't work with various parsers...
         String orgXmlSaxDriver = System.getProperty("org.xml.sax.driver");
         if (UtilValidate.isEmpty(orgXmlSaxDriver)) orgXmlSaxDriver = "org.apache.xerces.parsers.SAXParser";
         XMLReader reader = XMLReaderFactory.createXMLReader(orgXmlSaxDriver);
         */

        /* This code is for a standard SAXParser and XMLReader like xerces or such; for speed we are using the Javolution reader
        XMLReader reader = null;

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();

            reader = parser.getXMLReader();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            Debug.logError(e, "Failed to get a SAX XML parser", module);
            throw new IllegalStateException("Failed to get a SAX XML parser");
        }
        */

        XMLReaderImpl parser = new XMLReaderImpl();

        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        // LocalResolver lr = new UtilXml.LocalResolver(new DefaultHandler());
        // reader.setEntityResolver(lr);

        numberRead = 0;
        try {
            boolean beganTransaction = false;
            if (transactionTimeout > -1) {
                beganTransaction = TransactionUtil.begin(transactionTimeout);
                Debug.logImportant("Transaction Timeout set to " + transactionTimeout / 3600 + " hours (" + transactionTimeout + " seconds)", module);
            }
            try {
                parser.parse(is);
                // make sure all of the values to write got written...
                if (valuesToWrite.size() > 0) {
                    writeValues(valuesToWrite);
                    valuesToWrite.clear();
                }
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
                String errMsg = "An error occurred saving the data, rolling back transaction (" + beganTransaction + ")";
                Debug.logError(e, errMsg, module);
                TransactionUtil.rollback(beganTransaction, errMsg, e);
                throw new SAXException("A transaction error occurred reading data", e);
            }
        } catch (GenericTransactionException e) {
            throw new SAXException("A transaction error occurred reading data", e);
        }
        Debug.logImportant("Finished " + numberRead + " values from " + docDescription, module);
        return numberRead;
    }

    protected void writeValues(List<GenericValue> valuesToWrite) throws GenericEntityException {
        if (this.checkDataOnly) {
            EntityDataAssert.checkValueList(valuesToWrite, delegator, this.getMessageList());
        } else {
            delegator.storeAll(valuesToWrite, doCacheClear, createDummyFks);
        }
    }

    public void characters(char[] values, int offset, int count) throws org.xml.sax.SAXException {
        if (isParseForTemplate) {
            // if null, don't worry about it
            if (this.currentNodeForTemplate != null) {
                Node newNode = this.documentForTemplate.createTextNode(new String(values, offset, count));
                this.currentNodeForTemplate.appendChild(newNode);
            }
            return;
        }

        if (currentValue != null && currentFieldName != null) {
            Text value = Text.valueOf(values, offset, count);

            // Debug.logInfo("characters: value=" + value, module);
            if (currentFieldValue == null) {
                currentFieldValue = value;
            } else {
                currentFieldValue = Text.valueOf(currentFieldValue).concat(value);
            }
        }
    }

    public void endDocument() throws org.xml.sax.SAXException {}

    public void endElement(CharArray namespaceURI, CharArray localName, CharArray fullName) throws org.xml.sax.SAXException {
        if (Debug.verboseOn()) Debug.logVerbose("endElement: localName=" + localName + ", fullName=" + fullName + ", numberRead=" + numberRead, module);
        String fullNameString = fullName.toString();
        if ("entity-engine-xml".equals(fullNameString)) {
            return;
        }
        if ("entity-engine-transform-xml".equals(fullNameString)) {
            // transform file & parse it, then return
            URL templateUrl = null;
            try {
                templateUrl = FlexibleLocation.resolveLocation(templatePath.toString());
            } catch (MalformedURLException e) {
                throw new SAXException("Could not find transform template with resource path [" + templatePath + "]; error was: " + e.toString());
            }

            if (templateUrl == null) {
                throw new SAXException("Could not find transform template with resource path: " + templatePath);
            } else {
                try {
                    Reader templateReader = new InputStreamReader(templateUrl.openStream());

                    StringWriter outWriter = new StringWriter();
                    Configuration config = new Configuration();
                    config.setObjectWrapper(BeansWrapper.getDefaultInstance());
                    config.setSetting("datetime_format", "yyyy-MM-dd HH:mm:ss.SSS");

                    Template template = new Template("FMImportFilter", templateReader, config);
                    NodeModel nodeModel = NodeModel.wrap(this.rootNodeForTemplate);

                    Map<String, Object> context = FastMap.newInstance();
                    BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
                    TemplateHashModel staticModels = wrapper.getStaticModels();
                    context.put("Static", staticModels);

                    context.put("doc", nodeModel);
                    template.process(context, outWriter);
                    String s = outWriter.toString();
                    if (Debug.verboseOn()) Debug.logVerbose("transformed xml: " + s, module);

                    EntitySaxReader reader = new EntitySaxReader(delegator);
                    reader.setUseTryInsertMethod(this.useTryInsertMethod);
                    try {
                        reader.setTransactionTimeout(this.transactionTimeout);
                    } catch (GenericTransactionException e1) {
                        // couldn't set tx timeout, shouldn't be a big deal
                    }

                    numberRead += reader.parse(s);
                } catch (TemplateException e) {
                    throw new SAXException("Error storing value", e);
                } catch (IOException e) {
                    throw new SAXException("Error storing value", e);
                }
            }

            return;
        }

        if (isParseForTemplate) {
            this.currentNodeForTemplate = this.currentNodeForTemplate.getParentNode();
            return;
        }

        if (currentValue != null) {
            if (currentFieldName != null) {
                if (UtilValidate.isNotEmpty(currentFieldValue)) {
                    if (currentValue.getModelEntity().isField(currentFieldName.toString())) {
                        ModelEntity modelEntity = currentValue.getModelEntity();
                        ModelField modelField = modelEntity.getField(currentFieldName.toString());
                        String type = modelField.getType();
                        if (type != null && type.equals("blob")) {
                            byte strData[] = new byte[currentFieldValue.length()];
                            strData = currentFieldValue.toString().getBytes();
                            byte binData[] = new byte[currentFieldValue.length()];
                            binData = Base64.base64Decode(strData);
                            currentValue.setBytes(currentFieldName.toString(), binData);
                        } else {
                            currentValue.setString(currentFieldName.toString(), currentFieldValue.toString());
                        }
                    } else {
                        Debug.logWarning("Ignoring invalid field name [" + currentFieldName + "] found for the entity: " + currentValue.getEntityName() + " with value=" + currentFieldValue, module);
                    }
                    currentFieldValue = null;
                }
                currentFieldName = null;
            } else {
                // before we write currentValue check to see if PK is there, if not and it is one field, generate it from a sequence using the entity name
                if (!currentValue.containsPrimaryKey()) {
                    if (currentValue.getModelEntity().getPksSize() == 1) {
                        ModelField modelField = currentValue.getModelEntity().getOnlyPk();
                        String newSeq = delegator.getNextSeqId(currentValue.getEntityName());
                        currentValue.setString(modelField.getName(), newSeq);
                    } else {
                        throw new SAXException("Cannot store value with incomplete primary key with more than 1 primary key field: " + currentValue);
                    }
                }

                try {
                    if (this.useTryInsertMethod && !this.checkDataOnly) {
                        // this technique is faster for data sets where most, if not all, values do not already exist in the database
                        try {
                            currentValue.create();
                        } catch (GenericEntityException e1) {
                            // create failed, try a store, if that fails too we have a real error and the catch outside of this should handle it
                            currentValue.store();
                        }
                    } else {
                        valuesToWrite.add(currentValue);
                        if (valuesToWrite.size() >= valuesPerWrite) {
                            writeValues(valuesToWrite);
                            valuesToWrite.clear();
                        }
                    }
                    numberRead++;
                    if ((numberRead % valuesPerMessage) == 0) {
                        Debug.logImportant("Another " + valuesPerMessage + " values imported: now up to " + numberRead, module);
                    }
                    currentValue = null;
                } catch (GenericEntityException e) {
                    String errMsg = "Error storing value";
                    Debug.logError(e, errMsg, module);
                    throw new SAXException(errMsg, e);
                }
            }
        }
    }

    public void endPrefixMapping(CharArray prefix) throws org.xml.sax.SAXException {}

    public void ignorableWhitespace(char[] values, int offset, int count) throws org.xml.sax.SAXException {
        // String value = new String(values, offset, count);
        // Debug.logInfo("ignorableWhitespace: value=" + value, module);
    }

    public void processingInstruction(CharArray target, CharArray instruction) throws org.xml.sax.SAXException {}

    public void setDocumentLocator(org.xml.sax.Locator locator) {
        this.locator = locator;
    }

    public void skippedEntity(CharArray name) throws org.xml.sax.SAXException {}

    public void startDocument() throws org.xml.sax.SAXException {}

    public void startElement(CharArray namepsaceURI, CharArray localName, CharArray fullName, Attributes attributes) throws org.xml.sax.SAXException {
        if (Debug.verboseOn()) Debug.logVerbose("startElement: localName=" + localName + ", fullName=" + fullName + ", attributes=" + attributes, module);
        String fullNameString = fullName.toString();
        if ("entity-engine-xml".equals(fullNameString)) {
            // check the maintain-timestamp flag
            CharSequence maintainTx = attributes.getValue("maintain-timestamps");
            if (maintainTx != null) {
                this.setMaintainTxStamps("true".equalsIgnoreCase(maintainTx.toString()));
            }

            // check the do-cache-clear flag
            CharSequence doCacheClear = attributes.getValue("do-cache-clear");
            if (doCacheClear != null) {
                this.setDoCacheClear("true".equalsIgnoreCase(doCacheClear.toString()));
            }

            // check the disable-eeca flag
            CharSequence ecaDisable = attributes.getValue("disable-eeca");
            if (ecaDisable != null) {
                this.setDisableEeca("true".equalsIgnoreCase(ecaDisable.toString()));
            }

            // check the use-dummy-fk flag
            CharSequence dummyFk = attributes.getValue("create-dummy-fk");
            if (dummyFk != null) {
                this.setCreateDummyFks("true".equalsIgnoreCase(dummyFk.toString()));
            }

            return;
        }

        if ("entity-engine-transform-xml".equals(fullNameString)) {
            templatePath = attributes.getValue("template");
            isParseForTemplate = true;
            documentForTemplate = UtilXml.makeEmptyXmlDocument();
            return;
        }

        if (isParseForTemplate) {
            Element newElement = this.documentForTemplate.createElement(fullNameString);
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                CharSequence name = attributes.getLocalName(i);
                CharSequence value = attributes.getValue(i);

                if (UtilValidate.isEmpty(name)) {
                    name = attributes.getQName(i);
                }
                newElement.setAttribute(name.toString(), value.toString());
            }

            if (this.currentNodeForTemplate == null) {
                this.currentNodeForTemplate = newElement;
                this.rootNodeForTemplate = newElement;
            } else {
                this.currentNodeForTemplate.appendChild(newElement);
                this.currentNodeForTemplate = newElement;
            }
            return;
        }

        if (currentValue != null) {
            // we have a nested value/CDATA element
            currentFieldName = fullName;
        } else {
            String entityName = fullNameString;

            // if a dash or colon is in the tag name, grab what is after it
            if (entityName.indexOf('-') > 0) {
                entityName = entityName.substring(entityName.indexOf('-') + 1);
            }
            if (entityName.indexOf(':') > 0) {
                entityName = entityName.substring(entityName.indexOf(':') + 1);
            }

            try {
                currentValue = delegator.makeValue(entityName);
                // TODO: do we really want this? it makes it so none of the values imported have create/update timestamps set
                // DEJ 10/16/04 I think they should all be stamped, so commenting this out
                // JAZ 12/10/04 I think it should be specified when creating the reader
                if (this.maintainTxStamps) {
                    currentValue.setIsFromEntitySync(true);
                }
            } catch (Exception e) {
                Debug.logError(e, module);
            }

            if (currentValue != null) {
                int length = attributes.getLength();

                for (int i = 0; i < length; i++) {
                    CharSequence name = attributes.getLocalName(i);
                    CharSequence value = attributes.getValue(i);

                    if (UtilValidate.isEmpty(name)) {
                        name = attributes.getQName(i);
                    }
                    try {
                        // treat empty strings as nulls
                        if (UtilValidate.isNotEmpty(value)) {
                            if (currentValue.getModelEntity().isField(name.toString())) {
                                currentValue.setString(name.toString(), value.toString());
                            } else {
                                Debug.logWarning("Ignoring invalid field name [" + name + "] found for the entity: " + currentValue.getEntityName() + " with value=" + value, module);
                            }
                        }
                    } catch (Exception e) {
                        Debug.logWarning(e, "Could not set field " + entityName + "." + name + " to the value " + value, module);
                    }
                }
            }
        }
    }

    //public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {}
    public void startPrefixMapping(CharArray arg0, CharArray arg1) throws SAXException {}

    // ======== ErrorHandler interface implementations ========

    public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logWarning(exception, "Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
    }

    public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logError(exception, "Fatal Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
        throw new SAXException("Fatal Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), exception);
    }

    public void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logWarning(exception, "Warning reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
    }
}
