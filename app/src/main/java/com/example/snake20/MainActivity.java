package com.example.snake20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private final List<SnakePoint> snakePointsList = new ArrayList<>();

    private SurfaceView surfaceView;
    private TextView scoreTV;

    private SurfaceHolder surfaceHolder;

    private String movingPosition = "right";

    private int score = 0;

    private static final int pointSize = 36;
    private static final int defaultTalePoints = 1; //СТАНДАРТНЫЙ РАЗМЕР ЗМЕЙКИ

    private static final int snakeColor = android.R.color.darker_gray;//ЦВЕТ ЗМЕЙКИ

    private static int snakeMovingSpeed = 830; //СКОРОСТЬ ЗМЕЙКИ (ОТ 1 ДО 1000)

    private int bonusPositionX, bonusPositionY;
    private Timer timer;

    private Canvas canvas = null;
    private Paint paint = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);

        surfaceView.getHolder().addCallback(this);

        surfaceView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            void onSwipeRight() {
                if (!movingPosition.equals("left")) {
                    movingPosition = "right";
                }
            }

            @Override
            void onSwipeLeft() {
                if (!movingPosition.equals("right")) {
                    movingPosition = "left";
                }
            }

            @Override
            void onSwipeTop() {
                if (!movingPosition.equals("bottom")) {
                    movingPosition = "top";
                }
            }

            @Override
            void onSwipeBottom() {
                if (!movingPosition.equals("top")) {
                    movingPosition = "bottom";
                }
            }
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void init() {
        score = 0;
        scoreTV.setText(String.valueOf(score));

        movingPosition = "right";

        snakePointsList.clear();
        int startPositionX = (pointSize) * defaultTalePoints; //МЕСТО СТАРТА ЗМЕЙКИ

        for (int i = 0; i < defaultTalePoints; i++) {///ДЕЛАЕМ ЗМЕЙКЕ СТАНДАРТНЫЙ РАЗМЕР

            SnakePoint snakePoint = new SnakePoint(startPositionX, pointSize);
            snakePointsList.add(snakePoint);
            startPositionX = startPositionX - (pointSize);

        }
        //ДОБАВЛЯЕМ СЛУЧАЙНЫЕ ОЧКИ НА ЭКРАНЕ ПОЕДАЕМЫЕ ЗМЕЙКОЙ
        addPoint();

        // СТАРТ ИГРЫ И ДВИЖЕНИЯ ЗМЕЙКИ
        onMoveSnake();
    }

    private void addPoint() {
        int surfaceWidth = surfaceView.getWidth() - pointSize * 2;
        int surfaceHeight = surfaceView.getHeight() - pointSize * 2;

        int randomXPosition = new Random().nextInt(surfaceWidth / pointSize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointSize);
        if ((randomXPosition % 2 != 0)) {
            randomXPosition = randomXPosition + 1;

        }
        if ((randomYPosition % 2 != 0)) {
            randomYPosition = randomYPosition + 1;

        }
        bonusPositionX = (pointSize * randomXPosition) + pointSize;
        bonusPositionY = (pointSize * randomYPosition) + pointSize;
    }


    private void onMoveSnake() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                if (headPositionX == bonusPositionX && bonusPositionY == headPositionY) {
                    growSnake();
                    addPoint();
                }

                switch (movingPosition) {
                    case "right":
                        moveSnake(headPositionX + (pointSize * 2), headPositionY);
                        break;
                    case "left":
                        moveSnake(headPositionX - (pointSize * 2), headPositionY);
                        break;
                    case "top":
                        moveSnake( headPositionX, headPositionY - (pointSize * 2));
                        break;
                    case "bottom":
                        moveSnake( headPositionX, headPositionY + (pointSize * 2));
                }


                if (checkGameOver(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY())) {
                    timer.purge();
                    timer.cancel();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Your score =" + score);
                            builder.setTitle("Game Over");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    MainActivity.this.init();
                                }
                            });
                            builder.create().show();
                        }
                    });
                    return;
                }

                //Рисуем игру
                drawGame(snakePointsList);

            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void moveSnake(int headX, int headY){
        int prevX = snakePointsList.get(0).getPositionX();
        int prevY = snakePointsList.get(0).getPositionY();

        snakePointsList.get(0).setPositionX(headX);
        snakePointsList.get(0).setPositionY(headY);

        for (int i = 1; i < snakePointsList.size(); i++) {
            int currentX = snakePointsList.get(i).getPositionX();
            int currentY = snakePointsList.get(i).getPositionY();

            snakePointsList.get(i).setPositionX(prevX);
            snakePointsList.get(i).setPositionY(prevY);

            prevX = currentX;
            prevY = currentY;
        }

    }
    private void drawGame(List<SnakePoint> snakePointsList){

        canvas = surfaceHolder.lockCanvas();
        Log.d("drawGame", "start drawing");

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        canvas.drawPaint(paint);

        paint.setColor(Color.RED);
        canvas.drawCircle(bonusPositionX, bonusPositionY, pointSize, paint);

        for (int i = 0; i < snakePointsList.size(); i++) {
            if(i == 0)
                paint.setColor(getResources().getColor(R.color.green));
            else
                paint.setColor(getResources().getColor(R.color.dark_green));

            canvas.drawRect(
                    snakePointsList.get(i).getPositionX() + pointSize,
                    snakePointsList.get(i).getPositionY() + pointSize,
                    snakePointsList.get(i).getPositionX() - pointSize,
                    snakePointsList.get(i).getPositionY() - pointSize,
                    paint);
        }

        surfaceHolder.unlockCanvasAndPost(canvas);
    }
    private void growSnake() {
        //должна быть какая-то координата
        SnakePoint snakePoints = new SnakePoint(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY());
        snakePointsList.add(snakePoints);
        score++;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTV.setText(String.valueOf(score));
            }
        });

    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {
        if (headPositionX < 0 ||
                headPositionY < 0 ||
                headPositionX >= surfaceView.getWidth() ||
                headPositionY >= surfaceView.getHeight()) {
            return true;
        }
        for (int i = 1; i < snakePointsList.size(); i++) {
            if(snakePointsList.get(i).getPositionX() == headPositionX &&
                    snakePointsList.get(i).getPositionY() == headPositionY)
                return true;
        }
        return false;
    }
}