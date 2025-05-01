package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Notification;
import rmi.BUEServiceInterface;
import rmi.RMIServiceConnector;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BUEServiceUI extends Application {
    private BUEServiceInterface service;
    private ListView<String> publishersListView;
    private ListView<String> notificationTypesListView;
    private ListView<String> subscribersListView;
    private ListView<String> notificationsListView;
    private Timer updateTimer;
    private int lastNotificationCount = 0;
    private int lastPublisherCount = 0;
    private int lastSubscriberMapSize = 0;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Connect to the RMI service
            service = RMIServiceConnector.getService();
            
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));

            // Create sections
            VBox publisherBox = createPublisherSection();
            VBox subscriberBox = createSubscriberSection();
            VBox notificationBox = createNotificationSection();
            
            // Create tabs for better organization
            TabPane tabPane = new TabPane();
            
            Tab publisherTab = new Tab("Publishers");
            publisherTab.setContent(publisherBox);
            publisherTab.setClosable(false);
            
            Tab subscriberTab = new Tab("Subscribers");
            subscriberTab.setContent(subscriberBox);
            subscriberTab.setClosable(false);
            
            Tab notificationTab = new Tab("Notifications");
            notificationTab.setContent(notificationBox);
            notificationTab.setClosable(false);
            
            tabPane.getTabs().addAll(publisherTab, subscriberTab, notificationTab);
            
            root.setCenter(tabPane);
            
            Scene scene = new Scene(root, 600, 500);
            primaryStage.setTitle("BUE Event Notification Service Client");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Start periodic updates
            startPeriodicUpdates();
            
            // For debugging, log the initial service state
            System.out.println("BUE Service UI connected to RMI service");
            System.out.println("Notification types: " + service.getNotificationTypes());
            System.out.println("Publishers: " + service.getPublishers());
            System.out.println("Subscribers: " + service.getSubscribers());
        } catch (Exception e) {
            showErrorAlert("Connection Error", "Failed to connect to BUE Service", e);
        }
    }
    
    private VBox createPublisherSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        Label title = new Label("Registered Publishers");
        publishersListView = new ListView<>();
        
        Button addTypeButton = new Button("Add Notification Type");
        TextField newTypeField = new TextField();
        newTypeField.setPromptText("Enter new notification type");
        
        addTypeButton.setOnAction(e -> {
            String newType = newTypeField.getText().trim();
            if (!newType.isEmpty()) {
                try {
                    if (service.addNotificationType(newType)) {
                        newTypeField.clear();
                        updateUI();
                        System.out.println("Added new notification type: " + newType);
                    } else {
                        showAlert("Type exists", "This notification type already exists.");
                    }
                } catch (Exception ex) {
                    showErrorAlert("Service Error", "Failed to add notification type", ex);
                }
            }
        });
        
        Label typesLabel = new Label("Notification Types");
        notificationTypesListView = new ListView<>();
        
        box.getChildren().addAll(title, publishersListView, 
                                 typesLabel, notificationTypesListView, 
                                 newTypeField, addTypeButton);
        return box;
    }
    
    private VBox createSubscriberSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        Label title = new Label("Subscribers by Notification Type");
        subscribersListView = new ListView<>();
        
        box.getChildren().addAll(title, subscribersListView);
        return box;
    }
    
    private VBox createNotificationSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        Label title = new Label("Recent Notifications");
        notificationsListView = new ListView<>();
        
        box.getChildren().addAll(title, notificationsListView);
        return box;
    }
    
    private void updateUI() {
        try {
            Platform.runLater(() -> {
                try {
                    // Check if there are changes before updating UI
                    boolean hasChanges = false;
                    
                    // Check publishers
                    List<String> publishers = service.getPublishers();
                    if (publishers.size() != lastPublisherCount) {
                        publishersListView.getItems().clear();
                        publishersListView.getItems().addAll(publishers);
                        lastPublisherCount = publishers.size();
                        hasChanges = true;
                    }
                    
                    // Update notification types (always update these as they're small)
                    notificationTypesListView.getItems().clear();
                    notificationTypesListView.getItems().addAll(service.getNotificationTypes());
                    
                    // Update subscribers
                    Map<String, java.util.List<String>> subscribersMap = service.getSubscribers();
                    if (subscribersMap.size() != lastSubscriberMapSize || subscribersMapChanged(subscribersMap)) {
                        subscribersListView.getItems().clear();
                        for (Map.Entry<String, java.util.List<String>> entry : subscribersMap.entrySet()) {
                            String type = entry.getKey();
                            java.util.List<String> subs = entry.getValue();
                            if (!subs.isEmpty()) {
                                subscribersListView.getItems().add(type + ": " + String.join(", ", subs));
                            }
                        }
                        lastSubscriberMapSize = subscribersMap.size();
                        hasChanges = true;
                    }
                    
                    // Update notifications
                    List<Notification> allNotifications = service.getAllNotifications();
                    if (allNotifications.size() != lastNotificationCount) {
                        notificationsListView.getItems().clear();
                        
                        if (allNotifications.size() > 0 && lastNotificationCount != allNotifications.size()) {
                            System.out.println("Notifications count changed from " + lastNotificationCount + 
                                             " to " + allNotifications.size());
                        }
                        
                        for (Notification notification : allNotifications) {
                            String displayText = notification.getTimestamp() + " - " + 
                                               notification.getType() + " from " + 
                                               notification.getPublisher() + ": " + 
                                               notification.getContent();
                            notificationsListView.getItems().add(displayText);
                        }
                        
                        lastNotificationCount = allNotifications.size();
                        hasChanges = true;
                    }
                    
                    if (hasChanges) {
                        System.out.println("UI updated due to service state changes");
                    }
                } catch (Exception e) {
                    System.err.println("Error updating UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error in updateUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean subscribersMapChanged(Map<String, java.util.List<String>> newMap) {
        // A simple check that at least detects some changes in subscribers
        int totalSubscribers = 0;
        for (java.util.List<String> subs : newMap.values()) {
            totalSubscribers += subs.size();
        }
        
        // Cache this value for future comparisons
        int previousTotal = 0;
        for (int i = 0; i < subscribersListView.getItems().size(); i++) {
            String item = subscribersListView.getItems().get(i);
            String[] parts = item.split(":");
            if (parts.length > 1) {
                String[] subscribers = parts[1].trim().split(",");
                previousTotal += subscribers.length;
            }
        }
        
        return totalSubscribers != previousTotal;
    }
    
    private void startPeriodicUpdates() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateUI();
            }
        }, 0, 2000); // Update every 2 seconds (increased from 500ms)
    }
    
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showErrorAlert(String title, String message, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message + "\n\nError: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        });
    }
    
    @Override
    public void stop() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 