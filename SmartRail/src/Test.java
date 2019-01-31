import Data.*;

import java.util.LinkedList;

public class Test {

    static Station leftSideUp = new Station("leftUp");
    static Station rightSideUp = new Station("rightUp");
    static Station leftSideDown = new Station("leftDn");
    static Station rightSideDown = new Station("rightDn");
    static Train t = leftSideUp.placeTrain();
 //   static Train r = rightSideUp.placeTrain();

    public static void main(String[] args){
        LinkedList<Rail> map = createMapWithSwitches();
        printMap(map);
        for(int i = 0; i < map.size(); i++){
            Thread t = new Thread(map.get(i), map.get(i).getName());
            t.start();
        }
        t.setDestination(rightSideDown);
   //     r.setDestination(leftSideDown);
        while(true){
            Thread singleTrain = new Thread(t);
//            Thread otherTrain = new Thread(r);
            singleTrain.start();
            //otherTrain.start();
            try {
                singleTrain.join();
              //  otherTrain.join();
            }
            catch(InterruptedException e){ System.err.println("Something is borked."); }
            if(t.getCurrentRail() == leftSideUp){ t.setDestination(rightSideDown); }
            else if (t.getCurrentRail() == rightSideDown){ t.setDestination(leftSideDown); }
            else if(t.getCurrentRail() == leftSideDown){t.setDestination(rightSideUp);}
            else if(t.getCurrentRail() == rightSideUp){t.setDestination(leftSideUp);}
        }
    }

    private static LinkedList<Rail> createBasicMap(){
        LinkedList<Rail> map = new LinkedList<>();
        Rail one = new Rail();
        Rail two = new Rail();
        Rail three = new Rail();
        Rail four = new Rail();
        Rail five = new Rail();
        leftSideUp.setRightRail(one);
        one.setLeftRail(leftSideUp);
        one.setRightRail(two);
        two.setLeftRail(one);
        two.setRightRail(three);
        three.setLeftRail(two);
        three.setRightRail(four);
        four.setLeftRail(three);
        four.setRightRail(five);
        five.setLeftRail(four);
        five.setRightRail(rightSideUp);
        rightSideUp.setLeftRail(five);
        map.add(leftSideUp);
        map.add(one);
        map.add(two);
        map.add(three);
        map.add(four);
        map.add(five);
        map.add(rightSideUp);
        return map;
    }

    private static LinkedList<Rail> createMapWithSwitches(){
        LinkedList<Rail> map = new LinkedList<>();
        map = createBasicMap();

        //create second line of rails and link them
        Rail one = new Rail();
        Switch upLeft = new Switch('u');
        Rail three = new Rail();
        Switch upRight = new Switch('u');
        Rail five = new Rail();
        leftSideDown.setRightRail(one);
        one.setLeftRail(leftSideDown);
        one.setRightRail(upLeft);
        upLeft.setLeftRail(one);
        upLeft.setRightRail(three);
        three.setLeftRail(upLeft);
        three.setRightRail(upRight);
        upRight.setLeftRail(three);
        upRight.setRightRail(five);
        five.setLeftRail(upRight);
        five.setRightRail(rightSideDown);
        rightSideDown.setLeftRail(five);


        upRight.setSecondRight(map.get(4));
        //System.out.println("upright attatched to " + map.get(4).getName());
        upLeft.setSecondLeft(map.get(2));

        map.add(leftSideDown);
        map.add(one);
        map.add(upLeft);
        map.add(three);
        map.add(upRight);
        map.add(five);
        map.add(rightSideDown);

        return map;
    }

    private static void printMap(LinkedList<Rail> map){
        for (int i = 0; i < map.size(); i++) {
            Rail r = map.get(i);
            r.print();
        }
        System.out.println();
        System.out.println("Train at " + t.getCurrentRail().getName());
    }

}
