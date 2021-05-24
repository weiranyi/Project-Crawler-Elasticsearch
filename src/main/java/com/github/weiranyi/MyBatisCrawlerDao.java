package com.github.weiranyi;

import com.github.weiranyi.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    // 获取下一个链接再删除
    @Override
    public String getNextLinkThenDelete() throws SQLException {
        //  SqlSession openSession(boolean autoCommit);这里设计事务，必须提交才生效，要设置参数为true
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.github.weiranyi.MyMapper.selectNextAvailableLink");
            if (url != null) {
                session.delete("com.github.weiranyi.MyMapper.deleteLink", url);
            }
            return url;
        }
    }

    // 插入新闻到数据库
    @Override
    public void insertNewsIntoDataBase(String url, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.weiranyi.MyMapper.insertNews", new News(url, title, content));
        }
    }

    //
    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            int count = (Integer) session.selectOne("com.github.weiranyi.MyMapper.countLink", link);
            return count != 0;
        }
    }

    // 高级MyBatis操作
    @Override
    public void insertProcessedLinked(String link) {
        // 创建一个map集合对象
        Map<String, Object> param = new HashMap<>();
        // 设置表名
        param.put("tableName", "links_already_processed");
        // 设置链接
        param.put("link", link);
        //  SqlSession openSession(boolean autoCommit);这里设计事务，必须提交才生效，要设置参数为true
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.weiranyi.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String href) {
        Map<String, Object> param = new HashMap<>();
        // 设置表名
        param.put("tableName", "links_to_be_processed");
        // 设置链接
        param.put("link", href);
        //  SqlSession openSession(boolean autoCommit);这里设计事务，必须提交才生效，要设置参数为true
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.weiranyi.MyMapper.insertLink", param);
        }
    }
}
