package team.cheese.service.sale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.cheese.dao.MyPage.JjimDao;
import team.cheese.domain.*;
import team.cheese.domain.MyPage.JjimDTO;
import team.cheese.domain.MyPage.SearchCondition;
import team.cheese.dao.*;
import team.cheese.dao.MyPage.UserInfoDao;
import team.cheese.domain.MyPage.UserInfoDTO;
import team.cheese.service.ChatService;
import team.cheese.service.ImgService;
import team.cheese.service.ImgServiceImpl;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleService {
    @Autowired
    SaleDao saleDao;
    @Autowired
    SaleCategoryDao saleCategoryDao;
    @Autowired
    AdministrativeDao administrativeDao;

    @Autowired
    TagDao tagDao;
    @Autowired
    AddrCdDao addrCdDao;
    @Autowired
    SaleTagDao saleTagDao;
    @Autowired
    UserInfoDao userInfoDao;

    @Autowired
    ImgService imgService;
    @Autowired
    JjimDao jjimDao;

    @Autowired
    ChatService chatService;

    // 전체 게시글 수 count
    public int getCount() throws Exception {
        // 총 작성된 글을 count
        return saleDao.countUse();
    }

    public List<SaleDto> getPage(Map map) throws Exception {
        return saleDao.selectList(map);
    }

    public SaleDto getSale(Long no) throws Exception {
        return saleDao.select(no);
    }

    public int userSaleCnt(String ur_id) throws Exception{
        return saleDao.userSaleCnt(ur_id);
    }

    // 판매자가 자신의 게시글을 삭제할 때
    @Transactional(propagation = Propagation.REQUIRED)
    public int remove(Long no, String seller_id) throws Exception {
        // 현재 상태를 'N'으로 변경해주는거 여기서 처리
        return saleDao.delete(no, seller_id);
    }

    // 판매자가 판매 게시글을 작성할 때
    @Transactional(propagation = Propagation.REQUIRED)
    public Long write(Map<String, Object> map) throws Exception {
        // insert 해주는 거 여기서 처리

        // 1. 필수로 들어와야 되는 값 체크
        /*  addr_cd(행정동코드), addr_name(주소명),
         *   seller_id(판매자id), seller_nick(판매자명),
         *   sal_i_cd(판매카테고리), sal_name(판매카테고리명),
         *   group_no(이미지그룹번호), img_full_rt(이미지루트),
         *   pro_s_cd(사용감), tx_s_cd(거래방법),
         *   trade_s_cd_1(거래방식), title(제목), contents(내용),
         *   price(가격), bid_cd(가격제시/나눔신청여부)
         */
        //     1.1. 값이 들어와 있지 않으면 rollback -> @Valid 사용

        // 2. sale 테이블에 insert

        // 3. sale_history 테이블에 insert
        //     3.1. 실패하면 rollback

        // 4. tag테이블에 tag정보 저장
        // 5. saleTag테이블에 교차정보 저장

        SaleDto saleDto = (SaleDto) map.get("saleDto");
        //      세션에서 ID 값을 가지고 옴
        // TestSession 클래스를 사용하여 세션을 설정
        String ur_id = saleDto.getSeller_id();

        //이미지 영역
        ArrayList<ImgDto> imgList = (ArrayList<ImgDto>) map.get("imgList");
        int gno = imgService.getGno()+1;
        String img_full_rt = imgService.reg_img(imgList, gno, ur_id);
        saleDto.setImg_full_rt(img_full_rt);
        saleDto.setGroup_no(gno);

        int insertSale = 0;
        try {
            insertSale = saleDao.insertSale(saleDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sal_no = saleDto.getNo();

        List<String> tagList = (List<String>) map.get("tagList");
        System.out.println(tagList);
        int insertTagTx = insertTagTx(sal_no, ur_id, tagList);

        return sal_no;
    }
    
    // tag 데이터를 insert하는 트렌젝션 문
    @Transactional(propagation = Propagation.REQUIRED)
    public int insertTagTx(Long sal_no, String ur_id, List<String> tagList) throws Exception {
        int insertTagTx = 0;
        int resultSaleTag = 0;
        for (String contents : tagList) {
            TagDto tagDto = tagDao.selectTagContents(contents);
            if (tagDto == null) { // contents가 중복값이 없는 경우
                tagDto = new TagDto(contents, ur_id); // 새로운 객체 생성
                insertTagTx = tagDao.insert(tagDto);
            } else { // contents가 중복값이 있는 경우
                tagDto.setLast_id(ur_id);
                insertTagTx = tagDao.updateSys(tagDto);
            }
            Long tag_no = tagDto.getNo();
            resultSaleTag = insertSaleTagTx(sal_no, tag_no, ur_id);
        }
        return insertTagTx + resultSaleTag;
    }

    // saleTag 교차 테이블 데이터를 insert하는 트렌젝션 문
    @Transactional(propagation = Propagation.REQUIRED)
    public int insertSaleTagTx(Long sal_no, Long tag_no, String ur_id) throws Exception {
        SaleTagDto saleTagDto = new SaleTagDto(sal_no, tag_no, ur_id, ur_id);

        int insertSaleTagTx = saleTagDao.insert(saleTagDto);
        return insertSaleTagTx;
    }

    // 판매자가 판매 게시글을 수정할 때
    @Transactional(propagation = Propagation.REQUIRED)
    public Long update(Map<String, Object> map) throws Exception {

        SaleDto saleDto = (SaleDto) map.get("saleDto");

        //      세션에서 ID 값을 가지고 옴
        // TestSession 클래스를 사용하여 세션을 설정
        String ur_id = saleDto.getSeller_id();

        //이미지 영역
        ArrayList<ImgDto> imgList = (ArrayList<ImgDto>) map.get("imgList");
        int gno = imgService.getGno()+1;
        String img_full_rt = imgService.modify_img(imgList, gno, saleDto.getGroup_no(), ur_id);
        saleDto.setImg_full_rt(img_full_rt);
        saleDto.setGroup_no(gno);

        int update = saleDao.update(saleDto);

        Long sal_no = saleDto.getNo();

        List<String> tagList = (List<String>) map.get("tagList");
        updateTagTx(sal_no, ur_id, tagList);
        return sal_no;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int updateTagTx(Long sal_no, String ur_id, List<String> tagList) throws Exception {
        int insertTagTx = 0;
        int resultSaleTag = 0;
        deleteSaleTagTx(sal_no); // sale_tag 테이블의 판매글 번호와 일치하는 행 삭제
        for (String contents : tagList) {
            TagDto tagDto = tagDao.selectTagContents(contents);
            if (tagDto == null) { // contents가 중복값이 없는 경우
                tagDto = new TagDto(contents, ur_id); // 새로운 객체 생성
                insertTagTx = tagDao.insert(tagDto);
            } else { // contents가 중복값이 있는 경우
                tagDto.setLast_id(ur_id);
                insertTagTx = tagDao.updateSys(tagDto);
            }
            Long tag_no = tagDto.getNo();
            resultSaleTag = insertSaleTagTx(sal_no, tag_no, ur_id);
        }
        return insertTagTx + resultSaleTag;
    }

    // saleTag 교차 테이블 데이터를 insert하는 트렌젝션 문
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteSaleTagTx(Long sal_no) throws Exception {
        saleTagDao.delete(sal_no);
    }

    public List<SaleDto> getList(Map map) throws Exception {
        List<SaleDto> saleList = saleDao.selectSaleList(map);

        return saleList;
    }

    public List<SaleDto> getSelectSellerList(Map map) throws Exception {
        List<SaleDto> saleList = saleDao.selectSeller(map);

        return saleList;
    }

    // 페이징된 게시글 list를 가지고 올 때
    @Transactional(propagation = Propagation.REQUIRED)
    public List<SaleDto> getPageList(Map map) throws Exception {
        List<SaleDto> saleList = saleDao.selectList(map);

        return saleList;
    }

    // 판매글 하나에 들어가서 게시글을 읽을 때
    @Transactional(propagation = Propagation.REQUIRED)
    public Map read(Long no) throws Exception {
        increaseViewCnt(no);

        // 판매글 번호를 넘겨 받아서 Dao에서 select로 처리
        SaleDto saleDto = saleDao.select(no);
        Map map = new HashMap();
        map.put("saleDto", saleDto);
        if(saleDto == null) {
            return map;
        }

        String sal_cd = saleDto.getSal_i_cd();

        Map categoryMap = new HashMap();

        String sal_name = "";
        if(sal_cd.length() == 9) {
            categoryMap.put("length", sal_cd.length());
            categoryMap.put("sal_cd", sal_cd);

            sal_name = saleCategoryDao.categoryName(categoryMap);
            map.put("category3Name", sal_name);

            sal_cd = sal_cd.substring(0,6);
        }

        if(sal_cd.length() == 6) {
            categoryMap.put("length", sal_cd.length());
            categoryMap.put("sal_cd", sal_cd);

            sal_name = saleCategoryDao.categoryName(categoryMap);
            map.put("category2Name", sal_name);

            sal_cd = sal_cd.substring(0,3);
        }

        if(sal_cd.length() == 3) {
            categoryMap.put("length", sal_cd.length());
            categoryMap.put("sal_cd", sal_cd);
            sal_name = saleCategoryDao.categoryName(categoryMap);
            map.put("category1Name", sal_name);
        }

        List<TagDto> tagDto = saleTagRead(no);
        map.put("tagDto", tagDto);

        return map;
    }

    public String category1Text(Map map) throws Exception {
        String categoryName = saleCategoryDao.categoryName(map);
        return categoryName;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<TagDto> saleTagRead(Long no) throws Exception {

        // 판매글 번호를 넘겨 받아서 Dao에서 select로 처리
        List<SaleTagDto> saleTagList = saleTagDao.selectSalNo(no);
        List<TagDto> tagDto = null;
        if(saleTagList.size() != 0) {
            tagDto = tagRead(saleTagList);
        }
        return tagDto;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<TagDto> tagRead(List<SaleTagDto> saleTagList) throws Exception {

        List<TagDto> tagDto = new ArrayList<>();

        for( SaleTagDto saleTagDto : saleTagList) {
            Long tagNo = saleTagDto.getTag_no();
            TagDto tag = tagDao.select(tagNo);
            tagDto.add(tag);
        }

        return tagDto;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void increaseViewCnt(Long no) throws Exception {
        saleDao.increaseViewCnt(no);
    }


    // 판매 게시글을 수정할 때
    @Transactional(propagation = Propagation.REQUIRED)
    public Map modify(Long no) throws Exception {
        // 판매글 내용을 받아서 전달
        SaleDto saleDto = saleDao.select(no);
        List<TagDto> tagList = tagDao.getTagContents(no);

        String tagContents = "";
        for(TagDto tagDto : tagList) {
            tagContents += "#" + tagDto.getContents();
        }

        Map map = new HashMap();
        map.put("saleDto", saleDto);
        map.put("tagContents", tagContents);

        return map;
    }

    public int getCount(Map map) throws Exception {

        int totalCnt = saleDao.countSale(map);

        return totalCnt;
    }

    public int getSelectSellerCount(Map map) throws Exception {

        int totalCnt = saleDao.countSelectSeller(map);

        return totalCnt;
    }

    public List<SaleDto> getSearchPage(SearchCondition sc) throws Exception {
        return saleDao.selectSearchPage(sc);
    }
    public int getSearchCnt(SearchCondition sc) throws Exception {
        return saleDao.selectSearchCount(sc);
    }

    //이놈이 판매상태
    @Transactional(propagation = Propagation.REQUIRED)
    public ArrayList<UserInfoDTO> updateSaleSCd(Long no, String sal_s_cd, String seller_id) throws Exception {
        Map map = new HashMap();
        map.put("no", no);
        map.put("sal_s_cd", sal_s_cd);
        map.put("seller_id", seller_id);

        saleDao.updateSaleSCd(map);

        if(sal_s_cd.equals("C")) {
            increamentCompleteCnt(seller_id);
        }
        ArrayList<UserInfoDTO> list = chatService.loadChatlist(no);
        return list;
        //loadChatlist
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void increamentCompleteCnt(String ur_id) throws Exception {
        userInfoDao.incrementCompleteCnt(ur_id);
    }

    public List<SaleCategoryDto> selectCategory2(String category) throws Exception{
        List<SaleCategoryDto> saleCategory = saleCategoryDao.selectCategory2(category);
        return saleCategory;
    }

    public List<SaleCategoryDto> selectCategory3(String category) throws Exception{
        List<SaleCategoryDto> saleCategory = saleCategoryDao.selectCategory3(category);
        return saleCategory;
    }

    public List<AdministrativeDto> selectAddrCdList(String user_id) throws Exception {
        return administrativeDao.selectAll();
    }

    public List<SaleCategoryDto> selectCategory1() throws Exception {
        return saleCategoryDao.selectCategory1();
    }

    public void buySale(SaleDto saleDto) throws Exception {
        if(saleDao.buySale(saleDto)!=1)
            throw new Exception("구매/예약시 예외발생");
    }
    // 상세 판매글페이지에 찜 버튼 눌렀는지
    public JjimDTO bringLike(JjimDTO jjimDTO) throws Exception {
        return jjimDao.findLike(jjimDTO);
    }
}

