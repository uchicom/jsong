// (c) 2017 uchicom
package com.uchicom.jsong;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.uchicom.util.ResourceUtil;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Jsong {

	private Properties config;
	private File file;
	private List<String> rowList;
	private int rowMax;
	private String[] heads;
	private int keyStartIndex = -1;
	private int keyLength = -1;
	private int typeIndex = -1;
	private int valueStartIndex = -1;
	private int valueLength = -1;

	public Jsong(File file) throws Exception {
		this(file, ResourceUtil.createProperties(Constants.configFile, "UTF-8"));
	}

	public Jsong(File file, Properties config) throws Exception {
		this.config = config;
		this.file = file;
		init();
	}

	private void init() throws Exception {
		// csvファイルを読み込む。
		rowList = Files.readAllLines(file.toPath(), Charset.forName("utf-8"));
		rowMax = rowList.size();
		if (rowMax > 0) {
			String titleLine = rowList.get(0);
			heads = titleLine.split(",");
			String key = config.getProperty(Constants.PROP_KEY);
			String type = config.getProperty(Constants.PROP_TYPE);
			for (int i = 0; i < heads.length; i++) {
				if (keyStartIndex < 0 && key.equals(heads[i])) {
					keyStartIndex = i;
				} else if (keyStartIndex >= 0 && keyLength < 0 && !"".equals(heads[i])) {
					// キーの深さ
					keyLength = i - keyStartIndex;
				}
				if (typeIndex < 0 && type.equals(heads[i])) {
					typeIndex = i;
				}

				if (keyStartIndex >= 0 && keyLength >= 0 && typeIndex >= 0 && keyStartIndex != i && typeIndex != i
						&& valueStartIndex < 0) {
					valueStartIndex = i;
				}
				if (valueStartIndex >= 0) {
					if (!"".equals(heads[i])) {
						valueLength = i - valueStartIndex + 1;
					} else {
						break;
					}
				}

			}
			if (keyStartIndex < 0 || keyLength < 0 || typeIndex < 0 || valueStartIndex < 0 || valueLength < 0) {
				throw new Exception("タイトル行の記述が不正です");
			}
		} else {
			throw new Exception("タイトル行がありません");
		}
		System.out.println(keyStartIndex + ":" + keyLength + ":" + typeIndex + ":" + valueStartIndex);

	}

	public HttpResult generate(String method, Map<String, String[]> paramMap) {
		if (method == null) {
			method = "GET";
		}
		if (paramMap == null) {
			throw new IllegalArgumentException("パラメータを指定してください");
		}
		// value列を探す
		int valueIndex = -1;
		String responseHeader = null;
		for (int i = valueStartIndex; i < valueStartIndex + valueLength; i++) {
			String[] methods = heads[i].split("\\|");
			String head = null;
			if (methods.length == 1) {
				if ("POST".equals(method)) {
					continue;
				} else {
					head = methods[0];
				}
			} else if (methods.length == 2) {
				switch (methods[0]) {
				case "GET":
				case "POST":
					if (methods[0].equals(method)) {
						head = methods[1];
					} else {
						continue;
					}
					default:
						if ("POST".equals(method)) {
							continue;
						} else {
							responseHeader = methods[0];
							head = methods[1];
						}
				}
			} else if (methods.length == 3) {
				switch (methods[0]) {
				case "GET":
				case "POST":
					if (methods[0].equals(method)) {
						responseHeader = methods[1];
						head = methods[2];
					} else {
						continue;
					}
					default:
						continue;
				}
			}
			String[] params = head.split("&");
			Map<String, List<String>> listMap = new HashMap<>();
			for (String param : params) {
				String[] keyValues = param.split("=");
				List<String> stringList = null;
				if (listMap.containsKey(keyValues[0])) {
					stringList = listMap.get(keyValues[0]);
				} else {
					stringList = new ArrayList<>();
					listMap.put(keyValues[0], stringList);
				}
				stringList.add(keyValues[1]);
			}
			Map<String, List<String>> paramListMap = new HashMap<>();
			paramMap.forEach((key, array)->{
				paramListMap.put(key, Arrays.asList(array));
			});
			if (paramListMap.equals(listMap)) {
				valueIndex = i;
				break;
			}
		}
		if (valueIndex < 0) {
			throw new IllegalArgumentException("パラメータに一致するバリュー列がありません");
		}
		String[] rows = rowList.get(1).split(",");
		StringBuffer strBuff = new StringBuffer(1024 * 4);
		strBuff.append(rows[typeIndex].substring(0, 1));
		generate(2, keyStartIndex, valueIndex, 0, strBuff, false);
		strBuff.append(rows[typeIndex].substring(1, 2));
		System.out.println("result:" + strBuff.toString());
		HttpResult result = new HttpResult();
		result.setHeader(responseHeader);
		result.setBody(strBuff.toString());
		return result;
	}
	public String generate(String column) {
		if (column == null) {
			throw new IllegalArgumentException("パラメータを指定してください");
		}
		// value列を探す
		int valueIndex = -1;
		for (int i = valueStartIndex; i < valueStartIndex + valueLength; i++) {
			if (column.equals(heads[i])) {
				valueIndex = i;
				break;
			}
		}
		if (valueIndex < 0) {
			throw new IllegalArgumentException("パラメータに一致するバリュー列がありません");
		}
		String[] rows = rowList.get(1).split(",");
		StringBuffer strBuff = new StringBuffer(1024 * 4);
		strBuff.append(rows[typeIndex].substring(0, 1));
		generate(2, keyStartIndex, valueIndex, 0, strBuff, false);
		strBuff.append(rows[typeIndex].substring(1, 2));
		System.out.println("result:" + strBuff.toString());
		return strBuff.toString();
	}

	public int generate(int rowIndex, int keyIndex, int valueIndex, int depth, StringBuffer strBuff, boolean isArray) {
		if (rowIndex >= rowMax) {
			return rowIndex;
		}
		boolean start = true;
		while (rowIndex < rowMax) {
			System.out.println(rowList.size() + ":" + rowMax + ":" + rowIndex);
			String[] rows = rowList.get(rowIndex).split(",");
			if ("".equals(rows[keyIndex + depth])) {
				break;
			}
			if (!start) {
				strBuff.append(",");
			}
			String key = rows[keyIndex + depth];
			switch (rows[typeIndex].toLowerCase()) {
			case "{}": // オブジェクト型
				if (key.length() != 0 && !isArray) {
					jsoned(key, strBuff);
					strBuff.append(":");
				}
				strBuff.append("{");
				rowIndex = generate(rowIndex + 1, keyIndex, valueIndex, depth + 1,
						strBuff, false);
				strBuff.append("}");
				rowIndex--;
				break;
			case "[]": // 単純配列型
				if (key.length() != 0 && !isArray) {
					jsoned(key, strBuff);
					strBuff.append(":");
				}
				strBuff.append("[");
				rowIndex = generate(rowIndex + 1, keyIndex, valueIndex, depth + 1,
						strBuff, true);
				strBuff.append("]");
				rowIndex--;
				break;
			case "string": // 文字列型
			case "number": // 数値型
			case "boolean": // 真偽値型
				if (!isArray) {
					jsoned(key, strBuff);
					strBuff.append(":");
				}
				jsoned(rows[valueIndex], strBuff);
				break;
			default:
				// 対象外
			}
			if (start) {
				start = false;
			}
			rowIndex++;
		}
		return rowIndex;
	}

	public static void jsoned(String value, StringBuffer strBuff) {
		strBuff.append("\"");
		strBuff.append(value.replace("\"", "\\\"").replace("\t", "\\t").replace("\n", "\\n"));
		strBuff.append("\"");
	}
}
