package launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicReference;

public class RMIMainLauncher extends Application {
    
    private AtomicReference<Process> rmiServiceProcess = new AtomicReference<>();
    
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        Label titleLabel = new Label("BUE Event Notification System with RMI");
        
        Button startServiceButton = new Button("Launch RMI BUE Service");
        startServiceButton.setMaxWidth(Double.MAX_VALUE);
        startServiceButton.setOnAction(e -> {
            try {
                // Start the RMI service in a separate process
                ProcessBuilder pb = new ProcessBuilder("java", 
                    "-cp", System.getProperty("java.class.path"),
                    "launcher.RMIServiceLauncher");
                pb.inheritIO();
                Process process = pb.start();
                rmiServiceProcess.set(process);
                System.out.println("Started RMI Service process");
                startServiceButton.setDisable(true);
            } catch (Exception ex) {
                System.err.println("Error starting RMI Service: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        Button stopServiceButton = new Button("Stop RMI Service");
        stopServiceButton.setMaxWidth(Double.MAX_VALUE);
        stopServiceButton.setOnAction(e -> {
            Process process = rmiServiceProcess.get();
            if (process != null && process.isAlive()) {
                try {
                    // Send the "exit" command to the RMI service
                    BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream()));
                    writer.write("exit\n");
                    writer.flush();
                    
                    // Give it a chance to shut down gracefully
                    new Thread(() -> {
                        try {
                            // Wait for the process to terminate
                            boolean exited = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                            if (!exited) {
                                // If it doesn't exit gracefully, force it
                                process.destroyForcibly();
                            }
                            Platform.runLater(() -> {
                                startServiceButton.setDisable(false);
                                System.out.println("RMI Service stopped");
                            });
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }).start();
                } catch (Exception ex) {
                    System.err.println("Error stopping RMI Service: " + ex.getMessage());
                    ex.printStackTrace();
                    // If we can't stop it gracefully, force it
                    process.destroyForcibly();
                    startServiceButton.setDisable(false);
                }
            } else {
                System.out.println("No RMI Service process to stop");
                startServiceButton.setDisable(false);
            }
        });
        
        Button startPublishersButton = new Button("Launch 3 Publishers");
        startPublishersButton.setMaxWidth(Double.MAX_VALUE);
        startPublishersButton.setOnAction(e -> {
            try {
                PublisherLauncher.main(new String[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        Button startSubscribersButton = new Button("Launch 3 Subscribers");
        startSubscribersButton.setMaxWidth(Double.MAX_VALUE);
        startSubscribersButton.setOnAction(e -> {
            try {
                SubscriberLauncher.main(new String[0]);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        root.getChildren().addAll(
            titleLabel,
            startServiceButton,
            stopServiceButton,
            startPublishersButton, 
            startSubscribersButton
        );
        
        Scene scene = new Scene(root, 350, 250);
        primaryStage.setTitle("BUE RMI System Launcher");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Add a shutdown hook to stop the RMI service when the application exits
        primaryStage.setOnCloseRequest(event -> {
            Process process = rmiServiceProcess.get();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                System.out.println("RMI Service stopped on application exit");
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 