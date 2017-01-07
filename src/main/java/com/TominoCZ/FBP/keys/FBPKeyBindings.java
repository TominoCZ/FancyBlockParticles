package com.TominoCZ.FBP.keys;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class FBPKeyBindings {
	public static KeyBinding FBPMenu, FBPFreeze;

	public static void init() {
		FBPMenu = new KeyBinding("Open Menu", Keyboard.KEY_P, "Fancy Block Particles");
		FBPFreeze = new KeyBinding("Toggle Freeze Effect", Keyboard.KEY_R, "Fancy Block Particles");

		ClientRegistry.registerKeyBinding(FBPMenu);
		ClientRegistry.registerKeyBinding(FBPFreeze);
	}
}