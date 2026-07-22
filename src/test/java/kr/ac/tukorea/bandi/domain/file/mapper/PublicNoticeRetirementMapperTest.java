package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementStatus;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.mapper.PublicNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class PublicNoticeRetirementMapperTest {

    private final PublicNoticeRetirementMapper retirementMapper;
    private final PublicNoticeMapper publicNoticeMapper;
    private final InternalNoticeMapper internalNoticeMapper;
    private final StoredFileMapper storedFileMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;

    private Long actorMemberId;

    @Autowired
    PublicNoticeRetirementMapperTest(PublicNoticeRetirementMapper retirementMapper,
                                    PublicNoticeMapper publicNoticeMapper,
                                    InternalNoticeMapper internalNoticeMapper,
                                    StoredFileMapper storedFileMapper,
                                    TeamMapper teamMapper,
                                    CohortMapper cohortMapper,
                                    MemberMapper memberMapper) {
        this.retirementMapper = retirementMapper;
        this.publicNoticeMapper = publicNoticeMapper;
        this.internalNoticeMapper = internalNoticeMapper;
        this.storedFileMapper = storedFileMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst()
                .orElseThrow()
                .getTeamId();
        Cohort cohort = new Cohort(null, "공시퇴역-26-2", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        Member member = new Member(null, "2026999001", "퇴역 담당자", null, null, null,
                teamId, cohort.getCohortId(), ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        actorMemberId = member.getMemberId();
    }

    @Test
    void 공시_전용_첨부를_삭제_대기_manifest로_스냅샷한다() {
        Long storedFileId = attachPublicNoticeFile("notice/retirement/pending");

        int inserted = retirementMapper.insertMissingManifestEntries();
        List<PublicNoticeRetirementManifest> manifests = retirementMapper.searchAll();

        assertThat(inserted).isEqualTo(1);
        assertThat(manifests).singleElement().satisfies(manifest -> {
            assertThat(manifest.storedFileId()).isEqualTo(storedFileId);
            assertThat(manifest.storageScope()).isEqualTo(StorageScope.PUBLIC);
            assertThat(manifest.retirementStatus()).isEqualTo(PublicNoticeRetirementStatus.PENDING);
            assertThat(manifest.processedDttm()).isNull();
        });
    }

    @Test
    void 내부_공지와_공유된_파일은_보존_상태로_전환한다() {
        Long storedFileId = attachPublicNoticeFile("notice/retirement/shared");
        retirementMapper.insertMissingManifestEntries();
        InternalNotice internalNotice = InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, "내부 공유", "내부 공지 본문", false, actorMemberId);
        internalNoticeMapper.insert(internalNotice);
        internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                internalNotice.getInternalNoticeId(), storedFileId, 0));

        int changed = retirementMapper.markPendingSharedReferences();

        assertThat(changed).isEqualTo(1);
        assertThat(retirementMapper.searchAll()).singleElement().satisfies(manifest -> {
            assertThat(manifest.retirementStatus())
                    .isEqualTo(PublicNoticeRetirementStatus.RETAINED_SHARED);
            assertThat(manifest.processedDttm()).isNotNull();
        });
    }

    private Long attachPublicNoticeFile(String storageKey) {
        PublicNotice notice = PublicNotice.draft("GENERAL", "외부 공시", "공시 본문", false,
                actorMemberId);
        publicNoticeMapper.insert(notice);
        StoredFile file = StoredFile.pending("notice.pdf", StorageScope.PUBLIC, storageKey,
                "application/pdf", 1024L, "a".repeat(64), actorMemberId);
        storedFileMapper.insert(file);
        storedFileMapper.updateReady(file.getStoredFileId(), "etag-retirement");
        publicNoticeMapper.insertAttachment(PublicNoticeAttachment.create(
                notice.getPublicNoticeId(), file.getStoredFileId(), 0));
        return file.getStoredFileId();
    }
}
