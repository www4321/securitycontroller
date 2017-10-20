//定义数据
var menuA = new Array("menu0", "menu1", "menu2", "menu3", "menu4", "menu5", "menu6");
var menuPhotoB = new Array("/images/top_nav/topNav_left_bg.gif", "/images/top_nav/topNav_1_on.gif", "/images/top_nav/topNav_2_on.gif", "/images/top_nav/topNav_3_on.gif", "/images/top_nav/topNav_4_on.gif", "/images/top_nav/topNav_5_on.gif", "/images/top_nav/topNav_6_on.gif");
var menuPhotoA = new Array("/images/top_nav/topNav_backHome.gif", "/images/top_nav/topNav_1_off.gif", "/images/top_nav/topNav_2_off.gif", "/images/top_nav/topNav_3_off.gif", "/images/top_nav/topNav_4_off.gif", "/images/top_nav/topNav_5_off.gif", "/images/top_nav/topNav_6_off.gif");
var initID = menuA[startID];
var initImg = menuPhotoB[startID];
//初始化
function InitMenu () {
	for (var i=1; i<=6; i++) {
		document.images[menuA[i]].src=menuPhotoA[i];
	}
	
	if(initID != 'menu0'){  
		document.images['menu0'].src='/images/top_nav/topNav_backHome.gif';
	}else{
		document.images['menu0'].src='/images/top_nav/topNav_left_bg.gif';
	}    
	document.images[menuA[startID]].src=menuPhotoB[startID];
}
//鼠标点击
function navchange(img,MenuId)
{
	for (var i=0; i<=6; i++) {
		document.images[menuA[i]].src=menuPhotoA[i];
	}
	document.images[MenuId].src=img;
	initImg=img;
	initID=MenuId;
}	
//鼠标移出
function checkChange (img, menuID) {
	if (menuID == initID) {
		document.images[menuID].src=initImg;
	}else {
		document.images[menuID].src=img;
	}
}
//回到首页
function returnMain(){
	document.images['menu0'].src='/images/top_nav/topNav_left_bg.gif';
	for (var i=1; i<=6; i++) {
		document.images[menuA[i]].src=menuPhotoA[i];
	}
	initImg='/images/top_nav/topNav_left_bg.gif';
	initID='menu0';
}
