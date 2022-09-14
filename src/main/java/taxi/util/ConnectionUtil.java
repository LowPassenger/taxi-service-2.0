package taxi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionUtil {
    private static final String URL =
            "jdbc:jtds:sqlserver://taxiservice.mssql.somee.com/taxiservice?serverTimezone=UTC";
    private static final String USERNAME = "dimmonishe_SQLLogin_1";
    private static final String PASSWORD = "5tb9me5hoy";
    private static final String JDBC_DRIVER = "com.sybase.jdbc2.jdbc.SybDriver";
    private static final Logger logger = LogManager.getLogger(ConnectionUtil.class);

    static {
        try {
            logger.debug("static class initialization for JDBC driver");
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("Can't find SQL Driver", e);
            throw new RuntimeException("Can't find SQL Driver", e);
        }
    }

    public static Connection getConnection() {
        logger.info("start getConnection method Params: username = {}, password OK", USERNAME);
        Properties dbProperties = new Properties();
        dbProperties.setProperty("user", USERNAME);
        dbProperties.setProperty("password", PASSWORD);
        try {
            logger.info("connection established");
            return DriverManager.getConnection(URL, dbProperties);
        } catch (SQLException e) {
            logger.error("Can't create connection to DB ", e);
            throw new RuntimeException("Can't create connection to DB ", e);
        }
    }
}
