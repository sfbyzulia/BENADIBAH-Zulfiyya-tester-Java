package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.ParkingType;

/**
 * Represents a parking spot within a parking system.
 */
public class ParkingSpot {
    private int number;
    private ParkingType parkingType;
    private boolean isAvailable;

    /**
     * Constructs a new ParkingSpot instance.
     * @param number the unique identifier for the parking spot
     * @param parkingType the type of vehicle the spot is designated for (e.g., CAR, BIKE)
     * @param isAvailable indicates if the spot is currently available
     */
    public ParkingSpot(int number, ParkingType parkingType, boolean isAvailable) {
        this.number = number;
        this.parkingType = parkingType;
        this.isAvailable = isAvailable;
    }

    /**
     * Retrieves the parking spot's unique identifier.
     * @return the unique identifier of the parking spot
     */
    public int getId() {
        return number;
    }

    /**
     * Sets the parking spot's unique identifier.
     * @param number the new unique identifier for the parking spot
     */
    public void setId(int number) {
        this.number = number;
    }

    /**
     * Retrieves the parking type of the spot.
     * @return the parking type of the spot
     */
    public ParkingType getParkingType() {
        return parkingType;
    }

    /**
     * Sets the parking type of the spot.
     * @param parkingType the new parking type for this spot
     */
    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }

    /**
     * Checks if the parking spot is available.
     * @return true if the parking spot is available, false otherwise
     */
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Sets the availability of the parking spot.
     * @param available the new availability state of the parking spot
     */
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return number == that.number;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return number;
    }
}
