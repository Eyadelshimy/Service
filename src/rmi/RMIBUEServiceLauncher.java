package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class RMIBUEServiceLauncher {
    
    public static final String SERVICE_NAME = "BUEService";
    public static final int RMI_PORT = 1099;
    private static BUEServiceImpl serviceImpl;
    private static Registry registry;
    
    public static void main(String[] args) {
        try {
            // Create the service implementation
            serviceImpl = new BUEServiceImpl();
            
            // Create or get the registry
            try {
                // Try to create a new registry
                registry = LocateRegistry.createRegistry(RMI_PORT);
                System.out.println("RMI registry created at port " + RMI_PORT);
            } catch (Exception e) {
                // If creation fails, a registry might already exist
                System.out.println("RMI registry already exists, getting reference");
                registry = LocateRegistry.getRegistry(RMI_PORT);
            }
            
            // Bind the service to the registry
            registry.rebind(SERVICE_NAME, serviceImpl);
            System.out.println("BUE Service bound to registry with name: " + SERVICE_NAME);
            
            System.out.println("BUE Service is ready to receive requests from publishers and subscribers");
            System.out.println("Type 'exit' to shut down the service gracefully");
            
            // Add a shutdown hook to handle Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down RMI service...");
                shutdownService();
            }));
            
            // Wait for 'exit' command
            Scanner scanner = new Scanner(System.in);
            String input;
            while (true) {
                input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("Shutting down RMI service...");
                    break;
                }
            }
            scanner.close();
            
            // Clean shutdown
            shutdownService();
            
            System.out.println("RMI service terminated");
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("BUE Service error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void shutdownService() {
        try {
            if (registry != null && serviceImpl != null) {
                // Unbind the service
                registry.unbind(SERVICE_NAME);
                System.out.println("Service unbound from registry");
                
                // Unexport the service
                UnicastRemoteObject.unexportObject(serviceImpl, true);
                System.out.println("Service unexported");
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 