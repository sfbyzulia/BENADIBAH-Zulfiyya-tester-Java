package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Optional;

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

    public void processIncomingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            // Check if vehicle is currently parked
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
                ticket.setParkingType(parkingSpot.getParkingType()); // Set the parking type
                
                ticketDAO.saveTicket(ticket);

                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehicleRegNumber();
            Optional<Ticket> optionalTicket = ticketDAO.getTicket(vehicleRegNumber);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                Date outTime = new Date();
                ticket.setOutTime(outTime);
                int ticketCount = ticketDAO.getTicketCount(vehicleRegNumber);
                boolean discount = (ticketCount > 1);
                fareCalculatorService.calculateFare(ticket, discount);
                if (ticketDAO.updateTicket(ticket)) {
                    ParkingSpot parkingSpot = ticket.getParkingSpot();
                    parkingSpot.setAvailable(true);
                    parkingSpotDAO.updateParking(parkingSpot);

                     // Calculate and format the parking duration
                long durationMillis = outTime.getTime() - ticket.getInTime().getTime();
                long minutes = (durationMillis / 1000) / 60;
                long hours = minutes / 60;
                minutes = minutes % 60;

                    System.out.println("Please pay the parking fare:" + ticket.getPriceText());
                    System.out.println("You have stayed " + hours + " hours and " + minutes + " minutes in our parking.");
                    System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
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

    private String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    private ParkingSpot getNextParkingNumberIfAvailable() {
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

    private ParkingType getVehicleType() {
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
