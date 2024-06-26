package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
        assertTrue(ticket.isPresent(), "Ticket should be present in the database");

        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.get().getParkingSpot().getId());
        assertNotNull(parkingSpot, "Parking spot should not be null");
        assertFalse(parkingSpot.isAvailable(), "Parking spot should be updated to unavailable");
    }

    @Test
public void testParkingLotExit() {
    testParkingACar();

      // Adding a delay to simulate parking time of 1 minute
      try {
        Thread.sleep(60000);  // Wait for 1 minute (60000 milliseconds)
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    parkingService.processExitingVehicle();

   // Retrieve the ticket from the database after exiting
    Optional<Ticket> optionalTicket = ticketDAO.getTicket("ABCDEF");
    assertTrue(optionalTicket.isPresent(), "Ticket was not found in the database after exiting.");
    Ticket ticket = optionalTicket.get();

    assertNotNull(ticket.getOutTime(), "Out time should be set after exiting");

    assertNotNull(ticket.getPrice(), "Fare should be calculated and set");

    // Check if the parking spot is available
    ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
    assertNotNull(parkingSpot, "Parking spot should not be null after exiting");
    assertTrue(parkingSpot.isAvailable(), "Parking spot should be updated to available");
    }
}
