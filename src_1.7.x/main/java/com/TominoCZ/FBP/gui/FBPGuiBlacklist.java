package com.TominoCZ.FBP.gui;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;

public class FBPGuiBlacklist extends GuiScreen {

	FBPGuiButtonBlacklist animation, particle;

	final FBPBlockPos selectedPos;
	final Block selectedBlock;

	ItemStack displayItemStack;

	boolean closing = false;

	public FBPGuiBlacklist(FBPBlockPos selected) {
		this.mc = Minecraft.getMinecraft();

		selectedPos = selected;

		Block b = mc.theWorld.getBlock(selected.getX(), selected.getY(), selected.getZ());

		selectedBlock = b;

		ItemStack is = b.getPickBlock(mc.objectMouseOver, mc.theWorld, selected.getX(), selected.getY(),
				selected.getZ(), mc.thePlayer);

		if (is == null)
			is = new ItemStack(b);

		displayItemStack = is.copy();
	}

	public FBPGuiBlacklist(ItemStack is) {
		this.mc = Minecraft.getMinecraft();

		selectedPos = null;
		selectedBlock = Block.getBlockFromItem(is.getItem());

		displayItemStack = is.copy();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();

		animation = new FBPGuiButtonBlacklist(0, this.width / 2 - 100 - 30, this.height / 2 - 30 + 35, "", false, true);
		particle = new FBPGuiButtonBlacklist(1, this.width / 2 + 100 - 30, this.height / 2 - 30 + 35, "", true,
				FBP.INSTANCE.isBlacklisted(selectedBlock));

		Item ib = Item.getItemFromBlock(selectedBlock);
		Block b = ib instanceof ItemBlock ? Block.getBlockFromItem(ib) : null;

		animation.enabled = false;
		particle.enabled = selectedBlock != Blocks.redstone_block;

		FBPGuiButton guide = new FBPGuiButton(-1, animation.xPosition + 30, animation.yPosition + 30 - 10,
				(animation.enabled ? "\u00A7a<" : "\u00A7c<") + "             "
						+ (particle.enabled ? "\u00A7a>" : "\u00A7c>"),
				false, false);
		guide.enabled = false;

		this.buttonList.addAll(Arrays.asList(new GuiButton[] { guide, animation, particle }));
	}

	@Override
	public void updateScreen() {
		Mouse.setGrabbed(true);

		boolean keyUp = false;

		if (selectedPos != null
				&& (mc.objectMouseOver == null || !mc.objectMouseOver.typeOfHit.equals(MovingObjectType.BLOCK))) {
			Block b = mc.theWorld.getBlock(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY,
					mc.objectMouseOver.blockZ);

			if (b != selectedBlock) {
				keyUp = true;
				FBPKeyInputHandler.INSTANCE.onInput();
			}
		}
		try {
			if (!Keyboard.isKeyDown(FBPKeyBindings.FBPQuickAdd.getKeyCode())
					|| (selectedPos == null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
				keyUp = true;
			}
		} catch (Exception e) {
			try {
				if (!Mouse.isButtonDown(FBPKeyBindings.FBPQuickAdd.getKeyCode() + 100)
						|| (selectedPos == null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
					keyUp = true;
				}
			} catch (Exception e1) {
				closing = true;
				e.printStackTrace();
			}
		}

		if (closing || keyUp) {
			Block b = selectedBlock;

			GuiButton selected = animation.func_146115_a() ? animation : (particle.func_146115_a() ? particle : null);

			if (selected != null) {
				if (selected.enabled) {
					if (!FBP.INSTANCE.isBlacklisted(b))
						FBP.INSTANCE.addToBlacklist(b);
					else
						FBP.INSTANCE.removeFromBlacklist(b);

					FBPConfigHandler.writeParticleBlacklist();

					mc.getSoundHandler().playSound(
							PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1));
				}
			}

			if (keyUp)
				FBPKeyInputHandler.INSTANCE.onInput();

			mc.displayGuiScreen(null);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		GuiButton clicked = animation.func_146115_a() ? animation : (particle.func_146115_a() ? particle : null);

		if (clicked != null && clicked.enabled)
			closing = true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		// LIMIT MOUSE POS
		int optionRadius = 30;
		mouseX = MathHelper.clamp_int(mouseX, animation.xPosition + optionRadius, particle.xPosition + optionRadius);
		mouseY = height / 2 + 35;

		// RENDER BLOCK
		int x = width / 2 - 32;
		int y = height / 2 - 30 - 60;

		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glScaled(4, 4, 4);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_LIGHTING);

		this.itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, mc.getTextureManager(), displayItemStack, 0,
				0);

		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;

		GL11.glScaled(0.25, 0.25, 0.25);
		GL11.glTranslatef(-x, -y, 0);
		GL11.glPopMatrix();

		RenderHelper.disableStandardItemLighting();

		Block b = selectedPos == null
				? (displayItemStack != null ? Block.getBlockFromItem(displayItemStack.getItem()) : null)
				: selectedBlock;

		// BLOCK INFO
		String itemName = "";
		if (b != null) {
			itemName = Block.blockRegistry.getNameForObject(b);

			itemName = ((itemName.contains(":") ? "\u00A76\u00A7l" : "\u00A7a\u00A7l") + itemName).replaceAll(":",
					"\u00A7c\u00A7l:\u00A7a\u00A7l");
		}
		FBPGuiHelper._drawCenteredString(fontRendererObj, itemName, width / 2, height / 2 - 19, 0);

		// EXCEPTIONS INFO
		String particleText1 = particle.enabled
				? (particle.func_146115_a() ? (particle.isInExceptions ? "\u00A7c\u00A7lREMOVE" : "\u00A7a\u00A7lADD")
						: "")
				: "\u00A7c\u00A7lCAN'T BE ADDED";

		FBPGuiHelper._drawCenteredString(fontRendererObj, "\u00A7c\u00A7lNOT AVAILABLE", animation.xPosition + 30,
				animation.yPosition + 65, 0);
		FBPGuiHelper._drawCenteredString(fontRendererObj, particleText1, particle.xPosition + 30,
				particle.yPosition + 65, 0);

		if (animation.func_146115_a())
			FBPGuiHelper._drawCenteredString(fontRendererObj, "\u00A7a\u00A7lPLACE ANIMATION", animation.xPosition + 30,
					animation.yPosition - 12, 0);
		if (particle.func_146115_a())
			FBPGuiHelper._drawCenteredString(fontRendererObj, "\u00A7a\u00A7lPARTICLES", particle.xPosition + 30,
					particle.yPosition - 12, 0);

		this.drawCenteredString(fontRendererObj, "\u00A7a\u00A7LBlacklist a Block", width / 2, 20, 0);

		// RENDER SCREEN
		super.drawScreen(mouseX, mouseY, partialTicks);

		// RENDER MOUSE
		mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glEnable(GL11.GL_BLEND);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GuiButton mouseOver = animation.func_146115_a() ? animation : (particle.func_146115_a() ? particle : null);

		int imageDiameter = 20;

		this.drawTexturedModalRect(mouseX - imageDiameter / 2, mouseY - imageDiameter / 2,
				mouseOver != null && !mouseOver.enabled ? 256 - imageDiameter * 2 : 256 - imageDiameter,
				256 - imageDiameter, imageDiameter, imageDiameter);
	}
}