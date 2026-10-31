package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.client.network.PlayerListEntry;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.client.ClientSettings;

public class PlayerArgumentType implements ArgumentType<PlayerListEntry> {
   private static final Collection<String> EXAMPLES = List.of("pan4ur", "06ED");

   public static PlayerArgumentType create() {
      return new PlayerArgumentType();
   }

   public PlayerListEntry parse(StringReader reader) throws CommandSyntaxException {
      String name = reader.readString();
      PlayerListEntry player = IManager.mc.getNetworkHandler().getPlayerList().stream().filter(p -> name.equals(p.getProfile().getName())).findFirst().orElse(null);
      if (player == null) {
         throw new DynamicCommandExceptionType(
               nickname -> Text.literal(
                     ClientSettings.isRu()
                        ? "\u0418\u0433\u0440\u043e\u043a " + nickname + " \u043d\u0435 \u0432 \u0441\u0435\u0442\u0438"
                        : "Player " + nickname + " offline"
                  )
            )
            .create(name);
      } else {
         return player;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(IManager.mc.getNetworkHandler().getPlayerList().stream().map(p -> p.getProfile().getName()), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
