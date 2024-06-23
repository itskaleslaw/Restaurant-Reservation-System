package org.group4;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

class Restaurant {
    private final String id;
    private final String name;
    private final Address address;
    private int rating;
    private boolean top10;
    private final int seatingCapacity;
    private final Map<String, Reservation> reservations = new HashMap<>();

    public Restaurant(String uniqueId, String name, Address address, int rating, boolean top10, int seatingCapacity) {
        this.id = (uniqueId == null) ? UUID.randomUUID().toString() : uniqueId;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.top10 = top10;
        this.seatingCapacity = seatingCapacity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public Address getAddress() {
        return address;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isTop10() {
        return top10;
    }

    public void setTop10(boolean top10) {
        this.top10 = top10;
    }

    public Reservation makeReservation(Customer customer, int partySize, LocalDateTime reservationDateTime, int credits) {
        // TODO: handle the case when the date is within 2 hours or other edge cases, like when customer has no funds
        //  Also remember since we are using a unique key for each reservation, the customer can absolutely NOT book a reservation
        //  for the same time.
        Reservation reservation = new Reservation(customer, partySize, reservationDateTime, credits);
        reservations.put(reservation.getKey(), reservation);
        // If the reservation fails, just return null
        return reservation;
    }

    public ArrivalStatus customerArrives(Customer customer, LocalDateTime reservationDateTime, LocalTime arrivalTime) {
        Reservation reservation = reservations.get(Reservation.generateKey(customer, reservationDateTime));
        System.out.println("Customer arrived with " + reservation);
        if (reservation == null) {
            return ArrivalStatus.WALK_IN;
        }
        LocalTime reservationTime = reservation.getDateTime().toLocalTime();
        if (arrivalTime.isAfter(reservationTime.plusMinutes(15))) {
            reservation.getCustomer().setMissedReservations(reservation.getCustomer().getMissedReservations() + 1);
            return ArrivalStatus.LATE;
        } else if (arrivalTime.isBefore(reservationTime)) {
            return ArrivalStatus.EARLY;
        } else if (arrivalTime.equals(reservationTime) || arrivalTime.isBefore(reservationTime.plusMinutes(15))) {
            reservation.getCustomer().setCredits(reservation.getCustomer().getCredits() + reservation.getCredits());
            return ArrivalStatus.ON_TIME;
        }
        return ArrivalStatus.WALK_IN;
    }

    public int checkSpace(LocalDateTime arrivalTime) {
        int usedSeats = 0;
        for (Reservation reservation : reservations.values()) {
            if (reservation.getDateTime().isEqual(arrivalTime) ||
                (reservation.getDateTime().isBefore(arrivalTime) && 
                reservation.getEndTime().isAfter(arrivalTime))) {
                usedSeats += reservation.getPartySize();
            }
        } 
        return seatingCapacity - usedSeats;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address=" + address +
                ", rating=" + rating +
                ", top10=" + top10 +
                ", seatingCapacity=" + seatingCapacity +
                '}';
    }

}
