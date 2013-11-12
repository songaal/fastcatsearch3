package org.fastcatsearch.ir.misc.keywordSuggest;


/**
 * 여러 리스트를 정렬 병합해서 하나같이 보여준다.
 * */
public class CompositeIdPosIterator extends IdPosIterator {

	private IdPosIterator iterator;

	public CompositeIdPosIterator() {
	}

	public void add(IdPosIterator idPosIterator) {
		if (idPosIterator.size() > 0) {
			if (iterator == null) {
				iterator = idPosIterator;
			} else {
				iterator = new OrIdPosIterator(iterator, idPosIterator);
			}
		}
	}

	public boolean next(IdPos idPos) {
		if(iterator == null){
			return false;
		}
		return iterator.next(idPos);
	}

	public static class OrIdPosIterator extends IdPosIterator {
		private IdPosIterator list1;
		private IdPosIterator list2;

		private boolean hasNext1;
		private boolean hasNext2;

		private IdPos idPos1 = new IdPos();
		private IdPos idPos2 = new IdPos();

		public OrIdPosIterator(IdPosIterator list1, IdPosIterator list2) {
			this.list1 = list1;
			this.list2 = list2;

			hasNext1 = list1.next(idPos1);
			hasNext2 = list2.next(idPos2);
		}

		public boolean next(IdPos idPos) {
			if (hasNext1 || hasNext2) {
				int id1 = idPos1.id();
				int id2 = idPos2.id();

				if (hasNext1 && hasNext2) {
					if (id1 < id2) {
						idPos.set(idPos1);
						hasNext1 = list1.next(idPos1);
					} else if (id1 > id2) {
						idPos.set(idPos2);
						hasNext2 = list2.next(idPos2);
					} else {
						// 동일키워드에 키워드 id까지 동일한 경우는 없다.
						// 다만 동일키워드를 중복입력했을때 발생가능.
						idPos.set(idPos1);
						hasNext1 = list1.next(idPos1);
						hasNext2 = list2.next(idPos2);
					}
					return true;
				}

				if (hasNext1) {
					idPos.set(idPos1);
					hasNext1 = list1.next(idPos1);
					return true;
				}

				if (hasNext2) {
					idPos.set(idPos2);
					hasNext2 = list2.next(idPos2);
					return true;
				}

			}

			return false;
		}

	}

}
