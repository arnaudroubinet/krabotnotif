package arn.roub.hook;

import arn.roub.hook.quartz.QuartzConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(QuartzConfiguration.class)
@SpringBootApplication
public class HookApplication {

    public static void main(String[] args) {
        SpringApplication.run(HookApplication.class, args);
    }
}
