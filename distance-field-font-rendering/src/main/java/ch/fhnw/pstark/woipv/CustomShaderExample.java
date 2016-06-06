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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.image.IImage.ComponentFormat;
import ch.fhnw.ether.image.IImage.ComponentType;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.render.shader.IShader;
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
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.AbstractMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
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

public final class CustomShaderExample {
	
	private static final Log LOG = Log.create();
	
	public static final class DistanceFieldFontMaterial extends AbstractMaterial implements ICustomMaterial {

		private final IShader shader;

//		private IVec3 fill_color;
//		private IVec3 line_color;
//		private IVec3 glow_color;
//		private float line_width;
		private IGPUImage color_map;
		
		public DistanceFieldFontMaterial(
				DistanceFieldFontShader shader, 
				IVec3 fill_color, 
				IVec3 line_color, 
				IVec3 glow_color, 
				float line_width, 
				IGPUImage colorMap) {
//			super(provide(new MaterialAttribute<Float>("custom.red_gain")), require(IGeometry.POSITION_ARRAY));
			super(
					provide(
							IMaterial.COLOR_MAP
//							new MaterialAttribute<IVec3>("dffr.fill_color"),
//							new MaterialAttribute<IVec3>("dffr.line_color"),
//							new MaterialAttribute<IVec3>("dffr.glow_color"),
//							new MaterialAttribute<Float>("dffr.line_width")
							), 
					require(
							IGeometry.POSITION_ARRAY,
							IGeometry.COLOR_MAP_ARRAY
							)
					);
			this.shader = shader;
//			this.line_width = line_width;
//			this.fill_color = fill_color;
//			this.line_color = line_color;
//			this.glow_color = glow_color;
			this.color_map = colorMap;
		}

		/**
		 * @return the color_map
		 */
		public IGPUImage getColor_map() {
			return color_map;
		}

		/**
		 * @param color_map the color_map to set
		 */
		public void setColor_map(IGPUImage color_map) {
			this.color_map = color_map;
			updateRequest();
		}

		@Override
		public IShader getShader() {
			return shader;
		}

		@Override
		public Object[] getData() {
			return data(color_map);
		}
	}

//	public static class DistanceFieldShader extends AbstractShader {
//		public DistanceFieldShader() {
//			// root, name, source, type
//			// root is used for hash keys only, as far as I can tell
//			// name is used for id(), which is used in toString()
//			super(IShader.class, "dffr.distance_field_shader", "/shaders/distance_field", Primitive.TRIANGLES);
//			addArray(new PositionArray());
//			addArray(new ColorMapArray("distance_field"));
//
//			addUniform(new ViewUniformBlock());
//			addUniform(new FloatUniform("dffr.line_width", "line_width"));
//			
//			addUniform(new Vec3FloatUniform("dffr.fill_color", "fill_color"));
//			addUniform(new Vec3FloatUniform("dffr.line_color", "line_color"));
//			addUniform(new Vec3FloatUniform("dffr.glow_color", "glow_color"));
//		}
//	}

	public static void main(String[] args) {
		new CustomShaderExample();
	}

	private IMesh mesh;

	// Setup the whole thing
	public CustomShaderExample() {
		// Init platform
		Platform.get().init();
		
		// Create controller
		IController controller = new DefaultController();
		controller.run(time -> {
			
			try{
				
				// Create view
				IView view = new DefaultView(controller, 100, 100, 500, 500, new IView.Config(ViewType.INTERACTIVE_VIEW, 0, IView.ViewFlag.SMOOTH_LINES), "Test");
	
				// Create scene and add triangle
				IScene scene = new DefaultScene(controller);
				controller.setScene(scene);
				
				controller.getCamera(view).setUp(new Vec3(0, 1,0));
				controller.getCamera(view).setPosition(new Vec3(0.5, 0.5, 4));
				controller.getCamera(view).setTarget(new Vec3(0.5, 0.5, 0));
				
				float[] vertices = { 0, 0, 0, 0.5f, 0, 0.5f, 0, 0, 0.5f };
				float[] colors = { 0.5f, 0.5f, 0.5f, 0.5f, 
						0.5f, 0.5f, 0.5f, 0.5f, 
						0.5f, 0.5f, 0.5f, 0.5f };
	
				
				Polygon polygon = new Polygon(new Vec3(0, 0, 0),
						new Vec3(1.6, 0,   0),
						new Vec3(1.6, 0.9, 0),
						new Vec3(0,   0.9, 0)
						);
	
				
				
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
				
				
				
				
				//IGPUImage t = IGPUImage.create(bi.getWidth(), bi.getHeight()+1, ComponentType.BYTE, ComponentFormat.RGB, byteBuffer);
				//IGPUImage t = IGPUImage.read(SimpleLightExample.class.getResource("/textures/test_woipv.jpg"));
				IGPUImage t = IGPUImage.read(new File("atlas.png"));
				
				float[] texCoords = {0,0, 1,0, 1,1,   0,0, 1,1, 0,1};
				
				DefaultGeometry g = DefaultGeometry.createVM(polygon.getTriangleVertices(), texCoords);

				mesh =  new DefaultMesh(
							Primitive.TRIANGLES, 
							new DistanceFieldFontMaterial(
									new DistanceFieldFontShader(), 
									RGB.WHITE,
									RGB.BLUE,
									RGB.RED,
									0.5f,
									t), 
							g,
							Queue.TRANSPARENCY );
				
				scene.add3DObject(mesh);
			} catch(Throwable e) {
				LOG.severe(e);
			}
		});
//		controller.animate((time, interval) -> {
//			((DistanceFieldFontMaterial) mesh.getMaterial()).setLine_width((float) Math.sin(time) + 1);
//		});
		
		Platform.get().run();
	}
}
