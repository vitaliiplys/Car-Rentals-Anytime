package com.example.carsharingservice.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class CustomPostgreSqlContainer extends PostgreSQLContainer<CustomPostgreSqlContainer> {
    private static final String DB_IMAGE = "postgres:latest";

    private static CustomPostgreSqlContainer postgreSQLContainer;

    private CustomPostgreSqlContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomPostgreSqlContainer getInstance() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new CustomPostgreSqlContainer();
        }
        return postgreSQLContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", postgreSQLContainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", postgreSQLContainer.getContainerName());
        System.setProperty("TEST_DB_PASSWORD", postgreSQLContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
