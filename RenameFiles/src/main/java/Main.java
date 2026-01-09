import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        // Specify the folder path
        String oldFolderPath = "D:\\Renaming\\Old";
        String newFolderPath = "D:\\Renaming\\New";
        int startingIndex = 1;          // e.g. 1, 50, 100

        // Open the folder
        File folder = new File(oldFolderPath);

        // Check if the folder exists and is a directory
        if (folder.exists() && folder.isDirectory()) {
            // Get all files in the folder
            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                // Sort files by name in ascending order
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                int total = files.length;
                int maxIndex = startingIndex + total - 1;

                int width = String.valueOf(maxIndex).length() + 1;

                // Rename files with an incrementing index
                int index = startingIndex;
                for (File file : files) {
                    if (file.isFile()) { // Only rename files, skip subfolders
                        // Create the new file name
                        //String newFileName = String.format("%d_%s", index, file.getName());
                        String newFileName = String.format("%0" + width + "d%s", index, ("." + getFileExtension(file.getName())));

                        // Get the new file path
                        File newFile = new File(newFolderPath + File.separator + newFileName);

                        // Rename the file
                        if (file.renameTo(newFile)) {
                            System.out.println("Renamed: " + file.getName() + " -> " + newFileName);
                        } else {
                            System.out.println("Failed to rename: " + file.getName());
                        }
                        // Increment the index
                        index++;
                    }
                }
            } else {
                System.out.println("No files found in the folder.");
            }
        } else {
            System.out.println("Invalid folder path or the folder does not exist.");
        }
    }

    public static String getFileExtension(String fileName) {
        // Check if the file name has a dot
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            // Extract and return the extension
            return fileName.substring(dotIndex + 1);
        }
        // Return an empty string if no extension is found
        return "";
    }
}