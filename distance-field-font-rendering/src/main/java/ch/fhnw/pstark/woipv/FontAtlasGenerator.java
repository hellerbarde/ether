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

public class FontAtlasGenerator
{	
	public static void main(String args[])
	{
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));

		FontAtlasGenerator atlasGenerator = new FontAtlasGenerator();
		BufferedImage bi = atlasGenerator.generate("DejaVuSans.ttf", 72, 1024, 1024, 5, " !\"#$%&'()*+,-./0123456789:;<=>?"+
                "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"+
                "`abcdefghijklmnopqrstuvwxyz{|}~").getImage();
		
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

	public FontAtlas generate(String fontpath, float fontsize, int width, int height, int padding, String characters)
	{

		Font baseFont = null;
		Font font = null;
		try {
			URL fonturl = FontAtlasGenerator.class.getResource("/fonts/" + fontpath);
			System.out.println("Font = " +
					fonturl);
			baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(fonturl.getFile()));
			//baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontpath));
			font = baseFont.deriveFont(Font.PLAIN, fontsize);

		} catch (FontFormatException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		Set<GlyphName> glyphSet = new TreeSet<GlyphName>(new GlyphNameComparator());
		
		for(char f : characters.toCharArray()){
			char[] c = {f};
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			Rectangle2D glyphbounds = font.getStringBounds(new String(c), image.createGraphics().getFontRenderContext());
			image = null;
			
			// We need a better way to deal with the troubles of kerning and other font metrics stuff
			BufferedImage glyph = new BufferedImage((int)glyphbounds.getWidth()+1, (int)(glyphbounds.getHeight()*1.3), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D gc = glyph.createGraphics();
			gc.setFont(font);
			gc.setColor(Color.WHITE);
			gc.drawChars(c,0, 1, 0, (int)glyphbounds.getHeight()+1);
			//BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			
			glyphSet.add(new GlyphName(glyph, new String(c)));
		}
		
		FontAtlas texture = new FontAtlas(width, height);
		
		int count = 0;
		
		for(GlyphName glyph : glyphSet)
		{
			System.out.println("Adding " + glyph.name + " to atlas (" + (++count) + ")");
			texture.AddImage(glyph.image, glyph.name, padding);
		}
		
		count = 0;
		texture.Write(width, height);
		return texture;
		//return texture.getImage();
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
}
