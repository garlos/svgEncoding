package com.logicallymarc.svg;

import de.erichseifert.vectorgraphics2d.SVGGraphics2D;

import java.awt.Color;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

  public static void main( String[] args )
	{
		System.out.println("Beginning SVG creation...");
		System.out.println("Converting: " + args[1]);

		final int _size = 10;
		String inputString = args[1];

		byte[] inputBytes = inputString.getBytes(StandardCharsets.UTF_8);
		StringBuilder binaryString = new StringBuilder();

		for(byte b : inputBytes) {
			binaryString.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
		}

		int blockCount = binaryString.length();

		System.out.println("To binary: " + binaryString.toString());
		System.out.println("Number of blocks: " + blockCount);

		long sqrtBlockCount = Math.round(Math.sqrt(blockCount));
		while (blockCount%sqrtBlockCount != 0) {
			++sqrtBlockCount;
		}
		long columns = (args[2].equals("h")) ? sqrtBlockCount : blockCount/sqrtBlockCount;
		long rows = (args[2].equals("h")) ? blockCount/sqrtBlockCount : sqrtBlockCount;

		System.out.println("Columns: " + columns);
		System.out.println("Rows: " + rows);

		System.out.println("Getting pieces and chunks...");
		List<String[]> chunks = new ArrayList<String[]>();
				int charCount = 0;

		for (int r=0; r<rows; r++) {
			int chunkSize = 1;
			for (int c=0; c<columns; c++) {
				char thisChar = binaryString.charAt(charCount++);
				char nextChar = ((c + 1) == columns) ?  'x' : binaryString.charAt(charCount);
				if (thisChar != nextChar) {
					chunks.add(new String[]{String.valueOf(thisChar), Integer.toString(chunkSize)});
					chunkSize = 1;
				} else {
					++chunkSize;
				}
//				System.out.println(charCount + " - Row: " + r + ", Col: " + c
//						+ ", Char: " + thisChar + ", Next Char: " + nextChar
//						+ ", Chunk size: " + chunkSize + ", Chunk count: " + chunks.size());
			}
		}

		int blackCount = 0;
		int whiteCount = 0;
		Map<String, Integer> blackMap = new HashMap<String, Integer>();
		Map<String, Integer> whiteMap = new HashMap<String, Integer>();

		for (String[] chunk : chunks) {
			//System.out.println(chunk[0] + ":" + chunk[1]);
			int chunkLength = Integer.parseInt(chunk[1]);
			if (chunk[0].equals("0")) {
				blackCount += chunkLength;
				if (blackMap.containsKey(chunk[1])) {
					blackMap.put(chunk[1], blackMap.get(chunk[1]) + 1);
				} else {
					blackMap.put(chunk[1], 1);
				}
			} else {
				whiteCount += chunkLength;
				if (whiteMap.containsKey(chunk[1])) {
					whiteMap.put(chunk[1], whiteMap.get(chunk[1]) + 1);
				} else {
					whiteMap.put(chunk[1], 1);
				}
			}
		}

		System.out.println("Black pieces: " + blackCount);
		System.out.println("White pieces: " + whiteCount);

		System.out.println("Black chunks:");
		for (String key : blackMap.keySet()) {
			System.out.println("Size: " + key + ", Count: " + blackMap.get(key));
		}

		System.out.println("White chunks:");
		for (String key : whiteMap.keySet()) {
			System.out.println("Size: " + key + ", Count: " + whiteMap.get(key));
		}

		try {
			SVGGraphics2D g = new SVGGraphics2D(0.0, 0.0, columns * _size, columns * _size);

			int xPos=0;
			int yPos=0;
			int currRowWidth = 1;

			for (int z=0; z<binaryString.length(); z++) {
				String currChar = Character.toString(binaryString.charAt(z));
				Color fillColor = (currChar.equals("0")) ? Color.BLACK : Color.WHITE;

//				System.out.println("Creating rect: xPos=" + xPos
//						+ ", yPos=" + yPos
//						+ ", row=" + rowIndex
//						+ ", fillColor=" + fillColor);

				g.setColor(fillColor);
				g.fillRect(xPos, yPos, _size, _size);

				if (currRowWidth == columns) {
					xPos = 0;
					yPos += _size;
					currRowWidth = 1;
				} else {
					xPos += _size;
					++currRowWidth;
				}
			}

			FileOutputStream file = new FileOutputStream(args[0]);
			try {
				file.write(g.getBytes());
			} finally {
				file.close();
			}

		} catch (Exception ex) {
			System.out.println("Exception: " + ex);
		}
	}
}
