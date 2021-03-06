package com.supermartijn642.trashcans.compat.mekanism;

import com.supermartijn642.trashcans.filter.ItemFilter;
import com.supermartijn642.trashcans.filter.LiquidTrashCanFilters;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Created 12/19/2020 by SuperMartijn642
 */
public class MekanismCompatOn extends MekanismCompatOff {

    public MekanismCompatOn(){
        LiquidTrashCanFilters.register(new GasFilterManager(), "gas");
    }

    @Override
    public boolean isInstalled(){
        return true;
    }

    @Override
    public Capability<?> getGasHandlerCapability(){
        return Capabilities.GAS_HANDLER_CAPABILITY;
    }

    @Override
    public boolean doesItemHaveGasStored(ItemStack stack){
        return stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).filter(handler -> {
            for(int i = 0; i < handler.getGasTankCount(); i++)
                if(!handler.getGasInTank(i).isEmpty())
                    return true;
            return false;
        }).isPresent();
    }

    @Override
    public boolean drainGasFromItem(ItemStack stack){
        return stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).map(handler -> {
            boolean changed = false;
            for(int tank = 0; tank < handler.getGasTankCount(); tank++)
                if(!handler.getGasInTank(tank).isEmpty()){
                    handler.extractGas(handler.getGasInTank(tank), Action.EXECUTE);
                    changed = true;
                }
            return changed;
        }).orElse(false);
    }

    @Override
    public Object getGasHandler(ArrayList<ItemFilter> filters, Supplier<Boolean> whitelist){
        return new IGasHandler() {
            @Override
            public int getGasTankCount(){
                return 1;
            }

            @Override
            public GasStack getGasInTank(int i){
                return GasStack.EMPTY;
            }

            @Override
            public void setGasInTank(int i, GasStack gasStack){
            }

            @Override
            public long getGasTankCapacity(int i){
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isGasValid(int i, GasStack gasStack){
                for(ItemFilter filter : filters){
                    if(filter != null && filter.matches(gasStack))
                        return whitelist.get();
                }
                return !whitelist.get();
            }

            @Override
            public GasStack insertGas(int i, GasStack gasStack, Action action){
                if(this.isGasValid(i, gasStack))
                    return GasStack.EMPTY;
                return gasStack;
            }

            @Override
            public GasStack extractGas(int i, long l, Action action){
                return GasStack.EMPTY;
            }
        };
    }

    @Override
    public boolean isGasStack(Object obj){
        return obj instanceof GasStack;
    }

    @Override
    public ItemStack getChemicalTankForGasStack(Object gasStack){
        ItemStack stack = new ItemStack(MekanismBlocks.CREATIVE_GAS_TANK);
        stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).ifPresent(handler -> {
            GasStack gas = ((GasStack)gasStack).copy();
            gas.setAmount(handler.getGasTankCapacity(0));
            handler.setGasInTank(0, gas);
        });
        return stack;
    }
}
