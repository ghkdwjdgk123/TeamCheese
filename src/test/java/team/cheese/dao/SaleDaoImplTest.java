package team.cheese.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

import team.cheese.dao.SaleDao;
import team.cheese.domain.SaleDto;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/**/root-context.xml"})
public class SaleDaoImplTest {
    @Autowired
    SaleDao saledao;

    @Test
    public void count() throws Exception {
        // sale테이블에 게시글이 몇개 들어있는지 확인하는 테스트
        System.out.println("count : " + saledao.count());
        int cnt = saledao.count();
        assertTrue(cnt == 13);
    }

    @Test
    public void testSelectAll() throws Exception {
        // 전체 글을 selectALL해왔을 때 첫번째 게시글이 david234가 작성했는지 확인하는 테스트
        System.out.println("selectAll : " + saledao.selectAll());
        List<SaleDto> list = saledao.selectAll();
        String seller_id = list.get(list.size()-1).getSeller_id();
        System.out.println(seller_id);
        assertTrue(seller_id.equals("david234"));
    }

    @Test
    public void testSelect() throws Exception {
        // 글 하나를 선택했을 때 게시글 번호와 판매자가 일치하는지 확인하는 테스트
        System.out.println("select : " + saledao.select(1));
        SaleDto saleDto = saledao.select(1);
        String seller_id = saleDto.getSeller_id();
        System.out.println(seller_id);
        assertTrue(seller_id.equals("david234"));
    }

    @Test
    public void testInsert() throws Exception {
        // 글 작성하기 테스트
        SaleDto saledto = new SaleDto();
        saledto.setSeller_id("asdf");
        saledto.setSal_i_cd("016001005");
        saledto.setPro_s_cd("C");
        saledto.setTx_s_cd("S");
        // 거래방법 1개만 작성
        saledto.setTrade_s_cd_1("F");
//        saledto.setTrade_s_cd_2('F');
        saledto.setPrice(28000);
        saledto.setSal_s_cd("S");
        saledto.setTitle("자바의 정석 팔아요");
        saledto.setContents("자바의 정석 2판 팔아요.");
        saledto.setBid_cd("N");
        saledto.setPickup_addr_cd("11060710");
        saledto.setDetail_addr("회기역 1번출구 앞(20시 이후만 가능)");
        saledto.setBrand("자바의 정석");
        saledto.setReg_price(30000);
        saledto.setCheck_addr_cd(0);

        System.out.println(saledto);

        int no = saledao.insert(saledto);
        System.out.println("확인 : " + saledto.getNo());
        System.out.println("성공(1)실패(0) : " + no);
//        assertTrue(no == 1);

        int cnt = saledao.count();
//        assertTrue(cnt == 10);
    }

    @Test
    public void testAdminState() throws Exception {
        // 관리자가 선택한 판매글의 번호의 상태에 개입할 때 상태를 바꿔주기
        SaleDto saleDto = saledao.select(3);
        System.out.println(saleDto.getNo());
        saledao.adminState(saleDto);
        System.out.println(saleDto.getAd_state());
//        assertTrue(saleDto.getAd_state() == 'N');
    }

    @Test
    public void testSaleState() throws Exception {
        // 판매자가 판매글글을 삭제하였을 때 상태를 바꿔주기
        SaleDto saleDto = saledao.select(8);
        System.out.println(saleDto.getNo());
        saledao.delete(saleDto);
        System.out.println(saleDto.getAd_state());
//        assertTrue(saleDto.getAd_state() == 'N');
    }

    @Test
    public void testSaleModify() throws Exception {
        // 판매글 작성자(판매자)가 판매들을 수정하는 경우
        SaleDto saleDto = saledao.select(5);
        saleDto.setTitle("자바의 정석 기본편 나눔");
        saleDto.setContents("자바의 정석 기본편 나눔합니다.");
        saleDto.setDetail_addr("상록중학교 정문");
        saleDto.setTx_s_cd("F");
        assertTrue(saledao.update(saleDto)==1);
    }

}