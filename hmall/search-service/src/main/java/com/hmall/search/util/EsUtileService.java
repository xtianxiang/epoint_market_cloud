package com.hmall.search.util;


import cn.hutool.db.PageResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.hmall.common.domain.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class EsUtileService {
    @Autowired
   private RestHighLevelClient restHighLevelClient;

    public EsUtileService(RestHighLevelClient client) {
        this.restHighLevelClient =client;
    }

    public EsUtileService(){

    }
    /**
     * @description:range查询分割符号
     */
   public static final String  RANGESPLIT = "RANGESPLIT";
    /**
     * @description:range查询key拼接分割符号
     */
    public static String KeyAddSPLIT(String prekey,String postkey){
        return prekey+RANGESPLIT+postkey;
    }
    /**
     * 创建索引
     *
     */
    public boolean createIndex(String indexName) {
        try {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            log.info("创建索引 response 值为： {}", response.toString());
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断索引是否存在
     *
     */
    public boolean existIndex(String indexName) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            System.out.println(restHighLevelClient);
            return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除索引
     *
     */
    public boolean deleteIndex(String indexName) {
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            log.info("删除索引{}，返回结果为{}", indexName, delete.isAcknowledged());
            return delete.isAcknowledged();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据id删除文档
     *
     */
    public boolean deleteDocById(String indexName, String id) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("删除索引{}中id为{}的文档，返回结果为{}", indexName, id, deleteResponse.status().toString());
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量插入数据
     *
     */
    public boolean multiAddDoc(String indexName, List<JSONObject> list) {
        try {
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(doc -> {
                String source = JSON.toJSONString(doc);
                IndexRequest indexRequest = new IndexRequest(indexName);
                indexRequest.source(source, XContentType.JSON);
                bulkRequest.add(indexRequest);
            });
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("向索引{}中批量插入数据的结果为{}", indexName, !bulkResponse.hasFailures());
            return !bulkResponse.hasFailures();
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新文档
     *
     */
    public boolean updateDoc(String indexName, String docId, JSONObject jsonObject) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(indexName, docId).doc(JSON.toJSONString(jsonObject), XContentType.JSON);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            int total = updateResponse.getShardInfo().getTotal();
            log.info("更新文档的影响数量为{}",total);
            return total > 0;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据id查询文档
     */
    public JSONObject queryDocById(String indexName, String docId) {
        JSONObject jsonObject = new JSONObject();
        try {
            GetRequest getRequest = new GetRequest(indexName, docId);
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            jsonObject = (JSONObject) JSONObject.toJSON(getResponse.getSource());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

/**
 * @description:通用条件查询，map类型的参数都为空时，默认查询全部
 * @author: xtxiang
 * @date:  18:43
 * @param: indexName 索引名称
 * @param: pageNum 当前页码数
 * @param: pageSize 分页大小
 * @param: sortField 排序字段
 * @param: sortOrder 排序规则
 * @param: highName 高亮字段
 * @param: andMap and条件集合
 * @param: orMap or条件集合
 * @param: dimAndMap 模糊and查询
 * @param: dimOrMap 模糊or查询
 * @param: rangeAndMap 模糊and查询
 * @param: rangeOrMap 模糊or查询
 * @return: PageDTO<JSONObject> 分页查询结果dto
 */
    public PageDTO<JSONObject> conditionSearch(String indexName, Integer pageNum, Integer pageSize, String sortField, SortOrder sortOrder, String highName, Map<String, Object> andMap,
                                               Map<String, Object> orMap, Map<String, Object> dimAndMap, Map<String, Object> dimOrMap, Map<String, Object> rangeAndMap, Map<String, Object> rangeOrMap) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        // 索引不存在时不报错
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        //构造搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = buildMultiQuery(andMap, orMap, dimAndMap, dimOrMap,rangeAndMap,rangeOrMap);
        sourceBuilder.query(boolQueryBuilder);
        //排序处理
        if(!StringUtils.isEmpty(sortField)&&!StringUtils.isEmpty(sortOrder)){
            sourceBuilder.sort(sortField, SortOrder.DESC);
        }

        //高亮处理
        if (!StringUtils.isEmpty(highName)) {
            buildHighlight(sourceBuilder, highName);
        }
        //分页处理
        buildPageLimit(sourceBuilder, pageNum, pageSize);
        //超时设置
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = null;

        try {
            //执行搜索
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchHits searchHits = searchResponse.getHits();
        List<JSONObject> resultList = new ArrayList<>();
        for (SearchHit hit : searchHits) {
            //原始查询结果数据
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //高亮处理
            if (!StringUtils.isEmpty(highName)) {
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlightField = highlightFields.get(highName);
                if (highlightField != null) {
                    Text[] fragments = highlightField.fragments();
                    StringBuilder value = new StringBuilder();
                    for (Text text : fragments) {
                        value.append(text);
                    }
                    sourceAsMap.put(highName, value.toString());
                }
            }
            JSONObject jsonObject =  JSONObject.parseObject(JSONObject.toJSONString(sourceAsMap));
            resultList.add(jsonObject);
        }

        long total = searchHits.getTotalHits().value;
        PageDTO<JSONObject> pageDTO = new PageDTO<>();
        pageDTO.setTotal(total);
        Long pages = pageSize==0?0L:(total%pageSize==0?total/pageSize:total/pageSize+1);
        pageDTO.setPages(pages);
        pageDTO.setCurrent(pageNum.longValue());
        pageDTO.setSize(pageSize.longValue());
        pageDTO.setList(resultList);
        return pageDTO;
    }

    /**
     * 构造多条件查询
     *
     */
    public BoolQueryBuilder buildMultiQuery(Map<String, Object> andMap, Map<String, Object> orMap, Map<String, Object> dimAndMap, Map<String, Object> dimOrMap, Map<String, Object> rangeAndMap, Map<String, Object> rangeOrMap) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //该值为true时搜索全部
        boolean searchAllFlag = true;
        //精确查询，and
        if (!CollectionUtils.isEmpty(andMap)) {
            for (Map.Entry<String, Object> entry : andMap.entrySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.must(matchQueryBuilder);
            }
            searchAllFlag = false;
        }
        //精确查询，or
        if (!CollectionUtils.isEmpty(orMap)) {
            for (Map.Entry<String, Object> entry : orMap.entrySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(entry.getKey(), entry.getValue());
                boolQueryBuilder.should(matchQueryBuilder);
            }
            searchAllFlag = false;
        }
        //模糊查询，and
        if (!CollectionUtils.isEmpty(dimAndMap)) {
            for (Map.Entry<String, Object> entry : dimAndMap.entrySet()) {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(entry.getKey(), "*" + entry.getValue() + "*");
                boolQueryBuilder.must(wildcardQueryBuilder);
            }
            searchAllFlag = false;
        }
        //模糊查询，or
        if (!CollectionUtils.isEmpty(dimOrMap)) {
            for (Map.Entry<String, Object> entry : dimOrMap.entrySet()) {
                WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(entry.getKey(), "*" + entry.getValue() + "*");
                boolQueryBuilder.should(wildcardQueryBuilder);
            }
            searchAllFlag = false;
        }
        //范围查询，rangeand
        if (!CollectionUtils.isEmpty(rangeAndMap)) {
            for (Map.Entry<String, Object> entry : rangeAndMap.entrySet()) {
                String[] rangesplits = entry.getKey().split(RANGESPLIT);
                if(rangesplits.length>=2) {
                    boolean isCheckOk = true;
                    RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(entry.getKey());
                    switch (rangesplits[1]){
                        case "<":
                            queryBuilder.lt(entry.getValue());
                            break;
                        case ">":
                            queryBuilder.gt(entry.getValue());
                            break;
                        case "<=":
                            queryBuilder.lte(entry.getValue());
                            break;
                        case ">=":
                            queryBuilder.gte(entry.getValue());
                            break;
                        default:
                            log.error("range范围查询不正确，运算符号不再范围内");
                            isCheckOk = false;
                            break;
                    }
                    if (isCheckOk) {
                        boolQueryBuilder.must(queryBuilder);
                    }
                }else {
                    log.error("range范围查询不正确，查询key需以RANGESPLIT分割");
                }
            }
        }
        //范围查询，rangeor
        if (!CollectionUtils.isEmpty(rangeOrMap)) {
            for (Map.Entry<String, Object> entry : rangeOrMap.entrySet()) {
                String[] rangesplits = entry.getKey().split(RANGESPLIT);
                if(rangesplits.length>=2) {
                    boolean isCheckOk = true;
                    RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(entry.getKey());
                    switch (rangesplits[1]){
                        case "<":
                            queryBuilder.lt(entry.getValue());
                            break;
                        case ">":
                            queryBuilder.gt(entry.getValue());
                            break;
                        case "<=":
                            queryBuilder.lte(entry.getValue());
                            break;
                        case ">=":
                            queryBuilder.gte(entry.getValue());
                            break;
                        default:
                            log.error("range范围查询不正确，运算符号不再范围内");
                            isCheckOk = false;
                            break;
                    }
                    if (isCheckOk) {
                        boolQueryBuilder.should(queryBuilder);
                    }
                }else {
                    log.error("range范围查询不正确，查询key需以RANGESPLIT分割");
                }
            }
        }
        if (searchAllFlag) {
            MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
            boolQueryBuilder.must(matchAllQueryBuilder);
        }


        return boolQueryBuilder;
    }

    /**
     * 构建高亮字段
     *
     */
    public void buildHighlight(SearchSourceBuilder sourceBuilder, String highName) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮字段
        highlightBuilder.field(highName);
        //多个高亮显示
        highlightBuilder.requireFieldMatch(false);
        //高亮标签前缀
        highlightBuilder.preTags("<span style='color:red'>");
        //高亮标签后缀
        highlightBuilder.postTags("</span>");

        sourceBuilder.highlighter(highlightBuilder);
    }

    /**
     * 构造分页
     */
    public void buildPageLimit(SearchSourceBuilder sourceBuilder, Integer pageNum, Integer pageSize) {
        System.out.println(sourceBuilder);
        if (sourceBuilder!=null && !StringUtils.isEmpty(pageNum) && !StringUtils.isEmpty(pageSize)) {
            sourceBuilder.from(pageSize * (pageNum-1) );
            sourceBuilder.size(pageSize);
        }
    }

    /**
     * @description:通用条件查询，map类型的参数都为空时，默认查询全部
     * @author: xtxiang
     * @date:  18:43
     * @param: indexName 索引名称
     * @param: pre 补全前缀
     * @param: suggestion 查询补全字段
     * @param: size 总量
     * @param: skipDuplicates 是否去重
     * suggest补全查询
     */
    public PageDTO<String> buildPageSuggestion(String indexName, String pre,String suggestion, Integer size,Boolean skipDuplicates){
        List<String> resultList = new ArrayList<>();
        // 1.准备请求
        SearchRequest request = new SearchRequest(indexName);
// 2.请求参数
        request.source()
                .suggest(new SuggestBuilder().addSuggestion(
                        "mySuggestion",
                        SuggestBuilders
                                .completionSuggestion(suggestion)
                                .prefix(pre)
                                .skipDuplicates(skipDuplicates)
                                .size(size)
                ));
        SearchResponse response = null;

        try {
// 3.发送请求
            response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

// 4.处理结果
        Suggest suggest = response.getSuggest();
// 4.1.根据名称获取补全结果
        CompletionSuggestion suggestionResult = suggest.getSuggestion("mySuggestion");
// 4.2.获取options并遍历
        for (CompletionSuggestion.Entry.Option option : suggestionResult.getOptions()) {
            // 4.3.获取一个option中的text，也就是补全的词条
            String text = option.getText().string();
            resultList.add(text);
        }
        PageDTO<String> page = new PageDTO<>();
        page.setTotal((long)(resultList.size()));
        page.setList(resultList);
        page.setPages(1L);
        page.setCurrent(1L);
        // 封装并返回
        return page;
    }
    public PageDTO<String> buildPageSuggestion(String indexName, String pre,String suggestion, Integer size){
       return this.buildPageSuggestion(indexName,pre,suggestion,size,true);
    }

    public PageDTO<String> buildPageSuggestion(String indexName, String pre,String suggestion){
        return this.buildPageSuggestion(indexName,pre,suggestion,10,true);
    }
    public PageDTO<String> buildPageSuggestion(String indexName, String pre,String suggestion,Boolean skipDuplicates){
        return this.buildPageSuggestion(indexName,pre,suggestion,10,skipDuplicates);
    }
}


