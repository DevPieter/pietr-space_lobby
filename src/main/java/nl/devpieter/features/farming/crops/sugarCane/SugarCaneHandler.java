package nl.devpieter.features.farming.crops.sugarCane;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import nl.devpieter.features.farming.FarmBlockHandler;
import nl.devpieter.models.BlockHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SugarCaneHandler extends FarmBlockHandler {

    public SugarCaneHandler() {
        super(4_000, 6_000, 4);
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("pieter_space", "sugar_cane_crop");
    }

    @Override
    public int getAge(@NotNull BlockHolder holder) {
        return getSugarCaneHeight(holder.instance(), holder.position().asPos());
    }

    @Override
    protected void grow(@NotNull BlockHolder holder, int currentAge, int newAge) {
        Instance instance = holder.instance();
        Pos position = holder.position().asPos();

        System.out.println("Growing sugar cane from age " + currentAge + " to " + newAge);

        if (newAge > currentAge) {
            Pos newBlockPos = position.add(0, currentAge + 1, 0);
            instance.setBlock(newBlockPos, Block.SUGAR_CANE);
        }
    }

    @Override
    public void replant(@NotNull BlockHolder holder, @Nullable Entity replanter) {
        Instance instance = holder.instance();
        Pos position = holder.position().asPos();

        System.out.println("Replanting sugar cane at " + position);

        int height = getSugarCaneHeight(instance, position);
        for (int y = 1; y < height; y++) {
            Pos removePos = position.add(0, y, 0);
            instance.setBlock(removePos, Block.AIR);
        }
    }

    private int getSugarCaneHeight(@NotNull Instance instance, @NotNull Pos position) {
        int height = 0;
        Pos checkPos = position;

        while (true) {
            checkPos = checkPos.add(0, 1, 0);
            Block aboveBlock = instance.getBlock(checkPos);

            if (!(aboveBlock.compare(Block.SUGAR_CANE))) {
                break;
            }

            height++;
        }

        return height;
    }
}
