package com.multi.travel.acc.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpot
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */


import com.multi.travel.acc.dto.AccDTO;
import com.multi.travel.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_acc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Acc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 아이디 (PK)

    @Column(name = "address", length = 100)
    private String address;   // 주소

    @Column(name = "title", length = 100)
    private String title;     // 제목

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;   // 설명

    @Column(name = "homepage", columnDefinition = "TEXT")
    private String homepage;

    @Column(name = "tel", length = 100)
    private String tel;       // 전화번호

    @Column(name = "mapx", precision = 13, scale = 10)
    private BigDecimal mapx;  // 경도

    @Column(name = "mapy", precision = 13, scale = 10)
    private BigDecimal mapy;  // 위도

    @Column(name = "areacode")
    private Integer areacode;  // 지역코드

    @Column(name = "sigungucode")
    private Integer sigungucode; // 시군구코드

    @Column
    private String lDongRegnCd;

    @Column(name = "first_image", length = 100)
    private String firstImage;  // 1번 이미지

    @Column(name = "second_image", length = 100)
    private String firstImage2; // 2번 이미지

    @CreationTimestamp
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;  // 생성일자

    @UpdateTimestamp
    @Column(name = "modified_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modifiedAt; // 수정일자

    @Column(name = "status", columnDefinition = "CHAR(1) DEFAULT 'Y' CHECK (status IN ('Y','N'))")
    private String status;  // 활성화 상태

    @Column(name = "rec_count", columnDefinition = "INT DEFAULT 0")
    private Integer recCount;  // 추천 개수

    @Column(name = "content_id", columnDefinition = "INT DEFAULT 0")
    private Integer contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_code")
    private Category category;

    @Transient
    private Double distanceKm;

    public void updateInfo(AccDTO accDTO) {

        if (StringUtils.hasText(accDTO.getAddress())) this.address = accDTO.getAddress();
        if (StringUtils.hasText(accDTO.getTitle())) this.title = accDTO.getTitle();
        if (StringUtils.hasText(accDTO.getDescription())) this.description = accDTO.getDescription();
        if (StringUtils.hasText(accDTO.getTel())) this.tel = accDTO.getTel();
        if (StringUtils.hasText(accDTO.getHomepage())) this.homepage = accDTO.getHomepage();
        if (StringUtils.hasText(accDTO.getLDongRegnCd())) this.lDongRegnCd = accDTO.getLDongRegnCd();
        if (StringUtils.hasText(accDTO.getStatus())) this.status = accDTO.getStatus();

        // 숫자는 null 체크만 필요
        if (accDTO.getMapx() != null) this.mapx = accDTO.getMapx();
        if (accDTO.getMapy() != null) this.mapy = accDTO.getMapy();
        if (accDTO.getAreacode() != null) this.areacode = accDTO.getAreacode();
        if (accDTO.getSigungucode() != null) this.sigungucode = accDTO.getSigungucode();
        if (accDTO.getContentId() != null) this.contentId = accDTO.getContentId();
    }

    public void changeStatus() {
        if (this.status == null || this.status.equals("Y")) {
            this.status = "N";
        } else {
            this.status = "Y";
        }
    }

    public void changeCategory(Category category) {
        this.category = category;
    }

    public void updateImage(String imageURL) {
        this.firstImage = imageURL;
    }
}
