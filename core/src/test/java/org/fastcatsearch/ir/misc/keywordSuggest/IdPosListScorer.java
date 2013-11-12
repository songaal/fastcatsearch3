package org.fastcatsearch.ir.misc.keywordSuggest;



/**
 * 여러 list를 받아서 id별 pos를 계산해서 score까지 리턴한다.
 * score에는 위치점수만 존재. 
 * caller에서 keyword별 점수를 확인해서 보여준다. 즉, 위치점수와 keyword점수 이중정렬.
 * */
public class IdPosListScorer {
	
	private IdPosScorer scorer;
	
	public IdPosListScorer(){
		
	}
	
	public void add(IdPosIterator idPosIterator) {
		if (idPosIterator.size() > 0) {
			if (scorer == null) {
				scorer = new UnaryIdPosScorer(idPosIterator);
			} else {
				scorer = new AndIdPosScorer(scorer, idPosIterator);
			}
		}
	}
	
	public boolean next(IdPosScore idPosScore){
		if(scorer == null){
			return false;
		}
		return scorer.next(idPosScore);
	}
	
	
	//pos가 4이상 떨어져있으면 버린다. 사이에 2 단어까지는 가능. 
	public static interface IdPosScorer {
		public boolean next(IdPosScore idPosScore);
	}
	
	
	public static class UnaryIdPosScorer implements IdPosScorer {
		private IdPosIterator iterator;
		
		public UnaryIdPosScorer(IdPosIterator iterator){
			this.iterator = iterator;
		}
		@Override
		public boolean next(IdPosScore idPosScore) {
			return iterator.next(idPosScore);
		}
		
	}
	
	public static class AndIdPosScorer implements IdPosScorer {
		private static int MAX_GAP = 3;
		
		private IdPosScorer list1;
		private IdPosIterator list2;
		
		private boolean hasNext1;
		private boolean hasNext2;
		
		private IdPosScore idPos1 = new IdPosScore();
		private IdPosScore idPos2 = new IdPosScore();
		
		public AndIdPosScorer(IdPosScorer idPosScorer, IdPosIterator nextIdPosIterator){
			this.list1 = idPosScorer;
			this.list2 = nextIdPosIterator;	
		}
		
		public boolean next(IdPosScore idPosScore){

			OUTTER: while(true){
				hasNext1 = list1.next(idPos1);
				hasNext2 = list2.next(idPos2);
	
				
				if(hasNext1 && hasNext2){
					int doc1 = idPos1.id();
					int doc2 = idPos2.id();
					
					while(hasNext1 && hasNext2 && (doc1 != doc2)){
						while(hasNext1 && (doc1 < doc2)){
							hasNext1 = list1.next(idPos1);
							doc1 = idPos1.id();
						}
						
						while(hasNext2 && (doc1 > doc2)){
							hasNext2 = list2.next(idPos2);
							doc2 = idPos2.id();
						}
					}
					
					if(hasNext1 && hasNext2 && (doc1 == doc2)){
						int score = idPos1.score();
						
						//pos를 보고 score를 갱신한다.
						int gap = idPos2.pos() - idPos1.pos();
						//gap이 0일순 없다.
						if(gap > 0){
							if(gap > MAX_GAP){
								//안됨.
								continue OUTTER;
							}else{
								score += gap;
							}
						}else{
							if(-gap < MAX_GAP){
								//안됨..
								continue OUTTER;
							}else{
								score -= (gap + 1); //역방향이면 1을 더뺀다.
							}
						}
						
						idPosScore.set(idPos2);
						idPosScore.setScore(score);
						return true; 
					}
					
					return false;
				}
				
				//절1과 절2중 하나라도 끝나면 AND 집합도 더이상 없는것이다.
				return false;
			}
		
		}
	}
	
	public static class IdPosScore extends IdPos {
		private int score;

		public int score() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}
		
		@Override
		public String toString(){
			return "[IdPosScore]"+ id + " : " + pos + ":" + score;
		}
	}
}
