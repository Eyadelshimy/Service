package rmi;

import model.Notification;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BUEServiceImpl extends UnicastRemoteObject implements BUEServiceInterface {
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

    public BUEServiceImpl() throws RemoteException {
        super();
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
        
        System.out.println("BUE Service started with RMI");
    }
    
    @Override
    public boolean registerPublisher(String publisherId) throws RemoteException {
        if (!publishers.contains(publisherId)) {
            publishers.add(publisherId);
            System.out.println("Publisher registered: " + publisherId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean deregisterPublisher(String publisherId) throws RemoteException {
        boolean result = publishers.remove(publisherId);
        if (result) {
            System.out.println("Publisher deregistered: " + publisherId);
        }
        return result;
    }
    
    @Override
    public boolean registerSubscriber(String subscriberId) throws RemoteException {
        // Just registering does not subscribe to notifications
        System.out.println("Subscriber registered: " + subscriberId);
        return true;
    }
    
    @Override
    public boolean deregisterSubscriber(String subscriberId) throws RemoteException {
        // Remove subscriber from all notification types
        boolean removed = false;
        for (List<String> subscriberList : subscribers.values()) {
            if (subscriberList.remove(subscriberId)) {
                removed = true;
            }
        }
        if (removed) {
            System.out.println("Subscriber deregistered: " + subscriberId);
        }
        return true;
    }
    
    @Override
    public boolean subscribe(String subscriberId, String notificationType) throws RemoteException {
        if (notificationTypes.contains(notificationType)) {
            subscribers.computeIfAbsent(notificationType, k -> new CopyOnWriteArrayList<>());
            if (!subscribers.get(notificationType).contains(subscriberId)) {
                subscribers.get(notificationType).add(subscriberId);
                System.out.println("Subscriber " + subscriberId + " subscribed to " + notificationType);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean unsubscribe(String subscriberId, String notificationType) throws RemoteException {
        if (subscribers.containsKey(notificationType)) {
            boolean result = subscribers.get(notificationType).remove(subscriberId);
            if (result) {
                System.out.println("Subscriber " + subscriberId + " unsubscribed from " + notificationType);
            }
            return result;
        }
        return false;
    }
    
    @Override
    public boolean publishNotification(Notification notification) throws RemoteException {
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
    
    @Override
    public List<Notification> getNotificationsForSubscriber(String subscriberId) throws RemoteException {
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
    
    @Override
    public List<String> getNotificationTypes() throws RemoteException {
        return new ArrayList<>(notificationTypes);
    }
    
    @Override
    public boolean addNotificationType(String type) throws RemoteException {
        if (!notificationTypes.contains(type)) {
            notificationTypes.add(type);
            System.out.println("Added notification type: " + type);
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> getPublishers() throws RemoteException {
        return new ArrayList<>(publishers);
    }
    
    @Override
    public Map<String, List<String>> getSubscribers() throws RemoteException {
        Map<String, List<String>> subscribersCopy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : subscribers.entrySet()) {
            subscribersCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return subscribersCopy;
    }
    
    @Override
    public List<Notification> getAllNotifications() throws RemoteException {
        return new ArrayList<>(recentNotifications);
    }
    
    private void cleanupOldNotifications() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(retentionTimeMinutes);
        recentNotifications.removeIf(notification -> notification.getTimestamp().isBefore(cutoffTime));
    }
} 