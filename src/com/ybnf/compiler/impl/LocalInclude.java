package com.ybnf.compiler.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ybnf.compiler.Include;

public class LocalInclude extends Include {
	private String result = "";
	private String filename;
	private String charset;

	public LocalInclude(String filename, String charset) throws Exception {
		this.filename = filename;
		this.charset = charset;
	}

	@Override
	public String readContent() throws IOException {
		if (result.equals("")) {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, charset);
			BufferedReader bReader = new BufferedReader(isr);
			StringBuilder sBuilder = new StringBuilder("");
			String buf;
			while ((buf = bReader.readLine()) != null) {
				sBuilder.append(buf);
			}
			bReader.close();
			isr.close();
			fis.close();
			result = sBuilder.toString();
		}
		return result;
	}

}
