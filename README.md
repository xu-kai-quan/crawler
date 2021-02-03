## Java多线程爬虫和ES数据分析实战

MySQL(Windows)
```shell script
docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -v D://JAVAproject/crawlerAndES/crawler/mysql-data:/var/lib/mysql -d mysql:5.7.33
```

Elasticsearch(Windows)
```shell script
 docker run -d -v D://JAVAproject/crawlerAndES/crawler/esdata:/usr/share/elasticsearch/data --name elasticsearch  -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.10.1
```

用flyway初始化数据库命令：
```shell script
mvn flyway:clean && mvn flyway:migrate
```

注意：

1.使用MySQL时要在数据库中运行
```
create database news
use news
```
2.用MySQL向Elasticsearch灌数据时

要同时在docker里开启MySQL和Elasticsearc
