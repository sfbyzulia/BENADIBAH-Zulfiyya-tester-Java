package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

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

    @AfterAll
    public static void tearDown() {
        // Cleanup resources, if any
    }

    @Test
public void testParkingACar() {
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    parkingService.processIncomingVehicle();

    // Verify ticket is saved and parking spot availability is updated
    Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
    assertTrue(ticket.isPresent(), "Ticket should be present in the database");

    // Fetch the parking spot and check if it's updated to unavailable
    ParkingSpot parkingSpot = ticket.get().getParkingSpot();
    ParkingSpot updatedParkingSpot = parkingSpotDAO.getParkingSpot(parkingSpot.getId());
    assertFalse(updatedParkingSpot.isAvailable(), "Parking spot should be updated to unavailable");
}

@Test
public void testParkingLotExit() {
    testParkingACar();  // Ensure there's a parked car
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    parkingService.processExitingVehicle();

    // Verify fare and out time
    Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
    assertTrue(ticket.isPresent(), "Ticket should be present");
    assertNotNull(ticket.get().getOutTime(), "Out time should be set");
    assertTrue(ticket.get().getPrice() > 0, "Fare should be calculated and set");

    // Verify that the parking spot is available again
    ParkingSpot parkingSpot = ticket.get().getParkingSpot();
    ParkingSpot updatedParkingSpot = parkingSpotDAO.getParkingSpot(parkingSpot.getId());
    assertTrue(updatedParkingSpot.isAvailable(), "Parking spot should be available");
    }
}