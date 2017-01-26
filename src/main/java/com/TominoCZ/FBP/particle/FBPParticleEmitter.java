package com.TominoCZ.FBP.particle;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang3.time.StopWatch;

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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FBPParticleEmitter extends ParticleEmitter {
	Field PosX, PosY, PosZ, MotionX, MotionY, MotionZ, SourceState;

	double X, Y, Z, mX, mY, mZ;

	Queue<Particle> queue;

	IBlockState prevSourceState;

	LinkedList newParticles = new LinkedList();
	
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
					Class c1 = Particle.class;

					if (FBP.isDev()) {
						PosX = c1.getDeclaredField("posX");
						PosY = c1.getDeclaredField("posY");
						PosZ = c1.getDeclaredField("posZ");

						MotionX = c1.getDeclaredField("motionX");
						MotionY = c1.getDeclaredField("motionY");
						MotionZ = c1.getDeclaredField("motionZ");

						SourceState = ParticleDigging.class.getDeclaredField("sourceState");
					} else {
						PosX = c1.getDeclaredField("field_187126_f");
						PosY = c1.getDeclaredField("field_187127_g");
						PosZ = c1.getDeclaredField("field_187128_h");

						MotionX = c1.getDeclaredField("field_187129_i");
						MotionY = c1.getDeclaredField("field_187130_j");
						MotionZ = c1.getDeclaredField("field_187131_k");

						SourceState = ParticleDigging.class.getDeclaredField("field_174847_a");
					}

					PosX.setAccessible(true);
					PosY.setAccessible(true);
					PosZ.setAccessible(true);

					MotionX.setAccessible(true);
					MotionY.setAccessible(true);
					MotionZ.setAccessible(true);

					SourceState.setAccessible(true);

					prevSourceState = (IBlockState) SourceState.get(particle);

					mX = MotionX.getDouble(particle);
					mY = MotionY.getDouble(particle);
					mZ = MotionZ.getDouble(particle);

					X = PosX.getDouble(particle);
					Y = PosY.getDouble(particle);
					Z = PosZ.getDouble(particle);
				} catch (Exception e) {
					prevSourceState = worldObj
							.getBlockState(new BlockPos(this.interpPosX, this.interpPosY, this.interpPosZ));
				}
				if (!(prevSourceState.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen)) {
					newParticles.add(new FBPParticleDigging(worldObj, X, Y, Z, mX, mY, mZ, prevSourceState));
				}

				queue.remove(particle);
			});
			if (!newParticles.isEmpty()) {
				queue.addAll(newParticles);
				newParticles.clear();
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