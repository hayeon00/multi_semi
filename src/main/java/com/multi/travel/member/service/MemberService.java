package com.multi.travel.member.service;

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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    @Value("${image.member.dir}")
    private String MEMBER_IMAGE_DIR;

    @Value("${image.member.url}")
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
        List<TripPlan> plans = tripPlanRepository.findAllByMemberLoginIdOrderByIdDesc(loginId);

        return plans.stream().map(plan -> {
            PlanReqDto dto = new PlanReqDto();
            dto.setMemberId(loginId);
            dto.setTourSpotId(null);
            dto.setTitle(plan.getTitle());
            dto.setNumberOfPeople(plan.getNumberOfPeople());
            dto.setStartDate(plan.getStartDate());
            dto.setEndDate(plan.getEndDate());
            return dto;
        }).collect(Collectors.toList());
    }

    /** ✅ 회원정보 수정 (이미지 포함) */
    @Transactional
    public void updateMember(String loginId, MemberReqDto dto) {
        log.info("========================================");
        log.info("[MemberService] 회원정보 수정 시작");
        log.info("loginId: {}", loginId);
        log.info("전달받은 DTO: username={}, email={}, tel={}",
                dto.getUsername(), dto.getEmail(), dto.getTel());
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
            // 2. 기본 정보 업데이트
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

            // 3. 이미지 파일 처리
            MultipartFile imageFile = dto.getImageFile();

            if (imageFile != null && !imageFile.isEmpty()) {
                log.info("새 이미지 업로드 시작: {}", imageFile.getOriginalFilename());

                // 기존 이미지 파일명
                String oldImage = member.getImage();

                // 확장자 추출
                String extension = imageFile.getOriginalFilename()
                        .substring(imageFile.getOriginalFilename().lastIndexOf("."));

                // 회원명 기반 유니크 파일명 생성
                String uniqueFileName = dto.getUsername().replaceAll("\\s+", "_")
                        + "_" + System.currentTimeMillis()
                        + extension;

                // 디렉토리가 없으면 생성
                File dir = new File(MEMBER_IMAGE_DIR);  // ✅ 수정!
                if (!dir.exists()) {
                    boolean created = dir.mkdirs();
                    log.info("디렉토리 생성: {}, 성공: {}", MEMBER_IMAGE_DIR, created);
                }

                // 새 파일 저장
                String filePath = MEMBER_IMAGE_DIR + uniqueFileName;  // ✅ 수정!
                imageFile.transferTo(new File(filePath));
                savedFileName = uniqueFileName;

                log.info("새 이미지 저장 완료: {}", savedFileName);

                // 기존 이미지 삭제 (기본 이미지가 아닌 경우에만)
                if (oldImage != null && !oldImage.isEmpty()
                        && !oldImage.equals("default.img")) {

                    File oldFile = new File(MEMBER_IMAGE_DIR + oldImage);  // ✅ 수정!
                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        log.info("기존 이미지 삭제: {}, 성공: {}", oldImage, deleted);
                    }
                }

                // DB에 파일명만 저장
                member.setImage(savedFileName);
                log.info("DB 이미지 필드 업데이트: {}", savedFileName);

            } else {
                log.info("이미지 변경 없음 - 기존 이미지 유지: {}", member.getImage());
            }

            // 4. 명시적 저장
            Member savedMember = memberRepository.saveAndFlush(member);

            log.info("========================================");
            log.info("[MemberService] 회원정보 수정 완료");
            log.info("저장된 정보: username={}, email={}, tel={}, image={}",
                    savedMember.getUsername(), savedMember.getEmail(),
                    savedMember.getTel(), savedMember.getImage());
            log.info("========================================");

        } catch (IOException e) {
            // 실패 시 임시로 저장된 이미지 삭제
            if (savedFileName != null) {
                File tempFile = new File(MEMBER_IMAGE_DIR + savedFileName);  // ✅ 수정!
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            log.error("[MemberService] 이미지 저장 중 오류 발생", e);
            throw new RuntimeException("회원 이미지 저장 실패", e);
        }
    }
}