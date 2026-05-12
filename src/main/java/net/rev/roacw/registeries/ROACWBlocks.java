package net.rev.roacw.registeries;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rev.roacw.ROACW;

import java.util.function.Supplier;

public class ROACWBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ROACW.MODID);

    public static final RegistryObject<Block> RAW_AURIC_BLOCK = registerBlock("raw_auric_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS).sound(SoundType.ANCIENT_DEBRIS)));
    public static final RegistryObject<Block> AURIC_ORE = registerBlock("auric_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS).sound(SoundType.ANCIENT_DEBRIS)
                    .strength(2f).requiresCorrectToolForDrops(), UniformInt.of(3, 6)));
    public static final RegistryObject<Block> DEEPSLATE_AURIC_ORE = registerBlock("deepslate_auric_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.ANCIENT_DEBRIS).sound(SoundType.ANCIENT_DEBRIS)
                    .strength(3f).requiresCorrectToolForDrops(), UniformInt.of(3, 6)));
    public static final RegistryObject<Block> AURIC_BLOCK = registerBlock("auric_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).sound(SoundType.NETHERITE_BLOCK)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ROACWItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
