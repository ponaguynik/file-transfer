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
    public Sender(Connection connection, int fileCount) throws IOException {
        if (connection.isClosed())
            throw new IOException("Connection is not established or closed");

        this.connection = connection;

        input = new ObjectInputStream(connection.getInputStream());
        output = new ObjectOutputStream(connection.getOutputStream());

        output.writeObject(fileCount);
    }

    /**
     * Send file to the receiver.
     *
     * @param file to send.
     */
    public void send(File file) throws IOException, ReportException {
        if (connection.isClosed())
            throw new IOException("Connection is closed");

        if (file.isDirectory())
            throw new IOException("Cannot send directory");

        //Check file for existence and send report
        if (file.exists())
            sendReport(true);
        else {
            sendReport(false);
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        String fileName = file.getName();

        //Send file name to the receiver
        output.writeObject(fileName);

        //Wait report whether receiver can have this file
        if (!waitReport()) {
            throw new ReportException();
        }

        //Send the file and report
        sendFile(file);

        //Wait report whether receiver has got the file
        if (!waitReport()) {
            throw new ReportException();
        }
    }

    private void sendFile(File file) throws IOException {
        try (
                BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(file))
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
