package ch.fhnw.pstark.woipv;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.AbstractMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;

public final class DistanceFieldFontMaterial extends AbstractMaterial implements ICustomMaterial {

	private final IShader shader;
	private IGPUImage color_map;
	
	public DistanceFieldFontMaterial(IGPUImage colorMap) {
		super(
				provide(
						IMaterial.COLOR_MAP
						), 
				require(
						IGeometry.POSITION_ARRAY,
						IGeometry.COLOR_MAP_ARRAY
						)
				);
		this.shader = new DistanceFieldFontShader();
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