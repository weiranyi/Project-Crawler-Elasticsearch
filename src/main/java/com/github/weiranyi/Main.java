package com.github.weiranyi;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // 【待处理】存放待处理的链接的池子
        List<String> linkPool = new ArrayList<>();
        // 【已处理】存放已经处理的链接
        Set<String> processedLinks = new HashSet<>();
        // 添加一个链接到池中
        linkPool.add("https://sina.cn");
        while (true) {
            // 链接池是空的就退出循环
            if (linkPool.isEmpty()) {
                break;
            }
            // 获取并移除最后一个链接，对于ArrayList来说更有效率
            String link = linkPool.remove(linkPool.size() - 1);

            // 若链接已经处理过了就跳到下一次循环
            if (processedLinks.contains(link)) {
                continue;
            }
            // 判断是否是感兴趣滴内容【新浪站内的网页】
            // link.contains("sina.cn") && !link.contains("passport.sina.cn") &&
            if ((link.contains("news.sina.cn")) || "https://sina.cn".equals(link)) {
                try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                    if (link.startsWith("//")) {
                        link = "https:" + link;
                    }
                    HttpGet httpGet = new HttpGet(link);
                    httpGet.addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
                    try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                        System.out.println(response1.getStatusLine());
                        System.out.println(link);
                        HttpEntity entity1 = response1.getEntity();
                        String html = EntityUtils.toString(entity1);

                        Document doc = Jsoup.parse(html);
                        // 使用CSS选择器,html中去获取
                        ArrayList<Element> links = doc.select("a");
                        for (Element aTag : links) {
                            // 获取href属性
                            linkPool.add(aTag.attr("href"));
                        }
                        // 假设这是一个新闻的详情页，就存入数据库，否则，就什么都不做
                        ArrayList<Element> articleTags = doc.select("article");
                        if (!articleTags.isEmpty()) {
                            for (Element articleTag : articleTags) {
                                String titile = articleTags.get(0).child(0).text();
                                System.out.println(titile);
                            }
                        }
                        processedLinks.add(link);
                    }
                }
            } else {
                // 不感兴趣
                continue;
            }
        }
//        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
//            HttpGet httpGet = new HttpGet("https://sina.cn/");
//            try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
//                System.out.println(response1.getStatusLine());
//                HttpEntity entity1 = response1.getEntity();
//                System.out.println(EntityUtils.toString(entity1));
//            }
//        }
    }
}
