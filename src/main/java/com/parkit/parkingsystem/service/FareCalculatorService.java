package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		calculateFare(ticket, false);
	}
	
	public void calculateFare(Ticket ticket, boolean discount) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		double inHour = ticket.getInTime().getTime()  ;
		double outHour = ticket.getOutTime().getTime();
		
		double reduction = 1;
		
		if (discount) {
			reduction = 0.95;
		}

		// TODO: Some tests are failing here. Need to check if this logic is correct
		double duration = (outHour - inHour) / 1000 / 60 / 60; // convert duration ms to hours
		System.out.println(duration);

		if (duration <= 0.5) {
			ticket.setPrice(0);
		} else {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * reduction);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * reduction);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");

			}
		}

	}
}