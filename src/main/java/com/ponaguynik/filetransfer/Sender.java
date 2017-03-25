package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.exception.ReportException;

import java.io.*;

/**
 * Sends files to a receiver with whom connection
 * is established.
 */
public class Sender {

    private Connection connection;

    private ObjectOutputStream output;
    private ObjectInputStream input;

    /**
     * Constructor requires a Connection object that
     * connected to the receiver and number of files that
     * will be sent.
     *
     * @param fileCount is passing to the receiver to know
     *                  how many files will be sent.
     */
    public Sender(Connection connection, int fileCount) {
        if (connection.isClosed())
            throw new RuntimeException("Connection is not established or closed");

        this.connection = connection;

        try {
            input = new ObjectInputStream(connection.getInputStream());
            output = new ObjectOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            output.writeObject(fileCount);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send file to the receiver.
     *
     * @param path absolute or relative path to a file.
     */
    public void send(String path) throws IOException, ReportException {
        if (connection.isClosed())
            throw new IOException("Connection is closed");

        //Check file for existence and extract file name
        String fileName = extractFileName(path);

        if (fileName != null)
            sendReport(true);
        else {
            sendReport(false);
            throw new IOException("File does not exist: " + path);
        }

        //Send file name to the receiver
        output.writeObject(fileName);

        //Wait report whether receiver can have this file
        if (!waitReport()) {
            throw new ReportException();
        }

        //Send the file and report
        sendFile(path);

        //Wait report whether receiver has got the file
        if (!waitReport()) {
            throw new ReportException();
        }
    }
    
    private String extractFileName(String path) {
        String[] splitPath = path.split("/");
        String fileName = splitPath[splitPath.length-1];
        File file = new File(path);

        return file.exists() ? fileName : null;
    }

    private void sendFile(String path) throws IOException {
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
}
