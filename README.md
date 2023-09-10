# MultiblockAPI

## Important Notes
1. This plugin has since been [archived](https://www.spigotmc.org/resources/multiblockapi-archived.70697/) and is no
longer undergoing active development. The jar is still available at that link, though.
2. Since its 1.0.0 release, a dependency gave the build trouble, causing builds to fail. This has been
fixed in version 1.0.1. The actual code did not change, so the 1.0.0 jar from the link above should work fine,
or you can build it yourself.

## What is it?
MultiblockAPI is a spigot API to allow the creation of multiblocks. 
Multiblocks are structures made from multiple blocks that, when together, make up their own special function.
This can be used to create, for example, machines, guard towers, and magical altars.

## Installation

Get the plugin from the spigot page: https://www.spigotmc.org/resources/multiblockapi.70697/

See https://jitpack.io/#com.github.Yona168/MultiblockAPI for adding it to your project. Just make sure you are not compiling the plugin in-
this is a standalone plugin. If you are using gradle, for example, do NOT use implementation-compileOnly works fine.

## Usage
### Types of Multiblocks
There are two kinds of multiblocks-normal, and tickable. 
Normal multiblocks do something on click, and tickable blocks are
enabled on tick and then do something continuously. First,
let's create a simple multiblock.

###Creating a Normal Multiblock
In creating a normal multiblock, there are five steps:
1. Define the structure
2. ID the structure
3. Define how state is created and stored
4. Define the multiblock function
5. Register multiblock with the API

Let's try to make a multiblock that simply sends the message "Hi" when clicked.
#### 1. Defining the structure
To define the structure, we use the `Pattern` class. Our structure needs structure blocks,
as well as a trigger block (the block that will trigger the action).
Let's say we want our multiblock to be a 2 level 2x4 cobblestone square,
with some diamonds on top, and a gold block to interact with. Consider this pattern:

C=Cobblestone, D=Diamond Block, G=Gold block

LEVEL 0:  
C C   
C C  
C C  
G C

LEVEL 1:

C D  
C D  
C D  
C D

We want level 1 to be on top of level 0. So, we have two levels,
four rows on each level, and 2 columns on each level. Let's specify those dimensions:

```java
Pattern myPattern=new PatternCreator(2,4,2).//Size Levels, Rows, Columns.  
level(0).fillLevel(Material.COBBLESTONE). //level 0 fill with cobble
.set(0,3, Material.GOLD_BLOCK). //set the gold block in bottom left corner for clicking
level(1).fillColumn(0, Material.COBBLESTONE). //Fill half of level 1  
.fillColumn(1, Material.DIAMOND_BLOCK). //Fill other half
triggerCoords(0,0,3); //Tell the PatternCreator that the gold block is our trigger.
``` 

#### 2. ID'ing the structure.
For 1.8 compatibility, MultiblockAPI provides its own ```NamespacedKey``` class for identification.
```java
NamespacedKey saysHiKey=new NamespacedKey(this, "HiBlock");
```

#### 3. Define how state is created and stored
As of now, we have defined the structure of our multiblock. What we have not done is give
it some way to instantiate that structure in the world. This is what ```MultiblockState``` is for.
Each instance of ```MultiblockState``` represents one instance of the Multiblock in the world.
Anything within a ```MultiblockState``` gets stored away, and only data specific to each instance should be stored in it.
For this example, we don't have specific data being stored in multiblocks, as each instance does the same thing
(says "Hi") on click. Therefore, we can simply use a provided implementation of ```MultiblockState```: ```SimpleMultiblockState```
What we have to do now is define a ```StateCreator``` that provides us with new states on demand.

```java
StateCreator<SimpleMultiblockState> myStateCreator=(multiblock, locationInfo, event)->new SimpleMultiblockState(multiblock, locationInfo, myPlugin);
```
Now, we have to decide how our state is going to be stored away in a database. The objects that handle these
are ```StateDataTunnels```-that is, they transport data from server to DB and back again.  
As of now, MultiblockAPI
supports the use of Kryo-a fast serialization library-to store states in files. To reference the default ```KryoDataTunnel```,
we simply do
```java
StateDataTunnel kryoDataTUnnel=StateDataTunnels.kryo();
```
#### 4. Define Multiblock Function
Now, what we are going to is take these and join them into one-a ```Multiblock``` object.
Just like with ```MultiblockState```, you could have your own implementation. However, using
```SimpleMultiblock``` is easy here:
```java
Multiblock<SimpleMultiblockState> saysHiMultiblock=new SimpleMultiblock(myPattern, saysHiId, kryoDataTunnel,myStateCreator);
saysHiMultiblock.onClick((event, state)->{ //Event is a PlayerInteractEvent
event.getPlayer().sendMessage("Hi!");
})
```
#### 5. Registering our Multiblock
Easy peasy! 
```java
MultiblockAPI.getAPI().getMultiblockRegistry().register(saysHiMultiblock, plugin);
```
And we're done!

### How States are Stored
```MultiblockStates``` are cached when their chunk is loaded, and offloaded when not.
This provides enable/disable functionality (see below)

### Custom states/multiblocks

When it comes to creating more complicated structures, you may want something besides the ```Simple```
implementations for ```MultiblockState``` and ```Multiblock```. If so, you can easily make your own implementations.
The recommended way is to simply extend the ```Simple``` implementations and add your own data.
When deciding which to extend, ask yourself this simple question: "Is my data specific to each multiblock instance?".
If yes, your data should be inside your state, and if not, inside your multiblock

What's also useful about extending states is that you can add things that happen:
 1. On enable (clicked for the first time or reloaded back into the world)
 2. On disable (when being offloaded or destroyed)
 3. On destroy (when being destroyed)
 
 So lets see how a custom state might work:
 
 ```java
class CounterState extends SimpleMultiblockState{
  private final LocationInfo locationInfo;
  private int timesClicked=0;
  private CounterState(UUID uuid, Multiblock multiblock, LocationInfo locationInfo, int timesClicked){
    super(uuid, multiblock, locationInfo);
    this.locationInfo=locationInfo;
    this.timesClicked=timesClicked;
  }
  public CounterState(Multiblock multiblock, LocationInfo locationInfo){
  this(UUID.randomUUID(), multiblock, locationInfo, 0);
  } 
  public void increment(){
    this.timesClicked++;
  } 
  public int getTimesClicked(){
  return this.timesClicked;
  }
  @Override
  public void onEnable(){
    //You dont have to override me, but you could!
  }
  @Override
  public void onDisable(){
  //You dont have to override me, but you could!
  } 
  @Override
  public void onDestroy(){
  //You dont have to override me, but you could!
  }
  
  @Override
  public MultiblockState snapshot(){ //Note: MUST override this method
    return new CounterState(this.getUniqueId(), getMultiblock(), locationInfo, timesClicked);
  }
} 
```
Then, when registering our multiblock:
```java
myMultiblock.onClick((event, state)->{
event.getPlayer().sendMessage("You have clicked me "+state.getTimesClicked()+" times!");
});
```
#### What's with this snapshot() thing?
When our multiblock is enabled, it cannot be stored. To avoid disabling multiblocks, storing, then re-enabling mid-game, 
this method exists to provide disabled state snapshots. Snapshot should return a new state representing
how the state looks when disabled, which is why this ALWAYS has to be overridden.  

The snapshot method is declared by the ```Backup``` interface, which is not automatically a part of
```MultiblockState```. If you have your own implementation of ```MultiblockState``` that does not implement ```Backup```,
it still works fine-the only difference being that your multiblocks will NOT be backed up while still in use-they 
will only be stored when the chunk they are in unloads, or the server stops. It is highly reccomended to implement it.

#### Tickable states.
So, using this concept, you can easily create tickable states. There's just a few extra methods you
have to implement. Here's a fully working example of a tower that, when clicked, shoots arrows at nearby hostiles.

```java
public class GuardTowerState extends AbstractTickableState implements Backup {
  private transient Monster currentTarget;
  private final Multiblock multiblock;
  private final LocationInfo locationInfo;
  private final Plugin pLugin;
  public GuardTowerState(Multiblock multiblock, LocationInfo locInfo, Plugin plugin) {
    this(UUID.randomUUID(), multiblock, locInfo, plugin);
  }

  private GuardTowerState(UUID uuid, Multiblock multiblock, LocationInfo locationInfo, Plugin plugin){
    super(uuid,multiblock,locationInfo,plugin);
    this.multiblock=multiblock;
    this.locationInfo=locationInfo;
    this.pLugin=plugin;
  }

  @Override
  public int getPeriod() { //how often the run() method is called (in ticks)
    return 5;
  }

  @Override
  public boolean isAsync() { //Whether run() runs asynchronously or synchronously.
    return false;
  }

  @Override
  public void run() {
    if(currentTarget==null||currentTarget.isDead()){
      Optional<Monster> target=this.getTriggerBlockLoc().getBlock().getWorld().getNearbyEntities(getTriggerBlockLoc(),5,5,5)
              .stream().filter(entity->entity instanceof Monster).map(it->(Monster)it).findFirst();
      target.ifPresent(monster->this.currentTarget=monster);
    }else{
      Vector targetVec = currentTarget.getLocation().toVector();
      Location aboveTowerLocation=getBlockByPattern(4,0,0).getLocation();
      aboveTowerLocation.setX(aboveTowerLocation.getX()+.5);
      aboveTowerLocation.setZ(aboveTowerLocation.getZ()+.5);
      Vector subtractedVec=aboveTowerLocation.toVector().subtract(targetVec);
      aboveTowerLocation.getWorld().spawnArrow(aboveTowerLocation,
             subtractedVec.setX(subtractedVec.getX()*-1).setZ(subtractedVec.getZ()*-1) , (float)(currentTarget.getLocation().distanceSquared(aboveTowerLocation)/135), 1);
    }
  }

  @Override
  public MultiblockState snapshot() {
    return new GuardTowerState(getUniqueid(),multiblock,locationInfo,pLugin);
  }

  @Override
  public void postTaskEnable() {
    Bukkit.broadcastMessage("Guard tower enabled!");
  }
  
  @Override
  public void postTaskDisable(){
   Bukkit.broadcastMessage("Guard tower disabled!");
  }
  /* The above two methods replace onEnable and onDisable-they are triggered after Bukkit schedules and unschedules the timer task.*/
}
```

And to register:
```java
class MyPlugin extends JavaPlugin{
public void onEnable(){
    API multiblockAPI = MultiblockAPI.getAPI();
    //Create what structure of the guard tower looks like
    Pattern guardTowerPattern = new PatternCreator(3, 1, 1) //Dimensions
            .level(0).set(0, 0, Material.COBBLESTONE) //y level=0. Material[0][0][0]=Cobble
            .level(1).set(0, 0, Material.DIAMOND_BLOCK)//y level=1 Mateerial[1][0][0]=diamond
            .level(2).set(0, 0, Material.GOLD_BLOCK)
            .triggerCoords(2, 0, 0);//Index in array where clicking enables it (gold block)
    NamespacedKey guardTowerId = new NamespacedKey(this, "Guard Tower");//note-own namespacedkey for backwards capability
    //A way for us to make states
    StateCreator<GuardTowerState> guardTowerStateStateCreator = (multiblock, locInfo, event) -> new GuardTowerState(multiblock, locInfo, this);
    //Multiblock object creation. DataTunnels.kryo() tells us to use the default kryo storer to store the states for this multiblock
    Multiblock<GuardTowerState> guardTowerMultiblock = new SimpleMultiblock<>(guardTowerPattern, guardTowerId, kryo(), guardTowerStateStateCreator);
    //pop multiblock in registry
    multiblockAPI.getMultiblockRegistry().register(guardTowerMultiblock, this);
}
}
```
Note how we implement Backup here-Backup is what defines our snapshot() method. There is no ```Simple```
implementation of ```TickableState```, so Backup is optional.  Again, though, highly reccomended to use it.

## Default Kryo Data Tunnel and why you should implement your own:
The default kryo data tunnel is easy to use, and is an option. The only issue with it is that it works outside of your plugin.
If a server owner suddenly decides to remove a plugin using this API, that could mess up kryo files due to class registration.
Creating your own ```KryoDataTunnel``` as an alternative is easy:
```java
Path folderToStoreIn=plugin.getDataFolder().resolve("multiblockchunks");
StateDataTunnel myKryoDataTunnel=new KryoDataTunnel(Kryogenic.getNewPool(MultiblockAPI.getAPI()), folderToStoreIn, plugin);
```
# Commands
1. /mbapi -> backs up all multiblocks that can be backed up.

# TODO
Abstract SimpleMultiblockState further to allow optional implementation of Backup w/ easy extendability.
