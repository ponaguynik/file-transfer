package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.connection.ConnectionFactory;
import com.ponaguynik.filetransfer.connection.ConnectionType;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;


import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileTransferTest {

    @Test
    public void checkArgs() {
        String[] testArgs = new String[3];
        //Test for empty args
        assertTrue(FileTransfer.checkArgs(testArgs).startsWith("Usage is:"));
        //Test for wrong command
        testArgs[0] = "sendf";
        testArgs[1] = "123.3213.12";
        testArgs[2] = "/asd/esr";
        assertTrue(FileTransfer.checkArgs(testArgs).startsWith("Usage is:"));
        //Test for incorrect ip address
        testArgs[0] = "receive";
        testArgs[1] = "256.0.0.1";
        assertTrue(FileTransfer.checkArgs(testArgs).startsWith("Incorrect ip address"));
        //Test for wrong path
        testArgs[1] = "127.0.0.1";
        testArgs[2] = "../asdf";
        assertTrue(FileTransfer.checkArgs(testArgs).startsWith("Cannot find directory"));
        //Everything is correct
        testArgs[2] = ".";
        assertNull(FileTransfer.checkArgs(testArgs));
    }

    @Test
    public void send() throws IOException {
        String ip = "127.0.2.1";
        File[] files = new File[2];
        files[0] = new File("src/test/resources/test-text.txt");
        files[1] = new File("src/test/resources/test-img.png");

        Connection connection = ConnectionFactory.getConnection(ConnectionType.CLIENT, ip, 48200);
        assert connection != null;
        connection.connect();

        FileTransfer.send(connection, files);
    }

    @Test
    public void receive() throws IOException {
        deleteTestFiles();
        String ip = "127.0.0.1";
        File directory = new File("src/test/resources/destination");

        Connection connection = ConnectionFactory.getConnection(ConnectionType.SERVER, ip, 48200);
        assert connection != null;
        connection.connect();

        FileTransfer.receive(connection, directory);
    }

    @Test
    public void testMainSend() {
        String[] args = {"send", "127.0.0.1", "src/test/resources/test-text.txt",
                "src/test/resources/test-img.png"};
        FileTransfer.main(args);
    }

    @Test
    public void testMainReceive() {
        deleteTestFiles();
        String[] args = {"receive", "127.0.2.1", "src/test/resources/destination"};
        FileTransfer.main(args);
    }

    private void deleteTestFiles() {
        File testImg = new File("src/test/resources/destination/test-img.png");
        testImg.delete();
        File testText = new File("src/test/resources/destination/test-text.txt");
        testText.delete();
    }
}