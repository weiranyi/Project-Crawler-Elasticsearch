package com.github.weiranyi.entity;

import java.time.Instant;

/**
 * @author: https://github.com/weiranyi
 * @description 这是一个新闻类
 * @date: 2021/5/22 9:00 下午
 * @Version 1.0
 * ''；
 */
public class News {
    private Integer id;
    private String url;
    private String content;
    private String title;
    // Instant：时刻、时间；可以去替代时间点
    private Instant createdAt;
    private Instant modifiedAt;

    public News() {

    }

    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
    }

    public News(News old) {
        this.id = old.id;
        this.url = old.url;
        this.content = old.content;
        this.title = old.title;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
