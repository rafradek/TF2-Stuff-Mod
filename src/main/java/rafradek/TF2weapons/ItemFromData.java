package rafradek.TF2weapons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.TF2Attribute.AttributeState;
import rafradek.TF2weapons.TF2Attribute.AttributeType;
import rafradek.TF2weapons.WeaponData.PropertyType;

public class ItemFromData extends Item {

	public static final WeaponData BLANK_DATA=new WeaponData("toloadfiles");
	public static final Predicate<WeaponData> VISIBLE_WEAPON=new Predicate<WeaponData>(){

		@Override
		public boolean apply(WeaponData input) {
			// TODO Auto-generated method stub
			return !input.getBoolean(PropertyType.HIDDEN)&&!input.getBoolean(PropertyType.ROLL_HIDDEN)&&!input.getString(PropertyType.CLASS).equals("cosmetic")&&!input.getString(PropertyType.CLASS).equals("crate");
		}
		
	};
	public ItemFromData()
    {
    	this.setCreativeTab(TF2weapons.tabutilitytf2);
        this.setUnlocalizedName("tf2usable");
        this.setMaxStackSize(1);
        this.setNoRepair();
        // TODO Auto-generated constructor stub
    }
	@SideOnly(Side.CLIENT)
    public CreativeTabs getCreativeTab()
    {
        return TF2weapons.tabutilitytf2;
    }
	public void onUpdate(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5)
   	{
    	if(getData(stack)==BLANK_DATA&&par3Entity instanceof EntityPlayer){
    		((EntityPlayer)par3Entity).inventory.setInventorySlotContents(par4, null);
    		stack.stackSize=0;
    		return;
    	}
   	}
	public static WeaponData getData(ItemStack stack){
		if(stack != null&& stack.hasTagCompound() && MapList.nameToData.containsKey(stack.getTagCompound().getString("Type"))){
			return MapList.nameToData.get(stack.getTagCompound().getString("Type"));
		}
		return BLANK_DATA;
	}
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
		Iterator<Entry<String,WeaponData>> iterator=MapList.nameToData.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String, WeaponData> entry=iterator.next();
			//System.out.println("Hidden: "+entry.getValue().hasProperty(PropertyType.HIDDEN));
			if(entry.getValue().hasProperty(PropertyType.HIDDEN)&&entry.getValue().getBoolean(PropertyType.HIDDEN)){
				continue;
			}
			Item item=MapList.weaponClasses.get(entry.getValue().getString(PropertyType.CLASS));
			if(item==this){
				par3List.add(ItemFromData.getNewStack(entry.getKey()));
			}
		}
    }
	public static ItemStack getNewStack(String type){
		//System.out.println("Dano: "+type+" "+MapList.weaponClasses.get(MapList.nameToCC.get(type).get("Class").getString())+" "+Thread.currentThread().getName());
		ItemStack stack=new ItemStack(MapList.weaponClasses.get(MapList.nameToData.get(type).getString(PropertyType.CLASS)));
		stack.setTagCompound((NBTTagCompound) MapList.buildInAttributes.get(type).copy());
		//System.out.println(stack.toString());
		return stack;
	}
	public static ItemStack getRandomWeapon(Random random,Predicate<WeaponData> predicate){
		
		ArrayList<String> weapons=new ArrayList<>();
		for(Entry<String,WeaponData> entry: MapList.nameToData.entrySet()){
			if(predicate.apply(entry.getValue())){
				weapons.add(entry.getKey());
			}
		}
		if(weapons.isEmpty()){
			return null;
		}
		return getNewStack(weapons.get(random.nextInt(weapons.size())));
	}
	public static ItemStack getRandomWeaponOfType(String type, float chanceOfParent,Random random){
		//WeaponData parent=MapList.nameToData.get(type);
		if(chanceOfParent>=0&&random.nextFloat()<=chanceOfParent){
			return getNewStack(type);
		}
		else{
			ArrayList<String> weapons=new ArrayList<>();
			if(chanceOfParent<0){
				weapons.add(type);
			}
			for(Entry<String,WeaponData> entry: MapList.nameToData.entrySet()){
				if(!entry.getValue().getBoolean(PropertyType.HIDDEN)&&!entry.getValue().getBoolean(PropertyType.ROLL_HIDDEN)&&entry.getValue().getString(PropertyType.BASED_ON).equals(type)){
					weapons.add(entry.getKey());
				}
			}
			if(weapons.size()>0)
				return getNewStack(weapons.get(random.nextInt(weapons.size())));
			else
				return getNewStack(type);
		}
		
	}
	public static ItemStack getRandomWeaponOfClass(String clazz, Random random, boolean showHidden){
		ArrayList<String> weapons=new ArrayList<>();
		for(Entry<String,WeaponData> entry: MapList.nameToData.entrySet()){
			if(!entry.getValue().getBoolean(PropertyType.HIDDEN)&&(showHidden||!entry.getValue().getBoolean(PropertyType.ROLL_HIDDEN))&&entry.getValue().getString(PropertyType.CLASS).equals(clazz)){
				weapons.add(entry.getKey());
			}
		}
		return getNewStack(weapons.get(random.nextInt(weapons.size())));
	}
	public static ItemStack getRandomWeaponOfSlotMob(final String mob,final int slot, Random random, final boolean showHidden){
		/*ArrayList<String> weapons=new ArrayList<>();
		for(Entry<String,WeaponData> entry: MapList.nameToData.entrySet()){
			if(!entry.getValue().getBoolean(PropertyType.ROLL_HIDDEN)&&entry.getValue().getString(PropertyType.TYPE).equals(type)){
				String[] mobTypes=entry.getValue().getString(PropertyType.MOB_TYPE).contains(s)
				for(String mobType:mobTypes){
					if(mob.equalsIgnoreCase(mobType.trim())){
						weapons.add(entry.getKey());
						break;
					}
				}
			}
		}*/
		return getRandomWeapon(random, new Predicate<WeaponData>(){

			@Override
			public boolean apply(WeaponData input) {
				// TODO Auto-generated method stub
				return !(input.getBoolean(PropertyType.ROLL_HIDDEN)&&!showHidden)&&input.getInt(PropertyType.SLOT)==slot&&input.getString(PropertyType.MOB_TYPE).contains(mob);
			}
			
		});
	}
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return getData(oldStack)!=getData(newStack)||(slotChanged);
    }

	public String getItemStackDisplayName(ItemStack stack)
    {
		if(ItemFromData.getData(stack) == BLANK_DATA){
			return "Weapon";
		}
		String name=ItemFromData.getData(stack).getString(PropertyType.NAME);
		if(stack.getTagCompound().getBoolean("Strange")){
			name=TextFormatting.GOLD+TF2EventBusListener.STRANGE_TITLES[stack.getTagCompound().getInteger("StrangeLevel")]+" "+name;
		}
		if(stack.getTagCompound().getBoolean("Australium")){
			name=TextFormatting.GOLD+"Australium "+name;
		}
		return name;
    }

	public static SoundEvent getSound(ItemStack stack, PropertyType name){
		return SoundEvent.REGISTRY.getObject(new ResourceLocation(getData(stack).getString(name)));
	}
	
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par2List, boolean par4)
    {	
		/*if (!par1ItemStack.hasTagCompound()) {
			par1ItemStack.getTagCompound()=new NBTTagCompound();
			par1ItemStack.getTagCompound().setTag("Attributes", (NBTTagCompound) ((ItemUsable)par1ItemStack.getItem()).buildInAttributes.copy());
		}*/
        if (par1ItemStack.hasTagCompound())
        {
        	NBTTagCompound attributeList=par1ItemStack.getTagCompound().getCompoundTag("Attributes");
    		Iterator<String> iterator=attributeList.getKeySet().iterator();
    		while(iterator.hasNext()){
    			String name=iterator.next();
    			NBTBase tag=attributeList.getTag(name);
    			if(tag instanceof NBTTagFloat){
    				NBTTagFloat tagFloat=(NBTTagFloat) tag;
    				TF2Attribute attribute=TF2Attribute.attributes[Integer.parseInt(name)];
    				if(attribute.state!=AttributeState.HIDDEN)
    					par2List.add(attribute.getTranslatedString(tagFloat.getFloat(),true));
    			}
            }
    		if(getData(par1ItemStack).hasProperty(PropertyType.DESC)){
    			par2List.add("");
    			par2List.add(getData(par1ItemStack).getString(PropertyType.DESC));
    		}
        }
    }

}
