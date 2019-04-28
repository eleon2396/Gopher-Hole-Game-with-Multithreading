package com.example.project4cs478eleon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

/*
    * Eric Leon eleon23 654889611
    * CS 478 Project 4: Gopher Hunting on Android
    *   This Application is a Gopher Hunting game on Android where two workers threads will take turns searching/finding the gopher
    *   in a 10x10 Matrix. This application will support two modes:
    *    1) Guess by Guess Mode: Here the user selects appropriate buttons to decide when to let the threads make a guess.
    *    2) Continuous Mode: Here the two workers threads will play without interruption until one thread wins the game.
    *  The user will be able to select which game mode to select.

     *   Note: Worker thread 1 will have the color red to denote its guesses. Worker thread 2 will have the color blue to denote its guesses.
     *   Every time a thread makes a guess one of the following five messages will be provided from the game system:
     *       1)Success: The thread guesses the hole containing the gopher and wins the game.
     *       2)Near-Miss: THe thread guessed one of the 8 holes adjacent to the gopher's hole. THis includes horizontally, vertically, diagonally. The game continues with the other worker thread's turn.
     *       3)Close Guess: The thread guesses a hole that's 2 holes away from the gopher's hole in any directions. This includes two holes horizontally, vertically, or diagonally.
     *       4)Complete miss: The thread get this response if they are no where near the gopher and the above cases fail.
     *       5)Disaster: The thread accidentally guesses a hole that has already been guessed, either by the same thread itself or the opponent thread.
 */


public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         Button startGameGuess;
         Button startGameContinuous;
        // *Start the game in Guess By Guess mode
        startGameGuess = (Button)findViewById(R.id.startInGuessModeButton);
        startGameGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent start = new Intent(getApplicationContext(), gopherMazeActivity.class);
                start.putExtra("com.MODE", 1);
                startActivity(start);
            }
        });

//        // *Start the game in Continuous Mode
//        startGameContinuous = (Button)findViewById(R.id.startContinuousButton);
//        startGameContinuous.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent start = new Intent(getApplicationContext(), gopherMazeActivity.class);
//                start.putExtra("com.MODE", 2);
//                startActivity(start);
//            }
//        });


    }

}
