package org.fastcatsearch.al;

import java.util.List;

public class LevenshteinDistance {
	
	public static double similar(int dist, int size1, int size2) {
		int min = size1;
		int max = size2;
		if(min > size2) {
			min = size2;
		}
		if(max < size1) {
			max = size1;
		}
		
		return (max - dist) * 100 / min;
	}
	
	
	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}
	
	@SuppressWarnings("rawtypes")
	public static int computeDistance(List phr1, 
			List phr2) {
		
		int[][] distance = new int[phr1.size() + 1][phr2.size() + 1];
		
		for(int i = 0; i <= phr1.size(); i++) {
			distance[i][0] = i;
		}
		for(int j = 0; j <= phr2.size(); j++) {
			distance[0][j] = j;
		}
		
		for(int i = 1; i <= phr1.size(); i++) {
			for(int j = 1; j <= phr2.size(); j++) {
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+((phr1.get(i - 1).equals(phr2.get(j - 1))) ? 0 : 1));
			}
		}
		
		return distance[phr1.size()][phr2.size()];
	}

	public static int computeDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distance[i][0] = i;
		}
		for (int j = 1; j <= str2.length(); j++) {
			distance[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));
			}
		}

		return distance[str1.length()][str2.length()];
	}
}