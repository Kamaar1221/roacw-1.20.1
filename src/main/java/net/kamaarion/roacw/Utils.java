package net.kamaarion.roacw;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public class Utils {
    public static boolean hasCurio(Player player, Item item)
    {
        return CuriosApi.getCuriosHelper().findEquippedCurio(item, player).isPresent();
    }

    public static int getNatureScaledPlagueAmplifier(LivingEntity caster) {
        var attr = caster.getAttribute(AttributeRegistry.NATURE_SPELL_POWER.get());
        float multiplier = attr != null ? (float) attr.getValue() : 1.0F;
        float bonusPercent = Math.max(0F, (multiplier - 1.0F) * 100F);
        return Math.min(5, Math.round(bonusPercent / 20F));
    }

    /**
     * Multiplier for rocket/nuke direct-hit and splash damage, sourced from
     * GTBC Spell Lib's magic_projectile_damage attribute. Base value is 1.0
     * (neutral), so a player with no relevant gear/buffs gets no change.
     * Falls back to 1.0 if the attribute instance is unexpectedly absent.
     */
    public static float getMagicProjectileDamageMultiplier(LivingEntity caster) {
        var attr = caster.getAttribute(GSLAttributeRegistry.MAGIC_PROJECTILE_DAMAGE.get());
        return attr != null ? (float) attr.getValue() : 1.0F;
    }
}