var clickPhoto = "/images/left_nav/left_07.gif";
var normalPhoto = "/images/left_nav/left_12.gif";
var secondClickPhoto = "/images/left_nav/left_08.gif";
var secondNormalPhoto = "/images/left_nav/left_09.gif";
var currentMenuStr = "";

//初始化
function InitLeftMenu () {
	if (currentTopLeftMenu>0) {
		document.getElementById("LeftMenu_"+currentTopLeftMenu).style.background = "url(" + clickPhoto + ")";
		if (currentTopSonLeftMenu > -1) {
			document.getElementById("LeftMenu_"+currentTopLeftMenu+"_son").style.display = "";
			if (currentTopSonLeftMenu > 0) {
				document.getElementById("LeftMenu_"+currentTopLeftMenu+"_son_"+currentTopSonLeftMenu).style.background = "url(" + secondClickPhoto + ") no-repeat";
			}
		}
	}
}

//一级菜单
function FirstLeftMenu (LeftMenuID, LeftMenuSon, URL) {
	//其它部分不可见
	var topLeftMenuString = LeftMenuID.substring(0, LeftMenuID.lastIndexOf("_")+1);
	if (currentMenuStr != LeftMenuID) {
		for (var i=1; i<=topLeftMenuNum; i++) {
			if (topLeftMenuString+i != LeftMenuID) {
				document.getElementById(topLeftMenuString+i).style.background = "url(" + normalPhoto + ")";
				if (currentTopSonLeftMenu>-1) {
					document.getElementById(topLeftMenuString + i + "_son").style.display = "none";
				}
			}
		}
		//选中部分可见
		topLeftMenu = document.getElementById(LeftMenuID);
		sonLeftMenu = document.getElementById(LeftMenuSon);
		if (sonLeftMenu.style.display != "") {
			topLeftMenu.style.background =  "url(" + clickPhoto + ")";
			sonLeftMenu.style.display = "";
		}else {
			topLeftMenu.style.background = "url(" + normalPhoto + ")";
			sonLeftMenu.style.display = "none";
		}
		currentMenuStr = LeftMenuID;
	}
	//超连接
	if (URL != "" && URL != undefined) {
		JumpUrl (URL);	
	}
}
//二级菜单
function SecondLeftMenu (LeftMenuID, URL, LeftMenuNum) {
	var topLeftMenuString = LeftMenuID.substring(0, LeftMenuID.lastIndexOf("_")+1);
	var sonLeftMenu = document.getElementById(LeftMenuID);
	for (var i=1; i<=LeftMenuNum; i++) {
		document.getElementById(topLeftMenuString+i).style.background = "url(" + secondNormalPhoto + ")";
	}
	sonLeftMenu.style.background = "url(" + secondClickPhoto + ") no-repeat";
	//超连接
	if (URL != "" && URL != undefined) {
		JumpUrl (URL);	
	}
}

//转到超连接
function JumpUrl (URL) {
	window.open(URL, target="_self");
}