package team.cheese.service;

import team.cheese.domain.QnaCategoryDto;
import team.cheese.domain.QnaDto;

import java.util.List;
import java.util.Map;

public interface QnaService {
    // 대분류 카테고리 조회
    List<QnaCategoryDto> getMajorCategories() throws Exception;

    // 중분류 카테고리 조회
    List<QnaCategoryDto> getSubCategories(long majorCategoryId)throws Exception;

    // 1:1 문의내역을 등록
    int write(QnaDto qnaDto)throws Exception;

    // 나의 문의 내역 목록 조회
    List<QnaDto> select(String ur_id)throws Exception;

    // 나의 문의 내역 읽기
    QnaDto read(long no)throws Exception;

    // 나의 문의 내역 삭제
    int remove(long no, String ur_id)throws Exception;

    // 나의 문의 내역 수정
    int modify(QnaDto qnaDto)throws Exception;

    // 나의 문의 내역 페이징
    List<QnaDto> selectPageByUserId(Map<String, Object> params)throws Exception;

    // 나의 문의 내역 전체 카운트
    int countQnasByUserId(String ur_id)throws Exception;
}
