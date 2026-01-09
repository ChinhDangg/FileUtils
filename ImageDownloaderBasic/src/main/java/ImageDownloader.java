import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageDownloader {
    public static void main(String[] args) {
        String url = "https://"; // Replace with your target URL
        String saveDir = "downloaded_images"; // Folder to save images

        try {
            // Create directory if it doesn't exist
            Path savePath = Paths.get(saveDir);
            if (!Files.exists(savePath)) {
                Files.createDirectory(savePath);
            }

            // Connect to the website and get the document
            Document doc = Jsoup.connect(url).get();

            // Select all img elements with class ""
            String elementClasses = "img.max-w-full.my-0.mx-auto";
            Elements images = doc.select(elementClasses);

            int start = 1;          // e.g. 1, 50, 100
            int total = images.size();
            int maxIndex = start + total - 1;

            // One extra leading digit beyond the max index digits.
            // Example: maxIndex=100 -> digits=3 -> width=4 -> 0001..0100
            int width = String.valueOf(maxIndex).length() + 1;

            int count = 1;
            for (Element img : images) {
                String imgSrc = img.absUrl("src"); // Get absolute URL of the image
                if (imgSrc.isEmpty()) continue;

                // Get the image file name
                String fileName = imgSrc.substring(imgSrc.lastIndexOf("/") + 1);
                if (fileName.isEmpty()) {
                    fileName = "image_" + (count < 10 ? ("0" + count) : count) + ".jpg";
                }

                // Download and save the image
                try {
                    URL imageUrl = new URL(imgSrc);
                    try (InputStream in = imageUrl.openStream();
                         FileOutputStream out = new FileOutputStream(Paths.get(saveDir, fileName).toString())) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Downloaded: " + fileName);
                        count++;
                    }
                } catch (IOException e) {
                    System.err.println("Error downloading " + imgSrc + ": " + e.getMessage());
                }
            }

            if (count == 1) {
                System.out.printf("No images found with class '%s'", elementClasses);
            }

        } catch (IOException e) {
            System.err.println("Error connecting to URL: " + e.getMessage());
        }
    }
}