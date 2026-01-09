import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

static class ImageEntry {
    long index;
    String extension;
    byte[] data;
}

void main() throws Exception {
    Path harPath = Path.of("images.har");
    Path outDir = Path.of("D:\\Downloads");

    Files.createDirectories(outDir);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(Files.newInputStream(harPath));
    JsonNode entries = root.path("log").path("entries");

    List<ImageEntry> images = new ArrayList<>();

    // ✅ Handles: 12.jpg, 12_123456.jpg, 12_anything.webp
    Pattern pattern = Pattern.compile(
            "/([0-9]+)(?:_[^/.]+)?\\.(png|jpg|jpeg|webp)(?:\\?|$)",
            Pattern.CASE_INSENSITIVE
    );

    for (JsonNode entry : entries) {
        String url = entry.path("request").path("url").asText();
        //System.out.println("Current url: " + url);

        JsonNode content = entry.path("response").path("content");
        if (!content.has("text") || content.get("text").asText().isEmpty()) {
            IO.println("SKIP (no body): " + url);
            continue;
        }

        String mime = content.path("mimeType").asText("");
        if (!mime.startsWith("image/")) {
            IO.println("SKIP (not image): " + url + " " + mime);
            continue;
        }

        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) continue;

        long index = Long.parseLong(matcher.group(1));
        String extension = "." + matcher.group(2);

        byte[] data = "base64".equals(content.path("encoding").asText())
                ? Base64.getDecoder().decode(content.get("text").asText())
                : content.get("text").asText().getBytes();

        ImageEntry img = new ImageEntry();
        img.index = index;
        img.extension = extension;
        img.data = data;

        images.add(img);
    }

    // 🔑 SORT BY PAGE ORDER
    images.sort(Comparator.comparingLong(i -> i.index));

    int start = 1;          // e.g. 1, 50, 100
    int total = images.size();
    int maxIndex = start + total - 1;

    // One extra leading digit beyond the max index digits.
    // Example: maxIndex=100 -> digits=3 -> width=4 -> 0001..0100
    int width = String.valueOf(maxIndex).length() + 1;

    int count = start;
    for (ImageEntry img : images) {
        String name = String.format("%0" + width + "d%s", count, img.extension);
        Files.write(outDir.resolve(name), img.data);
        count++;
    }


    IO.println("✅ Extracted " + (count - 1) + " images in correct order");
}