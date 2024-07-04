package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBasePrepareService {

    private static final Logger logger = LogManager.getLogger("DataBasePrepareService");
    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries() {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = dataBaseTestConfig.getConnection();
            ps = connection.prepareStatement("DELETE FROM ticket");
            ps.execute();
            ps = connection.prepareStatement("UPDATE parking SET available = true");
            ps.execute();
        } catch (SQLException e) {
            logger.error("Error clearing database entries", e);
        } finally {
            dataBaseTestConfig.closePreparedStatement(ps);
            dataBaseTestConfig.closeConnection(connection);
        }
    }
}