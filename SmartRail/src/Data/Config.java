package Data;
import java.io.IOException;

public class Config {

    private MapReader mapReader;

    public Config()
    {
        try {
            mapReader = new MapReader("Resources/railMap.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public Rail[][] getMapGrid()
    {
        Rail[][] mapG = mapReader.getRailGrid();
        return mapG;
    }
}
