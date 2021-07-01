# Automated Crafting
An open-source plugin for Spigot 1.12 through 1.16 which adds powerful autocrafters capable of automatically crafting anything. With a configuration to allow further customisation like increasing the difficulty of obtaining the autocrafters.

<br/>

**How to use (when using default configuration settings)**

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

<br/> <br/>

**Adding this plugin as a dependency**

If you want to use this plugin as a dependency. You can use a very handy service called [**jitpack.io**](https://jitpack.io/). <br/>
This services makes it easy to add any git repository as a dependency.

_Gradle_<br/>
For Gradle you'll need to add the following six lines to your _build.gradle_ file:
```gradle
repositories {
     maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.Aeltumn:automated_crafting:main-SNAPSHOT'
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
    <groupId>com.github.Aeltumn</groupId>
    <artifactId>automated_crafting</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```
