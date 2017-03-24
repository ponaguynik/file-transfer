package com.ponaguynik.filetransfer;

import com.ponaguynik.filetransfer.connection.Connection;
import com.ponaguynik.filetransfer.connection.ConnectionFactory;
import com.ponaguynik.filetransfer.connection.ConnectionType;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class FileTransfer {

    private static final int SOCKET_PORT = 49200;

    private static Connection connection;


    public static void main(String... args) {
        String checkString = checkArgs(args);
        if (checkString != null) {
            System.out.println(checkString);
            System.exit(1);
        }

        StopThread stopThread = new StopThread();
        stopThread.start();

        if (args[0].equals("send"))
            System.out.println(send(args));
        else
            System.out.println(receive(args[1], args[2]));

        stopThread.interrupt();
        System.out.println("Type something to exit");
    }

    public static String checkArgs(String[] args) {
        if (args.length < 3)
            return usageMessage();

        String command = args[0];
        String ip = args[1];
        String[] paths = Arrays.copyOfRange(args, 2, args.length);

        if (command == null || !command.equals("send") && !command.equals("receive")) {
            return usageMessage();
        }

        if (!ip.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.)" +
                "{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
            return "Incorrect ip address";
        }

        if (command.equals("send")) {
            for (String path : paths) {
                File file = new File(path);

                if (file.isDirectory())
                    return "FileTransfer can send only files\n\t" + path;

                if (!file.exists()) {
                    return "Couldn't find file:\n\t" + path;
                }
            }
        } else if (command.equals("receive")) {
            File file = new File(paths[0]);

            if (file.isFile())
                return "Received files can be stored only in folders:\n\t" + paths[0];

            if (!file.exists())
                return "Couldn't find directory:\n\t" + paths[0];
        }

        return null;
    }

    private static String usageMessage() {
        return  "Usage is: java -jar file-transfer.jar [command] [ip] [path];\n\t[command]: 'send', 'receive';" +
                "\n\t[ip]: ip address of a receiver or a sender (0.0.0.0);" +
                "\n\t[path] (can send multiple files): absolute or relative path (../file.txt).";
    }

    public static String send(String[] args) {
        String ip = args[1];
        String[] filePaths = Arrays.copyOfRange(args, 2, args.length);

        connection = ConnectionFactory.getConnection(ConnectionType.SERVER, ip, SOCKET_PORT);
        try {
            assert connection != null;
            connection.connect();
        } catch (IOException e) {
            return "Couldn't establish connection to " + ip;
        }

        Sender sender = new Sender(connection, filePaths.length);

        for (String filePath : filePaths) {
            String[] splitPath = filePath.split("/");
            String fileName = splitPath[splitPath.length - 1];

            try {
                sender.send(filePath);
                System.out.println(fileName + " has been successfully sent to " + ip);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            } catch (Exception e) {
                System.out.println("Failed to send the file " + fileName + " to " + ip);
            }
        }
        connection.close();

        return "Finished sending the files";
    }

    public static String receive(String ip, String storagePath) {
        connection = ConnectionFactory.getConnection(ConnectionType.CLIENT, ip, SOCKET_PORT);
        try {
            assert connection != null;
            connection.connect();
        } catch (IOException e) {
            return "Couldn't establish connection to " + ip;
        }

        Receiver receiver = new Receiver(connection);
        int fileCount = receiver.getFileCount();

        while (fileCount-- != 0) {
            try {
                String fileName = receiver.receive(storagePath);
                System.out.println(fileName + " has been stored to " + storagePath);
            } catch (IOException e) {
                System.out.println(e.getMessage() + ": " + storagePath);
            } catch (Exception e) {
                System.out.println("Failed to receive a file");
            }
        }
        connection.close();

        return "Finished receiving the files";
    }

    public static class StopThread extends Thread {

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.interrupted()) {
                if (scanner.nextLine().equalsIgnoreCase("stop")) {
                    try {
                        if (connection != null)
                            connection.interrupt();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

}