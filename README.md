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

OtherDrops relies on submodules, so after cloning the project:

    $ git submodule update --init

Then stick a Bukkit build in `lib`, following the `bukkit-version-build.jar`
naming convention:

    $ wget -O lib/bukkit-0.0.1-r746.jar http://ci.bukkit.org/job/dev-Bukkit/promotion/latest/Recommended/artifact/target/bukkit-0.0.1-SNAPSHOT.jar
    $ ant build

Ant will automatically pick out the latest Bukkit build in `lib`. To manually
specify a Bukkit version to build against, set the `lib.bukkit.version`
property on the command line:

    $ ant -Dlib.bukkit.version="0.0.1-r746" build

Use `ant -p` to see a complete list of Ant tasks.
