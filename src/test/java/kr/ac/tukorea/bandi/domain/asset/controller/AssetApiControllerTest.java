package kr.ac.tukorea.bandi.domain.asset.controller;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitUpdateParam;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    void 품목과_개별_장비를_수정한다() throws Exception {
        mockMvc.perform(put("/api/assets/{assetItemId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "전원 케이블",
                                  "categoryCode": "CABLE",
                                  "ownerType": "CLUB",
                                  "totalQuantity": 12,
                                  "storageLocation": "창고 B"
                                }
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(patch("/api/assets/{assetItemId}/status", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REPAIR\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/assets/units/{assetUnitId}", 30L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "REPAIR",
                                  "storageLocation": "수리 업체"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(assetService).updateItem(ACTOR_ID, 20L,
                new AssetItemUpdateParam("전원 케이블", "CABLE",
                        AssetOwnerType.CLUB, null, null, 12,
                        "창고 B", null, null));
        verify(assetService).changeItemStatus(ACTOR_ID, 20L,
                AssetStatus.REPAIR, null);
        verify(assetService).updateUnit(ACTOR_ID,
                new AssetUnitUpdateParam(30L, AssetStatus.REPAIR,
                        "수리 업체", null));
    }

    @Test
    void 품목_사진은_presigned_url로_리다이렉트한다() throws Exception {
        given(assetService.createPhotoDownloadUrl(ACTOR_ID, 20L))
                .willReturn("http://localhost:9000/bandi-private/photo");

        mockMvc.perform(get("/api/assets/{assetItemId}/photo/download", 20L))
                .andExpect(status().isFound());
    }
}
