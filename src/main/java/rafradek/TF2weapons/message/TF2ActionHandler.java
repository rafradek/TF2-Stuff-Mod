package rafradek.TF2weapons.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.network.play.server.SPacketUpdateBossInfo.Operation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoLerping;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.characters.EntityStatue;
import rafradek.TF2weapons.weapons.ItemUsable;
import rafradek.TF2weapons.weapons.WeaponLoopSound;
import rafradek.TF2weapons.weapons.WeaponsCapability;

public class TF2ActionHandler implements IMessageHandler<TF2Message.ActionMessage, IMessage> {
	
	/*public static Map<EntityLivingBase,Integer> playerAction=new HashMap<EntityLivingBase,Integer>();
	public static Map<EntityLivingBase,Integer> playerActionClient=new HashMap<EntityLivingBase,Integer>();*/
	//public static ThreadLocalMap<EntityLivingBase,Integer> playerAction=new ThreadLocalMap<EntityLivingBase,Integer>();
	//public static ThreadLocalMap<EntityLivingBase,Integer> previousPlayerAction=new ThreadLocalMap<EntityLivingBase,Integer>();
	@Override
	public IMessage onMessage(final TF2Message.ActionMessage message, final MessageContext ctx) {
		if(ctx.side==Side.SERVER){
			final EntityPlayerMP player=ctx.getServerHandler().playerEntity;
			((WorldServer)player.worldObj).addScheduledTask(new Runnable(){
				
				@Override
				public void run() {
					if(message.value<=15){
						handleMessage(message, player,false);
						message.entity=player.getEntityId();
						TF2weapons.network.sendToDimension(message, player.dimension);
					}
					else if(message.value==99){
						Entity wearer=ctx.getServerHandler().playerEntity.worldObj.getEntityByID(message.entity);
						//System.out.println("ID: "+message.entity+" "+wearer);
						if(wearer==null||!(wearer instanceof EntityPlayer)){
							wearer=player;
						}
						TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) wearer, 0, wearer.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(0)), player);
						TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) wearer, 1, wearer.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(1)), player);
						TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) wearer, 2, wearer.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(2)), player);
						TF2weapons.network.sendTo(new TF2Message.WearableChangeMessage((EntityPlayer) wearer, 3, wearer.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(3)), player);
					}
					else if(message.value==16){
						player.worldObj.getScoreboard().addPlayerToTeam(player.getName(), "RED");
					}
					else if(message.value==17){
						player.worldObj.getScoreboard().addPlayerToTeam(player.getName(), "BLU");
					}
					else if(message.value==23){
						player.fallDistance=0;
					}
				}
				
			});
		}
		else{
			final EntityLivingBase player=(EntityLivingBase) Minecraft.getMinecraft().theWorld.getEntityByID(message.entity);
			Minecraft.getMinecraft().addScheduledTask(new Runnable(){

				@Override
				public void run() {
					if(message.value<=15){
						handleMessage(message, player,true);
					}
					else if(message.value==19){
						if(player != null){
							player.setDead();
							player.worldObj.spawnEntityInWorld(new EntityStatue(player.worldObj,player));
						}
					}
					else if(message.value==22){
						if(player !=null && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().hasTagCompound()){
							player.getHeldItemMainhand().getTagCompound().setByte("active",(byte) 2);
						}
					}
				}
				
			});
		}
		return null;
	}
	/*public static class TF2ActionHandlerReturn implements IMessageHandler<TF2Message.ActionMessage, IMessage> {

		@Override
		public IMessage onMessage(TF2Message.ActionMessage message, MessageContext ctx) {
			EntityLivingBase player=(EntityLivingBase) Minecraft.getMinecraft().theWorld.getEntityByID(message.entity);
			handleMessage(message, player);
			return null;
		}

	}*/
	public static void handleMessage(TF2Message.ActionMessage message,EntityLivingBase player,boolean client){
		if(player!=null){
			/*int oldValue=playerAction.get().containsKey(player)?playerAction.get().get(player):0;
			if(player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemUsable){
				if((oldValue&1)==0&&(message.value&1)!=0){
					((ItemUsable)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).startUse(player.getHeldItem(EnumHand.MAIN_HAND), player, player.worldObj);
				}
				if((oldValue&1)==0&&(message.value&1)!=0){
					((ItemUsable)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).endUse(player.getHeldItem(EnumHand.MAIN_HAND), player, player.worldObj);
				}
			}*/
			/*if(previousPlayerAction.get(player.worldObj.isRemote).containsKey(player)){
				previousPlayerAction.get(player.worldObj.isRemote).put(player, 0);
			}
			int oldState=previousPlayerAction.get(player.worldObj.isRemote).get(player);
			
			previousPlayerAction.get(player.worldObj.isRemote).put(player, playerAction.get(true).get(player));*/
			
			WeaponsCapability cap= player.getCapability(TF2weapons.WEAPONS_CAP, null);
			ItemStack stack=player.getHeldItem(EnumHand.MAIN_HAND);
			int oldState=cap.state&3;
			
			cap.state=message.value+(cap.state&8);
			
			if(stack != null&&stack.getItem() instanceof ItemUsable&&oldState!=(message.value&3)&&stack.getTagCompound().getByte("active")==2){
				if((oldState&2)<(message.value&2)){
					((ItemUsable)stack.getItem()).startUse(stack, player, player.worldObj, oldState, message.value&3);
					cap.stateDo(player, stack);
				}
				else if((oldState&2)>(message.value&2)){
					((ItemUsable)stack.getItem()).endUse(stack, player, player.worldObj, oldState, message.value&3);
				}
				if((oldState&1)<(message.value&1)){
					((ItemUsable)stack.getItem()).startUse(stack, player, player.worldObj, oldState, message.value&3);
					cap.stateDo(player, stack);
				}
				else if((oldState&1)>(message.value&1)){
					((ItemUsable)stack.getItem()).endUse(stack, player, player.worldObj, oldState, message.value&3);
				}
			}
			//System.out.println("change "+playerAction.get(player.worldObj.isRemote).get(player));
			//System.out.println("dostal: "+message.value);
		}
	}
	public static ArrayList<EnumAction> value=new ArrayList<EnumAction>();
	public static enum EnumAction{
		
		IDLE,
		FIRE,
		ALTFIRE,
		RELOAD;

		private EnumAction(){
			value.add(this);
		}
		
		/*public boolean leftClick(){
			return this==FIRE||this==DOUBLE;
		}
		public boolean rightClick(){
			return this==ALTFIRE||this==DOUBLE;
		}*/
	}
}
