package rafradek.TF2weapons.characters;

import java.util.Random;

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
import rafradek.TF2weapons.TF2weapons;

public class EntityBuildingFunction extends LootFunction {

	//public int[] possibleValues;
	//public int[] withClass;
	public EntityBuildingFunction(LootCondition[] conditionsIn)
    {
        super(conditionsIn);
    }
    public ItemStack apply(ItemStack stack, Random rand, LootContext context)
    {
    	Entity entity=context.getEntity(EntityTarget.THIS);
    	if(entity!=null&&entity instanceof EntityTF2Character){
    		stack.setItemDamage(18+rand.nextInt(3)*2+((EntityTF2Character)entity).getEntTeam());
    	}
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<EntityBuildingFunction>
        {
            public Serializer()
            {
                super(new ResourceLocation("random_meta"), EntityBuildingFunction.class);
            }

            public void serialize(JsonObject object, EntityBuildingFunction functionClazz, JsonSerializationContext serializationContext)
            {
            	//object.add("possibleValues", serializationContext.serialize(functionClazz.possibleValues));
                //object.add("data", serializationContext.serialize(functionClazz.metaRange));
            }

            public EntityBuildingFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn)
            {
                return new EntityBuildingFunction(conditionsIn);
            }
        }
}
