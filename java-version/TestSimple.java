public class TestSimple {
    public static void main(String[] args) {
        System.out.println("Test program started");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        // Test if Gson is available
        try {
            Class.forName("com.google.gson.Gson");
            System.out.println("Gson library available");
        } catch (ClassNotFoundException e) {
            System.out.println("Gson library not available: " + e.getMessage());
        }

        System.out.println("Test completed");
    }
}
