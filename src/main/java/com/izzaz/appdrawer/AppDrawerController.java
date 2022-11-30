package com.izzaz.appdrawer;

import com.izzaz.appdrawer.jiconextract2.JIconExtract;
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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;

public class AppDrawerController implements Initializable {

    @FXML
    private Label welcomeText;

    @FXML
    private AnchorPane mainView;

    @FXML
    private Button btn;

    @FXML
    private Button refreshButton;

    @FXML
    private ChoiceBox<String> itemCountFilter;

    @FXML
    private WebView webView;

    protected String appDataLocalDir = System.getenv("LOCALAPPDATA") + "\\IzzazAppDrawer\\";

    protected List<AppFile> appFileList = new ArrayList<>();



    WebEngine webEngine;
    JSCallBack jsCallBack;


    @FXML
    protected void onHelloButtonClick() {

        Stage stage= (Stage)btn.getScene().getWindow();
        FileChooser fileC = new FileChooser();
        fileC.setTitle("Open File");
        File file = fileC.showOpenDialog(stage);
        welcomeText.setText("Selected : " + (Objects.nonNull(file) ? file.getName():"No file selected"));
        if (Objects.nonNull(file)){
            var fileNameNoExt = FilenameUtils.removeExtension(file.getName());
            try{
                String appLocation = appDataLocalDir + fileNameNoExt + ".txt";
                String imgLocation = appDataLocalDir + "icon\\" + fileNameNoExt + ".png";
                System.out.println(appLocation);
                File newApp = new File(appLocation);
                AppFile appFile = AppFile.builder()
                        .id(fileNameNoExt)
                        .displayName(fileNameNoExt)
                        .pathToApp(file.getAbsolutePath())
                        .pathToImage(imgLocation)
                        .htmlCode(generateHtmlCode(imgLocation,fileNameNoExt,fileNameNoExt))
                        .build();

                FileUtils.writeStringToFile(newApp, writeAppToFile(appFile), Charset.defaultCharset());
                Files.createDirectories(Paths.get(appDataLocalDir + "icon\\"));
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


    protected void onEditButtonClick(String id) {
        mainView.setEffect(new GaussianBlur());
        var appFileOpt = appFileList.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();

        if (appFileOpt.isPresent()) {
            var appFile = appFileOpt.get();
            VBox vbox1 = new VBox();
            GridPane gridPane = new GridPane();
            gridPane.setVgap(20);
            Text spaceCreator = new Text();
            gridPane.add(spaceCreator,0,0);
            Rectangle rectangle = new Rectangle(0, 0, 200, 200);
            rectangle.setArcWidth(30.0);   // Corner radius
            rectangle.setArcHeight(30.0);
            ImagePattern pattern = new ImagePattern(new Image(appFile.getPathToImage(), 200, 200, false, false)); // Resizing);
            rectangle.setFill(pattern);
            rectangle.setEffect(new DropShadow(20, Color.BLACK));
            Text appText = new Text(appFile.getDisplayName());
            appText.setId("idText");
            vbox1.setAlignment(Pos.CENTER);
            vbox1.getChildren().addAll(appText,gridPane,rectangle);

//            VBox vbox2 = new VBox();
            GridPane grid2 = new GridPane();
            Text displayNameLabel = new Text("Name");
            TextField displayName = new TextField(appFile.getDisplayName());
            Button confirmDisplayNameChange = new Button("✔");
            grid2.setHgap(10);
            grid2.add(displayNameLabel,0,0);
            grid2.add(displayName,1,0);
            grid2.add(confirmDisplayNameChange,2,0);
            grid2.setAlignment(Pos.CENTER);
//            vbox2.setAlignment(Pos.CENTER);
//            vbox2.getChildren().addAll(displayNameLabel,displayName,confirmDisplayNameChange);

            VBox vbox3 = new VBox();
            Button changeImg = new Button("Change Image");
            Text changeImgFileName = new Text();
            vbox3.setAlignment(Pos.CENTER);
            vbox3.getChildren().addAll(changeImg,changeImgFileName);

            Button del = new Button("Delete");
            Button cancel = new Button("Cancel");


            VBox appVbox = new VBox(10);
            appVbox.setId("appVbox");
            appVbox.setBackground(new Background(new BackgroundFill(Color.web("#323232"), new CornerRadii(50), Insets.EMPTY)));
            appVbox.getChildren().addAll(vbox1,grid2,vbox3, del, cancel);
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

            var primaryWindow = refreshButton.getScene().getWindow();
            FadeTransition transition = new FadeTransition(Duration.millis(500),appVbox);
            transition.setFromValue(0.0);
            transition.setToValue(1.0);
            transition.play();
            //Animation part



            Timeline timelineClose = new Timeline();
            KeyFrame keyClose = new KeyFrame(Duration.millis(250),
                    new KeyValue(dialog.getScene().getRoot().opacityProperty(), 0));
            dialog.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if(Boolean.FALSE.equals(newValue))

                    timelineClose.getKeyFrames().add(keyClose);
                    timelineClose.setOnFinished((ae) -> {
                        mainView.setEffect(null);
                        dialog.close();
                    });
                    timelineClose.play();




            });

            confirmDisplayNameChange.setOnAction(actionEvent -> {
                if (!displayName.getText().isBlank()){
                    String appLocation = appDataLocalDir + id + ".txt";
                    String imgLocation = appDataLocalDir + "icon\\" + id + ".png";
                    appFile.setDisplayName(displayName.getText());
                    appFile.setHtmlCode(generateHtmlCode(imgLocation,appFile.getDisplayName(),appFile.getId()));

                    appText.setText(appFile.getDisplayName());
                    File newApp = new File(appLocation);
                    try {
                        FileUtils.writeStringToFile(newApp, writeAppToFile(appFile), Charset.defaultCharset());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refreshButton.fire();
                }
            });
            //Delete Button
            del.setOnAction(actionEvent -> {
                onAppDelete(id);
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
    public void onAppDelete(String id) {
        var app = appFileList.stream()
                .filter(file -> file.getId().equals(id))
                .findFirst();
        if (app.isPresent()) {
            var txtPath =  appDataLocalDir + id+".txt";;
            var imgPath = app.get().getPathToImage();
            try {
                File txtFile = new File(txtPath);
                File imgFile = new File(imgPath);
                FileUtils.delete(txtFile);
                FileUtils.delete(imgFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        refreshButton.fire();
    }


    public String loadApps(){
        appFileList.clear();
        AtomicReference<String> fullWebViewHtml = new AtomicReference<>("");
        if (Files.exists(Path.of(appDataLocalDir))){
            ArrayList<File>fileList = (ArrayList<File>) FileUtils.listFiles(new File(appDataLocalDir),null,true);

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

    private String writeAppToFile(AppFile appFile){
        return "id="+appFile.getId() + "\n"+
                "displayName="+appFile.getDisplayName() + "\n"+
                "pathToApp="+appFile.getPathToApp() + "\n"+
                "pathToImage="+appFile.getPathToImage() + "\n"+
                "htmlCode="+appFile.getHtmlCode();
    }

    public String structureHtmlCode(String html){
        return "<html>" +
                "<head>" +
                "</head>" +
                "<body>" + html +
                "</body> " +
                "</html>";

    }
    public String generateHtmlCode(String imagePath, String displayName,String id) {
        return ("<div class=\"container\"><img src=\"file:/"+imagePath+"\"/><p class=\"title\">"+displayName+"</p><div class=\"overlay\"></div><div class=\"button-open\" onclick=\"app.processOnClickOpen(\'"+id+"\')\"><a href=\"#\"> Open </a></div><div class=\"button-option\" onclick=\"app.processOnClickOption(\'"+id+"\')\"><a href=\"#\"> Edit </a></div></div>");

    }
    public void log(Object e){
        System.out.println(e);
    }

    public WritableImage getImageViewFromPath(String path,int width, int height){
        BufferedImage image = JIconExtract.getIconForFile(width,height,path);
        assert image != null;
        return SwingFXUtils.toFXImage(image,null);
    }

    private void refreshPage(String html){
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
        onEditButtonClick(id);
        log("After Delete");
//        webEngine.loadContent("");
//        log("After loadContent");
//        log("engine : "+engine.getDocument());
//        log("webEngine : "+webEngine.getDocument());
//        log("in bridge is thread :"+ Platform.isFxApplicationThread());

    }

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path logDir = Paths.get(appDataLocalDir+"icon\\");
        logDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);




        refreshButton.setOnAction(event -> {
            refreshPage(loadApps());
        });
        webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue)-> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                jsCallBack = new JSCallBack(webEngine);
                jsObject.setMember("app", this);
                log(newValue);
            }
        });
        String css = this.getClass().getResource("styles.css").toExternalForm();
        String css1 = this.getClass().getResource("imagehover.css").toExternalForm();
        webEngine.setUserStyleSheetLocation(css);
        webEngine.setUserStyleSheetLocation(css1);
        webEngine.setJavaScriptEnabled(true);
        var html = loadApps();
        webEngine.loadContent(html);
        log("in initialize is thread :"+ Platform.isFxApplicationThread());


    }
    public class JSCallBack {
        private WebEngine engine;
        protected JSCallBack(WebEngine engine){
            this.engine = engine;
            String css = this.getClass().getResource("styles.css").toExternalForm();
            String css1 = this.getClass().getResource("imagehover.css").toExternalForm();
            this.engine.setUserStyleSheetLocation(css);
            this.engine.setUserStyleSheetLocation(css1);
            this.engine.setJavaScriptEnabled(true);
        }

        public void processOnClickOpen(String id) {
            log("Before open");
            log(engine.getDocument());
            log(webEngine.getDocument());
            onAppOpen(id);
            log(engine.getDocument());
            log("After open");
        }

        public void processOnClickOption(String id) {
            onEditButtonClick(id);
            log("After Delete");

            webEngine.loadContent("");
            log("After loadContent");
            log("engine : "+engine.getDocument());
            log("webEngine : "+webEngine.getDocument());
            log("in bridge is thread :"+ Platform.isFxApplicationThread());

        }
        public void showTime() {
            System.out.println("Show Time");
        }
    }


}