package com.github.weiranyi;

import com.github.weiranyi.entity.News;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: https://github.com/weiranyi
 * @description 生成一亿条数据
 * @date: 2021/3/31 4:09 下午
 * @Version 1.0
 */
public class ElasticsearchDataGenerator {
    private static void writeSingleThread(List<News> newsFromMySQL) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            // 单线程写入2000*1000 = 200_0000数据
            for (int i = 0; i < 1000; i++) {
                // 批处理操作
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromMySQL) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> data = new HashMap<>();
                    data.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    data.put("url", news.getUrl());
                    data.put("title", news.getTitle());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifiedAt());
                    request.source(data, XContentType.JSON);
                    bulkRequest.add(request);
                    IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                    System.out.println(response.status().getStatus());
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(Thread.currentThread().getName() + "finishes" + i + "：" + bulkResponse.status().getStatus());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        List<News> newsFromMySQL = getNewsFromMySQL(sqlSessionFactory);

        for (int i = 0; i < 16; i++) {
            new Thread(() -> writeSingleThread(newsFromMySQL)).start();
        }
    }

    private static List<News> getNewsFromMySQL(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.weiranyi.MockMapper.selectNews");
        }
    }
}
