package com.example.final_project.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 200 : 요청 성공
     */
    SUCCESS(true, HttpStatus.OK.value(), "요청에 성공하였습니다."),


    /**
     * 400 : User Request, Response 오류
     */
    USERS_EMPTY_USERID(false, HttpStatus.BAD_REQUEST.value(), "회원 ID를 입력해주세요."),
    USERS_EMPTY_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "이메일을 입력해주세요."),
    USERS_EMPTY_NAME(false, HttpStatus.BAD_REQUEST.value(), "이름을 입력해주세요."),
    ALL_EMPTY_CONDITION(false, HttpStatus.BAD_REQUEST.value(), "최소 하나의 검색 조건을 입력해주세요."),
    EMPTY_PAGE_NUM(false, HttpStatus.BAD_REQUEST.value(), "현재 페이지를 입력해주세요."),
    USERS_EMPTY_PASSWORD(false, HttpStatus.BAD_REQUEST.value(), "패스워드를 입력해주세요."),
    USERS_EMPTY_ADDRESS(false, HttpStatus.BAD_REQUEST.value(), "주소를 입력해주세요."),
    TEST_EMPTY_COMMENT(false, HttpStatus.BAD_REQUEST.value(), "코멘트를 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_USERID(false,HttpStatus.BAD_REQUEST.value(),"중복된 ID입니다."),
    POST_USERS_EXISTS_EMAIL(false,HttpStatus.BAD_REQUEST.value(),"중복된 이메일입니다."),
    POST_USERS_EXISTS_KAKAO(false,HttpStatus.BAD_REQUEST.value(),"카카오 회원으로 중복된 이메일입니다."),
    POST_USERS_EXISTS_NAVER(false,HttpStatus.BAD_REQUEST.value(),"네이버 회원으로 중복된 이메일입니다."),
    POST_USERS_EXISTS_FACEBOOK(false,HttpStatus.BAD_REQUEST.value(),"페이스북 회원으로 중복된 이메일입니다."),
    POST_TEST_EXISTS_MEMO(false,HttpStatus.BAD_REQUEST.value(),"중복된 메모입니다."),

    RESPONSE_ERROR(false, HttpStatus.NOT_FOUND.value(), "값을 불러오는데 실패하였습니다."),
    INVALID_OAUTH_TYPE(false, HttpStatus.BAD_REQUEST.value(), "알 수 없는 소셜 로그인 형식입니다."),
    INVALID_OAUTH_ACTION_TYPE(false, HttpStatus.BAD_REQUEST.value(), "알 수 없는 소셜 동작 형식입니다."),
    INVALID_STATE(false, HttpStatus.BAD_REQUEST.value(), "유효하지 않은 state 값입니다."),
    DUPLICATED_EMAIL(false, HttpStatus.BAD_REQUEST.value(), "중복된 이메일입니다."),
    INVALID_MEMO(false,HttpStatus.NOT_FOUND.value(), "존재하지 않는 메모입니다."),
    FAILED_TO_LOGIN(false,HttpStatus.NOT_FOUND.value(),"없는 아이디거나 비밀번호가 틀렸습니다."),
    INVALID_USER_JWT(false,HttpStatus.FORBIDDEN.value(),"권한이 없는 유저의 접근입니다."),
    NOT_FIND_USER(false,HttpStatus.NOT_FOUND.value(),"일치하는 유저가 없습니다."),
    NOT_FIND_LOGIN_SESSION(false,HttpStatus.NOT_FOUND.value(),"로그인 상태가 아닙니다."),
    EXPIRED_USER(false, HttpStatus.BAD_REQUEST.value(), "비활성화된 사용자입니다. 개인정보 동의를 해주세요."),
    BLOCKED_USER(false, HttpStatus.BAD_REQUEST.value(), "차단된 사용자입니다."),
    DELETE_USER(false, HttpStatus.BAD_REQUEST.value(), "탈퇴한 사용자입니다."),
    DENY_USER(false, HttpStatus.BAD_REQUEST.value(), "작성자가 아닌 사용자입니다."),
    EXPIRED_REFRESHTOKEN(false, HttpStatus.BAD_REQUEST.value(), "refresh 토큰이 만료되었습니다."),
    EXPIRED_JWT(false, HttpStatus.BAD_REQUEST.value(), "access 토큰이 만료되었습니다."),
    MALFORMEDJWT(false, HttpStatus.BAD_REQUEST.value(), "토큰 값이 undefined 입니다."),
    /**
     * 400 : Funding Request, Response 오류
     */
    NOT_FIND_FUNDING(false,HttpStatus.NOT_FOUND.value(),"일치하는 펀딩 게시글이 없습니다."),
    NOT_FIND_FUNDING_CART(false,HttpStatus.NOT_FOUND.value(),"해당 장바구니가 없습니다."),
    NOT_FIND_FUNDING_CART_ITEM(false,HttpStatus.NOT_FOUND.value(),"해당 장바구니 아이템이 없습니다."),
    NOT_FIND_FUNDING_PARTICIPANTS(false,HttpStatus.NOT_FOUND.value(),"일치하는 펀딩 참여자가 없습니다."),
    NOT_FIND_FUNDING_CATEGORY(false,HttpStatus.NOT_FOUND.value(),"일치하는 펀딩 카테고리가 없습니다."),
    FUNDING_EMPTY_TITLE(false, HttpStatus.BAD_REQUEST.value(), "제목을 입력해주세요."),
    FUNDING_EMPTY_PARTICIPANT(false, HttpStatus.BAD_REQUEST.value(), "인원을 입력해주세요."),
    FUNDING_EMPTY_AMOUNT(false, HttpStatus.BAD_REQUEST.value(), "펀딩 금액을 입력해주세요."),
    FUNDING_EMPTY_BLOB(false, HttpStatus.BAD_REQUEST.value(), "썸네일 혹은 게시글 내용을 입력해주세요."),
    EMPTY_PAYMENT_USER(false, HttpStatus.BAD_REQUEST.value(), "결제 정보가 등록되지 않았습니다."),
    EMPTY_PAYMENT_BANK(false, HttpStatus.BAD_REQUEST.value(), "결제 계좌의 은행 등록되지 않았습니다."),
    EMPTY_PAYMENT_BANK_ACCOUNT(false, HttpStatus.BAD_REQUEST.value(), "결제 계좌 번호가 등록되지 않았습니다."),
    EXISTS_PAYMENT_USER(false,HttpStatus.BAD_REQUEST.value(),"이미 결제 정보가 등록되었습니다."),
    EXIST_CART_ITEM(false,HttpStatus.BAD_REQUEST.value(),"이미 장바구니에 추가된 펀딩입니다."),
    PARTICIPANTS_USER(false,HttpStatus.BAD_REQUEST.value(),"이미 펀딩에 참여하였습니다."),
    PARTICIPANTS_COMPLETE_USER(false,HttpStatus.BAD_REQUEST.value(),"이미 참여되어 달성 성공한 펀딩입니다."),
    PARTICIPANTS_REFUND_REQUEST_USER(false,HttpStatus.BAD_REQUEST.value(),"이미 참여되어 환불 진행 중인 펀딩입니다."),
    PARTICIPANTS_REFUND_COMPLETE_USER(false,HttpStatus.BAD_REQUEST.value(),"환불이 완료된 펀딩입니다."),

    /**
     * 400 : StudentRequest Request, Response 오류
     */
    NOT_FIND_STUDENTREQUEST(false,HttpStatus.NOT_FOUND.value(),"게시글이 존재하지 않습니다."),
    NOT_FIND_STUDENTREQUEST_COMMENT(false,HttpStatus.NOT_FOUND.value(),"해당 댓글이 존재하지 않습니다."),
    NOT_FIND_STUDENTREQUEST_CATEGORY(false,HttpStatus.NOT_FOUND.value(), "일치하는 카테고리가 없습니다."),
    STUDENTREQUEST_EMPTY_TITLE(false,HttpStatus.BAD_REQUEST.value(), "제목을 입력해주세요"),
    STUDENTREQUEST_EMPTY_CATEGORY(false,HttpStatus.BAD_REQUEST.value(), "카테고리를 입력해주세요."),
    STUDENTREQUEST_EMPTY_BLOB(false,HttpStatus.BAD_REQUEST.value(), "게시글의 내용을 입력해주세요."),
    STUDENTREQUESTCOMMENT_EMPTY_CONTENT(false, HttpStatus.BAD_REQUEST.value(), "댓글의 내용을 입력해주세요."),
    ACCESS_DENY_USER(false, HttpStatus.BAD_REQUEST.value(), "작성자가 아니므로 접근 불가합니다."),

    /**
     * 400 : City Request, Response 오류
     */
    NOT_FIND_CITY(false,HttpStatus.NOT_FOUND.value(),"일치하는 도시를 찾을 수 없습니다."),



    /**
     * 500 :  Database, Server 오류
     */
    DATABASE_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버와의 연결에 실패하였습니다."),
    PASSWORD_ENCRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "비밀번호 복호화에 실패하였습니다."),


    MODIFY_FAIL_USERNAME(false,HttpStatus.INTERNAL_SERVER_ERROR.value(),"유저네임 수정 실패"),
    DELETE_FAIL_USERNAME(false,HttpStatus.INTERNAL_SERVER_ERROR.value(),"유저 삭제 실패"),
    MODIFY_FAIL_MEMO(false,HttpStatus.INTERNAL_SERVER_ERROR.value(),"메모 수정 실패"),
    UNEXPECTED_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "예상치 못한 에러가 발생했습니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}