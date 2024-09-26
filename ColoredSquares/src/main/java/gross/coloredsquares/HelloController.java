package gross.coloredsquares;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;


public class HelloController {
    @FXML
    private Label colorStrip;
    @FXML
    private Text score;
    @FXML
    private Text newPoints;
    private int scoreNum;
    @FXML
    private Label welcomeMessage;
    @FXML
    public Button startButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Pane mainPane;
    private final Random random = new Random();
    private int rand;

    // Variables to manage game time controls
    private Timer timer;
    private TimerTask task;
    private Timeline timeline;
    private ArrayList<PauseTransition> pauseTransitions;
    private int counter = 0;
    private boolean isPaused = false;
    private final int MAX_TASKS = 20;
    private final int SQUARE_SIZE = 35;
    private final Color[] colors = {
            Color.color(0.9, 0.2, 0.0),
            Color.color(0.0, 0.9, 0.0),
            Color.color(0.0, 0.7, 0.9),
            Color.color(0.9, 0.9, 0.0),
            Color.color(0.9, 0.5, 0.9)
    };
    private final double[] seconds = {2.5, 3.0, 3.5, 4.0};
    private final int[] points = {5, 3, 2, 1};

    @FXML
    protected void onStartButtonClick() throws InterruptedException {

        // Start Button disappears
        startButton.setVisible(false);
        startButton.setDisable(true); // no longer clickable
        welcomeMessage.setVisible(false);

        pauseButton.setVisible(true);
        colorStrip.setVisible(true);
        score.setVisible(true);

        pauseTransitions = new ArrayList<>();
        startTimer();
    }

    public void startTimer()
    {
        // Create a Timer and TimerTask that runs every 2 seconds, up to 100 times
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                // Change colors
                int temp = rand;
                // Prevents the same color from being repeated back to back
                while (temp == rand) {
                    rand = random.nextInt(0, 5);
                }
                colorStrip.setBackground(Background.fill(colors[rand]));
                counter++;

                // Create a Timeline that generates squares every quarter of a second
                timeline = new Timeline(new KeyFrame(Duration.millis(250), event -> {
                    popupSquares(mainPane);  // Add a square at each interval
                }));
                timeline.setCycleCount(8);  // Run 8 cycles (2 seconds)
                timeline.play();  // Start the timeline

                // Stop the timer after 100 executions
                if (counter >= MAX_TASKS) {
                    timer.cancel();  // Stop the color strip
                    if (timeline != null) {
                        timeline.stop();// Stop the squares
                    }
                    endGame(); // display score
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 2000);  // 0 delay, 2 second period
    }

    @FXML
    protected void onPauseButtonClick() throws InterruptedException {
        // Change text between 'Pause' and 'Resume'
        if (!isPaused)
        {
            isPaused = true;
            timer.cancel();
            timeline.stop();
            for (PauseTransition pause : pauseTransitions) {
                pause.stop();
            }
            pauseButton.setText("Resume");
        }
        else {
            isPaused = false;
            pauseButton.setText("Pause");
            startTimer();
        }
    }

    public void popupSquares(Pane pane)
    {
        rand = random.nextInt(0, 5);
        Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);

        // Randomize square's position within pane
        double x = random.nextDouble() * (pane.getWidth() - SQUARE_SIZE);
        double y = random.nextDouble() * (pane.getHeight() - SQUARE_SIZE);
        square.setX(x);
        square.setY(y);

        // Set a random color for the square
        square.setFill(colors[rand]);

        // Set # of points based on time displayed
        rand = random.nextInt(0, 4);
        double secsDisplayed = seconds[rand];
        int pts = points[rand];

        // Create Text to display number of Points
        Text text = new Text("" + pts);
        text.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 16)); // Set font size

        // Manually position the text at the center of the rectangle
        double textWidth = text.getLayoutBounds().getWidth();
        double textHeight = text.getLayoutBounds().getHeight();
        text.setX(square.getX() + (square.getWidth() - textWidth) / 2);
        text.setY(square.getY() + (square.getHeight() + textHeight - 2) / 2);

        // Add both the Rectangle and the Text to the pane
        pane.getChildren().addAll(square, text);

        square.setOnMouseClicked(event -> {
            onMouseClick(square, text, pts);
        });

        // Schedule the square to disappear after 5 seconds
        if (square != null) {
            scheduleDisappear(square, text, pane, secsDisplayed);
        }
    }

    public void popupText(Text text)
    {
        text.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> text.setVisible(false));  // Hide the text after 3 seconds
        pause.play();
    }

    public void onMouseClick(Rectangle square, Text txt, int pts)
    {
        Color squareColor = (Color) square.getFill();
        Color labelColor = (Color) colorStrip.getBackground().getFills().get(0).getFill();
        if (squareColor == labelColor) {
            scoreNum += pts;
            score.setText("Score:   " + scoreNum);
            newPoints.setText("+ " + pts);
            newPoints.setFill(Color.GREEN);
        }
        else {
            scoreNum -= pts;
            score.setText("Score:   " + scoreNum);
            newPoints.setText("- " + pts);
            newPoints.setFill(Color.RED);
        }

        popupText(newPoints);
        // Remove the square when clicked
        mainPane.getChildren().removeAll(square, txt);
    }

    // Method to schedule the square's disappearance after a certain time
    private void scheduleDisappear(Rectangle square, Text txt, Pane pane, double secsDisplayed) {
        PauseTransition pause = new PauseTransition(Duration.millis(secsDisplayed*1000)); // Set time to disappear - between 1.5 and 3 seconds
        pause.setOnFinished(event -> pane.getChildren().removeAll(square, txt)); // Remove the square when time is up
        pause.play();  // Start the pause transition

        pauseTransitions.add(pause);
    }

    public void endGame()
    {
        // Disable Pause button
        pauseButton.setVisible(false);
        pauseButton.setDisable(true);
        colorStrip.setVisible(false);
        score.setVisible(false);

        Platform.runLater(() -> {
            // Clear pane of all squares
            mainPane.getChildren().clear();
            Label endMessage = new Label();
            mainPane.getChildren().add(endMessage);
            // Manually center the label in the pane by binding its layout position
            endMessage.layoutXProperty().bind(mainPane.widthProperty().subtract(endMessage.widthProperty()).divide(2));
            endMessage.layoutYProperty().bind(mainPane.heightProperty().subtract(endMessage.heightProperty()).divide(2));

            endMessage.setAlignment(Pos.CENTER);
            endMessage.setFont(Font.font(48));
            endMessage.setText("Nice Job!\n" + score.getText());
        });
    }

}