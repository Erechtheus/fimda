package de.dfki.lt.fimda.fimda;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest
public class WebLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnDefaultMessage() throws Exception {

        String text = "p.A123T and Val158Met";
        List<String> lines = Files.readAllLines(Paths.get(this.getClass().getResource("/result.json").getFile()), StandardCharsets.UTF_8);
        String expectedResult = String.join("\n", lines);

        this.mockMvc.perform(get("/annotate?text="+text)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(equalToIgnoringWhiteSpace(expectedResult)));
    }
}
