package kr.ac.tukorea.bandi.domain.asset.service;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetItemResponse;
import kr.ac.tukorea.bandi.domain.asset.mapper.AssetMapper;
import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.audit.service.AuditService;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetQueryServiceTest {

    @Mock
    private AssetMapper assetMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;
    @Mock
    private AuditService auditService;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetMapper, memberService, fileService,
                auditService, Clock.systemUTC());
        given(memberService.lookupAccessContext(1L)).willReturn(
                new MemberAccessContext(1L, 2L, false, false, true));
    }

    @Test
    void 활성_멤버가_조건에_맞는_품목을_조회한다() {
        AssetSearchCondition condition = new AssetSearchCondition("케이블",
                "EQUIPMENT", AssetTrackingType.QUANTITY,
                AssetStatus.AVAILABLE);
        given(assetMapper.searchItems(condition)).willReturn(List.of(item()));
        given(assetMapper.countItems(condition)).willReturn(1L);

        PageResponse<AssetItemResponse> result = assetService.searchItems(1L,
                condition);

        assertThat(result.items()).extracting(AssetItemResponse::name)
                .containsExactly("케이블");
        verify(assetMapper).searchItems(condition);
    }

    private AssetItem item() {
        return new AssetItem(3L, "케이블", "EQUIPMENT",
                AssetTrackingType.QUANTITY, AssetOwnerType.CLUB, null, null,
                10, "창고", AssetStatus.AVAILABLE, null, null);
    }
}
