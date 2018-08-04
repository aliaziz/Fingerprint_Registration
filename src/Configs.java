

import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.io.PrintStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Configs
        extends JFrame {

    public static String username = "username";
    public static String lastname = "lastname";
    public static String image = "image";
    public static final String imageSize = "imageSize";
    public static final String empCode = "empCode";
    public static final String isLoggedIn = "isLoggedIn";
    static String BASE_URL = "BASE_URL";

    public Configs() {
    }

    public void DrawImage(JLabel fingerprintpanel, Image img) {
        int w = 256;
        int h = 288;
        byte[] imageraw = new byte[w * h];
        int[] imagesize = new int[1];
        int[] rawpic = new int[w * h];

        fpLibrary.INSTANCE.GetImage(imageraw, imagesize);
        for (int i = 0; i < w * h; i++) {
            int m = imageraw[i] & 0xFF;
            rawpic[i] = (m | m << 8 | m << 16 | 0xFF000000);
        }
        img = createImage(new MemoryImageSource(w, h, rawpic, 0, w));
        System.out.print(img + " after being created");
        fingerprintpanel.setIcon(new ImageIcon(img));
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) 
                    + Character.digit(s.charAt(i + 1), 16)));
        }

        return data;
    }
}
