package kr.ac.tukorea.bandi.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StyleGuideController.class)
@ActiveProfiles("prod")
class StyleGuideControllerProdProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prod_프로파일에서는_스타일_가이드가_노출되지_않는다() throws Exception {
        mockMvc.perform(get("/style-guide").with(user("tester")))
                .andExpect(status().isNotFound());
    }
}
