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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;


	@BeforeEach
	public void setUpPerTest() {
		try {
			Ticket ticket = new Ticket();

			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");

			when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() throws Exception {

		when(inputReaderUtil.readSelection()).thenReturn(3);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO, never()).getNextAvailableSlot(ParkingType.CAR);
		assertNull(parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		assertEquals(null, parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		assertEquals(3, parkingSpot.getId());
		assertTrue(parkingSpot.isAvailable());
	}

	@Test
	public void processExitingVehicleTestUnableUpdate() throws Exception {
		
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

		ArgumentCaptor<Ticket> ticketCaptor= ArgumentCaptor.forClass(Ticket.class);
		
		parkingService.processExitingVehicle();
		
		verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());
		
		Ticket ticketChecked = ticketCaptor.getValue()	;
		
		assertNotNull(ticketChecked.getOutTime());
		assertFalse(ticketChecked.getParkingSpot().isAvailable());
	}

	@Test
	public void ProcessIncomingVehicle() throws Exception {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(2);

		ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
		
		parkingService.processIncomingVehicle();

		verify(ticketDAO, times(1)).saveTicket(ticketCaptor.capture());
		
		Ticket savedTicket = ticketCaptor.getValue()	;
		
		assertNotNull(savedTicket.getInTime());
		assertNotNull(savedTicket.getParkingSpot());
		assertEquals(2, savedTicket.getParkingSpot().getId());
		assertFalse(savedTicket.getParkingSpot().isAvailable());
		}

	@Test
	public void processExitingVehicleTest() throws Exception {
		
		when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

		ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

		parkingService.processExitingVehicle();
	
		verify(ticketDAO, times(1)).updateTicket(ticketCaptor.capture());
		
		Ticket updatedTicket = ticketCaptor.getValue();

		assertNotNull(updatedTicket.getOutTime());
		assertTrue(updatedTicket.getParkingSpot().isAvailable());


	}

}