// (c) 2017 uchicom
package com.uchicom.jsong;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.swing.JOptionPane;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Main {

	/**
	 * ファイル名とコードを指定してファイルを作成する。
	 * 出力結果はファイル名+_jsonディレクトリ/カラム名+.jsonファイル.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		if (args.length != 0) {
			for (String arg : args) {
				String[] splits = arg.split(":");
				File file = new File(splits[0]);
				String[] columns = splits[1].split(",");
				Jsong jsong = new Jsong(file);
				File dir = new File(file.getParent(), file.getName() + "_json");
				dir.mkdirs();
				for (String column : columns) {
					System.out.println("col " + column);
					if (!file.isDirectory()) {
						Files.write(new File(dir, column + ".json").toPath(),
								jsong.generate(column).getBytes("utf-8"), StandardOpenOption.CREATE);
					}
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}


}
