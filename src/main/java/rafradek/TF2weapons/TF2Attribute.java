package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.text.TextFormatting;
import rafradek.TF2weapons.TF2Attribute.AttributeState;
import rafradek.TF2weapons.TF2Attribute.AttributeType;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.projectiles.EntityProjectileSimple;
import rafradek.TF2weapons.weapons.ItemBulletWeapon;
import rafradek.TF2weapons.weapons.ItemFlameThrower;
import rafradek.TF2weapons.weapons.ItemMedigun;
import rafradek.TF2weapons.weapons.ItemMinigun;
import rafradek.TF2weapons.weapons.ItemProjectileWeapon;
import rafradek.TF2weapons.weapons.ItemWeapon;
import rafradek.TF2weapons.weapons.ItemSniperRifle;
import rafradek.TF2weapons.weapons.ItemSoldierBackpack;
import rafradek.TF2weapons.weapons.ItemUsable;

public class TF2Attribute {

	public static TF2Attribute[] attributes=new TF2Attribute[64];
	
	public int id;
	public String name;
	public AttributeType typeOfValue;
	public String effect;
	public float defaultValue;
	public AttributeState state;

	private Predicate<ItemStack> canApply;

	public int numLevels;

	public float perLevel;

	public int cost;

	private int weight;
	
	public static final Predicate<ItemStack> ITEM_WEAPON=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemWeapon;
		}
		
	};
	public static final Predicate<ItemStack> NOT_FLAMETHROWER=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemWeapon && !(input.getItem() instanceof ItemFlameThrower);
		}
		
	};
	public static final Predicate<ItemStack> FLAMETHROWER=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemFlameThrower;
		}
		
	};
	public static final Predicate<ItemStack> IGNITE=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemFlameThrower || getModifier("BurnOnHit",input,0,null)>0;
		}
		
	};
	public static final Predicate<ItemStack> WITH_CLIP=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemWeapon && ((ItemWeapon)input.getItem()).hasClip(input);
		}
		
	};
	public static final Predicate<ItemStack> WITH_SPREAD=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemWeapon && (((ItemWeapon)input.getItem()).getWeaponSpreadBase(input, null)!=0 || ((ItemWeapon)input.getItem()).getWeaponMinDamage(input, null)!=1);
		}
		
	};
	public static final Predicate<ItemStack> WITH_AMMO=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemUsable && ItemFromData.getData(input).getInt(PropertyType.AMMO_TYPE)!=0;
		}
		
	};
	public static final Predicate<ItemStack> ITEM_BULLET=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemBulletWeapon || MapList.projectileClasses.get(ItemFromData.getData(input).getString(PropertyType.PROJECTILE)).isAssignableFrom(EntityProjectileSimple.class);
		}
		
	};
	public static final Predicate<ItemStack> ITEM_PROJECTILE=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemProjectileWeapon;
		}
		
	};
	public static final Predicate<ItemStack> ITEM_MINIGUN=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemMinigun;
		}
		
	};
	public static final Predicate<ItemStack> ITEM_SNIPER_RIFLE=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemSniperRifle;
		}
		
	};
	public static final Predicate<ItemStack> EXPLOSIVE=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemProjectileWeapon&&!(input.getItem() instanceof ItemFlameThrower || MapList.projectileClasses.get(ItemFromData.getData(input).getString(PropertyType.PROJECTILE)).isAssignableFrom(EntityProjectileSimple.class));
		}
		
	};
	public static final Predicate<ItemStack> MEDIGUN=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemMedigun;
		}
		
	};
	public static final Predicate<ItemStack> BANNER=new Predicate<ItemStack>(){

		@Override
		public boolean apply(ItemStack input) {
			// TODO Auto-generated method stub
			return input.getItem() instanceof ItemSoldierBackpack;
		}
		
	};
	/**
	 * 
	 * @param id
	 * @param name
	 * @param effect
	 * @param typeOfValue
	 * @param defaultValue
	 * @param state 1 dodatni, 0 neutralny, -1 ujemny
	 */
	
	public static enum AttributeType{
		PERCENTAGE,
		INVERTED_PERCENTAGE,
		ADDITIVE;
	}
	public static enum AttributeState{
		POSITIVE,
		NEGATIVE,
		NEUTRAL,
		HIDDEN;
	}
	public TF2Attribute(int id, String name, String effect, AttributeType typeOfValue, float defaultValue, AttributeState state, Predicate<ItemStack> canApply, float perLevel, int numLevels, int cost, int weight) {
		this.id=id;
		attributes[id]=this;
		MapList.nameToAttribute.put(name, this);
		this.name=name;
		this.effect=effect;
		this.typeOfValue=typeOfValue;
		this.defaultValue=defaultValue;
		this.state=state;
		this.canApply=canApply;
		this.numLevels=numLevels;
		this.perLevel=perLevel;
		this.cost=cost;
		this.weight=weight;
	}
	public static void initAttributes(){
		new TF2Attribute(0, "DamageBonus", "Damage", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, ITEM_WEAPON,0.25f,4,160,4);
		new TF2Attribute(1, "DamagePenalty", "Damage", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, ITEM_WEAPON,0.15f,2,-140,1);
		new TF2Attribute(2, "ClipSizeBonus", "Clip Size", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, WITH_CLIP,0.5f,4,140,3);
		new TF2Attribute(3, "ClipSizePenalty", "Clip Size", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, WITH_CLIP,0.25f,2,-8,1);
		new TF2Attribute(4, "MinigunSpinBonus", "Minigun Spinup", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.POSITIVE, ITEM_MINIGUN,-0.15f,4,100,2);
		new TF2Attribute(5, "MinigunSpinPenalty", "Minigun Spinup", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, ITEM_MINIGUN,0.1f,2,-200,1);
		new TF2Attribute(6, "FireRateBonus", "Fire Rate", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.POSITIVE, NOT_FLAMETHROWER,-0.1f,4,80,4);
		new TF2Attribute(7, "FireRatePenalty", "Fire Rate", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.NEGATIVE, NOT_FLAMETHROWER,0.06f,2,-100,1);
		new TF2Attribute(8, "SpreadBonus", "Spread", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),-0.15f,3,120,1);
		new TF2Attribute(9, "SpreadPenalty", "Spread", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(10, "PelletBonus", "Pellet Count", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(11, "PelletPenalty", "Pellet Count", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(12, "ReloadRateBonus", "Reload Time", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.POSITIVE, WITH_CLIP,-0.2f,3,100,2);
		new TF2Attribute(13, "ReloadRatePenalty", "Reload Time", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, WITH_CLIP,0.2f,3,-200,1);
		new TF2Attribute(14, "KnockbackBonus", "Knockback", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(15, "KnockbackPenalty", "Knockback", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(16, "ChargeBonus", "Charge", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, ITEM_SNIPER_RIFLE,0.25f,4,100,2);
		new TF2Attribute(17, "ChargePenalty", "Charge", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.NEGATIVE, ITEM_SNIPER_RIFLE,-0.15f,2,-140,1);
		new TF2Attribute(18, "SpreadAdd", "Spread", AttributeType.ADDITIVE, 0f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(19, "ProjectileSpeedBonus", "Proj Speed", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, ITEM_PROJECTILE,0.25f,4,100,2);
		new TF2Attribute(20, "ProjectileSpeedPenalty", "Proj Speed", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, ITEM_PROJECTILE,0.15f,2,-150,1);
		new TF2Attribute(21, "ExplosionRadiusBonus", "Explosion Radius", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, EXPLOSIVE,0.2f,3,100,1);
		new TF2Attribute(22, "ExplosionRadiusPenalty", "Explosion Radius", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(23, "DestroyOnImpact", "Coll Remove", AttributeType.ADDITIVE, 0f, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(24, "AmmoEfficiencyBonus", "Ammo Eff", AttributeType.INVERTED_PERCENTAGE, 1f, AttributeState.POSITIVE, WITH_AMMO,-0.2f,3,120,2);
		new TF2Attribute(25, "AmmoEfficiencyPenalty", "Ammo Eff", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, WITH_AMMO,0.15f,2,-150,2);
		new TF2Attribute(26, "Penetration", "Penetration", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, ITEM_BULLET,1,1,160,1);
		new TF2Attribute(27, "HealRateBonus", "Heal", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, MEDIGUN,0.25f,4,100,2);
		new TF2Attribute(28, "HealRatePenalty", "Heal", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, MEDIGUN,-0.15f,2,-150,1);
		new TF2Attribute(29, "OverHealBonus", "Overheal", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, MEDIGUN,0.25f,4,100,2);
		new TF2Attribute(30, "OverHealPenalty", "Overheal", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, MEDIGUN,-0.15f,2,-150,1);
		new TF2Attribute(31, "BurnTimeBonus", "Burn Time", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, IGNITE,0.5f,4,140,2);
		new TF2Attribute(32, "BurnTimePenalty", "Burn Time", AttributeType.PERCENTAGE, 1f, AttributeState.NEGATIVE, IGNITE,-0.5f,4,1,2);
		new TF2Attribute(33, "HealthOnKill", "Health Kill", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, ITEM_WEAPON,2.5f,4,80,2);
		new TF2Attribute(34, "AccuracyBonus", "Accuracy", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, WITH_SPREAD,0.25f,3,120,1);
		new TF2Attribute(35, "BuffDurationBonus","Buff Duration", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, BANNER,0.25f,4,80,1);
		new TF2Attribute(36, "FlameRangeBonus","Flame Range", AttributeType.PERCENTAGE, 1f, AttributeState.POSITIVE, FLAMETHROWER,0.25f,4,80,1);
		new TF2Attribute(37, "CritBurning","Crit Burn", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(38, "BurnOnHit","Burn Hit", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(39, "DestroyBlock","Destroy Block", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.or(ITEM_BULLET,EXPLOSIVE),1f,2,180,1);
		new TF2Attribute(40, "NoRandomCrit","Random Crit", AttributeType.ADDITIVE, 0, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),1f,2,180,1);
		new TF2Attribute(41, "CritRocket","Crit Rocket", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(42, "CritMini","Crit Mini", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(43, "UberOnHit","Uber Hit", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(44, "BallRelease","Ball Release", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(45, "HealthPenalty","Health", AttributeType.ADDITIVE, 0f, AttributeState.HIDDEN, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(46, "MovementBonus","Speed", AttributeType.PERCENTAGE, 1, AttributeState.HIDDEN, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(47, "MarkForDeathSelf","Mark Death", AttributeType.ADDITIVE, 0, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(48, "CritOnKill","Crit Kill", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(49, "FireResistBonus","Fire Resist", AttributeType.INVERTED_PERCENTAGE, 1, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(50, "DamageResistPenalty","Damage Resist", AttributeType.PERCENTAGE, 1, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(51, "ExplosionResistBonus","Explosion Resist", AttributeType.INVERTED_PERCENTAGE, 1, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(52, "DamageNonBurnPenalty","Damage Non Burn", AttributeType.PERCENTAGE, 1, AttributeState.NEGATIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		new TF2Attribute(53, "CollectHeads","Kill Count", AttributeType.ADDITIVE, 0, AttributeState.POSITIVE, Predicates.<ItemStack>alwaysFalse(),0,0,0,1);
		//new TF2Attribute(23, "He", "Coll Remove", "Additive", 0f, -1);
	}
	public static float getModifier(String effect, ItemStack stack, float initial,EntityLivingBase entity) {
		float value=initial;
		if(stack.getTagCompound() != null){
			NBTTagCompound attributeList=stack.getTagCompound().getCompoundTag("Attributes");
			Iterator<String> iterator=attributeList.getKeySet().iterator();
			while(iterator.hasNext()){
				String name=iterator.next();
				NBTBase tag=attributeList.getTag(name);
				if(tag instanceof NBTTagFloat){
					TF2Attribute attribute=attributes[Integer.parseInt(name)];
					if(attribute.effect.equals(effect))
						if(attribute.typeOfValue==AttributeType.ADDITIVE)
							value+=((NBTTagFloat)tag).getFloat();
						else
							value*=((NBTTagFloat)tag).getFloat();
				}
			}
		}
		if(entity!=null&&entity instanceof EntityTF2Character){
			value*=((EntityTF2Character)entity).getAttributeModifier(effect);
		}
		return value;
	}
	
	public String getTranslatedString(float value,boolean withColor){
		String valueStr=String.valueOf(value);
		if(this.typeOfValue==AttributeType.PERCENTAGE) 
			valueStr=Integer.toString(Math.round((value-1)*100))+"%";
		else if(this.typeOfValue==AttributeType.INVERTED_PERCENTAGE) 
			valueStr=Integer.toString(Math.round((1-value)*100))+"%";
		else if(this.typeOfValue==AttributeType.ADDITIVE) 
			valueStr=Integer.toString(Math.round(value));
		
		if(withColor){
			TextFormatting color=this.state == AttributeState.POSITIVE ? TextFormatting.AQUA : (this.state == AttributeState.NEGATIVE ? TextFormatting.RED : TextFormatting.WHITE);
			return color+I18n.format("weaponAttribute."+this.name, new Object[] {valueStr});
		}
		else{
			return I18n.format("weaponAttribute."+this.name, new Object[] {valueStr});
		}
			
	}
	
	public static List<TF2Attribute> getAllPassibleAttributesForUpgradeStation(){
		List<TF2Attribute> list=new ArrayList<>();
		for(TF2Attribute attr:attributes){
			if(attr != null && attr.canApply!=Predicates.<ItemStack>alwaysFalse()&&attr.state==AttributeState.POSITIVE){
				for(int i=0;i<attr.weight;i++)
					list.add(attr);
			}
		}
		return list;
	}
	public static void upgradeItemStack(ItemStack stack, int value,Random rand){
		List<TF2Attribute> list=new ArrayList<>();
		int lowestCost=Integer.MAX_VALUE;
		int maxCount=value/80;
		for(TF2Attribute attr:attributes){
			if(attr != null && attr.canApply(stack) &&attr.state==AttributeState.POSITIVE){
				for(int i=0;i<attr.weight;i++)
					list.add(attr);
				lowestCost=Math.min(lowestCost, attr.cost);
			}
		}
		if(list.size()>0){
			int i=0;
			NBTTagCompound tag=stack.getTagCompound().getCompoundTag("Attributes");
			while(i<maxCount&&value>=lowestCost){
				i++;
				TF2Attribute attr=list.get(rand.nextInt(list.size()));
				if(attr.cost<=value&& attr.calculateCurrLevel(stack)<=attr.numLevels){
					value-=attr.cost;
					
		    		String key=String.valueOf(attr.id);
		    		
		    		if(!tag.hasKey(key)){
		    			tag.setFloat(key, attr.defaultValue);
		    		}
		    		tag.setFloat(key, tag.getFloat(key)+attr.perLevel);
				}
			}
		}
	}
	public int calculateCurrLevel(ItemStack stack){
		if(stack == null){
			return 0;
		}
		if(!stack.getTagCompound().getCompoundTag("Attributes").hasKey(String.valueOf(this.id))){
			return 0;
		}
    	float valueOfAttr=stack.getTagCompound().getCompoundTag("Attributes").getFloat(String.valueOf(this.id));
    	return Math.round((valueOfAttr-this.defaultValue)/this.perLevel);
    }
	public int getUpgradeCost(ItemStack stack){
		if(stack == null||!(stack.getItem() instanceof ItemFromData)){
			return this.cost;
		}
		int baseCost=this.cost;
		if(ItemFromData.getData(stack).getInt(PropertyType.COST)<=12){
			baseCost/=2;
		}
		int additionalCost=0;
		int lastUpgradesCost=stack.getTagCompound().getInteger("TotalCost");
		if(lastUpgradesCost>0){
			additionalCost=lastUpgradesCost/10;
			baseCost+=baseCost/10;
		}
		baseCost+=additionalCost;
		return Math.min(1400,baseCost);
	}
	public boolean canApply(ItemStack stack){
		return stack!=null && this.canApply.apply(stack);
	}
}
