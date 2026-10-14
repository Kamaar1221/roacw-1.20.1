package net.kamaarion.roacw.spells.fire;

import com.gametechbc.traveloptics.spells.TravelopticsSpellAnimations;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.dread.DreadPulse;
import net.kamaarion.roacw.registeries.ROACWAnimationRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class DreadSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "dread");

    private static final float MAX_RADIUS = 8.0F;
    private static final float HEALTH_THRESHOLD = 20.0F;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(25)
            .build();

    public DreadSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ROACWAnimationRegistry.TYRANT_ROAR;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(ROACWSoundRegistry.TYRANT_ROAR.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void playSound(Optional<SoundEvent> sound, Entity entity) {
        sound.ifPresent(soundEvent ->
                entity.playSound(soundEvent, 3.0F, 1.0F)
        );
    }

    @Override
    public void onCast(
            Level level,
            int spellLevel,
            LivingEntity entity,
            CastSource castSource,
            MagicData playerMagicData
    ) {
        if (!level.isClientSide) {
            DreadPulse pulse = new DreadPulse(
                    ROACWEntityRegistry.DREAD_PULSE.get(),
                    level
            );

            pulse.setOwner(entity);
            pulse.setRadius(getRadius(spellLevel));
            pulse.setHealthThreshold(getHealthThreshold(spellLevel));
            pulse.setDreadDuration(getDreadDuration(spellLevel));
            pulse.setDreadAmplifier(getDreadAmplifier(spellLevel));

            pulse.setPos(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ()
            );

            level.addFreshEntity(pulse);
        }

        super.onCast(
                level,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    @Override
    public List<MutableComponent> getUniqueInfo(
            int spellLevel,
            LivingEntity caster
    ) {
        return List.of(
                Component.literal(
                        "Radius: " + String.format(
                                "%.1f",
                                getRadius(spellLevel)
                        )
                ),
                Component.literal(
                        "Health Threshold: " + String.format(
                                "%.1f",
                                getHealthThreshold(spellLevel)
                        )
                )
        );
    }

    private float getRadius(int spellLevel) {
        return MAX_RADIUS + (spellLevel - 1) * 2.0F;
    }

    private float getHealthThreshold(int spellLevel) {
        return HEALTH_THRESHOLD + (spellLevel - 1) * 5.0F;
    }

    private int getDreadDuration(int spellLevel) {
        return 100 + (spellLevel - 1) * 20;
    }

    private int getDreadAmplifier(int spellLevel) {
        return Math.max(0, spellLevel - 1);
    }
}