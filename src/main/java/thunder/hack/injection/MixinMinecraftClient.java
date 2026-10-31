package thunder.hack.injection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.RunArgs;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.Icons;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.events.impl.EventHandleBlockBreaking;
import thunder.hack.events.impl.EventPostTick;
import thunder.hack.events.impl.EventScreen;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.WindowResizeCallback;

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient {
   @Shadow
   @Final
   private Window window;
   @Unique
   private String[] shittyServers = new String[]{"mineblaze", "musteryworld", "dexland", "masedworld", "vimeworld", "hypemc", "vimemc"};

   @Shadow
   public abstract void setScreen(@Nullable Screen var1);

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   void postWindowInit(RunArgs args, CallbackInfo ci) {
      try {
         FontRenderers.settings = FontRenderers.create(12.0F, "comfortaa");
         FontRenderers.modules = FontRenderers.create(15.0F, "comfortaa");
         FontRenderers.categories = FontRenderers.create(18.0F, "comfortaa");
         FontRenderers.thglitch = FontRenderers.create(36.0F, "glitched");
         FontRenderers.thglitchBig = FontRenderers.create(72.0F, "glitched");
         FontRenderers.monsterrat = FontRenderers.create(18.0F, "monsterrat");
         FontRenderers.sf_bold = FontRenderers.create(16.0F, "sf_bold");
         FontRenderers.sf_medium = FontRenderers.create(16.0F, "sf_medium");
         FontRenderers.sf_medium_mini = FontRenderers.create(12.0F, "sf_medium");
         FontRenderers.sf_medium_modules = FontRenderers.create(14.0F, "sf_medium");
         FontRenderers.sf_bold_mini = FontRenderers.create(14.0F, "sf_bold");
         FontRenderers.sf_bold_micro = FontRenderers.create(12.0F, "sf_bold");
         FontRenderers.profont = FontRenderers.create(16.0F, "profont");
         FontRenderers.icons = FontRenderers.create(20.0F, "icons");
         FontRenderers.mid_icons = FontRenderers.create(46.0F, "icons");
         FontRenderers.big_icons = FontRenderers.create(72.0F, "icons");
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   void preTickHook(CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         ThunderHack.EVENT_BUS.post(new EventTick());
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   void postTickHook(CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         ThunderHack.EVENT_BUS.post(new EventPostTick());
      }
   }

   @Inject(
      method = {"onResolutionChanged"},
      at = {@At("TAIL")}
   )
   private void captureResize(CallbackInfo ci) {
      ((WindowResizeCallback)WindowResizeCallback.EVENT.invoker()).onResized((MinecraftClient)this, this.window);
   }

   @Inject(
      method = {"doItemPick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void doItemPickHook(CallbackInfo ci) {
   }

   @Inject(
      method = {"setOverlay"},
      at = {@At("HEAD")}
   )
   public void setOverlay(Overlay overlay, CallbackInfo ci) {
   }

   @Inject(
      method = {"setScreen"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setScreenHookPre(Screen screen, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventScreen event = new EventScreen(screen);
         ThunderHack.EVENT_BUS.post(event);
         if (event.isCancelled() || ClickGUI.close && screen == null) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"setScreen"},
      at = {@At("RETURN")}
   )
   public void setScreenHookPost(Screen screen, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if (screen instanceof MultiplayerScreen mScreen && ModuleManager.antiServerAdd.isEnabled() && mScreen.getServerList() != null) {
            for (int i = 0; i < mScreen.getServerList().size(); i++) {
               ServerInfo info = mScreen.getServerList().get(i);

               for (String server : this.shittyServers) {
                  if (info != null && info.address != null && info.address.toLowerCase().contains(server.toLowerCase())) {
                     mScreen.getServerList().remove(info);
                     mScreen.getServerList().saveFile();
                     this.setScreen(screen);
                     break;
                  }
               }
            }
         }
      }
   }

   @Redirect(
      method = {"<init>"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"
      )
   )
   private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
      if (GLFW.glfwGetPlatform() == 393218) {
         MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
      } else {
         this.setWindowIcon(ThunderHack.class.getResourceAsStream("/icon.png"), ThunderHack.class.getResourceAsStream("/icon.png"));
      }
   }

   public void setWindowIcon(InputStream img16x16, InputStream img32x32) {
      try {
         MemoryStack memorystack = MemoryStack.stackPush();

         try {
            Buffer buffer = GLFWImage.malloc(2, memorystack);
            List<InputStream> imgList = List.of(img16x16, img32x32);
            List<ByteBuffer> buffers = new ArrayList<>();

            for (int i = 0; i < imgList.size(); i++) {
               NativeImage nativeImage = NativeImage.read(imgList.get(i));
               ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);
               bytebuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());
               buffer.position(i);
               buffer.width(nativeImage.getWidth());
               buffer.height(nativeImage.getHeight());
               buffer.pixels(bytebuffer);
               buffers.add(bytebuffer);
            }

            try {
               if (GLFW.glfwGetPlatform() != 393219) {
                  GLFW.glfwSetWindowIcon(Module.mc.getWindow().getHandle(), buffer);
               }
            } catch (Exception var11) {
            }

            buffers.forEach(MemoryUtil::memFree);
         } catch (Throwable var12) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var10) {
                  var12.addSuppressed(var10);
               }
            }

            throw var12;
         }

         if (memorystack != null) {
            memorystack.close();
         }
      } catch (IOException var13) {
      }
   }

   @Inject(
      method = {"doAttack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
      EventAttack event = new EventAttack(null, true);
      ThunderHack.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"handleBlockBreaking"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void handleBlockBreakingHook(boolean breaking, CallbackInfo ci) {
      EventHandleBlockBreaking event = new EventHandleBlockBreaking();
      ThunderHack.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
