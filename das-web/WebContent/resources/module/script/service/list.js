jst.complete(function() {
	var _ = jst.DomX, N = null, F = false, T = true, WB = jst.ui.WaitingBox;

	// 编辑设置类;
	var EditSetting = function() {
		var r = this;
		// ------------------------------------------添加接入服务------------------------------------------;
		var HS = "padding:3px 3px 3px 6px;lineHeight:25px;color:#0000FF;fontWeight: bold";
		var DC = "DEFINITION";

		var editArea = _.$$("div", T).s("padding:3px");

		var baseInfoH = _.$$("div", T).s(HS).txt("1. 配置基本信息：");
		var typeSettingH = _.$$("div", T).s(HS).txt("2. 配置服务类型：");
		var processorSettingH = _.$$("div", T).s(HS).txt("3. 选择服务处理器：");
		var processorOuterSettingH = _.$$("div", T).s(HS).txt("4. 配置服务处理器-外部参数：");
		var processorInnerSettingH = _.$$("div", T).s(HS).txt("5. 配置服务处理器-内部参数：");

		var baseInfoTb = _.tb().c("b0_p3");
		var typeSettingTb = _.tb().c("b0_p3");
		var processorSettingTb = _.tb().c("b0_p3");
		var processorOuterSettingTb = _.tb().c("b0_p3");
		var processorInnerSettingTb = _.tb().c("b0_p3");

		editArea.$(baseInfoH).$(baseInfoTb);
		editArea.$(typeSettingH).$(typeSettingTb);
		editArea.$(processorSettingH).$(processorSettingTb);
		editArea.$(processorOuterSettingH).$(processorOuterSettingTb);
		editArea.$(processorInnerSettingH).$(processorInnerSettingTb);

		editArea.$(_.$$("div", T).h(8).s("overflow:hidden").$(_.$$("div", T)));

		var blurTrim = function() {
			this.value = this.value.trim();
		};

		// 基本信息----------------------------------------------------------------;

		var codeRow = baseInfoTb.insert(-1);
		var codeTitleCell = codeRow.insert(-1).c("title").txt("接入服务编码：");
		var codeCell = codeRow.insert(-1).c("content");
		var updateCodeInput = _.$$("input", T).attr("type", "hidden"); // 修改前的服务编码;
		var codeInput = _.$$("input", T).attr("type", "text").w(150); // 服务编码;
		codeInput.c(DC).attr("name", "serviceCode"); // 设定元素名称;
		codeInput.onblur = blurTrim;
		codeCell.$(updateCodeInput).$(codeInput);

		var nameRow = baseInfoTb.insert(-1);
		var nameTitleCell = nameRow.insert(-1).c("title").txt("接入服务名称：");
		var nameCell = nameRow.insert(-1).c("content");
		var nameInput = _.$$("input", T).attr("type", "text").w(150);
		nameInput.c(DC).attr("name", "serviceCode"); // 设定元素名称;
		nameInput.onblur = blurTrim;
		nameCell.$(nameInput);

		var autoStartupRow = baseInfoTb.insert(-1);
		var autoStartupTitleCell = autoStartupRow.insert(-1).c("title").txt("跟随系统启动：");
		var autoStartupCell = autoStartupRow.insert(-1).c("content");
		var autoStartupSel = _.$$("select", T).w(60);
		autoStartupSel.options.add(new Option("否", "0"));
		autoStartupSel.options.add(new Option("是", "1"));
		autoStartupSel.c(DC).attr("name", "autoStartup"); // 设定元素名称;
		autoStartupCell.$(autoStartupSel);

		// 服务类型----------------------------------------------------------------;
		var serviceTypeSel = _.$$("select", T).w(152);
		serviceTypeSel.options.add(new Option("--选择服务类型--", "-1"));
		for (var i = 0; i < serviceTypes.length; i++) {
			var type = serviceTypes[i];
			serviceTypeSel.options.add(new Option(type.name, type.code));
		}

		var processorSel = N; // 处理器选择框;

		var processorChangeEvent = function() {
			processorInnerSettingTb.clear(0);
			processorOuterSettingTb.clear(0);

			var psIndex = this.selectedIndex - 1;
			if (psIndex < 0) {
				return;
			}

			var ps = serviceProcessors[psIndex]; // 处理器对象;
			var psCode = ps.code; // 处理器编码;
			var typeCode = serviceTypeSel.value; // 服务类型编码;

			var ipDefinitions = ps.innerParamDefinitions; // 加载处理器内部参数;
			for (var i = 0; i < ipDefinitions.length; i++) {
				var def = ipDefinitions[i];

				var defRow = processorInnerSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");

				var defEl = N;
				switch (def.type) {
				case "text":
					defEl = _.$$("input", T).attr("autocomplete", "off").attr("type", "text");
					defEl.onblur = blurTrim;
					break;
				case "select":
					defEl = _.$$("select", T);
					for (var j = 0; j < def.options.length; j++) {
						var opt = def.options[j];
						defEl.options.add(new Option(opt.name, opt.value));
					}
					defEl.selectedIndex = 0;
					break;
				default:
					break;
				}

				if (!defEl) {
					continue;
				}

				defEl.c(DC).attr("name", def.code + "-inner-" + psCode + "-" + typeCode); // 设定元素名称;

				if (N != def.width) {
					defEl.w(def.width);
				}

				defCell.$(defEl);
			}

			var opDefinitions = ps.outerParamDefinitions; // 加载处理器外部参数;
			for (var i = 0; i < opDefinitions.length; i++) {
				var def = opDefinitions[i];

				var defRow = processorOuterSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");

				var defEl = N;
				switch (def.type) {
				case "text":
					defEl = _.$$("input", T).attr("autocomplete", "off").attr("type", "text");
					defEl.onblur = blurTrim;
					break;
				case "select":
					defEl = _.$$("select", T);
					for (var j = 0; j < def.options.length; j++) {
						var opt = def.options[j];
						defEl.options.add(new Option(opt.name, opt.value));
					}
					defEl.selectedIndex = 0;
					break;
				case "mapping":
					new (function() {
						var mDef = def;
						var mapTb = _.tb();
						var mapBtnCell = mapTb.insert(-1).insert(-1).s("verticalAlign:top").attr("colSpan", 4);
						var mapSpan = _.$$("span", T).lh(18).s("cursor:pointer;color:#0000FF").txt("添加映射元素").unSel();

						mapSpan.onclick = function() {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							valInput.onblur = blurTrim;
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						};

						mapBtnCell.$(mapSpan);
						defCell.$(mapTb);
					});
					break;
				default:
					break;
				}

				if (!defEl) {
					continue;
				}

				defEl.c(DC).attr("name", def.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;

				if (N != def.width) {
					defEl.w(def.width);
				}

				defCell.$(defEl);
			}
		};

		// 服务类型改变事件;
		var serviceTypeChangeEvent = function() {
			typeSettingTb.clear(1);
			processorSettingTb.clear(0);
			processorInnerSettingTb.clear(0);
			processorOuterSettingTb.clear(0);

			var typeIndex = serviceTypeSel.selectedIndex - 1;
			if (typeIndex < 0) {
				return;
			}

			var serviceType = serviceTypes[typeIndex], typeCode = serviceType.code; // 服务类型;
			var paramDefinitions = serviceType.paramDefinitions; // 服务参数定义;

			for (var i = 0; i < paramDefinitions.length; i++) {
				var def = paramDefinitions[i];

				var defRow = typeSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");
				
				// add by wx
				var defEl = N;
				switch(def.type) {
				case "text":
					defEl = _.$$("input", T).attr("type", "text");
					defEl.onblur = blurTrim;
					break;
				case "mapping":
					new (function() {
						var mDef = def;
						var mapTb = _.tb();
						var mapBtnCell = mapTb.insert(-1).insert(-1).s("verticalAlign:top").attr("colSpan", 4);
						var mapSpan = _.$$("span", T).lh(18).s("cursor:pointer;color:#0000FF").txt("添加映射元素").unSel();

						mapSpan.onclick = function() {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-" + typeCode); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-" + typeCode); // 设定元素名称;
							valInput.onblur = blurTrim;
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						};

						mapBtnCell.$(mapSpan);
						defCell.$(mapTb);
					});
					break;
				default:
					break;
				}
				
				if (!defEl) {
					continue;
				}
				defEl.c(DC).attr("name", def.code + "-" + typeCode); // 设定元素名称;

				if (N != def.width) {
					defEl.w(def.width);
				}

				defCell.$(defEl);
			}

			processorSel = _.$$("select", T).w(252);
			processorSel.attr("id", "processorSel"); // 设定处理器选择框id;
			processorSel.attr("name", "processorCode"); // 设定元素名称;

			processorSel.options.add(new Option("--选择服务处理器--", "-1"));
			for (var i = 0; i < serviceProcessors.length; i++) {
				var ps = serviceProcessors[i];
				processorSel.options.add(new Option(ps.name, ps.code));
			}
			processorSel.selectedIndex = 0;

			// 处理器选择改变事件;
			processorSel.onchange = processorChangeEvent;

			var psDefRow = processorSettingTb.insert(-1);
			var psDefTitleCell = psDefRow.insert(-1).c("title").txt("选择服务处理器：");
			var psDefCell = psDefRow.insert(-1).c("content");
			psDefCell.$(processorSel);

			processorSel.onchange();
		};

		serviceTypeSel.onchange = function() {
			serviceTypeChangeEvent();
		};

		var typeRow = typeSettingTb.insert(-1);
		var typeTitleCell = typeRow.insert(-1).c("title").txt("数据接入类型：");
		var typeCell = typeRow.insert(-1).c("content");
		typeCell.$(serviceTypeSel);

		// 初始化修改;
		var initUpdate = function(service) {
			updateCodeInput.value = service.code;
			codeInput.value = service.code;
			nameInput.value = service.name;
			autoStartupSel.value = service.autoStartup ? "1" : "0";

			var hd = service.serviceHandle;
			var sType = hd.serviceType;
			serviceTypeSel.value = sType.code;

			sTypeParams = hd.serviceTypeParams; // 初始化服务类型参数;
			for (var i = 0; i < sTypeParams.length; i++) {
				var sTypeParam = sTypeParams[i];
				var def = sTypeParam.definition;

				var defRow = typeSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");
				
				// add by wx
				var defEl = N;
				switch(def.type) {
				case "text":
					defEl = _.$$("input", T).attr("type", "text");
					defEl.onblur = blurTrim;
					defEl.value = sTypeParam.value;
					break;
				case "mapping":
					new (function() {
						var mDef = def;
						var mapTb = _.tb();
						var mapBtnCell = mapTb.insert(-1).insert(-1).s("verticalAlign:top").attr("colSpan", 4);
						var mapSpan = _.$$("span", T).lh(18).s("cursor:pointer;color:#0000FF").txt("添加映射元素").unSel();

						mapSpan.onclick = function() {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-" + typeCode); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-" + typeCode); // 设定元素名称;
							valInput.onblur = blurTrim;
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						};

						mapBtnCell.$(mapSpan);
						defCell.$(mapTb);

						var keys = sTypeParam.keys, values = sTypeParam.values;
						for (var j = 0; j < keys.length; j++) {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-" + sType.code); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyInput.value = keys[j];
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-" + sType.code); // 设定元素名称;
							valInput.onblur = blurTrim;
							valInput.value = values[j];
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						}
					});
					break;
				default:
					break;
				}

				if (!defEl) {
					continue;
				}

				defEl.c(DC).attr("name", def.code + "-" + sType.code); // 设定元素名称;

				if (N != def.width) {
					defEl.w(def.width);
				}

				defCell.$(defEl);
			}

			var processor = hd.serviceProcessor;
			if (!processor) {
				return;
			}

			processorSel = _.$$("select", T).w(252);
			processorSel.attr("id", "processorSel"); // 设定处理器选择框id;
			processorSel.attr("name", "processorCode"); // 设定元素名称;

			processorSel.options.add(new Option("--选择服务处理器--", "-1"));
			for (var i = 0; i < serviceProcessors.length; i++) {
				var ps = serviceProcessors[i];
				processorSel.options.add(new Option(ps.name, ps.code));
			}

			var psCode = processor.code;
			var typeCode = sType.code;

			processorSel.value = psCode;

			// 处理器选择改变事件;
			processorSel.onchange = processorChangeEvent;

			var psDefRow = processorSettingTb.insert(-1);
			var psDefTitleCell = psDefRow.insert(-1).c("title").txt("选择服务处理器：");
			var psDefCell = psDefRow.insert(-1).c("content");
			psDefCell.$(processorSel);

			// 初始化处理器外部参数----------------------------------------------------------------;
			var pOuterParams = hd.processorOuterParams;
			for (var i = 0; i < pOuterParams.length; i++) {
				var param = pOuterParams[i];
				var def = param.definition;

				var defRow = processorOuterSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");

				var defEl = N;
				switch (def.type) {
				case "text":
					defEl = _.$$("input", T).attr("autocomplete", "off").attr("type", "text");
					defEl.onblur = blurTrim;
					defEl.value = param.value;
					break;
				case "select":
					defEl = _.$$("select", T);
					for (var j = 0; j < def.options.length; j++) {
						var opt = def.options[j];
						defEl.options.add(new Option(opt.name, opt.value));
					}
					defEl.value = param.value;
					break;
				case "mapping":
					new (function() {
						var mDef = def;
						var mapTb = _.tb();
						var mapBtnCell = mapTb.insert(-1).insert(-1).s("verticalAlign:top").attr("colSpan", 4);
						var mapSpan = _.$$("span", T).lh(18).s("cursor:pointer;color:#0000FF").txt("添加映射元素").unSel();

						mapSpan.onclick = function() {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							valInput.onblur = blurTrim;
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						};

						mapBtnCell.$(mapSpan);
						defCell.$(mapTb);

						var keys = param.keys, values = param.values;
						for (var j = 0; j < keys.length; j++) {
							var mRow = mapTb.insert(-1);

							var keyTitleCell = mRow.insert(-1).s("paddingLeft:0;paddingRight:0").txt("键：");
							var keyCell = mRow.insert(-1).s("paddingRight:10px");
							var keyInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							keyInput.c(DC).attr("name", "key-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							keyInput.onblur = blurTrim;
							keyInput.value = keys[j];
							keyCell.$(keyInput);

							var valTitleCell = mRow.insert(-1).s("paddingRight:0").txt("值：");
							var valCell = mRow.insert(-1).s("paddingRight:10px");
							var valInput = _.$$("input", T).w(jst.gv(mDef.width, 40)).attr("type", "text");
							valInput.c(DC).attr("name", "val-" + mDef.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;
							valInput.onblur = blurTrim;
							valInput.value = values[j];
							valCell.$(valInput);

							var delCell = mRow.insert(-1);
							var delSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
							delSpan.onclick = function() {
								mapTb.deleteRow(delSpan.parentNode.parentNode.rowIndex);
							};
							delCell.$(delSpan);
						}
					});
					break;
				default:
					break;
				}

				if (!defEl) {
					continue;
				}

				defEl.c(DC).attr("name", def.code + "-outer-" + psCode + "-" + typeCode); // 设定元素名称;

				if (N != def.width) {
					defEl.w(def.width);
				}

				defCell.$(defEl);
			}

			// 初始化处理器内部参数----------------------------------------------------------------;
			var pInnerParams = hd.processorInnerParams;
			for (var i = 0; i < pInnerParams.length; i++) {
				var param = pInnerParams[i];
				var def = param.definition;

				var defRow = processorInnerSettingTb.insert(-1);
				var defTitleCell = defRow.insert(-1).c("title").txt(def.name + "：");
				var defCell = defRow.insert(-1).c("content");

				var defEl = N;
				switch (def.type) {
				case "text":
					defEl = _.$$("input", T).attr("autocomplete", "off").attr("type", "text");
					defEl.onblur = blurTrim;
					break;
				case "select":
					defEl = _.$$("select", T);
					for (var j = 0; j < def.options.length; j++) {
						var opt = def.options[j];
						defEl.options.add(new Option(opt.name, opt.value));
					}
					defEl.selectedIndex = 0;
					break;
				default:
					break;
				}

				if (!defEl) {
					continue;
				}

				defEl.c(DC).attr("name", def.code + "-inner-" + psCode + "-" + typeCode); // 设定元素名称;
				if (N != def.width) {
					defEl.w(def.width);
				}
				defEl.value = param.value;

				defCell.$(defEl);
			}
		};

		// --------------保存服务---------------;
		var saveService = function() {
			// --BEGIN--校验---------------------------------------------;
			var updateServiceCode = updateCodeInput.value;

			var serviceCode = codeInput.value;
			if ("" == serviceCode) {
				alert("错误：请输入接入服务编码");
				return;
			}

			var serviceName = nameInput.value;
			if ("" == serviceName) {
				alert("错误：请输入接入服务名称");
				return;
			}

			var serviceTypeCode = serviceTypeSel.value;
			if ("-1" == serviceTypeCode) {
				alert("错误：没有选择服务类型");
				return;
			}

			var serviceType = N;
			for (var i = 0; i < serviceTypes.length; i++) {
				var t = serviceTypes[i];
				if (serviceTypeCode == t.code) {
					serviceType = t;
					break;
				}
			}

			if (!serviceType) {
				alert("错误：服务类型不存在");
				return;
			}

			var psSel = _.$("processorSel");
			if (!psSel) {
				alert("错误：服务处理器元素不存在");
				return;
			}

			var psCode = psSel.value;
			if ("-1" == psCode) {
				alert("错误：没有选择服务处理器");
				return;
			}

			var ps = N;
			for (var i = 0; i < serviceProcessors.length; i++) {
				var processor = serviceProcessors[i];
				if (psCode == processor.code) {
					ps = processor;
					break;
				}
			}

			if (!ps) {
				alert("错误：服务处理器不存在");
				return;
			}

			var paramEls = editArea.clsEls(DC);

			for (var i = 0; i < paramEls.length; i++) {
				var e = paramEls[i];
				if ("" == e.value || N == e.value) {
					alert("错误：定义参数值不能为空");
					return;
				}
			}
			// --END--校验-----------------------------------------------;

			// 构造请求数据;
			var data = ("updateServiceCode=" + encodeURIComponent(updateServiceCode));
			data += ("&serviceCode=" + encodeURIComponent(serviceCode));
			data += ("&serviceName=" + encodeURIComponent(serviceName));
			data += ("&autoStartup=" + encodeURIComponent(autoStartupSel.value));
			data += ("&serviceTypeCode=" + encodeURIComponent(serviceTypeCode));
			data += ("&processorCode=" + encodeURIComponent(psCode));
			for (var i = 0; i < paramEls.length; i++) {
				var e = paramEls[i];
				data += ("&" + e.name + "=" + encodeURIComponent(e.value));
			}

			var saveServiceReq = new jst.util.SimpleAsyncRequest({
				async : F,
				method : "post",
				url : ctx + "/service/async/save.html",
				data : data,
				finishEvent : function(rq) {
					var result = jst.exec(rq.responseText);
					if (0 == result.status) {
						editDialog.close();
						window.location.reload(T);
					} else {
						alert(result.message);
					}
				},
				errorEvent : function() {
					alert("错误：请求操作时出现异常");
				}
			});
			saveServiceReq.send();
		};

		var editDialog = new jst.ui.FlatDialog({
			title : "",
			height : 700,
			width : 600,
			over : T,
			resize : "enable",
			buttons : [ {
				text : "保存服务",
				event : function() {
					saveService();
				}
			} ]
		});
		editDialog.setEl(editArea);

		editDialog.setOnBeforeOpen(function() {
			updateCodeInput.value = "";
			codeInput.value = "";
			nameInput.value = "";
			autoStartupSel.selectedIndex = 0;
			serviceTypeSel.selectedIndex = 0;
			processorSel = N;
			serviceTypeSel.onchange();
		});

		// 添加服务初始化;
		r.addServiceInit = function() {
			editDialog.setTitle("添加接入服务");
			editDialog.open();
		};

		r.updateServiceInit = function(service) {
			if (!service) {
				alert("错误：无法初始化修改服务，服务对象为空");
			}
			editDialog.setTitle("修改接入服务");
			editDialog.open();
			initUpdate(service);
		};
	};

	var editSetting = new EditSetting(); // 编辑服务设置对象;

	// ------------------------------------------加载服务列表------------------------------------------;
	var listTb = _.tb(_.$("listTb"));

	// 启动服务等待;
	var startupWB = new WB({
		title : "启动服务",
		minWaitingTime : 500,
		content : "正在启动服务，请稍后..."
	});

	// 停止服务等待;
	var shutdownWB = new WB({
		title : "停止服务",
		minWaitingTime : 500,
		content : "正在停止服务，请稍后..."
	});

	var updateWB = new WB({
		title : "修改服务",
		minWaitingTime : 500,
		content : "正在获取服务，请稍后..."
	});

	// 读取服务列表;
	var loadServicesReq = new jst.util.SimpleAsyncRequest({
		async : F,
		method : "post",
		url : ctx + "/service/async/loadList.html",
		finishEvent : function(rq) {
			var services = jst.exec(rq.responseText);
			for (var i = 0; i < services.length; i++) {
				new (function() {
					var service = services[i];

					var row = listTb.insert(-1);

					row.insert(-1).txt(i + 1);
					row.insert(-1).txt(service.code);
					row.insert(-1).txt(service.name);
					row.insert(-1).txt(service.autoStartup ? "是" : "否");
					var runStatusCell = row.insert(-1).s("color:" + (service.run ? "#00AA00" : "#FF0000")).txt(service.run ? "正在运行" : "未启动");

					var ctrlCell = row.insert(-1);

					var runCtrlSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF");
					runCtrlSpan.runStatus = service.run;
					runCtrlSpan.txt(runCtrlSpan.runStatus ? "停止" : "启动").unSel();
					runCtrlSpan.onclick = function() {
						if (this.runStatus) { // 正在运行，停止操作;
							if (confirm("确实要停止服务 [ " + service.name + " ] 吗？")) {
								var shutdownServiceReq = new jst.util.SimpleAsyncRequest({ // 停止服务请求;
									async : T,
									method : "post",
									url : ctx + "/service/async/shutdown.html",
									data : "code=" + service.code,
									finishEvent : function(rq) {
										shutdownWB.close(function() {
											var result = jst.exec(rq.responseText);
											if (0 == result.status) {
												runStatusCell.s("color:#FF0000").txt("未启动");
												runCtrlSpan.runStatus = F;
												runCtrlSpan.txt("启动");
											} else {
												alert(result.message);
											}
										});
									},
									errorEvent : function() {
										shutdownWB.close(function() {
											alert("错误：请求操作时出现异常");
										});
									}
								});
								shutdownServiceReq.send();
								shutdownWB.open();
							}
						} else { // 未启动，启动操作;
							var startupServiceReq = new jst.util.SimpleAsyncRequest({ // 启动服务请求;
								async : T,
								method : "post",
								url : ctx + "/service/async/startup.html",
								data : "code=" + service.code,
								finishEvent : function(rq) {
									startupWB.close(function() {
										var result = jst.exec(rq.responseText);
										if (0 == result.status) {
											runStatusCell.s("color:#00AA00").txt("正在运行");
											runCtrlSpan.runStatus = T;
											runCtrlSpan.txt("停止");
										} else {
											alert(result.message);
										}
									});
								},
								errorEvent : function() {
									startupWB.close(function() {
										alert("错误：请求操作时出现异常");
									});
								}
							});
							startupServiceReq.send();
							startupWB.open();
						}
					};

					var sp1 = _.$$("span", T).txt(" ");

					var updateSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("修改").unSel();
					updateSpan.onclick = function() {
						var loadServiceReq = new jst.util.SimpleAsyncRequest({ // 修改前获取服务请求;
							async : T,
							method : "post",
							url : ctx + "/service/async/load.html",
							data : "code=" + service.code,
							finishEvent : function(rq) {
								updateWB.close(function() {
									var result = jst.exec(rq.responseText);
									if (0 == result.status) {
										editSetting.updateServiceInit(result.resultObj);
									} else {
										alert("错误：请求操作时出现异常");
									}
								});
							},
							errorEvent : function() {
								alert("错误：请求操作时出现异常");
							}
						});
						loadServiceReq.send();
						updateWB.open();
					};

					var sp2 = _.$$("span", T).txt(" ");

					var deleteSpan = _.$$("span", T).s("cursor:pointer;color:#0000FF").txt("删除").unSel();
					deleteSpan.onclick = function() {
						if (confirm("确实要删除服务 [ " + service.name + " ] 吗？")) {
							var removeServiceReq = new jst.util.SimpleAsyncRequest({ // 删除服务请求;
								async : F,
								method : "post",
								url : ctx + "/service/async/remove.html",
								data : "code=" + service.code,
								finishEvent : function(rq) {
									var result = jst.exec(rq.responseText);
									if (0 == result.status) {
										window.location.reload(T);
									} else {
										alert(result.message);
									}
								},
								errorEvent : function() {
									alert("错误：请求操作时出现异常");
								}
							});
							removeServiceReq.send();
						}
					};

					ctrlCell.$(runCtrlSpan).$(sp1).$(updateSpan).$(sp2).$(deleteSpan);
				});
			}
		},
		errorEvent : function() {
			alert("错误：请求操作时出现异常");
		}
	});
	loadServicesReq.send();

	_.$("addService").onclick = function() {
		editSetting.addServiceInit();
		addService.blur();
	};
});
