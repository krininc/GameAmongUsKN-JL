import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;
import javafx.geometry.*;
import javafx.animation.*;
import java.io.*;
import java.util.*;
import javafx.scene.input.KeyEvent;

/**
 * AmongUSStarter with JavaFX and Threads
 * Loading imposters
 * Loading background
 * Control actors and backgrounds
 * Create many number of imposters - random controlled
 * RGB based collision
 * Collsion between two imposters
 */

public class Game2DCleanProf extends Application {
   // Window attributes
   private Stage stage;
   private Scene scene;

   private static String[] args;

   private final static String CREWMATE_IMAGE = "Blue.png"; // file with icon for a racer
   private final static String CREWMATE_RUNNERS = "Yellow.png"; // file with icon for a racer
   private final static String BACKGROUND_IMAGE = "background3.0.png";
   private final static String CREWMATE_RUNNING = "running.gif";
   private final static String CREWMATE_LEFT = "BlueLeft.png";
   private final ImageView aPicView = new ImageView(CREWMATE_IMAGE);
   private int OldX = -1;
   private int OldY = -1;
   private String lastMove = "";

   private MediaPlayer mediaPlayer;
   String fileName = "music.mp3";

   private Menu menu;
   private MenuBar menuBar;
   private MenuItem miAudio;

   private Slider volumeSlider;

   // crewmates
   private CrewmateRacer masterCrewmate = null;
   private ArrayList<CrewmateRacer> robotCrewmates = new ArrayList<>();

   // movable background
   private MovableBackground movableBackground = null;

   private Image backgroundCollision = null;

   // Animation timer
   private AnimationTimer timer = null;
   private int counter = 0;
   private boolean moveUP = false, moveDown = false, moveRight = false, moveLeft = false;

   private StackPane root;
   private FlowPane topPane;
   private FlowPane midPane;
   private FlowPane bottomPane;
   private Button btnAudio;
   private Button btnPlay;

   private Button audioSettings;
   private Button colorPicker;
   private int sceneXx = 1200;
   private int sceneYy = 700;

   public static final ObservableList<String> color = FXCollections.observableArrayList("Blue", "Yellow", "Red",
         "Purple", "Pink", "Orange", "Light Green", "Green", "Dark Green",
         "Cyan", "Beige", "Brown");

   private ComboBox<String> cmbColor = new ComboBox<>(color);

   // main program
   public static void main(String[] _args) {
      args = _args;
      launch(args);
   }

   // start() method, called via launch
   public void start(Stage _stage) {
      // stage seteup
      stage = _stage;
      stage.setTitle("Among us - Final Project K.N. & J.L.");
      stage.setOnCloseRequest(
            new EventHandler<WindowEvent>() {
               public void handle(WindowEvent evt) {
                  System.exit(0);
               }
            });

      // root pane

      btnPlay = new Button("Play");
      btnAudio = new Button("Turn On Audio");
      btnAudio.setOnAction(new EventHandler<ActionEvent>() {

         @Override
         public void handle(ActionEvent arg0) {
            // TODO Auto-generated method stub
            if (btnAudio.getText().equals("Turn On Audio")) {
               doSound(fileName);
               btnAudio.setText("Turn Off Audio");
            } else if (btnAudio.getText().equals("Turn Off Audio")) {
               doSoundOff(fileName);
               btnAudio.setText("Turn On Audio");
            }

         }

      });

      btnPlay.setPrefWidth(150);
      btnPlay.setPrefHeight(50);

      btnPlay.setOnAction(new EventHandler<ActionEvent>() {

         @Override
         public void handle(ActionEvent arg0) {
            initializeScene();

         }

      });

      /* Adjust volume */

      volumeSlider = new Slider(0, 100, 50);
      volumeSlider.setShowTickLabels(true);
      volumeSlider.setShowTickMarks(true);
      volumeSlider.setMajorTickUnit(50);
      volumeSlider.setMinorTickCount(5);
      volumeSlider.setBlockIncrement(10);

      volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {

         @Override
         public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            double volume = newValue.doubleValue() / 100.0;
            mediaPlayer.setVolume(volume);
         }

         
      });
      volumeSlider.setDisable(true);

      audioSettings = new Button("Audio settings");
      audioSettings.setPrefWidth(100);
      audioSettings.setPrefHeight(35);

      audioSettings.setOnAction(e -> {
         FlowPane audioSettingsPane = new FlowPane();
         FlowPane audioSettingsPane2 = new FlowPane();
         FlowPane audioSettingsPane3 = new FlowPane();
         FlowPane audioSettingsPane4 = new FlowPane();

         Button back = new Button("<-- Back to main screen");

         back.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               // TODO Auto-generated method stub
               stage.setScene(scene);
            }
         });
         Label adjustAudio = new Label("Adjust music volume: ");

         audioSettingsPane3.getChildren().addAll(back);
         audioSettingsPane.getChildren().addAll(btnAudio);
         audioSettingsPane2.getChildren().addAll(volumeSlider);
         audioSettingsPane4.getChildren().addAll(adjustAudio);

         audioSettingsPane.setAlignment(Pos.CENTER);
         audioSettingsPane2.setAlignment(Pos.CENTER);
         audioSettingsPane4.setAlignment(Pos.CENTER);
         audioSettingsPane3.setAlignment(Pos.TOP_LEFT);

         GridPane audioPane = new GridPane();
         audioPane.getChildren().addAll(audioSettingsPane3, audioSettingsPane, audioSettingsPane4, audioSettingsPane2);
         audioPane.setRowIndex(audioSettingsPane3, 0);
         audioPane.setRowIndex(audioSettingsPane, 1);
         audioPane.setRowIndex(audioSettingsPane2, 3);
         audioPane.setRowIndex(audioSettingsPane4, 2);
         audioPane.setVgap(20);
         audioPane.setAlignment(Pos.CENTER);
         Scene audioSettingsScene = new Scene(audioPane, 1200, 700);
         Image backgroundImage = new Image("amongus.jpg");
         BackgroundImage background = new BackgroundImage(backgroundImage, null, null, null, null);
         audioPane.setBackground(new Background(background));
         stage.setScene(audioSettingsScene);
         stage.show();
      });
      colorPicker = new Button("Pick the color of your character");
      colorPicker.setPrefWidth(100);
      colorPicker.setPrefHeight(35);
      colorPicker.setOnAction(e -> {
         FlowPane pane1 = new FlowPane();
         FlowPane pane2 = new FlowPane();
         FlowPane pane3 = new FlowPane();

         Button back = new Button("<-- Back to main screen");

         back.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
               // TODO Auto-generated method stub
               stage.setScene(scene);
            }
         });
         Label lblCharColor = new Label("Pick the color of your character: ");

         pane1.getChildren().addAll(back);
         pane2.getChildren().addAll(cmbColor);
         pane1.setAlignment(Pos.TOP_LEFT);
         pane2.setAlignment(Pos.CENTER);
         pane3.getChildren().addAll(lblCharColor);
         pane3.setAlignment(Pos.CENTER);

         /* !!!!!!!!!!! OVO DAJE ERROR NE ZNAM ZASTO !!!!!!!!!!! */
         // cmbColor.setOnAction(new EventHandler<ActionEvent>() {

         // @Override
         // public void handle(ActionEvent event) {
         // String selectedColor = cmbColor.getValue();
         // String image = selectedColor + ".png";
         // masterCrewmate.setImage(CREWMATE_RUNNERS);
         // }
         // });

         GridPane colorPane = new GridPane();
         colorPane.getChildren().addAll(pane1, pane3, pane2);
         colorPane.setRowIndex(pane1, 0);
         colorPane.setRowIndex(pane2, 2);
         colorPane.setRowIndex(pane3, 1);
         colorPane.setVgap(20);
         colorPane.setAlignment(Pos.CENTER);
         Scene colorPickerScene = new Scene(colorPane, 1200, 700);
         Image backgroundImage = new Image("amongus.jpg");
         BackgroundImage background = new BackgroundImage(backgroundImage, null, null, null, null);
         colorPane.setBackground(new Background(background));
         stage.setScene(colorPickerScene);
         stage.show();
      });

      topPane = new FlowPane(20, 20);
      topPane.getChildren().addAll(btnPlay);
      topPane.setAlignment(Pos.CENTER);
      midPane = new FlowPane(20, 20);
      midPane.getChildren().addAll(audioSettings);
      midPane.setAlignment(Pos.CENTER);
      bottomPane = new FlowPane(20, 20);
      bottomPane.getChildren().addAll(colorPicker);
      bottomPane.setAlignment(Pos.CENTER);

      GridPane gPane = new GridPane();
      gPane.getChildren().addAll(topPane, midPane, bottomPane);
      gPane.setRowIndex(topPane, 0);
      gPane.setRowIndex(midPane, 1);
      gPane.setRowIndex(bottomPane, 2);
      gPane.setVgap(10);
      gPane.setAlignment(Pos.CENTER);
      // midPane.getChildren().addAll(audioSettings);

      Image backgroundImage = new Image("amongus.jpg");
      BackgroundImage background = new BackgroundImage(backgroundImage, null, null, null, null);
      gPane.setBackground(new Background(background));

      root = new StackPane();

      scene = new Scene(gPane, sceneXx, sceneYy);

      stage.setScene(scene);
      stage.getIcons().add(new Image("amongus.jpg"));
      stage.show();
   }

   // start the game scene
   private void initializeScene() {

      masterCrewmate = new CrewmateRacer();
      for (int i = 0; i < 5; i++) {
         CrewmateRacer cR = new CrewmateRacer();
         robotCrewmates.add(cR);
      }
      // create background
      CrewmateRacer cr = new CrewmateRacer();
      movableBackground = new MovableBackground(cr);

      // add background
      this.root.getChildren().add(movableBackground);
      // add to the root
      this.root.getChildren().add(masterCrewmate);
      this.root.getChildren().addAll(robotCrewmates);


      scene = new Scene(root, 1200, 700);

      stage.setScene(scene);
      stage.show();

      // KEYBOARD CONTROL

      scene.setOnKeyPressed(
            new EventHandler<KeyEvent>() {

               @Override
               public void handle(KeyEvent event) {

                  switch (event.getCode()) {
                     case UP:
                        moveUP = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                     case DOWN:
                        moveDown = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                     case LEFT:
                        moveLeft = true;
                        // masterCrewmate.setImage(CREWMATE_LEFT);

                        break;
                     case RIGHT:
                        moveRight = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                     case A:
                        moveLeft = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        // masterCrewmate.setImage(CREWMATE_LEFT);

                        break;
                     case W:
                        moveUP = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                     case D:
                        moveRight = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                     case S:
                        moveDown = true;
                        // masterCrewmate.setImage(CREWMATE_RUNNING);
                        break;
                  }

               }
            });

      scene.setOnKeyReleased(
            new EventHandler<KeyEvent>() {
               @Override
               public void handle(KeyEvent event) {
                  switch (event.getCode()) {
                     case UP:
                        moveUP = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                     case DOWN:
                        moveDown = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                     case LEFT:
                        moveLeft = false;
                        masterCrewmate.setImage(CREWMATE_IMAGE);
                    

                        break;
                     case RIGHT:
                        moveRight = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;

                     case A:
                        moveLeft = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                     case W:
                        moveUP = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                     case D:
                        moveRight = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                     case S:
                        moveDown = false;
                        // masterCrewmate.setImage(CREWMATE_IMAGE);
                        break;
                  }

               }
            });

      backgroundCollision = new Image(BACKGROUND_IMAGE);

      timer = new AnimationTimer() {
         @Override
         public void handle(long now) {

            for (int i = 0; i < robotCrewmates.size(); i++) {
               robotCrewmates.get(i).update();
            }

            movableBackground.update();
         }
      };
      timer.start();
   }

   private void doSound(String fileName) {

      String path = getClass().getResource(fileName).getPath();
      Media media = new Media(new File("src/music.mp3").toURI().toString());
      mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      volumeSlider.setDisable(false);
      mediaPlayer.play();

   }

   private void doSoundOff(String fileName) {
      if (mediaPlayer != null) {
         mediaPlayer.stop();
         volumeSlider.setDisable(true);
         mediaPlayer = null;
      }

   }

   // inner class
   class CrewmateRacer extends Pane {
      private int racerPosX = 500;
      private int racerPosY = 300;
      private ImageView aPicView = null;


      private boolean isMaster = true;
      private double SceneX = sceneXx / 3000.0;
      private double SceneY = sceneYy / 1687.0;

      public CrewmateRacer() {
         this.isMaster = isMaster;

         aPicView = new ImageView(CREWMATE_IMAGE);
         aPicView.setTranslateX(racerPosX);
         aPicView.setTranslateY(racerPosY);
        this.getChildren().add(aPicView);
      }

      public void update() {

         double speed = 2.2;

      }

      public void setImage(String imageFileName) {
         aPicView.setImage(new Image(imageFileName));
      }

      public int getRacerPosX() {
         return racerPosX;
      }

      public int getRacerPosY() {
         return racerPosY;
      }

      public void setRacerPosX(int value) {
         if (value != OldX)
            System.out.println("Set pos X: " + value);
         racerPosX = value;
      }

      public void setRacerPosY(int value) {
         if (value != OldY)
            System.out.println("Set pos Y: " + value);
         racerPosY = value;
      }

   }

   // background
   class MovableBackground extends Pane {

      private ImageView aPicView = null;
      private Image backgroundImage = null;
      private CrewmateRacer crewmateRacer;
      private double sceneX = sceneXx / 3000.0;
      private double sceneY = sceneYy / 1687.0;
      private boolean gameRunning = true;
      Task1 task1 = new Task1();
      Task2 task2 = new Task2();
      Task3 task3 = new Task3();

      private int BackgroundPosX = 0;
      private int BackgroundPosY = 0;

      public MovableBackground(CrewmateRacer cr) {
         this.crewmateRacer = cr;
         aPicView = new ImageView(BACKGROUND_IMAGE);
         aPicView.setTranslateX(BackgroundPosX);

         aPicView.setTranslateY(BackgroundPosY);
         this.getChildren().add(aPicView);

      }

      public void update() {
         double speed = 3;
         PixelReader pixelReader = backgroundCollision.getPixelReader();
         Color color = pixelReader.getColor((int) crewmateRacer.getRacerPosX(), (int) crewmateRacer.getRacerPosY());

         if ((int) crewmateRacer.getRacerPosX() >= 165 && (int) crewmateRacer.getRacerPosX() <= 250
               && (int) crewmateRacer.getRacerPosY() >= 471 && (int) crewmateRacer.getRacerPosY() <= 555) {
            if(!task1.complete){
            startTask1();
               moveLeft = false;
               moveRight = false;
               moveUP = false;
               moveDown = false;
            }  

         }
         if (color.equals(Color.RED)) {

            if(!task2.complete){
               startTask2();
                  moveLeft = false;
                  moveRight = false;
                  moveUP = false;
                  moveDown = false;
               }  

         }
         if ((int) crewmateRacer.getRacerPosX() >= 2230 && (int) crewmateRacer.getRacerPosX() <= 2410
               && (int) crewmateRacer.getRacerPosY() >= 1450 && (int) crewmateRacer.getRacerPosY() <= 1525) {

                  if(!task3.complete){
                     startTask3();
                        moveLeft = false;
                        moveRight = false;
                        moveUP = false;
                        moveDown = false;
                     }  
         }

         if(task1.complete && task2.complete && task3.complete){
            timer.stop();
            endGame();
           
          
            
         }
         ArrayList<Color> colorList = new ArrayList<Color>();
         boolean allowedMove = false;

         if (moveDown) {
            for (int i = 0; i < (int) (speed + 1); i++) {
               colorList.add(pixelReader.getColor((int) crewmateRacer.getRacerPosX(),
                     (int) crewmateRacer.getRacerPosY() + i + 1));
            }

            allowedMove = colorList.contains(Color.BLACK) ? false : true;
         } else if (moveUP) {
            for (int i = 0; i < (int) (speed + 1); i++) {
               colorList.add(pixelReader.getColor((int) crewmateRacer.getRacerPosX(),
                     (int) crewmateRacer.getRacerPosY() - i - 1));
            }

            allowedMove = colorList.contains(Color.BLACK) ? false : true;
         }

         else if (moveLeft) {
            for (int i = 0; i < (int) (speed + 1); i++) {
               colorList.add(pixelReader.getColor((int) crewmateRacer.getRacerPosX() - i - 1,
                     (int) crewmateRacer.getRacerPosY()));
            }

            allowedMove = colorList.contains(Color.BLACK) ? false : true;
         } else if (moveRight) {
            for (int i = 0; i < (int) (speed + 1); i++) {
               colorList.add(pixelReader.getColor((int) crewmateRacer.getRacerPosX() + i + 1,
                     (int) crewmateRacer.getRacerPosY()));
            }

            allowedMove = colorList.contains(Color.BLACK) ? false : true;
         }
         // System.out.println(color);

         if (color.equals(Color.BLACK) && !allowedMove) {
            System.out.println(color);
            speed = 0.1;
         }

         if (moveDown && allowedMove) {
            BackgroundPosY -= speed;
            crewmateRacer.setRacerPosY((int) (crewmateRacer.getRacerPosY() + speed));
            lastMove = "moveDown";
         }
         if (moveUP && allowedMove) {
            BackgroundPosY += speed;

            crewmateRacer.setRacerPosY((int) (crewmateRacer.getRacerPosY() - speed));
            lastMove = "moveUp";

         }
         if (moveLeft && allowedMove) {
            BackgroundPosX += speed;

            crewmateRacer.setRacerPosX((int) (crewmateRacer.getRacerPosX() - speed));
            lastMove = "moveLeft";
         }
         if (moveRight && allowedMove) {
            BackgroundPosX -= speed;
            crewmateRacer.setRacerPosX((int) (crewmateRacer.getRacerPosX() + speed));
            lastMove = "moveRight";
         }

         aPicView.setTranslateX(BackgroundPosX);
         aPicView.setTranslateY(BackgroundPosY);

      }
      private boolean hasStarted = false;
      private boolean hasStarted2 = false;
      private boolean hasStarted3 = false;

      private void startTask1() {   
         if (!hasStarted) {
         try {
            task1.start(new Stage());
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         hasStarted = true;
            }
      }

      private void startTask2() {

         if (!hasStarted2) {
            try {
               task2.start(new Stage());
            } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            hasStarted2 = true;
         }
   
      }
   
      private void startTask3() {
   
         if (!hasStarted3) {
             try {
               task3.start(new Stage());
            } catch (Exception e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            hasStarted3 = true;
         }
   
      }
   }

 
private void endGame(){
   Platform.runLater(() ->{
      Alert alert = new Alert(AlertType.CONFIRMATION, "Congratulations");
      alert.setHeaderText("Congratulations you won the game!");
      alert.setContentText("You are victorious!");
     
      alert.showAndWait();
      this.start(stage);
  

   });
  
}
   public double getPlayerHeight() {
      return this.aPicView.getImage().getHeight();
   }

   public double getPlayerWidth() {
      return this.aPicView.getImage().getWidth();
   }

} // end class Races