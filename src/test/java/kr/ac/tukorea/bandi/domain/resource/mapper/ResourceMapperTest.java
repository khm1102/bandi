package kr.ac.tukorea.bandi.domain.resource.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class ResourceMapperTest {

    private static final String SHARE_TOKEN =
            "A0a1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0";

    private final ResourceMapper resourceMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;

    private Long memberId;

    @Autowired
    ResourceMapperTest(ResourceMapper resourceMapper, TeamMapper teamMapper,
                       CohortMapper cohortMapper, MemberMapper memberMapper) {
        this.resourceMapper = resourceMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
    }

    @BeforeEach
    void setUp() {
        Team team = teamMapper.searchAll().get(0);
        Cohort cohort = new Cohort(null, "26-자료공유", true);
        cohortMapper.insert(cohort);
        Member member = new Member(null, "2026000100", "자료작성자", null, null, null,
                team.getTeamId(), cohort.getCohortId(), ClubRole.MEMBER,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        memberId = member.getMemberId();
    }

    @Test
    void 삭제되지_않은_자료의_공유_토큰을_조회하고_중단할_수_있다() {
        Resource resource = Resource.create("공유 자료", "본문", memberId);
        resourceMapper.insert(resource);

        resourceMapper.updateShareToken(resource.getResourceId(), SHARE_TOKEN);

        assertThat(resourceMapper.lookupShareTokenForUpdate(resource.getResourceId()))
                .contains(SHARE_TOKEN);
        assertThat(resourceMapper.lookupPublicShare(SHARE_TOKEN))
                .isPresent()
                .get()
                .extracting("title")
                .isEqualTo("공유 자료");
        assertThat(resourceMapper.lookupDetail(resource.getResourceId()))
                .isPresent()
                .get()
                .extracting("createdByMemberId")
                .isEqualTo(memberId);

        resourceMapper.updateShareToken(resource.getResourceId(), null);

        assertThat(resourceMapper.lookupPublicShare(SHARE_TOKEN)).isEmpty();
    }
}
