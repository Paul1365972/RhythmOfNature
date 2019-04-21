package io.github.paul1365972.rhythmofnature.renderer.textures;

public interface RenderableTexture {
	
	default AtlasPos getAtlasPos() {
		return AtlasPos.IDENTITY;
	}
	
	default boolean isAtlased() {
		return false;
	}
	
	int getBackingTexture();
	
}
