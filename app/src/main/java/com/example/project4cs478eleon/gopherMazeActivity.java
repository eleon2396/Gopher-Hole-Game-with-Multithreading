package com.example.project4cs478eleon;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

/*
 * Eric Leon eleon23 654889611
 * CS 478 Project 4: Gopher Hunting on Android
 *   This Application is a Gopher Hunting game on Android where two workers threads will take turns searching/finding the gopher
 *   in a 10x10 Matrix. This application will support two modes:
 *    1) Guess by Guess Mode: Here the user selects appropriate buttons to decide when to let the threads make a guess. Here each worker thread communicates through
            * the UI thread to know when to make a move.
 *    2) Continuous Mode: Here the two workers threads will play without interruption until one thread wins the game.
            * Here the workers threads WILL communicate with eachother know when to make a move. They will send messages to eachother in order to make the
            * game continuous.
 *  The user will be able to select which game mode to select.

 *   NOTE: Worker thread 1 will have the color red to denote its guesses. Worker thread 2 will have the color blue to denote its guesses.
 *   Worker thread 1 Guessing algorithm: This worker will always randomly guess where the gopher is.
 *   Worker thread 2 Guessing algorithm: This worker will always guess the middle of the board  at the start and will make educates guesses as it gets close to the gopher hole(the thread will know this
     * when its current guess results in a close guess or a near miss). When it achieves a near miss/close guess it starts making adjacent guesses which will improve
     * its chances of guessing the gopher hole. Worker thread 2 has information of what the current guesses are on the board and is able to make a optimal guess(not guessing a spot which has already
     * been guessed by itself(thread 2) or thread 1). Worker thread 2 DOESN'T know where the gopher spot is and DOESN'T KNOW THE RESULTS OF THREAD 1 JUST WHERE THREAD 1 HAS GUESSED. However, because of this when it
     * gets a near miss/close guess, it will randomly select one of adjacent spots from the previous guess. This can sometimes lead Worker thread 2 to stray farther away from the
     * gopher hole, which then leads it to start making random guesses until it gets another guess which results in a near miss/close guess.

 *   Every time a thread makes a guess one of the following five messages will be provided from the game system:
 *       1)Success: The thread guesses the hole containing the gopher and wins the game.
 *       2)Near-Miss: THe thread guessed one of the 8 holes adjacent to the gopher's hole. THis includes horizontally, vertically, diagonally. The game continues with the other worker thread's turn.
 *       3)Close Guess: The thread guesses a hole that's 2 holes away from the gopher's hole in any directions. This includes two holes horizontally, vertically, or diagonally.
 *       4)Complete miss: The thread get this response if they are no where near the gopher and the above cases fail.
 *       5)Disaster: The thread accidentally guesses a hole that has already been guessed, either by the same thread itself or the opponent thread.

 */

public class gopherMazeActivity extends AppCompatActivity {

    // *Back End Array for the gopherMaze end that'll hold -1 for non-guessed spots and 0 for spots that have been guessed
    private int gopherMazeBack[] = new int[100];
    // *Front end Array to hold the image drawables and to hold the changes to the UI based on the back end array
    private int holesBoardFront[] = new int[100];
    // *Variable for the Grid view
    private GridView mGrid;

    // * Arraylist to hold all the adjacent holes relative to the gopher 1 hole away
    private ArrayList<Integer> adjacentGopherHoles = new ArrayList<>();

    // * Arraylist to hold all the adjacent holes relative to the gopher 2 holes away
    private ArrayList<Integer> adjacentGopher_TWOHOLES = new ArrayList<>();

    // * Array list for potential guesses that worker 2 will use to make an educated guess
    private ArrayList<Integer> potentialGuesses = new ArrayList<>();


    // * All these arrays are the possible adjacent positions based on the gopher location, these cover all edge cases as well. This deals with one hole away
    private int adjacentSpotsMIDDLE[] = {-1, -9, -10, -11, 1, 9, 10, 11};
    private int adjacentSpotsTOPLEFT[] = {1, 10, 11};
    private int adjacentSpotsTOPRIGHT[] = {-1, 9, 10};
    private int adjacentSpotsBOTTOMLEFT[] = {-10, -9, 1};
    private int adjacentSpotsBOTTOMRIGHT[] = {-1, -10, -11};
    private int adjacentSpotsLEFTMIDDLE[] = {-10, -9, 1, 10, 11};
    private int adjacentSpotsRIGHTMIDDLE[] = {-11, -10, -1, 9, 10};

    // * Make arrays for adajcent spots two holes away?
    private int adjacent2SpotsMIDDLE[] = {-2, -18, -20, -22, 2, 18, 20, 22 };
    private int adjacent2SpotsTOPLEFT[] = {2, 20, 22 };
    private int adjacent2SpotsTOPRIGHT[]=  {-2, 18, 20 };
    private int adjacent2SpotsBOTTOMLEFT[] = {-20, -18, 2};
    private int adjacent2SpotsBOTTOMRIGHT[] = {-2, -20, -22};
    private int adjacent2SpotsLEFTMIDDLE[] = {-20, -18, 2, 20, 22};
    private int adjacent2SpotsRIGHTMIDDLE[] = {-22, -20, -2, 18, 20};

    // * Array lists to store guessed spots already
    private ArrayList<Integer> thread1Guesses = new ArrayList<>();
    private ArrayList<Integer> thread2Guesses = new ArrayList<>();
    private ArrayList<Integer> educatedGuesses = new ArrayList<>();

    // * These are locks to be used to make sure the threads are synchronized
    private Boolean thread1Lock = false;
    private Boolean thread2Lock = false;
    private Boolean thread2ResultsLock = false;
    private Boolean adjacentGopherLock = false;
    private Boolean adjacentGopher2HolesLock = false;

    // * Strings to show which thread has a turn
    private String thread1Turn = "Thread's Turn: 1";
    private String thread2Turn = "Thread's Turn: 2";

    // *Messages to be used by the Main UI Thread
    public static final int SET_PROGRESS_VISIBLE = 0 ;
    public static final int SET_PROGRESS_INVISIBLE = 1 ;
    public static final int POST_PROGRESS = 2 ;
    public static final int UPDATE_IMAGE_VIEW = 3 ;
    public static final int MAKE_MOVE = 4;
    public static final int END_TURN = 5;
    public static final int WAIT = 6;
    public static final int WINNER = 7;

    // * Messages for Thread
    public static final int THREAD_MOVE = 9;
    public static final int CONTINUOUS  = 12;

    // * Messages to be displayed to the result text view
    private int messageNUM = -1;
    private String msg1 = "Success";
    private String msg2 = "Near-Miss";
    private String msg3 = "Close Guess";
    private String msg4 = "Disaster";
    private String msg5 = "Complete Miss";

    // * FOR THREADS
    UIHandler mHandler;
    Worker1 thread1;
    Worker2 thread2;

    // *Variables to store the gopher Spot and the game mode and turn making
    private int gopherSpot = 0;
    private int turn = 0;
    private int continuousMODE = 0;
    private boolean winner = false;

    // * Handler for the Main thread
    private class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            switch (what) {
                case SET_PROGRESS_VISIBLE:
                    mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    break;
                case POST_PROGRESS:
                    mProgressBar.setProgress(msg.arg1);
                    break;
                case SET_PROGRESS_INVISIBLE:
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    break;
                case UPDATE_IMAGE_VIEW:
                    messageNUM = (int)msg.obj;
                    if(messageNUM != 4) {
                        gridChild = (ImageView) mGrid.getChildAt((int) msg.arg1);
                        gridChild.setImageResource((int) msg.arg2);
                    }

                    displayResult(messageNUM);
                    break;
                case MAKE_MOVE:
                    // * Here the threads will take turns making a move
                    turn++;
                    if(turn % 2 == 1){
                        Message m = thread1.handlerWorker1.obtainMessage(THREAD_MOVE);
                        thread1.handlerWorker1.sendMessage(m);
                    }
                    else if(turn % 2 == 0){
                        Message m = thread2.handlerWorker2.obtainMessage(THREAD_MOVE);
                        thread2.handlerWorker2.sendMessage(m);
                    }

                    break;
                case END_TURN:
                    // * When the thread has ended
                    messageNUM = (int)msg.arg1;
                    String t = (String)msg.obj;
                    threadTurn.setText(t);
                    displayResult(messageNUM);
                    break;
                case WINNER:
                    // * A thread has won and now see things to the finishing state, disable buttons, display who won, and end threads
                    String threadWinner = "Thread: " + Integer.toString((int)msg.arg2);
                    Toast.makeText(getApplicationContext(), "Thread: " + Integer.toString((int)msg.arg2) + " Has WON!", Toast.LENGTH_LONG).show();
                    guessButton.setEnabled(false);
                    switchModes.setEnabled(false);
                    threadTurn.setEnabled(false);
                    result.setEnabled(false);
                    winnerDisplay.setText("Winner: " + threadWinner);
                    Toast.makeText(getApplicationContext(), "Thanks for playing!", Toast.LENGTH_LONG).show();
                    try {
                        thread1.interrupt();
                        thread2.interrupt();
                    }
                    catch (Exception e){
                        System.out.println("SORRY THE THREADS DIDNT CLOSE PROPERLY");
                    }
                    break;
            }

        }
    }

    // * Variables referring to the buttons and such on the UI
    private Button guessButton;
    private Button switchModes;
    private ProgressBar mProgressBar;
    private ImageView gridChild;
    private TextView threadTurn;
    private TextView result;
    private TextView winnerDisplay;

    // *Variables to change hole image to specific color based on the thread that guesssed it
    private int redSquare = R.drawable.red;
    private int blueSquare = R.drawable.blue;
    private int gopherImg = R.drawable.gopher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gopher_maze);

        // * hide the gopher
        hideGopher();
        System.out.println(Integer.toString(gopherSpot));
        // * initialize the boards
        initializeBoards();

        // * get all the adjacent positions relative to the gopher, 1 spot away
        findallAdjacents();

        // * Get the progress bar
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        // * Set up the handler, the worker1 and worker2 with their handlers and start them
        mHandler = new UIHandler();
        thread1 = new Worker1(mHandler);
        thread2 = new Worker2(mHandler);
        thread1.start();
        thread2.start();

        // * Get the grid and assign its item adapter
        mGrid = (GridView)findViewById(R.id.gopherMazeGridview);
        myItemAdapter mAdapter  = new myItemAdapter(this, holesBoardFront);
        mGrid.setAdapter(mAdapter);

        // * This shows who's turn it is at the beginning which will be thread 1 in this instance, this will change as the game progresses.
        threadTurn = (TextView) findViewById(R.id.threadTurnTextView);
        // * This shows the result of a thread's turn and in this instance nothing has happened yet
        result = (TextView) findViewById(R.id.resultTextView);
        threadTurn.setText(thread1Turn);
        displayResult(messageNUM);
        // * On click for the guess button for the guess by guess mode
        guessButton = (Button)findViewById(R.id.guessButton);

        // * The textview to display the winner in the end
        winnerDisplay = (TextView) findViewById(R.id.displayWinnerTextView);

        // * On click listener for the guess button
        guessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // *Change the hole that was guessed by the thread to a red or blue square depending on the thread that made the guess
                Message m = thread1.handlerWorker1.obtainMessage(WAIT);
                thread1.handlerWorker1.sendMessage(m);


            }
        });


        // * On click for switch mode button in order to switch to continuous mode
        switchModes = (Button)findViewById(R.id.switchToContinuousButton);
        switchModes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    continuousMODE = 1;
                    guessButton.setEnabled(false);
                    switchModes.setEnabled(false);
                    Message m2 = thread1.handlerWorker1.obtainMessage(WAIT);
                    thread1.handlerWorker1.sendMessage(m2);

            }
        });
    }


    // * Function to handle which message to be displayed
    public synchronized void displayResult(int r){
        switch(r){
            case -1:
                result.setText("Result: ");
                break;
            case 1:

                result.setText("Result: " + msg1);
                break;
            case 2:

                result.setText("Result: " + msg2);
                break;
            case 3:
                result.setText("Result: " + msg3);
                break;
            case 4:

                result.setText("Result: " + msg4);
                break;
            default:

                result.setText("Result: " + msg5);
                break;

        }
    }


    // * Initialize both boards, the back board to -1 and the front board to either the images or a gridview???????
    private void initializeBoards(){
        for(int x = 0; x < 100; x++){

            if(x == gopherSpot){
                holesBoardFront[x] = gopherImg;
                gopherMazeBack[x] = 0;
            }
            else {
                holesBoardFront[x] = R.drawable.hole;
                gopherMazeBack[x] = -1;
            }
        }
    }

    // * Generate a random number and hide it somewhere in the maze
    private void hideGopher(){
        Random r = new Random();
        int randPos = r.nextInt(100 );
        gopherSpot = randPos;
    }

    // * Function to be used by Thread 1 which will be using the "Guess random strategy" to find the gopher and win.
    private synchronized int generateRandomNum(){
        Random r = new Random();
        int randPos = r.nextInt(100 );
        return randPos;
    }

    // * function to make a list of all the adjacent positions to the gopher spot, this covers all edge cases
    private synchronized void findallAdjacents(){
        if(gopherSpot == 0){
            adjacentSPOTS(adjacentSpotsTOPLEFT);
            adajacentSpotsTwoHoles(adjacent2SpotsTOPLEFT);
        }
        else if(gopherSpot == 9){
            adjacentSPOTS(adjacentSpotsTOPRIGHT);
            adajacentSpotsTwoHoles(adjacent2SpotsTOPRIGHT);
        }
        else if(gopherSpot == 90){
            adjacentSPOTS(adjacentSpotsBOTTOMLEFT);
            adajacentSpotsTwoHoles(adjacent2SpotsBOTTOMLEFT);
        }
        else if(gopherSpot == 99){
            adjacentSPOTS(adjacentSpotsBOTTOMRIGHT);
            adajacentSpotsTwoHoles(adjacent2SpotsBOTTOMRIGHT);
        }
        else if(gopherSpot % 10 == 0 ){
            //&& gopherSpot != 0 && gopherSpot != 90 <---- dont need it?
            adjacentSPOTS(adjacentSpotsLEFTMIDDLE);
            adajacentSpotsTwoHoles(adjacent2SpotsLEFTMIDDLE);
        }
        else if((gopherSpot+1) % 10 == 0  ){
            //&& (gopherSpot+1) != 10 && (gopherSpot+1) != 100 <--- dont need it?
            adjacentSPOTS(adjacentSpotsRIGHTMIDDLE);
            adajacentSpotsTwoHoles(adjacent2SpotsRIGHTMIDDLE);
        }
        else{
            adjacentSPOTS(adjacentSpotsMIDDLE);
            adajacentSpotsTwoHoles(adjacent2SpotsMIDDLE);
        }

    }


    // * Helper Function to find the adjacent spots for middle gopher spot, one hole away
    private synchronized void adjacentSPOTS(int[] spots){
        for(int i = 0; i < spots.length; i++){
            int spot = gopherSpot + spots[i];
            if(spot >= 0 && spot < 100) {
                adjacentGopherHoles.add(spot);
            }
        }
    }

    // * Helper function to the find the adjacent spots that are two holes away from the gopher
    private synchronized void adajacentSpotsTwoHoles(int[] spots){
        for(int i = 0; i < spots.length; i++){
            int spot = gopherSpot + spots[i];
            if(spot >= 0 && spot < 100) {
                adjacentGopher_TWOHOLES.add(spot);
            }
        }
    }

    // * Function for when a thread found the gopher
    private synchronized boolean foundGopher(int guess){
        if(guess == gopherSpot){
            return true;
        }
        return false;
    }

    // * Function to check if a threads guess was already made/ also used to see if a guess is one hole or two holes away
    private synchronized boolean checkGuesses(ArrayList<Integer> a, int guess){

        for(int x: a){
            if(x == guess){
                return true;
            }
        }
        return false;
    }

    // * Worker thread 1, this worker's thread strategy to find the gopher hole is just to guess at random
    private class Worker1 extends Thread{
        // * Handler reference to the main handler and make a reference handler for worker1
        public Handler handlerWorker1;
        private UIHandler mainHandler;

        public Worker1(UIHandler h){
            mainHandler = h;
        }

        @SuppressLint("HandlerLeak")
        public void run(){
            Looper.prepare();
            handlerWorker1 = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    int what = msg.what;
                    switch(what){
                        case WAIT:
                            handlerWorker1.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message m2;
                                    m2 = mainHandler.obtainMessage(SET_PROGRESS_VISIBLE);
                                    mainHandler.sendMessage(m2);

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 0;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 33;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 66;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(SET_PROGRESS_INVISIBLE);
                                    mainHandler.sendMessage(m2);

                                    m2 = mainHandler.obtainMessage(MAKE_MOVE);
                                    mainHandler.sendMessage(m2);


                                }
                            });
                            break;

                        case THREAD_MOVE:
                            handlerWorker1.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message m2;
                                    // * Get message to UI's queue, send bitmap along with message
                                    m2 = mainHandler.obtainMessage(UPDATE_IMAGE_VIEW) ;
                                    // * Generate random number to guess
                                    int guess = generateRandomNum();

                                    // * Set the args for the message object
                                    m2.arg1 = guess;
                                    m2.arg2 = redSquare;

                                    // * Start checking the thread's guess
                                    boolean oneHole = false;
                                    boolean twoHoles = false;
                                    boolean foundGopher = false;
                                    boolean sameHole = false;
                                    boolean sameHoleByThread = false;

                                    // * Here the variables are set to see what scenario they will fall in
                                    synchronized (adjacentGopherLock) {
                                        oneHole = checkGuesses(adjacentGopherHoles, guess);
                                    }
                                    synchronized (adjacentGopher2HolesLock) {
                                        twoHoles = checkGuesses(adjacentGopher_TWOHOLES, guess);
                                    }
                                    // * Check if the thread found the gopher
                                    foundGopher = foundGopher(guess);
                                    synchronized (thread2Lock) {
                                        sameHole = checkGuesses(thread2Guesses, guess);
                                    }
                                    synchronized (thread1Lock) {
                                        sameHoleByThread = checkGuesses(thread1Guesses, guess);
                                    }

                                    // * Guess a hole that thread 1 has already guessed or the same thread guess the same hole
                                    if(sameHole || sameHoleByThread){
                                        m2.obj = 4;
                                    }
                                    else if(foundGopher){
                                        m2.obj = 1;
                                    }
                                    // * This is a near miss
                                    else  if(oneHole) {
                                        m2.obj = 2;
                                    }

                                    // * This is a close guess
                                    else if(twoHoles){
                                        m2.obj = 3;
                                    }
                                    // * This is a complete miss
                                    else{
                                        m2.obj = 0;
                                    }

                                    synchronized (thread1Lock) {
                                        // * Adding guess onto array list
                                        thread1Guesses.add(guess);
                                    }

                                    mainHandler.sendMessage(m2) ;

                                    // * check if the winner is found
                                    winner = foundGopher(guess);

                                    // * if a winner if found
                                    if(winner == true) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            System.out.println("Thread interrupted!");
                                        }
                                        m2 = mainHandler.obtainMessage(WINNER);
                                        m2.arg2 = 1;
                                        mainHandler.sendMessage(m2);
                                    }
                                    // * Else continue the game
                                    else {
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            System.out.println("Thread interrupted!");
                                        }

                                        m2 = mainHandler.obtainMessage(END_TURN);
                                        m2.arg1 = -1;
                                        m2.obj = thread2Turn;
                                        mainHandler.sendMessage(m2);

                                        // * If the continuous mode is selected
                                        if(continuousMODE == 1){
                                            Message continuousM = thread2.handlerWorker2.obtainMessage(WAIT);
                                            thread2.handlerWorker2.sendMessage(continuousM);

                                        }
                                    }

                                }
                            });
                            break;

                        case CONTINUOUS:
                            handlerWorker1.post(new Runnable() {
                                @Override
                                public void run() {
                                //Toast.makeText(getApplicationContext(), "THREAD 1", Toast.LENGTH_LONG).show();
                                Message m2 = mainHandler.obtainMessage(MAKE_MOVE);
                                mainHandler.sendMessage(m2);
                                }
                            });
                            break;
                    }
                }
            }; // * End of Handler
            Looper.loop();
        }

    }

    // * This function will help with making the guess more smarter, it will get all the adjacent spots for the next guess
    private synchronized void helpedEducateGuess(int spot){
        if(spot == 0){
            findGuessAdjacents(adjacentSpotsTOPLEFT, spot, potentialGuesses);

        }
        else if(spot == 9){
            findGuessAdjacents(adjacentSpotsTOPRIGHT, spot, potentialGuesses);

        }
        else if(spot == 90){
            findGuessAdjacents(adjacentSpotsBOTTOMLEFT, spot, potentialGuesses);

        }
        else if(spot == 99){
            findGuessAdjacents(adjacentSpotsBOTTOMRIGHT, spot, potentialGuesses);

        }
        else if(spot % 10 == 0 ){
            //&& gopherSpot != 0 && gopherSpot != 90 <---- dont need it?
            findGuessAdjacents(adjacentSpotsLEFTMIDDLE, spot, potentialGuesses);

        }
        else if((spot+1) % 10 == 0  ){
            //&& (gopherSpot+1) != 10 && (gopherSpot+1) != 100 <--- dont need it?
            findGuessAdjacents(adjacentSpotsRIGHTMIDDLE, spot, potentialGuesses);

        }
        else{
            findGuessAdjacents(adjacentSpotsMIDDLE, spot, potentialGuesses);

        }
    }

    // * Helper Function to find the adjacent spots of the recent guess
    private synchronized void findGuessAdjacents(int[] spots, int pastGuess, ArrayList<Integer> potential){
        for(int i = 0; i < spots.length; i++){
            int spot = pastGuess + spots[i];
            if(spot >= 0 && spot < 100) {
                potential.add(spot);
            }
        }
    }

    // * This function will make an educated guess for thread 2 based on their result and their recent guess, thread 2 is also aware of where thread 1 has guessed BUT doesn't know the
    // * result of thread 1's guesses, it just knows if the spot is taken.
    private synchronized int educatedGuess (ArrayList<Integer> result, int pastGuess){

        // * new guess to be made
        int returnNewGuess = 0;
        // * get what the past result was
        int recentResult = result.get(result.size()-1);
        System.out.println("RESULTS: " + Integer.toString(recentResult));


        // * Complete miss and Disaster (HOWEVER THIS CODE SHOULD NEVER RUN IF IT IS A DISASTER BECAUSE
        // * OF THE WAY I HANDLED THE DISASTER SECNARIO BEFORE IT EVEN GETS HERE...THIS IS JUST IN CASE .-.
        // * Thread 2 should never cause a disaster(hopefully)
        if(recentResult == 0 || recentResult == 4){
            // * This will prevent Worker 2 from making guesses that have already been guessed
            do {
                int randPos = generateRandomNum();
                returnNewGuess = randPos;
            }while(thread2Guesses.contains(returnNewGuess) || thread1Guesses.contains(returnNewGuess));
        }
        // * Near Miss
        // * This will prevent Worker 2 from making guesses that have already been guessed
        else if(recentResult == 2 || recentResult ==  3){
            // * Get the adjacent spots to the new guess
            helpedEducateGuess(pastGuess);
            Random r = new Random();
            do {
                int randPos = r.nextInt(potentialGuesses.size());
                returnNewGuess = potentialGuesses.get(randPos);
            }while(thread2Guesses.contains(returnNewGuess) || thread1Guesses.contains(returnNewGuess));

            // * Clear the array so that it will be new for the next optimal guess
            potentialGuesses.clear();

        }
        System.out.println("GUESS: " + returnNewGuess);
        // * return the new guess
        return returnNewGuess;
    }

    // * Worker thread 2, this worker's strategy to find the gopher hole is that it starts in the middle at 5 and makes educated guess to find the gopher.
        //  * The algorithm for thread 2 is smart enough to not make the same guess as the it did before and select a random adjacent spot. It will also check that the new educated guess
        // * hasn't been guessed by itself(thread 2) or by thread 1. Thread 2 only knows the location where Thread 1 has guessed and DOESN'T know what the results of Thread 1 are.
    private class Worker2 extends Thread{
        // * Reference to the main handler and worker 2 handler
        public Handler handlerWorker2;
        private UIHandler mainHandler;

        // * Flag to see if it is the first guess, which will always be random
        private int firstGuess = -1;
        // * Worker 2 will start in the middle of the map, spot 55
        private int guess = 55;
        // * Variable to hold the next smart guess
        private int smartGuess = 0;

        public Worker2(UIHandler h){
            mainHandler = h;
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run(){
            Looper.prepare();
            handlerWorker2 = new Handler() {
                @Override
                public void handleMessage(Message msg){
                    int what = msg.what;
                    switch(what){
                        case WAIT:
                            handlerWorker2.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message m2;
                                    m2 = mainHandler.obtainMessage(SET_PROGRESS_VISIBLE);
                                    mainHandler.sendMessage(m2);

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 0;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 33;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(POST_PROGRESS);
                                    m2.arg1 = 66;
                                    mainHandler.sendMessage(m2);

                                    try { Thread.sleep(500); }
                                    catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }

                                    m2 = mainHandler.obtainMessage(SET_PROGRESS_INVISIBLE);
                                    mainHandler.sendMessage(m2);

                                    m2 = mainHandler.obtainMessage(MAKE_MOVE);
                                    mainHandler.sendMessage(m2);
                                }
                            });
                            break;

                        case THREAD_MOVE:
                            handlerWorker2.post(new Runnable() {
                                @Override
                                public void run() {
                                    Message m2;
                                    // * Get message to UI's queue, send bitmap along with message
                                    m2 = mainHandler.obtainMessage(UPDATE_IMAGE_VIEW) ;
                                    // * Check if is it the first guess, if so generate random numner and change the flag
                                    if(firstGuess == -1){
                                        firstGuess = 0;
                                    }
                                    // * Else make an educated guess
                                    else if(firstGuess != -1){
                                        smartGuess = educatedGuess(educatedGuesses, guess );
                                        guess = smartGuess;
                                    }

                                    // * Set the args for the message object
                                    m2.arg1 = guess;
                                    m2.arg2 = blueSquare;

                                    // * Start checking the thread's guess
                                    boolean oneHole = false;
                                    boolean twoHoles = false;
                                    boolean foundGopher = false;
                                    boolean sameHole = false;
                                    boolean sameHoleThread = false;

                                    // * Set all the variables to see under which scenario it will land in
                                    synchronized (adjacentGopherLock) {
                                        oneHole = checkGuesses(adjacentGopherHoles, guess);
                                    }
                                    synchronized (adjacentGopher2HolesLock) {
                                        twoHoles = checkGuesses(adjacentGopher_TWOHOLES, guess);
                                    }
                                    foundGopher = foundGopher(guess);

                                    synchronized (thread1Lock) {
                                        sameHole = checkGuesses(thread1Guesses, guess);
                                    }
                                    synchronized (thread2Lock) {
                                        sameHoleThread = checkGuesses(thread2Guesses, guess);
                                    }


                                    // * These if statements will trigger what message will be displayed for thread 2's turn
                                    // * Guess a hole that thread 2 has already guessed or the thread guess the same hole
                                    if(sameHole || sameHoleThread){
                                        m2.obj = 4;
                                    }
                                    // * Check if the thread found the gopher
                                    else if(foundGopher){
                                        m2.obj = 1;
                                    }
                                    // * This is a near miss
                                    else  if(oneHole) {
                                        m2.obj = 2;
                                    }

                                    // * This is a close guess
                                    else if(twoHoles){
                                        m2.obj = 3;
                                    }

                                    // * This is a complete miss
                                    else{
                                        m2.obj = 0;
                                    }
                                    synchronized (thread2Lock) {
                                        // * Adding guess onto array list
                                        thread2Guesses.add(guess);
                                    }
                                    synchronized (thread2ResultsLock){
                                        educatedGuesses.add((int)m2.obj);
                                    }
                                    mainHandler.sendMessage(m2) ;

                                    // * See if a winner is found
                                    boolean winner = foundGopher(guess);

                                    // * If a winner is found
                                    if(winner == true) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            System.out.println("Thread interrupted!");
                                        }
                                        m2 = mainHandler.obtainMessage(WINNER);
                                        m2.arg1 = guess;
                                        m2.arg2 = 2;
                                        mainHandler.sendMessage(m2);
                                    }
                                    // * Else continue the game
                                    else {
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            System.out.println("Thread interrupted!");
                                        }
                                        ;

                                        m2 = mainHandler.obtainMessage(END_TURN);
                                        m2.arg1 = -1;
                                        m2.obj = thread1Turn;
                                        mainHandler.sendMessage(m2);

                                        // * See if the continuous mode is selected
                                        if(continuousMODE == 1){
                                            Message continuousM = thread1.handlerWorker1.obtainMessage(WAIT);
                                            thread1.handlerWorker1.sendMessage(continuousM);

                                        }
                                    }
                                }
                            });
                            break;

                        case CONTINUOUS:
                            handlerWorker2.post(new Runnable() {
                                @Override
                                public void run() {
                                   // Toast.makeText(getApplicationContext(), "THREAD 2", Toast.LENGTH_LONG).show();
                                    Message m2 = mainHandler.obtainMessage(MAKE_MOVE);
                                    mainHandler.sendMessage(m2);
                                }
                            });
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}


