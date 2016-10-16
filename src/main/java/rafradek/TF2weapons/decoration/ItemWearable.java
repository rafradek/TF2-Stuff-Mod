package rafradek.TF2weapons.decoration;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;

public class ItemWearable extends ItemFromData {

	public static int usedModel;
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot slot, Entity player){
		return slot==(isHat(stack)?EntityEquipmentSlot.HEAD:EntityEquipmentSlot.CHEST);
    }
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world,EntityPlayer living,EnumHand hand) {
		if(!world.isRemote){
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
    
    public boolean isHat(ItemStack stack){
    	return getData(stack).getBoolean(PropertyType.HAT);
    }
    
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
    {
		return getData(stack).getString(PropertyType.ARMOR_IMAGE);
    }
}
