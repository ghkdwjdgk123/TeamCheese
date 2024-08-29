package team.cheese.controller.CommunityBoard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import team.cheese.domain.AddrCdDto;
import team.cheese.domain.Comment.CommentDto;
import team.cheese.domain.CommunityBoard.CommunityBoardDto;
import team.cheese.domain.CommunityHeart.CommunityHeartDto;
import team.cheese.domain.ImgDto;
import team.cheese.domain.MyPage.UserInfoDTO;
import team.cheese.domain.ProfileimgDto;
import team.cheese.entity.ImgFactory;
import team.cheese.entity.PageHandler;
import team.cheese.service.Comment.CommentService;
import team.cheese.service.CommunityBoard.CommunityBoardService;
import team.cheese.service.CommunityHeart.CommunityHeartService;
import team.cheese.service.ImgService;
import team.cheese.service.MyPage.UserInfoService;
import team.cheese.service.UserService;

import javax.annotation.RegEx;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.stream.events.Comment;
import java.util.*;


@Controller
@RequestMapping(value = "/community")
public  class CommunityBoardController {
    @Autowired
    CommunityBoardService communityBoardService;

    @Autowired
    CommunityHeartService communityHeartService;

    @Autowired
    CommentService commentService;
    @Autowired
    ImgService imgService;

    @Autowired
    UserInfoService userInfoService;

    //community메인페이지
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String communityBoardHome(Model m) throws Exception {

        List<CommunityBoardDto> list = communityBoardService.readAll();
        m.addAttribute("list", list);

        return "/CommunityHome";

    }

    //community세부 리스트 페이지
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String communityBoardList(CommunityBoardDto communityBoardDto, Model m) throws Exception {

        List<CommunityBoardDto> list = communityBoardService.readAll();
        m.addAttribute("list", list);
        return "/CommunityList";
    }

    //    community세부 리스트 페이지ajax
    @RequestMapping(value = "/home/story", method = RequestMethod.GET)
    @ResponseBody
    public List test(Character ur_state) throws Exception {
        List<CommunityBoardDto> list = communityBoardService.readAll();

        return list;
    }


    @GetMapping("/story")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBoards(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "6") int pageSize,
                                                         @RequestParam(defaultValue = "commu_A") String category) throws Exception {

        int totalCount = communityBoardService.getCountByCategory(category);
        PageHandler ph = new PageHandler(totalCount, page, pageSize);


        List<CommunityBoardDto> list = communityBoardService.getPageByCategory(category, ph.getOffset(), pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("content", list);
        response.put("ph", ph);


        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    //글쓰기 페이지로 이동
    @RequestMapping(value = "/write", method = RequestMethod.GET)
    public String communityBoard() throws Exception {
        return "/CommunityWriteBoard";
    }


    //세션값 필요

    @ResponseBody
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Object> map,
                                           Model m,
                                           HttpServletRequest request) throws Exception {

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String userNick = (String) session.getAttribute("userNick");


        List<AddrCdDto> addrCdList = (List<AddrCdDto>) session.getAttribute("userAddrCdDtoList");



        if (userId == null || userNick == null) {
            return new ResponseEntity<>("User not logged in or session expired", HttpStatus.UNAUTHORIZED);
        }

        AddrCdDto selectedAddr = null;
        for (AddrCdDto addrCdDto : addrCdList) {
            if (addrCdDto.getUr_id().equals(userId)) {
                selectedAddr = addrCdDto;
                break;
            }
        }

        if (selectedAddr == null) {
            return new ResponseEntity<>("Address not found for user", HttpStatus.BAD_REQUEST);
        }

        // ObjectMapper : JSON 형태를 JAVA 객체로 변환

        ObjectMapper objectMapper = new ObjectMapper();
        CommunityBoardDto communityBoardDto = objectMapper.convertValue(map.get("communityBoardDto"), CommunityBoardDto.class);

        ArrayList<ImgDto> imgList = objectMapper.convertValue(map.get("imgList"), new TypeReference<ArrayList<ImgDto>>() {
        });

        if (imgList.size() != 0) {
            //이미지영역
            ImgFactory ifc = new ImgFactory();
            //이미지 유효성검사 하는곳
            imgList = ifc.checkimgfile(map);
        }


        //필수
        communityBoardDto.setfirst_id(userId);
        communityBoardDto.setlast_id(userId);
        // 유효성 검사를 위한 값 설정
        communityBoardDto.setur_id(userId);
        communityBoardDto.setNick(userNick);
        communityBoardDto.setaddr_cd(selectedAddr.getAddr_cd());
        communityBoardDto.setaddr_no((int) selectedAddr.getNo());
        communityBoardDto.setaddr_name(selectedAddr.getAddr_name());

        System.out.println(communityBoardDto);
        //    유효성 검사 수행
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CommunityBoardDto>> violations = validator.validate(communityBoardDto);

        for (ConstraintViolation<CommunityBoardDto> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();
            CommunityBoardDto rootBean = violation.getRootBean();

        }

//         유효성 검사 결과 확인
        if (!violations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Map mapDto = new HashMap();
            mapDto.put("communityBoardDto", communityBoardDto);
            mapDto.put("imgList", imgList);
            communityBoardService.write(mapDto);
            return new ResponseEntity<>("/community/list", HttpStatus.OK);

        } catch (Exception e) {

            return new ResponseEntity<>("필수값을 입력해주세요", HttpStatus.BAD_REQUEST);
        }
    }



    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public String read(Integer no, Model m, RedirectAttributes redirectAttributes) throws Exception {
        try {
            CommunityBoardDto communityBoardDto = communityBoardService.read(no);
            m.addAttribute("communityBoardDto", communityBoardDto);

            //프로필 가져옴
            UserInfoDTO userinfo = userInfoService.read(communityBoardDto.getur_id());
            Long num = Long.valueOf(no);
            ProfileimgDto pdto = new ProfileimgDto(num, userinfo.getUr_id(), userinfo.getNick(), userinfo.getImg_full_rt());
            m.addAttribute("profile", pdto);

            List<ImgDto> imglist = imgService.read(communityBoardDto.getGroup_no());
            m.addAttribute("imglist", imglist);

            //하트수
            String totalLikeCount = communityHeartService.countLike(no);
            m.addAttribute("totalLikeCount", totalLikeCount);

            //댓글수
            int totalCommentCount = communityBoardDto.getComment_count();
            m.addAttribute("totalCommentCount", totalCommentCount);

            return "/CommunityBoard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            return "/ErrorPage";
        }
    }


    //세션값 필요
    @ResponseBody
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public ResponseEntity<String> update(@RequestBody Map<String, Object> map, Model m, HttpServletRequest request) throws Exception {

        //Interceptor 사전에 들림
        // ObjectMapper : JSON 형태를 JAVA 객체로 변환

        ObjectMapper objectMapper = new ObjectMapper();
        CommunityBoardDto communityBoardDto = objectMapper.convertValue(map.get("communityBoardDto"), CommunityBoardDto.class);

        ArrayList<ImgDto> imgList = objectMapper.convertValue(map.get("imgList"), new TypeReference<ArrayList<ImgDto>>() {
        });

        if (imgList.size() != 0) {
            //이미지영역
            ImgFactory ifc = new ImgFactory();
            //이미지 유효성검사 하는곳
            imgList = ifc.checkimgfile(map);
            if (imgList == null) {
                return new ResponseEntity<String>("이미지 등록 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        //    유효성 검사 수행
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CommunityBoardDto>> violations = validator.validate(communityBoardDto);

        for (ConstraintViolation<CommunityBoardDto> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object invalidValue = violation.getInvalidValue();
            CommunityBoardDto rootBean = violation.getRootBean();

        }

        // 유효성 검사 결과 확인
        if (!violations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Map mapDto = new HashMap();
            mapDto.put("communityBoardDto", communityBoardDto);
            mapDto.put("imgList", imgList);
            communityBoardService.modify(mapDto);
            return new ResponseEntity<>("/community/list", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("죄송합니다.글 수정에 실패했습니다.", HttpStatus.BAD_REQUEST);
        }

    }

    //세션값 필요
    //삭제(상태변경)
    @RequestMapping(value = "/userStateChange", method = RequestMethod.POST)
    public ResponseEntity<?> userStateChange(@RequestBody CommunityBoardDto communityBoardDto) throws Exception {

        try {
            int updateResult = communityBoardService.userStateChange(communityBoardDto);

            if (updateResult == 1) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "상태 변경에 성공하였습니다.");
                response.put("newState", communityBoardDto.getur_state());
                return ResponseEntity.ok(response);

            } else {
                // 정상적으로 처리되지 않은 경우, 내부 서버 오류 응답
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경 실패");
            }
        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("처리 중 오류 발생: " + e.getMessage());
        }
    }



    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String Edit(Integer no, Model m, HttpServletRequest request, RedirectAttributes redirectAttributes) throws Exception {

        CommunityBoardDto communityBoardDto = communityBoardService.findCommunityBoardById(no);

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");
        String userNick = (String) session.getAttribute("userNick");


        if (userId == null || userNick == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 먼저 하셔야합니다.");
            return "redirect:/community/read?no=" + no;

        }

        if (!Objects.equals(userId, communityBoardDto.getur_id())) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보가 일치하지 않습니다.");
            return "redirect:/community/read?no=" + no;
        } else {
            m.addAttribute("communityBoardDto", communityBoardDto);
        }


        m.addAttribute("communityBoardDto", communityBoardDto);

        List<ImgDto> imglist = imgService.read(communityBoardDto.getGroup_no());
        m.addAttribute("imglist", imglist);

        return "CommunityWriteBoard";
    }


    //하트 누를때 상태
    @ResponseBody
    @RequestMapping(value = "/doLike", method = RequestMethod.PATCH)
    public ResponseEntity<Map<String, Object>> doLike(@RequestBody Map<String, Integer> requestBody, HttpServletRequest request) throws Exception {

        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        int postNo = requestBody.get("no");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 먼저 해주세요"));
        }

        try {
            CommunityHeartDto communityHeartDto = new CommunityHeartDto();
            communityHeartDto.setUr_id(userId);
            communityHeartDto.setPost_no(postNo);
            //필수
            communityHeartDto.setFirst_id(userId);
            communityHeartDto.setLast_id(userId);


            communityHeartService.doLike(communityHeartDto);


            Integer countLike = Integer.valueOf(communityHeartService.countLike(postNo));
            Integer totalLike = communityBoardService.totalLike(postNo);
            Map<String, Object> response = new HashMap<>();
            response.put("countLike", countLike);
            response.put("totalLike", totalLike);



            return ResponseEntity.ok(response);


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "서버 에러가 발생했습니다."));
        }
    }


    //댓글쓰기
    @PostMapping("/writeComment")
    @ResponseBody
    public ResponseEntity<List<CommentDto>> write(@RequestBody CommentDto commentDto, HttpServletRequest request) throws Exception {
        try {
            // 세션에서 ur_id와 nick 가져오기, 기본값 설정
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            String userNick = (String) session.getAttribute("userNick");
            // DTO에 세션에서 가져온 데이터 설정
            commentDto.setUr_id(userId);
            commentDto.setNick(userNick);
            commentDto.setFirst_id(userId);
            commentDto.setLast_id(userId);

            // 최대 번호 찾기 및 예외 처리
            Integer maxNo = commentService.findMaxByPostNo(commentDto.getPost_no());
            commentDto.setNo(maxNo + 1);


            //    유효성 검사 수행
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

            // 댓글 작성
            commentService.write(commentDto);


            //프로필 가져옴


            // 댓글 목록 읽기
            List<CommentDto> comments = commentService.readAll(commentDto.getPost_no());
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            // 로깅 및 에러 응답 처리

            e.printStackTrace();

        }
        return null;
    }

    //댓글 읽어오기
    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> readComments(@RequestParam int postId) throws Exception {
        try {

            List<CommentDto> comments = commentService.readAll(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/getComment")
    public ResponseEntity<Object>getComment(@RequestParam Integer no, @RequestParam Integer post_no, HttpServletRequest request ) throws Exception {
        try{
            CommentDto commentDto = new CommentDto();
            commentDto.setPost_no(post_no);
            commentDto.setNo(no);



            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            String userNick = (String) session.getAttribute("userNick");


            //no와 post_no값을 주고 모든 필드값을 받아옴
            CommentDto comment = commentService.read(commentDto);


            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            if (!Objects.equals(userId, comment.getUr_id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            //모든 comment의 정보를 보낸다.
            Map<String, Object> response = new HashMap<>();
            response.put("comment", comment);
            response.put("sessionUserId", userId);

            return ResponseEntity.ok(response);

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}