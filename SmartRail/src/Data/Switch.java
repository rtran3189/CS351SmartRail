package Data;

import javafx.scene.image.Image;

import java.util.ArrayList;

public class Switch extends Rail implements Drawable {

    private Rail secondRight;
    private Rail secondLeft;
    private Image myImage;
    //set to 'u' if switch allows movement to line above.
    //set to 'd' if switch allows movement to the line below.
    //Assume switch can only link either up or down.
    private char whichType;
    private static int counter = 1;

    public void updateImage(){
        if(whichType == 'u'){
            if(isLocked){
                myImage = new Image("Resources/lockedSwitchUp.png");
            }
            else{
                myImage = new Image("Resources/SwitchUp.png");
            }
        }
        else if(whichType == 'd'){
            if(isLocked){
                myImage = new Image("Resources/lockedSwitchDown.png");
            }
            else{
                myImage = new Image("Resources/SwitchDown.png");
            }
        }
        Visualizer.getInstance().updateImage(this);
    }

    public Switch(char type){
        this.name = "Switch" + counter;
        this.whichType = type;
        counter++;
    }

    @Override
    public Image getImage() {
        return myImage;
    }

    public void setSecondLeft(Rail secondLeft) {
        this.secondLeft = secondLeft;
        secondLeft.linkSwitch(this);
    }

    public void setSecondRight(Rail secondRight) {
        this.secondRight = secondRight;
        secondRight.linkSwitch(this);
    }

    public ArrayList<Rail> getLeftRails(){
        ArrayList rails = new ArrayList();
        rails.add(left);
        rails.add(secondLeft);
        return rails;
    }

    public ArrayList<Rail> getRightRails(){
        ArrayList rails = new ArrayList();
        rails.add(right);
        rails.add(secondRight);
        return rails;
    }

    //@Override
    public void securePath(Message m){
        //rail already in use, wait until it is freed by passing train
        if(isLocked) {
            while (isLocked) {
                try {
                    //send message back to train if applicable
                    if (returningMessage) {
                        returningMessage = false;
                        String returnDirection = m.getNextReturnDirection();
                        if (returnDirection.equals("l")) {
                            left.setReturningMessage(true);
                            left.receiveMessage(m);
                        } else if (returnDirection.equals("r")) {
                            right.setReturningMessage(true);
                            right.receiveMessage(m);
                        } else if (returnDirection.equals("dl") || returnDirection.equals("ul")){
                            secondLeft.setReturningMessage(true);
                            secondLeft.receiveMessage(m);
                        }
                        else if(returnDirection.equals("ur") || returnDirection.equals("dr")){
                            secondRight.setReturningMessage(true);
                            secondRight.receiveMessage(m);
                        }
                        else if (returnDirection.equals("train")) {
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
        //lock this path and continue locking until station is reached.
        else {
            //m.printLockPath();
            isLocked = true;
            updateImage();
            System.out.println(name + " locked.");
            String direction = m.getNextRailToLock();
            //System.out.println(direction);
            //path being locked
            //handle bookkeeping for when to return message to train
            if (direction.equals("train")) {
                m.addToReturnPath("train");
                direction = m.getNextRailToLock();
            }
            if (direction.equals(m.getDestination())) {
                String returnDirection = m.getNextReturnDirection();
                //System.out.println(returnDirection);
                if (returnDirection.equals("l")) {
                    left.setReturningMessage(true);
                    left.receiveMessage(m);
                } else if (returnDirection.equals("r")) {
                    right.setReturningMessage(true);
                    right.receiveMessage(m);
                } else if (returnDirection.equals("train")) {
                    m.returnToTrain();
                }
            }
            else {
                //get the direction to lock and send the message there
                if (direction.equals("r")) {
                    m.addToReturnPath("l");
                    right.receiveMessage(m);
                } else if (direction.equals("l")) {
                    m.addToReturnPath("r");
                    left.receiveMessage(m);
                } else if (direction.equals("ul")) {
                    m.addToReturnPath("dr");
                    secondLeft.receiveMessage(m);
                } else if (direction.equals("dl")) {
                    m.addToReturnPath("ur");
                    secondLeft.receiveMessage(m);
                } else if (direction.equals("ur")) {
                    m.addToReturnPath("dl");
                    secondRight.receiveMessage(m);
                } else if (direction.equals("dr")) {
                    m.addToReturnPath("ul");
                    secondRight.receiveMessage(m);
                }
            }
        }
    }

    //@Override
    protected void searchPathDirectional(Message m, boolean direction){
        try {
            Message responseOne = null;
            Message responseTwo = null;
            Message copy = m.copy();
            if (direction) {
                //search right normally
                if (!(right == null)) {
                    super.searchPathDirectional(m, direction);
                    //System.out.println("waiting for response 1");
                    responseOne = inbox.take();
                    //responseOne.printPath();
                }
                //have to split message and wait for both responses.
                if (!(secondRight == null)) {
                    copy.addToPath(whichType + "r");
                    secondRight.receiveMessage(copy);
                    //System.out.println("waiting for response 2");
                    responseTwo = inbox.take();
                    //responseTwo.printPath();
                }
                //responseOne.printPath();
                //responseTwo.printPath();
                if (responseTwo == null) {
                    returnToSender(responseOne);
                } else if (responseOne == null) {
                    returnToSender(responseTwo);
                }
                //decide between the two responses
                else {
                    //response one leads nowhere, send back the second message
                    if (responseOne.getIsFailed()) {
                        returnToSender(responseTwo);
                    }
                    //response two leads nowhere, send back first message
                    else if (responseTwo.getIsFailed()) {
                        returnToSender(responseOne);
                    }
                    //neither message is failed, send back shorter path
                    else {
                        //responseOne.printPath();
                        //responseTwo.printPath();
                        if (responseOne.isCompletePath()) {
                            returnToSender(responseOne);
                        } else {
                            returnToSender(responseTwo);
                        }
                    }
                }
            } else {
                //search left normally
                if(!(left == null)){
                    super.searchPathDirectional(m, direction);
                    responseOne = inbox.take();
                    //System.out.println("r1:");
                    //responseOne.printPath();
                }
                //have to split message
                if(!(secondLeft == null)){
                    copy.addToPath(whichType + "l");
                    secondLeft.receiveMessage(copy);
                    responseTwo = inbox.take();
                    //System.out.println("r2:");
                    //responseTwo.printPath();
                }
                //only have one response, send that back
                if (responseTwo == null) {
                    returnToSender(responseOne);
                } else if (responseOne == null) {
                    returnToSender(responseTwo);
                }
                //decide between the two responses
                else {
                    //response one leads nowhere, send back the second message
                    if (responseOne.getIsFailed()) {
                        returnToSender(responseTwo);
                    }
                    //response two leads nowhere, send back first message
                    else if (responseTwo.getIsFailed()) {
                        returnToSender(responseOne);
                    }
                    //neither message is failed, send back better path.
                    else {
                        if (responseOne.isCompletePath()) {
                            returnToSender(responseOne);
                        } else {
                            returnToSender(responseTwo);
                        }
                    }
                }
            }
        }
        catch(InterruptedException e){System.err.print(e);}
    }

    protected void passMessage(Message m, String whichNeighbor){
        //System.out.println(name + " "+ whichNeighbor);
        if(whichNeighbor.equals("l")){left.receiveMessage(m);}
        else if(whichNeighbor.equals("r")){right.receiveMessage(m);}
        else if(whichNeighbor.equals("train")){ m.returnToTrain(); }
        else if(whichNeighbor.equals("ur") || whichNeighbor.equals("dr")){secondRight.receiveMessage(m);}
        else if(whichNeighbor.equals("dl") || whichNeighbor.equals("ul")){secondLeft.receiveMessage(m);}
    }

    public void print(){
        if(secondLeft == null){System.out.print('/');}
        else{System.out.print('\\');}
    }

    //return false if this switch has two left neighbors,
    //return true if this switch has two right neighbors
    public boolean whichSecondNeighbor(){
        if(secondLeft == null){ return true ;}
        return false;
    }

    public String getSecondNeighborName(){
        if(secondLeft == null){ return secondRight.getName(); }
        return secondLeft.getName();
    }

    public char getType(){
        return whichType;
    }

    public static char getType(Switch s){ return s.getType(); }

    /**Used only for train movement*/
    public Rail getSecondRight(){
        return secondRight;
    }

    public Rail getSecondLeft(){
        return secondLeft;
    }

    public Rail getASwitch(){
        if(secondLeft == null){return secondRight;}
        return secondLeft;
    }
}
