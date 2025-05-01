# BUE Event Notification System

A Publish-Subscribe architecture-based application for event notifications.

## Overview

This system implements a publish-subscribe pattern with the following components:

1. **BUE Service**: Central service that manages publishers, subscribers, and notifications.
2. **Publishers**: Applications that register with the service and publish notifications.
3. **Subscribers**: Applications that register with the service and receive notifications based on their subscriptions.

## Implementation Options

The system provides two implementation options:

### Local Implementation (Non-RMI)
- All components run in the same JVM
- Communication through direct method calls
- Suitable for testing and development

### RMI Implementation
- Components can run in separate JVMs or on different machines
- Communication through Java Remote Method Invocation (RMI)
- Better represents a true distributed system

## Components

- **Model**: Contains the Notification class.
- **Service**: Implements the BUEService singleton and RMI service.
- **UI**: JavaFX user interfaces for the service, publishers, and subscribers.
- **Launcher**: Classes to start multiple instances of each component.
- **RMI**: Interfaces and implementations for remote communication.

## How to Run

### Non-RMI Version:
1. **Main Launcher**: Run `launcher.MainLauncher` to start a launcher application that allows you to start all components.
2. **Individual Components**:
   - Service: Run `launcher.BUEServiceLauncher` to start the BUE Service.
   - Publishers: Run `launcher.PublisherLauncher` to start three publisher applications.
   - Subscribers: Run `launcher.SubscriberLauncher` to start three subscriber applications.

### RMI Version:
1. **RMI Main Launcher**: Run `launcher.RMIMainLauncher` to start a launcher application for RMI components.
2. **RMI Service**: Run `launcher.RMIServiceLauncher` to start the RMI service.
3. **Individual Components**:
   - The publisher and subscriber launchers will connect to the RMI service.

## Using the System

1. **Start the BUE Service** first.
2. **Start Publishers**:
   - Enter a unique Publisher ID and click "Register".
   - Select a notification type and enter content.
   - Click "Publish" to send a notification.
3. **Start Subscribers**:
   - Enter a unique Subscriber ID and click "Register".
   - Select notification types from the list and click "Subscribe to Selected".
   - Notifications matching your subscriptions will appear in the bottom panel.

## Default Notification Types

The system comes with default notification types:
- INFO
- WARNING
- ERROR
- UPDATE
- ALERT

You can add more notification types through the BUE Service UI.

## Features

- Publishers can register/deregister with the service.
- Subscribers can register/deregister and subscribe to specific notification types.
- Notifications are stored for a configurable retention time (default: 30 minutes).
- Real-time updates of notification delivery.
- Multiple publishers and subscribers can run simultaneously.
- With RMI, components can run on different machines across a network.

## Requirements

- Java 8 or higher
- JavaFX (included in JDK 8, separate library in newer Java versions)

## VM Arguments

If running with Java 11 or newer, add these VM arguments to each run configuration:
```
--module-path "path\to\your\javafx\lib" --add-modules javafx.controls,javafx.fxml
```

Replace "path\to\your\javafx\lib" with the actual path to your JavaFX SDK's lib folder. 