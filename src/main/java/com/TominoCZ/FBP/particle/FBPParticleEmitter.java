package com.TominoCZ.FBP.particle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.TominoCZ.FBP.FBP;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FBPParticleEmitter extends ParticleEmitter {
	double X, Y, Z, mX, mY, mZ;

	Queue<Particle> queue;

	IBlockState prevSourceState;

	List toAdd = new LinkedList();
	List toRemove = new ArrayList();

	public FBPParticleEmitter(World w, Queue<Particle> q) {
		super(w, new EntityItem(w), EnumParticleTypes.BLOCK_CRACK);
		queue = q;
	}

	@Override
	public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
	}

	@Override
	public void setAlphaF(float alpha) {
	}

	@Override
	public void setMaxAge(int p_187114_1_) {
	}

	@Override
	public void onUpdate() {
		if (queue != null && !Minecraft.getMinecraft().isGamePaused() && FBP.isEnabled()) {
			queue.stream().filter(particle -> particle instanceof ParticleDigging).forEach(particle -> {
				try {
					Class c = Particle.class;

					prevSourceState = (IBlockState) ReflectionHelper
							.findField(ParticleDigging.class, "sourceState", "field_174847_a").get(particle);

					mX = (double) ReflectionHelper.findField(c, "motionX", "field_187129_i").get(particle);
					mY = (double) ReflectionHelper.findField(c, "motionY", "field_187130_j").get(particle);
					mZ = (double) ReflectionHelper.findField(c, "motionZ", "field_187131_k").get(particle);

					X = (double) ReflectionHelper.findField(c, "posX", "field_187126_f").get(particle);
					Y = (double) ReflectionHelper.findField(c, "posY", "field_187127_g").get(particle);
					Z = (double) ReflectionHelper.findField(c, "posZ", "field_187128_h").get(particle);
				} catch (Exception e) {
					prevSourceState = worldObj
							.getBlockState(new BlockPos(this.interpPosX, this.interpPosY, this.interpPosZ));
				}
				if (!(prevSourceState.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen)) {
					if (FBP.spawnRedstoneBlockParticles ? true : prevSourceState.getBlock() != Blocks.REDSTONE_BLOCK)
						toAdd.add(new FBPParticleDigging(worldObj, X, Y, Z, mX, mY, mZ, prevSourceState));
				}

				toRemove.add(particle);
			});
			if (!toRemove.isEmpty()) {
				queue.removeAll(toRemove);
				toRemove.clear();
			}
			if (!toAdd.isEmpty()) {
				queue.addAll(toAdd);
				toAdd.clear();
			}
		}
	}

	@Override
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
	}

	@Override
	public void setParticleTexture(TextureAtlasSprite texture) {
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {
	}

	@Override
	public void nextTextureIndexX() {
	}

	@Override
	public void setExpired() {
	}

	@Override
	protected void setSize(float p_187115_1_, float p_187115_2_) {
	}

	@Override
	public void setPosition(double p_187109_1_, double p_187109_3_, double p_187109_5_) {
	}

	@Override
	public void moveEntity(double x, double y, double z) {
	}

	@Override
	protected void resetPositionToBB() {
	}

	@Override
	public void setEntityBoundingBox(AxisAlignedBB p_187108_1_) {
	}
}