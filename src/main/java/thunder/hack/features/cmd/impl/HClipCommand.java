package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

public class HClipCommand extends Command {
   public HClipCommand() {
      super("hclip");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         literal("s")
            .executes(
               context -> {
                  double x = -((double)MathHelper.sin(mc.player.getYaw() * (float) (Math.PI / 180.0)) * 0.8);
                  double z = (double)MathHelper.cos(mc.player.getYaw() * (float) (Math.PI / 180.0)) * 0.8;

                  for (int i = 0; i < 10; i++) {
                     mc.player
                        .networkHandler
                        .sendPacket(new PositionAndOnGround(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z, false));
                  }

                  mc.player.setPosition(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z);
                  return 1;
               }
            )
      );
      builder.then(
         arg("count", DoubleArgumentType.doubleArg())
            .executes(
               context -> {
                  double speed = (Double)context.getArgument("count", Double.class);

                  try {
                     sendMessage(
                        Formatting.GREEN
                           + "\u043a\u043b\u0438\u043f\u0430\u0435\u043c\u0441\u044f \u043d\u0430  "
                           + speed
                           + " \u0431\u043b\u043e\u043a\u043e\u0432."
                     );
                     mc.player
                        .setPosition(
                           mc.player.getX() - (double)MathHelper.sin(mc.player.getYaw() * (float) (Math.PI / 180.0)) * speed,
                           mc.player.getY(),
                           mc.player.getZ() + (double)MathHelper.cos(mc.player.getYaw() * (float) (Math.PI / 180.0)) * speed
                        );
                  } catch (Exception var4) {
                  }

                  return 1;
               }
            )
      );
      builder.executes(context -> {
         sendMessage("\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439 .hclip <\u0447\u0438\u0441\u043b\u043e>, .hclip s");
         return 1;
      });
   }
}
