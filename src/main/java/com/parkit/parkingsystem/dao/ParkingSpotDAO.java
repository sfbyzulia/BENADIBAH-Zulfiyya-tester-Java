package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ParkingSpotDAO {
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Retrieves the next available parking spot of a specified type from the database.
     * @param parkingType The type of parking spot to search for.
     * @return An instance of ParkingSpot if available, otherwise null.
     */
    public ParkingSpot getNextAvailableSlot(ParkingType parkingType){
        Connection con = null;
        ParkingSpot parkingSpot = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
            ps.setString(1, parkingType.toString());
            ResultSet rs = ps.executeQuery();
        if(rs.next()){
            int parkingNumber = rs.getInt(1);
            if (parkingNumber > 0) {
            parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            }
        }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return parkingSpot;
    }

    /**
     * Updates the availability of a parking spot in the database.
     * @param parkingSpot The parking spot to update.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateParking(ParkingSpot parkingSpot){
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            dataBaseConfig.closePreparedStatement(ps);
            logger.info("Updated parking spot {} to available: {}", parkingSpot.getId(), parkingSpot.isAvailable());
            return (updateRowCount == 1);
        }catch (Exception ex){
            logger.error("Error updating parking info",ex);
            return false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
    }

    /**
     * Retrieves a parking spot by its ID from the database.
     * @param id The ID of the parking spot to retrieve.
     * @return The ParkingSpot object if found, otherwise null.
     */
    public ParkingSpot getParkingSpot(int id) {
         Connection con = null;
         ParkingSpot parkingSpot = null;
         try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from parking where PARKING_NUMBER = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            parkingSpot = new ParkingSpot(id, ParkingType.valueOf(rs.getString("TYPE")), rs.getBoolean("AVAILABLE"));
         }
        dataBaseConfig.closeResultSet(rs);
        dataBaseConfig.closePreparedStatement(ps);
    } catch (Exception ex) {
        logger.error("Error fetching parking spot", ex);
    } finally {
        dataBaseConfig.closeConnection(con);
    }
    return parkingSpot;
    }
}