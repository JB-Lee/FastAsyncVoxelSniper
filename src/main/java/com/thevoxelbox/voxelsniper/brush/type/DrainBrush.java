package com.thevoxelbox.voxelsniper.brush.type;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.thevoxelbox.voxelsniper.sniper.snipe.Snipe;
import com.thevoxelbox.voxelsniper.sniper.snipe.message.SnipeMessenger;
import com.thevoxelbox.voxelsniper.sniper.toolkit.ToolkitProperties;
import com.thevoxelbox.voxelsniper.util.material.Materials;
import com.thevoxelbox.voxelsniper.util.math.MathHelper;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Stream;

public class DrainBrush extends AbstractBrush {

    private double trueCircle;
    private boolean disc;

    @Override
    public void handleCommand(String[] parameters, Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        String firstParameter = parameters[0];

        if (firstParameter.equalsIgnoreCase("info")) {
            messenger.sendMessage(ChatColor.GOLD + "Drain Brush Parameters:");
            messenger.sendMessage(ChatColor.AQUA + "/b drain [true|false] -- Uses a true sphere algorithm instead of the " +
                    "skinnier version with classic sniper nubs. Default is false.");
            messenger.sendMessage(ChatColor.AQUA + "/b drain d -- Toggles disc drain mode, as opposed to a ball drain mode.");
        } else {
            if (parameters.length == 1) {
                if (firstParameter.equalsIgnoreCase("true")) {
                    this.trueCircle = 0.5;
                    messenger.sendMessage(ChatColor.AQUA + "True circle mode ON.");
                } else if (firstParameter.equalsIgnoreCase("false")) {
                    this.trueCircle = 0;
                    messenger.sendMessage(ChatColor.AQUA + "True circle mode OFF.");
                } else if (firstParameter.equalsIgnoreCase("d")) {
                    if (this.disc) {
                        this.disc = false;
                        messenger.sendMessage(ChatColor.AQUA + "Disc drain mode OFF");
                    } else {
                        this.disc = true;
                        messenger.sendMessage(ChatColor.AQUA + "Disc drain mode ON");
                    }
                } else {
                    messenger.sendMessage(ChatColor.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
                }
            } else {
                messenger.sendMessage(ChatColor.RED + "Invalid brush parameters length! Use the \"info\" parameter to display parameter " +
                        "info.");
            }
        }
    }

    @Override
    public List<String> handleCompletions(String[] parameters, Snipe snipe) {
        if (parameters.length == 1) {
            String parameter = parameters[0];
            return super.sortCompletions(Stream.of("true", "false", "d"), parameter, 0);
        }
        return super.handleCompletions(parameters, snipe);
    }

    @Override
    public void handleArrowAction(Snipe snipe) {
        drain(snipe);
    }

    @Override
    public void handleGunpowderAction(Snipe snipe) {
        drain(snipe);
    }

    private void drain(Snipe snipe) {
        ToolkitProperties toolkitProperties = snipe.getToolkitProperties();
        int brushSize = toolkitProperties.getBrushSize();
        double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
        BlockVector3 targetBlock = getTargetBlock();
        int targetBlockX = targetBlock.getX();
        int targetBlockY = targetBlock.getY();
        int targetBlockZ = targetBlock.getZ();
        if (this.disc) {
            for (int x = brushSize; x >= 0; x--) {
                double xSquared = MathHelper.square(x);
                for (int y = brushSize; y >= 0; y--) {
                    double ySquared = MathHelper.square(y);
                    if (xSquared + ySquared <= brushSizeSquared) {
                        BlockType typePlusPlus = getBlockType(targetBlock.add(x, 0, y));
                        if (Materials.isLiquid(typePlusPlus)) {
                            setBlockType(targetBlock.add(x, 0, y), BlockTypes.AIR);
                        }
                        BlockType typePlusMinus = getBlockType(targetBlockX + x, targetBlockY, targetBlockZ - y);
                        if (Materials.isLiquid(typePlusMinus)) {
                            setBlockType(targetBlockX + x, targetBlockY, targetBlockZ - y, BlockTypes.AIR);
                        }
                        BlockType typeMinusPlus = getBlockType(targetBlockX - x, targetBlockY, targetBlockZ + y);
                        if (Materials.isLiquid(typeMinusPlus)) {
                            setBlockType(targetBlockX - x, targetBlockY, targetBlockZ + y, BlockTypes.AIR);
                        }
                        BlockType typeMinusMinus = getBlockType(targetBlockX - x, targetBlockY, targetBlockZ - y);
                        if (Materials.isLiquid(typeMinusMinus)) {
                            setBlockType(targetBlockX - x, targetBlockY, targetBlockZ - y, BlockTypes.AIR);
                        }
                    }
                }
            }
        } else {
            for (int y = (brushSize + 1) * 2; y >= 0; y--) {
                double ySquared = MathHelper.square(y - brushSize);
                for (int x = (brushSize + 1) * 2; x >= 0; x--) {
                    double xSquared = MathHelper.square(x - brushSize);
                    for (int z = (brushSize + 1) * 2; z >= 0; z--) {
                        if ((xSquared + MathHelper.square(z - brushSize) + ySquared) <= brushSizeSquared) {
                            BlockType type = getBlockType(
                                    targetBlockX + x - brushSize,
                                    targetBlockY + z - brushSize,
                                    targetBlockZ + y - brushSize
                            );
                            if (Materials.isLiquid(type)) {
                                setBlockType(
                                        targetBlockX + x - brushSize,
                                        targetBlockY + z - brushSize,
                                        targetBlockZ + y - brushSize,
                                        BlockTypes.AIR
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendInfo(Snipe snipe) {
        SnipeMessenger messenger = snipe.createMessenger();
        messenger.sendBrushNameMessage();
        messenger.sendBrushSizeMessage();
        messenger.sendMessage(ChatColor.AQUA + (Double.compare(this.trueCircle, 0.5) == 0
                ? "True circle mode ON"
                : "True circle mode OFF"));
        messenger.sendMessage(ChatColor.AQUA + (this.disc ? "Disc drain mode ON" : "Disc drain mode OFF"));
    }

}
