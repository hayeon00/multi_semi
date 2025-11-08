package com.multi.travel.api.dto;

/*
 * Please explain the class!!!
 *
 * @filename    : TourSpotResDTO
 * @author      : Choi MinHyeok
 * @since       : 25. 11. 8. 토요일
 */


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class TourSpotResDTO {
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
        private String addr1;
        //private String addr2;
        private String areacode;
        //private String cat1;
        //private String cat2;
        //private String cat3;
        private String contentid;
        private String contenttypeid;
        private String createdtime;
        private String firstimage;
        private String firstimage2;
        //private String cpyrhtDivCd;
        private String mapx;
        private String mapy;
        //private String mlevel;
        private String modifiedtime;
        private String sigungucode;
        private String tel;
        private String title;
        //private String zipcode;
        @JsonProperty("lDongRegnCd")
        private String lDongRegnCd;
        private String lDongSignguCd;
        //private String lclsSystm1;
        //private String lclsSystm2;
        //private String lclsSystm3;
    }
}