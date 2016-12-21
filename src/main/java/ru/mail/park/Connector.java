package ru.mail.park;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;

public class Connector {
    public static final String DRIVER = "com.mysql.jdbc.Driver";

    public static final String URL = "jdbc:mysql://localhost:3306/TPForum?autoreconnect=true&useUnicode=yes&useSSL=false&characterEncoding=UTF-8";

    public static final String USER = "root";

    public static final String PASSWORD = "626836";
    public static final int MAX_ACTIVE = 5;

    public DataSource createSource() throws Exception
    {
        Class.forName(DRIVER).newInstance();
        final GenericObjectPool     connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(MAX_ACTIVE);
        final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(URL, USER, PASSWORD);

        final PoolableConnectionFactory pcf = new PoolableConnectionFactory(
                connectionFactory,
                connectionPool,
                null,
                null,
                false,
                true);
        return new PoolingDataSource(connectionPool);
    }
}
