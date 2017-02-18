package com.TominoCZ.FBP.particle;

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

					ReflectionHelper.setPrivateValue(c, particle, 0, "field_70544_f", "particleScale");

					prevSourceState = (IBlockState) ReflectionHelper
							.findField(ParticleDigging.class, "field_174847_a", "sourceState").get(particle);

					if (prevSourceState != null
							&& (!(prevSourceState.getBlock() instanceof BlockLiquid)
									&& !(FBP.frozen && !FBP.spawnWhileFrozen))
							&& (FBP.spawnRedstoneBlockParticles
									|| prevSourceState.getBlock() != Blocks.REDSTONE_BLOCK)) {
						mX = ReflectionHelper.findField(c, "field_187129_i", "motionX").getDouble(particle);
						mY = ReflectionHelper.findField(c, "field_187130_j", "motionY").getDouble(particle);
						mZ = ReflectionHelper.findField(c, "field_187131_k", "motionZ").getDouble(particle);

						X = ReflectionHelper.findField(c, "field_187126_f", "posX").getDouble(particle);
						Y = ReflectionHelper.findField(c, "field_187127_g", "posY").getDouble(particle);
						Z = ReflectionHelper.findField(c, "field_187128_h", "posZ").getDouble(particle);
					}
				} catch (Exception e) {
					if (Minecraft.getMinecraft().thePlayer.onGround) {
						if ((prevSourceState = worldObj.getBlockState(
								new BlockPos(this.interpPosX, this.interpPosY, this.interpPosZ))) == null) {
							prevSourceState = Blocks.LAVA.getDefaultState();
						}
					}
				}
				if (prevSourceState != null
						&& (!(prevSourceState.getBlock() instanceof BlockLiquid)
								&& !(FBP.frozen && !FBP.spawnWhileFrozen))
						&& (FBP.spawnRedstoneBlockParticles || prevSourceState.getBlock() != Blocks.REDSTONE_BLOCK))
					toAdd.add(new FBPParticleDigging(worldObj, X, Y, Z, mX, mY, mZ, prevSourceState));

				particle.setExpired();
			});
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