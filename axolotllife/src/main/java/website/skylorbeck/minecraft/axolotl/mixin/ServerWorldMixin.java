package website.skylorbeck.minecraft.axolotl.mixin;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import website.skylorbeck.minecraft.axolotl.Axolotl;
import website.skylorbeck.minecraft.axolotl.PlayerEntityAccessor;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow @Final
    List<ServerPlayerEntity> players;

    @Shadow public abstract ServerScoreboard getScoreboard();

    @Shadow @Final private MinecraftServer server;

    @Inject(at = @At("HEAD"), method = "tick")
    public void injectedTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerScoreboard scoreboard = getScoreboard();
        String playername;
        for (PlayerEntity player:this.players) {
            playername = player.getEntityName();
            if (((PlayerEntityAccessor)player).getAxostage() == 0 ) {
                if (scoreboard.getPlayerScore(playername, scoreboard.getObjective("fish")).getScore() >= 3) {
                    ((PlayerEntityAccessor)player).setAxostage(1);
                    sendModelToAll(player.getUuid(),1);
                }
                if (!player.isWet()){
                    player.damage(DamageSource.DRYOUT, 1.0F);
                }
            }
            if (((PlayerEntityAccessor)player).getAxostage() == 1 && scoreboard.getPlayerScore(playername, scoreboard.getObjective("drowned")).getScore() >= 2) {
                ((PlayerEntityAccessor)player).setAxostage(2);
                ItemStack helm = Items.LEATHER_HELMET.getDefaultStack();
                helm.addEnchantment(Enchantments.RESPIRATION,5);
                player.equipStack(EquipmentSlot.HEAD, helm);
                sendModelToAll(player.getUuid(),2);
            }
            if (((PlayerEntityAccessor)player).getAxostage() == 2 && scoreboard.getPlayerScore(playername, scoreboard.getObjective("zombie")).getScore() >= 1
                    && scoreboard.getPlayerScore(playername, scoreboard.getObjective("spider")).getScore() >= 1
                    && scoreboard.getPlayerScore(playername, scoreboard.getObjective("skeleton")).getScore() >= 1 ) {
                ((PlayerEntityAccessor)player).setAxostage(3);
                sendModelToAll(player.getUuid(),3);
            }
            if (((PlayerEntityAccessor)player).getAxostage() == 3 && scoreboard.getPlayerScore(playername, scoreboard.getObjective("enderman")).getScore() >= 1
                    && scoreboard.getPlayerScore(playername, scoreboard.getObjective("piglin")).getScore() >= 1
                    && scoreboard.getPlayerScore(playername, scoreboard.getObjective("blaze")).getScore() >= 1
                    && scoreboard.getPlayerScore(playername, scoreboard.getObjective("witherskel")).getScore() >= 1 ) {
                ((PlayerEntityAccessor)player).setAxostage(4);
                sendModelToAll(player.getUuid(),4);
            }
            if (scoreboard.getPlayerScore(playername, scoreboard.getObjective("override")).getScore() >= 1) {
                int score = scoreboard.getPlayerScore(playername, scoreboard.getObjective("override")).getScore();
                if (score>=5) {
                    ((PlayerEntityAccessor) player).setAxostage(0);
                } else {
                    ((PlayerEntityAccessor) player).setAxostage(score);
                }
                sendModelToAll(player.getUuid(),score);
                scoreboard.getPlayerScore(playername, scoreboard.getObjective("override")).setScore(0);
            }
        }
    }

    private void sendModelToAll(UUID UUID, int model) {
        for (PlayerEntity player : this.players) {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeString(UUID.toString()).writeInt(model);
            ServerSidePacketRegistryImpl.INSTANCE.sendToPlayer(player,Axolotl.setmodel,packetByteBuf);

        }
    }

}
