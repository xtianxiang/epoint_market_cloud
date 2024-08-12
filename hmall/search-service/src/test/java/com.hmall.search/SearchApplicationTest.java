package com.hmall.search;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.alibaba.fastjson.JSONObject;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.service.ItemService;
import com.hmall.search.util.EsUtileService;
import org.apache.http.client.config.RequestConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/8 16:13
 * @DESCRIPTION
 */
@SpringBootTest
 class SearchApplicationTest {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ItemService itemService;
    @Test
    void testExistsIndex() throws IOException {
        // 1.准备Request
        GetIndexRequest request = new GetIndexRequest("items");
        // 3.发送请求
        boolean isExists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(isExists ? "存在" : "不存在");
        client.close();
    }

    @Test
    void testBulkRequest() throws IOException {
        // 查询所有的酒店数据
        List<Item> list = itemService.list();
        System.out.println("----------begin----------------"+System.currentTimeMillis()+"---------------");
        System.out.println("=========size======="+list.size()+"===============");
        EsUtileService esUtileService = new EsUtileService();
        List<JSONObject> collect = list.stream().map(re -> JSONObject.parseObject(JSONObject.toJSONString(re))).collect(Collectors.toList());

        esUtileService.multiAddDoc("items",collect);
       /* // 1.准备Request
        BulkRequest request = new BulkRequest();
        // 2.准备参数
        for (Item hotel : list) {
            // 2.1.转为HotelDoc
            ItemDoc hotelDoc = new ItemDoc(hotel);
            // 2.2.转json
            String json = JSONUtil.toJsonStr(hotelDoc);
            // 2.3.添加请求
            request.add(new IndexRequest("items").id(hotel.getId().toString()).source(json, XContentType.JSON));
        }*/
        System.out.println("-------------end-------------"+System.currentTimeMillis()+"---------------");
     /*   // 3.发送请求
        client.bulk(request, RequestOptions.DEFAULT);*/
    }

    @Test
    void testQuery() throws IOException {
        // 1.根据id查询商品数据
        Item item = itemService.getById(1013332L);
        // 2.转换为文档类型
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 3.将ItemDTO转json
        String doc = JSONUtil.toJsonStr(itemDoc);
        // 1.准备Request
        BulkRequest request = new BulkRequest();
            // 2.3.添加请求
            request.add(new IndexRequest("items").id(item.getId().toString()).source(doc, XContentType.JSON));

        client.bulk(request, RequestOptions.DEFAULT);
        client.close();

    }
}
