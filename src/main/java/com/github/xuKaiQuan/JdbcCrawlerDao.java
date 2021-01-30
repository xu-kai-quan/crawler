package com.github.xuKaiQuan;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:D:\\JAVAproject\\crawlerAndES\\crawler\\news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            updateDatabase(link, "DELETE FROM LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }

    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    public void insertNewsIntoDatabase(String url, String title, String content) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into news (url ,title, content ,created_at,modified_at) values (?,?,?,now(),now())")) {
            preparedStatement.setString(1, url);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
        }
    }

    public boolean isLinkProcessed(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            preparedStatement.setString(1, link);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }


}

