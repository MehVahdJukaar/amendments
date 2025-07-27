package net.mehvahdjukaar.amendments.common.entity;

import org.joml.Quaternionf;

public interface IVisualRotationProvider {

    Quaternionf amendments$getVisualRotation(float partialTicks);
}
