package com.dongpop.urin.domain.inquiry.service;


import com.dongpop.urin.domain.inquiry.dto.request.InquiryDataDto;
import com.dongpop.urin.domain.inquiry.dto.response.InquiryDetailDto;
import com.dongpop.urin.domain.inquiry.dto.response.InquiryDto;
import com.dongpop.urin.domain.inquiry.dto.response.InquiryListDto;
import com.dongpop.urin.domain.inquiry.repository.Inquiry;
import com.dongpop.urin.domain.inquiry.repository.InquiryRepository;
import com.dongpop.urin.domain.member.repository.Member;
import com.dongpop.urin.domain.member.repository.MemberRepository;
import com.dongpop.urin.domain.study.repository.Study;
import com.dongpop.urin.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class InquiryService {

    private static final String DELETE_MESSAGE = "삭제된 댓글입니다.";

    private final InquiryRepository inquiryRepository;
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public InquiryListDto getStudyInquiries(int studyId, Pageable pageable) {
        Page<Inquiry> inquiryPage = inquiryRepository.findAllByStudyIdAndParentIsNull(studyId, pageable);

        int totalPages = inquiryPage.getTotalPages();
        List<InquiryDto> inquiryList = new ArrayList<>();

        inquiryPage.toList().forEach(i -> {
            InquiryDetailDto parent = InquiryDetailDto.builder()
                    .inquiryId(i.getId())
                    .contents(i.isDeleted() ? DELETE_MESSAGE : i.getContents())
                    .writerId(i.getWriter().getId()) //TODO: 삭제된 댓글도 사용자 아이디는 보여주는지 다시 확인
                    .writer(i.getWriter().getNickname())
                    //TODO: 문자열 변환 TEST
                    .createdAt(i.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
                    .isDeleted(i.isDeleted()).build();

            List<InquiryDetailDto> children = i.getChildren().stream()
                    .map(c ->
                            InquiryDetailDto.builder()
                                    .inquiryId(c.getId())
                                    .contents(c.isDeleted() ? DELETE_MESSAGE : c.getContents())
                                    .writerId(c.getWriter().getId())
                                    .writer(c.getWriter().getNickname())
                                    .createdAt(c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
                                    .isDeleted(c.isDeleted()).build()
                    ).collect(Collectors.toList());

            inquiryList.add(new InquiryDto(parent, children));
        });

        return new InquiryListDto(totalPages, inquiryList);
    }

    @Transactional
    public void writeInquiry(int writerId, int studyId, InquiryDataDto inquiryDataDto) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("해당 스터디가 존재하지 않습니다."));
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));

        Inquiry newInquiry = Inquiry.builder()
                .contents(inquiryDataDto.getContents())
                .writer(writer)
                .study(study).build();

        Inquiry parent = inquiryRepository.findById(inquiryDataDto.getParent())
                .orElseGet(() -> null);
        newInquiry.addParentInquiry(parent);

        inquiryRepository.save(newInquiry);
    }

    @Transactional
    public void updateFeed(int memberId, int studyId, int inquiryId, String contents) {
        Inquiry inquiry = checkWriterAuthorizaion(memberId, inquiryId);
        inquiry.updateInquiryData(contents);
    }

    @Transactional
    public void deleteInquiry(int memberId, int studyId, int inquiryId) {
        Inquiry inquiry = checkWriterAuthorizaion(memberId, inquiryId);
        inquiry.deleteInquiry();
    }
    private Inquiry checkWriterAuthorizaion(int memberId, int inquiryId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 회원이 존재하지 않습니다."));
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NoSuchElementException("해당 질문이 존재하지 않습니다."));
        if (member.getId() != inquiry.getWriter().getId()) {
            //TODO: 401에러 던지기
            throw new RuntimeException("권한이 없습니다.");
        }
        return inquiry;
    }
}