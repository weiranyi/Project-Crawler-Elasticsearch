package com.github.weiranyi;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author: https://github.com/weiranyi
 * @description 搜索引擎，该类用于实现搜索功能
 * @date: 2021/5/26 2:56 下午
 * @Version 1.0
 */

public class ElasticsearchEngine {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("请输入一个搜索关键字");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
            String keyword = reader.readLine();
            System.out.println(keyword);
            search(keyword);
        }
    }

    private static void search(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest request = new SearchRequest("news");
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyword, "title", "content")));

            SearchResponse result = client.search(request, RequestOptions.DEFAULT);

            for (SearchHit hit : result.getHits()) {
                System.out.println(hit.getSourceAsString());
            }
        }
    }
}
