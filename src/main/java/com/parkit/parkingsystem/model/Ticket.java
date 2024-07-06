package com.parkit.parkingsystem.model;

import java.util.Date;

import com.parkit.parkingsystem.constants.ParkingType;

/**
 * Represents a parking ticket issued to a vehicle.
 */
public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private String priceText;  // New field to store the price as text with EUR symbol
    private Date inTime;
    private Date outTime;
    private ParkingType parkingType; // New field for parking type

    /**
     * Gets the unique identifier for the ticket.
     * @return the ticket's identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the ticket.
     * @param id the new identifier
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the parking spot associated with this ticket.
     * @return the parking spot
     */
    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    /**
     * Sets the parking spot associated with this ticket.
     * @param parkingSpot the parking spot
     */
    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    /**
     * Gets the vehicle registration number associated with this ticket.
     * @return the vehicle registration number
     */
    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    /**
     * Sets the vehicle registration number for this ticket.
     * @param vehicleRegNumber the new vehicle registration number
     */
    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    /**
     * Gets the price charged for the parking.
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price charged for the parking.
     * @param price the new price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the price as a text string, typically formatted with currency.
     * @return the price text
     */
    public String getPriceText() {
        return priceText;
    }

    /**
     * Sets the price text, typically formatted with currency.
     * @param priceText the new price text
     */
    public void setPriceText(String priceText) {
        this.priceText = priceText;
    }

    /**
     * Gets the time when the vehicle entered the parking.
     * @return the in-time
     */
    public Date getInTime() {
        return inTime;
    }

    /**
     * Sets the time when the vehicle entered the parking.
     * @param inTime the new in-time
     */
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    /**
     * Gets the time when the vehicle exited the parking.
     * @return the out-time
     */
    public Date getOutTime() {
        return outTime;
    }

    /**
     * Sets the time when the vehicle exited the parking.
     * @param outTime the new out-time
     */
    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }

    /**
     * Gets the parking type designated for the parking spot.
     * @return the parking type
     */
    public ParkingType getParkingType() {
        return parkingType;
    }

    /**
     * Sets the parking type designated for the parking spot.
     * @param parkingType the new parking type
     */
    public void setParkingType(ParkingType parkingType) {
        this.parkingType = parkingType;
    }
 }