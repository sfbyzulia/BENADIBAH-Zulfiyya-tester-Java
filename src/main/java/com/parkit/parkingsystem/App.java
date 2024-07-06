package com.parkit.parkingsystem;

import com.parkit.parkingsystem.service.InteractiveShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application class that serves as the entry point to the parking system.
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");
    
    /**
     * Main method to start the parking system application.
     * @param args Command line arguments, not used in this application.
     */
    public static void main(String args[]){
        logger.info("Initializing Parking System");
        InteractiveShell.loadInterface();
    }
}
