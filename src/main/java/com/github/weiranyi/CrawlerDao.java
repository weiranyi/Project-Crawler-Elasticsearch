package com.github.weiranyi;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDataBase(String url, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLinked(String link);

    void insertLinkToBeProcessed(String href);
}
