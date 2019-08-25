# Automated Crafting
An open-source plugin for spigot which adds powerful autocrafters capable of automatically crafting anything.

<br/>

**How to use**

1) Place a dropper in the world.

![Step 1](https://i.ibb.co/Mg3hKbD/2019-08-16-16-13-13.png)


2) Put an item frame on any side of the dropper.

![Step 2](https://i.ibb.co/vccK0T8/2019-08-16-16-13-21.png)


3) Put the item you want to craft in the item frame.

![Step 3](https://i.ibb.co/fxQ3p96/2019-08-16-16-13-30.png)

4) Fill up the dropper with the crafting ingredients.

![Step 4](https://i.ibb.co/5nCPpct/2019-08-16-16-13-44.png)

5) Enjoy your new autocrafter!

![Step 5](https://i.ibb.co/6Nqq03s/2019-08-16-16-13-50.png)

<br/><br/>

**Other Features**
- Autocrafters will put the items in the container on the block the dropper is facing towards, otherwise the item will be dropped.
- Powering the dropper makes it stop automatically crafting. (locking the dropper also has this effect)
- Plugins can cancel the AutoPreCraftItemEvent in this plugin to disable certain items from being automatically crafting, for example the code below will block any diamond helmets from being crafted.
```java
@EventHandler
public void onAutoCraft(AutoPreCraftItemEvent e) {
    if(e.getCraftedItem().getType()==Material.DIAMOND_HELMET) e.setCancelled(true);
}
```
- Alternatively plugins  can cancel AutoPostCraftItemEvent to undo the crafting just before it is completed. Here you can also get the itemstacks that will be used up.
- Custom autocrafter-only recipes can be added by creating recipe json files in the /recipes/ subfolder of the plugin's configuration folder. These recipes should follow the same guidelines as datapack recipe jsons follow. There are a few extra features though:
   - Both the result item and any ingredient items can have aditional keys instead of the regular `item`, `count` and `tag`. You can also add `data`, `displayName`, `lore` and `enchantments` (list of key/values with the enchantment ids)
   - For example:
   ```json
   {
     "type": "crafting_shaped",
     "pattern": [
       "FI"
     ],
     "key": {
       "F": {
         "item": "minecraft:flint",
         "displayName": "Custom Flint"
       },
       "I": {
         "item": "minecraft:iron_ingot",
         "displayName": "Red Iron",
         "enchantments": {
           "minecraft:unbreaking": 10
         }
       }
     },
     "result": {
       "item": "minecraft:gold_ingot",
       "count": 4,
       "displayName": "Magical Gold",
       "lore": [
           "A custom autocrafter-exclusive item made from flint and iron ingots named Red Iron with Unbreaking X!"
       ],
       "enchantments": {
           "minecraft:sharpness": 255
       }
     }
   }
   ```
   - The json above, when placed in /recipes/ and named ``magical_gold.json`` will add a recipe that only works in autocrafters which checks for the ingredients to be flint and an iron ingot named Red Iron with the Unbreaking X enchantments. Using display name, lore and enchantment checks on the ingredients makes these impossible to craft in vanilla but in combination with custom recipe plugins these can be very useful.
   - Please note that these custom recipes only work in the autocrafters and NOT in the crafting table. Use regular datapacks if you want these as crafting table recipes.
   - Another note: vanilla recipe jsons have a ``group`` argument, you can still use that but it is ignored by autocrafters. Aditionally, types that are not crafting_shaped or crafting_shapeless are ignored because the autocrafter isn't an autosmelter.

<br/> <br/>

**Adding this plugin as a dependency**

If you want to use this plugin as a dependency, for example to use the AutoCraftItemEvent. You can use a very handy service called [**jitpack.io**](https://jitpack.io/).<br/>
This services makes it easy to add any git repositry as a dependency.

_Gradle_<br/>
For Gradle you'll need to add the following six lines to your _build.gradle_ file:
```gradle
repositories {
     maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.daniel-goossens:automated_crafting:master-SNAPSHOT'
}
```

_Maven_<br/>
For Maven you can add the following lines to your _pom.xml_ file:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.daniel-goossens</groupId>
    <artifactId>automated_crafting</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```
