package ch.fhnw.pstark.woipv;

// Original was released into Public Domain (https://github.com/lukaszdk/texture-atlas-generator)
// Adjusted by Philip Stark for ether (c) 2016
// 

import java.util.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.imageio.*;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.*;

public class FontAtlas
{	
	public static void main(String args[])
	{
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));

		FontAtlas atlasGenerator = new FontAtlas();
		BufferedImage bi = atlasGenerator.generate("DejaVuSans.ttf", 72, 1024, 1024, 5, " !\"#$%&'()*+,-./0123456789:;<=>?"+
                "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"+
                "`abcdefghijklmnopqrstuvwxyz{|}~");
		
		ByteBuffer byteBuffer;
		DataBuffer dataBuffer = bi.getRaster().getDataBuffer();

		if (dataBuffer instanceof DataBufferByte) {
			System.out.println("DataBufferByte");
		    byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
		    byteBuffer = ByteBuffer.wrap(pixelData);
		}
		else if (dataBuffer instanceof DataBufferUShort) {
			System.out.println("DataBufferUShort");
		    short[] pixelData = ((DataBufferUShort) dataBuffer).getData();
		    byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
		    byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
		}
		else if (dataBuffer instanceof DataBufferShort) {
			System.out.println("DataBufferShort");
		    short[] pixelData = ((DataBufferShort) dataBuffer).getData();
		    byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
		    byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
		}
		else if (dataBuffer instanceof DataBufferInt) {
			System.out.println("DataBufferInt");
		    int[] pixelData = ((DataBufferInt) dataBuffer).getData();
		    byteBuffer = ByteBuffer.allocate(pixelData.length * 4);
		    byteBuffer.asIntBuffer().put(IntBuffer.wrap(pixelData));
		}
		else {
		    throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
		}
		
		System.out.println(byteBuffer);
	}

	public BufferedImage generate(String fontpath, float fontsize, int width, int height, int padding, String characters)
	{

		Font baseFont = null;
		Font font = null;
		try {
			URL fonturl = FontAtlas.class.getResource("/fonts/" + fontpath);
			System.out.println("Font = " +
					fonturl);
			baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(fonturl.getFile()));
			//baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontpath));
			font = baseFont.deriveFont(Font.PLAIN, fontsize);

		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<GlyphName> glyphSet = new TreeSet<GlyphName>(new GlyphNameComparator());
		
		for(char f : characters.toCharArray()){
			char[] c = {f};
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			Rectangle2D glyphbounds = font.getStringBounds(new String(c), image.createGraphics().getFontRenderContext());
			image = null;
			
			// We need a better way to deal with the troubles of kerning and other font metrics stuff
			BufferedImage glyph = new BufferedImage((int)glyphbounds.getWidth()+1, (int)(glyphbounds.getHeight()*1.3), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D gc = glyph.createGraphics();
			gc.setFont(font);
			gc.setColor(Color.BLACK);
			gc.drawChars(c,0, 1, 0, (int)glyphbounds.getHeight()+1);
			//BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			
			glyphSet.add(new GlyphName(glyph, new String(c)));
		}
		
		List<Texture> textures = new ArrayList<Texture>();
		Texture texture = new Texture(width, height);
		textures.add(new Texture(width, height));
		
		int count = 0;
		
		for(GlyphName glyph : glyphSet)
		{
			boolean added = false;
			
			System.out.println("Adding " + glyph.name + " to atlas (" + (++count) + ")");
			texture.AddImage(glyph.image, glyph.name, padding);
		}
		
		count = 0;
		texture.Write(width, height);
		
		return texture.getImage();
	}
	
	private class GlyphName 
	{
		public BufferedImage image;
		public String name;
		
		public GlyphName(BufferedImage image, String name)
		{
			this.image = image;
			this.name = name;
		}
	}
	
	private class GlyphNameComparator implements Comparator<GlyphName>
	{
		public int compare(GlyphName image1, GlyphName image2)
		{
			int area1 = image1.image.getWidth() * image1.image.getHeight();
			int area2 = image2.image.getWidth() * image2.image.getHeight();
		
			if(area1 != area2)
			{
				return area2 - area1;
			}
			else
			{
				return image1.name.compareTo(image2.name);
			}
		}
	}
	
	public class Texture
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

		public Texture(int width, int height)
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			graphics = image.createGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fill(new Rectangle(width, height));
			
			root = new Node(0,0, width, height);
			rectangleMap = new TreeMap<String, Rectangle>();
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
				
//				BufferedWriter atlas = new BufferedWriter(new FileWriter(name + ".txt"));
//				
//				for(Map.Entry<String, Rectangle> e : rectangleMap.entrySet())
//				{
//					Rectangle r = e.getValue();
//					String keyVal = e.getKey();
//					if (fileNameOnly)
//						keyVal = keyVal.substring(keyVal.lastIndexOf('/') + 1);
//					if (unitCoordinates)
//					{
//						atlas.write(keyVal + " " + r.x/(float)width + " " + r.y/(float)height + " " + r.width/(float)width + " " + r.height/(float)height);
//					}
//					else
//						atlas.write(keyVal + " " + r.x + " " + r.y + " " + r.width + " " + r.height);
//					atlas.newLine();
//				}
//				
//				atlas.close();
			}
			catch(IOException e)
			{
				
			}
		}
	}
}
