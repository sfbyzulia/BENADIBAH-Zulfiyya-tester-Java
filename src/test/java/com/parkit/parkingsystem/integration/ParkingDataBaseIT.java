package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingDataBaseIT {

    private static DataBaseConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
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

    @AfterEach
    public void tearDownPerTest() {
        System.out.println("---- Test Completed ----");
    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
        assertTrue(ticket.isPresent(), "Ticket should be present in the database");

        assertFalse(parkingSpotDAO.getParkingSpot(ticket.get().getParkingSpot().getId()).isAvailable(), "Parking spot should be updated to unavailable");
    }

    @Test
    public void testParkingLotExit() {
        testParkingACar();  // Ensure a car is parked first

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        // Delay to ensure out_time is different from in_time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        parkingService.processExitingVehicle();

        Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
        assertTrue(ticket.isPresent(), "Ticket should be present in the database");
        assertNotNull(ticket.get().getOutTime(), "Out time should be set after exiting");

        // Log the out time for debugging
        System.out.println("Out time after exiting: " + ticket.get().getOutTime());

        assertTrue(ticket.get().getPrice() >= 0, "Fare should be calculated and set in the ticket");

        assertTrue(parkingSpotDAO.getParkingSpot(ticket.get().getParkingSpot().getId()).isAvailable(), "Parking spot should be updated to available");
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        testParkingLotExit();  // First exit
        testParkingACar();  // Park again

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Delay to ensure out_time is different from in_time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        parkingService.processExitingVehicle();

        Optional<Ticket> ticket = ticketDAO.getTicket("ABCDEF");
        assertTrue(ticket.isPresent(), "Ticket should be present in the database");
        assertNotNull(ticket.get().getOutTime(), "Out time should be set after exiting");  // Ensure out time is set
        assertTrue(ticket.get().getPrice() >= 0, "Fare should be calculated and set in the ticket");
    }
}