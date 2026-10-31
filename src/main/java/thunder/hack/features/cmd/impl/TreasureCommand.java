package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

public class TreasureCommand extends Command {
   public TreasureCommand() {
      super("gettreasure", "treasure");
   }

   @Override
   public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(
         context -> {
            if (mc.player.getMainHandStack().getItem().toString().equals("filled_map")) {
               Record nbt = (Record)mc.player.getMainHandStack().getOrDefault(DataComponentTypes.MAP_DECORATIONS, PotionContentsComponent.DEFAULT);
               if (nbt == null) {
                  return 1;
               }

               StringBuilder result = new StringBuilder();
               String rawNbt = nbt.toString();

               for (int i = rawNbt.indexOf("x="); i < rawNbt.indexOf(", rotation") - 2; i++) {
                  result.append(rawNbt.charAt(i));
               }

               sendMessage(
                  ClientSettings.isRu()
                     ? "\u041d\u0430\u0448\u0435\u043b! \u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b: " + result
                     : "Found! Coords: " + result
               );
            } else {
               sendMessage(
                  ClientSettings.isRu()
                     ? "\u0412\u043e\u0437\u044c\u043c\u0438 \u043a\u0430\u0440\u0442\u0443 \u0432 \u0440\u0443\u043a\u0438!"
                     : "Take a map into your hand!"
               );
            }

            return 1;
         }
      );
   }
}
