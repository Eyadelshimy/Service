package launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.PublisherUI;

public class PublisherLauncher extends Application {
    private static int instanceCount = 0;
    private static final int TOTAL_INSTANCES = 3;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start the first instance directly
        new PublisherUI().start(new Stage());
        
        // Start two more instances
        for (int i = 1; i < TOTAL_INSTANCES; i++) {
            Stage newStage = new Stage();
            PublisherUI publisherUI = new PublisherUI();
            publisherUI.start(newStage);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 