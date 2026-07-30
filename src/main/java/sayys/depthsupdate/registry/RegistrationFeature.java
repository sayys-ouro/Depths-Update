package sayys.depthsupdate.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Encapsulates a set of registry entries that are controlled by a single configuration toggle.
 */
public class RegistrationFeature {
    private final BooleanSupplier configToggle;
    private final List<IForgeRegistryEntry<?>> entries = new ArrayList<>();
    private final Set<IForgeRegistryEntry<?>> skippedModels = new HashSet<>();

    private BiConsumer<Block, RegistryEvent.Register<Item>> itemBlockProvider = (block, event) -> {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    };

    private Consumer<ModelRegistryEvent> modelOverrideCallback = (event) -> {};
    private Runnable initCallback = () -> {};

    public RegistrationFeature(BooleanSupplier configToggle) {
        this.configToggle = configToggle;
    }

    public RegistrationFeature add(IForgeRegistryEntry<?>... newEntries) {
        Collections.addAll(this.entries, newEntries);
        return this;
    }

    public RegistrationFeature withItemBlockProvider(BiConsumer<Block, RegistryEvent.Register<Item>> provider) {
        this.itemBlockProvider = provider;
        return this;
    }

    public RegistrationFeature skipDefaultModel(IForgeRegistryEntry<?>... entriesToSkip) {
        Collections.addAll(this.skippedModels, entriesToSkip);
        return this;
    }

    public RegistrationFeature withModelOverrides(Consumer<ModelRegistryEvent> callback) {
        this.modelOverrideCallback = callback;
        return this;
    }

    public RegistrationFeature withInit(Runnable callback) {
        this.initCallback = callback;
        return this;
    }

    public boolean isEnabled() {
        return configToggle.getAsBoolean();
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        if (!isEnabled()) return;

        for (IForgeRegistryEntry<?> entry : entries) {
            if (entry instanceof Block) {
                event.getRegistry().register((Block) entry);
            }
        }
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        if (!isEnabled()) return;

        for (IForgeRegistryEntry<?> entry : entries) {
            if (entry instanceof Item) {
                event.getRegistry().register((Item) entry);
            } else if (entry instanceof Block) {
                itemBlockProvider.accept((Block) entry, event);
            }
        }
    }

    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        if (!isEnabled()) return;

        for (IForgeRegistryEntry<?> entry : entries) {
            if (entry instanceof SoundEvent) {
                event.getRegistry().register((SoundEvent) entry);
            }
        }
    }

    public void registerModels(ModelRegistryEvent event) {
        if (!isEnabled()) return;

        for (IForgeRegistryEntry<?> entry : entries) {
            if (skippedModels.contains(entry)) continue;

            if (entry instanceof IHasModel) {
                Item item = entry instanceof Item ? (Item) entry : Item.getItemFromBlock((Block) entry);
                if (item != Items.AIR) {
                    ((IHasModel) entry).registerModel(item);
                }
            } else if (entry instanceof Block) {
                Block block = (Block) entry;
                Item item = Item.getItemFromBlock(block);
                if (item != Items.AIR) {
                    registerDefaultBlockModel(block);
                }
            } else if (entry instanceof Item) {
                Item item = (Item) entry;
                if (item.getRegistryName() != null) {
                    registerDefaultItemModel(item);
                }
            }
        }

        modelOverrideCallback.accept(event);
    }

    public void init() {
        if (isEnabled()) {
            initCallback.run();
        }
    }

    private void registerDefaultBlockModel(Block block) {
        if (block.getRegistryName() != null) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }

    private void registerDefaultItemModel(Item item) {
        if (item.getRegistryName() != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

}
