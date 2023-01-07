package com.dongpop.urin.domain.feed.dto.response;

import com.dongpop.urin.domain.feed.entity.Feed;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.persistence.GeneratedValue;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
@Data
public class FeedDetailDto {
    private static final String DELETE_MESSAGE = "삭제된 댓글입니다.";

    private int feedId;
    private String contents;
    private String createdAt;
    private Boolean isDeleted;

    private int writerId;
    private String writer;

    public static FeedDetailDto toDto(Feed feed) {
        return FeedDetailDto.builder()
                .feedId(feed.getId())
                .contents(feed.isDeleted() ? DELETE_MESSAGE : feed.getContents())
                .writerId(feed.getWriter().getId())
                .writer(feed.getWriter().getNickname())
                .createdAt(feed.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .isDeleted(feed.isDeleted()).build();
    }
}
