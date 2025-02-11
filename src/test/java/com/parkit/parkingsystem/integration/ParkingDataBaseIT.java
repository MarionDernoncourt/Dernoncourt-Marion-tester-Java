package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	@Spy
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {
		 dataBasePrepareService.clearDataBaseEntries();
	}

	@Test
	public void testParkingACar() throws Exception {
		// TODO: check that a ticket is actualy saved in DB and Parking table is updated
		// with availability

		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		ParkingSpot parkingSpotAvailable = parkingService.getNextParkingNumberIfAvailable();

		parkingService.processIncomingVehicle();

		String vehRegNumber = inputReaderUtil.readVehicleRegistrationNumber();

		Ticket ticket = ticketDAO.getTicket(vehRegNumber);
		assertEquals(ticket.getParkingSpot().getId(), parkingSpotAvailable.getId());
		assertFalse(ticketDAO.getTicket(vehRegNumber).getParkingSpot().isAvailable());

	}

	@Test
	public void testParkingLotExit() throws Exception {
		// TODO: check that the fare generated and out time are populated correctly in
		// the database

		// Process Incoming Vehicule
		testParkingACar();

		// Recuperation du ticket
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		String vehRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
		Ticket ticket = ticketDAO.getTicket(vehRegNumber);

		// Modification heure d'entrée pour générer un tarif différent de 0
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - 60 * 60 * 1000);
		ticket.setInTime(inTime);
		ticketDAO.updateTicketForInTime(ticket);

		// process Exiting Vehicule
		parkingService.processExitingVehicle();

		// Récuperation du ticket du véhicule après sa sortie
		Ticket ticketExiting = ticketDAO.getTicket(vehRegNumber);

		assertNotNull(ticketExiting.getOutTime());
		assertNotNull(ticketExiting.getPrice());
	}

	@Test
	public void testParkingLotExitRecurringUser() throws Exception {

		String vehRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
		
		testParkingLotExit();
		Thread.sleep(1000);
		testParkingLotExit();

		Ticket ticket = ticketDAO.getTicket(vehRegNumber); // ticket le plus récent

		
		System.out.println("le prixpour recurring user: " + ticket.getPrice());

	}

}
