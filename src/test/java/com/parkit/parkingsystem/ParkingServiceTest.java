package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
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

	private FareCalculatorService fareCalculatorService = new FareCalculatorService();

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
	public void processExitingVehicleTest() {
		// GIVEN
		String vehicleRegNumber = ticket.getVehicleRegNumber();
		ticket = ticketDAO.getTicket(vehicleRegNumber);
		Date outTime = new Date(System.currentTimeMillis());
		ticket.setOutTime(outTime);
		when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(2);

		// WHEN
		boolean discount = ticketDAO.getNbTicket(vehicleRegNumber) > 1;
		fareCalculatorService.calculateFare(ticket, discount);
		double fare = ticket.getPrice();
		double fareRounded = Math.round(fare * 100.0) / 100.0;
		
		if (ticketDAO.updateTicket(ticket)) {
			ParkingSpot parkingSpot = ticket.getParkingSpot();
			parkingSpot.setAvailable(true);
			parkingSpotDAO.updateParking(parkingSpot);
			System.out.println("Please pay the parking fare:" + ticket.getPrice());
			System.out.println(
					"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
		} else {
			System.out.println("Unable to update ticket information. Error occurred");
		}
		// THEN
		 verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

		// THEN
		assertEquals(fareRounded, (1.43));

	}
	
	@Test
	public void processExitingVehicleWithoutDiscountTest() {
		// GIVEN
		String vehicleRegNumber = ticket.getVehicleRegNumber();
		ticket = ticketDAO.getTicket(vehicleRegNumber);
		Date outTime = new Date(System.currentTimeMillis());
		ticket.setOutTime(outTime);
		when(ticketDAO.getNbTicket(vehicleRegNumber)).thenReturn(2);

		// WHEN
		boolean discount = ticketDAO.getNbTicket(vehicleRegNumber) < 1;
		fareCalculatorService.calculateFare(ticket, discount);
		double fare = ticket.getPrice();
		double fareRounded = Math.round(fare * 100.0) / 100.0;

		if (ticketDAO.updateTicket(ticket)) {
			ParkingSpot parkingSpot = ticket.getParkingSpot();
			parkingSpot.setAvailable(true);
			parkingSpotDAO.updateParking(parkingSpot);
			System.out.println("Please pay the parking fare:" + ticket.getPrice());
			System.out.println(
					"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
		} else {
			System.out.println("Unable to update ticket information. Error occurred");
		}
		// THEN
		 verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

		assertEquals(fareRounded, (1.5));

	}

}
