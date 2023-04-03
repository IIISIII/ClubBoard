package edu.sns.clubboard.port

interface ServerInterface
{
    fun login(id: String, password: String) : Boolean //

    fun createAccount() // 계정 생성

    fun sendAuthorizedMail(email: String) : Boolean // 이메일 보내기 성공: true, 실패: false

    fun matchAuthorizedNumber(number: Int) : Boolean // 인증번호 검증 성공: true, 실패: false

    fun getClubBoardList(max: Int) : List<String> // 동아리 게시판 목록 가져오기 max: 가져올 목록 수

    fun getPostList(max: Int) : List<String> // 게시물 목록 가져오기

    fun uploadPost() : Boolean // 게시물 업로드

    fun updatePost() : Boolean // 게시물 수정

    fun deletePost() : Boolean // 게시물 삭제

    fun joinClubBoard() // 동아리 게시판 접속 승인 요청

    fun acceptRequest() // 요청 승인

    fun declineRequest() // 요청 거절

    fun havePermission() : Boolean // 권한 확인
}