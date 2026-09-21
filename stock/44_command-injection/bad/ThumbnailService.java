package bad;

public class ThumbnailService {

    public void createThumbnail(String fileName) {
        try {
            String cmd = "sh -c pdftoppm -png /var/uploads/" + fileName
                    + " /var/thumbs/" + fileName;
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
