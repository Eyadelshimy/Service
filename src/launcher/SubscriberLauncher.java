package launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.SubscriberUI;

public class SubscriberLauncher extends Application {
    private static final int TOTAL_INSTANCES = 3;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start the first instance directly
        new SubscriberUI().start(new Stage());
        
        // Start two more instances
        for (int i = 1; i < TOTAL_INSTANCES; i++) {
            Stage newStage = new Stage();
            SubscriberUI subscriberUI = new SubscriberUI();
            subscriberUI.start(newStage);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 