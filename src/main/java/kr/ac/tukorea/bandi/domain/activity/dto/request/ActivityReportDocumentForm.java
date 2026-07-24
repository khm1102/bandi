package kr.ac.tukorea.bandi.domain.activity.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityReportDocumentForm {

    private String title;
    private String representative;
    private String location;
    private String activityAt;
    private String content;
}
