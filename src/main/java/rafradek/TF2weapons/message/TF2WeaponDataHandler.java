package rafradek.TF2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.MapList;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.message.TF2Message.PropertyMessage;
import rafradek.TF2weapons.message.TF2Message.WeaponDataMessage;
import rafradek.TF2weapons.weapons.ItemMedigun;

public class TF2WeaponDataHandler implements IMessageHandler<TF2Message.WeaponDataMessage, IMessage> {

	public static int size;
	@Override
	public IMessage onMessage(final WeaponDataMessage message, MessageContext ctx) {
		
		TF2weapons.loadWeapon(message.weapon.getName(), message.weapon);
		ClientProxy.RegisterWeaponData(message.weapon);
		return null;
	}

}
