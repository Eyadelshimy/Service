package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Notification;
import rmi.BUEServiceInterface;
import rmi.RMIServiceConnector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class SubscriberUI extends Application {
    private BUEServiceInterface service;
    private TextField subscriberIdField;
    private ListView<String> notificationTypesListView;
    private Button registerButton;
    private Button deregisterButton;
    private Button subscribeButton;
    private Button unsubscribeButton;
    private ListView<String> receivedNotificationsListView;
    private String subscriberId;
    private boolean isRegistered = false;
    private Timer updateTimer;
    private Timer typesUpdateTimer; // New timer for updating notification types
    private Set<String> processedNotifications = new HashSet<>();
    
    // Debug: keep track of subscribed types for this subscriber
    private Set<String> subscribedTypes = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        try {
            // Connect to the RMI service
            service = RMIServiceConnector.getService();
            
            VBox root = new VBox(10);
            root.setPadding(new Insets(10));

            // Subscriber ID section
            GridPane subscriberIdPane = new GridPane();
            subscriberIdPane.setHgap(10);
            subscriberIdPane.setVgap(10);
            subscriberIdPane.setPadding(new Insets(0, 0, 10, 0));

            Label idLabel = new Label("Subscriber ID:");
            subscriberIdField = new TextField();
            subscriberIdField.setPromptText("Enter unique subscriber ID");
            
            registerButton = new Button("Register");
            registerButton.setOnAction(e -> registerSubscriber());
            
            deregisterButton = new Button("Deregister");
            deregisterButton.setOnAction(e -> deregisterSubscriber());
            deregisterButton.setDisable(true);

            subscriberIdPane.add(idLabel, 0, 0);
            subscriberIdPane.add(subscriberIdField, 1, 0);
            subscriberIdPane.add(registerButton, 2, 0);
            subscriberIdPane.add(deregisterButton, 3, 0);

            // Subscription section
            VBox subscriptionPane = new VBox(10);
            subscriptionPane.setPadding(new Insets(10, 0, 10, 0));

            Label typesLabel = new Label("Available Notification Types:");
            notificationTypesListView = new ListView<>();
            notificationTypesListView.setPrefHeight(150);
            notificationTypesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            notificationTypesListView.setDisable(true);
            
            GridPane buttonPane = new GridPane();
            buttonPane.setHgap(10);
            
            subscribeButton = new Button("Subscribe to Selected");
            subscribeButton.setOnAction(e -> subscribeToNotifications());
            subscribeButton.setDisable(true);
            
            unsubscribeButton = new Button("Unsubscribe from Selected");
            unsubscribeButton.setOnAction(e -> unsubscribeFromNotifications());
            unsubscribeButton.setDisable(true);
            
            // Add debug button to show current subscriptions
            Button debugButton = new Button("Debug: Check Subscriptions");
            debugButton.setOnAction(e -> checkSubscriptions());
            
            buttonPane.add(subscribeButton, 0, 0);
            buttonPane.add(unsubscribeButton, 1, 0);
            buttonPane.add(debugButton, 2, 0);

            subscriptionPane.getChildren().addAll(typesLabel, notificationTypesListView, buttonPane);

            // Notification section
            Label notificationsLabel = new Label("Received Notifications:");
            receivedNotificationsListView = new ListView<>();
            receivedNotificationsListView.setPrefHeight(200);

            root.getChildren().addAll(
                new Label("Subscriber Application"),
                subscriberIdPane,
                new Separator(),
                subscriptionPane,
                new Separator(),
                notificationsLabel,
                receivedNotificationsListView
            );

            Scene scene = new Scene(root, 650, 550);
            primaryStage.setTitle("BUE Subscriber");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Initialize notification types
            updateNotificationTypes();
        } catch (Exception e) {
            showErrorAlert("Connection Error", "Failed to connect to BUE Service", e);
        }
    }
    
    private void checkSubscriptions() {
        try {
            // Get all subscribers from service
            Map<String, List<String>> allSubscribers = service.getSubscribers();
            
            // Check which types this subscriber is subscribed to according to the service
            System.out.println("DEBUG - Subscriber " + subscriberId + " subscriptions:");
            System.out.println("Local record shows subscribed to: " + subscribedTypes);
            System.out.println("Service record shows:");
            
            boolean foundInService = false;
            for (Map.Entry<String, List<String>> entry : allSubscribers.entrySet()) {
                String type = entry.getKey();
                List<String> subscribers = entry.getValue();
                
                if (subscribers.contains(subscriberId)) {
                    System.out.println("  - Subscribed to: " + type);
                    foundInService = true;
                }
            }
            
            if (!foundInService) {
                System.out.println("  - Not found in any subscription list!");
            }
            
            // Check recent notifications
            List<Notification> allNotifications = service.getAllNotifications();
            System.out.println("Total notifications in system: " + allNotifications.size());
            for (Notification notification : allNotifications) {
                System.out.println("  - " + notification);
            }
        } catch (Exception e) {
            System.err.println("Error checking subscriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void registerSubscriber() {
        String id = subscriberIdField.getText().trim();
        if (id.isEmpty()) {
            showAlert("Error", "Please enter a subscriber ID");
            return;
        }
        
        try {
            if (service.registerSubscriber(id)) {
                subscriberId = id;
                isRegistered = true;
                subscriberIdField.setDisable(true);
                registerButton.setDisable(true);
                deregisterButton.setDisable(false);
                notificationTypesListView.setDisable(false);
                subscribeButton.setDisable(false);
                unsubscribeButton.setDisable(false);
                showAlert("Success", "Subscriber registered successfully");
                
                // Debug
                System.out.println("Registered subscriber with ID: " + subscriberId);
                
                // Start receiving notifications
                startReceivingNotifications();
                
                // Start periodically updating notification types
                startUpdatingNotificationTypes();
            } else {
                showAlert("Error", "Failed to register subscriber");
            }
        } catch (Exception e) {
            showErrorAlert("Registration Error", "Failed to register subscriber", e);
        }
    }
    
    private void deregisterSubscriber() {
        try {
            if (service.deregisterSubscriber(subscriberId)) {
                isRegistered = false;
                subscriberIdField.setDisable(false);
                registerButton.setDisable(false);
                deregisterButton.setDisable(true);
                notificationTypesListView.setDisable(true);
                subscribeButton.setDisable(true);
                unsubscribeButton.setDisable(true);
                showAlert("Success", "Subscriber deregistered successfully");
                
                // Stop receiving notifications
                stopReceivingNotifications();
                // Clear the processed notifications set
                processedNotifications.clear();
                // Clear the subscribed types
                subscribedTypes.clear();
                
                // Stop updating notification types
                stopUpdatingNotificationTypes();
            } else {
                showAlert("Error", "Failed to deregister subscriber");
            }
        } catch (Exception e) {
            showErrorAlert("Deregistration Error", "Failed to deregister subscriber", e);
        }
    }
    
    private void subscribeToNotifications() {
        List<String> selectedTypes = notificationTypesListView.getSelectionModel().getSelectedItems();
        if (selectedTypes.isEmpty()) {
            showAlert("Error", "Please select at least one notification type");
            return;
        }
        
        try {
            boolean anySuccess = false;
            for (String type : selectedTypes) {
                if (service.subscribe(subscriberId, type)) {
                    anySuccess = true;
                    // Add to local record
                    subscribedTypes.add(type);
                    System.out.println("Successfully subscribed to: " + type);
                } else {
                    System.out.println("Failed to subscribe to: " + type);
                }
            }
            
            if (anySuccess) {
                showAlert("Success", "Subscribed to selected notification types");
                // Immediately check for notifications after subscribing
                updateReceivedNotifications();
                
                // Debug - print current subscriptions
                System.out.println("Current subscriptions for " + subscriberId + ": " + subscribedTypes);
            } else {
                showAlert("Error", "Failed to subscribe to notification types");
            }
        } catch (Exception e) {
            showErrorAlert("Subscription Error", "Failed to subscribe to notifications", e);
        }
    }
    
    private void unsubscribeFromNotifications() {
        List<String> selectedTypes = notificationTypesListView.getSelectionModel().getSelectedItems();
        if (selectedTypes.isEmpty()) {
            showAlert("Error", "Please select at least one notification type");
            return;
        }
        
        try {
            boolean anySuccess = false;
            for (String type : selectedTypes) {
                if (service.unsubscribe(subscriberId, type)) {
                    anySuccess = true;
                    // Remove from local record
                    subscribedTypes.remove(type);
                }
            }
            
            if (anySuccess) {
                showAlert("Success", "Unsubscribed from selected notification types");
                // Update UI immediately after unsubscribing
                updateReceivedNotifications();
            } else {
                showAlert("Error", "Failed to unsubscribe from notification types");
            }
        } catch (Exception e) {
            showErrorAlert("Unsubscription Error", "Failed to unsubscribe from notifications", e);
        }
    }
    
    private void updateNotificationTypes() {
        try {
            List<String> types = service.getNotificationTypes();
            // Save current selection
            List<String> selectedItems = new ArrayList<>(notificationTypesListView.getSelectionModel().getSelectedItems());
            
            // Update items preserving selection if possible
            notificationTypesListView.getItems().clear();
            notificationTypesListView.getItems().addAll(types);
            
            // Restore selection for items that still exist
            for (String item : selectedItems) {
                if (types.contains(item)) {
                    notificationTypesListView.getSelectionModel().select(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating notification types: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void startReceivingNotifications() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateReceivedNotifications();
            }
        }, 0, 1000); // Check for new notifications every second
    }
    
    private void stopReceivingNotifications() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
    
    private void startUpdatingNotificationTypes() {
        typesUpdateTimer = new Timer(true);
        typesUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateNotificationTypes());
            }
        }, 0, 2000); // Update notification types every 2 seconds
    }
    
    private void stopUpdatingNotificationTypes() {
        if (typesUpdateTimer != null) {
            typesUpdateTimer.cancel();
            typesUpdateTimer = null;
        }
    }
    
    private void updateReceivedNotifications() {
        if (isRegistered) {
            try {
                List<Notification> notifications = service.getNotificationsForSubscriber(subscriberId);
                
                // Check if there are any new notifications that haven't been processed
                List<String> newDisplayTexts = new ArrayList<>();
                boolean hasNewNotifications = false;
                
                System.out.println("Checking notifications for subscriber: " + subscriberId + 
                                 ", subscribed to: " + subscribedTypes);
                System.out.println("Service returned " + notifications.size() + " notifications");
                
                for (Notification notification : notifications) {
                    String notificationId = notification.getPublisher() + "-" + 
                                          notification.getTimestamp() + "-" + 
                                          notification.getType() + "-" + 
                                          notification.getContent();
                    
                    if (!processedNotifications.contains(notificationId)) {
                        hasNewNotifications = true;
                        processedNotifications.add(notificationId);
                        
                        String displayText = notification.getTimestamp() + " - " + 
                                          notification.getType() + " from " + 
                                          notification.getPublisher() + ": " + 
                                          notification.getContent();
                        newDisplayTexts.add(displayText);
                        System.out.println("New notification received: " + displayText);
                    }
                }
                
                // Only update UI if there are new notifications
                if (hasNewNotifications) {
                    Platform.runLater(() -> {
                        // Add new notifications to the top of the list
                        for (String displayText : newDisplayTexts) {
                            receivedNotificationsListView.getItems().add(0, displayText);
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Error receiving notifications: " + e.getMessage());
                e.printStackTrace();
            }
        }
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
        stopReceivingNotifications();
        stopUpdatingNotificationTypes();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 