package ru.mail.park.Configurations;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import javax.sql.DataSource;

@EnableAutoConfiguration
public class MysqlConnectionConfig {
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3306/Database?autoreconnect=true&useUnicode=yes&useSSL=false&characterEncoding=UTF-8";
    public static final String USER = "root";
    public static final String PASSWORD = "626836";
    public static final int MAX_ACTIVE = 5;

    public DataSource createSource() throws Exception
    {
        Class.forName(DRIVER).newInstance();
        final GenericObjectPool connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(MAX_ACTIVE);
        final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(URL, USER, PASSWORD);

        final PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory,
                connectionPool, null, null, false, true);
        return new PoolingDataSource(connectionPool);
    }
}
