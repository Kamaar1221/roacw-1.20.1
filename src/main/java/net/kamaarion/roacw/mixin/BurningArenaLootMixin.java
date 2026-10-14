package net.kamaarion.roacw.mixin;

import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(TemplateStructurePiece.class)
public abstract class BurningArenaLootMixin {

    @Shadow
    @Final
    protected String templateName;

    @Shadow
    protected BlockPos templatePosition;

    @Shadow
    protected StructureTemplate template;

    @Shadow
    protected StructurePlaceSettings placeSettings;

    @Unique
    private static final ResourceLocation ROACW$BURNING_ARENA =
            ResourceLocation.tryBuild("cataclysm", "burning_arena2");

    @Unique
    private static final BlockPos ROACW$CHEST_TEMPLATE_POS =
            new BlockPos(42, 41, 4);

    @Inject(method = "postProcess", at = @At("RETURN"))
    private void roacw$addYharonLoot(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox boundingBox,
            ChunkPos chunkPos,
            BlockPos referencePos,
            CallbackInfo ci
    ) {
        if (!ROACW$BURNING_ARENA.toString().equals(templateName)) {
            return;
        }

        BlockPos relativePos = StructureTemplate.calculateRelativePosition(
                placeSettings,
                ROACW$CHEST_TEMPLATE_POS
        );

        BlockPos chestPos = templatePosition.offset(relativePos);

        if (!boundingBox.isInside(chestPos)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(chestPos);

        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }

        Item bone = ROACWItemRegistry.YHARON_BONE.get();
        Item feather = ROACWItemRegistry.YHARON_FEATHER.get();

        if (!roacw$containsItem(chest, bone)) {
            int totalBones = random.nextInt(5) + 4; // 4–8 bones
            roacw$insertSplitStacks(chest, bone, totalBones, random);
        }

        if (!roacw$containsItem(chest, feather)) {
            int totalFeathers = random.nextInt(5) + 4; // 4–8 feathers
            roacw$insertSplitStacks(chest, feather, totalFeathers, random);
        }

        chest.setChanged();
    }

    @Unique
    private static boolean roacw$containsItem(
            ChestBlockEntity chest,
            Item item
    ) {
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            if (chest.getItem(slot).is(item)) {
                return true;
            }
        }

        return false;
    }

    @Unique
    private static boolean roacw$insertSplitStacks(
            ChestBlockEntity chest,
            Item item,
            int total,
            RandomSource random
    ) {
        int remaining = total;
        int maxStackSize = item.getDefaultInstance().getMaxStackSize();

        while (remaining > 0) {
            List<Integer> emptySlots = new ArrayList<>();

            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                if (chest.getItem(slot).isEmpty()) {
                    emptySlots.add(slot);
                }
            }

            if (emptySlots.isEmpty()) {
                return false;
            }

            int slot = emptySlots.get(random.nextInt(emptySlots.size()));

            int stackSize = Math.min(
                    remaining,
                    Math.min(maxStackSize, random.nextInt(3) + 1)
            );

            chest.setItem(slot, new ItemStack(item, stackSize));
            remaining -= stackSize;
        }

        return true;
    }
}