package com.eden.javafx.test.client;

import com.eden.javafx.test.async.UpdateImageViewAsync;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class MainApp extends Application {

  private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
  private ImageView imageView;
  private Session session;

  @OnOpen
  public void onOpen(Session session) {
    LOGGER.log(Level.INFO, "server connection opened");
    this.session = session;
  }

  @OnMessage(maxMessageSize = 1024 * 1024 * 10)
  public void onMessage(InputStream input) {
      
      int availableBytes = -1;
      try {
          availableBytes = input.available();
     } catch(IOException e) {
         LOGGER.log(Level.SEVERE, "", e);
     }
      
     LOGGER.log(Level.INFO, "New message from server. Bytes: " + availableBytes);
     Platform.runLater(new UpdateImageViewAsync(this.imageView, new Image(input)));
  }

  @OnClose
  public void onClose() {
    LOGGER.log(Level.INFO, "server connection closed");
    connectToWebSocket();
  }

  @Override
  public void start(final Stage primaryStage) {
    connectToWebSocket();

    Button btn = new Button();
    btn.setText("Send Image!");
    btn.setPrefSize(400, 27);
    btn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectAndSendImage(primaryStage);
      }
    });
    imageView = new ImageView();
    imageView.setFitHeight(400);
    imageView.setFitWidth(400);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    AnchorPane root = new AnchorPane();

    AnchorPane.setTopAnchor(btn, 0.0);
    AnchorPane.setLeftAnchor(btn, 0.0);
    AnchorPane.setRightAnchor(btn, 0.0);
    AnchorPane.setTopAnchor(imageView, 27.0);
    AnchorPane.setBottomAnchor(imageView, 0.0);
    AnchorPane.setLeftAnchor(imageView, 0.0);
    AnchorPane.setRightAnchor(imageView, 0.0);

    root.getChildren().add(btn);
    root.getChildren().add(imageView);

    Scene scene = new Scene(root, 400, 427);

    primaryStage.setTitle("Image push!");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private void selectAndSendImage(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image to Send");
    File file = fileChooser.showOpenDialog(stage);
    LOGGER.log(Level.INFO, "Size of choosen file is: " + file.length());
    
    try (InputStream input = new FileInputStream(file);
            OutputStream output = session.getBasicRemote().getSendStream()) {
      byte[] buffer = new byte[1024];
      int read;
      int ctr = 0;
      while ((read = input.read(buffer)) > 0) {
        ctr++;
        LOGGER.log(Level.INFO, "sending image bytes: " + (1024 * (ctr - 1)) + " to " + ((1024 * (ctr - 1)) + read));
        output.write(buffer, 0, read);
      }
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, "sending image failed 1!", ex);
    } catch(Exception ex) {
      LOGGER.log(Level.SEVERE, "sending image failed 2!", ex);        
    } catch(Throwable t) {
       LOGGER.log(Level.SEVERE, "sending image failed 3!", t);       
    }
  }

  private void connectToWebSocket() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    try {
      URI uri = URI.create("ws://localhost:8080/images");
      container.connectToServer(this, uri);
    } catch (DeploymentException | IOException ex) {
      LOGGER.log(Level.SEVERE, "connect to server failed!");
      System.exit(-1);
    }
  }
}