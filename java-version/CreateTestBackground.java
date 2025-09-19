import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CreateTestBackground {
    public static void main(String[] args) {
        try {
            // 创建背景目录
            File backgroundDir = new File("USERDATA/background");
            if (!backgroundDir.exists()) {
                backgroundDir.mkdirs();
            }
            
            // Create test background images
            createGradientBackground("USERDATA/background/blue_gradient.png",
                                   Color.BLUE, Color.CYAN, 1920, 1080);

            createGradientBackground("USERDATA/background/green_gradient.png",
                                   Color.GREEN, Color.YELLOW, 1920, 1080);

            createGradientBackground("USERDATA/background/purple_gradient.png",
                                   Color.MAGENTA, Color.PINK, 1920, 1080);

            createGradientBackground("USERDATA/background/black_white_gradient.png",
                                   Color.BLACK, Color.WHITE, 1920, 1080);

            System.out.println("Test background images created successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void createGradientBackground(String filename, Color color1, Color color2, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 设置渐变
            GradientPaint gradient = new GradientPaint(0, 0, color1, width, height, color2);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            
            // Add decorative text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "Test Background";
            int x = (width - fm.stringWidth(text)) / 2;
            int y = height / 2;
            g2d.drawString(text, x, y);
            
            g2d.dispose();
            
            // Save image
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Created background image: " + filename);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
