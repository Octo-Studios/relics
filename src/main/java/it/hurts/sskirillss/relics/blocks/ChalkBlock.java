package it.hurts.sskirillss.relics.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class ChalkBlock extends Block implements IVoidBlock {
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);

    public ChalkBlock() {
        super(AbstractBlock.Properties.of(Material.TOP_SNOW)
                .instabreak()
                .noCollission()
                .randomTicks()
                .noOcclusion());
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.getBlockState(pos.below()).canOcclude())
            worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AABB;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return AABB;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (worldIn.isRainingAt(pos))
            worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
}