package net.mehvahdjukaar.amendments.common.entity;

import org.joml.Matrix4f;

public interface IVisualTransformationProvider {

    Matrix4f amendments$getVisualTransformation(float partialTicks);
}
