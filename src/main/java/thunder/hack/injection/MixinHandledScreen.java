package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.text.Text;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.Core;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.misc.PeekScreen;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

@Mixin({HandledScreen.class})
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
   @Unique
   private final Timer delayTimer = new Timer();
   @Unique
   private float animation = 0.0F;
   @Unique
   private Runnable postRender;
   @Shadow
   @Nullable
   protected Slot focusedSlot;
   @Shadow
   protected int x;
   @Shadow
   protected int y;
   private static final ItemStack[] ITEMS = new ItemStack[27];
   private Map<Render2DEngine.Rectangle, Integer> clickableRects = new HashMap<>();

   protected MixinHandledScreen(Text title) {
      super(title);
   }

   @Shadow
   protected abstract boolean isPointOverSlot(Slot var1, double var2, double var4);

   @Shadow
   protected abstract void onMouseClick(Slot var1, int var2, int var3, SlotActionType var4);

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void drawScreenHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (ModuleManager.inventoryAnimation.isEnabled()) {
         this.animation = AnimationUtility.fast(this.animation, 1.0F, ModuleManager.inventoryAnimation.speed.getValue());
         context.getMatrices().push();
         float scale = 0.5F + 0.5F * this.animation;
         context.getMatrices().translate((float)this.width / 2.0F * (1.0F - scale), (float)this.height / 2.0F * (1.0F - scale), 0.0F);
         context.getMatrices().scale(scale, scale, 1.0F);
      }
   }

   private boolean shit() {
      return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 340)
         || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 344);
   }

   private boolean attack() {
      return Core.hold_mouse0;
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (ModuleManager.inventoryAnimation.isEnabled()) {
         context.getMatrices().pop();
      } else {
         this.animation = 0.0F;
      }

      if (!Module.fullNullCheck()) {
         if (this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty() && this.client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            if (this.focusedSlot.getStack().contains(DataComponentTypes.CONTAINER)) {
               this.renderShulkerToolTip(context, mouseX, mouseY, 0, 0, this.focusedSlot.getStack());
            } else if (this.focusedSlot.getStack().getItem() == Items.FILLED_MAP) {
               this.drawMapPreview(context, this.focusedSlot.getStack(), mouseX, mouseY);
            }
         }

         int xOffset = 0;
         int yOffset = 20;
         int stage = 0;
      }
   }

   @Inject(
      method = {"drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"},
      at = {@At("TAIL")}
   )
   protected void drawSlotHook(DrawContext context, Slot slot, CallbackInfo ci) {
   }

   public boolean renderShulkerToolTip(DrawContext context, int offsetX, int offsetY, int mouseX, int mouseY, ItemStack stack) {
      try {
         ContainerComponent compoundTag = (ContainerComponent)stack.get(DataComponentTypes.CONTAINER);
         if (compoundTag == null) {
            return false;
         } else {
            float[] colors = new float[]{1.0F, 1.0F, 1.0F};
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
               try {
                  Color c = new Color(Objects.requireNonNull(ShulkerBoxBlock.getColor(stack.getItem())).getEntityColor());
                  colors = new float[]{(float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getRed() / 255.0F, (float)c.getAlpha() / 255.0F};
               } catch (NullPointerException var12) {
                  colors = new float[]{1.0F, 1.0F, 1.0F};
               }
            }

            this.draw(context, compoundTag.stream().toList(), offsetX, offsetY, mouseX, mouseY, colors);
            return true;
         }
      } catch (Exception var13) {
         return false;
      }
   }

   @Inject(
      method = {"drawMouseoverTooltip"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if (this.focusedSlot != null
            && !this.focusedSlot.getStack().isEmpty()
            && this.client.player.playerScreenHandler.getCursorStack().isEmpty()
            && this.focusedSlot.getStack().getItem() == Items.FILLED_MAP) {
            ci.cancel();
         }
      }
   }

   @Unique
   private void draw(DrawContext context, List<ItemStack> itemStacks, int offsetX, int offsetY, int mouseX, int mouseY, float[] colors) {
      RenderSystem.disableDepthTest();
      GL11.glClear(256);
      offsetX += 8;
      offsetY -= 82;
      this.drawBackground(context, offsetX, offsetY, colors);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      DiffuseLighting.enableGuiDepthLighting();
      int row = 0;
      int i = 0;

      for (ItemStack itemStack : itemStacks) {
         context.drawItem(itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);
         context.drawItemInSlot(Module.mc.textRenderer, itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);
         if (mouseX > offsetX + 8 + i * 18 && mouseX < offsetX + 28 + i * 18 && mouseY > offsetY + 7 + row * 18 && mouseY < offsetY + 27 + row * 18) {
            this.postRender = () -> context.drawTooltip(this.textRenderer, getTooltipFromItem(Module.mc, itemStack), itemStack.getTooltipData(), mouseX, mouseY);
         }

         if (++i >= 9) {
            i = 0;
            row++;
         }
      }

      DiffuseLighting.disableGuiDepthLighting();
      RenderSystem.enableDepthTest();
   }

   private void drawBackground(DrawContext context, int x, int y, float[] colors) {
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1.0F);
      RenderSystem.texParameter(3553, 10240, 9729);
      RenderSystem.texParameter(3553, 10241, 9987);
      context.drawTexture(TextureStorage.container, x, y, 0.0F, 0.0F, 176, 67, 176, 67);
      RenderSystem.enableBlend();
   }

   private void drawMapPreview(DrawContext context, ItemStack stack, int x, int y) {
      RenderSystem.enableBlend();
      context.getMatrices().push();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int y1 = y - 12;
      int x1 = x + 8;
      int z = 300;
      MapState mapState = FilledMapItem.getMapState(stack, this.client.world);
      if (mapState != null) {
         mapState.getPlayerSyncData(this.client.player);
         x1 += 8;
         y1 += 8;
         int var14 = 310;
         double scale = 0.65625;
         context.getMatrices().translate((float)x1, (float)y1, (float)var14);
         context.getMatrices().scale((float)scale, (float)scale, 0.0F);
         Immediate consumer = this.client.getBufferBuilders().getEntityVertexConsumers();
         this.client
            .gameRenderer
            .getMapRenderer()
            .draw(context.getMatrices(), consumer, (MapIdComponent)stack.get(DataComponentTypes.MAP_ID), mapState, false, 15728880);
      }

      context.getMatrices().pop();
   }

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      if (!Module.fullNullCheck()) {
         if (button == 2
            && this.focusedSlot != null
            && !this.focusedSlot.getStack().isEmpty()
            && this.client.player.playerScreenHandler.getCursorStack().isEmpty()) {
            ItemStack itemStack = this.focusedSlot.getStack();
            if (itemStack.contains(DataComponentTypes.CONTAINER)) {
               Arrays.fill(ITEMS, ItemStack.EMPTY);
               ContainerComponent nbt = (ContainerComponent)itemStack.get(DataComponentTypes.CONTAINER);
               if (nbt != null) {
                  List<ItemStack> list = nbt.stream().toList();

                  for (int i = 0; i < list.size(); i++) {
                     ITEMS[i] = list.get(i);
                  }
               }

               this.client
                  .setScreen(
                     new PeekScreen(
                        new ShulkerBoxScreenHandler(0, this.client.player.getInventory(), new SimpleInventory(ITEMS)),
                        this.client.player.getInventory(),
                        this.focusedSlot.getStack().getName(),
                        ((BlockItem)this.focusedSlot.getStack().getItem()).getBlock()
                     )
                  );
               cir.setReturnValue(true);
            }
         }

         for (Render2DEngine.Rectangle rect : this.clickableRects.keySet()) {
            if (rect.contains(mouseX, mouseY)) {
               Module.mc
                  .interactionManager
                  .clickSlot(Module.mc.player.currentScreenHandler.syncId, this.clickableRects.get(rect), 0, SlotActionType.PICKUP, Module.mc.player);
            }
         }
      }
   }
}
