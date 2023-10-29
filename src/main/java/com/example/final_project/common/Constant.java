package com.example.final_project.common;

public class Constant {
    public enum SocialLoginType{
        LOCAL, GOOGLE, KAKAO, NAVER, FACEBOOK
    }

    public enum SocialActionType{
        LOGIN,
        LOGOUT
    }

    //권한 관리 없음으로 모든 인증된 spring security 유저 권한은 USER로 통일
    public enum RoleType{
        USER
    }

    public enum TargetType{
        FUNDING, STUDENT_REQUEST
    }

    //Default State:: ACTIVE
    public enum State {
        /*Default State:: ACTIVE*/
        ACTIVE
    }

    public enum UserState {
        ACTIVE, DELETE;
    }

    public enum FundingState {
        ACTIVE, IN_PROGRESS, DELETE, COMPLETE, FAIL;
    }

    public enum FundingCommentState  {
        ACTIVE, DELETE;
    }

    public enum FundingParticipantsState {
        ACTIVE, COMPLETE, REFUND_NEEDED, REFUND_COMPLETE;
    }

    public enum StudentRequestState {
        ACTIVE, INACTIVE, DELETE;
    }

    public enum StudentRequestCommentState {
        ACTIVE, DELETE;
    }

        public enum FundingMessageState {
        ACTIVE, SEND, CHECKED;
    }


}