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

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.LineMaterial;
import ch.fhnw.ether.scene.mesh.material.PointMaterial;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.Log;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.GeodesicSphere;
import ch.fhnw.util.math.geometry.Polygon;

public final class SimpleSphereExample {
	private static final Log LOG = Log.create();

	public static void main(String[] args) {
		new SimpleSphereExample();
	}

	public SimpleSphereExample() {
		// Init platform
		Platform.get().init();
		
		// Create controller
		IController controller = new DefaultController();
		controller.run(time -> {
			try {
				// Create view
				IView view = new DefaultView(controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "Simple Sphere");

				// Create scene and add some content
				IScene scene = new DefaultScene(controller);
				controller.setScene(scene);
				controller.getCamera(view).setUp(new Vec3(0, 1,0));
				controller.getCamera(view).setPosition(new Vec3(0.5, 0.5, 4));
				controller.getCamera(view).setTarget(new Vec3(0.5, 0.5, 0));

				Polygon polygon = new Polygon(new Vec3(0, 0, 0),
						new Vec3(1.6, 0,   0),
						new Vec3(1.6, 0.9, 0),
						new Vec3(0,   0.9, 0)
						);

				IGPUImage t = IGPUImage.read(SimpleLightExample.class.getResource("/textures/earth_nasa.jpg"));
				
				float[] texCoords = {0,0, 1,0, 1,1,   0,0, 1,1, 0,1};
				IMesh beamer_screen = new DefaultMesh(Primitive.TRIANGLES, new ColorMapMaterial(t), DefaultGeometry.createVM(polygon.getTriangleVertices(), texCoords), Queue.DEPTH);
				beamer_screen.setPosition(Vec3.ZERO);

				scene.add3DObjects(beamer_screen);
				
			} catch(Throwable t) {
				LOG.severe(t);
			}
		});
		
		Platform.get().run();
	}
}
