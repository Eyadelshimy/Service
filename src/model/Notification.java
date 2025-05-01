package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String type;
    private String content;
    private LocalDateTime timestamp;
    private String publisher;

    public Notification(String type, String content, String publisher) {
        this.type = type;
        this.content = content;
        this.publisher = publisher;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPublisher() {
        return publisher;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", publisher='" + publisher + '\'' +
                '}';
    }
} 