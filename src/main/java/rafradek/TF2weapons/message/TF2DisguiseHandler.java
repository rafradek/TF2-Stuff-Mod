package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.MapList;
import rafradek.TF2weapons.TF2EventBusListener;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.DisguiseMessage;
import rafradek.TF2weapons.message.TF2Message.PropertyMessage;
import rafradek.TF2weapons.message.TF2Message.WeaponDataMessage;
import rafradek.TF2weapons.weapons.ItemDisguiseKit;
import rafradek.TF2weapons.weapons.ItemMedigun;

public class TF2DisguiseHandler implements IMessageHandler<TF2Message.DisguiseMessage, IMessage> {

	public static int size;
	@Override
	public IMessage onMessage(final DisguiseMessage message, MessageContext ctx) {
		final EntityPlayerMP player=ctx.getServerHandler().playerEntity;
		((WorldServer)player.worldObj).addScheduledTask(new Runnable(){

			@Override
			public void run() {
				ItemStack stack;
				if(((stack=player.getHeldItemMainhand())!= null && stack.getItem() instanceof ItemDisguiseKit) || ((stack=player.getHeldItemOffhand())!= null && stack.getItem() instanceof ItemDisguiseKit)){
					ItemDisguiseKit.startDisguise(player, player.worldObj, message.value);
					if(!player.capabilities.isCreativeMode)
						stack.damageItem(1, player);
				}
			}
			
		});
		return null;
	}

}
