package zad2;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static final String GEAR = "GEAR";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        if (args[0].equalsIgnoreCase("tourist")) {
            Tourist tourist = new Tourist();
            tourist.init();
        } else if (args[0].equalsIgnoreCase("supplier")) {
            Supplier supplier = new Supplier();
            supplier.init();
        } else {
            Admin admin = new Admin();
            admin.init();
        }
    }
}
