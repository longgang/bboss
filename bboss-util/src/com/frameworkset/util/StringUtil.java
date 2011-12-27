/*****************************************************************************
 *                                                                           *
 *  This file is part of the tna framework distribution.                     *
 *  Documentation and updates may be get from  biaoping.yin the author of    *
 *  this framework							     *
 *                                                                           *
 *  Sun Public License Notice:                                               *
 *                                                                           *
 *  The contents of this file are subject to the Sun Public License Version  *
 *  1.0 (the "License"); you may not use this file except in compliance with *
 *  the License. A copy of the License is available at http://www.sun.com    *
 *                                                                             *
 *  The Original Code is tag. The Initial Developer of the Original          *
 *  Code is biaoping yin. Portions created by biaoping yin are Copyright     *
 *  (C) 2000.  All Rights Reserved.                                          *
 *                                                                           *
 *  GNU Public License Notice:                                               *
 *                                                                           *
 *  Alternatively, the contents of this file may be used under the terms of  *
 *  the GNU Lesser General Public License (the "LGPL"), in which case the    *
 *  provisions of LGPL are applicable instead of those above. If you wish to *
 *  allow use of your version of this file only under the  terms of the LGPL *
 *  and not to allow others to use your version of this file under the SPL,  *
 *  indicate your decision by deleting the provisions above and replace      *
 *  them with the notice and other provisions required by the LGPL.  If you  *
 *  do not delete the provisions above, a recipient may use your version of  *
 *  this file under either the SPL or the LGPL.                              *
 *                                                                           *
 *  biaoping.yin (yin-bp@163.com)                                            *
 *  Author of Learning Java 						     					 *
 *                                                                           *
 *****************************************************************************/
package com.frameworkset.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.sql.Blob;
import java.sql.Clob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.StringSubstitution;
import org.frameworkset.util.CollectionUtils;
import org.frameworkset.util.ObjectUtils;

import sun.security.action.GetPropertyAction;

/**
 * To change for your class or interface DAO��VOObject String������PO��������ת��������.
 * 
 * @author biaoping.yin
 * @version 1.0
 */

public class StringUtil  {
	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// ���ų���
	public static final String COMMA = ",";

	// �մ�����
	public static final String BLANK = "";

	/**
	 * A constant passed to the {@link #split split()}methods indicating that
	 * all occurrences of a pattern should be used to split a string.
	 */
	public static final int SPLIT_ALL = 0;

	/**
	 * ���µı�����dontNeedEncoding��dfltEncName��caseDiff �Ǵ�jdk 1.4 java.net.URLEncoder
	 * ����ֲ����
	 */

	private static BitSet dontNeedEncoding;

	private static String dfltEncName = null;

	static final int caseDiff = ('a' - 'A');

	static {

		/*
		 * The list of characters that are not encoded has been determined as
		 * follows:
		 * 
		 * RFC 2396 states: ----- Data characters that are allowed in a URI but
		 * do not have a reserved purpose are called unreserved. These include
		 * upper and lower case letters, decimal digits, and a limited set of
		 * punctuation marks and symbols.
		 * 
		 * unreserved = alphanum | mark
		 * 
		 * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
		 * 
		 * Unreserved characters can be escaped without changing the semantics
		 * of the URI, but this should not be done unless the URI is being used
		 * in a context that does not allow the unescaped character to appear.
		 * -----
		 * 
		 * It appears that both Netscape and Internet Explorer escape all
		 * special characters from this list with the exception of "-", "_",
		 * ".", "*". While it is not clear why they are escaping the other
		 * characters, perhaps it is safest to assume that there might be
		 * contexts in which the others are unsafe if not escaped. Therefore, we
		 * will use the same list. It is also noteworthy that this is consistent
		 * with O'Reilly's "HTML: The Definitive Guide" (page 164).
		 * 
		 * As a last note, Intenet Explorer does not encode the "@" character
		 * which is clearly not unreserved according to the RFC. We are being
		 * consistent with the RFC in this matter, as is Netscape.
		 */

		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			dontNeedEncoding.set(i);
		}
		dontNeedEncoding.set(' '); /*
									 * encoding a space to a + is done in the
									 * encode() method
									 */
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('_');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('*');

		dfltEncName = (String) AccessController
				.doPrivileged(new GetPropertyAction("file.encoding"));
	}

	/**
	 * ��һ���ַ������ݶ��ŷֲ�
	 */
	public static String[] split(String s) {
		return split(s, COMMA);
	}

	/**
	 * ���ַ������ݸ����ָ����ֲ�
	 */
	public static String[] split(String s, String delimiter) {
		return split(s, delimiter, true);

		// if (s == null || delimiter == null) {
		// return new String[0];
		// }
		//
		// s = s.trim();
		//
		// if (!s.endsWith(delimiter)) {
		// s += delimiter;
		// }
		//
		// if (s.equals(delimiter)) {
		// return new String[0];
		// }
		//
		// List nodeValues = new ArrayList();
		//
		// if (delimiter.equals("\n") || delimiter.equals("\r")) {
		// try {
		// BufferedReader br = new BufferedReader(new StringReader(s));
		//
		// String line = null;
		//
		// while ((line = br.readLine()) != null) {
		// nodeValues.add(line);
		// }
		//
		// br.close();
		// }
		// catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
		// }
		// else {
		// int offset = 0;
		// int pos = s.indexOf(delimiter, offset);
		//
		// while (pos != -1) {
		// nodeValues.add(s.substring(offset, pos));
		//
		// offset = pos + delimiter.length();
		// pos = s.indexOf(delimiter, offset);
		// }
		// }
		//
		// return (String[])nodeValues.toArray(new String[0]);
	}

	public static String getRealPath(HttpServletRequest request, String path) {
		String contextPath = request.getContextPath();

		if (contextPath == null) {
//			System.out.println("StringUtil.getRealPath() contextPath:"
//					+ contextPath);
			return path;
		}
		if (path == null) {
			return null;
		}
		if (path.startsWith("/") && !path.startsWith(contextPath + "/")) {
			if (!contextPath.equals("/"))
				return contextPath + path;
			else {
				return path;
			}

		} else {
			return path;
		}

	}
	
	public static String getRealPath(String contextPath, String path) {
		
		
		if (contextPath == null || contextPath.equals("")) {
//			System.out.println("StringUtil.getRealPath() contextPath:"
//					+ contextPath);
			return path == null?"":path;
		}
		if (path == null || path.equals("")) {
			
			return contextPath;
		}
		
		contextPath = contextPath.replace('\\', '/');
		path = path.replace('\\', '/');
		if (path.startsWith("/") ) {
			
			if (!contextPath.endsWith("/"))
				return contextPath + path;
			else {
				return contextPath.substring(0,contextPath.length() - 1) + path;
			}

		} else {
			if (!contextPath.endsWith("/"))
				return contextPath + "/" + path;
			else {
				return contextPath + path;
			}
		}

	}

	public static boolean containKey(String[] values, String key) {
		if (values == null || key == null) {
			return false;
		}
		boolean contain = false;
		for (int i = 0; i < values.length; i++) {

			// System.out.println("values[" + i + "]:" + values[i]);
			// System.out.println("key:" + key);
			if (values[i].equals(key)) {
				contain = true;
				break;
			}
		}
		return contain;
	}

	public static String getFormatDate(Date date, String formate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(formate);
		if (date == null) {
			return null;
		}
		return dateFormat.format(date);

	}

	public static Date stringToDate(String date) {
		if (date == null || date.trim().equals("")) {
			return null;
		}

		// date = date.replace('-', '/');
		// SimpleDateFormat format = new SimpleDateFormat();
		try {
			return format.parse(date);
		} catch (ParseException e) {
			return new Date(date);
		}
	}

	public static Date stringToDate(String date, String format_) {
		if (date == null || date.trim().equals("")) {
			return null;
		}

		// date = date.replace('-', '/');
		SimpleDateFormat format = new SimpleDateFormat(format_);
		try {
			return format.parse(date);
		} catch (ParseException e) {
			return new Date(date);
		}
	}

	/**
	 * ��ȡ�ļ�����
	 * 
	 * @param path
	 *            �ļ�·��
	 * @return String
	 */
	public static String getFileName(String path) {
		int index = path.lastIndexOf('/');
		String fileName = "";
		if (index == -1) {
			index = path.lastIndexOf('\\');
		}

		fileName = path.substring(index + 1);
		return fileName;
	}

	public static String getFileName(String prefix, String extension)
			throws UnsupportedEncodingException {
		// prefix = MessageUtility.getValidFileName(prefix);
		// UTF8 URL encoding only works in IE, not Mozilla
		String fileName = URLEncoder.encode(prefix);
		// Bug of IE (http://support.microsoft.com/?kbid=816868)
		// Cannot be more than 150(I don't know the exact number)
		int limit = 150 - extension.length();
		if (fileName.length() > limit) {
			// because the UTF-8 encoding scheme uses 9 bytes to represent a
			// single CJK character
			fileName = URLEncoder.encode(prefix.substring(0, Math.min(prefix
					.length(), limit / 9)));
		}
		return fileName + extension;
	}

	/**
	 * ����������ת�����ַ�������
	 * 
	 * @param dates
	 *            Date[] ��������
	 * @return String[] �ַ�������
	 */
	public static String[] dateArrayTOStringArray(Date[] dates) {
		if (dates == null) {
			return null;
		}
		String[] dates_s = new String[dates.length];
		for (int i = 0; i < dates.length; i++) {
			dates_s[i] = format.format(dates[i]);
		}
		return dates_s;
	}

	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static Date[] stringArrayTODateArray(String[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		Date[] dates = new Date[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			if(dateformat != null)
			{
				try {
					dates[i] = dateformat.parse((dates_s[i]));
				} catch (ParseException e) {
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new Date(dl);
				}
				
			}
			else
			{
				try
				{
					dates[i] = new Date(dates_s[i]);
				}
				catch (Exception e)
				{
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new Date(dl);
				}
			}
			
		}
		return dates;
	}
	
	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static Date[] longArrayTODateArray(long[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		Date[] dates = new Date[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			
			{
				dates[i] = new Date(dates_s[i]);
			}
			
		}
		return dates;
	}

	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static java.sql.Date[] stringArrayTOSQLDateArray(String[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		java.sql.Date[] dates = new java.sql.Date[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			if(dateformat != null)
			{
				try {
					dates[i] = new java.sql.Date(dateformat.parse((dates_s[i]))
					.getTime());
				} catch (ParseException e) {
//					dates[i] = new java.sql.Date(new java.util.Date(dates_s[i])
//					.getTime());
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new java.sql.Date(dl);
				}
				
			}
			else
			{
				try
				{
					dates[i] = new java.sql.Date(new java.util.Date(dates_s[i])
					.getTime());
				}
				catch (Exception e)
				{
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new java.sql.Date(dl);
				}
			}
			
		}
		return dates;
	}
	
	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static java.sql.Date[] longArrayTOSQLDateArray(long[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		java.sql.Date[] dates = new java.sql.Date[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			
			{
				dates[i] = new java.sql.Date(dates_s[i]);
			}
			
		}
		return dates;
	}
	
	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static java.sql.Timestamp[] stringArrayTOTimestampArray(String[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		java.sql.Timestamp[] dates = new java.sql.Timestamp[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			if(dateformat != null)
			{
				try {
					dates[i] = new java.sql.Timestamp(dateformat.parse((dates_s[i]))
					.getTime());
				} catch (ParseException e) {
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new java.sql.Timestamp(dl);
//					dates[i] = new java.sql.Timestamp(new java.util.Date(dates_s[i])
//					.getTime());
				}
				
			}
			else
			{
				try
				{
					dates[i] = new java.sql.Timestamp(new java.util.Date(dates_s[i])
					.getTime());
				}
				catch (Exception e)
				{
					long dl = Long.parseLong(dates_s[i]);
					dates[i] = new java.sql.Timestamp(dl);
				}
			}
		}
		return dates;
	}
	
	/**
	 * ���ַ�������ת������������
	 * 
	 * @param dates
	 *            Date[] �ַ�������
	 * @return String[] ��������
	 */
	public static java.sql.Timestamp[] longArrayTOTimestampArray(long[] dates_s,SimpleDateFormat dateformat) {
		if (dates_s == null) {
			return null;
		}
		java.sql.Timestamp[] dates = new java.sql.Timestamp[dates_s.length];
		for (int i = 0; i < dates_s.length; i++) {
			
			{
				dates[i] = new java.sql.Timestamp(dates_s[i]);
			}
		}
		return dates;
	}

	/**
	 * @param val
	 * @param string
	 * @param string2
	 * @return
	 */
	public static String replace(String val, String string, String string2) {
		// TODO Auto-generated method stub
		return StringUtils.replace(val, string, string2);
	}

	/**
	 * @param val
	 * @param string
	 * @param string2
	 * @return
	 */
	public static String replaceChars(String val, String string, String string2) {
		// TODO Auto-generated method stub
		return StringUtils.replaceChars(val, string, string2);
	}

	/**
	 * �ַ����滻����
	 * 
	 * @param val
	 *            String
	 * @param str1
	 *            String
	 * @param str2
	 *            String
	 * @return String
	 */
	public static String replaceAll(String val, String str1, String str2) {
		return replaceAll(val, str1, str2, true);
	}

	public static String replaceFirst(String val, String str1, String str2) {
		return replaceFirst(val, str1, str2, true);
	}

	public static String replaceFirst(String val, String str1, String str2,
			boolean CASE_INSENSITIVE) {
		String patternStr = str1;

		/**
		 * �����������ʽpatternStr�����øñ���ʽ�봫���sql������ģʽƥ��,
		 * ���ƥ����ȷ�����ƥ���������ȡ�����϶���õ�6���֣���ŵ������в����� ������
		 */

		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern = null;

		try {
			if (CASE_INSENSITIVE) {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.DEFAULT_MASK);
			} else {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.CASE_INSENSITIVE_MASK);
			}
			PatternMatcher matcher = new Perl5Matcher();
			return org.apache.oro.text.regex.Util.substitute(matcher, pattern,
					new StringSubstitution(str2), val);

		} catch (MalformedPatternException e) {
			e.printStackTrace();

			return val;
		}

	}

	public static String replaceAll(String val, String str1, String str2,
			boolean CASE_INSENSITIVE) {
		String patternStr = str1;

		/**
		 * �����������ʽpatternStr�����øñ���ʽ�봫���sql������ģʽƥ��,
		 * ���ƥ����ȷ�����ƥ���������ȡ�����϶���õ�6���֣���ŵ������в����� ������
		 */

		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern = null;

		try {
			if (CASE_INSENSITIVE) {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.DEFAULT_MASK);
			} else {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.CASE_INSENSITIVE_MASK);
			}
			PatternMatcher matcher = new Perl5Matcher();
			return org.apache.oro.text.regex.Util.substitute(matcher, pattern,
					new StringSubstitution(str2), val,
					org.apache.oro.text.regex.Util.SUBSTITUTE_ALL);

		} catch (MalformedPatternException e) {
			e.printStackTrace();

			return val;
		}
	}

	public static String replaceAll(String val, String str1, String str2,
			int mask) {
		String patternStr = str1;

		/**
		 * �����������ʽpatternStr�����øñ���ʽ�봫���sql������ģʽƥ��,
		 * ���ƥ����ȷ�����ƥ���������ȡ�����϶���õ�6���֣���ŵ������в����� ������
		 */

		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern = null;

		try {
			
			pattern = compiler.compile(patternStr,
					mask);
			
			PatternMatcher matcher = new Perl5Matcher();
			return org.apache.oro.text.regex.Util.substitute(matcher, pattern,
					new StringSubstitution(str2), val,
					org.apache.oro.text.regex.Util.SUBSTITUTE_ALL);

		} catch (MalformedPatternException e) {
			e.printStackTrace();

			return val;
		}
	}

	/**
	 * �ָ��ַ���Ϊ���麯��
	 * 
	 * @param val
	 *            String
	 * @param token
	 *            String
	 * @param CASE_INSENSITIVE
	 *            boolean
	 * @return String[]
	 */
	public static String[] split(String val, String token,
			boolean CASE_INSENSITIVE) {
		String patternStr = token;
		/**
		 * �����������ʽpatternStr�����øñ���ʽ�봫���sql������ģʽƥ��,
		 * ���ƥ����ȷ�����ƥ���������ȡ�����϶���õ�6���֣���ŵ������в����� ������
		 */

		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern = null;

		try {
			if (CASE_INSENSITIVE) {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.DEFAULT_MASK);
			} else {
				pattern = compiler.compile(patternStr,
						Perl5Compiler.CASE_INSENSITIVE_MASK);
			}

			PatternMatcher matcher = new Perl5Matcher();
			List list = new ArrayList();
			split(list, matcher, pattern, val, SPLIT_ALL);
			String[] rets = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				rets[i] = (String) list.get(i);

			}
			return rets;

		} catch (MalformedPatternException e) {
			e.printStackTrace();

			return new String[] { val };
		}

	}
	
	/**
	 * �ָ��ַ���Ϊ���麯��
	 * 
	 * @param val
	 *            String
	 * @param token
	 *            String
	 * @param CASE_INSENSITIVE
	 *            boolean
	 * @return String[]
	 */
	public static String[] split(String val, String token,
			int mask) {
		String patternStr = token;
		/**
		 * �����������ʽpatternStr�����øñ���ʽ�봫���sql������ģʽƥ��,
		 * ���ƥ����ȷ�����ƥ���������ȡ�����϶���õ�6���֣���ŵ������в����� ������
		 */

		PatternCompiler compiler = new Perl5Compiler();
		Pattern pattern = null;

		try {
			
				pattern = compiler.compile(patternStr,
						mask);
			

			PatternMatcher matcher = new Perl5Matcher();
			List list = new ArrayList();
			split(list, matcher, pattern, val, SPLIT_ALL);
			String[] rets = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				rets[i] = (String) list.get(i);

			}
			return rets;

		} catch (MalformedPatternException e) {
			e.printStackTrace();

			return new String[] { val };
		}

	}

	private static void split(Collection results, PatternMatcher matcher,
			Pattern pattern, String input, int limit) {
		int beginOffset;
		MatchResult currentResult;
		PatternMatcherInput pinput;

		pinput = new PatternMatcherInput(input);
		beginOffset = 0;

		while (--limit != 0 && matcher.contains(pinput, pattern)) {
			currentResult = matcher.getMatch();
			results.add(input.substring(beginOffset, currentResult
					.beginOffset(0)));
			beginOffset = currentResult.endOffset(0);
		}

		results.add(input.substring(beginOffset, input.length()));
	}

	public static String replaceNull(String resource) {
		return resource == null ? "" : resource;
	}

	public static String getParameter(HttpServletRequest request, String name,
			String defaultValue) {
		String value = request.getParameter(name);
		return value != null ? value : defaultValue;
	}

	public static void main(String args[]) {
//		String str = "����,'bb,cc,'dd";
//		try {
//			str = new String(str.getBytes(), "utf-8");
//		} catch (UnsupportedEncodingException ex) {
//		}
//		System.out.println(str.getBytes()[0]);
//		System.out.println(str.getBytes()[1]);
//		System.out.println(str.getBytes()[2]);
//		System.out.println(str.getBytes()[3]);
//
//		System.out.println("?".getBytes()[0]);
		int maxlength = 16;
		String replace  ="...";
		String outStr = "2010��02��04��12ʱ�����ν�����Ů��1987��06��18����������֤��430981198706184686������ʡ�佭���佭���ϴ���������ʮ��������24�ţ��������侭Ӫ�������е��Ŷ���������װ�걻���ˡ��Ӿ������������������ֳ��˽�ϵ����������12ʱ���ν�����ĸ�׻�־Ԫ�ڵ��������⣬�������ӽ�����ڣ�����һ�����԰���ڵ����¾���ȥ����ע��������һ���Ӿͽ��е��ԣ���ȡ�����������̨������700Ԫ�����";
		
		System.out.println(StringUtil.getHandleString(maxlength,replace,false,false,outStr));
		
outStr = "2010��02��07��11ʱ��������ӱ��������2·�������ϱ����ԣ���ץ��һ�������ˡ��񾯳����󣬾����飬����ӱ�ڵ���10ʱ40������2·�������ϣ�;�б��������Ӱ����ֽ�3100Ԫ��һ��������ץ����һ�������ߡ� ";
		
		System.out.println(StringUtil.getHandleString(maxlength,replace,false,false,outStr));
	}

	/**
	 * Translates a string into <code>application/x-www-form-urlencoded</code>
	 * format using a specific encoding scheme. This method uses the supplied
	 * encoding scheme to obtain the bytes for unsafe characters.
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilites.</em>
	 * 
	 * @param s
	 *            <code>String</code> to be translated.
	 * @param enc
	 *            The name of a supported <a
	 *            href="../lang/package-summary.html#charenc">character encoding
	 *            </a>.
	 * @return the translated <code>String</code>.
	 * @exception UnsupportedEncodingException
	 *                If the named encoding is not supported
	 * @see URLDecoder#decode(java.lang.String, java.lang.String)
	 * @since 1.4
	 */
	public static String encode(String s, String enc) {
		if (enc == null || enc.trim().equals("")) {
			enc = dfltEncName;
		}
		boolean needToChange = false;
		boolean wroteUnencodedChar = false;
		int maxBytesPerChar = 10; // rather arbitrary limit, but safe for now
		StringBuffer out = new StringBuffer(s.length());
		ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);

		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(buf, enc);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < s.length(); i++) {
			int c = (int) s.charAt(i);
			// System.out.println("Examining character: " + c);
			if (dontNeedEncoding.get(c)) {
				if (c == ' ') {
					c = '+';
					needToChange = true;
				}
				// System.out.println("Storing: " + c);
				out.append((char) c);
				wroteUnencodedChar = true;
			} else {
				// convert to external encoding before hex conversion
				try {
					if (wroteUnencodedChar) { // Fix for 4407610
						writer = new OutputStreamWriter(buf, enc);
						wroteUnencodedChar = false;
					}
					writer.write(c);
					/*
					 * If this character represents the start of a Unicode
					 * surrogate pair, then pass in two characters. It's not
					 * clear what should be done if a bytes reserved in the
					 * surrogate pairs range occurs outside of a legal surrogate
					 * pair. For now, just treat it as if it were any other
					 * character.
					 */
					if (c >= 0xD800 && c <= 0xDBFF) {
						/*
						 * System.out.println(Integer.toHexString(c) + " is high
						 * surrogate");
						 */
						if ((i + 1) < s.length()) {
							int d = (int) s.charAt(i + 1);
							/*
							 * System.out.println("\tExamining " +
							 * Integer.toHexString(d));
							 */
							if (d >= 0xDC00 && d <= 0xDFFF) {
								/*
								 * System.out.println("\t" +
								 * Integer.toHexString(d) + " is low
								 * surrogate");
								 */
								writer.write(d);
								i++;
							}
						}
					}
					writer.flush();
				} catch (IOException e) {
					buf.reset();
					continue;
				}
				byte[] ba = buf.toByteArray();
				for (int j = 0; j < ba.length; j++) {
					out.append('%');
					char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
					// converting to use uppercase letter as part of
					// the hex value if ch is a letter.
					if (Character.isLetter(ch)) {
						ch -= caseDiff;
					}
					out.append(ch);
					ch = Character.forDigit(ba[j] & 0xF, 16);
					if (Character.isLetter(ch)) {
						ch -= caseDiff;
					}
					out.append(ch);
				}
				buf.reset();
				needToChange = true;
			}
		}

		return (needToChange ? out.toString() : s);
	}

	/**
	 * Translates a string into <code>application/x-www-form-urlencoded</code>
	 * format using a specific encoding scheme. This method uses the supplied
	 * encoding scheme to obtain the bytes for unsafe characters.
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilites.</em>
	 * 
	 * @param s
	 *            <code>String</code> to be translated.
	 * @param enc
	 *            The name of a supported <a
	 *            href="../lang/package-summary.html#charenc">character encoding
	 *            </a>.
	 * @return the translated <code>String</code>.
	 * @exception UnsupportedEncodingException
	 *                If the named encoding is not supported
	 * @see URLDecoder#decode(java.lang.String, java.lang.String)
	 * @since 1.4
	 */
	public static String encode(String s) {
		return encode(s, dfltEncName);
	}

	public static String toUTF(String inpara) {

		char temchr;

		int ascchr;

		int i;

		String rtstr = new String("");

		if (inpara == null) {

			inpara = "";

		}

		for (i = 0; i < inpara.length(); i++) {
			temchr = inpara.charAt(i);
			ascchr = temchr + 0;
			// System.out.println(ascchr);

			// System.out.println(Integer.toBinaryString(ascchr));

			rtstr = rtstr + "&#x" + Integer.toHexString(ascchr) + ";";

		}

		return rtstr;

	}

	public static String toGB2312(String inpara) {

		// System.out.println("ԭ�����ַ���Ϊ��" + inpara);

		if (inpara == null) {

			inpara = "";

		}

		try {

			char[] temp = inpara.toCharArray();

			byte[] b = new byte[temp.length];

			// System.out.println("�ֳ�char[]���ַ�����Ϊ��" + temp);

			// System.out.println("�����ַ����ĳ���Ϊ��" + temp.length);

			int tempint;

			for (int i = 0; i < temp.length; i++) {

				b[i] = (byte) temp[i];

				tempint = (int) b[i];

				// System.out.println("��" + i + "���ַ��ı���Ϊ��" + tempint +
				// "\t��������Ϊ��" +
				// Integer.toBinaryString(tempint));
			}

			String deststring = new String(b, "gb2312");

			// System.out.println(deststring);

			return deststring;

		} catch (java.io.UnsupportedEncodingException e) {
			return "��֧�ֵ��ַ�����";
		}
	}

	public List splitString(String src, int size) {
		if (src == null)
			return null;
		List segs = new ArrayList();
		StringBuffer seg = new StringBuffer();
		while (src.length() > size) {

		}
		if (src.length() <= size) {
			segs.add(src);
			return segs;
		}

		// int length = msg.length();
		// int splitNum = (int) (length / step) + 1;
		// boolean flag = true;
		// if (splitNum > 10) {
		// splitNum = 10;
		// flag = false;
		// }
		// int len = 0;
		// for (int i = 0; i < splitNum; i++) {
		// if (i == 0) {
		// String spMsg = msg.substring(0, step) +
		// getFirstEnd(splitNum);
		// v.addElement(spMsg);
		// spMsg = null;
		// len = step;
		// continue;
		// }
		// if (i == splitNum - 1) {
		// String spMsg = null;
		// if (flag == false)
		// spMsg = getSecondFirst(i, splitNum) +
		// msg.substring(len, len + step);
		// v.addElement(spMsg);
		// spMsg = null;
		// continue;
		// }
		// String spMsg = null;
		// spMsg = getSecondFirst(i, splitNum) +
		// msg.substring(len, len + step) +
		// getSecondEnd(i, splitNum);
		// v.addElement(spMsg);
		// spMsg = null;
		// }
		return null;

	}

	public String splitString(String src, int offset, int size) {
		if (src == null || src.equals("")) {
			return "";
		}
		if (offset < src.length()) {
			return src.substring(offset, size);
		} else {
			int newoffset = src.length() % size;

		}
		return (String) splitString(src, size).get(offset);
	}

	public static String replaceNull(String value, String nullReplace) {
		return value == null ? nullReplace : value;
	}

	public static boolean getBoolean(String value, boolean nullReplace) {
		boolean ret = false;
		if (value == null)
			ret = nullReplace;
		else if (value.trim().equalsIgnoreCase("true"))
			ret = true;
		else
			ret = false;
		return ret;

	}

	public static int getInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * �ж������Ƿ���javascript����
	 * 
	 * @param nodeLink
	 * @return
	 */
	public static boolean isJavascript(String nodeLink) {
		return nodeLink != null
				&& nodeLink.toLowerCase().startsWith("javascript:");

	}

	/**
	 * ��html�б�������ַ�ת��Ϊת���
	 * 
	 * @param text
	 * @return
	 */
	public static String HTMLEncode(String text) {
		text = StringUtil.replaceAll(text, "&", "&amp;");
		text = StringUtil.replaceAll(text, "\"", "&quot;");
		text = StringUtil.replaceAll(text, "<", "&lt;");
		text = StringUtil.replaceAll(text, ">", "&gt;");
		text = StringUtil.replaceAll(text, "'", "&#146;");
		text = StringUtil.replaceAll(text, "\\ ", "&nbsp;");
		text = StringUtil.replaceAll(text, "\n", "<br>");
		text = StringUtil.replaceAll(text, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return text;
	}
	
	
	/**
	 * ��html�б�������ַ�ת��Ϊת���
	 * 
	 * @param text
	 * @return
	 */
	public static String HTMLNoBREncode(String text) {
		text = StringUtil.replaceAll(text, "&", "&amp;");
		text = StringUtil.replaceAll(text, "\"", "&quot;");
		text = StringUtil.replaceAll(text, "<", "&lt;");
		text = StringUtil.replaceAll(text, ">", "&gt;");
		text = StringUtil.replaceAll(text, "'", "&#146;");
		text = StringUtil.replaceAll(text, "\\ ", "&nbsp;");
//		text = StringUtil.replaceAll(text, "\n", "<br>");
//		text = StringUtil.replaceAll(text, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return text;
	}

	/**
	 * ��ת����ַ�����ԭ
	 * 
	 * @param text
	 * @return
	 */
	public static String HTMLEncodej(String text) {
		text = StringUtil.replaceAll(text, "&amp;", "&");
		text = StringUtil.replaceAll(text, "&quot;", "\"");
		text = StringUtil.replaceAll(text, "&lt;", "<");
		text = StringUtil.replaceAll(text, "&gt;", ">");
		text = StringUtil.replaceAll(text, "&#146;", "'");
		text = StringUtil.replaceAll(text, "&nbsp;", "\\ ");
		text = StringUtil.replaceAll(text, "<br>", "\n");
		text = StringUtil.replaceAll(text, "&nbsp;&nbsp;&nbsp;&nbsp;", "\t");
		return text;
	}
	/**
	 * ��ת����ַ�����ԭ
	 * 
	 * @param text
	 * @return
	 */
	public static String HTMLNoBREncodej(String text) {
		text = StringUtil.replaceAll(text, "&amp;", "&");
		text = StringUtil.replaceAll(text, "&quot;", "\"");
		text = StringUtil.replaceAll(text, "&lt;", "<");
		text = StringUtil.replaceAll(text, "&gt;", ">");
		text = StringUtil.replaceAll(text, "&#146;", "'");
		text = StringUtil.replaceAll(text, "&nbsp;", "\\ ");
//		text = StringUtil.replaceAll(text, "<br>", "\n");
//		text = StringUtil.replaceAll(text, "&nbsp;&nbsp;&nbsp;&nbsp;", "\t");
		return text;
	}

	public static String getHandleString(int maxlength, String replace,
			boolean htmlencode, boolean htmldecode, String outStr) {
		if (maxlength > 0 && outStr != null && outStr.length() > maxlength) {
			outStr = outStr.substring(0, maxlength);
			if (replace != null)
				outStr += replace;
		}
		if (htmlencode) {
			return StringUtil.HTMLEncode(outStr);
		} else if (htmlencode) {
			return StringUtil.HTMLEncodej(outStr);
		} else {
			return outStr;
		}

	}

	/**
	 * ������Ϣ��־
	 * 
	 * @param messages
	 * @return
	 */
	public static String buildStringMessage(List messages) {
		if (messages == null || messages.size() == 0)
			return null;
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0)
				str.append(messages.get(i));
			else
				str.append("\\n").append(messages.get(i));

		}
		return str.toString();
	}
	
	public static boolean hasText(String str) {
		return hasText((CharSequence) str);
	}
	/**
	 * Check whether the given CharSequence has actual text.
	 * More specifically, returns <code>true</code> if the string not <code>null</code>,
	 * its length is greater than 0, and it contains at least one non-whitespace character.
	 * <p><pre>
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * @param str the CharSequence to check (may be <code>null</code>)
	 * @return <code>true</code> if the CharSequence is not <code>null</code>,
	 * its length is greater than 0, and it does not contain whitespace only
	 * @see java.lang.Character#isWhitespace
	 */
	public static boolean hasText(CharSequence str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check that the given CharSequence is neither <code>null</code> nor of length 0.
	 * Note: Will return <code>true</code> for a CharSequence that purely consists of whitespace.
	 * <p><pre>
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param str the CharSequence to check (may be <code>null</code>)
	 * @return <code>true</code> if the CharSequence is not null and has length
	 * @see #hasText(String)
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}
	/**
	 * Test if the given String starts with the specified prefix,
	 * ignoring upper/lower case.
	 * @param str the String to check
	 * @param prefix the prefix to look for
	 * @see java.lang.String#startsWith
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		if (str.startsWith(prefix)) {
			return true;
		}
		if (str.length() < prefix.length()) {
			return false;
		}
		String lcStr = str.substring(0, prefix.length()).toLowerCase();
		String lcPrefix = prefix.toLowerCase();
		return lcStr.equals(lcPrefix);
	}
	
	/**
	 * Compare two paths after normalization of them.
	 * @param path1 first path for comparison
	 * @param path2 second path for comparison
	 * @return whether the two paths are equivalent after normalization
	 */
	public static boolean pathEquals(String path1, String path2) {
		return cleanPath(path1).equals(cleanPath(path2));
	}
	/**
	 * Normalize the path by suppressing sequences like "path/.." and
	 * inner simple dots.
	 * <p>The result is convenient for path comparison. For other uses,
	 * notice that Windows separators ("\") are replaced by simple slashes.
	 * @param path the original path
	 * @return the normalized path
	 */
	public static String cleanPath(String path) {
		String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		List pathElements = new LinkedList();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			if (CURRENT_PATH.equals(pathArray[i])) {
				// Points to current directory - drop it.
			}
			else if (TOP_PATH.equals(pathArray[i])) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, pathArray[i]);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, TOP_PATH);
		}

		return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
	}
	
	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * <p>A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of potential
	 * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
	 * @param str the input String
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	/**
	 * Take a String which is a delimited list and convert it to a String array.
	 * <p>A single delimiter can consists of more than one character: It will still
	 * be considered as single delimiter string, rather than as bunch of potential
	 * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
	 * @param str the input String
	 * @param delimiter the delimiter between elements (this is a single delimiter,
	 * rather than a bunch individual delimiter characters)
	 * @param charsToDelete a set of characters to delete. Useful for deleting unwanted
	 * line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a String.
	 * @return an array of the tokens in the list
	 * @see #tokenizeToStringArray
	 */
	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}
		List result = new ArrayList();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos = 0;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}
	
	/**
	 * Copy the given Collection into a String array.
	 * The Collection must contain String elements only.
	 * @param collection the Collection to copy
	 * @return the String array (<code>null</code> if the passed-in
	 * Collection was <code>null</code>)
	 */
	public static String[] toStringArray(Collection collection) {
		if (collection == null) {
			return null;
		}
		return (String[]) collection.toArray(new String[collection.size()]);
	}
	
	/**
	 * Copy the given Collection into a String array.
	 * The Collection must contain String elements only.
	 * @param collection the Collection to copy
	 * @return the String array (<code>null</code> if the passed-in
	 * Collection was <code>null</code>)
	 */
	public static Integer[] toIntArray(Collection<Integer> collection) {
		if (collection == null) {
			return null;
		}
		return (Integer[]) collection.toArray(new Integer[collection.size()]);
	}
	
	

	/**
	 * Copy the given Enumeration into a String array.
	 * The Enumeration must contain String elements only.
	 * @param enumeration the Enumeration to copy
	 * @return the String array (<code>null</code> if the passed-in
	 * Enumeration was <code>null</code>)
	 */
	public static String[] toStringArray(Enumeration enumeration) {
		if (enumeration == null) {
			return null;
		}
		List list = Collections.list(enumeration);
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Trim the elements of the given String array,
	 * calling <code>String.trim()</code> on each of them.
	 * @param array the original String array
	 * @return the resulting array (of the same size) with trimmed elements
	 */
	public static String[] trimArrayElements(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[0];
		}
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++) {
			String element = array[i];
			result[i] = (element != null ? element.trim() : null);
		}
		return result;
	}

	/**
	 * Remove duplicate Strings from the given array.
	 * Also sorts the array, as it uses a TreeSet.
	 * @param array the String array
	 * @return an array without duplicates, in natural sort order
	 */
	public static String[] removeDuplicateStrings(String[] array) {
		if (ObjectUtils.isEmpty(array)) {
			return array;
		}
		Set set = new TreeSet();
		for (int i = 0; i < array.length; i++) {
			set.add(array[i]);
		}
		return toStringArray(set);
	}
	
	/**
	 * Delete any character in a given String.
	 * @param inString the original String
	 * @param charsToDelete a set of characters to delete.
	 * E.g. "az\n" will delete 'a's, 'z's and new lines.
	 * @return the resulting String
	 */
	public static String deleteAny(String inString, String charsToDelete) {
		if (!hasLength(inString) || !hasLength(charsToDelete)) {
			return inString;
		}
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for <code>toString()</code> implementations.
	 * @param coll the Collection to display
	 * @param delim the delimiter to use (probably a ",")
	 * @param prefix the String to start each element with
	 * @param suffix the String to end each element with
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection coll, String delim, String prefix, String suffix) {
		if (CollectionUtils.isEmpty(coll)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Iterator it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
	/**
	 * Convenience method to return a Collection as a delimited (e.g. CSV)
	 * String. E.g. useful for <code>toString()</code> implementations.
	 * @param coll the Collection to display
	 * @param delim the delimiter to use (probably a ",")
	 * @return the delimited String
	 */
	public static String collectionToDelimitedString(Collection coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convenience method to return a Collection as a CSV String.
	 * E.g. useful for <code>toString()</code> implementations.
	 * @param coll the Collection to display
	 * @return the delimited String
	 */
	public static String collectionToCommaDelimitedString(Collection coll) {
		return collectionToDelimitedString(coll, ",");
	}
	
	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * Trims tokens and omits empty tokens.
	 * <p>The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using <code>delimitedListToStringArray</code>
	 * @param str the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String
	 * (each of those characters is individually considered as delimiter).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see java.lang.String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * <p>The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using <code>delimitedListToStringArray</code>
	 * @param str the String to tokenize
	 * @param delimiters the delimiter characters, assembled as String
	 * (each of those characters is individually considered as delimiter)
	 * @param trimTokens trim the tokens via String's <code>trim</code>
	 * @param ignoreEmptyTokens omit empty tokens from the result array
	 * (only applies to tokens that are empty after trimming; StringTokenizer
	 * will not consider subsequent delimiters as token in the first place).
	 * @return an array of the tokens (<code>null</code> if the input String
	 * was <code>null</code>)
	 * @see java.util.StringTokenizer
	 * @see java.lang.String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(
			String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

		if (str == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List tokens = new ArrayList();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}
	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';
	
	/**
	 * Apply the given relative path to the given path,
	 * assuming standard Java folder separation (i.e. "/" separators);
	 * @param path the path to start from (usually a full file path)
	 * @param relativePath the relative path to apply
	 * (relative to the full file path above)
	 * @return the full file path that results from applying the relative path
	 */
	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}
	
	
	

	/**
	 * Extract the filename from the given path,
	 * e.g. "mypath/myfile.txt" -> "myfile.txt".
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename, or <code>null</code> if none
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}
	
	/**
	 * Convenience method to return a String array as a CSV String.
	 * E.g. useful for <code>toString()</code> implementations.
	 * @param arr the array to display
	 * @return the delimited String
	 */
	public static String arrayToCommaDelimitedString(Object[] arr) {
		return arrayToDelimitedString(arr, ",");
	}
	
	/**
	 * Convenience method to return a String array as a delimited (e.g. CSV)
	 * String. E.g. useful for <code>toString()</code> implementations.
	 * @param arr the array to display
	 * @param delim the delimiter to use (probably a ",")
	 * @return the delimited String
	 */
	public static String arrayToDelimitedString(Object[] arr, String delim) {
		if (ObjectUtils.isEmpty(arr)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				sb.append(delim);
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Trim trailing whitespace from the given String.
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimTrailingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}

	/**
	 * Trim all occurences of the supplied leading character from the given String.
	 * @param str the String to check
	 * @param leadingCharacter the leading character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str, char leadingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && buf.charAt(0) == leadingCharacter) {
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}

	/**
	 * Trim all occurences of the supplied trailing character from the given String.
	 * @param str the String to check
	 * @param trailingCharacter the trailing character to be trimmed
	 * @return the trimmed String
	 */
	public static String trimTrailingCharacter(String str, char trailingCharacter) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && buf.charAt(buf.length() - 1) == trailingCharacter) {
			buf.deleteCharAt(buf.length() - 1);
		}
		return buf.toString();
	}
	
	/**
	 * Trim leading whitespace from the given String.
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimLeadingWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}
	
	/**
	 * Count the occurrences of the substring in string s.
	 * @param str string to search in. Return 0 if this is null.
	 * @param sub string to search for. Return 0 if this is null.
	 */
	public static int countOccurrencesOf(String str, String sub) {
		if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
			return 0;
		}
		int count = 0, pos = 0, idx = 0;
		while ((idx = str.indexOf(sub, pos)) != -1) {
			++count;
			pos = idx + sub.length();
		}
		return count;
	}
	
	/**
	 * Strip the filename extension from the given path,
	 * e.g. "mypath/myfile.txt" -> "mypath/myfile".
	 * @param path the file path (may be <code>null</code>)
	 * @return the path with stripped filename extension,
	 * or <code>null</code> if none
	 */
	public static String stripFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		return (sepIndex != -1 ? path.substring(0, sepIndex) : path);
	}

	/**
	 * Convert a CSV list into an array of Strings.
	 * @param str the input String
	 * @return an array of Strings, or the empty array in case of empty input
	 */
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}
	
	/**
	 * Append the given String to the given String array, returning a new array
	 * consisting of the input array contents plus the given String.
	 * @param array the array to append to (can be <code>null</code>)
	 * @param str the String to append
	 * @return the new array (never <code>null</code>)
	 */
	public static String[] addStringToArray(String[] array, String str) {
		if (ObjectUtils.isEmpty(array)) {
			return new String[] {str};
		}
		String[] newArr = new String[array.length + 1];
		System.arraycopy(array, 0, newArr, 0, array.length);
		newArr[array.length] = str;
		return newArr;
	}

	/**
	 * Trim <i>all</i> whitespace from the given String:
	 * leading, trailing, and inbetween characters.
	 * @param str the String to check
	 * @return the trimmed String
	 * @see java.lang.Character#isWhitespace
	 */
	public static String trimAllWhitespace(String str) {
		if (!hasLength(str)) {
			return str;
		}
		StringBuffer buf = new StringBuffer(str);
		int index = 0;
		while (buf.length() > index) {
			if (Character.isWhitespace(buf.charAt(index))) {
				buf.deleteCharAt(index);
			}
			else {
				index++;
			}
		}
		return buf.toString();
	}
	
	/**
	 * Parse the given <code>localeString</code> into a {@link Locale}.
	 * <p>This is the inverse operation of {@link Locale#toString Locale's toString}.
	 * @param localeString the locale string, following <code>Locale's</code>
	 * <code>toString()</code> format ("en", "en_UK", etc);
	 * also accepts spaces as separators, as an alternative to underscores
	 * @return a corresponding <code>Locale</code> instance
	 */
	public static Locale parseLocaleString(String localeString) {
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		String variant = "";
		if (parts.length >= 2) {
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_")) {
				variant = trimLeadingCharacter(variant, '_');
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	/**
	 * Determine the RFC 3066 compliant language tag,
	 * as used for the HTTP "Accept-Language" header.
	 * @param locale the Locale to transform to a language tag
	 * @return the RFC 3066 compliant language tag as String
	 */
	public static String toLanguageTag(Locale locale) {
		return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
	}
	

	/**
	 * Extract the filename extension from the given path,
	 * e.g. "mypath/myfile.txt" -> "txt".
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename extension, or <code>null</code> if none
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		return (sepIndex != -1 ? path.substring(sepIndex + 1) : null);
	}
	
	public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file) throws Exception {
        OutputStream out = null;
        RandomAccessFile raf = null;
        try {
        	raf = new RandomAccessFile(file, "r");
        	out =  response.getOutputStream();
            long fileSize = raf.length();
            long rangeStart = 0;
            long rangeFinish = fileSize - 1;

            // accept attempts to resume download (if any)
            String range = request.getHeader("Range");
            if (range != null && range.startsWith("bytes=")) {
                String pureRange = range.replaceAll("bytes=", "");
                int rangeSep = pureRange.indexOf("-");

                try {
                    rangeStart = Long.parseLong(pureRange.substring(0, rangeSep));
                    if (rangeStart > fileSize || rangeStart < 0) rangeStart = 0;
                } catch (NumberFormatException e) {
                    // ignore the exception, keep rangeStart unchanged
                }

                if (rangeSep < pureRange.length() - 1) {
                    try {
                        rangeFinish = Long.parseLong(pureRange.substring(rangeSep + 1));
                        if (rangeFinish < 0 || rangeFinish >= fileSize) rangeFinish = fileSize - 1;
                    } catch (NumberFormatException e) {
                        // ignore the exception
                    }
                }
            }

            // set some headers


            response.setHeader("Content-Disposition", "attachment; filename=" + new String(file.getName().getBytes(),"ISO-8859-1").replaceAll(" ", "-"));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", Long.toString(rangeFinish - rangeStart + 1));
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeFinish + "/" + fileSize);

            // seek to the requested offset
            raf.seek(rangeStart);

            // send the file
            byte buffer[] = new byte[1024];

            long len;
            int totalRead = 0;
            boolean nomore = false;
            while (true) {
                len = raf.read(buffer);
                if (len > 0 && totalRead + len > rangeFinish - rangeStart + 1) {
                    // read more then required?
                    // adjust the length
                    len = rangeFinish - rangeStart + 1 - totalRead;
                    nomore = true;
                }

                if (len > 0) {
                    out.write(buffer, 0, (int) len);
                    totalRead += len;
                    if (nomore) break;
                } else {
                    break;
                }
            }
            out.flush();
        } 
        catch(Exception e)
        {
        	throw e;
        }
        finally {
            try
			{
				raf.close();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            try
			{
				out.close();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
 
 public static void sendFile(HttpServletRequest request, HttpServletResponse response, String filename,Blob blob) throws Exception {
	 if(blob == null)
		 return ;
	 sendFile_( request,  response,  filename,blob.getBinaryStream(),blob.length());
 }
 
 public static void sendFile(HttpServletRequest request, HttpServletResponse response, String filename,Clob clob) throws Exception {
	 if(clob == null)
		 return ;
	 sendFile_( request,  response,  filename,clob.getAsciiStream(),clob.length());
 }
 
 public static void sendFile_(HttpServletRequest request, HttpServletResponse response, String filename,InputStream in,long fileSize) throws Exception {
        OutputStream out = null;
//        InputStream in = null;
        try {
        	if(in == null)
        		return;
        	out = response.getOutputStream();
        	
//        	if(blob == null)
//        		return ;
//        	in = blob.getBinaryStream();
//            long fileSize = blob.length();
            long rangeStart = 0;
            long rangeFinish = fileSize - 1;

            // accept attempts to resume download (if any)
            String range = request.getHeader("Range");
            if (range != null && range.startsWith("bytes=")) {
                String pureRange = range.replaceAll("bytes=", "");
                int rangeSep = pureRange.indexOf("-");

                try {
                    rangeStart = Long.parseLong(pureRange.substring(0, rangeSep));
                    if (rangeStart > fileSize || rangeStart < 0) rangeStart = 0;
                } catch (NumberFormatException e) {
                    // ignore the exception, keep rangeStart unchanged
                }

                if (rangeSep < pureRange.length() - 1) {
                    try {
                        rangeFinish = Long.parseLong(pureRange.substring(rangeSep + 1));
                        if (rangeFinish < 0 || rangeFinish >= fileSize) rangeFinish = fileSize - 1;
                    } catch (NumberFormatException e) {
                        // ignore the exception
                    }
                }
            }

            // set some headers


            response.setHeader("Content-Disposition", "attachment; filename=" + new String(filename.getBytes(),"ISO-8859-1").replaceAll(" ", "-"));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", Long.toString(rangeFinish - rangeStart + 1));
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeFinish + "/" + fileSize);

            // seek to the requested offset
            

            // send the file
            byte buffer[] = new byte[1024];
            in.skip(rangeStart);
            long len;
            int totalRead = 0;
            boolean nomore = false;
            while (true) {
                len = in.read(buffer);
                if (len > 0 && totalRead + len > rangeFinish - rangeStart + 1) {
                    // read more then required?
                    // adjust the length
                    len = rangeFinish - rangeStart + 1 - totalRead;
                    nomore = true;
                }

                if (len > 0) {
                    out.write(buffer, 0, (int) len);
                    totalRead += len;
                    if (nomore) break;
                } else {
                    break;
                }
            }
            out.flush();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	throw e;
        }
        finally {
        	try
			{
        		if(in != null)
        			in.close();		
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try
			{
            	if(out != null)
            		out.close();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
 
 	public static final String AOP_PROPERTIES_PATH = "/aop.properties";
	public static InputStream getInputStream(String resourcefile,Class clazz) throws IOException {
		InputStream is = null;

		is = clazz.getResourceAsStream(resourcefile);
		if (is == null) {
			throw new FileNotFoundException(resourcefile
					+ " cannot be opened because it does not exist");
		}
		return is;
	}
	
	public static Properties getProperties(String resourcefile,Class clazz) throws IOException {
		InputStream is = getInputStream(resourcefile,clazz);
		try {
			Properties props = new Properties();
			props.load(is);
			return props;

		} finally {
			is.close();
		}
	}
	
	public static boolean isEmpty(String value)
	{
		return value == null || "".equals(value);
	}
}