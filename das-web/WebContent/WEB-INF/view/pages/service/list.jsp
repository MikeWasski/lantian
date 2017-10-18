<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/view/includes/taglib.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jst" uri="http://www.jlrnt.net/jst"%>
<!DOCTYPE html>
<html>
<head>
<title>接入服务</title>
<style type="text/css">
table.list td {
	white-space: nowrap;
	text-align: center;
}

table.list tr.header td {
	padding: 8px;
	font-weight: bold;
	background-color: #EEEEEE;
	white-space: nowrap;
	text-align: center;
}

table td.title {
	text-align: right;
	vertical-align: top;
	width: 100px;
	line-height: 24px;
}

table td.content {
	text-align: left;
}
</style>
<jst:includes>
	<jst:include name="jst.DomX" />
	<jst:include name="jst.ui.FlatDialog" />
	<jst:include name="jst.util.SimpleAsyncRequest" />
</jst:includes>
<script type="text/javascript">
	var serviceTypes = <mt:toJson jsonObject="${requestScope.serviceTypes}"/>;
	var serviceProcessors = <mt:toJson jsonObject="${requestScope.serviceProcessors}"/>;
</script>
</head>
<body>
	<div style="height: 100%; width: 100%; overflow: auto">
		<div style="zoom: 1">
			<div style="padding: 3px">
				<div style="padding: 3px">
					<input id="addService" type="button" style="width: 100px" value="添加服务" />
				</div>
				<div style="padding: 3px">
					<table id="listTb" class="b1_p6 list" style="width: 100%">
						<tr class="header">
							<td style="width: 80px; padding: 6px">序号</td>
							<td style="width: 200px">接入服务编码</td>
							<td>接入服务名称</td>
							<td style="width: 150px">跟随系统启动</td>
							<td style="width: 150px">服务运行状态</td>
							<td style="width: 150px">服务操作控制</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="${ctx}/resources/module/script/service/list.js"></script>
</body>
</html>
