package Data;

import javafx.scene.image.Image;

import java.beans.Visibility;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Rail implements Runnable, Drawable{

    LinkedBlockingQueue<Message> inbox = new LinkedBlockingQueue<Message>();

    protected String name;
    protected Rail left = null;
    protected Rail right = null;
    protected boolean isLocked = false;
    protected boolean isStation = false;
    protected boolean returningMessage = false;
    protected static int counter = 1;
    private boolean linkedToSwitch = false;
    protected Switch aSwitch;
    private Image myImage = new Image("Resources/rail.png");
    private int myX;
    private int myY;

    public Rail()
    {
        this.name = "Rail" + counter;
        counter++;
    }

    //used to change image to red when locked and black when unlocked
    public void updateImage(){
        if(aSwitch == null){
            if(isLocked){myImage = new Image("Resources/lockedRail.png");}
            else{myImage = new Image("Resources/rail.png");}
        }
        else{
            System.out.println("gucci");
            char c = aSwitch.getType();
            if(c == 'u'){
                if(isLocked){myImage = new Image("Resources/lockedRailWithUpSwitch.png");}
                else{myImage = new Image("Resources/railWithUpSwitch.png");}
            }
            else if(c == 'd'){
                if(isLocked){myImage = new Image("Resources/lockedRailWithDownSwitch.png");}
                else{myImage = new Image("Resources/railWithDownSwitch.png");}
            }
        }
        Visualizer.getInstance().updateImage(this);
    }

    public void receiveMessage(Message m){
        inbox.add(m);
        //System.out.println("Message recieved by " + name);
    }

    //Special constructor used for Stations only
    public Rail(String name){
        isStation = true;
        this.name = name;
    }

    @Override
    public int getX() {
        return myX;
    }

    @Override
    public int getY() {
        return myY;
    }

    @Override
    public Image getImage() {
        return myImage;
    }

    public void setPosition(int x, int y)
    {
        //System.out.println(name + x + y);
        myX = x;
        myY = y;
    }

    public void setLeftRail(Rail r){
        this.left = r;
    }

    public void setRightRail(Rail r){
        this.right = r;
    }

    public void linkSwitch(Switch s){
        linkedToSwitch = true;
        aSwitch = s;
        updateImage();
        System.out.println( name + " attached to switch " + s.getName());
    }

    public Rail getLeftRail()
    {
        return left;
    }

    public Rail getRightRail()
    {
        return right;
    }

    public String getName(){ return name; }

    //Used to begin search for a path to target station.
    //Currently assumes stations have either a left or right neighbor, but not both.
    public void searchPath(Message m){
        //send the message back to the train if its completed
        if(m.isCompletePath()){
            passMessage(m, m.getNextReturnDirection());
        }
        //message is not completed so continue search
        else {
            //this is the target, send message back through system
            if (isStation && name == m.getDestination()) {
                String nextDirection = m.getNextReturnDirection();
                m.addStationToPath(name);
                //Trivial case where train is already on the target station.
                if(nextDirection.equals("train")){m.returnToTrain();}
                else{
                    passMessage(m, nextDirection);
                }
            }
            //search left if possible
            else if (left != null) {
                m.addToPath("l");
                left.receiveMessage(m);
            }
            //search right if possible
            else if (right != null) {
                m.addToPath("r");
                right.receiveMessage(m);
            }
        }
    }

    //pass message m to specified neighbor.
    protected void passMessage(Message m, String whichNeighbor){
        //System.out.println(name + " "+ whichNeighbor);
        if(whichNeighbor.equals("l")){left.receiveMessage(m);}
        else if(whichNeighbor.equals("r")){right.receiveMessage(m);}
        else if(whichNeighbor.equals("dr") || whichNeighbor.equals("ur")){aSwitch.receiveMessage(m);}
        else if(whichNeighbor.equals("dl") || whichNeighbor.equals("ul")){aSwitch.receiveMessage(m);}
        else if(whichNeighbor.equals("train")){ m.returnToTrain(); }
        else if(!(aSwitch == null)){aSwitch.receiveMessage(m);}
    }

    //@ param boolean direction - true for right, false for left.
    protected void searchPathDirectional(Message m, boolean direction){
        //System.out.println(direction);
        //m.printPath();

        Message responseOne = null;
        Message responseTwo = null;
        try {
            //System.out.println(m.getDestination());
            //station found, add self and send message back
            if (isStation && name == m.getDestination()) {
                String nextDirection = m.getNextReturnDirection();
                //System.out.println(name);
                m.addStationToPath(name);
                //Trivial case where train is already on the target station.
                //m.printPath();
                if (nextDirection.equals("train")) {
                    m.returnToTrain();
                } else {
                    passMessage(m, nextDirection);
                }
            }
            //path is searching to the right
            else if (direction) {
                //System.out.println(m.getIsFailed());
                //two possible rights, split message and wait for both
                //System.out.print(name + " " + linkedToSwitch);
                if (linkedToSwitch) {
                    //this rail is the single right neighbor of the linked switch,
                    //no message split neccessary
                    boolean temp = aSwitch.whichSecondNeighbor();
                    //System.out.println(temp);
                    if (temp) {
                        m.addToPath("r");
                        right.receiveMessage(m);
                    }
                    //two possible paths to the right, split message and wait for both responses
                    else {
                        Message split = m.copy();
                        m.addToPath("r");
                        right.receiveMessage(m);
                        responseOne = inbox.take();
                        //System.out.print("r1");
                        //responseOne.printPath();
                        char c = aSwitch.getType();
                        //second path is below
                        if (c == 'u') {
                            //m.printPath();
                            //System.out.println(name + " put me here");
                            split.addToPath("dr");
                        }
                        else{
                            split.addToPath("ur");
                        }
                        aSwitch.receiveMessage(split);
                        responseTwo = inbox.take();
                        //System.out.print("r2");
                        //responseTwo.printPath();

                        returnToSender(compareMessages(responseOne, responseTwo));
                    }
                }
                //searched as far right as possible and did not find target, return fail
                else if (right == null) {
                    //System.out.println(name + " set message to failed");
                    //m.printPath();
                    m.setFailed();
                    passMessage(m, m.getNextReturnDirection());
                }
                //can search farther right, add self and continue search
                else {
                    m.addToPath("r");
                    right.receiveMessage(m);
                }
            }
            //path is searching to the left
            else {
                if (linkedToSwitch) {
                    //System.out.println(name);
                    //this rail is the single left neighbor of the linked switch,
                    //no message split neccessary
                    if (!aSwitch.whichSecondNeighbor()) {
                        m.addToPath("l");
                        left.receiveMessage(m);
                    }
                    //two possible paths to the left, split message and wait for both responses
                    else {
                        //m.printPath();
                        Message split = m.copy();
                        m.addToPath("l");
                        left.receiveMessage(m);
                        responseOne = inbox.take();
                        if(responseOne.hasDifferentTrain(m)){
                            Message temp = responseOne;
                            System.out.println("in use");
                            responseOne = inbox.take();
                            inbox.add(temp);
                        }
                        //System.out.print("r1");
                        //responseOne.printPath();
                        char c = aSwitch.getType();
                        //second path is below
                        if (c == 'u') {
                            split.addToPath("dl");
                        }
                        else{
                            split.addToPath("ul");
                        }
                        aSwitch.receiveMessage(split);
                        responseTwo = inbox.take();
                        //System.out.print("r2");
                        //responseTwo.printPath();
                        //System.out.println(responseTwo.isLockingMessage());

                        Message test = compareMessages(responseOne, responseTwo);
                        //test.printPath();
                        returnToSender(test);
                    }
                }
                //searched as far left as possible and did not find target, return fail
                else if (left == null) {
                    m.setFailed();
                    passMessage(m, m.getNextReturnDirection());
                }
                //can search farther left, add self and continue search
                else{
                    m.addToPath("l");
                    left.receiveMessage(m);
                }
            }
        } catch(InterruptedException e){System.out.println(e);}
    }

    public Message compareMessages(Message one, Message two){
        if(one.getIsFailed()){return two;}
        else if(two.getIsFailed()){return one;}
        else{
            if(one.getPathLength() < two.getPathLength()){
                return one;
            }
            return two;
        }
    }

    protected void returnToSender(Message m){
        String nextDirection = m.getNextReturnDirection();
        //System.out.println(name + nextDirection);
        passMessage(m, nextDirection);
        //m.printPath();
        //System.out.println(nextDirection);
    }


    public void securePath(Message m){
        //rail already in use, wait until it is freed by passing train
        if(isLocked) {
            while (isLocked) {
                try {
                    //send message back to train if applicable
                    if (returningMessage) {
                        returningMessage = false;
                        String returnDirection = m.getNextReturnDirection();
                        //System.out.println(returnDirection);
                        if (returnDirection.equals("l")) {
                            left.setReturningMessage(true);
                            left.receiveMessage(m);
                        } else if (returnDirection.equals("r")) {
                            right.setReturningMessage(true);
                            right.receiveMessage(m);
                        } else if (!(aSwitch == null)){
                            aSwitch.setReturningMessage(true);
                            aSwitch.receiveMessage(m);
                        } else if (returnDirection.equals("train")) {
                            m.returnToTrain();
                        }
                    } else {
                        synchronized (this) {
                            wait(500);
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println("Rail " + name + " interrupted.");
                }
            }
        }
        else {
            //m.printLockPath();
            isLocked = true;
            updateImage();
            System.out.println( name + " locked.");
            String direction = m.getNextRailToLock();
            //System.out.println(direction);
            //path locked, return message to train
            if (direction.equals(m.getDestination())) {
                String returnDirection = m.getNextReturnDirection();
                if (returnDirection.equals("l")) {
                    left.setReturningMessage(true);
                    left.receiveMessage(m);
                } else if (returnDirection.equals("r")) {
                    right.setReturningMessage(true);
                    right.receiveMessage(m);
                } else if (!(aSwitch == null)){
                    aSwitch.setReturningMessage(true);
                    aSwitch.receiveMessage(m);
                } else if (returnDirection.equals("train")) {
                    m.returnToTrain();
                }
            }
            //path being locked
            else {

                //handle bookkeeping for when to return message to train
                if (direction.equals("train")) {
                    m.addToReturnPath("train");
                    direction = m.getNextRailToLock();
                }
                //System.out.println(direction);
                //System.out.println(left.getName());
                //get the direction to lock and send the message there
                if (direction.equals("r")) {
                    m.addToReturnPath("l");
                    right.receiveMessage(m);
                } else if (direction.equals("l")) {
                    m.addToReturnPath("r");
                    left.receiveMessage(m);
                } else if (direction.equals("ul")) {
                    m.addToReturnPath("dr");
                    aSwitch.receiveMessage(m);
                } else if (direction.equals("dl")) {
                    m.addToReturnPath("ur");
                    aSwitch.receiveMessage(m);
                } else if (direction.equals("ur")) {
                    m.addToReturnPath("dl");
                    aSwitch.receiveMessage(m);
                } else if (direction.equals("dr")) {
                    m.addToReturnPath("ul");
                    aSwitch.receiveMessage(m);
                }
            }
        }
    }

    public void setReturningMessage(boolean b){
        this.returningMessage = b;
    }

    public void releaseRail(){
        isLocked = false;
        updateImage();
        System.out.println(name + " released.");
    }

    private void handleMessage(Message m){
        //check first is message is for locking path or searching for path
        if(m.isLockingMessage()){
            //System.out.println(name + " using locking message");
            securePath(m);
        }
        else {
            //m should only be empty if this is the current station the train is on
            //start the search for path
            if (m.isEmpty() && !m.getIsFailed()) {
                //System.out.println(m.isEmpty());
                searchPath(m);
            } else {
                if(m.getIsFailed()){
                    //System.out.println(name + "passed failed message");
                    returnToSender(m);
                }
                else if (!m.isCompletePath()) {
                    //System.out.print(name);
                    //m.printPath();
                    String direction = m.getPathDirection();
                    if (direction.equals("l") || direction.equals("ul") || direction.equals("dl")) {
                        searchPathDirectional(m, false);
                    } else if (direction.equals("r") || direction.equals("ur") || direction.equals("dr")) {
                        searchPathDirectional(m, true);
                    }
                } else {
                    String nextDirection = m.getNextReturnDirection();
                    passMessage(m, nextDirection);
                    //System.out.println(name + " passed to " + nextDirection);
                }
            }
        }
    }

    @Override
    public void run() {
        try{
            //set image to the correct one at start
            updateImage();
            while(true){
                Visualizer.getInstance().updateImage(this);
                //System.out.println("Updated");
                Message inUse = inbox.take();
                //System.out.println(name + " active");
                handleMessage(inUse);
            }
        } catch (InterruptedException e){ System.err.println("Rail " + name + " interrupted."); }
    }

    public boolean isLocked (){ return isLocked; }

    public void lockRail(){
        isLocked = true;
        updateImage();
    }

    public void print(){ System.out.print('=');}

    public Rail getASwitch(){
        return aSwitch;
    }

}
