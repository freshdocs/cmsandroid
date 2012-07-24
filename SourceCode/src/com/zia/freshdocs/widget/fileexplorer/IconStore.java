package com.zia.freshdocs.widget.fileexplorer;

import com.zia.freshdocs.R;

public class IconStore {
	public static Integer getIconByExtendsion(String ext){
		String extendsion = ext.toLowerCase();
		
		if(extendsion.endsWith("7z"))return R.drawable.a7z;
		if(extendsion.endsWith("ai"))return R.drawable.ai;
		if(extendsion.endsWith("app"))return R.drawable.app;
		if(extendsion.endsWith("asp"))return R.drawable.asp;
		if(extendsion.endsWith("avi"))return R.drawable.avi;
		
		
		if(extendsion.endsWith("bat"))return R.drawable.bat;
		if(extendsion.endsWith("bmp"))return R.drawable.bmp;
		
		if(extendsion.endsWith("cdr"))return R.drawable.cdr;
		if(extendsion.endsWith("css"))return R.drawable.css;
		if(extendsion.endsWith("csv"))return R.drawable.csv;
		
		if(extendsion.endsWith("dmg"))return R.drawable.dmg;
		if(extendsion.endsWith("doc"))return R.drawable.doc;
		if(extendsion.endsWith("docx"))return R.drawable.docx;
		if(extendsion.endsWith("dwf"))return R.drawable.dwf;
		if(extendsion.endsWith("dwg"))return R.drawable.dwg;
		
		if(extendsion.endsWith("eps"))return R.drawable.eps;
		if(extendsion.endsWith("exe"))return R.drawable.exe;
		
		if(extendsion.endsWith("fla"))return R.drawable.fla;
		if(extendsion.endsWith("flv"))return R.drawable.flv;
		if(extendsion.endsWith("fnt"))return R.drawable.fnt;
		if(extendsion.endsWith("fon"))return R.drawable.fon;
		
		if(extendsion.endsWith("gdoc"))return R.drawable.gdoc;
		if(extendsion.endsWith("gif"))return R.drawable.gif;
		if(extendsion.endsWith("gpres"))return R.drawable.gpres;
		if(extendsion.endsWith("gsheet"))return R.drawable.gsheet;
		
		if(extendsion.endsWith("htm"))return R.drawable.htm;
		if(extendsion.endsWith("html"))return R.drawable.html;
		
		if(extendsion.endsWith("indd"))return R.drawable.indd;
		if(extendsion.endsWith("iso"))return R.drawable.iso;
		
		if(extendsion.endsWith("jpg"))return R.drawable.jpg;
		if(extendsion.endsWith("jpeg"))return R.drawable.jpeg;
		if(extendsion.endsWith("js"))return R.drawable.js;
		if(extendsion.endsWith("jsp"))return R.drawable.jsp;
		
		if(extendsion.endsWith("mid"))return R.drawable.mid;
		if(extendsion.endsWith("midi"))return R.drawable.midi;
		if(extendsion.endsWith("mind"))return R.drawable.mind;
		if(extendsion.endsWith("mm"))return R.drawable.mm;
		if(extendsion.endsWith("mmap"))return R.drawable.mmap;
		if(extendsion.endsWith("mov"))return R.drawable.mov;
		if(extendsion.endsWith("mp3"))return R.drawable.mp3;
		if(extendsion.endsWith("mp4"))return R.drawable.mp4;
		if(extendsion.endsWith("mpeg"))return R.drawable.mpeg;
		if(extendsion.endsWith("mpg"))return R.drawable.mpg;
		if(extendsion.endsWith("mpp"))return R.drawable.mpp;
		if(extendsion.endsWith("msg"))return R.drawable.msg;
		
		if(extendsion.endsWith("odp"))return R.drawable.odp;
		if(extendsion.endsWith("ods"))return R.drawable.ods;
		if(extendsion.endsWith("odt"))return R.drawable.odt;
		if(extendsion.endsWith("ogg"))return R.drawable.ogg;
		if(extendsion.endsWith("otf"))return R.drawable.otf;
		
		if(extendsion.endsWith("pdd"))return R.drawable.pdd;
		if(extendsion.endsWith("pdf"))return R.drawable.pdf;
		if(extendsion.endsWith("php"))return R.drawable.php;
		if(extendsion.endsWith("png"))return R.drawable.png;
		if(extendsion.endsWith("ppt"))return R.drawable.ppt;
		if(extendsion.endsWith("pptx"))return R.drawable.pptx;
		if(extendsion.endsWith("psb"))return R.drawable.psb;
		if(extendsion.endsWith("psd"))return R.drawable.psd;
		
		if(extendsion.endsWith("qbb"))return R.drawable.qbb;
		if(extendsion.endsWith("qfx"))return R.drawable.qfx;
		if(extendsion.endsWith("qif"))return R.drawable.qif;
		if(extendsion.endsWith("qt"))return R.drawable.qt;
		
		if(extendsion.endsWith("ra"))return R.drawable.ra;
		if(extendsion.endsWith("ram"))return R.drawable.ram;
		if(extendsion.endsWith("rar"))return R.drawable.rar;
		if(extendsion.endsWith("raw"))return R.drawable.raw;
		if(extendsion.endsWith("rtf"))return R.drawable.rtf;
		
		if(extendsion.endsWith("sql"))return R.drawable.sql;
		if(extendsion.endsWith("svg"))return R.drawable.svg;
		if(extendsion.endsWith("swf"))return R.drawable.swf;
		
		if(extendsion.endsWith("tif"))return R.drawable.tif;
		if(extendsion.endsWith("tiff"))return R.drawable.tiff;
		if(extendsion.endsWith("tt"))return R.drawable.tt;
		if(extendsion.endsWith("ttf"))return R.drawable.ttf;
		if(extendsion.endsWith("txt"))return R.drawable.txt;
		
		if(extendsion.endsWith("vsd"))return R.drawable.vsd;
		
		if(extendsion.endsWith("wav"))return R.drawable.wav;
		if(extendsion.endsWith("wma"))return R.drawable.wma;
		if(extendsion.endsWith("wmv"))return R.drawable.wmv;
		if(extendsion.endsWith("wpd"))return R.drawable.wpd;
		
		if(extendsion.endsWith("xhtml"))return R.drawable.xhtml;
		if(extendsion.endsWith("xls"))return R.drawable.xls;
		if(extendsion.endsWith("xlsx"))return R.drawable.xlsx;
		if(extendsion.endsWith("xml"))return R.drawable.xml;
		
		if(extendsion.endsWith("zip"))return R.drawable.zip;
		
		if(extendsion.endsWith("folder"))return R.drawable.folder;
		
		if(extendsion.endsWith("root"))return R.drawable.tree32;
		
		return R.drawable.generic;
	}
}
