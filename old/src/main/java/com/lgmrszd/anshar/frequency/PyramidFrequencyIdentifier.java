package com.lgmrszd.anshar.frequency;


import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lgmrszd.anshar.Anshar.LOGGER;

public final class PyramidFrequencyIdentifier implements IFrequencyIdentifier {
    private final int level;
    private final List<List<Identifier>> pyramidArrangements;
    private final Identifier dimension;
    private final static Identifier owIdentifier = Identifier.of("minecraft", "overworld");

    private PyramidFrequencyIdentifier(List<List<Identifier>> pyramidArrangements, int level, Identifier dimension) {
        this.pyramidArrangements = pyramidArrangements;
        this.level = level;
        this.dimension = dimension;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isValidInDim(Identifier dim) {
        return dim.equals(dimension);
    }

    @Override
    public int hashCode() {
        int hash = Integer.MAX_VALUE;
        for (List<Identifier> pyramidArrangement: pyramidArrangements) {
            hash = Math.min(pyramidArrangement.hashCode(), hash);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PyramidFrequencyIdentifier other)) return false;
        if (!dimension.equals(other.dimension)) return false;
        List<Identifier> otherArrangement = other.pyramidArrangements.get(0);
        for (List<Identifier> pyramidArrangement: pyramidArrangements) {
            if (pyramidArrangement.equals(otherArrangement)) return true;
        }
        return false;
    }

    public void toNbt(NbtCompound tag) {
        List<Identifier> flattened = pyramidArrangements.get(0);
        String blockList = flattened.stream()
                .map(Identifier::toString)
                .collect(StringBuilder::new, (builder, str) -> builder.append(str).append(","), StringBuilder::append)
                .toString();
        tag.putInt("level", level);
        tag.putString("dim", dimension.toString());
        tag.putString("blocks", blockList);
    }

    public static PyramidFrequencyIdentifier fromNbt(NbtCompound tag) {
        int level = tag.getInt("level");
        Identifier dimension = tag.contains("dim") ?
                Identifier.tryParse(tag.getString("dim")) :
                owIdentifier;
        String blockList = tag.getString("blocks");
        List<Identifier> flattened = Arrays.stream(blockList.split(","))
                .map(Identifier::tryParse)
                .toList();
        return fromFlattened(flattened, level, dimension);
    }

    private static List<Identifier> flatten(List<List<Identifier>> topBlocks, List<List<Identifier>> bottomBlocks) {
//        int size = level == 1 ? 9 :
//                (level * 2 + 1) * (level * 2 + 1) + (level * 2 + 3) * (level * 2 + 3);
        List<Identifier> flattened = new ArrayList<>(topBlocks.size() + bottomBlocks.size());
        bottomBlocks.forEach(flattened::addAll);
        topBlocks.forEach(flattened::addAll);
        return flattened;
    }

    private static PyramidFrequencyIdentifier fromFlattened(List<Identifier> flattened, int level, Identifier dimension) {
        int size = flattened.size();
        if (size < 9){
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }
        if (size == 9 && level != 1) {
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }
        if (size > 9 && (8*level*level + 2) != size) {
            LOGGER.error(String.format("Unflattening error: wrong size %d for level %d", size, level));
            return null;
        }

        int bottomEdge = level * 2 + 1;
        int bottomEdgeMinus1 = bottomEdge - 1;
        List<List<List<Identifier>>> bottomBlocks = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            List<List<Identifier>> bottomBlocks2d = new ArrayList<>(bottomEdge);
            for (int j = 0; j < bottomEdge; j++) {
                bottomBlocks2d.add(new ArrayList<>(Collections.nCopies(bottomEdge, null)));
            }
            bottomBlocks.add(bottomBlocks2d);
        }

        int bottomBlocksSize = bottomEdge * bottomEdge;
        for (int i = 0; i < bottomBlocksSize; i++) {
            Identifier blockId = flattened.get(i);
            // We store rows
            // X is row number
            // Z is column number in the row
            int x = i / bottomEdge;
            int z = i % bottomEdge;
            bottomBlocks.get(0).get(x).set(z, blockId);
            bottomBlocks.get(1).get(z).set(bottomEdgeMinus1-x, blockId);
            bottomBlocks.get(2).get(bottomEdgeMinus1-x).set(bottomEdgeMinus1-z, blockId);
            bottomBlocks.get(3).get(bottomEdgeMinus1-z).set(x, blockId);
        }

        List<List<List<Identifier>>> topBlocks = new ArrayList<>(4);
        if (level == 1) {
            for (int i = 0; i < 4; i++) {
                topBlocks.add(Collections.emptyList());
            }
        } else {
            int topEdge = bottomEdge - 2;
            int topEdgeMinus1 = topEdge - 1;
            int topBlocksSize = topEdge * topEdge;

            for (int i = 0; i < 4; i++) {
                List<List<Identifier>> topBlocks2d = new ArrayList<>(topEdge);
                for (int j = 0; j < topEdge; j++) {
                    topBlocks2d.add(new ArrayList<>(Collections.nCopies(topEdge, null)));
                }
                topBlocks.add(topBlocks2d);
            }

            for (int i = 0; i < topBlocksSize; i++) {
                Identifier blockId = flattened.get(i + bottomBlocksSize);
                int x = i / topEdge;
                int z = i % topEdge;
                topBlocks.get(0).get(x).set(z, blockId);
                topBlocks.get(1).get(z).set(topEdgeMinus1-x, blockId);
                topBlocks.get(2).get(topEdgeMinus1-x).set(topEdgeMinus1-z, blockId);
                topBlocks.get(3).get(topEdgeMinus1-z).set(x, blockId);
            }
        }

        List<List<Identifier>> pyramidArrangements = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            pyramidArrangements.add(flatten(topBlocks.get(i), bottomBlocks.get(i)));
        }

        return new PyramidFrequencyIdentifier(pyramidArrangements, level, dimension);
    }

    public static PyramidFrequencyIdentifier scanForPyramid(World world, BlockPos pos, int level) {
        if (level < 1) {
            LOGGER.error("Tried to create PyramidFrequencyIdentifier with level {}", level);
            return null;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int bottomEdge = level * 2 + 1;

        List<List<List<Identifier>>> bottomBlocks = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            List<List<Identifier>> bottomBlocks2d = new ArrayList<>(bottomEdge);
            for (int j = 0; j < bottomEdge; j++) {
                bottomBlocks2d.add(new ArrayList<>(Collections.nCopies(bottomEdge, null)));
            }
            bottomBlocks.add(bottomBlocks2d);
        }

        int level_y_abs = y - level;
        int bottomEdgeMinus1 = bottomEdge - 1;
        for (int layer_x = 0; layer_x < bottomEdge; layer_x++)
            for (int layer_z = 0; layer_z < bottomEdge; layer_z++) {
                int level_x_abs = layer_x + x - level;
                int level_z_abs = layer_z + z - level;
                Identifier blockId = Registries.BLOCK.getId(
                        world.getBlockState(
                                new BlockPos(
                                        level_x_abs,
                                        level_y_abs,
                                        level_z_abs
                                )
                        ).getBlock()
                );
                bottomBlocks.get(0).get(layer_x).set(layer_z, blockId);
                bottomBlocks.get(1).get(layer_z).set(bottomEdgeMinus1-layer_x, blockId);
                bottomBlocks.get(2).get(bottomEdgeMinus1-layer_x).set(bottomEdgeMinus1-layer_z, blockId);
                bottomBlocks.get(3).get(bottomEdgeMinus1-layer_z).set(layer_x, blockId);
            }

//        Identifier[][][] topBlocks;
        List<List<List<Identifier>>> topBlocks = new ArrayList<>(4);
        if (level > 1) {
            int up_level = level - 1;
            int topEdge = level * 2 - 1;

            for (int i = 0; i < 4; i++) {
                List<List<Identifier>> topBlocks2d = new ArrayList<>(topEdge);
                for (int j = 0; j < topEdge; j++) {
                    topBlocks2d.add(new ArrayList<>(Collections.nCopies(topEdge, null)));
                }
                topBlocks.add(topBlocks2d);
            }

            int topEdgeMinus1 = topEdge - 1;

            for (int layer_x = 0; layer_x < topEdge; layer_x++)
                for (int layer_z = 0; layer_z < topEdge; layer_z++) {
                    level_y_abs = y - up_level + Math.min(
                            up_level - Math.abs(layer_x - up_level),
                            up_level - Math.abs(layer_z - up_level)
                    );
                    if (level_y_abs == y) level_y_abs = y - 1;
                    int level_x_abs = layer_x + x - up_level;
                    int level_z_abs = layer_z + z - up_level;
                    Identifier blockId = Registries.BLOCK.getId(
                            world.getBlockState(
                                    new BlockPos(
                                            level_x_abs,
                                            level_y_abs,
                                            level_z_abs
                                    )
                            ).getBlock()
                    );
                    topBlocks.get(0).get(layer_x).set(layer_z, blockId);
                    topBlocks.get(1).get(layer_z).set(topEdgeMinus1-layer_x, blockId);
                    topBlocks.get(2).get(topEdgeMinus1-layer_x).set(topEdgeMinus1-layer_z, blockId);
                    topBlocks.get(3).get(topEdgeMinus1-layer_z).set(layer_x, blockId);
                }

        } else {
            for (int i = 0; i < 4; i++) {
                topBlocks.add(Collections.emptyList());
            }
        }

        List<List<Identifier>> pyramidArrangements = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            pyramidArrangements.add(flatten(topBlocks.get(i), bottomBlocks.get(i)));
        }

        Identifier dimension = world.getRegistryKey().getValue();

        return new PyramidFrequencyIdentifier(pyramidArrangements, level, dimension);
    }
}
