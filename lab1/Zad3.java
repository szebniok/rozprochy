package lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class Server3 {
    void listen() {
        try (DatagramSocket socket = new DatagramSocket(12345)) {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                int msg = ByteBuffer.wrap(packet.getData()).order(ByteOrder.LITTLE_ENDIAN).getInt();
                System.out.println("received: " + msg + " from " + packet.getAddress().getHostAddress());
                buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(msg + 1).array();

                DatagramPacket reply = new DatagramPacket(buffer, buffer.length, packet.getSocketAddress());
                socket.send(reply);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Zad3 {
    public static void main(String[] args) {
        if (args.length == 0) return;
        if (args[0].equals("server")) {
            Server3 server = new Server3();
            server.listen();
        } else {
            Client client = new Client();
            client.send();
        }
    }
}
