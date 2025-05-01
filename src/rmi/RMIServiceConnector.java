package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServiceConnector {
    
    private static BUEServiceInterface service;
    
    /**
     * Connect to the RMI service
     * @return The service interface
     * @throws Exception if connection fails
     */
    public static synchronized BUEServiceInterface getService() throws Exception {
        if (service == null) {
            Registry registry = LocateRegistry.getRegistry("localhost", RMIBUEServiceLauncher.RMI_PORT);
            service = (BUEServiceInterface) registry.lookup(RMIBUEServiceLauncher.SERVICE_NAME);
            System.out.println("Connected to RMI service: " + RMIBUEServiceLauncher.SERVICE_NAME);
        }
        return service;
    }
} 