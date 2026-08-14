package co.sena.iam.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Composition root del contenedor iam-api (bootstrap + wiring). */
@SpringBootApplication
public class IamApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamApiApplication.class, args);
    }
}
