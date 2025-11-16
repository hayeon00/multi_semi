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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    @Value("${image.member.image-dir}")
    private String MEMBER_IMAGE_DIR;

    @Value("${image.member.image-url}")
    private String MEMBER_IMAGE_URL;

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
                .orElseThrow(() -> new IllegalArgumentException("해당회원이 존재하지 않습니다"));
        return MemberResDto.fromEntity(member);
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
                .orElseThrow(() -> new IllegalArgumentException("해당회원이 존재하지 않습니다"));
        return MemberResDto.fromEntity(member);
    }

    /** ✅ 로그인한 회원이 작성한 여행계획 전체조회 */
    public List<PlanReqDto> getMyTripPlans(String loginId) {
        List<TripPlan> plans = tripPlanRepository.findAllByMember_LoginId(loginId);

        return plans.stream().map(plan -> {
            PlanReqDto dto = new PlanReqDto();
            dto.setMemberId(loginId);
            dto.setTourSpotId(plan.getTourSpotId());
            dto.setTitle(plan.getTitle());
            dto.setNumberOfPeople(plan.getNumberOfPeople());
            dto.setStartDate(plan.getStartDate());
            dto.setEndDate(plan.getEndDate());
            return dto;
        }).collect(Collectors.toList());
    }

    /** ✅ 회원정보 수정 (이미지 포함) */
    @Transactional
    public void updateMember(String loginId, MemberReqDto dto, MultipartFile file) {
        log.info("========================================");
        log.info("[MemberService] 회원정보 수정 시작");
        log.info("loginId: {}", loginId);
        log.info("전달받은 DTO: username={}, email={}, tel={}",
                dto.getUsername(), dto.getEmail(), dto.getTel());
        log.info("file: {}", file != null ? file.getOriginalFilename() : "없음");
        log.info("========================================");

        // 1. 회원 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.error("회원을 찾을 수 없음: {}", loginId);
                    return new RuntimeException("해당 회원이 존재하지 않습니다.");
                });

        log.info("조회된 회원 ID: {}", member.getId());
        log.info("수정 전 정보: username={}, email={}, tel={}, image={}",
                member.getUsername(), member.getEmail(), member.getTel(), member.getImage());

        String savedFileName = null;

        try {
            // 기존 이미지 (URL 또는 파일명)
            String oldImage = member.getImage();

            // 2. ✅ 기본 정보 업데이트
            if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
                log.info("username 변경: {} -> {}", member.getUsername(), dto.getUsername());
                member.setUsername(dto.getUsername());
            }

            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                log.info("email 변경: {} -> {}", member.getEmail(), dto.getEmail());
                member.setEmail(dto.getEmail());
            }

            if (dto.getTel() != null && !dto.getTel().isEmpty()) {
                log.info("tel 변경: {} -> {}", member.getTel(), dto.getTel());
                member.setTel(dto.getTel());
            }

            // 3. ✅ 새 이미지 업로드가 있을 경우 (관광지 방식과 동일)
            if (file != null && !file.isEmpty()) {
                log.info("새 이미지 업로드 시작: {}", file.getOriginalFilename());

                // 확장자 추출
                String extension = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf("."));

                // 회원 loginId 기반 유니크 파일명 생성
                String uniqueFileName = member.getLoginId() + "_"
                        + UUID.randomUUID().toString().replace("-", "")
                        + extension;

                // ✅ FileUploadUtils 사용 (관광지와 동일)
                savedFileName = FileUploadUtils.saveFile(MEMBER_IMAGE_DIR, uniqueFileName, file);
                log.info("✅ 새 이미지 저장 완료: {}", savedFileName);

                // ✅ 기존 이미지 삭제 (URL → 파일명 변환)
                if (oldImage != null && !oldImage.isEmpty()
                        && !oldImage.equals("default.img")
                        && !oldImage.equals(MEMBER_IMAGE_URL + "default.img")) {

                    // URL에서 파일명만 추출
                    String oldFileName = oldImage.replace(MEMBER_IMAGE_URL, "");
                    FileUploadUtils.deleteFile(MEMBER_IMAGE_DIR, oldFileName);
                    log.info("✅ 기존 이미지 삭제: {}", oldFileName);
                }

                // ✅ DB에는 접근 가능한 URL 형태로 저장 (관광지와 동일)
                member.setImage(MEMBER_IMAGE_URL + savedFileName);
                log.info("✅ DB 이미지 필드 업데이트: {}", member.getImage());

            } else {
                // ✅ 이미지 변경 없음 — 기존 이미지 그대로 유지
                log.info("이미지 변경 없음 - 기존 이미지 유지: {}", oldImage);
                member.setImage(oldImage);
            }

            // 4. 변경 감지로 UPDATE 반영
            memberRepository.save(member);

            log.info("========================================");
            log.info("[MemberService] 회원정보 수정 완료");
            log.info("저장된 정보: username={}, email={}, tel={}, image={}",
                    member.getUsername(), member.getEmail(),
                    member.getTel(), member.getImage());
            log.info("========================================");

        } catch (IOException e) {
            // 실패 시 임시로 저장된 이미지 삭제
            if (savedFileName != null) {
                FileUploadUtils.deleteFile(MEMBER_IMAGE_DIR, savedFileName);
            }
            log.error("[MemberService] 이미지 저장 중 오류 발생", e);
            throw new RuntimeException("회원 이미지 저장 실패", e);
        }
    }
}