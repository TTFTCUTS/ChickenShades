package ttftcuts.chickenshades;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = ChickenShades.MODID, version = ChickenShades.VERSION, clientSideOnly = true)
public class ChickenShades implements IResourceManagerReloadListener
{
    public static final String MODID = "chickenshades";
    public static final String VERSION = "1.2.0";

    public static final Logger logger = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static ChickenShades instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        ((IReloadableResourceManager)(Minecraft.getMinecraft().getResourceManager())).registerReloadListener(this);
    }

    // ####################################################################

    public static Map<Class<? extends EntityLivingBase>, Map<String, String>> resourceNames = new HashMap<>();
    public static Map<RenderLivingBase<? extends EntityLivingBase>, LayerShades<? extends EntityLivingBase>> layers = new HashMap<>();
    public static Set<String> blacklist = new HashSet<String>();
    public static Set<String> whitelist = new HashSet<String>();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (RenderLivingBase b : layers.keySet()) {
            //b.removeLayer(layers.get(b));
            b.layerRenderers.remove(layers.get(b));
        }
        resourceNames.clear();
        layers.clear();
        blacklist.clear();
        whitelist.clear();
    }

    @SubscribeEvent
    public void handleFanciness(RenderLivingEvent.Pre<EntityLivingBase> event) {
        EntityLivingBase entity = event.getEntity();
        RenderLivingBase<EntityLivingBase> renderer = event.getRenderer();

        String resourceName = getResourceName(entity);
        
        if (blacklist.contains(resourceName)) {
            return;
        }

        ResourceLocation texture = new ResourceLocation(ChickenShades.MODID, resourceName + ".png");

        if (!whitelist.contains(resourceName)) {
            IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();

            IResource r = null;

            try {
                r = resourceManager.getResource(texture);
            } catch (Exception e) {
                // no need for an error printout here since they are expected most of the time
            }

            if (r != null) {
                whitelist.add(resourceName);
            } else {
                blacklist.add(resourceName);
            }
        }

        if (whitelist.contains(resourceName)){
            if (!layers.containsKey(renderer)) {
                LayerShades layer = new LayerShades(renderer);
                layers.put(renderer, layer);
                renderer.addLayer(layer);
            }

            LayerShades layer = layers.get(renderer);

            layer.texture = texture;
        }
    }

    @SubscribeEvent
    public void unhandleFanciness(RenderLivingEvent.Post<EntityLivingBase> event) {
        RenderLivingBase<EntityLivingBase> renderer = event.getRenderer();

        if (layers.containsKey(renderer)) {
            LayerShades layer = layers.get(renderer);
            layer.texture = null;
        }
    }

    public static String getResourceName(EntityLivingBase entity) {
        Class<? extends EntityLivingBase> clazz = entity.getClass();

        if (!resourceNames.containsKey(clazz)) {
            resourceNames.put(clazz, new HashMap<String, String>());
        }

        Map<String,String> submap = resourceNames.get(clazz);
        String name = entity.hasCustomName() ? entity.getCustomNameTag().toLowerCase() : entity.getName().toLowerCase();

        if (!submap.containsKey(name)) {


            submap.put(name, buildResourceName(getTypeName(clazz), name));
        }

        return submap.get(name);
    }

    public static String getTypeName(Class<? extends EntityLivingBase> clazz) {
        ResourceLocation typename = EntityList.getKey(clazz);
        String name = typename!=null? typename.toString() : "";

        if (name.equals("")) {
            name = clazz.getSimpleName();
        }

        return name.toLowerCase(Locale.ENGLISH);
    }

    private static final Pattern PATTERN = Pattern.compile("[^A-Za-z0-9_\\-.]");
    private static final int MAX_LENGTH = 127;

    public static String buildResourceName(String typeName, String displayName) {
        StringBuffer sb = new StringBuffer();

        Matcher m = PATTERN.matcher((typeName +"." + displayName).replace(" ", "_"));

        while (m.find()) {
            m.appendReplacement(sb,".");
        }
        m.appendTail(sb);

        String encoded = sb.toString();

        int end = Math.min(encoded.length(),MAX_LENGTH);
        return encoded.substring(0,end).toLowerCase(Locale.ENGLISH);
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemMonsterPlacer) {
            String name = ItemMonsterPlacer.getNamedIdFrom(stack).toString();
            
            Class<? extends Entity> clazz = EntityList.getClassFromName(name);

            if (clazz!= null && EntityLivingBase.class.isAssignableFrom(clazz)) {
                Class<? extends EntityLivingBase> livingclazz = (Class<? extends EntityLivingBase>)clazz;

                name = getTypeName(livingclazz);

                event.getToolTip().add(ChatFormatting.GRAY + "ChickenShades: "+ name);
            }
        }
    }
}
