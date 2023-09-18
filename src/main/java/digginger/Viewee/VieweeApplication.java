package digginger.Viewee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VieweeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VieweeApplication.class, args);
	}

	@Bean
	public String selectedModel(){
		return "gpt-3.5-turbo";
	}

	@Bean
	public String systemRole(){
		return "systemRole";
	}
}