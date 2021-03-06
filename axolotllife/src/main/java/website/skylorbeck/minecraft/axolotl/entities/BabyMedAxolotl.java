package website.skylorbeck.minecraft.axolotl.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Objects;

public class BabyMedAxolotl extends AxoBaseEntity implements IAnimatable {
    AnimationFactory factory = new AnimationFactory(this);

    public BabyMedAxolotl(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5, this::predicate));
    }


    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        if (this.isSubmergedInWater() || this.isInsideWaterOrBubbleColumn() || this.isTouchingWater() || this.isSwimming() || this.isSubmergedIn(FluidTags.WATER)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.swin", true));
        } else if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.walk", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.static", true));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void useAbility() {
        ItemStack itemStack = Items.TIPPED_ARROW.getDefaultStack();
        PotionUtil.setPotion(itemStack, Potions.AWKWARD);
        float f = 0.75f;
        if (!world.isClient) {
            PersistentProjectileEntity persistentProjectileEntity = ProjectileUtil.createArrowProjectile(Objects.requireNonNull(world.getClosestPlayer(this, 5d)),itemStack,1);
            persistentProjectileEntity.setProperties(world.getClosestPlayer(this,5d), world.getClosestPlayer(this,5d).getPitch(), world.getClosestPlayer(this,5d).getYaw(), 0.0F, f * 3.0F, 0.0F);
            persistentProjectileEntity.setCritical(true);
            persistentProjectileEntity.pickupType= PersistentProjectileEntity.PickupPermission.DISALLOWED;
            world.spawnEntity(persistentProjectileEntity);
        }
    }
}