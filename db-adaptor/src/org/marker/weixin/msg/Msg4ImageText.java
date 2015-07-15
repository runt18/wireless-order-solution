package org.marker.weixin.msg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Msg4ImageText extends Msg {
	private String articleCount;
	private List<Data4Item> items = new ArrayList<>(3);

	public Msg4ImageText(Msg received) {
		super(received, Msg4Head.MsgType.MSG_TYPE_IMAGE_TEXT);
	}

	public Msg4ImageText(Msg4Head head) {
		super(head, Msg4Head.MsgType.MSG_TYPE_IMAGE_TEXT);
	}
	
	@Override
	public void write(Document document) {
		Element root = document.createElement("xml");
		this.head.write(root, document);
		Element articleCountElement = document.createElement("ArticleCount");
		articleCountElement.setTextContent(this.articleCount);

		Element articlesElement = document.createElement("Articles");
		int size = Integer.parseInt(this.articleCount);
		for (int i = 0; i < size; i++) {
			Data4Item currentItem = (Data4Item) this.items.get(i);
			Element itemElement = document.createElement("item");
			Element titleElement = document.createElement("Title");
			titleElement.setTextContent(currentItem.getTitle());
			Element descriptionElement = document.createElement("Description");
			descriptionElement.setTextContent(currentItem.getDescription());
			Element picUrlElement = document.createElement("PicUrl");
			picUrlElement.setTextContent(currentItem.getPicUrl());
			Element urlElement = document.createElement("Url");
			urlElement.setTextContent(currentItem.getUrl());
			itemElement.appendChild(titleElement);
			itemElement.appendChild(descriptionElement);
			itemElement.appendChild(picUrlElement);
			itemElement.appendChild(urlElement);

			articlesElement.appendChild(itemElement);
		}
		root.appendChild(articleCountElement);
		root.appendChild(articlesElement);

		document.appendChild(root);
	}

	@Override
	public void read(Document document) {
		this.articleCount = document.getElementsByTagName("ArticleCount").item(0).getTextContent();
		for(int i = 0 ; i < document.getElementsByTagName("Articles").getLength(); i++){
			NodeList items = document.getElementsByTagName("Articles").item(i).getChildNodes();
			for(int j = 0; j < items.getLength(); j++){
				Element eachItem = (Element)items.item(j);
				addItem(new Data4Item(eachItem.getElementsByTagName("Title").item(0).getTextContent(),
							  eachItem.getElementsByTagName("Description").item(0).getTextContent(),
							  eachItem.getElementsByTagName("PicUrl").item(0).getTextContent(),
							  eachItem.getElementsByTagName("Url").item(0).getTextContent()));
			}
		}
	}

	public Msg4ImageText addItem(Data4Item item) {
		this.items.add(item);
		reflushArticleCount();
		return this;
	}

	public void removeItem(int index) {
		this.items.remove(index);
		reflushArticleCount();
	}

	private void reflushArticleCount() {
		this.articleCount = Integer.toString(this.items.size());
	}
	
	public List<Data4Item> getItems(){
		return Collections.unmodifiableList(this.items);
	}
	
	@Override
	public String toString(){
		final String sep = System.getProperty("line.separator");
		StringBuilder msg = new StringBuilder();
		msg.append("ArticleCount : " + this.articleCount).append(sep);
		for(Data4Item item : this.items){
			msg.append("|-").append(item.toString()).append(sep);
		}
		return msg.toString();
	}
}
