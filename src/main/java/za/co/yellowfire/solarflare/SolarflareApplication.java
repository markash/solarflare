package za.co.yellowfire.solarflare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import za.co.yellowfire.solarflare.model.Persist;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan(basePackageClasses = {SolarflareApplication.class})
public class SolarflareApplication extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext ctx = SpringApplication.run(SolarflareApplication.class, args);
		
		Persist persist = ctx.getBean(Persist.class);
		System.out.println(persist.listDbEntries());
		
		persist.create(5);
		System.out.println(persist.listDbEntries());
	}
}
