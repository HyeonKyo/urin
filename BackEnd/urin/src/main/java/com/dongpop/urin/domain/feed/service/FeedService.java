package com.dongpop.urin.domain.feed.service;


import com.dongpop.urin.domain.feed.dto.request.FeedDataDto;
import com.dongpop.urin.domain.feed.dto.response.FeedDetailDto;
import com.dongpop.urin.domain.feed.dto.response.FeedDto;
import com.dongpop.urin.domain.feed.dto.response.FeedListDto;
import com.dongpop.urin.domain.feed.entity.Feed;
import com.dongpop.urin.domain.feed.repository.FeedRepository;
import com.dongpop.urin.domain.member.entity.Member;
import com.dongpop.urin.domain.participant.entity.Participant;
import com.dongpop.urin.domain.study.entity.Study;
import com.dongpop.urin.domain.study.repository.StudyRepository;
import com.dongpop.urin.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dongpop.urin.global.error.errorcode.CommonErrorCode.DO_NOT_HAVE_AUTHORIZATION;
import static com.dongpop.urin.global.error.errorcode.CommonErrorCode.NO_SUCH_ELEMENTS;
import static com.dongpop.urin.global.error.errorcode.FeedErrorCode.*;
import static com.dongpop.urin.global.error.errorcode.StudyErrorCode.STUDY_DOES_NOT_EXIST;

@RequiredArgsConstructor
@Service
public class FeedService {
    private final FeedRepository feedRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public FeedListDto getStudyFeeds(int studyId, Pageable pageable, Member member) {
        Study study = getStudy(studyId);
        checkRegisteredMember(study, member);

        Page<Feed> feedPage = feedRepository.findAllByStudyIdAndParentIsNull(studyId, pageable);

        int totalPages = feedPage.getTotalPages();
        List<FeedDto> feedList = new ArrayList<>();

        feedPage.toList().forEach(f -> {
            FeedDetailDto parent = FeedDetailDto.toDto(f);
            List<FeedDetailDto> children = f.getChildren().stream()
                    .map(FeedDetailDto::toDto)
                    .collect(Collectors.toList());
            feedList.add(new FeedDto(parent, children));
        });

        return new FeedListDto(totalPages, feedList);
    }

    @Transactional
    public void writeFeed(Member writer, int studyId, FeedDataDto feedDataDto) {
        Study study = getStudy(studyId);
        checkRegisteredMember(study, writer);

        Feed newFeed = Feed.builder()
                .contents(feedDataDto.getContents())
                .writer(writer)
                .study(study).build();

        if (feedDataDto.isChildFeed()) {
            Feed parent = feedRepository.findById(feedDataDto.getParent())
                    .orElseThrow(() -> new CustomException(PARENT_FEED_IS_NOT_EXIST));
            checkSameStudy(studyId, study);
            newFeed.addParentFeed(parent);
        }
        feedRepository.save(newFeed);
    }

    @Transactional
    public void updateFeed(Member member, int studyId, int feedId, String contents) {
        Feed feed = checkUpdateAuthorization(member, studyId, feedId);
        feed.updateFeedData(contents);
    }

    @Transactional
    public void deleteFeed(Member member, int studyId, int feedId) {
        Feed feed = checkDeleteAuthorization(member, studyId, feedId);
        feed.deleteFeed();
    }

    private Study getStudy(int studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(STUDY_DOES_NOT_EXIST));
    }

    private void checkRegisteredMember(Study study, Member writer) {
        for (Participant participant : study.getParticipants()) {
            if (participant.getMember().getId() == writer.getId() && !participant.getWithdrawal()) {
                return;
            }
        }
        throw new CustomException(NOT_REGISTED_MEMBER);
    }

    private Feed checkUpdateAuthorization(Member member, int studyId, int feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomException(NO_SUCH_ELEMENTS));
        Study study = getStudy(studyId);
        checkSameStudy(studyId, feed.getStudy());

        if (member.getId() != feed.getWriter().getId()) {
            throw new CustomException(DO_NOT_HAVE_AUTHORIZATION);
        }
        return feed;
    }

    private Feed checkDeleteAuthorization(Member member, int studyId, int feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new CustomException(NO_SUCH_ELEMENTS));
        Study study = getStudy(studyId);
        checkSameStudy(studyId, feed.getStudy());
        if (!(member.getId() == feed.getWriter().getId() || member.getId() == study.getStudyLeader().getId())) {
            throw new CustomException(DO_NOT_HAVE_AUTHORIZATION);
        }
        return feed;
    }

    private void checkSameStudy(int studyId, Study study) {
        if (!study.getId().equals(studyId)) {
            throw new CustomException(DIFFRENT_STUDY);
        }
    }
}
