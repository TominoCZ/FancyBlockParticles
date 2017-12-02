package com.TominoCZ.FBP.particle;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.model.FBPModelTransformer;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class FBPParticleBlock extends Particle {

	public BlockPos pos;

	Block block;
	IBlockState blockState;

	BlockModelRenderer mr;

	IBakedModel modelPrefab;

	Minecraft mc;

	EnumFacing facing;

	FBPVector3d prevRot;
	FBPVector3d rot;

	long textureSeed;

	float startingHeight;
	float startingAngle;
	float step = 0.00275f;

	float height;
	float prevHeight;

	float smoothHeight;

	boolean lookingUp;
	boolean spawned = false;
	long tick = -1;

	boolean blockSet = false;

	TileEntity tileEntity;

	public FBPParticleBlock(World worldIn, double posXIn, double posYIn, double posZIn, IBlockState state, long rand) {
		super(worldIn, posXIn, posYIn, posZIn);

		pos = new BlockPos(posXIn, posYIn, posZIn);

		mc = Minecraft.getMinecraft();

		facing = mc.thePlayer.getHorizontalFacing();

		lookingUp = Float.valueOf(MathHelper.wrapDegrees(mc.thePlayer.rotationPitch)) <= 0;

		height = startingHeight = (float) FBP.random.nextDouble(0.065, 0.115);
		startingAngle = (float) FBP.random.nextDouble(0.03125, 0.0635);

		prevRot = new FBPVector3d();
		rot = new FBPVector3d();

		textureSeed = rand;

		block = (blockState = state).getBlock();

		prepareModelForRender(state);

		prevRot.x = rot.x = 0;
		prevRot.z = rot.z = 0;

		this.canCollide = false;

		if (modelPrefab == null) {
			canCollide = true;
			this.isExpired = true;
		}

		tileEntity = worldIn.getTileEntity(pos);
	}

	private void prepareModelForRender(IBlockState state) {
		mr = mc.getBlockRendererDispatcher().getBlockModelRenderer();

		BlockModelShapes shapes = mc.getBlockRendererDispatcher().getBlockModelShapes();
		modelPrefab = mc.getBlockRendererDispatcher().getModelForState(state);
		this.particleTexture = shapes.getModelManager().getBlockModelShapes().getTexture(state);

		modelPrefab = FBPModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new FBPModelTransformer.IVertexTransformer() {

					@SuppressWarnings("incomplete-switch")
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							Vector3f vec = new Vector3f(data[0], data[1], data[2]);

							switch (facing) {
							case EAST:
								rot.z = -startingAngle;
								rot.x = -startingAngle;

								vec.x += 0.00469011612D;
								vec.z -= 0.00319011612D;
								break;
							case NORTH:
								rot.x = -startingAngle;
								rot.z = startingAngle;

								vec.x -= 0.003989D;
								vec.z -= 0.00469011612D;// orig
								break;
							case SOUTH:
								rot.x = startingAngle;
								rot.z = -startingAngle;

								vec.x += 0.003989D;
								vec.z += 0.00469011612D;// orig
								break;
							case WEST:
								rot.z = startingAngle;
								rot.x = startingAngle;

								vec.x -= 0.00469011612D;
								vec.z += 0.00469011612D;
								break;
							}

							vec.y += 0.00448325;

							vec = FBPRenderUtil.rotatef_f(vec, (float) rot.x, (float) rot.y, (float) rot.z, facing);

							return new float[] {
									vec.x + (facing == EnumFacing.EAST ? -startingHeight
											: (facing == EnumFacing.WEST ? startingHeight : 0)),
									vec.y + startingHeight, vec.z + (facing == EnumFacing.NORTH ? -startingHeight
											: (facing == EnumFacing.SOUTH ? startingHeight : 0)) };
						}

						return data;
					}
				});
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onUpdate() {
		if (++particleAge >= 10)
			killParticle();

		if (!canCollide) {
			IBlockState s = mc.theWorld.getBlockState(pos);

			if (s.getBlock() != FBP.FBPBlock || s.getBlock() == block) {
				if (blockSet && s.getBlock() == Blocks.AIR) {
					// the block was destroyed during the animation
					killParticle();

					FBP.FBPBlock.onBlockDestroyedByPlayer(mc.theWorld, pos, s);
					mc.theWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
					return;
				}

				FBP.FBPBlock.copyState(mc.theWorld, pos, blockState, this);
				mc.theWorld.setBlockState(pos, FBP.FBPBlock.getDefaultState(), 2);

				Chunk c = mc.theWorld.getChunkFromBlockCoords(pos);
				c.resetRelightChecks();
				c.setLightPopulated(true);

				FBPRenderUtil.markBlockForRender(pos);

				blockSet = true;
			}

			spawned = true;
		}

		if (this.isExpired || mc.isGamePaused())
			return;

		prevHeight = height;

		prevRot.copyFrom(rot);

		switch (facing) {
		case EAST:
			rot.z += step;
			rot.x -= step;
			break;
		case NORTH:
			rot.x -= step;
			rot.z -= step;
			break;
		case SOUTH:
			rot.x += step;
			rot.z += step;
			break;
		case WEST:
			rot.z -= step;
			rot.x += step;
			break;
		}

		height -= step * 5f;

		step *= 1.5678982f;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void renderParticle(VertexBuffer buff, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		if (this.isExpired)
			return;

		if (canCollide) {
			Block b = mc.theWorld.getBlockState(pos).getBlock();
			if (block != b && b != Blocks.AIR && mc.theWorld.getBlockState(pos).getBlock() != blockState.getBlock()) {
				mc.theWorld.setBlockState(pos, blockState, 2);

				if (tileEntity != null)
					mc.theWorld.setTileEntity(pos, tileEntity);

				mc.theWorld.sendPacketToServer(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, pos, facing));

				FBPRenderUtil.markBlockForRender(pos);

				// cleanup just to make sure it gets removed
				FBP.INSTANCE.eventHandler.removePosEntry(pos);
			}
			if (tick >= 1) {
				killParticle();
				return;
			}

			tick++;
		}
		if (!spawned)
			return;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX) - 0.5f;
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY) - 0.5f;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ) - 0.5f;

		final FBPVector3d smoothRot = rot.partialVec(prevRot, partialTicks);

		smoothHeight = ((float) (prevHeight + (height - prevHeight) * (double) partialTicks));

		if (smoothHeight <= 0)
			smoothHeight = 0;

		switch (facing) {
		case EAST:
			if (smoothRot.z >= startingAngle) {
				this.canCollide = true;
				smoothRot.z = startingAngle;
				smoothRot.x = -startingAngle;
			}
			break;
		case NORTH:
			if (smoothRot.x <= -startingAngle) {
				this.canCollide = true;
				smoothRot.x = -startingAngle;
				smoothRot.z = -startingAngle;
			}
			break;
		case SOUTH:
			if (smoothRot.x >= startingAngle) {
				this.canCollide = true;
				smoothRot.x = startingAngle;
				smoothRot.z = startingAngle;
			}
			break;
		case WEST:
			if (smoothRot.z <= -startingAngle) {
				this.canCollide = true;
				smoothRot.z = -startingAngle;
				smoothRot.x = startingAngle;
			}
			break;
		}

		if (FBP.spawnPlaceParticles && canCollide && tick == 0) {
			if ((!(FBP.frozen && !FBP.spawnWhileFrozen)
					&& (FBP.spawnRedstoneBlockParticles || block != Blocks.REDSTONE_BLOCK))
					&& mc.gameSettings.particleSetting < 2) {
				spawnParticles();
			}
		}
		buff.setTranslation(f5 - pos.getX(), f6 - pos.getY(), f7 - pos.getZ());

		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		IBakedModel modelForRender = FBPModelTransformer.transform(modelPrefab, blockState, textureSeed,
				new FBPModelTransformer.IVertexTransformer() {
					@Override
					public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
						if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
							Vector3f vec = FBPRenderUtil.rotatef_f(new Vector3f(data[0], data[1], data[2]),
									(float) smoothRot.x, (float) smoothRot.y, (float) smoothRot.z, facing);

							float f = (startingHeight - smoothHeight);

							float x = (facing == EnumFacing.EAST ? f : (facing == EnumFacing.WEST ? -f : 0));
							float y = -f;
							float z = (facing == EnumFacing.NORTH ? -f : (facing == EnumFacing.SOUTH ? f : 0));

							return new float[] { vec.x + x, vec.y + y, vec.z + z };
						}

						return data;
					}
				});

		GlStateManager.enableCull();
		GlStateManager.enableColorMaterial();
		GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE);

		mr.renderModel(mc.theWorld, modelForRender, blockState, pos, buff, false, textureSeed);

		buff.setTranslation(0, 0, 0);

		Tessellator.getInstance().draw();
		mc.getTextureManager().bindTexture(FBP.LOCATION_PARTICLE_TEXTURE);
		buff.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	private void spawnParticles() {
		if (mc.theWorld.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() instanceof BlockAir)
			return;

		AxisAlignedBB aabb = block.getSelectedBoundingBox(blockState, mc.theWorld, pos);

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

			mc.effectRenderer
					.addEffect(new FBPParticleDigging(mc.theWorld, corner.x, pos.getY() + 0.1f, corner.y, mX, 0, mZ, 1,
							1, 1, block.getActualState(blockState, mc.theWorld, pos), null, 0.6f, this.particleTexture)
									.multipleParticleScaleBy(0.5f).multiplyVelocity(0.5f));
		}

		for (Vector2d corner : corners) {
			if (corner == null)
				continue;

			double mX = middle.x - corner.x;
			double mZ = middle.y - corner.y;

			mX /= -0.45;
			mZ /= -0.45;

			mc.effectRenderer.addEffect(
					new FBPParticleDigging(mc.theWorld, corner.x, pos.getY() + 0.1f, corner.y, mX / 3, 0, mZ / 3, 1, 1,
							1, block.getActualState(blockState, mc.theWorld, pos), null, 0.6f, this.particleTexture)
									.multipleParticleScaleBy(0.75f).multiplyVelocity(0.75f));
		}
	}

	public void killParticle() {
		this.isExpired = true;

		FBP.FBPBlock.blockNodes.remove(pos);
		FBP.INSTANCE.eventHandler.removePosEntry(pos);
	}

	@Override
	public void setExpired() {
		FBP.INSTANCE.eventHandler.removePosEntry(pos);
	}
}
