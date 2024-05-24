<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="../fixed/header.jsp"%>

<style>
    #saleListTB {
        margin: 0 auto; /* 수평 가운데 정렬 */
        width: 80%; /* 테이블의 너비 설정 */
        text-align: center; /* 텍스트 가운데 정렬 */
    }

    .page-space {
        margin: 0 5px; /* 공백 크기 조절 */
    }
    /* 임시로 style 추가 : css 수정 필요*/
    img {
        width: 100px;
        height: 100px;
    }
</style>

<div class="maincontent">
<button type="button" onclick="writeBtn()">글쓰기</button>
    <c:choose>
        <c:when test="${empty sessionScope.userId}">
            <select id="addr_cd" style="display: none;">
                <option id="selectAll" value="null" selected>전체</option>
                <c:forEach var="AddrCd" items="${addrCdList}">
                    <option value="<c:out value='${AddrCd.addr_cd}'/>"><c:out value='${AddrCd.addr_name}'/></option>
                </c:forEach>
            </select>
        </c:when>
        <c:otherwise>
            <select id="addr_cd">
                <option id="selectAll" value="null" selected>전체</option>
                <c:forEach var="AddrCd" items="${addrCdList}">
                    <option value="<c:out value='${AddrCd.addr_cd}'/>"><c:out value='${AddrCd.addr_name}'/></option>
                </c:forEach>
            </select>
        </c:otherwise>
    </c:choose>
    <br>
<select id="category1" onchange="loadCategory2()">
    <option value="null" selected>대분류(전체)</option>
    <c:forEach var="category" items="${saleCategory1}">
        <option value="<c:out value='${category.sal_cd}'/>"><c:out value='${category.name}'/></option>
    </c:forEach>
</select>

<select id="category2" onchange="loadCategory3()">
    <option value="" disabled selected>중분류</option>
</select>

<select id="category3">
    <option value="" disabled selected>소분류</option>
</select>
<p style="color: orangered;" id="salecategoryMsg"></p>
<span><b><p style="display: inline; color: red" id="sal_name"></p></b> 상품</span>
<br><br>
<table id="saleListTB">
    <tr>
        <th class="no">번호</th>
        <th class="img">이미지</th>
        <th class="title">제목</th>
        <th class="saleStatus">판매상태</th>
        <th class="writer">이름</th>
        <th class="addr_name">주소명</th>
        <th class="regdate">등록일</th>
        <th class="viewcnt">조회수</th>
    </tr>
    <tbody id="saleList">
    </tbody>
</table>
<br>
<div id="pageContainer" style="text-align: center">
</div>
<br>
</div>
<script>
    $(document).ready(function () {
        let sessionId = "${sessionScope.userId}";
        let saleStatusText;
        let sal_name = "";

        window.loadCategory2 = function () {
            let category1Value = $('#category1').val();
            console.log(category1Value)
            if (category1Value !== "") {
                $.ajax({
                    type: "POST",
                    url: "/sale/saleCategory2",
                    dataType: "json", // 받을 값
                    data: {category1: category1Value},
                    success: function (data) {
                        let category2Select = document.getElementById("category2");
                        category2Select.innerHTML = "<option value='' disabled selected>중분류</option>";
                        let category3Select = document.getElementById("category3");
                        category3Select.innerHTML = "<option value='' disabled selected>소분류</option>";
                        console.log("data.length : ", data.length);
                        if (data.length > 0) {
                            data.forEach(function (category) {
                                // console.log(typeof category);
                                if (category.sal_cd.startsWith(category1Value)) {
                                    let option = new Option(category.name, category.sal_cd);
                                    category2Select.add(option);
                                }
                            });
                        }
                    },
                    error: function (xhr, status, error) {
                        alert("error", error);
                    }
                });
            }
        };

        window.loadCategory3 = function () {
            let category2Value = $('#category2').val();
            console.log(category2Value)
            if (category2Value !== "") {
                $.ajax({
                    type: "POST",
                    url: "/sale/saleCategory3",
                    dataType: "json",
                    data: {category2: category2Value},
                    success: function (data) {
                        let category3Select = document.getElementById("category3");
                        category3Select.innerHTML = "<option value='' disabled selected>소분류</option>";
                        console.log("data.length : ", data.length);
                        if (data.length > 0) {
                            data.forEach(function (category) {
                                if (category.sal_cd.startsWith(category2Value)) {
                                    let option = new Option(category.name, category.sal_cd);
                                    category3Select.add(option);
                                }
                            });
                        }
                    },
                    error: function (xhr, status, error) {
                        alert("Error", error);
                    }
                });
            }
        };

        window.saleList = function(addr_cd, sal_i_cd, page = 1, pageSize = 10) {
            $.ajax({
                type: 'GET',       // 요청 메서드
                url: "/sale/salePage?page=" + page + "&pageSize=" + pageSize + "&addr_cd=" + addr_cd + "&sal_i_cd=" + sal_i_cd,  // 요청 URI
                headers: {"content-type": "application/json"}, // 요청 헤더
                dataType: 'json',
                success: function (data) {
                    // 후기글 목록과 페이징 정보 가져오기
                    // let comments = result.comments;
                    let ph = data.ph;
                    let saleList = data.saleList;
                    let startOfToday = data.startOfToday;
                    $("#saleList").html(updateSaleList(saleList, startOfToday, ph, addr_cd, sal_i_cd));
                },
                error: function (result) {
                    alert("화면 로딩 중 오류 발생");
                    // alert(result.responseText)
                } // 에러가 발생했을 때, 호출될 함수
            }); // $.ajax()
        }

        // "전체" 옵션이 보이지 않도록 설정
        if (!!sessionId?.trim()) {
            document.getElementById("selectAll").style.display = "none"; // "전체" 옵션 숨기기

            // 첫 번째 옵션 선택
            let addrCdSelect = document.getElementById("addr_cd");
            addrCdSelect.selectedIndex = 1;
        }

        let addr_cd = $("#addr_cd").val();
        let sal_i_cd = $("#category1").val();

        // 대분류 선택 전 메시지 설정
        $("#sal_name").text($("#category1 option:checked").text());

        saleList(addr_cd, sal_i_cd);

        // AddrCd 선택이 변경될 때마다 실행되는 함수
        $("#addr_cd").change(function () {
            // 선택된 AddrCd 값 가져오기
            let addr_cd = $(this).val();
            let sal_i_cd = null;
            $("#category1").val('null').prop('selected', true);
            $("#category2").val('').prop('selected', true);
            $("#category3").val('').prop('selected', true);
            $("#sal_name").text("전체");

            saleList(addr_cd, sal_i_cd);
        });

        $("#category1").change(function () {
            // 선택된 AddrCd 값 가져오기
            let addr_cd = $("#addr_cd").val();
            let sal_i_cd = $(this).val();
            $("#sal_name").text($("#category1 option:checked").text());

            saleList(addr_cd, sal_i_cd);
        });

        $("#category2").change(function () {
            // 선택된 AddrCd 값 가져오기
            let addr_cd = $("#addr_cd").val();
            let sal_i_cd = $(this).val();
            $("#sal_name").text($("#category2 option:checked").text());

            saleList(addr_cd, sal_i_cd);
        });

        $("#category3").change(function () {
            // 선택된 AddrCd 값 가져오기
            let addr_cd = $("#addr_cd").val();
            let sal_i_cd = $(this).val();
            $("#sal_name").text($("#category3 option:checked").text());

            saleList(addr_cd, sal_i_cd);
        });

        window.writeBtn = function () {
            // 선택된 주소 코드(addr_cd) 값 가져오기
            let addrCdValue = $('#addr_cd').val();
            let addrCdName = $("#addr_cd option:checked").text();

            // form 엘리먼트 생성
            let form = $('<form>', {
                method: 'POST',
                action: '/sale/write',
                acceptCharset: 'UTF-8'
            });

            // hidden input 생성
            $('<input>').attr({
                type: 'hidden',
                name: 'addr_cd',
                value: addrCdValue
            }).appendTo(form);

            $('<input>').attr({
                type: 'hidden',
                name: 'addr_name',
                value: addrCdName
            }).appendTo(form);

            // form을 body에 추가하고 자동으로 submit
            form.appendTo('body').submit();
        }

        // 업데이트된 saleList를 화면에 출력하는 함수
        function updateSaleList(saleList, startOfToday, ph, addr_cd, sal_i_cd) {
            // 기존 saleList 테이블의 tbody를 선택하여 내용을 비웁니다.
            $("#saleList").empty();

            if (saleList.length > 0) {
                // 판매 상태에 따라 텍스트 설정
                saleList.forEach(function (sale) {
                    switch (sale.sal_s_cd) {
                        case 'S':
                            saleStatusText = '판매중';
                            break;
                        case 'R':
                            saleStatusText = '예약중';
                            break;
                        case 'C':
                            saleStatusText = '거래완료';
                            break;
                        default:
                            saleStatusText = '';
                    }

                    let row = $("<tr>");
                    row.append($("<td>").text(sale.no)); // 판매 번호
                    row.append($("<td>").addClass("Thumbnail_ima").html("<a href='/sale/read?no=" + sale.no + "'>" + "<img src='/img/display?fileName=" + sale.img_full_rt + "'/>" + "</a>")); // 이미지
                    row.append($("<td>").addClass("title").html("<a href='/sale/read?no=" + sale.no + "'>" + sale.title + "</a>")); // 제목
                    row.append($("<td>").text(saleStatusText)); // 판매 상태
                    row.append($("<td>").text(sale.seller_nick)); // 판매자 닉네임
                    row.append($("<td>").text(sale.addr_name)); // 주소명

                    let saleDate = new Date(sale.h_date);

                    row.append($("<td>").addClass("regdate").text(dateToString(sale.h_date, startOfToday)));
                    row.append($("<td>").text(sale.view_cnt)); // 조회수
                    $("#saleList").append(row);
                });
            } else {
                $("#saleList").append("<tr><td colspan='5'>데이터가 없습니다.</td></tr>");
            }

            $("#pageContainer").empty(); // 기존에 있는 페이지 내용 비우기

            if (ph.totalCnt != null && ph.totalCnt != 0) {
                let pageContainer = $('<div>').attr('id', 'pageContainer').css('text-align', 'center'); // 새로운 div 엘리먼트 생성
                if (ph.prevPage) {
                    pageContainer.append('<a onclick="saleList(\'' + addr_cd + '\', ' + sal_i_cd + ', ' + (ph.beginPage - 1) + ', ' + ph.pageSize + ')">&lt;</a>');
                }
                for (let i = ph.beginPage; i <= ph.endPage; i++) {
                    // 페이지 번호 사이에 공백 추가
                    pageContainer.append('<span class="page-space"></span>');
                    pageContainer.append('<a class="page ' + (i == ph.page ? "paging-active" : "") + '" href="#" onclick="saleList(\'' + addr_cd + '\', ' + sal_i_cd + ', ' + i + ', ' + ph.pageSize + ')">' + i + '</a>');
                }
                if (ph.nextPage) {
                    pageContainer.append('<span class="page-space"></span>');
                    pageContainer.append('<a onclick="saleList(\'' + addr_cd + '\', ' + sal_i_cd + ', ' + (ph.endPage + 1) + ', ' + ph.pageSize + ')">&gt;</a>');
                }
                $("#pageContainer").html(pageContainer); // 새로 생성한 페이지 컨테이너를 추가
            }
        }

        function addZero(value = 1) {
            return value > 9 ? value : "0" + value;
        }

        function dateToString(ms = 0, startOfToday) {
            console.log(startOfToday);
            let date = new Date(ms);

            let yyyy = date.getFullYear();
            let mm = addZero(date.getMonth() + 1);
            let dd = addZero(date.getDate());

            let HH = addZero(date.getHours());
            let MM = addZero(date.getMinutes());
            let ss = addZero(date.getSeconds());

            let now = new Date();

            if (date.getTime() >= startOfToday) {
                let secondsAgo = Math.floor((now.getTime() - date.getTime()) / 1000);
                if (secondsAgo < 60) {
                    return secondsAgo + "초 전";
                } else {
                    let minutesAgo = Math.floor(secondsAgo / 60);
                    if (minutesAgo < 60) {
                        return minutesAgo + "분 전";
                    } else {
                        let hoursAgo = Math.floor(minutesAgo / 60);
                        return hoursAgo + "시간 전";
                    }
                }
            }
            let daysAgo = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24)) + 1;
            return daysAgo + "일 전";
        }
    });
</script>


<%@include file="../fixed/footer.jsp"%>