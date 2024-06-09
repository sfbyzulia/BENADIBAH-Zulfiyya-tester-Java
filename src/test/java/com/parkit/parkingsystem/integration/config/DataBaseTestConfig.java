package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DataBaseTestConfig extends DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

    @Override
    public Connection getConnection() {
        logger.info("Create DB connection");
        try {
            // Attempt to establish a connection to the test database
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "R00tP@ssw0rd!");
        } catch (SQLException e) {
            // Log an error if the connection could not be established
            logger.error("Database connection error", e);
            // Return null indicating that the connection was not successful
            return null;
        }
    }

    @Override
    public void closeConnection(Connection con){
        if(con != null){
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
    }

    @Override
    public void closePreparedStatement(PreparedStatement ps) {
        if(ps != null){
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement", e);
            }
        }
    }

    @Override
    public void closeResultSet(ResultSet rs) {
        if(rs != null){
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set", e);
            }
        }
    }
}
