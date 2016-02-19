package rocks.reckt3r;

import me.reckter.telegram.Telegram;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * Created by hannes on 18.02.16.
 */
@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan({"me.reckter", "rocks.reckt3r"})
@EnableJpaRepositories
@EntityScan
@Component
@Configuration
@EnableScheduling
public class Main {


    @Bean
    public Telegram telegram() {
        return new Telegram(System.getenv("BOT_KEY"), Integer.parseInt(System.getenv("ADMIN_ACC")), System.getenv("ERROR_BOT_KEY"));
    }



    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
