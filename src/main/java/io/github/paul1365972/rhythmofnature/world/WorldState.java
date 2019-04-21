package io.github.paul1365972.rhythmofnature.world;

import io.github.paul1365972.rhythmofnature.client.Context;
import io.github.paul1365972.rhythmofnature.client.managers.ResourceManager;
import io.github.paul1365972.rhythmofnature.renderer.Painter;

public class WorldState {
	
	public void tick(Context context) {
	}
	
	public void render(Context context, Painter painter) {
		ResourceManager rm = context.getResourceManager();
		painter.setView(0, 0, 0, 1, 1);
	}
}
