package com.example.spaceinvaders;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpaceInvadersClone extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private static final String TITLE = "Space Invaders Clone";

    private static final int PLAYER_WIDTH = 60;
    private static final int PLAYER_HEIGHT = 40;
    private static final double PLAYER_SPEED = 5.0;

    private static final int ENEMY_WIDTH = 40;
    private static final int ENEMY_HEIGHT = 30;
    private static final double ENEMY_SPEED = 1.0;
    private static final int ENEMY_ROWS = 4;
    private static final int ENEMY_COLUMNS = 8;
    private static final double ENEMY_VERTICAL_GAP = 50;
    private static final double ENEMY_HORIZONTAL_GAP = 50;

    private static final int BULLET_WIDTH = 4;
    private static final int BULLET_HEIGHT = 10;
    private static final double BULLET_SPEED = 5.0;

    private static final int MAX_AMMO = 15;
    private int ammoCount;

    private Group root;
    private Canvas canvas;
    private GraphicsContext gc;
    private Spaceship spaceship;
    private List<Invader> invaders;
    private List<Laser> lasers;
    private boolean gameRunning;
    private int score;
    private boolean playerMoved;
    private boolean showControls;
    private int level;

    private Image enemyImage;
    private Image spaceshipImage;
    private Image backgroundImage;

    private MediaPlayer mediaPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new Group();
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        spaceship = new Spaceship(WIDTH / 2 - PLAYER_WIDTH / 2, HEIGHT - PLAYER_HEIGHT - 10);
        invaders = new ArrayList<>();
        lasers = new ArrayList<>();
        gameRunning = true;
        score = 0;
        ammoCount = MAX_AMMO;
        playerMoved = false;
        showControls = true;
        level = 1;

        enemyImage = new Image("E:\\Android Applications\\SpaceInvaders\\src\\main\\java\\com\\example\\spaceinvaders\\enemy.png");
        spaceshipImage = new Image("E:\\Android Applications\\SpaceInvaders\\src\\main\\java\\com\\example\\spaceinvaders\\player.png");
        backgroundImage = new Image("E:\\Android Applications\\SpaceInvaders\\src\\main\\java\\com\\example\\spaceinvaders\\background.png");

        initInvaders();

        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));

        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        String musicFile = "E:\\Android Applications\\SpaceInvaders\\src\\main\\java\\com\\example\\spaceinvaders\\background_music.mpeg";
        Media media = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();

        Timeline gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> gameUpdate()));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    private void gameUpdate() {
        if (!gameRunning) {
            return;
        }

        updateSpaceship();
        if (playerMoved) {
            updateInvaders();
            showControls = false;
        }
        updateLasers();

        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Render background image
        gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);

        renderSpaceship();
        renderInvaders();
        renderLasers();
        renderScore();
        renderAmmoCount();
        if (showControls) {
            renderControls();
        }

        checkCollisions();
        checkGameOver();
        checkLevelComplete();
    }

    private void updateSpaceship() {
        if (spaceship.isMovingLeft() && spaceship.getX() > 0) {
            spaceship.moveLeft();
            playerMoved = true;
        } else if (spaceship.isMovingRight() && spaceship.getX() < WIDTH - PLAYER_WIDTH) {
            spaceship.moveRight();
            playerMoved = true;
        }
    }

    private void updateInvaders() {
        boolean moveDown = false;

        for (Invader invader : invaders) {
            if (invader.isVisible()) {
                invader.move();

                if (invader.getX() <= 0 || invader.getX() + ENEMY_WIDTH >= WIDTH) {
                    moveDown = true;
                }
            }
        }

        if (moveDown) {
            for (Invader invader : invaders) {
                if (invader.isVisible()) {
                    invader.moveDown();
                }
            }
        }
    }

    private void updateLasers() {
        for (Laser laser : lasers) {
            if (laser.isVisible()) {
                laser.move();
            }
        }
    }

    private void renderSpaceship() {
        gc.drawImage(spaceshipImage, spaceship.getX(), spaceship.getY(), PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    private void renderInvaders() {
        for (Invader invader : invaders) {
            if (invader.isVisible()) {
                gc.drawImage(enemyImage, invader.getX(), invader.getY(), ENEMY_WIDTH, ENEMY_HEIGHT);
            }
        }
    }

    private void renderLasers() {
        gc.setFill(Color.RED);
        for (Laser laser : lasers) {
            if (laser.isVisible()) {
                gc.fillRect(laser.getX(), laser.getY(), BULLET_WIDTH, BULLET_HEIGHT);
            }
        }
    }

    private void renderScore() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Score: " + score, 10, 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Level: " + level, 10, 40);
    }

    private void renderAmmoCount() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 18));
        gc.fillText("Ammo: " + ammoCount, 10, HEIGHT - 10);
    }

    private void renderControls() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Arrow Keys To Move", WIDTH / 2, HEIGHT / 2);
        gc.fillText("Space to Shoot & R to Reload", WIDTH / 2, HEIGHT / 2 + 20);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void checkCollisions() {
        for (Laser laser : lasers) {
            if (laser.isVisible()) {
                for (Invader invader : invaders) {
                    if (invader.isVisible() && laser.intersects(invader)) {
                        laser.setVisible(false);
                        invader.setVisible(false);
                        score = score + (level * 100);

                        Image destructionImage = new Image("E:\\Android Applications\\SpaceInvaders\\src\\main\\java\\com\\example\\spaceinvaders\\explosion.png");
                        gc.drawImage(destructionImage, invader.getX(), invader.getY(), ENEMY_WIDTH, ENEMY_HEIGHT);

                        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.75), e -> {
                            invader.setVisible(false);
                        }));
                        timeline.play();
                    }
                }
            }
        }
    }

    private void checkGameOver() {
        for (Invader invader : invaders) {
            if (invader.isVisible() && invader.getY() + ENEMY_HEIGHT >= spaceship.getY()) {
                gameRunning = false;
                showGameOver();
                break;
            }
        }
    }

    private void checkLevelComplete() {
        boolean allInvadersDestroyed = true;
        for (Invader invader : invaders) {
            if (invader.isVisible()) {
                allInvadersDestroyed = false;
                break;
            }
        }

        if (allInvadersDestroyed) {
            level++;
            increaseInvaderSpeed();
            clearLasers();
            resetSpaceship();
            initInvaders();
        }
    }

    private void increaseInvaderSpeed() {
        for (Invader invader : invaders) {
            invader.setSpeed(ENEMY_SPEED + level * 0.5);
        }
    }

    private void clearLasers() {
        lasers.clear();
    }

    private void resetSpaceship() {
        spaceship.setX(WIDTH / 2 - PLAYER_WIDTH / 2);
        spaceship.setY(HEIGHT - PLAYER_HEIGHT - 10);
    }

    private void showGameOver() {
        VBox gameOverBox = new VBox();
        gameOverBox.setAlignment(Pos.CENTER);
        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setFont(Font.font("Arial", 36));
        gameOverLabel.setTextFill(Color.WHITE); // Set the fill color to white
        Label scoreLabel = new Label("Score: " + score);
        scoreLabel.setFont(Font.font("Arial", 24));
        scoreLabel.setTextFill(Color.WHITE); // Set the fill color to white
        gameOverBox.getChildren().addAll(gameOverLabel, scoreLabel);

        Scene scene = root.getScene();
        gameOverBox.layoutXProperty().bind(scene.widthProperty().subtract(gameOverBox.widthProperty()).divide(2));
        gameOverBox.layoutYProperty().bind(scene.heightProperty().subtract(gameOverBox.heightProperty()).divide(2));

        root.getChildren().add(gameOverBox);

        // Stop the background music
        mediaPlayer.stop();
    }

    private void initInvaders() {
        for (int row = 0; row < ENEMY_ROWS; row++) {
            for (int column = 0; column < ENEMY_COLUMNS; column++) {
                double x = column * (ENEMY_WIDTH + ENEMY_HORIZONTAL_GAP) + 50;
                double y = row * (ENEMY_HEIGHT + ENEMY_VERTICAL_GAP) + 50;
                invaders.add(new Invader(x, y, ENEMY_SPEED + level * 0.5));
            }
        }
    }

    private void handleKeyPress(KeyCode keyCode) {
        if (keyCode == KeyCode.LEFT) {
            spaceship.setMovingLeft(true);
        } else if (keyCode == KeyCode.RIGHT) {
            spaceship.setMovingRight(true);
        } else if (keyCode == KeyCode.SPACE) {
            if (ammoCount > 0) {
                lasers.add(new Laser(spaceship.getX() + PLAYER_WIDTH / 2 - BULLET_WIDTH / 2, spaceship.getY()));
                ammoCount--;
            }
        } else if (keyCode == KeyCode.R) {
            reloadAmmo();
        }
    }

    private void handleKeyRelease(KeyCode keyCode) {
        if (keyCode == KeyCode.LEFT) {
            spaceship.setMovingLeft(false);
        } else if (keyCode == KeyCode.RIGHT) {
            spaceship.setMovingRight(false);
        }
    }

    private void reloadAmmo() {
        ammoCount = MAX_AMMO;
        int scoreToSubtract = (int) (score * 0.05);
        score -= scoreToSubtract;
    }

    private class Spaceship {
        private double x;
        private double y;
        private boolean movingLeft;
        private boolean movingRight;

        public Spaceship(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public boolean isMovingLeft() {
            return movingLeft;
        }

        public boolean isMovingRight() {
            return movingRight;
        }

        public void setMovingLeft(boolean movingLeft) {
            this.movingLeft = movingLeft;
        }

        public void setMovingRight(boolean movingRight) {
            this.movingRight = movingRight;
        }

        public void moveLeft() {
            x -= PLAYER_SPEED;
        }

        public void moveRight() {
            x += PLAYER_SPEED;
        }
    }

    private class Invader {
        private double x;
        private double y;
        private double speed;
        private boolean visible;

        public Invader(double x, double y, double speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.visible = true;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public void move() {
            x += speed;
        }

        public void moveDown() {
            y += ENEMY_HEIGHT;
            speed *= -1;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }
    }

    private class Laser {
        private double x;
        private double y;
        private boolean visible;

        public Laser(double x, double y) {
            this.x = x;
            this.y = y;
            this.visible = true;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public void move() {
            y -= BULLET_SPEED;
        }

        public boolean intersects(Invader invader) {
            return x + BULLET_WIDTH >= invader.getX() && x <= invader.getX() + ENEMY_WIDTH &&
                    y + BULLET_HEIGHT >= invader.getY() && y <= invader.getY() + ENEMY_HEIGHT;
        }
    }
}
