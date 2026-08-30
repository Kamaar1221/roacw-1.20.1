package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.kamaarion.roacw.ROACW;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ROACWAttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ROACW.MODID);

    // Exo Magic
    public static final RegistryObject<Attribute> EXO_MAGIC_RESIST = registerResistanceAttribute("exo");
    public static final RegistryObject<Attribute> EXO_MAGIC_POWER = registerPowerAttribute("exo");


    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event)
    {

        event.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> event.add(entity, attribute.get())));
    }

    // ;_;
    private static RegistryObject<Attribute> registerResistanceAttribute(String id)
    {
        return ATTRIBUTES.register(id + "_magic_resist", () ->
                (new MagicRangedAttribute("attribute.roacw." + id + "_magic_resist",
                        1.0D, 0, 10).setSyncable(true)));
    }

    private static RegistryObject<Attribute> registerPowerAttribute(String id)
    {
        return ATTRIBUTES.register(id + "_spell_power", () ->
                (new MagicRangedAttribute("attribute.roacw." + id + "_spell_power",
                        1.0D, 0, 10).setSyncable(true)));
    }

    public static void register(IEventBus eventBus)
    {
        ATTRIBUTES.register(eventBus);
    }
}
