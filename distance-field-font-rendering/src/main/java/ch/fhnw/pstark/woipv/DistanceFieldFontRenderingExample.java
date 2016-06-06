/*
 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.pstark.woipv;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.GLGPUImage;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.image.IImage.AlphaMode;
import ch.fhnw.ether.image.IImage.ComponentFormat;
import ch.fhnw.ether.image.IImage.ComponentType;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.base.Vec3FloatUniform;
import ch.fhnw.ether.render.variable.builtin.ColorArray;
import ch.fhnw.ether.render.variable.builtin.ColorMapArray;
import ch.fhnw.ether.render.variable.builtin.PositionArray;
import ch.fhnw.ether.render.variable.builtin.ViewUniformBlock;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.IView.ViewType;
import ch.fhnw.util.Log;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.IVec3;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.Polygon;
import ch.fhnw.ether.scene.attribute.IAttribute;

public final class DistanceFieldFontRenderingExample {
	
	private static final Log LOG = Log.create();
	
	public static void main(String[] args) {
		new DistanceFieldFontRenderingExample();
	}

	private IMesh mesh;

	// Setup the whole thing
	public DistanceFieldFontRenderingExample() {
		// Init platform
		Platform.get().init();
		
		// Create controller
		IController controller = new DefaultController();
		controller.run(time -> {
			
			try{
				
				// Create view
				IView view = new DefaultView(controller, 100, 100, 1280, 720, new IView.Config(ViewType.INTERACTIVE_VIEW, 0, IView.ViewFlag.GRID), "Test");
	
				// Create scene and add triangle
				IScene scene = new DefaultScene(controller);
				controller.setScene(scene);
				
				controller.getCamera(view).setUp(new Vec3(0, 1,0));
				controller.getCamera(view).setPosition(new Vec3(0.5, 0.5, 4));
				controller.getCamera(view).setTarget(new Vec3(0.5, 0.5, 0));
					

	
				FontAtlasGenerator atlasGenerator = new FontAtlasGenerator();
				
				FontAtlas atlasinstance = atlasGenerator.generate("arial.ttf", 100, 1024, 1024, 5, 
						" !\"#$%&'()*+,-./0123456789:;<=>?"+
				                "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"+
				                "`abcdefghijklmnopqrstuvwxyz{|}~");
				BufferedImage bi = atlasinstance.getImage();
				
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
				
				/*DistanceFieldGenerator dfg = new DistanceFieldGenerator();
				dfg.setSpread(10);
				dfg.setDownscale(4);
				BufferedImage out = dfg.generateDistanceField(bi);
				try
				{
					ImageIO.write(out, "png", new File("atlas2.png"));
				}
				catch(IOException e)
				{
					System.out.println(e.getStackTrace());
				}
				*/
				
				byte[] pixelDataRaw = ((DataBufferByte) dataBuffer).getData();
				
				byte[] distanceMap = DistanceFieldUtils.make_distance_mapb(pixelDataRaw, bi.getWidth(), bi.getHeight());
				
				BufferedImage new_image = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
				byte[] array = ((DataBufferByte) new_image.getRaster().getDataBuffer()).getData();
				System.out.println("new " + array.length);
				System.out.println("pixelDataRaw " + pixelDataRaw.length);
				System.out.println("distanceMap " + distanceMap.length);
				System.arraycopy(distanceMap, 0, array, 0, array.length);
				
				try
				{
					ImageIO.write(new_image, "png", new File("atlas4.png"));
				}
				catch(IOException e)
				{
					System.out.println(e.getStackTrace());
				}
				
				// I don't understand why this is not working...
				//IGPUImage atlas = IGPUImage.create(bi.getWidth(), bi.getHeight(), ComponentType.BYTE, ComponentFormat.G, byteBuffer);
				//IGPUImage atlas = new GLGPUImage(bi.getWidth(), bi.getHeight(), ComponentType.BYTE, ComponentFormat.G, AlphaMode.PRE_MULTIPLIED, byteBuffer);
				//IGPUImage atlas = IGPUImage.read(SimpleLightExample.class.getResource("/textures/test_woipv.jpg"));
				
				IGPUImage atlas = IGPUImage.read(new File("atlas4.png"));
//				IGPUImage atlas = IGPUImage.read(new File("atlas_freetypegl.png"));
//				IGPUImage atlas = IGPUImage.read(new File("atlas3.png"));
//				IGPUImage atlas = IGPUImage.read(new File("atlas2.png"));
				
				Map<String, Rectangle> glyphs = atlasinstance.getRectangleMap();
				
				float x1 = (float)glyphs.get("A").getX();
				float y1 = (float)glyphs.get("A").getY();
				float x2 = x1+(float)glyphs.get("A").getWidth();
				float y2 = y1+(float)glyphs.get("A").getHeight();
//				System.out.println(""+x1+x2+y1+y2);
//				float[] texCoords = {x1,y1, x2,y1, x2,y2,   x1,y1, x2,y2, x1,y2};
				float[] texCoords = {0,0, 1,0, 1,1,   0,0, 1,1, 0,1};
				
				
				
				Polygon polygon = new Polygon(new Vec3(0, 0, 0),
						new Vec3(1.6, 0,   0),
						new Vec3(1.6, 1.6, 0),
						new Vec3(0,   1.6, 0)
						);
				
				DefaultGeometry g = DefaultGeometry.createVM(polygon.getTriangleVertices(), texCoords);

				mesh =  new DefaultMesh(
							Primitive.TRIANGLES, 
							new DistanceFieldFontMaterial(atlas), 
							g,
							Queue.TRANSPARENCY );
				
				scene.add3DObject(mesh);
				
			} catch(Throwable e) {
				LOG.severe(e);
			}
		});
		
		Platform.get().run();
	}
}
