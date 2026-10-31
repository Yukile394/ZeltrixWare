package thunder.hack.injection.accesors;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.component.type.PotionContentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AreaEffectCloudEntity.class})
public interface IAreaEffectCloudEntity {
   @Accessor("potionContentsComponent")
   PotionContentsComponent getPotionContentsComponent();
}
