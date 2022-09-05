import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;


public class Client {
    public static void main(String[] args) {
        try {
//            Connection
            int port = 1234;
            String address = "127.0.0.1";
            Socket socket = new Socket(address, port);
            System.out.println("Successful connection with Server...");
            File client_folder_file = new File("Client");
            String client_folder_path = client_folder_file.getAbsolutePath();
            client_folder_file.mkdirs();
            client_folder_file.setWritable(true);

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            boolean running = true;
            while (running) {
                int option = menu();
                dataOutputStream.write(option);
                switch (option) {
                    case 1 -> showClientFiles(client_folder_file.listFiles());
                    case 2 -> showServerFiles(dataOutputStream, dataInputStream, client_folder_path);
                    case 3 -> {
//                        Upload File from Client to Server
                        System.out.println("Select File(s)/Folder");
//                        File Chooser
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setMultiSelectionEnabled(true);
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//                        File Chooser result
                        int result = fileChooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            Utilities.send_handler(dataOutputStream, fileChooser.getSelectedFiles());
                        }
                    }
                    case 4 -> {
                        running = false;
                        dataInputStream.close();
                        dataOutputStream.close();
                        socket.close();
                    }
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    public static void showServerFiles(DataOutputStream dataOutputStream, DataInputStream dataInputStream, String localFolder) throws IOException {
        int numberOfFiles = dataInputStream.read();
        String[] pathnames = new String[numberOfFiles];
        String[] types = new String[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            types[i] = dataInputStream.readUTF();
            pathnames[i] = dataInputStream.readUTF();
        }

        numberedShowFiles(pathnames, types);
        if (numberOfFiles > 0) {
            int selection = selectOption();

            if (selection < pathnames.length) {
                System.out.println("[1] Download");
                System.out.println("[2] Delete");
                if (types[selection].equals("folder")) {
                    System.out.println("[3] Open");
                }

                String action = "";

                switch (selectOption()) {
                    case 1 -> action = "download";
                    case 2 -> action = "delete";
                    case 3 -> action = "open";
                }

                dataOutputStream.write(selection);
                dataOutputStream.writeUTF(action);

                switch (action) {
                    case "download" -> {
                        Utilities.receive_handler(dataInputStream, localFolder);
                    }
                    case "delete" -> {
                        System.out.println(dataInputStream.readUTF());
                    }
                    case "open" -> {
                        showServerFiles(dataOutputStream, dataInputStream, localFolder);
                    }
                }
            } else {
                dataOutputStream.write(selection);
            }
        }
    }

    public static int selectOption() {
        System.out.println("Select number");
        Scanner scan = new Scanner(System.in);
        return scan.nextInt();
    }

    public static void showClientFiles(File[] clientFiles) {
        if (clientFiles == null || clientFiles.length == 0) {
            System.out.println("Empty folder");
        } else {
            for (int i = 0; i < clientFiles.length; i++) {
                System.out.print("[" + i + "]" + " ");
                if (clientFiles[i].isDirectory()) {
                    System.out.print("<DIR> ");
                } else {
                    System.out.print("      ");
                }
                System.out.println(clientFiles[i].getName());
            }
            System.out.println("[" + clientFiles.length + "] Exit");


            int selection = selectOption();
            if (selection < clientFiles.length) {
                File selectedFile = new File(clientFiles[selection].getAbsolutePath());

                System.out.println("[1] Delete");
                if (clientFiles[selection].isDirectory()) {
                    System.out.println("[2] Open");
                }

                switch (selectOption()) {
                    case 1 -> {
                        boolean deleted;
                        if (selectedFile.isDirectory()) {
                            deleted = Utilities.deleteDirectory(selectedFile);
                        } else {
                            deleted = selectedFile.delete();
                        }
                        if (deleted) {
                            System.out.println("Deleted " + clientFiles[selection].getName());

                        } else {
                            System.out.println("Failed to delete " + clientFiles[selection].getName());
                        }

                    }
                    case 2 -> {
                        showClientFiles(selectedFile.listFiles());
                    }
                }
            }
        }
    }

    public static void numberedShowFiles(String pathnames[], String types[]) {
        if (pathnames == null || pathnames.length == 0) {
            System.out.println("Empty folder");
        } else {
            for (int i = 0; i < pathnames.length; i++) {
                System.out.print("[" + i + "]" + " ");
                if (types[i].equals("folder")) {
                    System.out.print("<DIR> ");
                } else {
                    System.out.print("      ");
                }
                System.out.println(pathnames[i]);
            }
            System.out.println("[" + pathnames.length + "] Exit");
        }
    }

    public static int menu() {
        boolean end = false;
        int selected = 4;
        while (!end) {
            Scanner scan = new Scanner(System.in);
            System.out.println("Select Option");
            System.out.println("1. Show local files");
            System.out.println("2. Show server files");
            System.out.println("3. Upload folder/file");
            System.out.println("4. Exit");

            selected = Integer.parseInt(String.valueOf(scan.nextLine().charAt(0)));

            if (selected <= 4 && selected > 0) {
                end = true;
            } else {
                System.out.println("Select a valid option");
            }
        }
        return selected;
    }


}
