package team.cheese.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import team.cheese.domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class ImgDao {
    @Autowired
    private SqlSession session;
    private static String namespace = "team.cheese.dao.ImgDao.";
    //이미지 테이블 작성
    public int insert(ImgDto img){
        return session.insert(namespace+"insert_img", img);
    }

    //이미지 그룹
    public int insert_group(HashMap map){
        return session.insert(namespace+"insert_group_img", map);
    }

    //그룹 번호를 수동으로 매기기
    public int select_group_max(){
        return session.selectOne(namespace+"select_group_max");
    }

    public ArrayList<ImgDto> select_all_img(){
        List<ImgDto> list = session.selectList(namespace+"select_all_img");
        return new ArrayList<>(list);
    }
    public ArrayList<ImgDto> select_img(int no){
        List<ImgDto> list = session.selectList(namespace+"select_img", no);
        return new ArrayList<>(list);
    }

    public int update(HashMap map){
        return session.update(namespace+"update_img_state", map);
    }


    //테스트 영역
    public ArrayList<ImgDto> select_img2(int no){
        List<ImgDto> list = session.selectList(namespace+"select_img2", no);
        return new ArrayList<>(list);
    }
    public int delete(String tb_name){
        return session.delete(namespace+"delete_table", tb_name);
    }

    public int count(String tb_name){
        return session.selectOne(namespace+"count", tb_name);
    }
    //예외
    // 이벤트 추가
    public int insert_event(EventDto edto){
        return session.insert(namespace+"insert_event", edto);
    }
    // 마이페이지 추가
    public int insert_user(UserInfoDTO udto){
        return session.insert(namespace+"insert_userinfo", udto);
    }
    //커뮤니티 추가
    public int insert_commu(CommunityBoardDto cdto){
        return session.insert(namespace+"insert_commu", cdto);
    }

}
