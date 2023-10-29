package com.example.final_project.src.funding_message;

import com.example.final_project.common.response.BaseResponse;
import com.example.final_project.src.funding.FundingService;
import com.example.final_project.src.funding_cart.model.FundingCartRes;
import com.example.final_project.src.funding_message.model.FundingMessageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class FundingMessageController {
    @Autowired
    private FundingService fundingService;
    @Autowired
    private FundingMessageService fundingMessageService;

    /**
     * 로그인한 유저의 취소된 펀딩으로 환불이 진행 중일  확인하지않은 메세지 모델 리스트 조회 API
     * [GET] /fundingmessage/all
     * @param request HttpServletRequest
     * @return BaseResponse<List<FundingMessageRes>>
     */
    @ResponseBody
    @GetMapping("/fundingmessage/all")
    public BaseResponse<List<FundingMessageRes>> getAllMessageByUser(HttpServletRequest request, @RequestParam(name = "state", required = false) String current_message_state) {
        // 참여자 상태가 "REFUND_NEEDED" 이나 아직 메시지 상태가 "SEND" 인 리스트 반환 ("CHECKED" 가 아니면 확인 안한 상태)
        List<FundingMessageRes> fundingMessageResList = fundingMessageService.getMessageAll(request, "REFUND_NEEDED", "SEND");
        return new BaseResponse<>(fundingMessageResList);
    }

    /**
     * 로그인한 유저의 취소된 펀딩의 확인하지않은 메세지 모델 카운트 조회 API
     * [GET] /fundingmessage/count
     * @param request HttpServletRequest
     * @return BaseResponse<Integer>
     */
    @ResponseBody
    @GetMapping("/fundingmessage/count")
    public BaseResponse<Integer> getCountMessageByUser(HttpServletRequest request) {
        // 참여자 상태가 "REFUND_NEEDED" 이나 아직 메시지 상태가 "SEND" 인 리스트 반환 ("CHECKED" 가 아니면 확인 안한 상태)
        return new BaseResponse<>(fundingMessageService.getCountMessage(request, "REFUND_NEEDED", "SEND"));
    }


    /**
     * 로그인한 유저의 쪽지 상태 변경 API
     * [GET] /fundingmessage/update
     * @param request HttpServletRequest
     * @param funding_id 게시글 고유 id
     * @param forward_state 변경할 상태값
     * @return BaseResponse<Integer>
     */
    @ResponseBody
    @Operation(summary = "jwt 인증 필요", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fundingmessage/update")
    public BaseResponse<String> getCountMessageByUser(HttpServletRequest request, Long funding_id, String forward_state) {

        /*Funding_Message (쪽지) 테이블의 상태(state):
        ACTIVE: 활성 상태
        SEND: 펀딩 취소 알림 발송 완료 상태
        CHECKED: 취소 알림을 수신자가 확인한 상태*/

        return new BaseResponse<>(fundingMessageService.updateMessageState(request, funding_id, forward_state));

    }
}
