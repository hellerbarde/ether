package ch.fhnw.pstark.woipv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class FontAtlas
{
	private class Node
	{
		public Rectangle rect;
		public Node child[];
		public BufferedImage image;
		
		public Node(int x, int y, int width, int height)
		{
			rect = new Rectangle(x, y, width, height);
			child = new Node[2];
			child[0] = null;
			child[1] = null;
			image = null;
		}
		
		public boolean IsLeaf()
		{
			return child[0] == null && child[1] == null;
		}
		
		// Algorithm from http://www.blackpawn.com/texts/lightmaps/
		public Node Insert(BufferedImage image, int padding)
		{
			if(!IsLeaf())
			{
				Node newNode = child[0].Insert(image, padding);
				
				if(newNode != null)
				{
					return newNode;
				}
				
				return child[1].Insert(image, padding);
			}
			else
			{
				if(this.image != null) 
				{
					return null; // occupied
				}
				
				if(image.getWidth() > rect.width || image.getHeight() > rect.height)
				{
					return null; // does not fit
				}
									
				if(image.getWidth() == rect.width && image.getHeight() == rect.height) 
				{						
					this.image = image; // perfect fit
					return this;
				}
				
				int dw = rect.width - image.getWidth();
				int dh = rect.height - image.getHeight();
				
				if(dw > dh)
				{
					child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
					child[1] = new Node(padding+rect.x+image.getWidth(), rect.y, rect.width - image.getWidth() - padding, rect.height);
				}
				else
				{
					child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
					child[1] = new Node(rect.x, padding+rect.y+image.getHeight(), rect.width, rect.height - image.getHeight() - padding);
				}
				/*if(dw > dh)
				{
					child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
					child[1] = new Node(padding+rect.x+image.getWidth(), rect.y, rect.width - image.getWidth(), rect.height);
				}
				else
				{
					child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
					child[1] = new Node(rect.x, padding+rect.y+image.getHeight(), rect.width, rect.height - image.getHeight());
				}*/
				
				return child[0].Insert(image, padding);
			}
		}
	}
	
	private BufferedImage image;
	private Graphics2D graphics;
	private Node root;
	private Map<String, Rectangle> rectangleMap;

	public FontAtlas(int width, int height)
	{
		image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		graphics = image.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fill(new Rectangle(width, height));
		
		root = new Node(0,0, width, height);
		rectangleMap = new TreeMap<String, Rectangle>();
	}
	
	/**
	 * @return the rectangleMap
	 */
	public Map<String, Rectangle> getRectangleMap() {
		return rectangleMap;
	}

	public boolean AddImage(BufferedImage image, String name, int padding)
	{
		Node node = root.Insert(image, padding);
		
		if(node == null)
		{
			return false;				
		}
		
		rectangleMap.put(name, node.rect);
		graphics.drawImage(image, null, node.rect.x, node.rect.y);
		
		
		return true;	
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	public void Write(int width, int height)
	{			
		try
		{
			ImageIO.write(image, "png", new File("atlas.png"));
		}
		catch(IOException e)
		{
			
		}
	}
}