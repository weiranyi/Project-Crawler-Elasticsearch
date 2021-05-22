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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        // 创建一个数据库链接
        Connection connection = connection = DriverManager.getConnection("jdbc:h2:file:/Users/yiweiran/Documents/workPlace/java/JavaProject-Crawler-Elasticsearch/news", "root", "123456");
        // 【待处理】存放待处理的链接的池子
        List<String> linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED;");
        // 【已处理】存放已经处理的链接
        Set<String> processedLinks = new HashSet<>(loadUrlsFromDatabase(connection, "select link from LINKS_ALREADY_PROCESSED;"));

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
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                // 使用CSS选择器,html中去获取
                ArrayList<Element> links = doc.select("a");
                // 用Java8引入的特性对代码进行简化，过程式语言变成描述式语言
                links.stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                // 假设这是一个新闻的详情页，就存入数据库，否则，就什么都不做
                storeIntoDatabaseIfItIsNewsPage(doc);
                processedLinks.add(link);

            } else {
                // 不感兴趣
                continue;
            }
        }

    }

    /*
     * 3、重构对数据库操作部分的代码
     *
     */
    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // 从数据库加载即将处理的代码
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        }
        return results;
    }


    /*
     * 2、将表达不同逻辑的代码抽象为短方法
     * 优点：
     * a.便于人脑理解
     * b.越短越容易复用
     * c.对于Java来说可以方便的对方法进行覆盖
     */
    // 通过http请求拿到HTML文档
    private static Document httpGetAndParseHtml(String link) throws IOException {
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
                return Jsoup.parse(html);
            }
        }
    }

    // 若是新闻页面就存到数据库中
    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String titile = articleTags.get(0).child(0).text();
                System.out.println(titile);
            }
        }
    }

    /*
     * 1、将长的判断条件抽取为不同的方法
     */
    // 感兴趣的链接
    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isIndexPage(link) && isNotLoginPage(link));
    }

    // 首页
    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    // 新闻页
    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    // 登录页
    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

}
