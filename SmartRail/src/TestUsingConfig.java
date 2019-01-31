import Data.Control;
import Data.Visualizer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TestUsingConfig extends Application
{
    private static Control controller;
    public static void main(String args[])
    {
        launch();
    }

    @Override
    public void start (Stage stage) throws Exception
    {
        BorderPane sp = new BorderPane();
        BorderPane bottomBar = new BorderPane();
        sp.setBottom(bottomBar);
        controller = new Control();
        //Visualizer v = new Visualizer(500,500);
        TextField input = new TextField();
        bottomBar.setCenter(input);
        Button submitInput = new Button("Enter input");
        bottomBar.setRight(submitInput);

        Button start = new Button("Start");
        start.setOnMouseClicked(e -> controller.startTrains());
        sp.setCenter(Visualizer.getInstance().getCanvas());
        sp.setBottom(new HBox(start, Visualizer.getInstance().getText()));
        stage.setScene(new Scene(sp));
        stage.show();
        stage.setResizable(false);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });


    }
}