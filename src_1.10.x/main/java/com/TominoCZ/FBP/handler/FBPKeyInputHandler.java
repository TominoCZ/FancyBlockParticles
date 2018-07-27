package com.TominoCZ.FBP.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.gui.FBPGuiBlacklist;
import com.TominoCZ.FBP.gui.FBPGuiMenuPage0;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class FBPKeyInputHandler {
	Minecraft mc;

	public static FBPKeyInputHandler INSTANCE;

	boolean wasOpened = false;

	public FBPKeyInputHandler() {
		mc = Minecraft.getMinecraft();
		INSTANCE = this;
	}

	@SubscribeEvent
	public void onKeyboardInput(InputEvent.KeyInputEvent e) {
		onInput();
	}

	@SubscribeEvent
	public void onMouseInput(InputEvent.MouseInputEvent e) {
		onInput();
	}

	public void onInput() {
		if (FBPKeyBindings.FBPMenu.isPressed())
			Minecraft.getMinecraft().displayGuiScreen(new FBPGuiMenuPage0());

		if (FBPKeyBindings.FBPToggle.isPressed())
			FBP.setEnabled(!FBP.enabled);

		if (FBPKeyBindings.FBPFreeze.isPressed())
			if (FBP.isEnabled())
				FBP.frozen = !FBP.frozen;
			else
				FBP.frozen = false;

		if (mc.currentScreen != null)
			return;

		boolean isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		boolean isKeyDown = FBPKeyBindings.FBPFastAdd.isKeyDown();

		if (isShiftDown && isKeyDown || isKeyDown) {
			Block b = null;
			ItemStack stack = null;

			boolean useHeldBlock = isShiftDown && mc.thePlayer.getHeldItemMainhand() != null
					&& mc.thePlayer.getHeldItemMainhand().getItem() != null
					&& (b = Block.getBlockFromName((stack = mc.thePlayer.getHeldItemMainhand()).getItem()
							.getRegistryName().toString())) != null
					&& b != Blocks.AIR;

			if (!wasOpened && isKeyDown
					&& ((mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.BLOCK))
							|| useHeldBlock)) {
				mc.displayGuiScreen(useHeldBlock ? (new FBPGuiBlacklist(stack))
						: (new FBPGuiBlacklist(mc.objectMouseOver.getBlockPos())));

				Mouse.setGrabbed(true);

				wasOpened = true;
			}
		} else if (wasOpened)
			wasOpened = false;
	}
}