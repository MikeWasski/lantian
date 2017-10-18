var _ = jst.DomX;
var ctPanel = null, contentLayout = null, mainLayout = null, crumbArea = null;

jst.complete(function() {
	crumbArea = _.$("crumbArea");

	ctPanel = new jst.ui.FlowPanel({
		border : 0
	});

	ctPanel.setEl("content");

	contentLayout = new jst.ui.BorderLayout({ // 内容区布局,不设定autoResize，由主布局驱动;
		center : {
			dest : ctPanel
		}
	});

	mainLayout = new jst.ui.BorderLayout({ // 主布局;
		bgColor : "#66FF66",
		north : {
			dest : "header",
			size : 40,
			resize : false
		},
		south : {
			dest : "footer",
			size : 20,
			resize : false
		},
		west : {
			dest : "nav",
			size : 180,
			resize : true
		},
		center : { // 中部区域使用内容区布局;
			dest : contentLayout
		},
		autoResize : true
	});

	mainLayout.fill("container");
});
