package com.github.weiranyi;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLink(String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void updataDatabase(String link, String sql) throws SQLException;

    void insertNewsIntoDataBase(String url, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

}
