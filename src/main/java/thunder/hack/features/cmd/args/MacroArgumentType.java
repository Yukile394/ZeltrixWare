package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.MacroManager;
import thunder.hack.features.modules.client.ClientSettings;

public class MacroArgumentType implements ArgumentType<MacroManager.Macro> {
   private static final Collection<String> EXAMPLES = Managers.MACRO.getMacros().stream().map(MacroManager.Macro::getName).limit(5L).toList();

   public static MacroArgumentType create() {
      return new MacroArgumentType();
   }

   public MacroManager.Macro parse(StringReader reader) throws CommandSyntaxException {
      MacroManager.Macro macro = Managers.MACRO.getMacroByName(reader.readString());
      if (macro == null) {
         throw new DynamicCommandExceptionType(
               name -> Text.literal(
                     ClientSettings.isRu()
                        ? "\u041c\u0430\u043a\u0440\u043e\u0441\u0430 "
                           + name.toString()
                           + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442("
                        : "Macro with name " + name.toString() + " does not exists("
                  )
            )
            .create(reader.readString());
      } else {
         return macro;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Managers.MACRO.getMacros().stream().map(MacroManager.Macro::getName), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
