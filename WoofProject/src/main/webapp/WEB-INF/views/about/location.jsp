<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Mire Woof</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<!-- css common Area 헤더 푸터에 쓸 css 경로-->
<%@ include file="/WEB-INF/views/common/style.jsp"%>
<!-- script common Area 헤더 푸터에 쓸 script 경로-->
<%@ include file="/WEB-INF/views/common/script.jsp"%>
<!-- css local Area 각 개별페이지 css 경로는 여기다가 쓸 것-->
<%-- <%@ include file="" %> --%>
<!-- script local Area  각 개별페이지 script 경로는 여기다가 쓸 것 -->
<%-- <%@ include file="" %> --%>
</head>
<body>
	<!-- Header Area -->
	<%@ include file="/WEB-INF/views/common/header.jsp"%>
	<!-- Menu Area -->
	<%@ include file="/WEB-INF/views/common/mainMenu.jsp"%>
	<!-- subMenu Area -->
	<main class="pt-2 text-center">
		<!-- ====================Content Area : <main> 과 </maim> 사이에 콘첸츠 작성 /======================================================== -->
		<h1><spring:message code="common.location"/></h1>
		<p><spring:message code="common.woofAddress"/></p>
		<!-- 지도를 표시할 div 입니다 -->
	
		<div id="map" style="width: 100%; height: 350px;"></div>

		<script type="text/javascript" src="//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapAppkey}&libraries=services"></script>
		<script>
			var mapContainer = document.getElementById('map'), // div to designate map
			mapOption = {
				center : new kakao.maps.LatLng(33.450701, 126.570667), // centering map
				level : 3 // level of map zooming 
			};
			var map = new kakao.maps.Map(mapContainer, mapOption); // create map
			// create object to change address to position
			var geocoder = new kakao.maps.services.Geocoder();
			// search position on map
			geocoder.addressSearch('서울 성동구 왕십리로 315', function(result, status) {
				// if search is successful,
				if (status === kakao.maps.services.Status.OK) {
					var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
					// mark position with a marker
					var marker = new kakao.maps.Marker({map : map, position : coords});
					// display information
					var infowindow = new kakao.maps.InfoWindow(
						{
							content : '<div style="width:150px;text-align:center;padding:6px 0;">MIREWOOF</div>'
						});
					infowindow.open(map, marker);
					// move to centered position
					map.setCenter(coords);
				}
			});
		</script>
	</main>
	<!-- Footer Area -->
	<%@ include file="/WEB-INF/views/common/footer.jsp"%>
</body>
</html>