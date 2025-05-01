package service;

import model.Notification;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BUEService {
    private static BUEService instance;
    
    // Maps notification types to list of subscribers
    private final Map<String, List<String>> subscribers = new ConcurrentHashMap<>();
    
    // List of registered publishers
    private final List<String> publishers = new CopyOnWriteArrayList<>();
    
    // List of available notification types
    private final List<String> notificationTypes = new CopyOnWriteArrayList<>();
    
    // Store notifications for a period of time
    private final List<Notification> recentNotifications = new CopyOnWriteArrayList<>();
    
    // Time to keep notifications (in minutes)
    private final int retentionTimeMinutes = 30;
    
    private BUEService() {
        // Initialize with some default notification types
        notificationTypes.add("INFO");
        notificationTypes.add("WARNING");
        notificationTypes.add("ERROR");
        notificationTypes.add("UPDATE");
        notificationTypes.add("ALERT");
        
        // Start a cleanup thread to remove old notifications
        Timer cleanupTimer = new Timer(true);
        cleanupTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupOldNotifications();
            }
        }, 60000, 60000); // Run every minute
    }
    
    public static synchronized BUEService getInstance() {
        if (instance == null) {
            instance = new BUEService();
        }
        return instance;
    }
    
    public boolean registerPublisher(String publisherId) {
        if (!publishers.contains(publisherId)) {
            publishers.add(publisherId);
            return true;
        }
        return false;
    }
    
    public boolean deregisterPublisher(String publisherId) {
        return publishers.remove(publisherId);
    }
    
    public boolean registerSubscriber(String subscriberId) {
        // Just registering does not subscribe to notifications
        return true;
    }
    
    public boolean deregisterSubscriber(String subscriberId) {
        // Remove subscriber from all notification types
        for (List<String> subscriberList : subscribers.values()) {
            subscriberList.remove(subscriberId);
        }
        return true;
    }
    
    public boolean subscribe(String subscriberId, String notificationType) {
        if (notificationTypes.contains(notificationType)) {
            subscribers.computeIfAbsent(notificationType, k -> new CopyOnWriteArrayList<>());
            if (!subscribers.get(notificationType).contains(subscriberId)) {
                subscribers.get(notificationType).add(subscriberId);
                return true;
            }
        }
        return false;
    }
    
    public boolean unsubscribe(String subscriberId, String notificationType) {
        if (subscribers.containsKey(notificationType)) {
            return subscribers.get(notificationType).remove(subscriberId);
        }
        return false;
    }
    
    public boolean publishNotification(Notification notification) {
        // Accept notification from any registered publisher
        if (publishers.contains(notification.getPublisher()) && 
            notificationTypes.contains(notification.getType())) {
            recentNotifications.add(notification);
            System.out.println("Published notification: " + notification);
            return true;
        }
        System.out.println("Failed to publish notification: " + notification + 
                         ", Publisher registered: " + publishers.contains(notification.getPublisher()) +
                         ", Type exists: " + notificationTypes.contains(notification.getType()));
        return false;
    }
    
    public List<Notification> getNotificationsForSubscriber(String subscriberId) {
        List<Notification> relevantNotifications = new ArrayList<>();
        
        System.out.println("Getting notifications for subscriber: " + subscriberId);
        System.out.println("Total notifications: " + recentNotifications.size());
        System.out.println("Subscriptions: " + subscribers);
        
        for (Notification notification : recentNotifications) {
            String type = notification.getType();
            if (subscribers.containsKey(type) && 
                subscribers.get(type).contains(subscriberId)) {
                relevantNotifications.add(notification);
                System.out.println("Matched notification: " + notification);
            }
        }
        
        return relevantNotifications;
    }
    
    public List<String> getNotificationTypes() {
        return new ArrayList<>(notificationTypes);
    }
    
    public boolean addNotificationType(String type) {
        if (!notificationTypes.contains(type)) {
            notificationTypes.add(type);
            return true;
        }
        return false;
    }
    
    public List<String> getPublishers() {
        return new ArrayList<>(publishers);
    }
    
    public Map<String, List<String>> getSubscribers() {
        Map<String, List<String>> subscribersCopy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : subscribers.entrySet()) {
            subscribersCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return subscribersCopy;
    }
    
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(recentNotifications);
    }
    
    private void cleanupOldNotifications() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(retentionTimeMinutes);
        recentNotifications.removeIf(notification -> notification.getTimestamp().isBefore(cutoffTime));
    }
} 