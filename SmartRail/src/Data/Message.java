package Data;

import java.util.LinkedList;

public class Message {

    private boolean isLockingMessage = false;
    private LinkedList<String> path = new LinkedList<>();
    private Train creator;
    private boolean isFailed = false;
    //used to return messages
    private LinkedList<String> returnPath = new LinkedList<>();
    //used to work with the path without editing the actual found path
    private LinkedList<String> lockPath = new LinkedList<>();
    private String destination;


    public Message(String destination, Train creator){
        this.destination = destination;
        this.creator = creator;
    }

    /**Create and return a copy of this message.
     * Used for message splitting.*/
    public Message copy(){
        Message m = new Message(this.destination, this.creator);
        m.setPath(copyPath(path));
        m.setReturnPath(copyPath(returnPath));
        m.setLockPath(copyPath(lockPath));
        return m;
    }

    //used for message copying
    private void setPath(LinkedList<String> path){
        this.path = path;
    }

    private void setReturnPath(LinkedList<String> returnPath){
        this.returnPath = returnPath;
    }

    private void setLockPath(LinkedList<String> lockPath){
        this.lockPath = lockPath;
    }

    private LinkedList<String> copyPath(LinkedList<String> pathToCopy){
        LinkedList<String> pathCopy = new LinkedList<>();
        for(String s : pathToCopy){
            pathCopy.add(s);
        }
        return pathCopy;
    }

    public synchronized void addToPath(String direction){
        path.add(direction);
        if(direction.equals("train")){
            returnPath.add("train");
        }
        else if(direction.equals("r")){
            returnPath.add("l");
        }
        else if(direction.equals("l")){
            returnPath.add("r");
        }
        else if(direction.equals("dr")){
            returnPath.add("ul");
        }
        else if(direction.equals("dl")){
            returnPath.add("ur");
        }
        else if(direction.equals("ur")){
            returnPath.add("dl");
        }
        else if(direction.equals("ul")){
            returnPath.add("dr");
        }
    }

    public synchronized void addStationToPath(String stationName){
        path.addLast(stationName);
    }

    //returns a copy of the found path
    public LinkedList<String> getPath() {
        LinkedList<String> pathCopy = new LinkedList<>();
        for(int i = 0; i < path.size(); i++){
            pathCopy.addLast(path.get(i));
        }
        return pathCopy;
    }

    public synchronized String getNextReturnDirection(){
        //printReturnPath();
        return returnPath.removeLast();
    }

    public synchronized String getPathDirection(){return path.getLast();}

    public boolean isCompletePath(){
        if(!path.isEmpty() && path.peek().equals("failed")){return true;}
        if(!path.isEmpty() && path.getLast().equals(destination)){ return true; }
        return false;
    }

    public String getDestination(){ return destination; }

    //message is "empty" if its head is "train"
    public boolean isEmpty(){
        if(path.isEmpty()){return true;}
        if(path.getLast().equals("train")){return true;}
        return false;
    }

    //failed to find target, set message to reflect this
    public synchronized void setFailed(){
        path.clear();
        path.add("failed");
        isFailed = true;
    }

    public boolean getIsFailed(){
        return isFailed;
    }

    public synchronized void printPath(){
        System.out.print("Path: ");
        for(String s : path){ System.out.print(s + ' ');}
        System.out.println();
    }

    public synchronized void printReturnPath(){
        System.out.print("ReturnPath: ");
        for(String s : returnPath){ System.out.print(s + ' ');}
        System.out.println();
    }

    public synchronized void printLockPath(){
        System.out.print("LockPath: ");
        for(String s : lockPath){ System.out.print(s + ' ');}
        System.out.println();
    }

    /** Set message to indicate rails need to lock a path (true)
     *  or message is being used to find a path (false) */
    public void setLockingMessage(boolean isLockingMessage){
        this.isLockingMessage = isLockingMessage;
    }

    public boolean isLockingMessage(){ return isLockingMessage; }

    /**used at the start of locking a path*/
    public void copyPathToLockPath(){
        lockPath = getPath();
    }

    public String getNextRailToLock(){return lockPath.pop();}

    public void addToReturnPath(String s){
        returnPath.addLast(s);
    }

    /**Used exclusively to re-add final station to lock path so that rails know to send message to train*/
    public void addToLockPath(String s){
        lockPath.addFirst(s);
    }

    public void returnToTrain(){
        creator.receiveMessage(this);
    }

    public int getPathLength(){
        return path.size();
    }

    private Train getCreator(){ return creator; }

    public boolean hasDifferentTrain(Message m){
        if(m.getCreator() == creator){return true;}
        return false;
    }
}
