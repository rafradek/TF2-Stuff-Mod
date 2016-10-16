package rafradek.TF2weapons.weapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import rafradek.TF2weapons.TF2weapons;

public class ItemAmmoBelt extends Item {

	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world,EntityPlayer living,EnumHand hand) {
		if(!world.isRemote){
			FMLNetworkHandler.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
	
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
    {
		return TF2weapons.MOD_ID+":textures/models/tf2/ammo_belt.png";
    }
}
