package com.github.weiranyi;

import com.github.weiranyi.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.security.SecureRandom;

/**
 * @author: https://github.com/weiranyi
 * @description 为后续的搜索操作创建数据，将已经爬取的数据复制粘贴
 * @date: 2021/5/25 7:23 下午
 * @Version 1.0
 */
public class MockDataGenerator {
    private static final int TAGET_ROW_COUNT = 100_0000;
    private static final SecureRandom random = new SecureRandom();

    private static void mockData(SqlSessionFactory sqlSessionFactory, int howMany) {
        //ExecutorType.BATCH
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            List<News> currentNews = session.selectList("com.github.weiranyi.MockMapper.selectNews");
            int count = howMany - currentNews.size();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInsert = new News(currentNews.get(index));
                    Instant currentTime = newsToBeInsert.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setModifiedAt(currentTime);
                    newsToBeInsert.setCreatedAt(currentTime);
                    session.insert("com.github.weiranyi.MockMapper.insertNews", newsToBeInsert);
                    // 进度条功能
                    // 将剩余待处理的链接处理完，System.out.println("Insert:" + index);
                    System.out.println("Left" + count);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }

        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mockData(sqlSessionFactory, TAGET_ROW_COUNT);
    }
}

/**
 * # 查询表中所有数据
 * SELECT * FROM NEWS
 * # 只保留日期
 * update NEWS set created_at = date(created_at),modified_at = date(modified_at)
 * # 建立索引
 * CREATE INDEX created_at_index ON NEWS(created_at)
 * # 查看索引【默认是B树】
 * show index from NEWS
 * # 索引前36S，索引后1S；再次运行，时间会继续缩短
 * # 数据库中存在多级缓存，再次运行就不是冷启动了
 * SELECT * FROM NEWS WHERE created_at = '2019-08-29'
 * # 查看当前SQL将会如何执行
 * EXPLAIN SELECT * FROM NEWS WHERE created_at = '2019-08-29'
 */
