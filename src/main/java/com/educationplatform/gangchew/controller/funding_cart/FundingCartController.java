package com.educationplatform.gangchew.controller.funding_cart;

import com.educationplatform.gangchew.common.exceptions.BaseException;
import com.educationplatform.gangchew.common.response.BaseResponse;
import com.educationplatform.gangchew.entity.funding_cart.FundingCart;
import com.educationplatform.gangchew.entity.funding_cart.FundingCartItem;
import com.educationplatform.gangchew.model.funding_cart.FundingCartRes;
import com.educationplatform.gangchew.service.funding.FundingService;
import com.educationplatform.gangchew.service.funding_cart.FundingCartService;
import com.educationplatform.gangchew.entity.funding.*;
import com.educationplatform.gangchew.model.funding.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.educationplatform.gangchew.common.response.BaseResponseStatus.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class FundingCartController {
    @Autowired
    private FundingCartService fundingCartService;


    /**
     * 로그인한 유저의 장바구니 목록 조회 API
     * [GET] /fundingcart/all
     * @param request HttpServletRequest
     * @return BaseResponse<List<FundingCartRes>>
     */
    @ResponseBody
    @GetMapping("/fundingcart/all")
    public BaseResponse<List<FundingCartRes>> getItemsInUserCart(HttpServletRequest request) {
        List<FundingCartRes> fundingCartItemList = fundingCartService.getItemsInUserCart(request);
        return new BaseResponse<>(fundingCartItemList);
    }


    /**
     * 로그인한 유저의 장바구니에 아이템 추가 API
     * [Get] /fundingcart/add
     * @param request HttpServletRequest
     * @param funding_id 펀딩 고유 ID
     * @return BaseResponse<List<FundingCartItem>>
     */
    @ResponseBody
    @GetMapping("/fundingcart/add")
    public BaseResponse<String> addItemToUserCart(HttpServletRequest request, @RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingCartService.addItemToUserCart(request, funding_id));
    }

    /**
     * 유저의 장바구니 아이템 수량 변경 API
     * [Get] /fundingcart/{plus or minus}
     * @param request HttpServletRequest
     * @param funding_id 펀딩 고유 ID
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/fundingcart/{action}")
    public BaseResponse<String> updateCartItemQuantity(HttpServletRequest request,
                                                   @PathVariable(name = "action") String action,
                                                   @RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingCartService.updateCartItemQuantity(request, action, funding_id));
    }

    /**
     * 유저의 장바구니 모든 아이템 삭제 API
     * [Get] /fundingcart/delete/all
     * @param request HttpServletRequest
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/fundingcart/delete/all")
    public BaseResponse<String> deleteAllItemsInUserCart(HttpServletRequest request) {
        return new BaseResponse<>(fundingCartService.deleteAllItemsInUserCart(request));
    }

    /**
     * 유저의 장바구니 특정 아이템 삭제 API
     * [Get] /fundingcart/delete
     * @param request HttpServletRequest
     * @param funding_id 펀딩 고유 ID
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/fundingcart/delete")
    public BaseResponse<String> deleteItemFromUserCart(HttpServletRequest request,
                                               @RequestParam(name = "funding") Long funding_id) {
        return new BaseResponse<>(fundingCartService.deleteItemInUserCart(request, funding_id));
    }
}