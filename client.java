import java.io.*;
import java.net.*;
import javax.swing.*;

public class client {
    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int PORT = 5000;

        // Step 1: Open a file dialog to select the file to download from the server
        JFileChooser fileChooser = new JFileChooser("C:\\Users\\yahia\\Downloads");  // Server's file directory
        fileChooser.setDialogTitle("Select a file to download from the server");
        int result = fileChooser.showOpenDialog(null);

        // If no file is selected, exit
        if (result != JFileChooser.APPROVE_OPTION) {
            System.out.println("❌ No file selected.");
            return;
        }

        String selectedFileName = fileChooser.getSelectedFile().getName();
        System.out.println("📂 Selected file: " + selectedFileName);  // Log the selected file name

        try (
            Socket socket = new Socket(serverIP, PORT);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            // Step 2: Request the file from the server
            dos.writeUTF(selectedFileName);  // Send the file name to the server

            // Step 3: Check if the file is found
            String fileStatus = dis.readUTF();
            if (fileStatus.equals("NOT_FOUND")) {
                System.out.println("❌ File not found on the server.");
                return;
            }

            // Step 4: Receive the file information (name and size)
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            File saveFile = new File("downloads/" + fileName);  // Save file inside the project folder
            System.out.println("📥 Downloading file: " + saveFile.getAbsolutePath());

            // Step 5: Receive the file content
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize) {
                    bytesRead = dis.read(buffer);
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                System.out.println("✅ File downloaded to: " + saveFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
