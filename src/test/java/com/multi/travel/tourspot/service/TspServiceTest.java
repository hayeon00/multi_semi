package com.multi.travel.tourspot.service;

import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
/*
 * Please explain the class!!!
 *
 * @filename    : TspServiceTest
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 월요일
 */

@SpringBootTest(classes = {
        TspService.class, TspRepository.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "image.add-resource-locations=file:/tmp/",
        "image.add-resource-handler=/img/**"
})
class TspServiceTest {

    @MockitoBean
    private TspRepository tspRepository;

    @Autowired
    private TspService tspService;

    @Test
    void getTourSpotList() {
        // given
        List<TourSpot> mockList = Arrays.asList(
                TourSpot.builder()
                        .id(1L)
                        .address("서울시 중구")
                        .title("남산타워")
                        .build(),
                TourSpot.builder()
                        .id(2L)
                        .address("부산 해운대구")
                        .title("해운대 해수욕장")
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        Page<TourSpot> mockPage = new PageImpl<>(mockList, pageable, mockList.size());

        // when
        when(tspRepository.findAll(pageable)).thenReturn(mockPage);

        Page<TourSpot> result = tspRepository.findAll(pageable);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals("남산타워", result.getContent().get(0).getTitle());
        System.out.println(result.getContent().get(0));
    }


    @Test
    void getTourSpotDetail() {
        //given
        TourSpot mock = TourSpot.builder()
                .id(1L)
                .address("테스트 주소")
                .address("1")
                .mapx(new BigDecimal(111.2111111))
                .mapy(new BigDecimal(37.2111111))
                .title("타이틀")
                .build();

        //when
        when(tspRepository.findByIdAndStatus(1L, "Y")).thenReturn(Optional.ofNullable(mock));
        Optional<TourSpot> tsp = tspRepository.findByIdAndStatus(1L, "Y");
        //then
        //assertThat()
    }
}