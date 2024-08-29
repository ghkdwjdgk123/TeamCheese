package team.cheese.dao;

import team.cheese.domain.FaqDto;

import java.util.List;
import java.util.Map;

public interface FaqDao {
    int count();

    // 전체 목록 조회
    List<FaqDto> selectAllFaq();

    // 부분 목록 조회
    List<FaqDto> selectMajorFaq(long que_id); // 변경된 부분

    // 검색어 조회
    List<FaqDto> searchFaqs(Map<String, Object> search);

    // title 클릭시 내용 조회
    String selectContents(long no); // 변경된 부분

    // faq글 삭제(관리자)
    int deleteAdmin(long no); // 변경된 부분

    // faq글 추가(관리자)
    int insertAdmin(FaqDto faq);
}
