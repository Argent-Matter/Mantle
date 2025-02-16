package slimeknights.mantle.registration;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.fluid.attributes.FluidAttributes;

import java.util.Optional;

public record FluidAttributeHandler(FluidAttributes attributes) implements FluidVariantAttributeHandler {

  @Override
  public Component getName(FluidVariant fluidVariant) {
    return attributes.getDisplayName(new FluidStack(fluidVariant, FluidConstants.BUCKET));
  }

  @Override
  public Optional<SoundEvent> getFillSound(FluidVariant variant) {
    return Optional.of(attributes.getFillSound(new FluidStack(variant, FluidConstants.BUCKET)));
  }

  @Override
  public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
    return Optional.of(attributes.getEmptySound(new FluidStack(variant, FluidConstants.BUCKET)));
  }

  @Override
  public int getLuminance(FluidVariant variant) {
    return attributes.getLuminosity(new FluidStack(variant, FluidConstants.BUCKET));
  }

  @Override
  public int getTemperature(FluidVariant variant) {
    return attributes.getTemperature(new FluidStack(variant, FluidConstants.BUCKET));
  }

  @Override
  public int getViscosity(FluidVariant variant, @Nullable Level world) {
    return attributes.getViscosity(new FluidStack(variant, FluidConstants.BUCKET));
  }

  @Override
  public boolean isLighterThanAir(FluidVariant variant) {
    return attributes.isGaseous(new FluidStack(variant, FluidConstants.BUCKET));
  }
}
