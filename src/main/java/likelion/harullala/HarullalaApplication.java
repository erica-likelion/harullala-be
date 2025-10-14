package likelion.harullala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HarullalaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HarullalaApplication.class, args);
    }

}
