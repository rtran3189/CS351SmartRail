package Data;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class MapReader {
    private Rail railGrid[][];
    private Station stationGrid[][];
    private int rowSize;
    private int colSize;
    private File file;
    private FileReader inputStream;
    private int c;
    private LinkedList<Switch> switchLink;

    /**
     * Constructor
     * @param path the path of the configuration text file.
     * @throws IOException
     */
    public MapReader(String path) throws IOException {
        readFile(path);
    }

    /**
     * @return railGrid Grid of rails
     */
    public Rail[][] getRailGrid()
    {
        return railGrid;
    }

    /**
     * @return Grid of stations
     */
    public Station[][] getStationGrid() { return stationGrid; }

    /**
     * @return Size of rows.
     */
    public int getRowSize() { return rowSize; }

    /**
     * @return Size of columns.
     */
    public int getColSize() { return colSize; }


    /**
     * Grabs the file and calls methods to parse different sections of it.
     * @param path
     * @throws IOException
     */
    private void readFile(String path) throws IOException {
        inputStream = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            file = new File(classLoader.getResource(path).getFile());
            inputStream = new FileReader(file);
            parseMapSize(file);
            parseMap(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Parses the symbols in the text file and builds the map from that.
     * @param f the file to parse.
     * @throws IOException
     */
    private void parseMap(FileReader f) throws IOException
    {
        switchLink = new LinkedList<>();
        boolean endOfFile = false;
        boolean readingStation;
        String stationName = "";
        StringBuilder sb = new StringBuilder();
        int row = 0;
        int col = 0;

        while(c != 10) // while c isn't a new line, this is used to ignore the first line (map dimensions)
        {
            c = f.read();
        }

        while(!endOfFile) // while c isn't end of file
        {
            c = f.read();
            if(c >= 0 && c <= 254 && c != 32)  //FOR DEBUGGING: Prints out the character responsible for creating a certain track
            {
                System.out.print((char) c);
            }

            if (c == '\n') //If a new line is detected, reset col and increment row index.
            {
                col = 0;
                row++;
            }

            else if(c == '\\') //Detects and creates a 'down' switch
            {
                Switch s = new Switch('d');
                railGrid[row][col] = s;
                switchLink.add(new Switch('d'));
                System.out.println(" : Down Switch created at [" + row + "][" + col + "] ");
                col++;
            }

            else if (c == '/') //Detects and creates an 'up' switch.
            {
                Switch s = new Switch('u');
                railGrid[row][col] = s;
                switchLink.add(new Switch('u'));
                System.out.println(" : Upper Switch created at [" + row + "][" + col + "] ");
                col++;
            }

            else if(c == '=') //Detects and creates a rail.
            {
                railGrid[row][col] = new Rail();
                railGrid[row][col].left = railGrid[row][col-1];
                System.out.println(" : Track created at [" + row + "][" + col + "] ");
                col++;
            }

            else if(c == ';') //Reads in the station's name and creates it.
            {
                readingStation = true;

                while(readingStation)
                {
                    c = f.read();
                    if(c == ';')
                    {
                        readingStation = false;
                        stationName = sb.toString();
                        sb.delete(0,sb.length());
                    }
                    else
                    {
                        sb.append((char)c);
                    }
                }

                if(!readingStation)
                {
                    railGrid[row][col] = new Station(row,col, stationName);
                    stationGrid[row][col] = new Station(row, col, stationName);
                    System.out.println(" : Station created at [" + row + "][" + col + "]: " + stationName);
                    col++;
                }
            }

            if(c == -1)
            {
                endOfFile = true;
            }
        }
        linkGrid();
    }

    /**
     * Traverses the array and links up that rail's sides with one another.
     * Links appropriately depending on that Rail's type such as rail, station, or switch.
     * Also assigns each with a thread.
     */
    private void linkGrid()
    {
        //instantiate visualizer
        Visualizer v = new Visualizer(colSize, rowSize);
        for(int rowIndex = 0; rowIndex < rowSize-1; rowIndex++)
        {
            for(int colIndex = 0; colIndex < colSize; colIndex++)
            {
                Thread t = new Thread(railGrid[rowIndex][colIndex], railGrid[rowIndex][colIndex].getName());
                //System.out.println("Starting : " + railGrid[rowIndex][colIndex].getName());
                railGrid[rowIndex][colIndex].setPosition(colIndex,rowIndex);
//                System.out.println(railGrid[rowIndex][colIndex].getName());

                // Checks to see if the index is starting at the left-most side.
                if(colIndex == 0) { railGrid[rowIndex][colIndex].right = railGrid[rowIndex][colIndex+1]; }

                // Checks if the column index is at the right-most side of the grid.
                else if (colIndex == (colSize - 1)) { railGrid[rowIndex][colIndex].left = railGrid[rowIndex][colIndex-1]; }

                // Checks if the object in the array is a Switch.
                else if (railGrid[rowIndex][colIndex] instanceof Switch)
                {
                    //Pops a switch off the LinkedList to set its correct neighbors.
                    Switch tempSwitch = switchLink.pop();

                    //If it is a switch that points up, link it appropriately. Also links left and right.
                    if(tempSwitch.getType() == 'u')
                    {
                        //Links the switch to the track directly above it in the grid.
                        tempSwitch.setSecondLeft(railGrid[rowIndex-1][colIndex]);
                        railGrid[rowIndex][colIndex] = tempSwitch;
                        railGrid[rowIndex][colIndex].left = railGrid[rowIndex][colIndex-1];
                        railGrid[rowIndex][colIndex].right = railGrid[rowIndex][colIndex+1];
                    }

                    //If it is a switch that points down, link appropriately. Also links left and right.
                    else if (tempSwitch.getType() == 'd')
                    {
                        //Links the switch to the track directly below it in the grid.
                        tempSwitch.setSecondRight(railGrid[rowIndex+1][colIndex]);
                        railGrid[rowIndex][colIndex] = tempSwitch;
                        railGrid[rowIndex][colIndex].left = railGrid[rowIndex][colIndex-1];
                        railGrid[rowIndex][colIndex].right = railGrid[rowIndex][colIndex+1];
                    }

                    else { System.err.println("this shouldn't happen..?"); }

                }
                // If it passes through the above tests, then it must be a rail-type that has left and right neighbors.
                else
                {
                    railGrid[rowIndex][colIndex].left = railGrid[rowIndex][colIndex-1];
                    railGrid[rowIndex][colIndex].right = railGrid[rowIndex][colIndex+1];
                }
                t.start();
            }
        }
        System.out.println("Grid pieces mapped!!");
    }


    /**
     * Assigns the map size based on what was given in the config file.
     * @param f A file
     */
    private void parseMapSize(File f)
    {
        Scanner scanner = null;
        try {
            scanner = new Scanner(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert scanner != null;

        colSize = scanner.nextInt();
        rowSize = scanner.nextInt();

        railGrid = new Rail[rowSize][colSize];
        stationGrid = new Station[rowSize][colSize];
    }
}
