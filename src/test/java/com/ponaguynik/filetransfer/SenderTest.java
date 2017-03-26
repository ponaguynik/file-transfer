package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.connection.ConnectionFactory;
import com.ponaguynik.filetransfer.connection.ConnectionType;
import org.junit.Test;

import java.io.File;

public class SenderTest {

    //Uncomment this code to test send()

    @Test
    public void send() throws Exception {
        deleteTestFiles();
        Connection connection = ConnectionFactory.getConnection(ConnectionType.SERVER, "127.0.0.1", 58000);
        assert connection != null;
        connection.connect();
        Sender sender = new Sender(connection, 1);
        sender.send(new File("src/test/resources/test-img.png"));
        sender.send(new File("src/test/resources/test-text.txt"));
        connection.close();
    }

    private void deleteTestFiles() {
        File testImg = new File("src/test/resources/destination/test-img.png");
        testImg.delete();
        File testText = new File("src/test/resources/destination/test-text.txt");
        testText.delete();
    }
}