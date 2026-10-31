package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Items;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.utility.player.InventoryUtility;

public class EClipCommand extends Command {
   public EClipCommand() {
      super("eclip");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(((LiteralArgumentBuilder)literal("bedrock").executes(context -> {
         this.execute(-((float)mc.player.getY()) - 3.0F);
         return 1;
      })).then(arg("number", FloatArgumentType.floatArg()).executes(context -> {
         float y = -((float)mc.player.getY()) - 3.0F;
         if (y == 0.0F) {
            y = (Float)context.getArgument("number", Float.class);
         }

         this.execute(y);
         return 1;
      })));
      builder.then(
         ((LiteralArgumentBuilder)literal("down")
               .executes(
                  context -> {
                     float y = 0.0F;

                     for (int i = 1; i < 255; i++) {
                        if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0))
                           == Blocks.AIR.getDefaultState()) {
                           y = (float)(-i - 1);
                           break;
                        }

                        if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0))
                           == Blocks.BEDROCK.getDefaultState()) {
                           sendMessage(
                              Formatting.RED
                                 + " \u043c\u043e\u0436\u043d\u043e \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e\u0434 \u0431\u0435\u0434\u0440\u043e\u043a"
                           );
                           sendMessage(Formatting.RED + " eclip bedrock");
                           return 1;
                        }
                     }

                     this.execute(y);
                     return 1;
                  }
               ))
            .then(
               arg("number", FloatArgumentType.floatArg())
                  .executes(
                     context -> {
                        float y = 0.0F;

                        for (int i = 1; i < 255; i++) {
                           if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0))
                              == Blocks.AIR.getDefaultState()) {
                              y = (float)(-i - 1);
                              break;
                           }

                           if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, -i, 0))
                              == Blocks.BEDROCK.getDefaultState()) {
                              sendMessage(
                                 Formatting.RED
                                    + " \u043c\u043e\u0436\u043d\u043e \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e\u0434 \u0431\u0435\u0434\u0440\u043e\u043a"
                              );
                              sendMessage(Formatting.RED + " eclip bedrock");
                              return 1;
                           }
                        }

                        if (y == 0.0F) {
                           y = (Float)context.getArgument("number", Float.class);
                        }

                        this.execute(y);
                        return 1;
                     }
                  )
            )
      );
      builder.then(
         ((LiteralArgumentBuilder)literal("up")
               .executes(
                  context -> {
                     float y = 0.0F;

                     for (int i = 4; i < 255; i++) {
                        if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, i, 0))
                           == Blocks.AIR.getDefaultState()) {
                           y = (float)(i + 1);
                           break;
                        }
                     }

                     this.execute(y);
                     return 1;
                  }
               ))
            .then(
               arg("number", FloatArgumentType.floatArg())
                  .executes(
                     context -> {
                        float y = 0.0F;

                        for (int i = 4; i < 255; i++) {
                           if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, i, 0))
                              == Blocks.AIR.getDefaultState()) {
                              y = (float)(i + 1);
                              break;
                           }
                        }

                        if (y == 0.0F) {
                           y = (Float)context.getArgument("number", Float.class);
                        }

                        this.execute(y);
                        return 1;
                     }
                  )
            )
      );
   }

   private void execute(float y) {
      int elytra;
      if ((elytra = InventoryUtility.findItemInInventory(Items.ELYTRA).slot()) == -1) {
         sendMessage(
            Formatting.RED
               + "\u0432\u0430\u043c \u043d\u0443\u0436\u043d\u044b \u044d\u043b\u0438\u0442\u0440\u044b \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (elytra != -2) {
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
         }

         mc.player.networkHandler.sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         mc.player.networkHandler.sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         mc.player
            .networkHandler
            .sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY() + (double)y, mc.player.getZ(), false));
         mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         if (elytra != -2) {
            mc.interactionManager.clickSlot(0, 6, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, elytra, 1, SlotActionType.PICKUP, mc.player);
         }

         mc.player.setPosition(mc.player.getX(), mc.player.getY() + (double)y, mc.player.getZ());
      }
   }
}
