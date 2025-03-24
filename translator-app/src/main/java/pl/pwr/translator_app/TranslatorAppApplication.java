package pl.pwr.translator_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.pwr.parser.QueryTranslator;

@SpringBootApplication
public class TranslatorAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TranslatorAppApplication.class, args);
	}

}
