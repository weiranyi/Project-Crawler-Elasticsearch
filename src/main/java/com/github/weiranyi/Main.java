package com.github.weiranyi;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        CrawlerDao dao = new MyBatisCrawlerDao();
        for (int i = 0; i < 5; i++) {
            new Crawler(dao).start();
        }
    }
}
