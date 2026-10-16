package net.kamaarion.roacw.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.kamaarion.roacw.entity.spells.pestilence_cloak.PestilenceCloakCloudEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class PestilenceCloakSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(
            "roacw",
            "pestilence_cloak"
    );

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(45.0)
            .setAllowCrafting(false)
            .build();

    private static final float BASE_RADIUS = 5.0F;
    private static final float RADIUS_PER_LEVEL = 2.5F;

    private static final int BASE_LIFETIME_TICKS = 200;
    private static final int LIFETIME_PER_LEVEL = 100;

    private static final float BASE_DAMAGE = 1.0F;
    private static final float DAMAGE_PER_LEVEL = 1.0F;

    public PestilenceCloakSpell() {
        this.baseManaCost = 100;
        this.manaCostPerLevel = 25;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float radius = getRadiusForLevel(spellLevel);
        float durationSeconds = getLifetimeTicksForLevel(spellLevel) / 20.0F;
        float damage = getDamageForLevel(spellLevel);

        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(damage, 1)
                ),
                Component.translatable(
                        "ui.roacw.plague_cloud_radius",
                        Utils.stringTruncation(radius, 1)
                ),
                Component.translatable(
                        "ui.roacw.plague_cloud_duration",
                        Utils.stringTruncation(durationSeconds, 1)
                )
        );
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void onCast(
            Level level,
            int spellLevel,
            LivingEntity entity,
            CastSource castSource,
            MagicData playerMagicData
    ) {
        if (level.isClientSide || entity == null) {
            return;
        }

        PestilenceCloakCloudEntity cloud = new PestilenceCloakCloudEntity(
                ROACWEntityRegistry.PESTILENCE_CLOAK_CLOUD.get(),
                level
        );

        cloud.setPos(
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );

        cloud.setOwner(entity);
        cloud.setRadius(getRadiusForLevel(spellLevel));
        cloud.setLifetime(getLifetimeTicksForLevel(spellLevel));
        cloud.setDamage(getDamageForLevel(spellLevel));

        level.addFreshEntity(cloud);

        super.onCast(
                level,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    private float getRadiusForLevel(int spellLevel) {
        return BASE_RADIUS + RADIUS_PER_LEVEL * (spellLevel - 1);
    }

    private int getLifetimeTicksForLevel(int spellLevel) {
        return BASE_LIFETIME_TICKS + LIFETIME_PER_LEVEL * (spellLevel - 1);
    }

    private float getDamageForLevel(int spellLevel) {
        return BASE_DAMAGE + DAMAGE_PER_LEVEL * (spellLevel - 1);
    }
}