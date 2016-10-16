package rafradek.TF2weapons.weapons;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.message.TF2Message.PredictionMessage;

public class ItemSapper extends ItemBulletWeapon {

	public ItemSapper()
    {
        super();
        this.setMaxStackSize(64);
    }
	
	public boolean onHit(ItemStack stack,EntityLivingBase attacker, Entity target){
		//System.out.println("Can hit: " + TF2weapons.canHit(attacker, target));
		if(target instanceof EntityBuilding&&!((EntityBuilding)target).isSapped()&&TF2weapons.canHit(attacker, target)){
			((EntityBuilding)target).setSapped(attacker,stack);
			((EntityBuilding)target).playSound(TF2Sounds.MOB_SAPPER_PLANT, 1.3f, 1);
			if(((EntityBuilding)target).getOwner()!=null){
				((EntityBuilding)target).getOwner().setRevengeTarget(attacker);
			}
			stack.stackSize--;
			if(stack.stackSize<=0&&attacker instanceof EntityPlayer){
				((EntityPlayer)attacker).inventory.deleteStack(stack);
				
			}
		}
		return false;
	}
	
	public float getMaxRange(){
		return 2.4f;
	}
	
	public float getBulletSize(){
		return 0.35f;
	}
	
	public boolean showTracer(ItemStack stack){
		return false;
	}
	public boolean doMuzzleFlash(ItemStack stack, EntityLivingBase attacker){
		
		return false;
	}
}
