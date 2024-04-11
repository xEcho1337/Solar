package net.echo.solar.common;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.util.Vector3i;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.echo.solar.player.SolarPlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Optional;

public class BreakUtils {

    public static float getBlockDamage(SolarPlayer player, Vector3i position) {
        WrappedBlockState block = player.getWorldTracker().getBlockAt(position);
        float hardness = block.getType().getHardness();

        if (hardness == -1) return 0;

        ItemStack itemInHand = SpigotConversionUtil.fromBukkitItemStack(player.getBukkitPlayer().getItemInHand());

        boolean canHarvest = canHarvest(itemInHand, block); // If he can harvest with the current tool
        float efficiency = getEfficiency(player, itemInHand); // Gets the efficiency

        // Vanilla formula to calculate the progression each tick
        return hardness < 0.0F ? 0.0F : efficiency / hardness / (!canHarvest ? 100.0F : 30.0F);
    }

    public static boolean canHarvest(ItemStack tool, WrappedBlockState block) {
        if (!block.getType().isRequiresCorrectTool()) {
            return true;
        } else {
            boolean canHarvest = false;

            // Why does this look so ugly?
            if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.AXE)) {
                canHarvest = BlockTags.MINEABLE_WITH_AXE.contains(block.getType());
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.PICKAXE)) {
                canHarvest = BlockTags.MINEABLE_WITH_PICKAXE.contains(block.getType());
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.SHOVEL)) {
                canHarvest = BlockTags.MINEABLE_WITH_SHOVEL.contains(block.getType());
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.HOE)) {
                canHarvest = BlockTags.MINEABLE_WITH_HOE.contains(block.getType());
            }

            return canHarvest;
        }
    }

    public static float getEfficiency(SolarPlayer player, ItemStack tool) {
        float speed = 1f;

        if (tool != null) {
            if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.WOOD_TIER)) { // Tier 0
                speed = 2.0f;
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.STONE_TIER)) { // Tier 1
                speed = 4.0f;
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.IRON_TIER)) { // Tier 2
                speed = 6.0f;
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.DIAMOND_TIER)) { // Tier 3
                speed = 8.0f;
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.GOLD_TIER)) { // Tier 0
                speed = 12.0f;
            } else if (tool.getType().hasAttribute(ItemTypes.ItemAttribute.NETHERITE_TIER)) { // Tier 4
                speed = 9.0f;
            }

            if (speed > 1) {
                int digSpeed = tool.getEnchantmentLevel(EnchantmentTypes.BLOCK_EFFICIENCY, PacketEvents.getAPI()
                        .getServerManager().getVersion().toClientVersion());

                if (digSpeed > 0) {
                    speed += speed * speed + 1;
                }
            }
        }

        Collection<PotionEffect> effects = player.getBukkitPlayer().getActivePotionEffects();

        Optional<PotionEffect> hasteEffect = effects.stream().filter(x -> x.getType().equals(PotionEffectType.FAST_DIGGING)).findFirst();
        Optional<PotionEffect> miningFatigue = effects.stream().filter(x -> x.getType().equals(PotionEffectType.SLOW_DIGGING)).findFirst();

        if (hasteEffect.isPresent()) {
            speed *= 1 + (hasteEffect.get().getAmplifier() + 1) * 0.2F;
        }

        if (miningFatigue.isPresent()) {
            switch (miningFatigue.get().getAmplifier()) {
                case 0:
                    speed *= 0.3f;
                    break;
                case 1:
                    speed *= 0.09f;
                    break;
                case 2:
                    speed *= 0.0027f;
                    break;
                default:
                    speed *= 8.1e-4f;
            }
        }

        if (!player.getPositionTracker().isOnGround()) {
            speed /= 5.0F;
        }

        return speed;
    }
}
