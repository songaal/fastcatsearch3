package org.fastcatsearch.ir.index.temp;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempSearchFieldMergerTest {
	private static Logger logger = LoggerFactory.getLogger(TempSearchFieldMergerTest.class);
	
	protected int[] heap;
	protected MockSearchFieldReader[] reader;
	protected int flushCount;
	
	
	class MockSearchFieldReader {
		CharVector[] termList;
		int pos;
		CharVector cv;
		int seq;
		public MockSearchFieldReader(String str, int seq) {
			String[] list = str.split(" ");
			this.termList = new CharVector[list.length];
			for (int i = 0; i < list.length; i++) {
				termList[i] = new CharVector(list[i]);
			}
			this.seq = seq;
		}

		public CharVector term() {
			return cv;
		}

		public boolean next() {
			if(pos < termList.length){
				cv = termList[pos++];
				return true;
			}
			cv = null;
			return false;
		}
		
		public int seq(){
			return seq;
		}
		
		public String toString(){
			return cv + "("+seq+")";
		}
		
	}
	

	@Test
	public void test() {
		flushCount = 3;
		
		reader = new MockSearchFieldReader[flushCount];
		reader[0] = new MockSearchFieldReader("82", 0);
		reader[1] = new MockSearchFieldReader("1 82", 1);
		reader[2] = new MockSearchFieldReader("82", 2);
		reader[0].next();
		reader[1].next();
		reader[2].next();
		
		
		CharVector cv = null, cvOld = null;
//		int prevSeq = -1;
		boolean isNumericField = true;
		
		makeHeap(flushCount, isNumericField);
		printHeap();
		
		while(true){
			
			int idx = heap[1];
			cv = reader[idx].term();
			if(cv == null){
				//all readers are done
				break;
			}
			
			System.out.println("####### "+cv + " >> seq=" +reader[idx].seq());
			
			reader[idx].next();
			
			cvOld = cv;
//			prevSeq = reader[idx].seq();
			printHeap();
			heapify(1, flushCount, isNumericField);
			printHeap();
			
		} //while(true)
	}
	
	
	private void printHeap() {
		for (int i = 1; i < heap.length; i++) {
			System.out.println("Heap-"+i+" : "+reader[heap[i]]);
		}
	}


	protected void makeHeap(int heapSize, boolean isNumeric){
		heap = new int[heapSize + 1];
		//index starts from 1
		for (int i = 0; i < heapSize; i++) {
			heap[i+1] = i;
		}
		
		int n = heapSize >> 1; //last inner node index
		
		for (int i = n; i > 0; i--) {
			heapify(i, heapSize, isNumeric);
		}
		
	}
	
	protected void heapify(int idx,int heapSize, boolean isNumeric){
		
		int temp = -1;
		int child = -1;

		while(idx <= heapSize){
			int left = idx << 1;// *=2
			int right = left + 1;
//			System.out.println(left+" , "+idx + " , "+right);
			if(left <= heapSize){
				if(right <= heapSize){
					int c = compareKey(left, right, isNumeric);
					if(c < 0){
						child = left;
					}else if(c > 0){
						child = right;
					}else{
						System.out.println("하위 value 둘이 같아서 seq확인.");
						//같다면 seq가 작은게 우선.
						int a = heap[left];
						int b = heap[right];
						System.out.println(reader[a].seq()+":"+reader[b].seq());
						if(reader[a].seq() < reader[b].seq()){
							System.out.println("하위에서 left채택!");
							child = left;
						}else{
							System.out.println("하위에서 right채택!");
							child = right;
						}
					}
				}else{
					//if there is no right el.
					child = left;
				}
			}else{
				//no children
				break;
			}
			
			//compare and swap
			System.out.println("하위와 자신을 비교!");
			int c = compareKey(child, idx, isNumeric);
			if(c < 0){
				temp = heap[child];
				heap[child] = heap[idx];
				heap[idx] = temp;
				idx = child;
//				System.out.println("idx1="+idx);
			}else if(c == 0){
				System.out.println("하위와 자신의 value가 같아서 seq확인.");
				//같다면 seq가 작은게 우선.
				int a = heap[idx];
				int b = heap[child];
				if(reader[a].seq() > reader[b].seq()){
					System.out.println("하위의 seq가 작아서 child채택!"+child);
					temp = heap[child];
					heap[child] = heap[idx];
					heap[idx] = temp;
					idx = child;
				}else{
					System.out.println("내것을 그대로 사용.");
					//sorted
					break;
				}
//				System.out.println("idx2="+idx);
			}else{
				//sorted, then do not check child
				break;
			}
			System.out.println("while continue.");
		}
		System.out.println("비교끝.");
	}
	
	protected int compareKey(int one, int another, boolean isNumeric){

		int a = heap[one];
		int b = heap[another];
		
		return compareKey(reader[a].term(), reader[b].term(), isNumeric);
	}
	
	protected int compareKey(CharVector term1, CharVector term2, boolean isNumeric){
		
		//reader gets EOS, returns null
		if(term1 == null && term2 == null){
			return 0;
		}else if(term1 == null)
			return 1;
		else if(term2 == null)
			return -1;
		
		if(isNumeric){
			if(term1.length() != term2.length())
				return term1.length() - term2.length();
		}
		
		int len = (term1.length() < term2.length()) ? term1.length() : term2.length();
				
		for (int i = 0; i < len; i++) {
			if(term1.charAt(i) != term2.charAt(i))
				return term1.charAt(i) - term2.charAt(i);
		}
		
		return term1.length() - term2.length();
	}

}
