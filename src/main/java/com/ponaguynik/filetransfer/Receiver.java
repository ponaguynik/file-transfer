package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.exception.ReportException;

import java.io.*;

public class Receiver {

    private Connection connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    private int fileCount;

    public Receiver(Connection connection) {
        if (connection.isClosed())
            throw new RuntimeException("Connection is not established or closed");

        this.connection = connection;

        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileCount = (int) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public String receive(String path) throws IOException, ReportException {
        if (connection.isClosed())
            throw new IOException("Connection is closed");

        if (!waitSender()) {
            throw new ReportException();
        }

        String fileName = null;
        try {
            fileName = (String) input.readObject();
        } catch (ClassNotFoundException ignore) {}

        File file = new File(path + File.separator + fileName);
        try {
            if (!file.createNewFile()) {
                sendReport(false);
                throw new IOException("File with such name already exists");
            }
        } catch (IOException e) {
            sendReport(false);
            throw  e;
        }
        sendReport(true);

        if (waitSender()) {
            try (
                    BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(file))
            ) {
                byte[] data = (byte[]) input.readObject();
                fileOutput.write(data);
            } catch (IOException e) {
                sendReport(false);
                throw e;
            } catch (ClassNotFoundException ignore) {}
        } else {
            throw new ReportException();
        }

        sendReport(true);

        return fileName;
    }

    private void sendReport(boolean success) throws IOException {
        input.readFully(new byte[input.available()]);
        output.writeObject(success);
    }

    private boolean waitSender() throws IOException {
        try {
            return (boolean) input.readObject();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public int getFileCount() {
        return fileCount;
    }
}
