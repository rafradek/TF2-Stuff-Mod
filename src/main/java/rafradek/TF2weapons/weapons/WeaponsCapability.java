package rafradek.TF2weapons.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.message.TF2Message;

public class WeaponsCapability implements ICapabilityProvider, INBTSerializable<NBTTagCompound>{
	
	public static final UUID HEADS_HEALTH=UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785FC3A");
	public static final UUID HEADS_SPEED=UUID.fromString("FA233E1C-4180-4865-B01B-B4A79785FC3A");
	public MovementInput lastMovementInput;
	public EntityLivingBase owner;
	public int state;
	public int minigunTicks;
	public int fire1Cool;
	public int fire2Cool;
	public int reloadCool;
	public int lastFire;
	public int critTime;
	public int healTarget=-1;
	public boolean mainHand;
	public HashMap<String,Integer> effectsCool=new HashMap<String, Integer>();
	public int chargeTicks;
	public boolean charging;
	public int critTimeCool;
	public ArrayList<TF2Message.PredictionMessage> predictionList=new ArrayList<TF2Message.PredictionMessage>();
	public float recoil;
	public int invisTicks;
	public int disguiseTicks;
	public int buffDuration;
	public boolean pressedStart;
	public int active;
	public boolean doubleJumped;
	public EntitySentry controlledSentry;
	public ResourceLocation skinDisguise;
	public boolean skinRetrieved;
	public String lastDisguiseValue;
	public String skinType;
	public int zombieHuntTicks;
	public int ticksBash;
	public boolean bashCritical;
	public int collectedHeads;
	public int collectedHeadsTime;
	public boolean wornEye;
	public AttributeModifier headsHealthMod=new AttributeModifier(HEADS_HEALTH,"Heads modifier",0,0);
	public AttributeModifier headsSpeedMod=new AttributeModifier(HEADS_SPEED,"Heads modifier",0,2);
	public WeaponsCapability(EntityLivingBase entity) {
		this.owner=entity;
	}

	public void addHead(){
		
		this.collectedHeadsTime=owner.ticksExisted;
		if(this.collectedHeads<9){
			this.collectedHeads++;
		}
		this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HEADS_HEALTH,"Heads modifier",collectedHeads,0));
		this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier(HEADS_SPEED,"Heads modifier",collectedHeads*0.04,2));
		this.owner.heal(1);
	}
	public void tick(){
		//System.out.println("graczin"+state);
		Iterator<Entry<String, Integer>> iterator=effectsCool.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String,Integer> entry=iterator.next();
			entry.setValue(entry.getValue()-1);
			if(entry.getValue()<=0){
				iterator.remove();
			}
		}

		if(collectedHeads>0 && collectedHeadsTime<this.owner.ticksExisted-1800){
			collectedHeads--;
			collectedHeadsTime=this.owner.ticksExisted;
			this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier(HEADS_HEALTH,"Heads modifier",collectedHeads,0));
			this.owner.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier(HEADS_SPEED,"Heads modifier",collectedHeads*0.04,2));
		}
		ItemStack stack = owner.getHeldItem(EnumHand.MAIN_HAND);
		/*if(itemProperties.get(client).get(player)==null){
			itemProperties.get(client).put(player, new NBTTagCompound());
		}*/
		//player.getEntityData().setTag("TF2", tag);
		this.zombieHuntTicks--;
		this.ticksBash--;
		this.lastFire-=50;
		if (stack != null&&stack.getItem() instanceof ItemUsable)
        {
			ItemUsable item=(ItemUsable) stack.getItem();
			
			//if(!client)
				//System.out.println(client+" rel " +item.getTagCompound().getShort("reload")+" "+state+" "+item.getDisplayName());
			if (this.reloadCool > 0)
            {
                this.reloadCool-=50;
            }
			if (this.fire1Cool > 0)
            {
				this.fire1Cool-=50;
            }
			if (this.fire2Cool > 0)
            {
				this.fire2Cool-=50;
            }
			if (this.fire1Cool <= 0 && stack.getTagCompound().getByte("active")==1){
				stack.getTagCompound().setByte("active", (byte) 2);
				if(item.isDoubleWielding(owner)){
					owner.getHeldItemOffhand().getTagCompound().setByte("active", (byte) 2);
				}
				item.draw(this,stack,owner,owner.worldObj);
	    		
	    		/*if(par3Entity instanceof EntityPlayerMP){
	    			TF2weapons.network.sendTo(new TF2Message.ActionMessage(22,(EntityLivingBase) par3Entity), (EntityPlayerMP)par3Entity);
	    		}*/
	    		
	    		if((this.state&3)>0){
	    			this.state=this.state&7;
					if((this.state&3)>0){
						item.startUse(stack,owner,owner.worldObj, 0, this.state&3);
					}
				}
			}
			if(owner.worldObj.isRemote){
				//((EntityPlayerMP)player).isChangingQuantityOnly=state>0;
				stack.animationsToGo=0;
			}
			if(owner instanceof EntityTF2Character){
				EntityTF2Character shooter=((EntityTF2Character)owner);
				if(shooter.getAttackTarget() != null){
					shooter.targetPrevPos[1]=shooter.targetPrevPos[0];
					shooter.targetPrevPos[3]=shooter.targetPrevPos[2];
					shooter.targetPrevPos[5]=shooter.targetPrevPos[4];
					shooter.targetPrevPos[0]=shooter.getAttackTarget().posX;
					shooter.targetPrevPos[2]=shooter.getAttackTarget().posY;
					shooter.targetPrevPos[4]=shooter.getAttackTarget().posZ;
				}
			}
			this.stateDo(owner, stack);
			if((!owner.worldObj.isRemote||owner!=Minecraft.getMinecraft().thePlayer)&&stack.getItem() instanceof ItemWeapon && ((ItemWeapon)stack.getItem()).hasClip(stack)&&
					(!(owner instanceof EntityPlayer)||ItemAmmo.searchForAmmo(owner, stack)!=null||owner.worldObj.isRemote)){
				if(((state&4)!=0||stack.getItemDamage()==stack.getMaxDamage())&&(state&8)==0&&stack.getItemDamage()!=0&& this.reloadCool<=0
						&&(this.fire1Cool<=0||((ItemWeapon)stack.getItem()).IsReloadingFullClip(stack))&&owner.getActivePotionEffect(TF2weapons.stun)==null){
					state+=8;
					this.reloadCool=((ItemWeapon)stack.getItem()).getWeaponFirstReloadTime(stack,owner);
					
					if(!owner.worldObj.isRemote&&owner instanceof EntityPlayerMP)
	                	TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), true,EnumHand.MAIN_HAND), (EntityPlayerMP) owner);
					
					if(owner.worldObj.isRemote&&((ItemWeapon)stack.getItem()).IsReloadingFullClip(stack)){
						TF2weapons.proxy.playReloadSound(owner,stack);
	                }
					
				}
				else if(this.fire1Cool<=0||((ItemWeapon)stack.getItem()).IsReloadingFullClip(stack)){
	                while ((state&8)!=0&&this.reloadCool <= 0 && stack.getItemDamage()!=0)
	                {
	                	//System.out.println("On client: "+owner.worldObj.isRemote);
	                	int maxAmmoUse=ItemAmmo.getAmmoAmount(owner, stack);
	                	int consumeAmount=0;
	                	
	                    if(((ItemWeapon)stack.getItem()).IsReloadingFullClip(stack)){
	                    	consumeAmount+=Math.min(stack.getItemDamage(),maxAmmoUse);
	                    	stack.setItemDamage(Math.max(0, stack.getItemDamage()-consumeAmount));
	                    	maxAmmoUse-=consumeAmount;
	                    	
	                    	if(maxAmmoUse>0&&item.isDoubleWielding(owner)){
	                    		consumeAmount+=Math.min(owner.getHeldItemOffhand().getItemDamage(),maxAmmoUse);
	                    		owner.getHeldItemOffhand().setItemDamage(Math.max(0, stack.getItemDamage()-consumeAmount));
	                    	}
	                    	
	                    } else {
	                    	consumeAmount=1;
	                    	stack.setItemDamage(stack.getItemDamage() - 1); 
	                    	TF2weapons.proxy.playReloadSound(owner,stack);
	                    }
	                    ItemAmmo.consumeAmmoGlobal(owner, stack, consumeAmount);
	                    if(!owner.worldObj.isRemote&&owner instanceof EntityPlayerMP)
	                    	TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), true,EnumHand.MAIN_HAND), (EntityPlayerMP) owner);
	                    
	                    this.reloadCool+=((ItemWeapon)stack.getItem()).getWeaponReloadTime(stack,owner);
	                    
	                    if(stack.getItemDamage()==0){
	                    	state-=8;
	                    	this.reloadCool=0;
	                    }
	                }
				}
	        }
			else{
				state&=7;
			}
        }
		else {
			owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemMinigun.slowdown);
			owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemSniperRifle.slowdown);
		}
	}
	

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return TF2weapons.WEAPONS_CAP!=null && capability==TF2weapons.WEAPONS_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (TF2weapons.WEAPONS_CAP != null && capability == TF2weapons.WEAPONS_CAP) return TF2weapons.WEAPONS_CAP.cast(this);
		return null;
	}

	public boolean startedPress(){
		if(this.pressedStart){
			this.pressedStart=false;
			return true;
		}
		return false;
		
	}
	/*public static void tick(boolean client){
		
		Map<EntityLivingBase,Integer> map=TF2ActionHandler.playerAction.get(client);
	
		if(map.isEmpty()) return;
		Iterator<EntityLivingBase> iterator=map.keySet().iterator();
		while(iterator.hasNext())
		{	
			EntityLivingBase player = iterator.next();
	
			if(player.isDead||player.deathTime>0){
	
				iterator.remove();
				continue;
			}
			int state=0;
			if(map.get(player)!=null){
				state=map.get(player);
			}
		
			ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
	
			WeaponsCapability cap=player.getCapability(TF2weapons.WEAPONS_CAP, null);
	
			cap.lastFire-=50;
			if (stack != null&&stack.getItem() instanceof ItemUsable)
	        {
				ItemUsable item=(ItemUsable) stack.getItem();
				
				//if(!client)
					//System.out.println(client+" rel " +item.getTagCompound().getShort("reload")+" "+state+" "+item.getDisplayName());
				if (cap.reloadCool > 0)
	            {
	                cap.reloadCool-=50;
	            }
				if (cap.fire1Cool > 0)
	            {
					cap.fire1Cool-=50;
	            }
				if (cap.fire2Cool > 0)
	            {
					cap.fire2Cool-=50;
	            }
				
				if(client){
					//((EntityPlayerMP)player).isChangingQuantityOnly=state>0;
					stack.animationsToGo=0;
				}
				if(player instanceof EntityTF2Character){
					EntityTF2Character shooter=((EntityTF2Character)player);
					if(shooter.getAttackTarget() != null){
						shooter.targetPrevPos[1]=shooter.targetPrevPos[0];
						shooter.targetPrevPos[3]=shooter.targetPrevPos[2];
						shooter.targetPrevPos[5]=shooter.targetPrevPos[4];
						shooter.targetPrevPos[0]=shooter.getAttackTarget().posX;
						shooter.targetPrevPos[2]=shooter.getAttackTarget().posY;
						shooter.targetPrevPos[4]=shooter.getAttackTarget().posZ;
					}
				}
				
				state=stateDo(player, stack, state,cap, client);
				if((!client||player!=Minecraft.getMinecraft().thePlayer)&&stack.getItem() instanceof ItemRangedWeapon && ((ItemRangedWeapon)stack.getItem()).hasClip(stack)){
					if(((state&4)!=0||stack.getItemDamage()==stack.getMaxDamage())&&(state&8)==0&&stack.getItemDamage()!=0&& cap.reloadCool<=0
							&&(cap.fire1Cool<=0||((ItemRangedWeapon)stack.getItem()).IsReloadingFullClip(stack))){
						state+=8;
						
						cap.reloadCool=((ItemRangedWeapon)stack.getItem()).getWeaponFirstReloadTime(stack,player);
						
						if(!client&&player instanceof EntityPlayerMP)
		                	TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), true,EnumHand.MAIN_HAND), (EntityPlayerMP) player);
						
						if(client&&((ItemRangedWeapon)stack.getItem()).IsReloadingFullClip(stack)){
							TF2weapons.proxy.playReloadSound(player,stack);
		                }
						
					}
					else if(cap.fire1Cool<=0||((ItemRangedWeapon)stack.getItem()).IsReloadingFullClip(stack)){
		                while ((state&8)!=0&&cap.reloadCool <= 0 && stack.getItemDamage()!=0)
		                {
		                    if(((ItemRangedWeapon)stack.getItem()).IsReloadingFullClip(stack)){
		                    	stack.setItemDamage(0);
		                    	if(item.isDoubleWielding(player)){
		                    		player.getHeldItemOffhand().setItemDamage(0);
		                    	}
		                    } else {
		                    	stack.setItemDamage(stack.getItemDamage() - 1); 
		                    	TF2weapons.proxy.playReloadSound(player,stack);
		                    }
		                    if(!client&&player instanceof EntityPlayerMP)
		                    	TF2weapons.network.sendTo(new TF2Message.UseMessage(stack.getItemDamage(), true,EnumHand.MAIN_HAND), (EntityPlayerMP) player);
		                    
		                    cap.reloadCool+=((ItemRangedWeapon)stack.getItem()).getWeaponReloadTime(stack,player);
		                    
		                    if(stack.getItemDamage()==0){
		                    	state-=8;
		                    	cap.reloadCool=0;
		                    }
		                }
					}
		        }
	        }
			else {
				player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemMinigun.slowdown);
				player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(ItemSniperRifle.slowdown);
			}
			map.put(player, state);
		}
	}*/
	public boolean shouldShoot(EntityLivingBase player,int state){
		return player.getActivePotionEffect(TF2weapons.stun)==null&&
				(!(!player.worldObj.isRemote && player instanceof EntityPlayer && 
						(this.predictionList.isEmpty()||(this.predictionList.get(0)!=null&&(this.predictionList.get(0).state&state)!=state)))&&
				!((player.worldObj.isRemote||!(player instanceof EntityPlayer))&&(this.state&state)!=state));
	}
	public void stateDo(EntityLivingBase player,ItemStack stack){
		ItemUsable item=(ItemUsable) stack.getItem();
		//System.out.println(stack.getTagCompound().getByte("active"));
		if((this.state&1)!=0 && stack.getTagCompound().getByte("active")==2){
			//System.out.println("firin");
	        item.fireTick(stack, player, player.worldObj);
	        
	        /*if (!item.hasTagCompound())
	        {
	            item.getTagCompound() = new NBTTagCompound();
	            item.getTagCompound().setTag("Attributes", (NBTTagCompound) ((ItemUsable)item.getItem()).buildInAttributes.copy());
	        }*/
	        
		}
		while(this.fire1Cool <= 0&&shouldShoot(player,1)){
	    	
	    	//System.out.println("PLAJERRRR: "+player);
				TF2Message.PredictionMessage message=null;
				if(!player.worldObj.isRemote && player instanceof EntityPlayer){
					message=this.predictionList.remove(0);
				}
				
				if(message==null){
					boolean canFire=item.canFire(player.worldObj, player, stack);
		    		if(item.isDoubleWielding(player)&&((ItemUsable) player.getHeldItemOffhand().getItem()).canFire(player.worldObj, player, player.getHeldItemOffhand())&&(this.lastFire>0||!canFire)){
		    			this.mainHand=!this.mainHand;
		    		}
		    		else{
		    			this.mainHand=true;
		    		}
		    		if(this.mainHand&&!canFire){
		    			break;
		    		}
				}
	    		else {
	    			this.mainHand=message.hand==EnumHand.MAIN_HAND;
	    			if(!((ItemUsable) player.getHeldItem(message.hand).getItem()).canFire(player.worldObj, player, player.getHeldItem(message.hand))){
	    				break;
	    			}
	    		}
	    		this.fire1Cool+=item.getFiringSpeed(stack,player);
	    		
	    		if(player instanceof EntityTF2Character){
	    			((EntityTF2Character)player).onShot();
	    		}
	    		
	    		if(player.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISED)&& !(item instanceof ItemSapper)){
	    			TF2EventBusListener.disguise(player, false);
	    		}
	    		
	    		
	    		double oldX=player.posX, oldY=player.posY, oldZ=player.posZ;
				float oldPitch=player.rotationPitch, oldYaw=player.rotationYawHead;
				
				if(!player.worldObj.isRemote && player instanceof EntityPlayer){
	    			
	    			this.preparePlayerPrediction(player, message);
	    		}
				if(player.worldObj.isRemote && player == ClientProxy.getLocalPlayer()){
					message=new TF2Message.PredictionMessage(player.posX, player.posY, player.posZ, player.rotationPitch, player.rotationYawHead, 1,this.mainHand?EnumHand.MAIN_HAND:EnumHand.OFF_HAND);
				}
				
	    		item.use(this.mainHand?stack:player.getHeldItemOffhand(), player, player.worldObj, this.mainHand?EnumHand.MAIN_HAND:EnumHand.OFF_HAND, message);
	    		
	    		player.removePotionEffect(TF2weapons.charging);
	    		
	    		if(player.worldObj.isRemote && player == ClientProxy.getLocalPlayer()){
	    			//System.out.println("Shoot Res: "+message.target);
		    		TF2weapons.network.sendToServer(message);
	    		}
	    		
	    		player.posX=oldX;
    			player.posY=oldY;
    			player.posZ=oldZ;
    			player.rotationYawHead=oldYaw;
    			player.rotationPitch=oldPitch;
    			
    			/*if(!player.worldObj.isRemote && player instanceof EntityPlayer){
    				this.predictionList.remove(0);
    				System.out.println(this.predictionList.size());
	    		}*/
    			
	    		this.lastFire=1250;
	    		if (stack.getItem() instanceof ItemWeapon)
	            {
	    			this.reloadCool=0;
	            	if((this.state&8)!=0){
	            		this.state-=8;
	            	}
	            }
    	}
		if((this.state&2)!=0 && stack.getTagCompound().getByte("active")==2){
			((ItemUsable)stack.getItem()).altFireTick(stack, player, player.worldObj);
			
		}
		while(this.fire2Cool <= 0&&shouldShoot(player,2)){
	    	
	    	//System.out.println("PLAJERRRR: "+player);
				TF2Message.PredictionMessage message=null;
				if(!player.worldObj.isRemote && player instanceof EntityPlayer){
					message=this.predictionList.remove(0);
				}
				
				if(item.getAltFiringSpeed(stack, player)==Short.MAX_VALUE||!item.canAltFire(player.worldObj, player, stack))
					break;
				this.fire2Cool+=item.getAltFiringSpeed(stack,player);
				
				if(player.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISED)){
	    			TF2EventBusListener.disguise(player, false);
	    		}
				
				
				double oldX=player.posX, oldY=player.posY, oldZ=player.posZ;
				float oldPitch=player.rotationPitch, oldYaw=player.rotationYawHead;
				
	    		if(!player.worldObj.isRemote && player instanceof EntityPlayer){
	    			this.preparePlayerPrediction(player, message);
	    		}
	    		
	    		((ItemUsable)stack.getItem()).altUse(stack, player, player.worldObj);
	    		
	    		player.removePotionEffect(TF2weapons.charging);
	    		if(player.worldObj.isRemote && player == ClientProxy.getLocalPlayer()){
		    		TF2weapons.network.sendToServer(new TF2Message.PredictionMessage(player.posX, player.posY, player.posZ, player.rotationPitch, player.rotationYawHead, 2,EnumHand.MAIN_HAND));
	    		}
	    		
	    		/*if(!player.worldObj.isRemote && player instanceof EntityPlayer){
    				this.predictionList.remove(0);
    				System.out.println(this.predictionList.size());
	    		}*/
	    		player.posX=oldX;
    			player.posY=oldY;
    			player.posZ=oldZ;
    			player.rotationYawHead=oldYaw;
    			player.rotationPitch=oldPitch;
    			
	    		if(!player.worldObj.isRemote)
	    			ItemUsable.sps++;
	    		if (stack.getItem() instanceof ItemWeapon)
	            {
	    			this.reloadCool=0;
	            	if((this.state&8)!=0){
	            		this.state-=8;
	            	}
	            }
	    	}
		
	}
	public void preparePlayerPrediction(EntityLivingBase player,TF2Message.PredictionMessage message){
		player.posX=message.x;
		player.posY=message.y;
		player.posZ=message.z;
		player.rotationYawHead=message.yaw;
		player.rotationPitch=message.pitch;
		this.mainHand=message.hand==EnumHand.MAIN_HAND;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag=new NBTTagCompound();
		tag.setByte("VisTicks", (byte) this.invisTicks);
		tag.setByte("DisguiseTicks", (byte) this.disguiseTicks);
		tag.setInteger("HealTarget", this.healTarget);
		NBTTagCompound cld=new NBTTagCompound();
		for(Entry<String,Integer> entry:this.effectsCool.entrySet()){
			cld.setInteger(entry.getKey(), entry.getValue());
		}
		tag.setTag("Cooldowns", cld);
		tag.setInteger("HealTarget", this.healTarget);
		tag.setShort("Heads", (short) this.collectedHeads);
		tag.setShort("HeadsCool",(short) (this.collectedHeadsTime-this.owner.ticksExisted));
		tag.setBoolean("Cloaked", this.owner.getDataManager().get(TF2EventBusListener.ENTITY_INVIS));
		tag.setBoolean("Disguised", this.owner.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISED));
		tag.setString("DisguiseType", this.owner.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISE_TYPE));
		tag.setBoolean("Uber", this.owner.getDataManager().get(TF2EventBusListener.ENTITY_UBER));
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		this.invisTicks=nbt.getByte("VisTicks");
		this.disguiseTicks=nbt.getByte("DisguiseTicks");
		this.healTarget=nbt.getByte("HealTarget");
		NBTTagCompound cld=nbt.getCompoundTag("Cooldowns");
		for(String key:nbt.getKeySet()){
			this.effectsCool.put(key, cld.getInteger(key));
		}
		this.collectedHeads=nbt.getShort("Heads");
		this.collectedHeadsTime=nbt.getShort("HeadsCool");
		this.owner.getDataManager().set(TF2EventBusListener.ENTITY_INVIS, nbt.getBoolean("Cloaked"));
		this.owner.getDataManager().set(TF2EventBusListener.ENTITY_DISGUISED, nbt.getBoolean("Disguised"));
		this.owner.getDataManager().set(TF2EventBusListener.ENTITY_UBER, nbt.getBoolean("Uber"));
		this.owner.getDataManager().set(TF2EventBusListener.ENTITY_DISGUISE_TYPE, nbt.getString("DisguiseType"));
		if(this.owner.getDataManager().get(TF2EventBusListener.ENTITY_DISGUISED)){
        	TF2EventBusListener.disguise(this.owner, true);
        }
	}
}
