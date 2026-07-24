package kr.ac.tukorea.bandi.domain.share.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticePublicShareResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourcePublicShareResponse;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ShareController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShareControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ShareController shareController;

    @MockitoBean
    private InternalNoticeService internalNoticeService;
    @MockitoBean
    private ResourceService resourceService;

    @Test
    void 비로그인_공유_페이지는_제목과_로그인_안내만_전달한다() throws Exception {
        given(internalNoticeService.lookupPublicShare("notice-token"))
                .willReturn(new InternalNoticePublicShareResponse(10L, "연습 안내"));

        mockMvc.perform(get("/share/notices/notice-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(view().name("share/landing"))
                .andExpect(model().attribute("shareTitle", "연습 안내"))
                .andExpect(model().attribute("detailPath", "/notices/10"))
                .andExpect(model().attributeDoesNotExist("body", "attachments", "author"));
    }

    @Test
    void 로그인한_사용자는_원래_내부_공지_상세로_이동한다() throws Exception {
        given(internalNoticeService.lookupPublicShare("notice-token"))
                .willReturn(new InternalNoticePublicShareResponse(10L, "연습 안내"));

        String view = shareController.notice("notice-token", authentication(),
                new ExtendedModelMap(), new MockHttpServletResponse());

        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("redirect:/notices/10");
    }

    @Test
    void 로그인한_사용자는_원래_자료_상세로_이동한다() throws Exception {
        given(resourceService.lookupPublicShare("resource-token"))
                .willReturn(new ResourcePublicShareResponse(20L, "연습 자료"));

        String view = shareController.resource("resource-token", authentication(),
                new ExtendedModelMap(), new MockHttpServletResponse());

        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("redirect:/resources/20");
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("member", null, List.of());
    }
}
