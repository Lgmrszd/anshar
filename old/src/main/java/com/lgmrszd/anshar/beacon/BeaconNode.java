package com.lgmrszd.anshar.beacon;

import java.util.Optional;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Immutable Beacon information storage
 */
public class BeaconNode {
    private final Text name;
    private final float[] color;
    private final BlockPos pos;

    public BeaconNode(IBeaconComponent beaconComponent) {
        this.pos = beaconComponent.getBeaconPos();
        this.name = beaconComponent.getName();
        float[] color = beaconComponent.topColor();
        if (color != null && color.length == 3){
            this.color = color;
        } else {
            this.color = new float[]{0, 0, 0};
        }
        
    }

    private BeaconNode(BlockPos pos, Text name, float[] color) {
        this.pos = pos;
        this.name = name;
        this.color = color;
    }

    public static BeaconNode fromNBT(NbtCompound tag) {
        return new BeaconNode(
            BlockPos.fromLong(tag.getLong("pos")),
            Text.Serialization.fromJson(tag.getString("name")),
            new float[]{tag.getFloat("r"), tag.getFloat("g"), tag.getFloat("b")}
        );
    }

    public NbtCompound toNBT() {
        var tag = new NbtCompound();
        tag.putLong("pos", pos.asLong());
        tag.putString("name", Text.Serialization.toJsonString(this.name));
        tag.putFloat("r", color[0]);
        tag.putFloat("g", color[1]);
        tag.putFloat("b", color[2]);
        return tag;
    }

    public static BeaconNode makeFake(BlockPos pos) {
        return new BeaconNode(pos, Text.literal("?????"), new float[]{1, 1, 1});
    }

    public Text getName() {return name;}
    public float[] getColor() {return color;}
    public BlockPos getPos() {return pos;}
    public Optional<BeaconBlockEntity> getBeacon() {return Optional.empty();}
    public int getColorHex() {
        int rgb = (int)(getColor()[0] * 255);
        rgb = (rgb<<8) + (int)(getColor()[1] * 255);
        rgb = (rgb<<8) + (int)(getColor()[2] * 255);
        return rgb;
    }
}
