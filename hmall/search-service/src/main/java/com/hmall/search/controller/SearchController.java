package com.hmall.search.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.alibaba.fastjson.JSONObject;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.service.ItemService;
import com.hmall.search.util.EsUtileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ItemService itemService;
    private final RestHighLevelClient restHighLevelClient;
    EsUtileService esUtileService = new EsUtileService();
    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<JSONObject> search(ItemPageQuery query) {
        // 分页查询
        Page<Item> result = itemService.lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .eq(Item::getStatus, 1)
                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
                .page(query.toMpPage("update_time", false));

        Map<String, Object> likeMap = new HashMap<>();
        if(StrUtil.isNotBlank(query.getKey())){
            likeMap.put("name",query.getKey());
        }

        Map<String, Object> andMap = new HashMap<>();
        if(StrUtil.isNotBlank(query.getBrand())){
            andMap.put("brand",query.getBrand());
        }
        if(StrUtil.isNotBlank(query.getCategory())){
            andMap.put("category",query.getCategory());
        }

        andMap.put("status",1);

        Map<String, Object> rangeMap = new HashMap<>();
        if(StrUtil.isNotBlank(query.getMaxPrice()+"")){
            andMap.put( EsUtileService.KeyAddSPLIT("price","<="),query.getMaxPrice());
        }
        if(StrUtil.isNotBlank(query.getMinPrice()+"")){
            andMap.put( EsUtileService.KeyAddSPLIT("price",">="),query.getMinPrice());
        }
        PageDTO<JSONObject> pageDTO = esUtileService.conditionSearch("items", query.getPageNo(), query.getPageSize(),
                StrUtil.isNotBlank(query.getSortBy()) ? query.getSortBy() : "update_time", query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC, null, andMap, null, likeMap, null, rangeMap, null);


        // 封装并返回
        return pageDTO;
    }

    /**
     * search 自动补全

     * @return vo
     */
    @RequestMapping(value = "/suggestion", method = RequestMethod.GET)
    public PageDTO<String>  suggestCart(@RequestBody String param) throws IOException {
        Set<String> itemNameList = new HashSet<>();
        JSONObject parseObject = JSONObject.parseObject(param);
        String pre = parseObject.getString("name");
       return esUtileService.buildPageSuggestion("items",pre,"suggestion",10,true);
    }


}
