package com.prupe.mcpatcher.cc;

import com.prupe.mcpatcher.MCLogger;
import com.prupe.mcpatcher.MCPatcherUtils;
import com.prupe.mcpatcher.TexturePackAPI;
import net.minecraft.src.ResourceLocation;

final class ColorMap {
	private static final MCLogger logger = MCLogger.getLogger("Custom Colors");
	private static final int COLORMAP_SIZE = 256;
	private static final float COLORMAP_SCALE = 255.0F;
	private int[] map;
	private int mapDefault;

	static int getX(double temperature, double rainfall) {
		return (int)(255.0D * (1.0D - Colorizer.clamp(temperature)));
	}

	static int getY(double temperature, double rainfall) {
		return (int)(255.0D * (1.0D - Colorizer.clamp(rainfall) * Colorizer.clamp(temperature)));
	}

	static float getBlockMetaKey(int blockID, int metadata) {
		return (float)blockID + (float)(metadata & 255) / 256.0F;
	}

	ColorMap(int defaultColor) {
		this.mapDefault = defaultColor;
	}

	void loadColorMap(boolean useCustom, ResourceLocation resource) {
		if (useCustom) {
			this.map = MCPatcherUtils.getImageRGB(TexturePackAPI.getImage(resource));

			if (this.map != null) {
				if (this.map.length != 65536) {
					logger.error("%s must be %dx%d", new Object[] {resource, Integer.valueOf(256), Integer.valueOf(256)});
					this.map = null;
				} else {
					this.mapDefault = this.colorize(16777215, 0.5D, 1.0D);
					logger.fine("using %s, default color %06x", new Object[] {resource, Integer.valueOf(this.mapDefault)});
				}
			}
		}
	}

	boolean isCustom() {
		return this.map != null;
	}

	void clear() {
		this.map = null;
	}

	int colorize() {
		return this.mapDefault;
	}

	int colorize(int defaultColor) {
		return this.map == null ? defaultColor : this.mapDefault;
	}

	int colorize(int defaultColor, double temperature, double rainfall) {
		return this.map == null ? defaultColor : this.map[256 * getY(temperature, rainfall) + getX(temperature, rainfall)];
	}

	int colorize(int defaultColor, int i, int j, int k) {
		return this.colorize(defaultColor, (double)BiomeHelper.getTemperature(i, j, k), (double)BiomeHelper.getRainfall(i, j, k));
	}
}
