package org.spoutcraft.client.gui.minimap;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Mouse;
import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.gui.GuiSpoutScreen;
import org.spoutcraft.spoutcraftapi.Spoutcraft;
import org.spoutcraft.spoutcraftapi.addon.Addon;
import org.spoutcraft.spoutcraftapi.animation.PropertyAnimation;
import org.spoutcraft.spoutcraftapi.gui.Button;
import org.spoutcraft.spoutcraftapi.gui.GenericButton;
import org.spoutcraft.spoutcraftapi.gui.GenericLabel;
import org.spoutcraft.spoutcraftapi.gui.Label;
import org.spoutcraft.spoutcraftapi.gui.Orientation;
import org.spoutcraft.spoutcraftapi.gui.RenderPriority;
import org.spoutcraft.spoutcraftapi.gui.Widget;

public class GuiOverviewMap extends GuiSpoutScreen {
	
	private MapWidget map;
	private Label title;
	private Button buttonDone, buttonAddWaypoint, buttonRemoveWaypoint, buttonZoomIn, buttonZoomOut, buttonShowPlayer;
	
	private boolean dragging = false;
	private int dragStartX = 0, dragStartY = 0;

	@Override
	protected void createInstances() {
		map = new MapWidget(this);
		title = new GenericLabel("Overview Map");
		buttonDone = new GenericButton("Done");
		buttonZoomIn = new GenericButton("+");
		buttonZoomOut = new GenericButton("-");
		buttonShowPlayer = new GenericButton("Player");
		buttonAddWaypoint = new GenericButton("Add Waypoint");
		map.setGeometry(0, 0, width, height - 56);
		map.showPlayer();
		
		Addon spoutcraft = Spoutcraft.getAddonManager().getAddon("Spoutcraft");
		getScreen().attachWidgets(spoutcraft, map, title, buttonDone, buttonZoomIn, buttonZoomOut, buttonShowPlayer, buttonAddWaypoint);
	}

	@Override
	protected void layoutWidgets() {
		title.setX(width / 2 - SpoutClient.getHandle().fontRenderer.getStringWidth(title.getText()) / 2);
		title.setY(5);
		
		map.setGeometry(0, 0, width, height);
		map.setPriority(RenderPriority.Highest);

		buttonZoomIn.setGeometry(5, height - 25, 20, 20);
		buttonZoomOut.setGeometry(25, height - 25, 20, 20);
		buttonDone.setGeometry(width - 55, height - 25, 50, 20);
		buttonShowPlayer.setGeometry(50, height - 25, 50, 20);
		buttonAddWaypoint.setGeometry(105, height - 25, 100, 20);
	}
	
	@Override
	public void buttonClicked(Button btn) {
		if(btn == buttonDone) {
			mc.displayGuiScreen(null);
		}
		if(btn == buttonZoomIn) {
			map.zoomBy(0.1f);
		}
		if(btn == buttonZoomOut) {
			map.zoomBy(-0.1f);
		}
		if(btn == buttonShowPlayer) {
			map.showPlayer();
		}
		if(btn == buttonAddWaypoint) {
			SpoutClient.getHandle().displayGuiScreen(new GuiAddWaypoint());
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		if(button == 0 && isInBoundingRect(map, x, y) && !hitsWidget(x, y, map, title)) {
			dragging = true;
			dragStartX = x;
			dragStartY = y;
		}
		super.mouseClicked(x, y, button);
	}

	private boolean hitsWidget(int x, int y, Widget ...exclude) {
		for(Widget widget:getScreen().getAttachedWidgets(true)) {
			if(isInBoundingRect(widget, x, y)) {
				if(ArrayUtils.contains(exclude, widget)) {
					continue;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	protected void mouseMovedOrUp(int x, int y, int button) {
		if(button == 0 && !Mouse.isButtonDown(button)) {
			dragging = false;
		}
		if(dragging) {
			int mX = (int) map.getScrollPosition(Orientation.HORIZONTAL);
			int mY = (int) map.getScrollPosition(Orientation.VERTICAL);
			mX -= x - dragStartX;
			mY -= y - dragStartY;
			map.setScrollPosition(Orientation.HORIZONTAL, mX);
			map.setScrollPosition(Orientation.VERTICAL, mY);
			
			dragStartX = x;
			dragStartY = y;
		}
		super.mouseMovedOrUp(x, y, button);
	}
}