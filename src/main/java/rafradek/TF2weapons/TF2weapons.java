package rafradek.TF2weapons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraft.world.storage.loot.properties.EntityPropertyManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.TF2EventBusListener.DestroyBlockEntry;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.building.EntityBuilding;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.building.EntityTeleporter;
import rafradek.TF2weapons.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.building.ItemBuildingBox;
import rafradek.TF2weapons.characters.EntityBuildingFunction;
import rafradek.TF2weapons.characters.EntityDemoman;
import rafradek.TF2weapons.characters.EntityEngineer;
import rafradek.TF2weapons.characters.EntityHeavy;
import rafradek.TF2weapons.characters.EntityMedic;
import rafradek.TF2weapons.characters.EntityMonoculus;
import rafradek.TF2weapons.characters.EntityOfClassFunction;
import rafradek.TF2weapons.characters.EntityPyro;
import rafradek.TF2weapons.characters.EntitySaxtonHale;
import rafradek.TF2weapons.characters.EntityScout;
import rafradek.TF2weapons.characters.EntitySniper;
import rafradek.TF2weapons.characters.EntitySoldier;
import rafradek.TF2weapons.characters.EntitySpy;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.characters.ItemMonsterPlacerPlus;
import rafradek.TF2weapons.characters.KilledByTeam;
import rafradek.TF2weapons.crafting.BlockAmmoFurnace;
import rafradek.TF2weapons.crafting.BlockCabinet;
import rafradek.TF2weapons.crafting.ContainerAmmoFurnace;
import rafradek.TF2weapons.crafting.ContainerTF2Workbench;
import rafradek.TF2weapons.crafting.GuiAmmoFurnace;
import rafradek.TF2weapons.crafting.GuiTF2Crafting;
import rafradek.TF2weapons.crafting.ItemTF2;
import rafradek.TF2weapons.crafting.OpenCrateRecipe;
import rafradek.TF2weapons.crafting.TileEntityAmmoFurnace;
import rafradek.TF2weapons.crafting.TileEntityCabinet;
import rafradek.TF2weapons.decoration.ContainerWearables;
import rafradek.TF2weapons.decoration.GuiWearables;
import rafradek.TF2weapons.decoration.InventoryWearables;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2BulletHandler;
import rafradek.TF2weapons.message.TF2CapabilityHandler;
import rafradek.TF2weapons.message.TF2DisguiseHandler;
import rafradek.TF2weapons.message.TF2GuiConfigHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2ProjectileHandler;
import rafradek.TF2weapons.message.TF2PropertyHandler;
import rafradek.TF2weapons.message.TF2ShowGuiHandler;
import rafradek.TF2weapons.message.TF2UseHandler;
import rafradek.TF2weapons.message.TF2WeaponDataHandler;
import rafradek.TF2weapons.message.TF2WearableChangeHandler;
import rafradek.TF2weapons.projectiles.EntityBall;
import rafradek.TF2weapons.projectiles.EntityFlame;
import rafradek.TF2weapons.projectiles.EntityFlare;
import rafradek.TF2weapons.projectiles.EntityGrenade;
import rafradek.TF2weapons.projectiles.EntityJar;
import rafradek.TF2weapons.projectiles.EntityRocket;
import rafradek.TF2weapons.projectiles.EntityStickybomb;
import rafradek.TF2weapons.projectiles.EntitySyringe;
import rafradek.TF2weapons.upgrade.BlockUpgradeStation;
import rafradek.TF2weapons.upgrade.ContainerUpgrades;
import rafradek.TF2weapons.upgrade.GuiUpgradeStation;
import rafradek.TF2weapons.upgrade.MannCoBuilding;
import rafradek.TF2weapons.upgrade.TileEntityUpgrades;
import rafradek.TF2weapons.weapons.InventoryAmmoBelt;
import rafradek.TF2weapons.weapons.ItemAmmo;
import rafradek.TF2weapons.weapons.ItemAmmoBelt;
import rafradek.TF2weapons.weapons.ItemDisguiseKit;
import rafradek.TF2weapons.weapons.ItemFireAmmo;
import rafradek.TF2weapons.weapons.ItemHorn;
import rafradek.TF2weapons.weapons.ItemMeleeWeapon;
import rafradek.TF2weapons.weapons.ItemProjectileWeapon;
import rafradek.TF2weapons.weapons.ItemSniperRifle;
import rafradek.TF2weapons.weapons.ItemWeapon;
import rafradek.TF2weapons.weapons.ItemUsable;
import rafradek.TF2weapons.weapons.ItemWrench;
import rafradek.TF2weapons.weapons.WeaponsCapability;

@Mod(modid = "rafradek_tf2_weapons", name =  "TF2 Stuff Mod", version = "0.7.1",guiFactory = "rafradek.TF2weapons.TF2GuiFactory", dependencies = "required-after:DynamicLights")
public class TF2weapons
{
	
	public static final String MOD_ID="rafradek_tf2_weapons";
	@Metadata("rafradek_tf2_weapons")
	public static ModMetadata metadata;
	
	@CapabilityInject(WeaponsCapability.class)
	public static final Capability<WeaponsCapability> WEAPONS_CAP = null;
	
	@CapabilityInject(InventoryWearables.class)
	public static final Capability<InventoryWearables> INVENTORY_CAP = null;
	
	@CapabilityInject(InventoryAmmoBelt.class)
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = null;
    public int[] itemid = new int[9];
    public static Configuration conf;
    
	public static CreativeTabs tabutilitytf2;
	public static CreativeTabs tabweapontf2;
	public static CreativeTabs tabsurvivaltf2;
    //public static final ArmorMaterial OPARMOR = EnumHelper.addArmorMaterial("OPARMOR", "", 1000, new int[] {24,0,0,0}, 100);
    public static SimpleNetworkWrapper network;
    public static Item itemPlacer;
    public static Item mobHeldItem;
    
	private static int weaponVersion;
	
	
	public static boolean destTerrain;
	public static boolean medigunLock;
	public static boolean fastMetalProduction;
	public static boolean sentryAttacksPlayers;
	public static boolean sentryAttacksMobs;
	public static boolean dispenserHeal;
	public static boolean disableSpawn;
	public static boolean randomCrits;
	public static String spawnOres;
	public static String naturalCheck;
	public static boolean dynamicLights;
	public static boolean dynamicLightsProj;
	@Instance(value="rafradek_tf2_weapons")
	public static TF2weapons instance;
	
    public File weaponDir;


	public static Block blockCabinet;
	public static Block blockCopperOre;
	public static Block blockLeadOre;
	public static Block blockAustraliumOre;
	public static Block blockAustralium;
	public static Block blockUpgradeStation;
	public static Block blockAmmoFurnace;
	
	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;
	
	public static Potion bonk;
	public static Potion stun;
	public static Potion crit;
	public static Potion buffbanner;
	public static Potion backup;
	public static Potion conch;
	public static Potion markDeath;
	public static Potion jarate;
	public static Potion madmilk;
	public static Potion critBoost;
	public static Potion charging;
	
	public static Item itemDisguiseKit;
	public static Item itemBuildingBox;
	public static Item itemSandvich;
	public static Item itemChocolate;
	public static Item itemAmmo;
	public static Item itemAmmoFire;
	public static Item itemAmmoMedigun;
	public static Item itemAmmoBelt;
	public static Item itemScoutBoots;
	public static Item itemMantreads;
	public static Item itemTF2;
	public static Item itemHorn;
	
	public static ResourceLocation lootTF2Character;
	public static ResourceLocation lootScout;
	public static ResourceLocation lootSpy;
	public static ResourceLocation lootHeavy;
	public static ResourceLocation lootEngineer;
	public static ResourceLocation lootMedic;
	public static ResourceLocation lootPyro;
	public static ResourceLocation lootSoldier;
	public static ResourceLocation lootDemoman;
	public static ResourceLocation lootSniper;
	public static ResourceLocation lootHale;
	public static MinecraftServer server;
	
	public static EntityLivingBase dummyEnt=new EntityCreeper(null);
	
    public static int getCurrentWeaponVersion(){
    	return 11;
    }
    
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
    	this.weaponDir=new File(event.getModConfigurationDirectory(),"TF2WeaponsLists");
    	if(!this.weaponDir.exists()){
    		this.weaponDir.mkdirs();
    	}
    	metadata.autogenerated=false;
    	
    	conf = new Configuration(event.getSuggestedConfigurationFile());
    	boolean shouldCopy=false;
    	if(!conf.hasKey("internal", "Weapon Config Version")){
    		shouldCopy=true;
    	}
    	createConfig();
    	File outputFile=new File(this.weaponDir,"Weapons.json");
    	File outputFile2=new File(this.weaponDir,"Cosmetics.json");
    	File outputFile3=new File(this.weaponDir,"Crates.json");
    	File file=event.getSourceFile();
    	//System.out.println("LOLOLOLOLOLOL "+file.getAbsolutePath());
    	//System.out.println("LOLOLOLOLOLOL2 "+event.getModConfigurationDirectory());
    	//System.out.println("Istnieje? "+outputFile.exists());
    	if(weaponVersion<getCurrentWeaponVersion()){
    		shouldCopy=true;
    	}
    	if(!outputFile.exists()||shouldCopy){
    		conf.get("internal", "Weapon Config Version", getCurrentWeaponVersion()).set(getCurrentWeaponVersion());
    		conf.save();
    		
    		if(file.isFile()){
				try {	
			    	ZipFile zip = new ZipFile(file);
			    	ZipEntry entry = zip.getEntry("Weapons.json");
			    	ZipEntry entryHats = zip.getEntry("Cosmetics.json");
			    	ZipEntry entryCrates = zip.getEntry("Crates.json");
			    	if(entry!=null) {
					
				    	InputStream zin = zip.getInputStream(entry);
				    	byte[] bytes=new byte[(int) entry.getSize()];
				    	zin.read(bytes);
				    	FileOutputStream str=new FileOutputStream(outputFile);
				    	str.write(bytes);
				    	str.close();
				    	zin.close();
				    	
					}
			    	if(entryHats!=null) {
						
				    	InputStream zin = zip.getInputStream(entryHats);
				    	byte[] bytes=new byte[(int) entryHats.getSize()];
				    	zin.read(bytes);
				    	FileOutputStream str=new FileOutputStream(outputFile2);
				    	str.write(bytes);
				    	str.close();
				    	zin.close();
				    	
					}
			    	if(entryCrates!=null) {
						
				    	InputStream zin = zip.getInputStream(entryCrates);
				    	byte[] bytes=new byte[(int) entryCrates.getSize()];
				    	zin.read(bytes);
				    	FileOutputStream str=new FileOutputStream(outputFile3);
				    	str.write(bytes);
				    	str.close();
				    	zin.close();
				    	
					}
			    	zip.close();
		    	} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	else{
	    		try {	
	    			File inputFile=new File(file,"Weapons.json");
	    			File inputFileHats=new File(file,"Cosmetics.json");
	    			File inputFileCrates=new File(file,"Crates.json");
			    	FileInputStream istr=new FileInputStream(inputFile);
			
			    	byte[] bytes=new byte[(int) inputFile.length()];
			    	istr.read(bytes);
			    	FileOutputStream str=new FileOutputStream(outputFile);
			    	str.write(bytes);
			    	str.close();
			    	istr.close();
			    	
			    	istr=new FileInputStream(inputFileHats);
					
			    	bytes=new byte[(int) inputFileHats.length()];
			    	istr.read(bytes);
			    	str=new FileOutputStream(outputFile2);
			    	str.write(bytes);
			    	str.close();
			    	istr.close();
			    	
			    	istr=new FileInputStream(inputFileCrates);
					
			    	bytes=new byte[(int) inputFileCrates.length()];
			    	istr.read(bytes);
			    	str=new FileOutputStream(outputFile3);
			    	str.write(bytes);
			    	str.close();
			    	istr.close();
			    	
		    	} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
    	
    	
    	MapList.initMaps();
    	TF2Attribute.initAttributes();
    	tabweapontf2=new CreativeTabs("tf2weapons"){
			@Override
			public Item getTabIconItem() {
				return MapList.weaponClasses.get("bullet");
			}
		};
    	tabutilitytf2=new CreativeTabs("tf2util"){
			@Override
			public Item getTabIconItem() {
				return itemDisguiseKit;
			}
		};
		tabsurvivaltf2=new CreativeTabs("tf2misc"){
			@Override
			public Item getTabIconItem() {
				return ItemBlock.getItemFromBlock(blockCabinet);
			}
		};
    	//EntityRegistry.registerModEntity(EntityBullet.class, "bullet", 1, this, 256, 100, false);
    	EntityRegistry.registerModEntity(EntityHeavy.class, "heavy", 2, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityScout.class, "scout", 3, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntitySniper.class, "sniper", 4, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntitySoldier.class, "soldier", 5, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityPyro.class, "pyro", 6, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityDemoman.class, "demoman", 7, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityMedic.class, "medic", 8, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntitySpy.class, "spy", 9, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityEngineer.class, "engineer", 10, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityRocket.class, "rocket", 11, this, 64,20, false);
    	EntityRegistry.registerModEntity(EntityFlame.class, "flame", 12, this, 0,20, false);
    	EntityRegistry.registerModEntity(EntityGrenade.class, "grenade", 13, this, 64,5, true);
    	EntityRegistry.registerModEntity(EntityStickybomb.class, "sticky", 14, this, 64,5, true);
    	EntityRegistry.registerModEntity(EntitySyringe.class, "syringe", 15, this, 64,20, false);
    	EntityRegistry.registerModEntity(EntityFlare.class, "flare", 20, this, 64,20, false);
    	EntityRegistry.registerModEntity(EntityJar.class, "jar", 21, this, 64,20, false);
    	EntityRegistry.registerModEntity(EntityBall.class, "ball", 22, this, 64,10, true);
    	EntityRegistry.registerModEntity(EntitySentry.class, "sentry", 16, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityDispenser.class, "dispenser", 17, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityTeleporter.class, "teleporter", 18, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntitySaxtonHale.class, "hale", 19, this, 80,3, true);
    	EntityRegistry.registerModEntity(EntityMonoculus.class, "monoculus", 23, this, 80,3, true);
    	//GameRegistry.registerItem(new ItemArmor(TF2weapons.OPARMOR, 3, 0).setUnlocalizedName("oparmor").setTextureName("diamond_helmet").setCreativeTab(tabtf2),"oparmor");
    	GameRegistry.register(itemPlacer=new ItemMonsterPlacerPlus().setUnlocalizedName("monsterPlacer").setRegistryName(TF2weapons.MOD_ID+":placer"));
    	GameRegistry.register(itemDisguiseKit=new ItemDisguiseKit().setUnlocalizedName("disguiseKit").setRegistryName(TF2weapons.MOD_ID+":disguise_kit"));
    	GameRegistry.register(itemBuildingBox=new ItemBuildingBox().setUnlocalizedName("buildingBox").setRegistryName(TF2weapons.MOD_ID+":building_box"));
    	GameRegistry.register(itemSandvich=new ItemFood(14, 1, false).setPotionEffect(new PotionEffect(MobEffects.REGENERATION,120,2), 1f).setUnlocalizedName("sandvich").setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID+":sandvich"));
    	GameRegistry.register(itemChocolate=new ItemFood(7, 0.6F, false).setPotionEffect(new PotionEffect(MobEffects.ABSORPTION,600,1), 1f).setUnlocalizedName("chocolate").setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID+":chocolate"));
    	GameRegistry.register(itemHorn=new ItemHorn().setUnlocalizedName("horn").setRegistryName(TF2weapons.MOD_ID+":horn"));
    	GameRegistry.register(itemAmmo=new ItemAmmo().setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID+":ammo"));
    	GameRegistry.register(itemAmmoFire=new ItemFireAmmo(10,300).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID+":ammo_fire"));
    	GameRegistry.register(itemAmmoMedigun=new ItemFireAmmo(12,700).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID+":ammo_medigun"));
    	GameRegistry.register(itemAmmoBelt=new ItemAmmoBelt().setUnlocalizedName("ammoBelt").setRegistryName(TF2weapons.MOD_ID+":ammo_belt").setCreativeTab(tabsurvivaltf2));
    	GameRegistry.register(itemScoutBoots=new ItemArmor(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET).setUnlocalizedName("scoutBoots").setRegistryName(TF2weapons.MOD_ID+":scout_shoes").setCreativeTab(tabutilitytf2));
    	GameRegistry.register(itemMantreads=new ItemArmor(ArmorMaterial.IRON, 0, EntityEquipmentSlot.FEET).setUnlocalizedName("mantreads").setRegistryName(TF2weapons.MOD_ID+":mantreads").setCreativeTab(tabutilitytf2));
    	//GameRegistry.register(itemCopperIngot=new Item().setUnlocalizedName("ingotCopper").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotCopper"));
    	//GameRegistry.register(itemLeadIngot=new Item().setUnlocalizedName("ingotLead").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotLead"));
    	//GameRegistry.register(itemAustraliumIngot=new Item().setUnlocalizedName("ingotAustralium").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotAustralium"));
    	GameRegistry.register(itemTF2=new ItemTF2().setRegistryName(TF2weapons.MOD_ID+":itemTF2"));
    	
    	
    	
    	GameRegistry.registerTileEntity(TileEntityCabinet.class,"TF2Cabinet");
    	GameRegistry.registerTileEntity(TileEntityUpgrades.class,"UpgradeStation");
    	GameRegistry.registerTileEntity(TileEntityAmmoFurnace.class,"AmmoFurnace");
    	
    	registerBlock(blockCabinet=new BlockCabinet().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("cabinet"),TF2weapons.MOD_ID+":tf2workbench");
    	registerBlock(blockAmmoFurnace=new BlockAmmoFurnace().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("ammoFurnace"),TF2weapons.MOD_ID+":ammo_furnace");
    	registerBlock(blockUpgradeStation=new BlockUpgradeStation().setBlockUnbreakable().setResistance(10.0F).setUnlocalizedName("upgradeStation"),TF2weapons.MOD_ID+":upgrade_station");
    	registerBlock(blockCopperOre=new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreCopper"),TF2weapons.MOD_ID+":copper_ore");
    	registerBlock(blockLeadOre=new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreLead"),TF2weapons.MOD_ID+":lead_ore");
    	registerBlock(blockAustraliumOre=new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(6.0F).setResistance(10.0F).setUnlocalizedName("oreAustralium"),TF2weapons.MOD_ID+":australium_ore");
    	registerBlock(blockAustralium=new Block(Material.IRON,MapColor.GOLD).setCreativeTab(tabsurvivaltf2).setHardness(9.0F).setResistance(20.0F).setUnlocalizedName("blockAustralium"),TF2weapons.MOD_ID+":australium_block");
    	
    	OreDictionary.registerOre("oreCopper", blockCopperOre);
    	OreDictionary.registerOre("oreLead", blockLeadOre);
    	OreDictionary.registerOre("oreAustralium", blockAustraliumOre);
    	OreDictionary.registerOre("blockAustralium", blockAustralium);
    	OreDictionary.registerOre("ingotCopper", new ItemStack(itemTF2,1,0));
    	OreDictionary.registerOre("ingotLead", new ItemStack(itemTF2,1,1));
    	OreDictionary.registerOre("ingotAustralium", new ItemStack(itemTF2,1,2));
    	OreDictionary.registerOre("nuggetAustralium", new ItemStack(itemTF2,1,9));
    	
    	blockCopperOre.setHarvestLevel("pickaxe", 1);
    	blockLeadOre.setHarvestLevel("pickaxe", 1);
    	blockAustraliumOre.setHarvestLevel("pickaxe", 2);
    	
    	
    	ItemAmmo.STACK_FILL=new ItemStack(itemAmmo);
    	
    	CapabilityManager.INSTANCE.register(WeaponsCapability.class, new NullStorage<WeaponsCapability>(), new Callable<WeaponsCapability>(){

			@Override
			public WeaponsCapability call() throws Exception {
				// TODO Auto-generated method stub
				return new WeaponsCapability(null);
			}
    		
    	});
    	CapabilityManager.INSTANCE.register(InventoryWearables.class, new NullStorage<InventoryWearables>(), new Callable<InventoryWearables>(){

			@Override
			public InventoryWearables call() throws Exception {
				// TODO Auto-generated method stub
				return new InventoryWearables();
			}
    		
    	});
    	
		GameRegistry.register(bonk=new PotionTF2Item(false, 0x696969,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/bonk.png")).setPotionName("effect.bonk").setRegistryName(TF2weapons.MOD_ID+":bonkEff"));
		GameRegistry.register(stun=new PotionTF2(true, 0,3,1).setPotionName("effect.stun").setRegistryName(TF2weapons.MOD_ID+":stunEff").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354F0D8BA", -0.5D, 2));
		GameRegistry.register(crit=new PotionTF2Item(false, 0,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/critacola.png")).setPotionName("effect.crit").setRegistryName(TF2weapons.MOD_ID+":critEff").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354E56B59", 0.25D, 2));
		GameRegistry.register(buffbanner=new PotionTF2Item(false, 0,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/buffbanner.png")).setPotionName("effect.banner").setRegistryName(TF2weapons.MOD_ID+":bannerEff"));
		GameRegistry.register(backup=new PotionTF2Item(false, 0,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/backup.png")).setPotionName("effect.backup").setRegistryName(TF2weapons.MOD_ID+":backupEff"));
		GameRegistry.register(conch=new PotionTF2Item(false, 0,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/conch.png")).setPotionName("effect.conch").setRegistryName(TF2weapons.MOD_ID+":conchEff").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E2", 0.25D, 2));
		GameRegistry.register(markDeath=new PotionTF2(true, 0, 1, 2).setPotionName("effect.markDeath").setRegistryName(TF2weapons.MOD_ID+":markDeathEff"));
		GameRegistry.register(critBoost=new PotionTF2(false, 0, 4, 0).setPotionName("effect.critBoost").setRegistryName(TF2weapons.MOD_ID+":critBoostEff"));
		GameRegistry.register(jarate=new PotionTF2Item(true, 0xFFD500,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/jarate.png")).setPotionName("effect.jarate").setRegistryName(TF2weapons.MOD_ID+":jarateEff"));
		GameRegistry.register(madmilk=new PotionTF2Item(true, 0xF1F1F1,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/madmilk.png")).setPotionName("effect.madmilk").setRegistryName(TF2weapons.MOD_ID+":madmilkEff"));
		GameRegistry.register(charging=new PotionTF2Item(false, 0,new ResourceLocation(TF2weapons.MOD_ID,"textures/items/charging_targe.png")).setPotionName("effect.charging").setRegistryName(TF2weapons.MOD_ID+":chargingEff").registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E6", 2D, 2));
		//conf.save();
		
		MapGenStructureIO.registerStructureComponent(MannCoBuilding.class, "ViMC");
		VillagerRegistry.instance().registerVillageCreationHandler(new MannCoBuilding.CreationHandler());
		
		proxy.preInit();
    }
    
    public static void createConfig(){
    	destTerrain=conf.getBoolean("Destructible terrain", "gameplay", false,"Explosions can destroy blocks");
    	medigunLock=conf.getBoolean("Medigun lock target", "gameplay", false,"Left Click selects healing target");
    	fastMetalProduction=conf.getBoolean("Fast metal production", "gameplay", false,"Dispensers produce metal every 5 seconds");
    	dispenserHeal=conf.getBoolean("Dispensers heal players", "gameplay", true,"Dispensers heal other players");
    	sentryAttacksPlayers=conf.getBoolean("Sentry attacks players", "gameplay", false,"Sentries attack other players");
    	sentryAttacksMobs=conf.getBoolean("Sentry attacks mobs", "gameplay", true,"Sentries attack enemy mobs");
    	disableSpawn=conf.getBoolean("Disable mob spawning", "gameplay", false,"Disable mod-specific mobs spawning (Requires game restart)");
    	weaponVersion=conf.getInt("Weapon Config Version", "internal", getCurrentWeaponVersion(), 0, 1000, "");
    	conf.get("gameplay", "Disable mob spawning",  false).setRequiresMcRestart(true);
    	spawnOres=conf.get("gameplay", "Spawn ores", "Default").setValidValues(new String[]{"Always","Default","Never"}).getString();
    	naturalCheck=conf.get("gameplay","Natural mob detection", "Always").setValidValues(new String[]{"Always","Fast","Never"}).getString();
    	dynamicLights=conf.getBoolean("Dynamic Lights","modcompatibility", true,"Enables custom light sources for AtomicStryker's Dynamic Lights mod") && Loader.isModLoaded("DynamicLights");
    	dynamicLightsProj=conf.getBoolean("Dynamic Lights - Projectiles","modcompatibility", true,"Should projectiles emit light");
    	randomCrits=conf.getBoolean("Random critical hits","gameplay", true,"Enables randomly appearing critical hits that deal 3x more damage");
    	updateOreGenStatus();
    	
        if(conf.hasChanged())
            conf.save();
    }
    
    public static void syncConfig(){
    	destTerrain=conf.get("gameplay", "Destructible terrain", false).getBoolean();
    	medigunLock=conf.get("gameplay", "Medigun lock target",  false).getBoolean();
    	fastMetalProduction=conf.get("gameplay", "Fast metal production",  false).getBoolean();
    	dispenserHeal=conf.get("gameplay", "Dispensers heal players",  true).getBoolean();
    	sentryAttacksPlayers=conf.get("gameplay", "Sentry attacks players",  false).getBoolean();
    	sentryAttacksMobs=conf.get("gameplay", "Sentry attacks mobs",  true).getBoolean();
    	disableSpawn=conf.get("gameplay", "Disable mob spawning",  false).getBoolean();
    	spawnOres=conf.get("gameplay", "Spawn ores", "Default").getString();
    	naturalCheck=conf.get("gameplay","Natural mob detection", "Always").getString();
    	dynamicLights=conf.get("modcompatibility","Dynamic Lights",  true).getBoolean() && Loader.isModLoaded("DynamicLights");
    	randomCrits=conf.get("gameplay", "Random critical hits",  true).getBoolean();
    	dynamicLightsProj=conf.get("modcompatibility","Dynamic Lights - Projectiles",  true).getBoolean();
    	updateOreGenStatus();
    	
        conf.save();
    }
    
    @SidedProxy(clientSide = "rafradek.TF2weapons.ClientProxy", serverSide = "rafradek.TF2weapons.CommonProxy")
    public static CommonProxy proxy;
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
    	GameRegistry.addSmelting(new ItemStack(blockCopperOre), new ItemStack(itemTF2,1,0), 0.5f);
    	GameRegistry.addSmelting(new ItemStack(blockLeadOre), new ItemStack(itemTF2,1,1), 0.55f);
    	GameRegistry.addSmelting(new ItemStack(blockAustraliumOre), new ItemStack(itemTF2,1,2), 2f);
    	GameRegistry.addSmelting(new ItemStack(itemTF2,1,3), new ItemStack(Items.IRON_INGOT,2), 0.35f);
    	GameRegistry.registerFuelHandler(new IFuelHandler(){

			@Override
			public int getBurnTime(ItemStack fuel) {
				// TODO Auto-generated method stub
				return fuel.getItem() instanceof ItemCrate?300:0;
			}
    		
    	});
    	GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCabinet),"SCS","SIS",'S',new ItemStack(itemTF2,1,3),'C',"workbench",'I',"blockIron"));
    	GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemTF2,1,2),"AAA","AAA","AAA",'A',new ItemStack(itemTF2,1,6)));
    	GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemTF2,9,6),"ingotAustralium"));
    	GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockAustralium),"AAA","AAA","AAA",'A',new ItemStack(itemTF2,1,2)));
    	GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemTF2,9,2),"blockAustralium"));
    	GameRegistry.addRecipe(new OpenCrateRecipe());
    	
    	LootFunctionManager.registerFunction( new EntityBuildingFunction.Serializer());
    	LootFunctionManager.registerFunction( new EntityOfClassFunction.Serializer());
    	LootConditionManager.registerCondition(new KilledByTeam.Serializer());
    	
    	lootTF2Character=LootTableList.register(new ResourceLocation(MOD_ID,"entities/tf2character"));
    	lootScout=LootTableList.register(new ResourceLocation(MOD_ID,"entities/scout"));
    	lootHeavy=LootTableList.register(new ResourceLocation(MOD_ID,"entities/heavy"));
    	lootSniper=LootTableList.register(new ResourceLocation(MOD_ID,"entities/sniper"));
    	lootSpy=LootTableList.register(new ResourceLocation(MOD_ID,"entities/spy"));
    	lootDemoman=LootTableList.register(new ResourceLocation(MOD_ID,"entities/demoman"));
    	lootEngineer=LootTableList.register(new ResourceLocation(MOD_ID,"entities/engineer"));
    	lootSoldier=LootTableList.register(new ResourceLocation(MOD_ID,"entities/soldier"));
    	lootMedic=LootTableList.register(new ResourceLocation(MOD_ID,"entities/medic"));
    	lootPyro=LootTableList.register(new ResourceLocation(MOD_ID,"entities/pyro"));
    	lootHale=LootTableList.register(new ResourceLocation(MOD_ID,"entities/hale"));
    	
		WeaponData.PropertyType.init();
    	ArrayList<Biome> biomesList=new ArrayList<Biome>();
    	for(Biome biome:GameRegistry.findRegistry(Biome.class)){
    		biomesList.add(biome);
    	}
    	if(!disableSpawn){
	    	Biome[] biomes=biomesList.toArray(new Biome[biomesList.size()]);
	    	EntityRegistry.addSpawn(EntitySpy.class, 9, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntityPyro.class, 12, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntityDemoman.class, 12, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntitySoldier.class, 12, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntitySniper.class, 9, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntityHeavy.class, 12, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntityScout.class, 12, 1, 3, EnumCreatureType.MONSTER,biomes);
	    	EntityRegistry.addSpawn(EntityEngineer.class, 9, 1, 3, EnumCreatureType.MONSTER,biomes);
    	}
    	
    	//new Item(2498).setUnlocalizedName("FakeItem").setTextureName(TF2weapons.MOD_ID+":saw").setCreativeTab(CreativeTabs.tabBlock);
    	BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemPlacer, new BehaviorDefaultDispenseItem()
    	{
            @Override
			public ItemStack dispenseStack(IBlockSource p_82487_1_, ItemStack p_82487_2_)
            {
                EnumFacing enumfacing = p_82487_1_.func_189992_e().getValue(BlockDispenser.FACING);
                double d0 = p_82487_1_.getX() + enumfacing.getFrontOffsetX();
                double d1 = p_82487_1_.getY() + 0.2F;
                double d2 = p_82487_1_.getZ() + enumfacing.getFrontOffsetZ();
                Entity entity = ItemMonsterPlacerPlus.spawnCreature(p_82487_1_.getWorld(), p_82487_2_.getItemDamage(), d0, d1, d2, p_82487_2_.getTagCompound()!=null&&p_82487_2_.getTagCompound().hasKey("SavedEntity")?p_82487_2_.getTagCompound().getCompoundTag("SavedEntity"):null);

                if (entity instanceof EntityLivingBase && p_82487_2_.hasDisplayName())
                {
                    ((EntityLiving)entity).setCustomNameTag(p_82487_2_.getDisplayName());
                }

                p_82487_2_.splitStack(1);
                return p_82487_2_;
            }
        });
    	
    	Iterator<String> iterator= MapList.weaponClasses.keySet().iterator();
    	while(iterator.hasNext()){
    		String name=iterator.next();
    		GameRegistry.register(MapList.weaponClasses.get(name), new ResourceLocation(MOD_ID,""+name.toLowerCase()));
    	}
    	if(event.getSide()==Side.CLIENT){
    		loadWeapons();
    	}
    	TF2Sounds.registerSounds();
    	
        network=NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Side.SERVER);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Side.SERVER);
		network.registerMessage(TF2BulletHandler.class, TF2Message.BulletMessage.class, 3, Side.SERVER);
		network.registerMessage(TF2ProjectileHandler.class, TF2Message.PredictionMessage.class, 4, Side.SERVER);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 5, Side.SERVER);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 7, Side.SERVER);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 9, Side.SERVER);
		network.registerMessage(TF2DisguiseHandler.class, TF2Message.DisguiseMessage.class, 11, Side.SERVER);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Side.CLIENT);
    	network.registerMessage(TF2UseHandler.class, TF2Message.UseMessage.class, 1, Side.CLIENT);
    	network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Side.CLIENT);
    	network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 6, Side.CLIENT);
    	network.registerMessage(TF2WeaponDataHandler.class, TF2Message.WeaponDataMessage.class, 8, Side.CLIENT);
    	network.registerMessage(TF2WearableChangeHandler.class, TF2Message.WearableChangeMessage.class, 10, Side.CLIENT);
    	NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler(){

			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos=new BlockPos(x,y,z);
				if(ID==0)
					return new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), false, player);
				else if(ID==1 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityCabinet)
					return new ContainerTF2Workbench(player,player.inventory, /*(TileEntityCabinet) world.getTileEntity(pos),*/ world,pos);
				else if(ID==2 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityUpgrades)
					return new ContainerUpgrades(player,player.inventory, (TileEntityUpgrades) world.getTileEntity(pos), world,pos);
				else if(ID==3 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new ContainerAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getTileEntity(pos));
				return null;
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos=new BlockPos(x,y,z);
				if(ID==0)
					return new GuiWearables(new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), true, player));
				else if(ID==1 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityCabinet)
					return new GuiTF2Crafting(player.inventory, /*(TileEntityCabinet) world.getTileEntity(pos),*/world, pos);
				else if(ID==2 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityUpgrades)
					return new GuiUpgradeStation(player.inventory, (TileEntityUpgrades) world.getTileEntity(pos),world, pos);
				else if(ID==3 && world.getTileEntity(pos)!=null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new GuiAmmoFurnace(player.inventory, (TileEntityAmmoFurnace)world.getTileEntity(pos));
				return null;
			}
    		
    	});
        //TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
        MinecraftForge.EVENT_BUS.register(new TF2EventBusListener());
        MinecraftForge.ORE_GEN_BUS.register(new TF2EventBusListener());
        //FMLCommonHandler.instance().bus().register(new TF2EventBusListener());
        proxy.registerRenderInformation();
        MapList.nameToData.clear();
    	MapList.buildInAttributes.clear();
    	
    	
    }
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
    	
    	//System.out.println("generateCopper: "+generateCopper);
    	//System.out.println("generateLead: "+generateLead);
    }
    
    public static void updateOreGenStatus(){
    	
    	generateCopper=false;
		generateLead=false;
		generateAustralium=false;
		
    	if(spawnOres.equals("Always")){
    		generateCopper=true;
    		generateLead=true;
    		generateAustralium=true;
    	}
    	else if(spawnOres.equals("Default")){
    		generateAustralium=true;
    		if(OreDictionary.getOres("oreCopper").size()==1){
        		generateCopper=true;
        	}
        	if(OreDictionary.getOres("oreLead").size()==1){
        		generateLead=true;
        	}
    	}
    }
    
    public static void registerBlock(Block block,String name){
    	GameRegistry.register(block.setRegistryName(name));
    	ItemBlock item=new ItemBlock(block);
    	item.setRegistryName(block.getRegistryName());
    	GameRegistry.register(item);
    	proxy.registerItemBlock(item);
    }
    public static TargetPoint pointFromEntity(Entity entity){
    	return new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 256);
    }
    public void loadWeapons() {
    	
    	MapList.nameToData.clear();
    	MapList.buildInAttributes.clear();
    	this.loadConfig(new File(this.weaponDir,"Weapons.json"));
    	File[] files=this.weaponDir.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".json")&&!arg1.equalsIgnoreCase("Weapons.json");
			}
    		
    	});
    	for(File file:files){
    		this.loadConfig(file);
    	}
    }
    public void loadConfig(File file){
    	/*Configuration weaponsFile= new Configuration(file);
        weaponsFile.load();*/
    	ArrayList<WeaponData> list=WeaponData.parseFile(file);
    	for(WeaponData data:list){
    		String weaponEntry= data.getName();
    		//Class<?> weaponClass = MapList.weaponClasses.get(weaponData.get("Class").getString());
    		try {
    			//System.out.println("attach "+weaponEntry);
    			if(PropertyType.BASED_ON.hasKey(data)&&MapList.nameToData.containsKey(PropertyType.BASED_ON.getString(data))) {
    				//System.out.println("attach "+weaponData.getName()+" "+weaponData.get("Based on").getString());
    				data=attach(MapList.nameToData.get(PropertyType.BASED_ON.getString(data)),data);
    				//weaponList[Integer.parseInt(weaponEntry)] =(ItemUsable) weaponClass.getConstructor(new Class[] {ConfigCategory.class, ConfigCategory.class}).newInstance(new Object[] {weaponPreData, weaponData});
    			}
    			loadWeapon(weaponEntry,data);
    			
    		}catch (Exception var4){
                var4.printStackTrace();
            }
    	}
    }
    public static void loadWeapon(String name,WeaponData weapon){
    	IForgeRegistry<SoundEvent> registry=GameRegistry.findRegistry(SoundEvent.class);
		for(PropertyType propType:weapon.properties.keySet()){
			if(propType.name.contains("sound")){
				ResourceLocation soundLocation=new ResourceLocation(propType.getString(weapon));
				if(!registry.containsKey(soundLocation)){
					GameRegistry.register(new SoundEvent(soundLocation),soundLocation);
					if(propType.name.equals("Fire sound")||propType.name.equals("Fire loop sound")){
						GameRegistry.register(new SoundEvent(new ResourceLocation(propType.getString(weapon)+".crit")),new ResourceLocation(propType.getString(weapon)+".crit"));
					}
				}
			}
		}
		/*else{
			weaponList[Integer.parseInt(weaponEntry)] =(ItemUsable) weaponClass.getConstructor(new Class[] {ConfigCategory.class, ConfigCategory.class}).newInstance(new Object[] {weaponData, null});
		}*/
		//GameRegistry.registerItem(weaponList[Integer.parseInt(weaponEntry)], "weapon"+Integer.parseInt(weaponEntry));
		MapList.nameToData.put(name, weapon);
		//System.out.println("Weapon read: "+name);
		/*for(Entry<PropertyType, WeaponData.Property> entry:weapon.properties.entrySet()){
			System.out.println("Property: "+entry.getKey().name+" Value: "+entry.getValue().intValue+" "+entry.getValue().stringValue);
		}*/
		//LanguageRegistry.instance().addStringLocalization(weaponData.get("Name").getString()+".name", weaponData.get("Name").getString());
		NBTTagCompound tag=new NBTTagCompound();
		tag.setString("Type", name);
		NBTTagCompound tag2=new NBTTagCompound();
		if(!weapon.attributes.isEmpty()){
        	for(Entry<TF2Attribute,Float> entry:weapon.attributes.entrySet()){
        		tag2.setFloat(String.valueOf(entry.getKey().id), entry.getValue());
        	}
        }
		tag.setTag("Attributes", tag2);
		MapList.buildInAttributes.put(name, tag);
    }
    public static WeaponData attach(WeaponData base, WeaponData additional){
    	for(PropertyType prop:base.properties.keySet()){
    		if(!additional.properties.containsKey(prop)){
    			additional.properties.put(prop, base.properties.get(prop));
    			//System.out.println("merged: "+additional.getName()+" "+key);
    		}
    	}
    	
    	//new ConfigCategory(null, additional);
    	return additional;
    }
    @Mod.EventHandler
    public void serverPreInit(FMLServerAboutToStartEvent event)
    {
    	//System.out.println("Starting server");
    	loadWeapons();
    	if(!event.getServer().isDedicatedServer()){
    		for(WeaponData weapon:MapList.nameToData.values()){
    			ClientProxy.RegisterWeaponData(weapon);
    		}
    	}
    }
    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new CommandGiveWeapon());
    	try {
    		server=event.getServer();
			File input=new File(((AnvilSaveConverter)server.getActiveAnvilConverter()).savesDirectory,server.getFolderName()+"/teleports.dat");
	    	NBTTagCompound tagRoot=CompressedStreamTools.readCompressed(new FileInputStream(input));
	    	NBTTagCompound tag=tagRoot.getCompoundTag("Teleporters");
	    	for(String keys:tag.getKeySet()){
	    		TeleporterData[] blockArray=new TeleporterData[EntityTeleporter.TP_PER_PLAYER];
	    		EntityTeleporter.teleporters.put(UUID.fromString(keys),blockArray);
	    		NBTTagCompound exitTag=tag.getCompoundTag(keys);
	    		for(int i=0;i<EntityTeleporter.TP_PER_PLAYER;i++){
	    			if(exitTag.hasKey(Integer.toString(i))){
	    				int[] array=exitTag.getIntArray(Integer.toString(i));
	    				blockArray[i]=new TeleporterData(new BlockPos(array[0],array[1],array[2]),array[3],array[4]);
	    			}
	    		}
	    	}
	    	EntityTeleporter.tpCount=tagRoot.getInteger("TPCount");
	    	
    	} catch (IOException e) {
    		System.err.println("Reading teleporter data skipped");
		}
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
    	MapList.nameToData.clear();
    	MapList.buildInAttributes.clear();
    	
    	File output=new File(((AnvilSaveConverter)server.getActiveAnvilConverter()).savesDirectory,server.getFolderName()+"/teleports.dat");
    	NBTTagCompound tagRoot=new NBTTagCompound();
    	NBTTagCompound tag=new NBTTagCompound();
    	tagRoot.setTag("Teleporters", tag);
    	
    	for(Entry<UUID,TeleporterData[]> entry:EntityTeleporter.teleporters.entrySet()){
    		NBTTagCompound exitTag=new NBTTagCompound();
    		for(int i=0;i<EntityTeleporter.TP_PER_PLAYER;i++){
    			TeleporterData blockPos=entry.getValue()[i];
    			if(blockPos!=null){
    				exitTag.setIntArray(Integer.toString(i),new int[]{blockPos.getX(),blockPos.getY(),blockPos.getZ(),blockPos.id,blockPos.dimension});
    			}
    		}
    		tag.setTag(entry.getKey().toString(), exitTag);
    	}
    	tagRoot.setInteger("TPCount", EntityTeleporter.tpCount);
    	
    	try {
			CompressedStreamTools.writeCompressed(tagRoot, new FileOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	EntityTeleporter.teleporters.clear();
    	EntityTeleporter.tpCount=0;
    }
    
    /*public static void openWearableGUI(EntityPlayerMP player){
    	InventoryBasic inventory=new InventoryBasic("Wearables", false, 3);
    	NBTTagList nbttaglist = player.getEntityData().getTagList("Wearables", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            inventory.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
        }
        if (player.openContainer != player.inventoryContainer)
        {
        	player.closeScreen();
        }

        player.getNextWindowId();
        player.connection.sendPacket(new SPacketOpenWindow(player.currentWindowId, "rafradek_wearables", inventory.getDisplayName(), inventory.getSizeInventory(), player.getEntityId()));
        player.openContainer = new ContainerHorseInventory(player.inventory, inventory, horse, this);
        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.addListener(this);
    }*/
    
    public static int calculateCritPre(ItemStack stack, EntityLivingBase living){
		int thisCritical=0;
		if(randomCrits && stack != null && stack.getItem() instanceof ItemWeapon){
	    	ItemWeapon item=(ItemWeapon) stack.getItem();
	    	if((!item.rapidFireCrits(stack)&&item.hasRandomCrits(stack,living) && living.getRNG().nextFloat()<=item.critChance(stack, living))||living.getCapability(WEAPONS_CAP, null).critTime>0)
	            thisCritical=2;
		}
		if(living.getActivePotionEffect(critBoost)!=null)
	    	thisCritical=2;
	    if(thisCritical==0&&living.getActivePotionEffect(crit)!=null||living.getActivePotionEffect(buffbanner)!=null)
	    	thisCritical=1;
	    if(stack != null && stack.getItem() instanceof ItemMeleeWeapon && (living.getActivePotionEffect(charging)!=null ||living.getCapability(WEAPONS_CAP, null).ticksBash>0)){
	    	if(thisCritical<2&&(living.getCapability(WEAPONS_CAP, null).ticksBash>0&&living.getCapability(WEAPONS_CAP, null).bashCritical)||(living.getActivePotionEffect(charging)!=null&&living.getActivePotionEffect(charging).getDuration()<12)){
	    		thisCritical=2;
	    	}
	    	else if(thisCritical==0 && (living.getCapability(WEAPONS_CAP, null).ticksBash>0||living.getActivePotionEffect(charging).getDuration()<30)){
	    		thisCritical=1;
	    	}
	    }
	    return thisCritical;
	}

	public static DamageSource causeBulletDamage(ItemStack weapon, Entity shooter, int critical, Entity projectile)
    {
        return (new DamageSourceBullet(weapon, projectile,shooter,critical)).setProjectile();
    }
    
    public static DamageSource causeDirectDamage(ItemStack weapon, Entity shooter, int critical)
    {
        return (new DamageSourceDirect(weapon, shooter,critical));
    }
    
    public static int roundUp(double value, int zero)
    {
        if (value > (int)value)
        {
            return (int)value + 1;
        }

        return (int)value;
    }
    public static double[] radiusRandom3D(float radius,Random random){
    	double x=Double.MAX_VALUE,y=Double.MAX_VALUE,z=Double.MAX_VALUE;
    	while (Math.sqrt(x*x+y*y+z*z)>radius){
    		x=random.nextDouble()*radius*2-radius;
    		y=random.nextDouble()*radius*2-radius;
    		z=random.nextDouble()*radius*2-radius;
    	}
    	return new double[]{x,y,z};
    	
    }
    public static double[] radiusRandom2D(float radius,Random random){
    	/*double x=random.nextDouble()*radius*2-radius;
    	radius -= Math.abs(x);
    	double y=random.nextDouble()*radius*2-radius;*/
    	
    	/*double t = 4*Math.PI*random.nextDouble()*radius-radius;
    	double u = (random.nextDouble()*radius*2-radius)+(random.nextDouble()*radius*2-raddddddddius);
    	double r = u>1?2-u:u;*/
    	float a=random.nextFloat(),b=random.nextFloat();
    	return new double[]{Math.max(a, b)*radius*Math.cos(2*Math.PI*Math.min(a, b)/Math.max(a, b)),Math.max(a, b)*radius*Math.sin(2*Math.PI*Math.min(a, b)/Math.max(a, b))};
    }
    public static List<RayTraceResult> pierce(World world,EntityLivingBase living, double startX, double startY,double startZ,double endX,double endY,double endZ,boolean headshot, float size,boolean pierce){
    	ArrayList<RayTraceResult> targets=new ArrayList<>();
    	Vec3d var17 = new Vec3d(startX, startY, startZ);
        Vec3d var3 = new Vec3d(endX, endY, endZ);
        RayTraceResult var4 = world.rayTraceBlocks(var17, var3, false, true,false);
        var17 = new Vec3d(startX, startY, startZ);
        var3 = new Vec3d(endX, endY, endZ);

        
        if (var4 != null)
        {
            var3 = new Vec3d(var4.hitVec.xCoord, var4.hitVec.yCoord, var4.hitVec.zCoord);
        }

        Entity var5 = null;
        List<Entity> var6 = world.getEntitiesWithinAABBExcludingEntity(living, living.getEntityBoundingBox().addCoord(endX - startX, endY - startY, endZ - startZ).expand(2D, 2D, 2D));
        //System.out.println("shoot: "+startX+","+startY+","+startZ+", do: "+endX+","+endY+","+endZ+" Count: "+var6.size());
        double var7 = 0.0D;
        Vec3d collideVec=new Vec3d(0, 0, 0);
        for (Entity target:var6)
        {
            //Entity var10 = (Entity)iterator.next();

            if (target.canBeCollidedWith() && (!(target instanceof EntityLivingBase) || (target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime <= 0)))
            {
            	AxisAlignedBB oldBB=target.getEntityBoundingBox();
            	if(TF2EventBusListener.moveEntities){
            		float ticktime=TF2EventBusListener.tickTime;
            		target.setEntityBoundingBox(target.getEntityBoundingBox().offset((target.prevPosX-target.posX)*(1-ticktime),
            				(target.prevPosY-target.posY)*(1-ticktime),(target.prevPosZ-target.posZ)*(1-ticktime)));
            	}
                AxisAlignedBB var12 = target.getEntityBoundingBox().expand(size, (double)size+0.16f, size);
                RayTraceResult var13 = var12.calculateIntercept(var17, var3);

                if (var13 != null)
                {
                    double var14 = var17.squareDistanceTo(var13.hitVec);

                    if (!pierce&&(var14 < var7 || var7 == 0.0D))
                    {
                        var5 = target;
                        var7 = var14;
                        collideVec=var13.hitVec;
                    }
                    else if(pierce){
                    	targets.add(getTraceResult(target,var13.hitVec,headshot,var17,var3));
                    }
                }
                target.setEntityBoundingBox(oldBB);
            }
        }
        //var4.hitInfo=false;
        if (!pierce &&  var5 != null && !(var5 instanceof EntityLivingBase && ((EntityLivingBase)var5).getHealth()<=0))
        {
            targets.add(getTraceResult(var5,collideVec,headshot,var17,var3));
        }
        if(targets.isEmpty() && var4 != null && var4.typeOfHit==Type.BLOCK){
        	targets.add(var4);
        }
        else if(targets.isEmpty()){
        	targets.add(new RayTraceResult(Type.MISS, var3, null, null));
        }
        return targets;
    }
    public static RayTraceResult getTraceResult(Entity target, Vec3d hitVec,boolean headshot,Vec3d start,Vec3d end){
    	RayTraceResult result = new RayTraceResult(target,hitVec);
        if(!(target instanceof EntityBuilding) && headshot){
            double ymax=target.getEntityBoundingBox().maxY;
    		AxisAlignedBB head=new AxisAlignedBB(target.posX-0.32, ymax-0.24, target.posZ-0.32,target.posX+0.32, ymax+0.48, target.posZ+0.32);
    		if(target instanceof EntityCreeper||target instanceof EntityEnderman||target instanceof EntityIronGolem){
    			head.offset(0, -0.24, 0);
    		}
    		if(target.width>target.height*0.65){
    			double offsetX=(double)(MathHelper.cos(target.rotationYaw / 180.0F * (float)Math.PI) * target.width/2);
    			double offsetZ=-(double)(MathHelper.sin(target.rotationYaw / 180.0F * (float)Math.PI) * target.width/2);//cos
    			//double offsetX2=- (double)(MathHelper.sin(living.rotationYaw / 180.0F * (float)Math.PI) * var5.width/2);
    			//double offsetZ2=(double)(MathHelper.cos(living.rotationYaw / 180.0F * (float)Math.PI) * var5.width/2);//cos
    			//System.out.println("Offsets: "+offsetX+" "+offsetZ+" "+offsetX2+" "+offsetZ2);
    			head.offset(offsetX, 0, offsetZ);
    		}
    		RayTraceResult var13 = head.calculateIntercept(start, end);
            result.hitInfo=var13;
        }
        return result;
    }
    public static boolean isUsingShield(Entity shielded, EntityLivingBase shooter){
    	if(shielded instanceof EntityLivingBase && ((EntityLivingBase) shielded).isActiveItemStackBlocking()){
    		Vec3d vec3d = shooter.getPositionVector();
    		if (vec3d != null)
            {
                Vec3d vec3d1 = shielded.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(shielded.getPositionVector()).normalize();
                vec3d2 = new Vec3d(vec3d2.xCoord, 0.0D, vec3d2.zCoord);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
                
            }
    	}
    	return false;
    }
    public static int calculateCritsPost(Entity target, EntityLivingBase shooter, int initial, ItemStack stack){
    	if(initial > 0 && (target instanceof EntityLivingBase && ((EntityLivingBase) target).getActivePotionEffect(TF2weapons.backup)!=null)){
    		initial = 0;
    	}
    	if(initial == 0 && (target instanceof EntityLivingBase && (((EntityLivingBase) target).getActivePotionEffect(TF2weapons.markDeath)!=null||((EntityLivingBase) target).getActivePotionEffect(TF2weapons.jarate)!=null))){
    		initial = 1;
    	}
    	if(initial < 2 && (stack !=null && target.isBurning() &&TF2Attribute.getModifier("Crit Burn", stack, 0, shooter)!=0)){
    		initial=2;
    	}
    	if(initial < 2 && (stack != null && shooter != null && shooter instanceof EntityPlayer && shooter.getDataManager().get(TF2EventBusListener.ENTITY_EXP_JUMP) && TF2Attribute.getModifier("Crit Rocket", stack, 0, shooter)!=0)){
    		initial=2;
    	}
    	if(initial == 1 && (stack != null && shooter != null && shooter instanceof EntityPlayer && TF2Attribute.getModifier("Crit Mini", stack, 0, shooter)!=0)){
    		initial=2;
    	}
    	if(target instanceof EntityMonoculus && initial==1){
    		initial=0;
    	}
    	return initial;
    }
    public static float calculateDamage(Entity target,World world, EntityLivingBase living, ItemStack stack,int critical,float distance){
    	ItemWeapon weapon=(ItemWeapon) stack.getItem();
    	float calculateddamage = weapon.getWeaponDamage(stack,living, target);
    	
    	if(calculateddamage==0){
    		return 0f;
    	}
    	if(target instanceof EntityBuilding){
    		return calculateddamage;
    	}
    	if(target!=living){
	    	if(critical==2){
	    		calculateddamage*=3;
	    	}
	    	else if(critical==1){
	    		calculateddamage*=1.35;
	    	}
    	}
    	if(target==dummyEnt){
    		distance*=0.5f;
    	}
        if(!(target instanceof EntityMonoculus) && weapon.getWeaponDamageFalloff(stack)>0&&(critical<2||target==living)){
        	if(distance <= weapon.getWeaponDamageFalloff(stack))
        		//calculateddamage *=weapon.maxDamage - ((distance / (float)weapon.damageFalloff) * (weapon.maxDamage-weapon.damage));
        		calculateddamage *= lerp(weapon.getWeaponMaxDamage(stack,living),1f,(distance / weapon.getWeaponDamageFalloff(stack)));
        	else if(critical==0){
        		//calculateddamage *=Math.max(weapon.getWeaponMinDamage(stack,living)/weapon.getWeaponDamage(stack,living), ((weapon.getWeaponDamage(stack,living)) - (((distance-weapon.getWeaponDamageFalloff(stack)) / ((float)weapon.getWeaponDamageFalloff(stack)*2)) * (weapon.getWeaponDamage(stack,living)-weapon.getWeaponMinDamage(stack,living))))/weapon.getWeaponDamage(stack,living));
        		calculateddamage *= lerp(1f,weapon.getWeaponMinDamage(stack,living),(Math.min(distance/weapon.getWeaponDamageFalloff(stack),TF2Attribute.getModifier("Accuracy", stack, 2,living))-1)/(TF2Attribute.getModifier("Accuracy", stack, 2,living)-1));
        		//calculateddamage *= 1 - (1-weapon.getWeaponMinDamage(stack,living))*(Math.min(distance/weapon.getWeaponDamageFalloff(stack),2*TF2Attribute.getModifier("Accuracy", stack, 1,living))-1*TF2Attribute.getModifier("Accuracy", stack, 1,living));
        		//System.out.println((distance-weapon.getWeaponDamageFalloff(stack))-(weapon.getWeaponDamageFalloff(stack)*2));
        	}
        	
        }
        if(target instanceof EntityEnderman){
        	calculateddamage *=0.4f;
        }
        if(isUsingShield(target, living)){
        	if(weapon instanceof ItemProjectileWeapon){
        		calculateddamage *=1.5f;
        	}
        	else{
        		calculateddamage *=0.5f;
        	}
        }
        /*if (living instanceof IRangedWeaponAttackMob)
        	calculateddamage*=((IRangedWeaponAttackMob)living).getAttributeModifier("Damage");*/
        return calculateddamage;
    }
    public static float lerp(float v0, float v1, float t) {
    	  return (1-t)*v0 + t*v1;
    }
    public static boolean isOnSameTeam(Entity entity1,Entity entity2){
    	return (getTeam(entity1)==getTeam(entity2)&&getTeam(entity1)!=null)||(entity2 instanceof EntityBuilding && 
        		((EntityBuilding)entity2).getOwner()==entity1)||(entity1 instanceof EntityBuilding && ((EntityBuilding)entity1).getOwner()==entity2)||entity1==entity2;
    	
    }
    public static Team getTeam(Entity living){
    	if(living instanceof EntityLivingBase){
    		return ((EntityLivingBase)living).getTeam();
    	}
    	else if(living instanceof IThrowableEntity){
    		return getTeam(((IThrowableEntity)living).getThrower());
    	}
    	return null;
    }
    public static int getTeamForDisplay(Entity living){
    	if(living instanceof EntityTF2Character){
    		return ((EntityTF2Character)living).getEntTeam();
    	}
    	else if(living instanceof EntityBuilding){
    		return ((EntityBuilding)living).getEntTeam();
    	}
    	else if(living instanceof EntityPlayer){
    		return ((EntityPlayer)living).getTeam()==living.worldObj.getScoreboard().getTeam("BLU")?1:0;
    	}
    	else if(living instanceof IThrowableEntity){
    		return getTeamForDisplay(((IThrowableEntity)living).getThrower());
    	}
    	return 0;
    }
    public static boolean canHit(EntityLivingBase shooter,Entity ent){
    	//System.out.println("allowed: "+isOnSameTeam(shooter,ent)+" "+!(shooter.getTeam()!=null&&shooter.getTeam().getAllowFriendlyFire())+" "+(ent!=shooter)+" "+!(shooter instanceof EntityBuilding&&((EntityBuilding)shooter).getOwner()==ent));
		return ent.isEntityAlive()&&!(ent instanceof EntityLivingBase&&
				isOnSameTeam(shooter,ent)&&
				!(shooter.getTeam()!=null&&shooter.getTeam().getAllowFriendlyFire())&&
				(ent!=shooter)&&!(shooter instanceof EntityBuilding&&((EntityBuilding)shooter).getOwner()==ent));
	}
    public static boolean lookingAt(EntityLivingBase entity,double max,double targetX,double targetY, double targetZ){
    	double d0 = targetX - entity.posX;
        double d1 = targetY - (entity.posY + (double)entity.getEyeHeight());
        double d2 = targetZ - entity.posZ;
        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        float f = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
        float compareyaw=Math.abs( 180 - Math.abs(Math.abs(f - MathHelper.wrapDegrees(entity.rotationYawHead)) - 180)); 
        float comparepitch=Math.abs( 180 - Math.abs(Math.abs(f1 - entity.rotationPitch) - 180)); 
        //System.out.println("Angl: "+compareyaw+" "+comparepitch);
        return compareyaw<max&&comparepitch<max;
    }
    public static boolean dealDamage(Entity entity,World world,EntityLivingBase living, ItemStack stack, int critical,float damage,DamageSource source){
    	if(world.isRemote) return false;
    	double lvelocityX = entity.motionX;
        double lvelocityY = entity.motionY;
        double lvelocityZ = entity.motionZ;
        entity.hurtResistantTime = 0;
        
        if(!(living instanceof EntityPlayer)&& entity instanceof EntityPlayer){
        	if(world.getDifficulty() == EnumDifficulty.EASY){
        		damage*=0.4f;
        	}
        	else if(world.getDifficulty() == EnumDifficulty.NORMAL){
        		damage*=0.55f;
        	}
        	else if(world.getDifficulty() == EnumDifficulty.HARD){
        		damage*=0.75f;
        	}
        	//source.setDifficultyScaled();
        }
        boolean usingShield=isUsingShield(entity, living);
        if(usingShield && source instanceof DamageSourceBullet && source.isExplosion()){
        	((DamageSourceBullet)source).removeProjecileStatus();
        }
        if(stack != null && stack.getItem() instanceof ItemWeapon && ItemFromData.getData(stack).hasProperty(PropertyType.HIT_SOUND)){
        	TF2weapons.playSound(entity,ItemFromData.getSound(stack, PropertyType.HIT_SOUND), ItemFromData.getData(stack).getName().equals("fryingpan")?2F:0.7F, 1F);
        }
        //System.out.println("dealt: "+damage);
        boolean knockback=canHit(living,entity);
        if (knockback&& entity.attackEntityFrom((DamageSource) source,damage))
        {
        	if(stack != null && stack.getItem() instanceof ItemWeapon){
        		((ItemWeapon) stack.getItem()).onDealDamage(stack, living, entity,source);
        	}
        	if(living instanceof EntityPlayer){
	        	if(!ItemUsable.lastDamage.containsKey(living))
	        		ItemUsable.lastDamage.put(living, new float[20]);
	        	ItemUsable.lastDamage.get(living)[0]+=damage;
	        }
        	if (entity instanceof EntityLivingBase)
	        {
        		
	            EntityLivingBase livingTarget = (EntityLivingBase)entity;
	            if(isUsingShield(entity, living)){
        			livingTarget.getActiveItemStack().damageItem((int) Math.max(damage*2, 1), livingTarget);
                }
	            //System.out.println(livingTarget.getHealth());
	            // System.out.println("Scaled"+source.isDifficultyScaled()+" "+damage);
	            livingTarget.hurtResistantTime = 20;
	            if(critical==2){
	            	TF2weapons.playSound(entity,TF2Sounds.MISC_CRIT, 1.5F, 1.2F / (world.rand.nextFloat() * 0.2F + 0.9F));
	            }
	            else if(critical == 1){
	            	TF2weapons.playSound(entity,TF2Sounds.MISC_MINI_CRIT, 1.5F, 1.2F / (world.rand.nextFloat() * 0.2F + 0.9F));
	            }
	            if(!(entity instanceof EntityBuilding))
	            	TF2weapons.playSound(entity,TF2Sounds.MISC_PAIN, 1F, 1.2F / (world.rand.nextFloat() * 0.2F + 0.9F));
	            /*if (living instanceof EntityPlayer && !world.isRemote)
	            {
	                EntityPlayer player = (EntityPlayer) living;
	                String string="";
	                for(int i=0; i<20;i++){
	                	string+=ItemUsable.lastDamage.get(living)[i]+" ";
	                }
	                
	                //player.addChatMessage(string);
	               // player.addChatMessage("Health: " + livingTarget.getHealth() + "/" + livingTarget.getMaxHealth() + " Armor: " + livingTarget.getTotalArmorValue()*4+ "% Critical: "+critical+" Distance: "+distance);
	            }*/
	            livingTarget.motionX=lvelocityX;
	            livingTarget.motionY=lvelocityY;
	            livingTarget.motionZ=lvelocityZ;
	        }
        }
        return knockback;
    }
    public static float damageBlock(BlockPos pos, EntityLivingBase living, World world, ItemStack stack, int critical, float damage,Vec3d forwardVec, Explosion explosion){
    	IBlockState state=world.getBlockState(pos);
    	Block block=state.getBlock();
		if(block.isAir(state, world, pos)||state.getBlockHardness(world, pos)<0)
			return 0;
		
		DestroyBlockEntry finalEntry=null;
		int entryId=0;
		int emptyId=-1;
		for(int i=0;i<TF2EventBusListener.destroyProgress.size();i++){
			DestroyBlockEntry entry=TF2EventBusListener.destroyProgress.get(i);
			if(emptyId==-1&&entry==null){
				emptyId=i;
			}
			if(entry != null && entry.world==world && entry.pos.equals(pos)){
				finalEntry=entry;
				entryId=i;
				break;
			}
		}
		if(finalEntry==null){
			finalEntry = new DestroyBlockEntry(pos,world);
			if(emptyId!=-1){
				TF2EventBusListener.destroyProgress.set(emptyId, finalEntry);
				entryId=emptyId;
			}
			else{
				TF2EventBusListener.destroyProgress.add(finalEntry);
				entryId=TF2EventBusListener.destroyProgress.size()-1;
			}
			
		}
		
		float hardness=getHardness(state,world, pos);
		
		if(stack != null && stack.getItem() instanceof ItemSniperRifle && hardness > 100){
			damage*=3;
		}
		finalEntry.curDamage+=damage;
		
		if(living != null)
			world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE,0xFFFF+entryId), pos, (int) ((finalEntry.curDamage/hardness)*10));
		
		if(finalEntry.curDamage>=hardness){
			if(living!= null && living instanceof EntityPlayer)
				block.harvestBlock(world, (EntityPlayer) living, pos, state, null, stack);
			else{
				block.dropBlockAsItem(world, pos, state, 0);
				block.onBlockExploded(world, pos, explosion);
			}
			TF2EventBusListener.destroyProgress.remove(finalEntry);

	        boolean flag = (living == null || !(living instanceof EntityPlayer) && world.isAirBlock(pos)) || block.removedByPlayer(state, world, pos, (EntityPlayer) living, true);

	        if (flag)
	        {
	        	if(living != null){
	        		world.playEvent(2001, pos, Block.getStateId(state));
	        		world.sendBlockBreakProgress(Math.min(Integer.MAX_VALUE,0xFFFF+entryId), pos, -1);
	        	}
	            block.onBlockDestroyedByPlayer(world, pos, state);
	            
	            if(forwardVec != null){
		            RayTraceResult trace=world.rayTraceBlocks(living.getPositionVector().addVector(0, living.getEyeHeight(), 0), forwardVec, false, true, false);
		            if(trace != null){
		            	damageBlock(trace.getBlockPos(),living,world,stack,critical,finalEntry.curDamage-hardness,forwardVec, explosion);
		            }
	            }
	        }
	        return finalEntry.curDamage-hardness;
		}
		return 0;
    }
    
    public static float getHardness(IBlockState state, World world, BlockPos pos){
    	return state.getBlockHardness(world, pos)*(!state.getMaterial().isToolNotRequired()&& !(state.getBlock() instanceof BlockStone) ?12f:5.5f);
    }
    public static int getMetal(EntityLivingBase entity){
    	if(entity instanceof EntityEngineer){
    		return ((EntityEngineer) entity).metal;
    	}
    	
    	return entity.getHeldItem(EnumHand.MAIN_HAND) != null && entity.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemWrench?200-entity.getHeldItem(EnumHand.MAIN_HAND).getItemDamage():0;
    }
    public static void setMetal(EntityLivingBase entity,int amount){
    	if(entity instanceof EntityEngineer){
    		((EntityEngineer) entity).metal=amount;
    	}
    	else if(entity.getHeldItem(EnumHand.MAIN_HAND) != null && entity.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemWrench){
    		entity.getHeldItem(EnumHand.MAIN_HAND).setItemDamage(200-amount);
    	}
    }
	public static class NullStorage<T> implements IStorage<T>{

		@Override
		public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public static int getExperiencePoints(EntityPlayer player){
    	int playerLevel=player.experienceLevel;
    	player.experienceLevel=0;
    	int totalExp=0;
    	for(int i=0;i<playerLevel;i++){
    		player.experienceLevel=i;
    		totalExp+=player.xpBarCap();
    	}
    	player.experienceLevel=playerLevel;
    	totalExp+=Math.round(player.experience*player.xpBarCap());
    	return totalExp;
    }
	public static void setExperiencePoints(EntityPlayer player,int amount){
    	player.experienceLevel=0;
    	player.experience=0;
    	player.addExperience(amount);
    }
	public static void stun(EntityLivingBase living, int duration, boolean noMovement){
		living.addPotionEffect(new PotionEffect(stun,duration,noMovement?1:0));
		living.addPotionEffect(new PotionEffect(MobEffects.NAUSEA,duration,0));
	}
	public static void playSound(Entity entity, SoundEvent event, float volume, float pitch){
		entity.worldObj.playSound(null, entity.posX, entity.posY, entity.posZ, event, entity.getSoundCategory(), volume, pitch);
	}
}
