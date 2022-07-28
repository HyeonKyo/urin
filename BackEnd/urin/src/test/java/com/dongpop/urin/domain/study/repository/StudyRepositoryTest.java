package com.dongpop.urin.domain.study.repository;

import com.dongpop.urin.domain.member.repository.Member;
import com.dongpop.urin.domain.member.repository.MemberRepository;
import com.dongpop.urin.domain.participant.repository.Participant;
import com.dongpop.urin.domain.participant.repository.ParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class StudyRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ParticipantRepository participantRepository;

    @Test
    void 스터디_리스트_테스트() throws Exception {

        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Study curStudy = Study.builder()
                    .title("제목" + i)
                    .notice("내용~~~" + i)
                    .isOnair(false)
                    .memberCapacity(6)
                    .status(StudyStatus.RECRUITING)
                    .build();
            if (i == 0 || i == 1) {
                curStudy.updateStatus(StudyStatus.TERMINATED);
            }
            studies.add(curStudy);
            studyRepository.save(curStudy);
        }

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Member curMember = Member.builder()
                    .nickname("이름" + i)
                    .password("pass" + i)
                    .role("user")
                    .build();
            members.add(curMember);
            memberRepository.save(curMember);
        }

        for (int i = 0; i < 20; i++) {
            Study curS = studies.get(i);
            for (int j = 0; j < 5; j++) {
                Member curM = members.get(i * 5 + j);
                boolean isLeader = j == 0 ? true : false;
                Participant participant = Participant.makeParticipant(curM, curS, isLeader);
                participantRepository.save(participant);
            }
        }

        PageRequest pageRequest = PageRequest.of(0, 9);
        Page<Study> studyPage = studyRepository.findAllStudy(pageRequest);

        assertThat(studyPage.getTotalElements()).isEqualTo(18); //종료된 스터디는 가져오지 않음
        assertThat(studyPage.toList().size()).isEqualTo(9);
        assertThat(studyPage.toList().get(0).getTitle()).isEqualTo("제목2");
    }

    @Test
    void 스터디_리스트_검색_테스트() {
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Study curStudy = Study.builder()
                    .title(i % 2 == 0 ? "검색 가능" + i : "검색 불가" + i)
                    .notice("내용~~~" + i)
                    .isOnair(false)
                    .memberCapacity(10)
                    .status(StudyStatus.RECRUITING)
                    .build();
            if (i == 0) {
                curStudy.updateStatus(StudyStatus.TERMINATED);
            }
            studies.add(curStudy);
            studyRepository.save(curStudy);
        }

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Member curMember = Member.builder()
                    .nickname("이름" + i)
                    .password("pass" + i)
                    .role("user")
                    .build();
            members.add(curMember);
            memberRepository.save(curMember);
        }

        for (int i = 0; i < 20; i++) {
            Study curS = studies.get(i);
            for (int j = 0; j < 5; j++) {
                Member curM = members.get(i * 5 + j);
                boolean isLeader = j == 0 ? true : false;
                Participant participant = Participant.makeParticipant(curM, curS, isLeader);
                participantRepository.save(participant);
            }
        }

        System.out.println("\n===============================\n");
        List<Study> all = studyRepository.findAll();
        for (Study s : all) {
            System.out.println("title = " + s.getTitle());
        }
        System.out.println("\n===============================\n");

        PageRequest pageRequest = PageRequest.of(0, 4);
        Page<Study> pageList = studyRepository.findSearchAllStudy("가능", pageRequest);

        assertThat(pageList.getTotalElements()).isEqualTo(9);
        assertThat(pageList.getTotalPages()).isEqualTo(3);
        assertThat(pageList.toList().size()).isEqualTo(4);
        assertThat(pageList.toList().get(0).getTitle()).isEqualTo("검색 가능2");
        assertThat(pageList.toList().get(1).getTitle()).isEqualTo("검색 가능4");
    }

    @Test
    void 스터디_상세_테스트() {
        Study study = Study.builder()
                .title("스터디")
                .notice("공지")
                .memberCapacity(6)
                .isOnair(false)
                .build();
        Integer studyId = studyRepository.save(study).getId();
        em.flush();
        em.clear();

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Member curMember = Member.builder()
                    .nickname("이름" + i)
                    .password("pass" + i)
                    .role("user")
                    .build();
            members.add(curMember);
            memberRepository.save(curMember);
        }

        for (int j = 0; j < 5; j++) {
            Member curM = members.get(j);
            boolean isLeader = j == 0 ? true : false;
            Participant participant = Participant.makeParticipant(curM, study, isLeader);
            participantRepository.save(participant);
        }

        Study findStudy = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException());

        assertThat(findStudy.getTitle()).isEqualTo("스터디");
        assertThat(findStudy.getNotice()).isEqualTo("공지");
        assertThat(findStudy.getMemberCapacity()).isEqualTo(6);
        assertThat(findStudy.getParticipants().size()).isEqualTo(5);
    }
}