package com.dongpop.urin.domain.inquiry.dto.response;

import com.dongpop.urin.domain.inquiry.entity.Inquiry;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Builder
@Data
public class InquiryDetailDto {
    private static final String DELETE_MESSAGE = "삭제된 댓글입니다.";

    private int inquiryId;
    private String contents;
    private String createdAt;
    private Boolean isDeleted;

    private int writerId;
    private String writer;

    public static InquiryDetailDto toDto(Inquiry inquiry) {
        return InquiryDetailDto.builder()
                .inquiryId(inquiry.getId())
                .contents(inquiry.isDeleted() ? DELETE_MESSAGE : inquiry.getContents())
                .writerId(inquiry.getWriter().getId())
                .writer(inquiry.getWriter().getNickname())
                .createdAt(inquiry.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .isDeleted(inquiry.isDeleted()).build();
    }
}
