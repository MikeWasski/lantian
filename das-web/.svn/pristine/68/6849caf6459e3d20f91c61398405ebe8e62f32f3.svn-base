<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/view/includes/taglib.jsp"%>
<%@ taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<!DOCTYPE html>
<html>
<head>
<title>
	<decorator:title />
</title>
<style type="text/css">
html, body {
	margin: 0;
	padding: 0;
	height: 100%;
	width: 100%;
	overflow: hidden
}

#container, #header, #footer, #content, #nav, #crumb {
	height: 100%;
	width: 100%;
	overflow: hidden;
}
</style>
<link rel="stylesheet" type="text/css" href="${ctx}/resources/common/css/style.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/resources/common/css/main_frame.css" />
<link rel="stylesheet" type="text/css" href="${ctx}/resources/themes/classical/sky_blue/theme.css" />
<decorator:head />
<jst:includes>
	<jst:include name="jst.ui.BorderLayout" />
	<jst:include name="jst.ui.FlowPanel" />
	<jst:include name="jst.ui.GroupNav" />
	<jst:include name="jst.ui.WaitingBox" />
</jst:includes>
<script type="text/javascript">
	var ctx = "${ctx}";
	var navMenus = [];
	var menus = <mt:toJson jsonObject="${requestScope.menus}" />;
</script>
<script type="text/javascript" src="${ctx}/resources/common/script/main_frame.js"></script>
</head>
<body>
	<%-- 容器区域 --%>
	<div id="container" style="background-color: #DFEFFF;"></div>
	<%-- 头部区域 --%>
	<div id="header">
		<jsp:include page="/WEB-INF/view/decorators/includes/header.jsp"></jsp:include>
	</div>
	<%-- 面包屑区域 --%>
	<div id="crumb" style="background-color: #F7F7F7">
		<div id="crumbArea" style="height: 100%; line-height: 24px; padding: 0 8px"></div>
	</div>
	<%-- 内容区域 --%>
	<div id="content" style="background-color: #FFFFFF">
		<decorator:body />
	</div>
	<%-- 尾部区域 --%>
	<div id="footer">
		<jsp:include page="/WEB-INF/view/decorators/includes/footer.jsp"></jsp:include>
	</div>
	<div style="display: none; background-image: url('${ctx}/resources/common/image/loading_01.gif');"></div>
</body>
</html>
