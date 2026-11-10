package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Las pruebas de contexto con base de datos se ejecutan en las clases *IT mediante Testcontainers")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
