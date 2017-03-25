package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.exception.ReportException;

import java.io.*;

/**
 * Receives files from a sender with whom
 * connection is established.
 */
public class Receiver {

    private Connection connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    /**
     * Number of files that will be sent.
     */
    private int fileCount;

    /**
     * Constructor requires a Connection object
     * that connected to the sender.
     * Gets number of files that will be sent.
     */
    public Receiver(Connection connection) {
        if (connection.isClosed())
            throw new RuntimeException("Connection is not established or closed");

        this.connection = connection;

        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            fileCount = (int) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receive file from the sender.
     *
     * @param path absolute or relative path to a
     *             folder where a file should be stored.
     * @return name of received file.
     */
    public String receive(String path) throws IOException, ReportException {
        if (connection.isClosed())
            throw new IOException("Connection is closed");

        //Wait report whether the sender is ready to send a file
        if (!waitReport()) {
            throw new ReportException();
        }

        //Get file name
        String fileName = null;
        try {
            fileName = (String) input.readObject();
        } catch (ClassNotFoundException ignore) {}


        File file = createFile(path, fileName);

        //Send report whether receiver is ready to get a file
        if (file != null) {
            sendReport(true);
        } else {
            sendReport(false);
            throw new IOException("File with such name already exists");
        }

        //Wait until sender is ready to send a file and store it
        if (waitReport()) {
            storeFile(file);
        } else {
            throw new ReportException();
        }

        sendReport(true);

        return fileName;
    }

    private File createFile(String path, String fileName) throws IOException {
        File file = new File(path + File.separator + fileName);
        try {
            if (!file.createNewFile())
                return null;
        } catch (IOException e) {
            sendReport(false);
            throw e;
        }

        return file;
    }

    private void storeFile(File file) throws IOException {
        try (
                BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(file))
        ) {
            byte[] data = (byte[]) input.readObject();
            fileOutput.write(data);
        } catch (IOException e) {
            sendReport(false);
            throw e;
        } catch (ClassNotFoundException ignore) {}
    }

    private void sendReport(boolean success) throws IOException {
        input.readFully(new byte[input.available()]);
        output.writeObject(success);
    }

    private boolean waitReport() throws IOException {
        try {
            return (boolean) input.readObject();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public int getFileCount() {
        return fileCount;
    }
}
