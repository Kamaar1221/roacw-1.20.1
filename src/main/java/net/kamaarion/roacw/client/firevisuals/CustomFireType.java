package net.kamaarion.roacw.client.firevisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class CustomFireType {
    private final ResourceLocation id;
    private final Predicate<LivingEntity> condition;
    private final Material frontMaterial;
    private final Material backMaterial;
    private final float red;
    private final float green;
    private final float blue;
    private final float overlayAlpha;
    private final float worldScale;
    private final boolean showWithVanillaFire;

    private CustomFireType(Builder builder) {
        this.id = builder.id;
        this.condition = Objects.requireNonNull(
                builder.condition,
                "Fire type '" + builder.id + "' needs an effect() or condition()"
        );

        ResourceLocation front = Objects.requireNonNull(
                builder.frontTexture,
                "Fire type '" + builder.id + "' needs a texture()"
        );

        ResourceLocation back = builder.backTexture != null ? builder.backTexture : front;

        this.frontMaterial = new Material(InventoryMenu.BLOCK_ATLAS, front);
        this.backMaterial = new Material(InventoryMenu.BLOCK_ATLAS, back);
        this.red = builder.red;
        this.green = builder.green;
        this.blue = builder.blue;
        this.overlayAlpha = builder.overlayAlpha;
        this.worldScale = builder.worldScale;
        this.showWithVanillaFire = builder.showWithVanillaFire;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation id() {
        return id;
    }

    public boolean appliesTo(LivingEntity entity) {
        return condition.test(entity);
    }

    public TextureAtlasSprite frontSprite() {
        return frontMaterial.sprite();
    }

    public TextureAtlasSprite backSprite() {
        return backMaterial.sprite();
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public float overlayAlpha() {
        return overlayAlpha;
    }

    public float worldScale() {
        return worldScale;
    }

    public boolean showWithVanillaFire() {
        return showWithVanillaFire;
    }

    static boolean hasEffectSynced(LivingEntity entity, MobEffect effect) {
        if (entity == Minecraft.getInstance().player) {
            return entity.hasEffect(effect);
        }

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        return effectId != null && ClientFireEffectCache.has(entity.getId(), effectId);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Predicate<LivingEntity> condition;
        private ResourceLocation frontTexture;
        private ResourceLocation backTexture;
        private float red = 1.0F;
        private float green = 1.0F;
        private float blue = 1.0F;
        private float overlayAlpha = 0.9F;
        private float worldScale = 1.4F;
        private boolean showWithVanillaFire = false;

        private Builder(ResourceLocation id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder effect(Supplier<? extends MobEffect> effect) {
            Objects.requireNonNull(effect, "effect");
            this.condition = entity -> hasEffectSynced(entity, effect.get());
            return this;
        }

        public Builder condition(Predicate<LivingEntity> condition) {
            this.condition = Objects.requireNonNull(condition, "condition");
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.frontTexture = Objects.requireNonNull(texture, "texture");
            return this;
        }

        public Builder backTexture(ResourceLocation texture) {
            this.backTexture = Objects.requireNonNull(texture, "texture");
            return this;
        }

        public Builder tint(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            return this;
        }

        public Builder overlayAlpha(float alpha) {
            this.overlayAlpha = alpha;
            return this;
        }

        public Builder worldScale(float scale) {
            this.worldScale = scale;
            return this;
        }

        public Builder showWithVanillaFire(boolean show) {
            this.showWithVanillaFire = show;
            return this;
        }

        public CustomFireType build() {
            return new CustomFireType(this);
        }
    }
}