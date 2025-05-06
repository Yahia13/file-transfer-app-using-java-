import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("📡 Connected to server.");

            while (true) {
                System.out.print("Enter file name (or 'exit' to quit): ");
                String fileName = scanner.nextLine().trim();
                dos.writeUTF(fileName);

                if (fileName.equalsIgnoreCase("exit")) break;

                System.out.print("Enter file genre: ");
                String genre = scanner.nextLine().trim();
                dos.writeUTF(genre);

                String status = dis.readUTF();
                if (status.equals("NOT_FOUND")) {
                    System.out.println("❌ File not found on server.");
                } else {
                    File receivedFile = new File("received_" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            if (bytesRead < 4096) break; // simple end-of-file detection
                        }
                        System.out.println("✅ File received: " + receivedFile.getAbsolutePath());
                    }
                }
            }

            System.out.println("👋 Disconnected from server.");

        } catch (IOException e) {
            System.out.println("❌ Could not connect to server.");
            e.printStackTrace();
        }
    }
}
