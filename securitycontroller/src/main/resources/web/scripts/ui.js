/*
 Author : BUPT
 Datetime : 2008-12-8
 Last Eidt: 2008-12-10

 Using:
	UI.tip();	//Create Title Tip
	UI.select();	//Set Style For Select:<span class="cmn_select"><select/></span>
	UI.gotop(); <=> UI.gotop('id');	//Create Gotop Tool
*/

String.prototype.hasString = function(o) {	//If Has String
	if (typeof o == 'object') {
		for (var i=0;i<o.length;i++) {
			if (!this.hasString(o[i])) return false;
		}
		return true;
	}
	else if (this.indexOf(o)!=-1) return true;
}
var UI = {
	tip : function() {
		this.Tip.build();
	},
	select : function(n) {
		this.Select.build(n);
	},
	gotop : function(n) {
		this.Gotop.build(n);
	},
	Attr : function(o,n,v) {
		o.setAttribute(n,v);
	},
	DC : function(n) {	//Dom Create Element
		return document.createElement(n);
	},
	EA : function (o,n,f) {
		if(o.addEventListener) {
			o.addEventListener(n,f,false);
			return true;
		}
		else if(o.attachEvent) {
			var r=o.attachEvent("on"+n,f);
			//UI.EventCache.add(o,evType,fn);
			return r;
		}
		else return false;
	},
	ER : function (o,n,f) {
		if(o.removeEventListener) {
			o.removeEventListener(n,f,false);
			return true;
		}
		else if(o.detachEvent) {
			var r=o.detachEvent("on"+n,f);
			return r;
		}
		else return false;
	},
	ET : function(e) {	//Event Target
		return e.target||e.srcElement;
	},
	G : function(n) {
		return document.getElementById(n);
	},
	GT : function(o,n) {
		return o.getElementsByTagName(n);
	},
	GC : function (n,o) {	//getElementByClassName -> UI.GC('a.hide.red')
		var o = (o) ? o : document,t,l,el = [];
		var arr = n.split('.');
		t = arr[0] == ''? '*':arr[0];
		arr.shift();
		l = this.GT(o,t);
		for(var i = 0;i < l.length;i++) {
			if(l[i].className.hasString(arr)) el.push(l[i]);
		}
		return el.length > 0 ? el : false;
	},
	GS : function(o,n) {	//Get Style
		if (o.currentStyle) {
			return o.currentStyle[n];
		}
		else if (window.getComputedStyle) {
			n = n.replace (/([A-Z])/g, "-$1");
			n = n.toLowerCase ();
			return window.getComputedStyle (o, null).getPropertyValue(n);
		}
		return null;
	},
	Browser : (function(){
		var b = {},i = navigator.userAgent;
		b.ie6 = i.hasString('MSIE 6');
		b.ie = i.hasString('MSIE');
		b.opera = i.hasString('Opera');
		b.safari = i.hasString('WebKit');
		return b;
	})()
}
UI.Tip = {	//Title Tip
	wrap : UI.DC('div'),
	build : function() {
		this.wrap.className = 'cmn_tip';
		this.wrap.innerHTML = '<iframe src="about:blank" style="display:none;position:absolute;z-index:-1;"></iframe><div class="cont"></div>';
		this.cover = UI.GT(this.wrap,'iframe')[0];
		this.cont = UI.GT(this.wrap,'div')[0];
		this.wrap.appendChild(this.cont);
		document.body.appendChild(this.wrap);
		UI.EA(document,'mouseover',function(e) {
			e = window.event || e;
			var o = UI.ET(e);
			if (o.title) {
				var css = UI.Tip.wrap.style,html=document.documentElement,body=document.body,W,H,T,L;
				W = html.clientWidth;
				H = html.clientHeight;
				T = html.scrollTop||body.scrollTop;
				L = html.scrollLeft||body.scrollLeft;
				UI.Tip.cont.innerHTML = o.title;
				o.title = '';
				css.cssText = '';
				e.clientY < H/2 ? css.top = e.clientY + T + 'px':css.bottom = H - e.clientY - (UI.Browser.ie6 ? 0:T) + 'px';
				e.clientX < W/2 ? css.left = e.clientX + L + 12 + 'px':css.right = W - e.clientX - L + 12 + 'px';
				UI.Tip.show();
				if (UI.Browser.ie6) {
					var cover = UI.Tip.cover.style,cont = UI.Tip.cont;
					cover.display = 'block';
					cover.width = cont.offsetWidth+'px';
					cover.height = cont.offsetHeight+'px';
				}
			}
		});
		UI.EA(document,'mouseout',function(e) {
			if (UI.Tip.cont.innerHTML) {
				e = window.event || e;
				var o = UI.ET(e);
				o.title = UI.Tip.cont.innerHTML;
				UI.Tip.cont.innerHTML = '';
				UI.Tip.hide();
			}
		});
	},
	show : function(e) {
		this.wrap.style.display = 'block';
	},
	hide : function() {
		this.wrap.style.display = 'none';
	}
}
UI.Select = {
	build : function(n) {
		var s = UI.GT(!n ? document : UI.G(n),'select');
		for (var i=0;i<s.length;i++) {
			var o = s[i];
			var p = o.parentNode;
			if (p.className.hasString('cmn_select')) {
				p.insertBefore(UI.DC('ins'),o);
				UI.GT(p,'ins')[0].innerHTML = s[i].options[0].innerHTML;
			}
			o.onchange = function() {
				this.previousSibling.innerHTML = this.options[this.selectedIndex].innerHTML;
			}
		}
	}
}
UI.Gotop = {
	title : '���ض���',
	className : 'gotop',
	text : 'Top',
	body : UI.DC('a'),
	_delay : null,
	build : function(id) {
		this.body.className = this.className;
		this.body.title = this.title;
		this.body.innerHTML = this.text;
		this.body.href = '#' + (id||'');
		document.body.appendChild(this.body);
		this.body.onfocus = function(){
			this.blur();
		}
		UI.EA(window,'scroll',function(){
			clearTimeout(UI.Gotop._delay);
			UI.Gotop._delay = setTimeout(function(){
				( window.scrollY || document.documentElement.scrollTop ) < 52 ? UI.Gotop.body.style.display = 'none':UI.Gotop.body.style.display = 'block';
			},50);
		});
	}
}
if (UI.Browser.ie6) {
	try{
		document.execCommand("BackgroundImageCache",false,true);
	}catch(e){}
}