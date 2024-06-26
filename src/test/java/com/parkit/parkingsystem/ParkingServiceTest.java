package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingType(ParkingType.CAR);

        when(ticketDAO.getTicket(anyString())).thenReturn(Optional.of(ticket));
        lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(parkingSpot);
        when(ticketDAO.isVehicleCurrentlyParked(anyString())).thenReturn(false);

        parkingService.processIncomingVehicle();

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO, times(1)).saveTicket(ticketCaptor.capture());
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);

        Ticket capturedTicket = ticketCaptor.getValue();
        assertNotNull(capturedTicket);
        assertEquals("ABCDEF", capturedTicket.getVehicleRegNumber());
        assertEquals(parkingSpot, capturedTicket.getParkingSpot());
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1 hour ago
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(Optional.of(ticket));
        doReturn(false).when(ticketDAO).updateTicket(any(Ticket.class));

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(parkingSpot);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertTrue(result.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(null);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        assertNull(result);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(3);

        assertThrows(IllegalArgumentException.class, () -> parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1 hour ago
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(Optional.of(ticket));
        doReturn(true).when(ticketDAO).updateTicket(any(Ticket.class));
        doReturn(true).when(parkingSpotDAO).updateParking(any(ParkingSpot.class));

        parkingService.processExitingVehicle();

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).updateTicket(ticketCaptor.capture());
        Ticket capturedTicket = ticketCaptor.getValue();

        assertNotNull(capturedTicket.getOutTime());
        assertTrue(capturedTicket.getPrice() > 0);
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
    }

    @Test
    public void processExitingVehicleTestWithDiscount() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1 hour ago
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(Optional.of(ticket));
        when(ticketDAO.getTicketCount(anyString())).thenReturn(2); // Returning 2 to simulate a repeat customer
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).updateTicket(ticketCaptor.capture());
        Ticket capturedTicket = ticketCaptor.getValue();

        assertNotNull(capturedTicket.getOutTime());
        assertTrue(capturedTicket.getPrice() > 0);
        verify(parkingSpotDAO, times(1)).updateParking(parkingSpot);
    }
}
