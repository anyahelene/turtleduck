package turtleduck.buffer;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import turtleduck.colors.Color;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.PositionVector;

public final class CustomVertexBuffer {

    public interface Default<T extends Base<T>>
            extends Base<T>, WithColor<T>, WithPosition<T>, WithNormal<T>, WithTexture2<T>, WithPointSize<T> {

    }

    public interface Base<T> {
        T next();

        /**
         * Set vertex attribute
         * 
         * @param location Attribute location
         * @param value    A value
         * @return this
         */
        <U> T set(VertexAttribute<U> attr, U value);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param col      A color
         * @return this
         */
        T set(int location, Color col);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param pos      A position
         * @return this
         */
        T set(int location, PositionVector pos);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param dir      A direction
         * @return this
         */
        T set(int location, DirectionVector dir);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xy       Vector value (other vector components default to 0)
         * @return this
         */
        T set(int location, Vector2fc xy);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xy       Vector value (other vector components default to 0)
         * @param z        Z component of vector
         * @return this
         */
        T set(int location, Vector2fc xy, double z);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xy       Vector value
         * @param z        Z component of vector
         * @param w        W component of vector
         * @return this
         */
        T set(int location, Vector2fc xy, double z, double w);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xyz      Vector value (other vector components default to 0)
         * @return this
         */
        T set(int location, Vector3fc xyz);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xyz      Vector value
         * @param w        W component of vector
         * @return this
         */
        T set(int location, Vector3fc xyz, double w);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param xyzw     Vector value
         * @return this
         */
        T set(int location, Vector4fc xyzw);

        /**
         * Set vertex attribute at location
         * 
         * Other vector components (if any) default to 0.
         * 
         * @param location Attribute location
         * @param x        X component of vector
         * @return this
         */
        T set(int location, double x);

        /**
         * Set vertex attribute at location
         * 
         * Other vector components (if any) default to 0.
         * 
         * 
         * @param location Attribute location
         * @param x        X component of vector
         * @param y        Y component of vector
         * @return this
         */
        T set(int location, double x, double y);

        /**
         * Set vertex attribute at location
         * 
         * Other vector components (if any) default to 0.
         * 
         * @param location Attribute location
         * @param x        X component of vector
         * @param y        Y component of vector
         * @param z        Z component of vector
         * @param w        W component of vector
         * @return this
         */
        T set(int location, double x, double y, double z);

        /**
         * Set vertex attribute at location
         * 
         * @param location Attribute location
         * @param x        X component of vector
         * @param y        Y component of vector
         * @param z        Z component of vector
         * @param w        W component of vector
         * @return this
         */
        T set(int location, double x, double y, double z, double w);
    }

    public interface WithPointSize<T extends Base<T>> {
        T pointSize(double pointSize);
    }

    public interface WithColor<T extends Base<T>> {
        /**
         * Set vertex color
         * 
         * @param col A color
         * @return this
         */
        T color(Color col);

        /**
         * Set vertex color
         * 
         * @param col Packed color, 0xAABBGGRR
         * @return this
         */
        T colorRGBA(int col);

        /**
         * Set vertex color
         * 
         * @param col Packed color, 0xAARRGGBB
         * @return this
         */
        T colorBGRA(int col);

        /**
         * Set vertex color
         * 
         * @param r Red component, 0–255
         * @param g Green component, 0–255
         * @param b Blue component, 0–255
         * @param a Alpha component, 0–255
         * @return this
         */
        T colorRGBA(int r, int g, int b, int a);

        /**
         * Set vertex color
         * 
         * Alpha defaults to 1.
         * 
         * @param rgb Color components, 0.0–1.0
         * @param a   Alpha component, 0.0–1.0
         * @return this
         */
        T color(Vector3fc rgb);

        /**
         * Set vertex color
         * 
         * @param rgb Color components, 0.0–1.0
         * @param a   Alpha component, 0.0–1.0
         * @return this
         */
        T color(Vector3fc rgb, double a);

        /**
         * Set vertex color
         * 
         * @param rgba Color components, 0.0–1.0
         * @return this
         */
        T color(Vector4fc rgba);

        /**
         * Set vertex color
         * 
         * Blue defaults to 0, alpha defaults to 1.
         * 
         * @param r Red component, 0.0–1.0
         * @param g Green component, 0.0–1.0
         * @return this
         */
        T color(double r, double g);

        /**
         * Set vertex color
         * 
         * Alpha defaults to 1.
         * 
         * @param r Red component, 0.0–1.0
         * @param g Green component, 0.0–1.0
         * @param b Blue component, 0.0–1.0
         * @return this
         */
        T color(double r, double g, double b);

        /**
         * Set vertex color
         * 
         * @param r Red component, 0.0–1.0
         * @param g Green component, 0.0–1.0
         * @param b Blue component, 0.0–1.0
         * @param a Alpha component, 0.0–1.0
         * @return this
         */
        T color(double r, double g, double b, double a);
    }

// also tangent, binormal
    public interface WithNormal<T extends Base<T>> {
        T normal(DirectionVector nrm);

        T normal(Vector2fc xy);

        T normal(Vector2fc xy, double z);

        T normal(Vector2fc xy, double z, double w);

        T normal(Vector3fc xyz);

        T normal(Vector3fc xyz, double w);

        T normal(Vector4fc xyz);

        T normal(double x, double y);

        T normal(double x, double y, double z);

        T normal(double x, double y, double z, double w);
    }

    public interface WithPosition<T extends Base<T>> {
        T position(PositionVector pos);

        T position(Vector2fc xy);

        T position(Vector2fc xy, double z);

        T position(Vector2fc xy, double z, double w);

        T position(Vector3fc xyz);

        T position(Vector3fc xyz, double w);

        T position(Vector4fc xyzw);

        T position(double x, double y);

        T position(double x, double y, double z);

        T position(double x, double y, double z, double w);
    }

    public interface WithTexture2<T extends Base<T>> {
        T texcoord(PositionVector pos);

        T texcoord(Vector2fc st);

        T texcoord(double s, double t);

    }

    public interface WithTexture4<T extends Base<T>> extends WithTexture2<T> {
        T texcoord(Vector3fc stp);

        T texcoord(Vector3fc stp, double q);

        T texcoord(Vector4fc stpq);

        T texcoord(double s, double t, double p);

        T texcoord(double s, double t, double p, double q);
    }

}
