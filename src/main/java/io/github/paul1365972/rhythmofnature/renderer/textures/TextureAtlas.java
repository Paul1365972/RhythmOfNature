package io.github.paul1365972.rhythmofnature.renderer.textures;

import io.github.paul1365972.rhythmofnature.client.managers.LoadedTexture;
import io.github.paul1365972.rhythmofnature.client.managers.LoadedTextureAtlas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextureAtlas extends Texture {
	private static Logger LOGGER = LogManager.getLogger();
	
	private String name;
	private List<SubTexture> subTextures;
	
	private TextureAtlas(int texture, String name, List<SubTexture> subTextures) {
		super(texture);
		this.name = name;
		this.subTextures = subTextures;
	}
	
	public static TextureAtlas load(LoadedTextureAtlas loadedAtlas) {
		Dimension size = loadedAtlas.getSize();
		Dimension imgSize = loadedAtlas.getImageSize();
		BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		List<SubTexture> subTextures = new ArrayList<>();
		
		int columns = size.width / imgSize.width;
		int rows = size.height / imgSize.height;
		int x = 0, y = 0;
		for (LoadedTexture tex : loadedAtlas.getLoadedTextures()) {
			BufferedImage src = resize(tex.getImg(), imgSize.width, imgSize.height);
			int[] srcPixels = src.getRGB(0, 0, imgSize.width, imgSize.height, null, 0, imgSize.width);
			img.setRGB(x * imgSize.width, y * imgSize.height, imgSize.width, imgSize.height, srcPixels, 0, imgSize.width);
			
			float widthU = imgSize.width * 1f / size.width;
			float heightV = imgSize.height * 1f / size.height;
			subTextures.add(new SubTexture(null, tex.getId(), tex.getName(), x * widthU, y * heightV, widthU, heightV));
			
			x++;
			if (x >= columns) {
				x = 0;
				y++;
			}
			if (y >= rows) {
				LOGGER.info("Texture Atlas not large enough");
				break;
			}
		}
		
		int texture = upload(convert(img, size.width, size.height), size.width, size.height);
		
		TextureAtlas atlas = new TextureAtlas(texture, loadedAtlas.getName(), subTextures);
		atlas.setMagFilter(GL11.GL_NEAREST);
		atlas.setMinFilter(GL11.GL_NEAREST);
		subTextures.forEach(subTexture -> subTexture._setTextureAtlas(atlas));
		return atlas;
	}
	
	private static BufferedImage resize(BufferedImage src, int width, int height) {
		if (src.getWidth() == width && src.getHeight() == height)
			return src;
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, width, height, 0, 0, src.getWidth(), src.getHeight(), null);
		g.dispose();
		return resized;
	}
	
	public String getName() {
		return name;
	}
	
	public List<SubTexture> getSubTextures() {
		return Collections.unmodifiableList(subTextures);
	}
	
}
