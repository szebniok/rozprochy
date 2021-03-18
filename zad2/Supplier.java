package zad2;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Supplier {
    private Channel channel;
    private AtomicInteger newId = new AtomicInteger(0);

    Supplier() throws IOException, TimeoutException {
        Connection connection = new ConnectionFactory().newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare("MOUNT", BuiltinExchangeType.TOPIC);
    }

    public void init() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the gear that you are supplying (separate items with comma): ");
        String[] suppliedGear = scanner.nextLine().split(",");
        for (String s : suppliedGear) {
            consumeGear(s);
        }
        consumeAdmin();
    }

    public void consumeGear(String requestedGear) throws IOException {
        channel.queueDeclare(requestedGear, true, false, false, null);
        channel.queueBind(requestedGear, "MOUNT", "gear." + requestedGear);

        Consumer gearConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String teamName = new String(body);
                String requestedGear = envelope.getRoutingKey().split("\\.")[1];
                System.out.println("Received order from: " + teamName + " for " + requestedGear);
                String ack = newId.getAndIncrement() + ":" + requestedGear;
                channel.basicPublish("MOUNT", "ack." + teamName, null, ack.getBytes());
            }
        };

        channel.basicConsume(requestedGear, true, gearConsumer);
    }

    private void consumeAdmin() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "MOUNT", "admin.#.supplier.#");

        Consumer acksConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("Admin message: " + msg);
            }
        };

        channel.basicConsume(queueName, true, acksConsumer);
    }
}