package Data;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Visualizer
{
    //The tile size constant, change when needed.
    public static final int TILE_SIZE = 50;

    private Image[][] ImageGrid;

    private Canvas canvas;
    private Text mText;
    private static Visualizer instance;

    public static Visualizer getInstance()
    {
        return instance;
    }

    public Visualizer(int width, int height)
    {
        canvas = new Canvas(width * TILE_SIZE, height * TILE_SIZE);
        mText = new Text("");
        mText.setFont(new Font(18));

        System.out.println("width "+ width + " height " + height);
        ImageGrid = new Image[height][width];
        instance = this;

        final Timeline t = new Timeline(new KeyFrame(Duration.seconds(1d/60d), e -> draw()));
        t.setCycleCount(Timeline.INDEFINITE);
        t.playFromStart();
    }

    /**
     * Set text value indicating pathfinding success or failure
     * @param s Text to be displayed to user
     */
    public void setText(final String s)
    {
        if(s.equals("Failed")) mText.setFill(Color.RED);
        else mText.setFill(Color.GREEN);
        mText.setText(s);
    }

    /**
     * Get text value used for pathfinding success/failure
     * @return text showing success/failure.
     */
    public Text getText() { return mText; }

    /**
     * @return the canvas
     */
    public Node getCanvas()
    {
        return canvas;
    }

    /**
     * Updates the image depending on the type of the object.
     * @param d
     */
    public void updateImage(Drawable d)
    {
        //System.out.println("pinged " + d.getY() + " " + d.getX());
        ImageGrid[d.getY()][d.getX()] = d.getImage();
        System.out.println("Something was added to [" + d.getY() + "][" + d.getX() +"]");
    }


    /**
     * Draws the image onto the canvas with appropriate spacing.
     */
    private void draw()
    {
        Image testImgTrain = new Image("Resources/train.png");
        Image testImgRail = new Image("Resources/rail.png");
        Image testImgStation = new Image("Resources/station.png");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //gc.drawImage(testImgTrain,0,0);
        //gc.drawImage(testImgRail,50,0);
        //gc.drawImage(testImgStation,100,0);
        for(int i = 0; i < ImageGrid.length; i++)
        {
            for(int j = 0; j < ImageGrid[0].length; j++)
            {
                //System.out.println("i "+ i + " j " + j);
                //trackAndStationGrid[i][j] = testImgRail;
                int xTile = j * TILE_SIZE;
                int yTile = i * TILE_SIZE;
                gc.drawImage(ImageGrid[i][j], xTile, yTile);
            }
        }
    }
}