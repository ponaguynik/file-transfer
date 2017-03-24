package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.exception.ReportException;

import java.io.*;

public class Sender {

    private Connection connection;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    public Sender(Connection connection, int fileCount) {
        if (connection.isClosed())
            throw new RuntimeException("Connection is not established or closed");

        this.connection = connection;

        try {
            input = new ObjectInputStream(connection.getInputStream());
            output = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            output.writeObject(fileCount);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void send(String path) throws IOException, ReportException {
        if (connection.isClosed())
            throw new IOException("Connection is closed");

        String[] splitPath = path.split("/");
        String fileName = splitPath[splitPath.length-1];
        File file = new File(path);
        if (!file.exists()) {
            sendReport(false);
            throw new IOException("File does not exist: " + path);
        }
        sendReport(true);

        output.writeObject(fileName);
        if (!waitReport()) {
            throw new ReportException();
        }

        try (
                BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(path))
        ) {
            byte[] data = new byte[fileInput.available()];
            fileInput.read(data);
            sendReport(true);
            output.writeObject(data);
        } catch (IOException e) {
            sendReport(false);
            throw e;
        }

        if (!waitReport()) {
            throw new ReportException();
        }
    }

    private boolean waitReport() throws IOException {
        try {
            return (boolean) input.readObject();
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    private void sendReport(boolean success) throws IOException {
        output.writeObject(success);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
