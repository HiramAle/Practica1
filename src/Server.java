import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            int port = 1234;
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Server started...");
            System.out.println("Waiting for clients");

            File folder = new File("Server");
            String path = folder.getAbsolutePath();
            folder.mkdirs();
            folder.setWritable(true);

            System.out.println("Server folder: " + path);

            for (; ; ) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected from " + socket.getInetAddress());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream((socket.getOutputStream()));

                boolean running = true;
                while (running) {
                    int option = dataInputStream.read();
                    switch (option) {
                        case 2 -> showServerFiles(dataOutputStream, dataInputStream, folder);
                        case 3 -> Utilities.receive_handler(dataInputStream, path);
                        case 4 -> {
                            running = false;
                            dataInputStream.close();
                            dataOutputStream.close();
                            socket.close();
                        }
                    }
                }
                System.out.println("Client disconnected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showServerFiles(DataOutputStream dataOutputStream, DataInputStream dataInputStream, File folder) throws IOException {
        File[] files = folder.listFiles();
        int size = files != null ? files.length : 0;
        dataOutputStream.write(size);
        if (size == 0) {
            files = new File[0];
        }
        for (File f : files) {
            String type;
            if (f.isDirectory()) {
                type = "folder";
            } else {
                type = "file";
            }
            dataOutputStream.writeUTF(type);
            dataOutputStream.writeUTF(f.getName());
        }

        if (size > 0) {
            int selected = dataInputStream.read();
            String action = dataInputStream.readUTF();

            switch (action) {
                case "download" -> {
                    File[] send = new File[1];
                    send[0] = files[selected];
                    Utilities.send_handler(dataOutputStream, send);
                }
                case "delete" -> {
                    File fileToDelete = new File(files[selected].getAbsolutePath());
                    boolean deleted;
                    if (fileToDelete.isDirectory()) {
                        deleted = Utilities.deleteDirectory(fileToDelete);
                    } else {
                        deleted = fileToDelete.delete();
                    }
                    if (deleted) {
                        System.out.println("Deleted " + fileToDelete.getName());
                        dataOutputStream.writeUTF("Deleted the file " + fileToDelete.getName());
                    } else {
                        System.out.println("Failed to delete " + fileToDelete.getName());
                        dataOutputStream.writeUTF("Failed to delete the file " + fileToDelete.getName());
                    }
                }
                case "open" -> {
                    File newFile = new File(files[selected].getAbsolutePath());
                    showServerFiles(dataOutputStream, dataInputStream, newFile);
                }
            }
        }
    }
}
