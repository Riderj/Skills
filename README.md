# Skills
Research/Unlock Tree API for Minecraft (Forge)

Skills API is meant to provide an easy way for mods to organize their research trees, quest trees, whatever trees, on per-player basis.

#Getting started
To get started, make an instance of ResearchMap with a unique name, fill it with ResearchNode instances, and register it.

#Advanced stuff
If you want to keep some additional information in your nodes, you will have to extend ResearchNode class, overload serialize() method, and register deserializer for your class. Take a look at method serialize0 and static initializer of ResearchNode for an example.

#Requirements
Skills API requires Java 8 and Minecraft Forge 1718+ to be used.
