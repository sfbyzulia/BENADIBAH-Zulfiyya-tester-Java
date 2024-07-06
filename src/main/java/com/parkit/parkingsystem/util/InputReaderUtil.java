package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * Utility class to handle reading user inputs from the console.
 */
public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in);
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    /**
     * Reads the user's selection from the console as an integer.
     * @return the integer value entered by the user, or -1 if the input is invalid.
     */
    public int readSelection() {
        try {
            int input = Integer.parseInt(scan.nextLine());
            return input;
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    /**
     * Reads the vehicle registration number from the console.
     * @return the vehicle registration number as a string.
     * @throws Exception if the input is invalid or an error occurs during input reading.
     */
    public String readVehicleRegistrationNumber() throws Exception {
        try {
            String vehicleRegNumber= scan.nextLine();
            if(vehicleRegNumber == null || vehicleRegNumber.trim().length()==0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }


}
