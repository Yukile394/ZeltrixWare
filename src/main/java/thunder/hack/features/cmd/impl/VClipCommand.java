package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("vclip");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         literal("down")
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
                              + (
                                 ClientSettings.isRu()
                                    ? "\u041d\u0435\u043a\u0443\u0434\u0430 \u043a\u043b\u0438\u043f\u0430\u0442\u044c!"
                                    : "There's nowhere to clip!"
                              )
                        );
                        return 1;
                     }
                  }

                  this.clip((double)y);
                  return 1;
               }
            )
      );
      builder.then(
         literal("up")
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

                  this.clip((double)y);
                  return 1;
               }
            )
      );
      builder.then(
         arg("count", DoubleArgumentType.doubleArg())
            .executes(
               context -> {
                  double count = (Double)context.getArgument("count", Double.class);

                  try {
                     sendMessage(
                        Formatting.GREEN
                           + "\u041a\u043b\u0438\u043f\u0430\u0435\u043c\u0441\u044f \u043d\u0430 "
                           + count
                           + " \u0431\u043b\u043e\u043a\u043e\u0432"
                     );
                     this.clip(count);
                  } catch (Exception var5) {
                  }

                  return 1;
               }
            )
      );
      builder.executes(context -> {
         sendMessage("\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439 .vclip <\u0447\u0438\u0441\u043b\u043e>");
         return 1;
      });
   }

   private void clip(double b) {
      if (ClientSettings.clipCommandMode.getValue() == ClientSettings.ClipCommandMode.Matrix) {
         for (int i = 0; i < 10; i++) {
            mc.player
               .networkHandler
               .sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         }

         for (int i = 0; i < 10; i++) {
            mc.player
               .networkHandler
               .sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY() + b, mc.player.getZ(), false));
         }
      } else {
         mc.player
            .networkHandler
            .sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY() + b, mc.player.getZ(), false));
      }

      mc.player.setPosition(mc.player.getX(), mc.player.getY() + b, mc.player.getZ());
   }
}
