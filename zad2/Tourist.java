package zad2;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Tourist {
    private Channel channel;

    Tourist() throws IOException, TimeoutException {
        Connection connection = new ConnectionFactory().newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare("MOUNT", BuiltinExchangeType.TOPIC);
    }

    public void init() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your team name:");
        String name = scanner.nextLine();

        consumeAck(name);
        consumeAdmin();

        System.out.println("Enter your gear equipment needs (or type in quit to quit):");
        while (true) {
            String requestedGear = scanner.nextLine();
            if (requestedGear.equalsIgnoreCase("quit")) {
                break;
            }

            System.out.println("Sending gear requirement: " + name + " " + requestedGear);
            channel.basicPublish("MOUNT", "gear." + requestedGear, null, name.getBytes());
        }
    }

    private void consumeAck(String name) throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "MOUNT", "ack." + name);

        Consumer acksConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String[] msg = new String(body).split(":");
                int id = Integer.parseInt(msg[0]);
                String deliveredGear = msg[1];
                System.out.println("Received " + deliveredGear + " with id of " + id);
            }
        };

        channel.basicConsume(queueName, true, acksConsumer);
    }

    private void consumeAdmin() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "MOUNT", "admin.#.tourist.#");

        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("Admin message: " + msg);
            }
        };

        channel.basicConsume(queueName, true, adminConsumer);
    }
}
