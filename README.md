# 项目：多线程爬虫与Elasticsearch搜索引擎实战
## 1、迭代内容：
- 版本1： 
  - 用Java编写一个多线程爬虫，完成HTTP请求、HTML解析等工作，得到数据后放入H2数据库中，借助Flyway将建表、添加原始数据的工作等（自动化）
  - 使用Maven进行包管理，使用CircleCI进行自动化测试，在生命周期绑定 Checkstyle、SpotBugs 插件保证代码质量
- 版本2：使用ORM（对象关系映射）重构，使用MyBatis框架
- 版本3：通过flyway插件迁移数据，将数据从H2 数据库迁移到MySQL数据库
- 版本4：将主函数从爬虫类中抽取出，形成新的类，方便调用爬虫线程
- 版本5：借助Elasticsearch编写一个简单的搜索程序
## 2、建立：
- 建立GitHub仓库并克隆到本地：
```shell
# 后期建议使用SSH
git clone https://github.com/weiranyi/JavaProject-Crawler-Elasticsearch.git
```
- 使用自动化工具Flyway完成自动建表工作：
```shell
mvn flyway:migrate
# 备用命令：上一次建表失败的情况下，想再次建表，可以使用本命令
mvn flyway:clean && mvn flyway:migrate
```
# 3、测试：
- 项目测试：
```shell
mvn verify
```
- 补充测试：
```shell
mvn spotbugs:check
mvn spotbugs:gui
#压制不必要的报错：
@SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
```
# 4、扩展：
[项目中MySQL和是Docker安装滴，Docker的使用可以点击这，参考该笔记](https://zhuanlan.zhihu.com/p/356987233)

# 5、展示：
- 搜索展示：
![搜索展示](https://github.com/weiranyi/Project-Crawler-Elasticsearch/blob/main/images/search_code.png?raw=true)

- 爬取的数据：
![数据展示](https://github.com/weiranyi/Project-Crawler-Elasticsearch/blob/main/images/news_database.png?raw=true)

- Docker展示：
![Docker](https://github.com/weiranyi/Project-Crawler-Elasticsearch/blob/main/images/Docker.png?raw=true)
  
- Elasticsearch
![Elasticsearch](https://github.com/weiranyi/Project-Crawler-Elasticsearch/blob/main/images/Elasticsearch.png?raw=true)