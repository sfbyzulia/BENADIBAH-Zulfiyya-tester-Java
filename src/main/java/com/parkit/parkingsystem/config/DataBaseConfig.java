package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    /**
     * Attempts to establish a connection to the database.
     * @return A connection to the database or null if a connection could not be established.
     */
    public Connection getConnection() {
        try {
            // Attempt to load the database driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Attempt to establish a connection to the database
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/prod", "root", "R00tP@ssw0rd!");
        } catch (ClassNotFoundException | SQLException e) {
            // Log an error if the connection could not be established
            logger.error("Database connection error", e);
            // Return null indicating that the connection was not successful
            return null;
        }
    }

    /**
     * Closes the given database connection.
     * @param con The connection to close.
     */
    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
    }

    /**
     * Closes the given PreparedStatement.
     * @param ps The PreparedStatement to close.
     */
    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement", e);
            }
        }
    }

    /**
     * Closes the given ResultSet.
     * @param rs The ResultSet to close.
     */
    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set", e);
            }
        }
    }
}
