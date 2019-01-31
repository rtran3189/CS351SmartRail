package Data;

import javafx.scene.image.Image;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Train implements Runnable, Drawable {
    private Rail currentRail;
    private Station destination;
    private boolean hasPath = false;
    private boolean pathSecured = false;
    private LinkedBlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    LinkedList<String> path = new LinkedList<>();
    private int myX;
    private int myY;
    private Image myImage;

    /**
     * Constructor that creates a train on a designated rail (has to be on a STATION rail though)
     * @param startingStation the station it starts on
     */
    public Train (Station startingStation)
    {
        currentRail = startingStation;
        startingStation.lockRail();
        myImage = new Image("Resources/train.png");
    }

    @Override
    public void run() {
        Visualizer.getInstance().updateImage(this);
        Message whereTo = new Message(destination.getName(), this);
        whereTo.addToPath("train");
        try {
//            System.out.println("Current Rail: " + currentRail.getName());
            while (true) {
                if(whereTo.isEmpty()){
                    whereTo = new Message(destination.getName(), this);
                    whereTo.addToPath("train");
                    currentRail.receiveMessage(whereTo);
                }
                //wait for path message to come back
                if(!hasPath) {
                    whereTo = inbox.take();
                    hasPath = true;
                }
                if(path.isEmpty()){
                    path = whereTo.getPath(); }
                System.out.print("Train ");
                printPath(path);
                //failed to find path, kill the thread
                if (!path.isEmpty() && path.peek().equals("failed")) {
                    System.err.println("Failed to find path to target");
                    Visualizer.getInstance().setText("Failed to find path");
                    hasPath = false;
                    break;
                }
                //path found, secure path and begin moving
                else if (!path.isEmpty() && path.getLast() == destination.getName()) {
                    //printPath(path);
                    if(!pathSecured){
                        whereTo.copyPathToLockPath();
                        whereTo.setLockingMessage(true);
                        currentRail.receiveMessage(whereTo);
                        whereTo = inbox.take();
                        Visualizer.getInstance().setText("Path found and secured");
                        pathSecured = true;
                    }
                    String next = path.pop();
                    //System.out.print(next);
                    if(next.equals("train")){}
                    else if(next.equals("r")){
                        Rail previousRail = currentRail;
                        System.out.println(currentRail.getName());
                        currentRail = currentRail.getRightRail();
                        previousRail.releaseRail();
                    }
                    else if(next.equals("l")){
                        Rail previousRail = currentRail;
                        System.out.println(currentRail.getName());
                        currentRail = currentRail.getLeftRail();
                        previousRail.releaseRail();
                    }
                    else if(next.equals("ul") || next.equals("dl")){
                        Rail previousRail = currentRail;
                        currentRail = currentRail.getASwitch();
                        previousRail.releaseRail();
                    }
                    else if(next.equals("ur") || next.equals("dr")){
                        Rail previousRail = currentRail;
                        currentRail = currentRail.getASwitch();
                        System.out.println(currentRail.getName());
                        previousRail.releaseRail();
                    }
                    //arrived at station, reset and wait until notified of destination change
                    else if(path.isEmpty()) synchronized (this){
                        currentRail.releaseRail();
                        hasPath = false;
                        pathSecured = false;
                        System.out.println("Arrived at " + currentRail.getName());
                        break;
                    }
                    //wait period before moving to next rail
                    synchronized (this) {
                        //System.out.println("Waiting to move again");
                        wait(250);
                    }
                } else {
                    //haven't found full path yet, wait a second and retry
                    synchronized (this) {
                        System.out.println("Waiting for path");
                        wait(1000);
                    }
                }
            }
        }
        //possibly deleted train.
        catch (InterruptedException e){ System.err.println("Train interrupted."); }
    }

    /**
     * Sets the destination of the train.
     * @param d the Station rail it wants to go to.
     */
    public void setDestination(Station d)
    {
        destination = d;
    }

    /**
     * Moves the train to the designated rail.
     * @param rail
     */
    public void moveToRail(Rail rail)
    {
        currentRail = rail;
    }

    /**
     * @return The current rail the train is on.
     */
    public Rail getCurrentRail()
    {
        return currentRail;
    }

    public void printPath(LinkedList<String> path){
        System.out.print("Path: ");
        for(String s : path){ System.out.print(s + ' ');}
        System.out.println();
    }

    public void receiveMessage(Message m){
        inbox.add(m);
    }

    public void setPathSecured(boolean b){
        pathSecured = b;
    }

    @Override
    public int getX() {
        return currentRail.getX();
    }

    @Override
    public int getY() {
        return currentRail.getY();
    }

    @Override
    public Image getImage() {
        return myImage;
    }
}
