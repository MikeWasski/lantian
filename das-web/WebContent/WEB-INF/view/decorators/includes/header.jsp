<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/view/includes/taglib.jsp"%>
<passport:logoutUrl var="logoutUrl" position="index.html" />
<div class="header">
	<div class="sys_name">数据接入系统</div>
	<div class="sys_menu">
		<table id="sysMenuTb" class="b0_p0" style="height: 100%"></table>
	</div>
	<div class="sys_option">
		<c:if test="${null!=sessionScope.member.remarkName}">
			<span>${sessionScope.member.remarkName}</span>
		</c:if>
		<c:if test="${null==sessionScope.member.remarkName}">
			<span>${sessionScope.member.userName}</span>
		</c:if>
		<span>&nbsp;|&nbsp;</span>
		<span>
			<a href="${logoutUrl}">注销</a>
		</span>
		<span>&nbsp;|&nbsp;</span>
		<span id="themeOption" style="position: relative; cursor: pointer">主题</span>
		<div id="themeArea" class="area" style="display: none">
			<div class="areaFm">
				<div class="areaBorder">
					<div class="areaPadding">
						<div id="themeContent" class="areaContent"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>