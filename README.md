# 项目：多线程爬虫与Elasticsearch搜索引擎实战
## 需求分析与算法设计：
- 需求：网页中的一个节点开始遍历所有节点
- 算法：使用了广度优先算法的变体

![img.png](https://github.com/weiranyi/JavaProject-Crawler-Elasticsearch/blob/yiweiran/images/flowChart.png?raw=true)
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

# 6、心得与收获：

## 1、做一个项目的原则

- 心法：
  - 1、把每个项目当作人生最好的项目来精雕细琢，一丝不苟滴写好文档，保证代码质量（以自己当前最高水平去完成，可以借助代码检测工具）
  - 2、使用业界标准化的模式和流程，每一行代码都不要是多余的（如：不要提交不该提交的文件 .idea 等不要上传到Github）；几乎不要有本地依赖，使用者能够毫无障碍的使用
  - 3、小步快跑，成就感，越小的变更越容易debug，越早进行越好
- 强制规范：
  - 1、【重要】使用GitHub+主干/分支模型进行开发
    - 禁止直接push master
    - 所有变更必须PR进行
  - 2、【重要】自动代码质量检查+测试
    - Checkstyle/SpotBugs
    - 最基本的自动化测试覆盖
- 项目设计流程
  - 多人协作【自顶向下】
    - 模块化
      - 各模块之间责任明确，界限清晰
      - 基本文档
      - 基本借口
    - 小步提交
      - 大的变更难以review
      - 的的变更更加棘手
      - 小步提交颗粒度
  - 单打独斗【自底向上】
    - 先实现功能
    - 在实现的过程中不断抽出公用的部分
      - 每当自己写的代码比较啰嗦（不断复制粘贴）的时候就得重构了
    - 通过重构实现模块化、接口化
- 项目演进：
  - 单线程 -> 多线程
  - console -> H2 -> MySQL
  - database -> Elasticsearch

- 好的代码习惯：
  - 不要写妥协的代码，将烂代码、啰嗦的代码重构掉，初学可能会学习大量烂代码
  - 有好的三方实现可以借用，如：Apache提供的包
  - 代码要有一个较好的扩展性

## 2、收获

- 冒烟测试；测试原则：每个测试是一个类，负责一个小的功能模块
- git命令回顾：
  - 新建分支的命令：
      ```shell
      git checkout -b basic-algorithm
      ```
  - 撤销 git add 操作，可以使用以下命令：
      ```shell
      git restore --staged src/main/java/com/github/weiranyi/Main.java
      ```
  - 若此时全部commit提交，想要撤销一个提交怎么办
      ```shell
      git reset HEAD~1
      ```
  - 撤销PR的提交
      ```shell
      git log --获得61b22195162ec24fbbf2ef020485bb0a524c82b9
      git revert 61b22195162ec24fbbf2ef020485bb0a524c82b9
      ```
  - 若在自己分支出现与master冲突时，可以通过force push
      ```shell
      git push -f
      ```
- commit提交文本，首行做标题行，第二行开始写内容，每行最好不要超过72个字符
- 使用了circleci检查，比自己发现代码问题还要细致
- 算法
  - DFS 深度优先算法
  - BFS 广度优先
- 重构
  - 短方法：
    - a.便于人脑理解
    - b.越短越容易复用
    - c.对于Java来说可以方便的对方法进行覆盖
- spotbugs
  - spotbugs goal：分析项目
  - check goal：分析项目，发现BUG就让build失败
    ```shell
        mvn spotbugs:check
        mvn spotbugs:gui
    ```
- maven默认生命周期：
  - maven在各生命周期什么都不做
  - 做什么需要依靠插件
    - maven-surefire-plugin官方检测插件
  - 插件可以绑定到maven的各个生命周期上
  - maven-compile-plugein
- ORM对象关系映射
- 索引：
  - MySQL：B+树，数据库全文索引非常慢；Mysql长处是非文本数据的索引
  - Elasticsearch：倒排索引
