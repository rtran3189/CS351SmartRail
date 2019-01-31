package Data;

import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.LinkedList;

public class Control
{
    //The sizes of the images, change these as needed.
    private int TILE_WIDTH = 50;
    private int TILE_HEIGHT = 50;

    private Station stationGrid[][];
    private LinkedList<Train> trainList = new LinkedList<>();
    private Train lastTrain = null;
    private MapReader reader;

    public Control()
    {
        try {
            reader = new MapReader("Resources/railMap.txt");
        } catch (IOException e) {
            System.out.println("file not found");
        }
        //new Visualizer(reader.getColSize(), reader.getRowSize()-1);
        stationGrid = reader.getStationGrid();


        Visualizer.getInstance().getCanvas().setOnMouseClicked(this::onClick);

    }

    /**
     * starts all trains in the Linked List
     */
    public void startTrains()
    {
        trainList.forEach(Train::run);
    }

    //Some stuff for clicking to move trains around, not done.
    //TODO: get this working if possible.
    private void onClick(MouseEvent mouseEvent)
    {
        int x = (int)(mouseEvent.getX() / TILE_WIDTH);
        int y = (int)(mouseEvent.getY() / TILE_HEIGHT);
        Station station = stationGrid[y][x];

        if(station == null) return;
        if(lastTrain != null)
        {
            lastTrain.setDestination(station);
        }
        Train train = new Train(station);
        lastTrain = train;
        trainList.add(train);
        station.placeTrain();
    }

}
