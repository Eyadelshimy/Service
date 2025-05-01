package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Notification;
import rmi.BUEServiceInterface;
import rmi.RMIServiceConnector;

public class PublisherUI extends Application {
    private BUEServiceInterface service;
    private TextField publisherIdField;
    private ComboBox<String> notificationTypeComboBox;
    private TextArea contentTextArea;
    private Button publishButton;
    private Button registerButton;
    private Button deregisterButton;
    private ListView<String> notificationHistoryListView;
    private String publisherId;
    private boolean isRegistered = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Connect to the RMI service
            service = RMIServiceConnector.getService();
            
            VBox root = new VBox(10);
            root.setPadding(new Insets(10));

            // Publisher ID section
            GridPane publisherIdPane = new GridPane();
            publisherIdPane.setHgap(10);
            publisherIdPane.setVgap(10);
            publisherIdPane.setPadding(new Insets(0, 0, 10, 0));

            Label idLabel = new Label("Publisher ID:");
            publisherIdField = new TextField();
            publisherIdField.setPromptText("Enter unique publisher ID");
            
            registerButton = new Button("Register");
            registerButton.setOnAction(e -> registerPublisher());
            
            deregisterButton = new Button("Deregister");
            deregisterButton.setOnAction(e -> deregisterPublisher());
            deregisterButton.setDisable(true);

            publisherIdPane.add(idLabel, 0, 0);
            publisherIdPane.add(publisherIdField, 1, 0);
            publisherIdPane.add(registerButton, 2, 0);
            publisherIdPane.add(deregisterButton, 3, 0);

            // Notification section
            GridPane notificationPane = new GridPane();
            notificationPane.setHgap(10);
            notificationPane.setVgap(10);
            notificationPane.setPadding(new Insets(10, 0, 10, 0));

            Label typeLabel = new Label("Notification Type:");
            notificationTypeComboBox = new ComboBox<>();
            notificationTypeComboBox.setDisable(true);
            
            Label contentLabel = new Label("Content:");
            contentTextArea = new TextArea();
            contentTextArea.setPromptText("Enter notification content");
            contentTextArea.setPrefRowCount(5);
            contentTextArea.setDisable(true);
            
            publishButton = new Button("Publish");
            publishButton.setOnAction(e -> publishNotification());
            publishButton.setDisable(true);

            notificationPane.add(typeLabel, 0, 0);
            notificationPane.add(notificationTypeComboBox, 1, 0);
            notificationPane.add(contentLabel, 0, 1);
            notificationPane.add(contentTextArea, 1, 1);
            notificationPane.add(publishButton, 1, 2);

            // History section
            Label historyLabel = new Label("Published Notifications:");
            notificationHistoryListView = new ListView<>();
            notificationHistoryListView.setPrefHeight(200);

            root.getChildren().addAll(
                new Label("Publisher Application"),
                publisherIdPane,
                new Separator(),
                notificationPane,
                new Separator(),
                historyLabel,
                notificationHistoryListView
            );

            Scene scene = new Scene(root, 550, 500);
            primaryStage.setTitle("BUE Publisher");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Initialize notification types
            updateNotificationTypes();
        } catch (Exception e) {
            showErrorAlert("Connection Error", "Failed to connect to BUE Service", e);
        }
    }
    
    private void registerPublisher() {
        String id = publisherIdField.getText().trim();
        if (id.isEmpty()) {
            showAlert("Error", "Please enter a publisher ID");
            return;
        }
        
        try {
            if (service.registerPublisher(id)) {
                publisherId = id;
                isRegistered = true;
                publisherIdField.setDisable(true);
                registerButton.setDisable(true);
                deregisterButton.setDisable(false);
                notificationTypeComboBox.setDisable(false);
                contentTextArea.setDisable(false);
                publishButton.setDisable(false);
                showAlert("Success", "Publisher registered successfully");
                System.out.println("Publisher registered: " + id);
                
                // Update notification types in case they've changed
                updateNotificationTypes();
            } else {
                showAlert("Error", "Publisher ID already exists");
            }
        } catch (Exception e) {
            showErrorAlert("Registration Error", "Failed to register publisher", e);
        }
    }
    
    private void deregisterPublisher() {
        try {
            if (service.deregisterPublisher(publisherId)) {
                isRegistered = false;
                publisherIdField.setDisable(false);
                registerButton.setDisable(false);
                deregisterButton.setDisable(true);
                notificationTypeComboBox.setDisable(true);
                contentTextArea.setDisable(true);
                publishButton.setDisable(true);
                showAlert("Success", "Publisher deregistered successfully");
                System.out.println("Publisher deregistered: " + publisherId);
            } else {
                showAlert("Error", "Failed to deregister publisher");
            }
        } catch (Exception e) {
            showErrorAlert("Deregistration Error", "Failed to deregister publisher", e);
        }
    }
    
    private void publishNotification() {
        if (!isRegistered) {
            showAlert("Error", "Publisher is not registered");
            return;
        }
        
        String type = notificationTypeComboBox.getValue();
        if (type == null || type.isEmpty()) {
            showAlert("Error", "Please select a notification type");
            return;
        }
        
        String content = contentTextArea.getText().trim();
        if (content.isEmpty()) {
            showAlert("Error", "Please enter notification content");
            return;
        }
        
        try {
            Notification notification = new Notification(type, content, publisherId);
            
            if (service.publishNotification(notification)) {
                contentTextArea.clear();
                String displayText = notification.getTimestamp() + " - " + 
                                    notification.getType() + ": " + 
                                    notification.getContent();
                notificationHistoryListView.getItems().add(0, displayText);
                showAlert("Success", "Notification published successfully");
                System.out.println("Published notification: " + type + " - " + content);
            } else {
                showAlert("Error", "Failed to publish notification");
                System.out.println("Failed to publish notification");
            }
        } catch (Exception e) {
            showErrorAlert("Publication Error", "Failed to publish notification", e);
        }
    }
    
    private void updateNotificationTypes() {
        try {
            notificationTypeComboBox.getItems().clear();
            
            java.util.List<String> types = service.getNotificationTypes();
            
            notificationTypeComboBox.getItems().addAll(types);
            if (!notificationTypeComboBox.getItems().isEmpty()) {
                notificationTypeComboBox.setValue(notificationTypeComboBox.getItems().get(0));
            }
        } catch (Exception e) {
            System.err.println("Error updating notification types: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\nError: " + e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 