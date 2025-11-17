package com.multi.travel.api.service;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotService
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */


import com.multi.travel.acc.entity.Acc;
import com.multi.travel.acc.repository.AccRepository;
import com.multi.travel.api.dto.DetailResDTO;
import com.multi.travel.api.dto.TourSpotResDTO;
import com.multi.travel.api.dto.TourSpotResDTO.Item;
import com.multi.travel.api.repository.AccApiRepository;
import com.multi.travel.api.repository.TourSpotApiRepository;
import com.multi.travel.category.CategoryRepository;
import com.multi.travel.tourspot.entity.TourSpot;
import com.multi.travel.tourspot.repository.TspRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiService {

    private static final String BASE_URL_TSP =
            "https://apis.data.go.kr/B551011/KorService2/areaBasedList2"
                    + "?numOfRows=1000"
                    + "&MobileOS=ETC"
                    + "&MobileApp=test"
                    + "&_type=json";

    private static final String BASE_URL_ACC =
            "https://apis.data.go.kr/B551011/KorService2/searchStay2"
                    + "?numOfRows=1000"
                    + "&MobileOS=ETC"
                    + "&MobileApp=test"
                    + "&_type=json";

    private static final String BASE_URL_DETAIL =
            "https://apis.data.go.kr/B551011/KorService2/detailCommon2"
                    + "?&MobileOS=ETC"
                    + "&MobileApp=test"
                    + "&_type=json";

    private static final int MAX_AREA = 45;
    private static final int[] codes = {
            11, 26, 27, 28, 29, 30, 31, 36_110, 41, 43, 44, 46, 47, 48, 50, 51, 52
    };
    private final RestTemplate restTemplate;
    private final TourSpotApiRepository tourSpotApiRepository;
    private final AccApiRepository accApiRepository;
    private final CategoryRepository categoryRepository;
    private final TspRepository tspRepository;
    private final AccRepository accRepository;

    @Value("${api.secret}")
    private String secretKey;

    public void collectAllTourSpots(int type) {
        log.info("===== [ApiService] 관광지 데이터 수집 시작 =====");

        for (int areaCode = 1; areaCode <= MAX_AREA; areaCode++) {
            int page = 1;
            boolean hasNext = true;

            log.info("▶ 지역 코드 {} 데이터 수집 시작", areaCode);

            while (hasNext) {
                String apiUrl = BASE_URL_TSP
                        + "&serviceKey=" + secretKey
                        + "&areaCode=" + areaCode
                        + "&pageNo=" + page
                        + "&contentTypeId=" + type;

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
        for (int code : codes) {
            int page = 1;
            boolean hasNext = true;

            log.info("▶ 지역 코드 {} 데이터 수집 시작", code);

            while (hasNext) {
                String apiUrl = BASE_URL_ACC
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
                    log.error("❌ API 호출 실패: areaCode={}, page={}, error={}", code, page, e.getMessage());
                    hasNext = false;
                }
            }
        }
        log.info("===== [ApiService] 숙소 데이터 수집 완료 =====");

    }

    private List<TourSpot> convertToTourSpot(List<Item> items) {
        return items.stream()
                .filter(this::isValidItem)
                .map(item -> TourSpot.builder()
                        .title(item.getTitle())
                        .address(item.getAddr1())
                        .mapx(new BigDecimal(item.getMapx()))
                        .mapy(new BigDecimal(item.getMapy()))
                        .tel(item.getTel())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .areacode(Integer.valueOf(item.getAreacode()))
                        .recCount(new Random().nextInt(100))
                        .sigungucode(Integer.valueOf(item.getSigungucode()))
                        .lDongRegnCd(item.getLDongRegnCd())
                        .contentId(Integer.valueOf(item.getContentid()))
                        .status("Y")
                        .category(categoryRepository.findById("tsp").orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

    private List<Acc> convertToAcc(List<Item> items) {
        return items.stream()
                .filter(this::isValidItem)
                .map(item -> Acc.builder()
                        .title(item.getTitle())
                        .address(item.getAddr1())
                        .mapx(new BigDecimal(item.getMapx()))
                        .mapy(new BigDecimal(item.getMapy()))
                        .tel(item.getTel())
                        .firstImage(item.getFirstimage())
                        .firstImage2(item.getFirstimage2())
                        .areacode(Integer.valueOf(item.getAreacode()))
                        .recCount(new Random().nextInt(100))
                        .sigungucode(Integer.valueOf(item.getSigungucode()))
                        .lDongRegnCd(item.getLDongRegnCd())
                        .contentId(Integer.valueOf(item.getContentid()))
                        .status("Y")
                        .category(categoryRepository.findById("acc").orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

    // ✅ 검증 조건 정의 (비어 있으면 false)
    private boolean isValidItem(Item item) {
        return isNotEmpty(item.getMapx()) &&
                isNotEmpty(item.getMapy()) &&
                isNotEmpty(item.getAreacode()) &&
                isNotEmpty(item.getSigungucode()) &&
                isNotEmpty(item.getContentid());
    }

    // ✅ 공백 체크
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }


    @Transactional
    public void insertDetail(Integer contentId, String type) {
        log.info("🚀 상세정보 조회 TargetId={}, TargetType={}", contentId, type);

        if (contentId == null) {
            log.warn("⚠️ contentId가 null입니다. type={}", type);
            return;
        }

        String apiUrl = BASE_URL_DETAIL
                + "&serviceKey=" + secretKey
                + "&contentId=" + contentId;

        try {
            ResponseEntity<DetailResDTO> response =
                    restTemplate.getForEntity(apiUrl, DetailResDTO.class);
            DetailResDTO resDTO = response.getBody();

            // 1️⃣ 응답 기본 구조 검증
            if (resDTO == null || resDTO.getResponse() == null || resDTO.getResponse().getHeader() == null) {
                throw new IllegalStateException("API 응답 구조가 올바르지 않습니다.");
            }

            String resultCode = resDTO.getResponse().getHeader().getResultCode();
            String resultMsg = resDTO.getResponse().getHeader().getResultMsg();

            // 2️⃣ API 키 만료 또는 인증 오류 감지
            if (!"0000".equals(resultCode)) {
                if (resultMsg != null &&
                        (resultMsg.contains("SERVICE") || resultMsg.contains("KEY") || resultMsg.contains("UNAUTHORIZED"))) {
                    throw new IllegalStateException("❌ API Key가 만료되었거나 유효하지 않습니다. (" + resultMsg + ")");
                }
                log.warn("⚠️ API 호출 실패 (code={}, msg={})", resultCode, resultMsg);
                return;
            }

            // 3️⃣ 정상 응답 데이터 검증
            DetailResDTO.Body body = resDTO.getResponse().getBody();
            if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
                log.warn("⚠️ 응답 데이터 누락: type={}, contentId={}", type, contentId);
                return;
            }

            List<DetailResDTO.Item> items = body.getItems().getItem();
            if (items.isEmpty() || items.get(0).getOverview() == null) {
                log.info("ℹ️ overview 데이터 없음 (type={}, contentId={})", type, contentId);
                return;
            }

            String overview = items.get(0).getOverview();
            String homepage = cleanHomepage(items.get(0).getHomepage());

            log.info("✅ [{}] contentId={} homepage={} overview={}", type, contentId, homepage, overview.substring(0, Math.min(40, overview.length())));

            // 4️⃣ DB 업데이트
            if ("tsp".equalsIgnoreCase(type)) {
                Optional<TourSpot> tsp = tspRepository.findByContentId(contentId);
                if (tsp.isPresent()) {
                    tsp.get().setDescription(overview);
                    tsp.get().setHomepage(homepage);
                    tspRepository.save(tsp.get());
                } else {
                    log.warn("⚠️ 해당 contentId={}의 TourSpot을 찾을 수 없습니다.", contentId);
                }
            } else if ("acc".equalsIgnoreCase(type)) {
                Optional<Acc> acc = accRepository.findByContentId(contentId);
                if (acc.isPresent()) {
                    acc.get().setDescription(overview);
                    acc.get().setHomepage(homepage);
                    accRepository.save(acc.get());
                } else {
                    log.warn("⚠️ 해당 contentId={}의 Acc를 찾을 수 없습니다.", contentId);
                }
            } else {
                log.warn("⚠️ 지원되지 않는 타입: {}", type);
            }

        } catch (IllegalStateException e) {
            // API 키 만료 등의 심각한 오류
            log.error("🚨 심각 오류: {}", e.getMessage());
            throw e; // 예외 다시 던져 상위 로직에서 처리 가능하도록
        } catch (Exception e) {
            log.error("❌ 상세정보 수집 실패: type={}, contentId={}, error={}", type, contentId, e.getMessage());
        }

        log.info("🏁 상세정보 수집 완료");
    }

    public static String cleanHomepage(String homepage) {
        if (homepage == null || homepage.isBlank()) return null;

        // href 속성의 값을 추출
        Pattern pattern = Pattern.compile("href\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(homepage);

        if (matcher.find()) {
            return matcher.group(1); // https://kansonghouse.kr/
        }

        // href가 없으면 단순히 HTML 태그 제거
        return homepage.replaceAll("<[^>]*>", "").trim();
    }

}