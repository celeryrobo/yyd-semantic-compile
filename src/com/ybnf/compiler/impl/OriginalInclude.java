package com.ybnf.compiler.impl;

import com.ybnf.compiler.Compiler;
import com.ybnf.compiler.Include;

public class OriginalInclude extends Include {

	@Override
	public Compiler compiler() throws Exception {
		Compiler compiler = new YbnfCompiler("#YBNF 1.0 utf8;");
		compiler.mergeGrammar(this.readContent());
		return compiler;
	}

	@Override
	public String readContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<_yyd_han_> = #'\\p{script=Han}'\n<_yyd_char_> = #'\\w'\n<_yyd_ch_> = _yyd_han_|_yyd_char_\n");
		sb.append("<_yyd_wstr_> = _yyd_han_+\n<_yyd_str_> = _yyd_char_+\n<_yyd_string_> = _yyd_ch_+\n");
		sb.append("<_yyd_digital_> = #'\\d'\n<_yyd_num_> = _yyd_digital_+\n");
		sb.append(
				"<_yyd_lxkh_> = '('|'（'\n<_yyd_rxkh_> = ')'|'）'\n<_yyd_lzkh_> = '['|'【'\n<_yyd_rzkh_> = ']'|'】'\n<_yyd_ldkh_> = '{'\n<_yyd_rdkh_> = '}'\n");
		sb.append("<_yyd_plus_> = '+'\n<_yyd_subtract_> = '-'\n<_yyd_multi_> = '*'|'×'\n<_yyd_division_> = '/'|'÷'\n");
		sb.append("<_yyd_point_> = '.'\n<_yyd_equal_> = '='\n");
		sb.append(
				"<_yyd_period_> = '.'|'。'\n<_yyd_comma_> = ','|'，'\n<_yyd_plaint_> = '!'|'！'\n<_yyd_semicolon_> = ';'|'；'\n<_yyd_colon_> = ':'|'：'\n<_yyd_question_> = '?'|'？'\n");
		sb.append(
				"<_yyd_punctuation_> = _yyd_period_|_yyd_comma_|_yyd_plaint_|_yyd_semicolon_|_yyd_colon_|_yyd_question_\n");
		return sb.toString();
	}

}
