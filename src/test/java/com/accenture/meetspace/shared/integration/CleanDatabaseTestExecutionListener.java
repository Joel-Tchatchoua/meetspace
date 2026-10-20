package com.accenture.meetspace.shared.integration;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CleanDatabaseTestExecutionListener extends AbstractTestExecutionListener {

    private static final String TABLES = """
            select table_name
            from information_schema.tables
            where table_schema = database()
              and table_type = 'BASE TABLE'
              and table_name <> 'flyway_schema_history'
            """;

    @Override
    public int getOrder() {
        return new TransactionalTestExecutionListener().getOrder() - 1;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        var dataSource = testContext.getApplicationContext().getBean(DataSource.class);
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            var tables = tables(statement);
            statement.execute("set foreign_key_checks = 0");
            try {
                for (var table : tables) {
                    statement.execute("truncate table `" + table + "`");
                }
            } finally {
                statement.execute("set foreign_key_checks = 1");
            }
        }
    }

    private static List<String> tables(Statement statement) throws Exception {
        var tables = new ArrayList<String>();
        try (var result = statement.executeQuery(TABLES)) {
            while (result.next()) {
                tables.add(result.getString(1));
            }
        }
        return tables;
    }
}
