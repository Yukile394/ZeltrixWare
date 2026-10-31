package thunder.hack.utility.discord.callbacks;

import com.sun.jna.Callback;
import thunder.hack.utility.discord.DiscordUser;

public interface ReadyCallback extends Callback {
   void apply(DiscordUser var1);
}
