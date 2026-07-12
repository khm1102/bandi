package kr.ac.tukorea.bandi.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StyleGuideController.class)
@ActiveProfiles("dev")
class StyleGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 스타일_가이드_페이지가_렌더링된다() throws Exception {
        mockMvc.perform(get("/style-guide").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("styleguide/index"))
                .andExpect(model().attributeExists("styleGuideRequest"));
    }

    @Test
    void 데모_폼_검증_실패_시_오류와_함께_다시_렌더링된다() throws Exception {
        mockMvc.perform(post("/style-guide").with(user("tester")).with(csrf())
                        .param("name", "")
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("styleguide/index"))
                .andExpect(model().attributeHasFieldErrors("styleGuideRequest", "name", "email"));
    }

    @Test
    void 데모_폼_저장_성공_시_리다이렉트하고_플래시_메시지를_남긴다() throws Exception {
        mockMvc.perform(post("/style-guide").with(user("tester")).with(csrf())
                        .param("name", "김반디"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/style-guide"))
                .andExpect(flash().attribute("toast", "저장되었습니다."));
    }
}
