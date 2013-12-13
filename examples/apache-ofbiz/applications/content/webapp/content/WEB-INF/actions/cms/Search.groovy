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

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.Term
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.ofbiz.base.util.Debug
import org.ofbiz.base.util.UtilHttp
import org.ofbiz.content.search.SearchWorker
import org.ofbiz.product.feature.ParametricSearch
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory

paramMap = UtilHttp.getParameterMap(request);
queryLine = paramMap.queryLine.toString();
Debug.logInfo("in search, queryLine:" + queryLine, "");

siteId = paramMap.lcSiteId;
Debug.logInfo("in search, siteId:" + siteId, "");

searchFeature1 = (String) paramMap.SEARCH_FEAT;
searchFeature2 = (String) paramMap.SEARCH_FEAT2;
searchFeature3 = (String) paramMap.SEARCH_FEAT3;

featureIdByType = ParametricSearch.makeFeatureIdByTypeMap(paramMap);
Debug.logInfo("in search, featureIdByType:" + featureIdByType, "");

combQuery = new BooleanQuery();
Directory directory = FSDirectory.open(new File(SearchWorker.getIndexPath(null)));
IndexReader reader = IndexReader.open(directory, true); // only searching, so read-only=true
Searcher searcher = null;
Analyzer analyzer = null;

try {
    Debug.logInfo("in search, indexPath:" + directory.toString(), "");
    searcher = new IndexSearcher(reader);
    Debug.logInfo("in search, searcher:" + searcher, "");
    analyzer = new StandardAnalyzer(Version.LUCENE_30);
} catch (java.io.FileNotFoundException e) {
    request.setAttribute("errorMsgReq", "No index file exists.");
    Debug.logError("in search, error:" + e.getMessage(), "");
    return;
}

if (queryLine || siteId) {
    Query query = null;
    if (queryLine) {
        QueryParser parser = new QueryParser(Version.LUCENE_30, "content", analyzer);
        query = parser.parse(queryLine);
        combQuery.add(query, BooleanClause.Occur.MUST);
    }
    Debug.logInfo("in search, combQuery(0):" + combQuery, "");

    if (siteId) {
        termQuery = new TermQuery(new Term("site", siteId.toString()));
        combQuery.add(termQuery, BooleanClause.Occur.MUST);
        Debug.logInfo("in search, termQuery:" + termQuery.toString(), "");
    }
    Debug.logInfo("in search, combQuery(1):" + combQuery, "");
}

if (searchFeature1 || searchFeature2 || searchFeature3 || !featureIdByType.isEmpty()) {
    featureQuery = new BooleanQuery();
    featuresRequired = BooleanClause.Occur.MUST;
    if ("any".equals(paramMap.any_or_all)) {
        featuresRequired = BooleanClause.Occur.SHOULD;
    }

    if (searchFeature1) {
        termQuery = new TermQuery(new Term("feature", searchFeature1));
        featureQuery.add(termQuery, featuresRequired);
        Debug.logInfo("in search searchFeature1, termQuery:" + termQuery.toString(), "");
    }

    if (searchFeature2) {
        termQuery = new TermQuery(new Term("feature", searchFeature2));
        featureQuery.add(termQuery, featuresRequired);
        Debug.logInfo("in search searchFeature2, termQuery:" + termQuery.toString(), "");
    }

    if (searchFeature3) {
        termQuery = new TermQuery(new Term("feature", searchFeature3));
        featureQuery.add(termQuery, featuresRequired);
        Debug.logInfo("in search searchFeature3, termQuery:" + termQuery.toString(), "");
    }

  if (featureIdByType) {
    featureIdByType.each { key, value ->
            termQuery = new TermQuery(new Term("feature", value));
            featureQuery.add(termQuery, featuresRequired);
            Debug.logInfo("in search searchFeature3, termQuery:" + termQuery.toString(), "");
        }
    combQuery.add(featureQuery, featuresRequired);
    }
}
if (searcher) {
    Debug.logInfo("in search searchFeature3, combQuery:" + combQuery.toString(), "");
    TopScoreDocCollector collector = TopScoreDocCollector.create(100, false); //defaulting to 100 results
    searcher.search(combQuery, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    Debug.logInfo("in search, hits:" + collector.getTotalHits(), "");

    contentList = [] as ArrayList;
    hitSet = [:] as HashSet;
    for (int start = 0; start < collector.getTotalHits(); start++) {
        Document doc = searcher.doc(hits[start].doc)
        contentId = doc.get("contentId");
        content = delegator.findOne("Content", [contentId : contentId], true);
        if (!hitSet.contains(contentId)) {
            contentList.add(content);
            hitSet.add(contentId);
        }
    }
    context.queryResults = contentList;
}
