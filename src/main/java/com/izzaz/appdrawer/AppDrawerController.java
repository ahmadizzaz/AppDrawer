package com.izzaz.appdrawer;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import com.izzaz.appdrawer.model.AppFile;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.SneakyThrows;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static com.izzaz.appdrawer.CommonUtils.*;

public class AppDrawerController implements Initializable {


    @FXML
    private AnchorPane mainView;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button btn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button resizeButton;

    @FXML
    private Button minimizeButton;

    @FXML
    private VBox categoryVBox;

    @FXML
    private HBox topBorder;

    @FXML
    private VBox webviewVBox;

    @FXML
    private WebView webView;


    protected String appDataLocalDir = System.getenv("LOCALAPPDATA") + "\\IzzazAppDrawer\\";
    protected String appDataLocalDirIcon = System.getenv("LOCALAPPDATA") + "\\IzzazAppDrawer\\icon\\";
    protected String appDataLocalDirApp = System.getenv("LOCALAPPDATA") + "\\IzzazAppDrawer\\app\\";

    protected List<AppFile> appFileList = new ArrayList<>();


    CookieManager manager = new CookieManager();
    WebEngine webEngine;


    @FXML
    protected void onHelloButtonClick() {

        Stage stage= (Stage)btn.getScene().getWindow();
        FileChooser fileC = new FileChooser();
        fileC.setTitle("Open File");
        File file = fileC.showOpenDialog(stage);
        if (Objects.nonNull(file)){
            var fileNameNoExt = FilenameUtils.removeExtension(file.getName());
            try{
                String appLocation = appDataLocalDirApp + fileNameNoExt + ".txt";
                String imgLocation = appDataLocalDirIcon + fileNameNoExt + ".png";
                File newApp = new File(appLocation);
                AppFile appFile = AppFile.builder()
                        .id(fileNameNoExt)
                        .displayName(fileNameNoExt)
                        .pathToApp(file.getAbsolutePath())
                        .pathToImage(imgLocation)
                        .htmlCode(generateHtmlCode(imgLocation,fileNameNoExt,fileNameNoExt))
                        .build();

                FileUtils.writeStringToFile(newApp, writeAppToFile(appFile), Charset.defaultCharset());
                Files.createDirectories(Paths.get(appDataLocalDirIcon));
                File pngFile = new File(imgLocation);
                var image = getImageViewFromPath(file.getAbsolutePath(),720,720);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(renderedImage,"png",pngFile);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        refreshButton.fire();


    }


    public String loadApps(){
        appFileList.clear();
        AtomicReference<String> fullWebViewHtml = new AtomicReference<>("");
        if (Files.exists(Path.of(appDataLocalDirApp))){
            ArrayList<File>fileList = (ArrayList<File>) FileUtils.listFiles(new File(appDataLocalDirApp),null,true);

            fileList.forEach(file -> {
                //Create individual items for grid
                if (file.getName().contains(".txt")){
                    try {
                        var content = FileUtils.readLines(file,Charset.defaultCharset());
                        var appFile = AppFile.builder()
                                .id(content.get(0).replace("id=",""))
                                .displayName(content.get(1).replace("displayName=",""))
                                .pathToApp(content.get(2).replace("pathToApp=",""))
                                .pathToImage(content.get(3).replace("pathToImage=",""))
                                .htmlCode(content.get(4).replace("htmlCode=",""))
                                .build();

                        appFileList.add(appFile);
                        fullWebViewHtml.set(fullWebViewHtml + appFile.getHtmlCode());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return structureHtmlCode(fullWebViewHtml.get());
    }

    private void refreshPage(String html){
        manager.getCookieStore().removeAll();
        webEngine.loadContent(html);
        webEngine.reload();
    }

    public void processOnClickOpen(String id) {
        log("Before open");
        onAppOpen(id);
        log("After open");
    }

    public void processOnClickOption(String id) {
        log("Before Delete");
        log(id);
        onEditButtonClick(id);
        log("After Delete");

    }

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        refreshButton.setOnAction(event -> {
            refreshPage(loadApps());
        });

        //Init WebView
        webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue)-> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("app", this);
            }
        });

        String css1 = this.getClass().getResource("imagehover.css").toExternalForm();
        webEngine.setUserStyleSheetLocation(css1);
        webEngine.setJavaScriptEnabled(true);
        var html = loadApps();
        webEngine.loadContent(html);

        //Init WebViewBox
        webviewVBox.setBackground(new Background(new BackgroundFill(Color.web("#72757e"), new CornerRadii(20), Insets.EMPTY)));


    }

    public void onAppOpen(String id) {
        var app = appFileList.stream()
                .filter(file -> file.getId().equals(id))
                .findFirst();
        if (app.isPresent()){
            var appPath = app.get().getPathToApp();
            File appFile = new File(appPath);
            if (appFile.exists()){
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", appFile.getAbsolutePath(), "-n", "100");
                try {
                    Process process = pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onEditButtonClick(String id) {
        mainView.setEffect(new GaussianBlur());
        var appFileOpt = appFileList.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
        if (appFileOpt.isPresent()) {
            var appFile = appFileOpt.get();

            //App name and Image
            VBox vbox1 = new VBox();
            GridPane gridPane = new GridPane();
            gridPane.setVgap(20);
            Text spaceCreator = new Text();
            gridPane.add(spaceCreator,0,0);
            Rectangle rectangle = new Rectangle(0, 0, 200, 200);
            rectangle.setArcWidth(30.0);   // Corner radius
            rectangle.setArcHeight(30.0);
            ImagePattern pattern = new ImagePattern(new Image(appFile.getPathToImage(), 200, 200, false, false));
            rectangle.setFill(pattern);
            rectangle.setEffect(new DropShadow(20, Color.BLACK));
            Text appText = new Text(appFile.getDisplayName());
            appText.setId("idText");
            vbox1.setAlignment(Pos.CENTER);
            vbox1.getChildren().addAll(appText,gridPane,rectangle);

            //Name Change text field button
            GridPane grid2 = new GridPane();
            Text displayNameLabel = new Text("Name");
            TextField displayName = new TextField(appFile.getDisplayName());
            Button confirmDisplayNameChange = new Button("✔");
            grid2.setHgap(10);
            grid2.add(displayNameLabel,0,0);
            grid2.add(displayName,1,0);
            grid2.add(confirmDisplayNameChange,2,0);
            grid2.setAlignment(Pos.CENTER);

            //Image button change
            GridPane grid3= new GridPane();
            Button changeImg = new Button("Change Image");
            Text changeImgFileName = new Text();
            grid3.setHgap(10);
            grid3.add(changeImg,0,0);
            grid3.add(changeImgFileName,1,0);
            grid3.setAlignment(Pos.CENTER);

            //Delete and cancel
            Button del = new Button("Delete");
            Button cancel = new Button("Cancel");

            //Clump it all together
            VBox appVbox = new VBox(10);
            appVbox.setId("appVbox");
            appVbox.setBackground(new Background(new BackgroundFill(Color.web("#323232"), new CornerRadii(50), Insets.EMPTY)));
            appVbox.getChildren().addAll(vbox1,grid2,grid3, del, cancel);
            appVbox.setAlignment(Pos.CENTER);
            appVbox.autosize();


            //Create Popup stage
            final Stage dialog = new Stage();
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.initOwner(refreshButton.getScene().getWindow());

            //Create the scene
            Scene dialogScene = new Scene(appVbox, 300, 500);
            String css = this.getClass().getResource("dialog.css").toExternalForm();
            dialogScene.getStylesheets().add(css);
            dialogScene.setFill(Color.TRANSPARENT); //Makes scene background transparent
            dialog.setScene(dialogScene);
            dialog.setResizable(false);
            dialog.show();

            //Transition for fade in
            FadeTransition transition = new FadeTransition(Duration.millis(500),appVbox);
            transition.setFromValue(0.0);
            transition.setToValue(1.0);
            transition.play();

            //Transition for fade out
            Timeline timelineClose = new Timeline();
            KeyFrame keyClose = new KeyFrame(Duration.millis(250),
                    new KeyValue(dialog.getScene().getRoot().opacityProperty(), 0));

            //If dialog not focused
            dialog.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if(Boolean.FALSE.equals(newValue))
                    timelineClose.getKeyFrames().add(keyClose);
                    timelineClose.setOnFinished((ae) -> {
                        mainView.setEffect(null);
                        dialog.close();
                    });
                timelineClose.play();
            });

            //Display button
            confirmDisplayNameChange.setOnAction(actionEvent -> {
                onNameChange(id,displayName,appFile,appText);
                refreshButton.fire();

            });

            //Change image button
            changeImg.setOnAction(actionEvent -> {
                onImgChange(id,dialog,appFile);
                refreshButton.fire();
                onEditButtonClick(id);
            });

            //Delete Button
            del.setOnAction(actionEvent -> {
                onAppDelete(id,appFile);
                refreshButton.fire();
                timelineClose.getKeyFrames().add(keyClose);
                timelineClose.setOnFinished((ae) -> {
                    mainView.setEffect(null);
                    dialog.close();
                });
                timelineClose.play();
            });

            //Cancel Button
            cancel.setOnAction(actionEvent -> {
                refreshButton.fire();
                timelineClose.getKeyFrames().add(keyClose);
                timelineClose.setOnFinished((ae) -> {
                    mainView.setEffect(null);
                    dialog.close();
                });
                timelineClose.play();
            });
        }
    }

    public void onNameChange(String id,TextField displayName,AppFile appFile,Text appText){
        if (!displayName.getText().isBlank()){
            String appLocation = appDataLocalDirApp + id + ".txt";
            String imgLocation = appDataLocalDirIcon + id + ".png";
            appFile.setDisplayName(displayName.getText());
            appFile.setHtmlCode(generateHtmlCode(imgLocation,appFile.getDisplayName(),appFile.getId()));

            appText.setText(appFile.getDisplayName());
            File newApp = new File(appLocation);
            try {
                FileUtils.writeStringToFile(newApp, writeAppToFile(appFile), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void onImgChange(String id,Stage dialog,AppFile appFile) {

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png","*.jpeg");
        FileChooser fileC = new FileChooser();
        fileC.getExtensionFilters().add(imageFilter);
        fileC.setTitle("Open File");
        File file = fileC.showOpenDialog(dialog);
        String imgLocation = appDataLocalDirIcon + id + ".png";
        if (Objects.nonNull(file)){
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                java.awt.Image resizedImage = bufferedImage.getScaledInstance(720,720, java.awt.Image.SCALE_SMOOTH);
                ImageIO.write(convertToBufferedImage(resizedImage), "png", new File(imgLocation));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onAppDelete(String id,AppFile appFile) {

        var txtPath =  appDataLocalDirApp + id+".txt";;
        var imgPath = appFile.getPathToImage();
        try {
            File txtFile = new File(txtPath);
            File imgFile = new File(imgPath);
            FileUtils.delete(txtFile);
            FileUtils.delete(imgFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setStageAndSetupListeners(Stage stage) {
        BorderlessScene scene = new BorderlessScene(stage, StageStyle.UNDECORATED, borderPane, 1262, 810);

        //remove css and add mine
        scene.removeDefaultCSS();
        String css = this.getClass().getResource("application.css").toExternalForm();
        scene.getStylesheets().add(css);

        //Set on move control
        scene.setMoveControl(topBorder);

        closeButton.setOnAction(e->{
            Platform.exit();
        });

        minimizeButton.setOnAction(e->{
            stage.setIconified(true);
        });

        resizeButton.setOnAction(e->{
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            log("Height : "+ bounds.getHeight()+ " width : "+stage.getWidth());
            if (stage.getHeight()<bounds.getHeight() && stage.getWidth()<bounds.getWidth()){
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
            }else{
                stage.setX(649);
                stage.setY(197);
                stage.setWidth(1267);
                stage.setHeight(820);
            }

            log(bounds);


        });

        //Other stuff
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.setMinHeight(820);
        stage.setMinWidth(1267);
        stage.setHeight(820);
        stage.setWidth(1267);
        stage.show();
    }

    public void setup() {
        //Init Category VBox
    }
}