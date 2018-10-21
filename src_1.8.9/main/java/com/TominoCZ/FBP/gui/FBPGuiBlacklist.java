package com.TominoCZ.FBP.gui;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

public class FBPGuiBlacklist extends GuiScreen {

	FBPGuiButtonBlacklist animation, particle;

	final BlockPos selectedPos;
	final IBlockState selectedBlock;

	ItemStack displayItemStack;

	boolean closing = false;

	public FBPGuiBlacklist(BlockPos selected) {
		this.mc = Minecraft.getMinecraft();

		selectedPos = selected;
		IBlockState state = mc.theWorld.getBlockState(selectedPos);

		ItemStack is = state.getBlock().getActualState(state, mc.theWorld, selectedPos).getBlock()
				.getPickBlock(mc.objectMouseOver, mc.theWorld, selectedPos);

		selectedBlock = state;

		displayItemStack = is.copy();
	}

	public FBPGuiBlacklist(ItemStack is) {
		this.mc = Minecraft.getMinecraft();

		selectedPos = null;
		selectedBlock = Block.getBlockFromName(is.getItem().getRegistryName().toString())
				.getStateFromMeta(is.getMetadata());

		displayItemStack = is.copy();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();

		animation = new FBPGuiButtonBlacklist(0, this.width / 2 - 100 - 30, this.height / 2 - 30 + 35, "", false,
				false);
		particle = new FBPGuiButtonBlacklist(1, this.width / 2 + 100 - 30, this.height / 2 - 30 + 35, "", true,
				FBP.INSTANCE.isBlacklisted(selectedBlock.getBlock()));

		Item ib = Item.getItemFromBlock(selectedBlock.getBlock());
		Block b = ib instanceof ItemBlock ? ((ItemBlock) ib).getBlock() : null;

		particle.enabled = selectedBlock.getBlock() != Blocks.redstone_block;

		FBPGuiButton guide = new FBPGuiButton(-1, animation.xPosition + 30, animation.yPosition + 30 - 10,
				(animation.enabled ? "\u00A7a<" : "\u00A7c<") + "             "
						+ (particle.enabled ? "\u00A7a>" : "\u00A7c>"),
				false, false);

		guide.enabled = animation.enabled = false;

		this.buttonList.addAll(Arrays.asList(new GuiButton[] { guide, animation, particle }));
	}

	@Override
	public void updateScreen() {
		Mouse.setGrabbed(true);

		boolean keyUp = false;

		if (selectedPos != null && (mc.objectMouseOver == null
				|| !mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK) || mc.theWorld
						.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() != selectedBlock.getBlock())) {
			keyUp = true;
			FBPKeyInputHandler.INSTANCE.onInput();
		}
		try {
			if (!Keyboard.isKeyDown(FBPKeyBindings.FBPFastAdd.getKeyCode())
					|| (selectedPos == null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
				keyUp = true;
			}
		} catch (Exception e) {
			try {
				if (!Mouse.isButtonDown(FBPKeyBindings.FBPFastAdd.getKeyCode() + 100)
						|| (selectedPos == null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
					keyUp = true;
				}
			} catch (Exception e1) {
				closing = true;
				e.printStackTrace();
			}
		}

		if (closing || keyUp) {
			Block b = selectedBlock.getBlock();

			GuiButton selected = animation.isMouseOver() ? animation : (particle.isMouseOver() ? particle : null);

			if (selected != null) {
				if (selected.enabled) {
					if (!FBP.INSTANCE.isBlacklisted(b))
						FBP.INSTANCE.addToBlacklist(b);
					else
						FBP.INSTANCE.removeFromBlacklist(b);

					if (particle.isMouseOver())
						FBPConfigHandler.writeParticleBlacklist();

					mc.getSoundHandler()
							.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press")));
				}
			}

			if (keyUp)
				FBPKeyInputHandler.INSTANCE.onInput();

			mc.displayGuiScreen(null);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		GuiButton clicked = animation.isMouseOver() ? animation : (particle.isMouseOver() ? particle : null);

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

		GlStateManager.enableDepth();
		GlStateManager.enableLight(0);
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(4, 4, 4);
		GlStateManager.enableColorMaterial();
		this.itemRender.renderItemAndEffectIntoGUI(displayItemStack, 0, 0);

		this.itemRender.zLevel = 0.0F;
		this.zLevel = 0.0F;

		GlStateManager.scale(0.25, 0.25, 0.25);
		GlStateManager.translate(-x, -y, 0);

		// BLOCK INFO
		String itemName = (selectedPos == null ? displayItemStack.getItem()
				: selectedBlock.getBlock().getRegistryName()).toString();
		itemName = ((itemName.contains(":") ? "\u00A76\u00A7l" : "\u00A7a\u00A7l") + itemName).replaceAll(":",
				"\u00A7c\u00A7l:\u00A7a\u00A7l");

		FBPGuiHelper._drawCenteredString(fontRendererObj, itemName, width / 2, height / 2 - 19, 0);

		// EXCEPTIONS INFO
		String animationText1 = "\u00A7c\u00A7lNOT AVAILABLE";
		String particleText1 = particle.enabled
				? (particle.isMouseOver() ? (particle.isInExceptions ? "\u00A7c\u00A7lREMOVE" : "\u00A7a\u00A7lADD")
						: "")
				: "\u00A7c\u00A7lCAN'T BE ADDED";

		FBPGuiHelper._drawCenteredString(fontRendererObj, animationText1, animation.xPosition + 30,
				animation.yPosition + 65, 0);
		FBPGuiHelper._drawCenteredString(fontRendererObj, particleText1, particle.xPosition + 30,
				particle.yPosition + 65, 0);

		if (animation.isMouseOver())
			FBPGuiHelper._drawCenteredString(fontRendererObj, "\u00A7a\u00A7lPLACE ANIMATION", animation.xPosition + 30,
					animation.yPosition - 12, 0);
		if (particle.isMouseOver())
			FBPGuiHelper._drawCenteredString(fontRendererObj, "\u00A7a\u00A7lPARTICLES", particle.xPosition + 30,
					particle.yPosition - 12, 0);

		this.drawCenteredString(fontRendererObj, "\u00A7LBlacklist a Block", width / 2, 20,
				fontRendererObj.getColorCode('a'));

		mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));

		// RENDER SCREEN
		super.drawScreen(mouseX, mouseY, partialTicks);

		// RENDER MOUSE
		mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.enableBlend();

		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GuiButton mouseOver = animation.isMouseOver() ? animation : (particle.isMouseOver() ? particle : null);

		int imageDiameter = 20;

		this.drawTexturedModalRect(mouseX - imageDiameter / 2, mouseY - imageDiameter / 2,
				mouseOver != null && !mouseOver.enabled ? 256 - imageDiameter * 2 : 256 - imageDiameter,
				256 - imageDiameter, imageDiameter, imageDiameter);
	}
}
