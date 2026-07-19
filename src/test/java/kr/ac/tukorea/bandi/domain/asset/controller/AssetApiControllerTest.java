package kr.ac.tukorea.bandi.domain.asset.controller;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateParam;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.asset.service.AssetService;
import kr.ac.tukorea.bandi.global.config.SecurityWebMvcConfig;
import kr.ac.tukorea.bandi.global.exception.ApiExceptionHandler;
import kr.ac.tukorea.bandi.global.security.LoginMemberArgumentResolver;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class AssetApiControllerTest {

    private static final Long ACTOR_ID = 10L;

    private final MockMvc mockMvc;

    @MockitoBean
    private AssetService assetService;

    @Autowired
    AssetApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 품목을_검색한다() throws Exception {
        given(assetService.searchItems(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/assets")
                        .param("keyword", "케이블")
                        .param("trackingType", "QUANTITY"))
                .andExpect(status().isOk());

        verify(assetService).searchItems(ACTOR_ID,
                new AssetSearchCondition("케이블", null,
                        AssetTrackingType.QUANTITY, null));
    }

    @Test
    void 품목을_등록한다() throws Exception {
        given(assetService.registerItem(any(), any())).willReturn(20L);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "케이블",
                                  "categoryCode": "EQUIPMENT",
                                  "trackingType": "QUANTITY",
                                  "ownerType": "CLUB",
                                  "totalQuantity": 10,
                                  "storageLocation": "창고"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20));

        verify(assetService).registerItem(ACTOR_ID,
                new AssetItemCreateParam("케이블", "EQUIPMENT",
                        AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null,
                        null, 10, "창고", null, null));
    }

    @Test
    void 빈_품목_등록은_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void 사용을_예약하고_반납한다() throws Exception {
        given(assetService.reserve(any(), any())).willReturn(30L);

        mockMvc.perform(post("/api/assets/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetItemId": 20,
                                  "performanceProjectId": 5,
                                  "teamId": 2,
                                  "quantity": 3,
                                  "startDttm": "2027-01-01T10:00:00",
                                  "expectedReturnDttm": "2027-01-02T10:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30));
        mockMvc.perform(post("/api/assets/usages/{assetUsageId}/return",
                        30L))
                .andExpect(status().isNoContent());

        verify(assetService).reserve(ACTOR_ID, new AssetUsageCreateParam(20L,
                null, 5L, 2L, 3,
                LocalDateTime.of(2027, 1, 1, 10, 0),
                LocalDateTime.of(2027, 1, 2, 10, 0), null));
        verify(assetService).returnUsage(ACTOR_ID, 30L);
    }
}
