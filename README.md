# FileTransfer

FileTransfer is a platform independent console application. It is used for transferring files between two devices through local 
network by ip and port. 

## Getting Started
### Requirements
Java 8, Apache Maven
### Build
Clone the repository:

`git clone https://github.com/ponaguynik/file-transfer.git`

Build with maven:

`mvn clean install`

Copy **target/file-transfer.jar** file whenever you want to use it.
## Usage
To transfer files between two devices you have to have **file-transfer.jar** on each of them.

To send files run **file-transfer.jar** with the *send* command on the first device.

To receive the files run **file-transfer.jar** with the *receive* command on the second device.

Usage of **file-transfer.jar**:

`java -jar file-transfer.jar [command] [ip] [path]`

Where
```
[command]: 'send', 'receive';
[ip]: ip address of a receiver or a sender (127.0.0.1);
[path]: for 'send' - files to send (../file.txt /home/user/test.png),
        for 'receive' - directory where to store (../direct).
```

If app cannot make connection you can interrupt connection attempt by typing `stop`.
## Example
The first device:

`java -jar file-transfer.jar send 192.168.0.102 /home/user/exm.txt ../../exm.jpg`

The second device:

`java -jar file-transfer.jar receive 192.168.0.101 /home/user/storage`
