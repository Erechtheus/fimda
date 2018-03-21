package de.dfki.lt.fimda.fimda;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnnotateRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void annotateShouldReturnDefaultMessage() throws Exception {

        String text = "p.A123T and Val158Met";
        List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("/result.json").getFile()), StandardCharsets.UTF_8);
        String expectedResult = String.join("\n", lines);

        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/annotate?text="+text, String.class)).isEqualToIgnoringWhitespace(expectedResult);
    }
}