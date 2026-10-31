package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

public class HorseSpeedCommand extends Command {
   public HorseSpeedCommand() {
      super("gethorsespeed", "horsespeed");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(
         context -> {
            if (mc.player.getVehicle() != null && mc.player.getVehicle() instanceof HorseEntity horse) {
               if (!horse.isSaddled()) {
                  if (ClientSettings.isRu()) {
                     sendMessage(Formatting.RED + "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u0441\u0435\u0434\u043b\u0430!");
                  } else {
                     sendMessage(Formatting.RED + "You don't have a saddle!");
                  }

                  return 1;
               }

               float speed = horse.forwardSpeed * 43.17F;
               float ratio = speed / 14.512F;
               String verbose = "";
               if ((double)ratio < 0.3) {
                  verbose = ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043a\u0440\u0438\u043d\u0436\u043e\u0432\u0430\u044f \u043b\u043e\u0448\u0430\u0434\u044c :("
                     : "Your horse is shitty :(";
               }

               if ((double)ratio > 0.3 && (double)ratio < 0.6) {
                  verbose = ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u043b\u043e\u0448\u0430\u0434\u044c"
                     : "Your horse is normal";
               }

               if ((double)ratio > 0.6) {
                  verbose = ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043f\u0438\u0437\u0434\u0430\u0442\u0430\u044f \u043b\u043e\u0448\u0430\u0434\u044c :)"
                     : "Your horse is good :)";
               }

               if ((double)ratio > 0.9) {
                  verbose = ClientSettings.isRu()
                     ? "\u0426\u044b\u0433\u0430\u043d\u044b \u0443\u0436\u0435 \u0432 \u043f\u0443\u0442\u0438"
                     : "Your horse is very good :)";
               }

               if (ClientSettings.isRu()) {
                  sendMessage(
                     Formatting.GREEN
                        + "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043b\u043e\u0448\u0430\u0434\u0438: "
                        + speed
                        + " \u0438\u0437 14.512. "
                        + verbose
                  );
               } else {
                  sendMessage(Formatting.GREEN + "Horse speed: " + speed + " out of 14.512. " + verbose);
               }
            } else if (ClientSettings.isRu()) {
               sendMessage(Formatting.RED + "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u043b\u043e\u0448\u0430\u0434\u0438!");
            } else {
               sendMessage(Formatting.RED + "You don't have a horse!");
            }

            return 1;
         }
      );
   }
}
