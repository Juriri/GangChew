package com.example.final_project;

import com.example.final_project.src.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequiredArgsConstructor
public class ViewController {
    @Autowired
    private UserRepository userRepository;
    /**
     * 메인 홈화면
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String toHomePage() {
        return "main";
    }
    @RequestMapping(value = "/rank", method = RequestMethod.GET)
    public String toMainPage() {
        return "rank";
    }

    @RequestMapping(value = "/introduce", method = RequestMethod.GET)
    public String toMain3Page() {
        return "introduce";
    }
/*
    @RequestMapping(value = "/home", method = RequestMethod.POST)
    public String toHomePage_p() {
        return "main";
    }
*/

    /**
     * 로그인 화면
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String toLoginPage() {
        return "user/login";
    }


    /**
     * 소셜 로그인 콜백 요청 화면
     */
/*    @RequestMapping(value = "/oauth2/{socialLoginType}/callback", method = RequestMethod.GET)
    public String toCallbackPage(@PathVariable(name = "socialLoginType") String socialLoginPath,
                                 @RequestParam(name = "code") String code) {
        return "user/callback";
    }*/

    /**
     * 회원가입 화면
     */
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String toSignupPage() {
        return "user/signup";
    }


    /**
     * 마이페이지 화면
     */
    @RequestMapping(value = "/mypage", method = RequestMethod.GET)
    public String toMyPage() {
        return "user/mypage";
    }


    /**
     * 펀딩 리스트 화면
     */
    @RequestMapping(value = "/funding", method = RequestMethod.GET)
    public String toFundingPage() {
        return "funding/fundingList";
    }


    /**
     * 펀딩글 작성 화면
     */
    @RequestMapping(value = "/fundingCreate", method = RequestMethod.GET)
    public String toFundingInsertPage() {
        return "funding/fundingCreate";
    }


    /**
     * 펀딩글 상세 화면
     */
    @RequestMapping(value = "/fundingDetail", method = RequestMethod.GET)
    public String toFundingDetailPage() {
        return "funding/fundingDetail";
    }

    /**
     * 펀딩글 수정 화면
     */
    @RequestMapping(value = "/fundingEdit", method = RequestMethod.GET)
    public String toFundingEditPage() {
        return "funding/fundingEdit";
    }



    /**
     * 장바구니 화면
     */
    @RequestMapping(value = "/mycart", method = RequestMethod.GET)
    public String toMyCartPage() {return "fundingcart/myCart";}

    /**
     * 강의요청 리스트 화면
     */
    @RequestMapping(value = "/studentrequest", method = RequestMethod.GET)
    public String toStudentListPage() {return "fundingrequest/requestList";}

    /**
     * 강의요청 작성 화면
     */
    @RequestMapping(value = "/studentrequest/create", method = RequestMethod.GET)
    public String toStudentCreatePage() {return "fundingrequest/requestCreate";}

    /**
     * 강의요청글 상세 화면
     */
    @RequestMapping(value = "/studentrequestDetail", method = RequestMethod.GET)
    public String toStudentRequestDetailPage() {
        return "fundingrequest/requestDetail";
    }

    /**
     * 강의요청글 수정 화면
     */
    
    @RequestMapping(value = "/studentRequestEdit", method = RequestMethod.GET)
    public String toStudentRequestEditPage() {
        return "fundingrequest/requestEdit";
    }

    /**
     * 결제 정보 입력 화면
     */

    @RequestMapping(value = "/paymentCreate", method = RequestMethod.GET)
    public String topaymentCreatePage() {
        return "funding/fundingPaymentCreate";
    }


    /**
     * ajax 모듈 호출
     */
    @RequestMapping(value = "/js/common-ajax.js", method = RequestMethod.GET)
    public String tocommonAjax() {
        return "../js/common-ajax.js";
    }


    /**
     * 마이페이지 정보 화면
     */
    @RequestMapping(value = "/myfunding", method = RequestMethod.GET)
    public String toMyfundingPage() {
        return "user/myfunding";
    }
}
