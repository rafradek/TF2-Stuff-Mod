package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import atomicstryker.dynamiclights.client.DynamicLights;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2EventBusListener.DestroyBlockEntry;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.building.EntityTeleporter;
import rafradek.TF2weapons.characters.EntityDemoman;
import rafradek.TF2weapons.characters.EntityHeavy;
import rafradek.TF2weapons.characters.EntityMedic;
import rafradek.TF2weapons.characters.EntityPyro;
import rafradek.TF2weapons.characters.EntitySaxtonHale;
import rafradek.TF2weapons.characters.EntitySoldier;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.crafting.ItemTF2;
import rafradek.TF2weapons.decoration.GuiWearables;
import rafradek.TF2weapons.decoration.InventoryWearables;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.weapons.ItemAmmo;
import rafradek.TF2weapons.weapons.ItemChargingTarge;
import rafradek.TF2weapons.weapons.ItemCloak;
import rafradek.TF2weapons.weapons.ItemDisguiseKit;
import rafradek.TF2weapons.weapons.ItemHorn;
import rafradek.TF2weapons.weapons.ItemMedigun;
import rafradek.TF2weapons.weapons.ItemMeleeWeapon;
import rafradek.TF2weapons.weapons.ItemMinigun;
import rafradek.TF2weapons.weapons.ItemWeapon;
import rafradek.TF2weapons.weapons.MuzzleFlashLightSource;
import rafradek.TF2weapons.weapons.ItemSniperRifle;
import rafradek.TF2weapons.weapons.ItemSoldierBackpack;
import rafradek.TF2weapons.weapons.ItemUsable;
import rafradek.TF2weapons.weapons.WeaponsCapability;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Team.CollisionRule;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

public class TF2EventBusListener {
	public int tickleft;
	public static TextureAtlasSprite pelletIcon;
    boolean alreadypressed;
	private boolean alreadypressedalt;
	private boolean alreadypressedreload;
	public static boolean moveEntities;
	//public ModelSkeleton skeletonModel=new ModelSkeleton();
	//private HashMap eligibleChunksForSpawning = new HashMap();
	public static final String[] STRANGE_TITLES=new String[]{"Strange","Unremarkable","Scarely lethal","Mildly Menacing","Somewhat threatening","Uncharitable","Notably dangerous","Sufficiently lethal","Truly feared","Spectacularly lethal","Gore-spatterer","Wicked nasty","Positively inhumane","Totally ordinary","Face-melting","Rage-inducing","Server-clearing","Epic","Legendary","Australian","Hale's own"};
	public static final int[] STRANGE_KILLS=new int[]{0,10,25,45,70,100,135,175,225,275,350,500,750,999,1000,1500,2500,5000,7500,7616,8500};
	public static HashMap<EntityLivingBase, EntityLivingBase> fakeEntities=new HashMap<>();
	public static HashMap<World, Integer> spawnEvents=new HashMap<>();
	public static float tickTime=0;
	
	public static ArrayList<DestroyBlockEntry> destroyProgress=new ArrayList<>();
	public static final DataParameter<Boolean> ENTITY_INVIS=new DataParameter<Boolean>(165,DataSerializers.BOOLEAN);
	public static final DataParameter<Boolean> ENTITY_DISGUISED=new DataParameter<Boolean>(167,DataSerializers.BOOLEAN);
	public static final DataParameter<String> ENTITY_DISGUISE_TYPE=new DataParameter<String>(168,DataSerializers.STRING);
	public static final DataParameter<Boolean> ENTITY_UBER=new DataParameter<Boolean>(169,DataSerializers.BOOLEAN);
	public static final DataParameter<Float> ENTITY_OVERHEAL=new DataParameter<Float>(170,DataSerializers.FLOAT);
	public static final DataParameter<Boolean> ENTITY_EXP_JUMP=new DataParameter<Boolean>(171,DataSerializers.BOOLEAN);
	public static final DataParameter<Integer> ENTITY_HEAL_TARGET=new DataParameter<Integer>(172,DataSerializers.VARINT);
	private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
	public static ArrayList<MuzzleFlashLightSource> muzzleFlashes=new ArrayList<>();
	/*@SubscribeEvent
	public void spawn(WorldEvent.PotentialSpawns event){
		int time=(int) (event.getWorld().getWorldInfo().getWorldTotalTime()/24000);
		if(MapList.scoutSpawn.containsKey(event.list)){
			MapList.scoutSpawn.get(event.list).itemWeight=time;
		}
		else{
			System.out.println("add");
			SpawnListEntry entry=new SpawnListEntry(EntityScout.class, time, 1, 3);
			event.list.add(entry);
			MapList.scoutSpawn.put(event.list,entry);
		}
	}*/
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event){
		//if(event.getMap().getGlTextureId()==1){
			//System.out.println("Registered icon: "+event.getMap().getGlTextureId());
			pelletIcon=event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID,"items/pellet3"));
			event.getMap().registerSprite(new ResourceLocation(TF2weapons.MOD_ID,"items/ammo_belt_empty"));
		//}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void keyJumpPress(InputEvent.KeyInputEvent event){
		Minecraft minecraft=Minecraft.getMinecraft();
		if (minecraft.currentScreen == null &&minecraft.gameSettings.keyBindJump.isKeyDown()&&!minecraft.thePlayer.onGround&&minecraft.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET)!=null&&minecraft.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()==TF2weapons.itemScoutBoots&&!minecraft.thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null).doubleJumped){
			 minecraft.thePlayer.jump();
			 minecraft.thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null).doubleJumped=true;
			 TF2weapons.network.sendToServer(new TF2Message.ActionMessage(23));
		 }
		if (minecraft.currentScreen == null && minecraft.thePlayer.getHeldItemMainhand() != null)
        {
            if ( minecraft.thePlayer.getHeldItemMainhand().getItem() instanceof ItemUsable)
            {
            	keyPressUpdate(minecraft.gameSettings.keyBindAttack.isKeyDown(),minecraft.gameSettings.keyBindUseItem.isKeyDown());
            }
        }
		
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void mousePress(MouseEvent event){
		if(event.getButton()!=-1){
			Minecraft minecraft=Minecraft.getMinecraft();
			if (minecraft.currentScreen == null && minecraft.thePlayer.getHeldItemMainhand() != null)
	        {
	            if ( minecraft.thePlayer.getHeldItemMainhand().getItem() instanceof ItemUsable)
	            {
	            	KeyBinding.setKeyBindState(event.getButton() - 100, event.isButtonstate());
	            	keyPressUpdate(minecraft.gameSettings.keyBindAttack.isKeyDown(),minecraft.gameSettings.keyBindUseItem.isKeyDown());
	            }
	        }
		}
	}
	@SideOnly(Side.CLIENT)
	public void keyPressUpdate(boolean attackKeyDown, boolean altAttackKeyDown){
		Minecraft minecraft=Minecraft.getMinecraft();
		
    	boolean changed=false;
        ItemStack item = minecraft.thePlayer.getHeldItemMainhand();

        if (attackKeyDown&&!this.alreadypressed)
        {
        	changed=true;
            this.alreadypressed=true;
        }if(!attackKeyDown&&this.alreadypressed)
        {
        	changed=true;
        	this.alreadypressed=false;
        }
        if (altAttackKeyDown&&!this.alreadypressedalt)
        {
        	changed=true;
            this.alreadypressedalt=true;
        }
        if(!altAttackKeyDown&&this.alreadypressedalt)
        {
        	changed=true;
        	this.alreadypressedalt=false;
        }
        if (ClientProxy.reload.isKeyDown()&&!this.alreadypressedreload)
        {
        	changed=true;
            this.alreadypressedreload=true;
        }
        if(!ClientProxy.reload.isKeyDown()&&this.alreadypressedreload)
        {
        	changed=true;
        	this.alreadypressedreload=false;
        }
        if(changed){
        	EntityLivingBase player=minecraft.thePlayer;
        	WeaponsCapability cap=minecraft.thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null);
        	int oldState=cap.state&3;
        	int plus=cap.state&8;
        	int state=this.getActionType(attackKeyDown,altAttackKeyDown)+plus;
        	cap.state=state;
        	if(item != null&&item.getItem() instanceof ItemUsable&&oldState!=(this.getActionType(attackKeyDown,altAttackKeyDown)&3)&&item.getTagCompound().getByte("active")==2){
				if((oldState&2)<(state&2)){
					cap.stateDo(player, item);
					((ItemUsable)item.getItem()).startUse(item, player, player.worldObj, oldState, state&3);
				}
				else if((oldState&2)>(state&2)){
					((ItemUsable)item.getItem()).endUse(item, player, player.worldObj, oldState, state&3);
				}
				if((oldState&1)<(state&1)){
					cap.stateDo(player, item);
					((ItemUsable)item.getItem()).startUse(item, player, player.worldObj, oldState, state&3);
				}
				else if((oldState&1)>(state&1)){
					((ItemUsable)item.getItem()).endUse(item, player, player.worldObj, oldState, state&3);
				}
			}
        	TF2weapons.network.sendToServer(new TF2Message.ActionMessage(this.getActionType(attackKeyDown,altAttackKeyDown)));
        }
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEnd(TickEvent.ClientTickEvent event){
		
		Minecraft minecraft=Minecraft.getMinecraft();
		
		if(event.phase==TickEvent.Phase.END){
			/*Iterator<Entry<EntityLivingBase,EntityLivingBase>> iterator=fakeEntities.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<EntityLivingBase,EntityLivingBase> entry=iterator.next();
				EntityLivingBase real=entry.getKey();
				EntityLivingBase fake=entry.getValue();
				fake.posX=real.posX;
				fake.posY=real.posY;
				fake.posZ=real.posZ;
				fake.prevPosX=real.prevPosX;
				fake.prevPosY=real.prevPosY;
				fake.prevPosZ=real.prevPosZ;
				fake.rotationPitch=real.rotationPitch;
				fake.rotationYaw=real.rotationYaw;
				fake.rotationYawHead=real.rotationYawHead;
				fake.motionX=real.motionX;
				fake.motionY=real.motionY;
				fake.motionZ=real.motionZ;
				//System.out.println("pos: "+fake.posX+" "+fake.posY+" "+fake.posZ);
			}*/
			 
			Iterator<EntityLivingBase> soundIterator=ClientProxy.soundsToStart.keySet().iterator();
			while(soundIterator.hasNext()){
				EntityLivingBase living=soundIterator.next();
				TF2weapons.proxy.playReloadSound(living, ClientProxy.soundsToStart.get(living));
				soundIterator.remove();
			}
			while(ClientProxy.weaponSoundsToStart.size()>0){
				minecraft.getSoundHandler().playSound(ClientProxy.weaponSoundsToStart.get(0));
				ClientProxy.weaponSoundsToStart.remove(0);
			}
			Iterator<MuzzleFlashLightSource> iterator=muzzleFlashes.iterator();
			while(iterator.hasNext()){
				MuzzleFlashLightSource light=iterator.next();
				light.update();
				if(light.over){
					DynamicLights.removeLightSource(light);
					iterator.remove();
				}
			}
	        //ItemUsable.tick(true);
		}
	}
	@SideOnly(Side.CLIENT)
	public int getActionType(boolean attackKeyDown, boolean altAttackKeyDown){
		int value=0;
		if(attackKeyDown){
			value++;
		}
		if(altAttackKeyDown){
			value+=2;
		}
		if(ClientProxy.reload.isKeyDown()){
			value+=4;
		}
		return value;
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void getFov(FOVUpdateEvent event){
		if(event.getEntity().getHeldItem(EnumHand.MAIN_HAND)!=null&&event.getEntity().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable){
			if(event.getEntity().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSniperRifle&&event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).charging){
				event.setNewfov(event.getFov()*0.55f);
			}
			else if(event.getEntity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(ItemMinigun.slowdownUUID)!=null){
				event.setNewfov(event.getFov()*1.4f);
			}
		}
	}
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
		if(Minecraft.getMinecraft().thePlayer!=null){
			if ((event.getGui() instanceof GuiInventory ||event.getGui() instanceof GuiContainerCreative|| event.getGui() instanceof GuiWearables)&&!Minecraft.getMinecraft().thePlayer.getCapability(TF2weapons.INVENTORY_CAP, null).isEmpty()) {
				//GuiContainer gui = (GuiContainer) event.getGui();
				event.getButtonList().add(new GuiButton(97535627, event.getGui().width/2-10, event.getGui().height/2-140, 20, 20, "W"));
			}
			
			if (event.getGui() instanceof GuiMerchant){
				if (((GuiMerchant)event.getGui()).getMerchant().getDisplayName().getUnformattedText().equals(I18n.translateToLocal("entity.rafradek_tf2_weapons.hale.name")))
					event.getButtonList().add(new GuiButton(7578, event.getGui().width/2-50, event.getGui().height/2-110, 100, 20, "Change Team"));
			}	
			Minecraft.getMinecraft().thePlayer.getCapability(TF2weapons.WEAPONS_CAP, null).state&=8;
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event) {

		if (event.getGui() instanceof GuiInventory||event.getGui() instanceof GuiContainerCreative) {
			if (event.getButton().id == 97535627) {
				//Minecraft.getMinecraft().displayGuiScreen(null);
				TF2weapons.network.sendToServer(new TF2Message.ShowGuiMessage(0));
			}
		}
		
		if (event.getGui() instanceof GuiWearables) {
			if (event.getButton().id == 97535627) {
				event.getGui().mc.displayGuiScreen(new GuiInventory(event.getGui().mc.thePlayer));
				//PacketHandler.INSTANCE.sendToServer(new PacketOpenNormalInventory(event.getGui().mc.thePlayer));
			}
		}
		if (event.getGui() instanceof GuiMerchant && event.getButton().id == 7578) {
			ClientProxy.displayScreenJoinTeam();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void applyRecoil(EntityViewRenderEvent.CameraSetup event){
		if(event.getEntity().hasCapability(TF2weapons.WEAPONS_CAP, null)){
			WeaponsCapability cap=event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null);
			event.setPitch(event.getPitch()-cap.recoil);
			if(cap.recoil>0){
				cap.recoil=Math.max((cap.recoil*0.8f)-0.06f,0);
			}
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Pre event){
		EntityPlayer player=Minecraft.getMinecraft().thePlayer;
		WeaponsCapability cap=player.getCapability(TF2weapons.WEAPONS_CAP, null);
		if(event.getType()==ElementType.HELMET&&player!=null&&player.getHeldItem(EnumHand.MAIN_HAND)!=null&&player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSniperRifle && cap.charging){
			//System.out.println("drawing");
			GL11.glDisable(GL11.GL_DEPTH_TEST);
	        GL11.glDepthMask(false);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        //Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(xCoord, yCoord, textureSprite, widthIn, heightIn);
	        Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.scopeTexture);
	        double widthDrawStart=(double)(event.getResolution().getScaledWidth()-event.getResolution().getScaledHeight())/2;
	        double widthDrawEnd=widthDrawStart+event.getResolution().getScaledHeight();
	        Tessellator tessellator = Tessellator.getInstance();
	        VertexBuffer renderer=tessellator.getBuffer();
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos(widthDrawStart, (double)event.getResolution().getScaledHeight(), -90.0D).tex( 0.0D, 1.0D).endVertex();
	        renderer.pos(widthDrawEnd, (double)event.getResolution().getScaledHeight(), -90.0D).tex( 1.0D, 1.0D).endVertex();
	        renderer.pos(widthDrawEnd, 0.0D, -90.0D).tex( 1.0D, 0.0D).endVertex();
	        renderer.pos(widthDrawStart, 0.0D, -90.0D).tex( 0.0D, 0.0D).endVertex();
	        tessellator.draw();
	        Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.blackTexture);
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos(0, (double)event.getResolution().getScaledHeight(), -90.0D).tex(0d,1d).endVertex();
	        renderer.pos(widthDrawStart, (double)event.getResolution().getScaledHeight(), -90.0D).tex(1d,1d).endVertex();
	        renderer.pos(widthDrawStart, 0.0D, -90.0D).tex(1d,0d).endVertex();
	        renderer.pos(0, 0.0D, -90.0D).tex(0d,0d).endVertex();
	        tessellator.draw();
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos(widthDrawEnd, (double)event.getResolution().getScaledHeight(), -90.0D).tex(0d,1d).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth(), (double)event.getResolution().getScaledHeight(), -90.0D).tex(1d,1d).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth(), 0.0D, -90.0D).tex(1d,0d).endVertex();
	        renderer.pos(widthDrawEnd, 0.0D, -90.0D).tex(0d,0d).endVertex();
	        tessellator.draw();
	        Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.chargeTexture);
	        GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.7F);
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50, (double)event.getResolution().getScaledHeight()/2+15, -90.0D).tex(0d,0.25d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+100, (double)event.getResolution().getScaledHeight()/2+15, -90.0D).tex(0.508d,0.25d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+100, (double)event.getResolution().getScaledHeight()/2, -90.0D).tex(0.508d,0d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50, (double)event.getResolution().getScaledHeight()/2, -90.0D).tex(0d,0d).endVertex();
	        tessellator.draw();
	        if(cap.chargeTicks>=20){
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+110, (double)event.getResolution().getScaledHeight()/2+18, -90.0D).tex(0d,0.57d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+121, (double)event.getResolution().getScaledHeight()/2+18, -90.0D).tex(0.125d,0.57d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+121, (double)event.getResolution().getScaledHeight()/2-3, -90.0D).tex(0.125d,0.25d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+110, (double)event.getResolution().getScaledHeight()/2-3, -90.0D).tex(0d,0.25d).endVertex();
		        tessellator.draw();
	        }
	        double progress=cap.chargeTicks/ItemSniperRifle.getChargeTime(player.getHeldItem(EnumHand.MAIN_HAND), player);
	        GL11.glColor4f(1F, 1F, 1F, 1F);
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50, (double)event.getResolution().getScaledHeight()/2+15, -90.0D).tex(0d,0.25d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50+progress*50, (double)event.getResolution().getScaledHeight()/2+15, -90.0D).tex(progress*0.508d,0.25d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50+progress*50, (double)event.getResolution().getScaledHeight()/2, -90.0D).tex(progress*0.508d,0d).endVertex();
	        renderer.pos((double)event.getResolution().getScaledWidth()/2+50, (double)event.getResolution().getScaledHeight()/2, -90.0D).tex(0d,0d).endVertex();
	        tessellator.draw();
	        if(progress==1d){
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+110, (double)event.getResolution().getScaledHeight()/2+18, -90.0D).tex(0d,0.57d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+121, (double)event.getResolution().getScaledHeight()/2+18, -90.0D).tex(0.125d,0.57d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+121, (double)event.getResolution().getScaledHeight()/2-3, -90.0D).tex(0.125d,0.25d).endVertex();
		        renderer.pos((double)event.getResolution().getScaledWidth()/2+110, (double)event.getResolution().getScaledHeight()/2-3, -90.0D).tex(0d,0.25d).endVertex();
		        tessellator.draw();
	        }
	        GL11.glDepthMask(true);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		if(event.getType()==ElementType.HOTBAR&&player!=null&&player.getHeldItem(EnumHand.MAIN_HAND)!=null&&player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMedigun){
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.healingTexture);
			Tessellator tessellator = Tessellator.getInstance();
	        VertexBuffer renderer=tessellator.getBuffer();
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        GL11.glDepthMask(false);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
	        
	        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
	        renderer.pos(event.getResolution().getScaledWidth()-140, event.getResolution().getScaledHeight()-18, 0.0D).tex( 0.0D, 0.265625D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-12, event.getResolution().getScaledHeight()-18, 0.0D).tex( 1.0D, 0.265625D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-12, event.getResolution().getScaledHeight()-52, 0.0D).tex( 1.0D, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-140, event.getResolution().getScaledHeight()-52, 0.0D).tex( 0.0D, 0.0D).endVertex();
	        tessellator.draw();
	        
			Entity healTarget=player.worldObj.getEntityByID(cap.healTarget);
			if(healTarget!=null&&healTarget instanceof EntityLivingBase){
				EntityLivingBase living=(EntityLivingBase)healTarget;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
		        //Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+72, 0.0D).tex( 0.0D, 0.265625D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+64, event.getResolution().getScaledHeight()/2+72, 0.0D).tex( 1.0D, 0.265625D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+64, event.getResolution().getScaledHeight()/2+38, 0.0D).tex( 1.0D, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+38, 0.0D).tex( 0.0D, 0.0D).endVertex();
		        tessellator.draw();
		        float overheal=1f+living.getAbsorptionAmount()/living.getMaxHealth();
		        if(overheal>1f){
		        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
			        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			        renderer.pos(event.getResolution().getScaledWidth()/2-47-10*overheal, event.getResolution().getScaledHeight()/2+55+10*overheal, 0.0D).tex( 0.0D, 0.59375D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-47+10*overheal, event.getResolution().getScaledHeight()/2+55+10*overheal, 0.0D).tex( 0.28125D, 0.59375D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-47+10*overheal, event.getResolution().getScaledHeight()/2+55-10*overheal, 0.0D).tex( 0.28125D, 0.3125D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-47-10*overheal, event.getResolution().getScaledHeight()/2+55-10*overheal, 0.0D).tex( 0.0D, 0.3125D).endVertex();
			        tessellator.draw();
		        }
		        GL11.glColor4f(0.12F, 0.12F, 0.12F, 1F);
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-58.3, event.getResolution().getScaledHeight()/2+66.4, 0.0D).tex( 0.0D, 0.59375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-35.7, event.getResolution().getScaledHeight()/2+66.4, 0.0D).tex( 0.28125D, 0.59375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-35.7, event.getResolution().getScaledHeight()/2+43.6, 0.0D).tex( 0.28125D, 0.3125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-58.3, event.getResolution().getScaledHeight()/2+43.6, 0.0D).tex( 0.0D, 0.3125D).endVertex();
		        tessellator.draw();
		        float health=living.getHealth()/living.getMaxHealth();
		        if(health>0.33f){
		        	GL11.glColor4f(0.9F, 0.9F, 0.9F, 1F);
		        }
		        else{
		        	GL11.glColor4f(0.85F, 0.0F, 0.0F, 1F);
		        }
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-57, event.getResolution().getScaledHeight()/2+65, 0.0D).tex(0.0D, 0.59375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-37, event.getResolution().getScaledHeight()/2+65, 0.0D).tex( 0.28125D, 0.59375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-37, event.getResolution().getScaledHeight()/2+65-health*20, 0.0D).tex( 0.28125D, 0.59375D-0.28125D*health).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-57, event.getResolution().getScaledHeight()/2+65-health*20, 0.0D).tex( 0.0D, 0.59375D-0.28125D*health).endVertex();
		        tessellator.draw();
		        Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), "Healing:", event.getResolution().getScaledWidth()/2-28, event.getResolution().getScaledHeight()/2+42, 16777215);
		        Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), living.getDisplayName().getFormattedText(), event.getResolution().getScaledWidth()/2-28, event.getResolution().getScaledHeight()/2+54, 16777215);
		        
			}
			
			float uber=player.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getFloat("ubercharge");
			Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), "UBERCHARGE: "
		        	+Math.round(uber*100f)+"%", event.getResolution().getScaledWidth()-130, event.getResolution().getScaledHeight()-48, 16777215);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33F);
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-20, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-20, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        tessellator.draw();
	        
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132+112*uber, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132+112*uber, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        tessellator.draw();
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		Entity mouseTarget=Minecraft.getMinecraft().objectMouseOver!=null?Minecraft.getMinecraft().objectMouseOver.entityHit:null;
		if(event.getType()==ElementType.HOTBAR&&player!=null&&mouseTarget!=null&&mouseTarget instanceof EntityBuilding&&TF2weapons.isOnSameTeam(player, mouseTarget)){
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.buildingTexture);
			Tessellator tessellator = Tessellator.getInstance();
	        VertexBuffer renderer=tessellator.getBuffer();
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        GL11.glDepthMask(false);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
	        if(mouseTarget instanceof EntitySentry){
	        	EntitySentry sentry=(EntitySentry) mouseTarget;
				//GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
		        //Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+84, 0.0D).tex( 0.0D, 0.4375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+84, 0.0D).tex( 0.5625D, 0.4375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+20, 0.0D).tex( 0.5625D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+20, 0.0D).tex( 0.0D, 0.1875D).endVertex();
		        tessellator.draw();
		        double imagePos=sentry.getLevel()==1?0.375D:sentry.getLevel()==2?0.1875D:0D;
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.75D, imagePos+0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.9375D, imagePos+0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.9375D, imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.75D, imagePos).endVertex();
		        tessellator.draw();
		        
		        imagePos=sentry.getLevel()==3?0D:0.0625D;
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+77, 0.0D).tex( 0.9375D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+77, 0.0D).tex( 1D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+61, 0.0D).tex( 1D, imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+61, 0.0D).tex( 0.9375D, imagePos).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+41, 0.0D).tex( 0.9375D, 0.25D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+41, 0.0D).tex( 1D, 0.25D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+25, 0.0D).tex( 1D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+25, 0.0D).tex( 0.9375D, 0.1875D).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+59, 0.0D).tex( 0.9375D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+59, 0.0D).tex( 1D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+43, 0.0D).tex( 1D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+43, 0.0D).tex( 0.9375D, 0.125D).endVertex();
		        tessellator.draw();
		        
		        imagePos=sentry.getLevel()==1?0.3125D:sentry.getLevel()==2?0.375D:0.4375D;
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+38, 0.0D).tex( 0.9375D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+38, 0.0D).tex( 1D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+22, 0.0D).tex( 1D, imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+22, 0.0D).tex( 0.9375D, imagePos).endVertex();
		        tessellator.draw();
		        
		        
		        Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), Integer.toString(sentry.getKills()), event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+29, 16777215);
		        float health=sentry.getHealth()/sentry.getMaxHealth();
		        if(health>0.33f){
		        	GL11.glColor4f(0.9F, 0.9F, 0.9F, 1F);
		        }
		        else{
		        	GL11.glColor4f(0.85F, 0.0F, 0.0F, 1F);
		        }
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		        for(int i=0;i<health*11;i++){
		        
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+75-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+75-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+79-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+79-i*5, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33F);
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+58, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+58, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+44, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+44, 0.0D).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+76, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+76, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+62, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+62, 0.0D).endVertex();
		        tessellator.draw();
		        
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+58, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+(double)sentry.getAmmo()/(double)sentry.getMaxAmmo()*55D, event.getResolution().getScaledHeight()/2+58, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+(double)sentry.getAmmo()/(double)sentry.getMaxAmmo()*55D, event.getResolution().getScaledHeight()/2+44, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+44, 0.0D).endVertex();
		        tessellator.draw();
		        
		        double xOffset=sentry.getLevel()<3?sentry.getProgress()*0.275D:sentry.getRocketAmmo()*2.75D;
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+76, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+xOffset, event.getResolution().getScaledHeight()/2+76, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+xOffset, event.getResolution().getScaledHeight()/2+62, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+62, 0.0D).endVertex();
		        tessellator.draw();
			}
	        else if(mouseTarget instanceof EntityDispenser){
	        	EntityDispenser dispenser=(EntityDispenser) mouseTarget;
				//GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
		        //Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.0D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.5625D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.5625D, 0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.0D, 0D).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.75D, 0.75D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.9375D, 0.75D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.9375D, 0.5625D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.75D, 0.5625D).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+50, 0.0D).tex( 0.9375D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+50, 0.0D).tex( 1D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+34, 0.0D).tex( 1D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+34, 0.0D).tex( 0.9375D, 0.125D).endVertex();
		        tessellator.draw();
		        
		        double imagePos=dispenser.getLevel()==1?0.3125D:dispenser.getLevel()==2?0.375D:0.4375D;
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+46, 0.0D).tex( 0.9375D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+46, 0.0D).tex( 1D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+30, 0.0D).tex( 1D, imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+30, 0.0D).tex( 0.9375D, imagePos).endVertex();
		        tessellator.draw();
		        
		        if(dispenser.getLevel()<3){
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+70, 0.0D).tex( 0.9375D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+70, 0.0D).tex( 1D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+54, 0.0D).tex( 1D, 0.0625).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+54, 0.0D).tex( 0.9375D, 0.0625).endVertex();
		        tessellator.draw();
		        }
		        float health=dispenser.getHealth()/dispenser.getMaxHealth();
		        if(health>0.33f){
		        	GL11.glColor4f(0.9F, 0.9F, 0.9F, 1F);
		        }
		        else{
		        	GL11.glColor4f(0.85F, 0.0F, 0.0F, 1F);
		        }
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		        for(int i=0;i<health*8;i++){
		        
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+67-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+67-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+71-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+71-i*5, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33F);
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
		        tessellator.draw();
		        
		        if(dispenser.getLevel()<3){
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
		        renderer.begin(7, DefaultVertexFormats.POSITION);
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+dispenser.getMetal()*0.1375D, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13+dispenser.getMetal()*0.1375D, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
		        tessellator.draw();
		        
		        if(dispenser.getLevel()<3){
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+dispenser.getProgress()*0.275D, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+dispenser.getProgress()*0.275D, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        
			}
	        else if(mouseTarget instanceof EntityTeleporter){
	        	EntityTeleporter teleporter=(EntityTeleporter) mouseTarget;
				//GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
		        //Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(event.getResolution().getScaledWidth()/2-64, event.getResolution().getScaledHeight()/2+35, 0, 0, 128, 40);
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.0D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.5625D, 0.1875D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+72, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.5625D, 0D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-72, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.0D, 0D).endVertex();
		        tessellator.draw();
		        
		        double imagePos=teleporter.isExit()?0.1875D:0;
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.5625D+imagePos, 0.9375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+76, 0.0D).tex( 0.75D+imagePos, 0.9375D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.75D+imagePos, 0.75D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+28, 0.0D).tex( 0.5625D+imagePos, 0.75D).endVertex();
		        tessellator.draw();
		        
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+50, 0.0D).tex( 0.9375D, 0.3125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+50, 0.0D).tex( 1D, 0.3125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+34, 0.0D).tex( 1D, 0.25D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+34, 0.0D).tex( 0.9375D, 0.25D).endVertex();
		        tessellator.draw();
		        
		        imagePos=teleporter.getLevel()==1?0.3125D:teleporter.getLevel()==2?0.375D:0.4375D;
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+46, 0.0D).tex( 0.9375D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+46, 0.0D).tex( 1D, 0.0625D+imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-6, event.getResolution().getScaledHeight()/2+30, 0.0D).tex( 1D, imagePos).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-22, event.getResolution().getScaledHeight()/2+30, 0.0D).tex( 0.9375D, imagePos).endVertex();
		        tessellator.draw();
		        
		        if(teleporter.getLevel()<3){
		        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+70, 0.0D).tex( 0.9375D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+70, 0.0D).tex( 1D, 0.125D).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2+11, event.getResolution().getScaledHeight()/2+54, 0.0D).tex( 1D, 0.0625).endVertex();
		        renderer.pos(event.getResolution().getScaledWidth()/2-5, event.getResolution().getScaledHeight()/2+54, 0.0D).tex( 0.9375D, 0.0625).endVertex();
		        tessellator.draw();
		        }
		        if(teleporter.getTPprogress()<=0){
		        	Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), teleporter.getTeleports()+" (ID: "+teleporter.getID()+")", event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+38, 16777215);
		        }
		        float health=teleporter.getHealth()/teleporter.getMaxHealth();
		        if(health>0.33f){
		        	GL11.glColor4f(0.9F, 0.9F, 0.9F, 1F);
		        }
		        else{
		        	GL11.glColor4f(0.85F, 0.0F, 0.0F, 1F);
		        }
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		        for(int i=0;i<health*8;i++){
		        
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+67-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+67-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-63, event.getResolution().getScaledHeight()/2+71-i*5, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2-53, event.getResolution().getScaledHeight()/2+71-i*5, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33F);
		        if(teleporter.getTPprogress()>0){
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        if(teleporter.getLevel()<3){
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+68, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
		        if(teleporter.getTPprogress()>0){
			        double tpProgress=(1-((double)teleporter.getTPprogress()/(teleporter.getLevel()==1?200:(teleporter.getLevel()==2?100:60))))*55;
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+tpProgress, event.getResolution().getScaledHeight()/2+49, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+tpProgress, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+35, 0.0D).endVertex();
			        tessellator.draw();
		        }
		        if(teleporter.getLevel()<3){
			        renderer.begin(7, DefaultVertexFormats.POSITION);
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+teleporter.getProgress()*0.275D, event.getResolution().getScaledHeight()/2+69, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13+teleporter.getProgress()*0.275D, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        renderer.pos(event.getResolution().getScaledWidth()/2+13, event.getResolution().getScaledHeight()/2+55, 0.0D).endVertex();
			        tessellator.draw();
		        }
	        }
			/*float uber=player.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getFloat("ubercharge");
			Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), "UBERCHARGE: "
		        	+Math.round(uber*100f)+"%", event.getResolution().getScaledWidth()-130, event.getResolution().getScaledHeight()-48, 16777215);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.33F);
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-20, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-20, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        tessellator.draw();
	        
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132+112*uber, event.getResolution().getScaledHeight()-22, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132+112*uber, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        renderer.pos(event.getResolution().getScaledWidth()-132, event.getResolution().getScaledHeight()-36, 0.0D).endVertex();
	        tessellator.draw();*/
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(true);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Pre event){
		if(event.getEntityPlayer()!=Minecraft.getMinecraft().thePlayer)
			renderBeam(event.getEntityPlayer(),event.getPartialRenderTick());
		/*InventoryWearables inventory=event.getEntityPlayer().getCapability(TF2weapons.INVENTORY_CAP, null);
		for(int i=0;i<inventory.getInventoryStackLimit();i++){
			ItemStack stack=inventory.getStackInSlot(i);
			if(stack!=null){
				GlStateManager.pushMatrix();
				event.getRenderer().getMainModel().bipedHead.postRender(0.0625f);
				GlStateManager.translate(0.0F, -0.25F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(0.625F, -0.625F, -0.625F);
				
				Minecraft.getMinecraft().getItemRenderer().renderItem(event.getEntityPlayer(), stack, ItemCameraTransforms.TransformType.HEAD);
				GlStateManager.popMatrix();
			}
		}*/
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderHand(RenderHandEvent event){
		
		if(Minecraft.getMinecraft().thePlayer.getDataManager().get(ENTITY_INVIS)){
			/*GL11.glEnable(GL11.GL_BLEND);
			GlStateManager.clear(256);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        if(Minecraft.getMinecraft().thePlayer.getEntityData().getInteger("VisTicks")>=20){
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75f);
			}
			else{
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.6f*(1-(float)Minecraft.getMinecraft().thePlayer.getEntityData().getInteger("VisTicks")/20));
			}
			try {
				Method method=EntityRenderer.class.getDeclaredMethod("renderHand", float.class, int.class);
				method.setAccessible(true);
				method.invoke(Minecraft.getMinecraft().entityRenderer, event.partialTicks,event.renderPass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			event.setCanceled(true);
		}
	}
	@SideOnly(Side.CLIENT)
	public static void renderBeam(EntityLivingBase ent, float partialTicks){
		if(!ent.hasCapability(TF2weapons.WEAPONS_CAP, null)) return;
		//System.out.println("Drawing");
		Entity healTarget=ent.worldObj.getEntityByID(ent.getCapability(TF2weapons.WEAPONS_CAP, null).healTarget);
		if(healTarget!=null){
			Entity camera=Minecraft.getMinecraft().getRenderViewEntity();
			double cameraX=camera.prevPosX+(camera.posX-camera.prevPosX)*partialTicks;
			double cameraY=camera.prevPosY+(camera.posY-camera.prevPosY)*partialTicks;
			double cameraZ=camera.prevPosZ+(camera.posZ-camera.prevPosZ)*partialTicks;
			//System.out.println("rendering");
			double xPos1=ent.prevPosX+(ent.posX-ent.prevPosX)*partialTicks;
			double yPos1=ent.prevPosY+(ent.posY-ent.prevPosY)*partialTicks;
			double zPos1=ent.prevPosZ+(ent.posZ-ent.prevPosZ)*partialTicks;
			double xPos2=healTarget.prevPosX+(healTarget.posX-healTarget.prevPosX)*partialTicks;
			double yPos2=healTarget.prevPosY+(healTarget.posY-healTarget.prevPosY)*partialTicks;
			double zPos2=healTarget.prevPosZ+(healTarget.posZ-healTarget.prevPosZ)*partialTicks;
			double xDist=xPos2-xPos1;
			double yDist=(yPos2+(healTarget.getEntityBoundingBox().maxY-healTarget.getEntityBoundingBox().minY)/2+0.1)-(yPos1+ent.getEyeHeight()-0.1);
			double zDist=zPos2-zPos1;
			float f = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
			float fullDist = MathHelper.sqrt_double(xDist * xDist + yDist * yDist + zDist * zDist);
			Tessellator tessellator = Tessellator.getInstance();
	        VertexBuffer renderer=tessellator.getBuffer();
	        GlStateManager.pushMatrix();
	        GlStateManager.translate((float)xPos1-cameraX, (float)(yPos1+ent.getEyeHeight()-0.1)-cameraY, (float)zPos1-cameraZ);
	        GL11.glRotatef((float)(Math.atan2(xDist, zDist) * 180.0D / Math.PI), 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef((float)(Math.atan2(yDist, (double)f) * 180.0D / Math.PI)*-1, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableTexture2D();
	        GlStateManager.disableLighting();
	        GL11.glEnable(GL11.GL_BLEND);
	        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	        if(TF2weapons.getTeamForDisplay(ent)==0){
	        	GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.23F);
	        }
	        else{
	        	GL11.glColor4f(0.0F, 0.0F, 1.0F, 0.23F);
	        }
			renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(-0.04, -0.04,0).endVertex();
	        renderer.pos(0.04,0.04,0).endVertex();
	        renderer.pos(0.04,0.04, fullDist).endVertex();
	        renderer.pos(-0.04,-0.04, fullDist).endVertex();
	        tessellator.draw();
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(-0.04,-0.04, fullDist).endVertex();
	        renderer.pos(0.04,0.04, fullDist).endVertex();
	        renderer.pos(0.04, 0.04,0).endVertex();
	        renderer.pos(-0.04, -0.04,0).endVertex();
	        tessellator.draw();
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(0.04, -0.04,0).endVertex();
	        renderer.pos(-0.04, 0.04,0).endVertex();
	        renderer.pos(-0.04,0.04, fullDist).endVertex();
	        renderer.pos(0.04,-0.04, fullDist).endVertex();
	        tessellator.draw();
	        renderer.begin(7, DefaultVertexFormats.POSITION);
	        renderer.pos(0.04,-0.04, fullDist).endVertex();
	        renderer.pos(-0.04,0.04, fullDist).endVertex();
	        renderer.pos(-0.04, 0.04,0).endVertex();
	        renderer.pos(0.04, -0.04,0).endVertex();
	        tessellator.draw();
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.enableTexture2D();
	        GlStateManager.enableLighting();
	        GlStateManager.popMatrix();
		}
	}
	public static float interpolateRotation(float par1, float par2, float par3)
    {
        float f;

        for (f = par2 - par1; f < -180.0F; f += 360.0F)
        {
            ;
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return par1 + par3 * f;
    }
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event){
		
		tickTime=event.renderTickTime;
		Minecraft minecraft=Minecraft.getMinecraft();
		if(event.phase==Phase.END){
			
			
			if (minecraft.currentScreen == null && minecraft.thePlayer.getHeldItemMainhand() != null)
	        {
	            if ( minecraft.thePlayer.getHeldItemMainhand().getItem() instanceof ItemUsable)
	            {
	            	Mouse.poll();
	            	minecraft.thePlayer.rotationYawHead=minecraft.thePlayer.rotationYaw;
	            	moveEntities=true;
	            	keyPressUpdate(Mouse.isButtonDown(minecraft.gameSettings.keyBindAttack.getKeyCode()+100),Mouse.isButtonDown(minecraft.gameSettings.keyBindUseItem.getKeyCode()+100));
	            	moveEntities=false;
	            }
	        }
		}
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderWorld(RenderWorldLastEvent event){
		if(Minecraft.getMinecraft().thePlayer!=null)
			renderBeam(Minecraft.getMinecraft().thePlayer, event.getPartialTicks());
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderLivingEntity(RenderLivingEvent.Pre<EntityLivingBase> event){
		
		if(!(event.getEntity() instanceof EntityPlayer || event.getEntity() instanceof EntityTF2Character)) return;
		int visTick=event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks;
		
		if(visTick>0){
			if(visTick>=20){
				event.setCanceled(true);
				//System.out.println("VisTicks "+event.getEntity().getEntityData().getInteger("VisTicks"));
			}
			else{
				//System.out.println("VisTicksRender "+event.getEntity().getEntityData().getInteger("VisTicks"));
				GL11.glEnable(GL11.GL_BLEND);
		        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		        Team team=event.getEntity().getTeam();
		        if(team==event.getEntity().worldObj.getScoreboard().getTeam("RED")){
		        	GL11.glColor4f(1.0F, 0.17F, 0.17F, 0.7f*(1-(float)visTick/20));
		        	
		        }
		        else if(team==event.getEntity().worldObj.getScoreboard().getTeam("BLU")){
		        	GL11.glColor4f(0.17F, 0.17F, 1.0F, 0.7f*(1-(float)visTick/20));
		        }
		        else{
		        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7f*(1-(float)visTick/20));
		        }
			}
		}
		else if(event.getEntity() instanceof EntityPlayer && event.getEntity().getHeldItem(EnumHand.MAIN_HAND) != null &&
				event.getEntity().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable && !(event.getEntity().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMeleeWeapon)&& event.getRenderer().getMainModel() instanceof ModelBiped){
			
			((ModelBiped)event.getRenderer().getMainModel()).rightArmPose=((event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).state&3)>0)||
					event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).charging?ModelBiped.ArmPose.BOW_AND_ARROW:ModelBiped.ArmPose.ITEM;
		}
		if(event.getEntity().getDataManager().get(ENTITY_UBER)){
			//GlStateManager.disableLighting();
			if(TF2weapons.getTeamForDisplay(event.getEntity())==0){
	        	GL11.glColor4f(1.0F, 0.33F, 0.33F, 1F);
	        	
	        }
	        else{
	        	GL11.glColor4f(0.33F, 0.33F, 1.0F, 1F);
	        }
		}

		if(event.getRenderer()!=ClientProxy.disguiseRender&&event.getRenderer()!=ClientProxy.disguiseRenderPlayer&&event.getRenderer()!=ClientProxy.disguiseRenderPlayerSmall&&event.getEntity().getDataManager().get(ENTITY_DISGUISED)){
			
			/*EntityLivingBase entToRender=fakeEntities.get(event.getEntity());
			entToRender.prevRenderYawOffset=event.getEntity().prevRenderYawOffset;
			entToRender.renderYawOffset=event.getEntity().renderYawOffset;
			entToRender.limbSwing=event.getEntity().limbSwing;
			entToRender.limbSwingAmount=event.getEntity().limbSwingAmount;
			entToRender.prevLimbSwingAmount=event.getEntity().prevLimbSwingAmount;
			*/
			//Entity camera=Minecraft.getMinecraft().getRenderViewEntity();
			float partialTicks=tickTime;/*0;
			if(camera.posX-camera.prevPosX!=0){
				partialTicks=(float) ((camera.posX-event.x)/(camera.posX-camera.prevPosX));
			}
			/"lel: "+event.x+" "+camera.posX+" "+camera.prevPosX+" "+);*/
			/*ModelBase model=ClientProxy.entityRenderers.get("Creeper");
			GlStateManager.pushMatrix();
	        GlStateManager.disableCull();
	        model.swingProgress = event.getEntity().getSwingProgress(partialTicks);
	        model.isRiding = event.getEntity().isRiding();
	        model.isChild = event.getEntity().isChild();

	        try
	        {
	            float f = interpolateRotation(event.getEntity().prevRenderYawOffset, event.getEntity().renderYawOffset, partialTicks);
	            float f1 = interpolateRotation(event.getEntity().prevRotationYawHead, event.getEntity().rotationYawHead, partialTicks);
	            float f2 = f1 - f;

	            if (event.getEntity().isRiding() && event.getEntity().ridingEntity instanceof EntityLivingBase)
	            {
	                EntityLivingBase entitylivingbase = (EntityLivingBase)event.getEntity().ridingEntity;
	                f = interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
	                f2 = f1 - f;
	                float f3 = MathHelper.wrapAngleTo180_float(f2);

	                if (f3 < -85.0F)
	                {
	                    f3 = -85.0F;
	                }

	                if (f3 >= 85.0F)
	                {
	                    f3 = 85.0F;
	                }

	                f = f1 - f3;

	                if (f3 * f3 > 2500.0F)
	                {
	                    f += f3 * 0.2F;
	                }
	            }

	            float f7 = event.getEntity().prevRotationPitch + (event.getEntity().rotationPitch - event.getEntity().prevRotationPitch) * partialTicks;
	            GlStateManager.translate((float)event.x, (float)event.y, (float)event.z);
	            float ticks= this.handleRotationFloat(event.getEntity(), partialTicks);
	            this.rotateCorpse(entity, f8, f, partialTicks);
	            GlStateManager.enableRescaleNormal();
	            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
	            this.preRenderCallback(entity, partialTicks);
	            float f4 = 0.0625F;
	            GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
	            float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
	            float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
	            GlStateManager.enableAlpha();
	            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
	            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

	            if (this.renderOutlines)
	            {
	                boolean flag1 = this.setScoreTeamColor(entity);
	                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

	                if (flag1)
	                {
	                    this.unsetScoreTeamColor();
	                }
	            }
	            else
	            {
	                boolean flag = event.renderer.setDoRenderBrightness(event.getEntity(), partialTicks);
	                M.renderModel(event.getEntity(), f6, f5, f8, f2, f7, 0.0625F);

	                if (flag)
	                {
	                    event.renderer.unsetBrightness();
	                }

	                GlStateManager.depthMask(true);
	                //event.renderer.renderLayers(event.getEntity(), f6, f5, partialTicks, f8, f2, f7, 0.0625F);
	            //}

	            GlStateManager.disableRescaleNormal();
	        }
	        catch (Exception exception)
	        {
	            //logger.error((String)"Couldn\'t render entity", (Throwable)exception);
	        }

	        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GlStateManager.enableTexture2D();
	        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	        GlStateManager.enableCull();
	        GlStateManager.popMatrix();

	        /*if (!event.renderer.renderOutlines)
	        {
	            super.doRender(entity, x, y, z, entityYaw, partialTicks);
	        }
	        */
			RenderLivingBase<EntityLivingBase> render=null;
			if(event.getEntity().getDataManager().get(ENTITY_DISGUISE_TYPE).startsWith("M:")){
				String mobType=event.getEntity().getDataManager().get(ENTITY_DISGUISE_TYPE).substring(2);
				if(ClientProxy.entityModel.containsKey(mobType)){
					ClientProxy.disguiseRender.setRenderOptions(ClientProxy.entityModel.get(mobType), ClientProxy.textureDisguise.get(mobType));
					render=ClientProxy.disguiseRender;
				}
			}
			else if(event.getEntity() instanceof AbstractClientPlayer && event.getEntity().getDataManager().get(ENTITY_DISGUISE_TYPE).startsWith("P:")){
				
				if("slim".equals(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).skinType))
					render=ClientProxy.disguiseRenderPlayerSmall;
				else
					render=ClientProxy.disguiseRenderPlayer;
			}
			if(render!=null){
				render.doRender(event.getEntity(), event.getX(), event.getY(), event.getZ(), event.getEntity().rotationYaw, partialTicks);
				event.setCanceled(true);
			}
		}
		
	}
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderLivingPostEntity(RenderLivingEvent.Post<EntityLivingBase> event){
		if(!(event.getEntity() instanceof EntityPlayer || event.getEntity() instanceof EntityTF2Character)) return;
		if(event.getEntity().getDataManager().get(ENTITY_UBER)){
	        GL11.glColor4f(1.0F, 1F, 1.0F, 1F);
	        //GlStateManager.enableLighting();
		}
		if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1F, 1.0F, 1F);
		}
	}
	@SubscribeEvent
	public void untargetable(LivingSetAttackTargetEvent event){
		if(event.getTarget() != null&&((event.getTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)&&event.getTarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>=20))){
			event.getEntityLiving().setRevengeTarget(null);
			if(event.getEntityLiving() instanceof EntityLiving){
				((EntityLiving)event.getEntity()).setAttackTarget(null);
			}
		}
		if(event.getTarget() != null&&(event.getTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)&&event.getTarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks==0&&ItemDisguiseKit.isDisguised(event.getTarget())&&event.getEntityLiving().getAttackingEntity()!=event.getTarget())){
			if(event.getEntityLiving() instanceof EntityLiving){
				((EntityLiving)event.getEntity()).setAttackTarget(null);
			}
		}
	}
	@SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		//TF2weapons.syncConfig();
        if(eventArgs.getModID().equals("rafradek_tf2_weapons"))
            TF2weapons.syncConfig();
    }
	@SubscribeEvent
	public void serverTickEnd(TickEvent.ServerTickEvent event){
		
		if(event.phase==TickEvent.Phase.START){
			
	    	if(tickleft<=0){
				tickleft=20;
				Object[] entArray=ItemUsable.lastDamage.keySet().toArray();
				for(int x=0; x<entArray.length;x++){
					Entity entity=(Entity) entArray[x];
					float[] dmg=ItemUsable.lastDamage.get(entArray[x]);
					for(int i=19;i>=0;i--){
						if(i>0){
							dmg[i]=dmg[i-1];
						}
						else{
							dmg[0]=0;
						}
					}
				}
				
			}
	    	else
				tickleft--;
		}
	}
	@SubscribeEvent
	public void worldTickEnd(TickEvent.WorldTickEvent event){
		if(event.phase==TickEvent.Phase.START && event.side == Side.SERVER){
			for(int i=0;i<destroyProgress.size();i++){
				DestroyBlockEntry entry=destroyProgress.get(i);
				
				if(entry!=null&&entry.world==event.world){
					
					
					entry.curDamage-=0.01f;
					if(entry.curDamage<=0||entry.world.isAirBlock(entry.pos)){
						destroyProgress.set(i, null);
						event.world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE,0xFFFF+i), entry.pos, -1);
						continue;
					}
					
					if(event.world.getWorldTime()%12==0){
						int val=(int) ((entry.curDamage/TF2weapons.getHardness(entry.world.getBlockState(entry.pos),entry.world,entry.pos))*10);
						event.world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE,0xFFFF+i), entry.pos, val);
					}
				}
			}
			if(!event.world.isRemote && event.world.getWorldTime()%24000==1){
				if(spawnEvents.containsKey(event.world)&&spawnEvents.get(event.world)==1){
					for(EntityPlayer player:event.world.playerEntities){
						player.addChatMessage(new TextComponentString("The event has just ended"));
					}
					spawnEvents.put(event.world, 0);
				}
				else if(new Random(event.world.getSeed() + (long)(event.world.getWorldTime() * event.world.getWorldTime() * 4987142) + (long)(event.world.getWorldTime() * 5947611)).nextInt(2)==0){
					for(EntityPlayer player:event.world.playerEntities){
						player.addChatMessage(new TextComponentString("A crowd of RED and BLU mercenaries comes near you to fight each other"));
					}
					spawnEvents.put(event.world, 1);
				}
			}
		}
	}
	public static boolean isSpawnEvent(World world){
		return spawnEvents.containsKey(world)&&spawnEvents.get(world)==1;
	}
	/*@SubscribeEvent
	public void spawnCharacters(TickEvent.WorldTickEvent event){
		if(!event.getWorld().isRemote && event.phase==TickEvent.Phase.END){
			
			//if(time!=0&&event.getWorld().rand.nextInt(2500/time)!=0) return;
			this.eligibleChunksForSpawning.clear();
            int i;
            int k;

            for (i = 0; i < event.getWorld().playerEntities.size(); ++i)
            {
                EntityPlayer entityplayer = (EntityPlayer)event.getWorld().playerEntities.get(i);
                int j = MathHelper.floor_double(entityplayer.posX / 16.0D);
                k = MathHelper.floor_double(entityplayer.posZ / 16.0D);
                byte b0 = 8;

                for (int l = -b0; l <= b0; ++l)
                {
                    for (int i1 = -b0; i1 <= b0; ++i1)
                    {
                        boolean flag3 = l == -b0 || l == b0 || i1 == -b0 || i1 == b0;
                        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(l + j, i1 + k);

                        if (!flag3)
                        {
                            this.eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(false));
                        }
                        else if (!this.eligibleChunksForSpawning.containsKey(chunkcoordintpair))
                        {
                            this.eligibleChunksForSpawning.put(chunkcoordintpair, Boolean.valueOf(true));
                        }
                    }
                }
            }

            i = 0;
            ChunkCoordinates chunkcoordinates = event.getWorld().getSpawnPoint();
            Iterator iterator = this.eligibleChunksForSpawning.keySet().iterator();
            ArrayList<ChunkCoordIntPair> tmp = new ArrayList(eligibleChunksForSpawning.keySet());
            Collections.shuffle(tmp);
            iterator = tmp.iterator();
            label110:

            while (iterator.hasNext())
            {
                ChunkCoordIntPair chunkcoordintpair1 = (ChunkCoordIntPair)iterator.next();

                if (!((Boolean)this.eligibleChunksForSpawning.get(chunkcoordintpair1)).booleanValue())
                {
                	Chunk chunk = event.getWorld().getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos);
                    int x = chunkcoordintpair1.chunkXPos * 16 + event.getWorld().rand.nextInt(16);
                    int z = chunkcoordintpair1.chunkZPos * 16 + event.getWorld().rand.nextInt(16);
                    int y = event.getWorld().rand.nextInt(chunk == null ? event.getWorld().getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
                    System.out.println("tick2");
                    if (!event.getWorld().getBlock(x, y, z).isNormalCube() && event.getWorld().getBlock(x, y, z).getMaterial() == Material.air)
                    {
                        int i2 = 0;
                        int j2 = 0;
                        int team=event.getWorld().rand.nextInt(2);
                        System.out.println("tick3");
                        while (j2 < 1)
                        {
                            int k2 = x;
                            int l2 = y;
                            int i3 = z;
                            byte b1 = 6;
                            IEntityLivingData ientitylivingdata = null;
                            int j3 = 0;
                            System.out.println("tick4");
                            while (true)
                            {
                            	System.out.println("tick5");
                                if (j3 < 4)
                                {
                                    label103:
                                    {
                                        k2 += event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1);
                                        l2 += event.getWorld().rand.nextInt(1) - event.getWorld().rand.nextInt(1);
                                        i3 += event.getWorld().rand.nextInt(b1) - event.getWorld().rand.nextInt(b1);

                                        if (canCreatureTypeSpawnAtLocation(EnumCreatureType.monster, event.getWorld(), k2, l2, i3))
                                        {System.out.println("tick6");
                                            float f = (float)k2 + 0.5F;
                                            float f1 = (float)l2;
                                            float f2 = (float)i3 + 0.5F;

                                            if (event.getWorld().getClosestPlayer((double)f, (double)f1, (double)f2, 24.0D) == null)
                                            {
                                            	System.out.println("tick7");
                                                float f3 = f - (float)chunkcoordinates.posX;
                                                float f4 = f1 - (float)chunkcoordinates.posY;
                                                float f5 = f2 - (float)chunkcoordinates.posZ;
                                                float f6 = f3 * f3 + f4 * f4 + f5 * f5;

                                                if (f6 >= 576.0F)
                                                {
                                                    EntityTF2Character entityliving=null;

                                                    System.out.println("tick8");
                                                    try
                                                    {
                                                    	switch (event.getWorld().rand.nextInt(2)){
                                                    	case 1:entityliving = new EntityScout(event.getWorld());
                                                    	default:entityliving = new EntityHeavy(event.getWorld());
                                                    	}
                                                        entityliving.setEntTeam(team);
                                                    }
                                                    catch (Exception exception)
                                                    {
                                                        exception.printStackTrace();
                                                    }

                                                    entityliving.setLocationAndAngles((double)f, (double)f1, (double)f2, event.getWorld().rand.nextFloat() * 360.0F, 0.0F);

                                                    Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, event.getWorld(), f, f1, f2);
                                                    if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entityliving.getCanSpawnHere()))
                                                    {
                                                    	System.out.println("tick9");
                                                        ++i2;
                                                        event.getWorld().spawnEntityInWorld(entityliving);
                                                        if (!ForgeEventFactory.doSpecialSpawn(entityliving, event.getWorld(), f, f1, f2))
                                                        {
                                                            ientitylivingdata = entityliving.onSpawnWithEgg(ientitylivingdata);
                                                        }

                                                        if (j2 >= ForgeEventFactory.getMaxSpawnPackSize(entityliving))
                                                        {
                                                            continue label110;
                                                        }
                                                    }

                                                    i += i2;
                                                }
                                            }
                                        }

                                        ++j3;
                                        continue;
                                    }
                                }
                                j2++;
                                break;
                            }
                        }
                    }
                }
            }
        }
	}
	public static boolean canCreatureTypeSpawnAtLocation(EnumCreatureType p_77190_0_, World p_77190_1_, int p_77190_2_, int p_77190_3_, int p_77190_4_)
    {
        if (!World.doesBlockHaveSolidTopSurface(p_77190_1_, p_77190_2_, p_77190_3_ - 1, p_77190_4_))
        {
            return false;
        }
        else
        {
            Block block = p_77190_1_.getBlock(p_77190_2_, p_77190_3_ - 1, p_77190_4_);
            boolean spawnBlock = block.canCreatureSpawn(p_77190_0_, p_77190_1_, p_77190_2_, p_77190_3_ - 1, p_77190_4_);
            return spawnBlock && block != Blocks.bedrock && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).isNormalCube() && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_, p_77190_4_).getMaterial().isLiquid() && !p_77190_1_.getBlock(p_77190_2_, p_77190_3_ + 1, p_77190_4_).isNormalCube();
        }
    }*/
	@SubscribeEvent
	public void stopHurt(LivingAttackEvent event){
		if(event.getSource().getEntity()!=null&&(event.getSource().damageType.equals("mob")||event.getSource().damageType.equals("player"))){
			EntityLivingBase damageSource=(EntityLivingBase) event.getSource().getEntity();
			if(damageSource.getActivePotionEffect(TF2weapons.stun)!=null || damageSource.getActivePotionEffect(TF2weapons.bonk)!=null){
				event.setCanceled(true);
			}
			if(damageSource.hasCapability(TF2weapons.WEAPONS_CAP, null)&&damageSource.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
				event.setCanceled(true);
			}
			if(damageSource.hasCapability(TF2weapons.WEAPONS_CAP, null)&&damageSource.getDataManager().get(ENTITY_DISGUISED)){
				disguise(damageSource,false);
			}
		}
		
		if(!event.isCanceled()&&event.getAmount()>0){
			/*if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
				event.getEntity().getEntityData().setInteger("VisTicks", Math.min(10,event.getEntity().getEntityData().getInteger("VisTicks")));
				event.getEntity().setInvisible(false);
				//System.out.println("notInvisible");
			}*/
			event.getEntityLiving().getEntityData().setInteger("lasthit", event.getEntityLiving().ticksExisted);
		}
		
	}
	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event){
		if(event.isWasDeath()){
			InventoryWearables oldInv=event.getOriginal().getCapability(TF2weapons.INVENTORY_CAP, null);
			InventoryWearables newInv=event.getEntityPlayer().getCapability(TF2weapons.INVENTORY_CAP, null);
			for(int i=0;i<3;i++){
				newInv.setInventorySlotContents(i, oldInv.getStackInSlot(i));
			}
		}
	}
	@SubscribeEvent
	public void uber(LivingHurtEvent event){
		/*if(event.getEntity().getEntityData().getByte("IsCloaked")!=0){
			event.getEntity().getEntityData().setInteger("VisTicks", Math.min(10,event.getEntity().getEntityData().getInteger("VisTicks")));
			event.getEntity().setInvisible(false);
			//System.out.println("notInvisible");
		}*/
		if(event.getEntityLiving().getActivePotionEffect(TF2weapons.crit)!=null){
			event.setAmount(event.getAmount()*1.1f);
		}
		for(ItemStack stack:event.getEntityLiving().getEquipmentAndArmor()){
			if(stack != null){
				//System.out.println("Damaged");
				event.setAmount(TF2Attribute.getModifier("Damage Resist", stack, event.getAmount(), event.getEntityLiving()));
				if(event.getSource().isExplosion() ){
					event.setAmount(TF2Attribute.getModifier("Explosion Resist", stack, event.getAmount(), event.getEntityLiving()));
					//System.out.println("Absorbed: "+TF2Attribute.getModifier("Explosion Resist", stack, 1, event.getEntityLiving()));
				}
				if(event.getSource().isFireDamage() ){
					event.setAmount(TF2Attribute.getModifier("Fire Resist", stack, event.getAmount(), event.getEntityLiving()));
				}
			}
		}
		if(event.getEntityLiving().getActivePotionEffect(TF2weapons.backup)!=null){
			if(event.getSource().getSourceOfDamage() instanceof EntityArrow){
				event.setAmount(Math.min(event.getAmount(),(float) 8f));
			}
			if((event.getSource().damageType.equals("mob")||event.getSource().damageType.equals("player")) && (event.getSource().getEntity()!=null && event.getSource().getEntity() instanceof EntityLivingBase && ((EntityLivingBase)event.getSource().getEntity()).getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"))!=null)){
				event.setAmount((float) Math.min(event.getAmount(), 1+((EntityLivingBase)event.getSource().getEntity()).getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getModifier(UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF")).getAmount()))/*((EntityLivingBase)event.getSource().getEntity()).getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName()).toArray(new AttributeModifier[2])[0].))*/;
			}
			event.setAmount(event.getAmount()*0.65f);
		}
		if(event.getEntityLiving() instanceof EntityTF2Character && event.getSource().getEntity()!=null && event.getSource().getEntity()==event.getEntity()){
			event.setAmount(event.getAmount()*0.35f);
		}
		
		if((event.getEntityLiving().getActivePotionEffect(TF2weapons.bonk)!=null||event.getEntityLiving().getDataManager().get(ENTITY_UBER))&&!event.getSource().canHarmInCreative()){
			event.setCanceled(true);
		}
		if((event.getSource().getEntity()!=null && event.getSource().getEntity() instanceof EntityLivingBase)){
			if(((EntityLivingBase)event.getSource().getEntity()).getActivePotionEffect(TF2weapons.bonk)!=null){
				event.setCanceled(true);
			}
			if(!(event.getSource() instanceof TF2DamageSource)){
				int crit=TF2weapons.calculateCritsPost(event.getEntityLiving(), (EntityLivingBase)event.getSource().getEntity(), TF2weapons.calculateCritPre(null, (EntityLivingBase)event.getSource().getEntity()), null);
				if(crit==1){
					event.setAmount(event.getAmount()*1.35f);
				}
				else if(crit==2){
					event.setAmount(event.getAmount()*2f);
				}
			}
			ItemStack backpack=ItemHorn.getBackpack((EntityLivingBase) event.getSource().getEntity());
			if(backpack!=null && !backpack.getTagCompound().getBoolean("Active")){
				((ItemSoldierBackpack)backpack.getItem()).addRage(backpack, event.getAmount(),event.getEntityLiving());
			}
			if(((EntityLivingBase)event.getSource().getEntity()).getActivePotionEffect(TF2weapons.conch)!=null){
				((EntityLivingBase) event.getSource().getEntity()).heal(0.35f*getDamageReduction(event.getSource(),event.getEntityLiving(),event.getAmount()));
			}
			if(event.getEntityLiving().getActivePotionEffect(TF2weapons.madmilk)!=null){
				((EntityLivingBase) event.getSource().getEntity()).heal(0.6f*getDamageReduction(event.getSource(),event.getEntityLiving(),event.getAmount()));
			}
		}
		if(!event.getEntityLiving().worldObj.isRemote&&event.getEntityLiving().getDataManager().get(ENTITY_OVERHEAL)>0){
			event.getEntityLiving().getEntityData().setFloat("Overheal",event.getEntityLiving().getAbsorptionAmount());
			if(event.getEntityLiving().getDataManager().get(ENTITY_OVERHEAL)<=0){
				event.getEntityLiving().getEntityData().setFloat("Overheal",-1f);
			}
			//TF2weapons.network.sendToDimension(new TF2Message.PropertyMessage("overheal", event.getEntityLiving().getAbsorptionAmount(),event.getEntityLiving()),event.getEntityLiving().dimension);
		}
	}
	public static float getDamageReduction(DamageSource source, EntityLivingBase living, float damage){
		return CombatRules.getDamageAfterMagicAbsorb(CombatRules.getDamageAfterAbsorb(damage, (float)living.getTotalArmorValue(), (float)living.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue()),EnchantmentHelper.getEnchantmentModifierDamage(living.getArmorInventoryList(), source));
	}
	@SubscribeEvent
	public void stopBreak(BlockEvent.BreakEvent event){
		
		if(event.getPlayer().getHeldItem(EnumHand.MAIN_HAND) !=null&&event.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable){
			event.setCanceled(true);
		}
		if(event.getPlayer().getActivePotionEffect(TF2weapons.bonk)!=null){
			event.setCanceled(true);
		}
		if(event.getPlayer().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
			event.setCanceled(true);
		}
		if(event.getPlayer().getActivePotionEffect(TF2weapons.stun)!=null){
			event.setCanceled(true);
		}
		if(event.getPlayer().getDataManager().get(ENTITY_DISGUISED)){
			disguise(event.getPlayer(),false);
		}
	}
	/*@SubscribeEvent
	public void stopInteract(PlayerInteractEvent event){
		if(!((event.==PlayerInteractEvent.Action.RIGHT_CLICK_AIR||event.action==PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)&&event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND)!=null&&(event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemCloak || event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemDisguiseKit))){
			if(event.getEntity()Player.getEntityData().getByte("Disguised")!=0){
				disguise(event.getEntity()Player,false);
			}
			if((event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND) != null&&event.getEntity()Player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable && event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)||event.getEntity()Player.getEntityData().getInteger("VisTicks")!=0){
				event.setCanceled(true);
			}
		}
	}*/
	@SubscribeEvent
	public void startTracking(PlayerEvent.StartTracking event){
		if(event.getTarget() instanceof EntityPlayer && !event.getTarget().worldObj.isRemote){
			//System.out.println("Tracking");
			InventoryWearables inv=event.getTarget().getCapability(TF2weapons.INVENTORY_CAP, null);
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) event.getTarget(),0,inv.getStackInSlot(0)), (EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) event.getTarget(),1,inv.getStackInSlot(1)), (EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) event.getTarget(),2,inv.getStackInSlot(2)), (EntityPlayerMP) event.getEntityPlayer());
			TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) event.getTarget(),3,inv.getStackInSlot(3)), (EntityPlayerMP) event.getEntityPlayer());
		}
		/*if(event.getEntity() instanceof EntityPlayer){
			if(event.getEntity().worldObj.isRemote){
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(99, (EntityLivingBase) event.getEntity()));
			}
		}*/
		
		/*if(event.getEntity().getDataManager().get(ENTITY_DISGUISE_TICKS)==40){
			disguise((EntityLivingBase) event.getEntity(), true);
		}*/
	}
	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event){
		if(event.getEntity() instanceof EntityPlayer){
			if(event.getEntity().worldObj.isRemote){
				TF2weapons.network.sendToServer(new TF2Message.ActionMessage(99, (EntityLivingBase) event.getEntity()));
			}
		}
	}
	@SubscribeEvent
	public void entityConstructing(EntityEvent.EntityConstructing event){
		

		if(event.getEntity() instanceof EntityLivingBase){
			event.getEntity().getDataManager().register(ENTITY_UBER, false);
			event.getEntity().getDataManager().register(ENTITY_OVERHEAL, 0f);
			
			if(event.getEntity() instanceof EntityTF2Character||event.getEntity() instanceof EntityPlayer){
				event.getEntity().getDataManager().register(ENTITY_INVIS, false);
				event.getEntity().getDataManager().register(ENTITY_DISGUISED, false);
				event.getEntity().getDataManager().register(ENTITY_DISGUISE_TYPE, "");
				event.getEntity().getDataManager().register(ENTITY_EXP_JUMP, false);
				event.getEntity().getDataManager().register(ENTITY_HEAL_TARGET, -1);
			}
		}
	}
	/*@SubscribeEvent
	public void ChunkLoad(ChunkEvent.Load event){
		if(!event.getWorld().isRemote){
			List<EntityTeleporter> teleporter=event.getWorld().getEntitiesWithinAABB(EntityTeleporter.class, new AxisAligned()));
		}
	}*/
	@SubscribeEvent
	public void cleanPlayer(PlayerLoggedOutEvent event){
		ItemUsable.lastDamage.remove(event.player);
	}
	@SubscribeEvent
	public void loadPlayer(PlayerLoggedInEvent event){
		//System.out.println("LoggedIn");
		if(TF2weapons.server.isDedicatedServer()||Minecraft.getMinecraft().getIntegratedServer().getPublic()){
			for(WeaponData weapon:MapList.nameToData.values()){
				TF2weapons.network.sendTo(new TF2Message.WeaponDataMessage(weapon), (EntityPlayerMP) event.player);
			}
		}
		/*TF2weapons.network.sendToAllAround(new TF2Message.WearableChangeMessage(event.player, 0, event.player.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(0)), TF2weapons.pointFromEntity(event.player));
		TF2weapons.network.sendToAllAround(new TF2Message.WearableChangeMessage(event.player, 1, event.player.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(1)), TF2weapons.pointFromEntity(event.player));
		TF2weapons.network.sendToAllAround(new TF2Message.WearableChangeMessage(event.player, 2, event.player.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(2)), TF2weapons.pointFromEntity(event.player));*/
	}
	/*@SubscribeEvent
	public void onConnect(ServerConnectionFromClientEvent event){
		new NetHandlerPlayServer(MinecraftServer.getServer(), event.manager, ((NetHandlerPlayServer)event.handler).playerEntity);
	}*/
	/*public static class PacketReceiveHack extends SimpleChannelInboundHandler<Packet>{

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
			System.out.println(msg);
		}
		
	}*/
	/*public static class MovePacketHack extends NetHandlerPlayServer{

		public MovePacketHack(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
			super(server, networkManagerIn, playerIn);
			// TODO Auto-generated constructor stub
		}
		public void processPlayer(C03PacketPlayer packetIn)
	    {
			System.out.println("send");
			super.processPlayer(packetIn);
	    }
	}*/
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickBlock event){
		if(event.getEntityPlayer().getActivePotionEffect(TF2weapons.stun)!=null){
			event.setCanceled(true);
		}
		if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
			event.setCanceled(true);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.RightClickItem event){
		ItemStack item=event.getEntityPlayer().getHeldItem(event.getHand());
		if(event.getEntityPlayer().getActivePotionEffect(TF2weapons.stun)!=null){
			event.setCanceled(true);
		}
		if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0&&!(item.getItem() instanceof ItemDisguiseKit)&&!(item.getItem() instanceof ItemCloak && item.getTagCompound().getBoolean("Active"))){
			event.setCanceled(true);
		}
		if(event.getEntity().getDataManager().get(ENTITY_DISGUISED)&&!(item.getItem() instanceof ItemFood||item.getItem() instanceof ItemCloak||item.getItem() instanceof ItemDisguiseKit)){
			disguise(event.getEntityPlayer(),false);
		}
		if(event.getEntityLiving().getActivePotionEffect(TF2weapons.bonk)!=null){
			event.setCanceled(true);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.EntityInteract event){
		
		if(event.getEntityPlayer().getActivePotionEffect(TF2weapons.stun)!=null){
			event.setCanceled(true);
		}
		if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
			event.setCanceled(true);
		}
		if(event.getEntity().getDataManager().get(ENTITY_DISGUISED)&&!(event.getEntityPlayer().getHeldItem(event.getHand()).getItem() instanceof ItemFood)){
			disguise(event.getEntityPlayer(),false);
		}
		if(event.getEntityLiving().getActivePotionEffect(TF2weapons.bonk)!=null){
			event.setCanceled(true);
		}
	}
	@SubscribeEvent
	public void stopUsing(PlayerInteractEvent.LeftClickBlock event){
		if(event.getEntityPlayer().getActivePotionEffect(TF2weapons.stun)!=null){
			event.setCanceled(true);
		}
		if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
			event.setCanceled(true);
		}
		event.getEntityPlayer().removePotionEffect(TF2weapons.charging);
	}
	@SubscribeEvent
	public void stopJump(LivingEvent.LivingJumpEvent event){
		
		if(event.getEntityLiving().getActivePotionEffect(TF2weapons.stun)!=null||event.getEntityLiving().getActivePotionEffect(TF2weapons.charging)!=null||(event.getEntityLiving().getHeldItemMainhand()!=null&&event.getEntityLiving().getHeldItemMainhand().getItem() instanceof ItemMinigun&&event.getEntityLiving().getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks>0)){
			event.getEntityLiving().isAirBorne=false;
			event.getEntityLiving().motionY-=0.5f;
			if(event.getEntityLiving().isSprinting()){
				float f = event.getEntityLiving().rotationYaw * 0.017453292F;
				event.getEntityLiving().motionX += (double)(MathHelper.sin(f) * 0.2F);
				event.getEntityLiving().motionZ -= (double)(MathHelper.cos(f) * 0.2F);
			}
		}
		
	}
	public static void disguise(EntityLivingBase entity, boolean active){
		entity.getDataManager().set(ENTITY_DISGUISED, active);
		entity.getCapability(TF2weapons.WEAPONS_CAP, null).disguiseTicks=0;
		//System.out.println("disguised: "+active);
		/*if(!entity.worldObj.isRemote){
			TF2weapons.network.sendToDimension(new TF2Message.PropertyMessage("Disguised", (byte)(active?1:0),entity),entity.dimension);
		}*/
		//ItemCloak.setInvisiblity(entity);
		/*if(entity.worldObj instanceof WorldServer && active){
			if(active){
				EntityCreeper creeper=new EntityCreeper(entity.worldObj);
				creeper.tasks.taskEntries.clear();
				creeper.targetTasks.taskEntries.clear();
				fakeEntities.put(entity, creeper);
				creeper.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				entity.worldObj.spawnEntityInWorld(creeper);
				//((WorldServer)event.getEntity().worldObj).getEntityTracker().untrackEntity(event.getEntity());
			}
			else{
				EntityLivingBase ent=fakeEntities.remove(entity);
				if(ent!=null){
					ent.setDead();
				}
			}
		}
		if(entity.worldObj.isRemote){
			if(active){
				EntityCreeper creeper=new EntityCreeper(entity.worldObj);
				creeper.tasks.taskEntries.clear();
				creeper.targetTasks.taskEntries.clear();
				fakeEntities.put(entity, creeper);
				creeper.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
				//((WorldServer)event.getEntity().worldObj).getEntityTracker().untrackEntity(event.getEntity());
				System.out.println("Disguise");
			}
			else{
				EntityLivingBase ent=fakeEntities.remove(entity);
				if(ent!=null){
					ent.setDead();
				}
			}
		}*/
	}
	@SubscribeEvent
	public void livingUpdate(final LivingEvent.LivingUpdateEvent event){
		final EntityLivingBase living=event.getEntityLiving();
		if(event.getEntity().isEntityAlive()&&(event.getEntity().hasCapability(TF2weapons.WEAPONS_CAP, null))){
			
			final WeaponsCapability cap=event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null);
			cap.tick();
			
			if(event.getEntity().getDataManager().get(ENTITY_EXP_JUMP)){
				if(event.getEntity().onGround){
					event.getEntity().getDataManager().set(ENTITY_EXP_JUMP, false);
				}
				event.getEntity().motionX=event.getEntity().motionX*1.04;
				event.getEntity().motionZ=event.getEntity().motionZ*1.04;
			}
			if(cap.doubleJumped && living.onGround){
				cap.doubleJumped=false;
			}
			/*if(event.getEntity() instanceof EntityPlayer){
				System.out.println("Invisible: "+event.getEntity().isInvisible());
			}*/
			if(!event.getEntity().worldObj.isRemote&&cap.disguiseTicks>0){
				//System.out.println("disguise progress: "+event.getEntity().getEntityData().getByte("DisguiseTicks"));
				if(++cap.disguiseTicks>=40){
					disguise(living,true);
					
				}
			}
			/*if(event.getEntity().worldObj.isRemote && event.getEntity().getEntityData().getByte("Disguised")==1&&(fakeEntities.get(living)==null||fakeEntities.get(living).isDead)){
				disguise(living,true);
			}*/
			PotionEffect effect=living.getActivePotionEffect(TF2weapons.charging);
			if(living.worldObj.isRemote){
				ClientProxy.doChargeTick(living);
			}
			if(!living.worldObj.isRemote && effect!=null){
				if(ItemChargingTarge.getChargingShield(living)==null){
					living.removePotionEffect(TF2weapons.charging);
				}
				Vec3d start=living.getPositionVector().addVector(0, living.height/2, 0);
				Vec3d end=start.addVector(-MathHelper.sin(living.rotationYaw / 180.0F * (float)Math.PI)*0.7, 0, MathHelper.cos(living.rotationYaw / 180.0F * (float)Math.PI)*0.7);
				//Vec3d end=start.addVector(living.motionX*10,0,living.motionZ*10);
				//System.out.println("yay: "+living.motionX+" "+living.motionZ);
				RayTraceResult result=TF2weapons.pierce(living.worldObj,living,start.xCoord, start.yCoord, start.zCoord,end.xCoord, end.yCoord, end.zCoord, false, 0.5f, false).get(0);
				if(result.entityHit!=null){
					float damage=5;
					if(effect.getDuration()>30){
						damage*=0.5f;
					}
					TF2weapons.dealDamage(result.entityHit, result.entityHit.worldObj, living, ItemChargingTarge.getChargingShield(living), 0, damage, TF2weapons.causeDirectDamage(ItemChargingTarge.getChargingShield(living), living, 0));
					cap.bashCritical=effect.getDuration()<20;
					cap.ticksBash=20;
					living.motionX=0;
					living.motionZ=0;
					living.removePotionEffect(TF2weapons.charging);
					
				}
			}
			if(living.worldObj.isRemote&&!living.getDataManager().get(ENTITY_DISGUISE_TYPE).equals(cap.lastDisguiseValue)&&living.getDataManager().get(ENTITY_DISGUISE_TYPE).startsWith("P:")){
					cap.lastDisguiseValue=living.getDataManager().get(ENTITY_DISGUISE_TYPE);
					cap.skinDisguise=null;
					cap.skinType=DefaultPlayerSkin.getSkinType(living.getUniqueID());
					THREAD_POOL.submit(new Runnable(){

						@Override
						public void run() {
							GameProfile profile=TileEntitySkull.updateGameprofile(new GameProfile(null,living.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISE_TYPE).substring(2)));
							if(profile.getId() != null){
								cap.skinType=DefaultPlayerSkin.getSkinType(profile.getId());
							}
							Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, new SkinManager.SkinAvailableCallback()
			                {
			                    public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture)
			                    {
			                    	if(typeIn==Type.SKIN){
			                    		if(typeIn==Type.SKIN){
											cap.skinDisguise=location;
										}
		                                cap.skinType=profileTexture.getMetadata("model");

		                                if (cap.skinType == null)
		                                {
		                                    cap.skinType = "default";
		                                }
			                        }
			                    }
			                }, false);
						}
						
					});
					
					/*Minecraft.getMinecraft().getSkinManager().loadSkin(new MinecraftProfileTexture("http://skins.minecraft.net/MinecraftSkins/"+living.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISE_TYPE).substring(2)+".png",null), Type.SKIN,new SkinAvailableCallback(){
						@Override
						public void skinAvailable(Type typeIn, ResourceLocation location,
								MinecraftProfileTexture profileTexture) {
							if(typeIn==Type.SKIN){
								cap.skinDisguise=location;
								System.out.println("RetrieveD");
							}
						}
						
					});*/
			}
			if(!living.worldObj.isRemote&&living.fallDistance>0&&living.getItemStackFromSlot(EntityEquipmentSlot.FEET)!=null&&living.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()==TF2weapons.itemMantreads){
				//System.out.println("Distance: "+living.fallDistance);
				for(EntityLivingBase target:event.getEntity().worldObj.getEntitiesWithinAABB(EntityLivingBase.class, living.getEntityBoundingBox().expand(0.2f, living.motionY, 0.2f), new Predicate<EntityLivingBase>(){

					@Override
					public boolean apply(EntityLivingBase input) {
						// TODO Auto-generated method stub
						return input!=living && !TF2weapons.isOnSameTeam(input, living);
					}
					
				})){
					
					float damage=Math.max(0, living.fallDistance-3)*1.7f;
					living.fallDistance=0;
					if(damage>0){
						target.attackEntityFrom(new EntityDamageSource("fallpl",living), damage);
						TF2weapons.playSound(living,TF2Sounds.WEAPON_MANTREADS, 1.5F, 1F);
					}
				}
			}
			if(event.getEntity().getDataManager().get(ENTITY_INVIS)){
				//System.out.println("cloak");
				boolean visible=living.hurtTime==10;
				if(!visible){
					List<Entity> closeEntities=event.getEntity().worldObj.getEntitiesInAABBexcluding(event.getEntity(), event.getEntity().getEntityBoundingBox().expand(1, 2, 1),new Predicate<Entity>(){
	
						@Override
						public boolean apply(Entity input) {
							// TODO Auto-generated method stub
							return input instanceof EntityLivingBase && !TF2weapons.isOnSameTeam(event.getEntity(), input);
						}
	
						
					});
					for(Entity ent: closeEntities){
						if(ent.getDistanceSqToEntity(event.getEntity())<1){
							visible=true;
						}
						break;
					}
				}
				if(visible){
					//System.out.println("reveal");
					cap.invisTicks=Math.min(10,cap.invisTicks);
					event.getEntity().setInvisible(false);
				}
				if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks<20){
					
					cap.invisTicks=Math.min(20,cap.invisTicks+2);
					
				}
				else {
					if(!event.getEntity().isInvisible()){
						//System.out.println("full");
						event.getEntity().setInvisible(true);
					}
					
				}
				boolean active=event.getEntity().worldObj.isRemote || ItemCloak.searchForWatches(living)!=null;
				if(!active){
					event.getEntity().getDataManager().set(ENTITY_INVIS,false);
					event.getEntity().setInvisible(false);
					//System.out.println("decloak");
				}
			}
			else{
				if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>0){
					cap.invisTicks--;
					if(event.getEntity().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks==0){
						event.getEntity().setInvisible(false);
					}
				}
			}
		}
		
		if(living.getAITarget()!=null&&living.getAITarget().hasCapability(TF2weapons.WEAPONS_CAP, null)&&living.getAITarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>=20){
			living.setRevengeTarget(null);
		}
		
		if(living instanceof EntityLiving&&((EntityLiving) event.getEntity()).getAttackTarget()!=null&&((EntityLiving) event.getEntity()).getAttackTarget().hasCapability(TF2weapons.WEAPONS_CAP, null)&&((EntityLiving) event.getEntity()).getAttackTarget().getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks>=20){
			((EntityLiving)event.getEntity()).setAttackTarget(null);
		}
		
		if(event.getEntity().getDataManager().get(ENTITY_OVERHEAL)==-1){
			living.getEntityData().setFloat("Overheal",0f);
			living.setAbsorptionAmount(0);
		}
		if(event.getEntity().getDataManager().get(ENTITY_OVERHEAL)>0){
			if(living.worldObj.isRemote){
				living.setAbsorptionAmount(living.getDataManager().get(ENTITY_OVERHEAL));
			}
			living.setAbsorptionAmount(living.getAbsorptionAmount()-living.getMaxHealth()*0.001666f);
			if(!living.worldObj.isRemote){
				
				if(living.getAbsorptionAmount()<=0){
					living.getEntityData().setFloat("Overheal",-1f);
				}
				else{
					living.getEntityData().setFloat("Overheal",living.getAbsorptionAmount());
				}
			}
		}
		
		if(event.getEntity().getDataManager().get(ENTITY_UBER)&&!(living.getHeldItem(EnumHand.MAIN_HAND) != null && living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemMedigun && living.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Activated"))){
			List<EntityLivingBase> list=living.worldObj.getEntitiesWithinAABB(
				EntityLivingBase.class,new AxisAlignedBB(living.posX-8, 
				living.posY-8, living.posZ-8, living.posX+8, 
				living.posY+8, living.posZ+8),new Predicate<EntityLivingBase>(){

					@Override
					public boolean apply(EntityLivingBase input) {
						// TODO Auto-generated method stub
						return input.worldObj.getEntityByID(input.getCapability(TF2weapons.WEAPONS_CAP, null)!=null?input.getCapability(TF2weapons.WEAPONS_CAP, null).healTarget:-1)==living&&input.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getBoolean("Activated");
					}
					
				});
			boolean isOK=!list.isEmpty();
			if(!isOK){
				event.getEntity().getEntityData().setBoolean("Ubercharge",false);
			}
		}
	}
	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event){
		if(!event.getWorld().isRemote&&event.getWorld().getScoreboard().getTeam("RED")==null){
			ScorePlayerTeam teamRed=event.getWorld().getScoreboard().createTeam("RED");
			ScorePlayerTeam teamBlu=event.getWorld().getScoreboard().createTeam("BLU");
			
			teamRed.setSeeFriendlyInvisiblesEnabled(true);
			teamRed.setAllowFriendlyFire(false);
			teamBlu.setSeeFriendlyInvisiblesEnabled(true);
			teamBlu.setAllowFriendlyFire(false);
			
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamRed);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamBlu);
			
		}
		if(!event.getWorld().isRemote&&event.getWorld().getScoreboard().getTeam("TF2Bosses")==null){
			ScorePlayerTeam teamBosses=event.getWorld().getScoreboard().createTeam("TF2Bosses");
			teamBosses.setSeeFriendlyInvisiblesEnabled(true);
			teamBosses.setAllowFriendlyFire(false);
			event.getWorld().getScoreboard().broadcastTeamInfoUpdate(teamBosses);
		}

		
	}
	@SubscribeEvent
	public void medicSpawn(LivingSpawnEvent.SpecialSpawn event){
		float chance=0;
		if(event.getEntity() instanceof EntityHeavy){
			chance=0.16f;
		}
		else if(event.getEntity() instanceof EntitySoldier){
			chance=0.08f;
		}
		else if(event.getEntity() instanceof EntityDemoman){
			chance=0.07f;
		}
		else if(event.getEntity() instanceof EntityPyro){
			chance=0.06f;
		}
		else{
			return;
		}
		if(event.getWorld().rand.nextFloat()<event.getWorld().getDifficulty().getDifficultyId()*chance){
			EntityMedic medic=new EntityMedic(event.getWorld());
			medic.setLocationAndAngles(event.getEntity().posX+event.getWorld().rand.nextDouble()*0.5-0.25, event.getEntity().posY, event.getEntity().posZ+event.getWorld().rand.nextDouble()*0.5-0.25,event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
			medic.natural=true;
			//medic.setEntTeam(event.getWorld().rand.nextInt(2));
			medic.onInitialSpawn(event.getWorld().getDifficultyForLocation(new BlockPos(event.getX(),event.getY(),event.getZ())),null);
			EntityTF2Character.nextEntTeam=medic.getEntTeam();
			
			event.getWorld().spawnEntityInWorld(medic);
		}
	}
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent.Entity event){
		if(event.getEntity() instanceof EntityPlayer || event.getEntity() instanceof EntityTF2Character){
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID,"weaponcapability"), new WeaponsCapability((EntityLivingBase) event.getEntity()));
		}
		if(event.getEntity() instanceof EntityPlayer&&!event.getEntity().hasCapability(TF2weapons.INVENTORY_CAP,null)){
			event.addCapability(new ResourceLocation(TF2weapons.MOD_ID,"wearablescapability"), new InventoryWearables());
		}
	}
	@SubscribeEvent
	public void livingDeath(LivingDeathEvent event){
		if(!event.getEntity().worldObj.isRemote && event.getSource()!=null&&event.getSource().getEntity()!=null&&event.getSource().getEntity() instanceof EntityLivingBase){
			ItemStack stack = null;
			EntityLivingBase living=(EntityLivingBase) event.getSource().getEntity();
			if(event.getSource() instanceof TF2DamageSource){
				stack=((TF2DamageSource)event.getSource()).getWeapon();
			}
			else{
				stack=living.getHeldItemMainhand();
			}
			if(stack!=null&&stack.hasTagCompound()&&stack.getTagCompound().getBoolean("Strange") && stack.getItem() instanceof ItemWeapon){
				if(!(event.getEntityLiving() instanceof EntityPlayer)){
					stack.getTagCompound().setInteger("Kills", stack.getTagCompound().getInteger("Kills")+1);
				}
				else{
					stack.getTagCompound().setInteger("PlayerKills", stack.getTagCompound().getInteger("PlayerKills")+1);
				}
				onStrangeUpdate(stack,living);
				if(stack.getTagCompound().getBoolean("Australium")){
					TF2weapons.network.sendToAllAround(new TF2Message.ActionMessage(19, event.getEntityLiving()), TF2weapons.pointFromEntity(event.getEntity()));
					event.getEntity().playSound(TF2Sounds.WEAPON_TO_GOLD, 1.5f, 2f);
					event.getEntityLiving().deathTime=20;
					event.getEntityLiving().onEntityUpdate();
					if(!(event.getEntity() instanceof EntityPlayer))
						event.getEntity().setSilent(true);
				}
			}
			if(stack != null && stack.getItem() instanceof ItemWeapon){
				if(living.hasCapability(TF2weapons.WEAPONS_CAP, null) && TF2Attribute.getModifier("Kill Count",stack,0,living)!=0){
					living.getCapability(TF2weapons.WEAPONS_CAP, null).addHead();
				}
				float toHeal=TF2Attribute.getModifier("Health Kill", stack, 0, living);
				if(toHeal!=0){
					living.heal(toHeal);
				}
				if(TF2Attribute.getModifier("Crit Kill", stack, 0, living)>0){
					living.addPotionEffect(new PotionEffect(TF2weapons.critBoost,(int) TF2Attribute.getModifier("Crit Kill", stack, 0, living)*20,1));
				}
			}
		}
		if(event.getEntityLiving() instanceof EntityPlayer &&!event.getEntity().worldObj.isRemote){
			InventoryWearables inv=event.getEntityLiving().getCapability(TF2weapons.INVENTORY_CAP, null);
			for(int i=3;i<13;i++){
				if(inv.getStackInSlot(i)!=null)
					event.getEntityLiving().entityDropItem(inv.getStackInSlot(i), 0.5f);
			}
		}
	}
	@SubscribeEvent
	public void generateOres(OreGenEvent.Post event){
		if(event.getWorld().provider.getDimension()==0){
			if(TF2weapons.generateCopper){
				generateOre(TF2weapons.blockCopperOre.getDefaultState(), 7,7,32,80,event.getWorld(),event.getRand(),event.getPos());
				generateOre(TF2weapons.blockCopperOre.getDefaultState(), 7,1,0,32,event.getWorld(),event.getRand(),event.getPos());
			}
			if(TF2weapons.generateLead){
				generateOre(TF2weapons.blockLeadOre.getDefaultState(), 5,6,24,74,event.getWorld(),event.getRand(),event.getPos());
				generateOre(TF2weapons.blockLeadOre.getDefaultState(), 5,1,0,24,event.getWorld(),event.getRand(),event.getPos());
			}
			if(TF2weapons.generateAustralium)
				generateOre(TF2weapons.blockAustraliumOre.getDefaultState(), 3,2,0,24,event.getWorld(),event.getRand(),event.getPos());
		}
	}
	public void generateOre(IBlockState state,int size, int count, int minY, int maxY,World world, Random random,BlockPos chunkPos){
		for(int i=0;i<count;i++){
			BlockPos pos=chunkPos.add(random.nextInt(16), minY+random.nextInt(maxY-minY), random.nextInt(16));
			new WorldGenMinable(state, size).generate(world, random, pos);
		}
	}
	@SubscribeEvent
	public void pickAmmo(EntityItemPickupEvent event){
		ItemStack stack=event.getItem().getEntityItem();
		if(stack.getItem() instanceof ItemAmmo && event.getEntityLiving().hasCapability(TF2weapons.INVENTORY_CAP, null)){
			IInventory inv=event.getEntityLiving().getCapability(TF2weapons.INVENTORY_CAP, null);
			if(inv.getStackInSlot(3)!=null){
				for(int i=4;i<inv.getSizeInventory();i++){
					ItemStack inSlot=inv.getStackInSlot(i);
					if(inSlot==null){
						inv.setInventorySlotContents(i, stack.copy());
						stack.stackSize=0;
					}
					else if(stack.isItemEqual(inSlot)&&ItemStack.areItemStackTagsEqual(stack, inSlot)){
						int size=stack.stackSize+inSlot.stackSize;
						
						if(size>stack.getMaxStackSize()){
							stack.stackSize=size-inSlot.getMaxStackSize();
							inSlot.stackSize=stack.getMaxStackSize();
						}
						else{
							inSlot.stackSize=size;
							stack.stackSize=0;
						}
					}
					if(stack.stackSize<=0){
						break;
					}
				}
			}
			if(stack.stackSize<=0){
				event.setResult(Result.ALLOW);
				return;
			}
		}
		event.setResult(Result.DEFAULT);
	}
	@SubscribeEvent
	public void addTooltip(ItemTooltipEvent event){
		if(event.getItemStack().hasTagCompound() && event.getItemStack().getTagCompound().getBoolean("Australium") && !(event.getItemStack().getItem() instanceof ItemFromData) && !event.getItemStack().hasDisplayName()){
			event.getToolTip().set(0, "Australium "+event.getToolTip().get(0));
		}
		if(event.getItemStack().hasTagCompound()&&event.getItemStack().getTagCompound().getBoolean("Strange")){
			if(!(event.getItemStack().getItem() instanceof ItemFromData) && !event.getItemStack().hasDisplayName())
			event.getToolTip().set(0, STRANGE_TITLES[event.getItemStack().getTagCompound().getInteger("StrangeLevel")]+" "+event.getToolTip().get(0));
			
			event.getToolTip().add("");
			if(event.getItemStack().getItem() instanceof ItemMedigun){
				event.getToolTip().add("Ubercharges: "+event.getItemStack().getTagCompound().getInteger("Ubercharges"));
			}
			else if(event.getItemStack().getItem() instanceof ItemCloak){
				event.getToolTip().add("Seconds cloaked: "+event.getItemStack().getTagCompound().getInteger("CloakTicks")/20);
			}
			else{
				event.getToolTip().add("Mob kills: "+event.getItemStack().getTagCompound().getInteger("Kills"));
				event.getToolTip().add("Player kills: "+event.getItemStack().getTagCompound().getShort("PlayerKills"));
			}
		}
	}
	public static void onStrangeUpdate(ItemStack stack, EntityLivingBase player){
		int points=0;
		if(stack.getItem() instanceof ItemMedigun){
			points=stack.getTagCompound().getInteger("Ubercharges");
		}
		else if(stack.getItem() instanceof ItemCloak){
			points=stack.getTagCompound().getInteger("CloakTicks")/400;
		}
		else{
			points=stack.getTagCompound().getInteger("Kills");
			points+=stack.getTagCompound().getInteger("PlayerKills")*5;
		}
		int calculatedLevel=0;
		
		if(points>=STRANGE_KILLS[STRANGE_KILLS.length-1])
			calculatedLevel=STRANGE_KILLS.length-1;
		else{
			for(int i=1;i<STRANGE_KILLS.length;i++){
				if(points<STRANGE_KILLS[i]){
					calculatedLevel=i-1;
					break;
				}
			}
		}
		
		if(calculatedLevel>stack.getTagCompound().getInteger("StrangeLevel")){
			stack.getTagCompound().setInteger("StrangeLevel",calculatedLevel);
			final int level=calculatedLevel;
			if(player instanceof EntityPlayerMP){
				((EntityPlayerMP)player).addStat(new Achievement(Integer.toString(player.getRNG().nextInt()), "strangeUp", 0, 0, stack, null){
					public ITextComponent getStatName()
				    {
						return super.getStatName().appendText(STRANGE_TITLES[level]);
				    }
				});
			}
		}
	}
	public static class DestroyBlockEntry{
		public BlockPos pos;
		public float curDamage;
		public World world;
		
		public DestroyBlockEntry(BlockPos pos, World world){
			this.world=world;
			this.pos=pos;
		}
	}
	
}
