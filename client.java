import java.io.*;
import java.net.*;
import javax.swing.*;

public class client {
    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int PORT = 5000;

        try (
            Socket socket = new Socket(serverIP, PORT);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {
            // Step 1: Request list of available files
            dos.writeUTF("LIST");
            DefaultListModel<String> fileListModel = new DefaultListModel<>();
            String fileName;
            while (!(fileName = dis.readUTF()).equals("END")) {
                fileListModel.addElement(fileName);
            }

            // Step 2: Show file chooser dialog
            JList<String> fileList = new JList<>(fileListModel);
            fileList.setVisibleRowCount(10);
            JScrollPane scrollPane = new JScrollPane(fileList);
            scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));
            fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            int option = JOptionPane.showConfirmDialog(null, scrollPane, "Select a file to download",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option != JOptionPane.OK_OPTION || fileList.getSelectedValue() == null) {
                System.out.println("❌ No file selected.");
                return;
            }

            String selectedFile = fileList.getSelectedValue();
            dos.writeUTF("GET");
            dos.writeUTF(selectedFile);

            String status = dis.readUTF();
            if (status.equals("NOT_FOUND")) {
                System.out.println("❌ File not found on server.");
                return;
            }

            // Step 3: Receive the file
            File saveFile = new File(System.getProperty("user.home") + "/Downloads/" + selectedFile);
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    if (bytesRead < 4096) break; // done
                }
            }

            System.out.println("✅ File downloaded to " + saveFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
