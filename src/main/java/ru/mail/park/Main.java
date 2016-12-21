package ru.mail.park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class Main {
    public static DataSource connection;

    public static void main(String[] args) throws Exception {
        final Connector connector = new Connector();
        connection = connector.createSource();
        SpringApplication.run(Main.class, args);
    }
}


