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

		double inHour = ticket.getInTime().getTime()  ; // Retourne l'heure d'entrée en ms
		double outHour = ticket.getOutTime().getTime(); // Retourne l'heure de sortie en ms
		
		double reduction = 1;
		
		if (discount) {
			reduction = 0.95;
		}

		
		double duration = (outHour - inHour) / 1000 / 60 / 60; // convert duration ms to hours
		
		if (duration <= 0.5) {
			ticket.setPrice(0);
		} else {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				double price = duration * Fare.CAR_RATE_PER_HOUR * reduction ;
				double priceRounded = Math.round(price * 100 ) / 100.0 ;
				ticket.setPrice(priceRounded);
				break;
			}
			case BIKE: {
				double price = duration * Fare.BIKE_RATE_PER_HOUR * reduction;
				double priceRounded = Math.round(price * 100 ) / 100.0 ;
				ticket.setPrice(priceRounded);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");

			}
		}

	}
}