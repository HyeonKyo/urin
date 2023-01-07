package com.dongpop.urin.domain.study.dto.response;

import com.dongpop.urin.domain.study.entity.Study;
import com.dongpop.urin.domain.study.entity.StudyStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class StudySummaryDto {
    private int id;
    private String title;
    private int memberCapacity;
    private int currentMember;
    private StudyStatus status;
    private String hashtagCodes;
    private List<String> hashtagNameList;

    public static StudySummaryDto toDto(Study study, List<String> hashtagNameList, String hashtagCodes) {
        return StudySummaryDto.builder()
                .id(study.getId())
                .memberCapacity(study.getMemberCapacity())
                .title(study.getTitle())
                .currentMember(study.getCurrentParticipantCount())
                .status(study.getStatus())
                .hashtagCodes(hashtagCodes)
                .hashtagNameList(hashtagNameList)
                .build();
    }
}
