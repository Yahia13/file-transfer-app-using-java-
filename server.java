import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class server {
    private static final int PORT = 5000;

    // Replace with your actual DB info
    private static final String DB_URL = "jdbc:mysql://localhost:3306/file_transfer_db";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🔌 Server started. Waiting for client...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("✅ Client connected: " + clientSocket.getInetAddress());

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

            String fileName = dis.readUTF();
            String genre = dis.readUTF();

            System.out.println("📁 Requested file: " + fileName + " | Genre: " + genre);

            File file = new File("files/" + fileName);
            if (!file.exists()) {
                dos.writeUTF("ERROR");
                System.out.println("❌ File not found.");
                return;
            } else {
                dos.writeUTF("OK");
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }

            fis.close();
            dis.close();
            dos.close();
            clientSocket.close();

            logTransferToDatabase(fileName, genre);
            System.out.println("✅ File sent and logged to MySQL DB.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logTransferToDatabase(String fileName, String genre) {
        String sql = "INSERT INTO file_transfers (file_name, genre, transfer_date) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pstmt.setString(1, fileName);
            pstmt.setString(2, genre);
            pstmt.setString(3, now);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Failed to log to MySQL DB.");
            e.printStackTrace();
        }
    }
}
