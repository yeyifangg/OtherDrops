OtherDrops
==========

(Note: This readme is rather out-of-date.)

OtherDrops is a plugin for the Minecraft Bukkit API that lets you completely
customize what blocks and dead mobs drop when they are destroyed. Apples from
leaves, no more broken glass, you name it!

The related discussion thread for this plugin is located at
<http://forums.bukkit.org/threads/4072/>.

Please see [the project wiki](https://github.com/cyklo/Bukkit-OtherBlocks/wiki)
for details on how to set up OtherDrops.

Building from source
--------------------

These instructions assume you have already forked and/or cloned the project and have on your computer.

OtherDrops comes with most dependencies already stored in the repository (for simplicity) however
you need to download a Bukkit build and place into the `lib` folder - rename it to `bukkit.jar`

Then build using your IDE or:

    $ ant build

Use `ant -p` to see a complete list of Ant tasks.
