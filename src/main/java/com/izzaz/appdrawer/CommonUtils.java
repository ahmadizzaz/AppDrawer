package com.izzaz.appdrawer;

import com.izzaz.appdrawer.jiconextract2.JIconExtract;
import com.izzaz.appdrawer.model.AppFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CommonUtils {
    public static BufferedImage convertToBufferedImage(java.awt.Image img) {

        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(
                img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();

        return bi;
    }

    public static String writeAppToFile(AppFile appFile){
        return "id="+appFile.getId() + "\n"+
                "displayName="+appFile.getDisplayName() + "\n"+
                "pathToApp="+appFile.getPathToApp() + "\n"+
                "pathToImage="+appFile.getPathToImage() + "\n"+
                "htmlCode="+appFile.getHtmlCode();
    }

    public static String structureHtmlCode(String html){
        return "<html>" +
                "<head>" +
                "</head>" +
                "<body>" + html +
                "</body> " +
                "</html>";

    }

    public static String generateHtmlCode(String imagePath, String displayName,String id) {
//        return
                return ("<div class=\"container\"><img src=\"file:/"+imagePath+"\"/><p class=\"title\">"+displayName+"</p><div class=\"overlay\"></div><div class=\"button-open\" onclick=\"app.processOnClickOpen(\'"+id+"\')\"><a href=\"#\"> Open </a></div><div class=\"button-option\" onclick=\"app.processOnClickOption(\'"+id+"\')\"><a href=\"#\"> Edit </a></div></div>");

    }
    public static void log(Object e){
        System.out.println(e);
    }

    public static WritableImage getImageViewFromPath(String path, int width, int height){
        BufferedImage image = JIconExtract.getIconForFile(width,height,path);
        assert image != null;
        return SwingFXUtils.toFXImage(image,null);
    }
}
