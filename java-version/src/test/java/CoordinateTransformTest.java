public class CoordinateTransformTest {
    
    public static void main(String[] args) {
        System.out.println("=== 坐标转换测试 ===");
        
        // 测试数据：从project_data.json中提取的实际数据
        int editCanvasX = 812;
        int editCanvasY = 660;
        int editCanvasWidth = 94;
        int editCanvasHeight = 20;
        
        // 原始分辨率
        int oldResWidth = 1024;
        int oldResHeight = 768;
        
        // 新分辨率
        int newResWidth = 1920;
        int newResHeight = 1080;
        
        System.out.println("原始数据:");
        System.out.println("  编辑画布坐标: (" + editCanvasX + "," + editCanvasY + "," + editCanvasWidth + "," + editCanvasHeight + ")");
        System.out.println("  原始分辨率: " + oldResWidth + "x" + oldResHeight);
        System.out.println("  新分辨率: " + newResWidth + "x" + newResHeight);
        
        // 计算相对位置
        double relativeX = (double) editCanvasX / oldResWidth;
        double relativeY = (double) editCanvasY / oldResHeight;
        double relativeWidth = (double) editCanvasWidth / oldResWidth;
        double relativeHeight = (double) editCanvasHeight / oldResHeight;
        
        System.out.println("\n相对位置:");
        System.out.println("  X: " + String.format("%.6f", relativeX) + " (" + String.format("%.2f%%", relativeX * 100) + ")");
        System.out.println("  Y: " + String.format("%.6f", relativeY) + " (" + String.format("%.2f%%", relativeY * 100) + ")");
        System.out.println("  Width: " + String.format("%.6f", relativeWidth) + " (" + String.format("%.2f%%", relativeWidth * 100) + ")");
        System.out.println("  Height: " + String.format("%.6f", relativeHeight) + " (" + String.format("%.2f%%", relativeHeight * 100) + ")");
        
        // 计算新的编辑画布坐标
        int newEditCanvasX = (int) Math.round(relativeX * newResWidth);
        int newEditCanvasY = (int) Math.round(relativeY * newResHeight);
        int newEditCanvasWidth = (int) Math.round(relativeWidth * newResWidth);
        int newEditCanvasHeight = (int) Math.round(relativeHeight * newResHeight);
        
        System.out.println("\n新的编辑画布坐标:");
        System.out.println("  (" + newEditCanvasX + "," + newEditCanvasY + "," + newEditCanvasWidth + "," + newEditCanvasHeight + ")");
        
        // 验证：计算在新分辨率下的相对位置
        double newRelativeX = (double) newEditCanvasX / newResWidth;
        double newRelativeY = (double) newEditCanvasY / newResHeight;
        double newRelativeWidth = (double) newEditCanvasWidth / newResWidth;
        double newRelativeHeight = (double) newEditCanvasHeight / newResHeight;
        
        System.out.println("\n验证 - 新坐标的相对位置:");
        System.out.println("  X: " + String.format("%.6f", newRelativeX) + " (" + String.format("%.2f%%", newRelativeX * 100) + ")");
        System.out.println("  Y: " + String.format("%.6f", newRelativeY) + " (" + String.format("%.2f%%", newRelativeY * 100) + ")");
        System.out.println("  Width: " + String.format("%.6f", newRelativeWidth) + " (" + String.format("%.2f%%", newRelativeWidth * 100) + ")");
        System.out.println("  Height: " + String.format("%.6f", newRelativeHeight) + " (" + String.format("%.2f%%", newRelativeHeight * 100) + ")");
        
        // 计算误差
        double errorX = Math.abs(relativeX - newRelativeX);
        double errorY = Math.abs(relativeY - newRelativeY);
        double errorWidth = Math.abs(relativeWidth - newRelativeWidth);
        double errorHeight = Math.abs(relativeHeight - newRelativeHeight);
        
        System.out.println("\n转换误差:");
        System.out.println("  X误差: " + String.format("%.8f", errorX));
        System.out.println("  Y误差: " + String.format("%.8f", errorY));
        System.out.println("  Width误差: " + String.format("%.8f", errorWidth));
        System.out.println("  Height误差: " + String.format("%.8f", errorHeight));
        
        // 测试主界面显示转换
        System.out.println("\n=== 主界面显示转换测试 ===");
        
        // 假设屏幕尺寸
        int screenWidth = 1920;
        int screenHeight = 1080;
        
        System.out.println("屏幕尺寸: " + screenWidth + "x" + screenHeight);
        
        // 主界面转换：从编辑画布坐标直接转换到屏幕坐标
        int mainDisplayX = (int) Math.round(relativeX * screenWidth);
        int mainDisplayY = (int) Math.round(relativeY * screenHeight);
        int mainDisplayWidth = (int) Math.round(relativeWidth * screenWidth);
        int mainDisplayHeight = (int) Math.round(relativeHeight * screenHeight);
        
        System.out.println("主界面显示坐标: (" + mainDisplayX + "," + mainDisplayY + "," + mainDisplayWidth + "," + mainDisplayHeight + ")");
        
        // 验证主界面相对位置
        double mainRelativeX = (double) mainDisplayX / screenWidth;
        double mainRelativeY = (double) mainDisplayY / screenHeight;
        
        System.out.println("主界面相对位置: X=" + String.format("%.6f", mainRelativeX) + ", Y=" + String.format("%.6f", mainRelativeY));
        System.out.println("原始相对位置: X=" + String.format("%.6f", relativeX) + ", Y=" + String.format("%.6f", relativeY));
        System.out.println("主界面误差: X=" + String.format("%.8f", Math.abs(relativeX - mainRelativeX)) + 
                         ", Y=" + String.format("%.8f", Math.abs(relativeY - mainRelativeY)));
    }
}
