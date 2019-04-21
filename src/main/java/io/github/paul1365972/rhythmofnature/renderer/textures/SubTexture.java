package io.github.paul1365972.rhythmofnature.renderer.textures;


public class SubTexture implements IndexedRenderableTexture {
	
	private final int id;
	private final String name;
	private AtlasPos atlasPos;
	private TextureAtlas atlas;
	
	public SubTexture(TextureAtlas atlas, int id, String name, AtlasPos atlasPos) {
		this.atlasPos = atlasPos;
		this.atlas = atlas;
		this.id = id;
		this.name = name;
	}
	
	public SubTexture(TextureAtlas atlas, int id, String name, float x, float y, float w, float h) {
		this(atlas, id, name, new AtlasPos(x, y, w, h));
	}
	
	@Override
	public AtlasPos getAtlasPos() {
		return atlasPos;
	}
	
	@Override
	public boolean isAtlased() {
		return true;
	}
	
	@Override
	public int getBackingTexture() {
		return getAtlas().getTexture();
	}
	
	void _setTextureAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}
	
	public TextureAtlas getAtlas() {
		return atlas;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
}
