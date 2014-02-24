package org.fastcatsearch.al;

import org.fastcatsearch.ir.io.ElementVector;

public abstract class HashFunctions {
	public static final HashFunctions RSHash = new RSHash();
	public static final HashFunctions DJBHash = new DJBHash();
	public static final HashFunctions SDBMHash = new SDBMHash();
	public static final HashFunctions JSHash = new JSHash();
	public static final HashFunctions ELFHash = new ELFHash();
	public static final HashFunctions BKDRHash = new BKDRHash();
	public static final HashFunctions APHash = new APHash();
	public static final HashFunctions PJWHash = new PJWHash();
	
	public static HashFunctions getInstance(String algorithm) {
		if("RSHASH".equalsIgnoreCase(algorithm) || "RS".equalsIgnoreCase(algorithm)) {
			return RSHash;
		} else if("DJBHASH".equalsIgnoreCase(algorithm) || "DJB".equalsIgnoreCase(algorithm)) {
			return DJBHash;
		} else if("SDBMHASH".equalsIgnoreCase(algorithm) || "SDBM".equalsIgnoreCase(algorithm)) {
			return SDBMHash;
		} else if("JSHASH".equalsIgnoreCase(algorithm) || "JS".equalsIgnoreCase(algorithm)) {
			return JSHash;
		} else if("ELFHASH".equalsIgnoreCase(algorithm) || "ELF".equalsIgnoreCase(algorithm)) {
			return ELFHash;
		} else if("BKDRHASH".equalsIgnoreCase(algorithm) || "BKDR".equalsIgnoreCase(algorithm)) {
			return BKDRHash;
		} else if("APHASH".equalsIgnoreCase(algorithm) || "AP".equalsIgnoreCase(algorithm)) {
			return APHash;
		} else if("PJWHASH".equalsIgnoreCase(algorithm) || "PJW".equalsIgnoreCase(algorithm)) {
			return PJWHash;
		}
		return null;
	}
	public abstract int hash(ElementVector cs, int bucketSize);
	public abstract int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase);
	
	protected char toUpperChar(int ch){
		if ((ch <= 'z' && ch >= 'a')) { // 소문자이면..
			ch -= 32;
		}
		return (char) ch;
	}
}

class RSHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int b = 378551;
		int a = 63689;
		int hashValue = 0;
		for(int i=0; i < cs.length(); i++) {
			hashValue = hashValue * a + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
			a = a * b;
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}
}

class DJBHash extends HashFunctions {
	@Override
	public int hash (ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = 5381;
		for(int i=0; i < cs.length(); i++) {
			hashValue = ((hashValue << 5) + hashValue) + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class SDBMHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = 0;
		for(int i=0; i < cs.length(); i++) {
			hashValue = (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i)) + (hashValue << 6) + (hashValue << 16) - hashValue;
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class JSHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = 1315423911;
		for(int i=0; i < cs.length(); i++) {
			hashValue ^= ((hashValue << 5) + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i)) + (hashValue >> 2));

		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class ELFHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = 0;
		int x = 0;
		for(int i=0; i < cs.length(); i++) {
			hashValue = (hashValue << 4) + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
			if(( x = hashValue & 0xF0000000) != 0) {
				hashValue ^= ( x >> 24);
				hashValue &= ~x;
			}
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class BKDRHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int seed = 131; // 31 131 1313 13131 131313 etc..
		int hashValue = 0;

		for(int i=0; i < cs.length(); i++) {
			hashValue = hashValue * seed + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class APHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = 0;

		for(int i=0; i < cs.length(); i++) {
			if ((i & 1) == 0) {
				hashValue ^= ((hashValue << 7) ^ (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i)) ^ (hashValue >> 3));
			} else {
				hashValue ^= (~((hashValue << 11) ^ (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i)) ^ (hashValue >> 5)));
			}
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}

class DEKHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int hashValue = cs.length();
		for(int i = 0; i < cs.length(); i++) {
			hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
	
}

class PJWHash extends HashFunctions {
	@Override
	public int hash(ElementVector cs, int bucketSize, boolean isIgnoreCase) {
		int BitsInUnignedInt = 4 * 8;
		int ThreeQuarters = ((BitsInUnignedInt  * 3) / 4);
		int OneEighth = (BitsInUnignedInt / 8);
		int HighBits = (0xFFFFFFFF) << (BitsInUnignedInt - OneEighth);
		int hashValue = 0;
		int test = 0;
		for(int i=0; i < cs.length(); i++) {
			hashValue = (hashValue << OneEighth) + (isIgnoreCase ? toUpperChar(cs.elementAt(i)) : cs.elementAt(i));
			if ((test = hashValue & HighBits) != 0) {
				hashValue = ((hashValue ^ (test >> ThreeQuarters)) & (~HighBits));
			}
		}
		return (hashValue & 0x7FFFFFFF) % bucketSize;
	}

	@Override
	public int hash(ElementVector cs, int bucketSize) {
		return hash(cs, bucketSize, false);
	}
}