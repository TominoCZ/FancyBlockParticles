package com.TominoCZ.FBP.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.node.BlockNode;
import com.TominoCZ.FBP.particle.FBPParticleBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class FBPAnimationDummyBlock extends Block {

	ConcurrentHashMap<FBPBlockPos, BlockNode> blockNodes = new ConcurrentHashMap<FBPBlockPos, BlockNode>();

	AxisAlignedBB FULL_BLOCK_AABB;
	AxisAlignedBB NULL_BLOCK_AABB;

	public FBPAnimationDummyBlock() {
		super(Material.rock);

		FULL_BLOCK_AABB = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		NULL_BLOCK_AABB = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	public void copyState(FBPBlockPos pos, Block b, int meta, FBPParticleBlock p) {
		if (hasNode(pos))
			return;

		blockNodes.put(pos, new BlockNode(b, meta, p));
	}

	public BlockNode getNode(FBPBlockPos p1) {
		Iterator it = blockNodes.keySet().iterator();

		while (it.hasNext()) {
			FBPBlockPos p2 = (FBPBlockPos) it.next();

			if (p1.getX() == p2.getX() && p1.getY() == p2.getY() && p1.getZ() == p2.getZ())
				return blockNodes.get(p2);
		}

		return null;
	}

	public void removeNode(FBPBlockPos p1) {
		Iterator it = blockNodes.keySet().iterator();

		while (it.hasNext()) {
			FBPBlockPos p2 = (FBPBlockPos) it.next();

			if (p1.getX() == p2.getX() && p1.getY() == p2.getY() && p1.getZ() == p2.getZ()) {
				blockNodes.remove(p2);
				break;
			}
		}
	}

	public boolean hasNode(FBPBlockPos pos) {
		return getNode(pos) != null;
	}

	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer p, int side, float hitX,
			float hitY, float hitZ) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos)) {
			BlockNode n = getNode(pos);

			try {
				return n.block.onBlockActivated(worldIn, x, y, z, p, side, hitX, hitY, hitZ);
			} catch (Throwable t) {
				return false;
			}
		}

		return super.onBlockActivated(worldIn, x, y, z, p, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, int x, int y, int z) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);

			if (hasNode(pos)) {
				BlockNode n = getNode(pos);

				return n.block.isReplaceable(worldIn, x, y, z);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, int x, int y, int z, Entity entityIn) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);

			if (hasNode(pos)) {
				BlockNode n = getNode(pos);

				n.block.onEntityCollidedWithBlock(worldIn, x, y, z, entityIn);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		try {
			if (hasNode(pos)) {
				BlockNode n = getNode(pos);

				return n.block.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return FULL_BLOCK_AABB.offset(x, y, z);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
		return NULL_BLOCK_AABB;
	}

	@Override
	public float getBlockHardness(World w, int x, int y, int z) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);

			if (hasNode(pos)) {
				BlockNode n = getNode(pos);

				return n.block.getBlockHardness(w, x, y, z);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return -1;
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, Block b, int meta) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);

			BlockNode node = getNode(pos);

			if (node == null)
				return;

			if (w.isRemote && b != node.block && b instanceof FBPAnimationDummyBlock
					|| b instanceof FBPAnimationDummyBlock)
				Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(x, y, z, b, meta);

			if (node.particle != null)
				node.particle.setDead();

			// cleanup just to make sure it gets removed
			FBP.INSTANCE.eventHandler.removePosEntry(pos);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void addCollisionBoxesToList(World w, int x, int y, int z, AxisAlignedBB aabb, List aabbs, Entity e) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);
			if (hasNode(pos))
				getNode(pos).block.addCollisionBoxesToList(w, x, y, z, aabb, aabbs, e);
			return;
		} catch (Exception e1) {

		}
	}

	@Override
	public float getExplosionResistance(Entity e, World w, int x, int y, int z, double ex, double ey, double ez) {
		FBPBlockPos p = new FBPBlockPos(x, y, z);
		if (hasNode(p))
			return getNode(p).block.getExplosionResistance(e, w, x, y, z, ex, ey, ez);

		return super.getExplosionResistance(e, w, x, y, z, ex, ey, ez);
	}

	@Override
	public float getExplosionResistance(Entity e) {
		FBPBlockPos pos = new FBPBlockPos(e.posX, e.posY, e.posZ);

		if (hasNode(pos))
			return getNode(pos).block.getExplosionResistance(e);

		return super.getExplosionResistance(e);
	}

	@Override
	public float getEnchantPowerBonus(World w, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.getEnchantPowerBonus(w, x, y, z);

		return super.getEnchantPowerBonus(w, x, y, z);
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.getFlammability(world, x, y, z, side);

		return super.getFlammability(world, x, y, z, side);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.getFireSpreadSpeed(world, x, y, z, side);

		return super.getFireSpreadSpeed(world, x, y, z, side);
	}

	@Override
	public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.getWeakChanges(world, x, y, z);

		return super.getWeakChanges(world, x, y, z);
	}

	@Override
	public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.canPlaceTorchOnTop(world, x, y, z);

		return true;
	}

	@Override
	public boolean canPlaceBlockAt(World w, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.canPlaceBlockAt(w, x, y, z);

		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World w, int x, int y, int z, int side) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.canPlaceBlockOnSide(w, x, y, z, side);

		return true;
	}

	@Override
	public Item getItem(World w, int x, int y, int z) {
		try {
			FBPBlockPos pos = new FBPBlockPos(x, y, z);

			if (hasNode(pos))
				return getNode(pos).block.getItem(w, x, y, z);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return new Item();
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess w, int x, int y, int z, int side) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.canConnectRedstone(w, x, y, z, side);

		return false;
	}

	@Override
	public void onBlockAdded(World w, int x, int y, int z) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			getNode(pos).block.onBlockAdded(w, x, y, z);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World w, int x, int y, int z, int meta, int fortune) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.getDrops(w, x, y, z, meta, fortune);

		return super.getDrops(w, x, y, z, meta, fortune);
	}

	@Override
	public Item getItemDropped(int i1, Random r, int i2) {
		return new Item();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getAmbientOcclusionLightValue() {
		return 1.0F;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		FBPBlockPos pos = new FBPBlockPos(x, y, z);

		if (hasNode(pos))
			return getNode(pos).block.isSideSolid(world, x, y, z, side);

		return true;
	}
}
