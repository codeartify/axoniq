package ch.fitnesslab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitnessLabApplication {
    public static void main(String[] args) {
        if (System.getProperty("io.netty.noUnsafe") == null) {
            System.setProperty("io.netty.noUnsafe", "true");
        }
        SpringApplication.run(FitnessLabApplication.class, args);
    }
}
