package com.multi.travel.recommend.service;

/*
 * Please explain the class!!!
 *
 * @filename    : RecService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 11. 화요일
 */


import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.auth.dto.CustomUser;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.course.entity.Course;
import com.multi.travel.course.repository.CourseRepository;
import com.multi.travel.member.entity.Member;
import com.multi.travel.member.repository.MemberRepository;
import com.multi.travel.recommend.dto.RecDTO;
import com.multi.travel.recommend.entity.Recommend;
import com.multi.travel.recommend.repository.RecRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecService {
    private final RecRepository recRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final TspRepository tspRepository;
    private final AccRepository accRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public RecDTO toggleRecommend(CustomUser customUser, Long targetId, String catCode) {
        if (customUser == null) {
            throw new AccessDeniedException("로그인이 필요한 기능입니다.");
        }
        Member member = memberRepository.findByLoginId(customUser.getUserId()).orElseThrow(() -> new EntityNotFoundException("Member not Found."));

        Optional<Recommend> existing =
                recRepository.findByMember_IdAndTargetIdAndCategory_CatCode(member.getId(), targetId, catCode);

        // 공통 targetEntity 조회 (TourSpot, Acc, Course)
        Object target = getTargetEntity(catCode, targetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Target Entity not found, userId :" + member.getId() + ", targetId : " + targetId + ", cat_code : " + catCode));

        if (existing.isPresent()) {
            Recommend rec = existing.get();
            rec.toggleStatus();

            if (rec.getStatus().equals("Y")) {
                RecCountUpdate(target, 1);
            } else {
                RecCountUpdate(target, -1);
            }

            return RecEntityToDTO(rec, targetId, member.getId(), catCode, rec.getStatus());
        } else {
            Recommend newRec = Recommend.builder()
                    .targetId(targetId)
                    .member(memberRepository.findById(member.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Member not found")))
                    .category(categoryRepository.findById(catCode)
                            .orElseThrow(() -> new EntityNotFoundException("Category not found")))
                    .status("Y")
                    .build();

            recRepository.save(newRec);

            RecCountUpdate(target, 1);

            return RecEntityToDTO(newRec, newRec.getTargetId(), member.getId(), catCode, "Y");
        }
    }

    private static RecDTO RecEntityToDTO(Recommend rec, Long targetId, Long userId, String catCode, String rec1) {
        return RecDTO.builder()
                .id(rec.getId())
                .targetId(targetId)
                .userId(userId)
                .catCode(catCode)
                .status(rec1)
                .build();
    }

    private Optional<?> getTargetEntity(String catCode, Long targetId) {
        return switch (catCode) {
            case "tsp" -> tspRepository.findById(targetId);
            case "acc" -> accRepository.findById(targetId);
            case "crs" -> courseRepository.findById(targetId);
            default -> Optional.empty();
        };
    }

    private void RecCountUpdate(Object target, int amount) {
        if (target instanceof TourSpot tsp) {
            tsp.setRecCount(tsp.getRecCount() + amount);
        } else if (target instanceof Acc acc) {
            acc.setRecCount(acc.getRecCount() + amount);
        } else if (target instanceof Course crs) {
            crs.setRecCount(crs.getRecCount() + amount);
        }
    }



}
