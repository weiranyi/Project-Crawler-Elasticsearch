package com.github.weiranyi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USER_NAME = "root";
    private static final String USER_PASSWORD = "123456";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/yiweiran/Documents/workPlace/java/JavaProject-Crawler-Elasticsearch/news", USER_NAME, USER_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("select link from LINKS_TO_BE_PROCESSED limit 1;");
        if (link != null) {
            updataDatabase(link, "delete FROM LINKS_TO_BE_PROCESSED where LINK=?");
        }
        return link;
    }

    public String getNextLink(String sql) throws SQLException {
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

    public void updataDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    public void insertNewsIntoDataBase(String url, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into news (url, title, content, created_at,MODIFIED_AT)values(?,?,?,now(),now())")) {
            statement.setString(1, url);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
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
}
