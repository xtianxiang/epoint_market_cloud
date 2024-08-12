package com.hmall.common.config;


import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;


@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
public class ElasticSearchClientConfig  {

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                new HttpHost("127.0.0.1",9200,"http")
        )
        );
        return client;
    }
/*

    @Bean
    RestHighLevelClient elasticsearchClient() {



        RestClientBuilder builder = RestClient.builder(

                new HttpHost("localhost", 9200))

                .setRequestConfigCallback(

                        new RestClientBuilder.RequestConfigCallback() {

                            @Override

                            public RequestConfig.Builder customizeRequestConfig(

                                    RequestConfig.Builder requestConfigBuilder) {

                                return requestConfigBuilder
                                        .setConnectTimeout(5000)
                                        .setSocketTimeout(600000);

                            }

                        });



        return new RestHighLevelClient(builder);

    }
*/


}
