package com.ponaguynik.filetransfer;

import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;


import java.io.File;

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
    public void send() {
        FileTransfer.send(new String[]{"send", "127.0.0.1", "src/test/resources/test-text.txt",
                "src/test/resources/test-img.png"});
    }

    @Test
    public void receive() {
        deleteTestFiles();
        FileTransfer.receive("127.0.0.1", "src/test/resources/destination");

    }

    private void deleteTestFiles() {
        File testImg = new File("src/test/resources/destination/test-img.png");
        testImg.delete();
        File testText = new File("src/test/resources/destination/test-text.txt");
        testText.delete();
    }
}