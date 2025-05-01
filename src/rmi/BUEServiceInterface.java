package rmi;

import model.Notification;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote interface for the BUE Service
 */
public interface BUEServiceInterface extends Remote {
    
    /**
     * Register a publisher with the service
     * @param publisherId The unique ID of the publisher
     * @return true if registration was successful
     */
    boolean registerPublisher(String publisherId) throws RemoteException;
    
    /**
     * Deregister a publisher from the service
     * @param publisherId The unique ID of the publisher
     * @return true if deregistration was successful
     */
    boolean deregisterPublisher(String publisherId) throws RemoteException;
    
    /**
     * Register a subscriber with the service
     * @param subscriberId The unique ID of the subscriber
     * @return true if registration was successful
     */
    boolean registerSubscriber(String subscriberId) throws RemoteException;
    
    /**
     * Deregister a subscriber from the service
     * @param subscriberId The unique ID of the subscriber
     * @return true if deregistration was successful
     */
    boolean deregisterSubscriber(String subscriberId) throws RemoteException;
    
    /**
     * Subscribe to a notification type
     * @param subscriberId The unique ID of the subscriber
     * @param notificationType The type of notification to subscribe to
     * @return true if subscription was successful
     */
    boolean subscribe(String subscriberId, String notificationType) throws RemoteException;
    
    /**
     * Unsubscribe from a notification type
     * @param subscriberId The unique ID of the subscriber
     * @param notificationType The type of notification to unsubscribe from
     * @return true if unsubscription was successful
     */
    boolean unsubscribe(String subscriberId, String notificationType) throws RemoteException;
    
    /**
     * Publish a notification to the service
     * @param notification The notification to publish
     * @return true if publication was successful
     */
    boolean publishNotification(Notification notification) throws RemoteException;
    
    /**
     * Get notifications for a subscriber
     * @param subscriberId The unique ID of the subscriber
     * @return List of notifications for the subscriber
     */
    List<Notification> getNotificationsForSubscriber(String subscriberId) throws RemoteException;
    
    /**
     * Get all available notification types
     * @return List of notification types
     */
    List<String> getNotificationTypes() throws RemoteException;
    
    /**
     * Add a new notification type
     * @param type The new notification type
     * @return true if the type was added successfully
     */
    boolean addNotificationType(String type) throws RemoteException;
    
    /**
     * Get all registered publishers
     * @return List of publisher IDs
     */
    List<String> getPublishers() throws RemoteException;
    
    /**
     * Get all subscribers by notification type
     * @return Map of notification types to subscriber lists
     */
    Map<String, List<String>> getSubscribers() throws RemoteException;
    
    /**
     * Get all notifications in the system
     * @return List of all notifications
     */
    List<Notification> getAllNotifications() throws RemoteException;
} 