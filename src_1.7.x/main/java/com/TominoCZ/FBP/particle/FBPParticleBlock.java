package com.TominoCZ.FBP.particle;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.renderer.FBPRenderBlocks;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class FBPParticleBlock extends EntityFX {

	public FBPBlockPos pos;

	public Block block;
	public int meta;

	Minecraft mc;

	EnumFacing facing;

	FBPVector3d prevRot;
	FBPVector3d rot;

	float startingHeight;
	float startingAngle;
	float step = 0.00275f;

	float height;
	float prevHeight;

	boolean lookingUp;
	boolean spawned = false;
	long tick = -1;

	TileEntity tileEntity;

	FBPRenderBlocks renderblocks;
	
	@SuppressWarnings("incomplete-switch")
	public FBPParticleBlock(World worldIn, double posXIn, double posYIn, double posZIn, Block b, int meta) {
		super(worldIn, posXIn, posYIn, posZIn);

		pos = new FBPBlockPos(posXIn, posYIn, posZIn);

		mc = Minecraft.getMinecraft();

		float yaw = mc.thePlayer.rotationYaw;

		int heading = MathHelper.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5D) & 3;

		facing = EnumFacing.valueOf(Direction.directions[heading]);

		lookingUp = Float.valueOf(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch)) <= 0;

		height = startingHeight = (float) FBP.random.nextDouble(0.065, 0.115);
		startingAngle = (float) FBP.random.nextDouble(0.03125, 0.0635);

		prevRot = new FBPVector3d();
		rot = new FBPVector3d();

		block = b;
		this.meta = meta;

		prevRot.x = rot.x = 0;
		prevRot.z = rot.z = 0;

		switch (facing) {
		case EAST:
			rot.z = -startingAngle;
			rot.x = -startingAngle;
			break;
		case NORTH:
			rot.x = -startingAngle;
			rot.z = startingAngle;
			break;
		case SOUTH:
			rot.x = startingAngle;
			rot.z = -startingAngle;
			break;
		case WEST:
			rot.z = startingAngle;
			rot.x = startingAngle;
			break;
		}

		this.isAirBorne = false;

		tileEntity = worldIn.getTileEntity((int) posX, (int) posY, (int) posZ);
		
		 renderblocks = new FBPRenderBlocks(mc.theWorld);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (++particleAge >= 10)
			setDead();

		if (!isAirBorne) {
			Block b = mc.theWorld.getBlock(pos.getX(), pos.getY(), pos.getZ());

			if (b == block) {
				Block bl = Blocks.ladder;
				
				bl.setBlockBounds(
						(float) block.getBlockBoundsMinX(),
						(float) block.getBlockBoundsMinY(),
						(float) block.getBlockBoundsMinZ(),
						(float) block.getBlockBoundsMaxX(),
						(float) block.getBlockBoundsMaxY(),
						(float) block.getBlockBoundsMaxZ());
				
				mc.theWorld.setBlock(pos.getX(), pos.getY(), pos.getZ(), bl, meta, 1);
			}
			
			spawned = true;
		}

		if (this.isDead || mc.isGamePaused())
			return;

		prevHeight = height;

		prevRot.copyFrom(rot);

		switch (facing) {
		case EAST:
			rot.z += step;
			rot.x += step;
			break;
		case NORTH:
			rot.x += step;
			rot.z -= step;
			break;
		case SOUTH:
			rot.x -= step;
			rot.z += step;
			break;
		case WEST:
			rot.z -= step;
			rot.x -= step;
			break;
		}

		height -= step * 5f;

		step *= 1.5678982f;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
		if (this.isDead)
			return;

		if (isAirBorne) {
			if (tick == -1) {
				mc.theWorld.setBlock(pos.getX(), pos.getY(), pos.getZ(), block, meta, 2);

				//mc.theWorld.notifyBlocksOfNeighborChange(pos.getX(), pos.getY(), pos.getZ(), block);

				if (tileEntity != null)
					mc.theWorld.setTileEntity(pos.getX(), pos.getY(), pos.getZ(), tileEntity);

				// cleanup just to make sure it gets removed
				FBP.INSTANCE.eventHandler.removePosEntry(pos);
			}
			if (tick >= 1) {
				setDead();
				return;
			}

			tick++;
		} else {
			Block bl = Blocks.ladder;
			
			bl.setBlockBounds(
					(float) block.getBlockBoundsMinX(),
					(float) block.getBlockBoundsMinY(),
					(float) block.getBlockBoundsMinZ(),
					(float) block.getBlockBoundsMaxX(),
					(float) block.getBlockBoundsMaxY(),
					(float) block.getBlockBoundsMaxZ());
			
			mc.theWorld.setBlock(pos.getX(), pos.getY(), pos.getZ(), bl, meta, 1);
		}
		if (!spawned)
			return;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;
		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX) - 0.5f;
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY) - 0.5f;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ) - 0.5f;

		final FBPVector3d smoothRot = rot.partialVec(prevRot, partialTicks);

		float smoothHeight = ((float) (prevHeight + (height - prevHeight) * (double) partialTicks));

		if (smoothHeight <= 0)
			smoothHeight = 0;

		FBPVector3d t = new FBPVector3d(0, smoothHeight, 0);
		FBPVector3d tRot = new FBPVector3d(0, smoothHeight, 0);

		switch (facing) {
		case EAST:
			if (smoothRot.z > 0) {
				this.isAirBorne = true;
				smoothRot.z = 0;
				smoothRot.x = 0;
			}

			t.x = -smoothHeight;
			t.z = smoothHeight;

			tRot.x = 1;
			break;
		case NORTH:
			if (smoothRot.z < 0) {
				this.isAirBorne = true;
				smoothRot.x = 0;
				smoothRot.z = 0;
			}

			t.x = smoothHeight;
			t.z = smoothHeight;
			break;
		case SOUTH:
			if (smoothRot.x < 0) {
				this.isAirBorne = true;
				smoothRot.x = 0;
				smoothRot.z = 0;
			}

			t.x = -smoothHeight;
			t.z = -smoothHeight;

			tRot.x = 1;
			tRot.z = 1;
			break;
		case WEST:
			if (smoothRot.z < 0) {
				this.isAirBorne = true;
				smoothRot.z = 0;
				smoothRot.x = 0;
			}

			t.x = smoothHeight;
			t.z = -smoothHeight;

			tRot.z = 1;
			break;
		}

		if (FBP.spawnPlaceParticles && isAirBorne && tick == 0) {
			if ((!(FBP.frozen && !FBP.spawnWhileFrozen)
					&& (FBP.spawnRedstoneBlockParticles || block != Blocks.redstone_block))
					&& mc.gameSettings.particleSetting < 2) {
				spawnParticles();
			}
		}

		// RENDER MODEL
		tes.draw();
		mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();
		tes.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());

		GL11.glPushMatrix();

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);
		GL11.glDisable(GL11.GL_LIGHTING);
		if (mc.gameSettings.ambientOcclusion != 0)
			GL11.glShadeModel(GL11.GL_SMOOTH);

		GL11.glTranslated(f5, f6, f7);

		// rotation
		GL11.glTranslated(tRot.x, tRot.y, tRot.z);
		GL11.glRotated(Math.toDegrees(smoothRot.x), 1, 0, 0);
		GL11.glRotated(Math.toDegrees(smoothRot.z), 0, 0, 1);
		GL11.glTranslated(-tRot.x, -tRot.y, -tRot.z);

		// movement
		GL11.glTranslated(t.x, t.y, t.z);
		
		renderblocks.setRenderAllFaces(true);
		renderblocks.renderBlockByRenderType(block, pos.getX(), pos.getY(), pos.getZ(), meta);
		renderblocks.setRenderAllFaces(false);

		tes.setTranslation(0, 0, 0);

		tes.draw();
		GL11.glPopMatrix();

		if (mc.gameSettings.ambientOcclusion != 0)
			GL11.glShadeModel(GL11.GL_FLAT);

		mc.getTextureManager().bindTexture(FBP.LOCATION_PARTICLE_TEXTURE);
		tes.startDrawingQuads();
	}

	private void spawnParticles() {
		FBPBlockPos p = pos.offset(EnumFacing.DOWN);

		if (mc.theWorld.getBlock(p.getX(), p.getY(), p.getZ()) instanceof BlockAir)
			return;

		AxisAlignedBB aabb = block.getSelectedBoundingBoxFromPool(mc.theWorld, pos.getX(), pos.getY(), pos.getZ());

		// z- = north
		// x- = west // block pos

		Vector2d[] corners = new Vector2d[] { new Vector2d(aabb.minX, aabb.minZ), new Vector2d(aabb.maxX, aabb.maxZ),

				new Vector2d(aabb.minX, aabb.maxZ), new Vector2d(aabb.maxX, aabb.minZ) };

		Vector2d middle = new Vector2d(pos.getX() + 0.5f, pos.getZ() + 0.5f);

		for (Vector2d corner : corners) {
			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.5;
			mZ /= -0.5;

			mc.effectRenderer.addEffect(
					new FBPParticleDigging(mc.theWorld, corner.x, pos.getY() + 0.1f, corner.y, mX, 0, mZ, 1, 1, 1,
							particleScale, block, meta, 0).applyColourMultiplier(pos.getX(), pos.getY(), pos.getZ())
									.multipleParticleScaleBy(0.31f).multiplyVelocity(0.5f));
		}

		for (Vector2d corner : corners) {
			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.45;
			mZ /= -0.45;

			mc.effectRenderer.addEffect(
					new FBPParticleDigging(mc.theWorld, corner.x, pos.getY() + 0.1f, corner.y, mX, 0, mZ, 1, 1, 1,
							particleScale, block, meta, 0).applyColourMultiplier(pos.getX(), pos.getY(), pos.getZ())
									.multipleParticleScaleBy(0.31f).multiplyVelocity(0.5f));
		}
	}

	@Override
	public void setDead() {
		this.isDead = true;

		mc.gameSettings.hideGUI = false;
		
		renderblocks.removeMetaTag(pos);
		FBP.FBPBlock.removeNode(pos);
		FBP.INSTANCE.eventHandler.removePosEntry(pos);
	}
}
