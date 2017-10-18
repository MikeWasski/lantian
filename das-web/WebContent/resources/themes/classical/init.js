jst.complete(function() {
	var _ = jst.DomX, N = null, F = false, T = true;

	var themes = [ {
		code : "sky_blue",
		name : "深空蓝"
	} ];

	var sysMenuTb = _.tb(_.$("sysMenuTb"));
	var sysMenuRow = sysMenuTb.insert(-1);

	var sysMenus = menus; // 系统菜单;

	var buildSysMenuUrl = function(menu) {
		var subMenus = menu.children;
		if (subMenus.length > 0) {
			return buildSysMenuUrl(subMenus[0]);
		}
		return menu.url;
	}

	var checkSysMenuCurrent = function(chkMenu) {
		var c = F;
		if (chkMenu.current) {
			return T;
		}
		var subMenus = chkMenu.children;
		for (var i = 0; i < subMenus.length; i++) {
			if (checkSysMenuCurrent(subMenus[i])) {
				return T;
			}
		}
		return F;
	};

	for (var i = 0; i < menus.length; i++) {
		new function() {
			var R = this;
			R.M = menus[i];

			var menuCell = sysMenuRow.insert(-1).txt(R.M.name); // 构造系统菜单元素;
			menuCell.title = R.M.url;

			var navMenus = R.M.children; // 左部导航菜单;

			R.url = buildSysMenuUrl(R.M);

			R.selected = checkSysMenuCurrent(R.M);

			if (R.selected) {
				menuCell.bg("#1C5A9D"); // 选中系统菜单;
			}

			if (R.M.current) {
				var nav = _.$("nav");
				if (nav) {
					nav.$();
				}
				return;
			}

			for (var j = 0; j < navMenus.length; j++) {
				new function() {
					var r = this;
					r.M = navMenus[j];

					var g = N;

					var navSubMenus = r.M.children; // 左部二级菜单;

					if (R.selected) {
						g = groupNav.addGroup({
							text : r.M.name,
							event : function(G) {
								window.location.href = ctx + r.M.url;
							}
						});
					}

					if (r.M.current) {
						g.select();
					}

					for (var k = 0; k < navSubMenus.length; k++) {
						if (R.selected) {
							new function() {
								var sr = this;
								sr.M = navSubMenus[k];
								var I = g.addItem({
									text : sr.M.name,
									event : function(G) {
										window.location.href = ctx + sr.M.url;
									}
								});

								if (sr.M.current) {
									menuCell.onclick = function() {
										window.location.href = ctx + sr.M.url;
									};
									g.open();
									I.select();
								}
							};
						}
					}
				};
			}

			if (R.url) {
				menuCell.onclick = function() {
					window.location.href = ctx + R.url;
				};
			}
		};
	}

	var themeOption = _.$("themeOption");
	var themeArea = _.$("themeArea");
	var themeContent = _.$("themeContent");

	themeArea.disp = F;

	for (var i = 0; i < themes.length; i++) {
		var itemFm = _.$$("div", T).c("itemFm");
		itemFm.themeCode = themes[i].code;

		var item = _.$$("div", T).c("item").txt(themes[i].name);
		themeContent.$(itemFm.$(item));

		itemFm.onmouseover = function() {
			this.addClass("itemHover");
		};

		itemFm.onmouseout = function() {
			this.removeClass("itemHover");
		};

		itemFm.onclick = function() {
			window.location.href = ctx + "/index.html?theme=" + this.themeCode;
		};
	}

	themeOption.onclick = function(e) {
		_.unBubble(jst.e(e));
		if (!themeArea.disp) {
			themeArea.disp = T;
			themeArea.d(T);
		}
	};

	_.addEvent(document.body, "click", function() {
		if (themeArea.disp) {
			themeArea.disp = F;
			themeArea.d(F);
		}
	});
});
