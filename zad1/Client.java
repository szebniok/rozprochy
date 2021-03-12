package zad1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String ASCII_ART =
            "żółta kaczka\n" +
            "   (@_      _      _      _\n" +
            "\\\\\\_\\     >(.)__ <(.)__ =(.)__\n" +
            "<____)     (___/  (___/  (___/";

    private static final String MULTICAST_ADDRESS = "224.23.12.77";

    String nickname;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    DatagramSocket datagramSocket;
    MulticastSocket multicastSocket;

    public void init() {
        Scanner stdin = new Scanner(System.in);
        System.out.print("Enter your nickname: ");
        this.nickname = stdin.nextLine();

        new Thread(this::connect).start();

        while (true) {
            printPrompt();
            String msg = stdin.nextLine();

            if (msg.equalsIgnoreCase("U")) {
                sendUDP(false);
            } else if (msg.equalsIgnoreCase("M")) {
                sendUDP(true);
            } else {
                send(msg);
            }
        }
    }

    public void connect() {
        try (Socket clientSocket = new Socket("127.0.0.1", 12345);
             DatagramSocket datagramSocket = new DatagramSocket(clientSocket.getLocalPort());
             MulticastSocket multicastSocket = new MulticastSocket(7777)) {

            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeUTF(nickname);

            this.datagramSocket = datagramSocket;
            new Thread(() -> listenUDP(false)).start();

            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            InetSocketAddress multicastAddress = new InetSocketAddress(MULTICAST_ADDRESS, 7777);
            multicastSocket.joinGroup(multicastAddress, networkInterface);
            this.multicastSocket = multicastSocket;
            new Thread(() -> listenUDP(true)).start();

            while (true) {
                String msg = inputStream.readUTF();
                clearLine();
                System.out.println("\r" + msg);
                printPrompt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenUDP(boolean isMulticast) {
        byte[] buffer = new byte[1024];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                if (isMulticast) {
                    multicastSocket.receive(packet);
                } else {
                    datagramSocket.receive(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[] fullMsg = new String(packet.getData()).trim().split(":");
            String sender = fullMsg[0];
            String msg = fullMsg[1];

            if (sender.equals(nickname)) {
                continue;
            }

            clearLine();
            String type = isMulticast ? " [UDP Multicast]:" : " [UDP]:";
            System.out.println("\r" + sender + type);
            System.out.println(msg);
            printPrompt();
        }
    }

    public void sendUDP(boolean isMulticast) {
        String msg = nickname + ":" + ASCII_ART;
        byte[] buffer = msg.getBytes();
        try {
            InetAddress destinationAddress =
                    isMulticast ? InetAddress.getByName(MULTICAST_ADDRESS) : InetAddress.getLocalHost();
            int port = isMulticast ? 7777 : 12345;
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, destinationAddress, port);
            if (isMulticast) {
                multicastSocket.send(packet);
            } else {
                datagramSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearLine() {
        System.out.print("\r" + " ".repeat(80));
    }

    private void printPrompt() {
        System.out.print("> ");
    }
}
