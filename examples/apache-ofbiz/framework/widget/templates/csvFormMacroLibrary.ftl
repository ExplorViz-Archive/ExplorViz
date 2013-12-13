<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#macro renderField text><#if text?exists>"${text?replace("\"", "\"\"")}"</#if></#macro>

<#macro renderDisplayField type imageLocation idName description title class alert inPlaceEditorId="" inPlaceEditorUrl="" inPlaceEditorParams="">
<@renderField description />,<#rt/>
</#macro>
<#macro renderHyperlinkField></#macro>

<#macro renderTextField name className alert value textSize maxlength id event action disabled clientAutocomplete ajaxUrl ajaxEnabled mask><@renderField value /></#macro>

<#macro renderTextareaField name className alert cols rows id readonly value visualEditorEnable language buttons><@renderField value /></#macro>

<#macro renderDateTimeField name className alert title value size maxlength step timeValues id dateType shortDateInput timeDropdownParamName defaultDateTimeString localizedIconTitle timeDropdown timeHourName classString hour1 hour2 timeMinutesName minutes isTwelveHour ampmName amSelected pmSelected compositeType formName mask><@renderField value /></#macro>

<#macro renderDropDownField name className alert id multiple formName otherFieldName event action size firstInList currentValue explicitDescription allowEmpty options fieldName otherFieldName otherValue otherFieldSize dDFCurrent ajaxEnabled noCurrentSelectedKey ajaxOptions frequency minChars choices autoSelect partialSearch partialChars ignoreCase fullSearch>
<#if currentValue?has_content && firstInList?has_content>
<@renderField explicitDescription />
<#else>
<#list options as item>
<@renderField item.description />
</#list>
</#if>
</#macro>

<#macro renderTooltip tooltip tooltipStyle></#macro>
<#macro renderCheckField items className alert id allChecked currentValue name event action></#macro>
<#macro renderRadioField items className alert currentValue noCurrentSelectedKey name event ation></#macro>

<#macro renderSubmitField buttonType className alert formName title name event action imgSrc confirmation containerId ajaxUrl></#macro>
<#macro renderResetField className alert name title></#macro>

<#macro renderHiddenField name value id event action></#macro>
<#macro renderIgnoredField></#macro>

<#macro renderFieldTitle style title id="" fieldHelpText=""><@renderField title />,</#macro>
<#macro renderSingleFormFieldTitle></#macro>

<#macro renderFormOpen linkUrl formType targetWindow containerId containerStyle autocomplete name useRowSubmit></#macro>
<#macro renderFormClose focusFieldName formName containerId hasRequiredField></#macro>
<#macro renderMultiFormClose></#macro>

<#macro renderFormatListWrapperOpen formName style columnStyles></#macro>
<#macro renderFormatListWrapperClose formName></#macro>

<#macro renderFormatHeaderRowOpen style></#macro>
<#macro renderFormatHeaderRowClose>

</#macro>
<#macro renderFormatHeaderRowCellOpen style positionSpan></#macro>
<#macro renderFormatHeaderRowCellClose></#macro>

<#macro renderFormatHeaderRowFormCellOpen style> </#macro>
<#macro renderFormatHeaderRowFormCellClose></#macro>
<#macro renderFormatHeaderRowFormCellTitleSeparator style isLast></#macro>

<#macro renderFormatItemRowOpen formName itemIndex altRowStyles evenRowStyle oddRowStyle></#macro>
<#macro renderFormatItemRowClose formName>

</#macro>
<#macro renderFormatItemRowCellOpen fieldName style positionSpan></#macro>
<#macro renderFormatItemRowCellClose fieldName></#macro>
<#macro renderFormatItemRowFormCellOpen style></#macro>
<#macro renderFormatItemRowFormCellClose></#macro>

<#macro renderFormatSingleWrapperOpen formName style></#macro>
<#macro renderFormatSingleWrapperClose formName>
</#macro>

<#macro renderFormatFieldRowOpen></#macro>
<#macro renderFormatFieldRowClose>
</#macro>
<#macro renderFormatFieldRowTitleCellOpen style> </#macro>
<#macro renderFormatFieldRowTitleCellClose></#macro>
<#macro renderFormatFieldRowSpacerCell></#macro>
<#macro renderFormatFieldRowWidgetCellOpen positionSpan style></#macro>
<#macro renderFormatFieldRowWidgetCellClose></#macro>

<#macro renderFormatEmptySpace>&nbsp;</#macro>

<#macro renderTextFindField name value defaultOption opEquals opBeginsWith opContains opIsEmpty opNotEqual className alert size maxlength autocomplete titleStyle hideIgnoreCase ignCase ignoreCase><@renderField value /></#macro>

<#macro renderDateFindField className alert name localizedInputTitle value size maxlength dateType formName defaultDateTimeString imgSrc localizedIconTitle titleStyle defaultOptionFrom defaultOptionThru opEquals opSameDay opGreaterThanFromDayStart opGreaterThan opGreaterThan opLessThan opUpToDay opUpThruDay opIsEmpty><@renderField value /></#macro>

<#macro renderRangeFindField className alert name value size maxlength autocomplete titleStyle defaultOptionFrom opEquals opGreaterThan opGreaterThanEquals opLessThan opLessThanEquals value2 defaultOptionThru>
<@renderField value />
</#macro>

<#macro renderLookupField className alert name value size maxlength id event action readonly autocomplete descriptionFieldName formName fieldFormName targetParameterIter imgSrc ajaxUrl ajaxEnabled presentation width height position fadeBackground clearText showDescription initiallyCollapsed></#macro>
<#macro renderNextPrev paginateStyle paginateFirstStyle viewIndex highIndex listSize viewSize ajaxEnabled javaScriptEnabled ajaxFirstUrl firstUrl paginateFirstLabel paginatePreviousStyle ajaxPreviousUrl previousUrl paginatePreviousLabel pageLabel ajaxSelectUrl selectUrl ajaxSelectSizeUrl selectSizeUrl commonDisplaying paginateNextStyle ajaxNextUrl nextUrl paginateNextLabel paginateLastStyle ajaxLastUrl lastUrl paginateLastLabel paginateViewSizeLabel></#macro>
<#macro renderFileField className alert name value size maxlength autocomplete><@renderField value /></#macro>
<#macro renderPasswordField className alert name value size maxlength id autocomplete></#macro>
<#macro renderImageField value description alternate border width height event action></#macro>
<#macro renderBanner style leftStyle rightStyle leftText text rightText></#macro>
<#macro renderFieldGroupOpen style id title collapsed collapsibleAreaId collapsible expandToolTip collapseToolTip></#macro>
<#macro renderFieldGroupClose style id title></#macro>

<#macro renderHyperlinkTitle name title showSelectAll="N"></#macro>
<#macro renderSortField style title linkUrl ajaxEnabled><@renderFieldTitle style title /></#macro>
<#macro formatBoundaryComment boundaryType widgetType widgetName></#macro>
<#macro makeHiddenFormLinkAnchor linkStyle hiddenFormName event action imgSrc description><@renderField description /></#macro>
<#macro makeHyperlinkString linkStyle hiddenFormName event action imgSrc title alternate linkUrl targetWindow description confirmation><@renderField description />,<#rt/></#macro>
