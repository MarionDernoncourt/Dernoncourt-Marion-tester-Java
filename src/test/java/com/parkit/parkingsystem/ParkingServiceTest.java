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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	Ticket ticket = new Ticket();

	@BeforeEach
	private void setUpPerTest() {
		try {

			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");

			lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

			lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

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

		assertEquals(null, parkingSpot);

	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		assertEquals(null, parkingSpot);
	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(3);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		assertEquals(3, parkingSpot.getId());
		assertTrue(parkingSpot.isAvailable());
	}

	@Test
	public void processExitingVehicleTestUnableUpdate() {

		System.out.println(ticket.getVehicleRegNumber());
		when(ticketDAO.updateTicket(ticket)).thenReturn(false);

		parkingService.processExitingVehicle();

		verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
	}

	@Test
	public void ProcessIncomingVehicle() {

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(2);

		parkingService.processIncomingVehicle();

		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

	}

	@Test
	public void processExitingVehicleTest() {

		when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(1);

		parkingService.processExitingVehicle();

		double ExpectedFare = 1.5;
		double fareRounded = Math.round(ticket.getPrice() * 100.0) / 100.0;

		// THEN
		verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
		assertEquals(ExpectedFare, fareRounded);

	}

}