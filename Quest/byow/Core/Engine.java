package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class Engine {
    TERenderer ter = new TERenderer(); // TO BE MARKED OUT FOR AUTOGRADER
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public Random rand;
    public long seed;
    public int level = 1;
    Avatar av;
    boolean hasFlower = false;
    boolean gameState = false;
    TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
    String movements = "";

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        InputSource inputSource = new KeyboardInputSource();
        while (inputSource.possibleNextInput()) {



            char c = inputSource.getNextKey();
            if (c == ':') {
                c = inputSource.getNextKey();
                if (c == 'Q') {
                    saveGame();
                    System.exit(0);
                }
            }


            switch (c) {
                case 'N':
                    StdDraw.clear(Color.BLACK);
                    StdDraw.setPenColor(Color.WHITE);
                    Font fontBig = new Font("Monaco", Font.BOLD, 30);
                    StdDraw.setFont(fontBig);
                    StdDraw.text(0.5, 0.75, "Enter a seed Number");
                    Font fontSmall = new Font("Monaco", Font.BOLD, 20);
                    StdDraw.setFont(fontSmall);
                    StdDraw.text(0.5, 0.35, "Input 's' after writing the number");

                    String stringNum = "";
                    boolean flag = false;
                    while (!flag) {
                        char n = inputSource.getNextKey();
                        if (n == 'S') {
                            flag = true;
                            break;
                        }
                        stringNum = stringNum + n;

                        StdDraw.clear(Color.BLACK);
                        StdDraw.setPenColor(Color.WHITE);
                        StdDraw.setFont(fontBig);
                        StdDraw.text(0.5, 0.75, "Enter a seed Number");
                        StdDraw.setFont(fontSmall);
                        StdDraw.text(0.5, 0.35, "Input 's' after writing the number");
                        StdDraw.text(0.5, 0.55, stringNum);

                    }

                    try {
                        long inputSeed = Long.parseLong(stringNum);
                        seed = inputSeed;
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Pass in a valid Number");
                    }

                    drawGameWorld(finalWorldFrame, level);
                    gameState = true;
                    BottomCenterDisplay();

                    break;


                case 'W':
                    movements = movements + 'W';
                    characterMovement("w", finalWorldFrame);
                    break;
                case 'A':
                    movements = movements + 'A';
                    characterMovement("a", finalWorldFrame);
                    break;
                case 'S':
                    movements = movements + 'S';
                    characterMovement("s", finalWorldFrame);
                    break;
                case 'D':
                    movements = movements + 'D';
                    characterMovement("d", finalWorldFrame);

                    break;
                case 'L':
                    finalWorldFrame = loadSavedGame();
                    gameState = true;
                    break;
                case 'R':
                    finalWorldFrame = replayGame();
                    gameState = true;
                    break;
                case '2':
                    if (level == 2) {
                        finalWorldFrame = initiateLevelTwo();
                        gameState = true;
                        break;
                    }
                default: break;
            }

            if (gameState) {
                int mouseX = (int) StdDraw.mouseX();
                int mouseY = (int) StdDraw.mouseY();
                TETile mouseTile = finalWorldFrame[mouseX][mouseY];
                if (mouseTile == Tileset.FLOOR) {
                    BottomDisplay("Floor tiles of Atlantis, formed with the dead turtle shell and octopus bones. Escape before they use your bones.");
                }
                if (mouseTile == Tileset.WALL) {
                    BottomDisplay("Thats a normal wall, you cant walk through that *Facepalm*");
                }
                if (mouseTile == Tileset.LOCKED_DOOR) {
                    if (!hasFlower) {
                        BottomDisplay("This is Aquaman's secret locked door, you must obtain the magic flower to open it");
                    } else {
                        BottomDisplay("This is Aquaman's secret locked door, quickly open and espcape");
                    }
                }
                if (mouseTile == Tileset.FLOWER) {
                    BottomDisplay("Queen Meera's magic flower");
                }
                if (mouseTile == Tileset.SPIKE) {
                    BottomDisplay("Posieden's Poisonous Spike");
                }

                if (mouseTile == Tileset.POISSON_GRASS) {
                    BottomDisplay("Venomous Grass, step on it on your own risk");
                }

            }


        }




    }




    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.



        input = input.toLowerCase(Locale.ROOT);
        String restCommands = input;
        if (!input.equals("")) {
            if (input.charAt(0) == 'n') {
                char[] inputChars = input.toCharArray();

                String seedInput = "";
                char letter = inputChars[1];
                int i = 1;
                while (letter != 's') {
                    seedInput = seedInput + letter;
                    i++;
                    letter = inputChars[i];
                }

                restCommands = input.substring(i + 1);

                long seedInputInt = Long.parseLong(seedInput);

                switch (input.charAt(0)) {
                    case 'n':
                        seed = seedInputInt;
                }
            }
        }



        drawGameWorld(finalWorldFrame, level);

        while (!restCommands.equals("")) {
            String firstCommand = restCommands.charAt(0) + "";
            if (firstCommand.equals(":")) {
                String secondCommand = restCommands.charAt(1) + "";
                if (secondCommand.equals("q")) {
                    saveGame();
                    System.exit(0);
                }
            } else if (firstCommand.equals("l")) {
                finalWorldFrame = loadSavedGame();
            } else {
                movements = movements + firstCommand;
                characterMovement(firstCommand, finalWorldFrame);
            }

            restCommands = restCommands.substring(1);
        }

        return finalWorldFrame;
    }

    public void drawGameWorld(TETile[][] finalWorldFrame, int level) {
        this.rand = new Random(seed);
        TETile t;
        switch (level) {
            case 1: t = Tileset.WATER; break;
            case 2: t = Tileset.TREE; break;
            default: t = Tileset.NOTHING; break;
        }

        TERenderer ter = new TERenderer(); // TO BE MARKED OUT FOR AUTOGRADER
        ter.initialize(WIDTH, HEIGHT); // TO BE MARKED OUT FOR AUTOGRADER

        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                finalWorldFrame[j][i] = Tileset.NOTHING;
            }
        }

        int numRooms = RandomUtils.uniform(rand, 20, 30);
        Room[] roomList = new Room[numRooms];

        // MAKING ALL THE ROOMS
        int index = 0;
        while (index < numRooms) {
            int RoomHeight = RandomUtils.uniform(rand, 4)  + 3;
            int RoomWidth = RandomUtils.uniform(rand, 4) + 3;
            int RoomYCoord = RandomUtils.uniform(rand, 4, 23);
            int RoomXCoord = RandomUtils.uniform(rand, 1,78);
            roomList[index] = new Room(RoomXCoord, RoomYCoord, RoomHeight, RoomWidth);
            Room rm = roomList[index];

            //System.out.println(RoomHeight + " + " + RoomWidth + " + " + RoomXCoord + "," + RoomYCoord); // TO BE REMOVED

            for (int b = rm.xCord; b < rm.width + rm.xCord; b++) {
                if (b + 2 >= WIDTH) {
                    continue;
                }
                for (int h = rm.yCord; h < rm.height + rm.yCord; h++) {
                    if (h + 3 >= HEIGHT) {
                        continue;
                    }
                    finalWorldFrame[b][h] = Tileset.FLOOR;
                }
            }
            index++;
        }






        // MAKING HALL WAYS
        for (int i = 0; i < numRooms; i++) {
            int startX = roomList[i].xCord;
            int startY = roomList[i].yCord;

            Room closestRoom = roomList[i];
            int minDistSq = 10000;

            for (int j = i + 1; j < numRooms; j++) {
                int a = startX - roomList[j].xCord;
                int b = startY - roomList[j].yCord;
                if ((a*a + b*b) < minDistSq) {
                    minDistSq = a*a + b*b;
                    closestRoom = roomList[j];
                }
            }

            int endX = closestRoom.xCord;
            int endY = closestRoom.yCord;

            //int endX = roomList[(i + 7) % 10].xCord;
            //int endY = roomList[(i + 7) % 10].yCord;

//            int endX = roomList[i + 1].xCord;
//            int endY = roomList[i + 1].yCord;

            //int endX = closestRoom.xCord;
            //int endY = closestRoom.yCord;

            if (startX < endX) {
                for (int start = startX; start <= endX; start++){
                    finalWorldFrame[start][startY] = Tileset.FLOOR;
                }
            } else if (startX > endX) {
                for (int start = endX; start <= startX; start++){
                    finalWorldFrame[start][startY] = Tileset.FLOOR;
                }
            }

            if (startY < endY) {
                for (int start = startY; start <= endY; start++){
                    finalWorldFrame[endX][start] = Tileset.FLOOR;
                }
            } else if (startY > endY) {
                for (int start = endY; start <= startY; start++){
                    finalWorldFrame[endX][start] = Tileset.FLOOR;
                }
            }

        }


        // Add Walls
        for (int i = 0; i < HEIGHT; i++) {
            if (i >= HEIGHT) {
                continue;
            }
            for (int j = 0; j < WIDTH; j++) {
                if (j >= WIDTH) {
                    continue;
                }
                if (finalWorldFrame[j][i].equals(Tileset.FLOOR)) {
                    for (int b = i - 1; b < i + 2; b++) {
                        for (int h = j - 1; h < j + 2; h++) {
                            if (finalWorldFrame[h][b].equals(Tileset.NOTHING)) {
                                finalWorldFrame[h][b] = Tileset.WALL;
                            }
                        }
                    }

                }

            }
        }

        for (int i = 2; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (finalWorldFrame[j][i].equals(Tileset.NOTHING)) {
                    finalWorldFrame[j][i] = t;
                }

            }
        }
        // Avatar Starting Point

        int randomStartPoint = RandomUtils.uniform(rand, 10);
        Room startingRoom = roomList[randomStartPoint];
        finalWorldFrame[startingRoom.xCord][startingRoom.yCord] = Tileset.AVATAR;
        av = new Avatar(startingRoom.xCord, startingRoom.yCord);

        // Flower Placement
        boolean flowerPlaced = false;
        while (!flowerPlaced) {
            int flowerX = 0;
            int flowerY = 0;

            // Keep the flower Far
            if (av.posX > WIDTH / 2) {
                flowerX = RandomUtils.uniform(rand, 40);
            } else {
                flowerX = RandomUtils.uniform(rand, 40) + 40;
            }

            if (av.posY > HEIGHT / 2) {
                flowerY = RandomUtils.uniform(rand, 15);
            } else {
                flowerY = RandomUtils.uniform(rand, 15) + 15;
            }



            if (finalWorldFrame[flowerX][flowerY] == Tileset.FLOOR) {
                if (flowerX + 1 < WIDTH) {
                    if (finalWorldFrame[flowerX + 1][flowerY] == Tileset.FLOOR){
                        finalWorldFrame[flowerX][flowerY] = Tileset.FLOWER;
                        flowerPlaced = true;
                    }
                }
            }
        }


        // Locked Door
        boolean doorPlaced = false;
        while (!doorPlaced) {
            int doorX = RandomUtils.uniform(rand, 80);
            int doorY = RandomUtils.uniform(rand, 30);
            if (finalWorldFrame[doorX][doorY] == Tileset.WALL) {
                if (doorX + 1 < WIDTH) {
                    if (finalWorldFrame[doorX + 1][doorY] == Tileset.FLOOR){
                        finalWorldFrame[doorX][doorY] = Tileset.LOCKED_DOOR;
                        doorPlaced = true;
                    }
                }
            }
        }

        //Add Spikes
        for (int i = 2; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (finalWorldFrame[j][i].equals(Tileset.FLOOR)) {
                    if (finalWorldFrame[j + 1][i].equals(Tileset.FLOOR) && finalWorldFrame[j - 1][i].equals(Tileset.FLOOR)) {
                        if (finalWorldFrame[j][i + 1].equals(Tileset.FLOOR) && finalWorldFrame[j][i - 1].equals(Tileset.FLOOR)) {
                            if (finalWorldFrame[j + 1][i + 1].equals(Tileset.FLOOR) && finalWorldFrame[j - 1][i - 1].equals(Tileset.FLOOR)) {
                                if (finalWorldFrame[j - 1][i + 1].equals(Tileset.FLOOR) && finalWorldFrame[j + 1][i - 1].equals(Tileset.FLOOR)) {
                                    if(level == 1) {
                                        finalWorldFrame[j][i] = Tileset.SPIKE;
                                    } else {
                                        finalWorldFrame[j][i] = Tileset.POISSON_GRASS;
                                    }

                                }
                            }
                        }
                    }
                }

            }
        }



        ter.renderFrame(finalWorldFrame); // TO BE MARKED OUT FOR AUTOGRADER

        StdDraw.setPenColor(Color.WHITE);
        Font fontSmaller = new Font("Monaco", Font.BOLD, 12);
        StdDraw.setFont(fontSmaller);
        StdDraw.textLeft(1, 0.5, "You are on Level " + "" + level);
        StdDraw.show();

    }

    public void BottomDisplay(String s) {
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmaller = new Font("Monaco", Font.BOLD, 12);
        StdDraw.setFont(fontSmaller);
        StdDraw.textLeft(1, 0.5, s);
        StdDraw.show();
    }

    public void BottomCenterDisplay() {
        StdDraw.setPenColor(Color.WHITE);
        if (level == 1) {
            StdDraw.textRight(WIDTH -2 , 0.5, "LOST CITY OF ATLANTIS");
            StdDraw.show();
        } else if (level == 2) {
            StdDraw.textRight(WIDTH -2 , 0.5, "DEPTHS OF THE AMAZON");
            StdDraw.show();
        }

    }

    public void characterMovement(String move, TETile[][] finalWorldFrame) {
        boolean firstAfterFlower = false;
        switch (move) {

            case "w":
                if (finalWorldFrame[av.posX ][av.posY + 1] == Tileset.SPIKE || finalWorldFrame[av.posX ][av.posY + 1] == Tileset.POISSON_GRASS) {
                    GameLostScreenSpikes();
                }
                if (finalWorldFrame[av.posX ][av.posY + 1] == Tileset.LOCKED_DOOR) {
                    if (hasFlower) {
                        levelOneEndScreen();
                    } else {
                        BottomDisplay("You need the magic flower to enter");
                    }

                    break;
                }
                if (finalWorldFrame[av.posX][av.posY + 1] != Tileset.WALL) {

                    if (finalWorldFrame[av.posX][av.posY + 1] == Tileset.FLOWER) {
                        hasFlower = true;
                        firstAfterFlower = true;
                    }
                    finalWorldFrame[av.posX][av.posY + 1] = Tileset.AVATAR;
                    finalWorldFrame[av.posX][av.posY] = Tileset.FLOOR;
                    av.posY++;
                    ter.renderFrame(finalWorldFrame);
                    BottomCenterDisplay();
                    if (hasFlower && firstAfterFlower) {
                        BottomDisplay("You have obtained the magic flower");
                    }
                    firstAfterFlower = false;
                }
                break;
            case "d":
                if (finalWorldFrame[av.posX + 1][av.posY] == Tileset.SPIKE || finalWorldFrame[av.posX + 1][av.posY] == Tileset.POISSON_GRASS) {
                    GameLostScreenSpikes();
                }
                if (finalWorldFrame[av.posX + 1][av.posY] == Tileset.LOCKED_DOOR) {
                    if (hasFlower) {
                        levelOneEndScreen();
                    } else {
                        BottomDisplay("You need the magic flower to enter");
                    }

                    break;
                }
                if (finalWorldFrame[av.posX + 1][av.posY] != Tileset.WALL) {

                    if (finalWorldFrame[av.posX + 1][av.posY] == Tileset.FLOWER) {
                        hasFlower = true;
                        firstAfterFlower = true;
                    }
                    finalWorldFrame[av.posX + 1][av.posY] = Tileset.AVATAR;
                    finalWorldFrame[av.posX][av.posY] = Tileset.FLOOR;
                    av.posX++;
                    ter.renderFrame(finalWorldFrame);
                    BottomCenterDisplay();
                    if (hasFlower && firstAfterFlower) {
                        BottomDisplay("You have obtained the magic flower");
                    }
                    firstAfterFlower = false;
                }
                break;
            case "a":
                if (finalWorldFrame[av.posX - 1][av.posY] == Tileset.SPIKE || finalWorldFrame[av.posX - 1][av.posY ] == Tileset.POISSON_GRASS) {
                    GameLostScreenSpikes();
                }
                if (finalWorldFrame[av.posX - 1][av.posY] == Tileset.LOCKED_DOOR) {
                    if (hasFlower) {
                        levelOneEndScreen();
                    } else {
                        BottomDisplay("You need the magic flower to enter");
                    }

                    break;
                }
                if (finalWorldFrame[av.posX - 1][av.posY] != Tileset.WALL) {


                    if (finalWorldFrame[av.posX - 1][av.posY] == Tileset.FLOWER) {
                        hasFlower = true;
                        firstAfterFlower = true;
                    }
                    finalWorldFrame[av.posX - 1][av.posY] = Tileset.AVATAR;
                    finalWorldFrame[av.posX][av.posY] = Tileset.FLOOR;
                    av.posX--;
                    ter.renderFrame(finalWorldFrame);
                    BottomCenterDisplay();
                    if (hasFlower && firstAfterFlower) {
                        BottomDisplay("You have obtained the magic flower");
                    }
                    firstAfterFlower = false;

                }
                break;
            case "s":
                if (finalWorldFrame[av.posX][av.posY - 1] == Tileset.SPIKE || finalWorldFrame[av.posX][av.posY - 1] == Tileset.POISSON_GRASS) {
                    GameLostScreenSpikes();
                }
                if (finalWorldFrame[av.posX][av.posY - 1] == Tileset.LOCKED_DOOR) {


                    if (hasFlower) {
                        levelOneEndScreen();
                    } else {
                        BottomDisplay("You need the magic flower to enter");
                    }

                    break;
                }
                if (finalWorldFrame[av.posX][av.posY - 1] != Tileset.WALL) {
                    if (finalWorldFrame[av.posX][av.posY - 1] == Tileset.FLOWER) {
                        hasFlower = true;
                        firstAfterFlower = true;
                    }
                    finalWorldFrame[av.posX][av.posY - 1] = Tileset.AVATAR;
                    finalWorldFrame[av.posX][av.posY] = Tileset.FLOOR;
                    av.posY--;
                    ter.renderFrame(finalWorldFrame);
                    BottomCenterDisplay();

                    if (hasFlower && firstAfterFlower) {
                        BottomDisplay("You have obtained the magic flower");
                    }
                    firstAfterFlower = false;
                }
                break;
            default: break;
        }

    }

    public void GameLostScreenSpikes() {
        gameState = false;
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "Game Over");
        if (level == 1) {
            StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You stepped on the Posieden's Poisonous Spike");
        } else if (level == 2) {
            StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You stepped on the Venomous Grass");
        }

        StdDraw.show();
        StdDraw.pause(10000);
        System.exit(0);
    }

    public void levelOneEndScreen() {
        if (level == 2) {
            levelTwoEndScreen();
        } else {
            gameState = false;
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 30);
            StdDraw.setFont(fontBig);
            StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "You have escaped the lost city of Atlantis");
            StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You Won!");
            StdDraw.show();

            StdDraw.pause(3000);
            StdDraw.clear(Color.BLACK);
            StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "You have escaped the lost city of Atlantis");
            StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You Won!...Or have you?");
            StdDraw.show();

            StdDraw.pause(4000);
            StdDraw.clear(Color.BLACK);
            StdDraw.text(0.5 * WIDTH, 0.5 * HEIGHT, "MUAHAHAHAH you have just Completed Level 1");
            StdDraw.show();

            level = 2;

            StdDraw.pause(5000);
            StdDraw.clear(Color.BLACK);
            StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "Level 2: Depths of the Amazon");
            StdDraw.text(0.5 * WIDTH, 0.55 * HEIGHT, "*Press '2' to continue*");
            StdDraw.show();
        }

    }

    public void levelTwoEndScreen() {
        gameState = false;
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "You Have Escaped from the depths of the Amazon Rainforest");
        StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You are finally free");
        StdDraw.show();

        StdDraw.pause(4000);
        StdDraw.clear(Color.BLACK);
        StdDraw.text(0.5 * WIDTH, 0.75 * HEIGHT, "You Have Escaped from the depths of the Amazon Rainforest");
        StdDraw.text(0.5 * WIDTH, 0.50 * HEIGHT, "You are finally free FOR REAL");
        StdDraw.show();

        StdDraw.pause(10000);
        System.exit(0);
    }

    public  TETile[][] initiateLevelTwo() {
        seed = seed*2;
        hasFlower = false;
        movements = "";
        level = 2;
        finalWorldFrame = new TETile[WIDTH][HEIGHT];
        return interactWithInputString(movements);

    }

    public void saveGame() {
        File SeedFile = Utils.join(CWD, "Seeed.txt");
        Utils.writeContents(SeedFile, String.valueOf(seed));
        File MovesFile = Utils.join(CWD, "Moves.txt");
        Utils.writeContents(MovesFile, String.valueOf(movements));
    }

    public TETile[][] loadSavedGame() {

        File pathToSeedFile = Utils.join(CWD, "Seeed.txt");
        File pathToMovesFile = Utils.join(CWD, "Moves.txt");
        String seedString = Utils.readContentsAsString(pathToSeedFile);
        String movesString = Utils.readContentsAsString(pathToMovesFile);
        if (movesString.equals("") || movesString == null) {
            System.exit(0);
        }

        long inputSeed = Long.parseLong(seedString);
        seed = inputSeed;
        return interactWithInputString(movesString);
    }

    public TETile[][] replayGame() {

        File pathToSeedFile = Utils.join(CWD, "Seeed.txt");
        File pathToMovesFile = Utils.join(CWD, "Moves.txt");
        String seedString = Utils.readContentsAsString(pathToSeedFile);
        String movesString = Utils.readContentsAsString(pathToMovesFile);
        if (movesString.equals("") || movesString == null) {
            System.exit(0);
        }

        long inputSeed = Long.parseLong(seedString);
        seed = inputSeed;


        drawGameWorld(finalWorldFrame, level);

        movesString = movesString.toLowerCase(Locale.ROOT);

        while (!movesString.equals("")) {
            String firstCommand = movesString.charAt(0) + "";
            movements = movements + firstCommand;
            StdDraw.pause(250);
            characterMovement(firstCommand, finalWorldFrame);
            movesString = movesString.substring(1);
        }

        return finalWorldFrame;


    }




    public class Room {
        int xCord;
        int yCord;
        int height;
        int width;

        public Room(int x, int y, int h, int w) {
            xCord = x;
            yCord = y;
            height = h;
            width = w;
        }
    }

    public class Avatar{
        int posX;
        int posY;
        public Avatar(int x, int y){
            posX = x;
            posY = y;
        }

    }

}
