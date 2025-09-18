package com.yor42.solarapocalypse.util;

import com.yor42.solarapocalypse.Main;
import com.yor42.solarapocalypse.capabilities.ChunkApocalypseProvider;
import com.yor42.solarapocalypse.capabilities.IChunkApocalypse;
import com.yor42.solarapocalypse.world.SolarApocalypseData;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Random;
import java.util.function.BiPredicate;

public class ApocalypseHelper {

    // 블록 변환 캐싱을 위한 상수들
    private static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();
    private static final IBlockState FIRE_STATE = Blocks.FIRE.getDefaultState();
    private static final IBlockState MAGMA_STATE = Blocks.MAGMA.getDefaultState();
    private static final IBlockState STONE_STATE = Blocks.STONE.getDefaultState();
    private static final IBlockState COBBLESTONE_STATE = Blocks.COBBLESTONE.getDefaultState();
    private static final IBlockState GLASS_STATE = Blocks.GLASS.getDefaultState();
    private static final IBlockState SAND_STATE = Blocks.SAND.getDefaultState();
    private static final IBlockState HARDENED_CLAY_STATE = Blocks.HARDENED_CLAY.getDefaultState();
    private static final IBlockState GRAVEL_STATE = Blocks.GRAVEL.getDefaultState();
    private static final IBlockState DIRT_STATE = Blocks.DIRT.getDefaultState();
    private static final IBlockState DEADBUSH_STATE = Blocks.DEADBUSH.getDefaultState();
    private static final IBlockState WATER_STATE = Blocks.WATER.getDefaultState();

    /**
     * 최적화된 블록 변환 - catchup 모드에서는 중간 과정을 건너뛰고 최종 결과만 적용
     */
    public static void applyStageToBlockAt(WorldServer world, BlockPos currentPos, int globalStage, boolean catchup_mode, Random rand) {
        if (globalStage <= 0) {
            return;
        }

        IBlockState state = world.getBlockState(currentPos);
        Block block = state.getBlock();

        // catchup 모드에서는 최종 결과만 적용 (성능 최적화)
        if (catchup_mode) {
            applyCatchupTransformation(world, currentPos, block, state, globalStage);
        } else {
            applyNormalTransformation(world, currentPos, block, state, globalStage, rand);
        }
    }

    /**
     * Catchup 모드 - 중간 과정 없이 최종 결과만 적용
     */
    private static void applyCatchupTransformation(WorldServer world, BlockPos pos, Block block, IBlockState state, int globalStage) {
        // 가연성 재료는 모든 stage에서 즉시 제거
        if (state.getMaterial().getCanBurn()) {
            world.setBlockState(pos, AIR_STATE, 2);
            return;
        }

        // 최종 단계별 변환 체크 (역순으로 체크해서 최고 단계 변환 적용)
        if (globalStage >= 7) {
            if (block == Blocks.STONE) {
                world.setBlockState(pos, MAGMA_STATE, 2);
                return;
            }
        }

        if (globalStage >= 5) {
            if (block == Blocks.COBBLESTONE) {
                world.setBlockState(pos, STONE_STATE, 2);
                return;
            }
            if (block instanceof BlockGlass) {
                world.setBlockState(pos, AIR_STATE, 2);
                return;
            }
            if (block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY) {
                world.setBlockState(pos, SAND_STATE, 2);
                return;
            }
        }

        if (globalStage >= 4) {
            if (block == Blocks.GRAVEL) {
                world.setBlockState(pos, COBBLESTONE_STATE, 2);
                return;
            }
            if (block == Blocks.CLAY) {
                world.setBlockState(pos, HARDENED_CLAY_STATE, 2);
                return;
            }
        }

        if (globalStage >= 3) {
            if (block instanceof BlockSand) {
                world.setBlockState(pos, GLASS_STATE, 2);
                return;
            }
            if (block instanceof BlockDirt) {
                world.setBlockState(pos, GRAVEL_STATE, 2);
                return;
            }
        }

        if (globalStage >= 2) {
            if (block instanceof BlockSapling) {
                world.setBlockState(pos, DEADBUSH_STATE, 2);
                return;
            }
            if (block instanceof BlockBush || block instanceof BlockVine ||
                    block instanceof BlockCocoa || block instanceof BlockCactus ||
                    block instanceof BlockSnowBlock || block instanceof BlockReed) {
                world.setBlockState(pos, AIR_STATE, 2);
                return;
            }
            if (block instanceof BlockGrassPath) {
                world.setBlockState(pos, DIRT_STATE, 2);
                return;
            }
            if (block instanceof BlockIce) {
                world.setBlockState(pos, WATER_STATE, 2);
                return;
            }
        }

        if (globalStage >= 1) {
            if (block instanceof BlockLeaves || block instanceof BlockDoublePlant ||
                    block instanceof BlockSnow || block instanceof BlockTallGrass ||
                    block instanceof BlockLilyPad) {
                world.setBlockState(pos, AIR_STATE, 2);
                return;
            }
            if (block instanceof BlockGrass || block instanceof BlockMycelium) {
                world.setBlockState(pos, DIRT_STATE, 2);
                return;
            }
            if (block == Blocks.MOSSY_COBBLESTONE) {
                world.setBlockState(pos, COBBLESTONE_STATE, 2);
                return;
            }
        }
    }

    private static void applyNormalTransformation(WorldServer world, BlockPos currentPos, Block block, IBlockState state, int globalStage, Random rand) {
        switch (globalStage) {
            case 7:
                if (block == Blocks.STONE) {
                    world.setBlockState(currentPos, MAGMA_STATE, 2);
                    return;
                }
            case 5:
                if (block == Blocks.COBBLESTONE) {
                    world.setBlockState(currentPos, STONE_STATE, 2);
                    return;
                } else if (block instanceof BlockGlass) {
                    world.destroyBlock(currentPos, false);
                    return;
                } else if (block == Blocks.HARDENED_CLAY || block == Blocks.STAINED_HARDENED_CLAY) {
                    world.setBlockState(currentPos, SAND_STATE, 2);
                    return;
                }
            case 4:
                if (block == Blocks.GRAVEL) {
                    world.setBlockState(currentPos, COBBLESTONE_STATE, 2);
                    return;
                } else if (block == Blocks.CLAY) {
                    world.setBlockState(currentPos, HARDENED_CLAY_STATE, 2);
                    return;
                }
            case 3:
                if (state.getMaterial().getCanBurn()) {
                    // 일반 모드에서는 불 효과 생성
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        if (facing == EnumFacing.DOWN) {
                            continue;
                        }
                        BlockPos pos = currentPos.offset(facing);
                        if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                            world.setBlockState(pos, FIRE_STATE, 2);
                            break;
                        }
                    }
                    return;
                } else if (block instanceof BlockSand) {
                    world.setBlockState(currentPos, GLASS_STATE, 2);
                    return;
                } else if (block instanceof BlockDirt) {
                    world.setBlockState(currentPos, GRAVEL_STATE, 2);
                    return;
                }
            case 2:
                if (block instanceof BlockSapling) {
                    world.setBlockState(currentPos, DEADBUSH_STATE, 2);
                    return;
                } else if (block instanceof BlockBush || block instanceof BlockVine ||
                        block instanceof BlockCocoa || block instanceof BlockCactus ||
                        block instanceof BlockSnowBlock || block instanceof BlockReed) {
                    world.setBlockState(currentPos, AIR_STATE, 2);
                    return;
                } else if (block instanceof BlockGrassPath) {
                    world.setBlockState(currentPos, DIRT_STATE, 2);
                    return;
                } else if (block instanceof BlockIce) {
                    world.setBlockState(currentPos, WATER_STATE, 2);
                    return;
                }
            case 1:
                if (block instanceof BlockLeaves || block instanceof BlockDoublePlant ||
                        block instanceof BlockSnow || block instanceof BlockTallGrass ||
                        block instanceof BlockLilyPad) {
                    world.setBlockState(currentPos, AIR_STATE, 2);
                    return;
                } else if (block instanceof BlockGrass || block instanceof BlockMycelium) {
                    world.setBlockState(currentPos, DIRT_STATE, 2);
                    return;
                } else if (block == Blocks.MOSSY_COBBLESTONE) {
                    world.setBlockState(currentPos, COBBLESTONE_STATE, 2);
                    return;
                }
        }
    }

    public static void doApocalypseCatchUp(World world, Chunk chunk, int to, Random rand, boolean offset) {
        // catchup 모드에서는 최종 단계만 적용하여 성능 향상
        ApocalypseHelper.applyStageToEntireChunk(world, chunk, to, rand,
                ((blockPos, random) -> {
                    // 확률 계산 최적화
                    SolarApocalypseData globalData = SolarApocalypseData.get(world);
                    long stageElapsedTime = globalData.getStageElapsedTime(world);
                    long halfphase = Main.TICKS_PER_STAGE / 2;

                    // 50% 이상 진행되었으면 무조건 적용
                    if (stageElapsedTime >= halfphase) {
                        return true;
                    }
                    // 그렇지 않으면 확률적 적용
                    return rand.nextFloat() < (float) (2.0 * stageElapsedTime) / Main.TICKS_PER_STAGE;
                }),
                true, // catchup 모드로 설정
                offset
        );
    }

    public static void BurnBabyBurn(Entity entity) {
        World world = entity.world;

        if (world.provider.getDimension() != 0 ||
                entity.ticksExisted % 19 != 0 ||
                world.isRemote) {
            return;
        }

        SolarApocalypseData data = SolarApocalypseData.get(world);
        int stage = data.getStage();
        if (stage < 3) {
            return;
        }

        // 플레이어 특별 조건 체크
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.isCreative() || player.isSpectator()) {
                return;
            }
        }

        // 환경 조건 체크 (비용이 큰 연산들을 마지막에)
        if (!world.isDaytime() ||
                entity.isInWater() ||
                !world.canSeeSky(new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ))) {
            return;
        }

        entity.setFire(8);
    }

    public static void applyStageToEntireChunk(World world, Chunk chunk, int stageToApply, Random rand,
                                               BiPredicate<BlockPos, Random> chance, boolean catchup_mode, boolean offset) {
        if (!chunk.isLoaded() || world.isRemote) {
            return;
        }

        int coordoffset = offset ? 8 : 0;
        int worldX = (chunk.x * 16) + coordoffset;
        int worldZ = (chunk.z * 16) + coordoffset;

        // BlockPos 재사용을 위한 MutableBlockPos 사용
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 위에서부터 아래로 스캔
                boolean foundSolidGround = false;

                for (int y = chunk.getTopFilledSegment() + 15; y > 0; y--) {
                    mutablePos.setPos(worldX + x, y, worldZ + z);
                    IBlockState state = world.getBlockState(mutablePos);
                    Block block = state.getBlock();

                    if (block == Blocks.AIR) {
                        continue;
                    }

                    // 확률 체크
                    if (chance.test(mutablePos, rand)) {
                        // 불변 BlockPos로 변환하여 처리
                        BlockPos immutablePos = mutablePos.toImmutable();

                        if (catchup_mode) {
                            // catchup 모드에서 연쇄 연소 시뮬레이션
                            boolean blockWasRemoved = applyCatchupTransformationWithChaining(
                                    (WorldServer) world, immutablePos, block, state, stageToApply);

                            // 블록이 제거되었으면 계속 아래로 스캔 (연쇄 효과)
                            if (blockWasRemoved) {
                                continue;
                            }
                        } else {
                            ApocalypseHelper.applyStageToBlockAt((WorldServer) world, immutablePos, stageToApply, catchup_mode, rand);
                        }
                    }

                    // 불투명하고 불에 타지 않는 블록에서 스캔 중단
                    if (state.isOpaqueCube() && !state.getMaterial().getCanBurn()) {
                        foundSolidGround = true;
                        break;
                    }
                }
            }
        }
    }

    private static boolean applyCatchupTransformationWithChaining(WorldServer world, BlockPos pos, Block block, IBlockState state, int globalStage) {
        if (state.getMaterial().getCanBurn()) {
            world.setBlockState(pos, AIR_STATE, 2);

            if (globalStage >= 3) {
                propagateFireEffect(world, pos, globalStage);
            }

            return true;
        }
        if (globalStage >= 1) {
            if (block instanceof BlockLeaves || block instanceof BlockDoublePlant ||
                    block instanceof BlockSnow || block instanceof BlockTallGrass ||
                    block instanceof BlockLilyPad) {
                world.setBlockState(pos, AIR_STATE, 2);
                return true;
            }
        }

        if (globalStage >= 2) {
            if (block instanceof BlockBush || block instanceof BlockVine ||
                    block instanceof BlockCocoa || block instanceof BlockCactus ||
                    block instanceof BlockSnowBlock || block instanceof BlockReed) {
                world.setBlockState(pos, AIR_STATE, 2);
                return true;
            }
        }

        applyCatchupTransformation(world, pos, block, state, globalStage);
        return false;
    }

    private static void propagateFireEffect(WorldServer world, BlockPos centerPos, int globalStage) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos adjacentPos = centerPos.offset(facing);
            IBlockState adjacentState = world.getBlockState(adjacentPos);

            if (adjacentState.getMaterial().getCanBurn() &&
                    adjacentState.getBlock() != Blocks.AIR) {

                world.setBlockState(adjacentPos, AIR_STATE, 2);
            }
        }
    }
}