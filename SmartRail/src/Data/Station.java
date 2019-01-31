package Data;

import javafx.scene.image.Image;

public class Station extends Rail implements Drawable{

    private Image myImage = new Image("Resources/station.png");
    private int myX;
    private int myY;

    public Station (String name) {
        super(name);
    }

    public Station (int y, int x, String name)
    {
        super(name);
        myX = x;
        myY = y;
    }


    //place a new train on the station and return a reference to the train
    public Train placeTrain(){
        Train t = new Train(this);
        this.lockRail();
        return t;
    }

    //remove a train.
    public void removeTrain(){}

    public void print(){
        System.out.print(name);
        if(right == null){System.out.println();}
    }

    @Override
    public Image getImage() {
        return myImage;
    }

    @Override
    public int getX() {
        return myX;
    }

    @Override
    public int getY() {
        return myY;
    }
}
