package com.github.weiranyi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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


public class Main {
    private static final String USER_NAME = "root";
    private static final String USER_PASSWORD = "123456";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/yiweiran/Documents/workPlace/java/JavaProject-Crawler-Elasticsearch/news", USER_NAME, USER_PASSWORD);
        String link = null;
        // 从数据库中加载下一个链接，若能加载到则进行下一个循环
        while ((link = getNextLinkThenDelete(connection)) != null) {
            // 若链接已经处理过了就跳到下一次循环
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            // 判断是否是感兴趣滴内容【新浪站内的网页】
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);
                // 分析页面url将它们放到即将处理的url池子中去
                parseUrlsFromAndStoreIntoDatabase(connection, doc);
                storeIntoDatabaseIfItIsNewsPage(doc);
                updataDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
            } else {
                // 不感兴趣
                continue;
            }
        }

    }

    private static void parseUrlsFromAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            updataDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
        }
    }


    /*
     * 4、优化主干逻辑，进一步重构
     */
    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED limit 1;");
        if (link != null) {
            updataDatabase(connection, link, "delete FROM LINKS_TO_BE_PROCESSED where LINK=?");
        }
        return link;
    }

    /*
     * 3、重构对数据库操作部分的代码
     */
    private static String getNextLink(Connection connection, String sql) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    private static void updataDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where LINK=?;")) {
            statement.setString(1, link);
            // 从数据库加载即将处理的代码
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }
//    private static List<String> deleteFromDatabase(Connection connection, String sql) throws SQLException {
//        List<String> results = new ArrayList<>();
//        try (PreparedStatement statement = connection.prepareStatement("delete FROM LINKS_TO_BE_PROCESSED where LINK=?")) {
//            statement.setString(1,link);
//            // 从数据库加载即将处理的代码
//            statement.executeUpdate();
//        }
//        return results;
//    }


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
