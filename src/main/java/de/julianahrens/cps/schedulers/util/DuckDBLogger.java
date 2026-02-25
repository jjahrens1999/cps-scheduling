package de.julianahrens.cps.schedulers.util;

import org.duckdb.DuckDBConnection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DuckDBLogger {
    public static final String DUCKDB_NAME = "output.db";

    private final DuckDBConnection connection;

    {
        try {
            connection = (DuckDBConnection) DriverManager.getConnection("jdbc:duckdb:" + DUCKDB_NAME);
            Statement statement = connection.createStatement();
            statement.execute(buildDdlStatement());
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildDdlStatement() {
        return """
                CREATE TABLE IF NOT EXISTS results (
                    task_id INTEGER,
                    start_time INTEGER,
                    finish_time INTEGER,
                    deadline INTEGER,
                    task_type STRING,
                    scheduler STRING,
                    switch_time INTEGER,
                    recipe_type STRING
                );
                """;
    }

    private String buildDmlStatement() {
        return """
                INSERT INTO results (task_id, start_time, finish_time, deadline, task_type, scheduler, switch_time, recipe_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
    }

    public void log(int taskId, double startTime, double finishTime, int deadline, String taskType, String scheduler, int switchTime, String recipeType) {
        try (PreparedStatement statement = connection.prepareStatement(buildDmlStatement())) {
            statement.setInt(1, taskId);
            statement.setInt(2, (int) startTime);
            statement.setInt(3, (int) finishTime);
            statement.setInt(4, deadline);
            statement.setString(5, taskType);
            statement.setString(6, scheduler);
            statement.setInt(7, switchTime);
            statement.setString(8, recipeType);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
