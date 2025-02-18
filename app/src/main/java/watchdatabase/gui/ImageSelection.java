package watchdatabase.gui;

import java.io.File;

import javax.swing.JFileChooser;

public class ImageSelection {

    public static File selectImageFile() {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        
        // Set the file filter to only allow image files (jpg, png, gif)
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
        
        // Show the file chooser dialog
        int returnValue = fileChooser.showOpenDialog(null);
        
        // If the user selected a file
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            // Return the selected file
            return fileChooser.getSelectedFile();
        }
        
        // Return null if no file was selected
        return null;
    }
}
