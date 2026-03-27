package org.example;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws Exception {

        String folderPath = "";
        File[] mainFolders = getMainFolders(folderPath); // ordered by name ascending
        for (int i = 0; i < mainFolders.length; i++) {
            File mainFolder = mainFolders[i];

            if (i < 43) { //
                System.out.println("Skipping folder index " + i + ": " + mainFolder.getName());
                continue;
            }

            System.out.println("Scanning [" + i + "]: " + mainFolder.getAbsolutePath());

            traverseAndEncode(mainFolder);
        }
    }

    public static File[] getMainFolders(String folderPath) {
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            // List only directories
            File[] subFolders = folder.listFiles(File::isDirectory);

            if (subFolders != null) {
                // Sort by name
                Arrays.sort(subFolders, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

//                for (File subFolder : subFolders) {
//                    System.out.println(subFolder.getName());
//                }
                return subFolders;
            } else {
                System.out.println("No subfolders found.");
            }
        } else {
            System.out.println("The specified path is not a folder or does not exist.");
        }
        return new File[]{};
    }

    public static void traverseAndEncode(File mainFolder) {
        try (Stream<Path> paths = Files.walk(mainFolder.toPath())) {
            // Collect all video files first
            List<Path> videos = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().matches("(?i).+\\.(mp4|mov)$"))
                    .toList();

            // Now encode each file (no risk of re-encoding new ones)
            for (Path video : videos) {
                System.out.println("   Found video: " + video.toAbsolutePath());
                try {
                    encode(video);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace(); // log but continue with other videos
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encode(Path path) throws IOException, InterruptedException {
        File srcFile = path.toFile();

        // Destination folder is the same as source
        File destDir = srcFile.getParentFile();

        String outputFile = destDir + File.separator +
                "encoded_" + srcFile.getName().replaceAll("(?i)\\.(mp4|mov)$", ".mp4");
        System.out.println("Encoding: " + srcFile.getName());

        // "-vf", "scale=1280:720", add this to specify resolution scaling, otherwise keeps the original
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", srcFile.getAbsolutePath(),
                "-c:v", "libx265",
                "-crf", "22",
                "-preset", "fast",
                "-c:a", "copy",
                "-x265-params", "pools=20",
                outputFile
        );

        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Finished: " + srcFile.getName());
        } else {
            System.err.println("Error encoding: " + srcFile.getName());
        }
    }

    public static void checkEncodeType(String fileName) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(fileName)) {
            grabber.start();

            // Get codec info for video stream
            int videoCodecParams = grabber.getVideoCodec();
            String codecName = grabber.getVideoCodecName();

            System.out.println("Video codec name: " + codecName);

            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}
