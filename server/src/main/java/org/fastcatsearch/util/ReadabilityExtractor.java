package org.fastcatsearch.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadabilityExtractor extends ContentExtractor{
	
	private static Logger logger = LoggerFactory.getLogger(ReadabilityExtractor.class);
	//가능성이 없는 태그 id와 class
	private static Pattern unlikelyCandidates = Pattern.compile("combx|comment|community|disqus|extra|foot|header|menu|remark|rss|shoutbox|sidebar|sponsor|ad-break|agegate|pagination|pager|popup|tweet|twitter|facebook|me2day|yozm",Pattern.CASE_INSENSITIVE); 
	//가능성이 높은 태그 id와 class
	private static Pattern okMaybeItsACandidate = Pattern.compile("and|article|body|column|main|shadow",Pattern.CASE_INSENSITIVE);   
	//태그의 id/class 무게 측정시 사용
	//무게를 가해주는 id나 class
	private static Pattern positive = Pattern.compile("article|body|content|entry|hentry|main|page|pagination|post|text|blog|story",Pattern.CASE_INSENSITIVE);   
	//무게를 감해주는 id나 class
	private static Pattern negative = Pattern.compile("combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|outbrain|promo|related|scroll|shoutbox|sidebar|sponsor|shopping|tags|tool|widget",Pattern.CASE_INSENSITIVE);
	//문자열 블록유형 태그
	private static Pattern divToPElements = Pattern.compile("<(a|blockquote|dl|div|img|ol|p|pre|table|ul)",Pattern.CASE_INSENSITIVE); 
	// 미리 삭제할 태그
	private static Pattern toRemove = Pattern.compile("(object|th|h1|button|iframe)",Pattern.CASE_INSENSITIVE);  

	private static String blankKillReg = "\\s{2,}";
	
	public String extract(String source){
		StringBuilder result = new StringBuilder();
		//html을 xml로 빠꾸어줌. htmlcleaner을 사용함.
		//1) 우선 script 태그를 제거한다.
		//2) CSS코드 삭제와 태크 매칭이 않된것을 보완한다.
		source = this.toXML(source);
		// org.w3c.dom tree 만들기.
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(new ByteArrayInputStream(source.getBytes()));//sb.build(new ByteArrayInputStream(source.getBytes()));
		} catch (ParserConfigurationException e) {
			logger.error("",e);
		} catch (SAXException e) {
			logger.error("",e);
		} catch (IOException e) {
			logger.error("",e);
		}
		
		NodeList allElements = document.getElementsByTagName("*");
		//점수 줄 대상 노드들.nodesToScore
        List<Node> nodesToScore = new ArrayList<Node>();
        for (int i = 0; i < allElements.getLength(); i++) {
        	Node n = allElements.item(i);
        	String unlikelyMatchString = "";
        	Node cn = n.getAttributes().getNamedItem("class");
        	if(cn != null) unlikelyMatchString += cn.getNodeValue();
        	Node in = n.getAttributes().getNamedItem("id");
        	if(in != null) unlikelyMatchString += in.getNodeValue();
        	//3) 내용태그일 가능성이 없는 태그들을 처리대상에서 제거한다. 
        	if (toRemove.matcher(unlikelyMatchString).find()) {
//        		n.setTextContent("");
        		n.getParentNode().removeChild(n);
        		continue;
			}
        	if (unlikelyCandidates.matcher(unlikelyMatchString).find()
        		&&	 !okMaybeItsACandidate.matcher(unlikelyMatchString).find()
        		&& !"body".equalsIgnoreCase(n.getNodeName())) {
//				n.setTextContent("");
        		n.getParentNode().removeChild(n);
				continue;
			}
        	//4) p,td,pre태그를 수집하고 하위에 text블록유형태그가 없는 div태그를 수집하고 하위에 text블록유형태그가 있는 div는 이 text블록유형태그들을 p로 바꿔준다.
        	if ("P".equalsIgnoreCase(n.getNodeName()) || "TD".equalsIgnoreCase(n.getNodeName()) ||"PRE".equalsIgnoreCase(n.getNodeName())) {
        			nodesToScore.add(n);
			}
        	if ("DIV".equalsIgnoreCase(n.getNodeName())) {
        		String nodeStr = nodeToString(n);
        		
        		if (!divToPElements.matcher(nodeStr).find()) {
					nodesToScore.add(n);
				} else {
					NodeList childs = n.getChildNodes();
					for (int j = 0; j < childs.getLength(); j++) {
						Node childNode = childs.item(j);
						String nodeName = childNode.getNodeName();
						if ("#text".equals(nodeName)) {// Node.TEXT_NODE
							Element newElem = document.createElement("p");
							newElem.setTextContent(childNode.getTextContent());
							if(childNode.getParentNode() != null)
								childNode.getParentNode().replaceChild(newElem, childNode);
						}
					}
				}
			}
        }
	/**
	 * 5) nodesToScore를 loop 돌면서 각 태그에 점수를 준다. 
	 * 1. 태그내 text의 길이가 25보다 짧으면 점수를 주지않는다. 
	 * 2. 기본적으로 1점을 준다.
	 *	3. 콤마개수만큼 점수를 더한다.
	 *	4. text길이 100 마다 1점씩 더하는데 최고 3점까지 더할수 있다.
	 *	5. 해당 노드의 점수를 부모노드에 더해주고 증조부모노드에 점수의 절반을 더해준다.
	 *	6. 점수를 더해주기전에 부모노드와 증조부노드를 FastcatDomNode로 초기화해준다. 초기화는 점수를 기본인 0으로 시작해서 태그에 따라 점수를 더하고 감한다.
	 *  7. 마지막으로 부모노드와 증조부모노드만 후보리스트에 저장한다.
	 **/
        	List<FastcatSearchDomNode> candidates = new ArrayList<FastcatSearchDomNode>();
        	for (int j = 0; j < nodesToScore.size(); j++) {
        		Node nn = nodesToScore.get(j);
        		Node parentNode = nn.getParentNode();
        		FastcatSearchDomNode pNode = null;
        		Node grandParentNode = parentNode != null ? parentNode.getParentNode() : null;
        		FastcatSearchDomNode gNode = null;
        		String nodeText = nn.getTextContent().trim();
        		if (parentNode == null) {
					continue;
				}
        		if (nodeText==null || nodeText.length() < 25) {
					continue;
				}
				pNode = initializeNode(parentNode);
	            candidates.add(pNode);
                if(grandParentNode != null) {
                	gNode = initializeNode(grandParentNode);
                	candidates.add(gNode);
                }
        		int contentScore = 0;
                contentScore += 1;
                contentScore += nodeText.split(",").length;
                contentScore += Math.min(Math.floor(nodeText.length() / 100), 3);
                pNode.setContentScore(pNode.getContentScore() + contentScore);
                if(gNode!=null)
                	gNode.setContentScore(gNode.getContentScore() + contentScore/2);
			}
        	//6) 후보리스트에서 점수가 제일 높은 노드를 찾는다.못 찾으면 그냥 body를 사용한다.
        	FastcatSearchDomNode topCandidate = null;
        	for (int j = 0; j <candidates.size(); j++) {
				FastcatSearchDomNode fdn = candidates.get(j);
				fdn.setContentScore(fdn.getContentScore()*(1-getLinkDensity((Element)fdn.getNode())));
				if (topCandidate == null || fdn.getContentScore() > topCandidate.getContentScore()) {
					topCandidate = fdn;
				}
			}
            if (topCandidate == null)
            {
            	Element newElem = document.createElement("DIV");
            	Node bodyNode = document.getElementsByTagName("body").item(0);//document.getFirstChild();
            	if (bodyNode == null) {
            		bodyNode = document.getFirstChild(); 
				}
				newElem.setTextContent(bodyNode.getTextContent());
				bodyNode.getParentNode().replaceChild(newElem, bodyNode);
				topCandidate = initializeNode(newElem);
            }
            
    /**
     * 7) 점수가 제일 높은 노드의 부모노드의 자식노드들(sibling)을 loop돌면서 본문으로 택할지 판단한다.
     * 1. threshold를 제일 높은 점수의 20% 와 10 사이에서 높은 것으로 정한다. 
	 * 2. 점수가 제일 높은 노드는 무조건 선택한다.
	 * 3. 노드의 class 이름이 점수가 제일 높은 노드랑 같은 노드는 보너스점수를 주는데 보너스점수를 제일 높은 점수의 20%로 준다.이 보너스점수와 해당 노드의 기존 점수의 합이 한계점보다 높거나 같으면 택한다.
     **/
            double siblingScoreThreshold = Math.max(10, topCandidate.getContentScore() * 0.2);
            NodeList siblingNodes = topCandidate.getNode().getParentNode().getChildNodes();
        	for (int j = 0; j < siblingNodes.getLength(); j++) {
        		Node siblingNode = siblingNodes.item(j);
                boolean append = false;
                if(siblingNode == topCandidate.getNode())
                {
                    append = true;
                }
                double contentBonus = 0;
                Node siblingNodeClassName = siblingNode.getAttributes() != null?siblingNode.getAttributes().getNamedItem("class"):null;
                String classNameSibling = "";
                if(siblingNodeClassName != null) classNameSibling = siblingNodeClassName.getNodeValue();
                Node topNodeClassName = topCandidate.getNode().getAttributes().getNamedItem("class");
                String classNameTop = "";
                if(topNodeClassName != null) classNameTop = topNodeClassName.getNodeValue();
                if(classNameTop.equalsIgnoreCase(classNameSibling) && !"".equals(classNameTop)) {
                    contentBonus += (topCandidate.getContentScore() * 0.2);
                }
                if((initializeNode(siblingNode).getContentScore()+contentBonus) >= siblingScoreThreshold)
                {
                    append = true;
                }
                if(append) {
                	//선택된 노드 후처리.
                    if (siblingNode instanceof Element) {
	                   	 Element e = (Element)siblingNode;
	                   	 //선택된 노드의 모든 하위 노드들을 loop돌면서 이 노드들의 부모노드의 linkDensity가 0.7보다 높은 노드는 광고나 무의미한 링크일 가능성이 높기때문에 삭제한다.
	                   	 //선택된 노드의 모든 하위 노드들중에서 h2,h3인 태그가 하나만 있으면 제거한다. 하나면 광고 타이틀이나 댓글타이틀일 가능성이 높기 때문이다.
	                   	 NodeList nl = e.getElementsByTagName("*");
	                   	 for (int i = 0; i < nl.getLength(); i++) {
	   						Element elm = (Element)nl.item(i);
	   						Node pNode = elm.getParentNode();
	   						double linkDensity = getLinkDensity((Element)pNode);
	   						if (linkDensity >= 0.7) {
	   							pNode.removeChild(elm);
	   						}
	   						else{
		   						 if(e.getElementsByTagName("h3").getLength() == 1) {
			     					pNode.removeChild(elm);
			     				}else if(e.getElementsByTagName("h2").getLength() == 1) {
			     					pNode.removeChild(elm);
			     				}
	   						}
	                   	 }
                    }
                    String finalStr = siblingNode.getTextContent().replaceAll(blankKillReg, " ").trim();
                	result.append(finalStr + "\n");
                }
			}
        	
		return result.toString();
	}
	
	/**
	 * js라이브러리중 선택된 노드 후처리중 사용했던 메소드.
	 * 효과적이지 못한것같아서 임시 버림.
	 * Clean an element of all tags of type "tag" if they look fishy.
     * "Fishy" is an algorithm based on content length, classnames, link density, number of images & embeds, etc.
	 * @param node
	 * @param append
	 */
//	private void cleanConditionally(Node node) {
//		Node parent = node.getParentNode();
//		Element e = (Element)node;
//		int weight = getClassWeight(node);
//		double contentScore = initializeNode(node).getContentScore();
//		if (weight+contentScore < 0) {
//			if (parent != null) {
//				parent.removeChild(node);
//			}
//		}else if(node.getTextContent().split(",").length < 10){
//			 /**
//             * If there are not very many commas, and the number of
//             * non-paragraph elements is more than paragraphs or other ominous signs, remove the element.
//            **/
//			
//			int pLength = e.getElementsByTagName("p").getLength();
//			int imgLength = e.getElementsByTagName("img").getLength();
//			int liLength = e.getElementsByTagName("li").getLength()-100;
//			int inputLength = e.getElementsByTagName("input").getLength();
//			int embedCount = e.getElementsByTagName("embed").getLength();
//			double linkDensity = getLinkDensity(e);
//			int contentLength = node.getTextContent().length();
//			if ( imgLength > pLength ) {
//                if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            } else if(liLength > pLength && !"ul".equalsIgnoreCase(e.getTagName()) && !"ol".equalsIgnoreCase(e.getTagName())) {
//                if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            } else if( inputLength > Math.floor(pLength/3) ) {
//            	if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            } else if(contentLength < 25 && (imgLength == 0 || imgLength > 2) ) {
//            	if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            } else if(weight < 25 && linkDensity > 0.2) {
//            	if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            } else if(weight >= 25 && linkDensity > 0.5) {
//            	if (parent != null) {
//    				parent.removeChild(e);
//    			}
//            } else if((embedCount == 1 && contentLength < 75) || embedCount > 1) {
//            	if (parent != null) {
//    				parent.removeChild(node);
//    			}
//            }
//			
//		}
//	}
	
	 /**
	 * text블록에서 링크걸린  text의 비율 계산하는 메소드.
	 * @param Element
	 * @return number (float)
	**/
    private double getLinkDensity(Element e) {
        NodeList links = e.getElementsByTagName("a");
        String textContent = e.getTextContent().replaceAll(blankKillReg, "").trim();
        int textLength = textContent.length();
        if (textLength <= 0) {
			return 1;
		}
        int linkLength = 0;
        for (int i = 0; i < links.getLength(); i++) {
        	String linkContent = links.item(i).getTextContent().replaceAll(blankKillReg, "");
        	linkLength += linkContent.trim().length();
		}
        return (double)linkLength / textLength;
    }
	
	 /**
     * org.w3c.dom 의 Node를 점수속성이 있는 FastcatDomNode로 초기화해주는 메소드.
     * @param Element
     * @return FastcatDomNode
    **/
    private FastcatSearchDomNode initializeNode(Node node) {
    	FastcatSearchDomNode fastNode = new FastcatSearchDomNode();
    	int contentScore = 0;
    	if ("DIV".equalsIgnoreCase(node.getNodeName())) {
    		contentScore += 5;
		} else if("PRE".equalsIgnoreCase(node.getNodeName())){

		} else if("TD".equalsIgnoreCase(node.getNodeName())){

		} else if("BLOCKQUOTE".equalsIgnoreCase(node.getNodeName())){
			contentScore += 3;
		} else if("ADDRESS".equalsIgnoreCase(node.getNodeName())){

		} else if("OL".equalsIgnoreCase(node.getNodeName())){

		} else if("UL".equalsIgnoreCase(node.getNodeName())){

		} else if("DL".equalsIgnoreCase(node.getNodeName())){

		} else if("DD".equalsIgnoreCase(node.getNodeName())){

		} else if("DT".equalsIgnoreCase(node.getNodeName())){

		} else if("LI".equalsIgnoreCase(node.getNodeName())){

		} else if("FORM".equalsIgnoreCase(node.getNodeName())){
			contentScore -= 3;
		} else if("H1".equalsIgnoreCase(node.getNodeName())){

		} else if("H2".equalsIgnoreCase(node.getNodeName())){

		} else if("H3".equalsIgnoreCase(node.getNodeName())){

		} else if("H4".equalsIgnoreCase(node.getNodeName())){

		} else if("H5".equalsIgnoreCase(node.getNodeName())){

		} else if("H6".equalsIgnoreCase(node.getNodeName())){

		} else if("TH".equalsIgnoreCase(node.getNodeName())){
			contentScore -= 5;
		}
    	// class weight
    	contentScore += getClassWeight(node);
    	fastNode.setContentScore(contentScore);
    	fastNode.setNode(node);
       
        return fastNode;
    }
    
    /**
     * 태그의 id와 class에 근거해 태그의 '무게'를 계산하는 메소드.
     * @param Element
     * @return number (Integer)
    **/
  //typeof : number,string,boolean,object,function,undefined
    private int getClassWeight(Node node) {
        int weight = 0;
        /* Look for a special classname */
       Node cn = node.getAttributes()!=null?node.getAttributes().getNamedItem("class"):null;
       String className = "";
        if(cn != null) className = cn.getNodeValue();
    	if (negative.matcher(className).find()) {
    		weight -= 25;
		}
    	if (positive.matcher(className).find()) {
    		weight += 25;
		}
    	/* Look for a special ID */
        Node in = node.getAttributes()!=null?node.getAttributes().getNamedItem("id"):null;
        String idName = "";
        if(in != null) idName = in.getNodeValue();
    	if (negative.matcher(idName).find()) {
    		weight -= 25;
		}
    	if (positive.matcher(idName).find()) {
    		weight += 25;
		}
        return weight;
    }
	// 파라미터 node의 하위 모든 태그의 이름을 얻는 메소드.
	private String nodeToString(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
        	String nodeName = nodeList.item(i).getNodeName();
        	if (!"#text".equals(nodeName)) {
        		sb.append(" <"+nodeName);
			}
			
		}
        return sb.toString();
    }
	
	/**
	 * htmlcleaner로 html string을 xml string으로 바꿔주는 메소드.
	 * @param source
	 * @return
	 */
	private String toXML(String source){
		try {
			CleanerProperties props = new CleanerProperties();
			props.setTranslateSpecialEntities(true);
			props.setOmitComments(true);
			props.setPruneTags("script,style");
			// namespace를 무시한다.
			props.setNamespacesAware(false);
			props.setAdvancedXmlEscape(true);
			props.setTranslateSpecialEntities(true);
			HtmlCleaner cl = new HtmlCleaner(props);
			TagNode tagNode = cl.clean(source);
			source = new PrettyXmlSerializer(props).getXmlAsString(tagNode);
		} catch (IOException e) {
			logger.error("",e);
		}
		return source;
	}
	//test용
	public static void main(String[] args) throws IOException {
		
		String source = getHTML("http://www.etnews.com/201112020133?mc=m_012_00001");
	
		System.out.println(new ReadabilityExtractor().extract(source));
	}
	//test용
	public static String getHTML(String strURL) throws IOException {
		URL url = new URL(strURL);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(),"euc-kr"));
		String s = "";
		StringBuilder sb = new StringBuilder("");
		while ((s = br.readLine()) != null) {
			sb.append(s + "\n");
		}
		return sb.toString();
	}
	
}
