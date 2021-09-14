package com.aljodomo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

/**
 * @author Aljoscha Domonell
 */
public class MediaFolderOrganizer {

    private final List<File> sources = new ArrayList<>();
    private final File destination;
    private final boolean moveFiles;
    private Scanner scanner;
    private boolean ignore = false;

    public MediaFolderOrganizer(String sourceFolders, String destinationFolder, boolean moveFiles) {
        String[] sourceLocations = sourceFolders.split(",");
        for(String s : sourceLocations) {
            this.sources.add(new File(s));
        }
        this.destination = new File(destinationFolder);
        this.scanner = new Scanner(System.in);
        this.moveFiles = moveFiles;

    }

    public void apply() {
        this.sources.forEach(this::apply);
    }

    private void apply(File file) {
        if (file.isDirectory()) {
            handleDirectory(file);
        } else {
            this.organize(file);
        }
    }

    private void organize(File file) {
        Path newLocation = getNewLocation(file);

        File newFile = newLocation.toFile();

        if (newFile.exists()) {
            handleNewFileExists(file, newFile);
        } else {
            if(!newFile.getParentFile().exists()){
                boolean directoriesCreated = newFile.getParentFile().mkdirs();
                if (!directoriesCreated) {
                    handleCouldNotCreateDirectories(file, newFile);
                } else {
                    recreateFileAtNewLocation(file, newFile);
                }
            } else {
                recreateFileAtNewLocation(file, newFile);
            }
        }
    }

    private void recreateFileAtNewLocation(File file, File newLocation) {
        if (moveFiles) {
            boolean fileRenamed = file.renameTo(newLocation);
            if (!fileRenamed) {
                handleNewFileExists(file, newLocation);
            }
        } else {
            copyFile(file, newLocation);
        }
    }

    private void copyFile(File file, File newLocation) {
        try {
            boolean newFileCreated = newLocation.createNewFile();
            if (!newFileCreated) {
                handleCouldNotCreateFile(file, newLocation);
            }
            Files.copy(file.toPath(), newLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            handleCouldNotCreateFile(file, newLocation);
        }
    }

    private Path getNewLocation(File file) {
        Date lastModified = new Date(file.lastModified());
        LocalDateTime date = LocalDateTime.ofInstant(lastModified.toInstant(), ZoneId.systemDefault());

        return Path.of(
                this.destination.getAbsolutePath(),
                date.getYear() + "",
                date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                file.getName()
        );

    }


    private boolean scannYesOrNo(String question) {
        if (this.ignore) {
            return true;
        }
        System.out.println(question);
        System.out.println("Type y or n");
        System.out.println("Or type ignore to ignore all future conflicts");
        String newLine = this.scanner.nextLine();
        if (newLine.equals("ignore")) {
            this.ignore = true;
            return true;
        }
        return newLine.equals("y");
    }

    private void handleCouldNotCreateFile(File file, File newFile) {
        System.out.println("File could not be created:");
        System.out.println("Source File: " + file.getAbsolutePath());
        System.out.println("Destination File: " + newFile.getAbsolutePath());
        if (!this.scannYesOrNo("Do you want to continue?")) {
            System.exit(1);
        }
    }

    private void handleCouldNotCreateDirectories(File file, File newFile) {
        System.out.println("Directories could not be created:");
        System.out.println("Source File: " + file.getAbsolutePath());
        System.out.println("Destination File: " + newFile.getAbsolutePath());
        if (!this.scannYesOrNo("Do you want to continue?")) {
            System.exit(1);
        }
    }

    private void handleNewFileExists(File file, File newFile) {

        if (file.lastModified() == newFile.lastModified()) {
            return;
        }

        System.out.println("Destination file already exists");
        System.out.println("Source File: " + file.getAbsolutePath());
        System.out.println("Destination File: " + newFile.getAbsolutePath());
        if (!this.scannYesOrNo("Do you want to continue?")) {
            System.exit(1);
        }
    }

    private void handleDirectory(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            Arrays.stream(list).forEach(this::apply);
        }
    }
}
