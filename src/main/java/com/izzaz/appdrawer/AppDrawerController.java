package com.izzaz.appdrawer;

import com.izzaz.appdrawer.jiconextract2.JIconExtract;
import com.izzaz.appdrawer.model.AppFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AppDrawerController implements Initializable {
    @FXML
    private Label welcomeText;


    @FXML
    private Button btn;


    @FXML
    private ChoiceBox<String> itemCountFilter;

    @FXML
    private WebView webView;

    protected String appDataLocalDir = System.getenv("LOCALAPPDATA") + "\\IzzazAppDrawer\\";

    protected List<AppFile> appFileList = new ArrayList<>();


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
                System.out.println(appLocation);
                File newApp = new File(appLocation);
                FileUtils.writeStringToFile(newApp, writeAppToFile(file, fileNameNoExt), Charset.defaultCharset());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        loadApps();
    }

    @FXML
    protected void onGridClick(MouseEvent e){
//        Node clickedNode = e.getPickResult().getIntersectedNode();//
//        Button del = new Button("Delete");
//        Button open = new Button("Open");
//        Button cancel = new Button("Cancel");
//        VBox appVbox = new VBox(10);
//        Text appText = new Text(clickedNode.getId());
//
//        var appName = clickedNode.getId();
//
//        if (clickedNode instanceof ImageView) {
//
//            ImageView aNode = (ImageView)clickedNode;
//
//            //Create Popup stage
//            final Stage dialog = new Stage();
//            dialog.initModality(Modality.NONE);
//            dialog.initOwner(gridPane.getScene().getWindow());
//
//            //populate the vbox
//            appVbox.getChildren().addAll(appText,new ImageView(aNode.getImage()) ,open,del,cancel);
//            appVbox.setAlignment(Pos.CENTER);
//            appVbox.autosize();
//
//            //Create the scene
//            Scene dialogScene = new Scene(appVbox, 200, 300);
//            dialog.setScene(dialogScene);
//            dialog.setResizable(false);
//            dialog.show();
//
//            //Open Button
//            open.setOnAction(actionEvent -> {
//                ArrayList<File>fileList = (ArrayList<File>) FileUtils.listFiles(new File(appDataLocalDir),null,true);
//                fileList.forEach(file -> {
//                    try {
//                        if (FilenameUtils.removeExtension(file.getName()).equals(appName)){
//                            var appPath = FileUtils.readLines(file,Charset.defaultCharset()).get(1).replace("path=","");
//                            File appFile = new File(appPath);
//                            if (appFile.exists()){
//                                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", appFile.getAbsolutePath(), "-n", "100");
//                                Process process = pb.start();
//                            }
//                        }
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                });
//                closeAndRefresh(dialog);
//            });
//
//            //Delete Button
//            del.setOnAction(actionEvent -> {
//                String appLocation = appDataLocalDir + appName+".txt";
//                File appFile = new File(appLocation);
//                try {
//                    FileUtils.delete(appFile);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//                closeAndRefresh(dialog);
//            });
//
//            //Cancel Button
//            cancel.setOnAction(actionEvent -> closeAndRefresh(dialog));
//

//        }
    }

    public void loadApps(){
        AtomicReference<String> fullWebViewHtml = new AtomicReference<>("");
        WebEngine webEngine = webView.getEngine();
        String css = this.getClass().getResource("styles.css").toExternalForm();
        String css1 = this.getClass().getResource("imagehover.css").toExternalForm();
        webEngine.setUserStyleSheetLocation(css);
        webEngine.setUserStyleSheetLocation(css1);
        webEngine.documentProperty().addListener((obs,oldDoc,newDoc)->{
            JSObject window = (JSObject) webEngine. executeScript("window");
            window.setMember("clicker",
                    new JSCallback());
        });

        itemCountFilter.setOnAction(actionEvent -> {
            loadApps();
        });
        if (Files.exists(Path.of(appDataLocalDir))){
            ArrayList<File>fileList = (ArrayList<File>) FileUtils.listFiles(new File(appDataLocalDir),null,true);
            AtomicInteger row = new AtomicInteger(0);
            AtomicInteger column = new AtomicInteger(0);

            fileList.forEach(file -> {
                //Create individual items for grid
                if (file.getName().contains(".txt")){
                    log("Row : "+ row.get() + ", Column : " + column.get());



                    try {
                        var content = FileUtils.readLines(file,Charset.defaultCharset());
                        var app = AppFile.builder()
                                .name(content.get(0).replace("app=",""))
                                .path(content.get(1).replace("path=",""))
                                .build();
                        if (!Files.exists(Path.of(appDataLocalDir + "icon\\"))){
                            Files.createDirectory(Path.of(appDataLocalDir + "icon\\"));
                        }
                        File pngFile = new File(appDataLocalDir + "icon\\"+app.getName()+".png");


                        var image = getImageViewFromPath(app.getPath(),720,720);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(image, null);
                        ImageIO.write(renderedImage,"png",pngFile);
                        app.setHtmlCode("<figure class=\"appCard\" onclick=\"clicker.process()\"><img src=\"file:/"+pngFile.getAbsolutePath()+"\"/><figcaption><h4>"+app.getName()+"</h4></figcaption></figure>");


                        fullWebViewHtml.set(fullWebViewHtml + app.getHtmlCode());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            log(fullWebViewHtml);
        }
        webEngine.loadContent(fullWebViewHtml.get());

    }

    private String writeAppToFile(File file,String fileNameNoExt){
        return "app="+fileNameNoExt + "\n"+
                "path="+file.getAbsolutePath();
    }

    private void log(Object e){
        System.out.println(e);
    }

    private WritableImage getImageViewFromPath(String path,int width, int height){
        BufferedImage image = JIconExtract.getIconForFile(width,height,path);
        assert image != null;
        return SwingFXUtils.toFXImage(image,null);
    }

    private void closeAndRefresh(Stage stage){
        stage.close();
        loadApps();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadApps();
    }

    public static class JSCallback {
        public void process() {
            // do something here...
            System.out.println("Clicked");
        }
    }
}