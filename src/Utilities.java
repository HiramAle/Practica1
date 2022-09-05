import java.io.*;
import java.util.Objects;

public class Utilities {

    public static void send_file(DataOutputStream dataOutputStream, File f) throws IOException {
        System.out.print("Name: " + f.getName());
        System.out.println(" Size: " + f.length() + " bytes");

        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(f.getAbsolutePath()));
        dataOutputStream.writeUTF(f.getName());
        dataOutputStream.flush();
        dataOutputStream.writeLong(f.length());
        dataOutputStream.flush();

        long bytes_sent = 0;
        int bytes_read = 0, percentage = 0;

        while (bytes_sent < f.length()) {
            byte[] b = new byte[1500];
            bytes_read = dataInputStream.read(b);
            dataOutputStream.write(b, 0, bytes_read);
            dataOutputStream.flush();
            bytes_sent = bytes_sent + bytes_read;
            percentage = (int) ((bytes_sent * 100) / f.length());
            System.out.println(percentage + "%");
        }

        System.out.println("File sent");
        dataInputStream.close();
    }

    public static void receive_file(DataInputStream dataInputStream, String path) throws IOException {
        String name = dataInputStream.readUTF();
        long size = dataInputStream.readLong();
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(path + "\\" + name));
        long received = 0;
        int read = 0, percentage = 0;

        while (received < size) {
            byte[] b = new byte[1500];
            read = dataInputStream.read(b);
            dataOutputStream.write(b, 0, read);
            dataOutputStream.flush();
            received = received + read;
            percentage = (int) ((received * 100) / size);
            System.out.println("Received " + percentage + " %");
        }

        System.out.println("File " + name + " received");
        dataOutputStream.close();
    }


    public static void send_handler(DataOutputStream dataOutputStream, File[] files) throws IOException {
        dataOutputStream.write(files.length);
        dataOutputStream.flush();
        for (File file : files) {
            if (file.isDirectory()) {
                dataOutputStream.writeUTF("folder");
                dataOutputStream.flush();
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.flush();
                File[] newFolder = file.listFiles() != null ? file.listFiles() : new File[0];
                send_handler(dataOutputStream, newFolder);
            } else {
                dataOutputStream.writeUTF("file");
                dataOutputStream.flush();
                send_file(dataOutputStream, file);
            }
        }
    }

    public static void receive_handler(DataInputStream dataInputStream, String path) throws IOException {
        int size = dataInputStream.read();
        String type;
        String name;
        for (int i = 0; i < size; i++) {
            type = dataInputStream.readUTF();
            if (type.equals("folder")) {
                name = dataInputStream.readUTF();
                File newFolder = new File(path + "\\" + name);
                newFolder.mkdirs();
                newFolder.setWritable(true);
                receive_handler(dataInputStream, newFolder.getAbsolutePath());
            } else if (type.equals("file")) {
                receive_file(dataInputStream, path);
            }

        }


    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] content = directoryToBeDeleted.listFiles();
        if (content != null) {
            for (File file : content) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }


}
