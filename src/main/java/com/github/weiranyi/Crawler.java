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
import java.util.stream.Collectors;


public class Crawler extends Thread {
    private CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        // 这样每个线程共享同一个链接
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String link;
            // 从数据库中加载下一个链接，若能加载到则进行下一个循环
            while ((link = dao.getNextLinkThenDelete()) != null) {
                // 若链接已经处理过了就跳到下一次循环
                if (dao.isLinkProcessed(link)) {
                    continue;
                }
                // 判断是否是感兴趣滴内容【新浪站内的网页】
                if (isInterestingLink(link)) {
                    Document doc = httpGetAndParseHtml(link);
                    // 分析页面url将它们放到即将处理的url池子中去
                    parseUrlsFromAndStoreIntoDatabase(doc);
                    storeIntoDatabaseIfItIsNewsPage(doc, link);
                    dao.insertProcessedLinked(link);
                    // dao.updataDatabase(link, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
                } else {
                    // 不感兴趣
                    continue;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseUrlsFromAndStoreIntoDatabase(Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (href.toLowerCase().startsWith("javascript")) {
                continue;
            }
            dao.insertLinkToBeProcessed(href);
            // dao.updataDatabase(href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
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

    private void storeIntoDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                // Collectors.joining("\n")得到的字符串用换行符分隔
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
                dao.insertNewsIntoDataBase(link, title, content);
            }
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isIndexPage(link) && isNotLoginPage(link));
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
