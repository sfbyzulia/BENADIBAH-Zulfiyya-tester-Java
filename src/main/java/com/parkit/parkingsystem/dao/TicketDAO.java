package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Checks if a vehicle is currently parked by looking for a ticket without an out time.
     * @param vehicleRegNumber The vehicle registration number to check.
     * @return true if the vehicle is currently parked, false otherwise.
     */
    public boolean isVehicleCurrentlyParked(String vehicleRegNumber) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT EXISTS (SELECT 1 FROM ticket WHERE vehicle_reg_number = ? AND out_time IS NULL)")) {
            ps.setString(1, vehicleRegNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException ex) {
            logger.error("Error checking if vehicle is currently parked", ex);
        }
        return false;
    }

    /**
     * Saves a ticket into the database.
     * @param ticket The ticket object to save.
     * @return true if the ticket was saved successfully, false otherwise.
     */
    public boolean saveTicket(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : new Timestamp(ticket.getOutTime().getTime()));
            ps.setString(6, ticket.getParkingSpot().getParkingType().toString()); // Add parking type to save
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("Error saving ticket", ex);
            return false;
        }
    }

    /**
     * Retrieves the most recent ticket for a given vehicle registration number.
     * @param vehicleRegNumber The vehicle registration number.
     * @return An Optional containing the ticket if found, or an empty Optional if no ticket is found.
     */
    public Optional<Ticket> getTicket(String vehicleRegNumber) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM ticket WHERE vehicle_reg_number = ? ORDER BY in_time DESC LIMIT 1")) {
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String parkingTypeStr = rs.getString("parking_type");
                if (parkingTypeStr == null) {
                    throw new IllegalStateException("Parking type is missing for vehicle registration number: " + vehicleRegNumber);
                }
                ParkingType parkingType = ParkingType.valueOf(parkingTypeStr);
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt("parking_number"), parkingType, false);
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt("id"));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble("price"));
                ticket.setInTime(rs.getTimestamp("in_time"));
                ticket.setOutTime(rs.getTimestamp("out_time"));
                ticket.setParkingType(ParkingType.valueOf(rs.getString("PARKING_TYPE"))); 
                return Optional.of(ticket);
            }
        } catch (SQLException ex) {
            logger.error("Error fetching active ticket", ex);
        }
        return Optional.empty();
    }    

    /**
     * Updates the information of a given ticket in the database.
     * @param ticket The ticket to update.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateTicket(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE ticket SET price=?, out_time=? WHERE id=?")) {
            ps.setDouble(1, ticket.getPrice());
            if (ticket.getOutTime() != null) {
                ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            } else {
                ps.setTimestamp(2, null); 
                logger.error("Attempting to update ticket without outTime: " + ticket.getId());
            }
            ps.setInt(3, ticket.getId());
            int updateCount = ps.executeUpdate();
            return (updateCount == 1);
        } catch (SQLException ex) {
            logger.error("Error updating ticket with ID: " + ticket.getId(), ex);
        }
        return false;
    }    

    /**
     * Retrieves the number of tickets associated with a specific vehicle registration number.
     * @param vehicleRegNumber The vehicle registration number to check.
     * @return The number of tickets found.
     */
    public int getNbTicket(String vehicleRegNumber) {
        String sql = "SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?";
        try (Connection con = dataBaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            logger.error("Error fetching ticket count", ex);
        }
        return 0;
    }
}
