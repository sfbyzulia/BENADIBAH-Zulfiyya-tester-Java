package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

/**
 * Manages the operations related to parking vehicles and processing their exit.
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * Processes the entry of a vehicle into the parking.
     * Registers the vehicle, allocates a parking spot, and saves the parking ticket.
     */
    public void processIncomingVehicle() {
        try {
            String vehicleRegNumber = getVehicleRegNumber();
            if (ticketDAO.isVehicleCurrentlyParked(vehicleRegNumber)) {
                System.out.println("Vehicle is already parked.");
                return;
            }

            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot != null && parkingSpot.getId() > 0){
                parkingSpot.setAvailable(false);
                boolean updated = parkingSpotDAO.updateParking(parkingSpot);
                logger.info("Parking spot {} set to unavailable: {}", parkingSpot.getId(), updated);

                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticket.setParkingType(parkingSpot.getParkingType());

                ticketDAO.saveTicket(ticket);

                if (ticketDAO.getNbTicket(vehicleRegNumber) > 1) {
                    System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
                }

                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
            }
        } catch(Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    /**
     * Processes the exit of a vehicle from the parking.
     * Calculates the parking fare and updates the parking ticket and spot status.
     */
    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehicleRegNumber();
            Optional<Ticket> optionalTicket = ticketDAO.getTicket(vehicleRegNumber);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                Date outTime = new Date();
                ticket.setOutTime(outTime);
    
                if (ticket.getOutTime() == null) {
                    logger.error("outTime is null before updating the ticket");
                } else {
                    logger.debug("Updating ticket with ID: " + ticket.getId() + " and outTime: " + new Timestamp(ticket.getOutTime().getTime()));
                }
    
                int ticketCount = ticketDAO.getNbTicket(vehicleRegNumber);
                boolean discount = (ticketCount > 1);
                fareCalculatorService.calculateFare(ticket, discount);  // Using calculateFare with discount
    
                if (ticketDAO.updateTicket(ticket)) {
                    ParkingSpot parkingSpot = ticket.getParkingSpot();
                    parkingSpot.setAvailable(true);
                    parkingSpotDAO.updateParking(parkingSpot);
    
                    long durationMillis = outTime.getTime() - ticket.getInTime().getTime();
                    long minutes = (durationMillis / 1000) / 60;
                    long hours = minutes / 60;
                    minutes = minutes % 60;
    
                    System.out.println("Please pay the parking fare: " + ticket.getPriceText());
                    System.out.println("You have stayed " + hours + " hours and " + minutes + " minutes in our parking.");
                    System.out.println("Recorded out-time for vehicle number: " + ticket.getVehicleRegNumber() + " is: " + outTime);
                } else {
                    System.out.println("Unable to update ticket information. Error occurred");
                }
            } else {
                System.out.println("Ticket not found");
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }

    /**
     * Retrieves the vehicle registration number from the user.
     * @return The vehicle registration number as entered by the user.
     * @throws Exception if an error occurs during reading input from the user.
     */
    public String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Identifies the next available parking spot based on the vehicle type.
     * @return A ParkingSpot object representing the next available spot, or null if none are available.
     */
    public ParkingSpot getNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingSpot = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingSpot == null || parkingSpot.getId() <= 0) {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    /**
     * Prompts the user to select a vehicle type and returns the corresponding ParkingType.
     * @return The ParkingType as selected by the user.
     * @throws IllegalArgumentException if an invalid input is provided.
     */
    public ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1:
                return ParkingType.CAR;
            case 2:
                return ParkingType.BIKE;
            default:
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
        }
    }
}