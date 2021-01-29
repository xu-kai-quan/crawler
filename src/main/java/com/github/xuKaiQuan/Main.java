package com.github.xuKaiQuan;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Main {

    private static List<String> loadUrlsFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        }
        return results;
    }

    public static void main(String[] args) throws IOException, SQLException {

        Connection connection = DriverManager.getConnection("jdbc:h2:file:D:\\JAVAproject\\crawlerAndES\\crawler\\news","root","root");
        List<String> linkPool = loadUrlsFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");

        Set<String> processedLinks = new HashSet<>(loadUrlsFromDatabase(connection, "select link from LINKS_ALREADY_PROCESSED"));


        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(linkPool.size() - 1);
            if (processedLinks.contains(link)) {
                continue;
            }
            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);

                //stream():对于数据流的操作, map():把一个数据变换成另一个数据,forEach():对于每一个都执行一个操作
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);

                //假如这是一个新闻的详情页面，就存入数据库，否则，就什么也不做。
                storeIntoDatabaseIfItIsNewPage(doc);

                processedLinks.add(link);
            } else {
                // 这是我们不感兴趣的，不处理它
            }
        }
    }


    private static void storeIntoDatabaseIfItIsNewPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        if (link.contains("//") && !link.contains("https:")) {
            link = "https:" + link;
        }
        System.out.println(link);

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }

    }

    private static boolean isInterestingLink(String link) {
        // 这是我们感兴趣的，我们只处理新浪站内的连接
        return (isIndexPage(link) || isNewPage(link)) && isNotLoginPage(link) && isBlogsHot(link);
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isBlogsHot(String link) {
        return !link.contains("hotnews.sina.cn");
    }
}
