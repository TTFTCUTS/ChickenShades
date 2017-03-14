package ttftcuts.chickenshades;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class LayerShades<T extends EntityLivingBase> implements LayerRenderer<T> {

    public ResourceLocation texture = null;

    private RenderLivingBase<T> renderer;

    public LayerShades(RenderLivingBase<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!entitylivingbaseIn.isInvisible() && texture != null) {
            this.renderer.bindTexture(texture);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(0f, -2.0f);
            this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.disablePolygonOffset();

        }
    }

    @Override public boolean shouldCombineTextures() {
        return true;
    }
}
