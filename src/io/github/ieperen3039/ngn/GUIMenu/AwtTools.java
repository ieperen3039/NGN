package io.github.ieperen3039.ngn.GUIMenu;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class AwtTools {
    private static void openFileDialog(Consumer<File> action, String extension) {
        FileDialog fd = new FileDialog((Frame) null, "Choose a file", FileDialog.LOAD);
        fd.setFile(extension);
        fd.setVisible(true);

        String filename = fd.getFile();
        if (filename != null) {
            String directory = fd.getDirectory();
            File file = Paths.get(directory, filename).toFile();
            action.accept(file);
        }

        fd.dispose();
    }
}
