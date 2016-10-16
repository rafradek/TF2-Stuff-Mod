package rafradek.TF2weapons.characters;

import java.util.Random;

import com.google.common.base.Predicate;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.properties.EntityOnFire;
import net.minecraft.world.storage.loot.properties.EntityProperty;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData;

public class EntityOfClassFunction extends LootFunction {

	//public int[] possibleValues;
	//public int[] withClass;
	public String weaponClass;
	public EntityOfClassFunction(LootCondition[] conditionsIn, String weaponClass)
    {
        super(conditionsIn);
        this.weaponClass=weaponClass;
    }
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
        return ItemFromData.getRandomWeaponOfClass(weaponClass, rand, true);
    }

    public static class Serializer extends LootFunction.Serializer<EntityOfClassFunction>
        {
            public Serializer()
            {
                super(new ResourceLocation("set_weapon_class"), EntityOfClassFunction.class);
            }

            public void serialize(JsonObject object, EntityOfClassFunction functionClazz, JsonSerializationContext serializationContext)
            {
            	object.addProperty("weaponClass", functionClazz.weaponClass);
            	//object.add("possibleValues", serializationContext.serialize(functionClazz.possibleValues));
                //object.add("data", serializationContext.serialize(functionClazz.metaRange));
            }

            public EntityOfClassFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn)
            {
                return new EntityOfClassFunction(conditionsIn,object.get("weaponClass").getAsString());
            }
        }
}
