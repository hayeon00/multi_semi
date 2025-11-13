package com.multi.travel.member.service;

import com.multi.travel.common.util.FileUploadUtils;
import com.multi.travel.member.dto.MemberReqDto;
import com.multi.travel.member.dto.MemberResDto;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.plan.dto.PlanReqDto;
import com.multi.travel.plan.entity.TripPlan;
import com.multi.travel.plan.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : MemberService
 * @since : 2025. 11. 8. 토요일
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    @Value("${image.member.dir}")
    private String IMAGE_DIR;

    @Value("${image.member.url}")
    private String IMAGE_URL;

    private final MemberRepository memberRepository;

    private final TripPlanRepository tripPlanRepository;


    public List<MemberResDto> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResDto::fromEntity)
                .toList();
    }

    public MemberResDto findOne(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() ->new IllegalArgumentException("해당회원이 존재하지 않습니다"));

        return MemberResDto.fromEntity(member);

    }

    @Transactional
    public Member update(MemberReqDto dto) {

        Member member = memberRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        MultipartFile imageFile = dto.getImageFile();
        String savedFileName = null;

        try {
            String oldImage = member.getImage();

            // 새 이미지 업로드 처리
            if (imageFile != null && !imageFile.isEmpty()) {

                // 확장자 추출 (.png / .jpg 등)
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));

                // 🔹 loginId 기반 유니크 파일명 생성 (공용 폴더에 저장)
                String uniqueFileName = dto.getLoginId() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

                savedFileName = FileUploadUtils.saveFile(IMAGE_DIR, uniqueFileName, imageFile);

                // 🔹 기존 이미지 삭제
                if (oldImage != null && !oldImage.isEmpty()) {
                    FileUploadUtils.deleteFile(IMAGE_DIR, oldImage);
                    log.info("[updateMember] 기존 이미지 삭제: {}", oldImage);
                }

                // 🔹 새 파일명 DB 반영
                member.updateInfo(dto.getUsername(), dto.getEmail(), dto.getTel(), savedFileName);

            } else {
                // 이미지 변경 안 함
                member.updateInfo(dto.getUsername(), dto.getEmail(), dto.getTel(), oldImage);
            }

        } catch (IOException e) {
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(IMAGE_DIR, savedFileName);
            }
            throw new RuntimeException("회원 프로필 이미지 저장 실패", e);
        }

        log.info("[updateMember] 프로필 업데이트 완료: {} / {}", member.getLoginId(), member.getImage());
        return member;
    }


    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.setStatus("N");
        memberRepository.save(member);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.setStatus(status);
        memberRepository.save(member);
    }



    public MemberResDto findByLoginId(String loginIdFromToken) {

        Member member = memberRepository.findByLoginId(loginIdFromToken)
                .orElseThrow(() ->new IllegalArgumentException("해당회원이 존재하지 않습니다"));

        return MemberResDto.fromEntity(member);
    }


    /** ✅ 로그인한 회원이 작성한 여행계획 전체조회 */
    public List<PlanReqDto> getMyTripPlans(String loginId) {
        List<TripPlan> plans = tripPlanRepository.findAllByMemberLoginIdOrderByIdDesc(loginId);

        return plans.stream().map(plan -> {
            PlanReqDto dto = new PlanReqDto();
            dto.setMemberId(loginId);
            // TourSpot ID 대신 출발지와 관련된 정보 없음 → null 또는 0
            dto.setTourSpotId(null);
            dto.setTitle(plan.getTitle());
            dto.setNumberOfPeople(plan.getNumberOfPeople());
            dto.setStartDate(plan.getStartDate());
            dto.setEndDate(plan.getEndDate());
            return dto;
        }).collect(Collectors.toList());
    }


}
