package choreography.deadline.common;

import choreography.deadline.ExchangeApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = { ExchangeApplication.class })
public class CucumberSpingConfiguration {}
