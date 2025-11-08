package com.multi.travel.api.service;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */


import com.multi.travel.acc.entity.Acc;
import com.multi.travel.api.dto.TourSpotResDTO;
import com.multi.travel.api.dto.TourSpotResDTO.Item;
import com.multi.travel.api.repository.AccApiRepository;
import com.multi.travel.api.repository.TourSpotApiRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {

    private final RestTemplate restTemplate;
    private final TourSpotApiRepository tourSpotApiRepository;
    private final AccApiRepository accApiRepository;

    @Value("${api.secret}")
    private String secretKey;

    private static final String BASE_URL =
            "https://apis.data.go.kr/B551011/KorService2/areaBasedList2"
                    + "?numOfRows=1000"
                    + "&MobileOS=ETC"
                    + "&MobileApp=test"
                    + "&contentTypeId=12"
                    + "&_type=json";

    private static final int MAX_AREA = 8;
    private static final int[] codes = {11, 26, 27, 28, 29, 30, 31, 46, 36110};

    public void collectAllTourSpots() {
        log.info("===== [ApiService] 관광지 데이터 수집 시작 =====");

        for (int areaCode = 1; areaCode <= MAX_AREA; areaCode++) {
            int page = 1;
            boolean hasNext = true;

            log.info("▶ 지역 코드 {} 데이터 수집 시작", areaCode);

            while (hasNext) {
                String apiUrl = BASE_URL
                        + "&serviceKey=" + secretKey
                        + "&areaCode=" + areaCode
                        + "&pageNo=" + page;

                try {
                    ResponseEntity<TourSpotResDTO> response =
                            restTemplate.getForEntity(apiUrl, TourSpotResDTO.class);

                    TourSpotResDTO resDTO = response.getBody();

                    // 응답 검증
                    if (resDTO == null || resDTO.getResponse() == null
                            || resDTO.getResponse().getBody() == null
                            || resDTO.getResponse().getBody().getItems() == null) {
                        log.warn("⚠️ 응답 데이터 누락: areaCode={}, page={}", areaCode, page);
                        break;
                    }

                    List<Item> items = resDTO.getResponse().getBody().getItems().getItem();

                    if (items == null || items.isEmpty()) {
                        log.info("ℹ️ 더 이상 데이터 없음 (areaCode={}, page={})", areaCode, page);
                        break;
                    }

                    log.info("✅ areaCode={}, page={}, items={}", areaCode, page, items.size());

                    //DB 저장
                    tourSpotApiRepository.saveAll(convertToTourSpot(items));

                    // 페이지 처리
                    int totalCount = resDTO.getResponse().getBody().getTotalCount();
                    int numOfRows = resDTO.getResponse().getBody().getNumOfRows();
                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

                    page++;
                    hasNext = page <= totalPages;

                    // API 서버 보호 (0.3초 간격 요청)
                    Thread.sleep(300);

                } catch (Exception e) {
                    log.error("❌ API 호출 실패: areaCode={}, page={}, error={}",
                            areaCode, page, e.getMessage());
                    hasNext = false;
                }
            }
        }

        log.info("===== [ApiService] 관광지 데이터 수집 완료 =====");
    }

    public void collectAllAccs() {
        log.info("===== [ApiService] 숙소 데이터 수집 시작 =====");
        for (int code : codes){
            int page = 1;
            boolean hasNext = true;

            log.info("▶ 지역 코드 {} 데이터 수집 시작", code);

            while (hasNext) {
                String apiUrl = BASE_URL
                        + "&serviceKey=" + secretKey
                        + "&lDongRegnCd=" + code
                        + "&pageNo=" + page;

                try {
                    ResponseEntity<TourSpotResDTO> response =
                            restTemplate.getForEntity(apiUrl, TourSpotResDTO.class);

                    TourSpotResDTO resDTO = response.getBody();

                    // 응답 검증
                    if (resDTO == null || resDTO.getResponse() == null
                            || resDTO.getResponse().getBody() == null
                            || resDTO.getResponse().getBody().getItems() == null) {
                        log.warn("⚠️ 응답 데이터 누락: code={}, page={}", code, page);
                        break;
                    }

                    List<Item> items = resDTO.getResponse().getBody().getItems().getItem();

                    if (items == null || items.isEmpty()) {
                        log.info("ℹ️ 더 이상 데이터 없음 (code={}, page={})", code, page);
                        break;
                    }

                    log.info("✅ code={}, page={}, items={}", code, page, items.size());

                    //DB 저장
                    accApiRepository.saveAll(convertToAcc(items));

                    // 페이지 처리
                    int totalCount = resDTO.getResponse().getBody().getTotalCount();
                    int numOfRows = resDTO.getResponse().getBody().getNumOfRows();
                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

                    page++;
                    hasNext = page <= totalPages;

                    // API 서버 보호 (0.3초 간격 요청)
                    Thread.sleep(300);

                } catch (Exception e) {
                    log.error("❌ API 호출 실패: areaCode={}, page={}, error={}",
                            code, page, e.getMessage());
                    hasNext = false;
                }
            }
        }
        log.info("===== [ApiService] 숙소 데이터 수집 완료 =====");

    }

    private List<TourSpot> convertToTourSpot(List<Item> items) {
        return items.stream()
                .map(item -> TourSpot.builder()
                        .title(item.getTitle())
                        .address(item.getAddr1())
                        .mapx(new BigDecimal(item.getMapx()))
                        .mapy(new BigDecimal(item.getMapy()))
                        .tel(item.getTel())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .areacode(Integer.valueOf(item.getAreacode()))
                        .recCount(0)
                        .sigungucode(Integer.valueOf(item.getSigungucode()))
                        .lDongRegnCd(item.getLDongRegnCd())
                        .tel(item.getTel())
                        //.category()
                        //.description
                        .status("Y")
                        .build())
                .collect(Collectors.toList());
    }

    private List<Acc> convertToAcc(List<Item> items) {
        return items.stream()
                .map(item -> Acc.builder()
                        .title(item.getTitle())
                        .address(item.getAddr1())
                        .mapx(new BigDecimal(item.getMapx()))
                        .mapy(new BigDecimal(item.getMapy()))
                        .tel(item.getTel())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .areacode(item.getAreacode().isEmpty() ? null : Integer.valueOf(item.getAreacode()))
                        .recCount(0)
                        .lDongRegnCd(item.getLDongRegnCd())
                        .tel(item.getTel())
                        //.category()
                        //.description
                        .status("Y")
                        .build())
                .collect(Collectors.toList());
    }
}