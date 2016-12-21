package ru.mail.park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mail.park.Configurations.MysqlConnectionConfig;

import javax.sql.DataSource;

@SpringBootApplication
public class Application {
    public static DataSource connection;

    public static void main(String[] args) throws Exception {
        final MysqlConnectionConfig connector = new MysqlConnectionConfig();
        connection = connector.createSource();
        SpringApplication.run(Application.class, args);
    }
}


