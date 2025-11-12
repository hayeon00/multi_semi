package com.multi.travel.api.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : DetailResDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 10. 토요일
 */


import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class DetailResDTO {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    public static class Items {
        private List<Item> item;
    }

    @Data
    @ToString
    public static class Item {
        private String homepage;
        private String overview; // 관광지 설명
    }
}
