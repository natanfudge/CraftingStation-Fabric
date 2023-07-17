package io.github.natanfudge.injection;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * We use java because it's more predictable for injection
 */
public interface ImmutableItemStack {
    public default boolean isEmpty() {
        throw new IllegalStateException();
    }

    public default boolean isStackable() {
        throw new IllegalStateException();
    }

    public default Item getItem() {
        throw new IllegalStateException();
    }

    public default int getCount() {
        throw new IllegalStateException();
    }

    @Nullable
    public default NbtCompound getNbt() {
        throw new IllegalStateException();
    }
}
