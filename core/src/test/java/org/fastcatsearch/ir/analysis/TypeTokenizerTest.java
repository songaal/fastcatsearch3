/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.analysis;

import org.fastcatsearch.ir.analysis.NaiveTermTokenizer;
import org.fastcatsearch.ir.io.CharVector;

import junit.framework.TestCase;


public class TypeTokenizerTest extends TestCase {
	public void test1(){
		NaiveTermTokenizer tokenizer = new NaiveTermTokenizer();
		String str = "Purpose: The remediation of soil polluted by polycyclic aromatic hydrocarbons (PAHs) is of great " +
				"importance due to the persistence and carcinogenic properties of PAHs. " +
				"Phytoremediation has been regarded as a promising alternative among suggested approaches. " +
				"For the establishment of highly effective remediation method and better understanding of the remediation mechanisms " +
				"by plants, the potentials of three plant species and their planting patterns on the remediation efficacy " +
				"were studied by pot experiments. " +
				"Materials and methods: Soils were amended with phenanthrene (Phe) or pyrene (Pyr) at five levels ranging from 0~322 " +
				"mg kg-1 , then incubated for 30 days. At each level of PAHs, four treatments were set as: (a) treatments 1, no plant " +
				"and microbe-inhibited with addition of 0.1%NaN3 ; (b) treatments 2, no plant and without the addition of NaN3 ; " +
				"(c) treatments 3, microbe-inhibited and planted; (d) treatments 4, planted and without inhibition of microbes. " +
				"For each planted treatment, single cropping of rape (Medicago sativa), alfalfa (Brassica campestris), and white " +
				"clover (Trifolium repens), and mixed cropping of rape with alfalfa and white clover were adapted. Seedlings of 7 " +
				"days old were then cultivated. After 70 days cultivation, soil and plant of each treatment were sampled for analyses" +
				" of PAHs. Results and discussion: Rape, alfalfa, and white clover all significantly promoted the degradation of PAHs" +
				" in soils; alfalfa and white clover showed higher efficiencies for the removal of PAHs. Averagely, about 41.46% of " +
				"Phe or 33.69% of Pyr were removed from soils after 70-day plantation of alfalfa, and 38.75% of Phe or 36.29% of Pyr " +
				"removed by white clover, as compared to the much lower degradation rates of 22.57% of Phe or 18.24% of Pyr for " +
				"non-planted controls. Mixed cropping significantly enhanced the remediation efficiencies as compared to single " +
				"cropping; about 74.87% of Phe or 62.81% of Pyr were removed by mixed cropping of rape and alfalfa, and 72.01% of " +
				"Phe or 68.44% of Pyr by mixed cropping of rape and white clover. Conclusions: The presence of vegetation " +
				"significantly promoted the dissipation of Phe or Pyr in soils and remediation efficiency varied greatly among plant species and cropping patterns. Rape had the lowest ability for the removal of PAHs, while alfalfa showed highest ability for the remediation of Phe and white clover was most effective for Pyr. Mixed cropping of rape with alfalfa or white clover was however far better for the remediation of soil PAHs than single cropping. ? " +
				"2010 Springer-Verlag.... $130,000...";
		
//		str = "HospitUn Rambla S.L. nano* *aa *.. asf?s";
		char[] charr = str.toCharArray();
		tokenizer.setInput(charr);
		CharVector token = new CharVector();
		System.out.println("--------------");
		while(tokenizer.nextToken(token)){
			System.out.println(">>"+token);
		}
	}
}
