package me.pixlent.item;

import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemContainer {
    public List<ItemStack> itemStacks = new ArrayList<>();

    public void acceptItemTransaction(ItemTransaction transaction) {
        for (ItemStack itemStack : itemStacks) {
            if (itemStack.material() == transaction.itemStack.material()) {
                itemStacks.remove(itemStack);
                itemStacks.add(itemStack.withAmount(itemStack.amount() + transaction.itemStack.amount()));
            }
        }

        itemStacks.add(transaction.itemStack);
    }

    public ItemTransaction transferItemStack(int index) {
        ItemTransaction transaction = new ItemTransaction(itemStacks.get(index));
        itemStacks.remove(index);
        return transaction;
    }
}
