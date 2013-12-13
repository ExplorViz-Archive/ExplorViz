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
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.PageTitleEditSurveyQuestions} ${uiLabelMap.ContentSurveySurveyId} ${surveyId}</li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
      <table class="basic-table hover-bar" cellspacing="0">
        <tr class="header-row">
          <td>${uiLabelMap.CommonId}</td>
          <td>${uiLabelMap.CommonType}</td>
          <td>${uiLabelMap.ContentSurveryCategory}</td>
          <td>${uiLabelMap.CommonDescription}</td>
          <td>${uiLabelMap.ContentSurveyQuestion}</td>
          <td>${uiLabelMap.CommonPage}</td>
          <td>${uiLabelMap.ContentSurveyMultiResp}</td>
          <td>${uiLabelMap.ContentSurveyMultiRespColumn}</td>
          <td>${uiLabelMap.CommonRequired}</td>
          <td>${uiLabelMap.CommonSequenceNum}</td>
          <td>${uiLabelMap.ContentSurveyWithQuestion}</td>
          <td>${uiLabelMap.ContentSurveyWithOption}</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        <#assign alt_row = false>
        <#list surveyQuestionAndApplList as surveyQuestionAndAppl>
          <#assign questionType = surveyQuestionAndAppl.getRelatedOneCache("SurveyQuestionType")/>
          <#assign questionCat = surveyQuestionAndAppl.getRelatedOneCache("SurveyQuestionCategory")?if_exists/>
          <#assign currentSurveyPage = surveyQuestionAndAppl.getRelatedOneCache("SurveyPage")?if_exists/>
          <#assign currentSurveyMultiResp = surveyQuestionAndAppl.getRelatedOneCache("SurveyMultiResp")?if_exists/>
          <#if currentSurveyMultiResp?has_content>
            <#assign currentSurveyMultiRespColumns = currentSurveyMultiResp.getRelated("SurveyMultiRespColumn")/>
          <#else/>
            <#assign currentSurveyMultiRespColumns = []/>
          </#if>
          
            <tr<#if alt_row> class="alternate-row"</#if>>
            <form method="post" action="<@ofbizUrl>updateSurveyQuestionAppl</@ofbizUrl>">
              <input type="hidden" name="surveyId" value="${surveyQuestionAndAppl.surveyId}" />
              <input type="hidden" name="surveyQuestionId" value="${surveyQuestionAndAppl.surveyQuestionId}" />
              <input type="hidden" name="fromDate" value="${surveyQuestionAndAppl.fromDate}" />
              <td>${surveyQuestionAndAppl.surveyQuestionId}</td>
              <td>${questionType.get("description",locale)}</td>
              <td>${(questionCat.description)?if_exists}</td>
              <td>${surveyQuestionAndAppl.description?if_exists}</td>
              <td><input type="text" name="question" size="30" value="${surveyQuestionAndAppl.question?if_exists?html}" />
              <td>
                <select name="surveyPageId">
                  <#if surveyQuestionAndAppl.surveyPageSeqId?has_content>
                    <option value="${surveyQuestionAndAppl.surveyPageSeqId}">${(currentSurveyPage.pageName)?if_exists} [${surveyQuestionAndAppl.surveyPageSeqId}]</option>
                    <option value="${surveyQuestionAndAppl.surveyPageSeqId}">----</option>
                  </#if>
                  <option value=""></option>
                  <#list surveyPageList as surveyPage>
                    <option value="${surveyPage.surveyPageSeqId}">${surveyPage.pageName?if_exists} [${surveyPage.surveyPageSeqId}]</option>
                  </#list>
                </select>
              </td>
              <td>
                <select name="surveyMultiRespId">
                  <#if surveyQuestionAndAppl.surveyMultiRespId?has_content>
                    <option value="${surveyQuestionAndAppl.surveyMultiRespId}">${(currentSurveyMultiResp.multiRespTitle)?if_exists} [${surveyQuestionAndAppl.surveyMultiRespId}]</option>
                    <option value="${surveyQuestionAndAppl.surveyMultiRespId}">----</option>
                  </#if>
                  <option value=""></option>
                  <#list surveyMultiRespList as surveyMultiResp>
                    <option value="${surveyMultiResp.surveyMultiRespId}">${surveyMultiResp.multiRespTitle} [${surveyMultiResp.surveyMultiRespId}]</option>
                  </#list>
                </select>
              </td>
              <#if currentSurveyMultiRespColumns?has_content>
              <td>
                <select name="surveyMultiRespColId">
                  <#if surveyQuestionAndAppl.surveyMultiRespColId?has_content>
                    <#assign currentSurveyMultiRespColumn = surveyQuestionAndAppl.getRelatedOne("SurveyMultiRespColumn")/>
                    <option value="${currentSurveyMultiRespColumn.surveyMultiRespColId}">${(currentSurveyMultiRespColumn.columnTitle)?if_exists} [${currentSurveyMultiRespColumn.surveyMultiRespColId}]</option>
                    <option value="${currentSurveyMultiRespColumn.surveyMultiRespColId}">----</option>
                  </#if>
                  <option value=""></option>
                  <#list currentSurveyMultiRespColumns as currentSurveyMultiRespColumn>
                    <option value="${currentSurveyMultiRespColumn.surveyMultiRespColId}">${currentSurveyMultiRespColumn.columnTitle} [${currentSurveyMultiRespColumn.surveyMultiRespColId}]</option>
                  </#list>
                </select>
              </td>
              <#else/>
                <td><input type="text" name="surveyMultiRespColId" size="4" value="${surveyQuestionAndAppl.surveyMultiRespColId?if_exists}"/></td>
              </#if>
              <td>
                <select name="requiredField">
                  <option>${surveyQuestionAndAppl.requiredField?default("N")}</option>
                  <option value="${surveyQuestionAndAppl.requiredField?default("N")}">----</option>
                  <option>Y</option><option>N</option>
                </select>
              </td>
              <td><input type="text" name="sequenceNum" size="5" value="${surveyQuestionAndAppl.sequenceNum?if_exists}"/></td>
              <td><input type="text" name="withSurveyQuestionId" size="5" value="${surveyQuestionAndAppl.withSurveyQuestionId?if_exists}"/></td>
              <td><input type="text" name="withSurveyOptionSeqId" size="5" value="${surveyQuestionAndAppl.withSurveyOptionSeqId?if_exists}"/></td>
              <td><input type="submit" value="${uiLabelMap.CommonUpdate}" class="smallSubmit"/></td>
              <td><a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}&amp;surveyQuestionId=${surveyQuestionAndAppl.surveyQuestionId}#edit</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonEdit}&nbsp;${uiLabelMap.ContentSurveyQuestion}</a></td>
              </form>
              <td>
                <form id="removeSurveyQuestion_${surveyQuestionAndAppl.surveyQuestionId}" action="<@ofbizUrl>removeSurveyQuestionAppl</@ofbizUrl>" method="post">
                  <input type="hidden" name="surveyId" value="${surveyQuestionAndAppl.surveyId}" />
                  <input type="hidden" name="surveyQuestionId" value="${surveyQuestionAndAppl.surveyQuestionId}" />
                  <input type="hidden" name="fromDate" value="${surveyQuestionAndAppl.fromDate}" />
                  <a href="javascript:document.getElementById('removeSurveyQuestion_${surveyQuestionAndAppl.surveyQuestionId}').submit();"" class="buttontext">${uiLabelMap.CommonRemove}</a>
                </form>
              </td>
            </tr>
          <#assign alt_row = !alt_row>
        </#list>
      </table>
  </div>
</div>
<#-- apply question from category -->
<#if surveyQuestionCategory?has_content>
    <div class="screenlet">
      <div class="screenlet-title-bar">
        <ul>
          <li class="h3">${uiLabelMap.ContentSurveyApplyQuestionFromCategory} - ${surveyQuestionCategory.description?if_exists} [${surveyQuestionCategory.surveyQuestionCategoryId}]</li>
        </ul>
        <br class="clear"/>
      </div>
      <div class="screenlet-body">
        <a name="appl">
        <table class="basic-table hover-bar" cellspacing="0">
            <tr class="header-row">
                <td>${uiLabelMap.CommonId}</td>
                <td>${uiLabelMap.CommonDescription}</td>
                <td>${uiLabelMap.CommonType}</td>
                <td>${uiLabelMap.ContentSurveyQuestion}</td>
                <td>${uiLabelMap.CommonPage}</td>
                <td>${uiLabelMap.ContentSurveyMultiResp}</td>
                <td>${uiLabelMap.ContentSurveyMultiRespColumn}</td>
                <td>${uiLabelMap.CommonRequired}</td>
                <td>${uiLabelMap.CommonSequenceNum}</td>
                <td>${uiLabelMap.ContentSurveyWithQuestion}</td>
                <td>${uiLabelMap.ContentSurveyWithOption}</td>
                <td>&nbsp;</td>
              </tr>
          <#assign alt_row = false>
          <#list categoryQuestions as question>
            <#assign questionType = question.getRelatedOne("SurveyQuestionType")>
            <form method="post" action="<@ofbizUrl>createSurveyQuestionAppl</@ofbizUrl>">
              <input type="hidden" name="surveyId" value="${requestParameters.surveyId}" />
              <input type="hidden" name="surveyQuestionId" value="${question.surveyQuestionId}" />
              <input type="hidden" name="surveyQuestionCategoryId" value="${requestParameters.surveyQuestionCategoryId}" />
              <tr<#if alt_row> class="alternate-row"</#if>>
                <td><a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}&amp;surveyQuestionId=${question.surveyQuestionId}&amp;surveyQuestionCategoryId=${requestParameters.surveyQuestionCategoryId}#edit</@ofbizUrl>" class="buttontext">${question.surveyQuestionId}</a></td>
                <td>${question.description?if_exists}</td>
                <td>${questionType.get("description",locale)}</td>
                <td>${question.question?if_exists}</td>
              <td>
                <select name="surveyPageId">
                  <option value=""></option>
                  <#list surveyPageList as surveyPage>
                    <option value="${surveyPage.surveyPageSeqId}">${surveyPage.pageName} [${surveyPage.surveyPageSeqId}]</option>
                  </#list>
                </select>
              </td>
              <td>
                <select name="surveyMultiRespId">
                  <option value=""></option>
                  <#list surveyMultiRespList as surveyMultiResp>
                    <option value="${surveyMultiResp.surveyMultiRespId}">${surveyMultiResp.multiRespTitle} [${surveyMultiResp.surveyMultiRespId}]</option>
                  </#list>
                </select>
              </td>
                <td><input type="text" name="surveyMultiRespColId" size="4"/></td>
                <td>
                  <select name="requiredField">
                    <option>N</option>
                    <option>Y</option>
                  </select>
                </td>
                <td><input type="text" name="sequenceNum" size="5"/></td>
                <td><input type="text" name="withSurveyQuestionId" size="5"/></td>
                <td><input type="text" name="withSurveyOptionSeqId" size="5"/></td>
                <td><input type="submit" value="${uiLabelMap.CommonApply}" class="smallSubmit"/></td>
              </tr>
            </form>
            <#assign alt_row = !alt_row>
          </#list>
        </table>
      </div>
    </div>
</#if>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.ContentSurveyApplyQuestionFromCategory}</li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
      <form method="post" action="<@ofbizUrl>EditSurveyQuestions</@ofbizUrl>">
        <input type="hidden" name="surveyId" value="${requestParameters.surveyId}"/>
        <select name="surveyQuestionCategoryId">
          <#list questionCategories as category>
            <option value="${category.surveyQuestionCategoryId}">${category.description?default("??")} [${category.surveyQuestionCategoryId}]</option>
          </#list>
        </select>
        &nbsp;
        <input type="submit" value="${uiLabelMap.CommonApply}" class="smallSubmit"/>
      </form>
  </div>
</div>
<div class="screenlet">
  <#-- new question / category -->
  <#if requestParameters.newCategory?default("N") == "Y">
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.ContentSurveyCreateQuestionCategory}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
      <a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonNew} ${uiLabelMap.ContentSurveyQuestion}</a>
      <br /><br />
      ${createSurveyQuestionCategoryWrapper.renderFormString(context)}
  <#else>
    <#if surveyQuestionId?has_content>
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.CommonEdit} ${uiLabelMap.ContentSurveyQuestion}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
      <a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonNew} ${uiLabelMap.ContentSurveyQuestion}</a>
    <#else>
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.ContentSurveyCreateQuestion}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
    </#if>
    <a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}&amp;newCategory=Y</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonNew} ${uiLabelMap.ContentSurveyQuestion} ${uiLabelMap.ContentSurveryCategory}</a>
    <br /><br />
    ${createSurveyQuestionWrapper.renderFormString(context)}
  </#if>
  </div>
</div>
<#if (surveyQuestion?has_content && surveyQuestion.surveyQuestionTypeId?default("") == "OPTION")>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.ContentSurveyOptions} - ${uiLabelMap.CommonId} ${surveyQuestion.surveyQuestionId?if_exists}</li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <table class="basic-table hover-bar" cellspacing="0">
      <tr class="header-row">
        <td>${uiLabelMap.CommonDescription}</td>
        <td>${uiLabelMap.CommonSequenceNum}</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
      <#assign alt_row = false>
      <#list questionOptions as option>
        <tr<#if alt_row> class="alternate-row"</#if>>
          <td>${option.description?if_exists}</td>
          <td>${option.sequenceNum?if_exists}</td>
          <td><a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}&amp;surveyQuestionId=${option.surveyQuestionId}&amp;surveyOptionSeqId=${option.surveyOptionSeqId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonEdit}</a></td>
          <td>
            <form id="deleteSurveyQuestionOption_${option_index}" action="<@ofbizUrl>deleteSurveyQuestionOption</@ofbizUrl>" method="post">
              <input type="hidden" name="surveyId" value="${requestParameters.surveyId}" />
              <input type="hidden" name="surveyQuestionId" value="${option.surveyQuestionId}" />
              <input type="hidden" name="surveyOptionSeqId" value="${option.surveyOptionSeqId}" />
              <a href="javascript:document.getElementById('deleteSurveyQuestionOption_${option_index}').submit();"" class="buttontext">${uiLabelMap.CommonRemove}</a>
            </form>
          </td>
        </tr>
        <#assign alt_row = !alt_row>
      </#list>
    </table>
  </div>
</div>
<div class="screenlet">
    <#if !surveyQuestionOption?has_content>
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.ContentSurveyCreateQuestionOption}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
    <#else>
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.ContentSurveyEditQuestionOption}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">
      <a href="<@ofbizUrl>EditSurveyQuestions?surveyId=${requestParameters.surveyId}&amp;surveyQuestionId=${surveyQuestionOption.surveyQuestionId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNew} ${uiLabelMap.ContentSurveyOption}]</a>
    </#if>
    ${createSurveyOptionWrapper.renderFormString()}
    </div>
</div>
</#if>
