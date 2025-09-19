import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CreateTestImage {
    public static void main(String[] args) {
        try {
            File dir = new File("USERDATA/appearance");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            int width = 100;
            int height = 50;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(0, 0, new Color(70, 130, 180),
                                                      0, height, new Color(100, 149, 237));
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, width, height, 10, 10);

            g2d.setColor(new Color(25, 25, 112));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(1, 1, width-2, height-2, 10, 10);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "BTN";
            int textX = (width - fm.stringWidth(text)) / 2;
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(text, textX, textY);

            g2d.dispose();

            File outputFile = new File("USERDATA/appearance/btn1.png");
            ImageIO.write(image, "PNG", outputFile);

            System.out.println("Test image created: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to create test image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
