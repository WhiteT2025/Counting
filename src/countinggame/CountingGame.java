package countinggame;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Toddler Counting Game (JavaFX)
 *
 * <p>An educational click-to-count activity that displays numbers 1–10 with a
 * twirl animation and an optional background, and plays a per-number sound.
 * When the player reaches 10, a large centered “GOOD JOB !” appears, a prompt
 * “Let’s Play Again:” shows above a green “Play Again” button, and the “Exit Game”
 * button remains available (blue).</p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Shows a number image (1.png … 10.png) and plays a sound (1.mp3 … 10.mp3).</li>
 *   <li>Simple rotate + scale animation on each number.</li>
 *   <li>End-of-loop UI with centered congratulations and replay prompt.</li>
 *   <li>Graceful MediaPlayer cleanup on window close and on each new sound.</li>
 * </ul>
 *
 * <h2>Expected Resources (on the runtime classpath)</h2>
 * <pre>
 *   bluesparklesbackground.png
 *   1.png … 10.png
 *   1.mp3 … 10.mp3
 * </pre>
 * The class searches these classpath locations (first match wins):
 * <pre>
 *   /countinggame/resources/
 *   /countinggame/
 *   /resources/
 *   /
 * </pre>
 * <p><strong>Tip:</strong> The simplest way to ensure loading succeeds without code changes is to put assets under
 * <code>src/countinggame/resources/</code> so they are packaged at <code>/countinggame/resources/...</code> on build.</p>
 *
 * <h2>Running in Eclipse (VM arguments)</h2>
 * <pre>
 *   --module-path "C:\javafx-sdk-24.0.2\lib"
 *   --add-modules javafx.controls,javafx.graphics,javafx.media
 *   --enable-native-access=javafx.graphics
 * </pre>
 *
 * @author Tennie White
 * @version 21.0
 */
public class CountingGame extends Application {

    // =========================
    // Layout & Style Constants
    // =========================

    /** Fixed scene width in pixels (kept square to simplify centering). */
    private static final int WIDTH = 576;
    /** Fixed scene height in pixels. */
    private static final int HEIGHT = 576;
    /** Logical target size for the main number image (ImageView fit size). */
    private static final int IMG_SIZE = 300;

    /** Base button style used during the counting phase. */
    private static final String STYLE_BTN_BASE  = "-fx-font-size: 16px;";
    /** Prominent green style used when the "Play Again" action is available. */
    private static final String STYLE_BTN_GREEN = "-fx-font-size: 16px; -fx-background-color: #2ecc71; -fx-text-fill: white;";
    /** Consistent blue style for the Exit button. */
    private static final String STYLE_BTN_BLUE  = "-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;";

    // =========================
    // Resource Lookup Settings
    // =========================

    /**
     * Classpath search bases (in order). The first location where a resource is found wins.
     * Keep these stable so project layouts remain compatible without code edits.
     */
    private static final String[] SEARCH_BASES = new String[] {
            "/countinggame/resources/",
            "/countinggame/",
            "/resources/",
            "/"
    };

    // =========================
    // Mutable State & Controls
    // =========================

    /** Current number being displayed (1–10). */
    private int currentNumber = 1;

    /** ImageView that displays the number graphic. */
    private ImageView imageView;

    /** Bottom status line (e.g., "This is number 3"). Hidden at end-state. */
    private Label statusLabel;

    /** Primary action button: counts up; becomes "Play Again" at end-state. */
    private Button nextButton;

    /** Secondary action button: exits the application; always visible and blue. */
    private Button exitButton;

    /** Prompt label shown just above the "Play Again" button in end-state. */
    private Label playAgainPrompt;

    /** Large centered congratulatory message shown in end-state. */
    private Label goodJobLabel;

    /**
     * Retained MediaPlayer reference to avoid premature GC while audio is playing.
     * Always stop/dispose before replacing.
     */
    private MediaPlayer mediaPlayer;

    /**
     * Application entry point for JavaFX. Builds the scene graph, wires handlers,
     * and shows the stage.
     *
     * <p>Best practices applied:
     * <ul>
     *   <li>Dispose media resources on close to release native handles.</li>
     *   <li>Bind label centering to root size; avoid magic coordinates.</li>
     *   <li>Keep styles centralized in constants.</li>
     * </ul>
     * </p>
     */
    @Override
    public void start(Stage primaryStage) {
        // Ensure audio device handles are released on app exit.
        primaryStage.setOnCloseRequest(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
        });

        final Pane root = new Pane();

        // ---- Optional background (null-safe) ----
        final ImageView background = new ImageView();
        final Image bg = loadImage("bluesparklesbackground.png");
        if (bg != null) background.setImage(bg);
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);

        // ---- Main number image ----
        imageView = new ImageView();
        imageView.setFitWidth(IMG_SIZE);
        imageView.setFitHeight(IMG_SIZE);
        imageView.setLayoutX((WIDTH - IMG_SIZE) / 2.0);
        imageView.setLayoutY((HEIGHT - IMG_SIZE) / 2.0);
        imageView.setPreserveRatio(true); // avoid distortion

        // ---- Bottom status line ----
        statusLabel = new Label();
        statusLabel.setLayoutX(50);
        statusLabel.setLayoutY(500);
        statusLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: gold; -fx-font-weight: bold;");

        // ---- Primary action (count / replay) ----
        nextButton = new Button("Click to Count");
        nextButton.setLayoutX(180);
        nextButton.setLayoutY(530);
        nextButton.setPrefWidth(180);
        nextButton.setPrefHeight(40);
        nextButton.setStyle(STYLE_BTN_BASE);
        nextButton.setOnAction(evt -> showNext());

        // ---- Exit (always available; blue) ----
        exitButton = new Button("Exit Game");
        exitButton.setLayoutX(380);
        exitButton.setLayoutY(530);
        exitButton.setPrefWidth(120);
        exitButton.setPrefHeight(40);
        exitButton.setStyle(STYLE_BTN_BLUE);
        exitButton.setOnAction(evt -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            System.exit(0);
        });

        // ---- End-state prompt above "Play Again" (hidden until the end) ----
        playAgainPrompt = new Label("Let's Play Again:");
        playAgainPrompt.setStyle("-fx-font-size: 18px; -fx-text-fill: gold; -fx-font-weight: bold;");
        playAgainPrompt.setLayoutX(nextButton.getLayoutX());
        playAgainPrompt.setLayoutY(nextButton.getLayoutY() - 28);
        playAgainPrompt.setVisible(false);

        // ---- Centered end-state "GOOD JOB !" (hidden until the end) ----
        goodJobLabel = new Label("GOOD JOB !");
        goodJobLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: gold; -fx-font-weight: bold;");
        goodJobLabel.setVisible(false);
        // Bind to center within the root pane; avoids hardcoded positions.
        goodJobLabel.layoutXProperty().bind(root.widthProperty().subtract(goodJobLabel.widthProperty()).divide(2));
        goodJobLabel.layoutYProperty().bind(root.heightProperty().subtract(goodJobLabel.heightProperty()).divide(2));

        // Build z-order: background → main content → controls/overlays
        root.getChildren().addAll(background, imageView, statusLabel,
                                  nextButton, exitButton, playAgainPrompt, goodJobLabel);

        // Start with number 1.
        showNumber(currentNumber);

        final Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Toddler Counting Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Displays the given number:
     * <ol>
     *   <li>Loads and shows its image (if present).</li>
     *   <li>Updates status text.</li>
     *   <li>Plays the matching audio clip.</li>
     *   <li>Runs the twirl animation.</li>
     * </ol>
     *
     * <p>Hides end-state labels and ensures the primary button returns to its base style.</p>
     *
     * @param number value from 1 to 10
     */
    private void showNumber(int number) {
        final Image numberImage = loadImage(number + ".png");
        if (numberImage != null) {
            imageView.setImage(numberImage);
        } else {
            // Log rather than throwing; this keeps the activity usable when assets are missing.
            System.out.println("Image not found for number " + number);
            imageView.setImage(null);
        }

        statusLabel.setVisible(true);
        statusLabel.setText("This is number " + number);
        goodJobLabel.setVisible(false);
        playAgainPrompt.setVisible(false);

        // While counting, keep the button neutral.
        nextButton.setStyle(STYLE_BTN_BASE);

        playSound(number);
        animateTwirl();
    }

    /**
     * Plays the audio clip named {@code <number>.mp3} if available.
     *
     * <p>Best practice: stop and dispose any previous {@link MediaPlayer} to release native
     * resources before creating a new one.</p>
     *
     * @param number the number whose sound should be played
     */
    private void playSound(int number) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        final String fileName = number + ".mp3";
        final URL url = findOnClasspath(fileName);
        if (url != null) {
            try {
                mediaPlayer = new MediaPlayer(new Media(url.toExternalForm()));
                mediaPlayer.play();
            } catch (Exception ex) {
                // Fail soft: keep the session alive, just report the issue.
                System.out.println("Failed to play sound '" + fileName + "': " + ex.getMessage());
            }
        } else {
            System.out.println("Sound not found: " + fileName);
        }
    }

    /**
     * Adds a one-second rotate and scale "twirl" effect to the current image.
     *
     * <p>Uses {@link ParallelTransition} to run both animations simultaneously.
     * Auto-reverse on scale provides a subtle "bounce" without additional code.</p>
     */
    private void animateTwirl() {
        final RotateTransition rotate = new RotateTransition(Duration.millis(1000), imageView);
        rotate.setByAngle(360);
        rotate.setCycleCount(1);

        final ScaleTransition scale = new ScaleTransition(Duration.millis(1000), imageView);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        new ParallelTransition(rotate, scale).play();
    }

    /**
     * Advances the counter or transitions to the end-state after 10.
     *
     * <p>End-state behavior:
     * <ul>
     *   <li>Clears the number image.</li>
     *   <li>Hides the status label.</li>
     *   <li>Shows centered “GOOD JOB !” and the “Let’s Play Again:” prompt.</li>
     *   <li>Turns the primary button green and repurposes it to restart.</li>
     * </ul>
     * </p>
     */
    private void showNext() {
        currentNumber++;
        if (currentNumber <= 10) {
            showNumber(currentNumber);
        } else {
            imageView.setImage(null);
            statusLabel.setVisible(false);

            goodJobLabel.setVisible(true);
            playAgainPrompt.setVisible(true);

            nextButton.setText("Play Again");
            nextButton.setStyle(STYLE_BTN_GREEN);
            nextButton.setOnAction(evt -> restart());

            // Exit button remains visible and blue (no change needed).
            exitButton.setVisible(true);
        }
    }

    /**
     * Resets UI and state to start the activity from number 1.
     *
     * <p>Best practices:
     * <ul>
     *   <li>Restore button label, style, and handler.</li>
     *   <li>Hide end-state labels.</li>
     *   <li>Call {@link #showNumber(int)} to refresh visuals and audio.</li>
     * </ul>
     * </p>
     */
    private void restart() {
        currentNumber = 1;

        goodJobLabel.setVisible(false);
        playAgainPrompt.setVisible(false);

        nextButton.setText("Click to Count");
        nextButton.setStyle(STYLE_BTN_BASE);
        nextButton.setOnAction(evt -> showNext());

        statusLabel.setVisible(true);
        showNumber(currentNumber);
    }

    // =========================
    // Resource Loading Helpers
    // =========================

    /**
     * Attempts to load an image by searching {@link #SEARCH_BASES} in order.
     * Returns {@code null} if not found.
     *
     * @param fileName simple file name, e.g. {@code "1.png"}
     * @return Image instance or {@code null} if unavailable
     */
    private Image loadImage(String fileName) {
        final URL url = findOnClasspath(fileName);
        return (url != null) ? new Image(url.toExternalForm()) : null;
        // Note: constructing Image from URL string enables JavaFX to manage resource streams internally.
    }

    /**
     * Locates a resource URL by trying each base folder. This keeps the code tolerant of
     * different project layouts and build tooling, as long as assets are on the classpath.
     *
     * @param fileName simple file name, e.g. {@code "1.mp3"}
     * @return URL to the resource, or {@code null} if not found
     */
    private URL findOnClasspath(String fileName) {
        for (String base : SEARCH_BASES) {
            final URL url = getClass().getResource(base + fileName);
            if (url != null) return url;
        }
        return null;
    }

    /**
     * Standard JavaFX launcher. VM arguments must include the JavaFX module-path
     * when running outside a module-aware packaging solution.
     *
     * @param args CLI args (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
