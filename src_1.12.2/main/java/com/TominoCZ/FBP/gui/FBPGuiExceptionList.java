package com.TominoCZ.FBP.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FBPGuiExceptionList extends GuiScreen {
	private FBPGuiTextField search;

	Block b;

	GuiScreen parent;

	boolean okToAdd = false;

	FBPGuiButton buttonAdd, buttonRemove;

	List<String> blockNames;

	public FBPGuiExceptionList(GuiScreen parent) {
		this.parent = parent;

		Block current;
		Item item;

		blockNames = Collections.synchronizedList(new ArrayList<String>());

		List<String> tmp = new ArrayList<String>();

		for (ResourceLocation rl : Block.REGISTRY.getKeys()) {
			current = Block.getBlockFromName(rl.toString());

			IBlockState defaultState = current.getDefaultState();

			item = Item.getItemFromBlock(current);

			if (item != Items.AIR && item instanceof ItemBlock)
				tmp.add(rl.toString());
		}

		blockNames.addAll(tmp.stream().sorted((s1, s2) -> s1.split(":")[1].length() - s2.split(":")[1].length())
				.collect(Collectors.toList()));
	}

	private boolean areSame(IBlockState s1, int meta) {
		try {
			IBlockState variant = s1.getBlock().getStateFromMeta(meta);
			return s1.getBlock() == variant.getBlock();
		} catch (Exception e) {

		}

		return true;
	}

	public void initGui() {
		this.buttonList.clear();

		int aWidth = 100;

		int sWidth = 200;
		int sHeight = 20;

		search = new FBPGuiTextField(0, mc.fontRenderer, this.width / 2 - sWidth / 2, this.height / 2 - 75, sWidth,
				sHeight);
		search.setFocused(true);
		search.setCanLoseFocus(true);
		search.setText(FBP.lastAdded);

		buttonAdd = new FBPGuiButton(1, this.width / 2 - aWidth / 2, search.y + 130, "\u00A7aADD", false, false);
		buttonRemove = new FBPGuiButton(2, buttonAdd.x, buttonAdd.y + 20, "\u00A7cREMOVE", false, false);
		buttonAdd.width = buttonRemove.width = aWidth;

		okToAdd = !FBP.INSTANCE.isInExceptions(b = Block.getBlockFromName(FBP.lastAdded));

		this.buttonList.add(buttonAdd);
		this.buttonList.add(buttonRemove);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		search.mouseClicked(x, y, button);
		
		if (button == 1 && x >= search.x && x < search.x + search.width && y >= search.y
				&& y < search.y + search.height) {
			search.setText("");
		}
	}

	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		if (keyCode == 1) {
			closeGui();
			return;
		}
		if (keyCode == 205 && search.selectionEnd != search.cursorPosition)
			search.selectionEnd = search.cursorPosition;
		else
			super.keyTyped(c, keyCode);

		search.textboxKeyTyped(c, keyCode);

		for (String name : blockNames) {
			if (name.contains(search.getText())) {
				b = Block.getBlockFromName((FBP.lastAdded = name));
				break;
			}
		}

		okToAdd = !FBP.INSTANCE.isInExceptions(b);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		search.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		parent.width = this.width;
		parent.height = this.height;

		parent.initGui();
		parent.drawScreen(0, 0, partialTicks);

		this.drawDefaultBackground();
		search.drawTextBox();

		String name = "";

		buttonAdd.enabled = okToAdd;
		buttonRemove.enabled = !okToAdd;

		if (b != null) {
			String itemName = I18n.format(b.getRegistryName().getResourcePath());

			name = itemName;

			drawStack(new ItemStack(b, 1, 0));
		} else {
			buttonAdd.enabled = false;
			name = "";
		}

		this.drawCenteredString(fontRenderer, name, buttonAdd.x + buttonAdd.width / 2, buttonAdd.y - 25,
				fontRenderer.getColorCode('6'));

		this.drawCenteredString(fontRenderer, "\u00A7LAdd Exception For Blocks", width / 2, 20,
				fontRenderer.getColorCode('a'));

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawStack(ItemStack itemstack) {
		GlStateManager.enableDepth();
		GlStateManager.enableLight(0);

		int x = search.x + search.width / 2 - 32;
		int y = search.y + 30;

		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(4, 4, 4);

		this.itemRender.renderItemAndEffectIntoGUI(itemstack, 0, 0);

		GlStateManager.scale(0.25, 0.25, 0.25);
		GlStateManager.translate(-x, -y, 0);

		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1:
			FBP.INSTANCE.addException(b);

			FBPConfigHandler.writeExceptions();
			break;
		case 2:
			FBP.INSTANCE.removeException(b);

			FBPConfigHandler.writeExceptions();
			break;
		}

		okToAdd = !FBP.INSTANCE.isInExceptions(b);
	}

	void closeGui() {
		mc.displayGuiScreen(parent);
	}
}
